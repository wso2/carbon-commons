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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.activation.DataHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Appender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Logger;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.logging.appender.CarbonMemoryAppender;
import org.wso2.carbon.logging.appender.LogEventAppender;
import org.wso2.carbon.logging.config.ServiceConfigManager;
import org.wso2.carbon.logging.service.data.LogEvent;
import org.wso2.carbon.logging.service.data.LogInfo;
import org.wso2.carbon.logging.service.data.PaginatedLogEvent;
import org.wso2.carbon.logging.service.data.PaginatedLogInfo;
import org.wso2.carbon.logging.util.LoggingConstants;
import org.wso2.carbon.logging.util.LoggingUtil;
import org.wso2.carbon.utils.DataPaginator;

/**
 * This is the Log Viewer service used for obtaining Log messages from locally
 * and from a remote configured syslog server.
 */
public class LogViewer {

	private static final Log log = LogFactory.getLog(LogViewer.class);

	public PaginatedLogInfo getPaginatedLogInfo(int pageNumber, String tenantDomain,
			String serviceName) throws Exception {
		LogInfo[] logs = LoggingUtil.getLogsIndex(tenantDomain, serviceName);
		if (logs != null) {
			List<LogInfo> logInfoList = Arrays.asList(logs);
			// Pagination
			PaginatedLogInfo paginatedLogInfo = new PaginatedLogInfo();
			DataPaginator.doPaging(pageNumber, logInfoList, paginatedLogInfo);
			return paginatedLogInfo;
		} else {
			return null;
		}
	}

	public PaginatedLogInfo getLocalLogFiles(int pageNumber, String domain, String serverKey) throws LogViewerException {
		LogInfo[] logs = null;
		if (LoggingUtil.isLogEventAppenderConfigured()) {
			logs = LoggingUtil.getRemoteLogFiles(domain, serverKey);
		} else if (isFileAppenderConfiguredForST()) {
			logs = null;

		}
		if (logs != null) {
			List<LogInfo> logInfoList = Arrays.asList(logs);
			PaginatedLogInfo paginatedLogInfo = new PaginatedLogInfo();
			DataPaginator.doPaging(pageNumber, logInfoList, paginatedLogInfo);
			return paginatedLogInfo;
		} else {
			return null;
		}
	}

