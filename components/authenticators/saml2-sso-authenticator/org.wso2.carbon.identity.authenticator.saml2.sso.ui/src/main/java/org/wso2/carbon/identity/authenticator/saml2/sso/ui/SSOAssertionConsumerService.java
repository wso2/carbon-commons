/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.identity.authenticator.saml2.sso.ui;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.LogoutResponse;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.SessionIndex;
import org.opensaml.saml2.core.Subject;
import org.opensaml.xml.XMLObject;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.authenticator.saml2.sso.common.FederatedSSOToken;
import org.wso2.carbon.identity.authenticator.saml2.sso.common.SAML2SSOAuthenticatorConstants;
import org.wso2.carbon.identity.authenticator.saml2.sso.common.SAML2SSOUIAuthenticatorException;
import org.wso2.carbon.identity.authenticator.saml2.sso.common.SAMLConstants;
import org.wso2.carbon.identity.authenticator.saml2.sso.common.SSOSessionManager;
import org.wso2.carbon.identity.authenticator.saml2.sso.common.Util;
import org.wso2.carbon.identity.authenticator.saml2.sso.ui.authenticator.SAML2SSOUIAuthenticator;
import org.wso2.carbon.identity.authenticator.saml2.sso.ui.client.SAMLSSOServiceClient;
import org.wso2.carbon.identity.authenticator.saml2.sso.ui.internal.SAML2SSOAuthFEDataHolder;
import org.wso2.carbon.identity.sso.saml.stub.IdentityException;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOAuthnReqDTO;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOReqValidationResponseDTO;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSORespDTO;
import org.wso2.carbon.ui.CarbonSSOSessionManager;
import org.wso2.carbon.ui.CarbonSecuredHttpContext;
import org.wso2.carbon.ui.CarbonUIAuthenticator;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * 
 */
public class SSOAssertionConsumerService extends HttpServlet {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 5451353570561170887L;
	public static final Log log = LogFactory.getLog(SSOAssertionConsumerService.class);
	
	  /**
     * session timeout happens in 10 hours
     */
    private static final int SSO_SESSION_EXPIRE = 36000;
    public static final String SSO_TOKEN_ID = "ssoTokenId";


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String samlRespString = req.getParameter(
                SAML2SSOAuthenticatorConstants.HTTP_POST_PARAM_SAML2_RESP);

		if (log.isDebugEnabled()) {
			log.debug("Processing SAML Response");
		}

        // Handle single logout requests
        if (req.getParameter(SAML2SSOAuthenticatorConstants.HTTP_POST_PARAM_SAML2_AUTH_REQ) != null) {
            handleSingleLogoutRequest(req, resp);
            return;
        }

        // If SAML Response is not present in the redirected req, send the user to an error page.
        if (samlRespString == null) {
            log.error("SAML Response is not present in the request.");
            handleMalformedResponses(req, resp,
                                     SAML2SSOAuthenticatorConstants.ErrorMessageConstants.RESPONSE_NOT_PRESENT);
            return;
        }

//        // If RELAY-STATE is invalid, redirect the users to an error page.
//        if (!SSOSessionManager.isValidResponse(relayState)) {
//            handleMalformedResponses(req, resp,
//                                     SAML2SSOAuthenticatorConstants.ErrorMessageConstants.RESPONSE_INVALID);
//            return;
//        }

