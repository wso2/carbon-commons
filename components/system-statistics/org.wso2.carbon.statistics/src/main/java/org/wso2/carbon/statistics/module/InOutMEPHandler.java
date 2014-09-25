/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * Handler to count all responses from services
 */
public class InOutMEPHandler extends AbstractHandler {
    private static final Log log = LogFactory.getLog(InOutMEPHandler.class);

    public InvocationResponse invoke(MessageContext outMsgContext) throws AxisFault {
        if(outMsgContext.getEnvelope() == null){
            return InvocationResponse.CONTINUE;
        }
        if (outMsgContext.getFLOW() != MessageContext.OUT_FLOW &&
            outMsgContext.getFLOW() != MessageContext.OUT_FAULT_FLOW) {
            log.error("InOutMEPHandler not deployed in OUT/OUT_FAULT flow. Flow: " +
                      outMsgContext.getFLOW());
            return InvocationResponse.CONTINUE;
        }
        try {
            AxisService axisService = outMsgContext.getAxisService();
            if(axisService == null) {
               updateStatistics(outMsgContext);
               return InvocationResponse.CONTINUE;
           } else if (SystemFilter.isFilteredOutService(axisService.getAxisServiceGroup()) ||
               axisService.isClientSide()) {
               return InvocationResponse.CONTINUE;
           }

            final AxisOperation axisOperation = outMsgContext.getAxisOperation();
            if(axisOperation != null && axisOperation.isControlOperation()){
                return InvocationResponse.CONTINUE;
            }
            if (axisOperation != null) {
                String mep = axisOperation.getMessageExchangePattern();
                if (mep != null &&
                    (mep.equals(WSDL2Constants.MEP_URI_OUT_IN) ||
                        mep.equals(WSDL2Constants.MEP_URI_OUT_ONLY) ||
                        mep.equals(WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN))) { // If this ConfigurationContext is used for sending messages out, do not change the stats
                    return InvocationResponse.CONTINUE;
                }
                // Process operation request count
                Parameter inOpCounter =
                    axisOperation.getParameter(StatisticsConstants.IN_OPERATION_COUNTER);
                if (inOpCounter != null) {
                    ((AtomicInteger) inOpCounter.getValue()).incrementAndGet();
                } else {
                    log.error(StatisticsConstants.IN_OPERATION_COUNTER +
                              " has not been set for operation " +
                              axisService.getName() + "." + axisOperation.getName());
                    return InvocationResponse.CONTINUE;
                }

                // Process operation response count
                Parameter outOpCounter =
                    axisOperation.getParameter(StatisticsConstants.OUT_OPERATION_COUNTER);
                if (outOpCounter != null) {
                    ((AtomicInteger) outOpCounter.getValue()).incrementAndGet();
                } else {
                    log.error(StatisticsConstants.OUT_OPERATION_COUNTER +
                              " has not been set for operation " +
                              axisService.getName() + "." + axisOperation.getName());
                    return InvocationResponse.CONTINUE;
                }
            }
            updateStatistics(outMsgContext);
        } catch (Throwable e) {
            log.error("Could not call InOutMEPHandler.invoke", e);
         }
        return InvocationResponse.CONTINUE;
    }


    private void updateStatistics(MessageContext outMsgContext) throws AxisFault {
        // Process System Request count
        Parameter globalRequestCounter =
            outMsgContext.getParameter(StatisticsConstants.GLOBAL_REQUEST_COUNTER);
        ((AtomicInteger) globalRequestCounter.getValue()).incrementAndGet();

        // Process System Response count
        Parameter globalResponseCounter =
            outMsgContext.getParameter(StatisticsConstants.GLOBAL_RESPONSE_COUNTER);
        ((AtomicInteger) globalResponseCounter.getValue()).incrementAndGet();

        updateCurrentInvocationGlobalStatistics(outMsgContext);

        // Calculate response times
        ResponseTimeCalculator.calculateResponseTimes(outMsgContext);
    }

    /**
     *  This method is used to update current request statistic , this statistics useful for bam to define SLAs using CEP
     *          RequestCount =1
     *         ResponseCount = 1
     *         FaultCount = 0
     * @param outMsgContext
     */
    private void updateCurrentInvocationGlobalStatistics(MessageContext outMsgContext) throws AxisFault {

        //Set request count 1
        outMsgContext.setProperty(StatisticsConstants.GLOBAL_CURRENT_REQUEST_COUNTER,1);

        //Set response count 1
        outMsgContext.setProperty(StatisticsConstants.GLOBAL_CURRENT_RESPONSE_COUNTER,1);

        //Set fault count 0
        outMsgContext.setProperty(StatisticsConstants.GLOBAL_CURRENT_FAULT_COUNTER,0);
    }
}
