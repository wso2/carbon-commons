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

package org.wso2.carbon.deployment.synchronizer.git.repository_creator;

import com.gitblit.Constants;
import com.gitblit.models.RegistrantAccessPermission;
import com.gitblit.models.RepositoryModel;
import com.gitblit.models.UserModel;
import com.gitblit.utils.RpcUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.deployment.synchronizer.DeploymentSynchronizerException;
import org.wso2.carbon.deployment.synchronizer.RepositoryCreator;
import org.wso2.carbon.deployment.synchronizer.RepositoryInformation;
import org.wso2.carbon.deployment.synchronizer.git.GitRepositoryInformation;
import org.wso2.carbon.deployment.synchronizer.git.internal.GitDeploymentSynchronizerConstants;
import org.wso2.carbon.deployment.synchronizer.git.util.CarbonUtilities;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * GitBlit based repository creator implementation
 */

public class GitBlitBasedRepositoryCreator implements RepositoryCreator{

    private static final Log log = LogFactory.getLog(GitBlitBasedRepositoryCreator.class);

    public RepositoryInformation createRepository(int tenantId, String baseUrl, String username, String password)
            throws DeploymentSynchronizerException {

        RepositoryInformation repoInfo;
        baseUrl = (baseUrl.endsWith("/")) ? baseUrl : baseUrl + "/";

        String serverKeyParam = CarbonUtilities.
                readConfigurationParameter(GitDeploymentSynchronizerConstants.SERVER_KEY);

        String repositoryName =  (serverKeyParam != null) ?
                serverKeyParam.toLowerCase() + "/tenant_" + Integer.toString(tenantId) + ".git" :
                "tenant_" + Integer.toString(tenantId) + ".git";

        String repoUrl = baseUrl + "git/" + repositoryName;

        UserModel userModel = getUserModel(baseUrl, username, username, password);

        if(userModel != null) {

            if(log.isDebugEnabled()) {
                log.debug("User already exists for tenant " + tenantId + ", url: " + repoUrl);
            }

        } else {
            //user is the same as admin user
            userModel = createUserModel(tenantId, baseUrl, username, password, username, password);
        }

        RepositoryModel repositoryModel = getRepositoryModel(baseUrl, repositoryName, username, password);

        if(repositoryModel != null) {

            if(log.isDebugEnabled()) {
                log.debug("Repository already exists for tenant " + tenantId + ", url: " + repoUrl);
            }

        } else {
            repositoryModel = createRepositoryModel(baseUrl, repositoryName, username, repoUrl, tenantId,
                    username, password);
        }
        repoInfo = new GitRepositoryInformation(repoUrl);

        setUserPermissions(baseUrl, repositoryModel, userModel, username, password);

        return repoInfo;
    }

    /**
     * Creates a user model for a git repository on the GitBlit server
     *
     * @param tenantId tenant Id
     * @param baseUrl GitBlit server url
     * @param username username of the user to be added
     * @param password password of the user to be added
     * @param adminUserName admin username of the server
     * @param adminPassword admin password of the server
     * @return created UserModel instance
     */
    private UserModel createUserModel (int tenantId, String baseUrl, String username, String password,
                                       String adminUserName, String adminPassword) {

        UserModel userModel = new UserModel(username);
        userModel.canAdmin = true;
        userModel.password = password;

        boolean isUserCreated = false;
        try {
            isUserCreated = RpcUtils.createUser(userModel, baseUrl, adminUserName, adminPassword.toCharArray());

        } catch (IOException e) {
            handleError("User creation failed for tenant " + tenantId, e);
        }

        if(isUserCreated) {
            log.info("User created successfully for tenant " + tenantId);
        }
        else {
            handleError("User creation failed for tenant " + tenantId + ", server url: " +
                baseUrl);
        }

        return userModel;
    }

