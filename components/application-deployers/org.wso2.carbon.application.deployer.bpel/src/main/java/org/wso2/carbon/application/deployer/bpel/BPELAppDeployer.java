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
package org.wso2.carbon.application.deployer.bpel;

import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.application.deployer.AppDeployerConstants;
import org.wso2.carbon.application.deployer.AppDeployerUtils;
import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.application.deployer.bpel.internal.BPELAppDeployerDSComponent;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.application.deployer.config.CappFile;
import org.wso2.carbon.application.deployer.handler.AppDeploymentHandler;

import java.io.File;
import java.util.List;
import java.util.Map;

public class BPELAppDeployer implements AppDeploymentHandler {

    private static final Log log = LogFactory.getLog(BPELAppDeployer.class);

    public static final String BPEL_TYPE = "bpel/workflow";
    public static final String BPEL_DIR = "bpel";

    private Map<String, Boolean> acceptanceList = null;

    /**
     * Check the artifact type and if it is a BPEL, copy it to the BPEL deployment hot folder
     *
     * @param carbonApp  - CarbonApplication instance to check for BPEL artifacts
     * @param axisConfig - AxisConfiguration of the current tenant
     */
    public void deployArtifacts(CarbonApplication carbonApp, AxisConfiguration axisConfig)
            throws DeploymentException {
        List<Artifact.Dependency> artifacts = carbonApp.getAppConfig().getApplicationArtifact()
                .getDependencies();

        // loop through all artifacts
        for (Artifact.Dependency dep : artifacts) {
            Deployer deployer;
            Artifact artifact = dep.getArtifact();
            if (artifact == null) {
                continue;
            }

            if (!isAccepted(artifact.getType())) {
                log.warn("Can't deploy artifact : " + artifact.getName() + " of type : " +
                        artifact.getType() + ". Required features are not installed in the system");
                continue;
            }

            if (BPEL_TYPE.equals(artifact.getType())) {
                deployer =  AppDeployerUtils.getArtifactDeployer(axisConfig, BPEL_DIR, "zip");
            } else {
                continue;
            }

            List<CappFile> files = artifact.getFiles();
            if (files.size() != 1) {
                log.error("BPEL workflows must have a single file to " +
                        "be deployed. But " + files.size() + " files found.");
                continue;
            }
            if (deployer != null) {
                String fileName = artifact.getFiles().get(0).getName();
                String artifactPath = artifact.getExtractedPath() + File.separator + fileName;
                try {
                    deployer.deploy(new DeploymentFileData(new File(artifactPath), deployer));
                    artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_DEPLOYED);
                } catch (DeploymentException e) {
                    artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_FAILED);
                    throw e;
                }
            }
        }
    }

    /**
     * Check the artifact type and if it is a BPEL, delete the file from the BPEL
     * deployment hot folder
     *
     * @param carbonApp  - CarbonApplication instance to check for BPEL artifacts
     * @param axisConfig - - axisConfig of the current tenant
     */
    public void undeployArtifacts(CarbonApplication carbonApp, AxisConfiguration axisConfig) {

        List<Artifact.Dependency> artifacts = carbonApp.getAppConfig().getApplicationArtifact()
                .getDependencies();

        for (Artifact.Dependency dep : artifacts) {
            Deployer deployer;
            Artifact artifact = dep.getArtifact();
            if (artifact == null) {
                continue;
            }

            if (BPEL_TYPE.equals(artifact.getType())) {
                deployer = AppDeployerUtils.getArtifactDeployer(axisConfig, BPEL_DIR, "zip");
            } else {
                continue;
            }

            // loop through all dependencies
            List<CappFile> files = artifact.getFiles();
            if (files.size() != 1) {
                log.error("A BPEL workflow must have a single file. But " +
                        files.size() + " files found.");
                continue;
            }

            if (deployer != null && AppDeployerConstants.DEPLOYMENT_STATUS_DEPLOYED.
                                equals(artifact.getDeploymentStatus())) {
                String fileName = artifact.getFiles().get(0).getName();
                String artifactPath = artifact.getExtractedPath() + File.separator + fileName;
                try {
                    File artifactFile = new File(artifactPath);
                    if (artifactFile.exists() && !artifactFile.delete()) {
                        log.warn("Couldn't delete App artifact file : " + artifactPath);
                    }
                    deployer.undeploy(artifactPath);
                    artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_PENDING);
                } catch (DeploymentException e) {
                    artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_FAILED);
                    log.error("Error occured while trying to un deploy : "+artifact.getName());
                }
            }
        }
    }

    /**
     * Check whether a particular artifact type can be accepted for deployment. If the type doesn't
     * exist in the acceptance list, we assume that it doesn't require any special features to be
     * installed in the system. Therefore, that type is accepted.
     * If the type exists in the acceptance list, the acceptance value is returned.
     *
     * @param serviceType - service type to be checked
     * @return true if all features are there or entry is null. else false
     */
    private boolean isAccepted(String serviceType) {
        if (acceptanceList == null) {
            acceptanceList = AppDeployerUtils.buildAcceptanceList(BPELAppDeployerDSComponent
                    .getRequiredFeatures());
        }
        Boolean acceptance = acceptanceList.get(serviceType);
        return (acceptance == null || acceptance);
    }

}
