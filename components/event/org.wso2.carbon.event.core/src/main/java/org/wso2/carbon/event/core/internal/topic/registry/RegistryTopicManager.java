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

package org.wso2.carbon.event.core.internal.topic.registry;

import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.event.core.internal.util.EventBrokerHolder;
import org.wso2.carbon.event.core.internal.util.JavaUtil;
import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.event.core.topic.TopicManager;
import org.wso2.carbon.event.core.topic.TopicNode;
import org.wso2.carbon.event.core.topic.TopicRolePermission;
import org.wso2.carbon.event.core.util.EventBrokerConstants;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.apache.axis2.databinding.utils.ConverterUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Pattern;

public class RegistryTopicManager implements TopicManager {

    private String topicStoragePath;
    private RegistryService registryService;


    public RegistryTopicManager(String topicStoragePath) {
        this.topicStoragePath = topicStoragePath;
        this.registryService = EventBrokerHolder.getInstance().getRegistryService();
    }

    public TopicNode getTopicTree() throws EventBrokerException {
        try {
            UserRegistry userRegistry =
                    this.registryService.getGovernanceSystemRegistry(EventBrokerHolder.getInstance().getTenantId());
            if (!userRegistry.resourceExists(topicStoragePath)) {
                userRegistry.put(topicStoragePath,
                                 userRegistry.newCollection());
            }
            Resource root = userRegistry.get(this.topicStoragePath);
            TopicNode rootTopic = new TopicNode("/", "/");
            buildTopicTree(rootTopic, (Collection) root, userRegistry);
            return rootTopic;
        } catch (RegistryException e) {
            throw new EventBrokerException(e.getMessage(), e);
        }
    }

    private void buildTopicTree(TopicNode topicNode, Collection resource, UserRegistry userRegistry)
            throws EventBrokerException {
        try {
            String[] children = resource.getChildren();
            if (children != null) {
                List<TopicNode> nodes = new ArrayList<TopicNode>();
                for (String childTopic : children) {
                    Resource childResource = userRegistry.get(childTopic);
                    if (childResource instanceof Collection) {
                        if (childTopic.endsWith("/")) {
                            childTopic = childTopic.substring(0, childTopic.length() - 2);
                        }
                        String nodeName = childTopic.substring(childTopic.lastIndexOf("/") + 1);
                        if (!nodeName.equals(EventBrokerConstants.EB_CONF_WS_SUBSCRIPTION_COLLECTION_NAME) &&
                            !nodeName.equals(EventBrokerConstants.EB_CONF_JMS_SUBSCRIPTION_COLLECTION_NAME)) {
                            childTopic =
                                    childTopic.substring(childTopic.indexOf(this.topicStoragePath)
                                                         + this.topicStoragePath.length() + 1);
                            TopicNode childNode = new TopicNode(nodeName, childTopic);
                            nodes.add(childNode);
                            buildTopicTree(childNode, (Collection) childResource, userRegistry);
                        }
                    }
                }
                topicNode.setChildren(nodes.toArray(new TopicNode[nodes.size()]));
            }
        } catch (RegistryException e) {
            throw new EventBrokerException(e.getMessage(), e);
        }
    }

    public void addTopic(String topicName) throws EventBrokerException {
        if (!validateTopicName(topicName)) {
            throw new EventBrokerException("Topic name " + topicName + " is not a valid topic name. " +
                                           "Only alphanumeric characters, hyphens (-), stars(*)," +
                                           " hash(#) ,dot(.),question mark(?)" +
                                           " and underscores (_) are allowed.");
        }

        String loggedInUser = CarbonContext.getThreadLocalCarbonContext().getUsername();

        try {
            UserRegistry userRegistry =
                    this.registryService.getGovernanceSystemRegistry(EventBrokerHolder.getInstance().getTenantId());
            String resourcePath = JavaUtil.getResourcePath(topicName, this.topicStoragePath);

            //we add the topic only if it does not exits. if the topic exists then
            //we don't do any thing.
            if (!userRegistry.resourceExists(resourcePath)) {
                Collection collection = userRegistry.newCollection();
                userRegistry.put(resourcePath, collection);

                // Grant this user (owner) rights to update permission on newly created topic
                UserRealm userRealm = EventBrokerHolder.getInstance().getRealmService().getTenantUserRealm(
                                                                     CarbonContext.getThreadLocalCarbonContext().getTenantId());

                userRealm.getAuthorizationManager().authorizeUser(
                        loggedInUser, resourcePath, EventBrokerConstants.EB_PERMISSION_CHANGE_PERMISSION);
            }
        } catch (RegistryException e) {
            throw new EventBrokerException("Can not access the config registry", e);
        } catch (UserStoreException e) {
            throw new EventBrokerException("Error while granting user " + loggedInUser +
                                           ", permission " + EventBrokerConstants.EB_PERMISSION_CHANGE_PERMISSION +
                                           ", on topic " + topicName, e);
        }
    }

