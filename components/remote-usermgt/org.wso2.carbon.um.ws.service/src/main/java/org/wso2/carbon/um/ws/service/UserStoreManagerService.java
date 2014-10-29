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

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.um.ws.service.dao.ClaimDTO;
import org.wso2.carbon.um.ws.service.dao.PermissionDTO;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.user.mgt.common.ClaimValue;

public class UserStoreManagerService extends AbstractAdmin {

    private static Log log = LogFactory.getLog(UserStoreManagerService.class.getClass());

    public void addUser(String userName, String credential, String[] roleList, ClaimValue[] claims,
            String profileName, boolean requirePasswordChange) throws UserStoreException {
        getUserStoreManager().addUser(userName, credential, roleList,
                convertClaimValueToMap(claims), profileName, requirePasswordChange);
    }

    public void setUserClaimValues(String userName, ClaimValue[] claims, String profileName)
            throws UserStoreException {
        getUserStoreManager().setUserClaimValues(userName, convertClaimValueToMap(claims),
                profileName);

    }

    public void addUserClaimValues(String userName, ClaimValue[] claims, String profileName)
            throws UserStoreException {

        for(ClaimValue claim:claims){
            String existingClaimValue = getUserStoreManager().getUserClaimValue(userName, claim.getClaimURI(), profileName);
            if(existingClaimValue == null){
                existingClaimValue = "";
            }
            if(claim.getValue() != null && !claim.getValue().equals("")){
                String claimValue;
                if(!existingClaimValue.equals("")){
                    claimValue = existingClaimValue + "," + claim.getValue();
                } else {
                    claimValue = claim.getValue();
                }
                getUserStoreManager().setUserClaimValue(userName, claim.getClaimURI(), claimValue, profileName);
            }
        }
    }

    public ClaimValue[] getUserClaimValuesForClaims(String userName, String[] claims,
            String profileName) throws UserStoreException {
        return convertMapToClaimValue(getUserStoreManager().getUserClaimValues(userName, claims,
                profileName));
    }

    public void addRole(String roleName, String[] userList, PermissionDTO[] permissions)
            throws UserStoreException {
        try {
            getUserStoreManager().addRole(roleName, userList, convertDTOToPermission(permissions), false);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw (UserStoreException) e;
        }
    }

    public ClaimDTO [] getUserClaimValues(String userName, String profileName)
            throws UserStoreException {
        return convertClaimToClaimDTO(getUserStoreManager().getUserClaimValues(userName,
                profileName));
    }

    public boolean authenticate(String userName, String credential) throws UserStoreException {
        return getUserStoreManager().authenticate(userName, credential);
    }

    public void updateCredential(String userName, String newCredential, String oldCredential)
            throws UserStoreException {
        getUserStoreManager().updateCredential(userName, newCredential, oldCredential);

    }

    public void updateCredentialByAdmin(String userName, String newCredential)
            throws UserStoreException {
        getUserStoreManager().updateCredentialByAdmin(userName, newCredential);
    }

    public long getPasswordExpirationTime(String username) throws UserStoreException {
        Date date = getUserStoreManager().getPasswordExpirationTime(username);
        if (date != null) {
            return date.getTime();
        }
        return -1;
    }

    public void deleteRole(String roleName) throws UserStoreException {
        getUserStoreManager().deleteRole(roleName);

    }

    public void deleteUser(String userName) throws UserStoreException {
        getUserStoreManager().deleteUser(userName);

    }

    public void deleteUserClaimValue(String userName, String claimURI, String profileName)
            throws UserStoreException {
        getUserStoreManager().deleteUserClaimValue(userName, claimURI, profileName);

    }

    public void deleteUserClaimValues(String userName, String[] claims, String profileName)
            throws UserStoreException {
        getUserStoreManager().deleteUserClaimValues(userName, claims, profileName);

    }

    public String[] getAllProfileNames() throws UserStoreException {
        return getUserStoreManager().getAllProfileNames();
    }

    public String[] getHybridRoles() throws UserStoreException {
        return getUserStoreManager().getHybridRoles();
    }

    public String[] getProfileNames(String userName) throws UserStoreException {
        return getUserStoreManager().getProfileNames(userName);
    }

    public String[] getRoleListOfUser(String userName) throws UserStoreException {
        return getUserStoreManager().getRoleListOfUser(userName);
    }

    public String[] getRoleNames() throws UserStoreException {
        return getUserStoreManager().getRoleNames();
    }

    public int getTenantId() throws UserStoreException {
        return getUserStoreManager().getTenantId();
    }

    public String[] getUserList(String claimUri, String claimValue, String profile)
                                                                        throws UserStoreException {
        return getUserStoreManager().getUserList(claimUri, claimValue, profile);
    }

    public int getTenantIdofUser(String username) throws UserStoreException {

        if (Util.isSuperTenant()) {
            return getUserStoreManager().getTenantId(username);
        } else {
            StringBuilder stringBuilder
                    = new StringBuilder("Unauthorized attempt to execute super tenant operation by tenant domain - ");
            stringBuilder.append(CarbonContext.getThreadLocalCarbonContext().getTenantDomain()).append(" tenant id - ")
                    .append(CarbonContext.getThreadLocalCarbonContext().getTenantId()).append(" user - ")
                    .append(CarbonContext.getThreadLocalCarbonContext().getUsername());
            log.warn(stringBuilder.toString());

            throw new UserStoreException("Access Denied");
        }

    }

