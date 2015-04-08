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

package org.wso2.carbon.deployment.synchronizer.repository;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.deployment.synchronizer.*;
import org.wso2.carbon.deployment.synchronizer.util.DeploymentSynchronizerConfiguration;
import org.wso2.carbon.deployment.synchronizer.util.RepositoryConfigParameter;
import org.wso2.carbon.deployment.synchronizer.internal.util.RepositoryReferenceHolder;
import org.wso2.carbon.deployment.synchronizer.internal.util.ServiceReferenceHolder;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.List;

/**
 * Utility methods for creating and managing DeploymentSynchronizer instances for Carbon
 * repositories
 */
public class CarbonRepositoryUtils {
    private static final Log log = LogFactory.getLog(CarbonRepositoryUtils.class);

    /**
     * Create and initialize a new DeploymentSynchronizer for the Carbon repository of the
     * specified tenant. This method first attempts to load the synchronizer configuration
     * from the registry. If a configuration does not exist in the registry, it will get the
     * configuration from the global ServerConfiguration of Carbon. Note that this method
     * does not start the created synchronizers. It only creates and initializes them using
     * the available configuration settings.
     *
     * @param tenantId ID of the tenant
     * @return a DeploymentSynchronizer instance or null if the synchronizer is disabled
     * @throws org.wso2.carbon.deployment.synchronizer.DeploymentSynchronizerException If an error occurs while initializing the synchronizer
     */
    public static DeploymentSynchronizer newCarbonRepositorySynchronizer(int tenantId)
            throws DeploymentSynchronizerException {

        DeploymentSynchronizerConfiguration config = getActiveSynchronizerConfiguration(tenantId);

        if (config.isEnabled()) {
            String filePath = MultitenantUtils.getAxis2RepositoryPath(tenantId);

            ArtifactRepository artifactRepository = createArtifactRepository(
                    config.getRepositoryType());
            artifactRepository.init(tenantId);
            DeploymentSynchronizer synchronizer = DeploymentSynchronizationManager.getInstance().
                    createSynchronizer(tenantId, artifactRepository, filePath);
            synchronizer.setAutoCommit(config.isAutoCommit());
            synchronizer.setAutoCheckout(config.isAutoCheckout());
            synchronizer.setPeriod(config.getPeriod());
            synchronizer.setUseEventing(config.isUseEventing());

            if (log.isDebugEnabled()) {
                log.debug("Registered file path:" + filePath + " for tenant: " + tenantId);
            }
            return synchronizer;
        }
        return null;
    }

    /**
     * Loads the deployment synchronizer configuration. It will attempt to get the configuration
     * from the registry of the tenant. Failing that, it will get the configuration from the
     * ServerConfiguration.
     *
     * @param tenantId Tenant ID
     * @return a DeploymentSynchronizerConfiguration instance
     * @throws org.wso2.carbon.deployment.synchronizer.DeploymentSynchronizerException if an error occurs while accessing the registry
     */
    public static DeploymentSynchronizerConfiguration getActiveSynchronizerConfiguration(
            int tenantId) throws DeploymentSynchronizerException {

        try {
            //Firrst attempt to get the configuration from carbon.xml
            DeploymentSynchronizerConfiguration config = getDeploymentSyncConfigurationFromConf();

            //If configuration has not been specified in carbon.xml
            if (config == null) {
                //Attempt to get configuration from local registry.
                config =  getDeploymentSyncConfigurationFromRegistry(tenantId);

                //If configuration does not exist in the local registry as well.
                if (config == null) {
                    //Get default Deployment Synchronizer Configuration
                    config = getDefaultDeploymentSyncConfiguration();
                }
            }
            else{
                //If config is obtained from carbon.xml, set attribute to disable UI
                config.setServerBasedConfiguration(true);
            }
            return config;
        } catch (RegistryException e) {
            throw new DeploymentSynchronizerException("Error while loading synchronizer " +
                    "configuration from the registry", e);
        }
    }

