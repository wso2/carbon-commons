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

package org.wso2.carbon.deployment.synchronizer.internal.util;

public class DeploymentSynchronizerConfiguration {

    private boolean enabled;
    private boolean autoCommit;
    private boolean autoCheckout;
    private boolean useEventing;

    private String repositoryType;

    private long period;

    private boolean serverBasedConfiguration;

    private RepositoryConfigParameter[] repositoryConfigParameters;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public boolean isAutoCheckout() {
        return autoCheckout;
    }

    public void setAutoCheckout(boolean autoCheckout) {
        this.autoCheckout = autoCheckout;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public boolean isUseEventing() {
        return useEventing;
    }

    public void setUseEventing(boolean useEventing) {
        this.useEventing = useEventing;
    }

    public String getRepositoryType() {
        return repositoryType;
    }

    public void setRepositoryType(String repositoryType) {
        this.repositoryType = repositoryType;
    }

    public RepositoryConfigParameter[] getRepositoryConfigParameters() {
        return repositoryConfigParameters;
    }

    public void setRepositoryConfigParameters(RepositoryConfigParameter[] repositoryConfigParameters) {
        this.repositoryConfigParameters = repositoryConfigParameters;
    }

    /**
     * Indicates whether the configuration is retrieved from the server config file (carbon.xml)
     * @return  True if configuration is retrieved from carbon.xml. False otherwise.
     */
    public boolean isServerBasedConfiguration() {
        return serverBasedConfiguration;
    }

    public void setServerBasedConfiguration(boolean serverBasedConfiguration) {
        this.serverBasedConfiguration = serverBasedConfiguration;
    }

}
