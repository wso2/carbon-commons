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

package org.wso2.carbon.event.core.internal.subscription.inmemory;

import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.event.core.subscription.SubscriptionManager;
import org.wso2.carbon.event.core.exception.EventBrokerException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * inmemory subscription manager. this keeps the subscriptions in a map
 * and returns the the subscriptions from that.
 */
public class InMemorySubscriptionManager implements SubscriptionManager{

    private Map<String, Subscription> subscriptionsMap;

    public InMemorySubscriptionManager() {
        this.subscriptionsMap = new ConcurrentHashMap();
    }

    public void addSubscription(Subscription subscription) {
        this.subscriptionsMap.put(subscription.getId(), subscription);
    }

    public List<Subscription> getAllSubscriptions() {
        List<Subscription> subscriptions = new ArrayList();
        subscriptions.addAll(this.subscriptionsMap.values());
        return subscriptions;
    }

    public Subscription getSubscription(String id) {
        return this.subscriptionsMap.get(id);
    }

    public void unSubscribe(String subscriptionID) throws EventBrokerException {
        this.subscriptionsMap.remove(subscriptionID);
    }

    public void renewSubscription(Subscription subscription) throws EventBrokerException {
         // TODO: implement the method properly
    }

    public String getTopicStoragePath() throws EventBrokerException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
