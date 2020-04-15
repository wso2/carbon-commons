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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.logging.view.stub.LogViewerStub;
import org.wso2.carbon.logging.view.ui.data.LogEvent;
import org.wso2.carbon.logging.view.ui.data.LogFileInfo;
import org.wso2.carbon.logging.view.ui.data.PaginatedLogEvent;
import org.wso2.carbon.logging.view.ui.data.PaginatedLogFileInfo;
import org.wso2.carbon.logging.view.ui.util.LoggingConstants;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.DataPaginator;

/**
 * This class serve requests coming from log view JSP pages.
 */
public class LogViewerClient {

    private static final Log log = LogFactory.getLog(LogViewerClient.class);
    private static LogFileProvider logFileProvider = new LogFileProvider();
    public LogViewerStub stub;

    public LogViewerClient(String cookie, String backendServerURL, ConfigurationContext configCtx)
            throws AxisFault {

        String serviceURL = backendServerURL + "LogViewer";
        stub = new LogViewerStub(configCtx, serviceURL);
    }

    /**
     * Clear all the logs in the buffer.
     */
    public void clearLogs() {

        try {
            stub.clearLogs();
        } catch (RemoteException e) {
            String msg = "Error occurred while getting logger data. Backend service may be unavailable";
            log.error(msg, e);
        }
    }

