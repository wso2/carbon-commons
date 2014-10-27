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

import org.wso2.carbon.event.core.Message;
import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.event.core.notify.NotificationManager;
import org.wso2.carbon.event.core.subscription.Subscription;

/**
 * event broker uses the Delivery manager to do the actuall pub/sub. event broker passes an
 * notification manager instance to delivery manager in order to have the reusable code across
 * many delivery managers. Delivery manager implemenation uses matching manager to manage and
 * get the matching subscriptions
 */
public interface DeliveryManager {
    /**
     * subscribe with the subscription details.
     * @param subscription
     */
    public void subscribe(Subscription subscription) throws EventBrokerException;

    /**
     * Notification manager is used to send the notifications
     * @param notificationManager
     */
    public void setNotificationManager(NotificationManager notificationManager);

    /**
     * publish an omElement to a topic
     * @param message
     * @param topicName
     */
    public void publish(Message message, String topicName, int deliveryMode) throws EventBrokerException;


    public void unSubscribe(String id) throws EventBrokerException;

    public void cleanUp() throws EventBrokerException;

    public void renewSubscription(Subscription subscription) throws EventBrokerException;

    public void initializeTenant() throws EventBrokerException;

}
