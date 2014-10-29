/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.logging.summarizer.utils;

import org.wso2.carbon.core.RegistryResources;

public final class LoggingConstants {

    public static final String LOGGERS = RegistryResources.LOGGING + "loggers/";
    public static final String APPENDERS = RegistryResources.LOGGING + "appenders/";
    public static final String SYSLOG = RegistryResources.LOGGERS + "syslog/SYSLOG_PROPERTIES";
    public static final String CASSANDRA = RegistryResources.LOGGERS + "cassandra/CASSANDRA_PROPERTIES";
    public static final String URL_SEPARATOR ="/";
    public static final String LOGGING_CONF_FILE ="summarizer-config.xml";
    public static final String SYSLOG_CONF_FILE ="syslog-config.xml";
    public static final String ETC_DIR = "etc";
    public static final String USERNAME_KEY = "username";
	public static final String PASSWORD_KEY = "password";
	public static final String DEFUALT_CLUSTER_NAME = "TestCluster";
    public static final String WSO2_STRATOS_MANAGER="WSO2 Stratos Manager";

    public static final String LOG_FILE_PATTERN = "log.file.pattern";
    public static final String LOG_CONSOLE_PATTERN = "log.console.pattern";
    public static final String LOG_MEMORY_PATTERN = "log.memory.pattern";
    public static final String MEMORY_APPENDER = "MemoryAppender";

    // global system settings
    public static final String SYSTEM_LOG_LEVEL = "wso2carbon.system.log.level";
    public static final String SYSTEM_LOG_PATTERN = "wso2carbon.system.log.pattern";
    public static final String LOG4J_FILE_LAST_MODIFIED = "wso2carbon.system.log.last.modified";

    public static final String LOG4J_FILE_FOUND = "log4j.file.not.found";
    public static final String CONFIG_FILENAME = "cloud-services-desc.xml";
    public static final String MULTITENANCY_CONFIG_FOLDER = "multitenancy";
    public static final String WSO2CARBON_CONSOLE_APPENDER = "CARBON_CONSOLE";
    public static final String WSO2CARBON_FILE_APPENDER = "CARBON_LOGFILE";
    public static final String WSO2CARBON_EVENT_APPENDER = "LOGEVENT";
    public static final String WSO2CARBON_MEMORY_APPENDER = "CARBON_MEMORY";
    public static final String WSO2CARBON_SYS_LOG_APPENDER = "CARBON_SYS_LOG";

	public static final int MEMORY_APPENDER_BUFFER_SZ = 200;

	public static final String DATE_FORMATTER = "yyyy-MM-dd";
	public static final String DATE_TIME_FORMATTER = "yyyy-MM-dd HH:mm:ss,SSS";
	public static final String GMT = "GMT";


    public static final class SyslogProperties {
    	public static final String LOG_INDEX_URL = "log.index.url";
    	public static final String SYSLOG_PORT = "syslog.port";
    	public static final String REALM = "realm";
    	public static final String USER_NAME ="user.name";
    	public static final String PASSWORD ="password";
    }

    public static final class HColumn {
    	public static final String TENANT_ID ="payload_tenantID";
    	public static final String SERVER_NAME = "payload_serverName";
    	public static final String APP_NAME = "payload_appName";
    	public static final String LOG_TIME ="payload_logTime";
    	public static final String LOGGER = "payload_logger";
    	public static final String PRIORITY = "payload_priority";
    	public static final String MESSAGE = "payload_message";
    	public static final String IP = "payload_ip";
    	public static final String STACKTRACE = "payload_stacktrace";
    	public static final String INSTANCE ="payload_instance";
    }

    public static final class CassandraProperties {
    	public static final String URL = "cassandra.host.url";
    	public static final String KEYSPACE = "cassandra.keyspace";
    	public static final String COLUMN_FAMILY ="cassandra.keyspace.column.family";
    	public static final String USER_NAME ="user.name";
    	public static final String PASSWORD ="password";
    	public static final String IS_CASSANDRA_AVAILABLE ="isCassandraAvailable";

    }


    public static final class LoggerProperties {
        public static final String NAME = "name";
        public static final String LOG_LEVEL = "log.level";
        public static final String ADDITIVITY = "additivity";
    }

