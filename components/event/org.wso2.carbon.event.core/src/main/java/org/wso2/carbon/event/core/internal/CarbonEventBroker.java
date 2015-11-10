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

package org.wso2.carbon.event.core.internal;

import org.apache.axiom.util.UIDGenerator;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.event.core.Message;
import org.wso2.carbon.event.core.delivery.DeliveryManager;
import org.wso2.carbon.event.core.exception.EventBrokerConfigurationException;
import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.event.core.internal.notify.CarbonNotificationManager;
import org.wso2.carbon.event.core.internal.util.EventBrokerHolder;
import org.wso2.carbon.event.core.subscription.EventDispatcher;
import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.event.core.subscription.SubscriptionManager;
import org.wso2.carbon.event.core.topic.TopicManager;
import org.wso2.carbon.event.core.util.EventBrokerConstants;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * The following class contains the carbon eventing implementation related to subscriptions and
 * topics.
 */
@Deprecated
public class CarbonEventBroker implements EventBroker {

    private SubscriptionManager subscriptionManager;
    private TopicManager topicManager;
    private DeliveryManager deliveryManager;
    private CarbonNotificationManager notificationManager;
    private ExecutorService executor;

    /**
     * Initializes the carbon event broker. Loads subscriptions new notifier manager set to delivery
     * manager.
     *
     * @throws EventBrokerConfigurationException
     */
    public void init() throws EventBrokerConfigurationException {

        this.notificationManager = new CarbonNotificationManager();
        // we pass the notification manager to delivery manager. Then delivery manager calls the
        // notify method with the given subscription details.
        // this way delivery manager can use the persisted repository of the subscription manager
        // to notify subscriptions if wanted.
        this.deliveryManager.setNotificationManager(this.notificationManager);
        // re subscribe the already existing subscriptions.
        // TODO: do the validations eg. expiraty time
        loadExistingSubscriptions();
    }

    /**
     * Loads existing subscriptions. Delivery manager is subscribed with non expired subscriptions
     *
     * @throws EventBrokerConfigurationException
     */
    private void loadExistingSubscriptions() throws EventBrokerConfigurationException {
        try {
            Calendar calendar = Calendar.getInstance();
            for (Subscription subscription : this.subscriptionManager.getAllSubscriptions()) {
                if ((subscription.getExpires() == null) ||
                    (calendar.before(subscription.getExpires()))) {
                    if (EventBrokerHolder.getInstance().getTenantDomain() != null) {
                        subscription
                                .setTenantDomain(EventBrokerHolder.getInstance().getTenantDomain());
                        subscription.setTenantId(EventBrokerHolder.getInstance().getTenantId());
                    }
                    deliveryManager.subscribe(subscription);
                }
            }
        } catch (EventBrokerException e) {
            throw new EventBrokerConfigurationException("Cannot get the subscriptions ", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeTenant() throws EventBrokerException, UserStoreException {
        this.deliveryManager.initializeTenant();
        loadExistingSubscriptions();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String subscribe(Subscription subscription)
            throws EventBrokerException {

        //if there is a subscription with the same topic and event sink url then
        //we think it is the same subscription.
        Subscription existingSubscription = getExistingNonExpiredSubscription(subscription);
        if (existingSubscription != null) {
            return existingSubscription.getId();
        }
        if (EventBrokerHolder.getInstance().getTenantDomain() != null) {
            subscription.setTenantDomain(EventBrokerHolder.getInstance().getTenantDomain());
            subscription.setTenantId(EventBrokerHolder.getInstance().getTenantId());
        }
        // generates an id for the subscription
        subscription.setId(UIDGenerator.generateUID());
        this.topicManager.addTopic(subscription.getTopicName());
        this.deliveryManager.subscribe(subscription);

        if (subscription.getEventDispatcherName() != null) {
            // we persists a subscription only if it has a event dispatcher
            // name. the subscriptions with only an event dispatcher is not persisted.
            this.subscriptionManager.addSubscription(subscription);
        } else {
            if (subscription.getEventDispatcher() == null) {
                throw new EventBrokerException(" subscription url, event " +
                                               "dispatcher name and event dispatcher is null");
            }
        }
        return subscription.getId();
    }

    /**
     * Gets existing subscription for a given new subscription topic that are not expired
     *
     * @param newSubscription the new subscription
     * @return the existing subscription
     * @throws EventBrokerException
     */
    private Subscription getExistingNonExpiredSubscription(Subscription newSubscription)
            throws EventBrokerException {
        Subscription[] subscriptions =
                this.topicManager.getSubscriptions(newSubscription.getTopicName(), false);
        Subscription existingSubscription = null;
        Calendar calendar = Calendar.getInstance();
        for (Subscription subscription : subscriptions) {
            if (subscription.getEventSinkURL() != null) {
                if (subscription.getEventSinkURL()
                        .equalsIgnoreCase(newSubscription.getEventSinkURL())) {
                    if ((subscription.getExpires() == null) ||
                        (calendar.before(subscription.getExpires()))) {
                        existingSubscription = subscription;
                        break;
                    }
                }
            }
        }
        return existingSubscription;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribe(String id) throws EventBrokerException {
        this.subscriptionManager.unSubscribe(id);
        this.deliveryManager.unSubscribe(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Subscription getSubscription(String id) throws EventBrokerException {
        return this.subscriptionManager.getSubscription(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void renewSubscription(Subscription subscription) throws EventBrokerException {
        // save the new expiration time to registry
        this.subscriptionManager.renewSubscription(subscription);
        this.deliveryManager.renewSubscription(subscription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Subscription> getAllSubscriptions(String filter) throws EventBrokerException {
        return this.subscriptionManager.getAllSubscriptions();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(Message message, String topicName) throws EventBrokerException {
        publish(message, topicName, EventBrokerConstants.EB_NON_PERSISTENT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(Message message, String topicName, int deliveryMode)
            throws EventBrokerException {
        EventPublisher eventPublisher =
                new EventPublisher(message,
                                   topicName,
                                   this.deliveryManager,
                                   deliveryMode,
                                   CarbonContext.getThreadLocalCarbonContext().getTenantId());
        this.executor.execute(eventPublisher);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishRobust(Message message, String topicName) throws EventBrokerException {
        publishRobust(message, topicName, EventBrokerConstants.EB_NON_PERSISTENT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishRobust(Message message, String topicName, int deliveryMode)
            throws EventBrokerException {
        this.deliveryManager.publish(message, topicName, deliveryMode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerEventDispatcher(String eventDispatcherName,
                                        EventDispatcher eventDispatcher) {
        this.notificationManager.registerEventDispatcher(eventDispatcherName, eventDispatcher);
    }

    /**
     * Sets new subscription manager
     *
     * @param subscriptionManager new subscription manager
     */
    public void setSubscriptionManager(SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicManager getTopicManager() {
        return topicManager;
    }

    /**
     * Sets new topic manager
     *
     * @param topicManager new topic manager
     */
    public void setTopicManager(TopicManager topicManager) {
        this.topicManager = topicManager;
    }

    /**
     * Sets the delivery manager
     *
     * @param deliveryManager new delivery manager
     */
    public void setDeliveryManager(DeliveryManager deliveryManager) {
        this.deliveryManager = deliveryManager;
    }

    /**
     * Sets the executor service
     *
     * @param executor new executor service
     */
    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    /**
     * Cleans up the delivery broker
     *
     * @throws EventBrokerException
     */
    public void cleanUp() throws EventBrokerException {
        this.deliveryManager.cleanUp();
    }
}