    /**
     * Load the deployment synchronizer configuration from the global ServerConfiguration
     * of Carbon.
     *
     * @return a DeploymentSynchronizerConfiguration instance
     * @throws org.wso2.carbon.deployment.synchronizer.DeploymentSynchronizerException on error
     */
    public static DeploymentSynchronizerConfiguration getDeploymentSyncConfigurationFromConf() throws DeploymentSynchronizerException{

        DeploymentSynchronizerConfiguration config = new DeploymentSynchronizerConfiguration();
        ServerConfiguration serverConfig = ServerConfiguration.getInstance();

        String value = serverConfig.getFirstProperty(DeploymentSynchronizerConstants.ENABLED);
        //If Deployment Synchronizer Configuration is not found in carbon.xml
        if(value == null){
            return null;
        }
        config.setEnabled(JavaUtils.isTrueExplicitly(value));

        if (config.isEnabled()) {
            value = serverConfig.getFirstProperty(DeploymentSynchronizerConstants.AUTO_CHECKOUT_MODE);
            config.setAutoCheckout(value != null && JavaUtils.isTrueExplicitly(value));

            value = serverConfig.getFirstProperty(DeploymentSynchronizerConstants.AUTO_COMMIT_MODE);
            config.setAutoCommit(value != null && JavaUtils.isTrueExplicitly(value));

            value = serverConfig.getFirstProperty(DeploymentSynchronizerConstants.USE_EVENTING);
            config.setUseEventing(value != null && JavaUtils.isTrueExplicitly(value));

            value = serverConfig.getFirstProperty(DeploymentSynchronizerConstants.AUTO_SYNC_PERIOD);
            if (value != null) {
                config.setPeriod(Long.parseLong(value));
            } else {
                config.setPeriod(DeploymentSynchronizerConstants.DEFAULT_AUTO_SYNC_PERIOD);
            }

            value = serverConfig.getFirstProperty(DeploymentSynchronizerConstants.REPOSITORY_TYPE);
            if (value != null) {
                config.setRepositoryType(value);
            } else {
                config.setRepositoryType(DeploymentSynchronizerConstants.DEFAULT_REPOSITORY_TYPE);
            }

            ArtifactRepository repository =
                    RepositoryReferenceHolder.getInstance().getRepositoryByType(config.getRepositoryType());
            if(repository == null){
                throw new DeploymentSynchronizerException("No Repository found for type " + config.getRepositoryType());
            }

            List<RepositoryConfigParameter> parameters = repository.getParameters();

            //If repository specific configuration parameters are found.
            if(parameters != null){
                //Find the 'value' of each parameter from the server config by parameter 'name' and attach to parameter
                for(RepositoryConfigParameter parameter : parameters){
                    parameter.setValue(serverConfig.getFirstProperty(parameter.getName()));
                }

                //Attach parameter list to config object.
                config.setRepositoryConfigParameters(
                        parameters.toArray(new RepositoryConfigParameter[parameters.size()]));
            }

            return config;
        }else{
            return config;
        }
    }

    public static DeploymentSynchronizerConfiguration getDefaultDeploymentSyncConfiguration() throws DeploymentSynchronizerException {

        DeploymentSynchronizerConfiguration config = new DeploymentSynchronizerConfiguration();
        config.setEnabled(false);
        config.setAutoCheckout(false);
        config.setAutoCommit(false);
        config.setUseEventing(false);
        config.setPeriod(DeploymentSynchronizerConstants.DEFAULT_AUTO_SYNC_PERIOD);
        config.setRepositoryType(DeploymentSynchronizerConstants.DEFAULT_REPOSITORY_TYPE);

        ArtifactRepository repository =
                RepositoryReferenceHolder.getInstance().getRepositoryByType(config.getRepositoryType());
        if(repository == null){
            throw new DeploymentSynchronizerException("No Repository found for type " + config.getRepositoryType());
        }

        List<RepositoryConfigParameter> parameters = repository.getParameters();

        //If repository specific configuration parameters are found.
        if(parameters != null && !parameters.isEmpty()){
            //Attach parameter list to config.
            config.setRepositoryConfigParameters(
                    parameters.toArray(new RepositoryConfigParameter[parameters.size()]));
        }

        return config;
    }

    /**
     * Returns the Carbon/Axis2 repository path for the given ConfigurationContext
     *
     * @param cfgCtx A ConfigurationContext instance owned by super tenant or some other tenant
     * @return Axis2 repository path from which the configuration is read
     */
    public static String getCarbonRepositoryFilePath(ConfigurationContext cfgCtx) {
        int tenantId = MultitenantUtils.getTenantId(cfgCtx);
        return MultitenantUtils.getAxis2RepositoryPath(tenantId);
    }

    /**
     * Checks whether deployment synchronizer is enabled for the Carbon repository of the
     * specified tenant. This method first checks whether a synchronizer configuration exists
     * in the registry (created by an admin service). If not it will try to get the configuration
     * from the Carbon ServerConfiguration.
     *
     * @param tenantId Tenant ID
     * @return true if deployment synchronizer is enabled for the repository
     * @throws org.wso2.carbon.deployment.synchronizer.DeploymentSynchronizerException if an error occurs while loading configuration from the registry
     */
    public static boolean isSynchronizerEnabled(int tenantId) throws DeploymentSynchronizerException {
        DeploymentSynchronizerConfiguration config = getActiveSynchronizerConfiguration(tenantId);
        return config.isEnabled();
    }

