/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.discovery;

import org.wso2.carbon.discovery.util.DiscoveryMgtUtils;
import org.wso2.carbon.discovery.messages.TargetService;
import org.wso2.carbon.discovery.util.*;
import org.wso2.carbon.core.AbstractAdmin;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.description.Parameter;
import org.apache.axiom.om.util.AXIOMUtil;

/**
 * Admin service implementation for managing WS-Discovery proxies, connecting to
 * external WS-D proxies and using its web service API.
 */
public class DiscoveryAdmin extends AbstractAdmin {

    /**
     * Add a new WS-D proxy to the system configuration. A WS-D proxy is identified by
     * a unique name and must contain a valid target URL.
     *
     * @param pd WS-D proxy details
     * @throws Exception on error
     */
    public void addDiscoveryProxy(DiscoveryProxyDetails pd) throws Exception {
        DiscoveryMgtUtils.addDiscoveryProxy(pd, getConfigSystemRegistry());
    }

    /**
     * Remove the specified WS-D proxy from the configuration
     *
     * @param name Name of the proxy to be removed
     * @throws Exception on error
     */
    public void removeDiscoveryProxy(String name) throws Exception {
        DiscoveryMgtUtils.removeDiscoveryProxy(name, getConfigSystemRegistry());
    }

    /**
     * Update an existing WS-D proxy configuration. This will overwrite any
     * previous settings.
     *
     * @param pd the updated WS-D proxy configuration
     * @throws Exception on error
     */
    public void updateDiscoveryProxy(DiscoveryProxyDetails pd) throws Exception {
        if (DiscoveryMgtUtils.discoveryProxyExists(pd.getName(), getConfigSystemRegistry())) {
            DiscoveryMgtUtils.removeDiscoveryProxy(pd.getName(), getConfigSystemRegistry());
        }
        DiscoveryMgtUtils.addDiscoveryProxy(pd, getConfigSystemRegistry());
    }

    /**
     * Get all the WS-D proxies configured.
     *
     * @return an array of WS-D proxy descriptions or null if no proxies are configured
     * @throws Exception on error
     */
    public DiscoveryProxyDetails[] getDiscoveryProxies() throws Exception {
        return DiscoveryMgtUtils.getDiscoveryProxies(getConfigSystemRegistry());
    }

    /**
     * Get the WS-D proxy description for the specified name
     *
     * @param name Name of the proxy
     * @return a WS-D proxy description or null if no such proxy exists
     * @throws Exception on error
     */
    public DiscoveryProxyDetails getDiscoveryProxy(String name) throws Exception {
        return DiscoveryMgtUtils.getDiscoveryProxy(name, getConfigSystemRegistry());
    }

    /**
     * Probe the specified WS-D proxy to search for target services discovered
     *
     * @param name Name of the proxy to be probed
     * @param probe Probe description containing the search criteria
     * @return an array of target service descriptions or null
     * @throws Exception on error
     */
    public TargetServiceDetails[] probeDiscoveryProxy(String name,
                                                      ProbeDetails probe) throws Exception {
        TargetService[] services = DiscoveryMgtUtils.probeDiscoveryProxy(name,
                probe, getConfigSystemRegistry());
        if (services != null) {
            TargetServiceDetails[] details = new TargetServiceDetails[services.length];
            for (int i = 0; i < services.length; i++) {
                details[i] = DiscoveryMgtUtils.getServiceDetails(services[i]);
            }
            return details;
        }
        return null;
    }

    /**
     * Contact the specified the WS-D proxy and resolve the service with the
     * specified service ID
     *
     * @param name Name of the WS-D proxy to be used
     * @param serviceId ID of the service to be resolved
     * @return a target service description or null
     * @throws Exception on error
     */
    public TargetServiceDetails resolveTargetService(String name,
                                                     String serviceId) throws Exception {
        TargetService service = DiscoveryMgtUtils.resolveService(name,
                serviceId, getConfigSystemRegistry());
        if (service != null) {
            return DiscoveryMgtUtils.getServiceDetails(service);
        }
        return null;
    }

    /**
     * Get the current service discovery configuration
     *
     * @return a ServiceDiscoveryConfig instance
     * @throws Exception on error
     */
    public ServiceDiscoveryConfig getServiceDiscoveryConfig() throws Exception {
        ServiceDiscoveryConfig config = new ServiceDiscoveryConfig();
        AxisConfiguration axisConfig = getAxisConfig();
        Parameter param = axisConfig.getParameter(DiscoveryConstants.DISCOVERY_PROXY);
        config.setEnabled(param != null);
        config.setProxyURL(DiscoveryMgtUtils.getDiscoveryProxyURL(axisConfig));
        return config;
    }

    /**
     * Enable publishing services using WS-Discovery to the specified Discovery proxy
     *
     * @param proxyURL URL of the target discovery proxy
     * @throws Exception on error
     */
    public void enableServiceDiscovery(String proxyURL) throws Exception {
        AxisConfiguration axisConfig = getAxisConfig();
        if (axisConfig.getParameter(DiscoveryConstants.DISCOVERY_PROXY) != null) {
            return;
        }

        Parameter param = new Parameter(DiscoveryConstants.DISCOVERY_PROXY, proxyURL);
        param.setParameterElement(AXIOMUtil.stringToOM("<parameter name=\"" +
                DiscoveryConstants.DISCOVERY_PROXY + "\">" + proxyURL + "</parameter>"));
        axisConfig.addParameter(param);
        Util.registerServiceObserver(axisConfig);
        DiscoveryMgtUtils.persistPublisherConfiguration(proxyURL, true, getConfigSystemRegistry());
    }

    /**
     * Disable publishing services using WS-Discovery
     *
     * @param sendBye true is BYE messages should be sent before disabling WS-Discovery
     * @throws Exception on error
     */
    public void disableServiceDiscovery(boolean sendBye) throws Exception {
        AxisConfiguration axisConfig = getAxisConfig();
        Parameter param = axisConfig.getParameter(DiscoveryConstants.DISCOVERY_PROXY);
        if (param == null) {
            return;
        }

        DiscoveryMgtUtils.persistPublisherConfiguration(param.getValue().toString(), false,
                getConfigSystemRegistry());
        Util.unregisterServiceObserver(axisConfig, sendBye);
        axisConfig.removeParameter(param);
    }
}
