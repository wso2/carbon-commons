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
package org.wso2.carbon.logging.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.logging.config.ServiceConfigManager;
import org.wso2.carbon.logging.config.SyslogConfigManager;
import org.wso2.carbon.logging.config.SyslogConfiguration;
import org.wso2.carbon.logging.internal.LoggingServiceComponent;
import org.wso2.carbon.logging.service.LogViewerException;
import org.wso2.carbon.logging.service.data.LogInfo;
import org.wso2.carbon.logging.service.data.LogMessage;
import org.wso2.carbon.logging.service.data.SyslogData;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * This is the Log Reader class to read log messages from remote/local file
 * systems
 */
public class LoggingReader {

	private static Log log = LogFactory.getLog(LoggingReader.class);

	private static final LogMessage[] NO_LOGS_MESSAGE = new LogMessage[] { new LogMessage(
			"NO_LOGS", "INFO") };
	private static final LogInfo[] NO_LOGS_INFO = new LogInfo[] { new LogInfo("NO_LOG_FILES",
			"---", "---") };

	/**
	 * This method is used to get LogIndex of a given log collector server. It
	 * gives a list of log files along with their dates and their size.
	 * 
	 * @param tenantDomain
	 *            - tenant domain of the log file we need to retrieve {This can
	 *            be done only for ST Mode}
	 * @param serviceName
	 *            - Service name which we need to retrieve logs files. {This can
	 *            be done only in Manager}
	 * @return logIndex - Array of LogInfor
	 * @throws LogViewerException
	 */
	public LogInfo[] getLogsIndex(String tenantDomain, String serviceName)
			throws LogViewerException {
		LogInfo[] logIndex;
		int tenantId = getTenantIdForDomain(tenantDomain);
		if (isSyslogOn()) { // if the syslog-config is on
			logIndex = getLogInfo(tenantId, serviceName);
		} else {
			// If data is not accessible through syslog and if its
			// Super-tenant/Stand-alone, access local log files.
			if (isSuperTenantUser()) {
				logIndex = getLocalLogInfo(tenantDomain, serviceName);
			} else {
				return null;
			}
		}
		return logIndex;
	}

	/**
	 * Get tenant specific log messages. Only 200 log files are retrieved at
	 * once.
	 * 
	 */
	public LogMessage[] getTenantLogs(String type, String keyword, String logFile, String logIndex,
			int maxLines, int start, int end, String tenantDomain, String serviceName)
			throws LogViewerException {
		int tenantId = getTenantIdForDomain(tenantDomain);
		if (keyword == null || keyword.equals("")) {
			// keyword is null
			if (type == null || type.equals("") || type.equalsIgnoreCase("ALL")) {
				return getHeadLogs(
						logIndex,
						getTenantLogMessages(logFile, maxLines, start - 1, end, tenantId,
								serviceName));
			} else {
				// type is NOT null and NOT equal to ALL
				return getTenantLogsForType(type, logFile, logIndex, maxLines, start, end,
						tenantId, serviceName);
			}
		} else {
			// keyword is NOT null
			if (type == null || type.equals("")) {
				// type is null
				return getTenantLogsForKey(keyword, logFile, logIndex, maxLines, start, end,
						tenantId, serviceName);
			} else {
				// type is NOT null and keyword is NOT null, but type can be
				// equal to ALL
				return searchTenantLog(type, keyword, logFile, logIndex, maxLines, start, end,
						tenantId, serviceName);
			}
		}
		
	}

	/**
	 * Read bottom up tenant logs.
	 */
	public LogMessage[] getBottomUpTenantLogs(String type, String keyword, String logFile,
			int maxLines, int start, int end, String tenantDomain, String serviceName)
			throws LogViewerException {
		int tenantId = getTenantIdForDomain(tenantDomain);
		if (keyword == null || keyword.equals("")) {
			// keyword is null
			if (type == null || type.equals("") || type.equalsIgnoreCase("ALL")) {
				return getBottomUpLogMessages(logFile, maxLines, start, end, tenantId, serviceName);
			} else {
				// type is NOT null and NOT equal to ALL
				return getBottomUpLogsForType(type, logFile, maxLines, start, end, tenantId,
						serviceName);
			}
		} else {
			// keyword is NOT null
			if (type == null || type.equals("")) {
				// type is null
				return getBottomUpLogsForKey(keyword, logFile, maxLines, start, end, tenantId,
						serviceName);
			} else {
				// type is NOT null and keyword is NOT null, but type can be
				// equal to ALL
				return searchBottomUpLogsTenantLog(type, keyword, logFile, maxLines, start, end,
						tenantId, serviceName);
			}
		}
	}

