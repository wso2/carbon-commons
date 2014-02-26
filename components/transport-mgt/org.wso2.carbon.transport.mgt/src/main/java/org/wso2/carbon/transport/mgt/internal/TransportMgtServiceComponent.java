/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.transport.mgt.internal;

import java.util.ArrayList;
import java.util.List;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.core.transports.TransportService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.transport.mgt.TransportStore;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="transport.mgt" immediate="true"
 * @scr.reference name="config.context.service" interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"  bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 * @scr.reference name="transport.service" interface="org.wso2.carbon.core.transports.TransportService"
 * cardinality="1..n" policy="dynamic" bind="addTransportService" unbind="removeTransportService"
 */
public class TransportMgtServiceComponent {

    private static Log log = LogFactory.getLog(TransportMgtServiceComponent.class);

    private AxisConfiguration axisConfig;
    private List<TransportService> trpList;

    public TransportMgtServiceComponent() {
        axisConfig = null;
        trpList = new ArrayList<TransportService>();
    }

    protected void activate(ComponentContext ctxt) {
        try {
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            //do not throw
        }
        log.debug("******* Transport mgt bundle is activated ******* ");
    }

    protected void deactivate(ComponentContext ctxt) {
        log.debug("******* Transport mgt bundle is deactivated ******* ");
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        axisConfig = contextService.getServerConfigContext().getAxisConfiguration();
        TransportStore trpStore = TransportStore.getInstance();
        for (TransportService trpService : trpList) {
            trpStore.addTransport(trpService.getName(), trpService, axisConfig);
        }
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        this.axisConfig = null;
    }

    protected void addTransportService(TransportService trpService) {
        if (axisConfig == null) {
            trpList.add(trpService);
        } else {
            TransportStore.getInstance().addTransport(trpService.getName(), trpService, axisConfig);
        }
    }

    protected void removeTransportService(TransportService trpService) {
        if (axisConfig == null) {
            trpList.remove(trpService);
        } else {
            TransportStore.getInstance().removeTransport(trpService.getName());
        }
    }    
    
}
