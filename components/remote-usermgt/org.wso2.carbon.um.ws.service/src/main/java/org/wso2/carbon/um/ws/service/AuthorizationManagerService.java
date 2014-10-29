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
package org.wso2.carbon.um.ws.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;

public class AuthorizationManagerService extends AbstractAdmin {

    private static Log log = LogFactory.getLog(AuthorizationManagerService.class.getClass());

    public void authorizeRole(String roleName, String resourceId, String action)
            throws UserStoreException {

        Util.checkAccess(resourceId);
        getAuthorizationManager().authorizeRole(roleName, resourceId, action);
    }

    public void authorizeUser(String userName, String resourceId, String action)
            throws UserStoreException {

        Util.checkAccess(resourceId);
        getAuthorizationManager().authorizeUser(userName, resourceId, action);
    }

    public void clearResourceAuthorizations(String resourceId) throws UserStoreException {
        getAuthorizationManager().clearResourceAuthorizations(resourceId);
    }

    public void clearRoleActionOnAllResources(String roleName, String action)
            throws UserStoreException {
        getAuthorizationManager().clearRoleActionOnAllResources(roleName, action);
    }

    public void clearRoleAuthorization(String roleName, String resourceId, String action)
            throws UserStoreException {
        getAuthorizationManager().clearRoleAuthorization(roleName, resourceId, action);
    }

    public void clearAllRoleAuthorization(String roleName) throws UserStoreException {
        getAuthorizationManager().clearRoleAuthorization(roleName);
    }

    public void clearUserAuthorization(String userName, String resourceId, String action)
            throws UserStoreException {
        getAuthorizationManager().clearUserAuthorization(userName, resourceId, action);
    }

    public void clearAllUserAuthorization(String userName) throws UserStoreException {
        getAuthorizationManager().clearUserAuthorization(userName);

    }

    public void denyRole(String roleName, String resourceId, String action)
            throws UserStoreException {
        getAuthorizationManager().denyRole(roleName, resourceId, action);

    }

    public void denyUser(String userName, String resourceId, String action)
            throws UserStoreException {
        getAuthorizationManager().denyUser(userName, resourceId, action);

    }

    public String[] getAllowedRolesForResource(String resourceId, String action)
            throws UserStoreException {
        return getAuthorizationManager().getAllowedRolesForResource(resourceId, action);
    }

    public String[] getDeniedRolesForResource(String resourceId, String action)
            throws UserStoreException {
        return getAuthorizationManager().getDeniedRolesForResource(resourceId, action);
    }

    public String[] getExplicitlyAllowedUsersForResource(String resourceId, String action)
            throws UserStoreException {
        return getAuthorizationManager().getExplicitlyAllowedUsersForResource(resourceId, action);
    }

    public String[] getExplicitlyDeniedUsersForResource(String resourceId, String action)
            throws UserStoreException {
        return getAuthorizationManager().getExplicitlyDeniedUsersForResource(resourceId, action);
    }

    public boolean isRoleAuthorized(String roleName, String resourceId, String action)
            throws UserStoreException {
        return getAuthorizationManager().isRoleAuthorized(roleName, resourceId, action);
    }

    public boolean isUserAuthorized(String userName, String resourceId, String action)
            throws UserStoreException {
        return getAuthorizationManager().isUserAuthorized(userName, resourceId, action);
    }

    public String[] getAllowedUIResourcesForUser(String userName, String permissionRootPath)
            throws UserStoreException {
        return getAuthorizationManager().getAllowedUIResourcesForUser(userName, permissionRootPath);
    }

    public void resetPermissionOnUpdateRole(String roleName, String newRoleName)
            throws UserStoreException {
        getAuthorizationManager().resetPermissionOnUpdateRole(roleName, newRoleName);
    }

    private AuthorizationManager getAuthorizationManager() throws UserStoreException {
        try {
            UserRealm realm = super.getUserRealm();
            if (realm == null) {
                log.error("User realm is null");
                throw new UserStoreException("UserRealm is null");
            }
            return realm.getAuthorizationManager();
        } catch (Exception e) {
            throw new UserStoreException(e);
        }
    }



}
