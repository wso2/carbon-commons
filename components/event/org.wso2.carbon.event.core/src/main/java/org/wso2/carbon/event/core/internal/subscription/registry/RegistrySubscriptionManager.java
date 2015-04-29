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

import org.apache.axis2.databinding.utils.ConverterUtil;
import org.wso2.carbon.event.core.exception.EventBrokerConfigurationException;
import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.event.core.internal.util.EventBrokerHolder;
import org.wso2.carbon.event.core.internal.util.JavaUtil;
import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.event.core.subscription.SubscriptionManager;
import org.wso2.carbon.event.core.util.EventBrokerConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Subscription manager implementation using registry.
 * <p/>
 * This subscription manager stores the subscriptions in the registry patch calculated as given
 * subscriptionStoragePath(read from the configuration file) + "/" + topicName +
 * "/system.subscriptions/" + subscriptionID
 * <p/>
 * In-order to get the subscriptions quickly it also stores the subscription details in a resource
 * called topicIndex.
 * <p/>
 * Topic index resource contains subscriptionID , topicName.
 * <p/>
 * When getting the subscriptions we calculate the subscription stored path using above two
 * parameters.
 */
public class RegistrySubscriptionManager implements SubscriptionManager {

    /**
     * Star wildcard - can substitute for exactly one word.
     */
    private static final  char STAR_WILDCARD = '*';

    /**
     * Hash wildcard - can substitute for zero or more words.
     */
    private static final char HASH_WILDCARD = '#';

    /**
     * Registry service to access the registry. this is get from the EBBrokerHolder
     */
    private RegistryService registryService;

    /**
     * Root patch which used to start the subscription related details.
     */
    private String topicStoragePath;

    /**
     * Topic index resource path.
     */
    private String indexStoragePath;

