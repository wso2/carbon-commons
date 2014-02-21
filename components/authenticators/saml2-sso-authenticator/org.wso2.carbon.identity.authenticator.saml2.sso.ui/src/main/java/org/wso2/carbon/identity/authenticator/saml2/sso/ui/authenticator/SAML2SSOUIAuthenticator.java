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
package org.wso2.carbon.identity.authenticator.saml2.sso.ui.authenticator;

import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Response;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.common.AuthenticationException;
import org.wso2.carbon.core.security.AuthenticatorsConfiguration;
import org.wso2.carbon.identity.authenticator.saml2.sso.common.SAML2SSOAuthenticatorConstants;
import org.wso2.carbon.identity.authenticator.saml2.sso.common.SAML2SSOUIAuthenticatorException;
import org.wso2.carbon.identity.authenticator.saml2.sso.common.client.SAML2SSOAuthenticationClient;
import org.wso2.carbon.identity.authenticator.saml2.sso.common.Util;
import org.wso2.carbon.identity.authenticator.saml2.sso.ui.internal.SAML2SSOAuthFEDataHolder;
import org.wso2.carbon.ui.AbstractCarbonUIAuthenticator;
import org.wso2.carbon.ui.CarbonSSOSessionManager;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

public class SAML2SSOUIAuthenticator extends AbstractCarbonUIAuthenticator {

    private static final int DEFAULT_PRIORITY_LEVEL = 50;

    public static final Log log = LogFactory.getLog(SAML2SSOUIAuthenticator.class);

    public boolean canHandle(HttpServletRequest request) {
        String relayState = request.getParameter(SAML2SSOAuthenticatorConstants.HTTP_POST_PARAM_RELAY_STATE);
        Object samlResponse = request.getAttribute(SAML2SSOAuthenticatorConstants.HTTP_ATTR_SAML2_RESP_TOKEN);
        // if it is a logout request, do not check for Response and Relay State
        if (request.getRequestURI().indexOf("/carbon/admin/logout_action.jsp") > -1) {
            return true;
        }
        // in case of a login request, check for Response and Relay State
        if (samlResponse != null && samlResponse instanceof Response && relayState != null) {
            return true;
        }
        return false;
    }

    public void authenticate(HttpServletRequest request) throws AuthenticationException {
        boolean isAuthenticated = false;
        HttpSession session = request.getSession();
        Response samlResponse = (Response) request.getAttribute(SAML2SSOAuthenticatorConstants.HTTP_ATTR_SAML2_RESP_TOKEN);
        String responseStr = request.getParameter(SAML2SSOAuthenticatorConstants.HTTP_POST_PARAM_SAML2_RESP);
        String username = getUsernameFromResponse(samlResponse);
        ServletContext servletContext = request.getSession().getServletContext();
        ConfigurationContext configContext = (ConfigurationContext) servletContext.getAttribute(
                CarbonConstants.CONFIGURATION_CONTEXT);
        String backEndServerURL = request.getParameter("backendURL");
        if (backEndServerURL == null) {
            backEndServerURL = CarbonUIUtil.getServerURL(servletContext, session);
        }
        session.setAttribute(CarbonConstants.SERVER_URL, backEndServerURL);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_AUTH_TOKEN);

        // authorize the user with the back-end
        SAML2SSOAuthenticationClient authenticationClient = null;
        try {
            if(log.isDebugEnabled()){
                log.debug("Invoking the SAML2 SSO Authenticator BE for the Response : " + responseStr);
            }
            authenticationClient = new SAML2SSOAuthenticationClient(
                    configContext, backEndServerURL, cookie, session);
            isAuthenticated = authenticationClient.login(responseStr, username);

            // add an entry to CarbonSSOSessionManager : IdpSessionIndex --> localSessionId
            if (isAuthenticated) {
                CarbonSSOSessionManager ssoSessionManager =
                        SAML2SSOAuthFEDataHolder.getInstance().getCarbonSSOSessionManager();
                String sessionId = getSessionIndexFromResponse(samlResponse);
                if (sessionId != null) {
                    // Session id is provided only when Single Logout enabled at the IdP.
                    ssoSessionManager.addSessionMapping(getSessionIndexFromResponse(samlResponse),
                            session.getId());
                    request.getSession().setAttribute(SAML2SSOAuthenticatorConstants.IDP_SESSION_INDEX, sessionId);
                }
                onSuccessAdminLogin(request, username);
            }
            else{
                log.error("Authentication failed.");
            }
        } catch (SAML2SSOUIAuthenticatorException e) {
            log.error("Error when authenticating the user : " + username, e);
            throw new AuthenticationException("Error when authenticating the user : " + username, e);
        }
        catch (Exception e) {
            log.error("Error when creating SAML2SSOAuthenticationClient.", e);
            throw new AuthenticationException("Error when creating SAML2SSOAuthenticationClient.", e);
        }
        if (!isAuthenticated) {
            throw new AuthenticationException("Authentication failure " + username);
        }
    }

    public void unauthenticate(Object o) throws Exception {
        HttpServletRequest request = (HttpServletRequest) o;
        HttpSession session = request.getSession();
        String username = (String) session.getAttribute(CarbonConstants.LOGGED_USER);
        ServletContext servletContext = session.getServletContext();
        ConfigurationContext configContext = (ConfigurationContext) servletContext
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

        String backendServerURL = CarbonUIUtil.getServerURL(servletContext, session);
        try {
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_AUTH_TOKEN);
            SAML2SSOAuthenticationClient authClient = new SAML2SSOAuthenticationClient(configContext,
                                                                                       backendServerURL,
                                                                                       cookie,
                                                                                       session);
            authClient.logout(session);
            log.info(username + "@" + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain() +" successfully logged out");
        } catch (Exception ignored) {
            String msg = "Configuration context is null.";
            log.error(msg);
            throw new Exception(msg);
        }

