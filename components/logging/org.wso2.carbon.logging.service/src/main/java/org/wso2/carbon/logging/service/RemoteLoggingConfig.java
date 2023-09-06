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
public class RemoteLoggingConfig implements RemoteLoggingConfigService{
    private static final Log log = LogFactory.getLog(RemoteLoggingConfig.class);
    private static final Log auditLog = CarbonConstants.AUDIT_LOG;

    private String filePath = System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH) + File.separator
            + "log4j2.properties";
    private File logPropFile = new File(filePath);

    private PropertiesConfiguration config;
    private PropertiesConfigurationLayout layout;

    public RemoteLoggingConfig() throws IOException {
    }

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
     * This method is used to reset the remote server configurations to the defaults
     *
     * @param data RemoteServerLoggerData object that contains the remote server configuration
     * @throws IOException if an error occurs while writing to the log4j2.properties file
     * @throws ConfigurationException if an error occurs while loading the log4j2.properties file
     */
    public void resetRemoteServerConfig(RemoteServerLoggerData data) throws IOException, ConfigurationException {
        boolean auditLogTypeStatus = false;
        boolean carbonLogTypeStatus = false;
        if (data != null) {
            auditLogTypeStatus = data.isAuditLogType();
            carbonLogTypeStatus = data.isCarbonLogType();
            if (!auditLogTypeStatus && !carbonLogTypeStatus) {
                throw new IllegalArgumentException("At least one log type should be selected");
            }
        }
        HashMap<String, Boolean> logTypeStatusMap = new HashMap<>();
        logTypeStatusMap.put(LoggingConstants.AUDIT_LOGFILE, auditLogTypeStatus);
        logTypeStatusMap.put(LoggingConstants.CARBON_LOGFILE, carbonLogTypeStatus);

        // This runs for all the types
        for (Map.Entry<String,Boolean> entry : logTypeStatusMap.entrySet()) {
            String appenderName = entry.getKey();
            if (entry.getValue()) {
                loadConfigs();
                ArrayList<String> list = Utils.getKeysOfAppender(logPropFile, appenderName);
                resetRemoteConfigurations(list, appenderName);
                applyConfigs();

                //Audit log for remote server logging configuration update
                Date currentTime = Calendar.getInstance().getTime();
                SimpleDateFormat date = new SimpleDateFormat("'['yyyy-MM-dd HH:mm:ss,SSSZ']'");
                auditLog.info("Remote carbon server logging configuration was reset successfully with by user: "
                        + CarbonContext.getThreadLocalCarbonContext().getUsername() + " for appender: "
                        + appenderName + " at: " + date.format(currentTime));
            }
        }
    }

    /**
     * This method is used to rewrite the log4j2.properties file with the default values
     *
     * @param appenderPropertiesList list of properties of the appender
     * @param appenderName name of the appender
     */
    private void resetRemoteConfigurations(ArrayList<String> appenderPropertiesList, String appenderName) {
        for (String key : appenderPropertiesList) {
            config.clearProperty(key);
        }

        // appender.CARBON_LOGFILE.name = CARBON_LOGFILE
        config.setProperty(getKey(appenderName, LoggingConstants.NAME_SUFFIX), appenderName);
        // appender.CARBON_LOGFILE.type = RollingFile
        config.setProperty(getKey(appenderName, LoggingConstants.TYPE_SUFFIX), LoggingConstants.ROLLING_FILE);
        // appender.CARBON_LOGFILE.fileName = ${sys:carbon.home}/repository/logs/wso2carbon.log
        config.setProperty(getKey(appenderName, LoggingConstants.FILE_NAME_SUFFIX), LoggingConstants.DEFAULT_CARBON_LOGFILE_PATH);
        // appender.CARBON_LOGFILE.filePattern = ${sys:carbon.home}/repository/logs/wso2carbon-%d{MM-dd-yyyy}-%i.log
        config.setProperty(getKey(appenderName, LoggingConstants.FILE_PATTERN_SUFFIX),
                LoggingConstants.DEFAULT_CARBON_LOGFILE_PATTERN);
        // appender.CARBON_LOGFILE.layout.type = PatternLayout
        config.setProperty(getKey(appenderName, LoggingConstants.LAYOUT_SUFFIX, LoggingConstants.TYPE_SUFFIX),
                LoggingConstants.PATTERN_LAYOUT_TYPE);
        // appender.CARBON_LOGFILE.layout.pattern = TID: [%tenantId] [%appName] [%d] %5p {%c} - %m%ex%n
        config.setProperty(getKey(appenderName, LoggingConstants.LAYOUT_SUFFIX, LoggingConstants.PATTERN_SUFFIX),
                LoggingConstants.CARBON_LOGS_DEFAULT_LAYOUT_PATTERN);
        // appender.CARBON_LOGFILE.policies.type = Policies
        config.setProperty(getKey(appenderName, LoggingConstants.POLICIES_SUFFIX, LoggingConstants.TYPE_SUFFIX),
                LoggingConstants.POLICIES);
        // appender.CARBON_LOGFILE.policies.time.type = TimeBasedTriggeringPolicy
        config.setProperty(getKey(appenderName, LoggingConstants.POLICIES_SUFFIX, LoggingConstants.TIME_SUFFIX,
                LoggingConstants.TYPE_SUFFIX), LoggingConstants.TIME_BASED_TRIGGERING_POLICY);
        // appender.CARBON_LOGFILE.policies.time.interval = 1
        config.setProperty(getKey(appenderName, LoggingConstants.POLICIES_SUFFIX, LoggingConstants.TIME_SUFFIX,
                LoggingConstants.INTERVAL_SUFFIX), LoggingConstants.DEFAULT_INTERVAL);
        // appender.CARBON_LOGFILE.policies.time.modulate = true
        config.setProperty(getKey(appenderName, LoggingConstants.POLICIES_SUFFIX, LoggingConstants.TIME_SUFFIX,
                LoggingConstants.MODULATE_SUFFIX), LoggingConstants.DEFAULT_MODULATE);
        // appender.CARBON_LOGFILE.policies.size.type = SizeBasedTriggeringPolicy
        config.setProperty(getKey(appenderName, LoggingConstants.POLICIES_SUFFIX, LoggingConstants.SIZE_SUFFIX,
                LoggingConstants.TYPE_SUFFIX), LoggingConstants.SIZE_BASED_TRIGGERING_POLICY);
        // appender.CARBON_LOGFILE.policies.size.size = 10MB
        config.setProperty(getKey(appenderName, LoggingConstants.POLICIES_SUFFIX, LoggingConstants.SIZE_SUFFIX,
                LoggingConstants.SIZE_SUFFIX), LoggingConstants.DEFAULT_SIZE);
        // appender.CARBON_LOGFILE.strategy.type = DefaultRolloverStrategy
        config.setProperty(getKey(appenderName, LoggingConstants.STRATEGY_SUFFIX, LoggingConstants.TYPE_SUFFIX),
                LoggingConstants.DEFAULT_ROLLOVER_STRATEGY);
        // appender.CARBON_LOGFILE.strategy.max = 20
        config.setProperty(getKey(appenderName, LoggingConstants.STRATEGY_SUFFIX, LoggingConstants.MAX_SUFFIX),
                LoggingConstants.DEFAULT_MAX);
        // appender.CARBON_LOGFILE.filter.threshold.type = ThresholdFilter
        config.setProperty(getKey(appenderName, LoggingConstants.FILTER_SUFFIX, LoggingConstants.THRESHOLD_SUFFIX,
                LoggingConstants.TYPE_SUFFIX), LoggingConstants.DEFAULT_THRESHOLD_FILTER_TYPE);
        // appender.CARBON_LOGFILE.filter.threshold.level = INFO
        config.setProperty(getKey(appenderName, LoggingConstants.FILTER_SUFFIX, LoggingConstants.THRESHOLD_SUFFIX,
                LoggingConstants.LEVEL_SUFFIX), LoggingConstants.THRESHOLD_FILTER_LEVEL);
    }

    /**
     * This method is used to generate the appender properties key based on the given tokens
     * @param tokens tokens to be joined
     * @return generated key
     */
    private static String getKey(String... tokens) {
        return LoggingConstants.APPENDER_PREFIX + String.join("", tokens);
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
        // appender.CARBON_LOGFILE.type = SecuredHttp
        config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.TYPE_SUFFIX,
                LoggingConstants.HTTP_APPENDER_TYPE);
        // appender.CARBON_LOGFILE.name = CARBON_LOGFILE
        config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.NAME_SUFFIX,
                appenderName);
        // appender.CARBON_LOGFILE.layout.type = PatternLayout
        config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.LAYOUT_SUFFIX
                        + LoggingConstants.TYPE_SUFFIX, LoggingConstants.PATTERN_LAYOUT_TYPE);
        // appender.CARBON_LOGFILE.layout.pattern = TID: [%tenantId] [%appName] [%d] %5p {%c} - %m%ex%n
        config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.LAYOUT_SUFFIX
                + LoggingConstants.PATTERN_SUFFIX,
                (layoutTypePatternValue != null && !layoutTypePatternValue.isEmpty()) ? layoutTypePatternValue :
                        layoutTypePatternDefaultValue);
        // appender.CARBON_LOGFILE.url = https://localhost:3000
        config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.URL_SUFFIX,
                data.getUrl());

        // Set the connection timeout if available
        if (!StringUtils.isEmpty(data.getConnectTimeoutMillis())) {
            config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName
                    + LoggingConstants.CONNECTION_TIMEOUT_SUFFIX, data.getConnectTimeoutMillis());
        }

        // Set the username and password if available
        if (!StringUtils.isEmpty(data.getUsername()) && !StringUtils.isEmpty(data.getPassword())) {
            // appender.CARBON_LOGFILE.username = user
            config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName
                    + LoggingConstants.AUTH_USERNAME_SUFFIX, data.getUsername());
            // appender.CARBON_LOGFILE.password = pass
            config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName
                    + LoggingConstants.AUTH_PASSWORD_SUFFIX, data.getPassword());
        }

        // appender.CARBON_LOGFILE.processingLimit = 1000
        config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName
                + LoggingConstants.PROCESSING_LIMIT_SUFFIX, LoggingConstants.DEFAULT_PROCESSING_LIMIT);

        // Set the SSL configurations if available
        if (!StringUtils.isEmpty(data.getKeystoreLocation())
                && !StringUtils.isEmpty(data.getKeystorePassword())
                && !StringUtils.isEmpty(data.getTruststoreLocation())
                && !StringUtils.isEmpty(data.getTruststorePassword())) {
            // appender.CARBON_LOGFILE.sslconf.type = SSLConf
            config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.SSL_SUFFIX
                            + LoggingConstants.TYPE_SUFFIX, LoggingConstants.DEFAULT_SSLCONF_TYPE);
            // appender.CARBON_LOGFILE.sslconf.protocol = SSL
            config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.SSL_SUFFIX
                    + LoggingConstants.PROTOCOL_SUFFIX, LoggingConstants.DEFAULT_SSL_PROTOCOL);
            // appender.CARBON_LOGFILE.sslconf.keyStoreLocation = repository/resources/security/wso2carbon.jks
            config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.SSL_SUFFIX
                            + LoggingConstants.KEYSTORE_LOCATION_SUFFIX, data.getKeystoreLocation());
            // appender.CARBON_LOGFILE.sslconf.keyStorePassword = $secret{log4j2_keystore_pass}
            config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.SSL_SUFFIX
                            + LoggingConstants.KEYSTORE_PASSWORD_SUFFIX, data.getKeystorePassword());
            // appender.CARBON_LOGFILE.sslconf.trustStoreLocation =repository/resources/security/client-truststore.jks
            config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.SSL_SUFFIX
                            + LoggingConstants.TRUSTSTORE_LOCATION_SUFFIX, data.getTruststoreLocation());
            // appender.CARBON_LOGFILE.sslconf.trustStorePassword = $secret{log4j2_truststore_pass}
            config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.SSL_SUFFIX
                            + LoggingConstants.TRUSTSTORE_PASSWORD_SUFFIX, data.getTruststorePassword());
            // appender.CARBON_LOGFILE.sslconf.verifyHostName = false
            config.setProperty(LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.SSL_SUFFIX
                    + LoggingConstants.VERIFY_HOSTNAME_SUFFIX, data.isVerifyHostname());
        }
        // appender.CARBON_LOGFILE.filter.threshold.type = ThresholdFilter
        config.setProperty(
                LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.FILTER_SUFFIX
                        + LoggingConstants.THRESHOLD_SUFFIX + LoggingConstants.TYPE_SUFFIX,
                LoggingConstants.DEFAULT_THRESHOLD_FILTER_TYPE);
        // appender.CARBON_LOGFILE.filter.threshold.level = INFO
        config.setProperty(
                LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.FILTER_SUFFIX
                        + LoggingConstants.THRESHOLD_SUFFIX + LoggingConstants.LEVEL_SUFFIX,
                LoggingConstants.THRESHOLD_FILTER_LEVEL);
    }

    private void applyConfigs() throws IOException, ConfigurationException {
        layout.save(new FileWriter(filePath, false));
    }
}
