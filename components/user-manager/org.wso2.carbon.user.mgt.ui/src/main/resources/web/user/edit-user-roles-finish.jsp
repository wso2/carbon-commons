<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@page import="org.apache.axis2.context.ConfigurationContext"%>
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@page import="org.wso2.carbon.user.mgt.ui.UserAdminClient"%>
<%@page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ page import="org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.UserAdminUIConstants" %>
<%@ page import="java.util.*" %>

<%
    boolean logout = false;
    boolean finish = false;
    boolean viewUsers = false;

    String BUNDLE = "org.wso2.carbon.userstore.ui.i18n.Resources";
	ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

	String username = CharacterEncoder.getSafeText(request.getParameter("username"));
    String disPlayName = CharacterEncoder.getSafeText(request.getParameter("disPlayName"));
    if(disPlayName == null || disPlayName.trim().length() == 0){
        disPlayName = username;
    }
	String[] selectedRoles = request.getParameterValues("selectedRoles");
	String[] shownRoles = request.getParameterValues("shownRoles");
    String pageNumber = CharacterEncoder.getSafeText(request.getParameter("pageNumber"));

    if(request.getParameter("logout") != null){
        logout = Boolean.parseBoolean(request.getParameter("logout"));
    }
    if(request.getParameter("finish") != null){
        finish = Boolean.parseBoolean(request.getParameter("finish"));
    }
    if(request.getParameter("viewRoles") != null){
        viewUsers = Boolean.parseBoolean(request.getParameter("viewRoles"));
    }

    try {
        String cookie = (String)session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        UserAdminClient client = new UserAdminClient(cookie, backendServerURL, configContext);
        ArrayList<String> deletedList = new ArrayList<String>();
        if(selectedRoles != null){
            Arrays.sort(selectedRoles);
        }

        if(shownRoles != null){
            for(String name : shownRoles){
                if(selectedRoles != null){
                    if(Arrays.binarySearch(selectedRoles, name) < 0){
                        deletedList.add(name);
                    }
                } else {
                    deletedList.add(name);
                }
            }
        }
        selectedRoles = addSelectedRoleLists(selectedRoles,(Map<String,Boolean>)session.getAttribute("checkedRolesMap"));
        addDeletedRoleLists(deletedList, (Map<String,Boolean>) session.getAttribute("checkedRolesMap"));

        if(viewUsers){
            client.addRemoveRolesOfUser(username, null,
                    deletedList.toArray(new String[deletedList.size()]));
        } else {
            client.addRemoveRolesOfUser(username, selectedRoles, null);
        }
       
        String message = MessageFormat.format(resourceBundle.getString("user.update"), username);
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.INFO, request);


        if(logout){
%>

<script type="text/javascript">
                window.location.href = "../admin/logout_action.jsp";
            </script>
<%
        } else if(finish) {
        	%>
        	<script type="text/javascript">
                location.href = "user-mgt.jsp?ordinal=1";
            </script>
        	<%
        } else if(viewUsers){
%>
            <script type="text/javascript">
                location.href = "view-roles.jsp?username=<%=username%>";
            </script>
<%
        } else {
%>
            <script type="text/javascript">
                location.href = "edit-user-roles.jsp?username=<%=username%>" + "&pageNumber=<%=pageNumber%>";
            </script>
<%
        }
    } catch (Exception e) {
         String message = MessageFormat.format(resourceBundle.getString("role.list.cannot.update"), username, e.getMessage());
         CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        if(viewUsers){
%>
            <script type="text/javascript">
                location.href = "view-roles.jsp?username=<%=username%>";
            </script>
<%
        } else {
%>
            <script type="text/javascript">
                location.href = "edit-user-roles.jsp?username=<%=username%>";
            </script>
<%
        }
    }
%>

<%!
    private String[] addSelectedRoleLists(String[] selectedRoles, Map<String,Boolean> sessionRolesMap){
        List<String> selectedRolesList = new ArrayList<String>();
        if(selectedRoles != null && selectedRoles.length > 0){
            selectedRolesList = new ArrayList<String>(Arrays.asList(selectedRoles));
        }
        if(sessionRolesMap != null){
            Set<String> keys = sessionRolesMap.keySet();
            for(String key:keys){
                if(sessionRolesMap.get(key) == true && !selectedRolesList.contains(key)){
                    selectedRolesList.add(key);
                }
            }
        }
        selectedRoles = selectedRolesList.toArray(new String[selectedRolesList.size()]);
        return selectedRoles;
    }

    private void addDeletedRoleLists(List<String> deletedRoles, Map<String,Boolean> sessionRolesMap){
        if(sessionRolesMap != null){
            Set<String> keys = sessionRolesMap.keySet();
            for(String key:keys){
                if(sessionRolesMap.get(key) == false && !deletedRoles.contains(key)){
                    deletedRoles.add(key);
                }
            }
        }
    }

%>