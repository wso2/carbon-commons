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

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;

import java.net.URL;

public class HttpConnectionConfig {
    private final Configuration configuration;
    private final LoggerContext loggerContext;
    private final String name;
    private final URL url;
    private final String method;
    private final int connectTimeoutMillis;
    private final int readTimeoutMillis;
    private final String username;
    private final String password;
    private Property[] headers;
    private final SslConfiguration sslConfiguration;
    private final boolean verifyHostname;

    public HttpConnectionConfig(Configuration configuration, LoggerContext loggerContext, String name, URL url,
                                String method, int connectTimeoutMillis, int readTimeoutMillis, String username,
                                String password, Property[] headers, SslConfiguration sslConfiguration,
                                boolean verifyHostname) {
        this.configuration = configuration;
        this.loggerContext = loggerContext;
        this.name = name;
        this.url = url;
        this.method = method;
        this.connectTimeoutMillis = connectTimeoutMillis;
        this.readTimeoutMillis = readTimeoutMillis;
        this.username = username;
        this.password = password;
        this.headers = headers;
        this.sslConfiguration = sslConfiguration;
        this.verifyHostname = verifyHostname;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public LoggerContext getLoggerContext() {
        return loggerContext;
    }

    public String getName() {
        return name;
    }

    public URL getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public int getReadTimeoutMillis() {
        return readTimeoutMillis;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Property[] getHeaders() {
        return headers;
    }

    public SslConfiguration getSslConfiguration() {
        return sslConfiguration;
    }

    public boolean isVerifyHostname() {
        return verifyHostname;
    }

    public void addHeader(String key, String value) {
        if (headers == null) {
            headers = new Property[1];
            headers[0] = Property.createProperty(key, value);
        } else {
            Property[] newHeaders = new Property[headers.length + 1];
            System.arraycopy(headers, 0, newHeaders, 0, headers.length);
            newHeaders[headers.length] = Property.createProperty(key, value);
            headers = newHeaders;
        }
    }
}
