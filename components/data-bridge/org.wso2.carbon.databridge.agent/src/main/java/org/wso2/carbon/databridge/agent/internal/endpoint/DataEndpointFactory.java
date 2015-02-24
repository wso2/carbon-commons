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
package org.wso2.carbon.databridge.agent.internal.endpoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataEndpointAgent;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;

public class DataEndpointFactory {

    private static final Log log = LogFactory.getLog(DataEndpointFactory.class);

    private static DataEndpointFactory instance;

    private DataEndpointFactory() throws DataEndpointAgentConfigurationException {
    }

    public static DataEndpointFactory getInstance()
            throws DataEndpointAgentConfigurationException {
        if (instance == null) {
            synchronized (DataEndpointFactory.class) {
                if (instance == null) instance = new DataEndpointFactory();
            }
        }
        return instance;
    }

    public DataEndpoint getNewDataEndpoint(String endPointType)
            throws DataEndpointAgentConfigurationException, DataEndpointException {
        try {
            DataEndpointAgent agent = AgentHolder.getInstance().getDataEndpointAgent(endPointType);
            DataEndpoint dataEndpoint = (DataEndpoint) (DataEndpointFactory.class.getClassLoader().
                    loadClass(agent.getAgentConfiguration().getClassName()).newInstance());
            return dataEndpoint;
        } catch (InstantiationException e) {
            log.error("Error while instantiating the endpoint class for endpoint name " + endPointType, e);
            throw new DataEndpointException("Error while instantiating the endpoint class for endpoint name " +
                    endPointType + ". " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            log.error("Error while instantiating the endpoint class for endpoint name " + endPointType, e);
            throw new DataEndpointException("Error while instantiating the endpoint class for endpoint name " +
                    endPointType + ". " + e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            log.error("Class defined: " + AgentHolder.getInstance().
                    getDataEndpointAgent(endPointType).getAgentConfiguration().getClassName() +
                    " cannot be found for endpoint name " + endPointType, e);
            throw new DataEndpointException("Class defined: " + AgentHolder.getInstance().
                    getDataEndpointAgent(endPointType).getAgentConfiguration().getClassName() +
                    " cannot be found for endpoint name " + endPointType + ". " + e.getMessage(), e);
        }
    }

    public DataEndpoint getNewDefaultDataEndpoint()
            throws DataEndpointAgentConfigurationException, DataEndpointException {
        String defaultDataEndpointType = AgentHolder.getInstance().getDefaultDataEndpointAgent().
                getAgentConfiguration().getDataEndpointName();
        return getNewDataEndpoint(defaultDataEndpointType);
    }

}