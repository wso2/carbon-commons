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

import org.apache.commons.configuration2.ex.ConfigurationException;
// import org.apache.commons.configuration2.PropertiesConfiguration;
// import org.apache.commons.configuration2.PropertiesConfigurationLayout;
// import org.apache.commons.configuration2.io.FileHandler;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.logging.service.LoggingConstants.LogType;
import org.wso2.carbon.logging.service.data.RemoteServerLoggerData;
import org.wso2.carbon.logging.service.internal.RemoteLoggingConfigDataHolder;
import org.wso2.carbon.logging.service.util.Log4j2PropertiesEditor;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.base.api.ServerConfigurationService;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
//import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This is the Admin service used for configuring the remote server logging configurations
 */
public class RemoteLoggingConfig implements RemoteLoggingConfigService {

    private static final Log log = LogFactory.getLog(RemoteLoggingConfig.class);
    private static final Log auditLog = CarbonConstants.AUDIT_LOG;

    private final String filePath =
            System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH) + File.separator + "log4j2.properties";
    private final File logPropFile = new File(filePath);

    // private PropertiesConfiguration config;
    // private PropertiesConfigurationLayout layout;
    // private FileHandler fileHandler;

    public RemoteLoggingConfig() throws IOException {

    }

    // // no-op loader retained for compatibility with older callers
    // private void loadConfigs() {
    //     // previously used Apache Commons Configuration to load file into `config`.
    //     // New implementation uses Log4j2PropertiesEditor which operates on the file directly,
    //     // so loading into an in-memory config is unnecessary.
    // }

    /**
     * This method is used to add a remote server configuration
     *
     * @param data RemoteServerLoggerData object that contains the remote server configuration
     * @throws IOException            if an error occurs while writing to the log4j2.properties file
     * @throws ConfigurationException if an error occurs while loading the log4j2.properties file
     */
    public void addRemoteServerConfig(RemoteServerLoggerData data) throws IOException, ConfigurationException {

        addRemoteServerConfig(data, false);
    }

    @Override
    public void addRemoteServerConfig(RemoteServerLoggerData data, boolean isPeriodicalSyncRequest)
            throws IOException, ConfigurationException {

        if (data == null) {
            throw new ConfigurationException("Data cannot be null");
        }

        String url = data.getUrl();
        String logType = data.getLogType();
        String appenderName = LoggingConstants.AUDIT_LOGFILE;
        if (LoggingConstants.CARBON.equals(logType)) {
            appenderName = LoggingConstants.CARBON_LOGFILE;
        }
        if (StringUtils.isBlank(url)) {
            throw new ConfigurationException("URL cannot be empty");
        }

        if (!isPeriodicalSyncRequest) {
            updateRemoteServerConfigInRegistry(data, appenderName);
        }
        //loadConfigs();
        //ArrayList<String> list = Utils.getKeysOfAppender(logPropFile, appenderName);
        encryptRemoteServerCredentials(data);
        //applyRemoteConfigurations(data, list, appenderName);
        //applyConfigs();
        // Load file as raw lines
    // Build the map of new key-value pairs for the appender
    Map<String, String> newProps = buildAppenderProperties(data, appenderName);

    // Debug: log what we're going to write so missing keys can be diagnosed
    if (log.isDebugEnabled()) {
        log.debug("Writing appender properties to file: " + logPropFile.getAbsolutePath() + " props: " + newProps);
    }

    // Write to log4j2.properties using the raw-file utilities
    // - preserves unrelated lines
    // - updates existing keys
    // - inserts missing keys contiguously
    // - atomic write
    Log4j2PropertiesEditor.writeUpdatedAppender(logPropFile, appenderName, newProps, false);   // do NOT remove existing keys; update + insert missing ones


    // Audit log entry
    logAuditForConfigUpdate(url, appenderName);
}

  /**
     * Build a LinkedHashMap of appender properties from RemoteServerLoggerData.
     * Maintains insertion order for consistent property file updates.
     */
    private Map<String, String> buildAppenderProperties(RemoteServerLoggerData data, String appenderName) {
        Map<String, String> map = new LinkedHashMap<>();
    String prefix = LoggingConstants.APPENDER_PREFIX + appenderName;

        map.put(prefix + LoggingConstants.URL_SUFFIX, data.getUrl());
        map.put(prefix + LoggingConstants.AUTH_USERNAME_SUFFIX, data.getUsername());
        map.put(prefix + LoggingConstants.AUTH_PASSWORD_SUFFIX, data.getPassword());
        map.put(prefix + LoggingConstants.TYPE_SUFFIX, data.getLogType());
        map.put(prefix + LoggingConstants.KEYSTORE_LOCATION_SUFFIX, data.getKeystoreLocation());
        map.put(prefix + LoggingConstants.KEYSTORE_PASSWORD_SUFFIX, data.getKeystorePassword());
        map.put(prefix + LoggingConstants.TRUSTSTORE_LOCATION_SUFFIX, data.getTruststoreLocation());
        map.put(prefix + LoggingConstants.TRUSTSTORE_PASSWORD_SUFFIX, data.getTruststorePassword());
        map.put(prefix + LoggingConstants.VERIFY_HOSTNAME_SUFFIX, Boolean.toString(data.isVerifyHostname()));
        map.put(prefix + LoggingConstants.CONNECTION_TIMEOUT_SUFFIX, data.getConnectTimeoutMillis());

        return map;
    }


    private void encryptRemoteServerCredentials(RemoteServerLoggerData data) throws ConfigurationException {

        data.setPassword(encryptRemoteServerCredential(data.getPassword()));
        data.setKeystorePassword(encryptRemoteServerCredential(data.getKeystorePassword()));
        data.setTruststorePassword(encryptRemoteServerCredential(data.getTruststorePassword()));
    }

    private String encryptRemoteServerCredential(String secretValue) throws ConfigurationException {

        if (StringUtils.isBlank(secretValue)) {
            return StringUtils.EMPTY;
        }
        if (secretValue.startsWith("$secret{") && secretValue.endsWith("}")) {
            // If the secret is already encrypted using CipherTool, return it as is.
            return secretValue;
        }
        // ServerConfigurationService may not be available in some environments (e.g. early startup).
        // If it's missing, behave as if hiding secrets is disabled (return the raw secret).
        ServerConfigurationService serverConfig = RemoteLoggingConfigDataHolder.getInstance()
                .getServerConfigurationService();
        if (serverConfig == null) {
            return secretValue;
        }
        String hideSecretsProp = serverConfig.getFirstProperty(LoggingConstants.REMOTE_LOGGING_HIDE_SECRETS);
        if (!Boolean.parseBoolean(hideSecretsProp)) {
            return secretValue;
        }
        try {
            return CryptoUtil.getDefaultCryptoUtil()
                    .encryptAndBase64Encode(secretValue.getBytes(StandardCharsets.UTF_8));
        } catch (CryptoException e) {
            throw new ConfigurationException("Error while adding the secret", e);
        }
    }

    private void logAuditForConfigUpdate(String url, String appenderName) {

        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("'['yyyy-MM-dd HH:mm:ss,SSSZ']'");
        String username = CarbonContext.getThreadLocalCarbonContext().getUsername();

        String logMessage = String.format(
                "Remote server logging configuration updated successfully with URL: %s by user: %s for appender: %s at: %s",
                url, username, appenderName, dateFormat.format(currentTime));

        auditLog.info(logMessage);
    }

    /**
     * This method is used to reset the remote server configurations to the defaults
     *
     * @param data RemoteServerLoggerData object that contains the remote server configuration
     * @throws IOException            if an error occurs while writing to the log4j2.properties file
     * @throws ConfigurationException if an error occurs while loading the log4j2.properties file
     */
    public void resetRemoteServerConfig(RemoteServerLoggerData data) throws IOException, ConfigurationException {

        resetRemoteServerConfig(data, false);
    }

    public void resetRemoteServerConfig(RemoteServerLoggerData data, boolean isPeriodicalSyncRequest)
            throws IOException, ConfigurationException {

        String logType = data.getLogType();
        String appenderName = LoggingConstants.AUDIT_LOGFILE;
        if (LoggingConstants.CARBON.equals(logType)) {
            appenderName = LoggingConstants.CARBON_LOGFILE;
        }

        if (!isPeriodicalSyncRequest) {
            resetRemoteServerConfigInRegistry(appenderName);
        }
    // load existing file state (no-op with new util)
    //loadConfigs();
    ArrayList<String> list = Log4j2PropertiesEditor.getKeysOfAppender(logPropFile, appenderName);
    resetRemoteConfigurations(list, appenderName);
    // applyConfigs is no-op now since Log4j2PropertiesEditor writes atomically
    //applyConfigs();

        logAuditForConfigReset(appenderName);
    }

    private void logAuditForConfigReset(String appenderName) {

        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("'['yyyy-MM-dd HH:mm:ss,SSSZ']'");
        String username = CarbonContext.getThreadLocalCarbonContext().getUsername();

        String logMessage = String.format(
                "Remote carbon server logging configuration was reset successfully by user: %s for appender: %s at: %s",
                username, appenderName, dateFormat.format(currentTime));
        auditLog.info(logMessage);
    }

    @Override
    public RemoteServerLoggerData getRemoteServerConfig(String logType) throws ConfigurationException {

        return getRemoteServerConfig(logType, true);
    }

    @Override
    public RemoteServerLoggerData getRemoteServerConfig(String logType, boolean includeSecrets) throws ConfigurationException {

        if (StringUtils.isBlank(logType)) {
            throw new ConfigurationException("Log type cannot be empty.");
        }

        Optional<RemoteServerLoggerData> remoteServerConfig = null;
        try {
            remoteServerConfig = RemoteLoggingConfigDataHolder.getInstance()
                    .getRemoteLoggingConfigDAO().getRemoteServerConfig(LogType.valueOf(logType));
            if (remoteServerConfig.isPresent() && !includeSecrets) {
                RemoteServerLoggerData data = remoteServerConfig.get();
                // If secrets are not to be included, clear the sensitive fields.
                data.setPassword(null);
                data.setKeystorePassword(null);
                data.setTruststorePassword(null);
                return data;
            }
            return remoteServerConfig.orElse(null);
        } catch (RemoteLoggingServerException e) {
            throw new ConfigurationException(e);
        }
    }

    public void syncRemoteServerConfigs() throws ConfigurationException, IOException {

        List<RemoteServerLoggerData> remoteServerLoggerResponseDataList = getRemoteServerConfigs();
        List<RemoteServerLoggerData> modifiedRemoteServerLoggerDataList = new ArrayList<>();
        List<RemoteServerLoggerData> removedRemoteServerLoggerDataList = new ArrayList<>();
        //loadConfigs();

        for (String logType : new String[]{LoggingConstants.AUDIT, LoggingConstants.CARBON}) {
            RemoteServerLoggerData remoteServerLoggerData =
                    findMatchingResponseData(remoteServerLoggerResponseDataList, logType);

            if (isDataUpdated(remoteServerLoggerData, logType)) {
                if (remoteServerLoggerData == null) {
                    removedRemoteServerLoggerDataList.add(createRemovingData(logType));
                } else {
                    modifiedRemoteServerLoggerDataList.add(
                            getRemoteServerLoggerDataFromResponseDTO(remoteServerLoggerData));
                }
            }
        }

        processRemoteServerLoggerData(modifiedRemoteServerLoggerDataList, false);
        processRemoteServerLoggerData(removedRemoteServerLoggerDataList, true);
    }

    private RemoteServerLoggerData getRemoteServerLoggerDataFromResponseDTO(
            RemoteServerLoggerData remoteServerLoggerData) {

        RemoteServerLoggerData data = new RemoteServerLoggerData();
        data.setUrl(remoteServerLoggerData.getUrl());
        data.setConnectTimeoutMillis(remoteServerLoggerData.getConnectTimeoutMillis());
        data.setUsername(remoteServerLoggerData.getUsername());
        data.setPassword(remoteServerLoggerData.getPassword());
        data.setKeystoreLocation(remoteServerLoggerData.getKeystoreLocation());
        data.setKeystorePassword(remoteServerLoggerData.getKeystorePassword());
        data.setTruststoreLocation(remoteServerLoggerData.getTruststoreLocation());
        data.setTruststorePassword(remoteServerLoggerData.getTruststorePassword());
        data.setVerifyHostname(remoteServerLoggerData.isVerifyHostname());
        data.setLogType(remoteServerLoggerData.getLogType());
        return data;
    }

    @Override
    public List<RemoteServerLoggerData> getRemoteServerConfigs() throws ConfigurationException {

        return getRemoteServerConfigs(true);
    }

    @Override
    public List<RemoteServerLoggerData> getRemoteServerConfigs(boolean includeSecrets) throws ConfigurationException {

        List<String> logTypes = new ArrayList<>();
        logTypes.add(LoggingConstants.AUDIT);
        logTypes.add(LoggingConstants.CARBON);
        List<RemoteServerLoggerData> remoteServerLoggerDataList = new ArrayList<>();
        for (String logType : logTypes) {
            RemoteServerLoggerData remoteServerLoggerData = getRemoteServerConfig(logType, includeSecrets);
            if (remoteServerLoggerData != null) {
                remoteServerLoggerDataList.add(remoteServerLoggerData);
            }
        }
        return remoteServerLoggerDataList;
    }

    /**
     * This method is used to rewrite the log4j2.properties file with the default values
     *
     * @param appenderPropertiesList list of properties of the appender
     * @param appenderName           name of the appender
     */
    private void resetRemoteConfigurations(ArrayList<String> appenderPropertiesList, String appenderName) {
    // Build a map of default properties for the appender and write them, removing existing keys.
    Map<String, String> defaults = new LinkedHashMap<>();
        String prefix = LoggingConstants.APPENDER_PREFIX + appenderName;

    defaults.put(prefix + LoggingConstants.NAME_SUFFIX, appenderName);
    defaults.put(prefix + LoggingConstants.TYPE_SUFFIX, LoggingConstants.ROLLING_FILE);

    String fileName = LoggingConstants.DEFAULT_CARBON_LOGFILE_PATH;
    String filePattern = LoggingConstants.DEFAULT_CARBON_LOGFILE_PATTERN;
    if (LoggingConstants.AUDIT_LOGFILE.equals(appenderName)) {
        fileName = LoggingConstants.DEFAULT_AUDIT_LOGFILE_PATH;
        filePattern = LoggingConstants.DEFAULT_AUDIT_LOGFILE_PATTERN;
    }
    defaults.put(prefix + LoggingConstants.FILE_NAME_SUFFIX, fileName);
    defaults.put(prefix + LoggingConstants.FILE_PATTERN_SUFFIX, filePattern);
    defaults.put(prefix + LoggingConstants.LAYOUT_SUFFIX + LoggingConstants.TYPE_SUFFIX,
        LoggingConstants.PATTERN_LAYOUT_TYPE);

    String layoutPattern = LoggingConstants.CARBON_LOGS_DEFAULT_LAYOUT_PATTERN;
    if (LoggingConstants.AUDIT_LOGFILE.equals(appenderName)) {
        layoutPattern = LoggingConstants.AUDIT_LOGS_DEFAULT_LAYOUT_PATTERN;
    }
    defaults.put(prefix + LoggingConstants.LAYOUT_SUFFIX + LoggingConstants.PATTERN_SUFFIX, layoutPattern);

    defaults.put(prefix + LoggingConstants.POLICIES_SUFFIX + LoggingConstants.TYPE_SUFFIX,
        LoggingConstants.POLICIES);
    defaults.put(prefix + LoggingConstants.POLICIES_SUFFIX + LoggingConstants.TIME_SUFFIX +
        LoggingConstants.TYPE_SUFFIX, LoggingConstants.TIME_BASED_TRIGGERING_POLICY);
    defaults.put(prefix + LoggingConstants.POLICIES_SUFFIX + LoggingConstants.TIME_SUFFIX +
        LoggingConstants.INTERVAL_SUFFIX, String.valueOf(LoggingConstants.DEFAULT_INTERVAL));
    defaults.put(prefix + LoggingConstants.POLICIES_SUFFIX + LoggingConstants.TIME_SUFFIX +
        LoggingConstants.MODULATE_SUFFIX, String.valueOf(LoggingConstants.DEFAULT_MODULATE));
    defaults.put(prefix + LoggingConstants.POLICIES_SUFFIX + LoggingConstants.SIZE_SUFFIX +
        LoggingConstants.TYPE_SUFFIX, LoggingConstants.SIZE_BASED_TRIGGERING_POLICY);
    defaults.put(prefix + LoggingConstants.POLICIES_SUFFIX + LoggingConstants.SIZE_SUFFIX +
        LoggingConstants.SIZE_SUFFIX, LoggingConstants.DEFAULT_SIZE);
    defaults.put(prefix + LoggingConstants.STRATEGY_SUFFIX + LoggingConstants.TYPE_SUFFIX,
        LoggingConstants.DEFAULT_ROLLOVER_STRATEGY);
    defaults.put(prefix + LoggingConstants.STRATEGY_SUFFIX + LoggingConstants.MAX_SUFFIX,
        String.valueOf(LoggingConstants.DEFAULT_MAX));
    defaults.put(prefix + LoggingConstants.FILTER_SUFFIX + LoggingConstants.THRESHOLD_SUFFIX +
        LoggingConstants.TYPE_SUFFIX, LoggingConstants.DEFAULT_THRESHOLD_FILTER_TYPE);
    String filterLevel = LoggingConstants.THRESHOLD_FILTER_LEVEL_DEBUG;
    if (LoggingConstants.AUDIT_LOGFILE.equals(appenderName)) {
        filterLevel = LoggingConstants.THRESHOLD_FILTER_LEVEL_INFO;
    }
    defaults.put(prefix + LoggingConstants.FILTER_SUFFIX + LoggingConstants.THRESHOLD_SUFFIX +
        LoggingConstants.LEVEL_SUFFIX, filterLevel);

    try {
        Log4j2PropertiesEditor.writeUpdatedAppender(logPropFile, appenderName, defaults, true);
    } catch (IOException e) {
        log.error("Error resetting appender properties for " + appenderName, e);
    }
    }

    /**
     * This method is used to generate the appender properties key based on the given tokens
     *
     * @param tokens tokens to be joined
     * @return generated key
     */
    private static String getKey(String... tokens) {

        return LoggingConstants.APPENDER_PREFIX + String.join("", tokens);
    }

    /**
     * This method is used to define the remote server configuration parameters
     *
     * @param data                   RemoteServerLoggerData object that contains the remote server configuration
     * @param appenderPropertiesList ArrayList of existing appender properties
     * @param appenderName           name of the appender
     * @throws IOException if an error occurs while reading from the log4j2.properties file
     */
    private void applyRemoteConfigurations(RemoteServerLoggerData data, ArrayList<String> appenderPropertiesList,
                                           String appenderName) throws IOException {
    String layoutTypeKey = LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.LAYOUT_SUFFIX +
        LoggingConstants.TYPE_SUFFIX;
    String layoutTypePatternKey = LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.LAYOUT_SUFFIX +
        LoggingConstants.PATTERN_SUFFIX;
    String layoutTypePatternDefaultValue = LoggingConstants.AUDIT_LOGS_DEFAULT_LAYOUT_PATTERN;
    if (LoggingConstants.CARBON_LOGFILE.equals(appenderName)) {
        layoutTypePatternDefaultValue = LoggingConstants.CARBON_LOGS_DEFAULT_LAYOUT_PATTERN;
    }
    String layoutTypePatternValue = null;
    for (String key : appenderPropertiesList) {
        if (layoutTypeKey.equals(key)) {
        String layoutTypeValue = Log4j2PropertiesEditor.getProperty(logPropFile, key);
        if (LoggingConstants.PATTERN_LAYOUT_TYPE.equals(layoutTypeValue)) {
            layoutTypePatternValue = Log4j2PropertiesEditor.getProperty(logPropFile, layoutTypePatternKey);
        }
        }
    }

    Map<String, String> newProps = new LinkedHashMap<>();
    String prefix = LoggingConstants.APPENDER_PREFIX + appenderName;

    newProps.put(prefix + LoggingConstants.TYPE_SUFFIX, LoggingConstants.HTTP_APPENDER_TYPE);
    newProps.put(prefix + LoggingConstants.NAME_SUFFIX, appenderName);
    newProps.put(prefix + LoggingConstants.LAYOUT_SUFFIX + LoggingConstants.TYPE_SUFFIX,
        LoggingConstants.PATTERN_LAYOUT_TYPE);
    newProps.put(prefix + LoggingConstants.LAYOUT_SUFFIX + LoggingConstants.PATTERN_SUFFIX,
        (layoutTypePatternValue != null && !layoutTypePatternValue.isEmpty()) ? layoutTypePatternValue :
            layoutTypePatternDefaultValue);
    newProps.put(prefix + LoggingConstants.URL_SUFFIX, data.getUrl());

    if (!StringUtils.isEmpty(data.getConnectTimeoutMillis())) {
        newProps.put(prefix + LoggingConstants.CONNECTION_TIMEOUT_SUFFIX, data.getConnectTimeoutMillis());
    }

    if (!StringUtils.isEmpty(data.getUsername()) && !StringUtils.isEmpty(data.getPassword())) {
        newProps.put(prefix + LoggingConstants.AUTH_USERNAME_SUFFIX, data.getUsername());
        newProps.put(prefix + LoggingConstants.AUTH_PASSWORD_SUFFIX, data.getPassword());
    }

    newProps.put(prefix + LoggingConstants.PROCESSING_LIMIT_SUFFIX, String.valueOf(LoggingConstants.DEFAULT_PROCESSING_LIMIT));

    if (!StringUtils.isEmpty(data.getKeystoreLocation()) && !StringUtils.isEmpty(data.getKeystorePassword()) &&
        !StringUtils.isEmpty(data.getTruststoreLocation()) && !StringUtils.isEmpty(data.getTruststorePassword())) {
        newProps.put(prefix + LoggingConstants.SSL_SUFFIX + LoggingConstants.TYPE_SUFFIX,
            LoggingConstants.DEFAULT_SSLCONF_TYPE);
        newProps.put(prefix + LoggingConstants.SSL_SUFFIX + LoggingConstants.PROTOCOL_SUFFIX,
            LoggingConstants.DEFAULT_SSL_PROTOCOL);
        newProps.put(prefix + LoggingConstants.SSL_SUFFIX + LoggingConstants.KEYSTORE_LOCATION_SUFFIX,
            data.getKeystoreLocation());
        newProps.put(prefix + LoggingConstants.SSL_SUFFIX + LoggingConstants.KEYSTORE_PASSWORD_SUFFIX,
            data.getKeystorePassword());
        newProps.put(prefix + LoggingConstants.SSL_SUFFIX + LoggingConstants.TRUSTSTORE_LOCATION_SUFFIX,
            data.getTruststoreLocation());
        newProps.put(prefix + LoggingConstants.SSL_SUFFIX + LoggingConstants.TRUSTSTORE_PASSWORD_SUFFIX,
            data.getTruststorePassword());
        newProps.put(prefix + LoggingConstants.SSL_SUFFIX + LoggingConstants.VERIFY_HOSTNAME_SUFFIX,
            Boolean.toString(data.isVerifyHostname()));
    }

    newProps.put(prefix + LoggingConstants.FILTER_SUFFIX + LoggingConstants.THRESHOLD_SUFFIX +
        LoggingConstants.TYPE_SUFFIX, LoggingConstants.DEFAULT_THRESHOLD_FILTER_TYPE);
    String filterLevel2 = LoggingConstants.THRESHOLD_FILTER_LEVEL_DEBUG;
    if (LoggingConstants.AUDIT_LOGFILE.equals(appenderName)) {
        filterLevel2 = LoggingConstants.THRESHOLD_FILTER_LEVEL_INFO;
    }
    newProps.put(prefix + LoggingConstants.FILTER_SUFFIX + LoggingConstants.THRESHOLD_SUFFIX +
        LoggingConstants.LEVEL_SUFFIX, filterLevel2);

    Log4j2PropertiesEditor.writeUpdatedAppender(logPropFile, appenderName, newProps, false);
    }

    // private void applyConfigs() {
    //     // Log4j2PropertiesEditor writes the file atomically. No further apply step required.
    // }

    private void updateRemoteServerConfigInRegistry(RemoteServerLoggerData data, String appenderName)
            throws ConfigurationException {

        try {
            LogType logType = getLogType(appenderName);
            RemoteLoggingConfigDataHolder.getInstance()
                    .getRemoteLoggingConfigDAO().saveRemoteServerConfig(data, logType);
        } catch (RemoteLoggingServerException e) {
            throw new ConfigurationException(e);
        }
    }

    private void resetRemoteServerConfigInRegistry(String appenderName) throws ConfigurationException {

        try {
            LogType logType = getLogType(appenderName);
            RemoteLoggingConfigDataHolder.getInstance().getRemoteLoggingConfigDAO().resetRemoteServerConfig(logType);
        } catch (RemoteLoggingServerException e) {
            throw new ConfigurationException(e);
        }
    }

    private RemoteServerLoggerData findMatchingResponseData(List<RemoteServerLoggerData> responseDataList,
                                                            String logType) {

        for (RemoteServerLoggerData responseData : responseDataList) {
            if (responseData.getLogType().equals(logType)) {
                return responseData;
            }
        }
        return null;
    }

    private boolean isDataUpdated(RemoteServerLoggerData remoteServerLoggerData, String logType) throws IOException {

        String appenderName = logType.equals(LoggingConstants.AUDIT) ? LoggingConstants.AUDIT_LOGFILE :
                LoggingConstants.CARBON_LOGFILE;
    Map<String, String> appenderProperties = Log4j2PropertiesEditor.getKeyValuesOfAppender(logPropFile, appenderName);

        if (remoteServerLoggerData == null) {
            return !LoggingConstants.ROLLING_FILE.equals(appenderProperties.get(
                    LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.TYPE_SUFFIX));
        }

        return !remoteServerLoggerData.getUrl().equals(appenderProperties.get(
                LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.URL_SUFFIX)) ||
                !(StringUtils.isBlank(remoteServerLoggerData.getKeystoreLocation()) && StringUtils.isBlank(
                        appenderProperties.get(
                                LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.SSL_SUFFIX +
                                        LoggingConstants.KEYSTORE_LOCATION_SUFFIX)) ||
                        remoteServerLoggerData.getKeystoreLocation().equals(appenderProperties.get(
                                LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.SSL_SUFFIX +
                                        LoggingConstants.KEYSTORE_LOCATION_SUFFIX))) ||
                !(StringUtils.isBlank(remoteServerLoggerData.getTruststoreLocation()) && StringUtils.isBlank(
                        appenderProperties.get(
                                LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.SSL_SUFFIX +
                                        LoggingConstants.TRUSTSTORE_LOCATION_SUFFIX)) ||
                        remoteServerLoggerData.getTruststoreLocation().equals(appenderProperties.get(
                                LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.SSL_SUFFIX +
                                        LoggingConstants.TRUSTSTORE_LOCATION_SUFFIX))) ||
                !remoteServerLoggerData.getConnectTimeoutMillis().equals(appenderProperties.get(
                        LoggingConstants.APPENDER_PREFIX + appenderName +
                                LoggingConstants.CONNECTION_TIMEOUT_SUFFIX)) || !remoteServerLoggerData.getUsername()
                .equals(appenderProperties.get(
                        LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.AUTH_USERNAME_SUFFIX)) ||
                !remoteServerLoggerData.getPassword().equals(appenderProperties.get(
                        LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.AUTH_PASSWORD_SUFFIX)) ||
                !(StringUtils.isBlank(remoteServerLoggerData.getKeystorePassword()) && StringUtils.isBlank(
                        appenderProperties.get(
                                LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.SSL_SUFFIX +
                                        LoggingConstants.KEYSTORE_PASSWORD_SUFFIX)) ||
                        remoteServerLoggerData.getKeystorePassword().equals(appenderProperties.get(
                                LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.SSL_SUFFIX +
                                        LoggingConstants.KEYSTORE_PASSWORD_SUFFIX))) ||
                !(StringUtils.isBlank(remoteServerLoggerData.getTruststorePassword()) && StringUtils.isBlank(
                        appenderProperties.get(
                                LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.SSL_SUFFIX +
                                        LoggingConstants.TRUSTSTORE_PASSWORD_SUFFIX)) ||
                        remoteServerLoggerData.getTruststorePassword().equals(appenderProperties.get(
                                LoggingConstants.APPENDER_PREFIX + appenderName + LoggingConstants.SSL_SUFFIX +
                                        LoggingConstants.TRUSTSTORE_PASSWORD_SUFFIX)));
    }

    private RemoteServerLoggerData createRemovingData(String logType) {

        RemoteServerLoggerData removingData = new RemoteServerLoggerData();
        removingData.setLogType(logType);
        return removingData;
    }

    private void processRemoteServerLoggerData(List<RemoteServerLoggerData> dataList, boolean isReset) {

        for (RemoteServerLoggerData remoteServerLoggerData : dataList) {
            try {
                if (isReset) {
                    resetRemoteServerConfig(remoteServerLoggerData, true);
                } else {
                    addRemoteServerConfig(remoteServerLoggerData, true);
                }
            } catch (IOException | ConfigurationException e) {
                log.error("Error occurred while syncing remote server configurations", e);
            }
        }
    }

    private static LoggingConstants.LogType getLogType(String appenderName) {

        LogType logType = LogType.AUDIT;
        if (LoggingConstants.CARBON_LOGFILE.equals(appenderName)) {
            logType = LogType.CARBON;
        }
        return logType;
    }
}