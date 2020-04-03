/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.logging.view.ui;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.logging.view.ui.data.LogFileInfo;
import org.wso2.carbon.logging.view.ui.util.LoggingConstants;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

/**
 * This class will handle log file related operations.
 */
public class LogFileProvider {

    private static final Log log = LogFactory.getLog(LogFileProvider.class);
    private static final String SERVER_KEY = "ServerKey";
    private static final String APPLICATION_TYPE_ZIP = "application/zip";

    /**
     * This method will return the information of log files in repository/logs.
     *
     * @param tenantDomain - Tenant domain eg: t1.com.
     * @param serverKey    Server name.
     * @return Info list of log files.
     */
    public List<LogFileInfo> getLogFileInfoList(String tenantDomain, String serverKey) {

        String folderPath = CarbonUtils.getCarbonLogsPath();
        List<LogFileInfo> logs = new ArrayList<>();
        LogFileInfo logFileInfo;
        String currentServerName = getCurrentServerName();
        if ((((tenantDomain == null || "".equals(tenantDomain)) && isSuperTenantUser()) ||
                (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME
                        .equalsIgnoreCase(tenantDomain))) &&
                (serverKey == null || "".equals(serverKey) || serverKey.equalsIgnoreCase(
                        currentServerName))) {

            File folder = new File(folderPath);
            FileFilter fileFilter = new WildcardFileFilter(LoggingConstants.RegexPatterns.LOCAL_CARBON_LOG_PATTERN);
            File[] listOfFiles = folder.listFiles(fileFilter);
            // folder.listFiles consumes lot of  memory when there are many files in the folder
            // (> 10 000), can be fixed with nio support in Java7
            if (listOfFiles == null) {
                // folder.listFiles can return a null, in that case return a default log info
                if (log.isDebugEnabled()) {
                    log.debug("List of log files of the given pattern is null.");
                }
                return getDefaultLogInfoList();
            }
            for (File file : listOfFiles) {
                String filename = file.getName();
                if (!filename.contains("trace")) {
                    String filePath = CarbonUtils.getCarbonLogsPath() + LoggingConstants.URL_SEPARATOR + filename;
                    File logfile = new File(filePath);
                    if (filename.contains("-")) {
                        String fileDate = filename;
                        fileDate = fileDate.substring(LoggingConstants.RegexPatterns.LOG_FILE_DATE_SEPARATOR.length(),
                                fileDate.length() - ".log".length());
                        logFileInfo = new LogFileInfo(filename, fileDate, getFileSize(logfile));
                    } else {
                        logFileInfo = new LogFileInfo(filename, LoggingConstants.RegexPatterns.CURRENT_LOG,
                                getFileSize(logfile));
                    }
                    logs.add(logFileInfo);
                }
            }
        }
        return getSortedPerLogInfoList(logs);
    }

    // return default response.
    private List<LogFileInfo> getDefaultLogInfoList() {

        List<LogFileInfo> defaultLogFileInfoList = new ArrayList<>();
        defaultLogFileInfoList.add(new LogFileInfo("NO_LOG_FILES",
                "---", "---"));
        return defaultLogFileInfoList;
    }

    private String getCurrentServerName() {
        return ServerConfiguration.getInstance().getFirstProperty(SERVER_KEY);
    }

    private boolean isSuperTenantUser() {
        CarbonContext carbonContext = CarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        return tenantId == MultitenantConstants.SUPER_TENANT_ID;
    }

    private String getFileSize(File file) {
        long bytes = file.length();
        int unit = 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    private List<LogFileInfo> getSortedPerLogInfoList(List<LogFileInfo> logs) {
        if (logs == null || logs.isEmpty()) {
            return getDefaultLogInfoList();
        } else {
            Collections.sort(logs, new Comparator<Object>() {
                public int compare(Object o1, Object o2) {

                    LogFileInfo log1 = (LogFileInfo) o1;
                    LogFileInfo log2 = (LogFileInfo) o2;
                    return log1.getLogName().compareToIgnoreCase(log2.getLogName());
                }

            });
            return logs;
        }
    }

    private static InputStream getLocalInputStream(String logFile) throws FileNotFoundException, LogViewerException {
        Path logFilePath = Paths.get(CarbonUtils.getCarbonLogsPath(), logFile);
        if (!isPathInsideBaseDirectory(Paths.get(CarbonUtils.getCarbonLogsPath()), logFilePath)) {
            throw new LogViewerException("Specified log file path is outside carbon logs directory.");
        }
        return new BufferedInputStream(new FileInputStream(logFilePath.toString()));
    }

    /**
     * Tests if the provided path is inside the base directory path.
     *
     * @param baseDirPath absolute {@link Path} of the base directory in which we want to check whether the given path
     *                    is inside
     * @param path        {@link Path} to be tested
     * @return {@code true} if the given path is inside the base directory path, otherwise {@code false}
     */
    private static boolean isPathInsideBaseDirectory(Path baseDirPath, Path path) {

        Path resolvedPath = baseDirPath.resolve(path).normalize();
        return resolvedPath.startsWith(baseDirPath.normalize());
    }

    /**
     * This method will download a log file with the given name.
     * @param logFile      - File name which need to download, this should not be null.
     * @return  DataHandler whith input stream of the file.
     * @throws LogViewerException   Error occurred while reading the file.
     */
    public DataHandler downloadLogFile(String logFile) throws LogViewerException {

        InputStream is = null;
        ByteArrayDataSource bytArrayDS;
        try {
            is = getInputStream(logFile);
            bytArrayDS = new ByteArrayDataSource(is, APPLICATION_TYPE_ZIP);
            return new DataHandler(bytArrayDS);
        } catch (LogViewerException e) {
            log.error("Cannot read InputStream from the file " + logFile, e);
            throw e;
        } catch (IOException e) {
            String msg = "Cannot read file size from the " + logFile;
            log.error(msg, e);
            throw new LogViewerException(msg, e);
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    log.error("Error while closing inputStream of log file", e);
                }
            }
        }
    }

    private InputStream getInputStream(String logFile)
            throws LogViewerException {

        InputStream inputStream;
        try {
            if (isSuperTenantUser()) {
                inputStream = getLocalInputStream(logFile);
            } else {
                throw new LogViewerException("Syslog Properties are not properly configured");
            }
            return inputStream;
        } catch (Exception e) {
            // cannot catch a specific exception since RegistryManager.getSyslogConfig (which is
            // used in the call stack of getLogDataStream()) throws an exception
            throw new LogViewerException("Error getting the file input stream", e);
        }
    }

}
