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

import org.wso2.carbon.event.core.EventBroker;

@Deprecated
public class EventAdminHolder {

    private static EventAdminHolder eventAdminHolder = new EventAdminHolder();

    private EventBroker eventBroker;

    public static EventAdminHolder getInstance(){
        return eventAdminHolder;
    }

    public EventBroker getEventBroker() {
        return eventBroker;
    }

    public void registerEventBroker(EventBroker eventBroker) {
        this.eventBroker = eventBroker;
    }

}
