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
package org.wso2.carbon.databridge.agent.internal.client;

import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentSecurityException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.agent.util.HADataPublisherUtil;

public abstract class AbstractClientPoolFactory extends BaseKeyedPoolableObjectFactory {
    @Override
    public Object makeObject(Object key)
            throws DataEndpointException, DataEndpointAgentSecurityException {
       Object[] urlParams = HADataPublisherUtil.getProtocolHostPort(key.toString());
        return createClient(urlParams[0].toString(),urlParams[1].toString(), Integer.parseInt(urlParams[2].toString()));
    }

    public abstract Object createClient(String protocol, String hostName, int port) throws DataEndpointException, DataEndpointAgentSecurityException;

    @Override
    public boolean validateObject(Object key, Object obj) {
        return validateClient(obj);
    }

    public abstract boolean validateClient(Object client);

    public void destroyObject(Object key, Object obj) {
        terminateClient(obj);
    }

    public abstract void terminateClient(Object client);

}
