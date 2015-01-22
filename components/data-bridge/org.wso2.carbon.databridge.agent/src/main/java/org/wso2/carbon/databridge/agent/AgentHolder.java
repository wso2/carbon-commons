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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.internal.conf.DataEndpointAgentConfiguration;
import org.wso2.carbon.databridge.agent.util.DataEndpointConstants;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;

public class AgentHolder {
    private static Log log = LogFactory.getLog(AgentHolder.class);

    private static String configPath;
    private static AgentHolder instance;

    private HashMap<String, DataEndpointAgent> dataEndpointAgents;

    private AgentHolder() {
        dataEndpointAgents = new HashMap<String, DataEndpointAgent>();
        try {
            loadConfiguration();
        } catch (DataEndpointAgentConfigurationException e) {
            log.error("Unable to complete initialization of agents." + e.getMessage(), e);
        }
    }

    public static AgentHolder getInstance() {
        if (instance == null) {
            instance = new AgentHolder();
        }
        return instance;
    }

    public void addDataEndpointAgent(DataEndpointAgent dataEndpointAgent) {
        dataEndpointAgents.put(dataEndpointAgent.getDataEndpointAgentConfiguration().getDataEndpointName(),
                dataEndpointAgent);
    }

    public synchronized DataEndpointAgent getDataEndpointAgent(String type) throws DataEndpointAgentConfigurationException {
        DataEndpointAgent agent = this.dataEndpointAgents.get(type);
        if (agent == null) {
            throw new DataEndpointAgentConfigurationException("No data agent configured for the type: " + type);
        }
        return agent;
    }

    private void loadConfiguration() throws DataEndpointAgentConfigurationException {
        BufferedInputStream inputStream = null;
        if (configPath == null) configPath = DataEndpointConstants.DATA_AGENT_CONF_FILE_PATH;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(new File(configPath)));
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            OMElement omElement = builder.getDocumentElement();
            omElement.build();

            Iterator agentIterator = omElement.getChildElements();
            while (agentIterator.hasNext()) {
                OMElement endPointConf = (OMElement) agentIterator.next();
                Iterator endPointIterator = endPointConf.getChildElements();
                addAgentConfiguration(endPointIterator);
            }
        } catch (FileNotFoundException e) {
            String errorMessage = DataEndpointConstants.DATA_AGENT_CONF_FILE_NAME
                    + "cannot be found in the path : " + DataEndpointConstants.DATA_AGENT_CONF_FILE_PATH;
            log.error(errorMessage, e);
            throw new DataEndpointAgentConfigurationException(errorMessage, e);
        } catch (XMLStreamException e) {
            String errorMessage = "Invalid XML for " + DataEndpointConstants.DATA_AGENT_CONF_FILE_NAME
                    + " located in the path : " + DataEndpointConstants.DATA_AGENT_CONF_FILE_PATH;
            log.error(errorMessage, e);
            throw new DataEndpointAgentConfigurationException(errorMessage, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                String errorMessage = "Can not close the input stream";
                log.error(errorMessage, e);
            }
        }
    }


    private void addAgentConfiguration(Iterator agentConfIterator) throws DataEndpointAgentConfigurationException {
        String name = null, endpointClass = null, trustStore = null, trustStorePw = null, clientPoolFactoryClass = null,
                secureClientPoolFactoryClass = null;
        int queueSize = 0, batchSize = 0, reconnectionInterval = 0;
        while (agentConfIterator.hasNext()) {
            OMElement element = (OMElement) agentConfIterator.next();
            if (element.getQName().getLocalPart().
                    equalsIgnoreCase(DataEndpointConstants.DATA_AGENT_ENDPOINT_NAME)) {
                name = element.getText().trim();
            } else if (element.getQName().getLocalPart().
                    equalsIgnoreCase(DataEndpointConstants.DATA_AGENT_ENDPOINT_CLASS)) {
                endpointClass = element.getText().trim();
            } else if (element.getQName().getLocalPart().equalsIgnoreCase(DataEndpointConstants.DATA_AGENT_TRUST_STORE_LOCATION)) {
                trustStore = element.getText().trim();
            } else if (element.getQName().getLocalPart().equalsIgnoreCase(DataEndpointConstants.DATA_AGENT_TRUST_STORE_PASSWORD)) {
                trustStorePw = element.getText().trim();
            } else if (element.getQName().getLocalPart().equalsIgnoreCase(DataEndpointConstants.DATA_AGENT_QUEUE_SIZE)) {
                queueSize = Integer.parseInt(element.getText().trim());
            } else if (element.getQName().getLocalPart().equalsIgnoreCase(DataEndpointConstants.DATA_AGENT_BATCH_SIZE)) {
                batchSize = Integer.parseInt(element.getText().trim());
            } else if (element.getQName().getLocalPart().equalsIgnoreCase(DataEndpointConstants.DATA_AGENT_CLIENT_POOL_FACTORY_CLASS)) {
                clientPoolFactoryClass = element.getText().trim();
            } else if (element.getQName().getLocalPart().equalsIgnoreCase(DataEndpointConstants.DATA_AGENT_SECURE_CLIENT_POOL_FACTORY_CLASS)) {
                secureClientPoolFactoryClass = element.getText().trim();
            } else if (element.getQName().getLocalPart().equalsIgnoreCase(DataEndpointConstants.DATA_AGENT_RECONNECTION_INTERVAL)) {
                reconnectionInterval = Integer.parseInt(element.getText().trim());
            }
        }
        if (name == null || name.isEmpty()) {
            throw new DataEndpointAgentConfigurationException("Endpoint name is not set in "
                    + DataEndpointConstants.DATA_AGENT_CONF_FILE_NAME);
        }
        if (endpointClass == null || endpointClass.isEmpty()) {
            throw new DataEndpointAgentConfigurationException("Endpoint class name is not set in "
                    + DataEndpointConstants.DATA_AGENT_CONF_FILE_NAME + " for name: " + name);
        }
        DataEndpointAgentConfiguration agentConfiguration = new DataEndpointAgentConfiguration(name, endpointClass);
        agentConfiguration.setTrustStore(trustStore);
        agentConfiguration.setTrustStorePassword(trustStorePw);
        if (queueSize != 0) agentConfiguration.setQueueSize(queueSize);
        if (batchSize != 0) agentConfiguration.setBatchSize(batchSize);
        agentConfiguration.setClientPoolFactoryClass(clientPoolFactoryClass);
        agentConfiguration.setSecureClientPoolFactoryClass(secureClientPoolFactoryClass);
        if (reconnectionInterval != 0) agentConfiguration.setReconnectionInterval(reconnectionInterval);
        DataEndpointAgent agent = new DataEndpointAgent(agentConfiguration);
        //TODO: load all config properties for data endpoint configuration
        addDataEndpointAgent(agent);
    }

    public DataEndpointAgent getDefaultDataEndpointAgent() throws DataEndpointAgentConfigurationException {
        Iterator iterator = dataEndpointAgents.keySet().iterator();
        if (iterator.hasNext()) {
            return dataEndpointAgents.get(iterator.next());
        } else {
            throw new DataEndpointAgentConfigurationException("No Data Endpoints configuration are available");
        }
    }

    public static void setConfigPath(String configPath) {
        AgentHolder.configPath = configPath;
    }
}
