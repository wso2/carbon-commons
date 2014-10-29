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

package org.wso2.carbon.event.core.internal.delivery.registry;

import org.wso2.carbon.event.core.delivery.MatchingManager;
import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.event.core.util.EventBrokerConstants;
import org.wso2.carbon.event.core.internal.util.EventBrokerHolder;
import org.wso2.carbon.event.core.internal.util.JavaUtil;
import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RegistryMatchingManager implements MatchingManager {

    /**
     * registry service to access the registry. this is get from the EBBrokerHolder
     */
    private RegistryService registryService;

    /**
     * root patch which used to start the subscription related deatils.
     */
    private String subscriptionStoragePath;


    public RegistryMatchingManager(String subscriptionStoragePath) {
        this.subscriptionStoragePath = subscriptionStoragePath;
        this.registryService = EventBrokerHolder.getInstance().getRegistryService();
    }

    public void addSubscription(Subscription subscription) {
        // do noting here we use the already persisted subscriptions in the registry
    }

    public List<Subscription> getMatchingSubscriptions(String topicName)
            throws EventBrokerException {

        // since all the subscriptions for the same topic is stored in the
        // same path we can get all the subscriptions by getting all the chlid
        // resources under to topoic name.

        String topicResourcePath =  JavaUtil.getResourcePath(topicName, this.subscriptionStoragePath);

        if (!topicResourcePath.endsWith("/")) {
            topicResourcePath += "/";
        }

        topicResourcePath += EventBrokerConstants.EB_CONF_WS_SUBSCRIPTION_COLLECTION_NAME;
        List<Subscription> matchingSubscriptions = new ArrayList();

        try {
            UserRegistry userRegistry =
                    this.registryService.getGovernanceSystemRegistry(EventBrokerHolder.getInstance().getTenantId());
            String subscriptionID = null;
            if (userRegistry.resourceExists(topicResourcePath)) {
                Collection subscriptions = (Collection) userRegistry.get(topicResourcePath);
                String[] subscriptionPaths = (String[]) subscriptions.getContent();
                for (String subscriptionPath : subscriptionPaths) {
                    Resource resource = userRegistry.get(subscriptionPath);
                    Subscription subscription = JavaUtil.getSubscription(resource);
                    subscriptionID = subscriptionPath.substring(subscriptionPath.lastIndexOf("/") + 1);
                    subscription.setId(subscriptionID);
                    subscription.setTopicName(topicName);

                    // check for expiration
                    Calendar current = Calendar.getInstance(); //Get current date and time
                    if (subscription.getExpires() != null) {
                        if (current.before(subscription.getExpires())) {
                            // add only valid subscriptions by checking the expiration
                            matchingSubscriptions.add(subscription);
                        }
                    } else {
                        // If a expiration dosen't exisits treat it as a never expire subscription, valid till unsubscribe
                        matchingSubscriptions.add(subscription);
                    }
                }
            }
        } catch (RegistryException e) {
            throw new EventBrokerException("Can not get the Registry ", e);
        }

        return matchingSubscriptions;
    }

    public void unSubscribe(String subscriptionID) throws EventBrokerException {
         //TODO : implement the method properly
    }

    public void renewSubscription(Subscription subscription) throws EventBrokerException {
        //TODO: implement the method properly
    }

    public void initializeTenant() throws EventBrokerException {
        // there is nothing to do when initializing the tenant
    }
}
