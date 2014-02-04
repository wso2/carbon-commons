/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.deployment.synchronizer.git;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.deployment.synchronizer.DeploymentSynchronizerException;

/**
 * Use a single repository - should be used for non-multitenant mode deployments
 */
public class SingleTenantGitRepositoryManager extends DefaultGitRepositoryManager {

    private static final Log log = LogFactory.getLog(DefaultGitRepositoryManager.class);

    @Override
    public void provisionRepository(int tenantId) throws DeploymentSynchronizerException {

        TenantGitRepositoryContext tenantGitRepoCtx = TenantGitRepositoryContextCache.
                getTenantRepositoryContextCache().
                retrieveCachedTenantGitContext(tenantId);

        if(tenantGitRepoCtx == null) {
            String errorMsg = "TenantGitRepositoryContext not available for tenant " + tenantId;
            log.error(errorMsg);
            handleError(errorMsg);
        }

        tenantGitRepoCtx.setRemoteRepoUrl(gitServerUrl);
        TenantGitRepositoryContextCache.getTenantRepositoryContextCache().
                updateTenantGitRepositoryContext(tenantId, tenantGitRepoCtx);
    }

    private void handleError (String errorMsg) throws DeploymentSynchronizerException {
        log.error(errorMsg);
        throw new DeploymentSynchronizerException(errorMsg);
    }
}
