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

package org.wso2.carbon.event.core.subscription;

import java.util.Calendar;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

/**
 * keeps all the subscription related data.
 * 
 */
public class Subscription {

    // these properties are come from the eventing specification
    private String eventSinkURL;
    private Calendar expires;
    private Map<String, String> properties;
    private EventFilter eventFilter;

    //TODO : add support for following
    // Delivary mode

    // these properites are used in wso2 carbon implementation.
    private String id;
    private String topicName;
    private EventDispatcher eventDispatcher;
    private String eventDispatcherName;
    private Date createdTime;
    private String owner;
    private String tenantDomain;
    private int tenantId;
    private String mode;

    public Subscription() {
        this.properties = new HashMap<String, String>();
    }

    public void addProperty(String name, String value){
        this.properties.put(name, value);
    }

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

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
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

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
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

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
