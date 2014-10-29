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

/**
 *  MBean interface of the StatisticsAdmin MBean
 */
public interface StatisticsAdminMBean {

    /**
     * Get the total requests received by the system which hosts this service
     *
     * @return the total requests received by the system which hosts this service
     * @throws Exception If request count cannot be found
     */
    int getSystemRequestCount() throws Exception;

    int getSystemFaultCount() throws Exception;

    int getSystemResponseCount() throws Exception;

    double getAvgSystemResponseTime();

    long getMaxSystemResponseTime();

    long getMinSystemResponseTime();

    /**
     * Obtain the number of requests that were received by the service
     *
     * @param serviceName
     * @return The number of requests that were received by the service
     */
    int getServiceRequestCount(String serviceName) throws Exception;

    int getServiceFaultCount(String serviceName) throws Exception;

    int getServiceResponseCount(String serviceName) throws Exception;

    long getMaxServiceResponseTime(String serviceName) throws Exception;

    long getMinServiceResponseTime(String serviceName) throws Exception;

    double getAvgServiceResponseTime(String serviceName) throws Exception;

    int getOperationRequestCount(String serviceName, String operationName) throws Exception;

    int getOperationFaultCount(String serviceName, String operationName) throws Exception;

    int getOperationResponseCount(String serviceName, String operationName) throws Exception;

    long getMaxOperationResponseTime(String serviceName, String operationName) throws Exception;

    long getMinOperationResponseTime(String serviceName, String operationName) throws Exception;

    double getAvgOperationResponseTime(String serviceName, String operationName) throws Exception;
}
