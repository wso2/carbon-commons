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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.event.core.Message;
import org.wso2.carbon.event.core.delivery.DeliveryManager;
import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.event.core.internal.util.EventBrokerHolder;
import org.wso2.carbon.event.core.notify.NotificationManager;
import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * this class provides a JMS based delivary manager
 */
public abstract class JMSDeliveryManager implements DeliveryManager {

     private static final Log log = LogFactory.getLog(JMSDeliveryManager.class);

    private NotificationManager notificationManager;

    private Map<String, JMSSubscriptionDetails> subscriptionIDSessionDetailsMap;

    protected JMSDeliveryManager() {
        this.subscriptionIDSessionDetailsMap
                = new ConcurrentHashMap<String, JMSSubscriptionDetails>();
    }

    protected abstract Properties getInitialContextProperties(String userName, String password);

    protected abstract TopicConnectionFactory getTopicConnectionFactory(
            InitialContext initialContext) throws EventBrokerException;

    protected abstract String getTopicName(String topicName);

    private boolean isDeactivated;

    public TopicConnection getTopicConnection(String userName) throws EventBrokerException {
        InitialContext initialContext = null;
        try {

            initialContext = new InitialContext(getInitialContextProperties(userName,
                    EventBrokerHolder.getInstance().getQpidServerDetails().getAccessKey()));
            TopicConnectionFactory topicConnectionFactory =
                    getTopicConnectionFactory(initialContext);

            TopicConnection topicConnection = topicConnectionFactory.createTopicConnection();
            topicConnection.start();
            return topicConnection;
        } catch (NamingException e) {
            throw new EventBrokerException("Can not create the initial context", e);
        } catch (JMSException e) {
            throw new EventBrokerException("Can not create topic connection", e);
        } catch (Exception e){
            throw new EventBrokerException("Can not create topic connection", e);
        } finally {
            if (initialContext != null){
                try {
                    initialContext.close();
                } catch (NamingException e) {
                    log.error("Can not close the inital context factory ", e);
                }
            }
        }
    }

    public void subscribe(Subscription subscription) throws EventBrokerException {

        if (isDeactivated()){
            return;
        }

        // in a multi tenant envirionment deployment synchronizer may creates subscriptions before
        // the event observer get activated.
        if (this.subscriptionIDSessionDetailsMap.containsKey(subscription.getId())){
            log.warn("There is an subscription already exists for the subscription with id " + subscription.getId());
            return;
        }
        JMSMessageListener jmsMessageListener =
                new JMSMessageListener(this.notificationManager, subscription);
        try {
            TopicConnection topicConnection = getTopicConnection(subscription.getOwner());
            TopicSession topicSession =
                    topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

            String topicName = "";
            if (subscription.getTenantDomain() != null && (!subscription.getTenantDomain().equals(org.wso2.carbon.base.
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME))) {

                if (!subscription.getTopicName().startsWith("/")) {
                    topicName = getTopicName(subscription.getTenantDomain() + "/" + subscription.getTopicName());
                } else {
                    topicName = getTopicName(subscription.getTenantDomain() + subscription.getTopicName());
                }
            } else {
                topicName = getTopicName(subscription.getTopicName());
            }
            Topic topic = topicSession.createTopic(topicName);
            //Some times we are not getting the proper topic with the required syntax, if it is not
            //appropriate we need to check and add the BURL syntax to fix the issue https://wso2.org/jira/browse/MB-185
            if(!topic.toString().startsWith("topic://amq.topic")){
                topic = topicSession.createTopic("BURL:"+topicName);
            }
            TopicSubscriber topicSubscriber =
                             topicSession.createDurableSubscriber(topic, subscription.getId());
            topicSubscriber.setMessageListener(jmsMessageListener);

            this.subscriptionIDSessionDetailsMap.put(subscription.getId(),
                    new JMSSubscriptionDetails(topicSubscriber, topicSession, topicConnection));
        } catch (JMSException e) {
            throw new EventBrokerException("Can not subscribe to topic " + subscription.getTopicName() + " " + e.getMessage(), e);
        }
    }

