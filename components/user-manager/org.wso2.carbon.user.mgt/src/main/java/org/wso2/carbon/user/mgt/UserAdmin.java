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
package org.wso2.carbon.user.mgt;

import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataHandler;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.mgt.common.*;

public class UserAdmin {

    private static Log log = LogFactory.getLog(UserAdmin.class);

    private static Log audit = CarbonConstants.AUDIT_LOG;
    
    private static String AUDIT_MESSAGE = "Initiator : %s | Action : %s | Target : %s | Data : { %s } | Result : %s ";

    public UserAdmin() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.wso2.carbon.user.mgt.UserAdmin#listInternalUsers(java.lang.String)
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.user.mgt.TestClass#listUsers(java.lang.String)
     */
	public String[] listUsers(String filter, int limit) throws UserAdminException {
		String[] users;
		users = getUserAdminProxy().listUsers(filter, limit);
		return users;
	}

    /**
     * 
     * @param filter
     * @param limit
     * @return
     * @throws UserAdminException
     */
	public FlaggedName[] listAllUsers(String filter, int limit) throws UserAdminException {
		FlaggedName[] names;
		names = getUserAdminProxy().listAllUsers(filter, limit);
		return names;
	}

    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.user.mgt.UserAdmin#getInternalRoles()
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.user.mgt.TestClass#getAllRolesNames()
     */
    public FlaggedName[] getAllRolesNames(String filter, int limit) throws UserAdminException {
        return  getUserAdminProxy().getAllRolesNames(filter, limit);
    }  
    
    
    public FlaggedName[] getAllSharedRoleNames(String filter, int limit) throws UserAdminException {
        return  getUserAdminProxy().getAllRolesNames(filter, limit);
    }  
    

    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.user.mgt.TestClass#isWritable()
     */
    public UserRealmInfo getUserRealmInfo() throws UserAdminException {
        return  getUserAdminProxy().getUserRealmInfo();
    }    ///

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.wso2.carbon.user.mgt.UserAdmin#addUserToInternalStore(java.lang.String
     * , java.lang.String, java.lang.String[])
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.user.mgt.TestClass#addUser(java.lang.String,
     * java.lang.String, java.lang.String[], java.util.Map, java.lang.String)
     */
	public void addUser(String userName, String password, String[] roles, ClaimValue[] claims,
			String profileName) throws UserAdminException {
		String result = null;
		try {
			getUserAdminProxy().addUser(userName, password, roles, claims, profileName);
			result = "Success";
		} catch (UserAdminException e) {
			result = "Failed";
			throw e;
		} finally {
			// ################### <Audit Logs> ##########################################
			StringBuilder builder = new StringBuilder();
			if (roles != null) {
				for (int i = 0; i < roles.length; i++) {
					builder.append(roles[i] + ",");
				}
			}
			audit.info(String.format(AUDIT_MESSAGE, getUser(), "Add User", userName, "Roles :"
					+ builder.toString(), result));
			// ################### </Audit Logs> ##########################################
		}
	}

    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.user.mgt.UserAdmin#changePassword(java.lang.String,
     * java.lang.String)
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.user.mgt.TestClass#changePassword(java.lang.String,
     * java.lang.String)
     */
    public void changePassword(String userName, String newPassword) throws UserAdminException {

		String result = null;

		try {
			getUserAdminProxy().changePassword(userName, newPassword);
			result = "Success";
		} catch (UserAdminException e) {
			result = "Failed";
			throw e;
		} finally {
			// ################### <Audit Logs> ##########################################
			audit.info(String.format(AUDIT_MESSAGE, getUser(), "Change Password by Administrator",
					userName, "", result));
			// ################### </Audit Logs> ##########################################
		}
		
	}

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.wso2.carbon.user.mgt.UserAdmin#deleteUserFromInternalStore(java.lang
     * .String)
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.user.mgt.TestClass#deleteUser(java.lang.String)
     */
	public void deleteUser(String userName) throws UserAdminException {

		String result = null;

		try {
			getUserAdminProxy().deleteUser(userName,
					CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.USER_CONFIGURATION));
			result = "Success";
		} catch (UserAdminException e) {
			result = "Failed";
			throw e;
		} finally {
			// ################### <Audit Logs> ##########################################
			audit.info(String.format(AUDIT_MESSAGE, getUser(), "Delete User",
					userName, "", result));
			// ################### </Audit Logs> ##########################################
		}

	}

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.wso2.carbon.user.mgt.UserAdmin#addRoleToInternalStore(java.lang.String
     * , java.lang.String[], java.lang.String[])
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.user.mgt.TestClass#addRole(java.lang.String,
     * java.lang.String[], java.util.Map)
     */
    public void addRole(String roleName, String[] userList, String[] permissions, boolean isSharedRole)
            throws UserAdminException {
		String result = null;

		try {
			UserRealm realm = (UserRealm) CarbonContext.getThreadLocalCarbonContext().getUserRealm();
			if (!isAllowedRoleName(roleName, realm)) {
				throw new UserAdminException("Role name is reserved by the system");
			}
			getUserAdminProxy().addRole(roleName, userList, permissions, isSharedRole);
			result = "Success";
		} catch (UserAdminException e) {
			result = "Failed";
			throw e;
		} finally {
			
			StringBuilder users = new StringBuilder();
			if (userList != null) {
				for (int i = 0; i < userList.length; i++) {
					users.append(userList[i] + ",");
				}
			}

			StringBuilder perms = new StringBuilder();
			if (permissions != null) {
				for (int i = 0; i < permissions.length; i++) {
					perms.append(permissions[i] + ",");
				}
			}

			// ################### <Audit Logs> ##########################################
			audit.info(String.format(AUDIT_MESSAGE, getUser(), "Add Role", roleName, "Users : "
					+ users.toString() + " Permissions : " + permissions.toString(), result));
			// ################### </Audit Logs> ##########################################
		}
	}

    /**
     * 
     * @param roleName
     * @param userList
     * @param permissions
     * @throws UserAdminException
     */
	public void addInternalRole(String roleName, String[] userList, String[] permissions)
			throws UserAdminException {
		String result = null;

		try {
			UserRealm realm = (UserRealm) CarbonContext.getThreadLocalCarbonContext().getUserRealm();
			if (!isAllowedRoleName(roleName, realm)) {
				throw new UserAdminException("Role name is reserved by the system");
			}
			getUserAdminProxy().addInternalRole(roleName, userList, permissions);
			result = "Success";
		} catch (UserAdminException e) {
			result = "Failed";
			throw e;
		} finally {

			StringBuilder users = new StringBuilder();
			if (userList != null) {
				for (int i = 0; i < userList.length; i++) {
					users.append(userList[i] + ",");
				}
			}

			StringBuilder perms = new StringBuilder();
			if (permissions != null) {
				for (int i = 0; i < permissions.length; i++) {
					perms.append(permissions[i] + ",");
				}
			}

			// ################### <Audit Logs> ##########################################
			audit.info(String.format(AUDIT_MESSAGE, getUser(), "Add Internal Role", roleName, "Users : "
					+ users.toString() + " Permissions : " + permissions.toString(), result));
			// ################### </Audit Logs> ##########################################
		}
	}
    
	/**
	 * 
	 * @param roleName
	 * @param realm
	 * @return
	 * @throws UserAdminException
	 */
	private boolean isAllowedRoleName(String roleName, UserRealm realm) throws UserAdminException {

        int index;
		index = roleName.indexOf("/");
		
		if (index > 0) {
			roleName = roleName.substring(index + 1);
		}

		try {
			return !realm.getRealmConfiguration().isReservedRoleName(roleName);
		} catch (UserStoreException e) {
			throw new UserAdminException(e.getMessage(), e);
		}
	}
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.wso2.carbon.user.mgt.UserAdmin#deleteRoleFromInternalStore(java.lang
     * .String)
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.user.mgt.TestClass#deleteRole(java.lang.String)
     */
	public void deleteRole(String roleName) throws UserAdminException {
		String result = null;

		try {
			getUserAdminProxy().deleteRole(roleName);
			result = "Success";
		} catch (UserAdminException e) {
			result = "Failed";
			throw e;
		} finally {

			// ################### <Audit Logs> ##########################################
			audit.info(String.format(AUDIT_MESSAGE, getUser(), "Delete Role", roleName, "",
					result));
			// ################### </Audit Logs> ##########################################
		}
	}

	/**
	 * 
	 * @param roleName
	 * @param newRoleName
	 * @throws UserAdminException
	 */
	public void updateRoleName(String roleName, String newRoleName) throws UserAdminException {
		String result = null;

		try {
			getUserAdminProxy().updateRoleName(roleName, newRoleName);
			result = "Success";
		} catch (UserAdminException e) {
			result = "Failed";
			throw e;
		} finally {

			// ################### <Audit Logs> ##########################################
			audit.info(String.format(AUDIT_MESSAGE, getUser(), "Update Role Name", roleName,
					"Old : " + roleName + " New : " + newRoleName, result));
			// ################### </Audit Logs> ##########################################
		}
	}

	/**
	 * 
	 * @return
	 * @throws UserAdminException
	 */
    public boolean hasMultipleUserStores() throws UserAdminException{
        return getUserAdminProxy().hasMultipleUserStores();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.user.mgt.UserAdmin#getUsersInRole(java.lang.String)
     */
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.wso2.carbon.user.mgt.TestClass#getUsersInfoOfRole(java.lang.String,
     * java.lang.String)
     */
    public FlaggedName[] getUsersOfRole(String roleName, String filter, int limit) throws UserAdminException {
        return getUserAdminProxy().getUsersOfRole(roleName, filter, limit);
    } 

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.wso2.carbon.user.mgt.UserAdmin#updateUsersOfRole(java.lang.String,
     * java.lang.String[], java.lang.String[])
     */
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.wso2.carbon.user.mgt.TestClass#updateUsersOfRole(java.lang.String,
     * java.lang.String[], java.lang.String[])
     */
	public void updateUsersOfRole(String roleName, FlaggedName[] userList)
			throws UserAdminException {
		String result = null;

		try {
			getUserAdminProxy().updateUsersOfRole(roleName, userList);
			result = "Success";
		} catch (UserAdminException e) {
			result = "Failed";
			throw e;
		} finally {
			StringBuilder users = new StringBuilder();
			if (userList != null) {
				for (int i = 0; i < userList.length; i++) {
					if (userList[i] != null) {
						users.append(userList[i].getItemName() + ",");
					}
				}
			}

			// ################### <Audit Logs> ##########################################
			audit.info(String.format(AUDIT_MESSAGE, getUser(), "Update Users of Role", roleName,
					"Users : " + users.toString(), result));
			// ################### </Audit Logs> ##########################################
		}
	}

    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.user.mgt.UserAdmin#getUsersInRole(java.lang.String)
     */
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.wso2.carbon.user.mgt.TestClass#getRoleInfoOfUser(java.lang.String)
     */
    public FlaggedName[] getRolesOfUser(String userName, String filter, int limit) throws UserAdminException {
        return getUserAdminProxy().getRolesOfUser(userName, filter, limit);
    }

    // FIXME: Fix the documentation of this class including this.
    public FlaggedName[] getRolesOfCurrentUser() throws UserAdminException {
        return getRolesOfUser(CarbonContext.getThreadLocalCarbonContext().getUsername(), "*", -1);
    }   ///

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.wso2.carbon.user.mgt.TestClass#updateRolesOfUser(java.lang.String,
     * java.lang.String)
     */
	public void updateRolesOfUser(String userName, String[] newUserList) throws UserAdminException {
		String result = null;

		try {
			getUserAdminProxy().updateRolesOfUser(userName, newUserList);
			result = "Success";
		} catch (UserAdminException e) {
			result = "Failed";
			throw e;
		} finally {
			StringBuilder roles = new StringBuilder();
			if (newUserList != null) {
				for (int i = 0; i < newUserList.length; i++) {
					if (newUserList[i] != null) {
						roles.append(newUserList[i] + ",");
					}
				}
			}

			// ################### <Audit Logs> ##########################################
			audit.info(String.format(AUDIT_MESSAGE, getUser(), "Update Users of Role", userName,
					"Roles : " + roles.toString(), result));
			// ################### </Audit Logs> ##########################################
		}
	}

	/**
	 * 
	 * @return
	 * @throws UserAdminException
	 */
    public UIPermissionNode getAllUIPermissions() throws UserAdminException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        return getUserAdminProxy().getAllUIPermissions(tenantId);
    }

    /**
     * 
     * @param roleName
     * @return
     * @throws UserAdminException
     */
    public UIPermissionNode getRolePermissions(String roleName) throws UserAdminException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        return getUserAdminProxy().getRolePermissions(roleName, tenantId);
    }

    /**
     * 
     * @param roleName
     * @param rawResources
     * @throws UserAdminException
     */
	public void setRoleUIPermission(String roleName, String[] rawResources)
			throws UserAdminException {
		String result = null;

		try {
			getUserAdminProxy().setRoleUIPermission(roleName, rawResources);
			result = "Success";
		} catch (UserAdminException e) {
			result = "Failed";
			throw e;
		} finally {
			StringBuilder permissions = new StringBuilder();
			if (rawResources != null) {
				for (int i = 0; i < rawResources.length; i++) {
					if (rawResources[i] != null) {
						permissions.append(rawResources[i] + ",");
					}
				}
			}

			// ################### <Audit Logs> ##########################################
			audit.info(String.format(AUDIT_MESSAGE, getUser(), "Set Role UI Permissions", roleName,
					"Permissions : " + permissions.toString(), result));
			// ################### </Audit Logs> ##########################################
		}
	}

	/**
	 * 
	 * @param fileName
	 * @param handler
	 * @param defaultPassword
	 * @throws UserAdminException
	 */
    public void bulkImportUsers(String fileName, DataHandler handler, String defaultPassword)
            throws UserAdminException {
        if(fileName == null || handler == null || defaultPassword == null) {
            throw new UserAdminException("Required data not provided");
        }
        try {
            InputStream inStream = handler.getInputStream();
            getUserAdminProxy().bulkImportUsers(fileName, inStream, defaultPassword);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        }

    }

    /**
     * 
     * @param oldPassword
     * @param newPassword
     * @throws UserAdminException
     * @throws AxisFault
     */
	public void changePasswordByUser(String oldPassword, String newPassword)
			throws UserAdminException, AxisFault {
		String result = null;

		try {
			getUserAdminProxy().changePasswordByUser(oldPassword, newPassword);
			result = "Success";
		} catch (UserAdminException e) {
			result = "Failed";
			throw e;
		} finally {

			// ################### <Audit Logs> ##########################################
			audit.info(String.format(AUDIT_MESSAGE, getUser(), "Change Password by User",
					getUser(), "", result));
			// ################### </Audit Logs> ##########################################
		}
	}


	/**
	 * 
	 * @param roleName
	 * @param newUsers
	 * @param deletedUsers
	 * @throws UserAdminException
	 */
	public void addRemoveUsersOfRole(String roleName, String[] newUsers, String[] deletedUsers)
			throws UserAdminException {
		String result = null;

		try {
			getUserAdminProxy().updateUsersOfRole(roleName, newUsers, deletedUsers);
			result = "Success";
		} catch (UserAdminException e) {
			result = "Failed";
			throw e;
		} finally {

			StringBuilder newUsersList = new StringBuilder();
			if (newUsers != null) {
				for (int i = 0; i < newUsers.length; i++) {
					newUsersList.append(newUsers[i] + ",");
				}
			}

			StringBuilder deletedUsersList = new StringBuilder();
			if (deletedUsers != null) {
				for (int i = 0; i < deletedUsers.length; i++) {
					deletedUsersList.append(deletedUsers[i] + ",");
				}
			}

			// ################### <Audit Logs> ##########################################
			audit.info(String.format(AUDIT_MESSAGE, getUser(), "Add/Remove Users from Role",
					roleName, "New Users : " + newUsersList.toString() + " Deleted Users : "
							+ deletedUsersList.toString(), result));
			// ################### </Audit Logs> ##########################################
		}
	}

	/**
	 * 
	 * @param userName
	 * @param newRoles
	 * @param deletedRoles
	 * @throws UserAdminException
	 */
	public void addRemoveRolesOfUser(String userName, String[] newRoles, String[] deletedRoles)
			throws UserAdminException {
		String result = null;

		try {
			getUserAdminProxy().updateRolesOfUser(userName, newRoles, deletedRoles);
			result = "Success";
		} catch (UserAdminException e) {
			result = "Failed";
			throw e;
		} finally {
			StringBuilder newRolesList = new StringBuilder();
			if (newRoles != null) {
				for (int i = 0; i < newRoles.length; i++) {
					newRolesList.append(newRoles[i] + ",");
				}
			}

			StringBuilder deletedUsersList = new StringBuilder();
			if (deletedRoles != null) {
				for (int i = 0; i < deletedRoles.length; i++) {
					deletedUsersList.append(deletedRoles[i] + ",");
				}
			}

			// ################### <Audit Logs> ##########################################
			audit.info(String.format(AUDIT_MESSAGE, getUser(), "Add/Remove Roles from User",
					userName, "New Roles : " + newRolesList.toString() + " Deleted Roles : "
							+ deletedUsersList.toString(), result));
			// ################### </Audit Logs> ##########################################
		}
	}

	
	/**
	 * 
	 * @param claimValue
	 * @param filter
	 * @param maxLimit
	 * @return
	 * @throws UserAdminException
	 */
	public FlaggedName[] listUserByClaim(ClaimValue claimValue, String filter, int maxLimit)
			throws UserAdminException {
		return getUserAdminProxy().listUsers(claimValue, filter, maxLimit);
	}

	/**
	 * 
	 * @return
	 */
	private UserRealmProxy getUserAdminProxy() {
		UserRealm realm = (UserRealm) CarbonContext.getThreadLocalCarbonContext().getUserRealm();
		return new UserRealmProxy(realm);
	}

	/**
	 * 
	 * @return
	 */
    private String getUser(){
        return CarbonContext.getThreadLocalCarbonContext().getUsername() + "@" +
                CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }
    
    public boolean isSharedRolesEnabled() throws UserAdminException{
    	return  getUserAdminProxy().isSharedRolesEnabled();
    }
}
