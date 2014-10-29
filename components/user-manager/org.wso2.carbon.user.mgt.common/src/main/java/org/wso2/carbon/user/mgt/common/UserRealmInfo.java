/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.user.mgt.common;

import org.wso2.carbon.CarbonConstants;

/**
 *
 */
public class UserRealmInfo {

    private String everyOneRole;

    private String adminRole;

    private String adminUser;

    private boolean isBulkImportSupported;

    private boolean isMultipleUserStore;    

    private String[] requiredUserClaims;

    private String[] userClaims;

    private String[] domainNames;

    private int maxUIPagesInCache;

    private int maxItemsPerUIPage;

    private boolean enableUIPageCache;

    private UserStoreInfo primaryUserStoreInfo;        

    private UserStoreInfo[] userStoresInfo;


    public String getEveryOneRole() {
        return everyOneRole;
    }

    public void setEveryOneRole(String everyOneRole) {
        this.everyOneRole = everyOneRole;
    }

    public String getAdminRole() {
        return adminRole;
    }

    public void setAdminRole(String adminRole) {
        this.adminRole = adminRole;
    }

    public String getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(String adminUser) {
        this.adminUser = adminUser;
    }

    public boolean isBulkImportSupported() {
        return isBulkImportSupported;
    }

    public void setBulkImportSupported(boolean bulkImportSupported) {
        isBulkImportSupported = bulkImportSupported;
    }

    public boolean isMultipleUserStore() {
        return isMultipleUserStore;
    }

    public void setMultipleUserStore(boolean multipleUserStore) {
        isMultipleUserStore = multipleUserStore;
    }

    public String[] getRequiredUserClaims() {
        return requiredUserClaims;
    }

    public void setRequiredUserClaims(String[] requiredUserClaims) {
        this.requiredUserClaims = requiredUserClaims;
    }

    public String[] getUserClaims() {
        return userClaims;
    }

    public void setUserClaims(String[] userClaims) {
        this.userClaims = userClaims;
    }

    public String[] getDomainNames() {
        return domainNames;
    }

    public void setDomainNames(String[] domainNames) {
        this.domainNames = domainNames;
    }

    public int getMaxUIPagesInCache() {
        return maxUIPagesInCache;
    }

    public void setMaxUIPagesInCache(int maxUIPagesInCache) {
        this.maxUIPagesInCache = maxUIPagesInCache;
    }

    public int getMaxItemsPerUIPage() {
        return maxItemsPerUIPage;
    }

    public void setMaxItemsPerUIPage(int maxItemsPerUIPage) {
        this.maxItemsPerUIPage = maxItemsPerUIPage;
    }

    public boolean isEnableUIPageCache() {
        return enableUIPageCache;
    }

    public void setEnableUIPageCache(boolean enableUIPageCache) {
        this.enableUIPageCache = enableUIPageCache;
    }

    public UserStoreInfo getPrimaryUserStoreInfo() {
        return primaryUserStoreInfo;
    }

    public void setPrimaryUserStoreInfo(UserStoreInfo primaryUserStoreInfo) {
        this.primaryUserStoreInfo = primaryUserStoreInfo;
    }

    public UserStoreInfo[] getUserStoresInfo() {
        return userStoresInfo;
    }

    public void setUserStoresInfo(UserStoreInfo[] userStoresInfo) {
        this.userStoresInfo = userStoresInfo;
    }

    public UserStoreInfo getUserStoreInfo(String domainName) {
        
        for(UserStoreInfo info : userStoresInfo){
            if(domainName != null && domainName.equalsIgnoreCase(info.getDomainName())){
                return info;
            }
        }

        return null;
    }

    public UserStoreInfo getUserStoreInfoForUser(String userName) {

        if(userName.contains(CarbonConstants.DOMAIN_SEPARATOR)){
            String domainName = userName.substring(0, userName.indexOf(CarbonConstants.DOMAIN_SEPARATOR));
            for(UserStoreInfo info : userStoresInfo){
                if(domainName != null && domainName.equalsIgnoreCase(info.getDomainName())){
                    return info;
                }
            }
        }

        return primaryUserStoreInfo;
    }
}
