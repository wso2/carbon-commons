package org.wso2.carbon.deployment.synchronizer.git.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;

import java.io.File;

/**
 * Git Repository Context class. Keeps track of git configurations per tenant.
 */
public class GitRepositoryContext {

    private static final Log log = LogFactory.getLog(GitRepositoryContext.class);

    private String gitRemoteRepoUrl;
    private String gitLocalRepoPath;
    private Repository localRepo;
    private Git git;
    private boolean cloneExists;
    private int tenantId;
    private File gitRepoDir;
    private RepositoryInformationServiceClient repoInfoServiceClient;
    private boolean keyBasedAuthentication;

    public GitRepositoryContext () {

    }

    public String getGitRemoteRepoUrl() {
        return gitRemoteRepoUrl;
    }

    public void setGitRemoteRepoUrl(String gitRemoteRepoUrl) {
        this.gitRemoteRepoUrl = gitRemoteRepoUrl;
    }

    public String getGitLocalRepoPath() {
        return gitLocalRepoPath;
    }

    public void setGitLocalRepoPath(String gitLocalRepoPath) {
        this.gitLocalRepoPath = gitLocalRepoPath;
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

    public File getGitRepoDir() {
        return gitRepoDir;
    }

    public void setGitRepoDir(File gitRepoDir) {
        this.gitRepoDir = gitRepoDir;
    }

    public RepositoryInformationServiceClient getRepoInfoServiceClient() {
        return repoInfoServiceClient;
    }

    public void setRepoInfoServiceClient(RepositoryInformationServiceClient repoInfoServiceClient) {
        this.repoInfoServiceClient = repoInfoServiceClient;
    }

    public boolean getKeyBasedAuthentication() {
        return keyBasedAuthentication;
    }

    public void setKeyBasedAuthentication(boolean keyBasedAuthentication) {
        this.keyBasedAuthentication = keyBasedAuthentication;
    }
}
