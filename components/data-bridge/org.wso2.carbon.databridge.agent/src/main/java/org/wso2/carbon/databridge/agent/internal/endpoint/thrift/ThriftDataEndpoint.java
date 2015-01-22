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
package org.wso2.carbon.databridge.agent.internal.endpoint.thrift;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.agent.internal.conf.DataEndpointConfiguration;
import org.wso2.carbon.databridge.agent.internal.endpoint.DataEndpoint;
import org.wso2.carbon.databridge.agent.util.DataEndpointConstants;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.SessionTimeoutException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.commons.exception.UndefinedEventTypeException;
import org.wso2.carbon.databridge.commons.thrift.data.ThriftEventBundle;
import org.wso2.carbon.databridge.commons.thrift.exception.ThriftAuthenticationException;
import org.wso2.carbon.databridge.commons.thrift.exception.ThriftSessionExpiredException;
import org.wso2.carbon.databridge.commons.thrift.exception.ThriftUndefinedEventTypeException;
import org.wso2.carbon.databridge.commons.thrift.service.general.ThriftEventTransmissionService;
import org.wso2.carbon.databridge.commons.thrift.service.secure.ThriftSecureEventTransmissionService;

import java.util.ArrayList;

public class ThriftDataEndpoint extends DataEndpoint {

    @Override
    protected String connect(Object client, String userName, String password) throws DataEndpointAuthenticationException {
        try {
            return ((ThriftSecureEventTransmissionService.Client) client).connect(userName, password);
        } catch (ThriftAuthenticationException e) {
            throw new DataEndpointAuthenticationException("Thrift Authentication Exception", e);
        } catch (TException e) {
            throw new DataEndpointAuthenticationException("Thrift exception", e);
        }
    }

    @Override
    protected void disconnect(Object client, String sessionId) throws DataEndpointAuthenticationException {
        try {
            ((ThriftSecureEventTransmissionService.Client) client).disconnect(sessionId);
        } catch (TException e) {
            throw new DataEndpointAuthenticationException("Thrift Exception", e);
        }
    }

    @Override
    protected void initialize() throws DataEndpointException {
    }

    @Override
    protected void send(Object client, ArrayList<Event> events) throws DataEndpointException,
            SessionTimeoutException, UndefinedEventTypeException {
        ThriftEventBundle thriftEventBundle = null;
        for (Event event : events) {
            thriftEventBundle = ThriftEventConverter.toThriftEventBundle(event, thriftEventBundle,
                    getDataEndpointConfiguration().getSessionId());
        }
        try {
            if (client instanceof ThriftSecureEventTransmissionService.Client) {
                ((ThriftSecureEventTransmissionService.Client) client).publish(thriftEventBundle);
            } else {
                ((ThriftEventTransmissionService.Client) client).publish(thriftEventBundle);
            }
        } catch (ThriftUndefinedEventTypeException e) {
            throw new UndefinedEventTypeException("Thrift Undefined Event Type Exception ", e);
        } catch (ThriftSessionExpiredException e) {
            throw new SessionTimeoutException("Thrift Session Expired Exception ", e);
        } catch (TException e) {
            throw new DataEndpointException("Cannot send Events", e);
        }
    }

}
