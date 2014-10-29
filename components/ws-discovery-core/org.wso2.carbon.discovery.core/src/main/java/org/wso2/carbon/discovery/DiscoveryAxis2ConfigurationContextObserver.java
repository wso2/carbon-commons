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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.discovery.util.DiscoveryMgtUtils;
import org.wso2.carbon.discovery.util.Util;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

/**
 * This observer implementation listens for configuration context creation and termination
 * events and enables/disables WS-Discovery for individual tenants as necessary. When a tenant
 * configuration context is created this observer checks whether WS-Discovery should be enabled
 * for that tenant by looking at a tenant AxisConfiguration parameter. If discovery should
 * be enabled, it will initiate a TargetServiceObserver instance and register with the tenant
 * AxisConfiguration. This will cause necessary HELLO messages to be sent for all the services
 * already deployed in the respective tenant AxisConfiguration.
 * Similarly when a tenant is terminated, it will look whether discovery is
 * enabled in it, and send out WS-Discovery BYE messages for all the deployed services. Then
 * it will wrap it up by disengaging the TargetServiceObserver from the tenant AxisConfiguration.
 */
public class DiscoveryAxis2ConfigurationContextObserver extends AbstractAxis2ConfigurationContextObserver {

    private static final Log log = LogFactory.getLog(DiscoveryAxis2ConfigurationContextObserver.class);

    public void createdConfigurationContext(ConfigurationContext configurationContext) {
        AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();
        try {
            if (DiscoveryMgtUtils.isServiceDiscoveryEnabled(axisConfig)) {
                if (log.isDebugEnabled()) {
                    String domain = PrivilegedCarbonContext.getThreadLocalCarbonContext().
                            getTenantDomain(true);
                    log.debug("Registering the Axis observer for WS-Discovery in tenant: " + domain);
                }
                Util.registerServiceObserver(axisConfig);
            }
        } catch (RegistryException e) {
            log.error("Checking whether service discovery is enabled for a tenant", e);
        }
    }

    public void terminatingConfigurationContext(ConfigurationContext configurationContext) {
        AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();
        Util.unregisterServiceObserver(axisConfig, true);
    }

}