    private String removeResourcePath(String topic) {
        String resourcePath = this.topicStoragePath;
        if (topic.indexOf(resourcePath) > -1) {
            topic = topic.substring(topic.indexOf(resourcePath) + resourcePath.length());
        }
        return topic;
    }




    public TopicRolePermission[] getTopicRolePermission(String topicName)
            throws EventBrokerException {
        String topicResoucePath = JavaUtil.getResourcePath(topicName, this.topicStoragePath);
        List<TopicRolePermission> topicRolePermissions = new ArrayList<TopicRolePermission>();
        UserRealm userRealm = CarbonContext.getThreadLocalCarbonContext().getUserRealm();
        String adminRole =
                EventBrokerHolder.getInstance().getRealmService().
                        getBootstrapRealmConfiguration().getAdminRoleName();
        TopicRolePermission topicRolePermission;
        try {
            for (String role : userRealm.getUserStoreManager().getRoleNames()) {
                // remove admin role and anonymous role related permissions
                if (!(role.equals(adminRole) ||
                      CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME.equals(role))) {
                    topicRolePermission = new TopicRolePermission();
                    topicRolePermission.setRoleName(role);
                    topicRolePermission.setAllowedToSubscribe(
                            userRealm.getAuthorizationManager().isRoleAuthorized(
                                    role, topicResoucePath, EventBrokerConstants.EB_PERMISSION_SUBSCRIBE));
                    topicRolePermission.setAllowedToPublish(
                            userRealm.getAuthorizationManager().isRoleAuthorized(
                                    role, topicResoucePath, EventBrokerConstants.EB_PERMISSION_PUBLISH));
                    topicRolePermissions.add(topicRolePermission);
                }
            }
            return topicRolePermissions.toArray(
                    new TopicRolePermission[topicRolePermissions.size()]);
        } catch (UserStoreException e) {
            throw new EventBrokerException("Can not access the Userstore manager ", e);
        }
    }

    public void updatePermissions(String topicName, TopicRolePermission[] topicRolePermissions)
            throws EventBrokerException {
        String topicResourcePath = JavaUtil.getResourcePath(topicName, this.topicStoragePath);
        UserRealm userRealm = CarbonContext.getThreadLocalCarbonContext().getUserRealm();
        String role;
        String loggedInUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
        try {
            if (!userRealm.getAuthorizationManager().isUserAuthorized(
                    loggedInUser, topicResourcePath,
                    EventBrokerConstants.EB_PERMISSION_CHANGE_PERMISSION)) {
                if (!JavaUtil.isAdmin(loggedInUser)) {
                    throw new EventBrokerException(" User " + loggedInUser + " can not change" +
                                                   " the permissions of " + topicName);
                }
            }
            for (TopicRolePermission topicRolePermission : topicRolePermissions) {
                role = topicRolePermission.getRoleName();
                if (topicRolePermission.isAllowedToSubscribe()) {
                    if (!userRealm.getAuthorizationManager().isRoleAuthorized(
                            role, topicResourcePath, EventBrokerConstants.EB_PERMISSION_SUBSCRIBE)) {
                        userRealm.getAuthorizationManager().authorizeRole(
                                role, topicResourcePath, EventBrokerConstants.EB_PERMISSION_SUBSCRIBE);
                    }
                } else {
                    if (userRealm.getAuthorizationManager().isRoleAuthorized(
                            role, topicResourcePath, EventBrokerConstants.EB_PERMISSION_SUBSCRIBE)) {
                        userRealm.getAuthorizationManager().denyRole(
                                role, topicResourcePath, EventBrokerConstants.EB_PERMISSION_SUBSCRIBE);
                    }
                }

                if (topicRolePermission.isAllowedToPublish()) {
                    if (!userRealm.getAuthorizationManager().isRoleAuthorized(
                            role, topicResourcePath, EventBrokerConstants.EB_PERMISSION_PUBLISH)) {
                        userRealm.getAuthorizationManager().authorizeRole(
                                role, topicResourcePath, EventBrokerConstants.EB_PERMISSION_PUBLISH);
                    }
                } else {
                    if (userRealm.getAuthorizationManager().isRoleAuthorized(
                            role, topicResourcePath, EventBrokerConstants.EB_PERMISSION_PUBLISH)) {
                        userRealm.getAuthorizationManager().denyRole(
                                role, topicResourcePath, EventBrokerConstants.EB_PERMISSION_PUBLISH);
                    }
                }
            }
        } catch (UserStoreException e) {
            throw new EventBrokerException("Can not access the user store manager", e);
        }
    }

