/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.email.verification.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.context.ConfigurationContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.email.verification.util.Util;
import org.wso2.carbon.email.verification.util.EmailVerifcationSubscriber;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="org.wso2.carbon.email.verification.internal.EmailVerificationServiceComponent"
 * immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="configuration.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */
public class EmailVerificationServiceComponent {
    private static Log log = LogFactory.getLog(EmailVerificationServiceComponent.class);
    private static ConfigurationContextService configurationContextService;
    protected void activate(ComponentContext context) {
        log.debug("******* Email Verification bundle is activated ******* ");
        try {
            start(context.getBundleContext());
            log.debug("******* Email Verification bundle is activated ******* ");
        } catch (Exception e) {
            log.debug("******* Failed to activate Email Verification bundle bundle ******* ");
        }
    }
    public void start(BundleContext context){
        // Registering the Emailverification Service as an OSGi Service
        EmailVerifcationSubscriber verifier = new EmailVerifcationSubscriber();
        context.registerService(EmailVerifcationSubscriber.class.getName(), verifier, null);
    }
    protected void deactivate(ComponentContext context) {
        log.debug("******* Email Verification bundle is deactivated ******* ");
    }

    protected void setRegistryService(RegistryService registryService) {
        Util.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        Util.setRegistryService(null);
    }

    protected void setConfigurationContextService(ConfigurationContextService configurationContextService){
        log.debug("Recieving ConfigurationContext Service");
        this.configurationContextService = configurationContextService;

    }
    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService){
        log.debug("Unsetting ConfigurationContext Service");
        this.configurationContextService = null;
    }
    public static ConfigurationContext getConfigurationContext(){
        if(configurationContextService.getServerConfigContext() ==  null){
            return null;
        }
        return configurationContextService.getServerConfigContext();
    }
}