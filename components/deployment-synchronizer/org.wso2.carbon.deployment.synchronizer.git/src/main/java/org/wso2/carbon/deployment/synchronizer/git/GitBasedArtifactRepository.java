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
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.wso2.carbon.deployment.synchronizer.ArtifactRepository;
import org.wso2.carbon.deployment.synchronizer.DeploymentSynchronizerException;
import org.wso2.carbon.deployment.synchronizer.RepositoryManager;
import org.wso2.carbon.deployment.synchronizer.git.internal.AbstractBehaviour;
import org.wso2.carbon.deployment.synchronizer.git.internal.GitDeploymentSynchronizerConstants;
import org.wso2.carbon.deployment.synchronizer.git.internal.GitDeploymentSyncronizerConfiguration;
import org.wso2.carbon.deployment.synchronizer.git.repository_creator.GitBlitBasedRepositoryCreator;
import org.wso2.carbon.deployment.synchronizer.git.repository_creator.SCMBasedRepositoryCreator;
import org.wso2.carbon.deployment.synchronizer.git.stratos2.S2Behaviour;
import org.wso2.carbon.deployment.synchronizer.git.stratos2.S2GitRepositoryManager;
import org.wso2.carbon.deployment.synchronizer.git.util.CarbonUtilities;
import org.wso2.carbon.deployment.synchronizer.git.util.FileUtilities;
import org.wso2.carbon.deployment.synchronizer.git.util.GitUtilities;
import org.wso2.carbon.deployment.synchronizer.DeploymentSynchronizerConstants;
import org.wso2.carbon.deployment.synchronizer.util.RepositoryConfigParameter;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Git based artifact repository
 */

public class GitBasedArtifactRepository implements ArtifactRepository {

    private static final Log log = LogFactory.getLog(GitBasedArtifactRepository.class);

    private RepositoryManager repositoryManager;
    private GitDeploymentSyncronizerConfiguration gitDepsyncConfig;
    private AbstractBehaviour behaviour;

    /*
    * Constructor
    * */
    public GitBasedArtifactRepository () {

        //TODO:fix properly
        if(!isGitDeploymentSyncEnabled()) {
            return;
        }

        readConfiguration();

        //standard worker manager separated deployment
        if(gitDepsyncConfig.isStandardDeployment()) {
            //GitBlit git server
            if(gitDepsyncConfig.getGitServer().equals(GitDeploymentSynchronizerConstants.SERVER_GITBLIT)){
                repositoryManager = new DefaultGitRepositoryManager(new GitBlitBasedRepositoryCreator());
            }
            //SCM git server
            else if (gitDepsyncConfig.getGitServer().equals(GitDeploymentSynchronizerConstants.SERVER_SCM)) {
                repositoryManager = new DefaultGitRepositoryManager(new SCMBasedRepositoryCreator());
            }
            //No specific Git server specified - use a single repository
            else if (gitDepsyncConfig.getGitServer().equals(GitDeploymentSynchronizerConstants.SERVER_UNSPECIFIED)) {
                repositoryManager = new SingleTenantGitRepositoryManager();
            }
            behaviour = new DefaultBehaviour();
        }
        //Stratos 2 specific deployment
        else {
            repositoryManager = new S2GitRepositoryManager();
            behaviour = new S2Behaviour();
        }
    }

    private boolean isGitDeploymentSyncEnabled () {

        //check if deployment synchronization is enabled
        String depSyncEnabledParam = CarbonUtilities.readConfigurationParameter(GitDeploymentSynchronizerConstants.ENABLED);

        //Check if deployment synchronization is enabled
        if (depSyncEnabledParam != null && depSyncEnabledParam.equals("true")) {

            //check if repository type is 'git', else no need to create GitBasedArtifactRepository instance
            String repoTypeParam = CarbonUtilities.readConfigurationParameter(GitDeploymentSynchronizerConstants.REPOSITORY_TYPE);
            if (repoTypeParam != null && repoTypeParam.equals(DeploymentSynchronizerConstants.REPOSITORY_TYPE_GIT)) {
                 return true;
            }
        }

        return false;
    }

