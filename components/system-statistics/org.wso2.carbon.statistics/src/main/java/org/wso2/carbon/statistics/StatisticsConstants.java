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

package org.wso2.carbon.statistics;

/**
 *
 */
public final class StatisticsConstants {
    public static final String GLOBAL_REQUEST_COUNTER = "wso2statistics.GlobalRequestCounter";
    public static final String GLOBAL_RESPONSE_COUNTER = "wso2statistics.GlobalResponseCounter";
    public static final String GLOBAL_CURRENT_REQUEST_COUNTER = "wso2statistics.GlobalCurrentRequestCounter";
    public static final String GLOBAL_CURRENT_RESPONSE_COUNTER = "wso2statistics.GlobalCurrentResponseCounter";
    public static final String GLOBAL_FAULT_COUNTER = "wso2statistics.GlobalFaultCounter";
    public static final String GLOBAL_CURRENT_FAULT_COUNTER = "wso2statistics.GlobalCurrentFaultCounter";

    public static final String SERVICE_RESPONSE_TIME_PROCESSOR = "wso2statistics.ServiceResponseTimeProc";
    public static final String OPERATION_RESPONSE_TIME_PROCESSOR = "wso2statistics.OperationResponseTimeProc";
    public static final String RESPONSE_TIME_PROCESSOR = "wso2statistics.ResponseTimeProcessor";
    public static final String IN_OPERATION_COUNTER = "wso2statistics.InOperationCounter";
    public static final String CURRENT_IN_OPERATION_AND_SERVICE_COUNTER = "wso2statistics.CurrentInOperationAndServiceCounter";
    public static final String OUT_OPERATION_COUNTER = "wso2statistics.OutOperationCounter";
    public static final String CURRENT_OUT_OPERATION_AND_SERVICE_COUNTER = "wso2statistics.CurrentOutOperationAndServiceCounter";
    public static final String OPERATION_FAULT_COUNTER = "wso2statistics.OperationFaultCounter";
    public static final String CURRENT_OPERATION_AND_SERVICE_FAULT_COUNTER = "wso2statistics.CurrentOperationAndServiceFaultCounter";
    public static final String REQUEST_RECEIVED_TIME = "wso2statistics.request.received.time";

    public static final String OPERATION_RESPONSE_TIME = "wso2statistics.operation.response.time";
    public static final String SERVICE_RESPONSE_TIME = "wso2statistics.service.response.time";


    public static final String GLOBAL_CURRENT_INVOCATION_RESPONSE_TIME =
            "wso2statistics.global.invocation.response.time";

    public static final String STATISTISTICS_MODULE_NAME = "wso2statistics";

    private StatisticsConstants() {
    }
}
