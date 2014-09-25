/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.wso2.carbon.tracer.module;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.wso2.carbon.tracer.TracerConstants;
import org.wso2.carbon.tracer.TracerUtils;
import org.wso2.carbon.utils.logging.CircularBuffer;

import java.util.HashMap;
import java.util.Map;

/**
 * A memory based trace persister which holds the traced messages in a cicular buffer
 */
public class MemoryBasedTracePersister implements TracePersister {

    private static final String TRACING_MAP    = "local_wso2tracer.map";
    private static final String REQUEST_NUMBER = "local_wso2tracer.request.number";

    private CircularBuffer msgBuffer = new CircularBuffer(TracerConstants.MSG_BUFFER_SZ);
    private String tracingStatus;

    private static class MessagePair {
        private String serviceName;
        private String operationName;
        private long   sequenceId;

        private TraceMessage inMessage;
        private TraceMessage outMessage;

        private MessagePair(String serviceName, String operationName, long sequenceId) {
            this.serviceName = serviceName;
            this.operationName = operationName;
            this.sequenceId = sequenceId;
        }

        public void setInMessage(TraceMessage request) {
            this.inMessage = request;
        }

        public void setOutMessage(TraceMessage response) {
            this.outMessage = response;
        }

        public TraceMessage getInMessage() {
            return inMessage;
        }

        public TraceMessage getOutMessage() {
            return outMessage;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            MessagePair that = (MessagePair) o;
            return sequenceId == that.sequenceId &&
                operationName.equals(that.operationName) &&
                serviceName.equals(that.serviceName);

        }

        @Override
        public int hashCode() {
            int result = serviceName.hashCode();
            result = 31 * result + operationName.hashCode();
            result = 31 * result + (int) (sequenceId ^ (sequenceId >>> 32));
            return result;
        }
    }

    public synchronized long saveMessage(String serviceName, String operationName,
                                         int messageFlow, MessageContext msgContext,
                                         OMElement env,
                                         long msgSequenceNumber) {
        long msgSequence = getMessageSequence(serviceName, operationName, msgContext,
                                              msgSequenceNumber);
        MessagePair tmp = new MessagePair(serviceName, operationName, msgSequence);
        Object[] objects = msgBuffer.getObjects(TracerConstants.MSG_BUFFER_SZ);
        boolean msgPairFound = false;
        for (Object object : objects) {
            MessagePair msgPair = (MessagePair) object;
            if (msgPair.equals(tmp)) {
                msgPairFound = true;
                TraceMessage msg = new TraceMessage(serviceName, operationName,
                                                    messageFlow,
                                                    msgSequence,
                                                    env);
                if (messageFlow == MessageContext.IN_FLOW ||
                    messageFlow == MessageContext.IN_FAULT_FLOW) {
                    msgPair.setInMessage(msg);
                } else if (messageFlow == MessageContext.OUT_FLOW ||
                    messageFlow == MessageContext.OUT_FAULT_FLOW) {
                    msgPair.setOutMessage(msg);
                }
            }
        }
        if (!msgPairFound) {
            TraceMessage msg = new TraceMessage(serviceName, operationName,
                                                messageFlow,
                                                msgSequence,
                                                env);
            if (messageFlow == MessageContext.IN_FLOW ||
                messageFlow == MessageContext.IN_FAULT_FLOW) {
                tmp.setInMessage(msg);
            } else if (messageFlow == MessageContext.OUT_FLOW ||
                messageFlow == MessageContext.OUT_FAULT_FLOW) {
                tmp.setOutMessage(msg);
            }
            msgBuffer.append(tmp);
        }
        return msgSequence;
    }

    public void saveTraceStatus(String onOff) {
        this.tracingStatus = onOff;
    }

    public boolean isTracingEnabled() {
        return tracingStatus != null && tracingStatus.equalsIgnoreCase("ON");
    }

    public synchronized String[] getMessages(String serviceName,
                                             String operationName,
                                             long messageSequence,
                                             MessageContext msgContext) {
        String[] responses = new String[2];
        Object[] objects = msgBuffer.getObjects(TracerConstants.MSG_BUFFER_SZ);
        MessagePair tmp = new MessagePair(serviceName, operationName, messageSequence);
        for (Object object : objects) {
            MessagePair msgPair = (MessagePair) object;
            if (msgPair.equals(tmp)) {
                TraceMessage inMessage = msgPair.getInMessage();
                if (inMessage != null && inMessage.getSoapEnvelope() != null) {
                    responses[0] = TracerUtils.getPrettyString(inMessage.getSoapEnvelope(),
                                                               msgContext);
                } else {
                    responses[0] = "No request found";
                }
                TraceMessage outMessage = msgPair.getOutMessage();
                if (outMessage != null && outMessage.getSoapEnvelope() != null) {
                    responses[1] = TracerUtils.getPrettyString(outMessage.getSoapEnvelope(),
                                                               msgContext);
                } else {
                    responses[1] = "No response found";
                }
            }
        }
        return responses;
    }

    private long getMessageSequence(String serviceName, String operationName,
                                    MessageContext msgContext, long msgSequenceNumber) {
        long msgSequence = 1;

        // check whether this is a continuation of an existing MEP
        synchronized (serviceName + operationName) {
            OperationContext operationContext = msgContext.getOperationContext();
            Object requestNumber = null;
            if (operationContext != null) {
                requestNumber = operationContext.getProperty(REQUEST_NUMBER);
            } else if (msgSequenceNumber != -1) {
                requestNumber = msgSequenceNumber;
            }
            if ((requestNumber != null) && requestNumber instanceof Long) {
                msgSequence = ((Long) requestNumber).intValue();
            } else {
                // Need to have a counter for each and operation
                Map monitoringHandlerMap =
                    (Map) msgContext.getConfigurationContext().getProperty(TRACING_MAP);

                if (monitoringHandlerMap == null) {
                    monitoringHandlerMap = new HashMap();
                    msgContext.getConfigurationContext().setProperty(TRACING_MAP,
                                                                     monitoringHandlerMap);
                }

                String key = serviceName + "." + operationName;
                Object counterInt = monitoringHandlerMap.get(key);
                if (counterInt == null) {
                    msgSequence = 0;
                } else if (counterInt instanceof Long) {
                    msgSequence = ((Long) counterInt).intValue() + 1;
                }

                monitoringHandlerMap.put(key, new Long(msgSequence));
                if (operationContext != null) {
                    operationContext.setProperty(REQUEST_NUMBER, new Long(msgSequence));
                }
            }
        }

        return msgSequence;
    }
}