    public static final class SyslogConfigProperties {
		public static final String SYSLOG_HOST = "syslogHost";
		public static final String PORT = "port";
		public static final String REALM = "realm";
		public static final String USER_NAME = "userName";
		public static final String PASSWORD = "password";
		public static final String LOG_PATTERN = "logPattern";
		public static final String IS_SYSLOG_ON ="isSyslogOn";
    }
    public static final class CassandraConfigProperties {
    	public static final String CASSANDRA_HOST = "cassandraHost";
    	public static final String KEYSPACE = "keyspace";
    	public static final String COLUMN_FAMILY ="columnFamily";
    	public static final String USER_NAME ="userName";
    	public static final String PASSWORD ="password";
    	public static final String IS_CASSANDRA_AVAILABLE ="isDataFromCassandra";
        public static final String IS_DELETE_COL_FAMILY = "deleteColFamily";
    	public static final String PUBLISHER_URL = "publisherURL";
    	public static final String PUBLISHER_USER = "publisherUser";
    	public static final String CLUSTER ="cluster";
    	public static final String PUBLISHER_PASSWORD = "publisherPassword";
    	public static final String ARCHIVED_HOST = "archivedHost";
    	public static final String ARCHIVED_USER = "archivedUser";
    	public static final String ARCHIVED_PASSWORD = "archivedPassword";
    	public static final String ARCHIVED_PORT = "archivedPort";
    	public static final String ARCHIVED_REALM = "archivedRealm";
    	public static final String HIVE_QUERY = "hiveQuery";
        public static final String CRON_EXPRESSION = "cronExpression";
        public static final String LOG_DIRECTORY = "logDirectory";
        public static final String TMP_LOG_DIRECTORY = "tmpLogDirectory";
        public static final String CONSISTENCY_LEVEL = "cassandraConsistencyLevel";
        public static final String AUTO_DISCOVERY_ENABLE = "enable";
        public static final String AUTO_DISCOVERY_DELAY = "delay";
        public static final String RETRY_DOWNED_HOSTS = "retryDownedHosts";
        public static final String RETRY_DOWNED_HOSTS_ENABLE = "enable";
        public static final String RETRY_DOWNED_HOSTS_QUEUE = "queueSize";
        public static final String AUTO_DISCOVERY = "cassandraAutoDiscovery";
    }

    public static final class BamProperties{
        public static final String BAM_USERNAME = "bamUserName";
        public static final String BAM_PASSWORD = "bamPassword";
    }

    public static final class HdfsProperties{
        public static final String HDFS_CONFIG = "hdfsConfig";
        public static final String ARCHIVED_LOG_LOCATION = "archivedLogLocation";
    }

    public static final class AppenderProperties {
        public static final String NAME = "name";
        public static final String PATTERN = "pattern";
        public static final String LOG_FILE_NAME = "log.file.name";
        public static final String IS_FILE_APPENDER = "is.file.appender";
        public static final String THRESHOLD = "threshold";
        public static final String FACILITY = "facility";
        public static final String SYS_LOG_HOST = "sys.log.host";
        public static final String IS_SYS_LOG_APPENDER = "is.sys.log.appender";
    }

    public static final class RegexPatterns {
    	public static final String HOST_PATTERN = "(\\d+\\.\\d+\\.\\d+\\.\\d+)";
    	public static final String CLOSE_BRACKET_PATTERN = "\\]  ";
    	public static final String OPEN_BRACKET_PATTERN = "\\[";
    	public static final String EXCEPTION_START_PATTERN = "\\sat\\s";
    	public static final String SYSLOG_DATE_PATTERN ="\\d{1,2}\\s\\d{2}:\\d{2}:\\d{2}\\s\\d{1,6}\\.\\d{1,6}\\.\\d{1,6}\\.\\d{1,6}";
    	public static final String OPEN_CURLY_BRACES_PATTERN="\\{";
    	public static final String NEW_LINE ="\n";
    	public static final String TENANT_PATTERN = "%T";
    	public static final String LOG_ERROR ="ERROR";
    	public static final String LOG_FATAL ="FATAL";
    	public static final String LOG_WARN ="WARN";
    	public static final String LOG_INFO ="INFO";
    	public static final String LOG_DEBUG ="DEBUG";
    	public static final String LOG_TRACE ="TRACE";
    	public static final String LOCAL_CARBON_LOG_PATTERN ="wso2carbon.log*";
    	public static final String LOG_FILE_DATE_SEPARATOR ="log.";
    	public static final String CURRENT_LOG = "0_Current Log";
    	public static final String SYSLOG_DOMAIN_PATTERN = ".*?([^.]+\\.[$.]+)";
    	public static final String SYS_LOG_FILE_NAME_PATTERN =".gz";
    	public static final String LOG_ERROR_HEADER_PATTERN = "\\[\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2},\\d{2,6}][\\s\\S.*]*\\s{0,2}ERROR";
    	public static final String LOG_WARN_HEADER_PATTERN = "\\[\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2},\\d{2,6}][\\s\\S.*]*\\s{0,2}WARN";
    	public static final String LOG_FATAL_HEADER_PATTERN = "\\[\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2},\\d{2,6}][\\s\\S.*]*\\s{0,2}FATAL";
    	public static final String LOG_DEBUG_HEADER_PATTERN = "\\[\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2},\\d{2,6}][\\s\\S.*]*\\s{0,2}DEBUG";
    	public static final String LOG_TRACE_HEADER_PATTERN = "\\[\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2},\\d{2,6}][\\s\\S.*]*\\s{0,2}TRACE";
    	public static final String LOG_INFO_HEADER_PATTERN = "\\[\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2},\\d{2,6}][\\s\\S.*]*\\s{0,2}INFO";
    	public static final String LOG_HEADER_PATTERN = "\\[\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2},\\d{2,6}]";
    	public static final String ERROR_LINE = "\\] ERROR \\{";
    	public static final String LINK_SEPARATOR_PATTERN ="a href=";
    	public static final String SYSLOG_DATE_SEPARATOR_PATTERN="<td align=\"right\">";
    	public static final String COLUMN_SEPARATOR_PATTERN ="</td>";
    	public static final String GT_PATTARN =">";
    	public static final String BACK_SLASH_PATTERN ="\"";
    }
}