    /**
     * Take all the log events from buffer for the current tenant id.
     *
     * @return List of log events.
     */
    public List<LogEvent> getAllLogs() {

        String currTenantId = String.valueOf(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
        ArrayList<LogEvent> eventArr;
        try {
            org.wso2.carbon.logging.view.data.xsd.LogEvent[] events = stub.getAllSystemLogs();
            eventArr = new ArrayList<>();
            // Converting from STUB logEvent to local logEvent
            for (org.wso2.carbon.logging.view.data.xsd.LogEvent event : events) {
                if (currTenantId.equals(event.getTenantId())) {
                    LogEvent temp = new LogEvent();
                    temp.setMessage(event.getMessage());
                    temp.setPriority(event.getPriority());
                    temp.setLogTime(event.getLogTime());
                    temp.setAppName(event.getAppName());
                    temp.setInstance(event.getInstance());
                    temp.setIp(event.getIp());
                    temp.setKey(event.getKey());
                    temp.setLogger(event.getLogger());
                    temp.setServerName(event.getServerName());
                    temp.setStacktrace(event.getStacktrace());
                    temp.setTenantId(event.getTenantId());
                    eventArr.add(temp);
                }
            }
        } catch (RemoteException e) {
            log.error("Error occured while receiving logs", e);
            return null;
        }
        // order from new logs to old logs
        Collections.reverse(eventArr);
        return eventArr;
    }

    /**
     * This method will return tha application names can be found in logs.
     *
     * @return List of application names.
     */
    public String[] getApplicationNames() {

        List<String> appList = new ArrayList<>();
        List<LogEvent> allLogs = getAllLogs();
        for (LogEvent event : allLogs) {
            if (event.getAppName() != null && !"".equals(event.getAppName())
                    && !"NA".equals(event.getAppName())
                    && !appList.contains(event.getAppName())
                    && !"STRATOS_ROOT".equals(event.getAppName())) {
                appList.add(event.getAppName());
            }
        }
        appList = getSortedApplicationNames(appList);
        return appList.toArray(new String[appList.size()]);
    }

    /**
     * This method will return logs filtered by a given application name.
     *
     * @param pageNumber      pagination page number.
     * @param type            log level.
     * @param keyword         search keyword.
     * @param applicationName application name.
     * @return logEvent consist of logs filtered by an Application name.
     */
    public PaginatedLogEvent getPaginatedApplicationLogEvents(int pageNumber, String type, String keyword,
                                                              String applicationName) {

        List<LogEvent> logMsgList = getAllLogs();
        logMsgList = filterLogs(logMsgList, type, keyword);
        if (applicationName != null && !applicationName.isEmpty()) {
            List<LogEvent> result = new ArrayList<>();
            for (LogEvent event : logMsgList) {
                if (applicationName.equals(event.getAppName())) {
                    result.add(event);
                }
            }
            return getPaginatedLogEvent(pageNumber, result);
        }
        return null;
    }

    // Sort the application names
    private List<String> getSortedApplicationNames(List<String> applicationNames) {

        Collections.sort(applicationNames, new Comparator<String>() {
            public int compare(String s1, String s2) {

                return s1.toLowerCase().compareTo(s2.toLowerCase());
            }

        });
        return applicationNames;
    }

    /**
     * This method will return the matching image url for a given log level.
     *
     * @param type Log level.
     * @return matching image location.
     */
    public String getImageName(String type) {

        if (type.equals("INFO")) {
            return "images/information.gif";
        } else if (type.equals("ERROR")) {
            return "images/error.png";
        } else if (type.equals("WARN")) {
            return "images/warn.png";
        } else if (type.equals("DEBUG")) {
            return "images/debug.png";
        } else if (type.equals("TRACE")) {
            return "images/trace.png";
        } else if (type.equals("FATAL")) {
            return "images/fatal.png";
        }
        return "";
    }

    /**
     * Check whether the current tenant is valid.
     *
     * @return validity of the current tenant.
     */
    public static boolean isValidTenant() {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        return tenantId != org.wso2.carbon.base.MultitenantConstants.INVALID_TENANT_ID;
    }

    /**
     * Get paginated log events filtered by type and search keyword.
     *
     * @param pageNumber pagination page number.
     * @param type       log level.
     * @param keyword    search keyword.
     * @return paginated log events.
     */
    public PaginatedLogEvent getPaginatedLogEvents(int pageNumber, String type, String keyword) {

        List<LogEvent> logMsgList = getAllLogs();
        List<LogEvent> result = filterLogs(logMsgList, type, keyword);
        return getPaginatedLogEvent(pageNumber, result);
    }

    // Filter given set of logs by log level and search keyword
    private List<LogEvent> filterLogs(List<LogEvent> logMsgList, String type, String keyword) {

        List<LogEvent> filteredByLevel = new ArrayList<>();
        if (type != null && !type.isEmpty() && !"ALL".equals(type)) {
            for (LogEvent event : logMsgList) {
                if (event.getPriority().equals(type)) {
                    filteredByLevel.add(event);
                }
            }
        } else {
            filteredByLevel = logMsgList;
        }
        List<LogEvent> filteredByKey = new ArrayList<>();
        if (keyword != null && !keyword.isEmpty()) {
            for (LogEvent event : filteredByLevel) {
                if (event.getMessage().contains(keyword)) {
                    filteredByKey.add(event);
                }
            }
        } else {
            filteredByKey = filteredByLevel;
        }
        return filteredByKey;
    }

    // Given all log events and page number, this method will give logs belong to the page.
    private PaginatedLogEvent getPaginatedLogEvent(int pageNumber, List<LogEvent> logMsgList) {

        if (logMsgList != null && !logMsgList.isEmpty()) {
            PaginatedLogEvent paginatedLogEvent = new PaginatedLogEvent();
            DataPaginator.doPaging(pageNumber, logMsgList, paginatedLogEvent);
            return paginatedLogEvent;
        } else {
            return null;
        }
    }

    /**
     * Get paginated list of log files in the repository/logs folder.
     *
     * @param pageNumber   page number.
     * @param tenantDomain tenant domain.
     * @param serverKey    search keyword.
     * @return paginated list of log files.
     */
    public PaginatedLogFileInfo getLocalLogFiles(int pageNumber, String tenantDomain, String serverKey) {

        List<LogFileInfo> logFileInfoList = logFileProvider
                .getLogFileInfoList(tenantDomain, serverKey);
        return getPaginatedLogFileInfo(pageNumber, logFileInfoList);
    }

    // Do the pagination for the log file list.
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

    public boolean isManager() {

        if (LoggingConstants.WSO2_STRATOS_MANAGER.equalsIgnoreCase(ServerConfiguration.getInstance()
                .getFirstProperty("ServerKey"))) {
            return true;
        } else {
            return false;
        }
    }

    public String[] getServiceNames() throws LogViewerException {

        String configFileName = CarbonUtils.getCarbonConfigDirPath() + File.separator +
                LoggingConstants.MULTITENANCY_CONFIG_FOLDER + File.separator +
                LoggingConstants.CONFIG_FILENAME;
        List<String> serviceNames = new ArrayList<String>();
        File configFile = new File(configFileName);
        if (configFile.exists()) {
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(configFile);
                XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(
                        inputStream);
                StAXOMBuilder builder = new StAXOMBuilder(parser);
                OMElement documentElement = builder.getDocumentElement();
                @SuppressWarnings("unchecked")
                Iterator<OMElement> properties = documentElement.getChildrenWithName(new QName(
                        "cloudService"));
                while (properties.hasNext()) {
                    OMElement element = properties.next();
                    Iterator<OMElement> child = element.getChildElements();
                    while (child.hasNext()) {
                        OMElement element1 = child.next();
                        if ("key".equalsIgnoreCase(element1.getLocalName())) {
                            serviceNames.add(element1.getText());
                        }
                    }
                }
            } catch (Exception e) {
                String msg = "Error in loading Stratos Configurations File: " + configFileName;
                throw new LogViewerException(msg, e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        log.error("Could not close the Configuration File " + configFileName, e);
                    }
                }
            }
        }
        return serviceNames.toArray(new String[serviceNames.size()]);
    }

    /**
     * This method will return all supported log levels.
     *
     * @return log levels as an array.
     */
    public String[] getLogLevels() {

        return new String[]{"ALL", "FATAL", "ERROR", "WARN", "INFO", "DEBUG", "TRACE"};
    }

    public DataHandler downloadArchivedLogFiles(String logFile)
            throws LogViewerException {

        return logFileProvider.downloadLogFile(logFile);
    }

    /**
     * Download log file with a given name.
     *
     * @param logFile  name of the log file.
     * @param response file download http response.
     */
    public void downloadArchivedLogFiles(String logFile, HttpServletResponse response) {

        String msg = "Error occurred while getting logger data. Backend service may be " +
                "unavailable";

        InputStream fileToDownload = null;
        try {
            logFile = logFile.replace(".gz", "");
            ServletOutputStream outputStream = response.getOutputStream();
            response.setContentType("application/txt");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + logFile.replaceAll("\\s", "_"));
            DataHandler data = downloadArchivedLogFiles(logFile);
            fileToDownload = data.getInputStream();
            int c;
            while ((c = fileToDownload.read()) != -1) {
                outputStream.write(c);
            }
            outputStream.flush();
            outputStream.flush();
        } catch (RemoteException e) {
            log.error(msg, e);
        } catch (LogViewerException e) {
            log.error(msg, e);
        } catch (IOException e) {
            log.error("Error while downloading file.", e);
        } finally {
            try {
                if (fileToDownload != null) {
                    fileToDownload.close();
                }
            } catch (IOException e) {
                log.error("Couldn't close the InputStream " + e.getMessage(), e);
            }
        }
    }
}
