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

package org.wso2.carbon.transport.fix.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.core.transports.TransportAxis2ConfigurationContextObserver;
import org.wso2.carbon.core.transports.TransportPersistenceManager;
import org.wso2.carbon.core.transports.TransportService;
import org.wso2.carbon.transport.fix.FIXTransportService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.net.URL;

/**
 * @scr.component name="fix.transport.services" immediate="true"
 * @scr.reference name="config.context.service" interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"  bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */
public class FIXTransportServiceComponent {

    private static Log log = LogFactory.getLog(FIXTransportServiceComponent.class);
    private ConfigurationContextService contextService;

    public FIXTransportServiceComponent() {
    }

    protected void activate(ComponentContext ctxt) {
        ConfigurationContext configContext;
        FIXTransportService fixTransport;
        //Properties props;
        log.debug("******* FIX Transport bundle is activated ******* ");

        try {
            if (contextService != null) {
                // Getting server's configContext instance
                configContext = contextService.getServerConfigContext();
            } else {
                throw new Exception("ConfigurationContext is not found while loading org.wso2.carbon.transport.fix bundle");
            }

            BundleContext bundleCtx = ctxt.getBundleContext();

            // Save the transport configuration in the registry if not already done so
            URL configURL = bundleCtx.getBundle().getResource(FIXTransportService.TRANSPORT_CONF);
            new TransportPersistenceManager(configContext.getAxisConfiguration()).
                    saveTransportConfiguration(FIXTransportService.TRANSPORT_NAME, configURL);
            TransportAxis2ConfigurationContextObserver cfgCtxObserver =
                    new TransportAxis2ConfigurationContextObserver(FIXTransportService.TRANSPORT_NAME, configURL);
            bundleCtx.registerService(Axis2ConfigurationContextObserver.class.getName(),
                                    cfgCtxObserver, null);

            // Instantiate FIXTransportService
            fixTransport = new FIXTransportService();

            // This should ideally contain properties of FIXTransportService as a collection of
            // key/value pair. Here we do not require to add any elements.
            //props = new Properties();

            // Register the FIXTransportService under TransportService interface.
            // This will make TransportManagement component to find this.
            bundleCtx.registerService(TransportService.class.getName(), fixTransport, null);

            if (log.isDebugEnabled()) {
                log.debug("Successfully registered the https transport service");
            }
        } catch (Exception e) {
            log.error("Error while activating FIX transport management bundle", e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        log.debug("******* FIX Transport bundle is deactivated ******* ");
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        this.contextService = contextService;
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        this.contextService = null;
    }
}
