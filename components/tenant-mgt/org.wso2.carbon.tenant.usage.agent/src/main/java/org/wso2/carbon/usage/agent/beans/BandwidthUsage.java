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


package org.wso2.carbon.usage.agent.beans;

/**
 * the bean class for BandwidthUsage with attributes
 * int tenant id,
 * String measurement
 * long value   *
 */

public class BandwidthUsage {
    private int tenantId;
    private String measurement;
    private long value;


    public String getMeasurement() {
        return measurement;
    }

    public BandwidthUsage(int tenantId, String measurement, long value) {
        this.tenantId = tenantId;
        this.measurement = measurement;
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public int getTenantId() {
        return tenantId;
    }
}
