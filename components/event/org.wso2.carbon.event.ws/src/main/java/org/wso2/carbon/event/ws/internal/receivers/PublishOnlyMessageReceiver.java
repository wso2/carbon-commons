/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.event.ws.internal.receivers;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.receivers.AbstractMessageReceiver;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.ws.internal.builders.utils.BuilderUtils;
import org.wso2.carbon.event.ws.internal.WSEventBrokerHolder;
import org.wso2.carbon.event.ws.internal.exception.WSEventException;
import org.wso2.carbon.event.ws.internal.util.EventBrokerUtils;
import org.wso2.carbon.event.ws.internal.util.EventingConstants;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.event.core.Message;
import org.wso2.carbon.event.core.exception.EventBrokerException;

@Deprecated
public class PublishOnlyMessageReceiver extends AbstractMessageReceiver {

    private static final Log log = LogFactory.getLog(PublishOnlyMessageReceiver.class);

    private boolean isEnabled(MessageContext mc, String operation) {
        if (mc.getAxisService() != null) {
            String operationValue =
                    (String) mc.getAxisService().getParameterValue(operation);
            return operationValue == null || !operationValue.toLowerCase().equals(
                    Boolean.toString(false));
        }
        return true;
    }

    public final void invokeBusinessLogic(MessageContext mc) throws AxisFault {
        try {
            handleEvent(mc);
        } catch (WSEventException e) {
            log.error("An exception occured. Unable to Process Request", e);
            SOAPEnvelope soapEnvelope = BuilderUtils.genFaultResponse(
                    EventingConstants.WSE_FAULT_CODE_RECEIVER, "EventSourceUnableToProcess",
                    "An exception occured. Unable to Process Request ", "", mc.isSOAP11());
            dispatchResponse(soapEnvelope, EventingConstants.WSA_FAULT, mc, true);
        }
    }


    protected void handleEvent(MessageContext mc) throws AxisFault, WSEventException {
        String topic = EventBrokerUtils.extractTopicFromMessage(mc);
        try {
            Message message = new Message();
            message.setMessage(mc.getEnvelope().getBody().getFirstElement());
            getBrokerService().publishRobust(message, topic);
        } catch (EventBrokerException e) {
            throw new WSEventException("Can not publish the message : " + e.getMessage(), e);
        }
    }


    private EventBroker getBrokerService() {
        return WSEventBrokerHolder.getInstance().getEventBroker();
    }

    /**
     * Dispatch the message to the target endpoint
     *
     * @param soapEnvelope   Soap Enevlop with message
     * @param responseAction WSE action for the response
     * @param mc             Message Context
     * @param isFault        Whether a Fault message must be sent
     * @throws AxisFault Thrown by the axis2 engine.
     */
    private void dispatchResponse(SOAPEnvelope soapEnvelope, String responseAction,
                                  MessageContext mc, boolean isFault) throws AxisFault {
        MessageContext rmc = MessageContextBuilder.createOutMessageContext(mc);
        rmc.getOperationContext().addMessageContext(rmc);
        replicateState(mc);
        rmc.setEnvelope(soapEnvelope);
        rmc.setWSAAction(responseAction);
        rmc.setSoapAction(responseAction);
        if (isFault) {
            AxisEngine.sendFault(rmc);
        } else {
            AxisEngine.send(rmc);
        }
    }
}
