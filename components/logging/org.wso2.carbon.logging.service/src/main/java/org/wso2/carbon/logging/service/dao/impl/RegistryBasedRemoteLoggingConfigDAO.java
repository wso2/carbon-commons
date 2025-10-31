/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.wso2.carbon.logging.service.dao.impl;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.logging.service.LoggingConstants;
import org.wso2.carbon.logging.service.LoggingConstants.LogType;
import org.wso2.carbon.logging.service.RemoteLoggingServerException;
import org.wso2.carbon.logging.service.dao.RemoteLoggingConfigDAO;
import org.wso2.carbon.logging.service.data.RemoteServerLoggerData;
import org.wso2.carbon.logging.service.internal.RemoteLoggingConfigDataHolder;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * This class is used to update the remote server logging configurations in the registry.
 */
public class RegistryBasedRemoteLoggingConfigDAO implements RemoteLoggingConfigDAO {

    @Override
    public void saveRemoteServerConfig(RemoteServerLoggerData data, LogType type)
            throws RemoteLoggingServerException {

        try {
            Registry registry =
                    RemoteLoggingConfigDataHolder.getInstance().getRegistryService().getConfigSystemRegistry();
            try {
                boolean transactionStarted = Transaction.isStarted();
                if (!transactionStarted) {
                    registry.beginTransaction();
                }

                Resource resource = getResourceFromRemoteServerLoggerData(data, registry, type);
                registry.put(LoggingConstants.REMOTE_SERVER_LOGGER_RESOURCE_PATH + "/" + type, resource);

                if (!transactionStarted) {
                    registry.commitTransaction();
                }
            } catch (Exception e) {
                registry.rollbackTransaction();
                throw new RemoteLoggingServerException(e.getMessage(), e);
            }
        } catch (RegistryException e) {
            throw new RemoteLoggingServerException("Error while updating the remote server logging configurations");
        }
    }

    @Override
    public Optional<RemoteServerLoggerData> getRemoteServerConfig(LogType type) throws RemoteLoggingServerException {

        try {
            String resourcePath = LoggingConstants.REMOTE_SERVER_LOGGER_RESOURCE_PATH + "/" + type;
            if (!RemoteLoggingConfigDataHolder.getInstance().getRegistryService().getConfigSystemRegistry()
                    .resourceExists(resourcePath)) {
                return Optional.empty();
            }
            Resource resource =
                    RemoteLoggingConfigDataHolder.getInstance().getRegistryService().getConfigSystemRegistry()
                            .get(resourcePath);
            return Optional.of(getRemoteServerLoggerDataFromResource(resource));
        } catch (RegistryException e) {
            throw new RemoteLoggingServerException(e.getMessage(), e);
        }
    }

    @Override
    public void resetRemoteServerConfig(LogType type) throws RemoteLoggingServerException {

        try {
            String resourcePath = LoggingConstants.REMOTE_SERVER_LOGGER_RESOURCE_PATH + "/" + type;
            if (!RemoteLoggingConfigDataHolder.getInstance().getRegistryService().getConfigSystemRegistry()
                    .resourceExists(resourcePath)) {
                return;
            }
            Registry registry =
                    RemoteLoggingConfigDataHolder.getInstance().getRegistryService().getConfigSystemRegistry();

            try {
                boolean transactionStarted = Transaction.isStarted();
                if (!transactionStarted) {
                    registry.beginTransaction();
                }

                registry.delete(resourcePath);

                if (!transactionStarted) {
                    registry.commitTransaction();
                }
            } catch (Exception e) {
                registry.rollbackTransaction();
                throw new RemoteLoggingServerException(e.getMessage(), e);
            }
        } catch (RegistryException e) {
            throw new RemoteLoggingServerException("Error while resetting the remote server logging configurations");
        }
    }

