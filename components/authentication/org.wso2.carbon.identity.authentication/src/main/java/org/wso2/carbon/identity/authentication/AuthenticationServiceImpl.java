/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.identity.authentication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserRealmService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * The default implementation of the <code>AuthenticationService</code>
 */
public class AuthenticationServiceImpl implements AuthenticationService {

    private static Log log = LogFactory.getLog(AuthenticationServiceImpl.class);

    private final SharedKeyAccessService sharedKeyAccessService;
    private final UserRealmService realmService;

    public AuthenticationServiceImpl(SharedKeyAccessService sharedKeyAccessService, UserRealmService realmService) {
        this.sharedKeyAccessService = sharedKeyAccessService;
        this.realmService = realmService;
    }

    /**
     * If the use is invalid, throws an <code>AuthenticationException</code>
     * If the password is equals to the shared key, returns <code>true</code>
     * Otherwise, calls the authenticate method of the <code>UserStoreManager<code>
     *
     * @param username The name of the user to be authenticated
     * @param password The password of the user to be authenticated.
     * @return <code>true</code> if the authentication is successful.
     * @throws AuthenticationException for failures in the authentication
     */
    public boolean authenticate(String username, String password) throws AuthenticationException {
        String tenantLessUsername = MultitenantUtils.getTenantAwareUsername(username);
        try {
            int tenantID = MultitenantConstants.SUPER_TENANT_ID;
            if (username.contains("@")) {
                tenantID = realmService.getTenantManager().getTenantId(username.substring(username.lastIndexOf("@") + 1));
            }
            UserRealm userRealm = realmService.getTenantUserRealm(tenantID);

            // User not found in the UM
            if (!userRealm.getUserStoreManager().isExistingUser(tenantLessUsername)) {
                throw new AuthenticationException("Invalid User : " + tenantLessUsername, log);
            }

            // Authenticate internal call from another Carbon bundle
            if (password.equals(sharedKeyAccessService.getSharedKey())) {
                return true;
            }

            // Check if the user is authenticated
            return userRealm.getUserStoreManager().authenticate(tenantLessUsername, password);

            // Let the engine know if the user is authenticated or not
        } catch (UserStoreException e) {
            throw new AuthenticationException("User not authenticated for the given username : " + tenantLessUsername, log);
        }
    }
}
