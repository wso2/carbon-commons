/*
 * Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
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
 *
 */
package org.wso2.carbon.tracer.module.handler;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.wsdl.WSDLConstants;
import org.wso2.carbon.core.util.SystemFilter;
import org.wso2.carbon.tracer.TracerConstants;
import org.wso2.carbon.tracer.module.TraceFilter;
import org.wso2.carbon.tracer.module.TracePersister;
import org.wso2.carbon.tracer.service.MessageInfo;
import org.wso2.carbon.utils.logging.CircularBuffer;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

/**
 * This handler gets called just before the message is written out to the transport sender.
 * If this handler discovers that the message has not been dispatched to a service due to some
 * reason, it will take on the responsibility of saving the request and response messages, under
 * the unknownService.unknownOperation.
 */
public class TracingMessageOutObservationHandler extends AbstractTracingHandler {

    public InvocationResponse invoke(MessageContext outMsgCtx) throws AxisFault {
        AxisService axisService = outMsgCtx.getAxisService();

        if(axisService != null && axisService.isClientSide()) {
            return InvocationResponse.CONTINUE;
        }

        // Do not trace messages from admin services
        if (axisService != null && axisService.getParent() != null) {
            if (SystemFilter.isFilteredOutService(axisService.getAxisServiceGroup())) {
                return InvocationResponse.CONTINUE;
            }
        }

        // If the Axis service is not found, still we should store the request and response
        // Axis service may not be found if a request is sent to a non-existing service, or if
        // security validations fail before dispatching
        if (axisService == null) {
            // since the OperationContext will be null in this case, we cannot get the in
            // MessageContext from the OperationContext. Hence, we will use the in MessageContext
            // which is stored as a property in the out MessageContext
            MessageContext inMsgCtx =
                (MessageContext) outMsgCtx.getProperty(MessageContext.IN_MESSAGE_CONTEXT);

            String serviceName = "unknownService";
            String operationName = "unknowOperation";

            // Store the request
            long msgSeq = storeMessage(serviceName, operationName, inMsgCtx, -1);
            appendMessage(outMsgCtx.getConfigurationContext(),
                          serviceName, operationName, msgSeq);
            // Store the response
            storeMessage(serviceName, operationName, outMsgCtx, msgSeq);
            return InvocationResponse.CONTINUE;
        }

        ConfigurationContext configCtx = outMsgCtx.getConfigurationContext();
        TraceFilter traceFilter =
                (TraceFilter) configCtx.getAxisConfiguration().
                                getParameter(TracerConstants.TRACE_FILTER_IMPL).getValue();
        if (traceFilter.isFilteredOut(outMsgCtx)) {
            return InvocationResponse.CONTINUE;
        }

        if ((outMsgCtx.getAxisOperation() != null) &&
            (outMsgCtx.getAxisOperation().getName() != null)) {
            String operationName =
                    outMsgCtx.getAxisOperation().getName().getLocalPart();
            String serviceName = axisService.getName();
            storeMessage(serviceName, operationName, outMsgCtx, -1);
        }
        return InvocationResponse.CONTINUE;
    }
}