    /**
     * Adds topic storage path and index storage path to user registry.
     *
     * @param topicStoragePath topic storage path. the path where topics will be stored.
     * @param indexStoragePath index storage path.
     * @throws EventBrokerConfigurationException
     */
    public RegistrySubscriptionManager(String topicStoragePath, String indexStoragePath)
            throws EventBrokerConfigurationException {

        this.registryService = EventBrokerHolder.getInstance().getRegistryService();
        this.topicStoragePath = topicStoragePath;
        this.indexStoragePath = indexStoragePath;

        // creates the the subscription index
        // when creating subscriptions we going to add entries to this this resource
        try {
            UserRegistry userRegistry =
                    this.registryService.getGovernanceSystemRegistry(EventBrokerHolder.getInstance()
                                                                             .getTenantId());

            //create the topic storage path if it does not exists
            if (!userRegistry.resourceExists(this.topicStoragePath)) {
                userRegistry.put(this.topicStoragePath, userRegistry.newCollection());
            }

            // we need to create the index here only it is not exists.
            if (!userRegistry.resourceExists(this.indexStoragePath)) {
                userRegistry.put(this.indexStoragePath, userRegistry.newResource());
            }

        } catch (RegistryException e) {
            throw new EventBrokerConfigurationException("Cannot access the registry ", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addSubscription(Subscription subscription) throws EventBrokerException {
        try {
            UserRegistry userRegistry =
                    this.registryService.getGovernanceSystemRegistry(EventBrokerHolder.getInstance()
                                                                             .getTenantId());
            String resourcePath =
                    getResourcePath(subscription.getId(), subscription.getTopicName());

            Resource resource = userRegistry.newResource();
            resource.setProperty(EventBrokerConstants.EB_RES_SUBSCRIPTION_URL, subscription
                    .getEventSinkURL());
            resource.setProperty(EventBrokerConstants.EB_RES_EVENT_DISPATCHER_NAME, subscription
                    .getEventDispatcherName());

            if (subscription.getExpires() != null) {
                resource.setProperty(EventBrokerConstants.EB_RES_EXPIRS, ConverterUtil
                        .convertToString(subscription
                                                 .getExpires()));
            }
            resource.setProperty(EventBrokerConstants.EB_RES_OWNER, subscription.getOwner());
            resource.setProperty(EventBrokerConstants.EB_RES_TOPIC_NAME, subscription
                    .getTopicName());
            resource.setProperty(EventBrokerConstants.EB_RES_CREATED_TIME,
                                 System.currentTimeMillis() + "");
            resource.setProperty(EventBrokerConstants.EB_RES_MODE, JavaUtil
                    .getSubscriptionMode(subscription
                                                 .getTopicName()));

            //set the other properties of the subscription.
            Map<String, String> properties = subscription.getProperties();
            for (String key : properties.keySet()) {
                resource.setProperty(key, properties.get(key));
            }

            userRegistry.put(resourcePath, resource);

            // add the subscription index
            String fullPath = this.indexStoragePath;
            Resource topicIndexResource;
            if (userRegistry.resourceExists(fullPath)) {
                topicIndexResource = userRegistry.get(fullPath);
                topicIndexResource.addProperty(subscription.getId(), subscription.getTopicName());
            } else {
                topicIndexResource = userRegistry.newResource();
                topicIndexResource.addProperty(subscription.getId(), subscription.getTopicName());
            }
            userRegistry.put(fullPath, topicIndexResource);

        } catch (RegistryException e) {
            throw new EventBrokerException("Cannot save to registry ", e);
        }

    }

    /**
     * Calculates the resource stored path using subscription id and the topic name.
     *
     * @param subscriptionID the subscription ID
     * @param topicName      topic name
     * @return the resource path
     */
    private String getResourcePath(String subscriptionID, String topicName) {
        String resourcePath = this.topicStoragePath;

        // first convert the . to /
        topicName = topicName.replaceAll("\\.", RegistryConstants.PATH_SEPARATOR);

        if (!topicName.startsWith(RegistryConstants.PATH_SEPARATOR)) {
            resourcePath = resourcePath + RegistryConstants.PATH_SEPARATOR;
        }

        // this topic name can have # and * marks if the user wants to subscribes to the
        // child topics as well. but we consider the topic here as the topic name just before any
        // special character.
        // eg. if topic name is myTopic/*/* then topic name is myTopic
        if (topicName.indexOf(STAR_WILDCARD) > -1) {
            topicName = topicName.substring(0, topicName.indexOf(STAR_WILDCARD));
        } else {
            if (topicName.indexOf(HASH_WILDCARD) > -1) {
                topicName = topicName.substring(0, topicName.indexOf(HASH_WILDCARD));
            }
        }

        resourcePath = resourcePath + topicName;

        if (!resourcePath.endsWith(RegistryConstants.PATH_SEPARATOR)) {
            resourcePath = resourcePath + RegistryConstants.PATH_SEPARATOR;
        }

        resourcePath = resourcePath + EventBrokerConstants.EB_CONF_WS_SUBSCRIPTION_COLLECTION_NAME +
                       RegistryConstants.PATH_SEPARATOR + subscriptionID;
        return resourcePath;
    }

    /**
     * Calculates the JMS subscription stored path for a WSSubscription using subscription id and
     * the topic name.
     *
     * @param subscriptionID the subscription ID
     * @param topicName      the topic name
     * @return the JMS subscription resource path for a subscription
     */
    private String getJMSSubResourcePath(String subscriptionID, String topicName) {
        String resourcePath = this.topicStoragePath;

        // first convert the . to /
        topicName = topicName.replaceAll("\\.", RegistryConstants.PATH_SEPARATOR);

        if (!topicName.startsWith(RegistryConstants.PATH_SEPARATOR)) {
            resourcePath = resourcePath + RegistryConstants.PATH_SEPARATOR;
        }

        // this topic name can have # and * marks if the user wants to subscribes to the
        // child topics as well. but we consider the topic here as the topic name just before any
        // special character.
        // eg. if topic name is myTopic/*/* then topic name is myTopic
        if (topicName.indexOf(STAR_WILDCARD) > -1) {
            topicName = topicName.substring(0, topicName.indexOf(STAR_WILDCARD));
        } else if (topicName.indexOf(HASH_WILDCARD) > -1) {
            topicName = topicName.substring(0, topicName.indexOf(HASH_WILDCARD));
        }

        resourcePath = resourcePath + topicName;

        if (!resourcePath.endsWith(RegistryConstants.PATH_SEPARATOR)) {
            resourcePath = resourcePath + RegistryConstants.PATH_SEPARATOR;
        }
        resourcePath = resourcePath +
                       (EventBrokerConstants.EB_CONF_JMS_SUBSCRIPTION_COLLECTION_NAME +
                        RegistryConstants.PATH_SEPARATOR + subscriptionID);
        return resourcePath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Subscription> getAllSubscriptions() throws EventBrokerException {
        List<Subscription> subscriptions = new ArrayList<Subscription>();

        try {
            UserRegistry userRegistry =
                    this.registryService.getGovernanceSystemRegistry(EventBrokerHolder.getInstance()
                                                                             .getTenantId());
            if (userRegistry.resourceExists(this.indexStoragePath)) {
                Resource topicIndexResource = userRegistry.get(this.indexStoragePath);
                Properties savedSubscriptions = topicIndexResource.getProperties();

                Resource subscriptionResource;
                Subscription subscription;
                String subscriptionID;
                String topicName;
                for (Enumeration e = savedSubscriptions.propertyNames(); e.hasMoreElements(); ) {
                    subscriptionID = (String) e.nextElement();
                    // when the registry is remotely mount to another registry. then registry
                    // automatically added some properties stays with registry we need to skip them.
                    if (!subscriptionID.startsWith("registry")) {
                        topicName = topicIndexResource.getProperty(subscriptionID);
                        subscriptionResource =
                                userRegistry.get(getResourcePath(subscriptionID, topicName));
                        subscription = JavaUtil.getSubscription(subscriptionResource);
                        subscription.setId(subscriptionID);
                        subscription.setTopicName(topicName);
                        subscription.setTenantId(EventBrokerHolder.getInstance().getTenantId());
                        subscriptions.add(subscription);
                    }
                }
            }

        } catch (RegistryException e) {
            throw new EventBrokerException("Cannot access the registry ", e);
        }
        return subscriptions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Subscription getSubscription(String id) throws EventBrokerException {

        try {
            UserRegistry userRegistry =
                    this.registryService.getGovernanceSystemRegistry(EventBrokerHolder.getInstance()
                                                                             .getTenantId());
            Resource topicIndexResource = userRegistry.get(this.indexStoragePath);
            String subscriptionPath = getResourcePath(id, topicIndexResource.getProperty(id));
            if (subscriptionPath != null && userRegistry.resourceExists(subscriptionPath)) {
                Resource subscriptionResource = userRegistry.get(subscriptionPath);
                Subscription subscription = JavaUtil.getSubscription(subscriptionResource);
                subscription.setTenantId(EventBrokerHolder.getInstance().getTenantId());
                return subscription;
            } else {
                return null;
            }
        } catch (RegistryException e) {
            throw new EventBrokerException("Cannot access the registry ", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void renewSubscription(Subscription subscription) throws EventBrokerException {

        try {
            UserRegistry userRegistry =
                    this.registryService.getGovernanceSystemRegistry(EventBrokerHolder.getInstance()
                                                                             .getTenantId());
            Resource topicIndexResource = userRegistry.get(this.indexStoragePath);
            String topicName = topicIndexResource.getProperty(subscription.getId());
            String subscriptionPath = getResourcePath(subscription.getId(), topicName);
            if (subscriptionPath != null) {
                Resource subscriptionResource = userRegistry.get(subscriptionPath);
                // Set the expires property only if it has been set again.
                if (subscription.getExpires() != null) {
                    subscriptionResource.setProperty(
                            EventBrokerConstants.EB_RES_EXPIRS,
                            ConverterUtil.convertToString(subscription.getExpires()));
                }
                // There might be updated subscription properties. Set them too.
                Subscription currentSubscription = JavaUtil.getSubscription(subscriptionResource);
                //Since the subscription renewal does not include name parameters setting up them
                //https://wso2.org/jira/browse/ESBJAVA-1021
                subscription.setTopicName(currentSubscription.getTopicName());
                Map<String, String> properties = currentSubscription.getProperties();
                for (String key : properties.keySet()) {
                    subscriptionResource.removeProperty(key);
                }
                properties = subscription.getProperties();
                for (String key : properties.keySet()) {
                    subscriptionResource.setProperty(key, properties.get(key));
                }
                userRegistry.put(subscriptionPath, subscriptionResource);
            } else {
                throw new EventBrokerException("Cannot find the resource to the subscription with" +
                                               " id " + subscription.getId());
            }
        } catch (RegistryException e) {
            throw new EventBrokerException("Cannot access the registry ", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unSubscribe(String subscriptionID) throws EventBrokerException {
        try {
            UserRegistry userRegistry =
                    this.registryService.getGovernanceSystemRegistry(EventBrokerHolder.getInstance()
                                                                             .getTenantId());
            String fullPath = this.indexStoragePath;
            if (userRegistry.resourceExists(fullPath)) {
                Resource topicIndexResource = userRegistry.get(fullPath);

                String topicName = topicIndexResource.getProperty(subscriptionID);
                // delete the subscriptions resource
                // if the registry is read only there can be situations where the the subscriptions
                // is not saved to registry and hence the topic name
                if (topicName != null) {
                    String resourcePath =  getResourcePath(subscriptionID, topicName);
                    if (userRegistry.resourceExists(resourcePath)) {
                        userRegistry.delete(resourcePath);
                    }
                    String jMSResourcePath =  getJMSSubResourcePath(subscriptionID, topicName);
                    if (userRegistry.resourceExists(jMSResourcePath)) {
                        userRegistry.delete(jMSResourcePath);
                    }
                }

                topicIndexResource.removeProperty(subscriptionID);

                userRegistry.put(fullPath, topicIndexResource);
            }

        } catch (RegistryException e) {
            throw new EventBrokerException("Cannot access the registry ", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTopicStoragePath() throws EventBrokerException {
        return topicStoragePath;
    }
}
