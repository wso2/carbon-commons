/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.logging.updater.internal;

import org.osgi.service.cm.ConfigurationAdmin;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.logging.service.RemoteLoggingConfigService;

import java.nio.file.attribute.FileTime;
import java.util.concurrent.ScheduledExecutorService;

public class DataHolder {

    private ConfigurationAdmin configurationAdmin;
    private ScheduledExecutorService scheduledExecutorService;
    private ScheduledExecutorService remoteLoggingConfigUpdaterExecutorService;
    private RemoteLoggingConfigService remoteLoggingConfigService;
    private ServerConfigurationService serverConfigurationService;
    private static final DataHolder instance = new DataHolder();
    private FileTime modifiedTime;

    private DataHolder() {

    }

    public ConfigurationAdmin getConfigurationAdmin() {

        return configurationAdmin;
    }

    public static DataHolder getInstance() {

        return instance;
    }

    public void setConfigurationAdmin(ConfigurationAdmin configurationAdmin) {

        this.configurationAdmin = configurationAdmin;
    }

    public void setScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {

        this.scheduledExecutorService = scheduledExecutorService;
    }

    public ScheduledExecutorService getScheduledExecutorService() {

        return scheduledExecutorService;
    }

    public FileTime getModifiedTime() {

        return modifiedTime;
    }

    public void setModifiedTime(FileTime modifiedTime) {

        this.modifiedTime = modifiedTime;
    }

    public ScheduledExecutorService getRemoteLoggingConfigUpdaterExecutorService() {

        return remoteLoggingConfigUpdaterExecutorService;
    }

    public void setRemoteLoggingConfigUpdaterExecutorService(
            ScheduledExecutorService remoteLoggingConfigUpdaterExecutorService) {

        this.remoteLoggingConfigUpdaterExecutorService = remoteLoggingConfigUpdaterExecutorService;
    }

    public RemoteLoggingConfigService getRemoteLoggingConfigService() {

        return remoteLoggingConfigService;
    }

    public void setRemoteLoggingConfigService(
            RemoteLoggingConfigService remoteLoggingConfigService) {

        this.remoteLoggingConfigService = remoteLoggingConfigService;
    }

    public ServerConfigurationService getServerConfigurationService() {

        return serverConfigurationService;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {

        this.serverConfigurationService = serverConfigurationService;
    }
}
