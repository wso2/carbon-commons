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

package org.wso2.carbon.databridge.agent;

import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.agent.internal.client.AbstractClientPoolFactory;
import org.wso2.carbon.databridge.agent.internal.client.AbstractSecureClientPoolFactory;
import org.wso2.carbon.databridge.agent.internal.client.ClientPool;
import org.wso2.carbon.databridge.agent.internal.client.SecureClientPool;
import org.wso2.carbon.databridge.agent.internal.conf.DataEndpointAgentConfiguration;
import org.wso2.carbon.databridge.agent.util.DataEndpointConstants;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class DataEndpointAgent {

    private ArrayList<DataPublisher> dataPublishers
            = new ArrayList<DataPublisher>();

    private GenericKeyedObjectPool transportPool;
    private GenericKeyedObjectPool securedTransportPool;

    private DataEndpointAgentConfiguration dataEndpointAgentConfiguration;

    public DataEndpointAgent(DataEndpointAgentConfiguration dataEndpointAgentConfiguration)
            throws DataEndpointAgentConfigurationException {
        this.dataEndpointAgentConfiguration = dataEndpointAgentConfiguration;
        initialize();
    }

    private void initialize() throws DataEndpointAgentConfigurationException {
        try {
            AbstractClientPoolFactory clientPoolFactory = (AbstractClientPoolFactory) (DataEndpointAgent.class.getClassLoader().
                    loadClass(dataEndpointAgentConfiguration.getClientPoolFactoryClass()).newInstance());
            AbstractSecureClientPoolFactory secureClientPoolFactory = (AbstractSecureClientPoolFactory) (DataEndpointAgent.class.getClassLoader().
                    loadClass(dataEndpointAgentConfiguration.getSecureClientPoolFactoryClass()).getConstructor(String.class, String.class).newInstance(
                    dataEndpointAgentConfiguration.getTrustStore(),
                    dataEndpointAgentConfiguration.getTrustStorePassword()));
            this.transportPool = new ClientPool().getClientPool(
                    clientPoolFactory,
                    Integer.parseInt(dataEndpointAgentConfiguration.
                            get(DataEndpointConstants.DATA_AGENT_MAX_TRANSPORT_POOL_SIZE)),
                    Integer.parseInt(dataEndpointAgentConfiguration.
                            get(DataEndpointConstants.DATA_AGENT_MAX_IDLE_CONNECTIONS)),
                    true,
                    Integer.parseInt(dataEndpointAgentConfiguration.
                            get(DataEndpointConstants.DATA_AGENT_EVICTION_TIME_PERIOD)),
                    Integer.parseInt(dataEndpointAgentConfiguration.
                            get(DataEndpointConstants.DATA_AGENT_MIN_IDLE_TIME_IN_POOL)));

            this.securedTransportPool = new SecureClientPool().getClientPool(
                    secureClientPoolFactory,
                    Integer.parseInt(dataEndpointAgentConfiguration.
                            get(DataEndpointConstants.DATA_AGENT_SECURE_MAX_TRANSPORT_POOL_SIZE)),
                    Integer.parseInt(dataEndpointAgentConfiguration.
                            get(DataEndpointConstants.DATA_AGENT_SECURE_MAX_IDLE_CONNECTIONS)),
                    true,
                    Integer.parseInt(dataEndpointAgentConfiguration.
                            get(DataEndpointConstants.DATA_AGENT_SECURE_EVICTION_TIME_PERIOD)),
                    Integer.parseInt(dataEndpointAgentConfiguration.
                            get(DataEndpointConstants.DATA_AGENT_SECURE_MIN_IDLE_TIME_IN_POOL)));

        } catch (InstantiationException e) {
            throw new DataEndpointAgentConfigurationException("Error while creating the client pool " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new DataEndpointAgentConfigurationException("Error while creating the client pool " + e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new DataEndpointAgentConfigurationException("Error while creating the client pool " + e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            throw new DataEndpointAgentConfigurationException("Error while creating the client pool " + e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw new DataEndpointAgentConfigurationException("Error while creating the client pool " + e.getMessage(), e);
        }
    }

    public void addHADataPublisher(DataPublisher dataPublisher) {
        dataPublishers.add(dataPublisher);
    }

    public DataEndpointAgentConfiguration getDataEndpointAgentConfiguration() {
        return dataEndpointAgentConfiguration;
    }

    public GenericKeyedObjectPool getTransportPool() {
        return transportPool;
    }

    public GenericKeyedObjectPool getSecuredTransportPool() {
        return securedTransportPool;
    }

    public void shutDown(DataPublisher dataPublisher) throws DataEndpointException {
        dataPublishers.remove(dataPublisher);
        if(dataPublishers.isEmpty()){
            try {
                transportPool.close();
                securedTransportPool.close();
            } catch (Exception e) {
                throw new DataEndpointException("Error while closing the transport pool", e);
            }
        }
    }
}

