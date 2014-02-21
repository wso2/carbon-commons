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
package org.wso2.carbon.identity.authenticator.saml2.sso;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.validation.ValidationException;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.core.common.AuthenticationException;
import org.wso2.carbon.core.security.AuthenticatorsConfiguration;
import org.wso2.carbon.core.services.authentication.CarbonServerAuthenticator;
import org.wso2.carbon.core.services.util.CarbonAuthenticationUtil;
import org.wso2.carbon.core.util.AnonymousSessionUtil;
import org.wso2.carbon.core.util.PermissionUpdateUtil;
import org.wso2.carbon.identity.authenticator.saml2.sso.dto.AuthnReqDTO;
import org.wso2.carbon.identity.authenticator.saml2.sso.internal.SAML2SSOAuthBEDataHolder;
import org.wso2.carbon.identity.authenticator.saml2.sso.util.Util;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.AuthenticationObserver;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.opensaml.xml.XMLObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SAML2SSOAuthenticator implements CarbonServerAuthenticator {

    private static final int DEFAULT_PRIORITY_LEVEL = 3;
    private static final String AUTHENTICATOR_NAME = SAML2SSOAuthenticatorBEConstants.SAML2_SSO_AUTHENTICATOR_NAME;

    public static final Log log = LogFactory.getLog(SAML2SSOAuthenticator.class);

    public boolean login(AuthnReqDTO authDto) {
    	HttpSession httpSession = getHttpSession();
    	try {
    		XMLObject xmlObject = Util.unmarshall(org.wso2.carbon.identity.authenticator.saml2.sso.common.Util.decode(authDto.getResponse()));

    		String username = getUsername(xmlObject);

    		if ((username == null) || username.trim().equals("")) {
    			log.error("Authentication Request is rejected. " +
    					"SAMLResponse does not contain the username of the subject.");
    			CarbonAuthenticationUtil.onFailedAdminLogin(httpSession, username, -1,
    					"SAML2 SSO Authentication", "Data");
    			// Unable to call #handleAuthenticationCompleted since there is no way to determine
    			// tenantId without knowing the username.
    			return false;
    		}

    		if (!validateAudienceRestriction(xmlObject)) {
    			log.error("Authentication Request is rejected. " +
    					"SAMLResponse AudienceRestriction validation failed.");
    			CarbonAuthenticationUtil.onFailedAdminLogin(httpSession, username, -1,
    					"SAML2 SSO Authentication", "Data");
    			return false;
    		}

    		RegistryService registryService = SAML2SSOAuthBEDataHolder.getInstance().getRegistryService();
    		RealmService realmService = SAML2SSOAuthBEDataHolder.getInstance().getRealmService();
    		String tenantDomain = MultitenantUtils.getTenantDomain(username);
    		int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
    		handleAuthenticationStarted(tenantId);
    		boolean isSignatureValid = validateSignature(xmlObject, tenantDomain);
    		if(!isSignatureValid){
    			log.error("Authentication Request is rejected. " +
    					" Signature validation failed.");
    			CarbonAuthenticationUtil.onFailedAdminLogin(httpSession, username, tenantId,
    					"SAML2 SSO Authentication", "Invalid Signature");
    			handleAuthenticationCompleted(tenantId, false);
    			return false;
    		}

    		username = MultitenantUtils.getTenantAwareUsername(username);
    		UserRealm realm = AnonymousSessionUtil.getRealmByTenantDomain(registryService,
    				realmService, tenantDomain);

    		PermissionUpdateUtil.updatePermissionTree(tenantId);
    		boolean isAuthorized = realm.getAuthorizationManager().isUserAuthorized(username,
    				"/permission/admin/login", CarbonConstants.UI_PERMISSION_ACTION);
    		if (isAuthorized) {
    			CarbonAuthenticationUtil.onSuccessAdminLogin(httpSession, username,
    					tenantId, tenantDomain, "SAML2 SSO Authentication");
    			handleAuthenticationCompleted(tenantId, true);
    			return true;
    		} else {
    			log.error("Authentication Request is rejected. Authorization Failure.");
    			CarbonAuthenticationUtil.onFailedAdminLogin(httpSession, username, tenantId,
    					"SAML2 SSO Authentication", "Invalid credential");
    			handleAuthenticationCompleted(tenantId, false);
    			return false;
    		}
    	} catch (Exception e) {
    		String msg = "System error while Authenticating/Authorizing User : " + e.getMessage();
    		log.error(msg, e);
    		return false;
    	}
    }

    private void handleAuthenticationStarted(int tenantId) {
        BundleContext bundleContext = SAML2SSOAuthBEDataHolder.getInstance().getBundleContext();
        if (bundleContext != null) {
            ServiceTracker tracker =
                    new ServiceTracker(bundleContext,
                            AuthenticationObserver.class.getName(), null);
            tracker.open();
            Object[] services = tracker.getServices();
            if (services != null) {
                for (Object service : services) {
                    ((AuthenticationObserver) service).startedAuthentication(tenantId);
                }
            }
            tracker.close();
        }
    }

    private void handleAuthenticationCompleted(int tenantId, boolean isSuccessful) {
        BundleContext bundleContext = SAML2SSOAuthBEDataHolder.getInstance().getBundleContext();
        if (bundleContext != null) {
            ServiceTracker tracker =
                    new ServiceTracker(bundleContext,
                            AuthenticationObserver.class.getName(), null);
            tracker.open();
            Object[] services = tracker.getServices();
            if (services != null) {
                for (Object service : services) {
                    ((AuthenticationObserver) service).completedAuthentication(
                            tenantId, isSuccessful);
                }
            }
            tracker.close();
        }
    }

    public void logout() {
        String loggedInUser;
        String delegatedBy;
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat date = new SimpleDateFormat("'['yyyy-MM-dd HH:mm:ss,SSSS']'");
        HttpSession session = getHttpSession();

        if (session != null) {
            loggedInUser = (String) session.getAttribute(ServerConstants.USER_LOGGED_IN);
            delegatedBy = (String) session.getAttribute("DELEGATED_BY");
            if (delegatedBy == null) {
                log.info("'" + loggedInUser + "' logged out at " + date.format(currentTime));
            } else {
                log.info("'" + loggedInUser + "' logged out at " + date.format(currentTime)
                         + " delegated by " + delegatedBy);
            }
            session.invalidate();
        }
    }

    public boolean isHandle(MessageContext messageContext) {
        return true;
    }

    public boolean isAuthenticated(MessageContext messageContext) {
        HttpServletRequest request = (HttpServletRequest) messageContext
                .getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        HttpSession httpSession = request.getSession();
        String loginStatus = (String) httpSession.getAttribute(ServerConstants.USER_LOGGED_IN);

        return (loginStatus != null);
    }

    public boolean authenticateWithRememberMe(MessageContext messageContext) {
        return false;
    }

    public int getPriority() {
        AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
        AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig =
                authenticatorsConfiguration.getAuthenticatorConfig(AUTHENTICATOR_NAME);
        if (authenticatorConfig != null && authenticatorConfig.getPriority() > 0) {
            return authenticatorConfig.getPriority();
        }
        return DEFAULT_PRIORITY_LEVEL;
    }

    public String getAuthenticatorName() {
        return AUTHENTICATOR_NAME;
    }

    public boolean isDisabled() {
        AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
        AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig =
                authenticatorsConfiguration.getAuthenticatorConfig(AUTHENTICATOR_NAME);
        if (authenticatorConfig != null) {
            return authenticatorConfig.isDisabled();
        }
        return false;
    }
    
    /**
     * Validate the signature of a SAML2 XMLObject
     *
     * @param xmlObject   SAML2 XMLObject
     * @param domainName domain name of the subject
     * @return true, if signature is valid.
     * @throws AuthenticationException
     */
    private boolean validateSignature(XMLObject xmlObject, String domainName) {
    	if(xmlObject instanceof Response){
    		return validateSignature((Response) xmlObject, domainName);
    	} else if (xmlObject instanceof Assertion){
    		return validateSignature((Assertion) xmlObject, domainName);
    	} else {
    		log.error("Only Response and Assertion objects are validated in this authendicator");
    		return false;
    	}
    }

    /**
     * Validate the signature of a SAML2 Response
     *
     * @param response   SAML2 Response
     * @param domainName domain name of the subject
     * @return true, if signature is valid.
     * @throws AuthenticationException
     */
    private boolean validateSignature(Response response, String domainName) {
    	boolean isSignatureValid = false;
    	if(response.getSignature() == null){
    		log.error("SAML Response is not signed. So authentication process will be terminated.");
    	}
    	else {
    		isSignatureValid = validateSignature(response.getSignature(), domainName);
    	}
    	return isSignatureValid;
    }

    /**
     * Validate the signature of a SAML2 Assertion
     *
     * @param response   SAML2 Response
     * @param domainName domain name of the subject
     * @return true, if signature is valid.
     * @throws AuthenticationException
     */
    private boolean validateSignature(Assertion assertion, String domainName) {
    	boolean isSignatureValid = false;
    	if(assertion.getSignature() == null){
    		log.error("SAML Assertion is not signed. So authentication process will be terminated.");
    	}
    	else {
    		isSignatureValid = validateSignature(assertion.getSignature(), domainName);
    	}
    	return isSignatureValid;
    }

    /**
     * Validate the signature of a SAML2 Signature
     *
     * @param signature   SAML2 Signature
     * @param domainName domain name of the subject
     * @return true, if signature is valid.
     * @throws AuthenticationException
     */
    private boolean validateSignature(Signature signature, String domainName){
    	boolean isSignatureValid = false;
    	try {
    		SignatureValidator validator = new SignatureValidator(Util.getX509CredentialImplForTenant(domainName));
    		validator.validate(signature);
    		isSignatureValid = true;
    	} catch (SAML2SSOAuthenticatorException e) {
    		String errorMsg = "Error when creating an X509CredentialImpl instance";
    		log.error(errorMsg, e);
    	} catch (ValidationException e) {
    		log.warn("Signature validation failed for a SAML2 Reposnse from domain : " + domainName);
    	}
    	return isSignatureValid;
    }

    /**
     * Get the username from the SAML2 XMLObject
     *
     * @param xmlObject SAML2 XMLObject
     * @return username
     */
    private String getUsername(XMLObject xmlObject) {
    	if(xmlObject instanceof Response){
    		return getUsernameFromResponse((Response) xmlObject);
    	} else if (xmlObject instanceof Assertion){
    		return getUsernameFromAssertion((Assertion) xmlObject);
    	} else {
    		return null;
    	}
    }

    /**
     * Get the username from the SAML2 Response
     *
     * @param response SAML2 Response
     * @return username
     */
    private String getUsernameFromResponse(Response response) {
    	List<Assertion> assertions = response.getAssertions();
    	Assertion assertion = null;
    	if (assertions != null && assertions.size() > 0) {
    		assertion = assertions.get(0);
    		return getUsernameFromAssertion(assertion);
    	}
    	return null;
    }

    /**
     * Get the username from the SAML2 Assertion
     *
     * @param assertion SAML2 assertion
     * @return username
     */
    private String getUsernameFromAssertion(Assertion assertion) {
    	return assertion.getSubject().getNameID().getValue();
    }

    /**
     * Get the Assertion from the SAML2 Response
     * 
     * @param response SAML2 Response
     * @return assertion
     */
    private Assertion getAssertionFromResponse(Response response) {
    	Assertion assertion = null;
    	if (response != null){
    		List<Assertion> assertions = response.getAssertions();
    		if (assertions != null && assertions.size() > 0) {
    			assertion = assertions.get(0);
    		} else {
    			log.error("SAML2 Response doesn't contain Assertions");
    		}
    	}
    	return assertion;
    }

    /**
     * Validate the AudienceRestriction of SAML2 XMLObject 
     * 
     * @param assertion SAML2 Assertion
     * @return validity
     */
    private boolean validateAudienceRestriction(XMLObject xmlObject) {
    	if(xmlObject instanceof Response){
    		return validateAudienceRestriction((Response) xmlObject);
    	} else if (xmlObject instanceof Assertion){
    		return validateAudienceRestriction((Assertion) xmlObject);
    	} else {
    		log.error("Only Response and Assertion objects are validated in this authendicator");
    		return false;
    	}
    }

    /**
     * Validate the AudienceRestriction of SAML2 Response 
     * 
     * @param assertion SAML2 Assertion
     * @return validity
     */
    public boolean validateAudienceRestriction(Response response) {
    	Assertion assertion = getAssertionFromResponse(response);
    	return validateAudienceRestriction(assertion);
    }

    /**
     * Validate the AudienceRestriction of SAML2 Assertion 
     * 
     * @param assertion SAML2 Assertion
     * @return validity
     */
    public boolean validateAudienceRestriction(Assertion assertion) {
    	if (assertion != null) {
    		Conditions conditions = assertion.getConditions();
    		if (conditions != null) {
    			List<AudienceRestriction> audienceRestrictions = conditions.getAudienceRestrictions();
    			if (audienceRestrictions != null && !audienceRestrictions.isEmpty()) {
    				for (AudienceRestriction audienceRestriction : audienceRestrictions) {
    					if (audienceRestriction.getAudiences() != null && audienceRestriction.getAudiences().size() > 0) {
    						for (Audience audience : audienceRestriction.getAudiences()) {
    							String spId = org.wso2.carbon.identity.authenticator.saml2.sso.common.Util.getServiceProviderId();
    							if (spId==null){
    								org.wso2.carbon.identity.authenticator.saml2.sso.common.Util.initSSOConfigParams();
    								spId = org.wso2.carbon.identity.authenticator.saml2.sso.common.Util.getServiceProviderId();
    							}
    							if (spId != null) {
    								if (spId.equals(audience.getAudienceURI())) {
    									return true;
    								}
    							} else {
    								log.warn("No SAML2 service provider ID defined.");
    							}
    						}
    					} else {
    						log.warn("SAML2 Response's AudienceRestriction doesn't contain Audiences");
    					}
    				}
    			} else {
    				log.error("SAML2 Response doesn't contain AudienceRestrictions");
    			}
    		} else {
    			log.error("SAML2 Response doesn't contain Conditions");
    		}
    	} 
    	return false;
    }

    private HttpSession getHttpSession() {
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        HttpSession httpSession = null;
        if (msgCtx != null) {
            HttpServletRequest request =
                    (HttpServletRequest) msgCtx.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
            httpSession = request.getSession();
        }
        return httpSession;
    }

}
