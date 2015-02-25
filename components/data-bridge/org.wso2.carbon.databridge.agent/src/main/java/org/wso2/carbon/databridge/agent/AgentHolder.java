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
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.conf.DataAgentsConfiguration;
import org.wso2.carbon.databridge.agent.util.DataEndpointConstants;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class AgentHolder {
    private static Log log = LogFactory.getLog(AgentHolder.class);
    private static String configPath;
    private static AgentHolder instance;
    private Map<String, DataEndpointAgent> dataEndpointAgents;
    private String defaultDataEndpointAgentName;

    private AgentHolder() throws DataEndpointAgentConfigurationException {
        try {
            dataEndpointAgents = new HashMap<String, DataEndpointAgent>();
            DataAgentsConfiguration dataAgentsConfiguration = loadConfiguration();
            boolean isDefault = true;
            for (AgentConfiguration agentConfiguration : dataAgentsConfiguration.getAgentConfigurations()) {
                addAgentConfiguration(agentConfiguration, isDefault);
                if (isDefault) isDefault = false;
            }
        } catch (DataEndpointAgentConfigurationException e) {
            log.error("Unable to complete initialization of agents." + e.getMessage(), e);
            throw e;
        }
    }

    public static AgentHolder getInstance() throws DataEndpointAgentConfigurationException {
        if (instance == null) {
            instance = new AgentHolder();
        }
        return instance;
    }

    public synchronized DataEndpointAgent getDataEndpointAgent(String type)
            throws DataEndpointAgentConfigurationException {
        DataEndpointAgent agent = this.dataEndpointAgents.get(type);
        if (agent == null) {
            throw new DataEndpointAgentConfigurationException("No data agent configured for the type: " + type);
        }
        return agent;
    }

    private DataAgentsConfiguration loadConfiguration()
            throws DataEndpointAgentConfigurationException {
        if (configPath == null) configPath = CarbonUtils.getCarbonConfigDirPath()
                + DataEndpointConstants.DATA_AGENT_CONF_FILE_PATH;
        try {
            File file = new File(configPath);
            JAXBContext jaxbContext = JAXBContext.newInstance(DataAgentsConfiguration.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            DataAgentsConfiguration dataAgentsConfiguration = (DataAgentsConfiguration) jaxbUnmarshaller.unmarshal(file);
            dataAgentsConfiguration.validateConfigurations();
            return dataAgentsConfiguration;
        } catch (JAXBException e) {
            throw new DataEndpointAgentConfigurationException("Error while loading the configuration file "
                    + configPath, e);
        }
    }


    private void addAgentConfiguration(AgentConfiguration agentConfiguration, boolean defaultAgent)
            throws DataEndpointAgentConfigurationException {
        DataEndpointAgent agent = new DataEndpointAgent(agentConfiguration);
        dataEndpointAgents.put(agent.getAgentConfiguration().getDataEndpointName(), agent);
        if (defaultAgent) {
            defaultDataEndpointAgentName = agent.getAgentConfiguration().getDataEndpointName();
        }
    }

    public DataEndpointAgent getDefaultDataEndpointAgent() throws DataEndpointAgentConfigurationException {
        return getDataEndpointAgent(defaultDataEndpointAgentName);
    }

    public static void setConfigPath(String configPath) {
        AgentHolder.configPath = configPath;
    }
}
