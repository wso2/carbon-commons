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

package org.wso2.carbon.logging.registry;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.net.SyslogAppender;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.logging.appender.CarbonMemoryAppender;
import org.wso2.carbon.utils.logging.CircularBuffer;
import org.wso2.carbon.logging.config.SyslogConfigManager;
import org.wso2.carbon.logging.config.SyslogConfiguration;
import org.wso2.carbon.logging.service.data.SyslogData;
import org.wso2.carbon.logging.util.LoggingConstants;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.ServerConstants;

public class RegistryManager {
	private static Log log = LogFactory.getLog(RegistryManager.class);

	private static Registry registry;

	public static void setRegistry(Registry registryParam) {
		registry = registryParam;
	}

	public String getConfigurationProperty(String propertyName)
			throws RegistryException {
		String resourcePath = RegistryResources.LOGGING + propertyName;
		String value = null;
		if (registry.resourceExists(resourcePath)) {
			Resource resource = registry.get(resourcePath);
			value = resource.getProperty(propertyName);
		}
		return value;
	}

	public void updateConfigurationProperty(String propertyName, String value)
			throws RegistryException {
		if (propertyName == null || value == null) {
			return;
		}
		String resourcePath = RegistryResources.LOGGING + propertyName;
		Resource resource;
		if (!registry.resourceExists(resourcePath)) {
			resource = registry.newResource();
			resource.addProperty(propertyName, value);
			registry.put(resourcePath, resource);
		} else {
			resource = registry.get(resourcePath);
			String existingValue = resource.getProperty(propertyName);
			if (!(existingValue != null && existingValue.equals(value))) {
				resource.setProperty(propertyName, value);
				registry.put(resourcePath, resource);
			}
		}
	}

	/**
	 * when getting loggers from getCurrentLoggers method it will return all the
	 * loggers in the system. this includes loggers we have initialized using
	 * getLogger methods. but these loggers does not have a log level set they
	 * get the log level from the parent loggers. but to store in registry we
	 * use only loggers with a log level
	 * <p/>
	 * Store the provided Log4J Logger in the Registry
	 * 
	 * @param logger
	 *            The Log4J Logger to be stored in the registry
	 */
	public void addLogger(Logger logger) throws Exception {
		try {
			registry.beginTransaction();
			Resource loggerResource = registry.newResource();
			loggerResource.addProperty(LoggingConstants.LoggerProperties.NAME,
					logger.getName());
			loggerResource.addProperty(
					LoggingConstants.LoggerProperties.LOG_LEVEL, logger
							.getEffectiveLevel().toString());
			loggerResource.addProperty(
					LoggingConstants.LoggerProperties.ADDITIVITY,
					Boolean.toString(logger.getAdditivity()));

			registry.put(LoggingConstants.LOGGERS + logger.getName(),
					loggerResource);
			registry.commitTransaction();
		} catch (Exception e) {
			registry.rollbackTransaction();
			log.error("Unable to add the logger", e);
			throw e;
		}
	}

	public Resource getLogger(String loggerName) throws Exception {
		String loggerResourcePath = LoggingConstants.LOGGERS + loggerName;
		if (registry.resourceExists(loggerResourcePath)) {
			return registry.get(loggerResourcePath);
		}
		return null;
	}

	public Collection getLoggers() throws Exception {
		if (registry.resourceExists(LoggingConstants.LOGGERS)) {
			return (Collection) registry.get(LoggingConstants.LOGGERS);
		}
		return null;
	}

	public void updateLogger(String loggerName, String loggerLevel,
			boolean aditivity) throws Exception {
		try {
			registry.beginTransaction();
			Resource loggerResource;
			String loggerResourcePath = LoggingConstants.LOGGERS + loggerName;
			if (registry.resourceExists(loggerResourcePath)) {
				loggerResource = registry.get(loggerResourcePath);
				loggerResource.setProperty(
						LoggingConstants.LoggerProperties.LOG_LEVEL,
						loggerLevel);
				loggerResource.setProperty(
						LoggingConstants.LoggerProperties.ADDITIVITY,
						Boolean.toString(aditivity));
				registry.put(loggerResourcePath, loggerResource);
			} else {
				loggerResource = registry.newResource();
				loggerResource.addProperty(
						LoggingConstants.LoggerProperties.NAME, loggerName);
				loggerResource.addProperty(
						LoggingConstants.LoggerProperties.LOG_LEVEL,
						loggerLevel);
				loggerResource.addProperty(
						LoggingConstants.LoggerProperties.ADDITIVITY,
						Boolean.toString(aditivity));
				registry.put(loggerResourcePath, loggerResource);
			}
			registry.commitTransaction();
		} catch (Exception e) {
			registry.rollbackTransaction();
			log.error("Unable to update the logger", e);
			throw e;
		}
	}

