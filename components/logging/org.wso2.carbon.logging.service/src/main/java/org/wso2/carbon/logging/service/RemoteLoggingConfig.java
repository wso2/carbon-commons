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
import java.util.HashMap;
import java.util.Map;

/**
 * This is the Admin service used for configuring the remote server logging configurations
 */
public class RemoteLoggingConfig {
    private static final Log log = LogFactory.getLog(RemoteLoggingConfig.class);
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

    public void addRemoteServerConfig(String url, String connectTimeoutMillis, boolean auditLogTypeStatus,
            boolean apiLogTypeStatus, boolean carbonLogTypeStatus) throws IOException, ConfigurationException {
        HashMap<String, Boolean> logTypeStatusMap = new HashMap<>();
        logTypeStatusMap.put(LoggingConstants.AUDIT_LOGFILE, auditLogTypeStatus);
        logTypeStatusMap.put(LoggingConstants.API_LOGFILE, apiLogTypeStatus);
        logTypeStatusMap.put(LoggingConstants.CARBON_LOGFILE, carbonLogTypeStatus);
        for (Map.Entry<String,Boolean> entry : logTypeStatusMap.entrySet()) {
            String appenderName = entry.getKey();
            if (entry.getValue()) {
                loadConfigs();
                ArrayList<String> list = Utils.getKeysOfAppender(logPropFile, appenderName);
                applyRemoteConfigurations(url, connectTimeoutMillis, list, appenderName);
                applyConfigs();
                //Audit log for remote server logging configuration update
                Date currentTime = Calendar.getInstance().getTime();
                SimpleDateFormat date = new SimpleDateFormat("'['yyyy-MM-dd HH:mm:ss,SSSZ']'");
                auditLog.info("Remote audit server logging configuration updated successfully with url: " + url
                        + " by user: " + CarbonContext.getThreadLocalCarbonContext().getUsername() + " for appender: "
                        + appenderName + " at: " + date.format(currentTime));
            } else {
                loadConfigs();
                ArrayList<String> list = Utils.getKeysOfAppender(logPropFile, appenderName);
                applyDefaultConfigurations(list, appenderName);
                applyConfigs();
                if(log.isDebugEnabled()) {
                    log.debug("Default logging configuration applied for appender: " + appenderName);
                }
            }
        }
    }

