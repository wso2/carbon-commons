/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.deployment.synchronizer.registry;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.deployment.synchronizer.internal.DeploymentSynchronizerConstants;
import org.wso2.carbon.deployment.synchronizer.DeploymentSynchronizerException;
import org.wso2.carbon.deployment.synchronizer.internal.util.ServiceReferenceHolder;
import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.event.ws.internal.builders.exceptions.InvalidMessageException;
import org.wso2.carbon.event.ws.internal.builders.utils.BuilderUtils;
import org.wso2.carbon.registry.common.eventing.RegistryEvent;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.eventing.RegistryEventingConstants;
import org.wso2.carbon.registry.eventing.services.EventingService;
import org.wso2.carbon.utils.ConfigurationContextService;

public class RegistryUtils {

    /**
     * Calculate and return the absolute path of a resource
     *
     * @param registryPath The relative path of the resource
     * @param basePath Base path of the registry partition
     * @return Absolute path calculated by combining the registry path and the base path
     */
    public static String getAbsoluteRegistryPath(String registryPath, String basePath) {
        if (!registryPath.startsWith("/")) {
            registryPath = "/" + registryPath;
        }

        return basePath + registryPath;
    }

    /**
     * Subscribe for events on a given registry collection
     *
     * @param registry Registry space in which the target collection resides
     * @param absolutePath Absolute path of the collection
     * @param endpoint The endpoint which should receive the events
     * @return Subscription ID
     * @throws DeploymentSynchronizerException If the subscription operation fails
     */
    public static String subscribeForRegistryEvents(UserRegistry registry, String absolutePath,
                                        String endpoint) throws DeploymentSynchronizerException {

        EventingService eventingService = ServiceReferenceHolder.getEventingService();
        if (eventingService == null) {
            throw new IllegalStateException("Registry eventing service unavailable");
        }

        String topic = RegistryEventingConstants.TOPIC_PREFIX + absolutePath +
                RegistryEvent.TOPIC_SEPARATOR + "#";

        PrivilegedCarbonContext.startTenantFlow();
        try {
            Subscription subscription =
                    BuilderUtils.createSubscription(endpoint,
                            DeploymentSynchronizerConstants.EVENT_FILTER_DIALECT, topic);
            subscription.setEventDispatcherName(RegistryEventingConstants.TOPIC_PREFIX);
            subscription.setTenantId(registry.getCallerTenantId());
            subscription.setOwner(registry.getUserName());

            PrivilegedCarbonContext currentContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            currentContext.setTenantId(registry.getCallerTenantId(), true);
            currentContext.setUserRealm(registry.getUserRealm());
            currentContext.setUsername(registry.getUserName());

            return eventingService.subscribe(subscription);
        } catch (InvalidMessageException e) {
            throw new DeploymentSynchronizerException("Error while subscribing for registry " +
                    "events on collection: " + absolutePath, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Unsubscribe from registry events
     *
     * @param subscriptionId ID of an existing subscription
     * @param tenantId Current tenant ID
     * @return true if successfully unsubscribed
     */
    public static boolean unsubscribeForRegistryEvents(String subscriptionId, int tenantId) {
        EventingService eventingService = ServiceReferenceHolder.getEventingService();
        if (eventingService == null) {
            throw new IllegalStateException("Registry eventing service unavailable");
        }

        PrivilegedCarbonContext.startTenantFlow();
        try {
            PrivilegedCarbonContext currentContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            currentContext.setTenantId(tenantId, true);
            return eventingService.unsubscribe(subscriptionId);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Returns the endpoint URL of the AutoCheckoutService
     *
     * @return a String URL or null of the service is not available
     */
    public static String getEventReceiverEndpoint() {
        ConfigurationContextService configurationContextService =
                ServiceReferenceHolder.getConfigurationContextService();
        if (configurationContextService == null) {
            throw new IllegalStateException("Configuration context service not available");
        }

        AxisConfiguration axisConfig = configurationContextService.getServerConfigContext().
                getAxisConfiguration();
        AxisService service;
        try {
            service = axisConfig.getService(DeploymentSynchronizerConstants.EVENT_RECEIVER_SERVICE);
        } catch (AxisFault axisFault) {
            throw new IllegalStateException("Event receiver service not available", axisFault);
        }

        for (String epr : service.getEPRs()) {
            if (epr.startsWith("http")) {
                return epr;
            }
        }
        return null;
    }

}
