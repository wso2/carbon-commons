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

import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.event.core.subscription.Subscription;

import javax.jms.TopicSubscriber;
import javax.jms.TopicSession;
import javax.jms.TopicConnection;
import javax.jms.JMSException;

/**
 * this class is used to keep the details of the jms subscription
 * to close the connections at the end
 */
public class JMSSubscriptionDetails {

    private TopicSubscriber topicSubscriber;
    private TopicSession topicSession;
    private TopicConnection topicConnection;

    public JMSSubscriptionDetails(TopicSubscriber topicSubscriber,
                                  TopicSession topicSession,
                                  TopicConnection topicConnection) {
        this.topicSubscriber = topicSubscriber;
        this.topicSession = topicSession;
        this.topicConnection = topicConnection;

    }

    public void close() throws EventBrokerException {
        try {
            this.topicSubscriber.close();
            this.topicSession.close();
            this.topicConnection.stop();
            this.topicConnection.close();
        } catch (JMSException e) {
            throw new EventBrokerException("Can not close connections ", e);
        }

    }

    public void renewSubscription(Subscription subscription) throws JMSException {
        JMSMessageListener jmsMessageListener =
                (JMSMessageListener) this.topicSubscriber.getMessageListener();
        jmsMessageListener.renewSubscription(subscription);
    }
}
