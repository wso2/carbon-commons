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
import org.wso2.carbon.logging.service.util.LoggingConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

public class SyslogConfigManager {

    private static final Log log = LogFactory.getLog(SyslogConfigManager.class);
    private static SyslogConfigManager syslogConfig;
    private static BundleContext bundleContext;

    public static SyslogConfigManager getSyslogConfig() {
        return syslogConfig;
    }

    public static void setSyslogConfig(SyslogConfigManager syslogConfig) {
        SyslogConfigManager.syslogConfig = syslogConfig;
    }

    public static void setBundleContext(BundleContext bundleContext) {
        SyslogConfigManager.bundleContext = bundleContext;
    }

    public static Log getLog() {
        return log;
    }

    public static String getSyslogPattern() {
        SyslogConfiguration config = loadSyslogConfiguration();
        return config.getSyslogLogPattern();
    }

    /**
     * Returns the configurations from the syslog configuration file.
     *
     * @return syslog configurations
     */
    public static SyslogConfiguration loadSyslogConfiguration() {
        // gets the configuration file name from the syslog-config.xml.
        String syslogConfigFileName = CarbonUtils.getCarbonConfigDirPath()
                          + RegistryConstants.PATH_SEPARATOR + LoggingConstants.ETC_DIR
                          + RegistryConstants.PATH_SEPARATOR + LoggingConstants.SYSLOG_CONF_FILE;
        return loadSyslogConfiguration(syslogConfigFileName);
    }

    private static SyslogConfiguration loadDefaultConfiguration() {
        SyslogConfiguration config = new SyslogConfiguration();
        config.setSyslogOn(false);
        return config;
    }

    /**
     * Loads the given Syslog Configuration file.
     *
     * @param configFilename
     *         Name of the configuration file
     * @return the syslog configuration data.
     */
    private static SyslogConfiguration loadSyslogConfiguration(String configFilename) {
        SyslogConfiguration config = new SyslogConfiguration();
        InputStream inputStream = null;
        try {
            inputStream = new SyslogConfigManager().getInputStream(configFilename);
        } catch (IOException e) {
            log.error("Could not close the Configuration File " + configFilename, e);
        }
        if (inputStream != null) {
            try {
                XMLStreamReader parser =
                        XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
                StAXOMBuilder builder = new StAXOMBuilder(parser);
                OMElement documentElement = builder.getDocumentElement();
                Iterator it = documentElement.getChildElements();
                while (it.hasNext()) {
                    OMElement element = (OMElement) it.next();
                    //Checks whether syslog configuration enable.
                    if (LoggingConstants.SyslogConfigProperties.IS_SYSLOG_ON
                            .equals(element.getLocalName())) {
                        String isSyslogOn = element.getText();
                        //by default, make the syslog off.
                        boolean isSyslogOnRequired = false;
                        if ("true".equalsIgnoreCase(isSyslogOn.trim())) {
                            isSyslogOnRequired = true;
                        }
                        config.setSyslogOn(isSyslogOnRequired);
                    } else if (LoggingConstants.SyslogConfigProperties.SYSLOG_HOST
                            .equals(element.getLocalName())) {
                        config.setSyslogHostURL(element.getText());
                    } else if (LoggingConstants.SyslogConfigProperties.PORT
                            .equals(element.getLocalName())) {
                        config.setPort(element.getText());
                    } else if (LoggingConstants.SyslogConfigProperties.REALM
                            .equals(element.getLocalName())) {
                        config.setRealm(element.getText());
                    } else if (LoggingConstants.SyslogConfigProperties.USER_NAME
                            .equals(element.getLocalName())) {
                        config.setUserName(element.getText());
                    } else if (LoggingConstants.SyslogConfigProperties.PASSWORD
                            .equals(element.getLocalName())) {
                        config.setPassword(element.getText());
                    } else if (LoggingConstants.SyslogConfigProperties.LOG_PATTERN
                            .equals(element.getLocalName())) {
                        config.setSyslogLogPattern(element.getText());
                    }
                }
                return config;
            } catch (Exception e) {
                String msg = "Error in loading Stratos Configurations File: " + configFilename +
                             ". Default Settings will be used.";
                log.error(msg, e);
                //returns the default configurations, if the file could not be loaded.
                return loadDefaultConfiguration();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        log.error("Could not close the Configuration File " + configFilename, e);
                    }
                }
            }
        }
        log.error("Unable to locate the stratos configurations file. " +
                  "Default Settings will be used.");
        // return the default configurations, if the file is not found.
        return loadDefaultConfiguration();
    }

    private InputStream getInputStream(String configFilename) throws IOException {
        InputStream inStream = null;
        File configFile = new File(configFilename);
        if (configFile.exists()) {
            inStream = new FileInputStream(configFile);
        }
        String warningMessage;
        if (inStream == null) {
            URL url;
            if (bundleContext != null) {
                if ((url = bundleContext.getBundle()
                                        .getResource(LoggingConstants.SYSLOG_CONF_FILE)) != null) {
                    inStream = url.openStream();
                } else {
                    warningMessage = "Bundle context could not find resource "
                                     + LoggingConstants.SYSLOG_CONF_FILE
                                     + " or user does not have sufficient permission to access " +
                                     "the resource.";
                    log.error(warningMessage);
                }

            } else {
                if ((url = this.getClass().getClassLoader()
                               .getResource(LoggingConstants.SYSLOG_CONF_FILE)) != null) {
                    inStream = url.openStream();
                } else {
                    warningMessage = "Could not find resource "
                                     + LoggingConstants.SYSLOG_CONF_FILE
                                     + " or user does not have sufficient permission to access the resource.";
                    log.error(warningMessage);
                }
            }
        }
        return inStream;
    }

}
