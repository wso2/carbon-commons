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
package org.wso2.carbon.application.mgt;


public class ApplicationMetadata {

    private String appName;
    private String appVersion;
    private ServiceGroupMetadata[] serviceGroups;
    private RegistryMetadata[] registryArtifacts;
    private String[] registryFilters;
    private String[] registryHandlers;
    private ArtifactDeploymentStatus[] artifactsDeploymentStatus;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppVersion(){
        return appVersion;
    }

    public void setAppVersion(String appVersion){
        this.appVersion = appVersion;
    }

    public ServiceGroupMetadata[] getServiceGroups() {
        return serviceGroups;
    }

    public void setServiceGroups(ServiceGroupMetadata[] serviceGroups) {
        this.serviceGroups = serviceGroups;
    }

    public String[] getRegistryFilters() {
        return registryFilters;
    }

    public void setRegistryFilters(String[] registryFilters) {
        this.registryFilters = registryFilters;
    }

    public String[] getRegistryHandlers() {
        return registryHandlers;
    }

    public void setRegistryHandlers(String[] registryHandlers) {
        this.registryHandlers = registryHandlers;
    }

    public RegistryMetadata[] getRegistryArtifacts() {
        return registryArtifacts;
    }

    public void setRegistryArtifacts(RegistryMetadata[] registryArtifacts) {
        this.registryArtifacts = registryArtifacts;
    }

    public ArtifactDeploymentStatus[] getArtifactsDeploymentStatus() {
        return artifactsDeploymentStatus;
    }

    public void setArtifactsDeploymentStatus(
            ArtifactDeploymentStatus[] artifactsDeploymentStatus) {
        this.artifactsDeploymentStatus = artifactsDeploymentStatus;
    }
}
