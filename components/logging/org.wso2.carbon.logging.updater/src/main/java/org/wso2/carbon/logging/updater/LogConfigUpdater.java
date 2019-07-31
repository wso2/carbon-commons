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


package org.wso2.carbon.logging.updater;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.io.File;

public class LogConfigUpdater implements Runnable {

    static final Log LOG = LogFactory.getLog(LogConfigUpdater.class);

    private ConfigurationAdmin configurationAdmin;

    public LogConfigUpdater(ConfigurationAdmin configurationAdmin) {

        this.configurationAdmin = configurationAdmin;
    }

    @Override
    public void run() {

        String carbonConfigDirPath = CarbonUtils.getCarbonConfigDirPath();
        File log4j2File = new File(carbonConfigDirPath + File.separator + "log4j2.properties");
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            Path carbonConfigDirectory = Paths.get(carbonConfigDirPath);
            carbonConfigDirectory.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            WatchKey key;
            while ((key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent<Path> watchEvent = (WatchEvent<Path>) event;
                    Path fileName = watchEvent.context();
                    Path changedFile = carbonConfigDirectory.resolve(fileName);
                    if (changedFile.toFile().equals(log4j2File)) {
                        updateLoggingConfiguration();
                    }
                }
                key.reset();
            }
        } catch (IOException e) {
            LOG.error("Error while retrieving watch events", e);
        } catch (InterruptedException e) {
            LOG.error("Error while waiting to watch events", e);
        }

    }

    private void updateLoggingConfiguration() throws IOException {
        Configuration configuration = configurationAdmin.getConfiguration("org.ops4j.pax.logging");
        configuration.update();

    }
}