    /**
     * Reads the configuration
     */
    private void readConfiguration () {

        gitDepsyncConfig = new GitDeploymentSyncronizerConfiguration();

        String standardDeploymentParam = CarbonUtilities.readConfigurationParameter(GitDeploymentSynchronizerConstants.DEPLOYMENT_METHOD);
        if (standardDeploymentParam != null && (standardDeploymentParam.equalsIgnoreCase("true") || standardDeploymentParam.equalsIgnoreCase("false"))) {
            gitDepsyncConfig.setStandardDeployment(Boolean.parseBoolean(standardDeploymentParam));
        }

        String gitServerParam = CarbonUtilities.readConfigurationParameter(GitDeploymentSynchronizerConstants.GIT_SERVER);
        if (gitServerParam != null) {
            gitDepsyncConfig.setGitServer(gitServerParam);
        }
    }

    /**
     * Called at tenant load to do initialization related to the tenant
     *
     * @param tenantId id of the tenant
     * @throws DeploymentSynchronizerException in case of an error
     */
    public void init (int tenantId) throws DeploymentSynchronizerException {

        TenantGitRepositoryContext repoCtx = new TenantGitRepositoryContext();

        String gitLocalRepoPath = MultitenantUtils.getAxis2RepositoryPath(tenantId);
        repoCtx.setTenantId(tenantId);
        repoCtx.setLocalRepoPath(gitLocalRepoPath);

        FileRepository localRepo = null;
        try {
            localRepo = new FileRepository(new File(gitLocalRepoPath + "/.git"));

        } catch (IOException e) {
            handleError("Error creating git local repository for tenant " + tenantId, e);
        }

        repoCtx.setLocalRepo(localRepo);
        repoCtx.setGit(new Git(localRepo));
        repoCtx.setCloneExists(false);

        TenantGitRepositoryContextCache.getTenantRepositoryContextCache().cacheTenantGitRepoContext(tenantId, repoCtx);

        //provision a repository
        repositoryManager.provisionRepository(tenantId);
        //repositoryManager.addRepository(tenantId, url);

        repositoryManager.getUrlInformation(tenantId);
        repositoryManager.getCredentialsInformation(tenantId);
    }

    /**
     * Commits any changes in the local repository to the relevant remote repository
     *
     * @param localRepoPath tenant's local repository path
     * @return true if commit is successful, else false
     * @throws DeploymentSynchronizerException in case of an error
     */
    public boolean commit(int tenantId, String localRepoPath) throws DeploymentSynchronizerException {

        String gitRepoUrl = repositoryManager.getUrlInformation(tenantId).getUrl();
        if(gitRepoUrl == null) { //url not available
            log.warn ("Remote repository URL not available for tenant " + tenantId + ", aborting commit");
            return false;
        }

        TenantGitRepositoryContext gitRepoCtx = TenantGitRepositoryContextCache.getTenantRepositoryContextCache().
                retrieveCachedTenantGitContext(tenantId);


        Status status = getGitStatus(gitRepoCtx);
        if (status == null) {
            return false;
        }

        if(status.isClean()) {//no changes, nothing to commit
            if(log.isDebugEnabled())
                log.debug("No changes detected in the local repository at " + localRepoPath);
            return false;
        }

        if(!addArtifacts(gitRepoCtx, getNewArtifacts(status)) &&
                !addArtifacts(gitRepoCtx, getModifiedArtifacts(status)) &&
                !removeArtifacts(gitRepoCtx, getRemovedArtifacts(status))) { //no changes!

            return false;
        }

        commitToLocalRepo(gitRepoCtx);
        pushToRemoteRepo(gitRepoCtx);

        return behaviour.requireSynchronizeRepositoryRequest();
    }


