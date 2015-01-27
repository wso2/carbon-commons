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

package org.wso2.carbon.event.core.internal.delivery.inmemory;

import org.wso2.carbon.event.core.delivery.MatchingManager;
import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.context.CarbonContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * in memory matching manager which keeps the topic and subscriptions in an hash map
 */
public class InMemoryMatchingManager implements MatchingManager {

    private static final Log log = LogFactory.getLog(InMemoryMatchingManager.class);
    private Map<Integer, InMemorySubscriptionStorage> tenantIDInMemorySubscriptionStorageMap;

    public InMemoryMatchingManager() {
        this.tenantIDInMemorySubscriptionStorageMap =
                            new ConcurrentHashMap<Integer, InMemorySubscriptionStorage>();
    }

    public void addSubscription(Subscription subscription) {
        InMemorySubscriptionStorage inMemorySubscriptionStorage =
                this.tenantIDInMemorySubscriptionStorageMap.get(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        inMemorySubscriptionStorage.addSubscription(subscription);

    }

    public List<Subscription> getMatchingSubscriptions(String topicName) {
        InMemorySubscriptionStorage inMemorySubscriptionStorage =
                this.tenantIDInMemorySubscriptionStorageMap.get(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        if(inMemorySubscriptionStorage != null) {
            return inMemorySubscriptionStorage.getMatchingSubscriptions(topicName);
        } else {
            return new ArrayList<Subscription>();
        }
    }

    public void unSubscribe(String subscriptionID) throws EventBrokerException {
        InMemorySubscriptionStorage inMemorySubscriptionStorage =
                this.tenantIDInMemorySubscriptionStorageMap.get(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        inMemorySubscriptionStorage.unSubscribe(subscriptionID);
    }

    public void renewSubscription(Subscription subscription) throws EventBrokerException {
        InMemorySubscriptionStorage inMemorySubscriptionStorage =
                this.tenantIDInMemorySubscriptionStorageMap.get(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        inMemorySubscriptionStorage.renewSubscription(subscription);
    }

    public void initializeTenant() throws EventBrokerException {
         if (tenantIDInMemorySubscriptionStorageMap.get(CarbonContext.getThreadLocalCarbonContext().getTenantId()) == null){
             this.tenantIDInMemorySubscriptionStorageMap.put(
                      CarbonContext.getThreadLocalCarbonContext().getTenantId(), new InMemorySubscriptionStorage());
         } else {
             log.warn("There is an InMemorySubscription for tenant with id "
                               + CarbonContext.getThreadLocalCarbonContext().getTenantId() + " exists ");
         }
    }
}
