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

package org.wso2.carbon.event.core.delivery;

import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.event.core.exception.EventBrokerException;

import java.util.List;

/**
 * matches the subscriptions with the topics
 */
public interface MatchingManager {

    /**
     * add subscriptions to matching manager
     * @param subscription
     */
    public void addSubscription(Subscription subscription) throws EventBrokerException;

    /**
     * then the matching subscriptions
     * @param topicName
     * @return
     */
    public List<Subscription> getMatchingSubscriptions(String topicName) throws EventBrokerException;

    /**
     * removes the subscription from the matching manager
     * @param subscriptionID
     * @throws EventBrokerException
     */
    public void unSubscribe(String subscriptionID) throws EventBrokerException;


    public void renewSubscription(Subscription subscription) throws EventBrokerException;


    public void initializeTenant() throws EventBrokerException;
   
}
