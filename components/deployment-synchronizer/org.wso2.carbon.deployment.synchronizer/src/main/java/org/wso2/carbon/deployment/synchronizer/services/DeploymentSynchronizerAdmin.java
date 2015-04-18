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

package org.wso2.carbon.deployment.synchronizer.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.deployment.synchronizer.ArtifactRepository;
import org.wso2.carbon.deployment.synchronizer.internal.DeploymentSynchronizationManager;
import org.wso2.carbon.deployment.synchronizer.internal.DeploymentSynchronizer;
import org.wso2.carbon.deployment.synchronizer.DeploymentSynchronizerException;
import org.wso2.carbon.deployment.synchronizer.internal.repository.CarbonRepositoryUtils;
import org.wso2.carbon.deployment.synchronizer.internal.util.DeploymentSynchronizerConfiguration;
import org.wso2.carbon.deployment.synchronizer.internal.util.RepositoryConfigParameter;
import org.wso2.carbon.deployment.synchronizer.internal.util.RepositoryReferenceHolder;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.List;
import java.util.Set;

/**
 * Admin service for managing the deployment synchronizer component and synchronizers engaged
 * on the Carbon repository.
 */
public class DeploymentSynchronizerAdmin extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(DeploymentSynchronizerAdmin.class);

    public void enableSynchronizerForCarbonRepository(DeploymentSynchronizerConfiguration config)
            throws DeploymentSynchronizerException {

        int tenantId = MultitenantUtils.getTenantId(getConfigContext());
        try {
            CarbonRepositoryUtils.persistConfiguration(config, tenantId);
            DeploymentSynchronizer synchronizer = null;

            try{
                //Attempt to create a new Repository Synchronizer.
                synchronizer = CarbonRepositoryUtils.
                        newCarbonRepositorySynchronizer(tenantId);
            }catch (DeploymentSynchronizerException e){
                //If creation fails, disable dep-sync and persist configuration
                config.setEnabled(false);
                CarbonRepositoryUtils.persistConfiguration(config, tenantId);
                throw e;
            }

            if (synchronizer != null) {
                synchronizer.start();
            } else {
                String msg = "Unable to create a deployment synchronizer instance";
                log.warn(msg);
                throw new DeploymentSynchronizerException(msg);
            }
        }   catch (RegistryException e) {
            handleException("Error while enabling deployment synchronizer", e);
        }
    }

    public void disableSynchronizerForCarbonRepository() throws DeploymentSynchronizerException {

        int tenantId = MultitenantUtils.getTenantId(getConfigContext());
        try {
            DeploymentSynchronizerConfiguration config =
                    CarbonRepositoryUtils.getActiveSynchronizerConfiguration(tenantId);
            if (config == null || !config.isEnabled()) {
                log.warn("Attempted to disable an already disabled deployment synchronizer");
                return;
            }
            config.setEnabled(false);
            CarbonRepositoryUtils.persistConfiguration(config, tenantId);
        } catch (RegistryException e) {
            handleException("Error while persisting the deployment synchronizer configuration", e);
        }

        String filePath = CarbonRepositoryUtils.getCarbonRepositoryFilePath(getConfigContext());
        DeploymentSynchronizer synchronizer =  DeploymentSynchronizationManager.getInstance().
                deleteSynchronizer(filePath);
        if (synchronizer != null) {
            synchronizer.stop();
        }
    }

    public void updateSynchronizerForCarbonRepository(DeploymentSynchronizerConfiguration config)
            throws DeploymentSynchronizerException {

        disableSynchronizerForCarbonRepository();
        enableSynchronizerForCarbonRepository(config);
    }

    public long getLastCommitTime() {
        String filePath = CarbonRepositoryUtils.getCarbonRepositoryFilePath(getConfigContext());
        DeploymentSynchronizer synchronizer = DeploymentSynchronizationManager.getInstance().
                getSynchronizer(filePath);
        if (synchronizer != null) {
            return synchronizer.getLastCommitTime();
        }
        return -1L;
    }

    public long getLastCheckoutTime() {
        String filePath = CarbonRepositoryUtils.getCarbonRepositoryFilePath(getConfigContext());
        DeploymentSynchronizer synchronizer = DeploymentSynchronizationManager.getInstance().
                getSynchronizer(filePath);
        if (synchronizer != null) {
            return synchronizer.getLastCheckoutTime();
        }
        return -1L;
    }

    public void checkout() throws DeploymentSynchronizerException {
        String filePath = CarbonRepositoryUtils.getCarbonRepositoryFilePath(getConfigContext());
        DeploymentSynchronizer synchronizer = DeploymentSynchronizationManager.getInstance().
                getSynchronizer(filePath);
        synchronizer.checkout();
    }

    public void commit() throws DeploymentSynchronizerException {
        String filePath = CarbonRepositoryUtils.getCarbonRepositoryFilePath(getConfigContext());
        DeploymentSynchronizer synchronizer = DeploymentSynchronizationManager.getInstance().
                getSynchronizer(filePath);
        synchronizer.commit();
    }

    public boolean synchronizerEnabledForCarbonRepository() {
        String filePath = CarbonRepositoryUtils.getCarbonRepositoryFilePath(getConfigContext());
        return DeploymentSynchronizationManager.getInstance().getSynchronizer(filePath) != null;
    }

    public DeploymentSynchronizerConfiguration getSynchronizerConfigurationForCarbonRepository()
            throws DeploymentSynchronizerException {

        int tenantId = MultitenantUtils.getTenantId(getConfigContext());
        return CarbonRepositoryUtils.getActiveSynchronizerConfiguration(tenantId);
    }
    
    public RepositoryConfigParameter[] getParamsByRepositoryType(String repositoryType){

        Set<ArtifactRepository> repositories = RepositoryReferenceHolder.getInstance().getRepositories().keySet();

        if(repositories != null && !repositories.isEmpty()){
            for(ArtifactRepository repository : repositories){
                if(repository.getRepositoryType().equals(repositoryType)){
                    List<RepositoryConfigParameter> parameters =
                            RepositoryReferenceHolder.getInstance().getRepositories().get(repository);
                    if(parameters != null && !parameters.isEmpty()){
                        return parameters.toArray(new RepositoryConfigParameter[parameters.size()]);
                    }
                }
            }
        }

        return new RepositoryConfigParameter[0];
    }

    /**
     * Get all available ArtifactRepositories
     * @return Available ArtifactRepositories
     */
    public String[] getRepositoryTypes(){

        Set<ArtifactRepository> repositories =
                RepositoryReferenceHolder.getInstance().getRepositories().keySet();
        if(repositories != null){
            String[] repositoryTypes = new String[repositories.size()];
            int index = 0;
            
            for(ArtifactRepository repository : repositories){
                repositoryTypes[index] = repository.getRepositoryType();
                index++;
            }
            return repositoryTypes;
        }
        return null;
    }

    private void handleException(String msg, Exception e) throws DeploymentSynchronizerException {
        log.error(msg, e);
        throw new DeploymentSynchronizerException(msg, e);
    }

}
