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
package org.wso2.carbon.application.mgt.bpel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.application.deployer.AppDeployerUtils;
import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.application.deployer.bpel.BPELAppDeployer;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.application.mgt.bpel.internal.BPELAppMgtServiceComponent;
import org.wso2.carbon.bpel.core.ode.integration.mgt.services.BPELPackageManagementServiceSkeleton;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.LimitedProcessInfoType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.PackageType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.Version_type0;
import org.wso2.carbon.core.AbstractAdmin;

import java.util.ArrayList;
import java.util.List;

public class BPELApplicationAdmin extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(BPELApplicationAdmin.class);

    /**
     * Gives a BPELAppMetadata object with all bpel packages deployed through the
     * given app.
     *
     * @param appName - input app name
     * @return - BPELAppMetadata object with found artifact info
     * @throws Exception - error on retrieving metadata
     */
    public BPELAppMetadata getBPELAppData(String appName) throws Exception {
        BPELAppMetadata data = new BPELAppMetadata();
        String tenantId = AppDeployerUtils.getTenantIdString(getAxisConfig());

        // Check whether there is an application in the system from the given name
        ArrayList<CarbonApplication> appList
                = BPELAppMgtServiceComponent.getAppManager().getCarbonApps(tenantId);
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
        // we use the bpel backend admin service to get processes from a bpel package
        BPELPackageManagementServiceSkeleton bpelAdmin =
                new BPELPackageManagementServiceSkeleton();
        // package list to return
        List<PackageMetadata> packageList = new ArrayList<PackageMetadata>();

        String packageName;
        for (Artifact.Dependency dep : deps) {
            Artifact artifact = dep.getArtifact();
            packageName = artifact.getRuntimeObjectName();
            if (packageName == null) {
                continue;
            }
            if (BPELAppDeployer.BPEL_TYPE.equals(artifact.getType())) {
                PackageMetadata packageMetadata = new PackageMetadata();
                packageMetadata.setPackageName(packageName);

                // get the list of processes
                List<String> processList = new ArrayList<String>();
                PackageType packageType = bpelAdmin.listProcessesInPackage(packageName);
                for (Version_type0 packageVersion : packageType.getVersions().getVersion()) {
                    if (packageVersion.getName().equals(packageName)) {
                        for (LimitedProcessInfoType process :
                                packageVersion.getProcesses().getProcess()) {
                            processList.add(process.getPid());
                        }
                    }
                }
                String[] processes = new String[processList.size()];
                packageMetadata.setProcessList(processes);

                packageList.add(packageMetadata);
            }
        }
        // convert the List into an array
        data.setPackages(packageList.toArray(new PackageMetadata[packageList.size()]));
        return data;
    }

}
