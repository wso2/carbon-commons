/*
* Copyright 2004,2013 The Apache Software Foundation.
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
package org.wso2.carbon.discovery.cxf;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.discovery.DiscoveryException;
import org.wso2.carbon.discovery.util.DiscoveryMgtUtils;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class CxfDiscoveryConfigurationContextObserver extends AbstractAxis2ConfigurationContextObserver {

    private static final Log log = LogFactory.getLog(CxfDiscoveryConfigurationContextObserver.class);

    protected Queue<CXFServiceInfo> initialMessagesList;

    public CxfDiscoveryConfigurationContextObserver(Queue<CXFServiceInfo> initialMessagesList) {
        this.initialMessagesList = initialMessagesList;
    }

    public void createdConfigurationContext(ConfigurationContext configurationContext) {
        AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();
        try {
            String domain = PrivilegedCarbonContext.getThreadLocalCarbonContext().
                    getTenantDomain(true);
            if (DiscoveryMgtUtils.isServiceDiscoveryEnabled(axisConfig)) {
                if (log.isDebugEnabled()) {
                    log.debug("Registering the Axis observer for WS-Discovery in tenant: " + domain);
                }
                List<CXFServiceInfo> tempMessagesList = new ArrayList<CXFServiceInfo>();
                CxfMessageSender messageSender = new CxfMessageSender();
                while (!initialMessagesList.isEmpty()) {
                    CXFServiceInfo message = initialMessagesList.poll();
                    try {
                        if (domain.equals(message.getTenantDomain())) {
                            messageSender.sendHello(message, null);
                        } else {
                            tempMessagesList.add(message);
                        }
                    } catch (DiscoveryException e) {
                        log.error("Error sending WS-Discovery Hello message to DiscoveryProxy for " +
                                message.getServiceName());
                    }
                }
                initialMessagesList.addAll(tempMessagesList);
            }
        } catch (RegistryException e) {
            log.error("Checking whether service discovery is enabled for a tenant", e);
        }
    }

}
