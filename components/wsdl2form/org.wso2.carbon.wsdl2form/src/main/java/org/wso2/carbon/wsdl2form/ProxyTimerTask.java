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
package org.wso2.carbon.wsdl2form;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;

/**
 * This will be the timer task that will be alive in every 30 mins (This value is hard coded and
 * cannot be configured.) in the corresponding AxisServiceGroup and removed the proxy services. If
 * there exist only one proxy service and if it is not touched, AxisServiceGroup will also removed
 * from the list.
 */
public class ProxyTimerTask extends TimerTask {

    private AxisConfiguration axisConfig;
    private static Log log = LogFactory.getLog(ProxyTimerTask.class);

    public ProxyTimerTask(AxisConfiguration axisConfig) {
        this.axisConfig = axisConfig;
    }
    public void run() {

        synchronized (axisConfig) {
            PrivilegedCarbonContext.startTenantFlow();
            try {
                PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                privilegedCarbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
                privilegedCarbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);

                AxisServiceGroup proxyAxisServiceGroup =
                        axisConfig.getServiceGroup(WSDL2FormGenerator.TRYIT_SG_NAME);
                if (proxyAxisServiceGroup != null) {
                    List removeServiceList = new ArrayList();
                    for (Iterator iterator = proxyAxisServiceGroup.getServices();
                         iterator.hasNext();) {
                        AxisService axisServce = (AxisService) iterator.next();
                        Long longTime =
                                (Long) axisServce
                                        .getParameterValue(WSDL2FormGenerator.LAST_TOUCH_TIME);
                        if ((System.currentTimeMillis() - longTime.longValue()) > WSDL2FormGenerator
                                .PERIOD) {
                            removeServiceList.add(axisServce.getName());
                        }

                    }
                    if (removeServiceList.size() > 0) {
                        for (Iterator iterator = removeServiceList.iterator(); iterator.hasNext();)
                        {
                            String axisServiceName = (String) iterator.next();
                            proxyAxisServiceGroup.removeService(axisServiceName);
                        }
                    }
                    boolean isLast = proxyAxisServiceGroup.getServices().hasNext();
                    if (!isLast) {
                        axisConfig.removeServiceGroup(WSDL2FormGenerator.TRYIT_SG_NAME);
                    }
                }
            } catch (AxisFault axisFault) {
                String msg = "Fault occured when manipulating Tryit proxy service group";
                log.error(msg, axisFault);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }

        }
    }
}