    /**
     * Loads the deployment synchronizer configuration from the configuration registry of the
     * specified tenant.
     *
     * @param tenantId Tenant ID
     * @return a DeploymentSynchronizerConfiguration object or null
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException if the registry cannot be accessed
     */
    public static DeploymentSynchronizerConfiguration getDeploymentSyncConfigurationFromRegistry(
            int tenantId) throws RegistryException {

        UserRegistry localRepository = getLocalRepository(tenantId);
        if (!localRepository.resourceExists(DeploymentSynchronizerConstants.CARBON_REPOSITORY)) {
            return null;
        }

        Resource resource = localRepository.get(DeploymentSynchronizerConstants.CARBON_REPOSITORY);
        DeploymentSynchronizerConfiguration config = new DeploymentSynchronizerConfiguration();
        String status = new String((byte[]) resource.getContent());
        if ("enabled".equals(status)) {
            config.setEnabled(true);
        }

        config.setAutoCheckout(Boolean.valueOf(resource.getProperty(
                DeploymentSynchronizerConstants.AUTO_CHECKOUT_MODE)));
        config.setAutoCommit(Boolean.valueOf(resource.getProperty(
                DeploymentSynchronizerConstants.AUTO_COMMIT_MODE)));
        config.setPeriod(Long.valueOf(resource.getProperty(
                DeploymentSynchronizerConstants.AUTO_SYNC_PERIOD)));
        config.setUseEventing(Boolean.valueOf(resource.getProperty(
                DeploymentSynchronizerConstants.USE_EVENTING)));
        config.setRepositoryType(resource.getProperty(
                DeploymentSynchronizerConstants.REPOSITORY_TYPE));

        ArtifactRepository repository =
                RepositoryReferenceHolder.getInstance().getRepositoryByType(config.getRepositoryType());
        if(repository == null){
            throw new RegistryException("No Repository found for type " + config.getRepositoryType());
        }

        List<RepositoryConfigParameter> parameters = repository.getParameters();

        //If repository specific configuration parameters are found.
        if(parameters != null){
            //Find the 'value' of each parameter from the registry by parameter 'name' and attach to parameter
            for(RepositoryConfigParameter parameter : parameters){
                parameter.setValue(resource.getProperty(parameter.getName()));
            }

            //Attach parameter list to config object.
            config.setRepositoryConfigParameters(parameters.toArray(new RepositoryConfigParameter[parameters.size()]));
        }

        resource.discard();
        return config;
    }

    /**
     * Save the given DeploymentSynchronizerConfiguration to the registry. The target
     * configuration registry space will be selected using the specified tenant ID. As a result
     * the configuration will be stored in the configuration registry of the specified
     * tenant.
     *
     * @param config The configuration to be saved
     * @param tenantId Tenant ID to select the configuration registry
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException if an error occurs while accessing the registry
     */
    public static void persistConfiguration(DeploymentSynchronizerConfiguration config,
                                            int tenantId) throws RegistryException {

        Resource resource;
        UserRegistry localRepository = getLocalRepository(tenantId);
        if (!localRepository.resourceExists(DeploymentSynchronizerConstants.CARBON_REPOSITORY)) {
            resource = localRepository.newResource();
        } else {
            resource = localRepository.get(DeploymentSynchronizerConstants.CARBON_REPOSITORY);
        }

        resource.setProperty(DeploymentSynchronizerConstants.AUTO_COMMIT_MODE,
                String.valueOf(config.isAutoCommit()));
        resource.setProperty(DeploymentSynchronizerConstants.AUTO_CHECKOUT_MODE,
                String.valueOf(config.isAutoCheckout()));
        resource.setProperty(DeploymentSynchronizerConstants.AUTO_SYNC_PERIOD,
                String.valueOf(config.getPeriod()));
        resource.setProperty(DeploymentSynchronizerConstants.USE_EVENTING,
                String.valueOf(config.isUseEventing()));
        resource.setProperty(DeploymentSynchronizerConstants.REPOSITORY_TYPE,
                config.getRepositoryType());
        resource.setContent(config.isEnabled() ? "enabled" : "disabled");

        //Get Repository specific configuration parameters from config object.
        RepositoryConfigParameter[] parameters = config.getRepositoryConfigParameters();

        if(parameters != null && parameters.length != 0){
            //Save each Repository specific configuration parameter in registry.
            for(int i=0; i<parameters.length; i++){
                resource.setProperty(parameters[i].getName(), parameters[i].getValue());
            }
        }

        localRepository.put(DeploymentSynchronizerConstants.CARBON_REPOSITORY, resource);
        resource.discard();
    }

    private static UserRegistry getLocalRepository(int tenantId) throws RegistryException {
        return ServiceReferenceHolder.getRegistryService().getLocalRepository(tenantId);
    }

    private static ArtifactRepository createArtifactRepository(
            String repositoryType) throws DeploymentSynchronizerException {

        ArtifactRepository artifactRepository = RepositoryReferenceHolder.getInstance().getRepositoryByType(repositoryType);
        if(artifactRepository == null){
            throw new DeploymentSynchronizerException("No Repository found for type " + repositoryType);
        }

        return artifactRepository;
    }

}
