/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.logging.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.logging.service.config.LoggingConfigManager;
import org.wso2.carbon.logging.service.config.ServiceConfigManager;
import org.wso2.carbon.logging.service.data.LogEvent;
import org.wso2.carbon.logging.service.data.LogFileInfo;
import org.wso2.carbon.logging.service.data.LoggingConfig;
import org.wso2.carbon.logging.service.data.PaginatedLogEvent;
import org.wso2.carbon.logging.service.data.PaginatedLogFileInfo;
import org.wso2.carbon.logging.service.provider.api.LogFileProvider;
import org.wso2.carbon.logging.service.provider.api.LogProvider;
import org.wso2.carbon.logging.service.util.LoggingConstants;
import org.wso2.carbon.logging.service.util.LoggingUtil;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.DataPaginator;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.List;

/**
 * This is the Log Viewer service used for obtaining Log messages from pluggable LogProvider and
 * LogFileProvider implementation as configured in the
 * <CARBON_HOME>/repository/conf/etc/logging-config.xml file.
 *
 */
public class LogViewer {

    private static final Log log = LogFactory.getLog(LogViewer.class);
    private static final String LOGGING_CONFIG_FILE_WITH_PATH = CarbonUtils.getCarbonConfigDirPath()
                                                    + RegistryConstants.PATH_SEPARATOR
                                                    + LoggingConstants.ETC_DIR
                                                    + RegistryConstants.PATH_SEPARATOR
                                                    + LoggingConstants.LOGGING_CONF_FILE;
    private static LoggingConfig loggingConfig;
    private static LogFileProvider logFileProvider;
    private static LogProvider logProvider;

    // configured classes are loaded during LogViewer class load time
    // inside this static block.
    static {
        // load the configuration from the config file.
        loggingConfig = loadLoggingConfiguration();

        String lpClass = loggingConfig.getLogProviderImplClassName();
        loadLogProviderClass(lpClass);

        String lfpClass = loggingConfig.getLogFileProviderImplClassName();
        loadLogFileProviderClass(lfpClass);
    }

    /**
     * Load the LogProvider implementation as mentioned in the config file. This method is called
     * when this class is loaded. (Called within the static block)
     *
     * @param lpClass
     *         - Log Provider implementation class name
     */
    private static void loadLogProviderClass(String lpClass) {
        try {
            // initiate Log provider instance
            if (lpClass != null && !"".equals(lpClass)) {
                Class<?> logProviderClass = Class.forName(lpClass);
                Constructor<?> constructor = logProviderClass.getConstructor();
                logProvider = (LogProvider) constructor.newInstance();
                logProvider.init(loggingConfig);
            } else {
                String msg = "Log provider is not defined in logging configuration file : " +
                             LOGGING_CONFIG_FILE_WITH_PATH;
                throw new LoggingConfigReaderException(msg);
            }
        } catch (Exception e) {
            String msg = "Error while loading log provider implementation class: " + lpClass;
            log.error(msg, e);
            // A RuntimeException is thrown here since an Exception cannot be thrown from the static
            // block. An Exception occurs when the class could not be loaded. We cannot proceed
            // further in that case, therefore we throw a RuntimeException.
            throw new RuntimeException(msg, e);
        }
    }

    /**
     * Load the LogFileProvider implementation as mentioned in the config file. This method is
     * called when this class is loaded. (Called within the static block)
     *
     * @param lfpClass
     *         - Log File Provider implementation class name
     */
    private static void loadLogFileProviderClass(String lfpClass) {
        try {
            // initiate log file provider instance
            if (lfpClass != null && !"".equals(lfpClass)) {
                Class<?> logFileProviderClass = Class.forName(lfpClass);
                Constructor<?> constructor = logFileProviderClass.getConstructor();
                logFileProvider = (LogFileProvider) constructor.newInstance();
                logFileProvider.init(loggingConfig);
            } else {
                String msg = "Log file provider is not defined in logging configuration file : " +
                             LOGGING_CONFIG_FILE_WITH_PATH;
                throw new LoggingConfigReaderException(msg);
            }
        } catch (Exception e) {
            String msg = "Error while loading log file provider implementation class: " + lfpClass;
            log.error(msg, e);
            // A RuntimeException is thrown here since an Exception cannot be thrown from the static
            // block. An Exception occurs when the class could not be loaded. We cannot proceed
            // further in that case, therefore we throw a RuntimeException.
            throw new RuntimeException(msg, e);
        }
    }

    /**
     * Load logging configuration from the logging-config file. This method is called when this
     * class is loaded. (Called within the static block)
     *
     * @return - a LoggingConfig
     */
    private static LoggingConfig loadLoggingConfiguration() {
        try {
            return LoggingConfigManager.loadLoggingConfiguration(
                    LOGGING_CONFIG_FILE_WITH_PATH);
        } catch (IOException e) {
            String msg = "Error while reading the configuration file";
            log.error(msg, e);
            // We cannot proceed further without reading the logging config properly.
            // Therefore throw a runtime exception
            throw new RuntimeException(msg, e);
        } catch (XMLStreamException e) {
            String msg = "Error while parsing the configuration file";
            log.error(msg, e);
            // We cannot proceed further without reading the logging config properly.
            // Therefore throw a runtime exception
            throw new RuntimeException(msg, e);
        } catch (LoggingConfigReaderException e) {
            String msg = "Error while reading the configuration file";
            log.error(msg, e);
            // We cannot proceed further without reading the logging config properly.
            // Therefore throw a runtime exception
            throw new RuntimeException(msg, e);
        }
    }

