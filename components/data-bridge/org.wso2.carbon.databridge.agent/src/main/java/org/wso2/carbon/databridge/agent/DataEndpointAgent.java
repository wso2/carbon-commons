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
import org.wso2.carbon.databridge.agent.internal.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.internal.endpoint.DataEndpoint;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class DataEndpointAgent {

    private ArrayList<DataPublisher> dataPublishers
            = new ArrayList<DataPublisher>();

    private GenericKeyedObjectPool transportPool;
    private GenericKeyedObjectPool securedTransportPool;

    private AgentConfiguration agentConfiguration;

    public DataEndpointAgent(AgentConfiguration agentConfiguration)
            throws DataEndpointAgentConfigurationException {
            this.agentConfiguration = agentConfiguration;
            initialize();
    }

    private void initialize() throws DataEndpointAgentConfigurationException {
        try {
            DataEndpoint dataEndpoint = (DataEndpoint)(DataEndpointAgent.class.getClassLoader().
                    loadClass(agentConfiguration.getClassName()).newInstance());
            AbstractClientPoolFactory clientPoolFactory = (AbstractClientPoolFactory) (DataEndpointAgent.class.getClassLoader().
                    loadClass(dataEndpoint.getClientPoolFactoryClass()).newInstance());
            AbstractSecureClientPoolFactory secureClientPoolFactory = (AbstractSecureClientPoolFactory) (DataEndpointAgent.class.getClassLoader().
                    loadClass(dataEndpoint.getSecureClientPoolFactoryClass()).getConstructor(String.class, String.class).newInstance(
                    agentConfiguration.getTrustStore(),
                    agentConfiguration.getTrustStorePassword()));
            this.transportPool = new ClientPool().getClientPool(
                    clientPoolFactory,
                    agentConfiguration.getMaxTransportPoolSize(),
                    agentConfiguration.getMaxIdleConnections(),
                    true,
                    agentConfiguration.getEvictionTimePeriod(),
                    agentConfiguration.getMinIdleTimeInPool());

            this.securedTransportPool = new SecureClientPool().getClientPool(
                    secureClientPoolFactory,
                    agentConfiguration.getSecureMaxTransportPoolSize(),
                    agentConfiguration.getSecureMaxIdleConnections(),
                    true,
                    agentConfiguration.getSecureEvictionTimePeriod(),
                    agentConfiguration.getSecureMinIdleTimeInPool());

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

    public void addDataPublisher(DataPublisher dataPublisher) {
        dataPublishers.add(dataPublisher);
    }

    public AgentConfiguration getAgentConfiguration() {
        return agentConfiguration;
    }

    public GenericKeyedObjectPool getTransportPool() {
        return transportPool;
    }

    public GenericKeyedObjectPool getSecuredTransportPool() {
        return securedTransportPool;
    }

    public void shutDown(DataPublisher dataPublisher) {
        dataPublishers.remove(dataPublisher);
    }

    public void shutDown() throws DataEndpointException {
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

