/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.discovery.module.handlers;

import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.description.Parameter;
import org.wso2.carbon.discovery.DiscoveryConstants;
import org.wso2.carbon.discovery.DiscoveryException;
import org.wso2.carbon.discovery.messages.TargetService;
import org.wso2.carbon.discovery.client.DiscoveryClient;

import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URISyntaxException;

public class DiscoveryOutHandler extends AbstractHandler {

    public InvocationResponse invoke(MessageContext messageContext) throws AxisFault {

        if ((messageContext.getTo() != null) &&
                (messageContext.getTo().getAddress().equals(DiscoveryConstants.DISCOVERY_TARGET_EPR))) {
            // if the message is send to discovery target end point we find the correct target end point
            // and proceed with the flow
            String discoveryProxy =
                    (String) messageContext.getOptions().getProperty(DiscoveryConstants.DISCOVERY_PROXY);
            if (discoveryProxy == null) {
                Parameter param = messageContext.getAxisService().getParameter(DiscoveryConstants.DISCOVERY_PROXY);
                if (param != null) {
                    discoveryProxy = (String) param.getValue();
                } else {
                    throw new AxisFault("Discovery Proxy Address has not been set");
                }
            }

            String[] scopes =
                    (String[]) messageContext.getOptions().getProperty(DiscoveryConstants.DISCOVERY_SCOPES);
            if (scopes == null) {
                scopes = new String[]{DiscoveryConstants.DISCOVERY_DEFAULT_SCOPE};
            }

            URI[] uriScopes = new URI[scopes.length];
            QName[] types =
                    (QName[]) messageContext.getOptions().getProperty(DiscoveryConstants.DISCOVERY_TYPES);

            try {
                for (int i = 0; i < scopes.length; i++) {
                    uriScopes[i] = new URI(scopes[i]);
                }
                DiscoveryClient discoveryClient = new DiscoveryClient(messageContext.getConfigurationContext(), discoveryProxy);
                TargetService[] targetServices = discoveryClient.probe(types, uriScopes);
                if ((targetServices != null) && (targetServices.length > 0)) {

                    // currently we try to use the first one.
                    URI targetURI = null;
                    if ((targetServices[0].getXAddresses() != null) && (targetServices[0].getXAddresses().length > 0)) {
                        targetURI = getTargetURI(targetServices[0].getXAddresses(),
                                (String) messageContext.getProperty(DiscoveryConstants.DISCOVERY_SCHEME));
                    } else {
                        // get the epr using a resolve request
                        TargetService targetService = discoveryClient.resolve(targetServices[0].getEpr().getAddress());
                        if ((targetService.getXAddresses() != null) && (targetService.getXAddresses().length > 0)) {
                            targetURI = getTargetURI(targetService.getXAddresses(),
                                    (String) messageContext.getProperty(DiscoveryConstants.DISCOVERY_SCHEME));
                        } else {
                            throw new AxisFault("Resolve message did not return the service location");
                        }
                    }
                    messageContext.setTo(new EndpointReference(targetURI.toString()));

                } else {
                    throw new AxisFault("Can not find an epr for given description");
                }
            } catch (DiscoveryException e) {
                throw new AxisFault("Can not create the discovery client ", e);
            } catch (URISyntaxException e) {
                throw new AxisFault("Can not create the URI from the scope", e);
            }

        }

        return InvocationResponse.CONTINUE;
    }

    private URI getTargetURI(URI[] xAddresses, String scheme) {

        URI targetAddress = null;
        // take http as the default scheme
        if (scheme == null) {
            scheme = "http";
        }
        for (URI xAddress : xAddresses) {
            if (xAddress.getScheme().equals(scheme)) {
                targetAddress = xAddress;
                break;
            }
        }

        if (targetAddress == null) {
            targetAddress = xAddresses[0];
        }

        return targetAddress;
    }

}
