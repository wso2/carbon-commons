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
import org.wso2.carbon.core.util.SystemFilter;
import org.wso2.carbon.tracer.TracerConstants;
import org.wso2.carbon.tracer.module.TraceFilter;
import org.wso2.carbon.tracer.module.TracePersister;

/**
 * This handler gets called after the service and operation have been found. It will save the message
 * with the service and operation name.
 */
public class TracingInPostDispatchHandler extends AbstractTracingHandler {

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        AxisService axisService = msgContext.getAxisService();

        if (axisService == null || axisService.isClientSide()) {
            return InvocationResponse.CONTINUE;
        }

        // Do not trace messages from admin services
        if (axisService.getParent() != null) {
            if (SystemFilter.isFilteredOutService(axisService.getAxisServiceGroup())) {
                return InvocationResponse.CONTINUE;
            }
        }

        ConfigurationContext configCtx = msgContext.getConfigurationContext();
        TraceFilter traceFilter =
            (TraceFilter) configCtx.getAxisConfiguration().
                getParameter(TracerConstants.TRACE_FILTER_IMPL).getValue();
        if (traceFilter.isFilteredOut(msgContext)) {
            return InvocationResponse.CONTINUE;
        }

        if ((msgContext.getAxisOperation() != null) &&
            (msgContext.getAxisOperation().getName() != null)) {
            String operationName =
                msgContext.getAxisOperation().getName().getLocalPart();
            String serviceName = axisService.getName();

            // Add the message id to the CircularBuffer.
            // We need to track only the IN_FLOW msg, since with that sequence number,
            // we can retrieve all other related messages from the persister.
            appendMessage(msgContext.getConfigurationContext(),
                          serviceName, operationName,
                          storeMessage(serviceName, operationName, msgContext));
        }
        return InvocationResponse.CONTINUE;
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
                                MessageContext msgCtxt) {
        TracePersister tracePersister =
            (TracePersister) msgCtxt.getConfigurationContext().getAxisConfiguration()
                                    .getParameter(TracerConstants.TRACE_PERSISTER_IMPL).getValue();
        return tracePersister.saveMessage(serviceName, operationName,
                                          msgCtxt.getFLOW(), msgCtxt,
                                          (OMElement) msgCtxt.getProperty(TracerConstants.TEMP_IN_ENVELOPE),
                                          -1); // Use the temp envelope
    }
}
