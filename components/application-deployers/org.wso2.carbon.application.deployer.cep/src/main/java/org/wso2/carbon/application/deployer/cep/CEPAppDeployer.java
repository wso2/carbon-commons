package org.wso2.carbon.application.deployer.cep;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.application.deployer.AppDeployerUtils;
import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.application.deployer.cep.internal.CEPAppDeployerDSComponent;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.application.deployer.config.CappFile;
import org.wso2.carbon.application.deployer.handler.AppDeploymentHandler;

import java.io.File;
import java.util.List;
import java.util.Map;


public class CEPAppDeployer implements AppDeploymentHandler {

    private static final Log log = LogFactory.getLog(CEPAppDeployer.class);

    public static final String CEP_TYPE = "cep/bucket";
    public static final String CEP_DIR = "cepbuckets";

    private Map<String, Boolean> acceptanceList = null;

    /**
     * Check the artifact type and if it is a cepservice, call the cepbucket Axis2 deployer
     *
     * @param carbonApp  - CarbonApplication instance to check for cep artifacts
     * @param axisConfig - AxisConfiguration of the current tenant
     */
    public void deployArtifacts(CarbonApplication carbonApp, AxisConfiguration axisConfig) {
        List<Artifact.Dependency> artifacts =
                carbonApp.getAppConfig().getApplicationArtifact().getDependencies();

        String repo = axisConfig.getRepository().getPath();

        String artifactPath, destPath;
        for (Artifact.Dependency dep : artifacts) {
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

            if (CEP_TYPE.equals(artifact.getType())) {
                destPath = repo + CEP_DIR;
            } else {
                continue;
            }

            List<CappFile> files = artifact.getFiles();
            if (files.size() != 1) {
                log.error("CEP must have a single file to " + "be deployed. But " + files.size() +
                        " files found.");
                continue;
            }
            String fileName = artifact.getFiles().get(0).getName();
            artifactPath = artifact.getExtractedPath() + File.separator + fileName;
             AppDeployerUtils.createDir(destPath);
            AppDeployerUtils.copyFile(artifactPath, destPath + File.separator + fileName);
        }
    }

    /**
     * Check the artifact type and if it is a cep, delete the file from the cep deployment hot
     * folder
     *
     * @param carbonApp  - CarbonApplication instance to check for cep artifacts
     * @param axisConfig - AxisConfiguration of the current tenant
     */
    public void undeployArtifacts(CarbonApplication carbonApp, AxisConfiguration axisConfig) {

        List<Artifact.Dependency> artifacts =
                carbonApp.getAppConfig().getApplicationArtifact().getDependencies();

        String repo = axisConfig.getRepository().getPath();
        String artifactPath, destPath;
        for (Artifact.Dependency dep : artifacts) {
            Artifact artifact = dep.getArtifact();
            if (artifact == null) {
                continue;
            }
            if (CEPAppDeployer.CEP_TYPE.equals(artifact.getType())) {
                destPath = repo + File.separator + CEPAppDeployer.CEP_DIR;
            } else {
                continue;
            }

            List<CappFile> files = artifact.getFiles();
            if (files.size() != 1) {
                log.error("A CEP must have a single file. But " + files.size() + " files found.");
                continue;
            }
            String fileName = artifact.getFiles().get(0).getName();
            artifactPath = destPath + File.separator + fileName;
             File artifactFile = new File(artifactPath);
            if (artifactFile.exists() && !artifactFile.delete()) {
                log.warn("Couldn't delete App artifact file : " + artifactPath);
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
            acceptanceList = AppDeployerUtils.buildAcceptanceList(CEPAppDeployerDSComponent.getRequiredFeatures());
        }
        Boolean acceptance = acceptanceList.get(serviceType);
        return (acceptance == null || acceptance);
    }

}
