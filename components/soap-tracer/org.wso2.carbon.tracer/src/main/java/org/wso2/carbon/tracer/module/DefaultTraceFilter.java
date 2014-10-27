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
package org.wso2.carbon.tracer.module;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ConfigurationContext;

/**
 * 
 */
public class DefaultTraceFilter implements TraceFilter{
    
    public void setConfigurationContext(ConfigurationContext configCtx) {
        //TODO: Method implementation
    }

    public void addServiceGroupToFilter(String serviceGroupName) {
        // Do nothing
    }

    public void addServiceToFilter(String serviceName) {
        // Do nothing
    }

    public boolean isFilteredOut(MessageContext msgContext) {
        return msgContext.isDoingMTOM();
    }
}
