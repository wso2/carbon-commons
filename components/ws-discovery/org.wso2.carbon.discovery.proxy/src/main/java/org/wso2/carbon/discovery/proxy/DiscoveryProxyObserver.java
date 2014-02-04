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

import org.apache.axis2.engine.AxisObserver;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEvent;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.AxisFault;
import org.apache.axiom.om.OMElement;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class DiscoveryProxyObserver implements AxisObserver {

    private static final Log log = LogFactory.getLog(DiscoveryProxyObserver.class);

    public static final String WSD_SEC_POLICY_FILE = "wsd-sec-policy.xml";

    public void init(AxisConfiguration axisConfiguration) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing WS-Discovery proxy observer");
        }

        try {
            AxisService service = axisConfiguration.getService("DiscoveryProxy");
            if (service != null) {
                engageSecurity(service);
            }
        } catch (AxisFault ignore) {

        }
    }

    public void serviceUpdate(AxisEvent axisEvent, AxisService axisService) {
        if ("DiscoveryProxy".equals(axisService.getName()) &&
                axisEvent.getEventType() == AxisEvent.SERVICE_DEPLOY) {

            engageSecurity(axisService);
        }
    }

    private void engageSecurity(AxisService service) {
        String path = CarbonUtils.getCarbonConfigDirPath() + File.separator + WSD_SEC_POLICY_FILE;
        FileInputStream in = null;
        try {
            in = new FileInputStream(path);
            AxisModule module = service.getAxisConfiguration().getModule("rampart");
            if (module == null) {
                log.error("Rampart module is not available in the system. Unable to engage " +
                        "security on the WS-Discovery proxy.");
                return;
            }

            log.info("Loading security policy for the WS-Discovery proxy from " + path);
            Policy policy = PolicyEngine.getPolicy(in);
            service.getPolicySubject().attachPolicy(policy);
            service.engageModule(module);

        } catch (FileNotFoundException e) {
            if (log.isDebugEnabled()) {
                log.debug("No security policies specified for the WS-Discovery proxy");
            }
        } catch (AxisFault e) {
            log.error("Error while engaging security on the WS-Discovery proxy", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("Error while closing the input stream to file: " + path, e);
                }
            }
        }
    }

    public void moduleUpdate(AxisEvent axisEvent, AxisModule axisModule) {

    }

    public void serviceGroupUpdate(AxisEvent axisEvent, AxisServiceGroup axisServiceGroup) {

    }

    public void addParameter(Parameter parameter) throws AxisFault {

    }

    public void deserializeParameters(OMElement omElement) throws AxisFault {

    }

    public Parameter getParameter(String s) {
        return null;
    }

    public ArrayList<Parameter> getParameters() {
        return null;
    }

    public boolean isParameterLocked(String s) {
        return false;
    }

    public void removeParameter(Parameter parameter) throws AxisFault {

    }
}
