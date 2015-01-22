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


import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataEndpointAgent;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.agent.exception.EventQueueFullException;
import org.wso2.carbon.databridge.agent.util.DataEndpointConstants;
import org.wso2.carbon.databridge.agent.util.HADataPublisherUtil;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.UndefinedEventTypeException;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DataEndpointGroup implements DataEndpointFailureCallback {
    private static final Log log = LogFactory.getLog(DataEndpointGroup.class);

    private ArrayList<DataEndpoint> dataEndpoints;
    private ArrayList<DataEndpoint> failedEventsDataEndpoints;
    private HAType haType;
    private EventQueue eventQueue;
    private int reconnectionInterval;

    private AtomicInteger currentDataPublisherIndex = new AtomicInteger();
    private AtomicInteger maximumDataPublisherIndex = new AtomicInteger();
    private ScheduledExecutorService reconnectionService = Executors.newScheduledThreadPool(1);

    private final Integer START_INDEX = 0;

    public enum HAType {
        FAILOVER, LOADBALANCE
    }

    public DataEndpointGroup(HAType haType, DataEndpointAgent agent) {
        this.dataEndpoints = new ArrayList<DataEndpoint>();
        this.failedEventsDataEndpoints = new ArrayList<DataEndpoint>();
        this.haType = haType;
        this.reconnectionInterval = agent.getDataEndpointAgentConfiguration().getReconnectionInterval();
        this.eventQueue = new EventQueue(agent.getDataEndpointAgentConfiguration().getQueueSize());
        this.reconnectionService.scheduleAtFixedRate(new ReconnectionTask(), reconnectionInterval,
                reconnectionInterval, TimeUnit.SECONDS);
    }

    public void addDataEndpoint(DataEndpoint dataEndpoint) {
        dataEndpoints.add(dataEndpoint);
        dataEndpoint.registerDataEndpointFailureCallback(this);
        maximumDataPublisherIndex.incrementAndGet();
    }

    public void tryPublish(Event event) throws EventQueueFullException {
        eventQueue.tryPut(event);
    }

    public void publish(Event event){
        eventQueue.put(event);
    }


    class EventQueue {
        private RingBuffer<Event> ringBuffer;
        private Disruptor<Event> eventQueue;

        public final EventFactory<Event> EVENT_FACTORY = new EventFactory<Event>() {
            public Event newInstance() {
                return new Event();
            }
        };

        public EventQueue(int queueSize) {
            eventQueue = new Disruptor<Event>(EVENT_FACTORY, queueSize, Executors.newCachedThreadPool());
            eventQueue.handleEventsWith(new EventQueueWorker());
            this.ringBuffer = eventQueue.start();
        }

        public void tryPut(Event event) throws EventQueueFullException {
            long sequence = 0;
            try {
                sequence = this.ringBuffer.tryNext(1);
                Event bufferedEvent = this.ringBuffer.get(sequence);
                updateEvent(bufferedEvent, event);
                this.ringBuffer.publish(sequence);
            } catch (InsufficientCapacityException e) {
                throw new EventQueueFullException("Cannot send events because the event queue is full", e);
            }
        }

        private void put(Event event) {
            long sequence = 0;
            sequence = this.ringBuffer.next();
            Event bufferedEvent = this.ringBuffer.get(sequence);
            updateEvent(bufferedEvent, event);
            this.ringBuffer.publish(sequence);
        }

        private void updateEvent(Event oldEvent, Event newEvent) {
            oldEvent.setArbitraryDataMap(newEvent.getArbitraryDataMap());
            oldEvent.setCorrelationData(newEvent.getCorrelationData());
            oldEvent.setMetaData(newEvent.getMetaData());
            oldEvent.setPayloadData(newEvent.getPayloadData());
            oldEvent.setStreamId(newEvent.getStreamId());
            oldEvent.setTimeStamp(newEvent.getTimeStamp());
        }

        private void shutdown(){
            eventQueue.shutdown();
        }
    }

    class EventQueueWorker implements EventHandler<Event> {

        @Override
        public void onEvent(Event event, long sequence, boolean endOfBatch) throws Exception {
            DataEndpoint endpoint = getDataEndpoint();
            processFailedEventsIfExists();
            endpoint.collectAndSend(event);
            if (endOfBatch) {
                flushAllDataEndpoints();
            }
        }
    }

    private void flushAllDataEndpoints() {
        for (DataEndpoint dataEndpoint : dataEndpoints) {
            if (dataEndpoint.isActive()) {
                dataEndpoint.flushEvents();
            }
        }
    }

    private synchronized void processFailedEventsIfExists()
            throws UndefinedEventTypeException, DataEndpointException {
        if (!failedEventsDataEndpoints.isEmpty()) {
            for (DataEndpoint failedEndpoint : failedEventsDataEndpoints) {
                if (failedEndpoint.isActive()) {
                    failedEndpoint.flushEvents();
                } else {
                    ArrayList<Event> failedEvents = failedEndpoint.getAndResetFailedEvents();
                    for (Event event : failedEvents) {
                        getDataEndpoint().collectAndSend(event);
                    }
                }
                failedEndpoint.resetFailedEvents();
            }
            failedEventsDataEndpoints.clear();
        }
    }

    public DataEndpoint getDataEndpoint() {
        int startIndex;
        if (haType.equals(HAType.FAILOVER)) {
            startIndex = getDataPublisherIndex();
        } else {
            startIndex = START_INDEX;
        }
        int index = startIndex;

        while (true) {
            DataEndpoint dataEndpoint = dataEndpoints.get(index);
            if (dataEndpoint.isActive()) {
                return dataEndpoint;
            } else {
                index++;
                if (index > maximumDataPublisherIndex.get() - 1) {
                    index = START_INDEX;
                }
                if (index == startIndex) {
                    /**
                     * Have fully iterated the data publisher list,
                     * and busy wait until data publisher
                     * becomes available
                     */
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    private synchronized int getDataPublisherIndex() {
        int index = currentDataPublisherIndex.getAndIncrement();
        if (index == maximumDataPublisherIndex.get()) {
            currentDataPublisherIndex.set(START_INDEX);
        }
        return index;
    }

    public ArrayList<Event> tryResendEvents(ArrayList<Event> events) {
        ArrayList<Event> unsuccessfulEvents = new ArrayList<Event>();
        for (Event event : events) {
            try {
                eventQueue.tryPut(event);
            } catch (EventQueueFullException e) {
                unsuccessfulEvents.add(event);
            }
        }
        return unsuccessfulEvents;
    }

    public void addFailedDataEndpoint(DataEndpoint dataEndpoint) {
        this.failedEventsDataEndpoints.add(dataEndpoint);
    }

    private class ReconnectionTask implements Runnable {
        public void run() {
            boolean isOneReceiverConnected = false;
            for (int i = START_INDEX; i < maximumDataPublisherIndex.get(); i++) {
                DataEndpoint dataEndpoint = dataEndpoints.get(i);
                if (!dataEndpoint.isConnected()) {
                    try {
                        dataEndpoint.connect();
                    } catch (Exception ex) {
                        dataEndpoint.deactivate();
                    }
                } else {
                    String[] urlElements = HADataPublisherUtil.getProtocolHostPort(
                            dataEndpoint.getDataEndpointConfiguration().getReceiverURL());
                    if (!isServerExists(urlElements[1], Integer.parseInt(urlElements[2]))) {
                        dataEndpoint.deactivate();
                    }
                }
                if (dataEndpoint.isConnected()) {
                    isOneReceiverConnected = true;
                }
            }
            if (!isOneReceiverConnected) {
                log.info("No receiver is reachable at reconnection, will try to reconnect every " + reconnectionInterval + " sec");
            }
        }

        private boolean isServerExists(String ip, int port) {
            try {
                new Socket(ip, port);
                return true;
            } catch (UnknownHostException e) {
                return false;
            } catch (IOException e) {
                return false;
            } catch (Exception e) {
                return false;
            }
        }
    }

    public String toString() {
        String group = "[ ";
        for (int i = 0; i < dataEndpoints.size(); i++) {
            DataEndpoint endpoint = dataEndpoints.get(i);
            group += endpoint.toString();
            if (i == dataEndpoints.size() - 1) {
                group += " ]";
                return group;
            } else {
                if (haType == HAType.FAILOVER) {
                    group += DataEndpointConstants.FAILOVER_URL_GROUP_SEPARATOR;
                } else {
                    group += DataEndpointConstants.LB_URL_GROUP_SEPARATOR;
                }
            }
        }
        return group;
    }

    public void shutdown(){
        eventQueue.shutdown();
        reconnectionService.shutdown();
        for (DataEndpoint dataEndpoint: dataEndpoints){
            dataEndpoint.shutdown();
        }
    }
}
