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
import org.wso2.carbon.event.core.util.EventBrokerConstants;
import org.wso2.carbon.event.core.internal.util.EventBrokerHolder;
import org.wso2.carbon.event.core.internal.util.JavaUtil;
import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.event.core.subscription.SubscriptionManager;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.*;

/**
 * subscription manager implementation using registry.
 * this subscription manager stores the subscriptions in the registry patch calcuated
 * as given
 *  subscriptionStoragePath(read from the configuration file) + "/" + topicName
 *  + "/system.subscriptions/" + subscriptionID
 *
 * inorder to get the subscriptions quickly it also stores the subscription details in a
 * resouce called topicIndex.
 * topic index resource contains
 * subscriptionID , topicName
 * when getting the subscriptions we callcualte the subscription stored path using above two
 * parameters.
 */
public class RegistrySubscriptionManager implements SubscriptionManager {

    /**
     * registry service to access the registry. this is get from the EBBrokerHolder
     */
    private RegistryService registryService;

    /**
     * root patch which used to start the subscription related deatils.
     */
    private String topicStoragePath;

    /**
     * topic index resource path.
     */
    private String indexStoragePath;

    public RegistrySubscriptionManager(String topicStoragePath, String indexStoragePath)
            throws EventBrokerConfigurationException {

        this.registryService = EventBrokerHolder.getInstance().getRegistryService();
        this.topicStoragePath = topicStoragePath;
        this.indexStoragePath = indexStoragePath;

        // creates the the subscription intex
        // when creating subscriptions we going to add entries to this this resource
        try {
            UserRegistry userRegistry =
                    this.registryService.getGovernanceSystemRegistry(EventBrokerHolder.getInstance().getTenantId());

            //create the topic storage path if it does not exists
            if (!userRegistry.resourceExists(this.topicStoragePath)) {
                userRegistry.put(this.topicStoragePath, userRegistry.newCollection());
            }
            //allow permissions to root topic
            //put the permissions to the user store. here we create the resource name from
            //the topic storage path of the registry.
            //here we allow permissions at each start up since some times user managers
            //may have changed.
            UserRealm userRealm =
                    EventBrokerHolder.getInstance().getRealmService().getTenantUserRealm(EventBrokerHolder.getInstance().getTenantId());
            for (String role : userRealm.getUserStoreManager().getRoleNames()) {
                userRealm.getAuthorizationManager().authorizeRole(
                        role, this.topicStoragePath, EventBrokerConstants.EB_PERMISSION_SUBSCRIBE);
                userRealm.getAuthorizationManager().authorizeRole(
                        role, this.topicStoragePath, EventBrokerConstants.EB_PERMISSION_PUBLISH);

            }
            // we need to create the index here only it is not exists.
            if (!userRegistry.resourceExists(this.indexStoragePath)) {
                userRegistry.put(this.indexStoragePath, userRegistry.newResource());
            }

        } catch (RegistryException e) {
            throw new EventBrokerConfigurationException("Can not access the registry ", e);
        } catch (UserStoreException e) {
            throw new EventBrokerConfigurationException("Can not access the user registry", e);
        }
    }

    /**
     * When adding a subscription first it stores in the registry as as set of property values
     * then add the subscription details to the topic index
     * @param subscription
     * @throws org.wso2.carbon.event.core.exception.EventBrokerException
     */
    public void addSubscription(Subscription subscription) throws EventBrokerException {


        try {
            UserRegistry userRegistry =
                    this.registryService.getGovernanceSystemRegistry(EventBrokerHolder.getInstance().getTenantId());
            String resourcePath = getResourcePath(subscription.getId(), subscription.getTopicName());

            Resource resource = userRegistry.newResource();
            resource.setProperty(EventBrokerConstants.EB_RES_SUBSCRIPTION_URL, subscription.getEventSinkURL());
            resource.setProperty(EventBrokerConstants.EB_RES_EVENT_DISPATCHER_NAME, subscription.getEventDispatcherName());

            if (subscription.getExpires() != null){
               resource.setProperty(EventBrokerConstants.EB_RES_EXPIRS, ConverterUtil.convertToString(subscription.getExpires()));
            }
            resource.setProperty(EventBrokerConstants.EB_RES_OWNER, subscription.getOwner());
            resource.setProperty(EventBrokerConstants.EB_RES_TOPIC_NAME, subscription.getTopicName());
            resource.setProperty(EventBrokerConstants.EB_RES_CREATED_TIME, System.currentTimeMillis()+"");
            resource.setProperty(EventBrokerConstants.EB_RES_MODE, JavaUtil.getSubscriptionMode(subscription.getTopicName()));

            //set the other properties of the subscription.
            Map<String, String> properties = subscription.getProperties();
            for (String key : properties.keySet()){
                resource.setProperty(key, properties.get(key));
            }

            userRegistry.put(resourcePath, resource);

            // add the subscription index
            String fullPath = this.indexStoragePath;
            Resource topicIndexResource;
            if(userRegistry.resourceExists(fullPath)){
                topicIndexResource = userRegistry.get(fullPath);
                topicIndexResource.addProperty(subscription.getId(), subscription.getTopicName());
            }else{
               topicIndexResource = userRegistry.newResource();
               topicIndexResource.addProperty(subscription.getId(), subscription.getTopicName());                
            }
            userRegistry.put(fullPath, topicIndexResource);

        } catch (RegistryException e) {
            throw new EventBrokerException("Can not save to registry ", e);
        }

    }

