/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.logging.service.internal;

import org.apache.axis2.clustering.ClusteringAgent;
import org.wso2.carbon.logging.service.RemoteLoggingConfigService;
import org.wso2.carbon.registry.core.service.RegistryService;

/**
 * Remote Logging Config Data Holder.
 */
public class RemoteLoggingConfigDataHolder {

    private static RemoteLoggingConfigDataHolder instance = new RemoteLoggingConfigDataHolder();
    private RegistryService registryService;
    private RemoteLoggingConfigService remoteLoggingConfigService;
    private ClusteringAgent clusteringAgent;

    private RemoteLoggingConfigDataHolder() {

    }

    public static RemoteLoggingConfigDataHolder getInstance() {

        return instance;
    }

    public RegistryService getRegistryService() {

        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {

        this.registryService = registryService;
    }

    public static void setInstance(RemoteLoggingConfigDataHolder instance) {

        RemoteLoggingConfigDataHolder.instance = instance;
    }

    public RemoteLoggingConfigService getRemoteLoggingConfigService() {

        return remoteLoggingConfigService;
    }

    public void setRemoteLoggingConfigService(
            RemoteLoggingConfigService remoteLoggingConfigService) {

        this.remoteLoggingConfigService = remoteLoggingConfigService;
    }

    public ClusteringAgent getClusteringAgent() {

        return clusteringAgent;
    }

    public void setClusteringAgent(ClusteringAgent clusteringAgent) {

        this.clusteringAgent = clusteringAgent;
    }
}