    private void applyRemoteConfigurations(String url, String connectTimeoutMillis,
            ArrayList<String> appenderPropertiesList, String appenderName) throws IOException {
        String layoutTypeKey = LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.LAYOUT_SUFFIX
                + LoggingConstants.TYPE_SUFFIX;
        String layoutTypePatternKey = LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.LAYOUT_SUFFIX
                + LoggingConstants.PATTERN_SUFFIX;
        String layoutTypePatternDefaultValue = LoggingConstants.AUDIT_LOGS_DEFAULT_LAYOUT_PATTERN;;
        if (LoggingConstants.API_LOGFILE.equals(appenderName)) {
            layoutTypePatternDefaultValue = LoggingConstants.API_LOGS_DEFAULT_LAYOUT_PATTERN;
        } else if (LoggingConstants.CARBON_LOGFILE.equals(appenderName)) {
            layoutTypePatternDefaultValue = LoggingConstants.CARBON_LOGS_DEFAULT_LAYOUT_PATTERN;
        }
        String layoutTypePatternValue = null;
        for (String key : appenderPropertiesList) {
            if (layoutTypeKey.equals(key)) {
                String layoutTypeValue = Utils.getProperty(logPropFile, key);
                if (LoggingConstants.PATTERN_LAYOUT_TYPE.equals(layoutTypeValue)) {
                    layoutTypePatternValue = Utils.getProperty(logPropFile, layoutTypePatternKey);
                }
            }
            config.clearProperty(key);
        }
        config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.TYPE_SUFFIX,
                LoggingConstants.HTTP_APPENDER_TYPE);
        config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.NAME_SUFFIX,
                appenderName);
        config.setProperty(
                LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.LAYOUT_SUFFIX
                        + LoggingConstants.TYPE_SUFFIX, LoggingConstants.PATTERN_LAYOUT_TYPE);
        if (layoutTypePatternValue != null && !layoutTypePatternValue.isEmpty()) {
            config.setProperty(
                    LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.LAYOUT_SUFFIX
                            + LoggingConstants.PATTERN_SUFFIX, layoutTypePatternValue);
        } else {
            config.setProperty(
                    LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.LAYOUT_SUFFIX
                            + LoggingConstants.PATTERN_SUFFIX, layoutTypePatternDefaultValue);
        }
        config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.URL_SUFFIX, url);
        if (connectTimeoutMillis != null && !connectTimeoutMillis.isEmpty()) {
            config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName
                            + LoggingConstants.CONNECTION_TIMEOUT_SUFFIX, connectTimeoutMillis);
        } else {
            config.clearProperty(LoggingConstants.APPENDER_PREFIX + appenderName
                    + LoggingConstants.CONNECTION_TIMEOUT_SUFFIX);
        }
        config.setProperty(
                LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.FILTER_SUFFIX
                        + LoggingConstants.THRESHOLD_SUFFIX + LoggingConstants.TYPE_SUFFIX,
                LoggingConstants.DEFAULT_THRESHOLD_FILTER_TYPE);
        config.setProperty(
                LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.FILTER_SUFFIX
                        + LoggingConstants.THRESHOLD_SUFFIX + LoggingConstants.LEVEL_SUFFIX,
                LoggingConstants.THRESHOLD_FILTER_LEVEL);
    }

    private void applyDefaultConfigurations(ArrayList<String> appenderPropertiesList, String appenderName) {
        if (appenderPropertiesList.contains(LoggingConstants.APPENDER_PREFIX + appenderName
                + LoggingConstants.URL_SUFFIX)) {
            //Clear all properties if the appender is a remote appender
            for (String key : appenderPropertiesList) {
                config.clearProperty(key);
            }
            //set default config for appender
            applyDefaultConfigsForLogs(appenderName);
        }
    }

    private void applyDefaultConfigsForLogs(String appenderName) {
        String appenderPrefixString = LoggingConstants.APPENDER_PREFIX + appenderName;
        config.setProperty(appenderPrefixString + LoggingConstants.TYPE_SUFFIX,
                LoggingConstants.ROLLING_FILE_APPENDER_TYPE);
        config.setProperty(appenderPrefixString + LoggingConstants.NAME_SUFFIX, appenderName);
        if (LoggingConstants.CARBON_LOGFILE.equals(appenderName)) {
            config.setProperty(appenderPrefixString + LoggingConstants.FILE_NAME_SUFFIX,
                    LoggingConstants.CARBON_LOGS_DEFAULT_FILE_NAME);
            config.setProperty(appenderPrefixString + LoggingConstants.FILE_PATTERN_SUFFIX,
                    LoggingConstants.CARBON_LOGS_DEFAULT_FILE_PATTERN);
            config.setProperty(appenderPrefixString + LoggingConstants.LAYOUT_SUFFIX + LoggingConstants.PATTERN_SUFFIX,
                    LoggingConstants.CARBON_LOGS_DEFAULT_LAYOUT_PATTERN);
            config.setProperty(appenderPrefixString + LoggingConstants.FILTER_SUFFIX + LoggingConstants.THRESHOLD_SUFFIX
                            + LoggingConstants.LEVEL_SUFFIX, LoggingConstants.CARBON_LOGS_DEFAULT_THRESHOLD_LEVEL);
        } else if (LoggingConstants.API_LOGFILE.equals(appenderName)) {
            config.setProperty(appenderPrefixString + LoggingConstants.FILE_NAME_SUFFIX,
                    LoggingConstants.API_LOGS_DEFAULT_FILE_NAME);
            config.setProperty(appenderPrefixString + LoggingConstants.FILE_PATTERN_SUFFIX,
                    LoggingConstants.API_LOGS_DEFAULT_FILE_PATTERN);
            config.setProperty(appenderPrefixString + LoggingConstants.LAYOUT_SUFFIX + LoggingConstants.PATTERN_SUFFIX,
                    LoggingConstants.API_LOGS_DEFAULT_LAYOUT_PATTERN);
            config.setProperty(appenderPrefixString + LoggingConstants.FILTER_SUFFIX + LoggingConstants.THRESHOLD_SUFFIX
                            + LoggingConstants.LEVEL_SUFFIX, LoggingConstants.API_LOGS_DEFAULT_THRESHOLD_LEVEL);
        } else if (LoggingConstants.AUDIT_LOGFILE.equals(appenderName)) {
            config.setProperty(appenderPrefixString + LoggingConstants.FILE_NAME_SUFFIX,
                    LoggingConstants.AUDIT_LOGS_DEFAULT_FILE_NAME);
            config.setProperty(appenderPrefixString + LoggingConstants.FILE_PATTERN_SUFFIX,
                    LoggingConstants.AUDIT_LOGS_DEFAULT_FILE_PATTERN);
            config.setProperty(appenderPrefixString + LoggingConstants.LAYOUT_SUFFIX + LoggingConstants.PATTERN_SUFFIX,
                    LoggingConstants.AUDIT_LOGS_DEFAULT_LAYOUT_PATTERN);
            config.setProperty(appenderPrefixString + LoggingConstants.FILTER_SUFFIX + LoggingConstants.THRESHOLD_SUFFIX
                            + LoggingConstants.LEVEL_SUFFIX, LoggingConstants.AUDIT_LOGS_DEFAULT_THRESHOLD_LEVEL);
        }
        config.setProperty(appenderPrefixString + LoggingConstants.LAYOUT_SUFFIX + LoggingConstants.TYPE_SUFFIX,
                LoggingConstants.PATTERN_LAYOUT_TYPE);
        config.setProperty(appenderPrefixString + LoggingConstants.POLICIES_SUFFIX + LoggingConstants.TYPE_SUFFIX,
                LoggingConstants.DEFAULT_POLICIES_TYPE);
        config.setProperty(appenderPrefixString + LoggingConstants.POLICIES_SUFFIX + LoggingConstants.TIME_SUFFIX
                        + LoggingConstants.TYPE_SUFFIX, LoggingConstants.DEFAULT_POLICIES_TIME_TYPE);
        config.setProperty(appenderPrefixString + LoggingConstants.POLICIES_SUFFIX + LoggingConstants.TIME_SUFFIX
                + LoggingConstants.INTERVAL_SUFFIX, LoggingConstants.DEFAULT_POLICIES_TIME_INTERVAL);
        config.setProperty(appenderPrefixString + LoggingConstants.POLICIES_SUFFIX + LoggingConstants.TIME_SUFFIX
                + LoggingConstants.MODULATE_SUFFIX, LoggingConstants.DEFAULT_POLICIES_TIME_MODULATE);
        config.setProperty(appenderPrefixString + LoggingConstants.POLICIES_SUFFIX + LoggingConstants.SIZE_SUFFIX
                        + LoggingConstants.TYPE_SUFFIX, LoggingConstants.DEFAULT_POLICIES_SIZE_TYPE);
        config.setProperty(appenderPrefixString + LoggingConstants.POLICIES_SUFFIX + LoggingConstants.SIZE_SUFFIX
                + LoggingConstants.SIZE_SUFFIX, LoggingConstants.DEFAULT_POLICIES_SIZE_SIZE);
        config.setProperty(appenderPrefixString + LoggingConstants.STRATEGY_SUFFIX + LoggingConstants.TYPE_SUFFIX,
                LoggingConstants.DEFAULT_STRATEGY_TYPE);
        config.setProperty(appenderPrefixString + LoggingConstants.STRATEGY_SUFFIX + LoggingConstants.MAX_SUFFIX,
                LoggingConstants.DEFAULT_STRATEGY_MAX);
        config.setProperty(appenderPrefixString + LoggingConstants.FILTER_SUFFIX + LoggingConstants.THRESHOLD_SUFFIX
                        + LoggingConstants.TYPE_SUFFIX, LoggingConstants.DEFAULT_THRESHOLD_FILTER_TYPE);
    }

    private void applyConfigs() throws IOException, ConfigurationException {
        layout.save(new FileWriter(filePath, false));
    }
}
