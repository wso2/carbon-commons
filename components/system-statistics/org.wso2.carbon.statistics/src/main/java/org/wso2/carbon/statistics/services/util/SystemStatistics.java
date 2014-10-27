/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.statistics.services.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.SystemFilter;
import org.wso2.carbon.statistics.services.SystemStatisticsUtil;
import org.wso2.carbon.utils.NetworkUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class SystemStatistics {
    private String serverName;
    private String serverStartTime;
    private String systemUpTime;
    private int totalRequestCount;
    private int totalResponseCount;
    private int totalFaultCount;
    private int currentInvocationRequestCount;
    private int currentInvocationResponseCount;
    private int currentInvocationFaultCount;
    private int services;
    private Metric usedMemory;
    private Metric totalMemory;
    private double avgResponseTime;
    private long currentInvocationResponseTime;
    private long minResponseTime;
    private long maxResponseTime;
    private String wso2wsasVersion;
    private SimpleDateFormat dateFormatter;
    private String javaVersion;

    private static final long GB_IN_BYTES = 1024 * 1024 * 1024;
    private static final long MB_IN_BYTES = 1024 * 1024;
    private static final long KB_IN_BYTES = 1024;
    private static final int SECONDS_PER_DAY = 3600 * 24;

    private SystemStatisticsUtil statService;

    public SystemStatistics(AxisConfiguration configuration) throws AxisFault {
        try {
            dateFormatter = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
            statService = new SystemStatisticsUtil();
            javaVersion = System.getProperty("java.version");
            update(configuration);
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }

    public SystemStatistics(MessageContext messageContext) throws AxisFault {
        try {
            statService = new SystemStatisticsUtil();
            javaVersion = System.getProperty("java.version");
            update(messageContext);
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }

    //This is for supporting old API
    public void update(AxisConfiguration axisConfig) throws AxisFault {

        int tenantId =  PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Parameter systemStartTime =
                axisConfig.getParameter(CarbonConstants.SERVER_START_TIME);
        long startTime = 0;
        if (systemStartTime != null) {
            startTime = Long.parseLong((String) systemStartTime.getValue());
        }
        Date stTime = new Date(startTime);

        // only super admin can view serverStartTime and systemUpTime
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            serverStartTime = dateFormatter.format(stTime);
            systemUpTime = (getTime((System.currentTimeMillis() - startTime) / 1000));
        }
        try {
            totalRequestCount = statService.getTotalSystemRequestCount(axisConfig);
            totalResponseCount = statService.getSystemResponseCount(axisConfig);
            totalFaultCount = statService.getSystemFaultCount(axisConfig);
            avgResponseTime = statService.getAvgSystemResponseTime(axisConfig);
            maxResponseTime = statService.getMaxSystemResponseTime(axisConfig);
            minResponseTime = statService.getMinSystemResponseTime(axisConfig);

        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }

        Runtime runtime = Runtime.getRuntime();

        // only super admin can view usedMemory and totalMemory
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            usedMemory = formatMemoryValue(runtime.totalMemory() - runtime.freeMemory());
            totalMemory = formatMemoryValue(runtime.totalMemory());
        }
        wso2wsasVersion = ServerConfiguration.getInstance().getFirstProperty("Version");

        int activeServices = 0;
        for (Iterator services = axisConfig.getServices().values().iterator();
             services.hasNext(); ) {
            AxisService axisService = (AxisService) services.next();
            AxisServiceGroup asGroup = (AxisServiceGroup) axisService.getParent();
            if (axisService.isActive() &&
                !axisService.isClientSide() &&
                !SystemFilter.isFilteredOutService(asGroup)) {
                activeServices++;
            }
        }

        this.services = activeServices;
        try {
            // only susper admin is allow to view serverName.
            if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
                serverName = NetworkUtils.getLocalHostname();
            }
        } catch (SocketException ignored) {
        }
    }

    /* This method use by data publishers to get update about current invocation. we need to use message context instead of
    AxisConfiguration, because in concurrent execution if we save values as axis parameters these values might get overwrite
    by another thread. */

    public void update(MessageContext messageContext) throws AxisFault {
        try {

            currentInvocationResponseTime = statService.getCurrentSystemResponseTime(messageContext);
            currentInvocationResponseCount = statService.getCurrentSystemResponseCount(messageContext);
            currentInvocationRequestCount = statService.getCurrentSystemRequestCount(messageContext);
            currentInvocationFaultCount = statService.getCurrentSystemFaultCount(messageContext);

        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }

    public int getCurrentInvocationRequestCount() {
        return currentInvocationRequestCount;
    }

    public void setCurrentInvocationRequestCount(int currentInvocationRequestCount) {
        this.currentInvocationRequestCount = currentInvocationRequestCount;
    }

    public int getCurrentInvocationResponseCount() {
        return currentInvocationResponseCount;
    }

    public void setCurrentInvocationResponseCount(int currentInvocationResponseCount) {
        this.currentInvocationResponseCount = currentInvocationResponseCount;
    }

    public int getCurrentInvocationFaultCount() {
        return currentInvocationFaultCount;
    }

    public void setCurrentInvocationFaultCount(int currentInvocationFaultCount) {
        this.currentInvocationFaultCount = currentInvocationFaultCount;
    }

    public long getCurrentInvocationResponseTime() {
        return currentInvocationResponseTime;
    }

    public void setCurrentInvocationResponseTime(long currentInvocationResponseTime) {
        this.currentInvocationResponseTime = currentInvocationResponseTime;
    }

    public SystemStatisticsUtil getStatService() {
        return statService;
    }

    public void setStatService(SystemStatisticsUtil statService) {
        this.statService = statService;
    }

    public String getSystemUpTime() {
        return systemUpTime;
    }

    public String getServerStartTime() {
        return serverStartTime;
    }

    public String getServerName() {
        return serverName;
    }

    public int getTotalRequestCount() {
        return totalRequestCount;
    }

    public int getServices() {
        return services;
    }

    public Metric getUsedMemory() {
        return usedMemory;
    }

    public Metric getTotalMemory() {
        return totalMemory;
    }

    public int getTotalFaultCount() {
        return totalFaultCount;
    }

    public int getTotalResponseCount() {
        return totalResponseCount;
    }

    private Metric formatMemoryValue(double value) {
        double tempVal = value;
        String unit = "bytes";
        if (value > 0) {
            if ((tempVal = (value / GB_IN_BYTES)) >= 1) {
                tempVal = round(tempVal, 2);
                unit = "GB";
            } else if ((tempVal = (value / MB_IN_BYTES)) >= 1) {
                tempVal = round(tempVal, 2);
                unit = " MB";
            } else if ((tempVal = (value / KB_IN_BYTES)) >= 1) {
                tempVal = round(tempVal, 2);
                unit = " KB";
            }
        }
        return new Metric(tempVal, unit);
    }

    private double round(double value, int numOfDecimalPlaces) {
        long val = 1;
        for (int i = 1; i <= numOfDecimalPlaces; i++) {
            val *= 10;
        }
        return Math.round(value * val) / (double) val;
    }

    private String getTime(long timeInSeconds) {
        long days;
        long hours;
        long minutes;
        long seconds;
        days = timeInSeconds / SECONDS_PER_DAY;
        timeInSeconds = timeInSeconds - (days * SECONDS_PER_DAY);
        hours = timeInSeconds / 3600;
        timeInSeconds = timeInSeconds - (hours * 3600);
        minutes = timeInSeconds / 60;
        timeInSeconds = timeInSeconds - (minutes * 60);
        seconds = timeInSeconds;

        return days + " day(s) " + hours + " hr(s) " + minutes + " min(s) " + seconds + " sec(s)";
    }

    public double getAvgResponseTime() {
        return avgResponseTime;
    }

    public long getMinResponseTime() {
        return minResponseTime;
    }

    public long getMaxResponseTime() {
        return maxResponseTime;
    }

    public String getWso2wsasVersion() {
        return wso2wsasVersion;
    }

    public void setWso2wsasVersion(String wso2wsasVersion) {
        this.wso2wsasVersion = wso2wsasVersion;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

}
