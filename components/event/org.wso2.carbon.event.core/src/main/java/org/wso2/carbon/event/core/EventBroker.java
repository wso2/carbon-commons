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

package org.wso2.carbon.event.core;

import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.event.core.subscription.EventDispatcher;
import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.event.core.topic.TopicManager;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.List;

/**
 * The main eventing interface for the other components. this interface supports two kinds of
 * subscriptions.
 * 1. Subscriptions with event dispatcher.
 *      Event broker does not persists these subscriptions and call the on message method upon
 *      receiving an event.
 * 2. Subscriptions with event dispatcher's name.
 *      Event broker persists these subscriptions. It is responsibility of the user who calls this
 *      method to register the {@link org.wso2.carbon.event.core.subscription.EventDispatcher} with
 *      the given name. In a server restart again caller has to register the
 *      {@link org.wso2.carbon.event.core.subscription.EventDispatcher}.
 */
public interface EventBroker {

    /**
     * Subscribe using the given subscription details.
     *
     * @param subscription the subscription
     * @return subscription ID
     * @throws org.wso2.carbon.event.core.exception.EventBrokerException
     */
    public String subscribe(Subscription subscription) throws EventBrokerException;

    /**
     * Unsubscribes a subscription
     *
     * @param id subscription ID
     * @throws EventBrokerException
     */
    public void unsubscribe(String id) throws EventBrokerException;

    /**
     * Get subscription from subscription ID
     *
     * @param id subscription ID
     * @return the subscription
     * @throws EventBrokerException
     */
    public Subscription getSubscription(String id) throws EventBrokerException;

    /**
     * Renews a subscription. Updates expiry time, removal and resetting of the properties
     *
     * @param subscription the subscription to renew
     * @throws EventBrokerException
     */
    public void renewSubscription(Subscription subscription) throws EventBrokerException;

    /**
     * Get all subscriptions
     *
     * @param filter filter value. use * for all
     * @return a list of subscriptions
     * @throws EventBrokerException
     */
    public List<Subscription> getAllSubscriptions(String filter) throws EventBrokerException;

    /**
     * Publish an event to the given topic asynchronously. i.e it starts a new thread to send the
     * message.
     *
     * @param message   - message to publish. this contains the OMElement of the message and any
     *                  properties.
     * @param topicName topic name
     */
    public void publish(Message message, String topicName) throws EventBrokerException;

    /**
     * Publish an event to the given topic asynchronously. i.e it starts a new thread to send the
     * message.
     *
     * @param message      - message to publish. this contains the OMElement of the message and any
     *                     properties.
     * @param topicName    topic name
     * @param deliveryMode - persist or not
     */
    public void publish(Message message, String topicName, int deliveryMode)
            throws EventBrokerException;

    /**
     * Publish an event to the given topic synchronously. i.e it uses the same thread to send the
     * message.
     *
     * @param message   message to publish. this contains the OMElement of the message and any
     *                  properties.
     * @param topicName topic name
     */
    public void publishRobust(Message message, String topicName) throws EventBrokerException;

    /**
     * Publish an event to the given topic synchronously. i.e it uses the same thread to send the
     * message.
     *
     * @param message      - message to publish. this contains the OMElement of the message and any
     *                     properties.
     * @param topicName    topic name
     * @param deliveryMode - persist or not
     */
    public void publishRobust(Message message, String topicName, int deliveryMode)
            throws EventBrokerException;

    /**
     * Register an event dispatcher in the case of using subscriptions with eventBrokerName
     *
     * @param eventDispatcherName event dispatcher name
     * @param eventDispatcher     event dispatcher
     */
    public void registerEventDispatcher(String eventDispatcherName,
                                        EventDispatcher eventDispatcher);

    /**
     * Gets the topic manager
     *
     * @return topic manager
     * @throws EventBrokerException
     */
    public TopicManager getTopicManager() throws EventBrokerException;

    /**
     * This method is called when a tenant initialize. to do the initialization works for tenants
     * for event broker
     *
     * @throws EventBrokerException
     * @throws UserStoreException
     */
    public void initializeTenant() throws EventBrokerException, UserStoreException;

}
