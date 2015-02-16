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

import org.wso2.carbon.databridge.agent.util.DataEndpointConstants;


public class DataEndpointAgentConfiguration {
    private String dataEndpointName;
    private String className;
    private String trustStore;
    private String trustStorePassword;
    private int reconnectionInterval;
    private int queueSize;
    private int batchSize;
    private int maxTransportPoolSize;
    private int maxIdleConnections;
    private int minIdleTimeInPool;
    private int evictionTimePeriod;
    private int secureMaxTransportPoolSize;
    private int secureMaxIdleConnections;
    private int secureMinIdleTimeInPool;
    private int secureEvictionTimePeriod;

    public DataEndpointAgentConfiguration(String dataEndpointName, String className) {
        this.dataEndpointName = dataEndpointName;
        this.className = className;
        this.queueSize = DataEndpointConstants.DEFAULT_DATA_AGENT_QUEUE_SIZE;
        this.batchSize = DataEndpointConstants.DEFAULT_DATA_AGENT_BATCH_SIZE;
        this.reconnectionInterval = DataEndpointConstants.DEFAULT_DATA_AGENT_RECONNECTION_INTERVAL;
        this.maxTransportPoolSize = DataEndpointConstants.DEFAULT_DATA_AGENT_MAX_TRANSPORT_POOL_SIZE;
        this.maxIdleConnections = DataEndpointConstants.DEFAULT_DATA_AGENT_MAX_IDLE_CONNECTIONS;
        this.minIdleTimeInPool = DataEndpointConstants.DEFAULT_DATA_AGENT_MIN_IDLE_TIME_IN_POOL;
        this.evictionTimePeriod = DataEndpointConstants.DEFAULT_DATA_AGENT_EVICTION_TIME_PERIOD;
        this.secureMaxTransportPoolSize = DataEndpointConstants.DEFAULT_DATA_AGENT_SECURE_MAX_TRANSPORT_POOL_SIZE;
        this.secureMaxIdleConnections = DataEndpointConstants.DEFAULT_DATA_AGENT_SECURE_MAX_IDLE_CONNECTIONS;
        this.secureMinIdleTimeInPool = DataEndpointConstants.DEFAULT_DATA_AGENT_SECURE_MIN_IDLE_TIME_IN_POOL;
        this.secureEvictionTimePeriod = DataEndpointConstants.DEFAULT_DATA_AGENT_SECURE_EVICTION_TIME_PERIOD;
    }


    public String getDataEndpointName() {
        return dataEndpointName;
    }

    public String getClassName() {
        return className;
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

    public int getReconnectionInterval() {
        return reconnectionInterval;
    }

    public void setReconnectionInterval(int reconnectionInterval) {
        this.reconnectionInterval = reconnectionInterval;
    }

    public int getMaxTransportPoolSize() {
        return maxTransportPoolSize;
    }

    public void setMaxTransportPoolSize(int maxTransportPoolSize) {
        this.maxTransportPoolSize = maxTransportPoolSize;
    }

    public int getMaxIdleConnections() {
        return maxIdleConnections;
    }

    public void setMaxIdleConnections(int maxIdleConnections) {
        this.maxIdleConnections = maxIdleConnections;
    }

    public int getMinIdleTimeInPool() {
        return minIdleTimeInPool;
    }

    public void setMinIdleTimeInPool(int minIdleTimeInPool) {
        this.minIdleTimeInPool = minIdleTimeInPool;
    }

    public int getEvictionTimePeriod() {
        return evictionTimePeriod;
    }

    public void setEvictionTimePeriod(int evictionTimePeriod) {
        this.evictionTimePeriod = evictionTimePeriod;
    }

    public int getSecureMaxTransportPoolSize() {
        return secureMaxTransportPoolSize;
    }

    public void setSecureMaxTransportPoolSize(int secureMaxTransportPoolSize) {
        this.secureMaxTransportPoolSize = secureMaxTransportPoolSize;
    }

    public int getSecureMinIdleTimeInPool() {
        return secureMinIdleTimeInPool;
    }

    public void setSecureMinIdleTimeInPool(int secureMinIdleTimeInPool) {
        this.secureMinIdleTimeInPool = secureMinIdleTimeInPool;
    }

    public int getSecureMaxIdleConnections() {
        return secureMaxIdleConnections;
    }

    public void setSecureMaxIdleConnections(int secureMaxIdleConnections) {
        this.secureMaxIdleConnections = secureMaxIdleConnections;
    }

    public int getSecureEvictionTimePeriod() {
        return secureEvictionTimePeriod;
    }

    public void setSecureEvictionTimePeriod(int secureEvictionTimePeriod) {
        this.secureEvictionTimePeriod = secureEvictionTimePeriod;
    }
}

