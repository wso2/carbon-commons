/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.datasource.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.datasource.*;
import org.apache.synapse.commons.datasource.factory.DataSourceInformationRepositoryFactory;
import org.apache.synapse.commons.util.MiscellaneousUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.datasource.DataSourceInformationManager;
import org.wso2.carbon.datasource.DataSourceInformationRepositoryService;
import org.wso2.carbon.datasource.DataSourceInformationRepositoryServiceImpl;
import org.wso2.carbon.datasource.DataSourceManagementHandler;
import org.wso2.carbon.datasource.multitenancy.DataSourceInitializer;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.securevault.SecretCallbackHandlerService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.securevault.secret.SecretCallbackHandler;
import org.wso2.securevault.secret.handler.SharedSecretCallbackHandlerCache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @scr.component name="org.wso2.carbon.datasource" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="secret.callback.handler.service"
 * interface="org.wso2.carbon.securevault.SecretCallbackHandlerService"
 * cardinality="1..1" policy="dynamic"
 * bind="setSecretCallbackHandlerService" unbind="unsetSecretCallbackHandlerService"
 */
public class DataSourceServiceComponent {

    private static final Log log = LogFactory.getLog(DataSourceServiceComponent.class);

    private static RegistryService registryService;

    private static DataSourceManagementHandler handler = DataSourceManagementHandler.getInstance();

    private static String DATA_SOURCE_PROPERTIES = "datasources.properties";

    private ServiceRegistration registration;

    private SecretCallbackHandlerService secretCallbackHandlerService;

    private static List<String> JNDIProviderPort;

    private static final int CARBON_DEFAULT_PORT_OFFSET = 0;

    private static final String CARBON_CONFIG_PORT_OFFSET_NODE = "Ports.Offset";

    private static final String CARBON_CONFIG_PORT_JNDI_PROVIDER_NODE = "Ports.JNDIProviderPort";

    private static final String DATA_SOURCE_PROPERTIES_JNDI_PROVIDER_PORT =
            "synapse.datasources.providerPort";

