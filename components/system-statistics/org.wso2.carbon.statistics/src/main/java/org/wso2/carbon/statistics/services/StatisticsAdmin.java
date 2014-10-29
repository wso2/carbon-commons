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
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.statistics.services.util.OperationStatistics;
import org.wso2.carbon.statistics.services.util.ServiceStatistics;
import org.wso2.carbon.statistics.services.util.SystemStatistics;
import org.wso2.carbon.statistics.webapp.ComputeData;
import org.wso2.carbon.statistics.webapp.data.StatisticData;
import org.wso2.carbon.utils.deployment.GhostDeployerUtils;

import javax.xml.namespace.QName;

/**
 *
 */
public class StatisticsAdmin extends AbstractAdmin implements StatisticsAdminMBean {
    private SystemStatisticsUtil sysStatsUtil = new SystemStatisticsUtil();
    private static Log log = LogFactory.getLog(StatisticsAdmin.class);

    public StatisticsAdmin() {
    }

    public SystemStatistics getSystemStatistics() throws AxisFault{
        return sysStatsUtil.getSystemStatistics(getAxisConfig());
    }

    public ServiceStatistics getServiceStatistics(String serviceName) throws AxisFault {
        return sysStatsUtil.getServiceStatistics(getAxisService(serviceName));
    }

    public OperationStatistics getOperationStatistics(String serviceName,
                                                      String operationName) throws AxisFault {
        return sysStatsUtil.getOperationStatistics(getAxisOperation(serviceName, operationName));
    }

    /**
     * Get the total requests received by the system which hosts this service
     *
     * @return the total requests received by the system which hosts this service
     * @throws AxisFault
     */
    public int getSystemRequestCount() throws AxisFault {
        return sysStatsUtil.getTotalSystemRequestCount(getAxisConfig());
    }

    public int getSystemFaultCount() throws AxisFault {
        return sysStatsUtil.getSystemFaultCount(getAxisConfig());
    }

    public int getSystemResponseCount() throws AxisFault {
        return sysStatsUtil.getSystemResponseCount(getAxisConfig());
    }

    public double getAvgSystemResponseTime() {
        return sysStatsUtil.getAvgSystemResponseTime(getAxisConfig());
    }

    public long getMaxSystemResponseTime() {
        return sysStatsUtil.getMaxSystemResponseTime(getAxisConfig());
    }

    public long getMinSystemResponseTime() {
        return sysStatsUtil.getMinSystemResponseTime(getAxisConfig());
    }

    /**
     * Obtain the number of requests that were received by the service
     *
     * @param serviceName
     * @return The number of requests that were received by the service
     */
    public int getServiceRequestCount(String serviceName) throws AxisFault {
        return sysStatsUtil.getServiceRequestCount(getAxisService(serviceName));
    }

    public int getServiceFaultCount(String serviceName) throws AxisFault {
        return sysStatsUtil.getServiceFaultCount(getAxisService(serviceName));
    }

    public int getServiceResponseCount(String serviceName) throws AxisFault {
        return sysStatsUtil.getServiceResponseCount(getAxisService(serviceName));
    }

    public long getMaxServiceResponseTime(String serviceName) throws AxisFault {
        return sysStatsUtil.getMaxServiceResponseTime(getAxisService(serviceName));
    }

    public long getMinServiceResponseTime(String serviceName) throws AxisFault {
        return sysStatsUtil.getMinServiceResponseTime(getAxisService(serviceName));
    }

    public double getAvgServiceResponseTime(String serviceName) throws AxisFault {
        return sysStatsUtil.getAvgServiceResponseTime(getAxisService(serviceName));
    }

    public int getOperationRequestCount(String serviceName, String operationName) throws AxisFault {
        return sysStatsUtil.getOperationRequestCount(getAxisOperation(serviceName, operationName));
    }

    public int getOperationFaultCount(String serviceName, String operationName) throws AxisFault {
        return sysStatsUtil.getOperationFaultCount(getAxisOperation(serviceName, operationName));
    }

    public int getOperationResponseCount(String serviceName,
                                         String operationName) throws AxisFault {
        return sysStatsUtil.getOperationResponseCount(getAxisOperation(serviceName, operationName));
    }

    public long getMaxOperationResponseTime(String serviceName,
                                            String operationName) throws AxisFault {
        return sysStatsUtil.getMaxOperationResponseTime(getAxisOperation(serviceName, operationName));
    }

    public long getMinOperationResponseTime(String serviceName,
                                            String operationName) throws AxisFault {
        return sysStatsUtil.getMinOperationResponseTime(getAxisOperation(serviceName, operationName));
    }

    public double getAvgOperationResponseTime(String serviceName,
                                              String operationName) throws AxisFault {
        return sysStatsUtil.getAvgOperationResponseTime(getAxisOperation(serviceName, operationName));
    }

    public void setConfigurationContext(ConfigurationContext configurationContext) {
        super.setConfigurationContext(configurationContext);
    }

    private AxisService getAxisService(String serviceName) {
        AxisConfiguration axisConfiguration = getAxisConfig();
        AxisService axisService = axisConfiguration.getServiceForActivation(serviceName);
        // Check if the service in in ghost list
        try {
            if (axisService == null && GhostDeployerUtils.
                    getTransitGhostServicesMap(axisConfiguration).containsKey(serviceName)) {
                GhostDeployerUtils.waitForServiceToLeaveTransit(serviceName, getAxisConfig());
                axisService = axisConfiguration.getServiceForActivation(serviceName);
            }
        } catch (AxisFault axisFault) {
            log.error("Error occurred while service : " + serviceName + " is " +
                      "trying to leave transit", axisFault);
        }
        return axisService;
    }

    private AxisOperation getAxisOperation(String serviceName,
                                           String operationName) {
        return getAxisService(serviceName).getOperation(new QName(operationName));
    }

    /**
     * Reading the web app statistics data
     */

    public StatisticData getWebappRelatedData(String webAppName) {
        try {

            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

            StatisticData statisticData = ComputeData.map.get(tenantId).get(webAppName);
            return statisticData;
        } catch (Exception e) {
            return null;
        }
    }


}
