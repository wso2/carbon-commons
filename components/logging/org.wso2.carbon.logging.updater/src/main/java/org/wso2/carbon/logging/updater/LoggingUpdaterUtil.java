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

import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

/**
 * Utility class for LoggingUpdater
 */
public class LoggingUpdaterUtil {

    public static FileTime readModifiedTime() throws LoggingUpdaterException {

        String carbonConfigDirPath = CarbonUtils.getCarbonConfigDirPath();
        File log4j2File = new File(carbonConfigDirPath + File.separator + "log4j2.properties");
        try {
            BasicFileAttributes log4j2FileAttributes
                    = Files.getFileAttributeView(log4j2File.toPath(), BasicFileAttributeView.class).readAttributes();
            return log4j2FileAttributes.lastModifiedTime();
        } catch (IOException e) {
            throw new LoggingUpdaterException("Error while reading log4j2.properties", e);
        }
    }

}
