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
/*
 * Copyright - WSO, Inc. (http://wso.com)
 *
 * Licensed under the Apache License, Version .0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.um.ws.api;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.carbon.um.ws.api.stub.PermissionDTO;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceStub;
import org.wso2.carbon.user.api.ClaimManager;
import org.wso2.carbon.user.api.Properties;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.Permission;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.core.tenant.Tenant;

import java.util.Date;
import java.util.Map;

public class WSUserStoreManager implements UserStoreManager {

    private RemoteUserStoreManagerServiceStub stub = null;

    private static Log log = LogFactory.getLog(WSUserStoreManager.class);

    public WSUserStoreManager(String serverUrl, String cookie, ConfigurationContext configCtxt)
                                                                                               throws UserStoreException {
        try {
            stub =
                   new RemoteUserStoreManagerServiceStub(configCtxt, serverUrl +
                                                                     "RemoteUserStoreManagerService");
            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } catch (AxisFault e) {
            handleException(e.getMessage(), e);
        }
    }

    public void addUser(String userName, Object credential, String[] roleList,
            Map<String, String> claims, String profileName, boolean requirePasswordChange)
            throws UserStoreException {
        try {
            if (!(credential instanceof String)) {
                throw new UserStoreException("Unsupported type of password");
            }
            String password = (String) credential;
            ClaimValue[] claimValues = WSRealmUtil.convertMapToClaimValue(claims);
            stub.addUser(userName, password, roleList, claimValues, profileName,
                    requirePasswordChange);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    public void addRole(String roleName, String[] userList, Permission[] permissions)
            throws UserStoreException {
        try {
            stub.addRole(roleName, userList, convertPermission(permissions));
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }

    }

    public void addUser(String userName, Object credential, String[] roleList,
            Map<String, String> claims, String profileName) throws UserStoreException {
        if (!(credential instanceof String)) {
            throw new UserStoreException("Unsupported type of password");
        }
        try {
            stub.addUser(userName, (String) credential, roleList, WSRealmUtil
                    .convertMapToClaimValue(claims), profileName, false);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }

    }

    public boolean authenticate(String userName, Object credential) throws UserStoreException {
        if (!(credential instanceof String)) {
            throw new UserStoreException("Unsupported type of password");
        }
        try {
            return stub.authenticate(userName, (String) credential);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return false;
    }

    public void deleteRole(String roleName) throws UserStoreException {
        try {
            stub.deleteRole(roleName);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }

    }

    public void deleteUser(String userName) throws UserStoreException {
        try {
            stub.deleteUser(userName);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    public void deleteUserClaimValue(String userName, String claimURI, String profileName)
            throws UserStoreException {
        try {
            stub.deleteUserClaimValue(userName, claimURI, profileName);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    public void deleteUserClaimValues(String userName, String[] claims, String profileName)
            throws UserStoreException {
        try {
            stub.deleteUserClaimValues(userName, claims, profileName);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }

    }

    public String[] getAllProfileNames() throws UserStoreException {
        try {
            return stub.getAllProfileNames();
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return null;
    }

    public String[] getHybridRoles() throws UserStoreException {
        try {
            return stub.getHybridRoles();
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return null;
    }

    public String[] getAllSecondaryRoles() throws UserStoreException {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Date getPasswordExpirationTime(String username) throws UserStoreException {
        try {
            long time = stub.getPasswordExpirationTime(username);
            if(time != -1) {
                return new Date(time);
            }
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return null;
    }

    public String[] getProfileNames(String userName) throws UserStoreException {
        try {
            return stub.getProfileNames(userName);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return null;
    }

    public String[] getRoleListOfUser(String userName) throws UserStoreException {
        try {
            return stub.getRoleListOfUser(userName);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return null;
    }

    public String[] getRoleNames() throws UserStoreException {
        try {
            return stub.getRoleNames();
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return null;
    }

    public String[] getRoleNames(boolean b) throws UserStoreException {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getTenantId() throws UserStoreException {
        try {
            return stub.getTenantId();
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return -1;
    }

    public int getTenantId(String username) throws UserStoreException {
        try {
            return stub.getTenantIdofUser(username);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return -1;
    }

    public String getUserClaimValue(String userName, String claim, String profileName)
            throws UserStoreException {
        try {
            return stub.getUserClaimValue(userName, claim, profileName);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return profileName;
    }

    public Claim[] getUserClaimValues(String userName, String profileName)
            throws UserStoreException {
        try {
            return WSRealmUtil.convertToClaims(stub.getUserClaimValues(userName, profileName));
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return null;
    }

    public Map<String, String> getUserClaimValues(String userName, String[] claims,
            String profileName) throws UserStoreException {
        try {
            return WSRealmUtil.
                convertClaimValuesToMap(stub.getUserClaimValuesForClaims(userName, claims, profileName));
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return null;
    }

    public int getUserId(String username) throws UserStoreException {
        try {
            return stub.getUserId(username);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return -1;
    }

    public String[] getUserListOfRole(String roleName) throws UserStoreException {
        try {
            return stub.getUserListOfRole(roleName);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return null;
    }

    public boolean isExistingRole(String roleName, boolean isSharedRole) throws UserStoreException {

        try {
            return stub.isExistingRole(roleName);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return false;
    }

    public boolean isExistingUser(String userName) throws UserStoreException {

        try {
            return stub.isExistingUser(userName);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return false;
    }

    public boolean isReadOnly() throws UserStoreException {

        try {
            return stub.isReadOnly();
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return false;
    }

    public String[] listUsers(String filter, int maxItemLimit) throws UserStoreException {

        try {
            return stub.listUsers(filter, maxItemLimit);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return null;
    }

    public void setUserClaimValue(String userName, String claimURI, String claimValue,
            String profileName) throws UserStoreException {

        try {
            stub.setUserClaimValue(userName, claimURI, claimValue, profileName);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    public void setUserClaimValues(String userName, Map<String, String> claims, String profileName)
            throws UserStoreException {
        try {
            stub.setUserClaimValues(userName, WSRealmUtil.convertMapToClaimValue(claims),
                    profileName);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    public void addUserClaimValue(String userName, String claimURI, String claimValue,
                                  String profileName) throws UserStoreException {

        try {
            stub.addUserClaimValue(userName, claimURI, claimValue, profileName);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    public void addUserClaimValues(String userName, Map<String, String> claims, String profileName)
            throws UserStoreException {
        try {
            stub.addUserClaimValues(userName, WSRealmUtil.convertMapToClaimValue(claims),
                    profileName);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    public void updateCredential(String userName, Object newCredential, Object oldCredential)
            throws UserStoreException {
        if (!(newCredential instanceof String) || !(oldCredential instanceof String)) {
            throw new UserStoreException("Unsupported type of password");
        }
        try {
            stub.updateCredential(userName, (String) newCredential, (String) oldCredential);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    public void updateCredentialByAdmin(String userName, Object newCredential)
            throws UserStoreException {
        if (!(newCredential instanceof String)) {
            throw new UserStoreException("Unsupported type of password");
        }

        try {
            stub.updateCredentialByAdmin(userName, (String) newCredential);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    public void updateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles)
            throws UserStoreException {
        try {
            stub.updateRoleListOfUser(userName, deletedRoles, newRoles);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    public void updateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers)
            throws UserStoreException {
        try {
            stub.updateUserListOfRole(roleName, deletedUsers, newUsers);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    public void updateRoleName(String roleName, String newRoleName) throws UserStoreException {
        try {
            stub.updateRoleName(roleName, newRoleName);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }

    }

    /**
     * This method is to check whether multiple profiles are allowed with a particular user-store.
     * For an example, currently, JDBC user store supports multiple profiles and where as ApacheDS
     * does not allow.
     * @return
     */
    public boolean isMultipleProfilesAllowed() {
        return true;
    }

    private PermissionDTO[] convertPermission(Permission[] permissions) {
        if (permissions == null) {
            return null;
        }
        PermissionDTO[] perms = new PermissionDTO[permissions.length];
        for (int i = 0; i < permissions.length; i++) {
            perms[i] = new org.wso2.carbon.um.ws.api.stub.PermissionDTO();
            perms[i].setAction(permissions[i].getAction());
            perms[i].setResourceId(permissions[i].getResourceId());
        }
        return perms;

    }

    private String[] handleException(String msg, Exception e) throws UserStoreException {
        log.error(e.getMessage(), e);
        throw new UserStoreException(msg, e);
    }

    public Map<String, String> getProperties(Tenant tenant) throws UserStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    public void addRole(String roleName, String[] userList,
                        org.wso2.carbon.user.api.Permission[] permissions, boolean isSharedRole)
            throws org.wso2.carbon.user.core.UserStoreException {
        addRole(roleName, userList, Permission[].class.cast(permissions));

    }

    public Map<String, String> getProperties(org.wso2.carbon.user.api.Tenant tenant)
            throws org.wso2.carbon.user.core.UserStoreException {
        return getProperties(Tenant.class.cast(tenant));
    }

    public void addRememberMe(String userName, String token)
            throws org.wso2.carbon.user.api.UserStoreException {
        // TODO Auto-generated method stub
        
    }

    public boolean isValidRememberMeToken(String userName, String token)
            throws org.wso2.carbon.user.api.UserStoreException {
        // TODO Auto-generated method stub
        return false;
    }

    public ClaimManager getClaimManager() throws org.wso2.carbon.user.api.UserStoreException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isSCIMEnabled() throws org.wso2.carbon.user.api.UserStoreException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isBulkImportSupported() throws UserStoreException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String[] getUserList(String claim, String claimValue, String profileName)
                                                                        throws UserStoreException {
        try {
            return stub.getUserList(claim, claimValue, profileName);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        
        return null;
    }

    public UserStoreManager getSecondaryUserStoreManager() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public UserStoreManager getSecondaryUserStoreManager(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addSecondaryUserStoreManager(String s, UserStoreManager userStoreManager) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setSecondaryUserStoreManager(UserStoreManager userStoreManager) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public RealmConfiguration getRealmConfiguration() {
        return null;
    }

    public Properties getDefaultUserStoreProperties() {
	    return null;
    }

	@Override
    public void addRole(String roleName, String[] userList,
                        org.wso2.carbon.user.api.Permission[] permissions)
                                                                          throws org.wso2.carbon.user.api.UserStoreException {
		addRole(roleName, userList, permissions, false);
    }

	@Override
    public boolean isExistingRole(String roleName) throws UserStoreException {
	    return isExistingRole(roleName, false);
    }

    public boolean isSharedGroupEnabled() {
	    return false;
    }

}