    public String getUserClaimValue(String userName, String claim, String profileName)
            throws UserStoreException {
        return getUserStoreManager().getUserClaimValue(userName, claim, profileName);
    }

    public int getUserId(String username) throws UserStoreException {
        return getUserStoreManager().getUserId(username);
    }

    public String[] getUserListOfRole(String roleName) throws UserStoreException {
        return getUserStoreManager().getUserListOfRole(roleName);
    }

    public boolean isExistingRole(String roleName) throws UserStoreException {
        return getUserStoreManager().isExistingRole(roleName);
    }

    public boolean isExistingUser(String userName) throws UserStoreException {
        return getUserStoreManager().isExistingUser(userName);
    }

    public boolean isReadOnly() throws UserStoreException {
        return getUserStoreManager().isReadOnly();
    }

    public String[] listUsers(String filter, int maxItemLimit) throws UserStoreException {
        return getUserStoreManager().listUsers(filter, maxItemLimit);
    }

    public void setUserClaimValue(String userName, String claimURI, String claimValue,
            String profileName) throws UserStoreException {
        getUserStoreManager().setUserClaimValue(userName, claimURI, claimValue, profileName);

    }

    public void addUserClaimValue(String userName, String claimURI, String claimValue, String profileName)
            throws UserStoreException {

        String existingClaimValue = getUserStoreManager().getUserClaimValue(userName, claimURI, profileName);
        if(existingClaimValue == null){
            existingClaimValue = "";
        }
        if(claimValue != null && !claimValue.equals("")){
            if(!existingClaimValue.equals("")){
                claimValue = existingClaimValue + "," + claimValue;
            }
        }
        getUserStoreManager().setUserClaimValue(userName, claimURI, claimValue, profileName);
    }

    public void updateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles)
            throws UserStoreException {
        getUserStoreManager().updateRoleListOfUser(userName, deletedRoles, newRoles);

    }

    public void updateRoleName(String roleName, String newRoleName) throws UserStoreException {
        getUserStoreManager().updateRoleName(roleName, newRoleName);
    }

    public void updateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers)
            throws UserStoreException {
        getUserStoreManager().updateUserListOfRole(roleName, deletedUsers, newUsers);
    }

    private UserStoreManager getUserStoreManager() throws UserStoreException {
        try {
            UserRealm realm = super.getUserRealm();
            if (realm == null) {
                throw new UserStoreException("UserRealm is null");
            }
            return realm.getUserStoreManager();
        } catch (Exception e) {
            throw new UserStoreException(e);
        }
    }

    private Map<String, String> convertClaimValueToMap(ClaimValue[] values) {
        Map<String, String> map = new HashMap<String, String>();
        for (ClaimValue claimValue : values) {
            map.put(claimValue.getClaimURI(), claimValue.getValue());
        }
        return map;
    }

    private ClaimValue[] convertMapToClaimValue(Map<String, String> map) {
        ClaimValue[] claims = new ClaimValue[map.size()];
        Iterator<Map.Entry<String, String>> ite = map.entrySet().iterator();
        int i = 0;
        while (ite.hasNext()) {
            Map.Entry<String, String> entry = ite.next();
            claims[i] = new ClaimValue();
            claims[i].setClaimURI(entry.getKey());
            claims[i].setValue(entry.getValue());
            i++;
        }
        return claims;
    }

    private ClaimDTO[] convertClaimToClaimDTO(Claim[] claims){

        List<ClaimDTO> ClaimDTOs = new ArrayList<ClaimDTO>();
        for(Claim claim : claims){
            ClaimDTO claimDTO = new ClaimDTO();
            claimDTO.setClaimUri(claim.getClaimUri());
            claimDTO.setValue(claim.getValue());
            claimDTO.setDescription(claim.getDescription());
            claimDTO.setDialectURI(claim.getDialectURI());
            claimDTO.setDisplayOrder(claim.getDisplayOrder());
            claimDTO.setRegEx(claim.getRegEx());
            claimDTO.setSupportedByDefault(claim.isSupportedByDefault());
            claimDTO.setRequired(claim.isRequired());
            ClaimDTOs.add(claimDTO);
        }
        return ClaimDTOs.toArray(new ClaimDTO[ClaimDTOs.size()]);
    }

    private Permission[] convertDTOToPermission(PermissionDTO[] permissionDTOs){

        List<Permission> permissions = new ArrayList<Permission>();
        for(PermissionDTO permissionDTO : permissionDTOs){
            Permission permission = new Permission(permissionDTO.getResourceId(),
                    permissionDTO.getAction());
            permissions.add(permission);
        }
        return permissions.toArray(new Permission[permissions.size()]);
    }

    public String[][] getProperties(Tenant tenant) throws UserStoreException {
        // TODO This method should only called by super tenant
        // Logic is not implemented yet

        if (!Util.isSuperTenant()) {
            StringBuilder stringBuilder
                    = new StringBuilder("Unauthorized attempt to execute super tenant operation by tenant domain - ");
            stringBuilder.append(CarbonContext.getThreadLocalCarbonContext().getTenantDomain()).append(" tenant id - ")
                    .append(CarbonContext.getThreadLocalCarbonContext().getTenantId()).append(" user - ")
                    .append(CarbonContext.getThreadLocalCarbonContext().getUsername());
            log.warn(stringBuilder.toString());

            throw new UserStoreException("Access Denied");
        }

        // TODO implement the logic
        return null;
    }

}
