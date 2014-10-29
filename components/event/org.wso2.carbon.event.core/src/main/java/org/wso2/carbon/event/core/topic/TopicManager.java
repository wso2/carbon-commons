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

public interface TopicManager {

    public TopicNode getTopicTree() throws EventBrokerException;

    public void addTopic(String topicName) throws EventBrokerException;

    public TopicRolePermission[] getTopicRolePermission(String topicName) throws EventBrokerException;

    public void updatePermissions(String topicName, TopicRolePermission[] topicRolePermissions) throws EventBrokerException;

    public Subscription[] getSubscriptions(String topicName, boolean withChildren) throws EventBrokerException;

    public Subscription[] getJMSSubscriptions(String topicName) throws EventBrokerException;

    public String[] getBackendRoles() throws EventBrokerException;

    public boolean removeTopic(String topicName) throws EventBrokerException;

    public boolean isTopicExists(String topicName) throws EventBrokerException;

}
