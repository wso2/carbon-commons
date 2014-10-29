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

package org.wso2.carbon.event.core.internal.notify;

import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.event.core.notify.NotificationManager;
import org.wso2.carbon.event.core.subscription.EventDispatcher;
import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.event.core.Message;

import java.util.Map;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Notification manager of the carbon. This implements the send notification
 * method which can be used by any delivary manager interface
 */
public class CarbonNotificationManager implements NotificationManager{

    private Map<String, EventDispatcher> eventDispatchMap;

    public CarbonNotificationManager() {
        this.eventDispatchMap = new ConcurrentHashMap();
    }

    public void sendNotification(Message message, Subscription subscription)
            throws EventBrokerException {

        if (subscription.getExpires() != null){
            Calendar calendar = Calendar.getInstance();
            if (calendar.after(subscription.getExpires())){
                // this sequence has already expied.
                return;
            }
        }

         //send the message using the appropriate method of the subscription attributes
        if (subscription.getEventDispatcher() != null){
            subscription.getEventDispatcher().notify(message, subscription);
        } else if (subscription.getEventDispatcherName() != null){
            EventDispatcher eventDispatcher =
                    this.eventDispatchMap.get(subscription.getEventDispatcherName());
            if (eventDispatcher == null){
                throw new EventBrokerException("Event dispatcher with name "
                        + subscription.getEventDispatcherName() + " is not exists");
            }
            eventDispatcher.notify(message, subscription);
        } else {
            throw new EventBrokerException("Can not send the notification ");
        }

    }

    public void registerEventDispatcher(String name, EventDispatcher eventDispatcher){
        this.eventDispatchMap.put(name, eventDispatcher);
    }
}
