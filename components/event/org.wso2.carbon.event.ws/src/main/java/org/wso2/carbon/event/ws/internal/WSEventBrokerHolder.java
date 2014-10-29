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

import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.event.core.util.EventBrokerConstants;
import org.wso2.carbon.event.ws.internal.notify.WSEventDispatcher;
import org.wso2.carbon.utils.ConfigurationContextService;

public class WSEventBrokerHolder {

    private static WSEventBrokerHolder wsEventBrokerHolder = new WSEventBrokerHolder();

    private EventBroker eventBroker;

    private ConfigurationContextService configurationContextService;

    public static WSEventBrokerHolder getInstance(){
        return wsEventBrokerHolder;
    }

    public EventBroker getEventBroker() {
        return eventBroker;
    }

    public void registerEventBroker(EventBroker eventBroker) {
        this.eventBroker = eventBroker;
    }

    public ConfigurationContextService getConfigurationContextService(){
        return configurationContextService;
    }

    public void registerConfigurationContextService(ConfigurationContextService configurationContextService){
        this.configurationContextService = configurationContextService;
    }

    public void registerWSEventDispatcher(){
        WSEventDispatcher wsEventDispatcher = new WSEventDispatcher();
        this.eventBroker.registerEventDispatcher(
                EventBrokerConstants.WS_EVENT_DISPATCHER_NAME, wsEventDispatcher);
    }
}
