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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.exception.*;
import org.wso2.carbon.databridge.agent.internal.conf.DataEndpointConfiguration;
import org.wso2.carbon.databridge.agent.internal.endpoint.DataEndpoint;
import org.wso2.carbon.databridge.agent.internal.endpoint.DataEndpointFactory;
import org.wso2.carbon.databridge.agent.internal.endpoint.DataEndpointGroup;
import org.wso2.carbon.databridge.agent.util.HADataPublisherUtil;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.TransportException;

import java.util.ArrayList;
import java.util.Map;


public class HADataPublisher {
    private static final Log log = LogFactory.getLog(HADataPublisher.class);

    private ArrayList<DataEndpointGroup> endpointGroups
            = new ArrayList<DataEndpointGroup>();
    private DataEndpointAgent dataEndpointAgent;

    public HADataPublisher(String receiverURLSet, String authURLSet, String username, String password)
            throws DataEndpointAgentConfigurationException,
            DataEndpointException, DataEndpointConfigurationException,
            DataEndpointAuthenticationException, TransportException {
        dataEndpointAgent = AgentHolder.getInstance().getDefaultDataEndpointAgent();
        processEndpoints(dataEndpointAgent.getDataEndpointAgentConfiguration().
                getDataEndpointName(), dataEndpointAgent, receiverURLSet, authURLSet, username, password);
        dataEndpointAgent.addHADataPublisher(this);
    }

    public HADataPublisher(String receiverURLSet, String username, String password)
            throws DataEndpointAgentConfigurationException,
            DataEndpointException, DataEndpointConfigurationException,
            DataEndpointAuthenticationException, TransportException {
        dataEndpointAgent = AgentHolder.getInstance().getDefaultDataEndpointAgent();
        processEndpoints(dataEndpointAgent.getDataEndpointAgentConfiguration().
                getDataEndpointName(), dataEndpointAgent, receiverURLSet, HADataPublisherUtil.
                getDefaultAuthURLSet(receiverURLSet), username, password);
        dataEndpointAgent.addHADataPublisher(this);
    }

    public HADataPublisher(String type, String receiverURLSet, String authURLSet, String username, String password)
            throws DataEndpointAgentConfigurationException,
            DataEndpointException, DataEndpointConfigurationException,
            DataEndpointAuthenticationException, TransportException {
        dataEndpointAgent = AgentHolder.getInstance().getDataEndpointAgent(type);
        processEndpoints(dataEndpointAgent.getDataEndpointAgentConfiguration().
                getDataEndpointName(), dataEndpointAgent, receiverURLSet, authURLSet, username, password);
        dataEndpointAgent.addHADataPublisher(this);
    }


    private void processEndpoints(String type, DataEndpointAgent dataEndpointAgent,
                                  String receiverURLSet, String authURLSet, String username, String password)
            throws DataEndpointConfigurationException, DataEndpointAgentConfigurationException,
            DataEndpointException, DataEndpointAuthenticationException, TransportException {
        ArrayList receiverURLGroups = HADataPublisherUtil.getEndpointGroups(receiverURLSet);
        ArrayList authURLGroups = HADataPublisherUtil.getEndpointGroups(authURLSet);
        HADataPublisherUtil.validateURLs(receiverURLGroups, authURLGroups);

        for (int i = 0; i < receiverURLGroups.size(); i++) {
            Object[] receiverGroup = (Object[]) receiverURLGroups.get(i);
            Object[] authGroup = (Object[]) authURLGroups.get(i);
            boolean failOver = (Boolean) receiverGroup[0];

            DataEndpointGroup endpointGroup;
            if (failOver) endpointGroup = new DataEndpointGroup(DataEndpointGroup.HAType.FAILOVER, dataEndpointAgent);
            else endpointGroup = new DataEndpointGroup(DataEndpointGroup.HAType.LOADBALANCE,
                    dataEndpointAgent);
            /**
             * Since the first element holds the failover/LB settings
             * we need to start iterating from 2nd element
             */
            for (int j = 1; j < receiverGroup.length; j++) {
                DataEndpointConfiguration endpointConfiguration = new DataEndpointConfiguration((String) receiverGroup[j],
                        (String) authGroup[j], username, password, dataEndpointAgent.getTransportPool(),
                        dataEndpointAgent.getSecuredTransportPool(), dataEndpointAgent.
                        getDataEndpointAgentConfiguration().getBatchSize());
                DataEndpoint dataEndpoint = DataEndpointFactory.getInstance().getNewDataEndpoint(type);
                dataEndpoint.initialize(endpointConfiguration);
                endpointGroup.addDataEndpoint(dataEndpoint);
            }
            endpointGroups.add(endpointGroup);
        }
    }

