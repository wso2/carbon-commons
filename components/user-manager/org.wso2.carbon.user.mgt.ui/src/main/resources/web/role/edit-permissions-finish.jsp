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
<%@ page import="java.util.ResourceBundle" %>
<%@page import="org.wso2.carbon.ui.util.CharacterEncoder"%>

<%

    String BUNDLE = "org.wso2.carbon.userstore.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    String forwardTo = "role-mgt.jsp?ordinal=1";
    String roleName = request.getParameter("roleName");
    

    if(request.getParameter("prevPage")!=null && request.getParameter("prevUser")!=null){
        String prevPage = request.getParameter("prevPage");
        String prevUser = request.getParameter("prevUser");
        String prevPageNumber = request.getParameter("prevPageNumber");
        if("view".equals(prevPage)){
            forwardTo = "../user/view-roles.jsp?username="+prevUser + "&pageNumber=" + prevPageNumber;
        }else if("edit".equals(prevPage)){
            forwardTo = "../user/edit-user-roles.jsp?username="+prevUser + "&pageNumber=" +prevPageNumber ;
        }
    }

    try {
        String[] selectedPermissions = request.getParameterValues("selectedPermissions");
        String cookie = (String)session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        UserAdminClient client = new UserAdminClient(cookie, backendServerURL, configContext);
        String message = MessageFormat.format(resourceBundle.getString("role.update"), roleName);
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.INFO, request);
//        if("external".equals(userType)){
//            proxy.updateSystemPermissionsOfExternalRole(roleBeanEditPermission.getRoleName(),
//                                                         roleBeanEditPermission.getSelectedPermissions());
//            forwardTo = "../userstore/ex-role-mgt.jsp?ordinal=1";
//        }else if("special".equals(userType)){
//            proxy.updateSystemPermissionsOfSpecialInternalRole(roleBeanEditPermission.getRoleName(),
//                                                                roleBeanEditPermission.getSelectedPermissions());
//            forwardTo = "../userstore/ex-role-mgt.jsp?ordinal=1";
//        }else{
             client.setRoleUIPermission(roleName, selectedPermissions);
             //forwardTo = "role-mgt.jsp?ordinal=1";
//        }

    } catch(InstantiationException e){
        CarbonUIMessage.sendCarbonUIMessage("Your session has timed out. Please try again.",
                CarbonUIMessage.ERROR, request);
        //forwardTo = "role-mgt.jsp?ordinal=1";
    } catch (Exception e) {
	    String message = MessageFormat.format(resourceBundle.getString("role.cannot.update"),
                CharacterEncoder.getSafeText(roleName), e.getMessage());
%>
<script type="text/javascript">
    jQuery(document).ready(function () {
        CARBON.showErrorDialog('<%=message%>',  function () {
            location.href = "role-mgt.jsp";
        });
    });
</script>
<%
    }
%>

<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>
