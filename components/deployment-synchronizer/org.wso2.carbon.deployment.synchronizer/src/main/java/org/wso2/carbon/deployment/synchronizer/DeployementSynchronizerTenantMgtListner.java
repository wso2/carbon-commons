/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.deployment.synchronizer;

import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.wso2.carbon.deployment.synchronizer.repository.CarbonRepositoryUtils;
import org.wso2.carbon.deployment.synchronizer.internal.util.RepositoryReferenceHolder;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.exception.StratosException;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;

//TODO add class level comment
public class DeployementSynchronizerTenantMgtListner implements TenantMgtListener {

    @Override
    public void onTenantCreate(TenantInfoBean tenantInfo) throws StratosException {

    }

    @Override
    public void onTenantUpdate(TenantInfoBean tenantInfo) throws StratosException {

    }

    @Override
    public void onTenantDelete(int tenantId) {

    }

    @Override
    public void onTenantRename(int tenantId, String oldDomainName, String newDomainName)
            throws StratosException {

    }

    @Override
    public void onTenantInitialActivation(int tenantId) throws StratosException {

    }

    @Override
    public void onTenantActivation(int tenantId) throws StratosException {

    }

    @Override
    public void onTenantDeactivation(int tenantId) throws StratosException {

    }

    @Override
    public void onSubscriptionPlanChange(int tenentId, String oldPlan, String newPlan)
            throws StratosException {

    }

    @Override
    public int getListenerOrder() {
        return 0;
    }

    @Override
    public void onPreDelete(int tenantId) throws StratosException {
        try {
            // Checking whether depsync is enabled in the configuration
            if(CarbonRepositoryUtils.getDeploymentSyncConfigurationFromConf().isEnabled()) {
                String repositoryType = CarbonRepositoryUtils.getDeploymentSyncConfigurationFromConf()
                        .getRepositoryType();
                ArtifactRepository artifactRepository = RepositoryReferenceHolder.getInstance()
                        .getRepositoryByType(repositoryType);
                artifactRepository.delete(tenantId);
            }

        } catch (SVNClientException e) {
            throw new StratosException(e.getMessage(), e);
        }
    }
}
