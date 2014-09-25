/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.tracer.module.handler;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.wso2.carbon.tracer.TracerConstants;
import org.wso2.carbon.tracer.module.TracePersister;
import org.wso2.carbon.tracer.service.MessageInfo;
import org.wso2.carbon.utils.logging.CircularBuffer;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * The abstract class which provides some basic operations for Tracing handlers which extend this
 * class
 */
public abstract class AbstractTracingHandler extends AbstractHandler {

    /**
     * Appends SOAP message metadata to a message buffer
     *
     * @param configCtx  The server ConfigurationContext
     * @param serviceName  The service name
     * @param operationName  The operation name
     * @param msgSeq The message sequence. Use -1 if unknown.
     */
    protected void appendMessage(ConfigurationContext configCtx,
                                 String serviceName,
                                 String operationName,
                                 Long msgSeq) {
        CircularBuffer buffer =
            (CircularBuffer) configCtx.getProperty(TracerConstants.MSG_SEQ_BUFFER);
        if (buffer == null){
            buffer = new CircularBuffer(TracerConstants.MSG_BUFFER_SZ);
            configCtx.setProperty(TracerConstants.MSG_SEQ_BUFFER, buffer);
        }
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date());
        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setMessageSequence(msgSeq);
        messageInfo.setOperationName(operationName);
        messageInfo.setServiceId(serviceName);
        messageInfo.setTimestamp(cal);
        buffer.append(messageInfo);
    }

    /**
     * Store the received message
     *
     * @param operationName operationName
     * @param serviceName   serviceName
     * @param msgCtxt       msgCtxt
     * @return the sequence of the message stored with respect to the operation
     *         in the service
     */
    protected long storeMessage(String serviceName,
                                String operationName,
                                MessageContext msgCtxt,
                                long msgSequenceNumber) {
        TracePersister tracePersister =
            (TracePersister) msgCtxt.getConfigurationContext().
                getAxisConfiguration().getParameter(TracerConstants.TRACE_PERSISTER_IMPL).getValue();
        return tracePersister.saveMessage(serviceName, operationName,
                                          msgCtxt.getFLOW(),
                                          msgCtxt,
                                          msgCtxt.getEnvelope().cloneOMElement(), msgSequenceNumber);
    }
}
