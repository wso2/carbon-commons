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
package org.wso2.carbon.identity.authenticator.saml2.sso.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.core.security.AuthenticatorsConfiguration;
import org.wso2.carbon.core.services.authentication.CarbonServerAuthenticator;
import org.wso2.carbon.identity.authenticator.saml2.sso.SAML2SSOAuthenticator;
import org.wso2.carbon.identity.authenticator.saml2.sso.SAML2SSOAuthenticatorBEConstants;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.Hashtable;
import java.util.Map;

/**
 * @scr.component name="saml2.sso.authenticator.dscomponent" immediate="true"
 * @scr.reference name="registry.service"
 *                interface="org.wso2.carbon.registry.core.service.RegistryService"
 *                cardinality="1..1" policy="dynamic" bind="setRegistryService"
 *                unbind="unsetRegistryService"
 * @scr.reference name="user.realmservice.default"
 *                interface="org.wso2.carbon.user.core.service.RealmService"
 *                cardinality="1..1" policy="dynamic" bind="setRealmService"
 *                unbind="unsetRealmService"
 */
public class SAML2SSOAuthenticatorDSComponent {

    private static final Log log = LogFactory.getLog(SAML2SSOAuthenticatorDSComponent.class);

    protected void activate(ComponentContext ctxt) {
        SAML2SSOAuthBEDataHolder.getInstance().setBundleContext(ctxt.getBundleContext());
        SAML2SSOAuthenticator authenticator = new SAML2SSOAuthenticator();
        Hashtable<String, String> props = new Hashtable<String, String>();
        props.put(CarbonConstants.AUTHENTICATOR_TYPE, authenticator.getAuthenticatorName());
        ctxt.getBundleContext().registerService(CarbonServerAuthenticator.class.getName(), authenticator, props);

        // Check whether the IdPCertAlias is set for signature validations of Tenant 0.
        configureIdPCertAlias();

        if (log.isDebugEnabled()) {
            log.debug("SAML2 SSO Authenticator BE Bundle activated successfuly.");
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        SAML2SSOAuthBEDataHolder.getInstance().setBundleContext(null);
        log.debug("SAML2 SSO Authenticator BE Bundle is deactivated ");
    }

    protected void setRegistryService(RegistryService registryService) {
        SAML2SSOAuthBEDataHolder.getInstance().setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        SAML2SSOAuthBEDataHolder.getInstance().setRegistryService(null);
    }

    protected void setRealmService(RealmService realmService) {
        SAML2SSOAuthBEDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        SAML2SSOAuthBEDataHolder.getInstance().setRealmService(null);
    }

    private void configureIdPCertAlias(){
        // read the meta data required for signature validation for assertions issued for Super Tenant.
        AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
        AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig =
                authenticatorsConfiguration.getAuthenticatorConfig(
                        SAML2SSOAuthenticatorBEConstants.SAML2_SSO_AUTHENTICATOR_NAME);

        if(authenticatorConfig != null){
            Map<String,String> authenticatorParams = authenticatorConfig.getParameters();
            // if this parameter is set, then use it with tenant 0. Otherwise use the default cert.
            if(authenticatorParams.containsKey(SAML2SSOAuthenticatorBEConstants.AUTH_CONFIG_PARAM_IDP_CERT_ALIAS)){
                SAML2SSOAuthBEDataHolder.getInstance().setIdPCertAlias(authenticatorParams.get(
                        SAML2SSOAuthenticatorBEConstants.AUTH_CONFIG_PARAM_IDP_CERT_ALIAS));
            }
        }

    }
}
