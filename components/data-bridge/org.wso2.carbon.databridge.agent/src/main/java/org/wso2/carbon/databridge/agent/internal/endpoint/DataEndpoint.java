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
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.agent.internal.conf.DataEndpointConfiguration;
import org.wso2.carbon.databridge.agent.util.DataEndpointConstants;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.SessionTimeoutException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.commons.exception.UndefinedEventTypeException;

import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class DataEndpoint {
    private static Log log = LogFactory.getLog(DataEndpoint.class);

    private boolean active;
    private DataEndpointConnectionWorker connectionWorker;
    private GenericKeyedObjectPool transportPool;
    private int batchSize;

    private AtomicBoolean isPublishing;
    private AtomicBoolean hasFailedEvents;
    private EventPublisher eventPublisher;
    private DataEndpointFailureCallback dataEndpointFailureCallback;

    private ExecutorService connectionService;
    private ArrayList<Event> events;

    public DataEndpoint() {
        this.batchSize = DataEndpointConstants.DEFAULT_DATA_AGENT_BATCH_SIZE;
        isPublishing = new AtomicBoolean(false);
        hasFailedEvents = new AtomicBoolean(false);
        eventPublisher = new EventPublisher(this);
        connectionService = Executors.newSingleThreadExecutor();
        events = new ArrayList<Event>();
    }

    void collectAndSend(Event event) throws DataEndpointException, UndefinedEventTypeException {
        events.add(event);
        if (events.size() == batchSize) {
            isPublishing.set(true);
            Thread thread = new Thread(eventPublisher);
            thread.start();
        }
    }

    void flushEvents() {
        if (events.size() != 0) {
            if (isPublishing.compareAndSet(false, true)) {
                Thread thread = new Thread(eventPublisher);
                thread.start();
            }
        }
    }


    void connect()
            throws TransportException,
            DataEndpointAuthenticationException, DataEndpointException {
        if (connectionWorker != null) {
            connectionService.submit(connectionWorker);
        } else {
            throw new DataEndpointException("Data Endpoint is not initialized");
        }
    }

    public void initialize(DataEndpointConfiguration dataEndpointConfiguration)
            throws DataEndpointException, DataEndpointAuthenticationException,
            TransportException {
        this.transportPool = dataEndpointConfiguration.getTransportPool();
        this.batchSize = dataEndpointConfiguration.getBatchSize();
        connectionWorker = new DataEndpointConnectionWorker();
        connectionWorker.initialize(this, dataEndpointConfiguration);
        initialize();
        connect();
    }

    protected abstract String connect(Object client, String userName, String password)
            throws DataEndpointAuthenticationException;

    protected abstract void disconnect(Object client, String sessionId)
            throws DataEndpointAuthenticationException;

    protected abstract void initialize() throws DataEndpointException;


    public boolean isActive() {
        return active && !isPublishing.get();
    }

    void activate() {
        active = true;
    }

    void deactivate() {
        active = false;
    }

    protected abstract void send(Object client, ArrayList<Event> events) throws
            DataEndpointException, SessionTimeoutException, UndefinedEventTypeException;


    protected DataEndpointConfiguration getDataEndpointConfiguration() {
        return this.connectionWorker.getDataEndpointConfiguration();
    }

    private Object getClient() throws DataEndpointException {
        try {
            return transportPool.borrowObject(getDataEndpointConfiguration().getPublisherKey());
        } catch (Exception e) {
            throw new DataEndpointException("Cannot borrow client for " +
                    getDataEndpointConfiguration().getPublisherKey(), e);
        }
    }

    private void returnClient(Object client) {
        try {
            transportPool.returnObject(getDataEndpointConfiguration().getPublisherKey(), client);
        } catch (Exception e) {
            log.warn("Error occurred while returning object to connection pool", e);
            discardClient();
        }
    }

    private void discardClient() {
        transportPool.clear(getDataEndpointConfiguration().getPublisherKey());
    }

    void registerDataEndpointFailureCallback(DataEndpointFailureCallback callback) {
        dataEndpointFailureCallback = callback;
    }

    class EventPublisher implements Runnable {
        private DataEndpoint dataEndpoint;

        EventPublisher(DataEndpoint dataEndpoint) {
            this.dataEndpoint = dataEndpoint;
        }


        @Override
        public void run() {
            try {
                publish();
            } catch (SessionTimeoutException e) {
                try {
                    connect();
                    publish();
                } catch (UndefinedEventTypeException ex) {
                    log.error("Unable to process this event.", ex);
                } catch (Exception ex) {
                    handleFailedEvents();
                }
            } catch (DataEndpointException e) {
                handleFailedEvents();
            } catch (UndefinedEventTypeException e) {
                log.error("Unable to process this event.", e);
            }
        }

        private void handleFailedEvents() {
            deactivate();
            events = dataEndpointFailureCallback.tryResendEvents(events);
            if (!events.isEmpty()) {
                hasFailedEvents.set(true);
                dataEndpointFailureCallback.addFailedDataEndpoint(dataEndpoint);
            }
            isPublishing.set(false);
        }

        private void publish() throws DataEndpointException,
                SessionTimeoutException,
                UndefinedEventTypeException {
            Object client = getClient();
            send(client, events);
            events.clear();
            isPublishing.set(false);
            returnClient(client);
        }
    }

    ArrayList<Event> getAndResetFailedEvents() {
        if (hasFailedEvents.get()) {
            ArrayList<Event> failedEvents = events;
            events = new ArrayList<Event>();
            return failedEvents;
        }
        return null;
    }

    void resetFailedEvents() {
        hasFailedEvents.set(false);
    }

    boolean isConnected() {
        return active;
    }

    public String toString() {
        return "( Receiver URL : " + getDataEndpointConfiguration().getReceiverURL() +
                ", Authentication URL : " + getDataEndpointConfiguration().getAuthURL() + ")";
    }

    public void shutdown() {
        if (isPublishing.get()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
        connectionWorker.disconnect(getDataEndpointConfiguration());
        connectionService.shutdown();
    }

    public abstract String getClientPoolFactoryClass();

    public abstract String getSecureClientPoolFactoryClass();
}
