package org.wso2.carbon.event.ui;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.um.ws.api.stub.RemoteAuthorizationManagerServiceStub;
import org.wso2.carbon.um.ws.api.stub.UserStoreExceptionException;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.carbon.user.mgt.ui.UserAdminClient;

public class AuthUtils {
    private static final Log log = LogFactory.getLog(AuthUtils.class);

    List<String> rolesWithAccess = new ArrayList<String>();
    List<String> usersWithAccess = new ArrayList<String>();

    List<String> rolesWithoutAccess = new ArrayList<String>();
    List<String> usersWithoutAccess = new ArrayList<String>();

    private UserAdminClient userAdminClient;
    private RemoteAuthorizationManagerServiceStub authClient;

    public AuthUtils(String resourceName, UserAdminClient userAdminClient,
            RemoteAuthorizationManagerServiceStub authClient) throws Exception {
        this.userAdminClient = userAdminClient;
        this.authClient = authClient;
        FlaggedName[] allRoles = userAdminClient.getAllRolesNames("*", -1);

        if (resourceName != null && resourceName.trim().length() > 0) {
            String[] rolesList = authClient.getAllowedRolesForResource(resourceName, "write");
            String[] usersList = authClient
                    .getExplicitlyAllowedUsersForResource(resourceName, "write");

            if (rolesList != null) {
                for (String role : rolesList) {
                    rolesWithAccess.add(role);
                }
            }
            if (usersList != null) {
                for (String user : usersList) {
                    usersWithAccess.add(user);
                }
            }
        }

        for (FlaggedName name : allRoles) {
            if (!rolesWithAccess.contains(name.getItemName())) {
                rolesWithoutAccess.add(name.getItemName());
            }
        }

        String[] allUsers = userAdminClient.listUsers("*", -1);
        for (String name : allUsers) {
            if (!usersWithAccess.contains(name)) {
                usersWithoutAccess.add(name);
            }
        }
    }

    public List<String> getRolesWithAccess() {
        return rolesWithAccess;
    }

    public void setRolesWithAccess(List<String> rolesWithAccess) {
        this.rolesWithAccess = rolesWithAccess;
    }

    public List<String> getUsersWithAccess() {
        return usersWithAccess;
    }

    public void setUsersWithAccess(List<String> usersWithAccess) {
        this.usersWithAccess = usersWithAccess;
    }

    public List<String> getRolesWithoutAccess() {
        return rolesWithoutAccess;
    }

    public void setRolesWithoutAccess(List<String> rolesWithoutAccess) {
        this.rolesWithoutAccess = rolesWithoutAccess;
    }

    public List<String> getUsersWithoutAccess() {
        return usersWithoutAccess;
    }

    public void setUsersWithoutAccess(List<String> usersWithoutAccess) {
        this.usersWithoutAccess = usersWithoutAccess;
    }

    public String updatePermissions(String resourcePath, String actorsToAddAsStr,
            String actorsToRemoveAsStr) throws RemoteException,
            UserStoreExceptionException {
        StringBuffer updateMessages = new StringBuffer();
        List<String> addRolesList = new ArrayList<String>(3);
        List<String> addUsersList = new ArrayList<String>(3);
        List<String> removeRolesList = new ArrayList<String>(3);
        List<String> removeUsersList = new ArrayList<String>(3);

        //** we parse the users roles from input
        if (actorsToAddAsStr != null && actorsToAddAsStr.trim().length() > 0) {
            String[] addTokens = actorsToAddAsStr.split("#");
            if (addTokens != null) {
                for (String token : addTokens) {
                    if (token.trim().length() > 0) {
                        String party = token.substring(0, token.indexOf("("));
                        if (token.contains("(U)")) {
                            addUsersList.add(party);
                        } else {
                            addRolesList.add(party);
                        }
                    }
                }
            }
        }

        if (actorsToRemoveAsStr != null
                && actorsToRemoveAsStr.trim().length() > 0) {
            String[] removeTokens = actorsToRemoveAsStr.split("#");
            if (removeTokens != null) {
                for (String token : removeTokens) {
                    if (token.trim().length() > 0) {
                        String party = token.substring(0, token.indexOf("("));
                        if (token.contains("(U)")) {
                            removeUsersList.add(party);
                        } else {
                            removeRolesList.add(party);
                        }
                    }
                }
            }
        }

        if (addUsersList.size() > 0) {
            for (String user : addUsersList) {
                authClient.authorizeUser(user, resourcePath, "write");
            }
            String msg = "Authorized " + addUsersList + " to access " + resourcePath;
            log.info(msg); updateMessages.append(msg);
        }
        if (addRolesList.size() > 0) {
            for (String role : addRolesList) {
                authClient.authorizeRole(role, resourcePath, "write");
            }
            String msg = "Authorized " + addRolesList + " to access " + resourcePath;
            log.info(msg); updateMessages.append(msg);
        }

        if (removeUsersList.size() > 0) {
            for (String user : removeUsersList) {
                authClient.clearUserAuthorization(user, resourcePath, "write");
            }
            String msg = "Deny " + removeUsersList + " to access " + resourcePath;
            log.info(msg); updateMessages.append(msg);
        }
        if (removeRolesList.size() > 0) {
            for (String role : removeRolesList) {
                authClient.clearRoleAuthorization(role, resourcePath, "write");
            }
            String msg = "Deny " + removeRolesList + " to access " + resourcePath;
            log.info(msg); updateMessages.append(msg);
        }
        return updateMessages.toString();
    }

}
