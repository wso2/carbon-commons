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
package org.wso2.carbon.statistics.services;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.statistics.StatisticsConstants;
import org.wso2.carbon.statistics.internal.ResponseTimeProcessor;
import org.wso2.carbon.statistics.services.util.OperationStatistics;
import org.wso2.carbon.statistics.services.util.ServiceStatistics;
import org.wso2.carbon.statistics.services.util.SystemStatistics;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This util is used for obtaining System statistics. It will be called by StatisticsAdmin service
 * as well as other components.
 */
public class SystemStatisticsUtil {

     public SystemStatistics getSystemStatistics(MessageContext context) throws AxisFault{
        SystemStatistics systemStatistics = new SystemStatistics(context);
        return systemStatistics;
    }

    public SystemStatistics getSystemStatistics(AxisConfiguration axisConfiguration) throws AxisFault{
        SystemStatistics systemStatistics = new SystemStatistics(axisConfiguration);
        return systemStatistics;
    }

    public ServiceStatistics getServiceStatistics(AxisService axisService) throws AxisFault {
        ServiceStatistics serviceStatistics = new ServiceStatistics();
        serviceStatistics.setAvgResponseTime(getAvgServiceResponseTime(axisService));
        serviceStatistics.setTotalFaultCount(getServiceFaultCount(axisService));
        serviceStatistics.setMaxResponseTime(getMaxServiceResponseTime(axisService));
        serviceStatistics.setMinResponseTime(getMinServiceResponseTime(axisService));
        serviceStatistics.setTotalRequestCount(getServiceRequestCount(axisService));
        serviceStatistics.setTotalResponseCount(getServiceResponseCount(axisService));
        return serviceStatistics;
    }


    public OperationStatistics getOperationStatistics(AxisOperation axisOp) throws AxisFault {
        OperationStatistics operationStatistics = new OperationStatistics();
        operationStatistics.setAvgResponseTime(getAvgOperationResponseTime(axisOp));
        operationStatistics.setTotalFaultCount(getOperationFaultCount(axisOp));
        operationStatistics.setMaxResponseTime(getMaxOperationResponseTime(axisOp));
        operationStatistics.setMinResponseTime(getMinOperationResponseTime(axisOp));
        operationStatistics.setTotalRequestCount(getOperationRequestCount(axisOp));
        operationStatistics.setTotalResponseCount(getOperationResponseCount(axisOp));

        return operationStatistics;
    }

    public int getTotalSystemRequestCount(AxisConfiguration axisConfig) throws AxisFault {
        return getSystemStatisticsCount(axisConfig, StatisticsConstants.GLOBAL_REQUEST_COUNTER);
    }

    public int getSystemFaultCount(AxisConfiguration axisConfig) throws AxisFault {
        return getSystemStatisticsCount(axisConfig, StatisticsConstants.GLOBAL_FAULT_COUNTER);
    }

    public int getSystemResponseCount(AxisConfiguration axisConfig) throws AxisFault {
        return getSystemStatisticsCount(axisConfig, StatisticsConstants.GLOBAL_RESPONSE_COUNTER);
    }

    private int getSystemStatisticsCount(AxisConfiguration axisConfig,
                                         String parameterName) {
        Parameter globalCounter =
                axisConfig.getParameter(parameterName);
        return ((AtomicInteger) globalCounter.getValue()).get();
    }


    private long getResponseTime(AxisService axisService) {
        Parameter responseTimeParameter = axisService.getParameter(
                StatisticsConstants.SERVICE_RESPONSE_TIME);
        if (responseTimeParameter != null) {
            Object value = responseTimeParameter.getValue();
            if (value instanceof Long) {
                return (Long) value;
            }
        }
        return 0;
    }



    private long getResponseTime(AxisOperation axisOp) {
        Parameter responseTimeParameter = axisOp.getParameter(
                StatisticsConstants.OPERATION_RESPONSE_TIME);
        if (responseTimeParameter != null) {
            Object value = responseTimeParameter.getValue();
            if (value instanceof Long) {
                return (Long) value;
            }
        }
        return 0;
    }


