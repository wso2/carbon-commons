/*
* Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.idp.mgt.ui.bean;

import org.wso2.carbon.idp.mgt.ui.util.IdentityProviderMgtUIUtil;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

public class TrustedIdPBean {

    /**
     * The trusted IdP's unique name for this tenant.
     */
    private String idPName;

    /**
     * If this IdP is the primary IdP for this tenant.
     */
    private boolean primary;

    /*
     * The trusted IdP's home realm identifier
     */
    private String homeRealmId;

    /**
     * The trusted IdP's Certificate for this tenant
     */
    private CertData certData;

    /**
     * The trusted IdP's claims for this tenant.
     */
    private List<String> claims;

    /**
     * The trusted IdP's claim mappings for this tenant.
     */
    private Map<String,String> claimMappings;

    /**
     * The trusted IdP's roles for this tenant.
     */
    private List<String> roles;

    /**
     * The trusted IdP's roles for this tenant.
     */
    private Map<String,String> roleMappings;

    /**
     * The alias by which the trusted Identity Provider
     * identifies the token endpoint of this authorization server.
     */
    private String tokenEndpointAlias;

    /**
     * If SAML2 Web SSO is enabled for this IdP.
     */
    private boolean isSAML2SSOEnabled;

    /**
     * The trusted IdP's Issuer ID for this tenant.
     */
    private String idpEntityId;

    /*
     * The service provider's Entity Id for this tenant
     */
    private String spEntityId;

    /**
     * The trusted IdP's URL for this tenant.
     */
    private String ssoUrl;

    /**
     * If the AuthnRequest has to be signed
     */
    private boolean isAuthnRequestSigned;

    /**
     * If Single Logout is enabled
     */
    private boolean isLogoutEnabled;

    /**
     * If the LogoutRequestUrl is different from ACS Url
     */
    private String logoutRequestUrl;

    /**
     * If the LogoutRequest has to be signed
     */
    private boolean isLogoutRequestSigned;

    /**
     * If SAMLResponse is signed
     */
    private boolean isAuthnResponseSigned;

    /**
     * If OpenIDConnect is enabled for this IdP.
     */
    private boolean isOIDCEnabled;

    /**
     * OAuth2 Authorize Endpoint
     */
    private String authzEndpointUrl;

    /**
     * OAuth2 Token Endpoint
     */
    private String tokenEndpointUrl;

    /**
     * OAuth2 Client Secret
     */
    private String clientId;

    /**
     * OAuth2 Client Id
     */
    private String clientSecret;

    //////////////////// Getters and Setters //////////////////////////

    public String getIdPName() {
        return idPName;
    }

    public void setIdPName(String idPName) {
        this.idPName = idPName;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public String getHomeRealmId() {
        return homeRealmId;
    }

    public void setHomeRealmId(String homeRealmId) {
        this.homeRealmId = homeRealmId;
    }

    public CertData getCertData(){
        return certData;
    }

    public void setCertData(CertData certData){
        this.certData = certData;
    }

    public List<String> getClaims() {
        return claims;
    }

    public void setClaims(List<String> claims) {
        this.claims = claims;
    }

    public Map<String, String> getClaimMappings() {
        return claimMappings;
    }

    public void setClaimMappings(Map<String, String> claimMappings) {
        this.claimMappings = claimMappings;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Map<String, String> getRoleMappings() {
        return roleMappings;
    }

    public void setRoleMappings(Map<String, String> roleMappings) {
        this.roleMappings = roleMappings;
    }

    public String getTokenEndpointAlias(){
        return tokenEndpointAlias;
    }

    public void setTokenEndpointAlias(String tokenEndpointAlias){
        this.tokenEndpointAlias = tokenEndpointAlias;
    }

    public boolean isSAML2SSOEnabled(){
        return isSAML2SSOEnabled;
    }

    public void setSAML2SSOEnabled(boolean isSAML2SSOEnabled){
        this.isSAML2SSOEnabled = isSAML2SSOEnabled;
    }

    public String getIdpEntityId() {
        return idpEntityId;
    }

    public void setIdpEntityId(String idpEntityId) {
        this.idpEntityId = idpEntityId;
    }

    public String getSpEntityId() {
        return spEntityId;
    }

    public void setSpEntityId(String spEntityId) {
        this.spEntityId = spEntityId;
    }

    public String getSSOUrl() {
        return ssoUrl;
    }

    public void setSSOUrl(String ssoUrl) throws MalformedURLException {
        if(ssoUrl != null && !ssoUrl.equals("")){
            IdentityProviderMgtUIUtil.validateURI(ssoUrl);
        }
        this.ssoUrl = ssoUrl;
    }

    public boolean isAuthnRequestSigned() {
        return isAuthnRequestSigned;
    }

    public void setAuthnRequestSigned(boolean authnRequestSigned) {
        isAuthnRequestSigned = authnRequestSigned;
    }

    public boolean isLogoutEnabled(){
        return isLogoutEnabled;
    }

    public void setLogoutEnabled(boolean isSLOEnabled){
        this.isLogoutEnabled = isSLOEnabled;
    }

    public String getLogoutRequestUrl() {
        return logoutRequestUrl;
    }

    public void setLogoutRequestUrl(String logoutRequestUrl) throws MalformedURLException {
        if(logoutRequestUrl != null && !logoutRequestUrl.equals("")){
            IdentityProviderMgtUIUtil.validateURI(logoutRequestUrl);
        }
        this.logoutRequestUrl = logoutRequestUrl;
    }

    public boolean isLogoutRequestSigned() {
        return isLogoutRequestSigned;
    }

    public void setLogoutRequestSigned(boolean logoutRequestSigned) {
        isLogoutRequestSigned = logoutRequestSigned;
    }

    public boolean isAuthnResponseSigned() {
        return isAuthnResponseSigned;
    }

    public void setAuthnResponseSigned(boolean authnResponseSigned) {
        isAuthnResponseSigned = authnResponseSigned;
    }

    public boolean isOIDCEnabled(){
        return isOIDCEnabled;
    }

    public void setOIDCEnabled(boolean isOIDCEnabled){
        this.isOIDCEnabled = isOIDCEnabled;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getAuthzEndpointUrl() {
        return authzEndpointUrl;
    }

    public void setAuthzEndpointUrl(String authzEndpointUrl) throws MalformedURLException {
        if(authzEndpointUrl != null && !authzEndpointUrl.equals("")){
            IdentityProviderMgtUIUtil.validateURI(authzEndpointUrl);
        }
        this.authzEndpointUrl = authzEndpointUrl;
    }

    public String getTokenEndpointUrl() {
        return tokenEndpointUrl;
    }

    public void setTokenEndpointUrl(String tokenEndpointUrl) throws MalformedURLException {
        if(tokenEndpointUrl != null && !tokenEndpointUrl.equals("")){
            IdentityProviderMgtUIUtil.validateURI(tokenEndpointUrl);
        }
        this.tokenEndpointUrl = tokenEndpointUrl;
    }
}
