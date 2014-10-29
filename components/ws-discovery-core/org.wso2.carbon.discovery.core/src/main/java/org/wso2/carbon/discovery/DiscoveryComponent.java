/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.discovery;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.ListenerManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.discovery.util.ConfigHolder;
import org.wso2.carbon.discovery.util.Util;
import org.wso2.carbon.discovery.util.DiscoveryMgtUtils;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.base.DiscoveryService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.core.ServerShutdownHandler;

import java.util.Properties;

/**
 * @scr.component name="org.wso2.carbon.discovery" immediate="true"
 * @scr.reference name="configuration.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 * @scr.reference name="listener.manager.service"
 * interface="org.apache.axis2.engine.ListenerManager"
 * cardinality="1..1" policy="dynamic"
 * bind="setListenerManager" unbind="unsetListenerManager"
 */
public class DiscoveryComponent {

    private static final Log log = LogFactory.getLog(DiscoveryComponent.class);

    private ConfigurationContextService cfgCtxSvc;
    private ServiceRegistration observerServiceRegistration;
    private ServiceRegistration discoveryServiceRegistration;

    protected void activate(ComponentContext context) {
        BundleContext bundleCtx = context.getBundleContext();
        if (cfgCtxSvc != null) {
            // Add the observers for new updates of services and activate/deactivate events
            // This will only register the observers in the main axis configuration
            AxisConfiguration mainAxisConfig = this.cfgCtxSvc.getServerConfigContext().
                    getAxisConfiguration();

            try {
                if (DiscoveryMgtUtils.isServiceDiscoveryEnabled(mainAxisConfig)) {
                    // register the service observer
                    if (log.isDebugEnabled()) {
                        log.debug("Registering the Axis observer for WS-Discovery");
                    }
                    Util.registerServiceObserver(mainAxisConfig);
                }
            } catch (RegistryException e) {
                log.error("Error while checking whether service discovery is enabled", e);
            }

            // register the shutdown handler
            // we always register the handler (handler can find out whether ws-d is enabled or not)
            if (log.isDebugEnabled()) {
                log.debug("Enabling the server shutdown handler for WS-Discovery");
            }
            DiscoveryShutdownHandler discoveryShutdownHandler = new DiscoveryShutdownHandler();
            bundleCtx.registerService(ServerShutdownHandler.class.getName(),
                    discoveryShutdownHandler, null);

        } else {
            log.warn("Error while initializing WS-Discovery core component in super tenant. " +
                    "ConfigurationContext service is unavailable.");
        }

        // This will take care of registering observers in tenant axis configurations
        observerServiceRegistration = bundleCtx.registerService(
                Axis2ConfigurationContextObserver.class.getName(),
                new DiscoveryAxis2ConfigurationContextObserver(), null);

        // Init and publish the discovery service for other components
        registerDiscoveryService(context);
    }

    private void registerDiscoveryService(ComponentContext context) {
        try {
            CarbonDiscoveryService discoveryService = new CarbonDiscoveryService();
            discoveryServiceRegistration =
                    context.getBundleContext().registerService(DiscoveryService.class.getName(),
                            discoveryService, null);
            if (log.isDebugEnabled()) {
                log.debug("Successfully registered the Carbon Discovery service");
            }
        } catch (DiscoveryException e) {
            log.error("Error while initializing the Carbon Discovery service. Discovery " +
                    "service will not be available", e);
        }
    }

    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating WS-Discovery core component");
        }

        // Un-register the OSGi service upon deactivation.
        if (discoveryServiceRegistration != null) {
            discoveryServiceRegistration.unregister();
            discoveryServiceRegistration = null;
        }

        if (observerServiceRegistration != null) {
            observerServiceRegistration.unregister();
            observerServiceRegistration = null;
        }
    }

    protected void setConfigurationContextService(ConfigurationContextService cfgCtxService) {
        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContextService bound to the discovery component");
        }
        this.cfgCtxSvc = cfgCtxService;
        ConfigHolder.getInstance().setClientConfigurationContext(
                cfgCtxService.getClientConfigContext());
        ConfigHolder.getInstance().setServerConfigurationContext(
                cfgCtxService.getServerConfigContext());
    }

    protected void unsetConfigurationContextService(ConfigurationContextService cfgCtxService) {
        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContextService unbound from the discovery component");
        }
        this.cfgCtxSvc = null;
        ConfigHolder.getInstance().setClientConfigurationContext(null);
        ConfigHolder.getInstance().setServerConfigurationContext(null);
    }

    protected void setListenerManager(ListenerManager lm) {
        if (log.isDebugEnabled()) {
            log.debug("Listener manager bound to the discovery component");
        }
    }

    protected void unsetListenerManager(ListenerManager lm) {
        if (log.isDebugEnabled()) {
            log.debug("Listener manager unbound from the discovery component");
        }
    }
}