    /**
     * creates a RepositoryModel in the given server at baseUrl, with repositoryName and userName
     *
     * @param baseUrl GitBlit server url
     * @param repositoryName repository name
     * @param userName user name of the owner of the repository
     * @param repoUrl full repository url
     * @param tenantId tenant Id
     * @param adminUserName admin user name of the server
     * @param adminPassword admin password of the server
     * @return RepositoryModel instance if successfully created, else null
     */
    private RepositoryModel createRepositoryModel (String baseUrl, String repositoryName, String userName,
                                                   String repoUrl, int tenantId, String adminUserName,
                                                   String adminPassword) {

        RepositoryModel repositoryModel = new RepositoryModel();
        repositoryModel.name = repositoryName;
        repositoryModel.owner = userName;
        //authenticated users can clone, push and view the repository
        repositoryModel.accessRestriction = Constants.AccessRestrictionType.VIEW;
        boolean isRepoCreated;

        try {
            isRepoCreated = RpcUtils.createRepository(repositoryModel, baseUrl, adminUserName, adminPassword.toCharArray());

        } catch (IOException e) {
            handleError("Repository creation failed for tenant " + tenantId, e);
            return null;
        }

        if (isRepoCreated) {
            log.info("Repository created successfully for tenant " + tenantId + ", url: " + repoUrl);
        }
        else {
            handleError("Repository creation failed for tenant " + tenantId);
            return  null;
        }

        return repositoryModel;
    }

    /**
     * Checks and returns the corresponding UserModel instance for the given username in the repository at baseUrl
     *
     * @param baseUrl GitBlit server url
     * @param userName username of the user
     * @param adminUserName admin username of the server
     * @param adminPassword admin password of the server
     * @return instance of UserModel if exists, else null
     */
    private UserModel getUserModel(String baseUrl, String userName, String adminUserName, String adminPassword) {

        UserModel userModel =  null;
        List<UserModel> users;

        try {
            users = RpcUtils.getUsers(baseUrl, adminUserName, adminPassword.toCharArray());

        } catch (IOException e) {
            log.error("Error retrieving user details from git server " + baseUrl, e);
            return  null;
        }

        for (UserModel model : users) {
            if (model.username.equals(userName)) {
                userModel = model;
                break;
            }
        }
        return userModel;
    }

    /**
     * Checks and returns the corresponding RepositoryModel instance for the given username in the repository at baseUrl
     *
     * @param baseUrl Gitblit server url
     * @param repositoryName repository name
     * @param adminUserName admin user name of the server
     * @param adminPassword admin password of the server
     * @return RepositoryModel instance if exists, else null
     */
    private RepositoryModel getRepositoryModel(String baseUrl, String repositoryName, String adminUserName,
                                               String adminPassword) {

        RepositoryModel repositoryModel = null;
        Map<String, RepositoryModel> repositories;

        try {
            repositories = RpcUtils.getRepositories(baseUrl, adminUserName, adminPassword.toCharArray());

        } catch (IOException e) {
            log.error("Error retrieving repository details from git server " + baseUrl, e);
            return null;
        }

        for (RepositoryModel model : repositories.values()) {
            if (model.name.equals(repositoryName)) {
                repositoryModel = model;
                break;
            }
        }
        return repositoryModel;
    }

    /**
     * Sets the permissions for the user described by UserModel to the repository describes by RepositoryModel
     *
     * @param baseUrl GitBlit server url
     * @param repoModel RepositoryModel instance
     * @param userModel UserModel instance
     * @param adminUserName admin user name of the server
     * @param adminPassword admin password of the server
     */
    private void setUserPermissions(String baseUrl, RepositoryModel repoModel, UserModel userModel,
                                    String adminUserName, String adminPassword) {

        List<RegistrantAccessPermission> permissions;

        try {
            permissions = RpcUtils.
                    getRepositoryMemberPermissions(repoModel, baseUrl, adminUserName, adminPassword.toCharArray());

        } catch (IOException e) {
            log.error("Retrieving permissions failed for repository " + repoModel.name, e);
            return;
        }

        for (RegistrantAccessPermission permission : permissions) {

            if(permission.registrant.equals(userModel.username)) {
                if(log.isDebugEnabled()) {
                    log.debug("Permission already set for user " + userModel.username + " for repository " +
                            repoModel.name);
                }
                return;
            }
        }

        //permission for the user not found, therefore add them
        permissions.add(new RegistrantAccessPermission(userModel.username, Constants.AccessPermission.PUSH,
                Constants.PermissionType.EXPLICIT, Constants.RegistrantType.USER, null, true));
    }

    private void handleError (String errorMsg) {

        log.error(errorMsg);
        throw new DeploymentSynchronizerException(errorMsg);
    }

    private void handleError (String errorMsg, Exception e) {

        log.error(errorMsg, e);
        throw new DeploymentSynchronizerException(errorMsg, e);
    }
}
