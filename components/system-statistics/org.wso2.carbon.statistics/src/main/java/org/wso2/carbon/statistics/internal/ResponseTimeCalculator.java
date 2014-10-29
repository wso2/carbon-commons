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
package org.wso2.carbon.statistics.internal;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.statistics.StatisticsConstants;
import org.wso2.carbon.statistics.services.SystemStatisticsUtil;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility for calculating respone times
 */
public class ResponseTimeCalculator {

    private static Log log = LogFactory.getLog(ResponseTimeCalculator.class);

    public static void calculateResponseTimes(MessageContext messageContext)
        throws AxisFault {

        OperationContext opctx = messageContext.getOperationContext();
        MessageContext inMsgCtx;
        if(opctx != null){
             inMsgCtx = opctx.getMessageContext(WSDL2Constants.MESSAGE_LABEL_IN);
        } else {
            inMsgCtx = (MessageContext) messageContext.getProperty(MessageContext.IN_MESSAGE_CONTEXT);
        }
        if(inMsgCtx == null){
            inMsgCtx = messageContext;
        }
        Object receivedTime =
            inMsgCtx.getProperty(StatisticsConstants.REQUEST_RECEIVED_TIME);
        if (receivedTime == null) {
            log.error(StatisticsConstants.REQUEST_RECEIVED_TIME +
                      " is null in the IN MessageContext");
            return;
        }
        long responseTime =
            System.currentTimeMillis() - Long.parseLong(receivedTime.toString());

        updateCurrentInvocationStatistic(messageContext,responseTime);

        // Handle global response time
        Parameter globalReqCounterParam =
            inMsgCtx.getParameter(StatisticsConstants.GLOBAL_REQUEST_COUNTER);
        int globalReqCount = ((AtomicInteger) globalReqCounterParam.getValue()).get();

        Parameter processor =
            inMsgCtx.getParameter(StatisticsConstants.RESPONSE_TIME_PROCESSOR);
        if (processor != null) {
            ((ResponseTimeProcessor) processor.getValue()).addResponseTime(responseTime,
                                                                           globalReqCount);
        }

        if (opctx != null) {
            // Handle operation response time
            AxisOperation axisOperation = opctx.getAxisOperation();
            if (axisOperation != null) {
                Parameter parameter =
                    axisOperation.getParameter(StatisticsConstants.OPERATION_RESPONSE_TIME_PROCESSOR);
                Parameter opReqCounterParam =
                    axisOperation.getParameter(StatisticsConstants.IN_OPERATION_COUNTER);
                int opReqCount = ((AtomicInteger) opReqCounterParam.getValue()).get();
                if (parameter != null) {
                    ((ResponseTimeProcessor) parameter.getValue()).addResponseTime(responseTime,
                                                                                   opReqCount);
                } else {
                    log.error(StatisticsConstants.OPERATION_RESPONSE_TIME_PROCESSOR +
                              " has not been set for operation " +
                              axisOperation.getAxisService().getName() +
                              "." + axisOperation.getName());
                    return;
                }

            }

            // Handle service response time
            AxisService axisService = messageContext.getAxisService();
            if (axisService != null) {
                Parameter parameter =
                    axisService.getParameter(StatisticsConstants.SERVICE_RESPONSE_TIME_PROCESSOR);
                int serviceRequestCount = new SystemStatisticsUtil().getServiceRequestCount(axisService);
                ((ResponseTimeProcessor) parameter.getValue()).addResponseTime(responseTime,
                                                                               serviceRequestCount);
            }
        }
    }

    private static void updateCurrentInvocationStatistic(MessageContext messageContext,
                                                         long responseTime) throws AxisFault {
        messageContext.setProperty(StatisticsConstants.GLOBAL_CURRENT_INVOCATION_RESPONSE_TIME,responseTime);

        if (messageContext.getAxisOperation() != null) {
            Parameter operationResponseTimeParam = new Parameter();
            operationResponseTimeParam.setName(StatisticsConstants.OPERATION_RESPONSE_TIME);
            operationResponseTimeParam.setValue(responseTime);
            messageContext.getAxisOperation().addParameter(operationResponseTimeParam);
        }

        if (messageContext.getAxisService() != null) {
            Parameter serviceResponseTimeParam = new Parameter();
            serviceResponseTimeParam.setName(StatisticsConstants.SERVICE_RESPONSE_TIME);
            serviceResponseTimeParam.setValue(responseTime);
            messageContext.getAxisService().addParameter(serviceResponseTimeParam);
        }
    }
}
