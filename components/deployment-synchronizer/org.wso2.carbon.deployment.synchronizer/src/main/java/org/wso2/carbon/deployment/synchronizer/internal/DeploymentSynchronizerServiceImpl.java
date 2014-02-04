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

import org.wso2.carbon.deployment.synchronizer.DeploymentSynchronizerException;
import org.wso2.carbon.deployment.synchronizer.internal.repository.CarbonRepositoryUtils;
import org.wso2.carbon.deployment.synchronizer.internal.util.DeploymentSynchronizerConfiguration;
import org.wso2.carbon.deployment.synchronizer.services.DeploymentSynchronizerService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.deployment.GhostDeployerUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public class DeploymentSynchronizerServiceImpl implements DeploymentSynchronizerService,
                                                          org.wso2.carbon.core.deployment.DeploymentSynchronizer {

    private DeploymentSynchronizationManager syncManager = DeploymentSynchronizationManager.getInstance();

    public boolean synchronizerExists(String filePath) {
        return syncManager.getSynchronizer(filePath) != null;
    }

    public boolean isAutoCommitOn(String filePath) throws DeploymentSynchronizerException {
        DeploymentSynchronizer synchronizer = getSynchronizer(filePath);
        return synchronizer.isAutoCommit();
    }

    public boolean isAutoCheckoutOn(String filePath) throws DeploymentSynchronizerException {
        DeploymentSynchronizer synchronizer = getSynchronizer(filePath);
        return synchronizer.isAutoCheckout();
    }

    public long getLastCommitTime(String filePath) throws DeploymentSynchronizerException {
        DeploymentSynchronizer synchronizer = getSynchronizer(filePath);
        return synchronizer.getLastCommitTime();
    }

    public long getLastCheckoutTime(String filePath) throws DeploymentSynchronizerException {
        DeploymentSynchronizer synchronizer = getSynchronizer(filePath);
        return synchronizer.getLastCheckoutTime();
    }

    public boolean update(int tenantId) {
        try {
            DeploymentSynchronizerConfiguration configuration =
                    CarbonRepositoryUtils.getActiveSynchronizerConfiguration(tenantId);

            //If Deployment Synchronization is disabled or autoCheckout is false, return false
            if(!configuration.isEnabled() || !configuration.isAutoCheckout()){
                return false;
            }
            DeploymentSynchronizer synchronizer =
                    getSynchronizer(MultitenantUtils.getAxis2RepositoryPath(tenantId));
            if (synchronizer == null) {
                synchronizer =
                        CarbonRepositoryUtils.newCarbonRepositorySynchronizer(tenantId);

                if(synchronizer == null){
                    return false;
                }

                if (GhostDeployerUtils.isGhostOn() && GhostDeployerUtils.isPartialUpdateEnabled() &&
                        CarbonUtils.isWorkerNode() && tenantId > 0) {
                    synchronizer.syncGhostMetaArtifacts();
                } else {
                    synchronizer.doInitialSyncUp();
                }
            } else if (GhostDeployerUtils.isGhostOn() && GhostDeployerUtils.isPartialUpdateEnabled() &&
                    CarbonUtils.isWorkerNode() && tenantId > 0) {
                return synchronizer.syncGhostMetaArtifacts();
            }
            return synchronizer.checkout();
        } catch (DeploymentSynchronizerException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean commit(int tenantId) {

        try {
            DeploymentSynchronizerConfiguration configuration =
                    CarbonRepositoryUtils.getActiveSynchronizerConfiguration(tenantId);

            //If Deployment Synchronization is disabled or autoCommit is false, return false
            if(!configuration.isEnabled() || !configuration.isAutoCommit()){
                return false;
            }
            DeploymentSynchronizer synchronizer =
                    getSynchronizer(MultitenantUtils.getAxis2RepositoryPath(tenantId));
            if (synchronizer == null) {
                synchronizer =
                        CarbonRepositoryUtils.newCarbonRepositorySynchronizer(tenantId);

                if(synchronizer == null){
                    return false;
                }
                synchronizer.doInitialSyncUp();
            }
            return synchronizer.commit();
        } catch (DeploymentSynchronizerException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean checkout(String filePath) throws DeploymentSynchronizerException {
        DeploymentSynchronizer synchronizer = getSynchronizer(filePath);
        return synchronizer.checkout();
    }

    @Override
    public boolean checkout(String filePath, int depth) throws DeploymentSynchronizerException {
        DeploymentSynchronizer synchronizer = getSynchronizer(filePath);
        return synchronizer.checkout(filePath, depth);
    }

    @Override
    public boolean update(String rootPath, String filePath, int depth) throws DeploymentSynchronizerException {
        DeploymentSynchronizer synchronizer = getSynchronizer(rootPath);
        return synchronizer.update(rootPath, filePath, depth);
    }

    public boolean commit(String filePath) throws DeploymentSynchronizerException {
        DeploymentSynchronizer synchronizer = getSynchronizer(filePath);
        return synchronizer.commit();
    }

    @Override
    public boolean commit(String rootPath, String filePath) throws DeploymentSynchronizerException {
        DeploymentSynchronizer synchronizer = getSynchronizer(rootPath);
        return synchronizer.commit(filePath);
    }

    private DeploymentSynchronizer getSynchronizer(String filePath)
            throws DeploymentSynchronizerException {

        DeploymentSynchronizer synchronizer = syncManager.getSynchronizer(filePath);
        if (synchronizer == null) {
            throw new DeploymentSynchronizerException("A repository synchronizer has not been " +
                                                      "engaged for the file path: " + filePath);
        }
        return synchronizer;
    }
}