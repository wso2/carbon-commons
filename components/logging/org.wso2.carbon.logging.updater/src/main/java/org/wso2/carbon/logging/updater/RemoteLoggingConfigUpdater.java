/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.logging.updater;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.logging.service.RemoteLoggingConfigService;

import java.io.IOException;

public class RemoteLoggingConfigUpdater implements Runnable {

    private static final Log LOG = LogFactory.getLog(RemoteLoggingConfigUpdater.class);

    private final RemoteLoggingConfigService remoteLoggingConfigService;

    public RemoteLoggingConfigUpdater(RemoteLoggingConfigService remoteLoggingConfigService) {

        this.remoteLoggingConfigService = remoteLoggingConfigService;
    }

    @Override
    public void run() {

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                    .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            remoteLoggingConfigService.syncRemoteServerConfigs();
            LOG.debug("successfully updated remote logging configurations");

        } catch (ConfigurationException | IOException e) {
            LOG.error("Error while updating logging configuration", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
}
