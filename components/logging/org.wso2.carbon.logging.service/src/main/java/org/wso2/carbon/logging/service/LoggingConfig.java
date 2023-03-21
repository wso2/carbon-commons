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
 *
 */

package org.wso2.carbon.logging.service;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.PropertiesConfigurationLayout;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.logging.service.data.LoggerData;
import org.wso2.carbon.logging.service.util.Utils;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This is the Admin service used for obtaining Log4J2 information about the system and also used for
 * managing the system Log4J2 configuration.
 */
public class LoggingConfig {
    private static final Log log = LogFactory.getLog(LoggingConfig.class);

    private String filePath = System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH) + File.separator
            + "log4j2.properties";
    private File logPropFile = new File(filePath);

    private PropertiesConfiguration config;
    private PropertiesConfigurationLayout layout;

    private static final String LOGGER_PREFIX = "logger.";
    private static final String LOGGER_LEVEL_SUFFIX = ".level";
    private static final String LOGGER_NAME_SUFFIX = ".name";
    private static final String LOGGERS_PROPERTY = "loggers";
    private static final String AUDIT_SERVER_URL = "appender.AUDIT_LOGFILE.url";
    private static final String AUDIT_SERVER_URL_TYPE = "appender.AUDIT_LOGFILE.type";
    private static final String AUDIT_SERVER_CONN_TIMEOUT = "appender.AUDIT_LOGFILE.connectTimeoutMillis";
    private static final String AUDIT_SERVER_FILENAME = "appender.AUDIT_LOGFILE.fileName";
    private static final String AUDIT_SERVER_FILE_PATTERN = "appender.AUDIT_LOGFILE.filePattern";
    private static final String AUDIT_SERVER_POLICIES_TYPE= "appender.AUDIT_LOGFILE.policies.type";
    private static final String AUDIT_SERVER_POLICIES_TIME_TYPE = "appender.AUDIT_LOGFILE.policies.time.type";
    private static final String AUDIT_SERVER_POLICIES_TIME_INTERVAL = "appender.AUDIT_LOGFILE.policies.time.interval";
    private static final String AUDIT_SERVER_POLICIES_TIME_MODULATE = "appender.AUDIT_LOGFILE.policies.time.modulate";
    private static final String AUDIT_SERVER_POLICIES_SIZE_TYPE = "appender.AUDIT_LOGFILE.policies.size.type";
    private static final String AUDIT_SERVER_POLICIES_SIZE_SIZE = "appender.AUDIT_LOGFILE.policies.size.size";
    private static final String AUDIT_SERVER_STRATEGY_TYPE = "appender.AUDIT_LOGFILE.strategy.type";
    private static final String AUDIT_SERVER_STRATEGY_MAX = "appender.AUDIT_LOGFILE.strategy.max";
    private static final String AUDIT_SERVER_STRATEGY_TAX = "appender.AUDIT_LOGFILE.strategy.tax";
    private static final String ROOT_LOGGER = "rootLogger";

    public String getLoggers() throws IOException {
        return Utils.getProperty(logPropFile, LOGGERS_PROPERTY);
    }

    public LoggerData getLoggerData(String loggerName) throws IOException {
        String logLevel = "";
        String componentName = "-";

        if (loggerName.equals(ROOT_LOGGER)) {
            logLevel = Utils.getProperty(logPropFile, loggerName + LOGGER_LEVEL_SUFFIX);
        } else {
            componentName = Utils.getProperty(logPropFile, LOGGER_PREFIX + loggerName + LOGGER_NAME_SUFFIX);
            logLevel = Utils.getProperty(logPropFile, LOGGER_PREFIX + loggerName + LOGGER_LEVEL_SUFFIX);
        }

        return new LoggerData(loggerName, logLevel, componentName);
    }

    public LoggerData[] getAllLoggerData(boolean beginsWith, String logNameFilter) throws IOException {
        List<LoggerData> list = new ArrayList<LoggerData>();
        if (logNameFilter != null) {
            logNameFilter = logNameFilter.trim();
        }

        String[] loggers = getLoggers().split(",");
        for (String logger : loggers) {
            if ((logNameFilter != null && beginsWith && logger.startsWith(logNameFilter)) || // Logger name begins with logNameFilter
                    (logNameFilter != null && !beginsWith && logger.indexOf(logNameFilter) != -1) || // Logger name contains logNameFilter
                    (logNameFilter == null || logNameFilter.trim().length() == 0)) {  // No logNameFilter specified
                LoggerData data = getLoggerData(logger.trim());
                list.add(data);
            }
        }
        Collections.sort(list,
                new Comparator<LoggerData>() {
                    public int compare(LoggerData arg0, LoggerData arg1) {
                        return arg0.getName().compareTo(arg1.getName());
                    }
                });

        LoggerData rootLoggerData = getLoggerData(ROOT_LOGGER);
        if ((logNameFilter != null && beginsWith && rootLoggerData.getName().startsWith(logNameFilter)) || // Logger name begins with logNameFilter
                (logNameFilter != null && !beginsWith && rootLoggerData.getName().indexOf(logNameFilter) != -1) || // Logger name contains logNameFilter
                (logNameFilter == null || logNameFilter.trim().length() == 0)) {  // No logNameFilter specified
            list.add(0, rootLoggerData);
        }

        return list.toArray(new LoggerData[list.size()]);
    }

    public void addLogger(String loggerName, String loggerClass, String logLevel) throws IOException, ConfigurationException {
        loadConfigs();
        String modifiedLogger = getLoggers().concat(", ").concat(loggerName);
        config.setProperty(LOGGERS_PROPERTY, modifiedLogger);
        config.setProperty(LOGGER_PREFIX + loggerName + LOGGER_NAME_SUFFIX, loggerClass);
        config.setProperty(LOGGER_PREFIX + loggerName + LOGGER_LEVEL_SUFFIX, logLevel);
        applyConfigs();
    }

    private void loadConfigs() throws FileNotFoundException, ConfigurationException {
        config = new PropertiesConfiguration();
        layout = new PropertiesConfigurationLayout(config);
        layout.load(new InputStreamReader(new FileInputStream(logPropFile)));
    }

    public void addRemoteServerConfig(String url, String connectTimeoutMillis) throws IOException,
            ConfigurationException {
        loadConfigs();
        getLoggerData("synapse-wire");
        config.setProperty(AUDIT_SERVER_URL_TYPE, "http");
        config.clearProperty(AUDIT_SERVER_FILENAME);
        config.clearProperty(AUDIT_SERVER_FILE_PATTERN);
        config.clearProperty(AUDIT_SERVER_POLICIES_TYPE);
        config.clearProperty(AUDIT_SERVER_POLICIES_TIME_TYPE);
        config.clearProperty(AUDIT_SERVER_POLICIES_TIME_INTERVAL);
        config.clearProperty(AUDIT_SERVER_POLICIES_TIME_MODULATE);
        config.clearProperty(AUDIT_SERVER_POLICIES_SIZE_TYPE);
        config.clearProperty(AUDIT_SERVER_POLICIES_SIZE_SIZE);
        config.clearProperty(AUDIT_SERVER_STRATEGY_TYPE);
        config.clearProperty(AUDIT_SERVER_STRATEGY_MAX);
        config.setProperty(AUDIT_SERVER_URL, url);
        config.setProperty(AUDIT_SERVER_CONN_TIMEOUT, connectTimeoutMillis);
        config.clearProperty(AUDIT_SERVER_STRATEGY_TAX);
        applyConfigs();
    }

//    public void removeRemoteServerConfig(String loggerName, String loggerClass, String url) throws IOException, ConfigurationException {
//        loadConfigs();
//        config.clearProperty(AUDIT_SERVER_URL);
//        applyConfigs();
//        //log an audit log
//    }

    private void applyConfigs() throws IOException, ConfigurationException {
        layout.save(new FileWriter(filePath, false));
    }

    public boolean isLoggerExist(String loggerName) throws IOException {
        String logger = getLoggers();
        String[] loggers = logger.split(",");
        return Arrays.asList(loggers).contains(loggerName);
    }
}