	public DataHandler downloadLogFiles(String logFile, String tenantDomain, String serviceName)
			throws LogViewerException {
		InputStream is;
		int tenantId = getTenantIdForDomain(tenantDomain);
		try {
			is = getInputStream(logFile, tenantId, serviceName);
		} catch (LogViewerException e) {
			throw new LogViewerException("Cannot read InputStream from the file " + logFile, e);
		}
		try {
			ByteArrayDataSource bytArrayDS = new ByteArrayDataSource(is, "application/zip");
			DataHandler dataHandler = new DataHandler(bytArrayDS);
			return dataHandler;
		} catch (IOException e) {
			throw new LogViewerException("Cannot read file size from the " + logFile, e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				throw new LogViewerException("Cannot close the input stream " + logFile, e);
			}
		}
	}
	public String[] getLogLinesFromFile(String logFile, int maxLogs, int start, int end,
			String tenantDomain, String serviceName) throws LogViewerException {
		int tenantId = getTenantIdForDomain(tenantDomain);
		ArrayList<String> logsList = new ArrayList<String>();
		InputStream logStream;
		if (end > maxLogs) {
			end = maxLogs;
		}
		try {
			logStream = getInputStream(logFile, tenantId, serviceName);
		} catch (Exception e) {
			throw new LogViewerException("Cannot find the specified file location to the log file",
					e);
		}
		BufferedReader dataInput = new BufferedReader(new InputStreamReader(logStream));
		int index = 1;
		String line;
		try {
			while ((line = dataInput.readLine()) != null) {
				if (index <= end && index > start) {
					logsList.add(line);
				}
				index++;
			}
			dataInput.close();
		} catch (IOException e) {
			throw new LogViewerException("Cannot read the log file", e);
		}
		return logsList.toArray(new String[logsList.size()]);
	}

	public int getLineNumbers(String logFile, String tenantDomain, String serviceName)
			throws Exception {
		InputStream is;
		int tenantId = getTenantIdForDomain(tenantDomain);
		try {
			is = getInputStream(logFile, tenantId, serviceName);
		} catch (LogViewerException e) {
			throw new LogViewerException("Cannot read InputStream from the file " + logFile, e);
		}
		try {
			byte[] c = new byte[1024];
			int count = 0;
			int readChars = 0;
			while ((readChars = is.read(c)) != -1) {
				for (int i = 0; i < readChars; ++i) {
					if (c[i] == '\n') {
						++count;
					}
				}
			}
			return count;
		} catch (IOException e) {
			throw new LogViewerException("Cannot read file size from the " + logFile, e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				throw new LogViewerException("Cannot close the input stream " + logFile, e);
			}
		}
	}

	public String getLogLine(String logFile, int lineNo, String tenantDomain, String serviceName)
			throws LogViewerException {
		String line;
		InputStream is;
		try {
			int tenantId = getTenantIdForDomain(tenantDomain);
			is = getInputStream(logFile, tenantId, serviceName);
		} catch (Exception e) {
			throw new LogViewerException("Cannot read InputStream from the file " + logFile, e);
		}
		BufferedReader dataInput = new BufferedReader(new InputStreamReader(is));
		try {
			int count = 1;
			try {
				while ((line = dataInput.readLine()) != null) {
					if (count == lineNo) {
						return line;
					}
					count++;
				}
			} catch (IOException e) {
				throw new LogViewerException("Cannot read line number from the " + logFile, e);
			}
		} finally {
			try {
				dataInput.close();
				is.close();
			} catch (IOException e) {
				throw new LogViewerException("Cannot close the input stream " + logFile, e);
			}
		}
		return null;
	}

	/**
	 * Check if the syslog is properly configured and accessible,
	 * 
	 * @param tenantId
	 * @return
	 * @throws LogViewerException
	 */
	public boolean isSysLogAppender(int tenantId) throws LogViewerException {
		return isSyslogOn();

	}

	public boolean isValidTenantDomain(String tenantDomain) {
		try {
			getTenantIdForDomain(tenantDomain);
			return true;
		} catch (LogViewerException e) {
			return false;
		}
	}