    protected void activate(ComponentContext cmpCtx) throws Exception {
        if (registryService != null) {
            try {
                PrivilegedCarbonContext.startTenantFlow();

                PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                carbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                carbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);


                int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
                Registry registry = registryService.getConfigSystemRegistry(tenantId);
                if (registry == null) {
                    handleException("Unable to retrieve the config registry from the registry " +
                            "service");
                }

                BundleContext bundleContext = cmpCtx.getBundleContext();
                bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(),
                        new DataSourceInitializer(), null);

                if (secretCallbackHandlerService != null) {
                    SecretCallbackHandler secretCallbackHandler =
                            secretCallbackHandlerService.getSecretCallbackHandler();
                    SharedSecretCallbackHandlerCache.getInstance()
                            .setSecretCallbackHandler(secretCallbackHandler);
                }

                DataSourceInformationRepository repository = getDSFromCarbonDSConfig();
                if (repository.getRepositoryListener() == null) {
                    DataSourceInformationRepositoryListener listener =
                            new DataSourceRepositoryManager(new InMemoryDataSourceRepository(),
                                    new JNDIBasedDataSourceRepository());
                    repository.setRepositoryListener(listener);
                }

                DataSourceInformationManager dsManager =
                        handler.getTenantDataSourceInformationManager();
                if (dsManager == null) {
                    dsManager = new DataSourceInformationManager();
                    dsManager.setRepository(repository);
                    dsManager.setRegistry(registry);
                    dsManager.populateDataSourceInformation();

                    handler.addDataSourceManager(tenantId, dsManager);
                }

                DataSourceInformationRepositoryService repositoryServiceImpl =
                        new DataSourceInformationRepositoryServiceImpl();
                registration = cmpCtx.getBundleContext().registerService(
                        DataSourceInformationRepositoryService.class.getName(),
                        repositoryServiceImpl, null);
                PrivilegedCarbonContext.endTenantFlow();
            } catch (RegistryException e) {
                log.error(e);
                handleException("Error in retrieving SystemRegistry from Registry Service");
            }
        }
    }

    protected void deactivate(ComponentContext componentContext) throws Exception {
        DataSourceInformationManager dsManager = handler.getTenantDataSourceInformationManager();
        if (dsManager != null) {
            dsManager.shutDown();
        }
        if (log.isDebugEnabled()) {
            log.debug("Stopping the (RuleServerManager Component");
        }
        componentContext.getBundleContext().ungetService(registration.getReference());
    }

    protected void setRegistryService(RegistryService regService) {
        registryService = regService;
    }

    protected void unsetRegistryService(RegistryService regService) {
        registryService = null;
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    private DataSourceInformationRepository getDSFromCarbonDSConfig() {
        String carbonConfDir = CarbonUtils.getCarbonConfigDirPath();
        String propertiesFile = carbonConfDir + File.separator +
                DATA_SOURCE_PROPERTIES;
        Properties props = loadProperties(propertiesFile);
        trimProperties(props);

        // Read Port Offset
        int portOffset = readPortOffset();

        if (CarbonUtils.getServerConfiguration().getFirstProperty(
                CARBON_CONFIG_PORT_JNDI_PROVIDER_NODE) != null) {
            //reading JNDIProviderPort from carbon.xml
            JNDIProviderPort = Arrays.asList(CarbonUtils.getServerConfiguration().getProperties(
                    CARBON_CONFIG_PORT_JNDI_PROVIDER_NODE));
            props.setProperty(DATA_SOURCE_PROPERTIES_JNDI_PROVIDER_PORT, Integer.toString(
                    Integer.parseInt(JNDIProviderPort.get(0).trim()) + portOffset));
        } else {
            String dsConfigJNDIPort = props.getProperty(DATA_SOURCE_PROPERTIES_JNDI_PROVIDER_PORT);
            if (dsConfigJNDIPort != null) {
                props.setProperty(DATA_SOURCE_PROPERTIES_JNDI_PROVIDER_PORT,
                        Integer.toString(Integer.parseInt(props.getProperty(
                                DATA_SOURCE_PROPERTIES_JNDI_PROVIDER_PORT).trim()) +
                                portOffset));
            } else {
                props.setProperty(DATA_SOURCE_PROPERTIES_JNDI_PROVIDER_PORT,
                        Integer.toString(DataSourceConstants.DEFAULT_PROVIDER_PORT + portOffset));
            }
        }

            return DataSourceInformationRepositoryFactory.createDataSourceInformationRepository(props);
    }

    private Properties loadProperties(String filePath) {

        File dataSourceFile = new File(filePath);
        if (!dataSourceFile.exists()) {
            return MiscellaneousUtil.loadProperties(DATA_SOURCE_PROPERTIES);
        }

        Properties properties = new Properties();
        InputStream in = null;
        try {
            in = new FileInputStream(dataSourceFile);
            properties.load(in);
        } catch (IOException e) {
            String msg = "Error loading properties from a file at :" + filePath;
            log.warn(msg, e);
            return properties;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {

                }
            }
        }
        return properties;
    }
    
    private int readPortOffset() {
        String portOffset =
                CarbonUtils.getServerConfiguration().getFirstProperty(CARBON_CONFIG_PORT_OFFSET_NODE);
        try {
            return ((portOffset != null) ? Integer.parseInt(portOffset.trim()) :
                    CARBON_DEFAULT_PORT_OFFSET);
        } catch (NumberFormatException e) {
            return CARBON_DEFAULT_PORT_OFFSET;
        }
    }

    private void handleException(String msg) {
        log.error(msg);
        throw new IllegalArgumentException(msg);
    }

    protected void setSecretCallbackHandlerService(
            SecretCallbackHandlerService secretCallbackHandlerService) {
        if (log.isDebugEnabled()) {
            log.debug("SecretCallbackHandlerService bound to the ESB initialization process");
        }
        this.secretCallbackHandlerService = secretCallbackHandlerService;
    }

    protected void unsetSecretCallbackHandlerService(
            SecretCallbackHandlerService secretCallbackHandlerService) {
        if (log.isDebugEnabled()) {
            log.debug("SecretCallbackHandlerService  unbound from the ESB environment");
        }
        this.secretCallbackHandlerService = null;
    }

    private void trimProperties(Properties props) {
        Iterator propEntriesItr = props.entrySet().iterator();
        while (propEntriesItr.hasNext()) {
            Map.Entry entry = (Map.Entry) propEntriesItr.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            String trimmedValue = value.trim();
            props.put(key, trimmedValue);
        }
    }

}
