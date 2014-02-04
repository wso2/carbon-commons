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
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.deployment.synchronizer.DeploymentSynchronizerException;
import org.wso2.carbon.deployment.synchronizer.RepositoryCreator;
import org.wso2.carbon.deployment.synchronizer.RepositoryInformation;
import org.wso2.carbon.deployment.synchronizer.RepositoryManager;
import org.wso2.carbon.deployment.synchronizer.git.internal.GitDeploymentSynchronizerConstants;

public class DefaultGitRepositoryManager extends RepositoryManager {

    private static final Log log = LogFactory.getLog(DefaultGitRepositoryManager.class);
    protected String gitServerUrl;
    protected String gitServerAdminUserName;
    protected String gitServerAdminPassword;

    public DefaultGitRepositoryManager() {
        readDefaultGitRepositoryConfiguration();
    }

    public DefaultGitRepositoryManager(RepositoryCreator repositoryCreator) {
        super(repositoryCreator);
        readDefaultGitRepositoryConfiguration();
    }

    private void readDefaultGitRepositoryConfiguration () throws DeploymentSynchronizerException {

        String gitRepositoryUrlParam = readConfigurationParameter(GitDeploymentSynchronizerConstants.GIT_REPO_BASE_URL);
        if (gitRepositoryUrlParam == null) {
            handleError("Required parameter " + GitDeploymentSynchronizerConstants.GIT_REPO_BASE_URL + " not found");
        }
        gitServerUrl = gitRepositoryUrlParam;

        String gitRepositoryUsernameParam = readConfigurationParameter(GitDeploymentSynchronizerConstants.GIT_USERNAME);
        if (gitRepositoryUsernameParam != null) {
            gitServerAdminUserName = gitRepositoryUsernameParam;
        }

        String gitRepositoryPasswordParam = readConfigurationParameter(GitDeploymentSynchronizerConstants.GIT_PASSWORD);
        if (gitRepositoryPasswordParam != null) {
            gitServerAdminPassword = gitRepositoryPasswordParam;
        }
    }

    private String readConfigurationParameter(String parameterKey) {
        return ServerConfiguration.getInstance().getFirstProperty(parameterKey);
    }

    @Override
    public RepositoryInformation getUrlInformation(int tenantId) throws DeploymentSynchronizerException {

        TenantGitRepositoryContext tenantGitRepoCtx = TenantGitRepositoryContextCache.
                getTenantRepositoryContextCache().
                retrieveCachedTenantGitContext(tenantId);

        return new GitRepositoryInformation(tenantGitRepoCtx.getRemoteRepoUrl());
    }

    @Override
    public RepositoryInformation getCredentialsInformation(int tenantId) throws DeploymentSynchronizerException {
        return new GitRepositoryInformation(gitServerAdminUserName, gitServerAdminPassword);
    }

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

        String repositoryUrl = repositoryCreator.createRepository(tenantId, gitServerUrl, gitServerAdminUserName,
                gitServerAdminPassword).getUrl();

        tenantGitRepoCtx.setRemoteRepoUrl(repositoryUrl);
        TenantGitRepositoryContextCache.getTenantRepositoryContextCache().
                updateTenantGitRepositoryContext(tenantId, tenantGitRepoCtx);
    }

    @Override
    public void addRepository(int tenantId, String url) throws DeploymentSynchronizerException {

    }

    private void handleError (String errorMsg) throws DeploymentSynchronizerException {
        log.error(errorMsg);
        throw new DeploymentSynchronizerException(errorMsg);
    }

    private void handleError (String errorMsg, Exception e) throws DeploymentSynchronizerException {
        log.error(errorMsg, e);
        throw new DeploymentSynchronizerException(errorMsg, e);
    }
}
