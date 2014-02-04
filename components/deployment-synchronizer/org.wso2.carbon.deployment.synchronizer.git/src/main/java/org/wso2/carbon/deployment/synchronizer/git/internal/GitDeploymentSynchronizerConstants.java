package org.wso2.carbon.deployment.synchronizer.git.internal;

import static org.wso2.carbon.deployment.synchronizer.DeploymentSynchronizerConstants.DEPLOYMENT_SYNCHRONIZER;

/**
 * Git based DeploymentSynchronizer constants
 */
public class GitDeploymentSynchronizerConstants {

    public static final String ENABLED = DEPLOYMENT_SYNCHRONIZER + ".Enabled";

    //EPR for the repository Information Service
    public static final String REPO_INFO_SERVICE_EPR = "RepoInfoServiceEpr";

    //CartridgeAlias property name from carbon.xml
    public static final String CARTRIDGE_ALIAS = "CartridgeAlias";

    //ServerKey property from the carbon.xml
    public static final String SERVER_KEY = "ServerKey";

    //Configuration parameter names read from carbon.xml
    public static final String REPOSITORY_TYPE = DEPLOYMENT_SYNCHRONIZER + ".RepositoryType";
    public static final String DEPLOYMENT_METHOD = DEPLOYMENT_SYNCHRONIZER + ".StandardDeployment";
    public static final String GIT_SERVER = DEPLOYMENT_SYNCHRONIZER + ".GitServer";
    public static final String GIT_REPO_BASE_URL = DEPLOYMENT_SYNCHRONIZER + ".GitBaseUrl";
    public static final String GIT_USERNAME = DEPLOYMENT_SYNCHRONIZER + ".GitUserName";
    public static final String GIT_PASSWORD = DEPLOYMENT_SYNCHRONIZER + ".GitPassword";

    //Git based constants
    public static final String GIT_REFS_HEADS_MASTER = "refs/heads/master";
    public static final String REMOTE = "remote";
    public static final String ORIGIN = "origin";
    public static final String URL = "url";
    public static final String FETCH = "fetch";
    public static final String BRANCH = "branch";
    public static final String MASTER = "master";
    public static final String MERGE = "merge";
    public static final String FETCH_LOCATION = "+refs/heads/*:refs/remotes/origin/*";

    //Git server implementations
    public static final String SERVER_GITBLIT = "gitblit";
    public static final String SERVER_SCM = "scm";
    public static final String SERVER_UNSPECIFIED = "unspecified";

}