	private String encriptSyslogPassword(String password)
			throws CryptoException {
		return CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(
				password.getBytes());
	}

	private String decriptPassword(String encriptedPassword)
			throws CryptoException, IOException {
		return new String(CryptoUtil.getDefaultCryptoUtil()
				.base64DecodeAndDecrypt(encriptedPassword));
	}



	public void addSyslogConfig(SyslogData syslogData) throws Exception {
		try {
			registry.beginTransaction();
			Resource syslogResource = registry.newResource();
			syslogResource.addProperty(
					LoggingConstants.SyslogProperties.LOG_INDEX_URL,
					syslogData.getUrl());
			syslogResource.addProperty(
					LoggingConstants.SyslogProperties.SYSLOG_PORT,
					syslogData.getPort());
			syslogResource.addProperty(LoggingConstants.SyslogProperties.REALM,
					syslogData.getRealm());
			syslogResource.addProperty(
					LoggingConstants.SyslogProperties.USER_NAME,
					syslogData.getUserName());
			syslogResource.addProperty(
					LoggingConstants.SyslogProperties.PASSWORD,
					encriptSyslogPassword(syslogData.getPassword()));
			registry.put(LoggingConstants.SYSLOG, syslogResource);
			registry.commitTransaction();
		} catch (RegistryException e) {
			registry.rollbackTransaction();
			log.error("Cannot add syslog properties ", e);
		}
	}

	/**
	 * Store the provided Log4J Appender in the Registry
	 * 
	 * @param appender
	 *            Log4J Appender to be stored in the registry
	 */
	public void addAppender(Appender appender) throws Exception {
		try {
			registry.beginTransaction();
			Resource appenderResource = registry.newResource();
			if (appender.requiresLayout()) {
				Layout layout = appender.getLayout();
				if (layout instanceof PatternLayout) {
					appenderResource.addProperty(
							LoggingConstants.AppenderProperties.PATTERN,
							((PatternLayout) layout).getConversionPattern());
				}
			}

			if (appender instanceof FileAppender) {
				FileAppender fileAppender = (FileAppender) appender;
				String fileName = fileAppender.getFile();
				File logFile = new File(fileName);
				if (!logFile.isAbsolute()) {
					if (fileName.startsWith(".")) {
						fileName = fileName.substring(1);
					}
					fileName = (System.getProperty(ServerConstants.CARBON_HOME)
							+ "/" + fileName).replace('\\', '/');
					fileAppender.setFile(fileName);
					fileAppender.activateOptions();
				}
				appenderResource.addProperty(
						LoggingConstants.AppenderProperties.LOG_FILE_NAME,
						fileName);
				appenderResource.addProperty(
						LoggingConstants.AppenderProperties.IS_FILE_APPENDER,
						Boolean.toString(true));
			} else {
				appenderResource.addProperty(
						LoggingConstants.AppenderProperties.IS_FILE_APPENDER,
						Boolean.toString(false));
			}

			if (appender instanceof CarbonMemoryAppender) {
				CarbonMemoryAppender memoryAppender = (CarbonMemoryAppender) appender;
				memoryAppender.setCircularBuffer(new CircularBuffer(
						LoggingConstants.MEMORY_APPENDER_BUFFER_SZ));
				memoryAppender.activateOptions();
			}

			// normally all the appenders inherit from AppenderSkelton
			if (appender instanceof AppenderSkeleton) {
				AppenderSkeleton appenderSkeleton = (AppenderSkeleton) appender;
				if (appenderSkeleton.getThreshold() != null) {
					appenderResource.addProperty(
							LoggingConstants.AppenderProperties.THRESHOLD,
							appenderSkeleton.getThreshold().toString());
				} else {
					appenderResource.addProperty(
							LoggingConstants.AppenderProperties.THRESHOLD,
							"DEBUG");
				}
			}

			if (appender instanceof SyslogAppender) {
				SyslogAppender syslogAppender = (SyslogAppender) appender;

				// if user has not set these properties system automatically
				// assign default values
				appenderResource.addProperty(
						LoggingConstants.AppenderProperties.SYS_LOG_HOST,
						syslogAppender.getSyslogHost());
				appenderResource.addProperty(
						LoggingConstants.AppenderProperties.FACILITY,
						syslogAppender.getFacility());
				appenderResource
						.addProperty(
								LoggingConstants.AppenderProperties.IS_SYS_LOG_APPENDER,
								Boolean.toString(true));
			} else {
				appenderResource
						.addProperty(
								LoggingConstants.AppenderProperties.IS_SYS_LOG_APPENDER,
								Boolean.toString(false));
			}
			registry.put(LoggingConstants.APPENDERS + appender.getName(),
					appenderResource);
			registry.commitTransaction();
		} catch (Exception e) {
			registry.rollbackTransaction();
			log.error("Cannot add appender", e);
		}
	}

