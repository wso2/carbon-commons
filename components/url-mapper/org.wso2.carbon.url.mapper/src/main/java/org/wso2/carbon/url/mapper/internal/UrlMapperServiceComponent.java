/*
 * Copyright WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.url.mapper.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.tomcat.api.CarbonTomcatService;
import org.wso2.carbon.tomcat.ext.utils.URLMappingHolder;
import org.wso2.carbon.tomcat.ext.valves.CarbonTomcatValve;
import org.wso2.carbon.tomcat.ext.valves.TomcatValveContainer;
import org.wso2.carbon.url.mapper.HotUpdateService;
import org.wso2.carbon.url.mapper.UrlMapperValve;
import org.wso2.carbon.url.mapper.data.MappingConfig;
import org.wso2.carbon.url.mapper.data.MappingData;
import org.wso2.carbon.url.mapper.internal.exception.UrlMapperException;
import org.wso2.carbon.url.mapper.internal.util.DataHolder;
import org.wso2.carbon.url.mapper.internal.util.HostUtil;
import org.wso2.carbon.url.mapper.internal.util.MappingConfigManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * This is urlmapper component which retrieve virtual host from
 * registry and add to tomcat engine in its initialization.
 * adds the CarbonTomcatValve to TomcatContainer.
 */
@Component(
        name = "org.wso2.carbon.url.mapper.UrlMapperServiceComponent",
        immediate = true)
public class UrlMapperServiceComponent {

    private static Log log = LogFactory.getLog(UrlMapperServiceComponent.class);

    private ServiceRegistration serviceRegistration;

    @Activate
    protected void activate(final ComponentContext componentContext) {

        final BundleContext bundleContext = componentContext.getBundleContext();
        // uninstall this component
        if (!CarbonUtils.isRunningInStandaloneMode()) {
            Thread th = new Thread() {

                public void run() {

                    try {
                        bundleContext.getBundle().uninstall();
                    } catch (Throwable e) {
                        log.warn("Error occurred while uninstalling webapp-mgt UI bundle", e);
                    }
                }
            };
            try {
                th.join();
            } catch (InterruptedException ignored) {
            }
            th.start();
        }
        serviceRegistration = bundleContext.registerService(HotUpdateService.class.getName(), new HotUpdateManager(),
                null);
        addMappingToInMemory();
        // register the UrlMapperValve to the TomcatValveContainer to add to the tomcat engine
        List<CarbonTomcatValve> carbonTomcatValves = new ArrayList<CarbonTomcatValve>();
        carbonTomcatValves.add(new UrlMapperValve());
        TomcatValveContainer.addValves(carbonTomcatValves);
        // load configuration file
        MappingConfig config = MappingConfigManager.loadMappingConfiguration();
        HostUtil.setUrlSuffix(config.getPrefix());
    }

    protected void addMappingToInMemory() {

        MappingData[] urlMappings = new MappingData[0];
        try {
            urlMappings = HostUtil.getAllMappingsFromRegistry();
        } catch (UrlMapperException e) {
            log.error("error while getting all mappings from registry", e);
        }
        if (urlMappings != null) {
            for (MappingData mapping : urlMappings) {
                if (mapping.getTenantDomain().equalsIgnoreCase(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                    if (mapping.isServiceMapping()) {
                        URLMappingHolder.getInstance().putUrlMappingForApplication(mapping.getMappingName(), mapping
                                .getUrl());
                    }
                }
            }
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {

        serviceRegistration.unregister();
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {

        DataHolder.getInstance().setServerConfigContext(contextService.getServerConfigContext());
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {

        DataHolder.getInstance().setServerConfigContext(null);
    }

    @Reference(
            name = "tomcat.service.provider",
            service = org.wso2.carbon.tomcat.api.CarbonTomcatService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetCarbonTomcatService")
    protected void setCarbonTomcatService(CarbonTomcatService carbonTomcatService) {
        // keeping the carbonTomcatService in UrlMapperAdminService class
        DataHolder.getInstance().setCarbonTomcatService(carbonTomcatService);
    }

    protected void unsetCarbonTomcatService(CarbonTomcatService carbonTomcatService) {

        DataHolder.getInstance().setCarbonTomcatService(null);
    }

    @Reference(
            name = "org.wso2.carbon.registry.service",
            service = org.wso2.carbon.registry.core.service.RegistryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {

        try {
            DataHolder.getInstance().setRegistry(registryService.getGovernanceSystemRegistry());
        } catch (Exception e) {
            log.error("Cannot  retrieve System Registry", e);
        }
    }

    protected void unsetRegistryService(RegistryService registryService) {

        DataHolder.getInstance().setRegistry(null);
    }

    @Reference(
            name = "user.realmservice.default",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        // keeping the realm service in the UrlMapperAdminService class
        DataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {

        DataHolder.getInstance().setRealmService(null);
    }
}
