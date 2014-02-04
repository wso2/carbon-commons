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

package org.wso2.carbon.deployment.synchronizer.subversion;

import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.wso2.carbon.deployment.synchronizer.TenantRepositoryContext;
import org.wso2.carbon.deployment.synchronizer.internal.util.DeploymentSynchronizerConfiguration;

/**
 * Stores SVN related details of a given tenant. One per each tenant
 *
 */
public class TenantSVNRepositoryContext extends TenantRepositoryContext {

    private SVNUrl svnUrl;
    private ISVNClientAdapter svnClient;

    private DeploymentSynchronizerConfiguration conf;

    private boolean ignoreExternals = true;
    private boolean forceUpdate = true;

    public SVNUrl getSvnUrl() {
        return svnUrl;
    }

    public void setSvnUrl(SVNUrl svnUrl) {
        this.svnUrl = svnUrl;
    }

    public ISVNClientAdapter getSvnClient() {
        return svnClient;
    }

    public void setSvnClient(ISVNClientAdapter svnClient) {
        this.svnClient = svnClient;
    }

    public DeploymentSynchronizerConfiguration getConf() {
        return conf;
    }

    public void setConf(DeploymentSynchronizerConfiguration conf) {
        this.conf = conf;
    }

    public boolean isIgnoreExternals() {
        return ignoreExternals;
    }

    public void setIgnoreExternals(boolean ignoreExternals) {
        this.ignoreExternals = ignoreExternals;
    }

    public boolean isForceUpdate() {
        return forceUpdate;
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

}
