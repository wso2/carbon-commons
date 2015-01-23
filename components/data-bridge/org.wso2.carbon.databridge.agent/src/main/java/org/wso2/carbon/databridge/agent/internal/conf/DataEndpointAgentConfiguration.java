/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.databridge.agent.internal.conf;

import org.wso2.carbon.databridge.agent.DataEndpointAgent;
import org.wso2.carbon.databridge.agent.util.DataEndpointConstants;

import java.util.HashMap;

public class DataEndpointAgentConfiguration {
    private String dataEndpointName;
    private String className;
    private String trustStore;
    private String trustStorePassword;
    private String clientPoolFactoryClass;
    private String secureClientPoolFactoryClass;
    private int reconnectionInterval;
    private int queueSize;
    private int batchSize;

    private HashMap<String, String> config;

    public DataEndpointAgentConfiguration(String dataEndpointName, String className) {
        this.config = new HashMap<String, String>();
        this.dataEndpointName = dataEndpointName;
        this.className = className;
        this.queueSize = DataEndpointConstants.DEFAULT_DATA_AGENT_QUEUE_SIZE;
        this.batchSize = DataEndpointConstants.DEFAULT_DATA_AGENT_BATCH_SIZE;
        this.reconnectionInterval = DataEndpointConstants.DEFAULT_DATA_AGENT_RECONNECTION_INTERVAL;
        initialize();
    }

    private void initialize() {
        this.config.put(DataEndpointConstants.DATA_AGENT_MAX_TRANSPORT_POOL_SIZE,
                String.valueOf(DataEndpointConstants.DEFAULT_DATA_AGENT_MAX_TRANSPORT_POOL_SIZE));
        this.config.put(DataEndpointConstants.DATA_AGENT_MAX_IDLE_CONNECTIONS,
                String.valueOf(DataEndpointConstants.DEFAULT_DATA_AGENT_MAX_IDLE_CONNECTIONS));
        this.config.put(DataEndpointConstants.DATA_AGENT_MIN_IDLE_TIME_IN_POOL,
                String.valueOf(DataEndpointConstants.DEFAULT_DATA_AGENT_MIN_IDLE_TIME_IN_POOL));
        this.config.put(DataEndpointConstants.DATA_AGENT_EVICTION_TIME_PERIOD,
                String.valueOf(DataEndpointConstants.DEFAULT_DATA_AGENT_EVICTION_TIME_PERIOD));
        this.config.put(DataEndpointConstants.DATA_AGENT_SECURE_MAX_TRANSPORT_POOL_SIZE,
                String.valueOf(DataEndpointConstants.DEFAULT_DATA_AGENT_SECURE_MAX_TRANSPORT_POOL_SIZE));
        this.config.put(DataEndpointConstants.DATA_AGENT_SECURE_MAX_IDLE_CONNECTIONS,
                String.valueOf(DataEndpointConstants.DEFAULT_DATA_AGENT_SECURE_MAX_IDLE_CONNECTIONS));
        this.config.put(DataEndpointConstants.DATA_AGENT_SECURE_MIN_IDLE_TIME_IN_POOL,
                String.valueOf(DataEndpointConstants.DEFAULT_DATA_AGENT_SECURE_MIN_IDLE_TIME_IN_POOL));
        this.config.put(DataEndpointConstants.DATA_AGENT_SECURE_EVICTION_TIME_PERIOD,
                String.valueOf(DataEndpointConstants.DEFAULT_DATA_AGENT_SECURE_EVICTION_TIME_PERIOD));
    }

    public String getDataEndpointName() {
        return dataEndpointName;
    }

    public String getClassName() {
        return className;
    }

    public void add(String configName, String value) {
        config.put(configName, value);
    }

    public String get(String configName) {
        return config.get(configName);
    }

    public String getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public String getClientPoolFactoryClass() {
        return clientPoolFactoryClass;
    }


    public void setClientPoolFactoryClass(String clientPoolFactoryClass) {
        this.clientPoolFactoryClass = clientPoolFactoryClass;
    }

    public String getSecureClientPoolFactoryClass() {
        return secureClientPoolFactoryClass;
    }

    public void setSecureClientPoolFactoryClass(String secureClientPoolFactoryClass) {
        this.secureClientPoolFactoryClass = secureClientPoolFactoryClass;
    }

    public int getReconnectionInterval() {
        return reconnectionInterval;
    }

    public void setReconnectionInterval(int reconnectionInterval) {
        this.reconnectionInterval = reconnectionInterval;
    }
}

