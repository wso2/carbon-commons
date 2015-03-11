/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.event.admin.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.admin.internal.exception.EventAdminException;
import org.wso2.carbon.event.admin.internal.util.EventAdminHolder;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.event.core.topic.TopicManager;
import org.wso2.carbon.event.core.topic.TopicNode;
import org.wso2.carbon.event.core.topic.TopicRolePermission;

import java.util.Calendar;

/**
 * Provides topic related functions as a web service.
 */
public class TopicManagerAdminService {

    /**
     * Logger to log information, warning, errors.
     */
    private static Log log = LogFactory.getLog(TopicManagerAdminService.class);

    /**
     * Gets all the topics
     *
     * @return A topic node
     * @throws EventAdminException
     */
    public TopicNode getAllTopics() throws EventAdminException {
        EventBroker eventBroker = EventAdminHolder.getInstance().getEventBroker();
        try {
            return eventBroker.getTopicManager().getTopicTree();
        } catch (EventBrokerException e) {
            log.error("Error in accessing topic manager", e);
            throw new EventAdminException("Error in accessing topic manager", e);
        }
    }

    /**
     * Gets the permission roles for a topic
     * Suppressing warning as this is used as a web service
     *
     * @param topic Topic name
     * @return An array of TopicRolePermission
     * @throws EventAdminException
     */
    @SuppressWarnings("UnusedDeclaration")
    public TopicRolePermission[] getTopicRolePermissions(String topic) throws EventAdminException {
        EventBroker eventBroker = EventAdminHolder.getInstance().getEventBroker();
        try {
            return eventBroker.getTopicManager().getTopicRolePermission(topic);
        } catch (EventBrokerException e) {
            log.error("Error in accessing topic manager", e);
            throw new EventAdminException("Error in accessing topic manager", e);
        }
    }

    /**
     * Adds a new topic
     *
     * @param topic New topic name
     * @throws EventAdminException
     */
    public void addTopic(String topic) throws EventAdminException {
        EventBroker eventBroker = EventAdminHolder.getInstance().getEventBroker();
        try {
            if (!eventBroker.getTopicManager().isTopicExists(topic)) {
                eventBroker.getTopicManager().addTopic(topic);
            } else {
                throw new EventAdminException("Topic with name : " + topic + " already exists!");
            }
        } catch (EventBrokerException e) {
            log.error("Error in adding a topic", e);
            throw new EventAdminException("Error in adding a topic", e);
        }
    }

    /**
     * Updates the permissions for roles of a topic
     * Suppressing warning as this is used as a web service
     *
     * @param topic                Topic name
     * @param topicRolePermissions New roles with permissions
     * @throws EventAdminException
     */
    @SuppressWarnings("UnusedDeclaration")
    public void updatePermission(String topic, TopicRolePermission[] topicRolePermissions)
            throws EventAdminException {
        EventBroker eventBroker = EventAdminHolder.getInstance().getEventBroker();
        try {
            eventBroker.getTopicManager().updatePermissions(topic, topicRolePermissions);
        } catch (EventBrokerException e) {
            log.error("Error in updating permissions for topic", e);
            throw new EventAdminException("Error in updating permissions for topic", e);
        }
    }

    /**
     * Gets all subscriptions for a topic with limited results to return
     * Suppressing warning as this is used as a web service
     *
     * @param topic                Topic name
     * @param startingIndex        Starting index of which the results should be returned
     * @param maxSubscriptionCount The amount of results to be returned
     * @return An array of Subscriptions
     * @throws EventAdminException
     */
    @SuppressWarnings("UnusedDeclaration")
    public Subscription[] getAllWSSubscriptionsForTopic(String topic, int startingIndex,
                                                        int maxSubscriptionCount)
            throws EventAdminException {
        EventBroker eventBroker = EventAdminHolder.getInstance().getEventBroker();
        try {
            TopicManager topicManager = eventBroker.getTopicManager();
            org.wso2.carbon.event.core.subscription.Subscription[] subscriptions =
                    topicManager.getSubscriptions(topic, true);

            int resultSetSize = maxSubscriptionCount;
            if ((subscriptions.length - startingIndex) < maxSubscriptionCount) {
                resultSetSize = (subscriptions.length - startingIndex);
            }
            Subscription[] subscriptionsDTO = new Subscription[resultSetSize];

            int index = 0;
            int subscriptionIndex = 0;
            for (org.wso2.carbon.event.core.subscription.Subscription backEndSubscription : subscriptions) {
                if (startingIndex == index || startingIndex < index) {
                    subscriptionsDTO[subscriptionIndex] = adaptSubscription(backEndSubscription);
                    subscriptionIndex++;
                    if (subscriptionIndex == maxSubscriptionCount) {
                        break;
                    }
                }
                index++;
            }
            return subscriptionsDTO;
        } catch (EventBrokerException e) {
            log.error("Error in accessing topic manager", e);
            throw new EventAdminException("Error in accessing topic manager", e);
        }
    }

    /**
     * Gets all the subscriptions
     * Suppressing warning as this is used as a web service
     *
     * @param topic Topic name
     * @return An array of subscriptions
     * @throws EventAdminException
     */
    @SuppressWarnings("UnusedDeclaration")
    public Subscription[] getWsSubscriptionsForTopic(String topic) throws EventAdminException {
        EventBroker eventBroker = EventAdminHolder.getInstance().getEventBroker();
        try {
            return adaptSubscriptions(eventBroker.getTopicManager().getSubscriptions(topic, true));
        } catch (EventBrokerException e) {
            log.error("Error in accessing topic manager", e);
            throw new EventAdminException("Error in accessing topic manager", e);
        }
    }