        // Handle valid messages, either SAML Responses or LogoutRequests
        try {
            XMLObject samlObject = Util.unmarshall(Util.decode(samlRespString));
            if (samlObject instanceof LogoutResponse) {   // if it is a logout response, redirect it to login page.
                resp.sendRedirect(getAdminConsoleURL(req) + "admin/logout_action.jsp?logoutcomplete=true");
            } else if (samlObject instanceof Response) {    // if it is a SAML Response
                handleSAMLResponses(req, resp, samlObject);
            }
        } catch (SAML2SSOUIAuthenticatorException e) {
            log.error("Error when processing the SAML Assertion in the request.", e);
            handleMalformedResponses(req, resp, SAML2SSOAuthenticatorConstants.ErrorMessageConstants.RESPONSE_MALFORMED);
        } catch (IdentityException e) {
        	 log.error("Error when processing the Federated SAML Assertion in the request.", e);
             handleMalformedResponses(req, resp, SAML2SSOAuthenticatorConstants.ErrorMessageConstants.RESPONSE_MALFORMED);
		}
    }

    /**
     * Handle SAML Responses and authenticate.
     *
     * @param req   HttpServletRequest
     * @param resp  HttpServletResponse
     * @param samlObject    SAML Response object
     * @throws ServletException Error when redirecting
     * @throws IOException  Error when redirecting
     * @throws IdentityException 
     */
    private void handleSAMLResponses(HttpServletRequest req, HttpServletResponse resp,
                                     XMLObject samlObject)
            throws ServletException, IOException, SAML2SSOUIAuthenticatorException, IdentityException {
        Response samlResponse;
        samlResponse = (Response) samlObject;
        List<Assertion> assertions = samlResponse.getAssertions();
        Assertion assertion = null;
        if (assertions != null && assertions.size() > 0) {
            assertion = assertions.get(0);
        }

		if (assertion == null) {
			if (samlResponse.getStatus() != null &&
			    samlResponse.getStatus().getStatusMessage() != null) {
				log.error(samlResponse.getStatus().getStatusMessage().getMessage());
			} else {
				log.error("SAML Assertion not found in the Response.");
			}
			throw new SAML2SSOUIAuthenticatorException("SAML Authentication Failed.");
		}

        // Get the subject name from the Response Object and forward it to login_action.jsp
        String username = null;
        if(assertion.getSubject() != null && assertion.getSubject().getNameID() != null){
            username = assertion.getSubject().getNameID().getValue();
        }
        
        if(log.isDebugEnabled()){
            log.debug("A username is extracted from the response. : " + username);
        }

        if(username == null){
            log.error("SAMLResponse does not contain the name of the subject");
            throw new SAML2SSOUIAuthenticatorException("SAMLResponse does not contain the name of the subject");
        }
        
		String relayState = req.getParameter(SAMLConstants.RELAY_STATE);
		boolean isFederated = false;

		if (relayState != null) {
			FederatedSSOToken federatedSSOToken = SSOSessionManager
					.getFederatedToken(relayState);
			if (federatedSSOToken != null) {
				isFederated = true;
				HttpServletRequest fedRequest = federatedSSOToken.getHttpServletRequest();
				HttpServletResponse fedResponse = federatedSSOToken.getHttpServletResponse();

				String samlRequest = fedRequest.getParameter("SAMLRequest");
				String authMode = SAMLConstants.AuthnModes.USERNAME_PASSWORD;
				String fedRelayState = fedRequest.getParameter(SAMLConstants.RELAY_STATE);
				String rpSessionId = fedRequest.getParameter(MultitenantConstants.SSO_AUTH_SESSION_ID);
								 
				Enumeration<String> e = fedRequest.getAttributeNames();

				while (e.hasMoreElements()) {
					String name = e.nextElement();
					req.setAttribute(name, fedRequest.getAttribute(name));
				}
				
				Cookie[] cookies = fedRequest.getCookies();

				if (cookies != null) {
					for (int i = 0; i < cookies.length; i++) {
						resp.addCookie(cookies[i]);
					}
				}

				HttpSession session = fedRequest.getSession();

		        // Use sessionID as the tokenID, if cookie is not set.
		        String ssoTokenID = session.getId();
		        Cookie tokenCookie = getSSOTokenCookie(fedRequest);
		        if (tokenCookie != null) {
		            ssoTokenID = tokenCookie.getValue();
		        }

				handleFederatedSAMLRequest(req, resp, ssoTokenID, samlRequest, fedRelayState, authMode,assertion.getSubject(),rpSessionId);
			}
		}
        
		if (!isFederated) {
			// Set the SAML2 Response as a HTTP Attribute, so it is not required to build the
			// assertion again.
			req.setAttribute(SAML2SSOAuthenticatorConstants.HTTP_ATTR_SAML2_RESP_TOKEN,
					samlResponse);

			String url = req.getRequestURI();
			url = url.replace("acs",
					"carbon/admin/login_action.jsp?username=" + URLEncoder.encode(username));
			RequestDispatcher reqDispatcher = req.getRequestDispatcher(url);
			req.getSession().setAttribute("CarbonAuthenticator", new SAML2SSOUIAuthenticator());
			reqDispatcher.forward(req, resp);
		}
    }

    /**
     * Handle malformed Responses.
     *
     * @param req   HttpServletRequest
     * @param resp  HttpServletResponse
     * @param errorMsg  Error message to be displayed in HttpServletResponse.jsp
     * @throws IOException  Error when redirecting
     */
    private void handleMalformedResponses(HttpServletRequest req, HttpServletResponse resp,
                                          String errorMsg) throws IOException {
        HttpSession session = req.getSession();
        session.setAttribute(SAML2SSOAuthenticatorConstants.NOTIFICATIONS_ERROR_MSG, errorMsg);
        resp.sendRedirect(getAdminConsoleURL(req) + "sso-acs/notifications.jsp");
        return;
    }


    /**
     * This method is used to handle the single logout requests sent by the Identity Provider
     *
     * @param req  Corresponding HttpServletRequest
     * @param resp Corresponding HttpServletResponse
     */
    private void handleSingleLogoutRequest(HttpServletRequest req, HttpServletResponse resp) {
        String logoutReqStr = decodeHTMLCharacters(req.getParameter(
                SAML2SSOAuthenticatorConstants.HTTP_POST_PARAM_SAML2_AUTH_REQ));
        CarbonSSOSessionManager ssoSessionManager = null;
        XMLObject samlObject = null;

        try {
            ssoSessionManager = SAML2SSOAuthFEDataHolder.getInstance().getCarbonSSOSessionManager();
            samlObject = Util.unmarshall(Util.decode(logoutReqStr));
        } catch (SAML2SSOUIAuthenticatorException e) {
            log.error("Error handling the single logout request", e);
        }

        if (samlObject instanceof LogoutRequest) {
            LogoutRequest logoutRequest = (LogoutRequest) samlObject;
            //  There can be only one session index entry.
            List<SessionIndex> sessionIndexList = logoutRequest.getSessionIndexes();
            if (sessionIndexList.size() > 0) {
                // mark this session as invalid.
                ssoSessionManager.makeSessionInvalid(sessionIndexList.get(0).getSessionIndex());
                clearHttpSession(req);
            }
        }
    }
    
    /**
     * Clear session cookies and parameters
     */
    private void clearHttpSession(HttpServletRequest req) {
    	
    	HttpSession session = req.getSession(false);
    	String username = (String) session.getAttribute(CarbonSecuredHttpContext.LOGGED_USER);
    	log.info("Invalidating session for user " + username);
    	
    	// invalidating backend session
    	try {
    		CarbonUIAuthenticator authenticator =
    				(CarbonUIAuthenticator) session.getAttribute(CarbonSecuredHttpContext.CARBON_AUTHNETICATOR);
    		if (authenticator != null) {
    			authenticator.unauthenticate(req);
    			log.debug("Backend session invalidated");
    		}
    	} catch (Exception e) {
    		log.error(e.getMessage());
    	}
    	
    	// clearing front end session
		session.setAttribute("authenticated", false);
		session.removeAttribute(CarbonSecuredHttpContext.LOGGED_USER);
        session.getServletContext().removeAttribute(CarbonSecuredHttpContext.LOGGED_USER);
        
       /* Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("requestedURI")) {
                    cookie.setValue(null);
                }
            }
        }*/
		
		try {
            session.invalidate();
        } catch (Exception ignored) {
        	log.error(ignored.getMessage());
        }
		
		if (log.isDebugEnabled()) {
			log.debug("Cleared authenticated session " + session.getId());
		}
    	
    }

    /**
     * Get the admin console url from the request.
     *
     * @param request httpServletReq that hits the ACS Servlet
     * @return Admin Console URL       https://10.100.1.221:8443/acs/carbon/
     */
    private String getAdminConsoleURL(HttpServletRequest request) {
        String url = CarbonUIUtil.getAdminConsoleURL(request);
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        if (url.indexOf("/acs") != -1) {
            url = url.replace("/acs", "");
        }
        return url;
    }

    /**
     * A utility method to decode an HTML encoded string
     *
     * @param encodedStr encoded String
     * @return decoded String
     */
    private String decodeHTMLCharacters(String encodedStr) {
        return encodedStr.replaceAll("&amp;", "&").replaceAll("&lt;", "<").replaceAll("&gt;", ">")
                .replaceAll("&quot;", "\"").replaceAll("&apos;", "'");

    }
    
	private void handleFederatedSAMLRequest(HttpServletRequest req, HttpServletResponse resp,
	                                        String ssoTokenID, String samlRequest,
	                                        String relayState, String authMode, Subject subject,
	                                        String rpSessionId) throws IdentityException,
	                                                           IOException, ServletException {
		// Instantiate the service client.
		HttpSession session = req.getSession();
		String serverURL = CarbonUIUtil.getServerURL(session.getServletContext(), session);
		ConfigurationContext configContext =
		                                     (ConfigurationContext) session.getServletContext()
		                                                                   .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
		SAMLSSOServiceClient ssoServiceClient = new SAMLSSOServiceClient(serverURL, configContext);
		
		String method = req.getMethod();
		boolean isPost = false;
		
		if ("post".equalsIgnoreCase(method)) {
			isPost = true;
		}
		
		SAMLSSOReqValidationResponseDTO signInRespDTO =
		                                                ssoServiceClient.validate(samlRequest,
		                                                                          null, ssoTokenID,
		                                                                          rpSessionId,
		                                                                          authMode,isPost);
		if (signInRespDTO.getValid()) {
			handleRequestFromLoginPage(req, resp, ssoTokenID,
			                           signInRespDTO.getAssertionConsumerURL(),
			                           signInRespDTO.getId(), signInRespDTO.getIssuer(),
			                           subject.getNameID().getValue(), subject.getNameID()
			                                                                  .getValue(),
			                           signInRespDTO.getRpSessionId(),
			                           signInRespDTO.getRequestMessageString(), relayState);
		}
	}
	
	private void handleRequestFromLoginPage(HttpServletRequest req, HttpServletResponse resp,
			String ssoTokenID,String assertionConsumerUrl, String id, String issuer, String userName, String subject,
			String rpSession, String requestMsgString,String relayState) throws IdentityException, IOException,
			ServletException {
		HttpSession session = req.getSession();

		// instantiate the service client
		String serverURL = CarbonUIUtil.getServerURL(session.getServletContext(), session);
		ConfigurationContext configContext = (ConfigurationContext) session.getServletContext()
				.getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
		SAMLSSOServiceClient ssoServiceClient = new SAMLSSOServiceClient(serverURL, configContext);

		// Create SAMLSSOAuthnReqDTO using the request Parameters
		SAMLSSOAuthnReqDTO authnReqDTO = new SAMLSSOAuthnReqDTO();
		
		authnReqDTO.setAssertionConsumerURL(assertionConsumerUrl);
		authnReqDTO.setId(id);
		authnReqDTO.setIssuer(issuer);
		authnReqDTO.setUsername(userName);
		authnReqDTO.setPassword("federated_idp_login");
		authnReqDTO.setSubject(subject);
		authnReqDTO.setRpSessionId(rpSession);
		authnReqDTO.setRequestMessageString(requestMsgString);
		
		// authenticate the user
		SAMLSSORespDTO authRespDTO = ssoServiceClient.authenticate(authnReqDTO, ssoTokenID);

		if (authRespDTO.getSessionEstablished()) { // authentication is SUCCESSFUL
		   // Store the cookie
			storeSSOTokenCookie(ssoTokenID, req, resp);
			// add relay state, assertion string and ACS URL as request parameters.
            req.setAttribute(SAMLConstants.RELAY_STATE, relayState);
            req.setAttribute(SAMLConstants.ASSERTION_STR, authRespDTO.getRespString());
            req.setAttribute(SAMLConstants.ASSRTN_CONSUMER_URL, authRespDTO.getAssertionConsumerURL());
            req.setAttribute(SAMLConstants.SUBJECT, authRespDTO.getSubject());
			RequestDispatcher reqDispatcher = req.getRequestDispatcher("/carbon/sso-acs/federation_ajaxprocessor.jsp");
            reqDispatcher.forward(req, resp);
            return;
		}
	}

	private void storeSSOTokenCookie(String ssoTokenID, HttpServletRequest req,
			HttpServletResponse resp) {
		Cookie ssoTokenCookie = getSSOTokenCookie(req);
		if (ssoTokenCookie == null) {
			ssoTokenCookie = new Cookie(SSO_TOKEN_ID, ssoTokenID);
		}
		ssoTokenCookie.setMaxAge(SSO_SESSION_EXPIRE);
		resp.addCookie(ssoTokenCookie);
	}
	
	private Cookie getSSOTokenCookie(HttpServletRequest req) {
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("ssoTokenId")) {
					return cookie;
				}
			}
		}
		return null;
	}
}
