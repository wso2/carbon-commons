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
@Deprecated
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
     * @throws EventAdminException Thrown when topic manager cannot be accessed.
     */
    @SuppressWarnings("UnusedDeclaration")
    public TopicRolePermission[] getTopicRolePermissions(String topic) throws EventAdminException {
        EventBroker eventBroker = EventAdminHolder.getInstance().getEventBroker();
        try {
            return eventBroker.getTopicManager().getTopicRolePermission(topic);
        } catch (EventBrokerException e) {
            String errorMessage = "Error in accessing topic manager";
            log.error(errorMessage, e);
            throw new EventAdminException(errorMessage, e);
        }
    }

    /**
     * Adds a new topic
     *
     * @param topic New topic name
     * @throws EventAdminException Thrown when accessing registry or when providing permissions.
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
            String errorMessage = "Error in adding a topic";
            log.error(errorMessage, e);
            throw new EventAdminException(errorMessage, e);
        }
    }

    /**
     * Updates the permissions for roles of a topic
     * Suppressing warning as this is used as a web service
     *
     * @param topic                Topic name
     * @param topicRolePermissions New roles with permissions
     * @throws EventAdminException Thrown when updating topic permissions.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void updatePermission(String topic, TopicRolePermission[] topicRolePermissions)
            throws EventAdminException {
        EventBroker eventBroker = EventAdminHolder.getInstance().getEventBroker();
        try {
            eventBroker.getTopicManager().updatePermissions(topic, topicRolePermissions);
        } catch (EventBrokerException e) {
            String errorMessage = "Error in updating permissions for topic";
            log.error(errorMessage, e);
            throw new EventAdminException(errorMessage, e);
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
     * @throws EventAdminException Thrown when accessing topic manager.
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
            String errorMessage = "Error in accessing topic manager";
            log.error(errorMessage, e);
            throw new EventAdminException(errorMessage, e);
        }
    }

    /**
     * Gets all the subscriptions
     * Suppressing warning as this is used as a web service
     *
     * @param topic Topic name
     * @return An array of subscriptions
     * @throws EventAdminException Thrown when accessing topic manager.
     */
    @SuppressWarnings("UnusedDeclaration")
    public Subscription[] getWsSubscriptionsForTopic(String topic) throws EventAdminException {
        EventBroker eventBroker = EventAdminHolder.getInstance().getEventBroker();
        try {
            return adaptSubscriptions(eventBroker.getTopicManager().getSubscriptions(topic, true));
        } catch (EventBrokerException e) {
            String errorMessage = "Error in accessing topic manager";
            log.error(errorMessage, e);
            throw new EventAdminException(errorMessage, e);
        }
    }

    /**
     * Gets the total number of subscriptions for a topic
     * Suppressing warning as this is used as a web service
     *
     * @param topic Topic name
     * @return Number of subscriptions
     * @throws EventAdminException Thrown when accessing topic manager.
     */
    @SuppressWarnings("UnusedDeclaration")
    public int getAllWSSubscriptionCountForTopic(String topic) throws EventAdminException {
        EventBroker eventBroker = EventAdminHolder.getInstance().getEventBroker();
        try {
            return eventBroker.getTopicManager().getSubscriptions(topic, true).length;
        } catch (EventBrokerException e) {
            String errorMessage = "Error in accessing topic manager";
            log.error(errorMessage, e);
            throw new EventAdminException(errorMessage, e);
        }
    }

    /**
     * Gets the JMS subscriptions for a topic
     * Suppressing warning as this is used as a web service
     *
     * @param topic Topic name
     * @return An array of subscriptions
     * @throws EventAdminException Thrown when getting JMS subscriptions details from registry.
     */
    @SuppressWarnings("UnusedDeclaration")
    public Subscription[] getJMSSubscriptionsForTopic(String topic)
            throws EventAdminException {
        EventBroker eventBroker = EventAdminHolder.getInstance().getEventBroker();
        try {
            return adaptSubscriptions(eventBroker.getTopicManager().getJMSSubscriptions(topic));
        } catch (EventBrokerException e) {
            String errorMessage = "Cannot get the jms subscriptions";
            log.error(errorMessage, e);
            throw new EventAdminException(errorMessage, e);
        }
    }

    /**
     * Gets user roles through topic manager
     *
     * @return A string array of roles
     * @throws EventAdminException Thrown when topic manager is unable to get user roles.
     */
    public String[] getUserRoles() throws EventAdminException {
        EventBroker eventBroker = EventAdminHolder.getInstance().getEventBroker();
        try {
            return eventBroker.getTopicManager().getBackendRoles();
        } catch (EventBrokerException e) {
            String errorMessage = "Error in getting user roles from topic manager";
            log.error(errorMessage, e);
            throw new EventAdminException(errorMessage, e);
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
            String errorMessage = "Error in removing a topic";
            log.error(errorMessage, e);
            throw new EventAdminException(errorMessage, e);
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
