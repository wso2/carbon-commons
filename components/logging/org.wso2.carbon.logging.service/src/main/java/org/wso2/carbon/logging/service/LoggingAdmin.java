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

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.PropertiesConfigurationLayout;
import org.apache.commons.configuration2.io.FileHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.logging.service.data.LoggerData;
import org.wso2.carbon.logging.service.util.Utils;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This is the Admin service used for obtaining Log4J2 information about the system and also used for
 * managing the system Log4J2 configuration.
 */
public class LoggingAdmin {
    private static final Log log = LogFactory.getLog(LoggingAdmin.class);

    private String filePath = System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH) + File.separator
            + "log4j2.properties";
    private File logPropFile = new File(filePath);

    private PropertiesConfiguration config;
    private PropertiesConfigurationLayout layout;
    private FileHandler fileHandler;

    private static final String LOGGER_PREFIX = "logger.";
    private static final String LOGGER_LEVEL_SUFFIX = ".level";
    private static final String LOGGER_NAME_SUFFIX = ".name";
    private static final String LOGGERS_PROPERTY = "loggers";
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

    public void updateLoggerData(String loggerName, String logLevel) throws IOException, ConfigurationException {
        loadConfigs();
        if (loggerName.equals(ROOT_LOGGER)) {
            config.setProperty(loggerName + LOGGER_LEVEL_SUFFIX, logLevel);
            applyConfigs();
        } else {
            config.setProperty(LOGGER_PREFIX + loggerName + LOGGER_LEVEL_SUFFIX, logLevel);
            applyConfigs();
        }
    }

    public void addLogger(String loggerName, String loggerClass, String logLevel) throws IOException, ConfigurationException {
        loadConfigs();
        String modifiedLogger = getLoggers().concat(", ").concat(loggerName);
        config.setProperty(LOGGERS_PROPERTY, modifiedLogger);
        config.setProperty(LOGGER_PREFIX + loggerName + LOGGER_NAME_SUFFIX, loggerClass);
        config.setProperty(LOGGER_PREFIX + loggerName + LOGGER_LEVEL_SUFFIX, logLevel);
        applyConfigs();
    }

    private void loadConfigs() throws ConfigurationException {
        config = new PropertiesConfiguration();
        layout = new PropertiesConfigurationLayout();
        config.setLayout(layout);
        fileHandler = new FileHandler(config);
        fileHandler.setFile(logPropFile);
        fileHandler.load();
    }

    private void applyConfigs() throws ConfigurationException {
        fileHandler.save();
    }

    public boolean isLoggerExist(String loggerName) throws IOException {
        String logger = getLoggers();
        String[] loggers = logger.split(",");
        return Arrays.asList(loggers).contains(loggerName);
    }
}
