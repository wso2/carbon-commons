/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.logging.service.sync;


import org.apache.axis2.clustering.ClusteringCommand;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.logging.service.util.LoggingUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * Cluster message to be sent when logging configurations are modified.
 */
public class LoggingConfigSyncRequest extends ClusteringMessage {

    private static final Log log = LogFactory.getLog(LoggingConfigSyncRequest.class);
    private static final long serialVersionUID = -689482772914112325L;

    public LoggingConfigSyncRequest() {
    }

    @Override
    public void execute(ConfigurationContext configurationContext) throws ClusteringFault {
        if (log.isDebugEnabled()) {
            log.debug("Received [" + this + "] ");
        }
        
        try {
            // load the configuration from registry as ST
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            carbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);

            LoggingUtil.loadCustomConfiguration();
        } catch (Exception e) {
            log.error("Cannot load logging configuration from the registry", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public ClusteringCommand getResponse() {
        return null;
    }

    @Override
    public String toString() {
        return "LoggingConfigSyncRequest{" + "messageId=" + getUuid() + "}";
    }
}
