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

import org.apache.catalina.Container;
import org.apache.catalina.core.StandardWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.application.deployer.AppDeployerUtils;
import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.application.deployer.webapp.WARCappDeployer;
import org.wso2.carbon.application.mgt.webapp.internal.WarAppServiceComponent;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.NetworkUtils;
import org.wso2.carbon.webapp.mgt.WebApplication;
import org.wso2.carbon.webapp.mgt.WebApplicationsHolder;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class WarApplicationAdmin extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(WarApplicationAdmin.class);

    public static final String STARTED = "Started";
    public static final String STOPPED = "Stopped";
    public static final String FAULTY = "Faulty";

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
        // metadata instance to return
        WarCappMetadata warCappMetadata = null;

        // get the webapp holder instance from config context
        WebApplicationsHolder holder = (WebApplicationsHolder) getConfigContext().
                        getProperty(CarbonConstants.WEB_APPLICATIONS_HOLDER);

        // first search in the started webapps
        String state = STARTED;
        WebApplication webApplication = holder.getStartedWebapps().get(fileName);

        // if it is null, try searching stopped webapps
        if (webApplication == null) {
            state = STOPPED;
            webApplication = holder.getStoppedWebapps().get(fileName);
        }

        // if it is still null, try searching faulty webapps
        if (webApplication == null) {
            state = FAULTY;
            webApplication = holder.getFaultyWebapps().get(fileName);
        }

        // if we could find a web app for the given file name, create the Metadata instance
        if (webApplication != null) {
            warCappMetadata = new WarCappMetadata();
            String appContext = webApplication.getContextName();
            for (Container container : webApplication.getContext().findChildren()) {
                if(((StandardWrapper) container).getServletClass().equals("org.apache.cxf.transport.servlet.CXFServlet")) {
                    appContext += (((StandardWrapper) container).findMappings())[0];
                }
            }
            if(appContext.endsWith("/*")) {
                appContext = appContext.substring(0, appContext.indexOf("/*"));
            }
            warCappMetadata.setContext(appContext);
            warCappMetadata.setState(state);
            warCappMetadata.setWebappFileName(webApplication.getWebappFile().getName());

            try {
                warCappMetadata.setHostName(NetworkUtils.getLocalHostname());
            } catch (SocketException e) {
                log.error("Error occurred while getting local hostname", e);
            }

            int httpPort = CarbonUtils.getTransportProxyPort(getConfigContext(), "http");
            if (httpPort == -1) {
                httpPort = CarbonUtils.getTransportPort(getConfigContext(), "http");
            }
            warCappMetadata.setHttpPort(httpPort);
        }
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
