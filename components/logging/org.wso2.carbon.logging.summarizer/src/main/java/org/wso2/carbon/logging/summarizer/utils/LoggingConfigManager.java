/*
 * Copyright The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// TOD0 Change the packaging to org.wso2.carbon.logging.common

package org.wso2.carbon.logging.summarizer.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

public class LoggingConfigManager {

    private static final Log log = LogFactory.getLog(LoggingConfigManager.class);
    private static LoggingConfigManager cassandraConfig;
    private static BundleContext bundleContext;

    public static LoggingConfigManager getCassandraConfig() {
        return cassandraConfig;
    }

    public static void setBundleContext(BundleContext bundleContext) {
        LoggingConfigManager.bundleContext = bundleContext;
    }

    public static void setCassandraConfig(LoggingConfigManager syslogConfig) {
        LoggingConfigManager.cassandraConfig = syslogConfig;
    }

    public static Log getLog() {
        return log;
    }

    public LoggingConfig getSyslogData() {
        return null;
    }

    /**
     * Returns the configurations from the Cassandra configuration file.
     *
     * @return cassandra configurations
     */
    public static LoggingConfig loadLoggingConfiguration() {
        // gets the configuration file name from the cassandra-config.xml.
        String cassandraConfigFileName = CarbonUtils.getCarbonConfigDirPath()
                + RegistryConstants.PATH_SEPARATOR
                + LoggingConstants.ETC_DIR
                + RegistryConstants.PATH_SEPARATOR
                + LoggingConstants.LOGGING_CONF_FILE;
        return loadLoggingConfiguration(cassandraConfigFileName);
    }

    private InputStream getInputStream(String configFilename)
            throws IOException {
        InputStream inStream = null;
        File configFile = new File(configFilename);
        if (configFile.exists()) {
            inStream = new FileInputStream(configFile);
        }
        String warningMessage = "";
        if (inStream == null) {
            URL url;
            if (bundleContext != null) {
                if ((url = bundleContext.getBundle().getResource(
                        LoggingConstants.LOGGING_CONF_FILE)) != null) {
                    inStream = url.openStream();
                } else {
                    warningMessage = "Bundle context could not find resource "
                            + LoggingConstants.LOGGING_CONF_FILE
                            + " or user does not have sufficient permission to access the resource.";
                    log.warn(warningMessage);
                }

            } else {
                if ((url = this.getClass().getClassLoader()
                        .getResource(LoggingConstants.LOGGING_CONF_FILE)) != null) {
                    inStream = url.openStream();
                } else {
                    warningMessage = "Could not find resource "
                            + LoggingConstants.LOGGING_CONF_FILE
                            + " or user does not have sufficient permission to access the resource.";
                    log.warn(warningMessage);
                }
            }
        }
        return inStream;
    }


    private static LoggingConfig loadDefaultConfiguration() {
        LoggingConfig config = new LoggingConfig();
        config.setCassandraServerAvailable(false);
        return config;
    }

    /**
     * Loads the given logging Configuration file.
     *
     * @param configFilename Name of the configuration file
     * @return the syslog configuration data.
     */
    private static LoggingConfig loadLoggingConfiguration(
            String configFilename) {
        LoggingConfig config = new LoggingConfig();
        InputStream inputStream = null;
        try {
            inputStream = new LoggingConfigManager()
                    .getInputStream(configFilename);
        } catch (IOException e1) {
            log.error("Could not close the Configuration File "
                    + configFilename);
        }
        if (inputStream != null) {
            try {
                XMLStreamReader parser = XMLInputFactory.newInstance()
                        .createXMLStreamReader(inputStream);
                StAXOMBuilder builder = new StAXOMBuilder(parser);
                OMElement documentElement = builder.getDocumentElement();
                @SuppressWarnings("rawtypes")
                Iterator it = documentElement.getChildElements();
                while (it.hasNext()) {
                    OMElement element = (OMElement) it.next();
                    // Checks whether logging configuration enable.
                    if (LoggingConstants.CassandraConfigProperties.IS_CASSANDRA_AVAILABLE
                            .equals(element.getLocalName())) {
                        String isCassandraOn = element.getText();
                        // by default, make the loggingConfig off.
                        boolean isCassandraAvailable = false;
                        if (isCassandraOn.trim().equalsIgnoreCase("true")) {
                            isCassandraAvailable = true;
                        }
                        config.setCassandraServerAvailable(isCassandraAvailable);
                    }if (LoggingConstants.CassandraConfigProperties.IS_DELETE_COL_FAMILY
                            .equals(element.getLocalName())) {
                        String isDeleteColFamily = element.getText();
                        // by default, make the loggingConfig off.
                        boolean isDeleteColumnFamily = false;
                        if (isDeleteColFamily.trim().equalsIgnoreCase("true")) {
                            isDeleteColumnFamily = true;
                        }
                        config.setDeleteColFamily(isDeleteColumnFamily);
                    } else if (LoggingConstants.CassandraConfigProperties.USER_NAME
                            .equals(element.getLocalName())) {
                        config.setCassUsername(element.getText());
                    } else if (LoggingConstants.CassandraConfigProperties.PASSWORD
                            .equals(element.getLocalName())) {
                        config.setCassPassword(element.getText());
                    } else if (LoggingConstants.CassandraConfigProperties.CONSISTENCY_LEVEL
                            .equals(element.getLocalName())) {
                        config.setConsistencyLevel(element.getText());
                    } else if (LoggingConstants.CassandraConfigProperties.AUTO_DISCOVERY
                            .equals(element.getLocalName())) {

                        String autoDiscoveryEnable = element.getAttributeValue(
                                new QName(LoggingConstants.CassandraConfigProperties.AUTO_DISCOVERY_ENABLE));
                        boolean isAutoDiscoveryEnable = false;
                        if (autoDiscoveryEnable.trim().equalsIgnoreCase("true")) {
                            isAutoDiscoveryEnable = true;
                        }
                        config.setAutoDiscoveryEnable(isAutoDiscoveryEnable);

                        String delay = element.getAttributeValue(
                                new QName(LoggingConstants.CassandraConfigProperties.AUTO_DISCOVERY_DELAY));
                        if (delay != null && !"".equals(delay.trim())) {
                            int delayAsInt = -1;
                            try {
                                delayAsInt = Integer.parseInt(delay.trim());
                            } catch (NumberFormatException ignored) {
                            }
                            if (delayAsInt > 0) {
                                config.setAutoDiscoveryDelay(delayAsInt);
                            }
                        }
                    } else if (LoggingConstants.CassandraConfigProperties.RETRY_DOWNED_HOSTS
                            .equals(element.getLocalName())) {

                        String retryDownedEnable = element.getAttributeValue(
                                new QName(LoggingConstants.CassandraConfigProperties.RETRY_DOWNED_HOSTS_ENABLE));
                        boolean isRetryDownedHostsEnable = false;
                        if (retryDownedEnable.trim().equalsIgnoreCase("true")) {
                            isRetryDownedHostsEnable = true;
                        }
                        config.setRetryDownedHostsEnable(isRetryDownedHostsEnable);

                        String queue = element.getAttributeValue(
                                new QName(LoggingConstants.CassandraConfigProperties.RETRY_DOWNED_HOSTS_QUEUE));
                        if (queue != null && !"".equals(queue.trim())) {
                            int queueAsInt = -1;
                            try {
                                queueAsInt = Integer.parseInt(queue.trim());
                            } catch (NumberFormatException ignored) {
                            }
                            if (queueAsInt > 0) {
                                config.setRetryDownedHostsQueueSize(queueAsInt);
                            }
                        }
                    } else if (LoggingConstants.CassandraConfigProperties.PUBLISHER_URL
                            .equals(element.getLocalName())) {
                        config.setPublisherURL(element.getText());
                    } else if (LoggingConstants.CassandraConfigProperties.PUBLISHER_USER
                            .equals(element.getLocalName())) {
                        config.setPublisherUser(element.getText());
                    } else if (LoggingConstants.CassandraConfigProperties.PUBLISHER_PASSWORD
                            .equals(element.getLocalName())) {
                        config.setPublisherPassword(element.getText());
                    } else if (LoggingConstants.CassandraConfigProperties.CRON_EXPRESSION
                            .equals(element.getLocalName())) {
                        config.setCronExpression(element.getText());
                    } else if (LoggingConstants.CassandraConfigProperties.HIVE_QUERY
                            .equals(element.getLocalName())) {
                        config.setHiveQuery(element.getText());
                    }
                    else if (LoggingConstants.CassandraConfigProperties.LOG_DIRECTORY
                            .equals(element.getLocalName())) {
                        config.setLogDirectory(element.getText());
                    }
                     else if (LoggingConstants.CassandraConfigProperties.TMP_LOG_DIRECTORY
                            .equals(element.getLocalName())) {
                        config.setTmpLogsDirectory(element.getText());
                    }
                    else if (LoggingConstants.CassandraConfigProperties.CASSANDRA_HOST
                            .equals(element.getLocalName())) {
                        config.setCassandraHost(element.getText());
                    }
                     else if (LoggingConstants.BamProperties.BAM_USERNAME
                            .equals(element.getLocalName())) {
                        config.setBamUserName(element.getText());
                    }
                     else if (LoggingConstants.BamProperties.BAM_PASSWORD
                            .equals(element.getLocalName())) {
                        config.setBamPassword(element.getText());
                    }
                    else if (LoggingConstants.HdfsProperties.HDFS_CONFIG
                            .equals(element.getLocalName())) {
                        config.setHdfsConfig(element.getText());
                    }
                    else if (LoggingConstants.HdfsProperties.ARCHIVED_LOG_LOCATION
                            .equals(element.getLocalName())) {
                        config.setArchivedLogLocation(element.getText());
                    }
                }
                //setting the default valus to config
                config.setColFamily("log");
                config.setCluster("admin");
                config.setKeyspace("EVENT_KS");
                return config;
            } catch (Exception
                    e) {
                String msg = "Error in loading Stratos Configurations File: "
                        + configFilename + ". Default Settings will be used.";
                log.error(msg, e);
                return loadDefaultConfiguration(); // returns the default
                // configurations, if the
                // file could not be loaded.
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        log.error("Could not close the Configuration File "
                                + configFilename);
                    }
                }
            }
        }

        log.error("Unable to locate the stratos configurations file. "
                + "Default Settings will be used.");
        return

                loadDefaultConfiguration(); // return the default configurations,
        // if the file not found.
    }
}
