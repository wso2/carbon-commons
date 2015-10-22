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

package org.wso2.carbon.event.core.internal.delivery;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.core.notify.NotificationManager;
import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.event.core.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * worker to send notifications. Delivary managers should start a new thread to send a notification.
 */
@Deprecated
public class Worker implements Runnable{

    private NotificationManager notificationManager;
    private Message message;
    private Subscription subscription;

    private static final Log log = LogFactory.getLog(Worker.class);

    public Worker(NotificationManager notificationManager,
                  Message message,
                  Subscription subscription) {
        this.notificationManager = notificationManager;
        this.message = message;
        this.subscription = subscription;
    }

    public void run() {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(this.subscription.getTenantId());
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(this.subscription.getOwner());
            PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
            this.notificationManager.sendNotification(this.message, this.subscription);
        } catch (EventBrokerException e) {
            log.error("Can not send the notification ", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
}
