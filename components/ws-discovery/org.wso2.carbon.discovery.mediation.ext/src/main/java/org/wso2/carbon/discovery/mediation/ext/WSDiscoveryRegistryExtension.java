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

package org.wso2.carbon.discovery.mediation.ext;

import org.wso2.carbon.discovery.client.DiscoveryClient;
import org.wso2.carbon.discovery.util.ConfigHolder;
import org.wso2.carbon.discovery.DiscoveryException;
import org.wso2.carbon.discovery.messages.TargetService;
import org.apache.axiom.om.OMNode;
import org.apache.axis2.context.ConfigurationContext;


public class WSDiscoveryRegistryExtension extends DiscoveryRegistryExtension {

    public OMNode findEndpoint(String uuid, String protocol) throws Exception {
        ConfigurationContext cfgCtx = ConfigHolder.getInstance().getClientConfigurationContext();
        String proxyURL = properties.getProperty(DISCOVERY_PROXY_URL);
        if (proxyURL == null) {
            throw new DiscoveryException("The discovery proxy URL is not specified");
        }
        DiscoveryClient client = new DiscoveryClient(cfgCtx, proxyURL);
        TargetService service = client.resolve(uuid);
        client.cleanup();
        return getEndpointFromService(service, protocol);
    }
}
