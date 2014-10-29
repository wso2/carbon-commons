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
 * interface to mange the subscriptions with a persistance storage. Event Broker implemantiaon
 * call this interface to add new subscriptions.
 * At the restart of the server it can retrive all the subscriptions and resubscribes.
 */
public interface SubscriptionManager {

    public void addSubscription(Subscription subscription) throws EventBrokerException;

    public List<Subscription> getAllSubscriptions() throws EventBrokerException;

    public Subscription getSubscription(String id) throws EventBrokerException;

    public void unSubscribe(String subscriptionID) throws EventBrokerException;

    public void renewSubscription(Subscription subscription) throws EventBrokerException;

    public String getTopicStoragePath() throws EventBrokerException;

}