    /**
     * calcualtes the resouce stored path using subscription id and the topic name
     * @param subscriptionID
     * @param topicName
     * @return
     */
    private String getResourcePath(String subscriptionID, String topicName) {
        String resourcePath = this.topicStoragePath;

        // first convert the . to /
        topicName = topicName.replaceAll("\\.", "/");

        if (!topicName.startsWith("/")) {
            resourcePath += "/";
        }

        // this topic name can have # and * marks if the user wants to subscribes to the
        // child topics as well. but we consider the topic here as the topic name just before any
        // special charactor.
        // eg. if topic name is myTopic/*/* then topic name is myTopic
        if (topicName.indexOf("*") > -1){
            topicName = topicName.substring(0, topicName.indexOf("*"));
        } else if (topicName.indexOf("#") > -1){
            topicName = topicName.substring(0, topicName.indexOf("#"));
        }

        resourcePath += topicName;

        if (!resourcePath.endsWith("/")) {
            resourcePath += "/";
        }

        resourcePath += EventBrokerConstants.EB_CONF_WS_SUBSCRIPTION_COLLECTION_NAME + "/" + subscriptionID;
        return resourcePath;
    }

    /**
     * Get all the subscriptions. it get the subscription ids from the topic index and get
     * relavent subscriptions.
     * @return
     * @throws org.wso2.carbon.event.core.exception.EventBrokerException
     */
    public List<Subscription> getAllSubscriptions() throws EventBrokerException {
        List<Subscription> subscriptions = new ArrayList<Subscription>();

        try {
            UserRegistry userRegistry =
                    this.registryService.getGovernanceSystemRegistry(EventBrokerHolder.getInstance().getTenantId());
            if (userRegistry.resourceExists(this.indexStoragePath)) {
                Resource topicIndexResource = userRegistry.get(this.indexStoragePath);
                Properties savedSubscriptions = topicIndexResource.getProperties();

                Resource subscriptionResource = null;
                Subscription subscription = null;
                String subscriptionID = null;
                String topicName = null;
                for (Enumeration e = savedSubscriptions.propertyNames(); e.hasMoreElements();) {
                    subscriptionID = (String) e.nextElement();
                    // when the registry is remotely mount to antoher registry. then registry automatically added
                    // some propertiest stary with registry we need to skip them.
                    if (!subscriptionID.startsWith("registry")) {
                        topicName = topicIndexResource.getProperty(subscriptionID);
                        if (userRegistry.resourceExists(getResourcePath(subscriptionID, topicName))) {
                        	subscriptionResource = userRegistry.get(getResourcePath(subscriptionID, topicName));
                            subscription = JavaUtil.getSubscription(subscriptionResource);
                            subscription.setId(subscriptionID);
                            subscription.setTopicName(topicName);
                            subscription.setTenantId(EventBrokerHolder.getInstance().getTenantId());
                            subscriptions.add(subscription);
                        }                        
                    }
                }
            }

        } catch (RegistryException e) {
            throw new EventBrokerException("Can not access the registry ", e);
        }
        return subscriptions;
    }

    public Subscription getSubscription(String id) throws EventBrokerException {

        try {
            UserRegistry userRegistry =
                    this.registryService.getGovernanceSystemRegistry(EventBrokerHolder.getInstance().getTenantId());
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
            throw new EventBrokerException("Can not access the registry ", e);
        }
    }

    public void renewSubscription(Subscription subscription) throws EventBrokerException {

         try {
            UserRegistry userRegistry =
                    this.registryService.getGovernanceSystemRegistry(EventBrokerHolder.getInstance().getTenantId());
            Resource topicIndexResource = userRegistry.get(this.indexStoragePath);
            String topicName = topicIndexResource.getProperty(subscription.getId());
            String subscriptionPath = getResourcePath(subscription.getId(), topicName);
            if (subscriptionPath != null) {
                Resource subscriptionResource = userRegistry.get(subscriptionPath);
                // Set the expires property only if it has been set again.
                if (subscription.getExpires() != null) {
                    subscriptionResource.setProperty(EventBrokerConstants.EB_RES_EXPIRS,
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
                throw new EventBrokerException("Can not find the resouce to the subscription with" +
                        " id " + subscription.getId());
            }
        } catch (RegistryException e) {
            throw new EventBrokerException("Can not access the registry ", e);
        }

    }

    public void unSubscribe(String subscriptionID) throws EventBrokerException {
        try {
            UserRegistry userRegistry =
                    this.registryService.getGovernanceSystemRegistry(EventBrokerHolder.getInstance().getTenantId());
            String fullPath = this.indexStoragePath;
            if (userRegistry.resourceExists(fullPath)) {
                Resource topicIndexResource = userRegistry.get(fullPath);

                String topicName = topicIndexResource.getProperty(subscriptionID);
                // delete the subscriptions resource
                // if the registry is read only there can be situations where the the subscriptions
                // is not saved to registry and hence the topic name
                if (topicName != null){
                    userRegistry.delete(getResourcePath(subscriptionID, topicName));
                }

                topicIndexResource.removeProperty(subscriptionID);

                userRegistry.put(fullPath, topicIndexResource);
            }

        } catch (RegistryException e) {
            throw new EventBrokerException("Can not access the registry ", e);
        }


    }

    public String getTopicStoragePath() throws EventBrokerException {
        return topicStoragePath;
    }
}
