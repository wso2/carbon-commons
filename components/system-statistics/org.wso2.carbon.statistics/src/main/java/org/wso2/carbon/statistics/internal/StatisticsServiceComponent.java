/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.statistics.internal;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.engine.AxisConfiguration;
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
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.statistics.StatisticsAxis2ConfigurationContextObserver;
import org.wso2.carbon.statistics.StatisticsConstants;
import org.wso2.carbon.statistics.services.StatisticsAdmin;
import org.wso2.carbon.statistics.services.SystemStatisticsUtil;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.MBeanRegistrar;

@Component(
        name = "statistics.service.component",
        immediate = true)
public class StatisticsServiceComponent {

    private static final Log log = LogFactory.getLog(StatisticsServiceComponent.class);

    private ConfigurationContext configContext;

    private ServerConfigurationService serverConfig;

    private ServiceRegistration statAdminServiceRegistration;

    private ServiceRegistration axisConfigCtxObserverServiceRegistration;

    private RegistryService registryService;

    private static RealmService realmService;

    @Activate
    protected void activate(ComponentContext ctxt) {

        try {
            // Engaging StatisticModule as an global module
            configContext.getAxisConfiguration().engageModule(StatisticsConstants.STATISTISTICS_MODULE_NAME);
            // Register Statistics MBean
            registerMBeans(serverConfig);
            // Registering StatisticsAdmin as an OSGi service.
            // Registering StatisticsAdmin as an OSGi service.
            BundleContext bundleCtx = ctxt.getBundleContext();
            statAdminServiceRegistration = bundleCtx.registerService(SystemStatisticsUtil.class.getName(), new
                    SystemStatisticsUtil(), null);
            axisConfigCtxObserverServiceRegistration = bundleCtx.registerService(Axis2ConfigurationContextObserver
                    .class.getName(), new StatisticsAxis2ConfigurationContextObserver(), null);
            log.debug("Statistics bundle is activated");
        } catch (Throwable e) {
            log.error("Failed to activate Statistics bundle", e);
        }
    }

    public RealmService getRealmService() {

        return realmService;
    }

    protected void setRealmService(RealmService realmService) {

        StatisticsServiceComponent.realmService = realmService;
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {
        // unregistering StatisticsAdmin service from the OSGi Service Register.
        statAdminServiceRegistration.unregister();
        axisConfigCtxObserverServiceRegistration.unregister();
        log.debug("Statistics bundle is deactivated");
    }

    private void registerMBeans(ServerConfigurationService serverConfig) {

        MBeanRegistrar.registerMBean(new StatisticsAdmin());
    }

    @Reference(
            name = "config.context.service",
            service = org.wso2.carbon.utils.ConfigurationContextService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService contextService) {

        this.configContext = contextService.getServerConfigContext();
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {

        AxisConfiguration axisConf = configContext.getAxisConfiguration();
        AxisModule statModule = axisConf.getModule(StatisticsConstants.STATISTISTICS_MODULE_NAME);
        if (statModule != null) {
            try {
                axisConf.disengageModule(statModule);
            } catch (AxisFault axisFault) {
                log.error("Failed disengage module: " + StatisticsConstants.STATISTISTICS_MODULE_NAME);
            }
        }
        this.configContext = null;
    }

    @Reference(
            name = "server.configuration",
            service = org.wso2.carbon.base.api.ServerConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetServerConfiguration")
    protected void setServerConfiguration(ServerConfigurationService serverConfiguration) {

        this.serverConfig = serverConfiguration;
    }

    protected void unsetServerConfiguration(ServerConfigurationService serverConfiguration) {

        this.serverConfig = null;
    }

    @Reference(
            name = "registry.service",
            service = org.wso2.carbon.registry.core.service.RegistryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {

        if (log.isDebugEnabled()) {
            log.info("Setting the Registry Service");
        }
        this.registryService = registryService;
    }

    protected void unsetRegistryService(RegistryService registryService) {

        if (log.isDebugEnabled()) {
            log.info("Unsetting the Registry Service");
        }
        this.registryService = null;
    }

    public static TenantManager getTenantManager() {

        return realmService.getTenantManager();
    }
}
