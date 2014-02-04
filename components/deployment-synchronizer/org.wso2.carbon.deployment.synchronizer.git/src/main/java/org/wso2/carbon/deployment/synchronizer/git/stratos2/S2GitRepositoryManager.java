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

package org.wso2.carbon.deployment.synchronizer.git.stratos2;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.adc.mgt.dao.xsd.RepositoryCredentials;
import org.wso2.carbon.adc.repository.information.RepositoryInformationServiceException;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.deployment.synchronizer.DeploymentSynchronizerException;
import org.wso2.carbon.deployment.synchronizer.RepositoryCreator;
import org.wso2.carbon.deployment.synchronizer.RepositoryInformation;
import org.wso2.carbon.deployment.synchronizer.RepositoryManager;
import org.wso2.carbon.deployment.synchronizer.git.GitRepositoryInformation;
import org.wso2.carbon.deployment.synchronizer.git.TenantGitRepositoryContext;
import org.wso2.carbon.deployment.synchronizer.git.TenantGitRepositoryContextCache;
import org.wso2.carbon.deployment.synchronizer.git.internal.GitDeploymentSynchronizerConstants;
import org.wso2.carbon.deployment.synchronizer.git.internal.RepositoryInformationServiceClient;

import java.rmi.RemoteException;

public class S2GitRepositoryManager extends RepositoryManager{

    private static final Log log = LogFactory.getLog(S2GitRepositoryManager.class);
    private String cartridgeAlias;
    private String repositoryInformationServiceEpr;
    private RepositoryInformationServiceClient repoInfoServiceClient;

    public S2GitRepositoryManager () {
        readS2GitRepositoryConfiguration();
        createRepositoryInformationServiceClient();
    }

    public S2GitRepositoryManager (RepositoryCreator repositoryCreator) {
        super(repositoryCreator);
        readS2GitRepositoryConfiguration();
        createRepositoryInformationServiceClient();
    }

    private void createRepositoryInformationServiceClient () throws DeploymentSynchronizerException {
        try {
            repoInfoServiceClient = new RepositoryInformationServiceClient(repositoryInformationServiceEpr);

        } catch (AxisFault axisFault) {
            handleError("Repository Information Service client initialization failed", axisFault);
        }
    }

    private void readS2GitRepositoryConfiguration() throws DeploymentSynchronizerException {

        cartridgeAlias = readConfigurationParameter(GitDeploymentSynchronizerConstants.CARTRIDGE_ALIAS);
        if (cartridgeAlias == null) {
            handleError("Required parameter " + GitDeploymentSynchronizerConstants.CARTRIDGE_ALIAS + " not found");
        }

        repositoryInformationServiceEpr = readConfigurationParameter(GitDeploymentSynchronizerConstants.REPO_INFO_SERVICE_EPR);
        if (repositoryInformationServiceEpr == null) {
            handleError("Required parameter " + GitDeploymentSynchronizerConstants.REPO_INFO_SERVICE_EPR + " not found");
        }
    }

    private String readConfigurationParameter(String parameterKey) {
        return ServerConfiguration.getInstance().getFirstProperty(parameterKey);
    }

    @Override
    public RepositoryInformation getUrlInformation(int tenantId) throws DeploymentSynchronizerException {

        //check in the local cache
        TenantGitRepositoryContext tenantGitRepoCtx = TenantGitRepositoryContextCache.
                getTenantRepositoryContextCache().
                retrieveCachedTenantGitContext(tenantId);

        if(tenantGitRepoCtx == null) {
            String errorMsg = "TenantGitRepositoryContext not available for tenant " + tenantId;
            log.error(errorMsg);
            handleError(errorMsg);
        }

        if(tenantGitRepoCtx.getRemoteRepoUrl() != null) { //available in local cache
            return new GitRepositoryInformation(tenantGitRepoCtx.getRemoteRepoUrl());
        }

        //not available in the local cache
        String repositoryUrl = null;

        try {
            repositoryUrl = repoInfoServiceClient.getGitRepositoryUrl(tenantId, cartridgeAlias);

        } catch (RemoteException e) {
            log.error(e.getMessage());
            repositoryUrl = null;

        } catch (RepositoryInformationServiceException e) {
            log.error(e.getMessage());
            repositoryUrl = null;
        }

        log.info("Recieved repo url [" + repositoryUrl + "] for tenant " + tenantId );

        //cache the url
        tenantGitRepoCtx.setRemoteRepoUrl(repositoryUrl);
        TenantGitRepositoryContextCache.getTenantRepositoryContextCache().
                updateTenantGitRepositoryContext(tenantId, tenantGitRepoCtx);

        return new GitRepositoryInformation(repositoryUrl);
    }

    @Override
    public RepositoryInformation getCredentialsInformation(int tenantId) throws DeploymentSynchronizerException {

        //check in the local cache
        TenantGitRepositoryContext tenantGitRepoCtx = TenantGitRepositoryContextCache.
                getTenantRepositoryContextCache().
                retrieveCachedTenantGitContext(tenantId);

        if(tenantGitRepoCtx == null) {
            String errorMsg = "TenantGitRepositoryContext not available for tenant " + tenantId;
            log.error(errorMsg);
            handleError(errorMsg);
        }

        if(tenantGitRepoCtx.getUsername() != null &&
                tenantGitRepoCtx.getPassword() != null) { //available in local cache
            return new GitRepositoryInformation(tenantGitRepoCtx.getUsername(),
                    tenantGitRepoCtx.getPassword());
        }

        //not available in the local cache
        RepositoryCredentials repoCredentials = null;

        try {
            repoCredentials = repoInfoServiceClient.getJsonRepositoryInformation(tenantId, cartridgeAlias);

        } catch (RemoteException e) {
            log.error(e.getMessage());
            repoCredentials = null;

        } catch (RepositoryInformationServiceException e) {
            log.error(e.getMessage());
            repoCredentials = null;
        }

        String userName;
        String password;
        if(repoCredentials == null) {
            return null;

        } else {
            userName = repoCredentials.getUserName();
            password = repoCredentials.getPassword();
        }

        log.info("Received repository user name [" + userName + "] for tenant " + tenantId);

        //cache username and password
        tenantGitRepoCtx.setUsername(userName);
        tenantGitRepoCtx.setPassword(password);
        TenantGitRepositoryContextCache.getTenantRepositoryContextCache().
                updateTenantGitRepositoryContext(tenantId, tenantGitRepoCtx);

        return new GitRepositoryInformation(userName, password);
    }

    @Override
    public void provisionRepository(int tenantId) throws DeploymentSynchronizerException {
        //no implementation
    }

    @Override
    public void addRepository(int tenantId, String url) throws DeploymentSynchronizerException {
        //no implementation
    }

    private void handleWarning (String warnMsg) {
        log.warn(warnMsg);
        throw new DeploymentSynchronizerException(warnMsg);
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
