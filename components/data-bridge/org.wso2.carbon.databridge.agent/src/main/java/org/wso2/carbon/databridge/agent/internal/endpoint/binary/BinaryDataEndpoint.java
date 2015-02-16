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
package org.wso2.carbon.databridge.agent.internal.endpoint.binary;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.agent.internal.endpoint.DataEndpoint;
import org.wso2.carbon.databridge.agent.internal.endpoint.thrift.ThriftEventConverter;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.SessionTimeoutException;
import org.wso2.carbon.databridge.commons.exception.UndefinedEventTypeException;
import org.wso2.carbon.databridge.commons.thrift.data.ThriftEventBundle;
import org.wso2.carbon.databridge.commons.thrift.exception.ThriftAuthenticationException;
import org.wso2.carbon.databridge.commons.thrift.exception.ThriftSessionExpiredException;
import org.wso2.carbon.databridge.commons.thrift.exception.ThriftUndefinedEventTypeException;
import org.wso2.carbon.databridge.commons.thrift.service.general.ThriftEventTransmissionService;
import org.wso2.carbon.databridge.commons.thrift.service.secure.ThriftSecureEventTransmissionService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.UUID;

public class BinaryDataEndpoint extends DataEndpoint {
    private static Log log = LogFactory.getLog(BinaryDataEndpoint.class);
    @Override
    protected String connect(Object client, String userName, String password) throws DataEndpointAuthenticationException {
        //TODO: need to handle authentication
        Socket socket = (Socket) client;
        if (!socket.isConnected()) {
            String hostAddress = socket.getInetAddress().getHostAddress();
            int port = socket.getPort();
            try {
                socket.close();
            } catch (IOException e) {
                //ignored
            }
            try {
                socket = new Socket(hostAddress, port);

            } catch (IOException e) {
                throw new DataEndpointAuthenticationException("Error while connecting to TCP endpoint with hostname :" +
                        hostAddress + ",  port:" + port);
            }
        }
        return UUID.randomUUID().toString();
    }

    @Override
    protected void disconnect(Object client, String sessionId) throws DataEndpointAuthenticationException {
        Socket socket = null;
        try {
            socket = (Socket) client;
            socket.close();
        } catch (IOException e) {
            log.warn("Cannot close the socket successfully from " + socket.getLocalAddress().getHostAddress()
                    + ":" + socket.getPort());
        }
    }

    @Override
    protected void initialize() throws DataEndpointException {
    }

    @Override
    protected void send(Object client, ArrayList<Event> events) throws DataEndpointException,
            SessionTimeoutException, UndefinedEventTypeException {
        Socket socket = (Socket) client;
        try {
            OutputStream outputStream = socket.getOutputStream();

        } catch (IOException e) {
            throw new DataEndpointException("Unable to send te events to the data endpoint. "+e.getMessage(), e);
        }
    }

    @Override
    public String getClientPoolFactoryClass() {
        return null;
    }

    @Override
    public String getSecureClientPoolFactoryClass() {
        return null;
    }

//    public void sendEvent(String streamId, Object[] event, boolean flush) throws IOException {
//        StreamRuntimeInfo streamRuntimeInfo = streamRuntimeInfoMap.get(streamId);
//
//        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
//
//        byte streamIdSize = streamRuntimeInfo.getStreamIdSize();
//        ByteBuffer buf = ByteBuffer.allocate(streamRuntimeInfo.getFixedMessageSize() + streamIdSize + 1);
//        buf.put(streamIdSize);
//        buf.put((streamRuntimeInfo.getStreamId()).getBytes(DEFAULT_CHARSET));
//
//        int[] stringDataIndex = new int[streamRuntimeInfo.getNoOfStringAttributes()];
//        int stringIndex = 0;
//        int stringSize = 0;
//        Attribute.Type[] types = streamRuntimeInfo.getAttributeTypes();
//        for (int i = 0, typesLength = types.length; i < typesLength; i++) {
//            Attribute.Type type = types[i];
//            switch (type) {
//                case INT:
//                    buf.putInt((Integer) event[i]);
//                    continue;
//                case LONG:
//                    buf.putLong((Long) event[i]);
//                    continue;
//                case BOOL:
//                    buf.put((byte) (((Boolean) event[i]) ? 1 : 0));
//                    continue;
//                case FLOAT:
//                    buf.putFloat((Float) event[i]);
//                    continue;
//                case DOUBLE:
//                    buf.putDouble((Double) event[i]);
//                    continue;
//                case STRING:
//                    short length = (short) ((String) event[i]).length();
//                    buf.putShort(length);
//                    stringDataIndex[stringIndex] = i;
//                    stringIndex++;
//                    stringSize += length;
//            }
//        }
//        arrayOutputStream.write(buf.array());
//
//        buf = ByteBuffer.allocate(stringSize);
//        for (int aStringIndex : stringDataIndex) {
//            buf.put(((String) event[aStringIndex]).getBytes(DEFAULT_CHARSET));
//        }
//        arrayOutputStream.write(buf.array());
//
//        if (!isSynchronous){
//            publishToDisruptor(arrayOutputStream.toByteArray());
//        }else {
//            publishEvent(arrayOutputStream.toByteArray(), flush);
//        }
//    }
//
//    private void publishEvent(byte[] data, boolean flush) throws IOException {
//        outputStream.write(data);
//        if (flush) {
//            outputStream.flush();
//        }
//    }
}
