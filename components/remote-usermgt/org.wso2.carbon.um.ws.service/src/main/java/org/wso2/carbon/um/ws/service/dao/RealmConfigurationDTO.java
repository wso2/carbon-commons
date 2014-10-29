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
package org.wso2.carbon.um.ws.service.dao;

import java.util.Date;

public class RealmConfigurationDTO {

    private String userStoreClass = null;
    private String authorizationManagerClass = null;
    private String adminRoleName = null;
    private String adminUserName = null;
    private String adminPassword = null;
    private String everyOneRoleName = null;
    private String realmClassName = null;
    private RealmPropertyDTO[] userStoreProperties = new RealmPropertyDTO[0];
    private RealmPropertyDTO[] authzProperties = new RealmPropertyDTO[0];
    private RealmPropertyDTO[] realmProperties = new RealmPropertyDTO[0];
    boolean isReadOnly = true;
    private int tenantId;
    private Date persistedTimestamp;
    private int maxUserListLength = -1;

    public RealmConfigurationDTO() {

    }

    public String getUserStoreClass() {
        return userStoreClass;
    }

    public void setUserStoreClass(String userStoreClass) {
        this.userStoreClass = userStoreClass;
    }

    public String getAuthorizationManagerClass() {
        return authorizationManagerClass;
    }

    public void setAuthorizationManagerClass(String authorizationManagerClass) {
        this.authorizationManagerClass = authorizationManagerClass;
    }

    public String getAdminRoleName() {
        return adminRoleName;
    }

    public void setAdminRoleName(String adminRoleName) {
        this.adminRoleName = adminRoleName;
    }

    public String getAdminUserName() {
        return adminUserName;
    }

    public void setAdminUserName(String adminUserName) {
        this.adminUserName = adminUserName;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getEveryOneRoleName() {
        return everyOneRoleName;
    }

    public void setEveryOneRoleName(String everyOneRoleName) {
        this.everyOneRoleName = everyOneRoleName;
    }

    public String getRealmClassName() {
        return realmClassName;
    }

    public void setRealmClassName(String realmClassName) {
        this.realmClassName = realmClassName;
    }

    public RealmPropertyDTO[] getUserStoreProperties() {
        return userStoreProperties;
    }

    public void setUserStoreProperties(RealmPropertyDTO[] userStoreProperties) {
        this.userStoreProperties = userStoreProperties;
    }

    public RealmPropertyDTO[] getAuthzProperties() {
        return authzProperties;
    }

    public void setAuthzProperties(RealmPropertyDTO[] authzProperties) {
        this.authzProperties = authzProperties;
    }

    public RealmPropertyDTO[] getRealmProperties() {
        return realmProperties;
    }

    public void setRealmProperties(RealmPropertyDTO[] realmProperties) {
        this.realmProperties = realmProperties;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public void setReadOnly(boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public Date getPersistedTimestamp() {
        return persistedTimestamp;
    }

    public void setPersistedTimestamp(Date persistedTimestamp) {
        this.persistedTimestamp = persistedTimestamp;
    }

    public int getMaxUserListLength() {
        return maxUserListLength;
    }

    public void setMaxUserListLength(int maxUserListLength) {
        this.maxUserListLength = maxUserListLength;
    }

}
