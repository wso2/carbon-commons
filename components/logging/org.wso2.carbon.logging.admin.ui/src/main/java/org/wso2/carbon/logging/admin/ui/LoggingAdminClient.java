/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.logging.admin.ui;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.logging.admin.stub.LoggingAdminStub;
import org.wso2.carbon.logging.admin.stub.types.carbon.AppenderData;
import org.wso2.carbon.logging.admin.stub.types.carbon.LogData;
import org.wso2.carbon.logging.admin.stub.types.carbon.LoggerData;
import org.wso2.carbon.logging.admin.stub.types.carbon.SyslogData;


import java.lang.Exception;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.HashMap;



public class LoggingAdminClient {
	private static final Log log = LogFactory.getLog(LoggingAdminClient.class);

	public LoggingAdminStub stub;

	public Map facilityMap;

	public LoggingAdminClient(String cookie, String backendServerURL,
			ConfigurationContext configCtx) throws AxisFault {

		String serviceURL = backendServerURL + "LoggingAdmin";
		stub = new LoggingAdminStub(configCtx, serviceURL);
		ServiceClient client = stub._getServiceClient();
		Options option = client.getOptions();
		option.setManageSession(true);
		option.setProperty(
				org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
				cookie);
	}

	public LogData getSysLog() throws Exception {
		try {
			return stub.getSystemLog();
		} catch (RemoteException e) {
			String msg = "Error occurred while getting Global logging configuration. Backend service may be unavailable";
			log.error(msg, e);
			throw e;
		}
	}

	public SyslogData getSysLogData() throws Exception {
		try {
			return stub.getSyslogData();
		} catch (RemoteException e) {
			String msg = "Error occurred while getting Syslog configuration. Backend service may be unavailable";
			log.error(msg, e);
			throw e;
		}
	}
	
	public void updateSyslogConfig(String url, String port, String realm,
			String userName, String password) throws Exception {
		try {
			stub.updateSyslogConfig(url, port, realm, userName, password);
		} catch (RemoteException e) {
			String msg = "Error occurred while getting logger data. Backend service may be unavailable";
			log.error(msg, e);
			throw e;
		}
	}
	public LoggerData[] getAllLoggerData(boolean beginsWith, String fileter)
			throws Exception {
		try {
			return stub.getAllLoggerData(fileter, beginsWith);
		} catch (RemoteException e) {
			String msg = "Error occurred while getting logger data. Backend service may be unavailable";
			log.error(msg, e);
			throw e;
		}
	}

	public void updateSystemLog(String logLevel, String logPattern,
			boolean persist) throws Exception, RemoteException {
		try {
			stub.updateSystemLog(logLevel, logPattern, persist);
		} catch (Exception e) {
			String msg = "Error occurred while updating globla log4j configuration.";
			log.error(msg, e);
			throw e;
		}
	}

	public String[] getLogLevels() {
		return new String[] { "OFF", "TRACE", "DEBUG", "INFO", "WARN", "ERROR",
				"FATAL" };
	}

	public Map getAppenderFacilities() {

		if (this.facilityMap != null) {
			return facilityMap;
		} else {
			Map<String, String> facilityMap = new HashMap<String, String>();
			facilityMap.put("kern", "LOG_KERN");
			facilityMap.put("user", "LOG_USER");
			facilityMap.put("mail", "LOG_MAIL");
			facilityMap.put("daemon", "LOG_DAEMON");
			facilityMap.put("auth", "LOG_AUTH");
			facilityMap.put("syslog", "LOG_SYSLOG");
			facilityMap.put("lpr", "LOG_LPR");
			facilityMap.put("news", "LOG_NEWS");
			facilityMap.put("uucp", "LOG_UUCP");
			facilityMap.put("cron", "LOG_CRON");
			facilityMap.put("authpriv", "LOG_AUTHPRIV");
			facilityMap.put("ftp", "LOG_FTP");
			facilityMap.put("local0", "LOG_LOCAL0");
			facilityMap.put("local1", "LOG_LOCAL1");
			facilityMap.put("local2", "LOG_LOCAL2");
			facilityMap.put("local3", "LOG_LOCAL3");
			facilityMap.put("local4", "LOG_LOCAL4");
			facilityMap.put("local5", "LOG_LOCAL5");
			facilityMap.put("local6", "LOG_LOCAL6");
			facilityMap.put("local6", "LOG_LOCAL7");
			return facilityMap;
		}
	}

	public void updateLoggerData(String loggerName, String logLevel,
			boolean additivity, boolean persist) throws Exception {
		try {
			stub.updateLoggerData(loggerName, logLevel, additivity, persist);
		} catch (Exception e) {
			String msg = "Error occurred while updating log4j logger configuration.";
			log.error(msg, e);
			throw e;
		}
	}

	public AppenderData getAppenderData(String appenderName) throws Exception {
		try {
			return stub.getAppenderData(appenderName);
		} catch (RemoteException e) {
			String msg = "Error occurred while getting log4j appender data.";
			log.error(msg, e);
			throw e;
		}
	}

	public void updateAppenderData(String appenderName, String logPattern,
			String threshold, String logFile, String sysLogHost,
			String facility, boolean persist) throws Exception {
		try {
			stub.updateAllAppenderData(appenderName, logPattern, threshold,
					logFile, sysLogHost, facility, persist);
		} catch (Exception e) {
			String msg = "Error occurred while updating log4j appender configuration.";
			log.error(msg, e);
			throw e;
		}
	}
	
	public String removeSyslogPattern(String appenderPattern) throws Exception {
		try {
			return stub.removeSyslogPattern(appenderPattern);
		} catch (Exception e) {
			String msg = "Error occurred while updating log4j appender configuration.";
			log.error(msg, e);
			throw e;
		}
	}

	public void restoreToDefaults() throws Exception {
		try {
			ServiceClient client = stub._getServiceClient();
			Options option = client.getOptions();
			option.setTimeOutInMilliSeconds(1000 * 180);
			stub.restoreDefaults();
		} catch (Exception e) {
			String msg = "Error occurred while restoring global Log4j configuration.";
			log.error(msg, e);
			throw e;
		}
	}
	
	public boolean isStratosService () throws Exception {
		try {
			return stub.isStratosService();
		} catch (Exception e) {
			String msg = "Error occurred while updating log4j appender configuration.";
			log.error(msg, e);
			throw e;
		}
	}
}