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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.PropertiesConfigurationLayout;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.logging.service.data.RemoteServerLoggerData;
import org.wso2.carbon.logging.service.util.Utils;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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

    /**
     * This method is used to add a remote server configuration
     *
     * @param data RemoteServerLoggerData object that contains the remote server configuration
     * @throws IOException if an error occurs while writing to the log4j2.properties file
     * @throws ConfigurationException if an error occurs while loading the log4j2.properties file
     */
    public void addRemoteServerConfig(RemoteServerLoggerData data) throws IOException, ConfigurationException {
        String url = null;
        boolean auditLogTypeStatus = false;
        boolean carbonLogTypeStatus = false;
        if (data != null) {
            url = data.getUrl();
            auditLogTypeStatus = data.isAuditLogType();
            carbonLogTypeStatus = data.isCarbonLogType();
            if (url == null || url.isEmpty()) {
                throw new IllegalArgumentException("URL cannot be empty");
            }
            if (!auditLogTypeStatus && !carbonLogTypeStatus) {
                throw new IllegalArgumentException("At least one log type should be selected");
            }
        }
        HashMap<String, Boolean> logTypeStatusMap = new HashMap<>();
        logTypeStatusMap.put(LoggingConstants.AUDIT_LOGFILE, auditLogTypeStatus);
        logTypeStatusMap.put(LoggingConstants.CARBON_LOGFILE, carbonLogTypeStatus);
        for (Map.Entry<String,Boolean> entry : logTypeStatusMap.entrySet()) {
            String appenderName = entry.getKey();
            if (entry.getValue()) {
                loadConfigs();
                ArrayList<String> list = Utils.getKeysOfAppender(logPropFile, appenderName);
                applyRemoteConfigurations(data, list, appenderName);
                applyConfigs();
                //Audit log for remote server logging configuration update
                Date currentTime = Calendar.getInstance().getTime();
                SimpleDateFormat date = new SimpleDateFormat("'['yyyy-MM-dd HH:mm:ss,SSSZ']'");
                auditLog.info("Remote audit server logging configuration updated successfully with url: " + url
                        + " by user: " + CarbonContext.getThreadLocalCarbonContext().getUsername() + " for appender: "
                        + appenderName + " at: " + date.format(currentTime));
            }
        }
    }

    /**
     * This method is used to define the remote server configuration parameters
     *
     * @param data RemoteServerLoggerData object that contains the remote server configuration
     * @param appenderPropertiesList ArrayList of existing appender properties
     * @param appenderName name of the appender
     * @throws IOException if an error occurs while reading from the log4j2.properties file
     */
    private void applyRemoteConfigurations(RemoteServerLoggerData data, ArrayList<String> appenderPropertiesList,
            String appenderName) throws IOException {
        String layoutTypeKey = LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.LAYOUT_SUFFIX
                + LoggingConstants.TYPE_SUFFIX;
        String layoutTypePatternKey = LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.LAYOUT_SUFFIX
                + LoggingConstants.PATTERN_SUFFIX;
        String layoutTypePatternDefaultValue = LoggingConstants.AUDIT_LOGS_DEFAULT_LAYOUT_PATTERN;;
        if (LoggingConstants.CARBON_LOGFILE.equals(appenderName)) {
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
        config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.LAYOUT_SUFFIX
                        + LoggingConstants.TYPE_SUFFIX, LoggingConstants.PATTERN_LAYOUT_TYPE);
        if (layoutTypePatternValue != null && !layoutTypePatternValue.isEmpty()) {
            config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.LAYOUT_SUFFIX
                            + LoggingConstants.PATTERN_SUFFIX, layoutTypePatternValue);
        } else {
            config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.LAYOUT_SUFFIX
                            + LoggingConstants.PATTERN_SUFFIX, layoutTypePatternDefaultValue);
        }
        config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.URL_SUFFIX,
                data.getUrl());
        if (data != null) {
            // Set the connection timeout if available
            if (!StringUtils.isEmpty(data.getConnectTimeoutMillis())) {
                config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName
                                + LoggingConstants.CONNECTION_TIMEOUT_SUFFIX, data.getConnectTimeoutMillis());
            }
            // Set verify hostname value if set as false because the default value is true
            if (!data.isVerifyHostname()) {
                config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName
                        + LoggingConstants.VERIFY_HOSTNAME_SUFFIX, data.isVerifyHostname());
            }
            // Set the username and password if available
            if (!StringUtils.isEmpty(data.getUsername()) && !StringUtils.isEmpty(data.getPassword())) {
                config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.HEADERS_SUFFIX
                                + LoggingConstants.TYPE_SUFFIX, LoggingConstants.DEFAULT_HEADER_TYPE);
                config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.HEADERS_SUFFIX
                                + LoggingConstants.NAME_SUFFIX, LoggingConstants.AUTHORIZATION_HEADER);
                String credentials = data.getUsername() + ":" + data.getPassword();
                byte[] base64EncodedHeader = Base64.encodeBase64(credentials.getBytes(StandardCharsets.UTF_8));
                config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.HEADERS_SUFFIX
                                + LoggingConstants.VALUE_SUFFIX, "Basic " + new String(base64EncodedHeader));
            }
            // Set the SSL configurations if available
            if (!StringUtils.isEmpty(data.getKeystoreLocation()) && !StringUtils.isEmpty(
                    data.getKeystorePassword()) && !StringUtils.isEmpty(data.getTruststoreLocation())
                    && !StringUtils.isEmpty(data.getTruststorePassword())) {
                config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.SSL_SUFFIX
                                + LoggingConstants.TYPE_SUFFIX, LoggingConstants.DEFAULT_SSL_TYPE);
                config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.SSL_SUFFIX
                                + LoggingConstants.KEYSTORE_SUFFIX + LoggingConstants.TYPE_SUFFIX,
                        LoggingConstants.DEFAULT_SSL_KEYSTORE_TYPE);
                config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.SSL_SUFFIX
                                + LoggingConstants.KEYSTORE_SUFFIX + LoggingConstants.LOCATION_SUFFIX,
                        data.getKeystoreLocation());
                config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.SSL_SUFFIX
                                + LoggingConstants.KEYSTORE_SUFFIX + LoggingConstants.PASSWORD_SUFFIX,
                        data.getKeystorePassword());
                config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.SSL_SUFFIX
                                + LoggingConstants.TRUSTSTORE_SUFFIX + LoggingConstants.TYPE_SUFFIX,
                        LoggingConstants.DEFAULT_SSL_TRUSTSTORE_TYPE);
                config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.SSL_SUFFIX
                                + LoggingConstants.TRUSTSTORE_SUFFIX + LoggingConstants.LOCATION_SUFFIX,
                        data.getTruststoreLocation());
                config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.SSL_SUFFIX
                                + LoggingConstants.TRUSTSTORE_SUFFIX + LoggingConstants.PASSWORD_SUFFIX,
                        data.getTruststorePassword());
            }
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

    private void applyConfigs() throws IOException, ConfigurationException {
        layout.save(new FileWriter(filePath, false));
    }
}
