/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.discovery.cxf.listeners;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.endpoint.ServerLifeCycleListener;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.discovery.DiscoveryException;
import org.wso2.carbon.discovery.cxf.CxfMessageSender;
import org.wso2.carbon.discovery.util.DiscoveryMgtUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * 
 */
@Deprecated
public class CxfDiscoveryServerListener implements ServerLifeCycleListener {
    private Log log = LogFactory.getLog(CxfDiscoveryServerListener.class);

    private static final String WS_DISCOVERY_SERVICE_NS =
        "http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01";
    public static final String WS_DISCOVERY_PARAMS = "wsDiscoveryParams";

    final Bus bus;
    volatile CxfMessageSender messageSender = new CxfMessageSender();

    public CxfDiscoveryServerListener(Bus bus) {
        this.bus = bus;
    }
    
    public void startServer(Server server) {
        try {

            throw new DiscoveryException("");
        } catch (DiscoveryException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void stopServer(Server server) {
        try {
            String url = getDiscoveryURL(getAxisConfig());
            Parameter discoveryParams = getDiscoveryParams(getAxisConfig());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    private AxisConfiguration getAxisConfig() {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        ConfigurationContext mainConfigContext = getConfigurationContext();
        AxisConfiguration axisConfig;
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            axisConfig = mainConfigContext.getAxisConfiguration();
        } else {
            axisConfig = TenantAxisUtils.getTenantConfigurationContext(tenantDomain, mainConfigContext).
                    getAxisConfiguration();
        }

        return axisConfig;
    }

    private String getDiscoveryURL(AxisConfiguration axisConfig) {
        Parameter discoveryProxyParam = DiscoveryMgtUtils.getDiscoveryParam(axisConfig);
        return (String) discoveryProxyParam.getValue();

    }

    private Parameter getDiscoveryParams(AxisConfiguration axisConfig) {
        Parameter discoveryProxyParam = axisConfig.getParameter(WS_DISCOVERY_PARAMS);
        return discoveryProxyParam;

    }

    public ConfigurationContext getConfigurationContext() {
        ConfigurationContextService configurationContextService = (ConfigurationContextService) PrivilegedCarbonContext.
                getThreadLocalCarbonContext().getOSGiService(ConfigurationContextService.class);
        return configurationContextService.getServerConfigContext();
    }
}
