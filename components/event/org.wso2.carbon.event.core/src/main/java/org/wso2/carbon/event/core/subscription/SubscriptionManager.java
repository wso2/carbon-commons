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

package org.wso2.carbon.event.core.subscription;

import org.wso2.carbon.event.core.exception.EventBrokerException;

import java.util.List;

/**
 * interface to mange the subscriptions with a persistence storage. Event Broker implementation
 * call this interface to add new subscriptions.
 * At the restart of the server it can retrieve all the subscriptions and resubscribes.
 */
public interface SubscriptionManager {

    /**
     * When adding a subscription first it stores in the registry as as set of property values
     * then add the subscription details to the topic index
     *
     * @param subscription the subscription to be added
     * @throws EventBrokerException
     */
    public void addSubscription(Subscription subscription) throws EventBrokerException;

    /**
     * Get all the subscriptions. it get the subscription ids from the topic index and get
     * relevant subscriptions.
     *
     * @return a list of subscriptions
     * @throws EventBrokerException
     */
    public List<Subscription> getAllSubscriptions() throws EventBrokerException;

    /**
     * Gets the subscription
     * @param id the subscription id
     * @return the relevant subscription
     * @throws EventBrokerException
     */
    public Subscription getSubscription(String id) throws EventBrokerException;

    /**
     * Un-Subscribing to a destination. Destination can be queue or topic.
     *
     * @param subscriptionID the subscription ID
     * @throws EventBrokerException
     */
    public void unSubscribe(String subscriptionID) throws EventBrokerException;

    /**
     * Renewing a subscription
     * @param subscription the subscription
     * @throws EventBrokerException
     */
    public void renewSubscription(Subscription subscription) throws EventBrokerException;

    /**
     * Gets the topic storage path
     *
     * @return the storage path
     * @throws EventBrokerException
     */
    public String getTopicStoragePath() throws EventBrokerException;

}
