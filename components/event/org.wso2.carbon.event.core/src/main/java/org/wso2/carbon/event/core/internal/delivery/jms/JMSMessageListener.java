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

package org.wso2.carbon.event.core.internal.delivery.jms;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.core.notify.NotificationManager;
import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;

import javax.jms.MessageListener;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.JMSException;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.util.Enumeration;

public class JMSMessageListener implements MessageListener {

    private Log log = LogFactory.getLog(JMSMessageListener.class);

    private NotificationManager notificationManager;
    private Subscription subscription;

    public JMSMessageListener(NotificationManager notificationManager, Subscription subscription) {
        this.notificationManager = notificationManager;
        this.subscription = subscription;
    }

    public void renewSubscription(Subscription subscription){
        this.subscription.setExpires(subscription.getExpires());
        this.subscription.setProperties(subscription.getProperties());
    }

    public void onMessage(Message message) {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(this.subscription.getTenantId());
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(this.subscription.getOwner());
            PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                StAXOMBuilder stAXOMBuilder =
                        new StAXOMBuilder(new ByteArrayInputStream(
                                textMessage.getText().getBytes()));
                org.wso2.carbon.event.core.Message messageToSend =
                        new org.wso2.carbon.event.core.Message();
                messageToSend.setMessage(stAXOMBuilder.getDocumentElement());
                // set the properties
                Enumeration propertyNames = message.getPropertyNames();
                String key = null;
                while (propertyNames.hasMoreElements()){
                    key = (String) propertyNames.nextElement();
                    messageToSend.addProperty(key, message.getStringProperty(key));
                }

                this.notificationManager.sendNotification(messageToSend, this.subscription);
            } else {
                log.warn("Non text message received");
            }
        } catch (JMSException e) {
            log.error("Can not read the text message ", e);
        } catch (XMLStreamException e) {
            log.error("Can not build the xml string", e);
        } catch (EventBrokerException e) {
            log.error("Can not send the notification ", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

    }
}
