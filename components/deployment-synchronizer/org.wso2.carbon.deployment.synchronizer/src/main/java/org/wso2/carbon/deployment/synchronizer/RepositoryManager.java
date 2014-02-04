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

package org.wso2.carbon.deployment.synchronizer;

public abstract class RepositoryManager {

    protected RepositoryCreator repositoryCreator;

    /**
     * Default constructor
     */
    public RepositoryManager () {
        this.repositoryCreator = null;
    }

    /**
     * Constructor
     *
     * @param repositoryCreator Repository creator instance
     */
    public RepositoryManager (RepositoryCreator repositoryCreator) {
        this.repositoryCreator = repositoryCreator;
    }

    /**
     * Sets the repository creator implementation
     *
     * @param repositoryCreator RepositoryCreator instance
     */
    public void setRepositoryCreator (RepositoryCreator repositoryCreator) {
        this.repositoryCreator = repositoryCreator;
    }

    /**
     * Returns a RepositoryInformation instance populated with repository url for a tenant
     *
     * @param tenantId tenant Id
     * @return RepositoryInformation instance if relevant details are found for tenant, else null
     * @throws DeploymentSynchronizerException in case of an error
     */
    public abstract RepositoryInformation getUrlInformation(int tenantId) throws DeploymentSynchronizerException;

    /**
     * Returns a RepositoryInformation instance populated with repository credentials for a tenant
     *
     * @param tenantId tenant Id
     * @return RepositoryInformation instance if relevant details are found for tenant, else null
     * @throws DeploymentSynchronizerException in case of an error
     */
    public abstract RepositoryInformation getCredentialsInformation(int tenantId) throws DeploymentSynchronizerException;

    /**
     * Creates a repository for the tenant in the location specified, if it doesn't exist already
     *
     * @param tenantId tenant Id
     * @throws DeploymentSynchronizerException in case of an error
     */
    public abstract void provisionRepository (int tenantId) throws DeploymentSynchronizerException;

    /**
     * Adds the already existing repository specified by tenant
     *
     * @param tenantId tenant Id
     * @param url repository url
     * @throws DeploymentSynchronizerException
     */
    public abstract void addRepository (int tenantId, String url) throws DeploymentSynchronizerException;

}
