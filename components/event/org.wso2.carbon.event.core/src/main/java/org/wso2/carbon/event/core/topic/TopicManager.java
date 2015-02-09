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

package org.wso2.carbon.event.core.topic;

import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.event.core.subscription.Subscription;

/**
 * An interface for topic manager. Includes actions which are related to topics.
 */
public interface TopicManager {

    /**
     * Gets the topic tree.
     *
     * @return a topic node which includes the name and its sub topics
     * @throws EventBrokerException
     */
    public TopicNode getTopicTree() throws EventBrokerException;

    /**
     * Adds a new topic
     *
     * @param topicName topic name
     * @throws EventBrokerException
     */
    public void addTopic(String topicName) throws EventBrokerException;

    /**
     * Gets the permissions for topic
     *
     * @param topicName topic name
     * @return an array of permissions for topics
     * @throws EventBrokerException
     */
    public TopicRolePermission[] getTopicRolePermission(String topicName) throws EventBrokerException;

    /**
     * Update permissions for a topic
     *
     * @param topicName topic name
     * @param topicRolePermissions array of permissions for topics
     * @throws EventBrokerException
     */
    public void updatePermissions(String topicName, TopicRolePermission[] topicRolePermissions) throws EventBrokerException;

    /**
     * Gets subscriptions for a topic
     *
     * @param topicName topic name
     * @param withChildren include topics with children. i.e subtopics
     * @return an array of subscriptions
     * @throws EventBrokerException
     */
    public Subscription[] getSubscriptions(String topicName, boolean withChildren) throws EventBrokerException;

    /**
     * Gets the JMS subscriptions for a given topic
     *
     * @param topicName topic name
     * @return an array of subscriptions
     * @throws EventBrokerException
     */
    public Subscription[] getJMSSubscriptions(String topicName) throws EventBrokerException;

    /**
     * Get array of backend roles excluding admin and wso2.anonymous.role roles
     *
     * @return an array of roles
     * @throws EventBrokerException
     */
    public String[] getBackendRoles() throws EventBrokerException;

    /**
     * Removes a topic
     *
     * @param topicName topic name
     * @return true if topic removal succeeded, false otherwise.
     * @throws EventBrokerException
     */
    public boolean removeTopic(String topicName) throws EventBrokerException;

    /**
     * Checks whether topic exists
     *
     * @param topicName topic name
     * @return true if topic exists, false otherwise.
     * @throws EventBrokerException
     */
    public boolean isTopicExists(String topicName) throws EventBrokerException;

}
