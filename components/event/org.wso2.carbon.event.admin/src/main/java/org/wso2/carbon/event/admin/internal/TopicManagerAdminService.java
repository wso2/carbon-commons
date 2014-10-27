package org.wso2.carbon.event.admin.internal;

import org.wso2.carbon.event.admin.internal.exception.EventAdminException;
import org.wso2.carbon.event.admin.internal.util.EventAdminHolder;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.event.core.topic.TopicManager;
import org.wso2.carbon.event.core.topic.TopicNode;
import org.wso2.carbon.event.core.topic.TopicRolePermission;

import java.util.Calendar;

public class TopicManagerAdminService {

    public TopicNode getAllTopics() throws EventAdminException {
        EventBroker eventBroker = EventAdminHolder.getInstance().getEventBroker();
        try {
            return eventBroker.getTopicManager().getTopicTree();
        } catch (EventBrokerException e) {
            throw new EventAdminException("Error in accessing topic manager ", e);
        }
    }

    public TopicRolePermission[] getTopicRolePermissions(String topic) throws EventAdminException {
        EventBroker eventBroker = EventAdminHolder.getInstance().getEventBroker();
        try {
            return eventBroker.getTopicManager().getTopicRolePermission(topic);
        } catch (EventBrokerException e) {
            throw new EventAdminException("Error in accessing topic manager", e);
        }
    }

    public void addTopic(String topic) throws EventAdminException {
        EventBroker eventBroker = EventAdminHolder.getInstance().getEventBroker();
        try {
            if (!eventBroker.getTopicManager().isTopicExists(topic)) {
                eventBroker.getTopicManager().addTopic(topic);
            }else {
                throw new EventAdminException("Topic with name : "+ topic + " already exists!");
            }
        } catch (EventBrokerException e) {
            throw new EventAdminException(e.getMessage(), e);
        }
    }

    public void updatePermission(String topic, TopicRolePermission[] topicRolePermissions)
            throws EventAdminException {
        EventBroker eventBroker = EventAdminHolder.getInstance().getEventBroker();
        try {
            eventBroker.getTopicManager().updatePermissions(topic, topicRolePermissions);
        } catch (EventBrokerException e) {
            throw new EventAdminException("Error: " + e.getMessage(), e);
        }
    }

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
            throw new EventAdminException("Error in accessing topic manager", e);
        }
    }

    public Subscription[] getWsSubscriptionsForTopic(String topic) throws EventAdminException {
        EventBroker eventBroker = EventAdminHolder.getInstance().getEventBroker();
        try {
            return adaptSubscriptions(eventBroker.getTopicManager().getSubscriptions(topic, true));
        } catch (EventBrokerException e) {
            throw new EventAdminException("Error in accessing topic manager", e);
        }
    }

    public int getAllWSSubscriptionCountForTopic(String topic) throws EventAdminException {
        EventBroker eventBroker = EventAdminHolder.getInstance().getEventBroker();
        try {
            return eventBroker.getTopicManager().getSubscriptions(topic, true).length;
        } catch (EventBrokerException e) {
            throw new EventAdminException("Error in accessing topic manager", e);
        }
    }

    public Subscription[] getJMSSubscriptionsForTopic(String topic)
            throws EventAdminException {
        EventBroker eventBroker = EventAdminHolder.getInstance().getEventBroker();
        try {
            return adaptSubscriptions(eventBroker.getTopicManager().getJMSSubscriptions(topic));
        } catch (EventBrokerException e) {
            throw new EventAdminException("Can not get the jms subscriptions", e);
        }
    }

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

    public String[] getUserRoles() throws EventAdminException {
        EventBroker eventBroker = EventAdminHolder.getInstance().getEventBroker();
        try {
            return eventBroker.getTopicManager().getBackendRoles();
        } catch (EventBrokerException e) {
            throw new EventAdminException("Error in getting User Roles from topic manager", e);
        }
    }

    public boolean removeTopic(String topic) throws EventAdminException {
        EventBroker eventBroker = EventAdminHolder.getInstance().getEventBroker();
        try {
            return eventBroker.getTopicManager().removeTopic(topic);
        } catch (EventBrokerException e) {
            throw new EventAdminException(e.getMessage(), e);
        }
    }
}
