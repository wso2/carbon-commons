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

public final class LoggingConstants {

    public static final String AUDIT_LOGFILE = "AUDIT_LOGFILE";
    public static final String CARBON_LOGFILE = "CARBON_LOGFILE";
    public static final String NAME_SUFFIX = ".name";
    public static final String APPENDER_PREFIX = "appender.";
    public static final String URL_SUFFIX = ".url";
    public static final String TYPE_SUFFIX = ".type";
    public static final String LAYOUT_SUFFIX = ".layout";
    public static final String PATTERN_SUFFIX = ".pattern";
    public static final String FILTER_SUFFIX = ".filter";
    public static final String THRESHOLD_SUFFIX = ".threshold";
    public static final String LEVEL_SUFFIX = ".level";
    public static final String CONNECTION_TIMEOUT_SUFFIX = ".connectTimeoutMillis";
    public static final String AUTH_USERNAME_SUFFIX = ".username";
    public static final String AUTH_PASSWORD_SUFFIX = ".password";
    public static final String PROCESSING_LIMIT_SUFFIX = ".processingLimit";
    public static final String PATTERN_LAYOUT_TYPE = "PatternLayout";
    public static final String DEFAULT_THRESHOLD_FILTER_TYPE = "ThresholdFilter";
    public static final String THRESHOLD_FILTER_LEVEL_INFO = "INFO";
    public static final String THRESHOLD_FILTER_LEVEL_DEBUG = "DEBUG";
    public static final String HTTP_APPENDER_TYPE = "SecuredHttp";
    public static final String AUDIT_LOGS_DEFAULT_LAYOUT_PATTERN = "TID: [%tenantId] [%d] %5p {%c} - %m%ex%n";

    // Default logging constants
    public static final String CARBON_LOGS_DEFAULT_LAYOUT_PATTERN = "TID: [%tenantId] [%appName] [%d] [%X{Correlation-ID}] %5p {%c} - %mm%ex%n";
    public static final String ROLLING_FILE = "RollingFile";
    public static final String DEFAULT_CARBON_LOGFILE_PATH = "${sys:carbon.home}/repository/logs/wso2carbon.log";
    public static final String DEFAULT_CARBON_LOGFILE_PATTERN = "${sys:carbon.home}/repository/logs/wso2carbon-%d{MM-dd-yyyy}-%i.log";
    public static final String DEFAULT_AUDIT_LOGFILE_PATH = "${sys:carbon.home}/repository/logs/audit.log";
    public static final String DEFAULT_AUDIT_LOGFILE_PATTERN = "${sys:carbon.home}/repository/logs/audit-%d{MM-dd-yyyy}.log";
    public static final String POLICIES = "Policies";
    public static final String TIME_BASED_TRIGGERING_POLICY = "TimeBasedTriggeringPolicy";
    public static final int DEFAULT_INTERVAL = 1;
    public static final boolean DEFAULT_MODULATE = true;
    public static final String SIZE_BASED_TRIGGERING_POLICY = "SizeBasedTriggeringPolicy";
    public static final String DEFAULT_SIZE = "10MB";
    public static final String DEFAULT_ROLLOVER_STRATEGY = "DefaultRolloverStrategy";
    public static final int DEFAULT_MAX = 20;
    public static final int DEFAULT_PROCESSING_LIMIT = 1000;

    public static final String API_LOGS_DEFAULT_LAYOUT_PATTERN = "[%d] %5p {%c} %X{apiName} - %m%ex%n";

    public static final String FILE_NAME_SUFFIX = ".fileName";
    public static final String FILE_PATTERN_SUFFIX = ".filePattern";
    public static final String POLICIES_SUFFIX = ".policies";
    public static final String STRATEGY_SUFFIX = ".strategy";
    public static final String TIME_SUFFIX = ".time";
    public static final String SIZE_SUFFIX = ".size";
    public static final String MAX_SUFFIX = ".max";
    public static final String INTERVAL_SUFFIX = ".interval";
    public static final String MODULATE_SUFFIX = ".modulate";

    // SSL related suffixes
    public static final String SSL_SUFFIX = ".sslconf";
    public static final String PROTOCOL_SUFFIX = ".protocol";
    public static final String KEYSTORE_LOCATION_SUFFIX = ".keyStoreLocation";
    public static final String KEYSTORE_PASSWORD_SUFFIX = ".keyStorePassword";
    public static final String TRUSTSTORE_LOCATION_SUFFIX = ".trustStoreLocation";
    public static final String TRUSTSTORE_PASSWORD_SUFFIX = ".trustStorePassword";
    public static final String DEFAULT_SSLCONF_TYPE = "SSLConf";
    public static final String DEFAULT_SSL_PROTOCOL = "SSL";
    public static final String VERIFY_HOSTNAME_SUFFIX = ".verifyHostName";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String TRUSTSTORE_PASSWORD = "trustStorePassword";
    public static final String TRUSTSTORE_LOCATION = "trustStoreLocation";
    public static final String CARBON_LOG_TYPE = "carbonLogType";
    public static final String CONNECT_TIMEOUT_MILLIS = "connectTimeoutMillis";
    public static final String AUDIT_LOG_TYPE = "auditLogType";
    public static final String URL = "url";
    public static final String VERIFY_HOSTNAME = "verifyHostName";
    public static final String KEYSTORE_PASSWORD = "keyStorePassword";
    public static final String KEYSTORE_LOCATION = "keyStoreLocation";
    public static final String CONNECTION_TIMEOUT = "connectionTimeout";
    public static final String REMOTE_SERVER_LOGGER_RESOURCE_PATH = "/identity/config/remoteServer";

    public static final String AUDIT = "AUDIT";
    public static final String CARBON = "CARBON";
    public static final String LOG_TYPE = "logType";

    public static final String REMOTE_LOGGING_HIDE_SECRETS = "RemoteLogging.HideSecrets";

    public enum LogType {
        AUDIT,
        CARBON
    }
}