    public void setNotificationManager(NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    public void publish(Message message, String topicName, int deliveryMode) throws EventBrokerException {

        if (isDeactivated()){
            return;
        }

        try {
            String userName = getLoggedInUserName();
            if ((userName == null) || (userName.equals(""))) {
                // use the system user name
                userName = CarbonConstants.REGISTRY_SYSTEM_USERNAME;
            }

            TopicConnection topicConnection = getTopicConnection(userName);
            TopicSession topicSession =
                    topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

            String tenantDomain= EventBrokerHolder.getInstance().getTenantDomain();
            if (tenantDomain != null && (!tenantDomain.equals(org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME))) {
                if (!topicName.startsWith("/")) {
                    topicName = getTopicName(tenantDomain + "/" + topicName);
                } else {
                    topicName = getTopicName(tenantDomain + topicName);
                }
            } else {
                topicName = getTopicName(topicName);
            }

            Topic topic = topicSession.createTopic(topicName);
            //Some times we are not getting the proper topic with the required syntax, if it is not
            //appropriate we need to check and add the BURL syntax to fix the issue https://wso2.org/jira/browse/MB-185
            if (!topic.toString().startsWith("topic://amq.topic")) {
                topic = topicSession.createTopic("BURL:" + topicName);
            }
            TopicPublisher topicPublisher = topicSession.createPublisher(topic);
            topicPublisher.setDeliveryMode(deliveryMode);
            TextMessage textMessage =
                    topicSession.createTextMessage(message.getMessage().toString());

            Map<String, String> properties = message.getProperties();
            for (String key : properties.keySet()){
                textMessage.setStringProperty(key, properties.get(key));
            }

            // saving the domain to be used send with the soap header
            if (CarbonContext.getThreadLocalCarbonContext().getTenantDomain() != null){
                textMessage.setStringProperty(MultitenantConstants.TENANT_DOMAIN_HEADER_NAME,
                                            CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
            }

            topicPublisher.publish(textMessage);
            topicPublisher.close();
            topicSession.close();
            topicConnection.stop();
            topicConnection.close();
        } catch (JMSException e) {
            throw new EventBrokerException("Can not publish to topic " + topicName + " " + e.getMessage(), e);
        }
    }

    public void unSubscribe(String id) throws EventBrokerException {

        JMSSubscriptionDetails jmsSubscriptionDetails =
                this.subscriptionIDSessionDetailsMap.remove(id);
        if (jmsSubscriptionDetails != null){
            jmsSubscriptionDetails.close();
        }
    }

    public void renewSubscription(Subscription subscription) throws EventBrokerException {
        JMSSubscriptionDetails jmsSubscriptionDetails =
                this.subscriptionIDSessionDetailsMap.get(subscription.getId());
        try {
            if (jmsSubscriptionDetails != null){
               jmsSubscriptionDetails.renewSubscription(subscription);
            }
        } catch (JMSException e) {
            throw new EventBrokerException("Can not renew the subscription ", e);
        }

    }

    public void cleanUp() throws EventBrokerException {
        setDeactivated(true);
        try {
            // allowed to countine with the threads which going to publish events
            Thread.sleep(3000);
        } catch (InterruptedException e) {}

        for (JMSSubscriptionDetails jmsSubscriptionDetails :
                this.subscriptionIDSessionDetailsMap.values()) {
            jmsSubscriptionDetails.close();
        }
    }

    private  String getLoggedInUserName() {
        String userName = "";
        if (CarbonContext.getThreadLocalCarbonContext().getTenantId() != 0) {
            userName = CarbonContext.getThreadLocalCarbonContext().getUsername() + "@"
                    + CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        } else {
            userName = CarbonContext.getThreadLocalCarbonContext().getUsername();
        }
        return userName;
    }

    public synchronized boolean isDeactivated() {
        return isDeactivated;
    }

    public synchronized void setDeactivated(boolean deactivated) {
        isDeactivated = deactivated;
    }

    public void initializeTenant() throws EventBrokerException {
        // there is no tenant specific initialization for jms deliveary manager
    }
}
