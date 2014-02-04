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

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.wso2.carbon.deployment.synchronizer.TenantRepositoryContext;

public class TenantGitRepositoryContext extends TenantRepositoryContext {

    private String localRepoPath;
    private String remoteRepoUrl;
    private String username;
    private String password;
    private Repository localRepo;
    private Git git;
    private boolean initialArtifactsSynced;
    private boolean cloneExists;
    private int tenantId;

    public TenantGitRepositoryContext () {

        localRepoPath = null;
        remoteRepoUrl = null;
        username = null;
        password = null;
        localRepo = null;
        git = null;
        cloneExists = false;
        initialArtifactsSynced = false;
        tenantId = 0;
    }

    public String getLocalRepoPath() {
        return localRepoPath;
    }

    public void setLocalRepoPath(String localRepoPath) {
        this.localRepoPath = localRepoPath;
    }

    public String getRemoteRepoUrl() {
        return remoteRepoUrl;
    }

    public void setRemoteRepoUrl(String remoteRepoUrl) {
        this.remoteRepoUrl = remoteRepoUrl;
    }

    public Repository getLocalRepo() {
        return localRepo;
    }

    public void setLocalRepo(Repository localRepo) {
        this.localRepo = localRepo;
    }

    public Git getGit() {
        return git;
    }

    public void setGit(Git git) {
        this.git = git;
    }

    public boolean cloneExists() {
        return cloneExists;
    }

    public void setCloneExists(boolean cloneExists) {
        this.cloneExists = cloneExists;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean initialArtifactsSynced() {
        return initialArtifactsSynced;
    }

    public void setInitialArtifactsSynced(boolean initialArtifactsSynced) {
        this.initialArtifactsSynced = initialArtifactsSynced;
    }
}