	private String getLogsServerURLforTenantService(String syslogServerURL, String logFile,
			int tenantId, String serviceName) throws LogViewerException {
		String serverurl = "";
		String lastChar = String.valueOf(syslogServerURL.charAt(syslogServerURL.length() - 1));
		if (lastChar.equals(LoggingConstants.URL_SEPARATOR)) { // http://my.log.server/logs/stratos/
			syslogServerURL = syslogServerURL.substring(0, syslogServerURL.length() - 1);
		}
		if (isSuperTenantUser()) { // ST can view tenant specific log files.
			if (isManager()) { // manager can view different services log
								// messages.
				if (serviceName != null && serviceName.length() > 0) {
					serverurl = syslogServerURL + LoggingConstants.URL_SEPARATOR + tenantId
							+ LoggingConstants.URL_SEPARATOR + serviceName
							+ LoggingConstants.URL_SEPARATOR;
				} else {
					serverurl = syslogServerURL + LoggingConstants.URL_SEPARATOR + tenantId
							+ LoggingConstants.URL_SEPARATOR
							+ LoggingConstants.WSO2_STRATOS_MANAGER
							+ LoggingConstants.URL_SEPARATOR;
				}
				try {
					if (!isStratosService()) { // stand-alone apps.
						serverurl = syslogServerURL + LoggingConstants.URL_SEPARATOR + tenantId
								+ LoggingConstants.URL_SEPARATOR
								+ ServerConfiguration.getInstance().getFirstProperty("ServerKey")
								+ LoggingConstants.URL_SEPARATOR;
					}
				} catch (LogViewerException e) {
					throw new LogViewerException("Cannot get log ServerURL for Tenant Service", e);
				}
			} else { // for other stratos services can view only their relevant
						// logs.
				serverurl = syslogServerURL + LoggingConstants.URL_SEPARATOR + tenantId
						+ LoggingConstants.URL_SEPARATOR
						+ ServerConfiguration.getInstance().getFirstProperty("ServerKey")
						+ LoggingConstants.URL_SEPARATOR;
			}

		} else { // tenant level logging
			if (isManager()) {
				if (serviceName != null && serviceName.length() > 0) {
					serverurl = syslogServerURL + LoggingConstants.URL_SEPARATOR
							+ CarbonContext.getCurrentContext().getTenantId()
							+ LoggingConstants.URL_SEPARATOR + serviceName
							+ LoggingConstants.URL_SEPARATOR;
				} else {
					serverurl = syslogServerURL + LoggingConstants.URL_SEPARATOR
							+ CarbonContext.getCurrentContext().getTenantId()
							+ LoggingConstants.URL_SEPARATOR
							+ LoggingConstants.WSO2_STRATOS_MANAGER
							+ LoggingConstants.URL_SEPARATOR;
				}
			} else {
				serverurl = syslogServerURL + LoggingConstants.URL_SEPARATOR
						+ CarbonContext.getCurrentContext().getTenantId()
						+ LoggingConstants.URL_SEPARATOR
						+ ServerConfiguration.getInstance().getFirstProperty("ServerKey")
						+ LoggingConstants.URL_SEPARATOR;
			}
		}
		serverurl = serverurl.replaceAll("\\s", "%20");
		logFile = logFile.replaceAll("\\s", "%20");
		return serverurl + logFile;
	}

	public boolean isStratosService() throws LogViewerException {
		String serviceName = ServerConfiguration.getInstance().getFirstProperty("ServerKey");
		return ServiceConfigManager.isStratosService(serviceName);
	}

	public boolean isManager() {
		if (LoggingConstants.WSO2_STRATOS_MANAGER.equalsIgnoreCase(ServerConfiguration.getInstance()
				.getFirstProperty("ServerKey"))) {
			return true;
		} else {
			return false;
		}
	}

	private InputStream getLogDataStream(String logFile, int tenantId, String productName)
			throws Exception {
		SyslogData syslogData = getSyslogData();
		String url = "";
		// manager can view all the products tenant log information
		url = getLogsServerURLforTenantService(syslogData.getUrl(), logFile, tenantId, productName);
		String password = syslogData.getPassword();
		String userName = syslogData.getUserName();
		int port = Integer.parseInt(syslogData.getPort());
		String realm = syslogData.getRealm();
		URI uri = new URI(url);
		String host = uri.getHost();
		HttpClient client = new HttpClient();
		client.getState().setCredentials(new AuthScope(host, port, realm),
				new UsernamePasswordCredentials(userName, password));
		GetMethod get = new GetMethod(url);
		get.setDoAuthentication(true);
		client.executeMethod(get);
		return get.getResponseBodyAsStream();
	}

