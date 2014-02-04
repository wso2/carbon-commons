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

import org.wso2.carbon.mediation.registry.RegistryExtension;
import org.wso2.carbon.discovery.messages.TargetService;
import org.apache.axiom.om.OMNode;
import org.apache.synapse.endpoints.AddressEndpoint;
import org.apache.synapse.endpoints.EndpointDefinition;
import org.apache.synapse.config.xml.endpoints.EndpointSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Properties;
import java.net.URI;

/**
 * Discovery registry extensions can find service endpoints given a key of the
 * form wsdd://<Service UUID>/<Protocol>. The protocol part is optional so when
 * not provided the first endpoint returned by the discovery API will be used as
 * the service endpoint. These extension implementations can be engaged into the
 * Carbon mediation registry adapter through the Synapse configuration. The returned
 * OMNode instances can be used to initialize Synapse address endpoints.
 */
public abstract class DiscoveryRegistryExtension implements RegistryExtension {

    public static final String WSDD_PREFIX = "wsdd://";
    public static final String DISCOVERY_PROXY_URL = "discoveryProxy";

    protected Properties properties;
    protected Log log;

    public void init(Properties properties) {
        log = LogFactory.getLog(this.getClass());
        this.properties = properties;
        if (log.isDebugEnabled()) {
            log.debug("Registry extension initialized");
        }
    }

    public OMNode lookup(String key) {
        String protocol = null;

        if (!key.startsWith(WSDD_PREFIX)) {
            return null;
        }

        key = key.substring(WSDD_PREFIX.length());
        int index = key.indexOf('/');
        if (index != -1) {
            protocol = key.substring(index + 1);
            key = key.substring(0, index);
        }

        if ("".equals(key)) {
            return null;
        }

        try {
            return findEndpoint(key, protocol);
        } catch (Exception e) {
            log.error("Error while retreiving discovered endpoint", e);
        }

        return null;
    }

    protected OMNode createAddressEndpoint(URI uri) {
        AddressEndpoint endpoint = new AddressEndpoint();
        EndpointDefinition def = new EndpointDefinition();
        def.setAddress(uri.toString());
        endpoint.setDefinition(def);
        return EndpointSerializer.getElementFromEndpoint(endpoint);
    }

    protected OMNode getEndpointFromService(TargetService service, String protocol) {
        if (service != null && service.getXAddresses() != null &&
                service.getXAddresses().length > 0) {
            
            URI[] addresses = service.getXAddresses();
            if (addresses.length > 1 && protocol != null && !"".equals(protocol)) {
                for (URI address : addresses) {
                    if (address.getScheme().equals(protocol)) {
                        return createAddressEndpoint(address);
                    }
                }
            } else {
                return createAddressEndpoint(addresses[0]);
            }
        }
        return null;
    }

    public abstract OMNode findEndpoint(String uuid, String protocol) throws Exception;
}
