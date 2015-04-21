/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.deployment.synchronizer.internal;

import static org.wso2.carbon.deployment.synchronizer.DeploymentSynchronizerConstants.DEPLOYMENT_SYNCHRONIZER;

public class DeploymentSynchronizerConstants {

    // Configuration parameters for carbon.xml
    public static final String ENABLED = DEPLOYMENT_SYNCHRONIZER + ".Enabled";
    public static final String AUTO_COMMIT_MODE = DEPLOYMENT_SYNCHRONIZER + ".AutoCommit";
    public static final String AUTO_CHECKOUT_MODE = DEPLOYMENT_SYNCHRONIZER + ".AutoCheckout";
    public static final String POOL_SIZE = DEPLOYMENT_SYNCHRONIZER + ".PoolSize";
    public static final String AUTO_SYNC_PERIOD = DEPLOYMENT_SYNCHRONIZER + ".AutoSyncPeriod";
    public static final String USE_EVENTING = DEPLOYMENT_SYNCHRONIZER + ".UseEventing";
    public static final String REPOSITORY_TYPE = DEPLOYMENT_SYNCHRONIZER + ".RepositoryType";
    public static final String SVN_URL = DEPLOYMENT_SYNCHRONIZER + ".SVNURL";
    public static final String SVN_USERNAME = DEPLOYMENT_SYNCHRONIZER + ".SVNUsername";
    public static final String SVN_PASSWORD = DEPLOYMENT_SYNCHRONIZER + ".SVNPassword";
    public static final String SVN_FORCE_UPDATE = DEPLOYMENT_SYNCHRONIZER + ".SVNForceUpdate";
    public static final String SVN_IGNORE_EXTERNALS = DEPLOYMENT_SYNCHRONIZER + ".SVNIgnoreExternals";
    public static final String SVN_APP_TENANT_TO_URL = DEPLOYMENT_SYNCHRONIZER + ".SVNAppendTenantToUrl";

    // Registry constants
    public static final String SUPER_TENANT_REGISTRY_PATH = "/repository/deployment/server";
    public static final String TENANT_REGISTRY_PATH = "/repository/deployment";
    public static final String DEPLOYMENT_SYNC_CONFIG_ROOT =
            "repository/components/org.wso2.carbon.deployment.synchronizer/";
    public static final String CARBON_REPOSITORY = DEPLOYMENT_SYNC_CONFIG_ROOT + "CarbonRepository";
    public static final String EVENT_FILTER_DIALECT =
            "http://wso2.org/registry/eventing/dialect/topicFilter";

    public static final String REPOSITORY_TYPE_REGISTRY = "registry";
    public static final String REPOSITORY_TYPE_SVN = "svn";
    public static final String REPOSITORY_TYPE_GIT = "git";

    // Defaults
    public static final int DEFAULT_POOL_SIZE = 20;
    public static final long DEFAULT_AUTO_SYNC_PERIOD = 60L;
    public static final String DEFAULT_REPOSITORY_TYPE = REPOSITORY_TYPE_REGISTRY;

    public static final String EVENT_RECEIVER_SERVICE = "AutoCheckoutService";
}