//        // memory cleanup : remove the invalid session from the invalid session list at the SSOSessionManager
//        CarbonSSOSessionManager ssoSessionManager =
//                    SAML2SSOAuthFEDataHolder.getInstance().getCarbonSSOSessionManager();
//        ssoSessionManager.removeInvalidSession(session.getId());

        // this attribute is used to avoid generate the logout request
        request.setAttribute(SAML2SSOAuthenticatorConstants.HTTP_ATTR_IS_LOGOUT_REQ, Boolean.valueOf(true));
        request.setAttribute(SAML2SSOAuthenticatorConstants.LOGGED_IN_USER, session.getAttribute(
                "logged-user"));
        
        request.setAttribute(SAML2SSOAuthenticatorConstants.EXTERNAL_LOGOUT_PAGE, Util.getExternalLogoutPage());
    }

    public int getPriority() {
        AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
        AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig =
                authenticatorsConfiguration.getAuthenticatorConfig(SAML2SSOAuthenticatorConstants.AUTHENTICATOR_NAME);
        if (authenticatorConfig != null && authenticatorConfig.getPriority() > 0) {
            return authenticatorConfig.getPriority();
        }
        return DEFAULT_PRIORITY_LEVEL;
    }

    public String getAuthenticatorName() {
        return SAML2SSOAuthenticatorConstants.AUTHENTICATOR_NAME;
    }

    public boolean isDisabled() {
        AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
        AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig =
                authenticatorsConfiguration.getAuthenticatorConfig(SAML2SSOAuthenticatorConstants.AUTHENTICATOR_NAME);
        if (authenticatorConfig != null) {
            return authenticatorConfig.isDisabled();
        }
        return false;
    }

    /**
     * Get the username from the SAML2 Response
     *
     * @param response SAML2 Response
     * @return username username contained in the SAML Response
     */
    private String getUsernameFromResponse(Response response) {
        List<Assertion> assertions = response.getAssertions();
        Assertion assertion = null;
        if (assertions != null && assertions.size() > 0) {
            // There can be only one assertion in a SAML Response, so get the first one
            assertion = assertions.get(0);
            return assertion.getSubject().getNameID().getValue();
        }
        return null;
    }

    /**
     * Read the session index from a Response
     *
     * @param response SAML Response
     * @return Session Index value contained in the Response
     */
    private String getSessionIndexFromResponse(Response response) {
        List<Assertion> assertions = response.getAssertions();
        String sessionIndex = null;
        if (assertions != null && assertions.size() > 0) {
            // There can be only one assertion in a SAML Response, so get the first one
            List<AuthnStatement> authnStatements = assertions.get(0).getAuthnStatements();
            if (authnStatements != null && authnStatements.size() > 0) {
                // There can be only one authentication stmt inside the SAML assertion of a SAML Response
                AuthnStatement authStmt = authnStatements.get(0);
                sessionIndex = authStmt.getSessionIndex();
            }
        }
        return sessionIndex;
    }
    
    public boolean reAuthenticateOnSessionExpire(Object object) throws AuthenticationException {
        return false;
    }


	@Override
	public void authenticateWithCookie(HttpServletRequest request)
			throws AuthenticationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	 public String doAuthentication(Object credentials, boolean isRememberMe, ServiceClient client,
	            HttpServletRequest request) throws AuthenticationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleRememberMe(Map transportHeaders, HttpServletRequest httpServletRequest)
			throws AuthenticationException {
		// TODO Auto-generated method stub
		
	}
}
