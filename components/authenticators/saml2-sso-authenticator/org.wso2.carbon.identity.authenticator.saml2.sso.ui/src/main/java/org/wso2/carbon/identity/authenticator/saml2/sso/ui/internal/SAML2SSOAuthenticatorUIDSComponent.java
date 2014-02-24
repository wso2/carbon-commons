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
package org.wso2.carbon.identity.authenticator.saml2.sso.ui.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.authenticator.saml2.sso.ui.SSOAssertionConsumerService;
import org.wso2.carbon.identity.authenticator.saml2.sso.common.Util;
import org.wso2.carbon.identity.authenticator.saml2.sso.ui.authenticator.SAML2SSOUIAuthenticator;
import org.wso2.carbon.identity.authenticator.saml2.sso.ui.filters.LoginPageFilter;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.ui.CarbonSSOSessionManager;
import org.wso2.carbon.ui.CarbonUIAuthenticator;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * @scr.component name="saml2.sso.authenticator.ui.dscomponent" immediate="true"
 * @scr.reference name="registry.service"
 *                interface="org.wso2.carbon.registry.core.service.RegistryService"
 *                cardinality="1..1" policy="dynamic" bind="setRegistryService"
 *                unbind="unsetRegistryService"
 * @scr.reference name="user.realmservice.default"
 *                interface="org.wso2.carbon.user.core.service.RealmService"
 *                cardinality="1..1" policy="dynamic" bind="setRealmService"
 *                unbind="unsetRealmService"
 * @scr.reference name="config.context.service"
 *                interface="org.wso2.carbon.utils.ConfigurationContextService"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setConfigurationContextService"
 *                unbind="unsetConfigurationContextService"
 * @scr.reference name="org.wso2.carbon.ui.CarbonSSOSessionManager"
 *                interface="org.wso2.carbon.ui.CarbonSSOSessionManager"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setCarbonSSOSessionManagerInstance"
 *                unbind="unsetCarbonSSOSessionManagerInstance"
 */

public class SAML2SSOAuthenticatorUIDSComponent {

    private static final Log log = LogFactory.getLog(SAML2SSOAuthenticatorUIDSComponent.class);

    protected void activate(ComponentContext ctxt) {

        if(Util.isAuthenticatorEnabled()){
            // initialize the SSO Config params during the start-up
            boolean initSuccess = Util.initSSOConfigParams();

            if (initSuccess) {
                HttpServlet loginServlet = new HttpServlet() {
                    @Override
                    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
                            throws ServletException, IOException {

                    }
                };

                Filter loginPageFilter = new LoginPageFilter();
                Dictionary loginPageFilterProps = new Hashtable(2);
                Dictionary redirectorParams = new Hashtable(3);

                redirectorParams.put("url-pattern", Util.getLoginPage());

                redirectorParams.put("associated-filter", loginPageFilter);
                redirectorParams.put("servlet-attributes", loginPageFilterProps);
                ctxt.getBundleContext().registerService(Servlet.class.getName(),
                                                                  loginServlet, redirectorParams);

                //Register the SSO Assertion Consumer Service Servlet
//                HttpServlet acsServlet = new SSOAssertionConsumerService();
//                Dictionary acsParams = new Hashtable(2);
//                acsParams.put("url-pattern","/acs");
//                acsParams.put("display-name", "SAML SSO Assertion Consumer Service");
//                ctxt.getBundleContext().registerService(Servlet.class.getName(), acsServlet, acsParams);

                // register the UI authenticator
                SAML2SSOUIAuthenticator authenticator = new SAML2SSOUIAuthenticator();
                Hashtable<String, String> props = new Hashtable<String, String>();
                props.put(CarbonConstants.AUTHENTICATOR_TYPE, authenticator.getAuthenticatorName());
                ctxt.getBundleContext().registerService(CarbonUIAuthenticator.class.getName(),
                                                                            authenticator, props);
                if (log.isDebugEnabled()) {
                    log.debug("SAML2 SSO Authenticator BE Bundle activated successfully.");
                }
            } else{
                log.warn("Initialization failed for SSO Authenticator. Starting with the default authenticator");
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("SAML2 SSO Authenticator is disabled");
            }
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        log.debug("SAML2 SSO Authenticator FE Bundle is deactivated ");
    }

    protected void setRegistryService(RegistryService registryService) {
        SAML2SSOAuthFEDataHolder.getInstance().setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        SAML2SSOAuthFEDataHolder.getInstance().setRegistryService(null);
    }

    protected void setRealmService(RealmService realmService) {
        SAML2SSOAuthFEDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        SAML2SSOAuthFEDataHolder.getInstance().setRealmService(null);
    }

    protected void setConfigurationContextService(ConfigurationContextService configCtxtService) {
        SAML2SSOAuthFEDataHolder.getInstance().setConfigurationContextService(configCtxtService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configCtxtService) {
        SAML2SSOAuthFEDataHolder.getInstance().setConfigurationContextService(null);
    }

    protected void setCarbonSSOSessionManagerInstance(CarbonSSOSessionManager carbonSSOSessionMgr) {
        SAML2SSOAuthFEDataHolder.getInstance().setCarbonSSOSessionManager(carbonSSOSessionMgr);
    }

    protected void unsetCarbonSSOSessionManagerInstance(CarbonSSOSessionManager carbonSSOSessionMgr) {
        SAML2SSOAuthFEDataHolder.getInstance().setCarbonSSOSessionManager(null);
    }
}