    /**
     * Quesries the git status for the repository given by gitRepoCtx
     *
     * @param gitRepoCtx TenantGitRepositoryContext instance for the tenant
     * @return Status instance updated with relevant status information,
     *         null in an error in getting the status
     */
    private Status getGitStatus (TenantGitRepositoryContext gitRepoCtx) {

        Git git = gitRepoCtx.getGit();
        StatusCommand statusCmd = git.status();
        Status status;

        try {
            status = statusCmd.call();

        } catch (GitAPIException e) {
            log.error("Git status operation for tenant " + gitRepoCtx.getTenantId() + " failed, ", e);
            status = null;
        }

        return status;
    }

    /**
     * Returns the newly added artifact set relevant to the current status of the repository
     *
     * @param status git status
     * @return artifact names set
     */
    private Set<String> getNewArtifacts (Status status) {

        return status.getUntracked();
    }

    /**
     * Returns the removed (undeployed) artifact set relevant to the current status of the repository
     *
     * @param status git status
     * @return artifact names set
     */
    private Set<String> getRemovedArtifacts (Status status) {

        return status.getMissing();
    }

    /**
     * Return the modified artifacts set relevant to the current status of the repository
     *
     * @param status git status
     * @return artifact names set
     */
    private Set<String> getModifiedArtifacts (Status status) {

        return status.getModified();
    }

    /**
     * Adds the artifacts to the local staging area
     *
     * @param gitRepoCtx TenantGitRepositoryContext instance
     * @param artifacts set of artifacts
     * @return true if artifacts were added
     */
    private boolean addArtifacts (TenantGitRepositoryContext gitRepoCtx, Set<String> artifacts) {

        if(artifacts.isEmpty()) {
            return false;
        }

        boolean artifactsAdded;
        AddCommand addCmd = gitRepoCtx.getGit().add();
        for (String artifact : artifacts) {
            addCmd.addFilepattern(artifact);
        }

        try {
            addCmd.call();
            artifactsAdded = true;

        } catch (GitAPIException e) {
            log.error("Adding artifact to the repository at " + gitRepoCtx.getLocalRepoPath() + "failed", e);
            artifactsAdded = false;
        }

        return artifactsAdded;
    }

    /**
     * Removes the set of artifacts from local repo
     *
     * @param gitRepoCtx TenantGitRepositoryContext instance
     * @param artifacts Set of artifact names to remove
     * @return true if artifacts were removed
     */
    private boolean removeArtifacts (TenantGitRepositoryContext gitRepoCtx, Set<String> artifacts) {

        if(artifacts.isEmpty()) {
            return false;
        }

        boolean artifactsRemoved;
        RmCommand rmCmd = gitRepoCtx.getGit().rm();
        for (String artifact : artifacts) {
            rmCmd.addFilepattern(artifact);
        }

        try {
            rmCmd.call();
            artifactsRemoved = true;

        } catch (GitAPIException e) {
            log.error("Removing artifact from the repository at " + gitRepoCtx.getLocalRepoPath() + "failed", e);
            artifactsRemoved = false;
        }

        return artifactsRemoved;
    }

    /**
     * Commits changes for a tenant to relevant the local repository
     *
     * @param gitRepoCtx TenantGitRepositoryContext instance for the tenant
     */
    private void commitToLocalRepo (TenantGitRepositoryContext gitRepoCtx) {

        CommitCommand commitCmd = gitRepoCtx.getGit().commit();
        commitCmd.setMessage("tenant " + gitRepoCtx.getTenantId() + "'s artifacts committed to repository at " +
                gitRepoCtx.getLocalRepoPath() + ", time stamp: " + System.currentTimeMillis());

        try {
            commitCmd.call();

        } catch (GitAPIException e) {
            log.error("Committing artifacts to repository failed for tenant " + gitRepoCtx.getTenantId(), e);
        }
    }

