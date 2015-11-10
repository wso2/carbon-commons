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

package org.wso2.carbon.event.core.internal;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.core.Message;
import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.event.core.delivery.DeliveryManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * this class use as the worker for the executor pool which publish the message
 */
@Deprecated
public class EventPublisher implements Runnable{

    private static final Log log = LogFactory.getLog(EventPublisher.class);

    private Message message;
    private String topicName;
    private DeliveryManager delivaryManager;
    private int deliveryMode;
    private int tenantID;

    public EventPublisher(Message message,
                          String topicName,
                          DeliveryManager delivaryManager,
                          int deliveryMode,
                          int tenantID) {
        this.message = message;
        this.topicName = topicName;
        this.delivaryManager = delivaryManager;
        this.deliveryMode = deliveryMode;
        this.tenantID = tenantID;
    }

    public void run() {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantID);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
            this.delivaryManager.publish(this.message, this.topicName, this.deliveryMode);
        } catch (EventBrokerException e) {
            log.error("Can not publish the message ", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
}
