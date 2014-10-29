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
package org.wso2.carbon.statistics.services.util;

/**
 *
 */
public class ServiceStatistics {
    private double avgResponseTime;
    private long minResponseTime;
    private long maxResponseTime;

    private int totalRequestCount;
    private int totalResponseCount;
    private int totalFaultCount;

    private double currentInvocationResponseTime;
    private int currentInvocationRequestCount;
    private int currentInvocationResponseCount;
    private int currentInvocationFaultCount;

    public ServiceStatistics() {
    }

    public double getCurrentInvocationResponseTime() {
        return currentInvocationResponseTime;
    }

    public void setCurrentInvocationResponseTime(double currentInvocationResponseTime) {
        this.currentInvocationResponseTime = currentInvocationResponseTime;
    }

    public int getCurrentInvocationRequestCount() {
        return currentInvocationRequestCount;
    }

    public void setCurrentInvocationRequestCount(int currentInvocationRequestCount) {
        this.currentInvocationRequestCount = currentInvocationRequestCount;
    }

    public int getCurrentInvocationFaultCount() {
        return currentInvocationFaultCount;
    }

    public void setCurrentInvocationFaultCount(int currentInvocationFaultCount) {
        this.currentInvocationFaultCount = currentInvocationFaultCount;
    }

    public int getCurrentInvocationResponseCount() {
        return currentInvocationResponseCount;
    }

    public void setCurrentInvocationResponseCount(int currentInvocationResponseCount) {
        this.currentInvocationResponseCount = currentInvocationResponseCount;
    }

    public double getAvgResponseTime() {
        return avgResponseTime;
    }

    public void setAvgResponseTime(double avgResponseTime) {
        this.avgResponseTime = avgResponseTime;
    }

    public long getMinResponseTime() {
        return minResponseTime;
    }

    public void setMinResponseTime(long minResponseTime) {
        this.minResponseTime = minResponseTime;
    }

    public long getMaxResponseTime() {
        return maxResponseTime;
    }

    public void setMaxResponseTime(long maxResponseTime) {
        this.maxResponseTime = maxResponseTime;
    }

    public int getTotalRequestCount() {
        return totalRequestCount;
    }

    public void setTotalRequestCount(int totalRequestCount) {
        this.totalRequestCount = totalRequestCount;
    }

    public int getTotalResponseCount() {
        return totalResponseCount;
    }

    public void setTotalResponseCount(int totalResponseCount) {
        this.totalResponseCount = totalResponseCount;
    }

    public int getTotalFaultCount() {
        return totalFaultCount;
    }

    public void setTotalFaultCount(int totalFaultCount) {
        this.totalFaultCount = totalFaultCount;
    }
}
