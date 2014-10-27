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
 * Handler to count all Faults
 */
public class FaultHandler extends AbstractHandler {
    private static Log log = LogFactory.getLog(FaultHandler.class);

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        MessageContext inMsgCtx =
            (MessageContext) msgContext.getProperty(MessageContext.IN_MESSAGE_CONTEXT);
        // inMsgCtx.getEnvelope() == null would indicate that the incoming XML was malformed (in most cases)
        if(msgContext.getEnvelope() == null || inMsgCtx.getEnvelope() == null){
            return InvocationResponse.CONTINUE;
        }
        try {
            AxisService axisService = msgContext.getAxisService();
            if(axisService == null) {
                updateStatistics(msgContext);
                return InvocationResponse.CONTINUE;
            } else if (SystemFilter.isFilteredOutService(axisService.getAxisServiceGroup()) ||
                axisService.isClientSide()) {
                return InvocationResponse.CONTINUE;
            }

            // Increment the operation fault count
            AxisOperation axisOperation = msgContext.getAxisOperation();
            if (axisOperation != null) {
                String mep = axisOperation.getMessageExchangePattern();
                if (mep != null &&
                    (mep.equals(WSDL2Constants.MEP_URI_OUT_IN) ||
                        mep.equals(WSDL2Constants.MEP_URI_OUT_ONLY) ||
                        mep.equals(WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN))) { // If this ConfigurationContext is used for sending messages out, do not change the stats
                    return InvocationResponse.CONTINUE;
                }
                // Process operation fault count
                Parameter parameter = axisOperation.getParameter(StatisticsConstants.OPERATION_FAULT_COUNTER);
                if (parameter != null) {
                    ((AtomicInteger) parameter.getValue()).incrementAndGet();
                } else {
                    AtomicInteger counter = new AtomicInteger(0);
                    counter.incrementAndGet();
                    parameter = new Parameter();
                    parameter.setName(StatisticsConstants.OPERATION_FAULT_COUNTER);
                    parameter.setValue(counter);
                    axisOperation.addParameter(parameter);
                }

                // Process operation request count
                Parameter operationParameter =
                    axisOperation.getParameter(StatisticsConstants.IN_OPERATION_COUNTER);

                if (operationParameter != null) {
                    ((AtomicInteger) operationParameter.getValue()).incrementAndGet();

                } else {
                    AtomicInteger operationCounter = new AtomicInteger(1);
                    operationParameter = new Parameter();
                    operationParameter.setName(StatisticsConstants.IN_OPERATION_COUNTER);
                    operationParameter.setValue(operationCounter);
                    axisOperation.addParameter(operationParameter);
                }
            }
            updateStatistics(msgContext);
        } catch (Throwable e) {
            log.error("Could not call FaultHandler.invoke", e);
         }
        return InvocationResponse.CONTINUE;
    }

    private void updateStatistics(MessageContext msgContext) throws AxisFault {
        // Process System Request count
        Parameter globalRequestCounter =
            msgContext.getParameter(StatisticsConstants.GLOBAL_REQUEST_COUNTER);
        ((AtomicInteger) globalRequestCounter.getValue()).incrementAndGet();

        // Increment the global fault count
        Parameter globalFaultCounter =
                msgContext.getParameter(StatisticsConstants.GLOBAL_FAULT_COUNTER);
        ((AtomicInteger) globalFaultCounter.getValue()).incrementAndGet();

        updateCurrentInvocationGlobalStatistics(msgContext);

        // Calculate response times
        ResponseTimeCalculator.calculateResponseTimes(msgContext);
    }




    /**
     *  This method is used to update current request statistic , this statistics useful for bam to define SLAs using CEP
     *          RequestCount =1
     *         ResponseCount = 0
     *         FaultCount = 1
     * @param msgContext
     */
    private void updateCurrentInvocationGlobalStatistics(MessageContext msgContext) throws AxisFault{

        //Set request count 1
        msgContext.setProperty(StatisticsConstants.GLOBAL_CURRENT_REQUEST_COUNTER,1);

        //Set response count 0
        msgContext.setProperty(StatisticsConstants.GLOBAL_CURRENT_RESPONSE_COUNTER,0);

        //Set fault count 1
        msgContext.setProperty(StatisticsConstants.GLOBAL_CURRENT_FAULT_COUNTER,1);

    }
}
