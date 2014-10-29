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

package org.wso2.carbon.event.core.internal.subscription.registry;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.wso2.carbon.event.core.exception.EventBrokerConfigurationException;
import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.event.core.internal.util.JavaUtil;
import org.wso2.carbon.event.core.subscription.SubscriptionManager;
import org.wso2.carbon.event.core.subscription.SubscriptionManagerFactory;
import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.event.core.util.EventBrokerConstants;

import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * factory class for registry based subscription manager
 */
public class RegistrySubscriptionManagerFactory implements SubscriptionManagerFactory {

    public static final String EB_ELE_TOPIC_STORAGE_PATH = "topicStoragePath";
    public static final String EB_ELE_INDEX_STORAGE_PATH = "indexStoragePath";

    public static final String EB_ELE_SUBSCRIPTION = "subscription";
    public static final String EB_ATTR_ID = "id";
    public static final String EB_ELE_TOPIC = "topic";
    public static final String EB_ELE_EVENT_SINK_URL = "eventSinkURL";
    public static final String EB_ELE_EXPIRES = "expires";
    public static final String EB_ELE_OWNER = "owner";

    public SubscriptionManager getSubscriptionManager(OMElement config)
            throws EventBrokerConfigurationException {

        String topicStoragePath = JavaUtil.getValue(config, EB_ELE_TOPIC_STORAGE_PATH);
        String indexStoragePath = JavaUtil.getValue(config, EB_ELE_INDEX_STORAGE_PATH);

        RegistrySubscriptionManager registrySubscriptionManager =
                new RegistrySubscriptionManager(topicStoragePath, indexStoragePath);

        // add the static subscriptions
       Iterator subscriptionsIter =
               config.getChildrenWithName(new QName(EventBrokerConstants.EB_CONF_NAMESPACE, EB_ELE_SUBSCRIPTION));
        OMElement subscriptionElement = null;
        Subscription subscription = null;
        String id = null;

        for (;subscriptionsIter.hasNext();) {
            subscriptionElement = (OMElement) subscriptionsIter.next();
            subscription = new Subscription();
            subscription.setId(subscriptionElement.getAttributeValue(new QName(null, EB_ATTR_ID)));
            // add this subscription only if it does not exists.
            try {
                if (registrySubscriptionManager.getSubscription(subscription.getId()) == null){
                    subscription.setTopicName(JavaUtil.getValue(subscriptionElement, EB_ELE_TOPIC));
                    subscription.setOwner(JavaUtil.getValue(subscriptionElement, EB_ELE_OWNER));
                    subscription.setEventSinkURL(
                            JavaUtil.getValue(subscriptionElement, EB_ELE_EVENT_SINK_URL));
                    subscription.setExpires(
                            ConverterUtil.convertToDateTime(
                                    JavaUtil.getValue(subscriptionElement, EB_ELE_EXPIRES)));
                    if (subscription.getEventSinkURL().startsWith("sqs://")) {
                        subscription.setEventDispatcherName(EventBrokerConstants.EVENT_SINK_DISPATCHER_NAME);
                    } else {
                        subscription.setEventDispatcherName(EventBrokerConstants.WS_EVENT_DISPATCHER_NAME);
                    }
                    registrySubscriptionManager.addSubscription(subscription);

                }
            } catch (EventBrokerException e) {
                throw new EventBrokerConfigurationException(
                              "Can not access the registry to read subscrptions ", e);
            }
        }

        return registrySubscriptionManager;
    }
}
