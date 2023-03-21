/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This is the Admin service used for configuring the remote server logging configurations
 */
public class LoggingConfig {
    private static final Log log = LogFactory.getLog(LoggingConfig.class);

    private String filePath = System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH) + File.separator
            + "log4j2.properties";
    private File logPropFile = new File(filePath);

    private PropertiesConfiguration config;
    private PropertiesConfigurationLayout layout;

    private void loadConfigs() throws FileNotFoundException, ConfigurationException {
        config = new PropertiesConfiguration();
        layout = new PropertiesConfigurationLayout(config);
        layout.load(new InputStreamReader(new FileInputStream(logPropFile)));
    }

    public void addRemoteServerConfig(String url, String connectTimeoutMillis)
            throws IOException, ConfigurationException {
        loadConfigs();
        config.setProperty(
                LoggingConstants.APPENDER_PREFIX + LoggingConstants.AUDIT_LOGFILE
                        + LoggingConstants.TYPE_SUFFIX, "http");
        config.clearProperty(
                LoggingConstants.APPENDER_PREFIX + LoggingConstants.AUDIT_LOGFILE
                        + LoggingConstants.FILENAME_SUFFIX);
        config.clearProperty(
                LoggingConstants.APPENDER_PREFIX + LoggingConstants.AUDIT_LOGFILE
                        + LoggingConstants.FILE_PATTERN_SUFFIX);
        config.clearProperty(
                LoggingConstants.APPENDER_PREFIX + LoggingConstants.AUDIT_LOGFILE
                        + LoggingConstants.POLICIES_SUFFIX + LoggingConstants.TYPE_SUFFIX);
        config.clearProperty(
                LoggingConstants.APPENDER_PREFIX + LoggingConstants.AUDIT_LOGFILE
                        + LoggingConstants.POLICIES_SUFFIX + LoggingConstants.TIME_SUFFIX
                        + LoggingConstants.TYPE_SUFFIX);
        config.clearProperty(
                LoggingConstants.APPENDER_PREFIX + LoggingConstants.AUDIT_LOGFILE
                        + LoggingConstants.POLICIES_SUFFIX + LoggingConstants.TIME_SUFFIX
                        + LoggingConstants.INTERVAL_SUFFIX);
        config.clearProperty(
                LoggingConstants.APPENDER_PREFIX + LoggingConstants.AUDIT_LOGFILE
                        + LoggingConstants.POLICIES_SUFFIX + LoggingConstants.TIME_SUFFIX
                        + LoggingConstants.MODULATE_SUFFIX);
        config.clearProperty(
                LoggingConstants.APPENDER_PREFIX + LoggingConstants.AUDIT_LOGFILE
                        + LoggingConstants.POLICIES_SUFFIX + LoggingConstants.SIZE_SUFFIX
                        + LoggingConstants.TYPE_SUFFIX);
        config.clearProperty(
                LoggingConstants.APPENDER_PREFIX + LoggingConstants.AUDIT_LOGFILE
                        + LoggingConstants.POLICIES_SUFFIX + LoggingConstants.SIZE_SUFFIX
                        + LoggingConstants.SIZE_SUFFIX);
        config.clearProperty(
                LoggingConstants.APPENDER_PREFIX + LoggingConstants.AUDIT_LOGFILE
                        + LoggingConstants.STRATEGY_SUFFIX + LoggingConstants.TYPE_SUFFIX);
        config.clearProperty(
                LoggingConstants.APPENDER_PREFIX + LoggingConstants.AUDIT_LOGFILE
                        + LoggingConstants.STRATEGY_SUFFIX + LoggingConstants.MAX_SUFFIX);
        config.setProperty(
                LoggingConstants.APPENDER_PREFIX + LoggingConstants.AUDIT_LOGFILE
                        + LoggingConstants.URL_SUFFIX, url);
        config.setProperty(
                LoggingConstants.APPENDER_PREFIX + LoggingConstants.AUDIT_LOGFILE
                        + LoggingConstants.CONNECTION_TIMEOUT_SUFFIX, connectTimeoutMillis);
        applyConfigs();
    }

    private void applyConfigs() throws IOException, ConfigurationException {
        layout.save(new FileWriter(filePath, false));
    }
}