    public double getAvgSystemResponseTime(AxisConfiguration axisConfig) {
        Parameter processor =
                axisConfig.getParameter(StatisticsConstants.RESPONSE_TIME_PROCESSOR);
        if (processor != null) {
            Object value = processor.getValue();
            if (value instanceof ResponseTimeProcessor) {
                return ((ResponseTimeProcessor) value).getAvgResponseTime();
            }
        }
        return 0;
    }

    public long getMaxSystemResponseTime(AxisConfiguration axisConfig) {
        Parameter processor =
                axisConfig.getParameter(StatisticsConstants.RESPONSE_TIME_PROCESSOR);
        if (processor != null) {
            Object value = processor.getValue();
            if (value instanceof ResponseTimeProcessor) {
                return ((ResponseTimeProcessor) value).getMaxResponseTime();
            }
        }
        return 0;
    }

    public long getMinSystemResponseTime(AxisConfiguration axisConfig) {
        Parameter processor =
                axisConfig.getParameter(StatisticsConstants.RESPONSE_TIME_PROCESSOR);
        if (processor != null) {
            Object value = processor.getValue();
            if (value instanceof ResponseTimeProcessor) {
                return ((ResponseTimeProcessor) value).getMinResponseTime();
            }
        }
        return 0;
    }

    public int getServiceRequestCount(AxisService axisService) throws AxisFault {
        int count = 0;
        for (Iterator opIter = axisService.getOperations(); opIter.hasNext();) {
            AxisOperation axisOp = (AxisOperation) opIter.next();
            Parameter parameter = axisOp.getParameter(StatisticsConstants.IN_OPERATION_COUNTER);
            if (parameter != null) {
                count += ((AtomicInteger) parameter.getValue()).get();
            }
        }
        return count;
    }

    public int getServiceFaultCount(AxisService axisService) throws AxisFault {
        int count = 0;
        for (Iterator opIter = axisService.getOperations(); opIter.hasNext();) {
            AxisOperation axisOp = (AxisOperation) opIter.next();
            Parameter parameter = axisOp.getParameter(StatisticsConstants.OPERATION_FAULT_COUNTER);
            if (parameter != null) {
                count += ((AtomicInteger) parameter.getValue()).get();
            }
        }
        return count;
    }

    public int getServiceResponseCount(AxisService axisService) throws AxisFault {
        int count = 0;
        for (Iterator opIter = axisService.getOperations(); opIter.hasNext();) {
            AxisOperation axisOp = (AxisOperation) opIter.next();
            Parameter parameter = axisOp.getParameter(StatisticsConstants.OUT_OPERATION_COUNTER);
            if (parameter != null) {
                count += ((AtomicInteger) parameter.getValue()).get();
            }
        }
        return count;
    }

    public long getMaxServiceResponseTime(AxisService axisService) throws AxisFault {
        long max = 0;
        Parameter parameter =
                axisService.getParameter(StatisticsConstants.SERVICE_RESPONSE_TIME_PROCESSOR);
        if (parameter != null) {
            ResponseTimeProcessor proc = (ResponseTimeProcessor) parameter.getValue();
            max = proc.getMaxResponseTime();
        }
        return max;
    }

    public long getMinServiceResponseTime(AxisService axisService) throws AxisFault {
        long min = 0;
        Parameter parameter =
                axisService.getParameter(StatisticsConstants.SERVICE_RESPONSE_TIME_PROCESSOR);
        if (parameter != null) {
            ResponseTimeProcessor proc = (ResponseTimeProcessor) parameter.getValue();
            min = proc.getMinResponseTime();
        }
        if (min == -1) {
            min = 0;
        }
        return min;
    }

    public double getAvgServiceResponseTime(AxisService axisService) throws AxisFault {
        double avg = 0;
        Parameter parameter =
                axisService.getParameter(StatisticsConstants.SERVICE_RESPONSE_TIME_PROCESSOR);
        if (parameter != null) {
            ResponseTimeProcessor proc = (ResponseTimeProcessor) parameter.getValue();
            avg = proc.getAvgResponseTime();
        }
        return avg;
    }

