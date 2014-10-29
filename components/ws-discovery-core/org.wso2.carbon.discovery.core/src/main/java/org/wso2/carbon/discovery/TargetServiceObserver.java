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

package org.wso2.carbon.discovery;

import org.apache.axis2.description.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEvent;
import org.apache.axis2.engine.AxisObserver;
import org.apache.axis2.AxisFault;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.discovery.util.DiscoveryMgtUtils;
import org.wso2.carbon.discovery.workers.MessageSender;
import org.wso2.carbon.CarbonConstants;

import java.util.ArrayList;
import java.util.Map;

/**
 * This AxisObserver implementation monitors the AxisConfiguration for service
 * life cycle events such as service start, stop, transport binding updates and
 * sends WS-D notifications as necessary.
 */
public class TargetServiceObserver implements AxisObserver {

    ParameterInclude parameterInclude = null;
    AxisConfiguration axisConfiguration = null;

    private Log log = LogFactory.getLog(TargetServiceObserver.class);

    public TargetServiceObserver() {
        if (log.isDebugEnabled()) {
            log.debug("Initializing the TargetServiceObserver for WS-Discovery notifications");
        }
        this.parameterInclude = new ParameterIncludeImpl();
    }

    public void init(AxisConfiguration axisConfig) {
        this.axisConfiguration = axisConfig;
        // There may be services deployed already
        // We need to send Hello messages for such services
        // Services deployed later will be picked up by the serviceUpdate event
        Map<String, AxisService> services = axisConfig.getServices();
        for (AxisService service : services.values()) {
            sendHello(service);
        }
    }

    private void sendHello(AxisService service) {
        // send the hello message
        MessageSender messageSender = new MessageSender();
        Parameter discoveryProxyParam = DiscoveryMgtUtils.getDiscoveryParam(this.axisConfiguration);
        if (discoveryProxyParam != null) {
            if (log.isDebugEnabled()) {
                log.debug("Sending WS-Discovery Hello message for the " +
                        "service " + service.getName());
            }

            try {
                messageSender.sendHello(service, (String) discoveryProxyParam.getValue());
            } catch (DiscoveryException e) {
                log.error("Cannot send the hello message ", e);
            }
        }
    }

    public void serviceUpdate(AxisEvent event, AxisService service) {
        int eventType = event.getEventType();
        if (eventType == AxisEvent.SERVICE_START ||
                eventType == AxisEvent.SERVICE_DEPLOY ||
                eventType == CarbonConstants.AxisEvent.TRANSPORT_BINDING_ADDED ||
                eventType == CarbonConstants.AxisEvent.TRANSPORT_BINDING_REMOVED) {

            // send the hello message
            sendHello(service);

        } else if ((event.getEventType() == AxisEvent.SERVICE_STOP) ||
                        (event.getEventType() == AxisEvent.SERVICE_REMOVE)){
            // send the bye message
            MessageSender messageSender = new MessageSender();
            Parameter discoveryProxyParam =
                    this.axisConfiguration.getParameter(DiscoveryConstants.DISCOVERY_PROXY);

            if (discoveryProxyParam != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Sending WS-Discovery Bye message for the " +
                            "service " + service.getName());
                }

                try {
                    messageSender.sendBye(service, (String) discoveryProxyParam.getValue());
                } catch (DiscoveryException e) {
                    log.error("Cannot send the bye message ", e);
                }
            }
        }
    }

    public void serviceGroupUpdate(AxisEvent event, AxisServiceGroup serviceGroup) {

    }

    public void moduleUpdate(AxisEvent event, AxisModule module) {

    }

    public void addParameter(Parameter param) throws AxisFault {
        this.parameterInclude.addParameter(param);
    }

    public void removeParameter(Parameter param) throws AxisFault {
        this.parameterInclude.removeParameter(param);
    }

    public void deserializeParameters(OMElement parameterElement) throws AxisFault {
        this.parameterInclude.deserializeParameters(parameterElement);
    }

    public Parameter getParameter(String name) {
        return this.parameterInclude.getParameter(name);
    }

    public ArrayList<Parameter> getParameters() {
        return this.parameterInclude.getParameters();
    }

    public boolean isParameterLocked(String parameterName) {
        return this.parameterInclude.isParameterLocked(parameterName);
    }
}
