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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.logging.service.data.RemoteServerLoggerData;
import org.wso2.carbon.logging.service.internal.RemoteLoggingConfigDataHolder;
import org.wso2.carbon.logging.service.util.Log4j2PropertiesEditor;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin service used for configuring the remote server logging configurations
 */
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.logging.service.RemoteLoggingConfigService",
                "service.scope=singleton"
        }
)
public class RemoteLoggingConfig implements RemoteLoggingConfigService {

    private static final Log log = LogFactory.getLog(RemoteLoggingConfig.class);
    private static final Log auditLog = CarbonConstants.AUDIT_LOG;

    // Centralized remove marker
    private static final String REMOVE_MARKER = "__REMOVE__";

    private final String filePath =
            System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH) + File.separator + "log4j2.properties";
    private final File logPropFile = new File(filePath);

    public RemoteLoggingConfig() throws IOException {

    }

    /**
     * This method is used to add a remote server configuration
     *
     * @param data                    RemoteServerLoggerData object that contains the remote server configuration
     * @throws IOException            if an error occurs while writing to the log4j2.properties file
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
        } else if (LoggingConstants.API.equals(logType)) {
            appenderName = LoggingConstants.API_LOGFILE;
        }
        if (StringUtils.isBlank(url)) {
            throw new ConfigurationException("URL cannot be empty");
        }
        if (!isPeriodicalSyncRequest) {
            updateRemoteServerConfigInRegistry(data, appenderName);
        }
        encryptRemoteServerCredentials(data);
        try {
            Map<String, String> newProps = buildAppenderProperties(data, appenderName);
            Log4j2PropertiesEditor.writeUpdatedAppender(logPropFile, appenderName, newProps,true);
            logAuditForConfigUpdate(url, appenderName);
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid parameters for remote server config: " + e.getMessage(), e);
        }
    }

    /**
     * Builds a LinkedHashMap of appender properties from the provided {@link RemoteServerLoggerData}.
     * Marks RollingFile-specific properties for removal and sets HTTP appender properties.
     *
     * @param data the remote server logger configuration data
     * @param appenderName the name of the appender to configure
     * @return a map of property keys to values for the appender
     * @throws IllegalArgumentException if {@code data} is null or {@code appenderName} is blank
     */
    private Map<String, String> buildAppenderProperties(RemoteServerLoggerData data, String appenderName) {

        if (data == null) {
            throw new IllegalArgumentException("RemoteServerLoggerData cannot be null");
        }
        if (StringUtils.isBlank(appenderName)) {
            throw new IllegalArgumentException("Appender name cannot be null or empty");
        }

        Map<String, String> map = new LinkedHashMap<>();
        String prefix = LoggingConstants.APPENDER_PREFIX + appenderName;

        // Mark RollingFile-specific properties for removal
        map.put(prefix + LoggingConstants.FILE_NAME_SUFFIX, REMOVE_MARKER);
        map.put(prefix + LoggingConstants.FILE_PATTERN_SUFFIX, REMOVE_MARKER);
        map.put(prefix + ".policies.type", REMOVE_MARKER);
        map.put(prefix + ".policies.time.type", REMOVE_MARKER);
        map.put(prefix + ".policies.time.interval", REMOVE_MARKER);
        map.put(prefix + ".policies.time.modulate", REMOVE_MARKER);
        map.put(prefix + ".policies.size.type", REMOVE_MARKER);
        map.put(prefix + ".policies.size.size", REMOVE_MARKER);
        map.put(prefix + ".strategy.type", REMOVE_MARKER);
        map.put(prefix + ".strategy.max", REMOVE_MARKER);

        // Core HTTP appender properties
        map.put(prefix + LoggingConstants.TYPE_SUFFIX, LoggingConstants.HTTP_APPENDER_TYPE);
        map.put(prefix + LoggingConstants.NAME_SUFFIX, appenderName);

        // Layout configuration
        map.put(prefix + LoggingConstants.LAYOUT_SUFFIX + LoggingConstants.TYPE_SUFFIX,
                LoggingConstants.PATTERN_LAYOUT_TYPE);

        String layoutPattern = LoggingConstants.CARBON_LOGS_DEFAULT_LAYOUT_PATTERN;
        if (LoggingConstants.AUDIT_LOGFILE.equals(appenderName)) {
            layoutPattern = LoggingConstants.AUDIT_LOGS_DEFAULT_LAYOUT_PATTERN;
        } else if (LoggingConstants.API_LOGFILE.equals(appenderName)) {
            layoutPattern = LoggingConstants.API_LOGS_DEFAULT_LAYOUT_PATTERN;
        }
        map.put(prefix + LoggingConstants.LAYOUT_SUFFIX + LoggingConstants.PATTERN_SUFFIX, layoutPattern);

        // HTTP-specific properties
        if (StringUtils.isNotBlank(data.getUrl())) {
            map.put(prefix + LoggingConstants.URL_SUFFIX, data.getUrl());
        }
        if (StringUtils.isNotBlank(data.getUsername())) {
            map.put(prefix + LoggingConstants.AUTH_USERNAME_SUFFIX, data.getUsername());
        }
        if (StringUtils.isNotBlank(data.getPassword())) {
            map.put(prefix + LoggingConstants.AUTH_PASSWORD_SUFFIX, data.getPassword());
        }
        if ( StringUtils.isNotBlank(data.getConnectTimeoutMillis())) {
            map.put(prefix + LoggingConstants.CONNECTION_TIMEOUT_SUFFIX, data.getConnectTimeoutMillis());
        }

        // Filter configuration
        map.put(prefix + LoggingConstants.FILTER_SUFFIX + LoggingConstants.THRESHOLD_SUFFIX +
                LoggingConstants.TYPE_SUFFIX, LoggingConstants.DEFAULT_THRESHOLD_FILTER_TYPE);
        String filterLevel = LoggingConstants.AUDIT_LOGFILE.equals(appenderName)
                ? LoggingConstants.THRESHOLD_FILTER_LEVEL_INFO
                : LoggingConstants.THRESHOLD_FILTER_LEVEL_DEBUG;
        map.put(prefix + LoggingConstants.FILTER_SUFFIX + LoggingConstants.THRESHOLD_SUFFIX +
                LoggingConstants.LEVEL_SUFFIX, filterLevel);

        // SSL configuration (if both keystore and truststore are provided)
        boolean hasKeystore = StringUtils.isNotBlank(data.getKeystoreLocation()) &&
                StringUtils.isNotBlank(data.getKeystorePassword());
        boolean hasTruststore = StringUtils.isNotBlank(data.getTruststoreLocation()) &&
                StringUtils.isNotBlank(data.getTruststorePassword());

        if (hasKeystore && hasTruststore) {
            addSslConfiguration(data, map, prefix);
            map.put(prefix + LoggingConstants.SSL_SUFFIX + LoggingConstants.VERIFY_HOSTNAME_SUFFIX,
                    String.valueOf(data.isVerifyHostname()));
        }
        return map;
    }

    /**
     * Adds SSL configuration properties to the appender properties map.
     *
     * @param data   the remote server logger data containing SSL configuration
     * @param map    the properties map to add SSL configuration to
     * @param prefix the appender property key prefix
     */
    private void addSslConfiguration(RemoteServerLoggerData data, Map<String, String> map, String prefix) {
        map.put(prefix + LoggingConstants.SSL_SUFFIX + LoggingConstants.TYPE_SUFFIX,
                LoggingConstants.DEFAULT_SSLCONF_TYPE);
        map.put(prefix + LoggingConstants.SSL_SUFFIX + LoggingConstants.PROTOCOL_SUFFIX,
                LoggingConstants.DEFAULT_SSL_PROTOCOL);
        map.put(prefix + LoggingConstants.SSL_SUFFIX + LoggingConstants.KEYSTORE_LOCATION_SUFFIX,
                data.getKeystoreLocation());
        map.put(prefix + LoggingConstants.SSL_SUFFIX + LoggingConstants.KEYSTORE_PASSWORD_SUFFIX,
                data.getKeystorePassword());
        map.put(prefix + LoggingConstants.SSL_SUFFIX + LoggingConstants.TRUSTSTORE_LOCATION_SUFFIX,
                data.getTruststoreLocation());
        map.put(prefix + LoggingConstants.SSL_SUFFIX + LoggingConstants.TRUSTSTORE_PASSWORD_SUFFIX,
                data.getTruststorePassword());
    }

    /**
     * Encrypts remote server credentials for storage in log4j2.properties.
     * Encryption is controlled by REMOTE_LOGGING_HIDE_SECRETS flag.
     * 
     * @param data RemoteServerLoggerData containing credentials to encrypt for log4j2.properties
     * @throws ConfigurationException if encryption fails when HIDE_SECRETS is enabled
     */
    private void encryptRemoteServerCredentials(RemoteServerLoggerData data) throws ConfigurationException {

        data.setPassword(encryptRemoteServerCredential(data.getPassword()));
        data.setKeystorePassword(encryptRemoteServerCredential(data.getKeystorePassword()));
        data.setTruststorePassword(encryptRemoteServerCredential(data.getTruststorePassword()));
    }

    /**
     * Encrypts credentials for storage in log4j2.properties based on HIDE_SECRETS flag.
     * 
     * @param secretValue the credential to potentially encrypt
     * @return encrypted credential if HIDE_SECRETS=true, plain text if HIDE_SECRETS=false
     * @throws ConfigurationException if encryption fails when HIDE_SECRETS is enabled
     */
    private String encryptRemoteServerCredential(String secretValue) throws ConfigurationException {

        if (StringUtils.isBlank(secretValue)) {
            return StringUtils.EMPTY;
        }
        if (secretValue.startsWith("$secret{") && secretValue.endsWith("}")) {
            // If the secret is already encrypted using CipherTool, return it as is.
            return secretValue;
        }
        if (!Boolean.parseBoolean(RemoteLoggingConfigDataHolder.getInstance().getServerConfigurationService()
                .getFirstProperty(LoggingConstants.REMOTE_LOGGING_HIDE_SECRETS))) {
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
     * Reset the remote server configurations to the defaults
     *
     * @param data                    RemoteServerLoggerData object that contains the remote server configuration
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
        } else if (LoggingConstants.API.equals(logType)) {
            appenderName = LoggingConstants.API_LOGFILE;
        }

        if (!isPeriodicalSyncRequest) {
            resetRemoteServerConfigInRegistry(appenderName);
        }
        resetRemoteConfigurations(appenderName);

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

        try {
            if (StringUtils.isBlank(logType)) {
                throw new ConfigurationException("Log type cannot be empty.");
            }
            String resourcePath = LoggingConstants.REMOTE_SERVER_LOGGER_RESOURCE_PATH + "/" + logType;
            if (!RemoteLoggingConfigDataHolder.getInstance().getRegistryService().getConfigSystemRegistry()
                    .resourceExists(resourcePath)) {
                return null;
            }
            Resource resource =
                    RemoteLoggingConfigDataHolder.getInstance().getRegistryService().getConfigSystemRegistry()
                            .get(resourcePath);
            RemoteServerLoggerData data = getRemoteServerServerLoggerDataSafe(resource);
            // If URL is missing/blank, skip returning this config to avoid downstream null URL usage.
            if (data == null || StringUtils.isBlank(data.getUrl())) {
                if (log.isDebugEnabled()) {
                    log.debug("Remote server logging config for logType '" + logType + "' has no URL; skipping.");
                }
                return null;
            }
            if (Boolean.parseBoolean(RemoteLoggingConfigDataHolder.getInstance().getServerConfigurationService()
                    .getFirstProperty(LoggingConstants.REMOTE_LOGGING_HIDE_SECRETS))) {
                // Hide secrets in API responses when HIDE_SECRETS=true
                data.setPassword(StringUtils.EMPTY);
                data.setKeystorePassword(StringUtils.EMPTY);
                data.setTruststorePassword(StringUtils.EMPTY);
            }
            return data;
        } catch (RegistryException e) {
            throw new ConfigurationException(e);
        }
    }

    public void syncRemoteServerConfigs() throws ConfigurationException, IOException {

        List<RemoteServerLoggerData> remoteServerLoggerResponseDataList = getRemoteServerConfigs();
        List<RemoteServerLoggerData> modifiedRemoteServerLoggerDataList = new ArrayList<>();
        List<RemoteServerLoggerData> removedRemoteServerLoggerDataList = new ArrayList<>();

        for (String logType : new String[]{LoggingConstants.AUDIT, LoggingConstants.CARBON, LoggingConstants.API}) {
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

    private RemoteServerLoggerData getRemoteServerServerLoggerDataSafe(Resource resource) {

        if (resource == null) {
            return null;
        }
        RemoteServerLoggerData data = new RemoteServerLoggerData();
        // Read and set properties; tolerate missing values.
        data.setUrl(resource.getProperty(LoggingConstants.URL));
        String timeout = resource.getProperty(LoggingConstants.CONNECTION_TIMEOUT);
        if (StringUtils.isBlank(timeout)) {
            // Maintain backward compatibility with alternative key if present
            timeout = resource.getProperty(LoggingConstants.CONNECT_TIMEOUT_MILLIS);
        }
        data.setConnectTimeoutMillis(timeout);
        data.setUsername(resource.getProperty(LoggingConstants.USERNAME));
        data.setPassword(getDecryptedSecret(resource, LoggingConstants.PASSWORD));
        data.setKeystoreLocation(resource.getProperty(LoggingConstants.KEYSTORE_LOCATION));
        data.setKeystorePassword(getDecryptedSecret(resource, LoggingConstants.KEYSTORE_PASSWORD));
        data.setTruststoreLocation(resource.getProperty(LoggingConstants.TRUSTSTORE_LOCATION));
        data.setTruststorePassword(getDecryptedSecret(resource, LoggingConstants.TRUSTSTORE_PASSWORD));
        String verifyHostname = resource.getProperty(LoggingConstants.VERIFY_HOSTNAME);
        data.setVerifyHostname(Boolean.parseBoolean(verifyHostname));
        data.setLogType(resource.getProperty(LoggingConstants.LOG_TYPE));
        return data;
    }

    @Override
    public List<RemoteServerLoggerData> getRemoteServerConfigs() throws ConfigurationException {

        List<String> logTypes = new ArrayList<>();
        logTypes.add(LoggingConstants.AUDIT);
        logTypes.add(LoggingConstants.CARBON);
        logTypes.add(LoggingConstants.API);
        List<RemoteServerLoggerData> remoteServerLoggerDataList = new ArrayList<>();
        for (String logType : logTypes) {
            RemoteServerLoggerData remoteServerLoggerData = getRemoteServerConfig(logType);
            if (remoteServerLoggerData != null) {
                remoteServerLoggerDataList.add(remoteServerLoggerData);
            }
        }
        return remoteServerLoggerDataList;
    }

    /**
     * This method is used to rewrite the log4j2.properties file with the default values
     *
     * @param appenderName           name of the appender
     */
    private void resetRemoteConfigurations(String appenderName) {

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
        } else if (LoggingConstants.API_LOGFILE.equals(appenderName)) {
            fileName = LoggingConstants.DEFAULT_API_LOGFILE_PATH;
            filePattern = LoggingConstants.DEFAULT_API_LOGFILE_PATTERN;
        }
        defaults.put(prefix + LoggingConstants.FILE_NAME_SUFFIX, fileName);
        defaults.put(prefix + LoggingConstants.FILE_PATTERN_SUFFIX, filePattern);
        defaults.put(prefix + LoggingConstants.LAYOUT_SUFFIX + LoggingConstants.TYPE_SUFFIX,
                LoggingConstants.PATTERN_LAYOUT_TYPE);

        String layoutPattern = LoggingConstants.CARBON_LOGS_DEFAULT_LAYOUT_PATTERN;
        if (LoggingConstants.AUDIT_LOGFILE.equals(appenderName)) {
            layoutPattern = LoggingConstants.AUDIT_LOGS_DEFAULT_LAYOUT_PATTERN;
        } else if (LoggingConstants.API_LOGFILE.equals(appenderName)) {
            layoutPattern = LoggingConstants.CARBON_LOGS_DEFAULT_LAYOUT_PATTERN;
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
        addFilterConfiguration(appenderName, defaults, prefix);

        try {
            Log4j2PropertiesEditor.writeUpdatedAppender(logPropFile, appenderName, defaults, false);
        } catch (IOException e) {
            log.error("Error resetting appender properties for " + appenderName, e);
            throw new RuntimeException("Failed to reset appender properties for " + appenderName, e);
        }
    }

    /**
     * Adds filter configuration to the default properties map for appender reset.
     *
     * @param appenderName the name of the appender being reset
     * @param defaults the map of default properties to add filter configuration to
     * @param prefix the appender property key prefix
     */
    private void addFilterConfiguration(String appenderName, Map<String, String> defaults, String prefix) {
        defaults.put(prefix + LoggingConstants.FILTER_SUFFIX + LoggingConstants.THRESHOLD_SUFFIX +
                LoggingConstants.TYPE_SUFFIX, LoggingConstants.DEFAULT_THRESHOLD_FILTER_TYPE);
        String filterLevel = LoggingConstants.THRESHOLD_FILTER_LEVEL_DEBUG;
        if (LoggingConstants.AUDIT_LOGFILE.equals(appenderName)) {
            filterLevel = LoggingConstants.THRESHOLD_FILTER_LEVEL_INFO;
        }
        defaults.put(prefix + LoggingConstants.FILTER_SUFFIX + LoggingConstants.THRESHOLD_SUFFIX +
                LoggingConstants.LEVEL_SUFFIX, filterLevel);
    }

    private void updateRemoteServerConfigInRegistry(RemoteServerLoggerData data, String appenderName)
            throws ConfigurationException {

        try {
            Registry registry =
                    RemoteLoggingConfigDataHolder.getInstance().getRegistryService().getConfigSystemRegistry();
            String logType = LoggingConstants.AUDIT;
            if (LoggingConstants.CARBON_LOGFILE.equals(appenderName)) {
                logType = LoggingConstants.CARBON;
            } else if (LoggingConstants.API_LOGFILE.equals(appenderName)) {
                logType = LoggingConstants.API;
            }
            try {
                boolean transactionStarted = Transaction.isStarted();
                if (!transactionStarted) {
                    registry.beginTransaction();
                }

                Resource resource = getResourceFromRemoteServerLoggerData(data, registry, logType);
                registry.put(LoggingConstants.REMOTE_SERVER_LOGGER_RESOURCE_PATH + "/" + logType, resource);

                if (!transactionStarted) {
                    registry.commitTransaction();
                }
            } catch (Exception e) {
                registry.rollbackTransaction();
                throw new ConfigurationException(e);
            }
        } catch (RegistryException e) {
            throw new ConfigurationException("Error while updating the remote server logging configurations");
        }
    }

    private Resource getResourceFromRemoteServerLoggerData(RemoteServerLoggerData data, Registry registry,
                                                           String logType) throws RegistryException, ConfigurationException {

        Resource resource = registry.newResource();
        resource.addProperty(LoggingConstants.URL, data.getUrl());
        resource.addProperty(LoggingConstants.USERNAME, data.getUsername());
        resource.addProperty(LoggingConstants.PASSWORD,
                getEncryptedSecret(data.getPassword(), LoggingConstants.PASSWORD));
        resource.addProperty(LoggingConstants.KEYSTORE_LOCATION, data.getKeystoreLocation());
        resource.addProperty(LoggingConstants.KEYSTORE_PASSWORD,
                getEncryptedSecret(data.getKeystorePassword(), LoggingConstants.KEYSTORE_PASSWORD));
        resource.addProperty(LoggingConstants.TRUSTSTORE_LOCATION, data.getTruststoreLocation());
        resource.addProperty(LoggingConstants.TRUSTSTORE_PASSWORD,
                getEncryptedSecret(data.getTruststorePassword(), LoggingConstants.TRUSTSTORE_PASSWORD));
        resource.addProperty(LoggingConstants.VERIFY_HOSTNAME, String.valueOf(data.isVerifyHostname()));
        resource.addProperty(LoggingConstants.LOG_TYPE, logType);
        resource.addProperty(LoggingConstants.CONNECT_TIMEOUT_MILLIS, data.getConnectTimeoutMillis());
        resource.addProperty(LoggingConstants.CONNECTION_TIMEOUT, data.getConnectTimeoutMillis());
        return resource;
    }

    /**
     * Encrypts credentials for Registry storage based on ENABLE_ENCRYPTION flag.
     * 
     * @param secretValue the credential to potentially encrypt
     * @param name the name of the credential for error reporting
     * @return encrypted credential if ENABLE_ENCRYPTION=true, plain text if ENABLE_ENCRYPTION=false
     * @throws ConfigurationException if encryption fails when ENABLE_ENCRYPTION is enabled
     */
    private String getEncryptedSecret(String secretValue, String name) throws  ConfigurationException {

        if (StringUtils.isBlank(secretValue)) {
            return StringUtils.EMPTY;
        }
        if (!Boolean.parseBoolean(RemoteLoggingConfigDataHolder.getInstance().getServerConfigurationService()
                .getFirstProperty(LoggingConstants.REMOTE_LOGGING_ENABLE_ENCRYPTION))) {
            return secretValue;
        }
        try {
            return CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(
                    secretValue.getBytes(StandardCharsets.UTF_8));
        } catch (CryptoException e) {
            throw new ConfigurationException("Error while adding the secret : " + name, e);
        }
    }

    /**
     * Decrypts credentials from Registry storage based on ENABLE_ENCRYPTION flag.
     * 
     * @param resource the Registry resource containing the credential
     * @param name the name of the credential property
     * @return decrypted credential if ENABLE_ENCRYPTION=true, plain text if ENABLE_ENCRYPTION=false
     */
    private String getDecryptedSecret(Resource resource, String name) {

        String secretValue = resource.getProperty(name);
        if (StringUtils.isBlank(secretValue)) {
            if (log.isDebugEnabled()) {
                log.debug("Empty or blank secret value found for property: " + name);
            }
            return StringUtils.EMPTY;
        }
        if (!Boolean.parseBoolean(RemoteLoggingConfigDataHolder.getInstance().getServerConfigurationService()
                .getFirstProperty(LoggingConstants.REMOTE_LOGGING_ENABLE_ENCRYPTION))) {
            return secretValue;
        }
        try {
            return new String(CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(
                    secretValue), StandardCharsets.UTF_8);
        } catch (CryptoException e) {
            return StringUtils.EMPTY;
        }
    }

    private void resetRemoteServerConfigInRegistry(String appenderName) throws ConfigurationException {

        try {
            String logType = LoggingConstants.AUDIT;
            if (LoggingConstants.CARBON_LOGFILE.equals(appenderName)) {
                logType = LoggingConstants.CARBON;
            } else if (LoggingConstants.API_LOGFILE.equals(appenderName)) {
                logType = LoggingConstants.API;
            }
            String resourcePath = LoggingConstants.REMOTE_SERVER_LOGGER_RESOURCE_PATH + "/" + logType;
            if (!RemoteLoggingConfigDataHolder.getInstance().getRegistryService().getConfigSystemRegistry()
                    .resourceExists(resourcePath)) {
                return;
            }
            Registry registry =
                    RemoteLoggingConfigDataHolder.getInstance().getRegistryService().getConfigSystemRegistry();

            try {
                boolean transactionStarted = Transaction.isStarted();
                if (!transactionStarted) {
                    registry.beginTransaction();
                }

                registry.delete(resourcePath);

                if (!transactionStarted) {
                    registry.commitTransaction();
                }
            } catch (Exception e) {
                registry.rollbackTransaction();
                throw new ConfigurationException(e);
            }
        } catch (RegistryException e) {
            throw new ConfigurationException("Error while resetting the remote server logging configurations");
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

        String appenderName = LoggingConstants.CARBON_LOGFILE;
        if (LoggingConstants.AUDIT.equals(logType)) {
            appenderName = LoggingConstants.AUDIT_LOGFILE;
        } else if (LoggingConstants.API.equals(logType)) {
            appenderName = LoggingConstants.API_LOGFILE;
        }

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
}