	public void updateCassandraConfig(String url, String keyspace,
			String columnFamily, String userName, String password)
			throws Exception {
		String cassandraResourcePath = LoggingConstants.CASSANDRA;
		try {
			if (registry.resourceExists(cassandraResourcePath)) {
				try {
					registry.beginTransaction();
					Resource appenderResource = registry
							.get(cassandraResourcePath);
					appenderResource.setProperty(
							LoggingConstants.CassandraProperties.URL, url);
					appenderResource.setProperty(
							LoggingConstants.CassandraProperties.KEYSPACE,
							keyspace);
					appenderResource.setProperty(
							LoggingConstants.CassandraProperties.COLUMN_FAMILY,
							columnFamily);
					appenderResource.setProperty(
							LoggingConstants.CassandraProperties.USER_NAME,
							userName);
					appenderResource.setProperty(
							LoggingConstants.CassandraProperties.PASSWORD,
							encriptSyslogPassword(password));
					registry.put(LoggingConstants.CASSANDRA, appenderResource);
					registry.commitTransaction();
				} catch (RegistryException e) {
					registry.rollbackTransaction();
					log.error("Unable to update the appender", e);
					throw e;
				}
			} else {
				
			}
		} catch (RegistryException e) {
			log.error("Unable to update the appender", e);
			throw e;
		}
	}

	/**
	 * this method is used to update syslog properties in the registry if syslog
	 * property is not available a new resource is added to the registry along
	 * with the syslog properties
	 * 
	 * @param url
	 *            - syslog logs location
	 * @param port
	 *            - port
	 * @param realm
	 *            - realm
	 * @param userName
	 *            - syslog server authentic user name
	 * @param password
	 *            - syslog server authentic password
	 * @throws Exception
	 */
	public void updateSyslogConfig(String url, String port, String realm,
			String userName, String password) throws Exception {
		String syslogResourcePath = LoggingConstants.SYSLOG;
		try {
			if (registry.resourceExists(syslogResourcePath)) {
				try {
					registry.beginTransaction();
					Resource appenderResource = registry
							.get(syslogResourcePath);
					appenderResource.setProperty(
							LoggingConstants.SyslogProperties.LOG_INDEX_URL,
							url);
					appenderResource
							.setProperty(
									LoggingConstants.SyslogProperties.SYSLOG_PORT,
									port);
					appenderResource.setProperty(
							LoggingConstants.SyslogProperties.REALM, realm);
					appenderResource.setProperty(
							LoggingConstants.SyslogProperties.USER_NAME,
							userName);
					appenderResource.setProperty(
							LoggingConstants.SyslogProperties.PASSWORD,
							encriptSyslogPassword(password));
					registry.put(LoggingConstants.SYSLOG, appenderResource);
					registry.commitTransaction();
				} catch (RegistryException e) {
					registry.rollbackTransaction();
					log.error("Unable to update the appender", e);
					throw e;
				}
			} else {
				SyslogData syslogData = new SyslogData(url, port, realm,
						userName, password);
				addSyslogConfig(syslogData);
			}
		} catch (RegistryException e) {
			log.error("Unable to update the appender", e);
			throw e;
		}

	}
	
	public SyslogData getSyslogData() throws Exception {
		Resource syslogConfigResource;
		try {
			syslogConfigResource = getSyslogConfig();
			String url = "";
			String port = "";
			String realm = "";
			String userName = "";
			String password = "";
			if (syslogConfigResource != null) { // Check if the properties are
												// coming from the registry.
				url = syslogConfigResource
						.getProperty(LoggingConstants.SyslogProperties.LOG_INDEX_URL);
				port = syslogConfigResource
						.getProperty(LoggingConstants.SyslogProperties.SYSLOG_PORT);
				realm = syslogConfigResource
						.getProperty(LoggingConstants.SyslogProperties.REALM);
				userName = syslogConfigResource
						.getProperty(LoggingConstants.SyslogProperties.USER_NAME);
				password = decriptPassword(syslogConfigResource
						.getProperty(LoggingConstants.SyslogProperties.PASSWORD));
			} else { // read syslog properties from the syslog-config.xml
				SyslogConfiguration config = SyslogConfigManager
						.loadSyslogConfiguration();
				url = config.getSyslogHostURL();
				port = config.getPort();
				realm = config.getRealm();
				userName = config.getUserName();
				password = config.getPassword();
			}

			return new SyslogData(url, port, realm, userName, password);
		} catch (Exception e) {
			log.error("Unable get SyslogData ", e);
			throw e;
		}

	}

