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
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.logging.service.util.Utils;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * This is the Admin service used for configuring the remote server logging configurations
 */
public class LoggingConfig {
    private static final Log log = LogFactory.getLog(LoggingConfig.class);
    private static final Log auditLog = CarbonConstants.AUDIT_LOG;

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
        ArrayList<String> list = Utils.getKeysOfAppender(logPropFile, LoggingConstants.AUDIT_LOGFILE);
        String layoutTypeKey = LoggingConstants.APPENDER_PREFIX + LoggingConstants.AUDIT_LOGFILE
                + LoggingConstants.LAYOUT_SUFFIX + LoggingConstants.TYPE_SUFFIX;
        String layoutTypePatternKey = LoggingConstants.APPENDER_PREFIX + LoggingConstants.AUDIT_LOGFILE
                + LoggingConstants.LAYOUT_SUFFIX + LoggingConstants.PATTERN_SUFFIX;
        String layoutTypePatternDefaultValue = LoggingConstants.DEFAULT_LAYOUT_PATTERN;
        String layoutTypePatternValue = null;
        for (String key : list) {
            if (layoutTypeKey.equals(key)) {
                String layoutTypeValue = Utils.getProperty(logPropFile, key);
                if (LoggingConstants.PATTERN_LAYOUT_TYPE.equals(layoutTypeValue)) {
                    layoutTypePatternValue = Utils.getProperty(logPropFile, layoutTypePatternKey);
                }
            }
            config.clearProperty(key);
        }
        config.setProperty(
                LoggingConstants.APPENDER_PREFIX + LoggingConstants.AUDIT_LOGFILE + LoggingConstants.TYPE_SUFFIX,
                LoggingConstants.HTTP_APPENDER_TYPE);
        config.setProperty(
                LoggingConstants.APPENDER_PREFIX + LoggingConstants.AUDIT_LOGFILE + LoggingConstants.NAME_SUFFIX,
                LoggingConstants.AUDIT_LOGFILE);
        config.setProperty(
                LoggingConstants.APPENDER_PREFIX + LoggingConstants.AUDIT_LOGFILE + LoggingConstants.LAYOUT_SUFFIX
                        + LoggingConstants.TYPE_SUFFIX, LoggingConstants.PATTERN_LAYOUT_TYPE);
        if (layoutTypePatternValue != null && !layoutTypePatternValue.isEmpty()) {
            config.setProperty(
                    LoggingConstants.APPENDER_PREFIX + LoggingConstants.AUDIT_LOGFILE + LoggingConstants.LAYOUT_SUFFIX
                            + LoggingConstants.PATTERN_SUFFIX, layoutTypePatternValue);
        } else {
            config.setProperty(
                    LoggingConstants.APPENDER_PREFIX + LoggingConstants.AUDIT_LOGFILE + LoggingConstants.LAYOUT_SUFFIX
                            + LoggingConstants.PATTERN_SUFFIX, layoutTypePatternDefaultValue);
        }
        config.setProperty(
                LoggingConstants.APPENDER_PREFIX + LoggingConstants.AUDIT_LOGFILE + LoggingConstants.URL_SUFFIX, url);
        if (connectTimeoutMillis != null && !connectTimeoutMillis.isEmpty()) {
            config.setProperty(
                    LoggingConstants.APPENDER_PREFIX + LoggingConstants.AUDIT_LOGFILE
                            + LoggingConstants.CONNECTION_TIMEOUT_SUFFIX, connectTimeoutMillis);
        } else {
            config.clearProperty(
                    LoggingConstants.APPENDER_PREFIX + LoggingConstants.AUDIT_LOGFILE
                            + LoggingConstants.CONNECTION_TIMEOUT_SUFFIX);
        }
        config.setProperty(
                LoggingConstants.APPENDER_PREFIX + LoggingConstants.AUDIT_LOGFILE + LoggingConstants.FILTER_SUFFIX
                        + LoggingConstants.THRESHOLD_SUFFIX + LoggingConstants.TYPE_SUFFIX,
                LoggingConstants.THRESHOLD_FILTER_TYPE);
        config.setProperty(
                LoggingConstants.APPENDER_PREFIX + LoggingConstants.AUDIT_LOGFILE + LoggingConstants.FILTER_SUFFIX
                        + LoggingConstants.THRESHOLD_SUFFIX + LoggingConstants.LEVEL_SUFFIX,
                LoggingConstants.THRESHOLD_FILTER_LEVEL);
        applyConfigs();
        //Audit log for remote audit server logging configuration update
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat date = new SimpleDateFormat("'['yyyy-MM-dd HH:mm:ss,SSSZ']'");
        auditLog.info("Remote audit server logging configuration updated successfully with url: " + url
                + ((connectTimeoutMillis != null) && !(connectTimeoutMillis.isEmpty()) ? " and connection timeout: "
                + connectTimeoutMillis + "ms" : "") + " by user: "
                + CarbonContext.getThreadLocalCarbonContext().getUsername() + " at: " + date.format(currentTime));
    }

    private void applyConfigs() throws IOException, ConfigurationException {
        layout.save(new FileWriter(filePath, false));
    }
}
