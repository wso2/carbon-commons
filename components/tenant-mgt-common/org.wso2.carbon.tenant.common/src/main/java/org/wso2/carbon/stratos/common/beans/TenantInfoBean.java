/*
 * Copyright (c) 2005-2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.stratos.common.beans;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Calendar;

/**
 * Bean class for Tenant Information
 */
@XmlRootElement
public class TenantInfoBean {

    private String admin; //admin's user name
    private String firstname;
    private String lastname;
    private String adminPassword;
    private String tenantDomain;
    private int tenantId;
    private String email;
    private boolean active;
    private String successKey;
    Calendar createdDate;
    private String originatedService;
    private String usagePlan;
    private String name;

    public TenantInfoBean(){}

    /*copy constructor*/
    public TenantInfoBean(TenantInfoBean tenantInfoBean) {
        this.admin = tenantInfoBean.admin;
        this.firstname = tenantInfoBean.firstname;
        this.lastname = tenantInfoBean.lastname;
        this.adminPassword = tenantInfoBean.adminPassword;
        this.tenantDomain = tenantInfoBean.tenantDomain;
        this.tenantId = tenantInfoBean.tenantId;
        this.email = tenantInfoBean.email;
        this.active = tenantInfoBean.active;
        this.successKey = tenantInfoBean.successKey;
        this.createdDate = (Calendar)tenantInfoBean.createdDate.clone();
        this.originatedService = tenantInfoBean.originatedService;
        this.usagePlan = tenantInfoBean.usagePlan;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {

        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getSuccessKey() {
        return successKey;
    }

    public void setSuccessKey(String successKey) {
        this.successKey = successKey;
    }

    public Calendar getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Calendar createdDate) {
        this.createdDate = createdDate;
    }

    public String getOriginatedService() {
        return originatedService;
    }

    public void setOriginatedService(String originatedService) {
        this.originatedService = originatedService;
    }

    public String getUsagePlan() {
        return usagePlan;
    }

    public void setUsagePlan(String usagePlan) {
        this.usagePlan = usagePlan;
    }

    /**
     * Returns the tenant name.
     *
     * @return The tenant name.
     */
    public String getName() {

        return name;
    }

    /**
     * Sets the tenant name.
     *
     * @param name The tenant name to set.
     */
    public void setName(String name) {

        this.name = name;
    }
}
