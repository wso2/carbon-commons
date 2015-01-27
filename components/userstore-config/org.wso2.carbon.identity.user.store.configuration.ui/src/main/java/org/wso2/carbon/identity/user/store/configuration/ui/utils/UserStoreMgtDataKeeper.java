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
package org.wso2.carbon.identity.user.store.configuration.ui.utils;

import org.wso2.carbon.identity.user.store.configuration.stub.dto.PropertyDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class UserStoreMgtDataKeeper {

    private static Map<String, Map<String, String>> userStoreManagers = new HashMap<String, Map<String, String>>();

    /**
     * Add a new user store manager to in-memory map, at the given domain name
     *
     * @param userStoreManager
     * @param domainName
     */
    public static void addUserStoreManager(PropertyDTO[] userStoreManager, String domainName) {
        UserStoreMgtDataKeeper.userStoreManagers.put(domainName, convertArrayToMap(userStoreManager));
    }
    
    /**
     * Clear  user store manager to in-memory map
     *
     */
    public static void clearUserStoreManager() {
        UserStoreMgtDataKeeper.userStoreManagers.clear();
    }

    /**
     * Get the user store manager by domain name
     *
     * @param domainName
     * @return
     */
    public static Map<String, String> getUserStoreManager(String domainName) {
        return userStoreManagers.get(domainName);
    }

    public static Set<String> getAvailableDomainNames() {
        return userStoreManagers.keySet();
    }

    /**
     * Convert a given String[] propertyName#propertyValue to a Map<String,String>
     *
     * @param properties
     * @return
     */
    private static Map<String, String> convertArrayToMap(PropertyDTO[] properties) {
        Map<String, String> propertyMap = new HashMap<String, String>();
        for (PropertyDTO propertyDTO : properties) {
            if (propertyDTO.getValue() != null) {
                propertyMap.put(propertyDTO.getName(), propertyDTO.getValue());
            }
        }

        return propertyMap;

    }
}
