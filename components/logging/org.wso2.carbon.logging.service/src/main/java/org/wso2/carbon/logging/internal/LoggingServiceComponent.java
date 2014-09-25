/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.logging.internal;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.logging.registry.RegistryManager;
import org.wso2.carbon.logging.util.LoggingConstants;
import org.wso2.carbon.logging.util.LoggingUtil;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="org.wso2.carbon.logging.services" immediate="true"
 * @scr.reference name="org.wso2.carbon.registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService"
 * unbind="unsetRegistryService"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"  bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 */
public class LoggingServiceComponent {
    private static Log log = LogFactory.getLog(LoggingServiceComponent.class);

    private RegistryService registryService;

    private static RealmService realmService;

    protected void activate(ComponentContext ctxt) {
        try {
            initLoggingConfiguration();
            BundleContext bundleContext = ctxt.getBundleContext();
        } catch (Exception e) {
            log.error("Cannot  initialize logging configuration", e);
        }
    }

    public RealmService getRealmService() {
        return realmService;
    }

    protected void setRealmService(RealmService realmService) {
        LoggingServiceComponent.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {
        setRealmService(null);
    }

    public static TenantManager getTenantManager() {
        return realmService.getTenantManager();
    }

    public static RealmConfiguration getBootstrapRealmConfiguration() {
        return realmService.getBootstrapRealmConfiguration();
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        DataHolder.getInstance().setServerConfigContext(contextService.getServerConfigContext());
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        DataHolder.getInstance().setServerConfigContext(null);
    }

    protected void deactivate(ComponentContext ctxt) {

    }

    protected void setRegistryService(RegistryService registryService) {
        try {
            RegistryManager.setRegistry(registryService.getConfigSystemRegistry());
            this.registryService = registryService;
        } catch (Exception e) {
            log.error("Cannot  retrieve System Registry", e);
        }
    }

    protected void unsetRegistryService(RegistryService registryService) {
        RegistryManager.setRegistry(null);
    }

    /**
     * Reads the database and check whether log setting already there. if so update the logger
     * settings with the database settings else update the data to data base
     *
     * @throws Exception If an error occurs when loading loggin config
     */
    private void initLoggingConfiguration() throws Exception {

        //checking whether log4j.properies file is changed.
        File confFolder = new File(CarbonUtils.getCarbonConfigDirPath());
        String loggingPropFilePath = confFolder.getAbsolutePath() + File.separator +
                "log4j.properties";
        // URL url = Thread.currentThread().getContextClassLoader().getResource("log4j.properties");
        File log4jFile = new File(loggingPropFilePath);

        if (!log4jFile.isFile()) {
            LoggingUtil.removeAllLoggersAndAppenders();
            LoggingUtil.updateConfigurationProperty(LoggingConstants.LOG4J_FILE_FOUND, "false");
            return;
        } else {
            LoggingUtil.updateConfigurationProperty(LoggingConstants.LOG4J_FILE_FOUND, "true");
        }

        long currentLastModified = log4jFile.lastModified();

        String lmStr =
                new RegistryManager().
                        getConfigurationProperty(LoggingConstants.LOG4J_FILE_LAST_MODIFIED);
        long previousLastModified = (lmStr != null) ? Long.parseLong(lmStr) : 0;

        if (previousLastModified != currentLastModified) {
            //log4j.properties file is changed..
            LoggingUtil.updateConfigurationProperty(LoggingConstants.LOG4J_FILE_LAST_MODIFIED,
                    Long.toString(currentLastModified));
            //Remove all the entries in the registry
            LoggingUtil.removeAllLoggersAndAppenders();
        } else {
            LoggingUtil.loadCustomConfiguration();
        }
    }
}
