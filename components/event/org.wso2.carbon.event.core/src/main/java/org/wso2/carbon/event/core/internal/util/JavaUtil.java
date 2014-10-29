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

package org.wso2.carbon.event.core.internal.util;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.event.core.exception.EventBrokerConfigurationException;
import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.event.core.util.EventBrokerConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.user.api.UserStoreException;

import javax.xml.namespace.QName;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

/**
 * some utility classes.
 */
public class JavaUtil {

    /**
     * returns an object with the type of attibute class.
     *
     * @param omElement
     * @return
     * @throws org.wso2.carbon.event.core.exception.EventBrokerConfigurationException
     *
     */
    public static Object getObject(OMElement omElement) throws EventBrokerConfigurationException {
        String className =
                omElement.getAttributeValue(new QName(null,
                                                      EventBrokerConstants.EB_CONF_ATTR_CLASS));
        try {
            Class factoryClass = Class.forName(className);
            return factoryClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new EventBrokerConfigurationException("Class " + className + " not found ", e);
        } catch (IllegalAccessException e) {
            throw new EventBrokerConfigurationException("Class not be accesed ", e);
        } catch (InstantiationException e) {
            throw new EventBrokerConfigurationException("Class not be instantiated ", e);
        }
    }

    public static String getValue(OMElement omElement, String localPart) {
        OMElement childElement =
                omElement.getFirstChildWithName(
                        new QName(omElement.getNamespace().getNamespaceURI(), localPart));
        return childElement.getText();
    }

    /**
     * creates the subscription object from the subscription resource
     *
     * @param subscriptionResource
     * @return
     */
    public static Subscription getSubscription(Resource subscriptionResource) {

        Subscription subscription = new Subscription();
        subscription.setTenantId(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        Properties properties = subscriptionResource.getProperties();
        if ((properties != null) && (!properties.isEmpty())) {
            for (Enumeration enumeration = properties.propertyNames(); enumeration.hasMoreElements();) {
                String propertyName = (String) enumeration.nextElement();
                if (EventBrokerConstants.EB_RES_SUBSCRIPTION_URL.equals(propertyName)) {
                    subscription.setEventSinkURL(
                            subscriptionResource.getProperty(EventBrokerConstants.EB_RES_SUBSCRIPTION_URL));
                } else if (EventBrokerConstants.EB_RES_EVENT_DISPATCHER_NAME.equals(propertyName)) {
                    subscription.setEventDispatcherName(
                            subscriptionResource.getProperty(EventBrokerConstants.EB_RES_EVENT_DISPATCHER_NAME));
                } else if (EventBrokerConstants.EB_RES_EXPIRS.equals(propertyName)) {
                    subscription.setExpires(
                            ConverterUtil.convertToDateTime(
                                    subscriptionResource.getProperty(EventBrokerConstants.EB_RES_EXPIRS)));
                } else if (EventBrokerConstants.EB_RES_OWNER.equals(propertyName)) {
                    subscription.setOwner(subscriptionResource.getProperty(EventBrokerConstants.EB_RES_OWNER));
                } else if (EventBrokerConstants.EB_RES_TOPIC_NAME.equals(propertyName)) {
                    subscription.setTopicName(subscriptionResource.getProperty(EventBrokerConstants.EB_RES_TOPIC_NAME));
                } else if (EventBrokerConstants.EB_RES_CREATED_TIME.equals(propertyName)) {
                    subscription.setCreatedTime(new Date(Long.parseLong(subscriptionResource.getProperty(EventBrokerConstants.EB_RES_CREATED_TIME))));
                } else if (EventBrokerConstants.EB_RES_MODE.equals(propertyName)) {
                    subscription.setMode(subscriptionResource.getProperty(EventBrokerConstants.EB_RES_MODE));
                } else {
                    subscription.addProperty(propertyName, subscriptionResource.getProperty(propertyName));
                }
            }
        }
        return subscription;
    }

    public static String getSubscriptionMode(String topicName) {
        int length = topicName.length();
        String modeSubstring = topicName.substring(length - 2);
        if (modeSubstring.equals("/*")) {
            return "mode_1";
        } else if (modeSubstring.equals("/#")) {
            return "mode_2";
        } else {
            return "mode_0";
        }
    }

    /**
     * Check if the given user has the admin role privileges
     *
     * @param username - the user to be checked for permissions
     * @return - true if given user is a admin role owned user, false otherwise
     * @throws EventBrokerException - if fails to get list of user roles
     */
    public static boolean isAdmin(String username) throws EventBrokerException {
        boolean isAdmin = false;
        try {
            String[] userRoles = EventBrokerHolder.getInstance().getRealmService().
                    getTenantUserRealm(CarbonContext.getThreadLocalCarbonContext().getTenantId()).
                    getUserStoreManager().getRoleListOfUser(username);

            String adminRole =
                    EventBrokerHolder.getInstance().getRealmService().
                            getBootstrapRealmConfiguration().getAdminRoleName();
            for (String userRole : userRoles) {
                if (adminRole.equals(userRole)) {
                    isAdmin = true;
                    break;
                }
            }
        } catch (UserStoreException e) {
            throw new EventBrokerException("Failed to get list of user roles", e);
        }

        return isAdmin;
    }

    public static String getResourcePath(String topicName, String topicStoragePath) {
        String resourcePath = topicStoragePath;

        // first convert the . to /
        topicName = topicName.replaceAll("\\.", "/");

        if (!topicName.startsWith("/")) {
            resourcePath += "/";
        }

        // this topic name can have # and * marks if the user wants to subscribes to the
        // child topics as well. but we consider the topic here as the topic name just before any
        // special charactor.
        // eg. if topic name is myTopic/*/* then topic name is myTopic
        if (topicName.indexOf("*") > -1) {
            topicName = topicName.substring(0, topicName.indexOf("*"));
        } else if (topicName.indexOf("#") > -1) {
            topicName = topicName.substring(0, topicName.indexOf("#"));
        }

        resourcePath += topicName;
        return resourcePath;
    }

}
