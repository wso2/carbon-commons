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
package org.wso2.carbon.identity.user.store.configuration.deployer;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.core.common.UserStoreDeploymentManager;

/**
 * This is to deploy a new User Store Management Configuration file dropped or created at repository/deployment/server/userstores
 * or repository/tenant/<>tenantId</>/userstores. Whenever a new file with .xml extension is added/deleted or a modification is done to
 * an existing file, deployer will automatically update the existing realm configuration org.wso2.carbon.identity.user.store.configuration
 * according to the new file.
 */
public class UserStoreConfigurationDeployer extends AbstractDeployer {


    private static Log log = LogFactory.getLog(UserStoreConfigurationDeployer.class);


    private AxisConfiguration axisConfig;

    public void init(ConfigurationContext configurationContext) {
        log.info("User Store Configuration Deployer initiated.");
        this.axisConfig = configurationContext.getAxisConfiguration();

    }

    /**
     * Trigger deploying of new org.wso2.carbon.identity.user.store.configuration file
     *
     * @param deploymentFileData information about the user store org.wso2.carbon.identity.user.store.configuration
     * @throws org.apache.axis2.deployment.DeploymentException
     *          for any errors
     */
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        UserStoreDeploymentManager userStoreDeploymentManager = new UserStoreDeploymentManager();
        userStoreDeploymentManager.deploy(deploymentFileData.getAbsolutePath());


    }

    
    /**
     * Trigger un-deploying of a deployed file. Removes the deleted user store from chain
     *
     * @param fileName: domain name --> file name
     * @throws org.apache.axis2.deployment.DeploymentException
     *          for any errors
     */
    public void undeploy(String fileName) throws DeploymentException {
        UserStoreDeploymentManager userStoreDeploymentManager = new UserStoreDeploymentManager();
        userStoreDeploymentManager.undeploy(fileName);

    }


    public void setDirectory(String s) {

    }

    public void setExtension(String s) {

    }

}
