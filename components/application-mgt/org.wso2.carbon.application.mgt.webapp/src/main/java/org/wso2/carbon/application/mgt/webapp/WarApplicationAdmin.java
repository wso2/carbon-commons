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

package org.wso2.carbon.application.mgt.webapp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.application.deployer.AppDeployerUtils;
import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.application.deployer.webapp.WARCappDeployer;
import org.wso2.carbon.application.mgt.webapp.internal.WarAppServiceComponent;
import org.wso2.carbon.core.AbstractAdmin;

import java.util.ArrayList;
import java.util.List;

public class WarApplicationAdmin extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(WarApplicationAdmin.class);

    /**
     * Gives a WarMetadata list which includes all web applications deployed through the
     * given Capp.
     *
     * @param appName - input app name
     * @return - WarMetadata array with found artifact info
     * @throws Exception - error on retrieving metadata
     */
    public WarCappMetadata[] getWarAppData(String appName) throws Exception {
        String tenantId = AppDeployerUtils.getTenantIdString(getAxisConfig());

        // Check whether there is an application in the system from the given name
        ArrayList<CarbonApplication> appList
                = WarAppServiceComponent.getAppManager().getCarbonApps(tenantId);
        CarbonApplication currentApplication = null;
        for (CarbonApplication application : appList) {
            if (appName.equals(application.getAppNameWithVersion())) {
                currentApplication = application;
                break;
            }
        }

        // If the app not found, throw an exception
        if (currentApplication == null) {
            String msg = "No Carbon Application found of the name : " + appName;
            log.error(msg);
            throw new Exception(msg);
        }

        // get all dependent artifacts of the cApp
        List<Artifact.Dependency> deps = currentApplication.getAppConfig().
                getApplicationArtifact().getDependencies();
        // package list to return
        List<WarCappMetadata> webappList = new ArrayList<WarCappMetadata>();

        for (Artifact.Dependency dep : deps) {
            Artifact artifact = dep.getArtifact();
            if (WARCappDeployer.WAR_TYPE.equals(artifact.getType())) {
                // war artifact can have only one file (a .war file). Try to find a webapp
                // which is already deployed and has the same file name
                webappList.add(getWebappMetadata(artifact.getFiles().get(0).getName()));
            }
        }
        // convert the List into an array and return
        return webappList.toArray(new WarCappMetadata[webappList.size()]);
    }

    /**
     * Search for a webapp which is deployed from the given file name
     *
     * @param fileName - .war file name
     * @return - if webapp found - WarCappMetadata instance, else null
     */
    private WarCappMetadata getWebappMetadata(String fileName) {
        // webapp metadata display of capp is deprecated.
        WarCappMetadata warCappMetadata = new WarCappMetadata();
        warCappMetadata.setWebappFileName(fileName);
        warCappMetadata.setContext(null);
        warCappMetadata.setState(null);
        warCappMetadata.setHostName(null);
        warCappMetadata.setHttpPort(-1);

        return warCappMetadata;
    }

    /**
     * Gives a list of WarCappMetadata which includes all the jaxws webapps deployed
     * through given C-App
     * @param appName - input app name
     * @return Array of WarCappMetadata with found jaxws webapps
     * @throws Exception - error on retrieving metadata
     */
    public WarCappMetadata[] getJaxWSWarAppData(String appName) throws Exception {
        String tenantId = AppDeployerUtils.getTenantIdString(getAxisConfig());

        // Check whether there is an application in the system from the given name
        ArrayList<CarbonApplication> appList
                = WarAppServiceComponent.getAppManager().getCarbonApps(tenantId);
        CarbonApplication currentApplication = null;
        for (CarbonApplication application : appList) {
            if (appName.equals(application.getAppNameWithVersion())) {
                currentApplication = application;
                break;
            }
        }

        // If the app not found, throw an exception
        if (currentApplication == null) {
            String msg = "No Carbon Application found of the name : " + appName;
            log.error(msg);
            throw new Exception(msg);
        }

        // get all dependent artifacts of the cApp
        List<Artifact.Dependency> deps = currentApplication.getAppConfig().
                getApplicationArtifact().getDependencies();
        // package list to return
        List<WarCappMetadata> webappList = new ArrayList<WarCappMetadata>();

        for (Artifact.Dependency dep : deps) {
            Artifact artifact = dep.getArtifact();
            if (WARCappDeployer.JAX_WAR_TYPE.equals(artifact.getType())) {
                webappList.add(getWebappMetadata(artifact.getFiles().get(0).getName()));
            }
        }
        // convert the List into an array and return
        return webappList.toArray(new WarCappMetadata[webappList.size()]);
    }

}