	private LogMessage[] getHeadLogs(String logIndex, LogMessage[] allLogs)
			throws LogViewerException {
		try {
			int index = Integer.parseInt(logIndex) + 1;
			int maxLen;
			if (index > allLogs.length || index == -1) {
				maxLen = allLogs.length;
			} else {
				maxLen = index;
			}
			LogMessage[] headLogs = new LogMessage[maxLen];
			for (int i = 0; i < maxLen; i++) {
				headLogs[i] = allLogs[i];
			}
			return headLogs;
		} catch (Exception e) {
			throw new LogViewerException("Cannot retrieve logs for bottom up", e);
		}
	}

	private LogInfo[] getSortedLogInfo(LogInfo logs[]) {
		int maxLen = logs.length;
		if (maxLen > 0) {
			List<LogInfo> logInfoList = Arrays.asList(logs);
			Collections.sort(logInfoList, new Comparator<Object>() {
				public int compare(Object o1, Object o2) {
					LogInfo log1 = (LogInfo) o1;
					LogInfo log2 = (LogInfo) o2;
					return log1.getLogName().compareToIgnoreCase(log2.getLogName());
				}

			});
			return (LogInfo[]) logInfoList.toArray(new LogInfo[logInfoList.size()]);
		} else {
			return NO_LOGS_INFO;
		}
	}

	private LogMessage[] getBottomUpLogsForType(String type, String logFile, int maxLogs,
			int start, int end, int tenantId, String serviceName) throws LogViewerException {
		List<LogMessage> resultList = new ArrayList<LogMessage>();
		LogMessage[] allLogs = getBottomUpLogMessages(logFile, maxLogs, start, end, tenantId,
				serviceName);
		for (int i = 0; i < allLogs.length; i++) {
			LogMessage tempLog = allLogs[i];
			if (tempLog != null && type.equals(tempLog.getType().trim())) {
				resultList.add(tempLog);
			}
		}
		if (resultList.size() > 0) {
			return resultList.toArray(new LogMessage[resultList.size()]);
		} else {
			return NO_LOGS_MESSAGE;
		}

	}

	private LogMessage[] getBottomUpLogsForKey(String keyword, String logFile, int maxLines,
			int start, int end, int tenantId, String serviceName) throws LogViewerException {
		List<LogMessage> resultList = new ArrayList<LogMessage>();
		LogMessage[] allLogs = getBottomUpLogMessages(logFile, maxLines, start, end, tenantId,
				serviceName);
		for (int i = 0; i < allLogs.length; i++) {
			LogMessage tempLog = allLogs[i];
			if (tempLog != null) {
				String message = tempLog.getLogMessage();
				if (message != null && message.toLowerCase().indexOf(keyword.toLowerCase()) > -1) {
					resultList.add(tempLog);
				}
			}
		}
		if (resultList.size() > 0) {
			return resultList.toArray(new LogMessage[resultList.size()]);
		} else {
			return NO_LOGS_MESSAGE;
		}
	}

