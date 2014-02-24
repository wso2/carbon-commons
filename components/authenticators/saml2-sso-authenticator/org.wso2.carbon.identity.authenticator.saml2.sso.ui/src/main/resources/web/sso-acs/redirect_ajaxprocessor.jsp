<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@page import="org.wso2.carbon.core.security.AuthenticatorsConfiguration"%>
<%@page import="org.opensaml.saml2.core.AuthnRequest" %>
<%@page import="org.opensaml.saml2.core.LogoutRequest" %>
<%@ page import="org.wso2.carbon.identity.authenticator.saml2.sso.common.SAML2SSOAuthenticatorConstants" %>
<%@ page import="org.wso2.carbon.identity.authenticator.saml2.sso.common.SSOSessionManager" %>
<%@ page import="org.wso2.carbon.identity.authenticator.saml2.sso.common.builders.AuthenticationRequestBuilder" %>
<%@ page import="org.wso2.carbon.identity.authenticator.saml2.sso.common.builders.LogoutRequestBuilder" %>
<%@ page
        import="org.wso2.carbon.identity.authenticator.saml2.sso.common.Util" %>
<%@ page import="org.wso2.carbon.registry.core.utils.UUIDGenerator" %>
<%@ page import="org.wso2.carbon.utils.multitenancy.MultitenantConstants" %>
<html>
<head></head>
<body>
<%
    String encodedReq = null;
    String relayState = "";
    String domain = null;
    if (request.getParameter(SAML2SSOAuthenticatorConstants.LOG_OUT_REQ) != null) {
        LogoutRequestBuilder logoutRequestBuilder = new LogoutRequestBuilder();
        LogoutRequest logoutReq = logoutRequestBuilder.buildLogoutRequest((String) request.getAttribute(
                SAML2SSOAuthenticatorConstants.LOGGED_IN_USER), SAML2SSOAuthenticatorConstants.LOGOUT_USER,
                (String)request.getSession().getAttribute(SAML2SSOAuthenticatorConstants.IDP_SESSION_INDEX));
        encodedReq = Util.encode(Util.marshall(logoutReq));
        relayState = UUIDGenerator.generateUUID();
    } else {
    	 AuthenticationRequestBuilder authnReqGenerator = new AuthenticationRequestBuilder();
         AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
         AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig =
                 authenticatorsConfiguration.getAuthenticatorConfig(SAML2SSOAuthenticatorConstants.AUTHENTICATOR_NAME); 
         AuthnRequest authRequest = authnReqGenerator.buildAuthenticationRequest(null,authenticatorConfig.getParameters().get(SAML2SSOAuthenticatorConstants.NAMEID_POLICY_FORMAT));;
         encodedReq = Util.encode(Util.marshall(authRequest));
         relayState = UUIDGenerator.generateUUID();
         domain = (String)request.getAttribute(MultitenantConstants.TENANT_DOMAIN);
    }
    // add the relay state to Session Manager
    SSOSessionManager.addAuthnRequest(relayState);

%>
 <p>You are now redirected to <%=Util.getIdentityProviderSSOServiceURL()%>. If the
 redirection fails, please click the post button.</p>
<form method="post" action="<%=Util.getIdentityProviderSSOServiceURL()%>">
    <p><input type="hidden" name="<%=SAML2SSOAuthenticatorConstants.HTTP_POST_PARAM_SAML2_AUTH_REQ%>"
              value="<%= encodedReq %>"/>
        <input type="hidden" name="RelayState" value="<%= relayState %>"/>
        <input type="hidden" name="<%= MultitenantConstants.TENANT_DOMAIN %>" value="<%= domain %>"/>
        <input type="hidden" name="<%= MultitenantConstants.SSO_AUTH_SESSION_ID %>" value="<%= session.getId() %>"/>
        <button type="submit">POST</button>
    </p>
</form>

<script type="text/javascript">
    document.forms[0].submit();
</script>

</body>
</html>