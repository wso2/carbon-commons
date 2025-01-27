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

package org.wso2.carbon.logging.service.dao;

import org.apache.commons.configuration.ConfigurationException;
import org.wso2.carbon.logging.service.LoggingConstants.LogType;
import org.wso2.carbon.logging.service.data.RemoteServerLoggerData;

import java.util.Optional;

/**
 * This is the interface used for managing the remote server logging configurations in the storage.
 */
public interface RemoteLoggingConfigDAO {

    /**
     * This method is used to add a remote server configuration from the storage.
     * @param data                      RemoteServerLoggerData object that contains the remote server configuration.
     * @param logType                   The log type of the remote server configuration.
     * @throws ConfigurationException   If an error occurs while loading the remote server configuration.
     */
    void saveRemoteServerConfigInRegistry(RemoteServerLoggerData data, LogType logType) throws ConfigurationException;

    /**
     * This method is used to get the remote server configuration from the storage.
     * @param logType                   The log type of the remote server configuration.
     * @return                          The remote server configuration.
     * @throws ConfigurationException   If an error occurs while loading the remote server configuration.
     */
    Optional<RemoteServerLoggerData> getRemoteServerConfig(LogType logType) throws ConfigurationException;

    /**
     * This method is used to reset the remote server configurations to the defaults in the storage.
     *
     * @param logType                   The log type of the remote server configuration.
     * @throws ConfigurationException   If an error occurs while loading the remote server configuration.
     */
    void resetRemoteServerConfigInRegistry(LogType logType) throws ConfigurationException;
}