	public boolean isSuperTenantUser() {
		CarbonContext carbonContext = CarbonContext.getCurrentContext();
		int tenantId = carbonContext.getTenantId();
		if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
			return true;
		} else {
			return false;
		}
	}

	private SyslogData getSyslogData() throws Exception {
		return LoggingUtil.getSyslogData();
	}

	private boolean isSyslogOn() {
		SyslogConfiguration syslogConfig = SyslogConfigManager.loadSyslogConfiguration();
		return syslogConfig.isSyslogOn();
	}

	private LogMessage[] getTenantLogsForType(String type, String logFile, String logIndex,
			int maxLines, int start, int end, int tenantId, String serviceName)
			throws LogViewerException {
		List<LogMessage> resultList = new ArrayList<LogMessage>();
		LogMessage[] allLogs = getHeadLogs(logIndex,
				getTenantLogMessages(logFile, maxLines, start - 1, end, tenantId, serviceName));
		for (int i = 0; i < allLogs.length; i++) {
			LogMessage tempLog = allLogs[i];
			if (type.equals(tempLog.getType().trim())) {
				resultList.add(tempLog);
			}
		}
		if (resultList.size() > 0) {
			return resultList.toArray(new LogMessage[resultList.size()]);
		} else {
			return NO_LOGS_MESSAGE;
		}

	}

	private LogMessage[] getTenantLogsForKey(String keyword, String logFile, String logIndex,
			int maxLines, int start, int end, int tenantId, String serviceName)
			throws LogViewerException {
		List<LogMessage> resultList = new ArrayList<LogMessage>();
		LogMessage[] allLogs = getHeadLogs(logIndex,
				getTenantLogMessages(logFile, maxLines, start - 1, end, tenantId, serviceName));
		for (int i = 0; i < allLogs.length; i++) {
			LogMessage tempLog = allLogs[i];
			String message = tempLog.getLogMessage();
			if (message != null && message.toLowerCase().indexOf(keyword.toLowerCase()) > -1) {
				resultList.add(tempLog);
			}
		}
		if (resultList.size() > 0) {
			return resultList.toArray(new LogMessage[resultList.size()]);
		} else {
			return NO_LOGS_MESSAGE;
		}
	}

	private String removeSyslogHeader(String line) {
		if (isValidPattern(LoggingConstants.RegexPatterns.SYSLOG_DATE_PATTERN, line)) {
			String lines[] = line.split(LoggingConstants.RegexPatterns.SYSLOG_DATE_PATTERN);
			if (lines.length > 1) {
				return lines[1];
			} else {
				return line;
			}
		} else {
			return line;
		}
	}

	private String cleanLogHeader(String line, int tenantId) {
		String tenantPattern = "";
		if (isSuperTenantUser()) {
			tenantPattern = SyslogConfigManager.getSyslogPattern().replace(
					LoggingConstants.RegexPatterns.TENANT_PATTERN, String.valueOf(tenantId));
		} else {
			tenantPattern = SyslogConfigManager.getSyslogPattern().replace(
					LoggingConstants.RegexPatterns.TENANT_PATTERN,
					String.valueOf(CarbonContext.getCurrentContext().getTenantId()));
		}
		return line.replace(tenantPattern.trim(), "");
	}

	private LogMessage getLogMessageForType(String log) {
		LogMessage logMessage = null;
		if (isTraceHeader(log)) {
			logMessage = new LogMessage(log, LoggingConstants.RegexPatterns.LOG_TRACE);
		} else if (isDebugHeader(log)) {
			logMessage = new LogMessage(log, LoggingConstants.RegexPatterns.LOG_DEBUG);
		} else if (isInfoHeader(log)) {
			logMessage = new LogMessage(log, LoggingConstants.RegexPatterns.LOG_INFO);
		} else if (isWarnHeader(log)) {
			logMessage = new LogMessage(log, LoggingConstants.RegexPatterns.LOG_WARN);
		} else if (isErrorHeader(log)) {
			logMessage = new LogMessage(log, LoggingConstants.RegexPatterns.LOG_ERROR);
		} else if (isFatalHeader(log)) {
			logMessage = new LogMessage(log, LoggingConstants.RegexPatterns.LOG_FATAL);
		}
		return logMessage;
	}

	private LogMessage[] getTenantLogMessages(String logFile, int maxLogs, int start, int end,
			int tenantId, String serviceName) throws LogViewerException {
		ArrayList<LogMessage> logsList = new ArrayList<LogMessage>();
		String errorLine = "";
		InputStream logStream;
		if (end > maxLogs) {
			end = maxLogs;
		}
		try {
			logStream = getInputStream(logFile, tenantId, serviceName);
		} catch (Exception e) {
			throw new LogViewerException("Cannot find the specified file location to the log file",
					e);
		}
		BufferedReader dataInput = new BufferedReader(new InputStreamReader(logStream));
		int index = 1;
		String line;
		boolean isSyslogFile;
		try {
			isSyslogFile = isSyslogOn();
		} catch (Exception e1) {
			throw new LogViewerException("Cannot validate syslog appender", e1);
		}
		try {
			while ((line = dataInput.readLine()) != null) {
				if (isSyslogFile) {// remove unwanted characters which are
									// generated by the syslog server.
					line = removeSyslogHeader(line);
				}
				line = cleanLogHeader(line, tenantId);
				if ((index <= end && index > start)) {
					// When if a log entry has multiple lines (ie exception ect)
					// it waits for valid log header,
					// and add the multiple log lines to the specific log header
					// (since we are reading from bottom up
					// those multiple lines belongs to the next valid log
					// header.
					LogMessage logMessage = null;
					if (isErrorHeader(line)) {
						if (!errorLine.equals("")) {
							errorLine = (String) errorLine.subSequence(0, (errorLine.length() - 1));
							logMessage = getLogMessageForType(errorLine);
							if (logMessage != null) {
								logsList.add(logMessage);
							}
							errorLine = "";
						}
						// when there are log messages with multiple lines one
						// after the other
						// next line is also considered as a error line
						errorLine = line;
					} else if (isFatalHeader(line)) {
						if (!errorLine.equals("")) {
							errorLine = (String) errorLine.subSequence(0, (errorLine.length() - 1));
							logMessage = getLogMessageForType(errorLine);
							if (logMessage != null) {
								logsList.add(logMessage);
							}
							errorLine = "";
						}
						errorLine = line;
					} else if (isTraceHeader(line)) {
						if (!errorLine.equals("")) {
							errorLine = (String) errorLine.subSequence(0, (errorLine.length() - 1));
							logMessage = getLogMessageForType(errorLine);
							if (logMessage != null) {
								logsList.add(logMessage);
							}
							errorLine = "";
						}
						errorLine = line;
					} else if (isInfoHeader(line)) {
						if (!errorLine.equals("")) {
							errorLine = (String) errorLine.subSequence(0, (errorLine.length() - 1));
							logMessage = getLogMessageForType(errorLine);
							if (logMessage != null) {
								logsList.add(logMessage);
							}
							errorLine = "";
						}
						errorLine = line;
					} else if (isWarnHeader(line)) {
						if (!errorLine.equals("")) {
							errorLine = (String) errorLine.subSequence(0, (errorLine.length() - 1));
							logMessage = getLogMessageForType(errorLine);
							if (logMessage != null) {
								logsList.add(logMessage);
							}
							errorLine = "";
						}
						errorLine = line;
					} else if (isDebugHeader(line)) {
						if (!errorLine.equals("")) {
							errorLine = (String) errorLine.subSequence(0, (errorLine.length() - 1));
							logMessage = getLogMessageForType(errorLine);
							if (logMessage != null) {
								logsList.add(logMessage);
							}
							errorLine = "";
						}
						errorLine = line;
					} else if (!isLogHeader(line)) {
						// if a log line has no valid log header that log line
						// is considered as a error line.
						errorLine = errorLine + line + LoggingConstants.RegexPatterns.NEW_LINE;
					} else if (isLogHeader(line)) {
						if (!errorLine.equals("")) {
							errorLine = (String) errorLine.subSequence(0, (errorLine.length() - 1));
							logMessage = getLogMessageForType(errorLine);
							if (logMessage != null) {
								logsList.add(logMessage);
							}
							errorLine = "";
						}
						logMessage = getLogMessageForType(line);
						if (logMessage != null) {
							logsList.add(logMessage);
						}
					} else {
						log.warn("The log message  " + line + " is ignored.");
					}
				}
				index++;
			}
			if (!errorLine.equals("")) {
				LogMessage logMessage = getLogMessageForType(errorLine);
				if (logMessage != null) {
					logsList.add(logMessage);
				} else {
					log.warn("The log message " + errorLine + " is ignored.");
				}
			}
			dataInput.close();
		} catch (IOException e) {
			throw new LogViewerException("Cannot read the log file", e);
		}
		return logsList.toArray(new LogMessage[logsList.size()]);
	}

	/*
	 * Read log messages from the bottom level.
	 */
	private LogMessage[] getBottomUpLogMessages(String logFile, int maxLogs, int start, int end,
			int tenantId, String serviceName) throws LogViewerException {
		start = (maxLogs - start) + 1;
		end = (start - 200);
		if (start > maxLogs) {
			start = maxLogs;
		}
		if (end > maxLogs) {
			end = maxLogs;
		}
		if (end < 0) {
			end = 0;
		}
		LogMessage[] logs = getTenantLogMessages(logFile, maxLogs, end, start, tenantId,
				serviceName);
		int maxLen = logs.length;
		LogMessage[] botomupLogs = new LogMessage[maxLen];
		for (int i = 0, j = maxLen - 1; i < maxLen; i++, j--) {
			botomupLogs[i] = logs[j];
		}
		return botomupLogs;
	}

	private InputStream getLocalInputStream(String logFile) throws FileNotFoundException {
		logFile = logFile.substring(logFile.lastIndexOf(System.getProperty("file.separator"))+1);
		String fileName = CarbonUtils.getCarbonLogsPath() + LoggingConstants.URL_SEPARATOR
				+ logFile;
		InputStream is = new BufferedInputStream(new FileInputStream(fileName));
		return is;
	}

	private InputStream getInputStream(String logFile, int tenantId, String serviceName)
			throws LogViewerException {
		InputStream inputStream;
		try {
			if (isSyslogOn()) {
				inputStream = getLogDataStream(logFile, tenantId, serviceName);
			} else {
				if (isSuperTenantUser()) {
					inputStream = getLocalInputStream(logFile);
				} else {
					throw new LogViewerException("Syslog Properties are not properly configured");
				}
			}
			return inputStream;
		} catch (Exception e) {
			throw new LogViewerException("Error getting the file inputstream", e);
		}

	}

	/*
	 * get logs from the local file system.
	 */
	public LogInfo[] getLocalLogInfo(String domain, String serverKey) {
        String folderPath = CarbonUtils.getCarbonLogsPath();
		LogInfo log = null;
        if((((domain.equals("") || domain == null) && isSuperTenantUser()) ||
                domain.equalsIgnoreCase(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) &&
           (serverKey == null || serverKey.equals("") || serverKey.equalsIgnoreCase(getCurrentServerName()))) {

            ArrayList<LogInfo> logs = new ArrayList<LogInfo>();
            File folder = new File(folderPath);
            FileFilter fileFilter = new WildcardFileFilter(
                    LoggingConstants.RegexPatterns.LOCAL_CARBON_LOG_PATTERN);
            File[] listOfFiles = folder.listFiles(fileFilter);
            for (File file : listOfFiles) {
                String filename = file.getName();
                String fileDates[] = filename
                        .split(LoggingConstants.RegexPatterns.LOG_FILE_DATE_SEPARATOR);
                String filePath = CarbonUtils.getCarbonLogsPath() + LoggingConstants.URL_SEPARATOR
                        + filename;
                File logfile = new File(filePath);
                if (fileDates.length == 2) {
                    log = new LogInfo(filename, fileDates[1], getFileSize(logfile));
                } else {
                    log = new LogInfo(filename, LoggingConstants.RegexPatterns.CURRENT_LOG,
                            getFileSize(logfile));
                }
                if (log != null) {
                    logs.add(log);
                }
            }
            return getSortedLogInfo(logs.toArray(new LogInfo[logs.size()]));
        } else {
            return null;
        }

	}

	private String getFileSize(File file) {
		long bytes = file.length();
		int unit = 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		char pre = "KMGTPE".charAt(exp - 1);
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	/**
	 * Get Log file index from log collector server.
	 * 
	 * @param tenantId
	 * @param serviceName
	 * @return LogInfo {Log Name, Date, Size}
	 * @throws LogViewerException
	 */
	private LogInfo[] getLogInfo(int tenantId, String serviceName) throws LogViewerException {
		InputStream logStream;
		try {
			logStream = getLogDataStream("", tenantId, serviceName);
		} catch (HttpException e) {
			throw new LogViewerException("Cannot establish the connection to the syslog server", e);
		} catch (IOException e) {
			throw new LogViewerException("Cannot find the specified file location to the log file",
					e);
		} catch (Exception e) {
			throw new LogViewerException("Cannot find the specified file location to the log file",
					e);
		}
		BufferedReader dataInput = new BufferedReader(new InputStreamReader(logStream));
		String line;
		ArrayList<LogInfo> logs = new ArrayList<LogInfo>();
		Pattern pattern = Pattern.compile(LoggingConstants.RegexPatterns.SYS_LOG_FILE_NAME_PATTERN);
		try {
			while ((line = dataInput.readLine()) != null) {
				String fileNameLinks[] = line
						.split(LoggingConstants.RegexPatterns.LINK_SEPARATOR_PATTERN);
				String fileDates[] = line
						.split((LoggingConstants.RegexPatterns.SYSLOG_DATE_SEPARATOR_PATTERN));
				String dates[] = null;
				String sizes[] = null;
				if (fileDates.length == 3) {
					dates = fileDates[1]
							.split(LoggingConstants.RegexPatterns.COLUMN_SEPARATOR_PATTERN);
					sizes = fileDates[2]
							.split(LoggingConstants.RegexPatterns.COLUMN_SEPARATOR_PATTERN);
				}
				if (fileNameLinks.length == 2) {
					String logFileName[] = fileNameLinks[1]
							.split(LoggingConstants.RegexPatterns.GT_PATTARN);
					Matcher matcher = pattern.matcher(logFileName[0]);
					if (matcher.find()) {
						if (logFileName != null && dates != null && sizes != null) {
							String logName = logFileName[0].replace(
									LoggingConstants.RegexPatterns.BACK_SLASH_PATTERN, "");
							logName = logName.replaceAll("%20", " ");
							LogInfo log = new LogInfo(logName, dates[0], sizes[0]);
							logs.add(log);
						}
					}
				}
			}
			dataInput.close();
		} catch (IOException e) {
			throw new LogViewerException("Cannot find the specified file location to the log file",
					e);
		}
		return getSortedLogInfo(logs.toArray(new LogInfo[logs.size()]));
	}

	/*
	 * Filter log messages according to the keyword/log type.
	 */
	private LogMessage[] searchTenantLog(String type, String keyword, String logFile,
			String logIndex, int maxLines, int start, int end, int tenantId, String serviceName)
			throws LogViewerException {

		if ("ALL".equalsIgnoreCase(type)) {
			return getTenantLogsForKey(keyword, logFile, logIndex, maxLines, start, end, tenantId,
					serviceName);

		} else {
			LogMessage[] filerByType = getTenantLogsForType(type, logFile, logIndex, maxLines,
					start, end, tenantId, serviceName);
			List<LogMessage> resultList = new ArrayList<LogMessage>();
			for (int i = 0; i < filerByType.length; i++) {
				String logMessage = filerByType[i].getLogMessage();
				if (logMessage != null
						&& logMessage.toLowerCase().indexOf(keyword.toLowerCase()) > -1) {
					resultList.add(filerByType[i]);
				}
			}
			if (resultList.isEmpty()) {
				return NO_LOGS_MESSAGE;
			}
			return resultList.toArray(new LogMessage[resultList.size()]);
		}
	}

	private LogMessage[] searchBottomUpLogsTenantLog(String type, String keyword, String logFile,
			int maxLines, int start, int end, int tenantId, String serviceName)
			throws LogViewerException {
		if ("ALL".equalsIgnoreCase(type)) {
			return getBottomUpLogsForKey(keyword, logFile, maxLines, start, end, tenantId,
					serviceName);

		} else {
			LogMessage[] filerByType = getBottomUpLogsForType(type, logFile, maxLines, start, end,
					tenantId, serviceName);
			List<LogMessage> resultList = new ArrayList<LogMessage>();
			for (int i = 0; i < filerByType.length; i++) {
				String logMessage = filerByType[i].getLogMessage();
				if (logMessage != null
						&& logMessage.toLowerCase().indexOf(keyword.toLowerCase()) > -1) {
					resultList.add(filerByType[i]);
				}
			}
			if (resultList.isEmpty()) {
				return NO_LOGS_MESSAGE;
			}
			return resultList.toArray(new LogMessage[resultList.size()]);
		}
	}

	private boolean isValidPattern(String expression, String str) {
		CharSequence inputStr = str;
		Pattern pattern = Pattern.compile(expression);
		Matcher matcher = pattern.matcher(inputStr);
		return matcher.find();
	}

	private boolean isErrorHeader(String line) {
		return isValidPattern(LoggingConstants.RegexPatterns.LOG_ERROR_HEADER_PATTERN, line);
	}

	private boolean isInfoHeader(String line) {
		return isValidPattern(LoggingConstants.RegexPatterns.LOG_INFO_HEADER_PATTERN, line);
	}

	private boolean isDebugHeader(String line) {
		return isValidPattern(LoggingConstants.RegexPatterns.LOG_DEBUG_HEADER_PATTERN, line);
	}

	private boolean isWarnHeader(String line) {
		return isValidPattern(LoggingConstants.RegexPatterns.LOG_WARN_HEADER_PATTERN, line);
	}

	private boolean isTraceHeader(String line) {
		return isValidPattern(LoggingConstants.RegexPatterns.LOG_TRACE_HEADER_PATTERN, line);
	}

	private boolean isFatalHeader(String line) {
		return isValidPattern(LoggingConstants.RegexPatterns.LOG_FATAL_HEADER_PATTERN, line);
	}

	private boolean isLogHeader(String line) {
		return isValidPattern(LoggingConstants.RegexPatterns.LOG_HEADER_PATTERN, line);
	}

	public int getTenantIdForDomain(String tenantDomain) throws LogViewerException {
		int tenantId;
		TenantManager tenantManager = LoggingServiceComponent.getTenantManager();
		if (tenantDomain == null || tenantDomain.equals("")) {
			tenantId = MultitenantConstants.SUPER_TENANT_ID;
		} else {

			try {
				tenantId = tenantManager.getTenantId(tenantDomain);
			} catch (UserStoreException e) {
				throw new LogViewerException("Cannot find tenant id for the given tenant domain.");
			}
		}
		return tenantId;
	}

    private String getCurrentServerName() {
        String serverName = ServerConfiguration.getInstance().getFirstProperty("ServerKey");
        return serverName;
    }
}
