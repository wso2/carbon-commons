/*
 * Copyright 2005,2014 WSO2, Inc. http://www.wso2.org
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

package org.wso2.carbon.logging.service.config;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.logging.service.LoggingConfigReaderException;
import org.wso2.carbon.logging.service.data.LoggingConfig;
import org.wso2.carbon.logging.service.util.LoggingConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

/***
 * This class is used to read the logging configurations from the configuration file.
 * eg file: <CARBON_SERVER>/repository/conf/etc/logging-config.xml
 */
public class LoggingConfigManager {

    private static final Log log = LogFactory.getLog(LoggingConfigManager.class);
    private static BundleContext bundleContext;

    public static void setBundleContext(BundleContext bundleContext) {
        LoggingConfigManager.bundleContext = bundleContext;
    }

    /**
     * Loads the given logging configuration file.
     *
     * @param configFilenameWithPath
     *         Name of the configuration file
     * @return the syslog configuration data. An empty configuration will be returned, if there any
     * issue while loading configurations.
     */
    public static LoggingConfig loadLoggingConfiguration(String configFilenameWithPath)
            throws IOException, XMLStreamException, LoggingConfigReaderException {
        InputStream inputStream;
        LoggingConfig config = new LoggingConfig();
        inputStream = new LoggingConfigManager().getInputStream(configFilenameWithPath);
        if (inputStream != null) {
            try {
                XMLStreamReader parser = XMLInputFactory.newInstance()
                                                        .createXMLStreamReader(inputStream);
                StAXOMBuilder builder = new StAXOMBuilder(parser);
                OMElement documentElement = builder.getDocumentElement();
                // load log provider configurations
                OMElement logProviderConfig = documentElement.getFirstChildWithName(
                        getQName(LoggingConstants.LogConfigProperties.LOG_PROVIDER_CONFIG));
                config = loadLogProviderProperties(config, logProviderConfig);
                // load log file provider configurations
                OMElement logFileProviderConfig = documentElement.getFirstChildWithName(
                        getQName(LoggingConstants.LogConfigProperties.LOG_FILE_PROVIDER_CONFIG));
                config = loadLogFileProviderProperties(config, logFileProviderConfig);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        log.error("Could not close the logging configuration file "
                                  + configFilenameWithPath, e);
                    }
                }
            }
        }
        return config;
    }

    /**
     * Load log file provider configs from the logging-config xml file.
     *
     * @param config
     *         - logging config which will be injected with config information
     * @param logFileProviderConfig
     *         - OM Element containing from the xml file
     * @return - logging config with injected LogFileProvider data
     * @throws LoggingConfigReaderException
     *         - if an error occured while parsing the xml file.
     */
    private static LoggingConfig loadLogFileProviderProperties(LoggingConfig config,
                                                               OMElement logFileProviderConfig)
            throws LoggingConfigReaderException {
        String implClass = logFileProviderConfig.getAttributeValue(
                new QName("", LoggingConstants.LogConfigProperties.CLASS_ATTRIBUTE));
        if (implClass != null) {
            config.setLogFileProviderImplClassName(implClass);
            OMElement propertiesElement = logFileProviderConfig.getFirstChildWithName(
                    getQName(LoggingConstants.LogConfigProperties.PROPERTIES));
            if (propertiesElement != null) {
                Object nextElement;
                OMElement propertyElement;
                // iterate through each property element in the xml file and add (name, value)
                // to the map within the LoggingConfig object for LogProvider properties.
                for (Iterator it = propertiesElement.getChildrenWithLocalName(
                        LoggingConstants.LogConfigProperties.PROPERTY); it.hasNext(); ) {
                    nextElement = it.next();
                    if (nextElement instanceof OMElement) {
                        propertyElement = (OMElement) nextElement;
                        config.setLogFileProviderProperty(
                                propertyElement.getAttributeValue(new QName(
                                        LoggingConstants.LogConfigProperties.PROPERTY_NAME)),
                                propertyElement.getAttributeValue(new QName(
                                        LoggingConstants.LogConfigProperties.PROPERTY_VALUE)));
                    }
                }
            } else {
                String msg = "Error loading log file provider properties for " + implClass +
                             " Check the logging configuration file";
                throw new LoggingConfigReaderException(msg);
            }
        } else {
            String msg = "LogFileProvider implementation class name is null, " +
                         "check the logging configuration file";
            throw new LoggingConfigReaderException(msg);
        }
        return config;
    }

    /**
     * Load log provider configs from the logging-config xml file.
     *
     * @param config
     *         - logging config which will be injected with config information
     * @param logProviderConfig
     *         - OM Element containing from the xml file
     * @return - logging config with injected LogProvider data
     * @throws LoggingConfigReaderException
     *         - if an error occured while parsing the xml file.
     */
    private static LoggingConfig loadLogProviderProperties(LoggingConfig config,
                                                           OMElement logProviderConfig)
            throws LoggingConfigReaderException {
        String implClass = logProviderConfig.getAttributeValue(
                new QName("", LoggingConstants.LogConfigProperties.CLASS_ATTRIBUTE));
        if (implClass != null) {
            config.setLogProviderImplClassName(implClass);
            // load log provider configuration
            OMElement propertiesElement = logProviderConfig.getFirstChildWithName(
                    getQName(LoggingConstants.LogConfigProperties.PROPERTIES));
            if (propertiesElement != null) {
                OMElement propertyElement;
                Object nextElement;
                // iterate through each property element in the xml file and add (name, value)
                // to the map within the LoggingConfig object for LogFileProvider properties.
                for (Iterator it = propertiesElement.getChildrenWithLocalName(
                        LoggingConstants.LogConfigProperties.PROPERTY); it.hasNext(); ) {
                    nextElement = it.next();
                    if (nextElement instanceof OMElement) {
                        propertyElement = (OMElement) nextElement;
                        config.setLogProviderProperty(
                                propertyElement.getAttributeValue(new QName(
                                        LoggingConstants.LogConfigProperties.PROPERTY_NAME)),
                                propertyElement.getAttributeValue(new QName(
                                        LoggingConstants.LogConfigProperties.PROPERTY_VALUE)));
                    }
                }
            } else {
                String msg = "Error loading log provider properties for " + implClass +
                             " Check the logging configuration file ";
                throw new LoggingConfigReaderException(msg);
            }
        } else {
            String msg = "LogProvider implementation class name is null, " +
                         "Check the loggging configuration file";
            throw new LoggingConfigReaderException(msg);
        }
        return config;
    }

    private static QName getQName(String localName) {
        return new QName(LoggingConstants.LogConfigProperties.DEFAULT_LOGGING_CONFIG_NAMESPACE,
                         localName);
    }

    /**
     * Get an input stream to read the file after loading the logging-config file.
     *
     * @param configFilename
     *         - configuration file name
     * @return - an input stream to the file
     * @throws IOException
     *         - if the file could not be opened
     */
    private InputStream getInputStream(String configFilename)
            throws IOException {
        InputStream inputStream = null;
        File configFile = new File(configFilename);
        if (configFile.exists()) {
            inputStream = new FileInputStream(configFile);
        }
        String warningMessage;
        if (inputStream == null) {
            URL url;
            if (bundleContext != null) {
                if ((url = bundleContext.getBundle().getResource(
                        LoggingConstants.LOGGING_CONF_FILE)) != null) {
                    inputStream = url.openStream();
                } else {
                    warningMessage = "Bundle context could not find resource "
                                     + LoggingConstants.LOGGING_CONF_FILE
                                     + " or user does not have sufficient permission to access " +
                                     "the resource.";
                    log.warn(warningMessage);
                }

            } else {
                if ((url = this.getClass().getClassLoader()
                               .getResource(LoggingConstants.LOGGING_CONF_FILE)) != null) {
                    inputStream = url.openStream();
                } else {
                    warningMessage = "Could not find resource "
                                     + LoggingConstants.LOGGING_CONF_FILE
                                     + " or user does not have sufficient permission to access " +
                                     "the resource.";
                    log.warn(warningMessage);
                }
            }
        }
        return inputStream;
    }
}