    public PaginatedLogFileInfo getPaginatedLogFileInfo(int pageNumber, String tenantDomain,
                                                        String serviceName)
            throws LogViewerException {
        List<LogFileInfo> logFileInfoList = logFileProvider.getLogFileInfoList(tenantDomain,
                                                                               serviceName);
        return getPaginatedLogFileInfo(pageNumber, logFileInfoList);
    }

    public PaginatedLogFileInfo getLocalLogFiles(int pageNumber, String tenantDomain,
                                                 String serverKey) throws LogViewerException {
        List<LogFileInfo> logFileInfoList = logFileProvider
                .getLogFileInfoList(tenantDomain, serverKey);
        return getPaginatedLogFileInfo(pageNumber, logFileInfoList);
    }

    private PaginatedLogFileInfo getPaginatedLogFileInfo(int pageNumber,
                                                         List<LogFileInfo> logFileInfoList) {
        if (logFileInfoList != null && !logFileInfoList.isEmpty()) {
            PaginatedLogFileInfo paginatedLogFileInfo = new PaginatedLogFileInfo();
            DataPaginator.doPaging(pageNumber, logFileInfoList, paginatedLogFileInfo);
            return paginatedLogFileInfo;
        } else {
            return null;
        }
    }

    public DataHandler downloadArchivedLogFiles(String logFile, String tenantDomain,
                                                String serverKey)
            throws LogViewerException {
        return logFileProvider.downloadLogFile(logFile, tenantDomain, serverKey);
    }

    public boolean isValidTenantDomain(String tenantDomain) {
        return LoggingUtil.isValidTenantDomain(tenantDomain);
    }

    public String[] getServiceNames() throws LogViewerException {
        return ServiceConfigManager.getServiceNames();
    }


    public boolean isManager() {
        return LoggingUtil.isManager();
    }

    public boolean isValidTenant(String tenantDomain) {
        return LoggingUtil.isValidTenant(tenantDomain);
    }

    public int getLineNumbers(String logFile) throws Exception {
        return LoggingUtil.getLineNumbers(logFile);
    }

    public String[] getLogLinesFromFile(String logFile, int maxLogs, int start, int end)
            throws LogViewerException {
        return LoggingUtil.getLogLinesFromFile(logFile, maxLogs, start, end);
    }

    public String[] getApplicationNames(String tenantDomain, String serverKey)
            throws LogViewerException {
        List<String> appNameList = logProvider.getApplicationNames(tenantDomain, serverKey);
        return appNameList.toArray(new String[appNameList.size()]);
    }

    public boolean isFileAppenderConfiguredForST() {
        Logger rootLogger = Logger.getRootLogger();
        FileAppender logger = (FileAppender) rootLogger.getAppender("CARBON_LOGFILE");
        return logger != null
               && CarbonContext.getThreadLocalCarbonContext()
                               .getTenantId() == MultitenantConstants.SUPER_TENANT_ID;
    }

    public LogEvent[] getAllSystemLogs() throws LogViewerException {
        List<LogEvent> logEventList = logProvider.getSystemLogs();
        return logEventList.toArray(new LogEvent[logEventList.size()]);
    }

    public PaginatedLogEvent getPaginatedLogEvents(int pageNumber, String type, String keyword,
                                                   String tenantDomain, String serverKey)
            throws LogViewerException {

        List<LogEvent> logMsgList = logProvider
                .getLogs(type, keyword, null, tenantDomain, serverKey);
        return getPaginatedLogEvent(pageNumber, logMsgList);
    }

    public PaginatedLogEvent getPaginatedApplicationLogEvents(int pageNumber, String type,
                                                              String keyword,
                                                              String applicationName,
                                                              String tenantDomain, String serverKey)
            throws LogViewerException {
        List<LogEvent> logMsgList = logProvider
                .getLogs(type, keyword, applicationName, tenantDomain, serverKey);
        return getPaginatedLogEvent(pageNumber, logMsgList);
    }

    private PaginatedLogEvent getPaginatedLogEvent(int pageNumber, List<LogEvent> logMsgList) {
        if (logMsgList != null && !logMsgList.isEmpty()) {
            PaginatedLogEvent paginatedLogEvent = new PaginatedLogEvent();
            DataPaginator.doPaging(pageNumber, logMsgList, paginatedLogEvent);
            return paginatedLogEvent;
        } else {
            return null;
        }
    }

    public int getNoOfLogEvents(String tenantDomain, String serverKey) throws LogViewerException {
        return logProvider.logsCount(tenantDomain, serverKey);
    }

    public LogEvent[] getLogs(String type, String keyword, String tenantDomain,
                              String serverKey) throws LogViewerException {
        List<LogEvent> logEventList = logProvider
                .getLogs(type, keyword, null, tenantDomain, serverKey);
        return logEventList.toArray(new LogEvent[logEventList.size()]);
    }

    public LogEvent[] getApplicationLogs(String type, String keyword, String appName,
                                         String tenantDomain,
                                         String serverKey) throws LogViewerException {
        List<LogEvent> logEventList = logProvider
                .getLogs(type, keyword, appName, tenantDomain, serverKey);
        return logEventList.toArray(new LogEvent[logEventList.size()]);
    }

    public boolean clearLogs() {
        return logProvider.clearLogs();
    }
}