    /**
     * Pushes the artifacts of the tenant to relevant remote repository
     *
     * @param gitRepoCtx TenantGitRepositoryContext instance for the tenant
     */
    private void pushToRemoteRepo(TenantGitRepositoryContext gitRepoCtx) {

        PushCommand pushCmd = gitRepoCtx.getGit().push();

        UsernamePasswordCredentialsProvider credentialsProvider = GitUtilities.createCredentialsProvider(repositoryManager,
                gitRepoCtx.getTenantId());

        if (credentialsProvider == null) {
            log.warn ("Remote repository credentials not available for tenant " + gitRepoCtx.getTenantId() +
                    ", aborting push");
            return;
        }
        pushCmd.setCredentialsProvider(credentialsProvider);

        try {
            pushCmd.call();

        } catch (GitAPIException e) {
            log.error("Pushing artifacts to remote repository failed for tenant " + gitRepoCtx.getTenantId(), e);
        }
    }

    /**
     * Method inherited from ArtifactRepository for initializing checkout
     *
     * @param localRepoPath local repository path of the tenant
     * @return true if success, else false
     * @throws DeploymentSynchronizerException if an error occurs
     */
    public boolean checkout (int tenantId, String localRepoPath) throws DeploymentSynchronizerException {

        String gitRepoUrl = repositoryManager.getUrlInformation(tenantId).getUrl();
        if(gitRepoUrl == null) { //url not available
            log.warn ("Remote repository URL not available for tenant " + tenantId + ", aborting checkout");
            return false;
        }

        TenantGitRepositoryContext gitRepoCtx = TenantGitRepositoryContextCache.getTenantRepositoryContextCache().
                retrieveCachedTenantGitContext(tenantId);

        File gitRepoDir = new File(gitRepoCtx.getLocalRepoPath());
        if (!gitRepoDir.exists()) {
             return cloneRepository(gitRepoCtx);
        }
        else {
            if (GitUtilities.isValidGitRepo(gitRepoCtx.getLocalRepo())) {
                log.info("Existing git repository detected for tenant " + gitRepoCtx.getTenantId() +
                        ", no clone required");
                try {
                    return pullArtifacts(gitRepoCtx);

                } catch (CheckoutConflictException e) {
                    //conflict(s) detected, try to checkout from local index
                    if(checkoutFromLocalIndex(gitRepoCtx, e.getConflictingPaths())) {
                        try {
                            //now pull the changes from remote repository
                            return pullArtifacts(gitRepoCtx);

                        } catch (CheckoutConflictException e1) {
                            //cannot happen here
                            log.error("Git pull for the path " + e1.getConflictingPaths().toString() +
                                    " failed due to conflicts", e1);
                        }
                    }
                    return false;
                }
            }
            else {
                if (behaviour.requireInitialLocalArtifactSync()) {
                    return syncInitialLocalArtifacts(gitRepoCtx);
                }
                else {
                    if(log.isDebugEnabled()) {
                        log.debug("Repository for tenant " + gitRepoCtx.getTenantId() + " is not a valid git repo, will try to delete");
                    }
                    FileUtilities.deleteFolderStructure(gitRepoDir);
                    return cloneRepository(gitRepoCtx);
                }
            }
        }

        /*if(behaviour.requireInitialLocalArtifactSync() && !gitRepoCtx.initialArtifactsSynced()) {
            return syncInitialLocalArtifacts(gitRepoCtx);
        }
        else if(!gitRepoCtx.cloneExists()) {
            return cloneRepository(gitRepoCtx);
        }
        else {
            return pullArtifacts(gitRepoCtx);
        }*/
    }

    /**
     * Sync any local artifact that are initially available with a remote repository
     *
     * @param gitRepoCtx TenantGitRepositoryContext instance
     * @return true if sync is success, else false
     */
    private boolean syncInitialLocalArtifacts(TenantGitRepositoryContext gitRepoCtx) {

        boolean syncedLocalArtifacts = false;

        Status status = getGitStatus(gitRepoCtx);
        if(status != null && !status.isClean()) {
            //initialize repository
            GitUtilities.InitGitRepository(new File(gitRepoCtx.getLocalRepoPath()));
            //add the remote repository (origin)
            syncedLocalArtifacts = GitUtilities.addRemote(gitRepoCtx.getLocalRepo(),
                    gitRepoCtx.getRemoteRepoUrl());
        }

        return syncedLocalArtifacts;
    }

