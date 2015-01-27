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

package org.wso2.carbon.event.admin.internal.util;

import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.core.EventBroker;

/**
 * @scr.component name="wsevent.component.admin" immediate="true"
 * @scr.reference name="eventbroker.service"
 * interface="org.wso2.carbon.event.core.EventBroker" cardinality="1..1"
 * policy="dynamic" bind="setEventBroker" unbind="unSetEventBroker"
 *
 */
public class EventAdminDS {

    protected void activate(ComponentContext context) {

    }

    protected void setEventBroker(EventBroker eventBroker) {
        EventAdminHolder.getInstance().registerEventBroker(eventBroker);
    }

    protected void unSetEventBroker(EventBroker eventBroker) {

    }
   
}
