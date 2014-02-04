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
package org.wso2.carbon.application.deployer.mashup;

import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.application.deployer.AppDeployerConstants;
import org.wso2.carbon.application.deployer.AppDeployerUtils;
import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.application.deployer.config.CappFile;
import org.wso2.carbon.application.deployer.handler.AppDeploymentHandler;
import org.wso2.carbon.application.deployer.mashup.internal.MashupAppDeployerDSComponent;

import java.io.File;
import java.util.List;
import java.util.Map;

public class MashupAppDeployer implements AppDeploymentHandler {

    private static final Log log = LogFactory.getLog(MashupAppDeployer.class);

    public static final String MASHUP_TYPE = "wso2/mashup";
    public static final String MASHUP_CONTEXT = "carbon";
    public static final String MASHUP_DIR = "jsservices";

    private Map<String, Boolean> acceptanceList = null;

    /**
     * Check the artifact type and if it is a Mashup, copy it to the Mashup deployment hot folder
     *
     * @param carbonApp  - CarbonApplication instance to check for Mashup artifacts
     * @param axisConfig - AxisConfiguration of the current tenant
     */
    public void deployArtifacts(CarbonApplication carbonApp, AxisConfiguration axisConfig)
            throws DeploymentException {
        List<Artifact.Dependency> artifacts =
                carbonApp.getAppConfig().getApplicationArtifact().getDependencies();

        for (Artifact.Dependency dep : artifacts) {
            Deployer deployer;
            Artifact artifact = dep.getArtifact();
            if (artifact == null) {
                continue;
            }

            String artifactName = artifact.getName();
            if (!isAccepted(artifact.getType())) {
                log.warn("Can't deploy artifact : " + artifactName + " of type : " +
                        artifact.getType() + ". Required features are not installed in the system");
                continue;
            }

            if (MASHUP_TYPE.equals(artifact.getType())) {
                deployer = AppDeployerUtils.getArtifactDeployer(axisConfig, MASHUP_DIR, "js");
            } else {
                continue;

            }

            List<CappFile> files = artifact.getFiles();
            if (files.size() != 1) {
                log.error("A Mashup must have a single file. But " +
                        files.size() + " files found.");
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
     * Check the artifact type and if it is a Gadget, delete the file from the Gadget deployment hot
     * folder
     *
     * @param carbonApp  - CarbonApplication instance to check for Gadget artifacts
     * @param axisConfig - AxisConfiguration of the current tenant
     */
    public void undeployArtifacts(CarbonApplication carbonApp, AxisConfiguration axisConfig) {

        List<Artifact.Dependency> artifacts =
                carbonApp.getAppConfig().getApplicationArtifact().getDependencies();

        for (Artifact.Dependency dep : artifacts) {
            Deployer deployer;
            Artifact artifact = dep.getArtifact();
            if (artifact == null) {
                continue;
            }

            if (MASHUP_TYPE.equals(artifact.getType())) {
                deployer = AppDeployerUtils.getArtifactDeployer(axisConfig, MASHUP_DIR, "js");
            } else {
                continue;
            }

            List<CappFile> files = artifact.getFiles();
            if (files.size() != 1) {
                log.error(
                        "A Mashup must have a single file. But " + files.size() + " files found.");
                continue;
            }

            if (deployer != null && AppDeployerConstants.DEPLOYMENT_STATUS_DEPLOYED.
                                equals(artifact.getDeploymentStatus())) {
                String fileName = artifact.getFiles().get(0).getName();
                String artifactPath = artifact.getExtractedPath() + File.separator + fileName;
                try {
                    deployer.undeploy(artifactPath);
                    artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_PENDING);
                    File artifactFile = new File(artifactPath);
                    if (artifactFile.exists() && !artifactFile.delete()) {
                        log.warn("Couldn't delete App artifact file : " + artifactPath);
                    }
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
     * installed in the system. Therefore, that type is accepted. If the type exists in the
     * acceptance list, the acceptance value is returned.
     *
     * @param serviceType - service type to be checked
     * @return true if all features are there or entry is null. else false
     */
    private boolean isAccepted(String serviceType) {
        if (acceptanceList == null) {
            acceptanceList = AppDeployerUtils
                    .buildAcceptanceList(MashupAppDeployerDSComponent.getRequiredFeatures());
        }
        Boolean acceptance = acceptanceList.get(serviceType);
        return (acceptance == null || acceptance);
    }

}
