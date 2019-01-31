/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.application.mgt;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.application.deployer.AppDeployerConstants;
import org.wso2.carbon.application.deployer.AppDeployerUtils;
import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.application.deployer.config.RegistryConfig;
import org.wso2.carbon.application.deployer.handler.DefaultAppDeployer;
import org.wso2.carbon.application.deployer.handler.RegistryResourceDeployer;
import org.wso2.carbon.application.deployer.persistence.CarbonAppPersistenceManager;
import org.wso2.carbon.application.mgt.internal.AppManagementServiceComponent;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.util.SystemFilter;
import org.wso2.carbon.utils.ServerConstants;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public class ApplicationAdmin extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(ApplicationAdmin.class);

    /**
     * Give the names of all applications in the system
     * @return - names array
     * @throws Exception - error on getting carbon app service
     */
    public String[] listAllApplications() throws Exception {
        String tenantId = AppDeployerUtils.getTenantIdString(getAxisConfig());
        ArrayList<CarbonApplication> appList
                = AppManagementServiceComponent.getAppManager().getCarbonApps(tenantId);
        List<String> existingApps = new ArrayList<String>();
        CarbonApplication tempApp;
        for (CarbonApplication anAppList : appList) {
            tempApp = anAppList;
            if (tempApp.isDeploymentCompleted()) {
                existingApps.add(tempApp.getAppNameWithVersion());
            }
        }
        return existingApps.toArray(new String[existingApps.size()]);
    }

    /**
     * Give the names of all faulty applications in the system
     * @return - names array
     * @throws Exception - error on getting carbon app service
     */
    public String[] listAllFaultyApplications() throws Exception {

        String tenantId = AppDeployerUtils.getTenantIdString(getAxisConfig());
        HashMap<String, Exception> faultyCarbonApps
                = AppManagementServiceComponent.getAppManager().getFaultyCarbonApps(tenantId);
        String fileName = null;
        List<String> existingFaultyApps = new ArrayList<String>();

        for (String anAppList : faultyCarbonApps.keySet()) {
            fileName = anAppList.substring(anAppList.lastIndexOf('/') + 1);
            existingFaultyApps.add(fileName);
        }
        return existingFaultyApps.toArray(new String[existingFaultyApps.size()]);

    }


    /**
     * Gives Fault Reason of the given Faulty Carbon Application
     *
     * @param faultCarbonAppName - name of the faulty Application
     * @throws Exception - error on getting carbon app service
     */
    public String getFaultException(String faultCarbonAppName) throws Exception {
        String tenantId = AppDeployerUtils.getTenantIdString(getAxisConfig());
        HashMap<String, Exception> appList
                = AppManagementServiceComponent.getAppManager().getFaultyCarbonApps(tenantId);

        String faultException = null;

        String fileName = null;
        for (String anAppList : appList.keySet()) {
            fileName = anAppList.substring(anAppList.lastIndexOf('/') + 1);

            if(faultCarbonAppName .equals(fileName)){
                Exception e = appList.get(anAppList);
                StringWriter errors = new StringWriter();
                e.printStackTrace(new PrintWriter(errors));
                faultException = errors.toString();
                break;
            }
        }
        return faultException;

    }

    /**
     * Deletes an entire application by deleting all its artifacts
     *
     * @param appName - name of the Application to be deleted
     * @throws Exception - invalid scenarios
     */
    public void deleteApplication(String appName) throws Exception {
        // If appName is null throw an exception
        if (appName == null) {
            handleException("Application name can't be null");
            return;
        }

        // CarbonApplication instance to delete
        CarbonApplication currentApp = null;

        // Iterate all applications for this tenant and find the application to delete
        String tenantId = AppDeployerUtils.getTenantIdString(getAxisConfig());
        ArrayList<CarbonApplication> appList =
                AppManagementServiceComponent.getAppManager().getCarbonApps(tenantId);
        for (CarbonApplication carbonApp : appList) {
            if (appName.equals(carbonApp.getAppName()) || appName.equals(carbonApp.getAppNameWithVersion())) {
                currentApp = carbonApp;
            }
        }

        // If requested application not found, throw an exception
        if (currentApp == null) {
            // Deleting the application on faulty application list, in case the application not found
            // in the active application list
            try {
                deleteFaultyApplication(new String[]{appName});
            } catch (Exception e) {
                handleException("No Carbon Application found of the name : " + appName);
            }
            return;
        }

        // Remove the app artifact file from repository, cApp hot undeployer will do the rest
        String appFilePath = currentApp.getAppFilePath();
        File file = new File(appFilePath);
        if (file.exists() && !file.delete()) {
            log.error("Artifact file couldn't be deleted for Application : "
                    + currentApp.getAppNameWithVersion());
        }
    }
    /**
     * Deletes an entire application by deleting all its artifacts
     *
     * @param faultyAppName - name of the Application to be deleted
     * @throws Exception - error on getting carbon app service
     */
    public void deleteFaultyApplication(String[] faultyAppName) throws Exception {
        String tenantId = AppDeployerUtils.getTenantIdString(getAxisConfig());
        HashMap<String, Exception> faultyCarbonAppList =
                AppManagementServiceComponent.getAppManager().getFaultyCarbonApps(tenantId);
        for(String faultyCarbonApplication : faultyAppName){
//        If appName is null throw an exception
            if (faultyCarbonApplication  == null) {
                handleException("Application name can't be null");
                return;
            }
            // CarbonApplication instance to delete
            String currentApp=null;
            String filename =null;

            // Iterate all applications for this tenant and find the application to delete
            for (String carbonApp : faultyCarbonAppList.keySet()) {

                filename = carbonApp.substring(carbonApp.lastIndexOf('/') + 1);
                if (faultyCarbonApplication .equals(filename) || faultyCarbonApplication .equals(filename.substring(0, filename.lastIndexOf('_')))) {
                    currentApp = carbonApp;
                    faultyCarbonAppList.remove(currentApp);
                    break;
                }
            }

            // If requested application not found, throw an exception
            if (currentApp == null) {
                handleException("No Carbon Application found of the name : " + faultyCarbonApplication );
                return;
            }

            // Remove the app artifact file from repository, cApp hot undeployer will do the rest
            String appFilePath = currentApp;
            File file = new File(appFilePath);
            if (file.exists() && !file.delete()) {
                log.error("Artifact file couldn't be deleted for Application : "
                          + filename);
            }
        }
    }
    public void deleteAllFaultyAppliations() throws Exception{
        String tenantId = AppDeployerUtils.getTenantIdString(getAxisConfig());
        HashMap<String, Exception> faultyCarbonAppList =
                AppManagementServiceComponent.getAppManager().getFaultyCarbonApps(tenantId);
        // CarbonApplication instance to delete
        String currentApp=null;
        String filename =null;


        // Iterate all applications for this tenant and find the application to delete
        for (String carbonApp : faultyCarbonAppList.keySet()) {
            //        If appName is null throw an exception
            if (carbonApp  == null) {
                handleException("Application name can't be null");
                return;
            }

            filename = carbonApp.substring(carbonApp.lastIndexOf('/') + 1);
        }


    }

    /**
     * Mark an application to be redeployed again in the next deployment pass.
     *
     * @param appPName - input app name
     * @throws Exception - error on redeploying app file
     */
    public void redeployApplication(String appName) throws Exception {
        String filePath = this.getCappFilepath(appName);

        // Reset the last modified time to trigger a redeployment of the file
        File cappFile = new File(filePath);
        cappFile.setLastModified(System.currentTimeMillis());
    }

    /**
     * Gives an ApplicationMetadata object with axis2 artifacts deployed through the given app.
     *
     * @param appName - input app name
     * @return - ApplicationMetadata object with found artifact info
     * @throws Exception - error on retrieving metadata
     */
    public ApplicationMetadata getAppData(String appName) throws Exception {
        // If appName is null throw an exception
        if (appName == null) {
            handleException("Application name can't be null");
            return null;
        }

        ApplicationMetadata appData = new ApplicationMetadata();
        CarbonApplication currentApplication = null;

        // Check whether there is an application in the system from the given name
        String tenantId = AppDeployerUtils.getTenantIdString(getAxisConfig());
        ArrayList<CarbonApplication> appList
                = AppManagementServiceComponent.getAppManager().getCarbonApps(tenantId);
        for (CarbonApplication application : appList) {
            if (appName.equals(application.getAppNameWithVersion())) {
                appData.setAppName(application.getAppName());
                currentApplication = application;
                break;
            }
        }

        // If the app not found, throw an exception
        if (currentApplication == null) {
            handleException("No Carbon Application found of the name : " + appName);
            return null;
        }

        String appVersion = currentApplication.getAppVersion();
        if (appVersion != null) {
            appData.setAppVersion(appVersion);
        }

        // list of service groups which are owned by this app
        List<ServiceGroupMetadata> serviceGroups = new ArrayList<ServiceGroupMetadata>();
        // list of registry artifacts
        List<RegistryMetadata> regArtifacts = new ArrayList<RegistryMetadata>();

        List<ArtifactDeploymentStatus> artifactDeploymentStatusList = new ArrayList<ArtifactDeploymentStatus>();

        List<String> regFilterList = new ArrayList<String>();
        List<String> regHandlerList = new ArrayList<String>();

        List<Artifact.Dependency> dependencies = currentApplication.getAppConfig().
                getApplicationArtifact().getDependencies();

        for (Artifact.Dependency dependency : dependencies) {
            Artifact artifact = dependency.getArtifact();
            String type = artifact.getType();
            String instanceName = artifact.getRuntimeObjectName();

            if (DefaultAppDeployer.AAR_TYPE.equals(type) ||
                    DefaultAppDeployer.DS_TYPE.equals(type)) {

                AxisServiceGroup sg;
                if (instanceName == null) {
                    sg = findServiceGroupForArtifact(artifact);
                    if (sg != null) {
                        // set the instance name in Artifact so that we don't have to find it
                        // next time
                        artifact.setRuntimeObjectName(sg.getServiceGroupName());
                    }
                } else {
                    sg = getAxisConfig().getServiceGroup(instanceName);
                }

                if (sg == null) {
                    continue;
                }
                // set the service group name
                ServiceGroupMetadata sgMetadata = new ServiceGroupMetadata();
                sgMetadata.setSgName(sg.getServiceGroupName());
                sgMetadata.setSgType(type);

                // find services in the service group
                List<String> services = new ArrayList<String>();
                for (Iterator serviceIter = sg.getServices(); serviceIter.hasNext();) {
                    AxisService axisService = (AxisService) serviceIter.next();
                    // ignore if this is a client side serivce
                    if (axisService.isClientSide()) {
                        break;
                    }
                    services.add(axisService.getName());
                }
                sgMetadata.setServices(services.toArray(new String[services.size()]));
                serviceGroups.add(sgMetadata);

            } else if (RegistryResourceDeployer.REGISTRY_RESOURCE_TYPE.equals(type)) {
                // Create a Registry config metadata instance
                RegistryConfig regConf = artifact.getRegConfig();
                if (regConf == null) {
                    regConf = readRegConfig(currentApplication.getAppName(), artifact.getName());
                }
                if (regConf == null) {
                    continue;
                }
                artifact.setRegConfig(regConf);
                RegistryMetadata regMeta = new RegistryMetadata();
                regMeta.setArtifactName(artifact.getName());

                List<String> resources = new ArrayList<String>();
                List<String> dumps = new ArrayList<String>();
                List<String> collections = new ArrayList<String>();
                List<Association> associations = new ArrayList<Association>();

                // add resources
                for (RegistryConfig.Resourse resourse : regConf.getResources()) {
                    resources.add(resourse.getPath() + "/" + resourse.getFileName());
                }

                // add dumps
                for (RegistryConfig.Dump dump : regConf.getDumps()) {
                    dumps.add(dump.getPath());
                }

                // add collections
                for (RegistryConfig.Collection collection : regConf.getCollections()) {
                    collections.add(collection.getPath());
                }
                // add associations
                for (RegistryConfig.Association association : regConf.getAssociations()) {
                    Association assoMeta = new Association(association.getSourcePath(),
                            association.getTargetPath());
                    associations.add(assoMeta);
                }

                if (resources.size() == 0 && dumps.size() == 0 && collections.size() == 0
                        && associations.size() == 0) {
                    continue;
                }
                regMeta.setResources(resources.toArray(new String[resources.size()]));
                regMeta.setDumps(dumps.toArray(new String[dumps.size()]));
                regMeta.setCollections(collections.toArray(new String[collections.size()]));
                regMeta.setAssociations(associations.toArray(new Association[associations.size()]));
                regArtifacts.add(regMeta);

            } else if (RegistryResourceDeployer.REGISTRY_FILTER_TYPE.equals(type)) {
                regFilterList.add(instanceName);
            } else if (RegistryResourceDeployer.REGISTRY_HANDLER_TYPE.equals(type)) {
                regHandlerList.add(instanceName);
            }

            ArtifactDeploymentStatus artifactDeploymentStatus = new ArtifactDeploymentStatus();
            artifactDeploymentStatus.setArtifactName(artifact.getName());
            if (artifact.getDeploymentStatus() != null) {
                artifactDeploymentStatus.setDeploymentStatus(artifact.getDeploymentStatus());
            }
            artifactDeploymentStatusList.add(artifactDeploymentStatus);
        }

        // Set found services in the appData object
        appData.setServiceGroups(serviceGroups
                                         .toArray(new ServiceGroupMetadata[serviceGroups.size()]));
        appData.setRegistryFilters(regFilterList.toArray(new String[regFilterList.size()]));
        appData.setRegistryHandlers(regHandlerList.toArray(new String[regHandlerList.size()]));
        appData.setRegistryArtifacts(regArtifacts
                .toArray(new RegistryMetadata[regArtifacts.size()]));
        appData.setArtifactsDeploymentStatus(artifactDeploymentStatusList.
                toArray(new ArtifactDeploymentStatus[artifactDeploymentStatusList.size()]));

        return appData;
    }

    /**
     * Finds the AxisServiceGroup which corresponds to the given cApp Artifact. Artifact file
     * name is used to identify the AxisServiceGroup. Then the service type is also checked with
     * the type of the given artifact.
     *
     * @param artifact - cApp artifact
     * @return - corresponding AxisServiceGroup
     */
    private AxisServiceGroup findServiceGroupForArtifact(Artifact artifact) {
        // Number of files in a service artifact should be 1
        if (artifact.getFiles().size() != 1) {
            return null;
        }

        String fileName = artifact.getFiles().get(0).getName();
        AxisConfiguration axisConfiguration = getAxisConfig();
        Iterator<AxisServiceGroup> serviceGroups = axisConfiguration.getServiceGroups();

        while (serviceGroups.hasNext()) {
            AxisServiceGroup sg = serviceGroups.next();

            // Filtering the admin services
            if (SystemFilter.isFilteredOutService(sg)) {
                continue;  // No advancement of currentIndex
            }

            AxisService axisService = null;
            Iterator<AxisService> services = sg.getServices();

            // Find a service with the file name in this service group
            while (services.hasNext()) {
                AxisService temp = services.next();
                if (temp.getFileName() != null) {
                    axisService = temp;
                    break;
                }
            }
            if (axisService != null) {
                String filePath = axisService.getFileName().getPath().trim();
                if (filePath.endsWith(fileName)) {
                    String serviceType = getArtifactTypeFromService(axisService, fileName);
                    if (serviceType.equals(artifact.getType())) {
                        return sg;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Finds the artifact type of the given service. Service type parameter is used to check this.
     *
     * @param service - AxisService instance
     * @param fileName - file name of the service artifact
     * @return - Service type
     */
    private String getArtifactTypeFromService(AxisService service, String fileName) {
        String artifactType = null;

        Parameter serviceTypeParam = service.getParameter(ServerConstants.SERVICE_TYPE);
        String serviceType;
        if (serviceTypeParam != null) {
            serviceType = (String) serviceTypeParam.getValue();
        } else {
            if (fileName.endsWith(".jar")) {
                serviceType = "jaxws";
            } else {
                serviceType = "axis2";
            }
        }

        if (serviceType.equals("axis2")) {
            artifactType = DefaultAppDeployer.AAR_TYPE;
        } else if (serviceType.equals("data_service")) {
            artifactType = DefaultAppDeployer.DS_TYPE;
        }
        return artifactType;
    }

    private RegistryConfig readRegConfig(String parentAppName, String artifactName) {
        RegistryConfig regConfig = null;
        try {
            CarbonAppPersistenceManager capm = new CarbonAppPersistenceManager(getAxisConfig());
            regConfig = capm.loadRegistryConfig(AppDeployerConstants.APPLICATIONS + parentAppName +
                                    AppDeployerConstants.APP_DEPENDENCIES + artifactName);
        } catch (Exception e) {
            log.error("Error while trying to load registry config for C-App : " +
                    parentAppName + " artifact : " + artifactName, e);
        }
        return regConfig;
    }

    /**
     * Used to download a carbon application archive. 
     * @param fileName the name of the application archive (.car) to be downloaded
     * @return datahandler corresponding to the .car file to be downloaded
     * @throws Exception for invalid scenarios 
     */
    public DataHandler downloadCappArchive(String fileName) throws Exception {
        String filePath = this.getCappFilepath(fileName);

        // Get a handle to the Capp file
        FileDataSource datasource = new FileDataSource(new File(filePath));
        DataHandler handler = new DataHandler(datasource);

        return handler;
    }

    private String getCappFilepath(String appName) throws Exception {
        String filePath = null;
        String tenantId = AppDeployerUtils.getTenantIdString(getAxisConfig());

        // Validate if there's a filename
        if (appName == null) {
            handleException("The app name must be specified (the value was null)");
        }

        // Iterate all applications for this tenant and find the application to download
        ArrayList<CarbonApplication> appList =
                AppManagementServiceComponent.getAppManager().getCarbonApps(tenantId);
        for (CarbonApplication carbonApp : appList) {
            if (appName.equals(carbonApp.getAppNameWithVersion())) {
                filePath = carbonApp.getAppFilePath();
                break;
            }
        }

        // Application not found in standard carbon apps, check if the app is faulty
        HashMap<String, Exception> faultyCarbonAppsList
                = AppManagementServiceComponent.getAppManager().getFaultyCarbonApps(tenantId);
        for (String faultyCarbonApp : faultyCarbonAppsList.keySet()) {
            String faultyFileName = faultyCarbonApp.substring(faultyCarbonApp.lastIndexOf('/') + 1);
            if (appName.equals(faultyFileName) || appName.equals(faultyFileName.substring(0, faultyFileName.lastIndexOf('_')))) {
                filePath = faultyCarbonApp;
                break;
            }
        }

        // Check if the app has been found
        if (filePath == null) {
            handleException("The application '" + appName + "' cannot be found");
        }

        return filePath;
    }

    private void handleException(String msg) throws CarbonException {
        log.error(msg);
        throw new CarbonException(msg);
    }
}
