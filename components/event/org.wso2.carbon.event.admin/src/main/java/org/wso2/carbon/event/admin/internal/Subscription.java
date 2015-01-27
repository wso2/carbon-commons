package org.wso2.carbon.event.admin.internal;

import org.wso2.carbon.event.core.subscription.EventDispatcher;
import org.wso2.carbon.event.core.subscription.EventFilter;

import java.util.Calendar;

public class Subscription {
    // these properties are come from the eventing specification
    private String eventSinkURL;
    private Calendar expires;
    private EventFilter eventFilter;

    //TODO : add support for following
    // Delivary mode

    // these properites are used in wso2 carbon implementation.
    private String id;
    private String topicName;
    private EventDispatcher eventDispatcher;
    private String eventDispatcherName;
    private Calendar createdTime;
    private String owner;
    private String mode;


    public String getEventSinkURL() {
        return eventSinkURL;
    }

    public void setEventSinkURL(String eventSinkURL) {
        this.eventSinkURL = eventSinkURL;
    }

    public Calendar getExpires() {
        return expires;
    }

    public void setExpires(Calendar expires) {
        this.expires = expires;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public EventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    public void setEventDispatcher(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    public String getEventDispatcherName() {
        return eventDispatcherName;
    }

    public void setEventDispatcherName(String eventDispatcherName) {
        this.eventDispatcherName = eventDispatcherName;
    }

    public Calendar getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Calendar createdTime) {
        this.createdTime = createdTime;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public EventFilter getEventFilter() {
        return eventFilter;
    }

    public void setEventFilter(EventFilter eventFilter) {
        this.eventFilter = eventFilter;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
