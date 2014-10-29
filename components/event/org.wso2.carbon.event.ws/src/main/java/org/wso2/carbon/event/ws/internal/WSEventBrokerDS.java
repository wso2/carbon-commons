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

package org.wso2.carbon.event.ws.internal;

import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.event.core.internal.util.EventBrokerHolder;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="wsevent.component" immediate="true"
 * @scr.reference name="eventbroker.service"
 * interface="org.wso2.carbon.event.core.EventBroker" cardinality="1..1"
 * policy="dynamic" bind="setEventBroker" unbind="unSetEventBroker"
 * @scr.reference name="configurationcontext.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */
public class WSEventBrokerDS {

    protected void activate(ComponentContext context) {
        WSEventBrokerHolder.getInstance().registerWSEventDispatcher();
    }

    protected void setEventBroker(EventBroker eventBroker) {
        WSEventBrokerHolder.getInstance().registerEventBroker(eventBroker);
    }

    protected void unSetEventBroker(EventBroker eventBroker) {

    }

    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        WSEventBrokerHolder.getInstance().registerConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {

    }

}
