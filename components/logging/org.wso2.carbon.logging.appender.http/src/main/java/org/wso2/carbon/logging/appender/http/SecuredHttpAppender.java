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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.wso2.carbon.logging.appender.http.models.HttpConnectionConfig;
import org.wso2.carbon.logging.appender.http.models.SslConfiguration;
import org.wso2.carbon.logging.appender.http.utils.AppenderConstants;
import org.wso2.carbon.logging.appender.http.utils.queue.PersistentQueue;
import org.wso2.carbon.logging.appender.http.utils.queue.exception.PersistentQueueException;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import java.io.Serializable;
import java.net.URL;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This implements the log4j appended that is used to publish logs to a remote server via HTTP.
 */
@Plugin(name = "SecuredHttp", category = "Core", elementType = "appender", printObject = true)
public class SecuredHttpAppender extends AbstractAppender {
    
    private static final Log log = LogFactory.getLog(SecuredHttpAppender.class);

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
        private int processingLimit = 1000;

        @PluginBuilderAttribute
        private int blockSizeInKB = 256;

        @PluginBuilderAttribute
        private int maxDiskSpaceInMB = 100;

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

        /**
         * The processing limit is no longer used. The queue size is now limited by the maxDiskSpaceInMB.
         * @deprecated Use {@link #getMaxDiskSpaceInMB()} instead.
         */
        @Deprecated
        public int getProcessingLimit() {
            return processingLimit;
        }

        public boolean isVerifyHostname() {
            return verifyHostname;
        }

        public int getBlockSizeInKB() {
            return blockSizeInKB;
        }

        public int getMaxDiskSpaceInMB() {
            return maxDiskSpaceInMB;
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

        /**
         * The processing limit is no longer used. The queue size is now limited by the maxDiskSpaceInMB.
         * @deprecated Use {@link #setMaxDiskSpaceInMB(int)} instead.
         */
        @Deprecated
        public B setProcessingLimit(final int processingLimit) {
            this.processingLimit = processingLimit;
            return asBuilder();
        }

        public B setVerifyHostname(final boolean verifyHostname) {
            this.verifyHostname = verifyHostname;
            return asBuilder();
        }

        public B setBlockSizeInKB(final int blockSizeInKB) {
            this.blockSizeInKB = blockSizeInKB;
            return asBuilder();
        }

        public B setMaxDiskSpaceInMB(final int maxDiskSpaceInMB) {
            this.maxDiskSpaceInMB = maxDiskSpaceInMB;
            return asBuilder();
        }

        @Override
        public SecuredHttpAppender build() {
            HttpConnectionConfig httpConnectionConfig = new HttpConnectionConfig(getConfiguration(),
                    getConfiguration().getLoggerContext(), getName(), url, method, connectTimeoutMillis,
                    readTimeoutMillis, username, password, headers, sslConfiguration, verifyHostname);
            return new SecuredHttpAppender(getName(), getLayout(), getFilter(), isIgnoreExceptions(),
                    getPropertyArray(), httpConnectionConfig,  maxDiskSpaceInMB * 1024 * 1024, blockSizeInKB * 1024);
        }
    }

    /**
     * Enum to represent the warning levels of the failure to publish logs to the remote server.
     */
    public enum FailureWaringLevel {
        NONE,
        INITIAL,
        HALF_QUEUE_SIZE,
        OVER_90_PERCENT,
        FULLY_USED,
        LOOSING_LOGS
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
    private FailureWaringLevel failureWarningLevel;
    private long lastLogLossWarningTime;
    private static final int FAILURE_WARNING_DELAY_MINUTES = 1;