    private Resource getResourceFromRemoteServerLoggerData(RemoteServerLoggerData data, Registry registry,
                                                           LogType logType)
            throws RegistryException, ConfigurationException {

        Resource resource = registry.newResource();
        resource.addProperty(LoggingConstants.URL, data.getUrl());
        resource.addProperty(LoggingConstants.USERNAME, data.getUsername());
        resource.addProperty(LoggingConstants.PASSWORD,
                getEncryptedSecret(data.getPassword(), LoggingConstants.PASSWORD));
        resource.addProperty(LoggingConstants.KEYSTORE_LOCATION, data.getKeystoreLocation());
        resource.addProperty(LoggingConstants.KEYSTORE_PASSWORD,
                getEncryptedSecret(data.getKeystorePassword(), LoggingConstants.KEYSTORE_PASSWORD));
        resource.addProperty(LoggingConstants.TRUSTSTORE_LOCATION, data.getTruststoreLocation());
        resource.addProperty(LoggingConstants.TRUSTSTORE_PASSWORD,
                getEncryptedSecret(data.getTruststorePassword(), LoggingConstants.TRUSTSTORE_PASSWORD));
        resource.addProperty(LoggingConstants.VERIFY_HOSTNAME, String.valueOf(data.isVerifyHostname()));
        resource.addProperty(LoggingConstants.LOG_TYPE, logType.toString());
        resource.addProperty(LoggingConstants.CONNECT_TIMEOUT_MILLIS, data.getConnectTimeoutMillis());
        resource.addProperty(LoggingConstants.CONNECTION_TIMEOUT, data.getConnectTimeoutMillis());
        return resource;
    }

    private String getEncryptedSecret(String secretValue, String name) throws  ConfigurationException {

        if (StringUtils.isBlank(secretValue)) {
            return StringUtils.EMPTY;
        }
        if (!Boolean.parseBoolean(RemoteLoggingConfigDataHolder.getInstance().getServerConfigurationService()
                .getFirstProperty(LoggingConstants.REMOTE_LOGGING_ENABLE_ENCRYPTION))) {
            return secretValue;
        }
        try {
            return CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(
                    secretValue.getBytes(StandardCharsets.UTF_8));
        } catch (CryptoException e) {
            throw new ConfigurationException("Error while adding the secret : " + name, e);
        }
    }

    private String getDecryptedSecret(Resource resource, String name) {

        String secretValue = resource.getProperty(name);
        if (StringUtils.isBlank(secretValue)) {
            return StringUtils.EMPTY;
        }
        if (!Boolean.parseBoolean(RemoteLoggingConfigDataHolder.getInstance().getServerConfigurationService()
                .getFirstProperty(LoggingConstants.REMOTE_LOGGING_ENABLE_ENCRYPTION))) {
            return secretValue;
        }
        try {
            return new String(CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(
                    secretValue), StandardCharsets.UTF_8);
        } catch (CryptoException e) {
            return secretValue;
        }
    }

    private RemoteServerLoggerData getRemoteServerLoggerDataFromResource(Resource resource) {

        RemoteServerLoggerData data = new RemoteServerLoggerData();
        data.setUrl(resource.getProperty(LoggingConstants.URL));
        data.setConnectTimeoutMillis(resource.getProperty(LoggingConstants.CONNECTION_TIMEOUT));
        data.setUsername(resource.getProperty(LoggingConstants.USERNAME));
        data.setPassword(getDecryptedSecret(resource, LoggingConstants.PASSWORD));
        data.setKeystoreLocation(resource.getProperty(LoggingConstants.KEYSTORE_LOCATION));
        data.setKeystorePassword(getDecryptedSecret(resource, LoggingConstants.KEYSTORE_PASSWORD));
        data.setTruststoreLocation(resource.getProperty(LoggingConstants.TRUSTSTORE_LOCATION));
        data.setTruststorePassword(getDecryptedSecret(resource, LoggingConstants.TRUSTSTORE_PASSWORD));
        data.setVerifyHostname(Boolean.parseBoolean(resource.getProperty(LoggingConstants.VERIFY_HOSTNAME)));
        data.setLogType(resource.getProperty(LoggingConstants.LOG_TYPE));
        return data;
    }
}