    /**
     * Clones the remote repository to the local repository path
     *
     * @param gitRepoCtx TenantGitRepositoryContext for the tenant
     * @return true if clone is success, else false
     */
    private boolean cloneRepository (TenantGitRepositoryContext gitRepoCtx) { //should happen only at the beginning

        boolean cloneSuccess = false;

        File gitRepoDir = new File(gitRepoCtx.getLocalRepoPath());
        /*if (gitRepoDir.exists()) {
            if(GitUtilities.isValidGitRepo(gitRepoCtx.getLocalRepo())) { //check if a this is a valid git repo
                log.info("Existing git repository detected for tenant " + gitRepoCtx.getTenantId() +
                        ", no clone required");
                gitRepoCtx.setCloneExists(true);
                return true;
            }
            else {
                if(log.isDebugEnabled()) {
                    log.debug("Repository for tenant " + gitRepoCtx.getTenantId() + " is not a valid git repo, will try to delete");
                }
                FileUtilities.deleteFolderStructure(gitRepoDir); //if not a valid git repo but non-empty, delete it (else the clone will not work)
            }
        }*/

        CloneCommand cloneCmd =  Git.cloneRepository().
                setURI(gitRepoCtx.getRemoteRepoUrl()).
                setDirectory(gitRepoDir).
                setBranch(GitDeploymentSynchronizerConstants.GIT_REFS_HEADS_MASTER);

        UsernamePasswordCredentialsProvider credentialsProvider = GitUtilities.createCredentialsProvider(repositoryManager,
                gitRepoCtx.getTenantId());

        if (credentialsProvider == null) {
            log.warn ("Remote repository credentials not available for tenant " + gitRepoCtx.getTenantId() +
                    ", aborting clone");
            return false;
        }
        cloneCmd.setCredentialsProvider(credentialsProvider);

        try {
            cloneCmd.call();
            log.info("Git clone operation for tenant " + gitRepoCtx.getTenantId() + " successful");
            gitRepoCtx.setCloneExists(true);
            cloneSuccess = true;

        } catch (TransportException e) {
            log.error("Accessing remote git repository failed for tenant " + gitRepoCtx.getTenantId(), e);

        } catch (GitAPIException e) {
            log.error("Git clone operation for tenant " + gitRepoCtx.getTenantId() + " failed", e);
        }

        return cloneSuccess;
    }

    /**
     * Pulling if any updates are available in the remote git repository. If basic authentication is required,
     * will call 'RepositoryInformationService' for credentials.
     *
     * @param gitRepoCtx TenantGitRepositoryContext instance for tenant
     * @return true if success, else false
     */
    private boolean pullArtifacts (TenantGitRepositoryContext gitRepoCtx) throws CheckoutConflictException {

        PullCommand pullCmd = gitRepoCtx.getGit().pull();

        UsernamePasswordCredentialsProvider credentialsProvider = GitUtilities.createCredentialsProvider(repositoryManager,
                gitRepoCtx.getTenantId());

        if (credentialsProvider == null) {
            log.warn ("Remote repository credentials not available for tenant " + gitRepoCtx.getTenantId() +
                    ", aborting pull");
            return false;
        }
        pullCmd.setCredentialsProvider(credentialsProvider);

        try {
            pullCmd.call();

        } catch (InvalidConfigurationException e) {
            log.warn("Git pull unsuccessful for tenant " + gitRepoCtx.getTenantId() + ", " + e.getMessage());
            FileUtilities.deleteFolderStructure(new File(gitRepoCtx.getLocalRepoPath()));
            cloneRepository(gitRepoCtx);
            return true;

        } catch (JGitInternalException e) {
            log.warn("Git pull unsuccessful for tenant " + gitRepoCtx.getTenantId() + ", " + e.getMessage());
            return false;

        } catch (TransportException e) {
            log.error("Accessing remote git repository " + gitRepoCtx.getRemoteRepoUrl() + " failed for tenant " + gitRepoCtx.getTenantId(), e);
            return false;

        } catch (CheckoutConflictException e) {
            log.warn("Git pull for the path " + e.getConflictingPaths().toString() + " failed due to conflicts");
            //FileUtilities.deleteFolderStructure(new File(gitRepoCtx.getLocalRepoPath()));
            //cloneRepository(gitRepoCtx);
            throw e;

        } catch (GitAPIException e) {
            log.error("Git pull operation for tenant " + gitRepoCtx.getTenantId() + " failed", e);
            return false;
        }

        return true;
    }

