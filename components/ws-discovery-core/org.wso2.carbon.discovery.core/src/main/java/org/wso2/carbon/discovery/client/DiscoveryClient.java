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

package org.wso2.carbon.discovery.client;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.AxisFault;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAbstractFactory;
import org.wso2.carbon.discovery.messages.Probe;
import org.wso2.carbon.discovery.messages.TargetService;
import org.wso2.carbon.discovery.messages.QueryMatch;
import org.wso2.carbon.discovery.messages.Resolve;
import org.wso2.carbon.discovery.DiscoveryException;
import org.wso2.carbon.discovery.DiscoveryConstants;
import org.wso2.carbon.discovery.DiscoveryOMUtils;

import javax.xml.namespace.QName;
import java.net.URI;

/**
 * The WS-Discovery client API. This API can be used to probe external discovery proxies by
 * sending WS-D probe requests. Also it can be used to WS-D resolve requests. This API is a
 * wrapper for the Axis2 client API. Therefore Axis2 modules such as Addressing and Rampart
 * can be enagaged on the DiscoveryClient to support various QoS requirements. However since
 * the ServiceClient API of Axis2 is not intended to be thread safe, it is not recommended
 * to use this class as a shared instance in a multithreaded setting. 
 */
public class DiscoveryClient {

    private ServiceClient serviceClient;

    public DiscoveryClient(ConfigurationContext cfgCtx, String proxyURL) throws DiscoveryException {
        try {
            serviceClient = new ServiceClient(cfgCtx, null);
            serviceClient.setTargetEPR(new EndpointReference(proxyURL));
        } catch (AxisFault axisFault) {
            throw new DiscoveryException("Error while initializing the WS-Discovery client", axisFault);
        }
    }

    public void setProperty(String name, Object value) {
        serviceClient.getOptions().setProperty(name, value);    
    }

    /**
     * Engage the named module on the Discovery client
     *
     * @param module Name of the module (eg: rampart)
     * @throws DiscoveryException If an error occurs while engaging the named module
     */
    public void engageModule(String module) throws DiscoveryException {
        try {
            serviceClient.engageModule(module);
        } catch (AxisFault axisFault) {
            throw new DiscoveryException("Error while engaging the module : " + module, axisFault);
        }
    }

    /**
     * Send a generic probe request to the Discovery proxy. The probe request will
     * not contain any scope information or type information on it and hence the proxy
     * will return all the target services registered on the proxy.
     *
     * @return an array of TargetService objects
     * @throws DiscoveryException if an error occured while contacting the WS-D proxy
     */
    public TargetService[] probe() throws DiscoveryException {
        return probe(null, null, null);
    }

    /**
     * Send a probe request with the specified types and scopes to the discovery proxy.
     * Default matching rule will be used to search for available target services.
     *
     * @param types Types to be included in the probe
     * @param scopes Scopes to be included in the probe
     * @return an array of TargetService objects
     * @throws DiscoveryException if an error occured while contacting the WS-D proxy
     */
    public TargetService[] probe(QName[] types, URI[] scopes) throws DiscoveryException {
        return probe(types, scopes, null);
    }

    /**
     * Send a probe request with the specified types and scopes to the discovery proxy.
     * The specified matching rule will be used to search for available target services.
     *
     * @param types Types to be included in the probe
     * @param scopes Scopes to be included in the probe
     * @param matchBy The matching rule for scopes
     * @return an array of TargetService objects
     * @throws DiscoveryException if an error occured while contacting the WS-D proxy
     */
    public TargetService[] probe(QName[] types, URI[] scopes, String matchBy)
            throws DiscoveryException {

        Probe probe = new Probe();
        probe.setTypes(types);
        probe.setScopes(scopes);

        if (matchBy == null) {
            matchBy = DiscoveryConstants.SCOPE_MATCH_RULE_DEAULT;
        }
        probe.setMatchBy(URI.create(matchBy));

        serviceClient.getOptions().setAction(DiscoveryConstants.WS_DISCOVERY_PROBE_ACTION);

        try {
            OMElement probeMatch = serviceClient.sendReceive(DiscoveryOMUtils.toOM(probe,
                    OMAbstractFactory.getSOAP11Factory()));
            QueryMatch match = DiscoveryOMUtils.getProbeMatchFromOM(probeMatch);
            serviceClient.cleanupTransport();
            return match.getTargetServices();
        } catch (AxisFault axisFault) {
            throw new DiscoveryException("Error while executing the WS-Discovery probe", axisFault);
        }
    }

    /**
     * Send a WS-Discovery resolve request for the given service ID
     * 
     * @param epr ID of the service to be resolved
     * @return a TargetService object or null if the ID cannot be resolved
     * @throws DiscoveryException if an error occured while contacting the WS-D proxy
     */
    public TargetService resolve(String epr) throws DiscoveryException {
        if (epr == null) {
            throw new DiscoveryException("Cannot perform the Resolve operation with a null EPR");
        }

        Resolve resolve = new Resolve(new EndpointReference(epr));

        serviceClient.getOptions().setAction(DiscoveryConstants.WS_DISCOVERY_RESOLVE_ACTION);

        try {
            OMElement resolveMatch = serviceClient.sendReceive(DiscoveryOMUtils.toOM(resolve,
                    OMAbstractFactory.getSOAP11Factory()));
            QueryMatch match = DiscoveryOMUtils.getResolveMatchFromOM(resolveMatch);
            serviceClient.cleanupTransport();
            if (match.getTargetServices() != null && match.getTargetServices().length == 1) {
                return match.getTargetServices()[0];
            }
        } catch (AxisFault axisFault) {
            throw new DiscoveryException("Error while executing the WS-Discovery resolve", axisFault);
        }
        return null;
    }

    /**
     * Cleanup the service client
     *
     * @throws DiscoveryException on error
     */
    public void cleanup() throws DiscoveryException {
        try {
            serviceClient.cleanup();
        } catch (AxisFault axisFault) {
            throw new DiscoveryException("Error while cleaning up the WS-Discovery client", axisFault);
        }
    }
}

