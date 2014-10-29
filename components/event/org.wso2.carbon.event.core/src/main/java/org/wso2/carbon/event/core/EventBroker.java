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

import org.wso2.carbon.event.core.exception.EventBrokerConfigurationException;
import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.event.core.subscription.EventDispatcher;
import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.event.core.topic.TopicManager;

import java.util.List;

/**
 * the main eventing interface for the other components. this interface supports two kinds of
 * subscriptions.
 * 1. subscritions with EventDispatcher
 *      Event broker does not persists these subscriptions and call the on message method upon
 *      receiving an event.
 * 2. subscriptions with EventDispatcherName
 *      Event broker persists these subscriptions. It is responsibility of the user who calls this
 *      method to register the eventDispatcher with the given name. In a server restart again
 *      caller has to register the Event dispatcher.
 */
public interface EventBroker {

    /**
     * subscribe using the gvien subscription details.
     * @param subscription
     * @return
     * @throws org.wso2.carbon.event.core.exception.EventBrokerException
     */
    public String subscribe(Subscription subscription) throws EventBrokerException;

    public void unsubscribe(String id) throws EventBrokerException;

    public Subscription getSubscription(String id) throws EventBrokerException;

    public void renewSubscription(Subscription subscription) throws EventBrokerException;

    public List<Subscription> getAllSubscriptions(String filter) throws EventBrokerException;

    /**
     * publish an event to the given topic asynchornously. i.e it starts a new thread to send
     * the message.
     * @param message - message to publish. this contains the OMElement of the message and
     * any properties.
     * @param topicName
     */
    public void publish(Message message, String topicName) throws EventBrokerException;

     /**
     * publish an event to the given topic asynchornously. i.e it starts a new thread to send
     * the message.
     * @param message - message to publish. this contains the OMElement of the message and
     * any properties.
     * @param topicName
     * @param deliveryMode - persist or not
     */
    public void publish(Message message, String topicName, int deliveryMode) throws EventBrokerException;

     /**
     * publish an event to the given topic synchornously. i.e it uses the same thread to send
     * the message.
     * @param message - message to publish. this contains the OMElement of the message and
     * any properties.
     * @param topicName
     */
    public void publishRobust(Message message, String topicName) throws EventBrokerException;

    /**
     * publish an event to the given topic synchornously. i.e it uses the same thread to send
     * the message.
     * @param message - message to publish. this contains the OMElement of the message and
     * any properties.
     * @param topicName
     * @param deliveryMode - persist or not
     */
    public void publishRobust(Message message, String topicName, int deliveryMode) throws EventBrokerException;

    /**
     * register an event dispatcher in the case of using subscriptions with eventBrokerName
     * @param eventDispatcherName
     * @param eventDispatcher
     */
    public void registerEventDispatcher(String eventDispatcherName,
                                        EventDispatcher eventDispatcher);

    public TopicManager getTopicManager() throws EventBrokerException;

    /**
     * this method is called when a tenat initialize. to do the initilaization works for tenants
     * for event broker
     * @throws EventBrokerConfigurationException
     */
    public void initializeTenant() throws EventBrokerException;

}