	public DataHandler downloadArchivedLogFiles(String logFile, String domain, String serverKey) throws Exception {
		return LoggingUtil.downloadArchivedLogFiles(logFile, domain, serverKey);
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
    
    public boolean isValidTenant(String domain) {
        int tenantId;
        if (domain == null || domain.equals("")) {
            tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        } else {
            try {
                tenantId = LoggingUtil.getTenantIdForDomain(domain);
            } catch (LogViewerException e) {
                log.error("error while getting tennat id from tenant domain", e);
                return false;
            }
        }
        
        if(tenantId == MultitenantConstants.INVALID_TENANT_ID) {
            return false;
        }
        return true;
    }

	public int getLineNumbers(String logFile) throws Exception {
		return LoggingUtil.getLineNumbers(logFile);
	}

	public String[] getLogLinesFromFile(String logFile, int maxLogs, int start, int end)
			throws LogViewerException {
		return LoggingUtil.getLogLinesFromFile(logFile, maxLogs, start, end);
	}

	public String[] getApplicationNames(String domain, String serverKey) throws LogViewerException {
		if (LoggingUtil.isLogEventAppenderConfigured()) {
			return LoggingUtil.getApplicationNamesFromCassandra(domain, serverKey);
		} else {
			return LoggingUtil.getApplicationNames(domain, serverKey);
		}
	}

	public boolean isLogEventReciverConfigured() {
		Logger rootLogger = Logger.getRootLogger();
		LogEventAppender logger = (LogEventAppender) rootLogger.getAppender("LOGEVENT");
		if (logger != null) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isFileAppenderConfiguredForST() {
		Logger rootLogger = Logger.getRootLogger();
		DailyRollingFileAppender logger = (DailyRollingFileAppender) rootLogger
				.getAppender("CARBON_LOGFILE");
		if (logger != null
				&& CarbonContext.getThreadLocalCarbonContext().getTenantId() == MultitenantConstants.SUPER_TENANT_ID) {
			return true;
		} else {
			return false;
		}
	}
	
	public LogEvent[] getAllSystemLogs() {
		return LoggingUtil.getAllSystemLogs();
	}

    public PaginatedLogEvent getPaginatedLogEvents(int pageNumber, String type, String keyword, String domain, String serverKey)
            throws LogViewerException {

        LogEvent list[];
        if (!LoggingUtil.isLogEventAppenderConfigured()) {
            list = getLogs(type, keyword, domain, serverKey);
        } else {
            list = LoggingUtil.getSortedLogsFromCassandra(type, keyword, domain, serverKey);
        }
        if (list != null) {
            List<LogEvent> logMsgList = Arrays.asList(list);
            PaginatedLogEvent paginatedLogEvent = new PaginatedLogEvent();
            DataPaginator.doPaging(pageNumber, logMsgList, paginatedLogEvent);
            return paginatedLogEvent;
        } else {
            return null;
        }

    }

	public int getNoOfLogEvents(String domain, String serverKey) throws LogViewerException {
		if (LoggingUtil.isLogEventAppenderConfigured()) {
			return LoggingUtil.getNoOfRows(domain, serverKey);
		} else {
			return -1;
		}
	}

	public PaginatedLogEvent getPaginatedApplicationLogEvents(int pageNumber, String type,
			String keyword, String applicationName, String domain, String serverKey) throws Exception {
		LogEvent list[];
		if (LoggingUtil.isLogEventAppenderConfigured()) {
			list = LoggingUtil.getSortedAppLogsFromCassandra(type, keyword, applicationName, domain, serverKey);
		} else {
			list = getApplicationLogs(type, keyword, applicationName, domain, serverKey);
		}
		if (list != null) {
			List<LogEvent> logMsgList = Arrays.asList(list);
			PaginatedLogEvent paginatedLogEvent = new PaginatedLogEvent();
			DataPaginator.doPaging(pageNumber, logMsgList, paginatedLogEvent);
			return paginatedLogEvent;
		} else {
			return null;
		}
	}

	public LogEvent[] getLogs(String type, String keyword, String domain, String serverKey) {

		if (keyword == null || keyword.equals("")) {
			// keyword is null
			if (type == null || type.equals("") || type.equalsIgnoreCase("ALL")) {
				return LoggingUtil.getLogs("", domain, serverKey);
			} else {
				// type is NOT null and NOT equal to ALL Application Name is not
				// needed
				return LoggingUtil.getLogsForType(type, "", domain, serverKey);
			}
		} else {
			// keyword is NOT null
			if (type == null || type.equals("")) {
				// type is null
				return LoggingUtil.getLogsForKey(keyword, "", domain, serverKey);
			} else {
				// type is NOT null and keyword is NOT null, but type can be
				// equal to ALL
				return LoggingUtil.searchLog(type, keyword, "", domain, serverKey);
			}
		}
	}

    
	public LogEvent[] getApplicationLogs(String type, String keyword, String appName, String domain, String serverKey) {
		if (keyword == null || keyword.equals("")) {
			// keyword is null
			if (type == null || type.equals("") || type.equalsIgnoreCase("ALL")) {
				return LoggingUtil.getLogs(appName, domain, serverKey);
			} else {
				// type is NOT null and NOT equal to ALL
				return LoggingUtil.getLogsForType(type, appName, domain, serverKey);
			}
		} else {
			// keyword is NOT null
			if (type == null || type.equals("")) {
				// type is null
				return LoggingUtil.getLogsForKey(keyword, appName, domain, serverKey);
			} else {
				// type is NOT null and keyword is NOT null, but type can be
				// equal to ALL
				return LoggingUtil.searchLog(type, keyword, appName, domain, serverKey);
			}
		}
	}
    
	public boolean clearLogs() {
		Appender appender = Logger.getRootLogger().getAppender(
				LoggingConstants.WSO2CARBON_MEMORY_APPENDER);
		if (appender instanceof CarbonMemoryAppender) {
			try {
				CarbonMemoryAppender memoryAppender = (CarbonMemoryAppender) appender;
				if (memoryAppender.getCircularQueue() != null) {
					memoryAppender.getCircularQueue().clear();
				}
				return true;
			} catch (Exception e) {
				return false;
			}
		} else {
			return false;
		}
	}
}
