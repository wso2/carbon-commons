/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.statistics.webapp.data;


public class StatisticData {

    private int requstCount;
    private int responseCount;
    private int faultCount;
    private double maximumResponseTime;
    private double minimumresponseTime;
    private double averageResponseTime;
    private double responseTime;
    private int tenantId;
    private String tenantName;
    private String webappName;

    public double getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(double responseTime) {
        this.responseTime = responseTime;
    }

    public String getWebappName() {
        return webappName;
    }

    public void setWebappName(String webappName) {
        this.webappName = webappName;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public int getRequstCount() {
        return requstCount;
    }

    public void setRequstCount(int requstCount) {
        this.requstCount = requstCount;
    }

    public int getResponseCount() {
        return responseCount;
    }

    public void setResponseCount(int responseCount) {
        this.responseCount = responseCount;
    }

    public int getFaultCount() {
        return faultCount;
    }

    public void setFaultCount(int faultCount) {
        this.faultCount = faultCount;
    }

    public double getMaximumResponseTime() {
        return maximumResponseTime;
    }

    public void setMaximumResponseTime(double maximumResponseTime) {
        this.maximumResponseTime = maximumResponseTime;
    }

    public double getMinimumresponseTime() {
        return minimumresponseTime;
    }

    public void setMinimumresponseTime(double minimumresponseTime) {
        this.minimumresponseTime = minimumresponseTime;
    }

    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    public void setAverageResponseTime(double averageResponseTime) {
        this.averageResponseTime = averageResponseTime;
    }
}
