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

package org.wso2.carbon.discovery.proxy;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.discovery.DiscoveryConstants;
import org.wso2.carbon.discovery.DiscoveryException;
import org.wso2.carbon.discovery.DiscoveryOMUtils;
import org.wso2.carbon.discovery.messages.Notification;
import org.wso2.carbon.discovery.messages.Probe;
import org.wso2.carbon.discovery.messages.QueryMatch;
import org.wso2.carbon.discovery.messages.Resolve;
import org.wso2.carbon.discovery.messages.TargetService;
import org.wso2.carbon.discovery.util.DiscoveryServiceUtils;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Web Services Dynamic Discovery proxy implementation. This service implementation
 * supports the four basic operations of a WS-Discovery proxy (managed mode) as described
 * in the protocol specification. Supported operations are Hello, Bye, Probe and Resolve.
 * Service metadata are stored in a WSO2 governance registry instance for later reference.
 */
public class DiscoveryProxy {

    private static final int DEFAULT_RETRY_TIMEOUT_SEC = 10;
    private static final int DEFAULT_INITIAL_DELAY_SEC = 10;
    private static final int RETRY_COUNT = 5;

    private static final Log log = LogFactory.getLog(DiscoveryProxy.class);

    public void Hello(OMElement helloElement) throws DiscoveryException {
        final Notification hello = DiscoveryOMUtils.getHelloFromOM(helloElement);
        final Map<String, String> headerMap = extractHeader();
        submitServiceForDiscovery(hello, headerMap);
    }

    private void submitServiceForDiscovery(final Notification hello,
                                           final Map<String, String> headerMap) {
        CarbonContext context = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        final String tenantDomain = context.getTenantDomain();
        final String username = context.getUsername();
        final int tenantId = context.getTenantId();
        // We need to copy the current thread's context details into the thread local context.
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext localHolder =
                            PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    localHolder.setTenantDomain(tenantDomain);
                    if (username != null) {
                        localHolder.setUsername(username);
                    }
                    localHolder.setTenantId(tenantId);
                    // Initially, we wait for a delay before beginning to discover the service. This is
                    // to leave room for service deployment.
                    Thread.sleep(DEFAULT_INITIAL_DELAY_SEC * 1000);
                    for (int i = 0; i < RETRY_COUNT; i++) {
                        try {
                            DiscoveryServiceUtils.addService(hello.getTargetService(), headerMap);
                            break;
                        } catch (RegistryException ignore) {
                            // If the WSDL import failed, it might be due to service not being deployed. So,
                            // we'll retry.
                            final long timeout = DEFAULT_RETRY_TIMEOUT_SEC *
                                                 (long) Math.pow(2, i);
                            log.info("Service Discovery Failed. Retrying after " + timeout + "s.");
                            Thread.sleep(timeout * 1000);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error while persisting the service description", e);
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }
        };
        Executors.newSingleThreadExecutor().submit(runnable);
    }

    private Map<String, String> extractHeader() {
        Map<String, String> map = new HashMap<String, String>();
        SOAPHeader header = MessageContext.getCurrentMessageContext().getEnvelope().getHeader();
        List<OMElement> elementList =
                header.getHeaderBlocksWithNSURI(DiscoveryConstants.DISCOVERY_HEADER_ELEMENT_NAMESPACE);
        for (OMElement element : elementList) {
            map.put(element.getLocalName(), element.getText());
        }
        return map;
    }

    public void Bye(OMElement byeElement) throws DiscoveryException {
        Notification bye = DiscoveryOMUtils.getByeFromOM(byeElement);
        try {
            DiscoveryServiceUtils.deactivateService(bye.getTargetService());
        } catch (Exception e) {
            throw new DiscoveryException("Error while persisting the " +
                    "service description", e);
        }
    }

    public OMElement Probe(OMElement probeElement) throws DiscoveryException {
        Probe probe = DiscoveryOMUtils.getProbeFromOM(probeElement);
        try {
            TargetService[] services = DiscoveryServiceUtils.findServices(probe);
            QueryMatch match = new QueryMatch(DiscoveryConstants.RESULT_TYPE_PROBE_MATCH,
                    services);
            return DiscoveryOMUtils.toOM(match, OMAbstractFactory.getSOAP11Factory());
        } catch (Exception e) {
            throw new DiscoveryException("Error while searching for services", e);
        }
    }

    public OMElement Resolve(OMElement resolveElement) throws DiscoveryException {
        Resolve resolve = DiscoveryOMUtils.getResolveFromOM(resolveElement);
        try {
            TargetService service = DiscoveryServiceUtils.getService(resolve.getEpr());
            QueryMatch match = new QueryMatch(DiscoveryConstants.RESULT_TYPE_RESOLVE_MATCH,
                    new TargetService[] { service });
            return DiscoveryOMUtils.toOM(match, OMAbstractFactory.getSOAP11Factory());
        } catch (Exception e) {
            throw new DiscoveryException("Error while resolving the service with ID: " +
                resolve.getEpr().getAddress());
        }
    }

}
