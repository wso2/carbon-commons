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
package org.wso2.carbon.tracer.service;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.Loader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.tracer.TracerConstants;
import org.wso2.carbon.tracer.module.MemoryBasedTracePersister;
import org.wso2.carbon.tracer.module.TracePersister;
import org.wso2.carbon.utils.logging.CircularBuffer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
@SuppressWarnings("unused")
public class TracerAdmin extends AbstractAdmin {

    private static Log log = LogFactory.getLog(TracerAdmin.class);

    public TracerServiceInfo getMessages(int numberOfMessages, String filter) throws AxisFault {

        ConfigurationContext configContext = getConfigContext();
        AxisConfiguration axisConfiguration = configContext.getAxisConfiguration();
        CircularBuffer<MessageInfo> msgSeqBuff = getMessageSequenceBuffer();
        List<MessageInfo> messageObjs;
        TracerServiceInfo tracerServiceInfo = new TracerServiceInfo();
        AxisModule axisModule = axisConfiguration.getModule(TracerConstants.WSO2_TRACER);

        if (axisModule == null) {
            throw new AxisFault(TracerConstants.WSO2_TRACER + " module is not available");
        }
        TracePersister tracePersister = getTracePersister();
        tracerServiceInfo.setTracePersister(tracePersister.getClass().getName());
        if (tracePersister.isTracingEnabled()) {
            if (!axisConfiguration.isEngaged(axisModule)) {
                axisConfiguration.engageModule(axisModule);
            }
            tracerServiceInfo.setFlag("ON");
        } else {
            if (axisConfiguration.isEngaged(axisModule)) {
                axisConfiguration.disengageModule(axisModule);
            }
            tracerServiceInfo.setFlag("OFF");
        }
        if (msgSeqBuff == null) {
            tracerServiceInfo.setEmpty(true);
            return tracerServiceInfo;
        } else {
            messageObjs = msgSeqBuff.get(numberOfMessages);

            if (messageObjs.isEmpty()) {
                tracerServiceInfo.setEmpty(true);
                return tracerServiceInfo;

            } else {
                ArrayList<MessageInfo> msgInfoList = new ArrayList<MessageInfo>();
                boolean filterProvided = (filter != null && (filter = filter.trim()).length() != 0);
                tracerServiceInfo.setFilter(filterProvided);

                for (MessageInfo mi : messageObjs) {
                    if (filterProvided) {
                        MessagePayload miPayload = getMessage(mi.getServiceId(),
                                                              mi.getOperationName(),
                                                              mi.getMessageSequence());
                        String req = miPayload.getRequest();
                        if (req == null) {
                            req = "";
                        }
                        String resp = miPayload.getResponse();
                        if (resp == null) {
                            resp = "";
                        }
                        if (req.toUpperCase().contains(filter.toUpperCase())
                            || resp.toUpperCase().contains(filter.toUpperCase())) {
                            msgInfoList.add(mi);
                        }
                    } else {
                        msgInfoList.add(mi);
                    }
                }

                if (filterProvided) {
                    tracerServiceInfo.setFilterString(filter);
                    if (msgInfoList.size() == 0) {
                        tracerServiceInfo.setEmpty(true);
                        return tracerServiceInfo;
                    }
                }

                Collections.reverse(msgInfoList);
                MessageInfo lastMessageInfo = msgInfoList.get(0);
                tracerServiceInfo.setMessageInfo(
                        msgInfoList.toArray(new MessageInfo[msgInfoList.size()]));
                MessagePayload lastMsg = getMessage(lastMessageInfo.getServiceId(),
                                                    lastMessageInfo.getOperationName(),
                                                    lastMessageInfo.getMessageSequence());
                tracerServiceInfo.setLastMessage(lastMsg);
                tracerServiceInfo.setEmpty(false);
            }
        }
        return tracerServiceInfo;
    }

