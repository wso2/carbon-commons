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

package org.wso2.carbon.deployment.synchronizer.registry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.deployment.synchronizer.ArtifactRepository;
import org.wso2.carbon.deployment.synchronizer.DeploymentSynchronizerException;
import org.wso2.carbon.deployment.synchronizer.internal.DeploymentSynchronizerConstants;
import org.wso2.carbon.deployment.synchronizer.internal.util.RepositoryConfigParameter;
import org.wso2.carbon.deployment.synchronizer.internal.util.ServiceReferenceHolder;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.synchronization.RegistrySynchronizer;
import org.wso2.carbon.registry.synchronization.SynchronizationException;
import org.wso2.carbon.registry.synchronization.Utils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.List;

/**
 * Use this class in conjunction with the DeploymentSynchronizer to synchronize a file system
 * repository against a repository stored in the registry.
 */
public class RegistryBasedArtifactRepository implements ArtifactRepository {

    private static final Log log = LogFactory.getLog(RegistryBasedArtifactRepository.class);
    
    private UserRegistry registry;
    private String registryPath;
    private String basePath;

    private String subscriptionId;

    public RegistryBasedArtifactRepository(){}

    public RegistryBasedArtifactRepository(UserRegistry registry,
                                           String registryPath, String basePath) {
        this.registry = registry;
        this.registryPath = registryPath;
        this.basePath = basePath;
    }

    public void init(int tenantId) throws DeploymentSynchronizerException {

        try {
            UserRegistry configRegistry = getConfigurationRegistry(tenantId);
            String tenantRegistryPath = getRegistryPath(tenantId);

            this.registry = configRegistry;
            this.registryPath = tenantRegistryPath;
            this.basePath = RegistryConstants.CONFIG_REGISTRY_BASE_PATH;

            if (!configRegistry.resourceExists(tenantRegistryPath)) {
                Collection collection = configRegistry.newCollection();
                configRegistry.put(tenantRegistryPath, collection);
                collection.discard();
            }

        } catch (RegistryException e) {
            throw new DeploymentSynchronizerException("Error while accessing registry for " +
                    "tenant: " + tenantId, e);
        }

//        try {
//            if (!registry.resourceExists(registryPath)) {
//                Collection collection = registry.newCollection();
//                registry.put(registryPath, collection);
//                collection.discard();
//            }
//        } catch (RegistryException e) {
//            handleException("Error while creating the registry collection at: " + registryPath, e);
//        }
    }

    private static UserRegistry getConfigurationRegistry(int tenantId) throws RegistryException {
        return ServiceReferenceHolder.getRegistryService().getConfigSystemRegistry(tenantId);
    }

    private static String getRegistryPath(int tenantId) {
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            return DeploymentSynchronizerConstants.SUPER_TENANT_REGISTRY_PATH;
        } else {
            return DeploymentSynchronizerConstants.TENANT_REGISTRY_PATH;
        }
    }
    public boolean commit(int tenantId, String filePath) throws DeploymentSynchronizerException {
        if (log.isDebugEnabled()) {
            log.debug("Committing artifacts at " + filePath + " to the collection at " +
                    registryPath);
        }
        try {
            if (!RegistrySynchronizer.isCheckedOut(filePath)) {
                RegistrySynchronizer.checkOut(registry, filePath, registryPath);
            }
            Utils.addResource(filePath);
            Utils.setResourcesDelete(filePath);
            return RegistrySynchronizer.checkIn(registry, filePath, false);
        } catch (SynchronizationException e) {
            handleException("Error while committing artifacts to the registry", e);
        }
        return false;
    }

    public boolean checkout(int tenantId, String filePath) throws DeploymentSynchronizerException {
        if (log.isDebugEnabled()) {
            log.debug("Checking out artifacts from " + registryPath + " to the file system " +
                    "at " + filePath);
        }
        boolean succeed = false;
        try {
            if (RegistrySynchronizer.isCheckedOut(filePath)) {
                succeed =  RegistrySynchronizer.update(registry, filePath, false);
            } else {
                succeed =  RegistrySynchronizer.checkOut(registry, filePath, registryPath);
            }
        } catch (SynchronizationException e) {
            handleException("Error while updating artifacts in the file system from the registry", e);
        }
        return succeed;
    }

    public void initAutoCheckout(boolean useEventing) throws DeploymentSynchronizerException {
        // In the registry based implementation we can subscribe for registry events
        if (useEventing && subscriptionId == null && ServiceReferenceHolder.getEventingService() != null) {
            String absolutePath = RegistryUtils.getAbsoluteRegistryPath(registryPath, basePath);
            subscriptionId = RegistryUtils.subscribeForRegistryEvents(registry, absolutePath,
                    RegistryUtils.getEventReceiverEndpoint());
            if (log.isDebugEnabled()) {
                log.debug("Subscribed for registry events on the collection: " + absolutePath +
                        " with the subscription ID: " + subscriptionId);
            }
        }
    }

    public void cleanupAutoCheckout() {
        if (subscriptionId == null) {
            return;
        }

        boolean unsubscribe = RegistryUtils.unsubscribeForRegistryEvents(subscriptionId,
                        registry.getTenantId());
        if (!unsubscribe) {
            log.warn("Subscription for registry events could not be removed");
        } else if (log.isDebugEnabled()) {
            log.debug("Unsubscribed from registry events with the ID: " + subscriptionId);
        }
        subscriptionId = null;
    }

    @Override
    public String getRepositoryType() {
        return DeploymentSynchronizerConstants.REPOSITORY_TYPE_REGISTRY;
    }

    @Override
    public List<RepositoryConfigParameter> getParameters(){
        //Returning null since the Registry Based Artifact Repository does not have any specific
        //configuration parameters.
        return null;
    }

    @Override
    public boolean checkout(int tenantId, String filePath, int depth)
            throws DeploymentSynchronizerException {
        throw new DeploymentSynchronizerException("Not implemented yet.");
    }

    @Override
    public boolean update(int tenantId, String rootPath, String filePath, int depth) throws DeploymentSynchronizerException {
        throw new DeploymentSynchronizerException("Not implemented yet.");
    }

    @Override
    public void cleanupTenantContext(int tenantId) {
        //nothing to do yet. TODO implement reg based tenant contexts
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RegistryBasedArtifactRepository that = (RegistryBasedArtifactRepository) o;

        if (!getRepositoryType().equals(that.getRepositoryType())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return getRepositoryType().hashCode();
    }

    private void handleException(String msg, Exception e) throws DeploymentSynchronizerException {
        log.error(msg, e);
        throw new DeploymentSynchronizerException(msg, e);
    }
}
