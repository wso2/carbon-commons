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

package org.wso2.carbon.logging.appender.http;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.HttpManager;
import org.apache.logging.log4j.core.appender.HttpURLConnectionManager;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.net.ssl.KeyStoreConfiguration;
import org.apache.logging.log4j.core.net.ssl.StoreConfigurationException;
import org.apache.logging.log4j.core.net.ssl.TrustStoreConfiguration;
import org.wso2.carbon.logging.appender.http.models.SslConfiguration;
import org.wso2.carbon.logging.appender.http.utils.AppenderConstants;
import org.wso2.carbon.logging.appender.http.models.HttpConnectionConfig;
import org.wso2.carbon.logging.appender.http.utils.queue.PersistentQueue;
import org.wso2.carbon.logging.appender.http.utils.queue.PersistentQueueException;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Plugin(name = "SecuredHttp", category = "Core", elementType = "appender", printObject = true)
public class SecuredHttpAppender extends AbstractAppender {

    /**
     * Builds HttpAppender instances.
     * @param <B> The type to build
     */
    public static class Builder<B extends Builder<B>> extends AbstractAppender.Builder<B>
            implements org.apache.logging.log4j.core.util.Builder<SecuredHttpAppender> {

        @PluginBuilderAttribute
        @Required(message = "No URL provided for SecuredHttpAppender")
        private URL url;

        @PluginBuilderAttribute
        private String method = "POST";

        @PluginBuilderAttribute
        private int connectTimeoutMillis = 0;

        @PluginBuilderAttribute
        private int readTimeoutMillis = 0;

        @PluginBuilderAttribute
        private String username = "";

        @PluginBuilderAttribute
        private String password = "";

        @PluginElement("Headers")
        private Property[] headers;

        @PluginElement("SslConfiguration")
        private SslConfiguration sslConfiguration;

        @PluginBuilderAttribute
        private boolean verifyHostname = true;

        @PluginBuilderAttribute
        private int processingLimit = 10;

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

        public int getProcessingLimit() {
            return processingLimit;
        }

        public B setUrl(final URL url) {
            this.url = url;
            return asBuilder();
        }

        public B setMethod(final String method) {
            this.method = method;
            return asBuilder();
        }

        public B setConnectTimeoutMillis(final int connectTimeoutMillis) {
            this.connectTimeoutMillis = connectTimeoutMillis;
            return asBuilder();
        }

        public B setReadTimeoutMillis(final int readTimeoutMillis) {
            this.readTimeoutMillis = readTimeoutMillis;
            return asBuilder();
        }

        public B setUsername(final String username) {
            this.username = username;
            return asBuilder();
        }

        public B setPassword(final String password) {
            this.password = password;
            return asBuilder();
        }

        public B setHeaders(final Property[] headers) {
            this.headers = headers;
            return asBuilder();
        }

        public B setSslConfiguration(final SslConfiguration sslConfiguration) {
            this.sslConfiguration = sslConfiguration;
            return asBuilder();
        }

        public B setVerifyHostname(final boolean verifyHostname) {
            this.verifyHostname = verifyHostname;
            return asBuilder();
        }

        public B setProcessingLimit(final int processingLimit) {
            this.processingLimit = processingLimit;
            return asBuilder();
        }

        @Override
        public SecuredHttpAppender build() {
            HttpConnectionConfig httpConnectionConfig = new HttpConnectionConfig(getConfiguration(),
                    getConfiguration().getLoggerContext(), getName(), url, method, connectTimeoutMillis,
                    readTimeoutMillis, username, password, headers, sslConfiguration, verifyHostname);
            return new SecuredHttpAppender(getName(), getLayout(), getFilter(), isIgnoreExceptions(),
                    getPropertyArray(), httpConnectionConfig, processingLimit);
        }
    }

    /**
     * @return a builder for a SecuredHttpAppender.
     */
    @PluginBuilderFactory
    public static <B extends Builder<B>> B newBuilder() {
        return new Builder<B>().asBuilder();
    }

    private HttpManager manager = null;
    private PersistentQueue<LogEvent> persistentQueue;
    private final HttpConnectionConfig httpConnConfig;
    private final ScheduledExecutorService scheduler;
    private boolean isManagerInitialized = false;
    private Date lastFailureFreeTime;
    private boolean initialFailureWarningIssued = false;
    private boolean finalLogFailureCountWarningIssued = false;

