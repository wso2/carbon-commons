/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.logging.service.provider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.logging.service.LogViewerException;
import org.wso2.carbon.logging.service.appender.CarbonMemoryAppender;
import org.wso2.carbon.logging.service.data.LogEvent;
import org.wso2.carbon.logging.service.data.LoggingConfig;
import org.wso2.carbon.logging.service.provider.api.LogProvider;
import org.wso2.carbon.logging.service.util.LoggingConstants;
import org.wso2.carbon.logging.service.util.LoggingUtil;
import org.wso2.carbon.utils.logging.TenantAwareLoggingEvent;
import org.wso2.carbon.utils.logging.TenantAwarePatternLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class InMemoryLogProvider implements LogProvider {

    private static final Log log = LogFactory.getLog(InMemoryLogProvider.class);
    private static final int DEFAULT_NO_OF_LOGS = 100;
    private static final String SERVER_KEY = "ServerKey";

    /**
     * Initialize the log provider by reading the property comes with logging configuration file
     * This will be called immediate after create new instance of ILogProvider
     *
     * @param loggingConfig - configuration class which read and keep configurations
     */
    @Override
    public void init(LoggingConfig loggingConfig) {
        // nothing to do
    }

    @Override
    public List<String> getApplicationNames(String tenantDomain, String serverKey) throws LogViewerException {
        List<String> appList = new ArrayList<String>();
        List<LogEvent> allLogs = getLogsByAppName("", tenantDomain, serverKey);
        for (LogEvent event : allLogs) {
            if (event.getAppName() != null && !"".equals(event.getAppName())
                    && !"NA".equals(event.getAppName())
                    && !LoggingUtil.isAdmingService(event.getAppName())
                    && !appList.contains(event.getAppName())
                    && !"STRATOS_ROOT".equals(event.getAppName())) {
                appList.add(event.getAppName());
            }
        }
        return getSortedApplicationNames(appList);
    }


    @Override
    public List<LogEvent> getSystemLogs() throws LogViewerException {
        List<LogEvent> resultList = new ArrayList<LogEvent>();
        Appender appender = Logger.getRootLogger().getAppender(
                LoggingConstants.WSO2CARBON_MEMORY_APPENDER);
        if (appender instanceof CarbonMemoryAppender) {
            CarbonMemoryAppender memoryAppender = (CarbonMemoryAppender) appender;
            List<TenantAwareLoggingEvent> tenantAwareLoggingEventList = getTenantAwareLoggingEventList(memoryAppender);
            for (TenantAwareLoggingEvent tenantAwareLoggingEvent : tenantAwareLoggingEventList) {
                if (tenantAwareLoggingEvent != null) {
                    resultList.add(createLogEvent(tenantAwareLoggingEvent));
                }
            }
            return reverseLogList(resultList);
        } else {
            return getDefaultLogEvents();
        }
    }

    private List<LogEvent> getDefaultLogEvents() {
        List<LogEvent> defaultLogEvents = new ArrayList<LogEvent>();
        defaultLogEvents.add(new LogEvent(
                "The log must be configured to use the "
                        + "org.wso2.carbon.logging.service.appender.CarbonMemoryAppender to view entries through the admin console",
                "NA"));
        return defaultLogEvents;
    }

    private List<TenantAwareLoggingEvent> getTenantAwareLoggingEventList(CarbonMemoryAppender memoryAppender) {
        int definedAmount;
        if (memoryAppender.getCircularQueue() != null) {
            definedAmount = memoryAppender.getBufferSize();
            if (definedAmount < 1) {
                return memoryAppender.getCircularQueue().get(DEFAULT_NO_OF_LOGS);
            } else {
                return memoryAppender.getCircularQueue().get(definedAmount);
            }
        } else {
            return new ArrayList<TenantAwareLoggingEvent>();
        }
    }

    @Override
    public List<LogEvent> getAllLogs(String tenantDomain, String serverKey) throws LogViewerException {
        return getLogs("ALL", tenantDomain, serverKey);
    }

    @Override
    public List<LogEvent> getLogsByAppName(String appName, String tenantDomain, String serverKey) throws LogViewerException {
        List<LogEvent> resultList = new ArrayList<LogEvent>();
        Appender appender = Logger.getRootLogger().getAppender(
                LoggingConstants.WSO2CARBON_MEMORY_APPENDER);
        if (appender instanceof CarbonMemoryAppender) {
            CarbonMemoryAppender memoryAppender = (CarbonMemoryAppender) appender;
            List<TenantAwareLoggingEvent> tenantAwareLoggingEventList = getTenantAwareLoggingEventList(memoryAppender);
            for (TenantAwareLoggingEvent tenantAwareLoggingEvent : tenantAwareLoggingEventList) {
                if (tenantAwareLoggingEvent != null) {
                    TenantAwarePatternLayout tenantIdPattern = new TenantAwarePatternLayout("%T");
                    TenantAwarePatternLayout productPattern = new TenantAwarePatternLayout("%S");
                    String productName = productPattern.format(tenantAwareLoggingEvent);
                    String tenantId = tenantIdPattern.format(tenantAwareLoggingEvent);
                    if (isCurrentTenantId(tenantId, tenantDomain) && isCurrentProduct(productName, serverKey)) {
                        if (appName == null || "".equals(appName)) {
                            resultList.add(createLogEvent(tenantAwareLoggingEvent));
                        } else {
                            TenantAwarePatternLayout appPattern = new TenantAwarePatternLayout("%A");
                            String currAppName = appPattern.format(tenantAwareLoggingEvent);
                            if (appName.equals(currAppName)) {
                                resultList.add(createLogEvent(tenantAwareLoggingEvent));
                            }
                        }
                    }
                }
            }
            return reverseLogList(resultList);
        } else {
            return getDefaultLogEvents();
        }
    }

    @Override
    public List<LogEvent> getLogs(String type, String keyword, String appName, String tenantDomain, String serverKey) throws LogViewerException {
        if (keyword == null || "".equals(keyword)) {
            // invalid keyword
            if (type == null || "".equals(type) || "ALL".equals(type)) {
                return this.getLogs(appName, tenantDomain, serverKey);
            } else {
                // type is NOT null and NOT equal to ALL Application Name is not needed
                return this.getLogsForType(type, appName, tenantDomain, serverKey);
            }
        } else {
            // valid keyword
            if (type == null || "".equals(type)) {
                // invalid type
                return this.getLogsForKey(keyword, appName, tenantDomain, serverKey);
            } else {
                // both type and keyword are valid, but type can be equal to ALL
                if ("ALL".equalsIgnoreCase(type)) {
                    return getLogsForKey(keyword, appName, tenantDomain, serverKey);
                } else {
                    List<LogEvent> filerByType = getLogsForType(type, appName, tenantDomain, serverKey);
                    List<LogEvent> resultList = new ArrayList<LogEvent>();
                    if (filerByType != null) {
                        for (LogEvent aFilerByType : filerByType) {
                            String logMessage = aFilerByType.getMessage();
                            String logger = aFilerByType.getLogger();
                            if (logMessage != null
                                    && logMessage.toLowerCase().contains(keyword.toLowerCase())) {
                                resultList.add(aFilerByType);
                            } else if (logger != null
                                    && logger.toLowerCase().contains(keyword.toLowerCase())) {
                                resultList.add(aFilerByType);
                            }
                        }
                    }
                    return reverseLogList(resultList);
                }
            }
        }

    }

    private List<LogEvent> getLogs(String appName, String tenantDomain, String serverKey) {
        List<LogEvent> resultList = new ArrayList<LogEvent>();
        Appender appender = Logger.getRootLogger().getAppender(
                LoggingConstants.WSO2CARBON_MEMORY_APPENDER);
        if (appender instanceof CarbonMemoryAppender) {
            CarbonMemoryAppender memoryAppender = (CarbonMemoryAppender) appender;
            List<TenantAwareLoggingEvent> tenantAwareLoggingEventList = getTenantAwareLoggingEventList(memoryAppender);
            for (TenantAwareLoggingEvent tenantAwareLoggingEvent : tenantAwareLoggingEventList) {
                if (tenantAwareLoggingEvent != null) {
                    TenantAwarePatternLayout tenantIdPattern = new TenantAwarePatternLayout("%T");
                    TenantAwarePatternLayout productPattern = new TenantAwarePatternLayout("%S");
                    String productName = productPattern.format(tenantAwareLoggingEvent);
                    String tenantId = tenantIdPattern.format(tenantAwareLoggingEvent);
                    if (isCurrentTenantId(tenantId, tenantDomain) && isCurrentProduct(productName, serverKey)) {
                        if (appName == null || "".equals(appName)) {
                            resultList.add(createLogEvent(tenantAwareLoggingEvent));
                        } else {
                            TenantAwarePatternLayout appPattern = new TenantAwarePatternLayout("%A");
                            String currAppName = appPattern.format(tenantAwareLoggingEvent);
                            if (appName.equals(currAppName)) {
                                resultList.add(createLogEvent(tenantAwareLoggingEvent));
                            }
                        }
                    }
                }
            }
            return reverseLogList(resultList);
        } else {
            return getDefaultLogEvents();
        }
    }

    @Override
    public int logsCount(String tenantDomain, String serverKey) throws LogViewerException {
        return 0;
    }

    @Override
    public boolean clearLogs() {
        Appender appender = Logger.getRootLogger().getAppender(LoggingConstants.WSO2CARBON_MEMORY_APPENDER);
        if (appender instanceof CarbonMemoryAppender) {
            CarbonMemoryAppender memoryAppender = (CarbonMemoryAppender) appender;
            memoryAppender.clearCircularBuffer();
            return true;
        }
        return false;
    }

    private boolean isCurrentTenantId(String tenantId, String domain) {
        String currTenantId;
        if ("".equals(domain)) {
            currTenantId = String.valueOf(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            return currTenantId.equals(tenantId);
        } else {
            try {
                currTenantId = String.valueOf(LoggingUtil.getTenantIdForDomain(domain));
                return currTenantId.equals(tenantId);
            } catch (LogViewerException e) {
                log.error("Error while getting current tenantId for domain " + domain, e);
                return false;
            }
        }
    }

    private boolean isCurrentProduct(String productName, String serverKey) {
        if ("".equals(serverKey)) {
            String currProductName = ServerConfiguration.getInstance().getFirstProperty(SERVER_KEY);
            return currProductName.equals(productName);
        } else {
            return productName.equals(serverKey);
        }

    }

    private LogEvent createLogEvent(TenantAwareLoggingEvent logEvt) {
        Appender appender = Logger.getRootLogger().getAppender(
                LoggingConstants.WSO2CARBON_MEMORY_APPENDER);
        CarbonMemoryAppender memoryAppender = (CarbonMemoryAppender) appender;
        List<String> patterns = Arrays.asList(memoryAppender.getColumnList().split(","));
        LogEvent event = new LogEvent();
        for (String pattern : patterns) {
            String currEle = (pattern).replace("%", "");
            TenantAwarePatternLayout patternLayout = new TenantAwarePatternLayout("%" + currEle);
            if ("T".equals(currEle)) {
                event.setTenantId(patternLayout.format(logEvt));
            } else if ("S".equals(currEle)) {
                event.setServerName(patternLayout.format(logEvt));
            } else if ("A".equals(currEle)) {
                event.setAppName(patternLayout.format(logEvt));
            } else if ("d".equals(currEle)) {
                event.setLogTime(patternLayout.format(logEvt));
            } else if ("c".equals(currEle)) {
                event.setLogger(patternLayout.format(logEvt));
            } else if ("p".equals(currEle)) {
                event.setPriority(patternLayout.format(logEvt));
            } else if ("m".equals(currEle)) {
                event.setMessage(patternLayout.format(logEvt));
            } else if ("I".equals(currEle)) {
                event.setInstance(patternLayout.format(logEvt));
            } else if ("Stacktrace".equals(currEle)) {
                if (logEvt.getThrowableInformation() != null) {
                    event.setStacktrace(getStacktrace(logEvt.getThrowableInformation().getThrowable()));
                } else {
                    event.setStacktrace("");
                }
            } else if ("H".equals(currEle)) {
                event.setIp(patternLayout.format(logEvt));
            }
        }
        return event;
    }

    private String getStacktrace(Throwable e) {
        StringBuilder stackTrace = new StringBuilder();
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        for (StackTraceElement ele : stackTraceElements) {
            stackTrace.append(ele.toString()).append("\n");
        }
        return stackTrace.toString();
    }

    private List<LogEvent> reverseLogList(List<LogEvent> resultList) {
        Collections.reverse(resultList);
        return resultList;
    }

    private List<LogEvent> getLogsForKey(String keyword, String appName, String tenantDomain, String serverKey) {
        List<LogEvent> resultList = new ArrayList<LogEvent>();
        Appender appender = Logger.getRootLogger().getAppender(
                LoggingConstants.WSO2CARBON_MEMORY_APPENDER);
        if (appender instanceof CarbonMemoryAppender) {
            CarbonMemoryAppender memoryAppender = (CarbonMemoryAppender) appender;
            List<TenantAwareLoggingEvent> tenantAwareLoggingEventList = getTenantAwareLoggingEventList(memoryAppender);
            for (TenantAwareLoggingEvent tenantAwareLoggingEvent : tenantAwareLoggingEventList) {
                if (tenantAwareLoggingEvent != null) {
                    TenantAwarePatternLayout tenantIdPattern = new TenantAwarePatternLayout("%T");
                    TenantAwarePatternLayout productPattern = new TenantAwarePatternLayout("%S");
                    TenantAwarePatternLayout messagePattern = new TenantAwarePatternLayout("%m");
                    TenantAwarePatternLayout loggerPattern = new TenantAwarePatternLayout("%c");
                    String productName = productPattern.format(tenantAwareLoggingEvent);
                    String tenantId = tenantIdPattern.format(tenantAwareLoggingEvent);
                    String result = messagePattern.format(tenantAwareLoggingEvent);
                    String logger = loggerPattern.format(tenantAwareLoggingEvent);
                    boolean isInLogMessage = result != null
                            && (result.toLowerCase().contains(keyword.toLowerCase()));
                    boolean isInLogger = logger != null
                            && (logger.toLowerCase().contains(keyword.toLowerCase()));
                    if (isCurrentTenantId(tenantId, tenantDomain) && isCurrentProduct(productName, serverKey)
                            && (isInLogMessage || isInLogger)) {
                        if (appName == null || "".equals(appName)) {
                            resultList.add(createLogEvent(tenantAwareLoggingEvent));
                        } else {
                            TenantAwarePatternLayout appPattern = new TenantAwarePatternLayout("%A");
                            String currAppName = appPattern.format(tenantAwareLoggingEvent);
                            if (appName.equals(currAppName)) {
                                resultList.add(createLogEvent(tenantAwareLoggingEvent));
                            }
                        }
                    }
                }
            }
            return reverseLogList(resultList);
        } else {
            return getDefaultLogEvents();
        }
    }

    private List<LogEvent> getLogsForType(String type, String appName, String tenantDomain, String serverKey) {
        List<LogEvent> resultList = new ArrayList<LogEvent>();
        Appender appender = Logger.getRootLogger().getAppender(
                LoggingConstants.WSO2CARBON_MEMORY_APPENDER);
        if (appender instanceof CarbonMemoryAppender) {
            CarbonMemoryAppender memoryAppender = (CarbonMemoryAppender) appender;
            List<TenantAwareLoggingEvent> tenantAwareLoggingEventList = getTenantAwareLoggingEventList(memoryAppender);
            for (TenantAwareLoggingEvent tenantAwareLoggingEvent : tenantAwareLoggingEventList) {
                if (tenantAwareLoggingEvent != null) {
                    TenantAwarePatternLayout tenantIdPattern = new TenantAwarePatternLayout("%T");
                    TenantAwarePatternLayout productPattern = new TenantAwarePatternLayout("%S");
                    String priority = tenantAwareLoggingEvent.getLevel().toString();
                    String productName = productPattern.format(tenantAwareLoggingEvent);
                    String tenantId = tenantIdPattern.format(tenantAwareLoggingEvent);
                    if ((priority.equals(type) && isCurrentTenantId(tenantId, tenantDomain) && isCurrentProduct(productName, serverKey))) {
                        if (appName == null || "".equals(appName)) {
                            resultList.add(createLogEvent(tenantAwareLoggingEvent));
                        } else {
                            TenantAwarePatternLayout appPattern = new TenantAwarePatternLayout("%A");
                            String currAppName = appPattern.format(tenantAwareLoggingEvent);
                            if (appName.equals(currAppName)) {
                                resultList.add(createLogEvent(tenantAwareLoggingEvent));
                            }
                        }
                    }
                }
            }
            return reverseLogList(resultList);
        } else {
            return getDefaultLogEvents();
        }
    }

    private List<String> getSortedApplicationNames(List<String> applicationNames) {
        Collections.sort(applicationNames, new Comparator<String>() {
            public int compare(String s1, String s2) {
                return s1.toLowerCase().compareTo(s2.toLowerCase());
            }

        });
        return applicationNames;
    }

}
