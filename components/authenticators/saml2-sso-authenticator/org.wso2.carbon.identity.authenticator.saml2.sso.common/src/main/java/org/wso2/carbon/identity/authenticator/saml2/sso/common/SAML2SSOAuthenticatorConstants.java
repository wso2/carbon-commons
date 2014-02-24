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
package org.wso2.carbon.identity.authenticator.saml2.sso.common;

public final class SAML2SSOAuthenticatorConstants {
    public static final String AUTHENTICATOR_NAME = "SAML2SSOAuthenticator";

    public static final String SAML2_NAME_ID_POLICY_TRANSIENT = "urn:oasis:names:tc:SAML:2.0:nameid-format:transient";
    public static final String SAML2_NAME_ID_POLICY_UNSPECIFIED = "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified";
   
    public static final String SAML2_NAME_ID_POLICY_PERSISTENT = "urn:oasis:names:tc:SAML:2.0:nameid-format:persistent";    
    public static final String SAML2_NAME_ID_POLICY_EMAIL = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
    public static final String SAML2_NAME_ID_POLICY_ENTITY = "urn:oasis:names:tc:SAML:2.0:nameid-format:entity";

    public static final String NAMEID_POLICY_FORMAT = "NameIDPolicyFormat";
   

    public static final String LOGOUT_USER = "urn:oasis:names:tc:SAML:2.0:logout:user";

    public static final String HTTP_POST_PARAM_SAML2_AUTH_REQ = "SAMLRequest";
    public static final String HTTP_POST_PARAM_SAML2_RESP = "SAMLResponse";
    public static final String HTTP_POST_PARAM_RELAY_STATE = "RelayState";
    public static final String HTTP_ATTR_SAML2_RESP_TOKEN = "SAML2ResponseToken";
    public static final String HTTP_ATTR_IS_LOGOUT_REQ = "logoutRequest";

    public static final String NOTIFICATIONS_ERROR_CODE = "ErrorCode";
    public static final String NOTIFICATIONS_ERROR_MSG = "ErrorMessage";
    public static final String LOG_OUT_REQ = "logout";
    public static final String LOGGED_IN_USER = "loggedInUser";
    
    public static final String IDP_SESSION_INDEX = "idpSessionIndex";
    
    // SSO Configuration Params
    public static final String SERVICE_PROVIDER_ID = "ServiceProviderID";
    public static final String IDENTITY_PROVIDER_SSO_SERVICE_URL = "IdentityProviderSSOServiceURL";
    public static final String LOGIN_PAGE = "LoginPage";
    public static final String LANDING_PAGE = "LandingPage";
    public static final String EXTERNAL_LOGOUT_PAGE = "ExternalLogoutPage";
    public static final String FEDERATION_CONFIG = "FederationConfig";
    public static final String FEDERATION_CONFIG_USER = "FederationConfigUser";
    public static final String FEDERATION_CONFIG_PASSWORD = "FederationConfigPassword";



    public static final class ErrorMessageConstants{
        public static final String RESPONSE_NOT_PRESENT = "response.not.present";
        public static final String RESPONSE_INVALID = "response.invalid";
        public static final String RESPONSE_MALFORMED = "response.malformed";
        public static final String SUCCESSFUL_SIGN_OUT = "successful.signed.out";
    }
}
