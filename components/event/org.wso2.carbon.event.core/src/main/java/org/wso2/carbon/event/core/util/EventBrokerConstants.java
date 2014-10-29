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

package org.wso2.carbon.event.core.util;

public interface EventBrokerConstants {

    String EB_CONF = "event-broker.xml";

    String EB_CONF_NAMESPACE = "http://wso2.org/carbon/event/broker";
    String EB_CONF_ELE_ROOT = "eventBrokerConfig";
    String EB_CONF_ELE_EVENT_BROKER = "eventBroker";
    String EB_CONF_ELE_SUBSCRIPTION_MANAGER = "subscriptionManager";
    String EB_CONF_ELE_TOPIC_MANAGER = "topicManager";
    String EB_CONF_ELE_DELIVERY_MANAGER = "deliveryManager";
    String EB_CONF_ELE_MATCHING_MANAGER = "matchingManager";
    String EB_CONF_ELE_EVENT_PUBLISHER = "eventPublisher";

    String EB_CONF_ATTR_CLASS = "class";

    String EB_CONF_WS_SUBSCRIPTION_COLLECTION_NAME = "ws.subscriptions";
    String EB_CONF_JMS_SUBSCRIPTION_COLLECTION_NAME = "jms.subscriptions";

    String EB_RES_SUBSCRIPTION_URL = "subscriptionURL";
    String EB_RES_EVENT_DISPATCHER_NAME = "eventDispatcherName";
    String EB_RES_EXPIRS = "expires";
    String EB_RES_CREATED_TIME = "createdTime";
    String EB_RES_OWNER = "owner";
    String EB_RES_TOPIC_NAME = "topicName";
    String EB_RES_MODE = "mode";

    String EB_PERMISSION_SUBSCRIBE = "subscribe";
    String EB_PERMISSION_PUBLISH = "publish";
    public static final String EB_PERMISSION_CHANGE_PERMISSION = "changePermission";

    String WS_EVENT_DISPATCHER_NAME = "wsEventDispatcher";
    String EVENT_SINK_DISPATCHER_NAME = "EventSinkDispatcher";

    int EB_NON_PERSISTENT = 1;
    int EB_PERSISTENT = 2;

}