    /**
     * Gets the total number of subscriptions for a topic
     * Suppressing warning as this is used as a web service
     *
     * @param topic Topic name
     * @return Number of subscriptions
     * @throws EventAdminException
     */
    @SuppressWarnings("UnusedDeclaration")
    public int getAllWSSubscriptionCountForTopic(String topic) throws EventAdminException {
        EventBroker eventBroker = EventAdminHolder.getInstance().getEventBroker();
        try {
            return eventBroker.getTopicManager().getSubscriptions(topic, true).length;
        } catch (EventBrokerException e) {
            log.error("Error in accessing topic manager", e);
            throw new EventAdminException("Error in accessing topic manager", e);
        }
    }

    /**
     * Gets the JMS subscriptions for a topic
     * Suppressing warning as this is used as a web service
     *
     * @param topic Topic name
     * @return An array of subscriptions
     * @throws EventAdminException
     */
    @SuppressWarnings("UnusedDeclaration")
    public Subscription[] getJMSSubscriptionsForTopic(String topic)
            throws EventAdminException {
        EventBroker eventBroker = EventAdminHolder.getInstance().getEventBroker();
        try {
            return adaptSubscriptions(eventBroker.getTopicManager().getJMSSubscriptions(topic));
        } catch (EventBrokerException e) {
            log.error("Cannot get the jms subscriptions", e);
            throw new EventAdminException("Cannot get the jms subscriptions", e);
        }
    }

    /**
     * Gets user roles through topic manager
     *
     * @return A string array of roles
     * @throws EventAdminException
     */
    public String[] getUserRoles() throws EventAdminException {
        EventBroker eventBroker = EventAdminHolder.getInstance().getEventBroker();
        try {
            return eventBroker.getTopicManager().getBackendRoles();
        } catch (EventBrokerException e) {
            log.error("Error in getting user roles from topic manager", e);
            throw new EventAdminException("Error in getting user roles from topic manager", e);
        }
    }

    /**
     * Removes a topic
     * Suppressing warning as this is used as a web service
     *
     * @param topic Topic name
     * @return true if topic existed to delete and deleted, false otherwise.
     * @throws EventAdminException
     */
    @SuppressWarnings("UnusedDeclaration")
    public boolean removeTopic(String topic) throws EventAdminException {
        EventBroker eventBroker = EventAdminHolder.getInstance().getEventBroker();
        try {
            return eventBroker.getTopicManager().removeTopic(topic);
        } catch (EventBrokerException e) {
            log.error("Error in removing a topic", e);
            throw new EventAdminException("Error in removing a topic", e);
        }
    }

    /**
     * Converting carbon event core subscription array to carbon event internal subscription array
     *
     * @param subscriptions A carbon event core subscriptions array
     * @return A carbon event internal subscription array
     */
    private Subscription[] adaptSubscriptions(
            org.wso2.carbon.event.core.subscription.Subscription[] subscriptions) {
        Subscription[] adminSubscriptions = new Subscription[subscriptions.length];
        Calendar calendar = Calendar.getInstance();
        int index = 0;
        for (org.wso2.carbon.event.core.subscription.Subscription coreSubscription : subscriptions) {
            calendar.setTime(coreSubscription.getCreatedTime());
            Subscription adminSubscription = new Subscription();
            adminSubscription.setCreatedTime(calendar);
            adminSubscription.setEventDispatcher(coreSubscription.getEventDispatcher());
            adminSubscription.setEventDispatcherName(coreSubscription.getEventDispatcherName());
            adminSubscription.setEventFilter(coreSubscription.getEventFilter());
            adminSubscription.setEventSinkURL(coreSubscription.getEventSinkURL());
            adminSubscription.setExpires(coreSubscription.getExpires());
            adminSubscription.setId(coreSubscription.getId());
            adminSubscription.setOwner(coreSubscription.getOwner());
            adminSubscription.setTopicName(coreSubscription.getTopicName());
            adminSubscription.setMode(coreSubscription.getMode());
            adminSubscriptions[index] = adminSubscription;
            index++;
        }
        return adminSubscriptions;
    }

    /**
     * Converting carbon event core subscription to carbon event internal subscription
     *
     * @param coreSubscription A carbon event core subscriptions
     * @return A carbon event internal subscription
     */
    private Subscription adaptSubscription(
            org.wso2.carbon.event.core.subscription.Subscription coreSubscription) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(coreSubscription.getCreatedTime());
        Subscription adminSubscription = new Subscription();
        adminSubscription.setCreatedTime(calendar);
        adminSubscription.setEventDispatcher(coreSubscription.getEventDispatcher());
        adminSubscription.setEventDispatcherName(coreSubscription.getEventDispatcherName());
        adminSubscription.setEventFilter(coreSubscription.getEventFilter());
        adminSubscription.setEventSinkURL(coreSubscription.getEventSinkURL());
        adminSubscription.setExpires(coreSubscription.getExpires());
        adminSubscription.setId(coreSubscription.getId());
        adminSubscription.setOwner(coreSubscription.getOwner());
        adminSubscription.setTopicName(coreSubscription.getTopicName());
        adminSubscription.setMode(coreSubscription.getMode());
        return adminSubscription;
    }
}