    public String getTopicStoragePath() {
        return topicStoragePath;
    }

    public void setTopicStoragePath(String topicStoragePath) {
        this.topicStoragePath = topicStoragePath;
    }


    public Subscription[] getSubscriptions(String topicName,
                                           boolean withChildren) throws EventBrokerException {

        List<Subscription> subscriptions = new ArrayList<Subscription>();
        Queue<String> pathsQueue = new LinkedList<String>();
        String resourcePath = JavaUtil.getResourcePath(topicName, this.topicStoragePath);

        pathsQueue.add(resourcePath);
        while (!pathsQueue.isEmpty()) {
            addSubscriptions(pathsQueue.remove(), subscriptions, pathsQueue, withChildren);
        }

        return subscriptions.toArray(new Subscription[subscriptions.size()]);

    }

    public Subscription[] getJMSSubscriptions(String topicName) throws EventBrokerException {
        try {
            Subscription[] subscriptionsArray = new Subscription[0];

            UserRegistry userRegistry =
                    this.registryService.getGovernanceSystemRegistry(EventBrokerHolder.getInstance().getTenantId());
            String resourcePath = JavaUtil.getResourcePath(topicName, this.topicStoragePath);
            if (!resourcePath.endsWith("/")) {
                resourcePath = resourcePath + "/";
            }
            resourcePath = resourcePath + EventBrokerConstants.EB_CONF_JMS_SUBSCRIPTION_COLLECTION_NAME;

            // Get subscriptions
            if (userRegistry.resourceExists(resourcePath)) {
                Collection subscriptionCollection = (Collection) userRegistry.get(resourcePath);
                subscriptionsArray =
                        new Subscription[subscriptionCollection.getChildCount()];

                int index = 0;
                for (String subs : subscriptionCollection.getChildren()) {
                    Collection subscription = (Collection) userRegistry.get(subs);

                    Subscription subscriptionDetails = new Subscription();
                    subscriptionDetails.setId(subscription.getProperty("Name"));
                    subscriptionDetails.setOwner(subscription.getProperty("Owner"));
                    subscriptionDetails.setCreatedTime(ConverterUtil.convertToDate(subscription.getProperty("createdTime")));

                    subscriptionsArray[index++] = subscriptionDetails;
                }
            }

            return subscriptionsArray;
        } catch (RegistryException e) {
            throw new EventBrokerException("Can not read the registry resouces ", e);
        }
    }

