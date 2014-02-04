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

package org.wso2.carbon.deployment.synchronizer;

import org.wso2.carbon.deployment.synchronizer.internal.util.RepositoryConfigParameter;

import java.util.List;

/**
 * Represents a remote repository instance. The DeploymentSynchronizer interacts with the
 * remote repository through this interface.
 */
public interface ArtifactRepository {

    /**
     * Initializes the remote artifact repository and prepare to synchronize the local
     * repository against it.
     *
     * @param tenantId ID of the tenant to which the synchronizer/repository belongs
     * @throws DeploymentSynchronizerException If an error occurs while initializing the repository
     */
    public void init(int tenantId) throws DeploymentSynchronizerException;

    /**
     * Commit the artifacts in the local repository to the remote repository
     *
     * @param tenantId tenant Id
     * @param filePath File path of the local repository
     * @throws DeploymentSynchronizerException on error
     * @return true if file changes were committed, false otherwise
     */
    public boolean commit(int tenantId, String filePath) throws DeploymentSynchronizerException;

    /**
     * Checkout all or updated artifacts from the remote repository to the local file system
     *
     * @param tenantId tenant Id
     * @param filePath File path of the local repository
     * @throws DeploymentSynchronizerException on error
     * @return true if files were checked out or updated, false otherwise
     */
    public boolean checkout(int tenantId, String filePath) throws DeploymentSynchronizerException;

    /**
     * Setup the remote repository for auto checkouts
     *
     * @param useEventing If eventing based auto checkout has been requested
     * @throws DeploymentSynchronizerException on error
     */
    public void initAutoCheckout(boolean useEventing) throws DeploymentSynchronizerException;

    /**
     * Clean up any actions taken during initializing auto checkout
     */
    public void cleanupAutoCheckout();

    /**
     *   Get the Repository Type of the Synchronizer
     * @return   The Repository Type
     */
    public String getRepositoryType();

    /**
     *  Get the list of configuration parameters specific for each Repository.
     * @return  List of RepositoryConfigParameters
     */
    public List<RepositoryConfigParameter> getParameters();

    /**
     * Checkout artifacts from the remote repository with given depth to the local file system
     *
     * @param tenantId tenant Id
     * @param filePath File path of the local repository
     * @param depth - given depth to get the update
     * @return true if files were checked out or updated, false otherwise
     * @throws DeploymentSynchronizerException on error
     */

    public boolean checkout(int tenantId, String filePath, int depth) throws DeploymentSynchronizerException;

    /**
     * Invoke the update operation on the specified file in the repository, with given depth
     *
     * @param tenantId tenant Id
     * @param rootPath - root path of the repository of which the dep-sychronizer is registered
     * @param filePathToUpdate - location of the file in the repository
     * @param depth Depth given to update, (eg 0 - empty, 3 - infinite)
     * @return true if files were updated, false otherwise
     * @throws DeploymentSynchronizerException on error
     */
    public boolean update(int tenantId, String rootPath, String filePathToUpdate, int depth) throws DeploymentSynchronizerException;

    /**
     * Cleanup the cached tenant context for the tenant specified by tenantId
     *
     * @param tenantId tenant Id
     */
    public void cleanupTenantContext(int tenantId);
}
