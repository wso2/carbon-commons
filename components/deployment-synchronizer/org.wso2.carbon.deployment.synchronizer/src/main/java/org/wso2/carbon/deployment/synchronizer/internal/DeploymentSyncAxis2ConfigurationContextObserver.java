/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.deployment.synchronizer.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.deployment.synchronizer.DeploymentSynchronizerException;
import org.wso2.carbon.deployment.synchronizer.internal.repository.CarbonRepositoryUtils;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.deployment.GhostDeployerUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * Keeps track of tenant configuration context creation and engages the deployment synchronizer for
 * their repositories as necessary.
 */
public class DeploymentSyncAxis2ConfigurationContextObserver extends AbstractAxis2ConfigurationContextObserver {

    private static final Log log = LogFactory.getLog(DeploymentSyncAxis2ConfigurationContextObserver.class);

    @Override
    public void creatingConfigurationContext(int tenantId) {
        try {
            if (!CarbonRepositoryUtils.isSynchronizerEnabled(tenantId)) {
                return;
            }

            if (log.isDebugEnabled()) {
                log.debug("Initializing the deployment synchronizer for tenant: " + tenantId);
            }

            DeploymentSynchronizer depsync =
                    CarbonRepositoryUtils.newCarbonRepositorySynchronizer(tenantId);

            if (GhostDeployerUtils.isGhostOn() && GhostDeployerUtils.isPartialUpdateEnabled()
                    && CarbonUtils.isWorkerNode() && tenantId > 0) {
                depsync.syncGhostMetaArtifacts();
            } else {
                depsync.doInitialSyncUp();
            }
            //TODO: Need to sync up only the ghost metadata is ghost deployment has been enabled
        } catch (DeploymentSynchronizerException e) {
            log.error("Error while initializing the deployment synchronizer for tenant: " + tenantId);
        }
    }

    @Override
    public void terminatingConfigurationContext(ConfigurationContext configCtx) {
        int tenantId = MultitenantUtils.getTenantId(configCtx);
        DeploymentSynchronizationManager.getInstance().deleteSynchronizer(tenantId);
    }
}
