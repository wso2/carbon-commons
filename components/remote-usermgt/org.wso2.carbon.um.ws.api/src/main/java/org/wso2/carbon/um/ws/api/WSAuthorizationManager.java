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
package org.wso2.carbon.um.ws.api;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.um.ws.api.stub.RemoteAuthorizationManagerServiceStub;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserStoreException;

public class WSAuthorizationManager implements AuthorizationManager{
    
    private RemoteAuthorizationManagerServiceStub stub = null;
    
    private static Log log = LogFactory.getLog(WSUserStoreManager.class);
    
    public WSAuthorizationManager(String serverUrl, String cookie,
                                  ConfigurationContext configCtxt) throws UserStoreException {

        try {
            stub =
                   new RemoteAuthorizationManagerServiceStub(configCtxt, serverUrl +
                                                                         "RemoteAuthorizationManagerService");
            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } catch (AxisFault e) {
            throw new UserStoreException();
        }
    }
    

    public void authorizeRole(String roleName, String resourceId, String action)
            throws UserStoreException {
        try {
            stub.authorizeRole(roleName, resourceId, action);
        } catch (Exception e) {
            this.handleException(e.getMessage(), e);
        }
    }

    public void authorizeUser(String userName, String resourceId, String action)
            throws UserStoreException {
        try {
            stub.authorizeUser(userName, resourceId, action);
        } catch (Exception e) {
            this.handleException(e.getMessage(), e);
        }
        
    }

    public void clearResourceAuthorizations(String resourceId) throws UserStoreException {
        try {
            stub.clearResourceAuthorizations(resourceId);
        } catch (Exception e) {
            this.handleException(e.getMessage(), e);
        }
        
    }

    public void clearRoleActionOnAllResources(String roleName, String action)
            throws UserStoreException {
        try {
            stub.clearRoleActionOnAllResources(roleName, action);
        } catch (Exception e) {
            this.handleException(e.getMessage(), e);
        }
        
    }

    public void clearRoleAuthorization(String roleName, String resourceId, String action)
            throws UserStoreException {
        try {
            stub.clearRoleAuthorization(roleName, resourceId, action);
        } catch (Exception e) {
            this.handleException(e.getMessage(), e);
        }
    }

    public void clearRoleAuthorization(String roleName) throws UserStoreException {
        try {
            stub.clearAllRoleAuthorization(roleName);
        } catch (Exception e) {
            this.handleException(e.getMessage(), e);
        }
        
    }

    public void clearUserAuthorization(String userName, String resourceId, String action)
            throws UserStoreException {
        try {
            stub.clearUserAuthorization(userName, resourceId, action);
        } catch (Exception e) {
            this.handleException(e.getMessage(), e);
        }
        
    }

    public void clearUserAuthorization(String userName) throws UserStoreException {
        try {
            stub.clearAllUserAuthorization(userName);
        } catch (Exception e) {
            this.handleException(e.getMessage(), e);
        }
        
    }

    public void denyRole(String roleName, String resourceId, String action)
            throws UserStoreException {
        try {
            stub.denyRole(roleName, resourceId, action);
        } catch (Exception e) {
            this.handleException(e.getMessage(), e);
        }
        
    }

    public void denyUser(String userName, String resourceId, String action)
            throws UserStoreException {
        try {
            stub.denyUser(userName, resourceId, action);
        } catch (Exception e) {
            this.handleException(e.getMessage(), e);
        }
    }

    public String[] getAllowedRolesForResource(String resourceId, String action)
            throws UserStoreException {
        try {
            return stub.getAllowedRolesForResource(resourceId, action);
        } catch (Exception e) {
            this.handleException(e.getMessage(), e);
        }
        return null;
    }

    public String[] getAllowedUIResourcesForUser(String userName, String permissionRootPath)
            throws UserStoreException {
        try {
            return stub.getAllowedUIResourcesForUser(userName, permissionRootPath);
        } catch (Exception e) {
            this.handleException(e.getMessage(), e);
        }
        return null;
    }

    public String[] getDeniedRolesForResource(String resourceId, String action)
            throws UserStoreException {
        try {
            return stub.getDeniedRolesForResource(resourceId, action);
        } catch (Exception e) {
            this.handleException(e.getMessage(), e);
        }
        return null;
    }

    public String[] getExplicitlyAllowedUsersForResource(String resourceId, String action)
            throws UserStoreException {
        try {
            return stub.getExplicitlyAllowedUsersForResource(resourceId, action);
        } catch (Exception e) {
            this.handleException(e.getMessage(), e);
        }
        return null;
    }

    public String[] getExplicitlyDeniedUsersForResource(String resourceId, String action)
            throws UserStoreException {
        try {
            return stub.getExplicitlyDeniedUsersForResource(resourceId, action);
        } catch (Exception e) {
            this.handleException(e.getMessage(), e);
        }
        return null;
    }

    public boolean isRoleAuthorized(String roleName, String resourceId, String action)
            throws UserStoreException {
        try {
            return stub.isRoleAuthorized(roleName, resourceId, action);
        } catch (Exception e) {
            this.handleException(e.getMessage(), e);
        }
        return false;
    }

    public boolean isUserAuthorized(String userName, String resourceId, String action)
            throws UserStoreException {
        try {
            return stub.isUserAuthorized(userName, resourceId, action);
        } catch (Exception e) {
            this.handleException(e.getMessage(), e);
        }
        return false;
    }

    public void resetPermissionOnUpdateRole(String roleName, String newRoleName)
            throws UserStoreException {
        try {
            stub.resetPermissionOnUpdateRole(roleName, newRoleName);
        } catch (Exception e) {
            this.handleException(e.getMessage(), e);
        }
    }

	public int getTenantId() throws UserStoreException {
		try {
            //return stub.getTenantId();
        } catch (Exception e) {
            this.handleException(e.getMessage(), e);
        }
		return 0;
	}



	private String[] handleException(String msg, Exception e) throws UserStoreException {
        log.error(e.getMessage(), e);
        throw new UserStoreException(msg, e);
    }


	@Override
    public String[] normalizeRoles(String[] roles) {
	    return roles;
    }
    

}
