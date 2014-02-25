package org.wso2.carbon.transport.local.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.core.transports.TransportService;
import org.wso2.carbon.transport.local.LocalTransportService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="local.transport.services" immediate="true"
 * @scr.reference name="config.context.service" interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"  bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */
public class LocalTransportServiceComponent {
    private static Log log = LogFactory.getLog(LocalTransportServiceComponent.class);
    private ConfigurationContextService contextService;

    public LocalTransportServiceComponent() {
    }

    protected void activate(ComponentContext ctxt) {
        log.debug("******* Local Transport bundle is activated ******* ");
        //Properties props = new Properties();
		LocalTransportService localTransport;
		ConfigurationContext configContext;

        if (log.isDebugEnabled()) {
			log.debug("Starting the local transport component ...");
		}


        try {

            if (contextService != null) {
                // Getting server's configContext instance
                configContext = contextService.getServerConfigContext();
            } else {
                throw new Exception("ConfigurationContext is not found while loading " +
                        "org.wso2.carbon.transport.local bundle");
            }

            // Instantiate LocalTransportService
            localTransport = new LocalTransportService();

            // Register the LocalTransportService under TransportService interface.
            // This will make TransportManagement component to find this.
            ctxt.getBundleContext().registerService(TransportService.class.getName(), localTransport, null);

            if (log.isDebugEnabled()) {
                log.debug("Successfully registered the local transport service");
            }

        } catch (Exception e) {
            log.error("Error while activating the Local transport management bundle", e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        log.debug("******* Local Transport bundle is deactivated ******* ");
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        this.contextService = contextService;
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        this.contextService = null;
    }
}