    private void addSubscriptions(String resourcePath,
                                  List<Subscription> subscriptions,
                                  Queue<String> pathsQueue,
                                  boolean withChildren) throws EventBrokerException {

        try {
            UserRegistry userRegistry =
                    this.registryService.getGovernanceSystemRegistry(EventBrokerHolder.getInstance().getTenantId());
            String subscriptionsPath = getSubscriptionsPath(resourcePath);

            //first if there are subscriptions for this topic add them. else go to the other folders.

            if (userRegistry.resourceExists(subscriptionsPath)) {
                Collection collection = (Collection) userRegistry.get(subscriptionsPath);
                for (String subscriptionPath : collection.getChildren()) {
                    Resource subscriptionResource = userRegistry.get(subscriptionPath);
                    Subscription subscription = JavaUtil.getSubscription(subscriptionResource);
                    subscription.setTopicName(removeResourcePath(resourcePath));

                    if (subscriptionPath.endsWith("/")) {
                        subscriptionPath = subscriptionsPath.substring(0, subscriptionPath.lastIndexOf("/"));
                    }
                    subscription.setId(subscriptionPath.substring(subscriptionPath.lastIndexOf("/") + 1));
                    subscriptions.add(subscription);
                }
            }

            // add child subscriptions
            if (withChildren) {
                Collection childResouces = (Collection) userRegistry.get(resourcePath);
                for (String childResoucePath : childResouces.getChildren()) {
                    if ((childResoucePath.indexOf(EventBrokerConstants.EB_CONF_WS_SUBSCRIPTION_COLLECTION_NAME) < 0) &&
                        (childResoucePath.indexOf(EventBrokerConstants.EB_CONF_JMS_SUBSCRIPTION_COLLECTION_NAME) < 0)) {
                        // i.e. this folder is a topic folder
                        pathsQueue.add(childResoucePath);
                    }
                }
            }

        } catch (RegistryException e) {
            throw new EventBrokerException("Can not access the registry", e);
        }
    }

    private String getSubscriptionsPath(String topicName) {

        if (!topicName.endsWith("/")) {
            topicName += "/";
        }

        topicName += EventBrokerConstants.EB_CONF_WS_SUBSCRIPTION_COLLECTION_NAME;
        return topicName;
    }

    private boolean validateTopicName(String topicName) {
        return Pattern.matches("[[a-zA-Z]+[^(\\x00-\\x80)]+[0-9_\\-/#*.?&\\s()]+]+",topicName);
    }

    /**
     * Get array of backend roles excluding admin and wso2.anonymous.role roles
     *
     * @return array of roles
     * @throws EventBrokerException if fails to get roles
     */
    public String[] getBackendRoles() throws EventBrokerException {
        UserRealm userRealm = CarbonContext.getThreadLocalCarbonContext().getUserRealm();
        try {
            String adminRole =
                    EventBrokerHolder.getInstance().getRealmService().
                            getBootstrapRealmConfiguration().getAdminRoleName();
            String[] allRoles = userRealm.getUserStoreManager().getRoleNames();
            // check if more roles available than admin role and anonymous role
            if (allRoles != null && allRoles.length > 1) {
                String[] rolesExceptAdminRole = new String[allRoles.length - 1];
                int index = 0;
                for (String role : allRoles) {
                    if (!(role.equals(adminRole) ||
                          CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME.equals(role))) {
                        rolesExceptAdminRole[index] = role;
                        index++;
                    }
                }
                return rolesExceptAdminRole;
            } else {
                return new String[0];
            }

        } catch (UserStoreException e) {
            throw new EventBrokerException("Unable to getRoles from user store", e);
        }
    }

    public boolean removeTopic(String topicName) throws EventBrokerException {

        try {
            UserRegistry userRegistry =
                    this.registryService.getGovernanceSystemRegistry(EventBrokerHolder.getInstance().getTenantId());
            String resourcePath = JavaUtil.getResourcePath(topicName, this.topicStoragePath);

            if (userRegistry.resourceExists(resourcePath)) {
                userRegistry.delete(resourcePath);
                return true;
            } else {
                return false;
            }
        } catch (RegistryException e) {
            throw new EventBrokerException("Can not access the config registry");
        }
    }

    @Override
    public boolean isTopicExists(String topicName) throws EventBrokerException {
          try {
            UserRegistry userRegistry =
                    this.registryService.getGovernanceSystemRegistry(EventBrokerHolder.getInstance().getTenantId());
            String resourcePath = JavaUtil.getResourcePath(topicName, this.topicStoragePath);
            return userRegistry.resourceExists(resourcePath);
        } catch (RegistryException e) {
            throw new EventBrokerException("Can not access the config registry");
        }
    }
}
