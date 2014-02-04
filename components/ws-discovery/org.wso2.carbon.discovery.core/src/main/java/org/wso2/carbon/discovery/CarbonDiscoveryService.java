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
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.base.DiscoveryService;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.discovery.client.DiscoveryClient;
import org.wso2.carbon.discovery.messages.TargetService;
import org.wso2.carbon.discovery.util.ConfigHolder;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 * The generic WS-Discovery service that can be used by any tenant or user to discover
 * service endpoints given a set of scopes and types. This service is published to
 * the Carbon OSGi runtime at the initialization of the Discovery core component and
 * any other component can make use of it after that.
 */
public class CarbonDiscoveryService implements DiscoveryService {

    private static final Log log = LogFactory.getLog(CarbonDiscoveryService.class);

    private String globalProxyURL;
    private ConfigurationContext clientCfgCtx = ConfigHolder.getInstance().
            getClientConfigurationContext();

    /**
     * Initialize the Carbon Discovery Service. If a discovery proxy is configured
     * in the Carbon configuration, this constructor attempts to find a common
     * discovery proxy URL to communicate with.
     *
     * @throws DiscoveryException If an error occurs while initializing the default client
     */
    public CarbonDiscoveryService() throws DiscoveryException {
        globalProxyURL = ServerConfiguration.getInstance().getFirstProperty(
                DiscoveryConstants.DISCOVERY_SERVICE_PROXY);
    }

    public String[] probe(QName[] types, URI[] scopes, String matchingRule,
                          int tenantId) throws CarbonException {

        // First check whether a Discovery proxy has been configured at tenant axis config level
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
        Parameter proxyURL = null;
        if (tenantDomain != null) {
            AxisConfiguration axisConf = TenantAxisUtils.getTenantAxisConfiguration(tenantDomain,
                    ConfigHolder.getInstance().getServerConfigurationContext());
            proxyURL = axisConf.getParameter(DiscoveryConstants.DISCOVERY_SERVICE_PROXY);

            if (proxyURL == null) {
                // If a discovery proxy is not set at tenant level, see if the tenant has
                // enabled service publishing and do a probe on the same URL
                proxyURL = axisConf.getParameter(DiscoveryConstants.DISCOVERY_PROXY);
            }
        }

        String proxyURLString;
        if (proxyURL != null && proxyURL.getValue() != null) {
            // Try to find a proxy URL configured at tenant AxisConfig level
            proxyURLString = proxyURL.getValue().toString();
        } else if (globalProxyURL != null) {
            // Check whether we have a globally configured proxy URL
            proxyURLString = globalProxyURL;
        } else {
            // No proxy URL => No discovery
            String msg = "No Discovery proxy has been configured at tenant level or global level";
            log.error(msg);
            throw new CarbonException(msg);
        }

        DiscoveryClient discoveryClient;
        try {
            discoveryClient = new DiscoveryClient(clientCfgCtx, proxyURLString);
        } catch (DiscoveryException e) {
            handleException("Error while initializing a Discovery client for the URL: " +
                        proxyURLString, e);
            return null;
        }

        Set<String> urlList = new HashSet<String>();
        Exception ex = null;

        try {
            TargetService[] services = discoveryClient.probe(types, scopes, matchingRule);
            if (services != null) {
                // The probe may have returned multiple services
                // Need to extract URLs from each of them
                for (TargetService service : services) {
                    if (service.getXAddresses() != null) {
                        for (URI uri : service.getXAddresses()) {
                            urlList.add(uri.toString());
                        }
                    }
                }
            }

        } catch (DiscoveryException e) {
            ex = e;
        } finally {
            try {
                discoveryClient.cleanup();
            } catch (DiscoveryException ignored) {
                // This one we let go by - We have completed the probe so we can return the URLs
                log.warn("Error while cleaning up the discovery client to URL: " + proxyURLString);
            }
        }

        if (ex != null) {
            handleException("Error while performing the Discovery probe", ex);
        }

        if (urlList.size() > 0) {
            return urlList.toArray(new String[urlList.size()]);
        }

        // This means the probe was successful - But it didn't return any services/endpoints
        return null;
    }

    private void handleException(String msg, Exception e) throws CarbonException {
        log.error(msg, e);
        throw new CarbonException(msg, e);
    }

}
