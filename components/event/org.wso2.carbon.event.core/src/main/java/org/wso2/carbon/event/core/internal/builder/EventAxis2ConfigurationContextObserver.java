package org.wso2.carbon.event.core.internal.builder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

import java.util.HashSet;
import java.util.Set;

public class EventAxis2ConfigurationContextObserver extends AbstractAxis2ConfigurationContextObserver {
	
    private static Log log = LogFactory.getLog(EventAxis2ConfigurationContextObserver.class);

    private EventBroker eventBroker;

    private Set<Integer> loadedTenants;

    public EventAxis2ConfigurationContextObserver() {
        this.loadedTenants = new HashSet<Integer>();
    }

    @Override
    public void creatingConfigurationContext(int tenantId) {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            if (!this.loadedTenants.contains(tenantId)) {
                this.eventBroker.initializeTenant();
                this.loadedTenants.add(tenantId);
            }
        } catch (Exception e) {
            log.error("Error in setting tenant information", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public void setEventBroker(EventBroker eventBroker) {
        this.eventBroker = eventBroker;
    }
    
}
