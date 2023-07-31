/*
 *
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.logging.appender.http.models;

import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

@Plugin(name = "SslConf", category = Core.CATEGORY_NAME, printObject = true)
public class SslConfiguration {

    private final String protocol;
    private final String keyStoreLocation;
    private final String keyStorePassword;
    private final String trustStoreLocation;
    private final String trustStorePassword;
    private final boolean verifyHostName;

    private SslConfiguration(final String protocol, final String keyStoreLocation, final String keyStorePassword,
                             final String trustStoreLocation, final String trustStorePassword,
                             boolean verifyHostName) {
        this.protocol = protocol;
        this.keyStoreLocation = keyStoreLocation;
        this.keyStorePassword = keyStorePassword;
        this.trustStoreLocation = trustStoreLocation;
        this.trustStorePassword = trustStorePassword;
        this.verifyHostName = verifyHostName;
    }

    @PluginFactory
    public static SslConfiguration createSslConfiguration(@PluginAttribute("protocol") final String protocol,
                                                          @PluginAttribute("keyStoreLocation") final String keyStoreLocation,
                                                          @PluginAttribute("keyStorePassword") final String keyStorePassword,
                                                          @PluginAttribute("trustStoreLocation") final String trustStoreLocation,
                                                          @PluginAttribute("trustStorePassword") final String trustStorePassword,
                                                          @PluginAttribute("verifyHostName") final boolean verifyHostName) {
        return new SslConfiguration(protocol, keyStoreLocation, keyStorePassword, trustStoreLocation,
                trustStorePassword, verifyHostName);
    }

    public String getProtocol() {
        return protocol;
    }

    public String getKeyStoreLocation() {
        return keyStoreLocation;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public String getTrustStoreLocation() {
        return trustStoreLocation;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public boolean isVerifyHostName() {
        return verifyHostName;
    }
}
