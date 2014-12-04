/**
 *
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.databridge.test.thrift;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.agent.thrift.lb.DataPublisherHolder;
import org.wso2.carbon.databridge.agent.thrift.lb.LoadBalancingDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.lb.ReceiverGroup;
import org.wso2.carbon.databridge.agent.thrift.util.DataPublisherUtil;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.*;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EventTest extends TestCase {

    private static Logger log = Logger.getLogger(EventTest.class);

    public void testSendingEvent()
            throws MalformedURLException, AuthenticationException, TransportException,
            AgentException, UndefinedEventTypeException,
            DifferentStreamDefinitionAlreadyDefinedException,
            InterruptedException, DataBridgeException,
            MalformedStreamDefinitionException,
            StreamDefinitionException {

        TestServer testServer = new TestServer();
        testServer.start(7625);
        KeyStoreUtil.setTrustStoreParams();
        Thread.sleep(2000);

        //according to the convention the authentication port will be 7611+100= 7711 and its host will be the same
        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7625", "admin", "admin");
        String id1 = dataPublisher.defineStream("{" +
                "  'name':'org.wso2.esb.MediatorStatistics'," +
                "  'version':'2.3.0'," +
                "  'nickName': 'Stock Quote Information'," +
                "  'description': 'Some Desc'," +
                "  'tags':['foo', 'bar']," +
                "  'metaData':[" +
                "          {'name':'ipAdd','type':'STRING'}" +
                "  ]," +
                "  'correlationData':[" +
                "          {'name':'correlationId','type':'STRING'}" +
                "  ]," +
                "  'payloadData':[" +
                "          {'name':'symbol','type':'STRING'}," +
                "          {'name':'price','type':'DOUBLE'}," +
                "          {'name':'volume','type':'INT'}," +
                "          {'name':'max','type':'DOUBLE'}," +
                "          {'name':'min','type':'Double'}" +
                "  ]" +
                "}");

        //In this case correlation data is null
        dataPublisher.publish(id1, new Object[]{"127.0.0.1"}, new Object[]{"HD34"}, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
        Map<String, String> map = new HashMap<String, String>();
        map.put("date", "2012/11/01");
        map.put("time", "14:23");
        dataPublisher.publish(id1, new Object[]{"127.0.0.2"}, new Object[]{"HD476"}, new Object[]{"MSFT", 196.8, 100, 200.6, 70.4}, map);
        Thread.sleep(3000);
        dataPublisher.stop();
        testServer.stop();
    }

    public void testSendingEventSendingNull()
            throws MalformedURLException, AuthenticationException, TransportException,
            AgentException, UndefinedEventTypeException,
            DifferentStreamDefinitionAlreadyDefinedException,
            InterruptedException, DataBridgeException,
            MalformedStreamDefinitionException,
            StreamDefinitionException {

        TestServer testServer = new TestServer();
        testServer.start(7626);
        KeyStoreUtil.setTrustStoreParams();
        Thread.sleep(3000);

        //according to the convention the authentication port will be 7611+100= 7711 and its host will be the same
        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7626", "admin", "admin");
        String id1 = dataPublisher.defineStream("{" +
                "  'name':'org.wso2.esb.MediatorStatistics'," +
                "  'version':'2.3.0'," +
                "  'nickName': 'Stock Quote Information'," +
                "  'description': 'Some Desc'," +
                "  'tags':['foo', 'bar']," +
                "  'metaData':[" +
                "          {'name':'ipAdd','type':'STRING'}" +
                "  ]," +
                "  'correlationData':[" +
                "          {'name':'correlationId','type':'STRING'}" +
                "  ]," +
                "  'payloadData':[" +
                "          {'name':'symbol','type':'STRING'}," +
                "          {'name':'price','type':'DOUBLE'}," +
                "          {'name':'volume','type':'INT'}," +
                "          {'name':'max','type':'DOUBLE'}," +
                "          {'name':'min','type':'Double'}" +
                "  ]" +
                "}");

        //In this case correlation data is null
        dataPublisher.publish(id1, new Object[]{"127.0.0.1"}, new Object[]{null}, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
        Thread.sleep(3000);
        dataPublisher.stop();
        testServer.stop();
    }

    public void testSendingMultipleEventsOfSameType()
            throws MalformedURLException, AuthenticationException, TransportException,
            AgentException, UndefinedEventTypeException,
            DifferentStreamDefinitionAlreadyDefinedException,
            InterruptedException, DataBridgeException,
            MalformedStreamDefinitionException,
            StreamDefinitionException {

        TestServer testServer = new TestServer();
        testServer.start(7612);
        KeyStoreUtil.setTrustStoreParams();

        Thread.sleep(3000);
        //according to the convention the authentication port will be 7612+100= 7711 and its host will be the same
        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7612", "admin", "admin");
        String streamId = dataPublisher.defineStream("{" +
                "  'name':'org.wso2.esb.MediatorStatistics'," +
                "  'version':'1.3.0'," +
                "  'nickName': 'Stock Quote Information'," +
                "  'description': 'Some Desc'," +
                "  'metaData':[" +
                "          {'name':'ipAdd','type':'STRING'}" +
                "  ]," +
                "  'payloadData':[" +
                "          {'name':'symbol','type':'STRING'}," +
                "          {'name':'price','type':'DOUBLE'}," +
                "          {'name':'volume','type':'INT'}," +
                "          {'name':'max','type':'DOUBLE'}," +
                "          {'name':'min','type':'Double'}" +
                "  ]" +
                "}");
        //In this case correlation data is null
        dataPublisher.publish(streamId, new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
        dataPublisher.publish(streamId, System.currentTimeMillis(), new Object[]{"127.0.0.2"}, null, new Object[]{"WSO2", 100.8, 200, 110.4, 74.7});
        //else the user can publish event it self 
        dataPublisher.publish(new Event(streamId, System.currentTimeMillis(), new Object[]{"127.0.0.3"}, null, new Object[]{"WSO2", 100.8, 200, 110.4, 74.7}));

        dataPublisher.stop();
        testServer.stop();

    }

    public void testSendingMultipleEventsOfDifferentType()
            throws MalformedURLException, AuthenticationException, TransportException,
            AgentException, UndefinedEventTypeException,
            DifferentStreamDefinitionAlreadyDefinedException,
            InterruptedException, DataBridgeException,
            MalformedStreamDefinitionException,
            StreamDefinitionException {

        TestServer testServer = new TestServer();
        testServer.start(7613);
        KeyStoreUtil.setTrustStoreParams();
        Thread.sleep(3000);

        //according to the convention the authentication port will be 7611+100= 7711 and its host will be the same
        DataPublisher dataPublisher = new DataPublisher("ssl://localhost:7713", "tcp://localhost:7613", "admin", "admin");
        Thread.sleep(2000);
        String streamId = dataPublisher.defineStream("{" +
                "  'name':'org.wso2.esb.MediatorStatistics'," +
                "  'version':'2.3.0'," +
                "  'nickName': 'Stock Quote Information'," +
                "  'description': 'Some Desc'," +
                "  'metaData':[" +
                "          {'name':'ipAdd','type':'STRING'}" +
                "  ]," +
                "  'payloadData':[" +
                "          {'name':'symbol','type':'STRING'}," +
                "          {'name':'price','type':'DOUBLE'}," +
                "          {'name':'volume','type':'INT'}," +
                "          {'name':'max','type':'DOUBLE'}," +
                "          {'name':'min','type':'Double'}" +
                "  ]" +
                "}");
        String shortStreamId = dataPublisher.defineStream("{" +
                "  'name':'org.wso2.esb.MediatorStatisticsShort'," +
                "  'version':'2.0.0'," +
                "  'nickName': 'Short Stock Quote Information'," +
                "  'description': 'Some Desc'," +
                "  'metaData':[" +
                "          {'name':'ipAdd','type':'STRING'}" +
                "  ]," +
                "  'payloadData':[" +
                "          {'name':'symbol','type':'STRING'}," +
                "          {'name':'price','type':'DOUBLE'}," +
                "          {'name':'volume','type':'INT'}" +
                "  ]" +
                "}");
        //In this case correlation data is null
        dataPublisher.publish(streamId, new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
        dataPublisher.publish(streamId, new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
        dataPublisher.publish(streamId, new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
        dataPublisher.publish(shortStreamId, System.currentTimeMillis(), new Object[]{"127.0.0.2"}, null, new Object[]{"WSO2", 100.8, 200});
        //else the user can publish event it self
        dataPublisher.publish(new Event(streamId, System.currentTimeMillis(), new Object[]{"127.0.0.3"}, null, new Object[]{"WSO2", 100.8, 200, 110.4, 74.7}));
        dataPublisher.stop();
        testServer.stop();
    }

//    public void testSendingWrongEvents()
//            throws MalformedURLException, AuthenticationException, TransportException,
//                   AgentException, UndefinedEventTypeException,
//                   DifferentStreamDefinitionAlreadyDefinedException, WrongEventTypeException,
//                   InterruptedException, DataBridgeException,
//                   MalformedStreamDefinitionException,
//                   StreamDefinitionException {
//
//        TestServer testServer = new TestServer();
//        testServer.start(7618);
//        KeyStoreUtil.setTrustStoreParams();
//
//        Thread.sleep(2000);
//        //according to the convention the authentication port will be 7611+100= 7711 and its host will be the same
//        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7618", "admin", "admin");
//        String streamId = dataPublisher.definedStream("{" +
//                                                          "  'name':'org.wso2.esb.MediatorStatistics'," +
//                                                          "  'version':'1.3.0'," +
//                                                          "  'nickName': 'Stock Quote Information'," +
//                                                          "  'description': 'Some Desc'," +
//                                                          "  'metaData':[" +
//                                                          "          {'name':'ipAdd','type':'STRING'}" +
//                                                          "  ]," +
//                                                          "  'payloadData':[" +
//                                                          "          {'name':'symbol','type':'STRING'}," +
//                                                          "          {'name':'price','type':'DOUBLE'}," +
//                                                          "          {'name':'volume','type':'INT'}," +
//                                                          "          {'name':'max','type':'DOUBLE'}," +
//                                                          "          {'name':'min','type':'Double'}" +
//                                                          "  ]" +
//                                                          "}");
//        //In this case correlation data is null
//        dataPublisher.publish(streamId, new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6});
//        Thread.sleep(3000);
//
//        dataPublisher.stop();
//        testServer.stop();
//
//    }

    public void testRequestingStreamId()
            throws MalformedURLException, AuthenticationException, TransportException,
            AgentException, UndefinedEventTypeException,
            DifferentStreamDefinitionAlreadyDefinedException,
            InterruptedException, DataBridgeException,
            MalformedStreamDefinitionException,
            StreamDefinitionException, NoStreamDefinitionExistException {

        TestServer testServer = new TestServer();
        testServer.start(7619);
        KeyStoreUtil.setTrustStoreParams();

        Thread.sleep(2000);
        //according to the convention the authentication port will be 7611+100= 7711 and its host will be the same
        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7619", "admin", "admin");
        dataPublisher.defineStream("{" +
                "  'name':'org.wso2.esb.MediatorStatistics'," +
                "  'version':'1.3.0'," +
                "  'nickName': 'Stock Quote Information'," +
                "  'description': 'Some Desc'," +
                "  'metaData':[" +
                "          {'name':'ipAdd','type':'STRING'}" +
                "  ]," +
                "  'payloadData':[" +
                "          {'name':'symbol','type':'STRING'}," +
                "          {'name':'price','type':'DOUBLE'}," +
                "          {'name':'volume','type':'INT'}," +
                "          {'name':'max','type':'DOUBLE'}," +
                "          {'name':'min','type':'Double'}" +
                "  ]" +
                "}");
        String receivedStreamId = dataPublisher.findStream("org.wso2.esb.MediatorStatistics", "1.3.0");
        //In this case correlation data is null
        dataPublisher.publish(receivedStreamId, new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6, 20.6});
        dataPublisher.publish(receivedStreamId, System.currentTimeMillis(), new Object[]{"127.0.0.2"}, null, new Object[]{"WSO2", 100.8, 200, 110.4, 74.7});
//        else the user can publish event it self
        dataPublisher.publish(new Event(receivedStreamId, System.currentTimeMillis(), new Object[]{"127.0.0.3"}, null, new Object[]{"WSO2", 100.8, 200, 110.4, 74.7}));

        dataPublisher.stop();
        testServer.stop();

    }

    public void testSendingSecureEventsOfSameType()
            throws MalformedURLException, AuthenticationException, TransportException,
            AgentException, UndefinedEventTypeException,
            DifferentStreamDefinitionAlreadyDefinedException,
            InterruptedException, DataBridgeException,
            MalformedStreamDefinitionException,
            StreamDefinitionException {

        TestServer testServer = new TestServer();
        testServer.start(7620);
        KeyStoreUtil.setTrustStoreParams();

        Thread.sleep(2000);
        //according to the convention the authentication port will be 7612+100= 7711 and its host will be the same
        DataPublisher dataPublisher = new DataPublisher("ssl://localhost:7720", "admin", "admin");
        Thread.sleep(2000);
        String streamId = dataPublisher.defineStream("{" +
                "  'name':'org.wso2.esb.MediatorStatistics'," +
                "  'version':'1.3.0'," +
                "  'nickName': 'Stock Quote Information'," +
                "  'description': 'Some Desc'," +
                "  'metaData':[" +
                "          {'name':'ipAdd','type':'STRING'}" +
                "  ]," +
                "  'payloadData':[" +
                "          {'name':'symbol','type':'STRING'}," +
                "          {'name':'price','type':'DOUBLE'}," +
                "          {'name':'volume','type':'INT'}," +
                "          {'name':'max','type':'DOUBLE'}," +
                "          {'name':'min','type':'Double'}" +
                "  ]" +
                "}");
        //In this case correlation data is null
        dataPublisher.publish(streamId, new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
        dataPublisher.publish(streamId, System.currentTimeMillis(), new Object[]{"127.0.0.2"}, null, new Object[]{"WSO2", 100.8, 200, 110.4, 74.7});
        //else the user can publish event it self
        dataPublisher.publish(new Event(streamId, System.currentTimeMillis(), new Object[]{"127.0.0.3"}, null, new Object[]{"WSO2", 100.8, 200, 110.4, 74.7}));

        dataPublisher.stop();
        testServer.stop();

    }

    public void testSendingSecureEventsByDefiningAllUrls()
            throws MalformedURLException, AuthenticationException, TransportException,
            AgentException, UndefinedEventTypeException,
            DifferentStreamDefinitionAlreadyDefinedException,
            InterruptedException, DataBridgeException,
            MalformedStreamDefinitionException,
            StreamDefinitionException {

        TestServer testServer = new TestServer();
        testServer.start(7621);
        KeyStoreUtil.setTrustStoreParams();

        Thread.sleep(2000);
        //according to the convention the authentication port will be 7612+100= 7711 and its host will be the same
        DataPublisher dataPublisher = new DataPublisher("ssl://localhost:7721", "ssl://localhost:7721", "admin", "admin");
        String streamId = dataPublisher.defineStream("{" +
                "  'name':'org.wso2.esb.MediatorStatistics'," +
                "  'version':'1.3.0'," +
                "  'nickName': 'Stock Quote Information'," +
                "  'description': 'Some Desc'," +
                "  'metaData':[" +
                "          {'name':'ipAdd','type':'STRING'}" +
                "  ]," +
                "  'payloadData':[" +
                "          {'name':'symbol','type':'STRING'}," +
                "          {'name':'price','type':'DOUBLE'}," +
                "          {'name':'volume','type':'INT'}," +
                "          {'name':'max','type':'DOUBLE'}," +
                "          {'name':'min','type':'Double'}" +
                "  ]" +
                "}");
        //In this case correlation data is null
        dataPublisher.publish(streamId, new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
        dataPublisher.publish(streamId, System.currentTimeMillis(), new Object[]{"127.0.0.2"}, null, new Object[]{"WSO2", 100.8, 200, 110.4, 74.7});
        //else the user can publish event it self
        dataPublisher.publish(new Event(streamId, System.currentTimeMillis(), new Object[]{"127.0.0.3"}, null, new Object[]{"WSO2", 100.8, 200, 110.4, 74.7}));

        dataPublisher.stop();
        testServer.stop();

    }

    public void testSessionTimeOut()
            throws MalformedURLException, AuthenticationException, TransportException,
            AgentException, UndefinedEventTypeException,
            DifferentStreamDefinitionAlreadyDefinedException,
            InterruptedException, DataBridgeException,
            MalformedStreamDefinitionException,
            StreamDefinitionException {

        TestServer testServer = new TestServer();
        testServer.start(7631);
        KeyStoreUtil.setTrustStoreParams();

        Thread.sleep(2000);
        //according to the convention the authentication port will be 7612+100= 7711 and its host will be the same
        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7631", "admin", "admin");
        String streamId = dataPublisher.defineStream("{" +
                "  'name':'org.wso2.esb.MediatorStatistics'," +
                "  'version':'1.3.0'," +
                "  'nickName': 'Stock Quote Information'," +
                "  'description': 'Some Desc'," +
                "  'metaData':[" +
                "          {'name':'ipAdd','type':'STRING'}" +
                "  ]," +
                "  'payloadData':[" +
                "          {'name':'symbol','type':'STRING'}," +
                "          {'name':'price','type':'DOUBLE'}," +
                "          {'name':'volume','type':'INT'}," +
                "          {'name':'max','type':'DOUBLE'}," +
                "          {'name':'min','type':'Double'}" +
                "  ]" +
                "}");
        //In this case correlation data is null
        dataPublisher.publish(streamId, new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
        Thread.sleep(3000);
        testServer.stop();
        Thread.sleep(12000);
        testServer.start(7631);
        Thread.sleep(3000);
        //Stream is again defined here because we are using inmMemoryDataStore at the server and it wont persist data
        // when using casandra the defineStream() is not needed
        streamId = dataPublisher.defineStream("{" +
                "  'name':'org.wso2.esb.MediatorStatistics'," +
                "  'version':'1.3.0'," +
                "  'nickName': 'Stock Quote Information'," +
                "  'description': 'Some Desc'," +
                "  'metaData':[" +
                "          {'name':'ipAdd','type':'STRING'}" +
                "  ]," +
                "  'payloadData':[" +
                "          {'name':'symbol','type':'STRING'}," +
                "          {'name':'price','type':'DOUBLE'}," +
                "          {'name':'volume','type':'INT'}," +
                "          {'name':'max','type':'DOUBLE'}," +
                "          {'name':'min','type':'Double'}" +
                "  ]" +
                "}");
        Thread.sleep(1000);
        dataPublisher.publish(streamId, new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
        dataPublisher.stop();
        testServer.stop();

    }


    public void testDeletingStream()
            throws MalformedURLException, AuthenticationException, TransportException,
            AgentException, UndefinedEventTypeException,
            DifferentStreamDefinitionAlreadyDefinedException,
            InterruptedException, DataBridgeException,
            MalformedStreamDefinitionException,
            StreamDefinitionException {

        TestServer testServer = new TestServer();
        testServer.start(7632);
        KeyStoreUtil.setTrustStoreParams();
        Thread.sleep(2000);

        //according to the convention the authentication port will be 7611+100= 7711 and its host will be the same
        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7632", "admin", "admin");
        String id1 = dataPublisher.defineStream("{" +
                "  'name':'org.wso2.esb.MediatorStatistics'," +
                "  'version':'2.3.0'," +
                "  'nickName': 'Stock Quote Information'," +
                "  'description': 'Some Desc'," +
                "  'tags':['foo', 'bar']," +
                "  'metaData':[" +
                "          {'name':'ipAdd','type':'STRING'}" +
                "  ]," +
                "  'correlationData':[" +
                "          {'name':'correlationId','type':'STRING'}" +
                "  ]," +
                "  'payloadData':[" +
                "          {'name':'symbol','type':'STRING'}," +
                "          {'name':'price','type':'DOUBLE'}," +
                "          {'name':'volume','type':'INT'}," +
                "          {'name':'max','type':'DOUBLE'}," +
                "          {'name':'min','type':'Double'}" +
                "  ]" +
                "}");

        //In this case correlation data is null
        dataPublisher.publish(id1, new Object[]{"127.0.0.1"}, new Object[]{"HD34"}, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
        Thread.sleep(3000);

        if (!dataPublisher.deleteStream(id1)) {
            Assert.fail("Stream not deleted");
        }
        boolean pass = false;
        try {
            dataPublisher.findStream("org.wso2.esb.MediatorStatistics", "2.3.0");
        } catch (NoStreamDefinitionExistException e) {
            pass = true;
        }
        Assert.assertEquals(true, pass);

        id1 = dataPublisher.defineStream("{" +
                "  'name':'org.wso2.esb.MediatorStatistics'," +
                "  'version':'2.3.0'," +
                "  'nickName': 'Stock Quote Information'," +
                "  'description': 'Some Desc'," +
                "  'tags':['foo', 'bar']," +
                "  'metaData':[" +
                "          {'name':'ipAdd','type':'STRING'}" +
                "  ]," +
                "  'correlationData':[" +
                "          {'name':'correlationId','type':'STRING'}" +
                "  ]," +
                "  'payloadData':[" +
                "          {'name':'symbol','type':'STRING'}," +
                "          {'name':'price','type':'DOUBLE'}," +
                "          {'name':'volume','type':'INT'}," +
                "          {'name':'max','type':'DOUBLE'}," +
                "          {'name':'min','type':'Double'}" +
                "  ]" +
                "}");

        //In this case correlation data is null
        dataPublisher.publish(id1, new Object[]{"127.0.0.1"}, new Object[]{"HD34"}, new Object[]{"WSO2", 96.8, 300, 120.6, 70.4});
        Thread.sleep(3000);

        dataPublisher.stop();
        testServer.stop();
    }

    public void testConnectionCloseOnEviction()
            throws IOException, AuthenticationException, TransportException,
            AgentException, UndefinedEventTypeException,
            DifferentStreamDefinitionAlreadyDefinedException,
            InterruptedException, DataBridgeException,
            MalformedStreamDefinitionException,
            StreamDefinitionException {

        TestServer testServer = new TestServer();
        testServer.start(7625);
        KeyStoreUtil.setTrustStoreParams();
        Thread.sleep(2000);

        //according to the convention the authentication port will be 7625+100= 7725 and its host will be the same
        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7625", "admin", "admin");
        String id1 = dataPublisher.defineStream("{" +
                "  'name':'org.wso2.esb.MediatorStatistics'," +
                "  'version':'2.3.0'," +
                "  'nickName': 'Stock Quote Information'," +
                "  'description': 'Some Desc'," +
                "  'tags':['foo', 'bar']," +
                "  'metaData':[" +
                "          {'name':'ipAdd','type':'STRING'}" +
                "  ]," +
                "  'correlationData':[" +
                "          {'name':'correlationId','type':'STRING'}" +
                "  ]," +
                "  'payloadData':[" +
                "          {'name':'symbol','type':'STRING'}," +
                "          {'name':'price','type':'DOUBLE'}," +
                "          {'name':'volume','type':'INT'}," +
                "          {'name':'max','type':'DOUBLE'}," +
                "          {'name':'min','type':'Double'}" +
                "  ]" +
                "}");

        dataPublisher.publish(id1, new Object[]{"127.0.0.1"}, new Object[]{"HD34"}, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
        Thread.sleep(10000);        //sleep > evictionTime
        dataPublisher.publish(id1, new Object[]{"127.0.0.2"}, new Object[]{"HD476"}, new Object[]{"MSFT", 196.8, 100, 200.6, 70.4});
        Thread.sleep(10000);        //sleep > evictionTime
        dataPublisher.publish(id1, new Object[]{"127.0.0.2"}, new Object[]{"HD476"}, new Object[]{"MSFT", 196.8, 100, 200.6, 70.4});
        Thread.sleep(10000);        //sleep > evictionTime
        dataPublisher.publish(id1, new Object[]{"127.0.0.2"}, new Object[]{"HD476"}, new Object[]{"MSFT", 196.8, 100, 200.6, 70.4});
        Thread.sleep(10000);        //sleep > evictionTime
        dataPublisher.publish(id1, new Object[]{"127.0.0.2"}, new Object[]{"HD476"}, new Object[]{"MSFT", 196.8, 100, 200.6, 70.4});
        Thread.sleep(10000);        //sleep > evictionTime
        dataPublisher.publish(id1, new Object[]{"127.0.0.2"}, new Object[]{"HD476"}, new Object[]{"MSFT", 196.8, 100, 200.6, 70.4});
        Thread.sleep(10000);        //sleep > evictionTime
        dataPublisher.publish(id1, new Object[]{"127.0.0.2"}, new Object[]{"HD476"}, new Object[]{"MSFT", 196.8, 100, 200.6, 70.4});
        Thread.sleep(10000);        //sleep > evictionTime
        dataPublisher.publish(id1, new Object[]{"127.0.0.2"}, new Object[]{"HD476"}, new Object[]{"MSFT", 196.8, 100, 200.6, 70.4});
        Thread.sleep(10000);        //sleep > evictionTime
        dataPublisher.publish(id1, new Object[]{"127.0.0.2"}, new Object[]{"HD476"}, new Object[]{"MSFT", 196.8, 100, 200.6, 70.4});

        String[] cmd = { "/bin/sh", "-c", "netstat -an | grep :7625" };     //netstat cmd to get connections to port 7625
        Process process = Runtime.getRuntime().exec(cmd);
        BufferedReader buffer = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = new String();
        int count =0;
        while((line = buffer.readLine()) != null)
        {
            System.out.println(line);
            if(line.contains("ESTABLISHED")){
                count++;
            }

        }
        Assert.assertTrue("No. of ESTABLISHED connections should be lower than or equal to two",count<=2);
        Thread.sleep(3000);
        dataPublisher.stop();
        testServer.stop();

    }

    public void testLoadBalancerDataPublisher()throws AgentException{

        KeyStoreUtil.setTrustStoreParams();

        String streamName = "analytics_Statistics";
        String streamVersion = "1.3.0";
        int numOfEvents=10005;

        String receiverUrls = "{tcp://localhost:7626}";

        ArrayList<ReceiverGroup> allReceiverGroups = new ArrayList<ReceiverGroup>();

        String streamDefintion = "{" +
                "  'name':'org.wso2.esb.MediatorStatistics'," +
                "  'version':'1.3.0'," +
                "  'nickName': 'Stock Quote Information'," +
                "  'description': 'Some Desc'," +
                "  'metaData':[" +
                "          {'name':'ipAdd','type':'STRING'}" +
                "  ]," +
                "  'payloadData':[" +
                "          {'name':'symbol','type':'STRING'}," +
                "          {'name':'price','type':'DOUBLE'}," +
                "          {'name':'volume','type':'INT'}," +
                "          {'name':'max','type':'DOUBLE'}," +
                "          {'name':'min','type':'Double'}" +
                "  ]" +
                "}";


        ArrayList<String> receiverGroupUrls = DataPublisherUtil.getReceiverGroups(receiverUrls);

        for (String aReceiverGroupURL : receiverGroupUrls) {
            ArrayList<DataPublisherHolder> dataPublisherHolders = new ArrayList<DataPublisherHolder>();
            String[] urls = aReceiverGroupURL.split(",");
            for (String aUrl : urls) {
                DataPublisherHolder aNode = new DataPublisherHolder(null, aUrl.trim(), "admin", "admin");
                dataPublisherHolders.add(aNode);
            }
            ReceiverGroup group = new ReceiverGroup(dataPublisherHolders);
            allReceiverGroups.add(group);
        }

        LoadBalancingDataPublisher loadBalancingDataPublisher = new LoadBalancingDataPublisher(allReceiverGroups);
        loadBalancingDataPublisher.addStreamDefinition(streamDefintion,streamName ,streamVersion );

        for (int i = 0; i < numOfEvents; i++) {
            try {
                loadBalancingDataPublisher.publish(streamName, streamVersion,generateEvent(streamName+":"+streamVersion));
                if(i >= 9998) {
                    log.info("Sending message #" + (i+1) + " to stream:" + streamName + ":" + streamVersion);
                }
            } catch (IllegalStateException e) {
                if(e.getMessage().equals("Queue full")){
                    Assert.fail("Received exception: " + e.getMessage() + ", Failing test case: testLoadBalancerDataPublisher()");
                } else{
                    throw e;
                }
            }
        }
        log.info("Sent " + numOfEvents + " successfully! Stopping LoadBalancingDataPublisher...");
        loadBalancingDataPublisher.stop();
    }

    private Event generateEvent(String streamId) {
        Event event = new Event();
        event.setStreamId(streamId);
        event.setTimeStamp(System.currentTimeMillis());

        event.setMetaData(createMetaData());
        event.setCorrelationData(createCorrelationData());
        event.setPayloadData(createPayloadData());
        return event;
    }

    private Object[] createMetaData() {
        Object[] objects = new Object[1];
        objects[0] = "127.0.0.1";
        return objects;
    }

    private Object[] createCorrelationData() {
        return null;
    }

    private Object[] createPayloadData() {
        Object[] objects = new Object[5];
        objects[0] = "IBM";
        objects[1] = 76.5;
        objects[2] = 234;
        objects[3] = 89.3;
        objects[4] = 70.5;
        return objects;
    }



}