    public int getOperationRequestCount(AxisOperation axisOperation) throws AxisFault {
        Parameter parameter =
                axisOperation.getParameter(StatisticsConstants.IN_OPERATION_COUNTER);
        if (parameter != null) {
            return ((AtomicInteger) parameter.getValue()).get();
        }
        return 0;
    }

    public int getOperationFaultCount(AxisOperation axisOperation) throws AxisFault {
        Parameter parameter =
                axisOperation.getParameter(StatisticsConstants.OPERATION_FAULT_COUNTER);
        if (parameter != null) {
            return ((AtomicInteger) parameter.getValue()).get();
        }
        return 0;
    }

    public int getOperationResponseCount(AxisOperation axisOperation) throws AxisFault {
        Parameter parameter =
                axisOperation.getParameter(StatisticsConstants.OUT_OPERATION_COUNTER);
        if (parameter != null) {
            return ((AtomicInteger) parameter.getValue()).get();
        }
        return 0;
    }

    public long getMaxOperationResponseTime(AxisOperation axisOperation) throws AxisFault {
        long max = 0;
        Parameter parameter =
                axisOperation.getParameter(StatisticsConstants.OPERATION_RESPONSE_TIME_PROCESSOR);
        if (parameter != null) {
            max = ((ResponseTimeProcessor) parameter.getValue()).getMaxResponseTime();
        }
        return max;
    }

    public long getMinOperationResponseTime(AxisOperation axisOperation) throws AxisFault {
        long min = 0;
        Parameter parameter =
                axisOperation.getParameter(StatisticsConstants.OPERATION_RESPONSE_TIME_PROCESSOR);
        if (parameter != null) {
            min = ((ResponseTimeProcessor) parameter.getValue()).getMinResponseTime();
        }
        if (min == -1) {
            min = 0;
        }
        return min;
    }

    public double getAvgOperationResponseTime(AxisOperation axisOperation) throws AxisFault {
        double avg = 0;
        Parameter parameter =
                axisOperation.getParameter(StatisticsConstants.OPERATION_RESPONSE_TIME_PROCESSOR);
        if (parameter != null) {
            avg = ((ResponseTimeProcessor) parameter.getValue()).getAvgResponseTime();
        }
        return avg;
    }

    public int getCurrentSystemResponseCount(MessageContext messageContext) {
        Object currentSystemResponseCount =
                messageContext.getProperty(StatisticsConstants.GLOBAL_CURRENT_RESPONSE_COUNTER);
        if (currentSystemResponseCount != null) {
            Object value = currentSystemResponseCount;
            if (value instanceof Integer) {
                return ((Integer) value);
            }
        }
        return 0;
    }

    public long getCurrentSystemResponseTime(MessageContext messageContext) {

        Object currentSystemResponseTime =
                messageContext.getProperty(StatisticsConstants.GLOBAL_CURRENT_INVOCATION_RESPONSE_TIME);
        if (currentSystemResponseTime != null) {
            Object value = currentSystemResponseTime;
            if (value instanceof Long) {
                return ((Long) value);
            }
        }
        return 0;
    }

    public int getCurrentSystemRequestCount(MessageContext messageContext) {
        Object currentSystemRequestCount =
                messageContext.getProperty(StatisticsConstants.GLOBAL_CURRENT_REQUEST_COUNTER);
        if (currentSystemRequestCount != null) {
            Object value = currentSystemRequestCount;
            if (value instanceof Integer) {
                return ((Integer) value);
            }
        }
        return 0;
    }

    public int getCurrentSystemFaultCount(MessageContext messageContext) {
        Object currentSystemFaultCount =
                messageContext.getProperty(StatisticsConstants.GLOBAL_CURRENT_FAULT_COUNTER);
        if (currentSystemFaultCount != null) {
            Object value = currentSystemFaultCount;
            if (value instanceof Integer) {
                return ((Integer) value);
            }
        }
        return 0;
    }
}