    protected SecuredHttpAppender(final String name, final Layout<? extends Serializable> layout, final Filter filter,
                                  final boolean ignoreExceptions, final Property[] properties,
                                  HttpConnectionConfig httpConnectionConfig, int maxDiskSpaceInBytes,
                                  int maxBatchSizeInBytes) {

        super(name, filter, layout, ignoreExceptions, properties);
        Objects.requireNonNull(layout, "layout");

        this.httpConnConfig = httpConnectionConfig;
        this.failureWarningLevel = FailureWaringLevel.NONE;

        try {
            validateDiskSpace(maxDiskSpaceInBytes);
        } catch (IllegalArgumentException e) {
            maxDiskSpaceInBytes = AppenderConstants.MINIMUM_DISK_SPACE_IN_BYTES;
            final String warningMessage = String.format("%s Disk space size set to default value of %d bytes.",
                    e.getMessage(), AppenderConstants.MINIMUM_DISK_SPACE_IN_BYTES);
            log.warn(warningMessage);
        }
        try {
            validateBatchSize(maxBatchSizeInBytes, maxDiskSpaceInBytes);
        } catch (IllegalArgumentException e) {
            maxBatchSizeInBytes = AppenderConstants.MINIMUM_BATCH_SIZE_IN_BYTES;
            final String warningMessage = String.format("%s Batch size set to default value of %d bytes.",
                    e.getMessage(), AppenderConstants.MINIMUM_BATCH_SIZE_IN_BYTES);
            log.warn(warningMessage);
        }

        try {
            this.persistentQueue = new PersistentQueue<>(AppenderConstants.QUEUE_DIRECTORY_PATH,
                    maxDiskSpaceInBytes, maxBatchSizeInBytes);
        } catch (PersistentQueueException e) {
            error("Error initializing the persistent queue", e);
        }

        scheduler = Executors.newScheduledThreadPool(AppenderConstants.SCHEDULER_CORE_POOL_SIZE);
        scheduler.scheduleWithFixedDelay(new LogPublisherTask(), AppenderConstants.SCHEDULER_INITIAL_DELAY,
                AppenderConstants.SCHEDULER_DELAY, TimeUnit.MILLISECONDS);
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
            if (e.getErrorType().equals(
                    PersistentQueueException.PersistentQueueErrorTypes.QUEUE_DISK_SPACE_LIMIT_EXCEEDED)) {
                    if (this.failureWarningLevel != FailureWaringLevel.LOOSING_LOGS) {
                        this.failureWarningLevel = FailureWaringLevel.FULLY_USED;
                    }
                    printWarningLogOnRemoteServerFailure();
            } else {
                error("An error was encountered when attempting to save logs to the queue.", e);
            }
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
            String truststorePassword = resolveSecretPassword(httpConnConfig.getSslConfiguration()
                    .getTrustStorePassword());
            try {
                KeyStoreConfiguration keyStoreConfiguration = KeyStoreConfiguration.createKeyStoreConfiguration(
                        httpConnConfig.getSslConfiguration().getKeyStoreLocation(),
                        keystorePassword.toCharArray(), null, null, null, null);

                TrustStoreConfiguration trustStoreConfiguration = TrustStoreConfiguration.createKeyStoreConfiguration(
                        httpConnConfig.getSslConfiguration().getTrustStoreLocation(),
                        truststorePassword.toCharArray(), null, null, null, null);

                sslConfiguration = org.apache.logging.log4j.core.net.ssl.SslConfiguration.createSSLConfiguration(
                        httpConnConfig.getSslConfiguration().getProtocol(), keyStoreConfiguration,
                        trustStoreConfiguration);
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

    private void printWarningLogOnRemoteServerFailure() {

        switch(this.failureWarningLevel) {
            case NONE:
                log.warn("Remote log publishing failure : Unable to publish logs to the remote server. " +
                        "Please check the remote server status.");
                this.failureWarningLevel = getCurrentWarningLevel();
                break;
            case INITIAL:
                if (persistentQueue.getUsedSpaceFraction() > 0.5) {
                    log.warn("Remote log publishing failure : The number of logs in the queue has " +
                            "exceeded 50% of the allocated queue limit. Please check the remote server status.");
                    this.failureWarningLevel = FailureWaringLevel.HALF_QUEUE_SIZE;
                }
                break;
            case HALF_QUEUE_SIZE:
                if (persistentQueue.getUsedSpaceFraction() > 0.9) {
                    log.warn("Remote log publishing failure : The number of logs in the queue has " +
                            "exceeded 90% of the allocated queue limit. Please check the remote server status.");
                    this.failureWarningLevel = FailureWaringLevel.OVER_90_PERCENT;
                }
                break;
            case OVER_90_PERCENT:
                if (persistentQueue.isFull()) {
                    log.warn("Remote log publishing failure : The number of logs in the queue has " +
                            "exceeded the allocated queue limit. Please check the remote server status.");
                    this.failureWarningLevel = FailureWaringLevel.FULLY_USED;
                }
                break;
            case FULLY_USED:
                log.warn("Remote log publishing failure : Allocated queue limit reached. Starting to " +
                        "loose logs. Please check the remote server status.");
                this.failureWarningLevel = FailureWaringLevel.LOOSING_LOGS;
                this.lastLogLossWarningTime = new Date().getTime();
                break;
            default:
                long timeSinceLastWarning = new Date().getTime() - lastLogLossWarningTime;
                if (timeSinceLastWarning > TimeUnit.MINUTES.toMillis(FAILURE_WARNING_DELAY_MINUTES)) {
                    log.warn("Remote log publishing failure : Allocated queue limit reached. Unsaved" +
                            " logs are being dumped. Please check the remote server status.");
                    this.lastLogLossWarningTime = new Date().getTime();
                }
                break;
        }
    }

    private FailureWaringLevel getCurrentWarningLevel() {

        if (persistentQueue.getUsedSpaceFraction() < 0.9) { // next half warning
            return FailureWaringLevel.INITIAL;
        } else if (!persistentQueue.isFull()) { // next check 90% warning
            return FailureWaringLevel.HALF_QUEUE_SIZE;
        }
        // on next check fully used warning issued
        return FailureWaringLevel.FULLY_USED;
    }

    private void resetWarningLogOnRemoteServerSuccess() {

        if (this.failureWarningLevel != FailureWaringLevel.NONE) {
            log.info("Remote server reconnected. Logs are being published to the remote server.");
            this.failureWarningLevel = FailureWaringLevel.NONE;
        }
    }

    private void validateBatchSize(int maxBatchSizeInBytes, int maxDiskSpaceInBytes) {

        if (maxBatchSizeInBytes < AppenderConstants.MINIMUM_BATCH_SIZE_IN_BYTES) {
            throw new IllegalArgumentException(String.format("The batch size cannot be less than %d bytes."
                    , AppenderConstants.MINIMUM_BATCH_SIZE_IN_BYTES));
        }
        if (maxBatchSizeInBytes >= maxDiskSpaceInBytes) {
            throw new IllegalArgumentException("The batch size cannot be less than or " +
                    "equal to the maximum disk space.");
        }
    }

    private void validateDiskSpace(int maxDiskSpaceInBytes) {

        if (maxDiskSpaceInBytes < AppenderConstants.MINIMUM_DISK_SPACE_IN_BYTES) {
            throw new IllegalArgumentException(String.format("The maximum disk space cannot be less than %d bytes."
                    , AppenderConstants.MINIMUM_DISK_SPACE_IN_BYTES));
        }
    }

    private final class LogPublisherTask implements Runnable {

        @Override
        public void run() {

            if (isManagerInitialized && !persistentQueue.isEmpty()) {
                // publish logs from the queue
                try {
                    LogEvent event = persistentQueue.peek();
                    if (event != null) {
                        manager.send(getLayout(), event);
                        persistentQueue.dequeue();
                        resetWarningLogOnRemoteServerSuccess();
                    }
                } catch (Exception e) {
                    printWarningLogOnRemoteServerFailure();
                }
            }
        }
    }
}