    protected SecuredHttpAppender(final String name, final Layout<? extends Serializable> layout, final Filter filter,
                                  final boolean ignoreExceptions, final Property[] properties,
                                  HttpConnectionConfig httpConnectionConfig, final int processingLimit) {
        super(name, filter, layout, ignoreExceptions, properties);
        Objects.requireNonNull(layout, "layout");

        this.httpConnConfig = httpConnectionConfig;
        try {
            this.persistentQueue = PersistentQueue.getInstance(AppenderConstants.QUEUE_DIRECTORY_PATH, 1024*1024 * 100,
                    1024 * 100);
        } catch (PersistentQueueException e) {
            error("Error initializing the persistent queue", e);
        }

        scheduler = Executors.newScheduledThreadPool(AppenderConstants.SCHEDULER_CORE_POOL_SIZE);
        scheduler.scheduleWithFixedDelay(new LogPublisherTask(), AppenderConstants.SCHEDULER_INITIAL_DELAY,
                AppenderConstants.SCHEDULER_DELAY, TimeUnit.MILLISECONDS);
        lastFailureFreeTime = new Date();
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public synchronized void append(LogEvent event) {

        if (!isManagerInitialized && ServerStartupMonitor.isInitialized()) {
            isManagerInitialized = initManager();
        }
        try {
            persistentQueue.enqueue(event.toImmutable());
        } catch (PersistentQueueException e) {
            error("An error was encountered when attempting to save logs to the queue.", e);
        }
    }

    @Override
    public boolean stop(final long timeout, final TimeUnit timeUnit) {
        setStopping();
        if (scheduler != null) {
            try {
                scheduler.shutdown();
                scheduler.awaitTermination(AppenderConstants.SCHEDULER_TERMINATION_DELAY, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // (Re-)Cancel if current thread also interrupted
                scheduler.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
                error("Interrupted while awaiting for Schedule Executor termination" + e.getMessage(), e);
            }
        }
        boolean stopped = super.stop(timeout, timeUnit, false);
        if (manager != null) {
            stopped &= manager.stop(timeout, timeUnit);
        }
        setStopped();
        isManagerInitialized = false;
        return stopped;
    }

    @Override
    public String toString() {
        return "SecuredHttpAppender{" +
                "name=" + getName() +
                ", state=" + getState() +
                '}';
    }

    private boolean initManager() {
        // Add authorization header if username and password is provided
        String authHeaderValue = getAuthHeaderValue(httpConnConfig.getUsername(), httpConnConfig.getPassword());
        if (authHeaderValue != null) {
            httpConnConfig.addHeader(AppenderConstants.AUTHORIZATION_HEADER, authHeaderValue);
        }

        boolean isHttps = httpConnConfig.getUrl().getProtocol().equalsIgnoreCase(AppenderConstants.HTTPS);
        if (isHttps && httpConnConfig.getSslConfiguration() == null) {
            error("SSL configuration is not provided for HTTPS scheme.");
            return false;
        }

        if (!isHttps && httpConnConfig.getSslConfiguration() != null) {
            error("SSL configuration can only be provided for HTTPS scheme.");
            return false;
        }

        org.apache.logging.log4j.core.net.ssl.SslConfiguration sslConfiguration = null;
        if (httpConnConfig.getSslConfiguration() != null) {
            String keystorePassword = resolveSecretPassword(httpConnConfig.getSslConfiguration().getKeyStorePassword());
            String truststorePassword = resolveSecretPassword(httpConnConfig.getSslConfiguration().getTrustStorePassword());
            try {
                KeyStoreConfiguration keyStoreConfiguration = KeyStoreConfiguration.createKeyStoreConfiguration(
                        httpConnConfig.getSslConfiguration().getKeyStoreLocation(),
                        keystorePassword.toCharArray(), null, null, null, null);

                TrustStoreConfiguration trustStoreConfiguration = TrustStoreConfiguration.createKeyStoreConfiguration(
                        httpConnConfig.getSslConfiguration().getTrustStoreLocation(),
                        truststorePassword.toCharArray(), null, null, null, null);

                sslConfiguration = org.apache.logging.log4j.core.net.ssl.SslConfiguration.createSSLConfiguration(
                        httpConnConfig.getSslConfiguration().getProtocol(), keyStoreConfiguration, trustStoreConfiguration);
            } catch (StoreConfigurationException e) {
                error("Error initializing the SSL configuration", e);
                return false;
            }
        }

        // Initialize the http manager
        manager = new HttpURLConnectionManager(httpConnConfig.getConfiguration(), httpConnConfig.getLoggerContext(),
                httpConnConfig.getName(), httpConnConfig.getUrl(), httpConnConfig.getMethod(),
                httpConnConfig.getConnectTimeoutMillis(), httpConnConfig.getReadTimeoutMillis(),
                httpConnConfig.getHeaders(), sslConfiguration, httpConnConfig.isVerifyHostname());
        manager.startup();
        return true;
    }

    private String getAuthHeaderValue(String username, String password) {
        // if both username and password is not provided, do not set the header
        boolean hasUsername = username != null && !username.isEmpty();
        boolean hasPassword = password != null && !password.isEmpty();
        if (!hasUsername && !hasPassword) {
            return null;
        } else if (!hasUsername) {
            throw new IllegalArgumentException("Username is not provided for SecuredHttpAppender");
        } else if (!hasPassword) {
            throw new IllegalArgumentException("Password is not provided for SecuredHttpAppender");
        }

        password = resolveSecretPassword(password);
        // get base64 hash of the "username:password" string
        String authString = username + ":" + password;
        return AppenderConstants.BASIC_AUTH_PREFIX + new String(Base64.getEncoder().encode(authString.getBytes()));
    }

    private String resolveSecretPassword(String password) {
        if (password.startsWith("$secret{") && password.endsWith("}")) {
            String alias = password.substring(password.indexOf("{") + 1, password.lastIndexOf("}"));
            Properties properties = new Properties();
            properties.put("password", password);
            SecretResolver secretResolver = SecretResolverFactory.create(properties);
            if (secretResolver.isInitialized()) {
                if (secretResolver.isTokenProtected(alias)) {
                    return secretResolver.resolve(alias);
                }
            }
        }
        return password;
    }

    private void printWarningLogOnRemoteServerFailure() throws IOException {
        // todo: change to match the new implementation of the queue
        long failureCountWarningThreshold = persistentQueue.getMaxDiskSpaceInBytes()/2;
        if(!initialFailureWarningIssued) {
            long timeSinceLastPublished = new Date().getTime() - lastFailureFreeTime.getTime();
            int FAILURE_WARNING_DELAY_MINUTES = 15;
            // initial warning in 15 minutes of no logs published.
            if (timeSinceLastPublished > TimeUnit.MINUTES.toMillis(FAILURE_WARNING_DELAY_MINUTES)) {
                getStatusLogger().warn("No logs have been published to the remote server for " +
                        FAILURE_WARNING_DELAY_MINUTES + " minutes. Please check the remote server status.");
                initialFailureWarningIssued = true;
            }
        } else if (!finalLogFailureCountWarningIssued
                && persistentQueue.getCurrentDiskUsage() > failureCountWarningThreshold) {
            // final warning when the queue size exceeds 50% of the queue limit.
            getStatusLogger().warn("The number of logs in the queue has exceeded 50% of the allocated queue limit. " +
                    "Please check the remote server status.");
            finalLogFailureCountWarningIssued = true;
        }
    }

    private void resetWarningLogOnRemoteServerSuccess() {

        initialFailureWarningIssued = false;
        finalLogFailureCountWarningIssued = false;
        lastFailureFreeTime = new Date();
    }

    private final class LogPublisherTask implements Runnable {
        @Override
        public void run() {

            // avoiding publishing attempts before the HttpManager is initialized
            if (!isManagerInitialized) return;
            // publish logs from the queue
            try {
                LogEvent event = (LogEvent) persistentQueue.dequeue();
                if(event!=null) {
                    manager.send(getLayout(), event);
                    resetWarningLogOnRemoteServerSuccess();
                }
            } catch (Exception e) {
                persistentQueue.undoPreviousDequeue(); //todo: implement this
                printWarningLogOnRemoteServerFailure(); // todo: handle this
            }
        }
    }
}