    /**
     * Checkout the artifacts from the local index. Any local working copy changes will be discarded.
     *
     * @param gitRepoCtx TenantGitRepositoryContext instance for the current tenant
     * @param paths List of paths for artifacts with conflicts
     * @return true if successfully checked out all the files from local index, else false
     */
    private boolean checkoutFromLocalIndex(TenantGitRepositoryContext gitRepoCtx, List<String> paths) {

        boolean checkoutSuccess = false;

        if(paths.isEmpty()) {
            return checkoutSuccess;
        }

        CheckoutCommand checkoutCmd = gitRepoCtx.getGit().checkout();
        for(String path : paths) {
            checkoutCmd.addPath(path);
        }

        try {
            checkoutCmd.call();
            if(log.isDebugEnabled()) {
                for(String path : paths) {
                    log.debug("Checked out conflicting file " + path + " from the local index successfully");
                }
            }
            checkoutSuccess = true;
            log.info("Checked out the conflicting files from the local index successfully");

        } catch (GitAPIException e) {
            log.error("Checking out artifacts from index failed", e);
        }

        return checkoutSuccess;
    }

    public void initAutoCheckout(boolean b) throws DeploymentSynchronizerException {
        //no implementation
    }

    public void cleanupAutoCheckout() {
        //no implementation
    }

    /**
     * Return the repository type
     *
     * @return repository type, i.e. git
     */
    public String getRepositoryType() {

        return DeploymentSynchronizerConstants.REPOSITORY_TYPE_GIT;
    }

    public List<RepositoryConfigParameter> getParameters() {

        return null;
    }

    /**
     * Partial checkout with defined depth. Currently not supported in GIT.
     *
     * @param tenantId tenant id
     * @param filePath local repository path
     * @param depth depth to checkout (0 - 3)
     * @return if success true, else false
     * @throws DeploymentSynchronizerException if an error occurs
     */
    public boolean checkout(int tenantId, String filePath, int depth) throws DeploymentSynchronizerException {

        return checkout(tenantId, filePath); //normal checkout is done
    }

    /**
     * Partial update with defined depth.Currently not supported in GIT.
     *
     * @param tenantId tenant Id
     * @param rootPath root path to the local repository
     * @param filePath path to sub directory to update
     * @param depth depth to update (0 - 3)
     * @return if success true, else false
     * @throws DeploymentSynchronizerException if an error occurs
     */
    public boolean update(int tenantId, String rootPath, String filePath, int depth) throws DeploymentSynchronizerException {

        return checkout(tenantId, rootPath); //normal checkout is done
    }

    /**
     * removed tenant's information from the cache
     *
     * @param tenantId tenant Id
     */
    public void cleanupTenantContext(int tenantId) {
        TenantGitRepositoryContextCache.getTenantRepositoryContextCache().
                removeCachedTenantGitContext(tenantId);
    }

    @Override
    public void delete(int tenantId) throws SVNClientException {

    }

    private void handleError (String errorMsg, Exception e) throws DeploymentSynchronizerException {
        log.error(errorMsg, e);
        throw new DeploymentSynchronizerException(errorMsg, e);
    }

}
