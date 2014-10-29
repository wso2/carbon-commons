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

import org.testng.annotations.Test;
import org.wso2.carbon.logging.service.LoggingConfigReaderException;
import org.wso2.carbon.logging.service.data.LoggingConfig;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Unit Tests for loading the configuration file names from the config file.
 */
public class LoggingConfigManagerTest {

    @Test(groups = {"org.wso2.carbon.logging.service.config"},
          description = "Test loading a configuration from the config file")
    public void testLoadLoggingConfiguration()
            throws XMLStreamException, IOException, LoggingConfigReaderException {
        String configFileNameWithPath = "." + File.separator + "src" + File.separator + "test" +
                                        File.separator + "resources" + File.separator +
                                        "logging-config.xml";
        LoggingConfig loggingConfig = LoggingConfigManager
                .loadLoggingConfiguration(configFileNameWithPath);
        assertEquals(loggingConfig.getLogProviderImplClassName(),
                     "org.wso2.carbon.logging.service.provider.InMemoryLogProvider",
                     "Unexpected LogProvider implementation class name was returned.");
        assertEquals(loggingConfig.getLogFileProviderImplClassName(),
                     "org.wso2.carbon.logging.service.provider.FileLogProvider",
                     "Unexpected LogFileProvider implementation class name was returned.");
    }


    @Test(groups = {"org.wso2.carbon.logging.service.config"},
          description = "Test loading a LogFileProvider configuration that contains some properties " +
                        "from the config file")
    public void testLoadLogProviderConfigurationWithProperties()
            throws XMLStreamException, IOException, LoggingConfigReaderException {
        String configFileNameWithPath = "." + File.separator + "src" + File.separator + "test" +
                                        File.separator + "resources" + File.separator +
                                        "logging-config-with-properties.xml";
        LoggingConfig loggingConfig = LoggingConfigManager
                .loadLoggingConfiguration(configFileNameWithPath);
        assertEquals(loggingConfig.getLogProviderImplClassName(),
                     "org.wso2.carbon.logging.service.provider.DummyCassandraLogProvider",
                     "Unexpected LogProvider implementation class name was returned.");

        String userName = "userName";
        assertEquals(loggingConfig.getLogProviderProperty(userName), "admin",
                     "Invalid property for: " + userName + " was returned.");

        String cassandraHost = "cassandraHost";
        assertEquals(loggingConfig.getLogProviderProperty(cassandraHost), "localhost:9160",
                     "Invalid property for: " + cassandraHost + " was returned.");

        String keyspace = "keyspace";
        assertEquals(loggingConfig.getLogProviderProperty(keyspace), "EVENT_KS",
                     "Invalid property for: " + keyspace + " was returned.");

        assertEquals(loggingConfig.getLogFileProviderImplClassName(),
                     "org.wso2.carbon.logging.service.provider.DummyFileLogProvider",
                     "Unexpected LogFileProvider implementation class name was returned.");
    }

    @Test(groups = {"org.wso2.carbon.logging.service.config"},
          description = "Test loading a LogFileProvider configuration that contains some properties " +
                        "from the config file")
    public void testLoadLogFileProviderConfigurationWithProperties()
            throws XMLStreamException, IOException, LoggingConfigReaderException {
        String configFileNameWithPath = "." + File.separator + "src" + File.separator + "test" +
                                        File.separator + "resources" + File.separator +
                                        "logging-config-with-properties.xml";
        LoggingConfig loggingConfig = LoggingConfigManager
                .loadLoggingConfiguration(configFileNameWithPath);
        assertEquals(loggingConfig.getLogFileProviderImplClassName(),
                     "org.wso2.carbon.logging.service.provider.DummyFileLogProvider",
                     "Unexpected LogFileProvider implementation class name was returned.");

        String userName = "userName";
        assertEquals(loggingConfig.getLogFileProviderProperty(userName), "admin",
                     "Invalid property for: " + userName + " was returned.");

        String password = "password";
        assertEquals(loggingConfig.getLogFileProviderProperty(password), "admin",
                     "Invalid property for: " + password + " was returned.");

        String port = "port";
        assertEquals(loggingConfig.getLogFileProviderProperty(port), "8080",
                     "Invalid property for: " + port + " was returned.");

        String domain = "domain";
        assertEquals(loggingConfig.getLogFileProviderProperty(domain), "carbon.super",
                     "Invalid property for: " + domain + " was returned.");

        assertEquals(loggingConfig.getLogProviderImplClassName(),
                     "org.wso2.carbon.logging.service.provider.DummyCassandraLogProvider",
                     "Unexpected LogProvider implementation class name was returned.");
    }

    @Test(groups = {"org.wso2.carbon.logging.service.config"},
          description = "Test loading an invalid configuration, should throw an exception",
          expectedExceptions = org.apache.axiom.om.OMException.class)
    public void testLoadInvalidConfiguration()
            throws IOException, XMLStreamException, LoggingConfigReaderException {
        String configFileNameWithPath = "." + File.separator + "src" + File.separator + "test" +
                                        File.separator + "resources" + File.separator +
                                        "invalid-config.xml";
        LoggingConfig loggingConfig = LoggingConfigManager
                .loadLoggingConfiguration(configFileNameWithPath);
        // Even in failure, an empty configuration should be returned, instead of a null.
        assertNotNull(loggingConfig, "Logging config was null.");
    }

    @Test(groups = {"org.wso2.carbon.logging.service.config"},
          description = "Test loading a non-existing configuration, should not throw an error, " +
                        "instead should load an empty config")
    public void testLoadNoConfiguration()
            throws IOException, XMLStreamException, LoggingConfigReaderException {
        String configFileNameWithPath = "invalid-config";
        LoggingConfig loggingConfig = LoggingConfigManager
                .loadLoggingConfiguration(configFileNameWithPath);
        // Even in failure, an empty configuration should be returned, instead of a null.
        assertNotNull(loggingConfig, "Logging config was null.");
    }
}
