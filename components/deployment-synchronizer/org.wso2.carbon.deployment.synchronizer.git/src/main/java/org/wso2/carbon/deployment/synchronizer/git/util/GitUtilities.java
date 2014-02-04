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

package org.wso2.carbon.deployment.synchronizer.git.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.wso2.carbon.deployment.synchronizer.DeploymentSynchronizerException;
import org.wso2.carbon.deployment.synchronizer.RepositoryInformation;
import org.wso2.carbon.deployment.synchronizer.RepositoryManager;
import org.wso2.carbon.deployment.synchronizer.git.internal.GitDeploymentSynchronizerConstants;

import java.io.File;
import java.io.IOException;

/**
 * Utility methods specific for Git
 */

public class GitUtilities {

    private static final Log log = LogFactory.getLog(GitUtilities.class);

    /**
     * Checks if an existing local repository is a valid git repository
     *
     * @param repository Repository instance
     * @return true if a valid git repo, else false
     */
    public static boolean isValidGitRepo (Repository repository) {

        for (Ref ref : repository.getAllRefs().values()) { //check if has been previously cloned successfully, not empty
            if (ref.getObjectId() == null)
                continue;
            return true;
        }

        return false;
    }

    /**
     * Creates and return a UsernamePasswordCredentialsProvider instance for a tenant
     *
     * @param tenantId tenant Id
     * @param repositoryManager RepositoryManager instance
     * @return UsernamePasswordCredentialsProvider instance or null if username/password is not valid
     */
    public static UsernamePasswordCredentialsProvider createCredentialsProvider (RepositoryManager repositoryManager,
                                                                                 int tenantId) {

        RepositoryInformation repoInfo = repositoryManager.getCredentialsInformation(tenantId);
        if(repoInfo == null) {
            return null;
        }

        String userName = repoInfo.getUserName();
        String password = repoInfo.getPassword();

        if (userName!= null && password != null) {
            return new UsernamePasswordCredentialsProvider(userName, password);

        } else {
            return new UsernamePasswordCredentialsProvider("", "");
        }

    }

    /**
     * Initialize local git repository
     *
     * @param gitRepoDir directory in the local file system
     */
    public static void InitGitRepository (File gitRepoDir) {

        try {
            Git.init().setDirectory(gitRepoDir).setBare(false).call();

        } catch (GitAPIException e) {
            String errorMsg = "Initializing local repo at " + gitRepoDir.getPath() + " failed";
            handleError(errorMsg, e);
        }
    }

    /**
     * Adds the remote repository at remoteUrl to the given local repository
     *
     * @param repository Repository instance representing local repo
     * @param remoteUrl remote repository url
     * @return true if remote successfully added, else false
     */
    public static boolean addRemote (Repository repository, String remoteUrl) {

        boolean remoteAdded = false;

        StoredConfig config = repository.getConfig();
        config.setString(GitDeploymentSynchronizerConstants.REMOTE,
                GitDeploymentSynchronizerConstants.ORIGIN,
                GitDeploymentSynchronizerConstants.URL,
                remoteUrl);

        config.setString(GitDeploymentSynchronizerConstants.REMOTE,
                GitDeploymentSynchronizerConstants.ORIGIN,
                GitDeploymentSynchronizerConstants.FETCH,
                GitDeploymentSynchronizerConstants.FETCH_LOCATION);

        config.setString(GitDeploymentSynchronizerConstants.BRANCH,
                GitDeploymentSynchronizerConstants.MASTER,
                GitDeploymentSynchronizerConstants.REMOTE,
                GitDeploymentSynchronizerConstants.ORIGIN);

        config.setString(GitDeploymentSynchronizerConstants.BRANCH,
                GitDeploymentSynchronizerConstants.MASTER,
                GitDeploymentSynchronizerConstants.MERGE,
                GitDeploymentSynchronizerConstants.GIT_REFS_HEADS_MASTER);

        try {
            config.save();
            remoteAdded = true;

        } catch (IOException e) {
            log.error("Error in adding remote origin " + remoteUrl + " for local repository " +
                    repository.toString(), e);
        }

        return remoteAdded;
    }

    private static void handleError (String errorMsg, Exception e) throws DeploymentSynchronizerException {
        log.error(errorMsg, e);
        throw new DeploymentSynchronizerException(errorMsg, e);
    }

}