	/**
	 * @return Registry resource of syslog property file.
	 * @throws Exception
	 */
	public Resource getSyslogConfig() throws Exception {
		String syslogResourcePath = LoggingConstants.SYSLOG;
		if (registry.resourceExists(syslogResourcePath)) {
			return registry.get(syslogResourcePath);
		}
		return null;
	}

	public Resource getCassandraConfig() throws Exception {
		String cassandraResourcePath = LoggingConstants.CASSANDRA;
		if (registry.resourceExists(cassandraResourcePath)) {
			return registry.get(cassandraResourcePath);
		}
		return null;
	}
	
	public void updateAppender(Appender appender, String appenderName,
			String appenderPattern, String threshold, String logFileName,
			String sysLogHost, String facility, boolean isFileAppender,
			boolean isSysLogAppender) throws Exception {

		String appenderResourcePath = LoggingConstants.APPENDERS + appenderName;
		if (registry.resourceExists(appenderResourcePath)) {
			try {
				registry.beginTransaction();

				Resource appenderResource = registry.get(appenderResourcePath);
				appenderResource.setProperty(
						LoggingConstants.AppenderProperties.PATTERN,
						appenderPattern);

				if (isFileAppender) {
					appenderResource.setProperty(
							LoggingConstants.AppenderProperties.LOG_FILE_NAME,
							logFileName);
					appenderResource
							.setProperty(
									LoggingConstants.AppenderProperties.IS_FILE_APPENDER,
									Boolean.toString(true));
				}

				if (isSysLogAppender) {
					appenderResource.setProperty(
							LoggingConstants.AppenderProperties.SYS_LOG_HOST,
							sysLogHost);
					appenderResource.setProperty(
							LoggingConstants.AppenderProperties.FACILITY,
							facility);
					appenderResource
							.setProperty(
									LoggingConstants.AppenderProperties.IS_SYS_LOG_APPENDER,
									Boolean.toString(true));
				}

				appenderResource.setProperty(
						LoggingConstants.AppenderProperties.THRESHOLD,
						threshold);
				registry.put(LoggingConstants.APPENDERS + appenderName,
						appenderResource);

				registry.commitTransaction();
			} catch (Exception e) {
				registry.rollbackTransaction();
				log.error("Unable to update the appender", e);
				throw e;
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug("appender " + appenderName
						+ " is not available, therefore adding to registry");
			}
			addAppender(appender);
			updateAppender(appender, appenderName, appenderPattern, threshold,
					logFileName, sysLogHost, facility, isFileAppender,
					isSysLogAppender);
		}
	}

	public Resource getAppender(String appenderName) throws Exception {
		String appenderResourcePath = LoggingConstants.APPENDERS + appenderName;
		if (registry.resourceExists(appenderResourcePath)) {
			return registry.get(appenderResourcePath);
		}
		return null;
	}

	public Collection getAppenders() throws Exception {
		if (registry.resourceExists(LoggingConstants.APPENDERS)) {
			return (Collection) registry.get(LoggingConstants.APPENDERS);
		}
		return null;
	}

	/**
	 * method is used to check current appender is a syslog appender.
	 * 
	 * @return isSyslogAppender
	 * @throws RegistryException
	 */
	public boolean isSysLogAppender() throws RegistryException {
		boolean isSyslogAppender = false;
		String appenderResourcePath = LoggingConstants.APPENDERS
				+ LoggingConstants.WSO2CARBON_SYS_LOG_APPENDER;
		if (registry.resourceExists(appenderResourcePath)) {
			Resource appenderResource = registry.get(appenderResourcePath);
			isSyslogAppender = Boolean
					.parseBoolean(appenderResource
							.getProperty(LoggingConstants.AppenderProperties.IS_SYS_LOG_APPENDER));
		}
		return isSyslogAppender;
	}

	public void removeAllRegistryEntries() throws Exception {
		try {
			registry.beginTransaction();
			if (registry.resourceExists(LoggingConstants.LOGGERS)) {
				registry.delete(LoggingConstants.LOGGERS);
			}
			if (registry.resourceExists(LoggingConstants.APPENDERS)) {
				registry.delete(LoggingConstants.APPENDERS);
			}

			if (registry.resourceExists(RegistryResources.LOGGING
					+ LoggingConstants.SYSTEM_LOG_PATTERN)) {
				registry.delete(RegistryResources.LOGGING
						+ LoggingConstants.SYSTEM_LOG_PATTERN);
			}
			if (registry.resourceExists(RegistryResources.LOGGING
					+ LoggingConstants.SYSTEM_LOG_LEVEL)) {
				registry.delete(RegistryResources.LOGGING
						+ LoggingConstants.SYSTEM_LOG_LEVEL);
			}
			registry.commitTransaction();
		} catch (Exception e) {
			registry.rollbackTransaction();
			log.error("Unable to remove all the loggers and appenders", e);
			throw e;
		}
	}
}
