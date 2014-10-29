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
package org.wso2.carbon.statistics.module;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.util.SystemFilter;
import org.wso2.carbon.statistics.StatisticsConstants;
import org.wso2.carbon.statistics.internal.ResponseTimeCalculator;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handler to count all requests
 */
public class InOnlyMEPHandler extends AbstractHandler {
    private static final Log log = LogFactory.getLog(InOnlyMEPHandler.class);

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        if (msgContext.getEnvelope() == null) {
            return InvocationResponse.CONTINUE;
        }
        if (msgContext.getFLOW() != MessageContext.IN_FLOW &&
            msgContext.getFLOW() != MessageContext.IN_FAULT_FLOW) {
            log.error("InOnlyMEPHandler not deployed in IN/IN_FAULT flow. Flow: " +
                      msgContext.getFLOW());
            return InvocationResponse.CONTINUE;
        }
        try {
            msgContext.setProperty(StatisticsConstants.REQUEST_RECEIVED_TIME,
                                   "" + System.currentTimeMillis());
        } catch (Throwable e) { // Catching Throwable since exceptions here should not be propagated up
            log.error("Could not call InOnlyMEPHandler.invoke", e);
        }
        return InvocationResponse.CONTINUE;
    }

    @Override
    // Handle IN_ONLY operations
    public void flowComplete(MessageContext msgContext) {
        if (msgContext.getEnvelope() == null) {
            return;
        }
        AxisService axisService = msgContext.getAxisService();
        if (axisService == null ||
            SystemFilter.isFilteredOutService(axisService.getAxisServiceGroup()) ||
            axisService.isClientSide()) {
            return;
        }

        try {
            // Process Request Counter
            OperationContext opContext = msgContext.getOperationContext();
            if (opContext != null && opContext.isComplete()) {
                AxisOperation axisOp = opContext.getAxisOperation();
                if (axisOp != null && axisOp.isControlOperation()) {
                    return;
                }
                if (axisOp != null) {
                    String mep = axisOp.getMessageExchangePattern();
                    if (mep != null &&
                        (mep.equals(WSDL2Constants.MEP_URI_IN_ONLY) ||
                         mep.equals(WSDL2Constants.MEP_URI_ROBUST_IN_ONLY))) {

                        // Increment operation counter
                        final AxisOperation axisOperation = msgContext.getAxisOperation();
                        if (axisOperation != null) {
                            Parameter operationParameter =
                                    axisOperation.getParameter(StatisticsConstants.IN_OPERATION_COUNTER);
                            if (operationParameter != null) {
                                ((AtomicInteger) operationParameter.getValue()).incrementAndGet();
                            } else {
                                log.error(StatisticsConstants.IN_OPERATION_COUNTER +
                                          " has not been set for operation " +
                                          axisService.getName() + "." + axisOperation.getName());
                                return;
                            }

                            // Calculate response times
                            try {
                                ResponseTimeCalculator.calculateResponseTimes(msgContext);
                            } catch (AxisFault axisFault) {
                                log.error("Cannot compute response times", axisFault);
                            }
                        }

                        // Increment global counter
                        Parameter globalRequestCounter =
                                msgContext.getParameter(StatisticsConstants.GLOBAL_REQUEST_COUNTER);
                        ((AtomicInteger) globalRequestCounter.getValue()).incrementAndGet();

                        updateCurrentInvocationGlobalStatistics(msgContext);
                    }
                }
            }
        } catch (Throwable e) {  // Catching Throwable since exceptions here should not be propagated up
            log.error("Could not call InOnlyMEPHandler.flowComplete", e);
        }
    }

    /**
     * This method is used to update current request statistic
     *
     * @param msgContext
     */
    private void updateCurrentInvocationGlobalStatistics(MessageContext msgContext) {
        //Set request count 1
        msgContext.setProperty(StatisticsConstants.GLOBAL_CURRENT_REQUEST_COUNTER,1);

        //Set response count 0
        msgContext.setProperty(StatisticsConstants.GLOBAL_CURRENT_RESPONSE_COUNTER,0);

        //Set fault count 0
        msgContext.setProperty(StatisticsConstants.GLOBAL_CURRENT_FAULT_COUNTER,0);

    }
}
