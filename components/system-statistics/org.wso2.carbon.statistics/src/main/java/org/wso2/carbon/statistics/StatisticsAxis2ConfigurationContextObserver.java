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
package org.wso2.carbon.statistics;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

/**
 * This class handled globally engaging statistic module when a new ConfigurationContext is created
 */
public class StatisticsAxis2ConfigurationContextObserver
        extends AbstractAxis2ConfigurationContextObserver {

    private Log log = LogFactory.getLog(StatisticsAxis2ConfigurationContextObserver.class);

    public void createdConfigurationContext(ConfigurationContext configurationContext) {
        AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();
        try {
            if (axisConfig.getModule(StatisticsConstants.STATISTISTICS_MODULE_NAME) != null) {
                axisConfig.engageModule(StatisticsConstants.STATISTISTICS_MODULE_NAME);
            }
        } catch (Throwable e) {
            PrivilegedCarbonContext carbonContext =
                    PrivilegedCarbonContext.getThreadLocalCarbonContext();
            String msg;
            if (carbonContext.getTenantDomain() != null) {
                msg = "Could not globally engage " + StatisticsConstants.STATISTISTICS_MODULE_NAME +
                      " module to tenant " + carbonContext.getTenantDomain() +
                      "[" + carbonContext.getTenantId() + "]";
            } else {
                msg = "Could not globally engage " + StatisticsConstants.STATISTISTICS_MODULE_NAME +
                      " module to super tenant ";
            }
            log.error(msg, e);
        }
    }

}
