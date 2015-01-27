/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.event.core.sharedmemory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.event.core.Message;
import org.wso2.carbon.event.core.exception.EventBrokerConfigurationException;
import org.wso2.carbon.event.core.sharedmemory.util.SharedMemoryCacheUtil;
import org.wso2.carbon.event.core.util.EventBrokerConstants;
import org.wso2.carbon.event.core.delivery.DeliveryManager;
import org.wso2.carbon.event.core.delivery.MatchingManager;
import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.event.core.internal.delivery.Worker;
import org.wso2.carbon.event.core.internal.util.JavaUtil;
import org.wso2.carbon.event.core.internal.util.EventBrokerHolder;
import org.wso2.carbon.event.core.notify.NotificationManager;
import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.CarbonConstants;

import javax.cache.Cache;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * shared memory implementation of the delivery manager.
 */
public class SharedMemoryDeliveryManager implements DeliveryManager {

	private static final Log log = LogFactory.getLog(SharedMemoryDeliveryManager.class);
    private ExecutorService executor;
    private NotificationManager notificationManager;
    private String topicStoragePath;
    private EventBroker eventBroker;

    public SharedMemoryDeliveryManager(ExecutorService executor, String topicStoragePath) {
        this.executor = executor;
        this.topicStoragePath = topicStoragePath;
    }

    public void subscribe(Subscription subscription) throws EventBrokerException {

       String resourcePath = JavaUtil.getResourcePath(subscription.getTopicName(), this.topicStoragePath);
        try {
            UserRealm userRealm =
                    EventBrokerHolder.getInstance().getRealmService().getTenantUserRealm
                                               (CarbonContext.getThreadLocalCarbonContext().getTenantId());
            String userName = subscription.getOwner();

            // trim the domain part if it is there.
            if (userName.indexOf('@') != -1){
                userName = userName.substring(0, userName.indexOf('@'));
            }
            if (userName.equals(CarbonConstants.REGISTRY_SYSTEM_USERNAME) ||
                    userRealm.getAuthorizationManager().isUserAuthorized(
                        userName,
                        resourcePath,
                        EventBrokerConstants.EB_PERMISSION_SUBSCRIBE)){
                getMatchingManager().addSubscription(subscription);
            } else {
                throw new EventBrokerException("User " + CarbonContext.getThreadLocalCarbonContext().getUsername()
                               + " is not allowed to subscribes to " + subscription.getTopicName());
            }
        } catch (UserStoreException e) {
            throw new EventBrokerException("Can not access the user store manager",e);
        }

    }

    public void setNotificationManager(NotificationManager notificationManager) {
       this.notificationManager = notificationManager;
    }

    public void publish(Message message, String topicName, int deliveryMode) throws EventBrokerException {

        String resourcePath = JavaUtil.getResourcePath(topicName, this.topicStoragePath);
        try {
            UserRealm userRealm =
                    EventBrokerHolder.getInstance().getRealmService().getTenantUserRealm
                            (CarbonContext.getThreadLocalCarbonContext().getTenantId());
            String userName = CarbonContext.getThreadLocalCarbonContext().getUsername();
            
            if (userName == null){
                userName = CarbonConstants.REGISTRY_SYSTEM_USERNAME;
            }
            if (userName.equals(CarbonConstants.REGISTRY_SYSTEM_USERNAME) ||
                    userRealm.getAuthorizationManager().isUserAuthorized(
                        userName,
                        resourcePath,
                        EventBrokerConstants.EB_PERMISSION_PUBLISH)) {
                List<Subscription> subscriptions = getMatchingManager().getMatchingSubscriptions(topicName);

                for (Subscription subscription : subscriptions) {
                    String verified = org.wso2.carbon.event.core.sharedmemory.SharedMemorySubscriptionStorage
                    						.getSubscriptionIDTopicNameCache().get(subscription.getId()+"-notVerfied");
                    
                    if(verified != null && "false".equalsIgnoreCase(verified)){
                            subscription.addProperty("notVerfied", "false");
                    }

                    this.executor.submit(new Worker(this.notificationManager, message, subscription));
                }
            } else {
                throw new EventBrokerException("User " + CarbonContext.getThreadLocalCarbonContext().getUsername()
                        + " is not allowed to publish to " + topicName);
            }
        } catch (UserStoreException e) {
            throw new EventBrokerException("Can not access the user store manager",e);
        }
    }

    public void setMatchingManager(MatchingManager matchingManager) {
    }

    public void unSubscribe(String id) throws EventBrokerException {
        getMatchingManager().unSubscribe(id);
    }

    public void cleanUp() {
        // nothing to clean up in the shared memory implementation.
    }

    public void renewSubscription(Subscription subscription) throws EventBrokerException {
        getMatchingManager().renewSubscription(subscription);
    }

    public void initializeTenant() throws EventBrokerException {
        getMatchingManager().initializeTenant();
    }

    private static Cache<Integer, SharedMemoryMatchingManager> getInMemoryMatchingCache() {
        return SharedMemoryCacheUtil.getInMemoryMatchingCache();
    }

    public synchronized MatchingManager getMatchingManager() throws EventBrokerConfigurationException {
        SharedMemoryMatchingManager inMemoryMatchingManager = getInMemoryMatchingCache().get(1);

        if(inMemoryMatchingManager == null) {
            inMemoryMatchingManager = new SharedMemoryMatchingManager();
            getInMemoryMatchingCache().put(1, inMemoryMatchingManager);
        }

        try {
            //call initialize tenant for super tenant
            inMemoryMatchingManager.initializeTenant();
        } catch (EventBrokerException e) {
            throw new EventBrokerConfigurationException("Can not initialize the in memory mathing manager",e);
        }

        return inMemoryMatchingManager;
    }

    public void setEventBroker(EventBroker eventbroker) {
        this.eventBroker = eventbroker;
    }
}
