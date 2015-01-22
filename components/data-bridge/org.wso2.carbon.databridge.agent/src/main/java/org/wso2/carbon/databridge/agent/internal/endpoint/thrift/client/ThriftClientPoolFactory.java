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
package org.wso2.carbon.databridge.agent.internal.endpoint.thrift.client;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.agent.internal.client.AbstractClientPoolFactory;
import org.wso2.carbon.databridge.agent.internal.conf.DataEndpointConfiguration;
import org.wso2.carbon.databridge.commons.thrift.service.general.ThriftEventTransmissionService;

public class ThriftClientPoolFactory extends AbstractClientPoolFactory {

    @Override
    public Object createClient(String protocol, String hostName, int port) throws DataEndpointException {
        if (protocol.equalsIgnoreCase(DataEndpointConfiguration.Protocol.TCP.toString())) {

            TTransport receiverTransport = null;
            receiverTransport = new TSocket(hostName, port);

            TProtocol tProtocol = new TBinaryProtocol(receiverTransport);
            ThriftEventTransmissionService.Client client = new ThriftEventTransmissionService.Client(tProtocol);
            try {
                receiverTransport.open();
            } catch (TTransportException e) {
                throw new DataEndpointException("Error while making the connection." + e.getMessage(), e);
            }

            return client;
        } else {
            THttpClient client = null;
            try {
                client = new THttpClient("http://" + hostName + ":" + port + "/thriftReceiver");
                TProtocol tProtocol = new TCompactProtocol(client);
                ThriftEventTransmissionService.Client publisherClient = new ThriftEventTransmissionService.Client(tProtocol);
                client.open();
                return publisherClient;
            } catch (TTransportException e) {
                throw new DataEndpointException("Error while making the connection." + e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean validateClient(Object client) {
        ThriftEventTransmissionService.Client thriftClient = (ThriftEventTransmissionService.Client) client;
        return thriftClient.getOutputProtocol().getTransport().isOpen();
    }

    @Override
    public void terminateClient(Object client) {
        ThriftEventTransmissionService.Client thriftClient = (ThriftEventTransmissionService.Client) client;
        thriftClient.getOutputProtocol().getTransport().close();
    }
}
