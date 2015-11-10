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

package org.wso2.carbon.event.core.internal.builder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.event.core.EventBundleNotificationService;
import org.wso2.carbon.event.core.qpid.QpidServerDetails;
import org.wso2.carbon.event.core.exception.EventBrokerConfigurationException;
import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.event.core.internal.CarbonEventBroker;
import org.wso2.carbon.event.core.internal.util.EventBrokerHolder;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

@Deprecated
public class EventBrokerHandler implements EventBundleNotificationService {

    private static final Log log = LogFactory.getLog(EventBrokerHandler.class);

    private ComponentContext context;

    private ServiceRegistration eventServiceRegistration;

    public EventBrokerHandler(ComponentContext context) {
        this.context = context;
    }

    public void startEventBroker() {
        try {
            // set incarnate this thread to supper tenat since carbon contexes can only be
            // run is supertenants
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(CarbonConstants.REGISTRY_SYSTEM_USERNAME);

            EventBroker eventBroker = EventBrokerBuilder.createEventBroker();
            this.eventServiceRegistration =
                    this.context.getBundleContext().registerService(EventBroker.class.getName(), eventBroker, null);

            // register the tenat login listener
            EventAxis2ConfigurationContextObserver observer = new EventAxis2ConfigurationContextObserver();
            observer.setEventBroker(eventBroker);

            this.context.getBundleContext().registerService(
                    Axis2ConfigurationContextObserver.class.getName(), observer, null);
            if(log.isDebugEnabled()){ 
				log.info("Successfully registered the event broker");
			}
        } catch (EventBrokerConfigurationException e) {
            log.error("Can not create the event broker", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public void stopEventBroker() {
        ServiceReference serviceReference =
                this.context.getBundleContext().getServiceReference(EventBroker.class.getName());

        CarbonEventBroker carbonEventBroker =
                (CarbonEventBroker) this.context.getBundleContext().getService(serviceReference);

        //unregister the service before cleaning up.
        this.eventServiceRegistration.unregister();

        try {
            carbonEventBroker.cleanUp();
        } catch (EventBrokerException e) {
            log.error("Can not clean up the carbon broker ", e);
        }
    }

    public void notifyStart(QpidServerDetails qpidServerDetails) {
        EventBrokerHolder.getInstance().registerQpidServerDetails(qpidServerDetails);
        this.startEventBroker();
    }
}