    public void publish(Event event) {
        for (DataEndpointGroup endpointGroup : endpointGroups) {
            endpointGroup.publish(event);
        }
    }

    public void publish(String streamId, Object[] metaDataArray, Object[] correlationDataArray,
                        Object[] payloadDataArray) {
        publish(new Event(streamId, System.currentTimeMillis(), metaDataArray,
                correlationDataArray, payloadDataArray));
    }

    public void publish(String streamId, Object[] metaDataArray, Object[] correlationDataArray,
                        Object[] payloadDataArray, Map<String, String> arbitraryDataMap) {
        publish(new Event(streamId, System.currentTimeMillis(), metaDataArray,
                correlationDataArray, payloadDataArray, arbitraryDataMap));
    }

    public void publish(String streamId, long timeStamp, Object[] metaDataArray,
                        Object[] correlationDataArray, Object[] payloadDataArray) {
        publish(new Event(streamId, timeStamp, metaDataArray, correlationDataArray, payloadDataArray));
    }

    public void publish(String streamId, long timeStamp, Object[] metaDataArray,
                        Object[] correlationDataArray, Object[] payloadDataArray, Map<String, String> arbitraryDataMap) {
        publish(new Event(streamId, timeStamp, metaDataArray, correlationDataArray, payloadDataArray, arbitraryDataMap));
    }

    public boolean tryPublish(Event event) {
        boolean sent = true;
        for (DataEndpointGroup endpointGroup : endpointGroups) {
            try {
                endpointGroup.tryPublish(event);
                sent = true;
            } catch (EventQueueFullException e) {
                log.error("Unable to process the event for endpoint group "
                        + endpointGroup.toString() + ", dropping the event. ", e);
                if (log.isDebugEnabled()) log.debug("Dropped Event: " + event.toString() + " for the endpoint group " +
                        endpointGroup.toString());
                sent = false;
            }
        }
        return sent;
    }

    public boolean tryPublish(String streamId, Object[] metaDataArray, Object[] correlationDataArray,
                              Object[] payloadDataArray) {
        return tryPublish(new Event(streamId, System.currentTimeMillis(), metaDataArray,
                correlationDataArray, payloadDataArray));
    }

    public boolean tryPublish(String streamId, Object[] metaDataArray, Object[] correlationDataArray,
                              Object[] payloadDataArray, Map<String, String> arbitraryDataMap) {
        return tryPublish(new Event(streamId, System.currentTimeMillis(), metaDataArray,
                correlationDataArray, payloadDataArray, arbitraryDataMap));
    }

    public boolean tryPublish(String streamId, long timeStamp, Object[] metaDataArray,
                              Object[] correlationDataArray, Object[] payloadDataArray) {
        return tryPublish(new Event(streamId, timeStamp, metaDataArray, correlationDataArray, payloadDataArray));
    }

    public boolean tryPublish(String streamId, long timeStamp, Object[] metaDataArray,
                              Object[] correlationDataArray, Object[] payloadDataArray, Map<String, String> arbitraryDataMap) {
        return tryPublish(new Event(streamId, timeStamp, metaDataArray, correlationDataArray, payloadDataArray, arbitraryDataMap));
    }

    public void shutdown() throws DataEndpointException {
        for (DataEndpointGroup dataEndpointGroup : endpointGroups) {
            dataEndpointGroup.shutdown();
        }
        dataEndpointAgent.shutDown(this);
    }
}