    /**
     * @param flag; support ON or OFF.
     * @return The information about the Tracer service
     * @throws AxisFault If the tracer module is not found
     */
    public TracerServiceInfo setMonitoring(String flag) throws AxisFault {
        if (!flag.equalsIgnoreCase("ON") && !flag.equalsIgnoreCase("OFF")) {
            throw new RuntimeException("IllegalArgument for monitoring status. Only 'ON' and 'OFF' is allowed");
        }
        TracerServiceInfo tracerServiceInfo = new TracerServiceInfo();
        ConfigurationContext configurationContext = getConfigContext();
        AxisConfiguration axisConfiguration = configurationContext.getAxisConfiguration();
        AxisModule axisModule = axisConfiguration.getModule(TracerConstants.WSO2_TRACER);

        if (axisModule == null) {
            throw new RuntimeException(TracerAdmin.class.getName() + " " +
                                       TracerConstants.WSO2_TRACER + " is not available");
        }

        if (flag.equalsIgnoreCase("ON")) {
            if (!axisConfiguration.isEngaged(axisModule.getName())) {
                try {
                    axisConfiguration.engageModule(axisModule);
                } catch (AxisFault axisFault) {
                    log.error(axisFault);
                    throw new RuntimeException(axisFault);
                }
            }
        } else if (flag.equalsIgnoreCase("OFF")) {
            if (axisConfiguration.isEngaged(axisModule.getName())) {
                axisConfiguration.disengageModule(axisModule);
                configurationContext.removeProperty(TracerConstants.MSG_SEQ_BUFFER);
            }
        }
        TracePersister tracePersister = getTracePersister();
        tracePersister.saveTraceStatus(flag);
        tracerServiceInfo.setEmpty(true);
        tracerServiceInfo.setFlag(flag);
        tracerServiceInfo.setTracePersister(tracePersister.getClass().getName());

        return tracerServiceInfo;
    }

    public MessagePayload getMessage(String serviceId,
                                     String operationName,
                                     long messageSequence) throws AxisFault {
        TracePersister tracePersisterImpl = getTracePersister();
        MessagePayload messagePayload = new MessagePayload();
        if (tracePersisterImpl != null) {
            String[] respArray =
                    tracePersisterImpl.getMessages(serviceId, operationName,
                                                   messageSequence,
                                                   MessageContext.getCurrentMessageContext());
            if (respArray[0] != null) {
                messagePayload.setRequest(respArray[0]);
            }
            if (respArray[1] != null) {
                messagePayload.setResponse(respArray[1]);
            }
        } else {
            String message = "Tracer service encountered an error ";
            log.error(message);
            throw new RuntimeException(message);
        }

        return messagePayload;

    }

    private TracePersister getTracePersister() throws AxisFault {
        Parameter tracePersisterParam =
                getAxisConfig().getParameter(TracerConstants.TRACE_PERSISTER_IMPL);
        return getTracePersister(tracePersisterParam);
    }

    public void clearAllSoapMessages(){
        CircularBuffer<MessageInfo> msgSeqBuff = getMessageSequenceBuffer();
        if (msgSeqBuff != null) {
            msgSeqBuff.clear();
        }
    }

    private CircularBuffer<MessageInfo> getMessageSequenceBuffer() {
        Object bufferProp = getConfigContext().getProperty(TracerConstants.MSG_SEQ_BUFFER);
        if (bufferProp instanceof CircularBuffer) {
            return (CircularBuffer<MessageInfo>) bufferProp;
        }
        else {
            return null;
        }
    }

    private TracePersister getTracePersister(Parameter tracePersisterParam) throws AxisFault {
        TracePersister tracePersister = null;
        if (tracePersisterParam != null) {
            Object tracePersisterImplObj = tracePersisterParam.getValue();
            if (tracePersisterImplObj instanceof TracePersister) {
                tracePersister = (TracePersister) tracePersisterImplObj;
            } else if (tracePersisterImplObj instanceof String) {
                //This will need in TestSuite
                try {
                    tracePersister =
                            (TracePersister) Loader
                                    .loadClass(((String) tracePersisterImplObj).trim())
                                    .newInstance();
                } catch (Exception e) {
                    String message = "Cannot instatiate TracePersister ";
                    log.error(message, e);
                    throw new RuntimeException(message, e);
                }
            }
        } else {
            return new MemoryBasedTracePersister(); // The default is the MemoryBasedTRacePersister
        }
        return tracePersister;
    }

}
