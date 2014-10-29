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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.cluster.mgt.admin.ui.ClusterAdminClient" %>

<%
    String groupName = request.getParameter("groupName");
    ConfigurationContext ctx = (ConfigurationContext) config.getServletContext()
            .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String backendServerURL = CarbonUIUtil
            .getServerURL(config.getServletContext(), session);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String action = request.getParameter("action");
    ClusterAdminClient client = new ClusterAdminClient(cookie, backendServerURL,
                                                       ctx, request.getLocale());

    try {
        if ("restart".equals(action)) {
            if (groupName != null) {
                client.restartGroup(groupName);
            } else {
                client.restartCluster();
            }
        } else if ("restartGracefully".equals(action)) {
            if (groupName != null) {
                client.restartGroupGracefully(groupName);
            } else {
                client.restartClusterGracefully();
            }
        } else if ("shutdown".equals(action)) {
            if (groupName != null) {
                client.shutdownGroup(groupName);
            } else {
                client.shutdownCluster();
            }
        } else if ("shutdownGracefully".equals(action)) {
            if (groupName != null) {
                client.shutdownGroupGracefully(groupName);
            } else {
                client.shutdownClusterGracefully();
            }
        } else if ("startMaintenance".equals(action)){
            if (groupName != null) {
                client.startGroupMaintenance(groupName);
            } else {
                client.startClusterMaintenance();
            }
        } else if ("endMaintenance".equals(action)){
           if (groupName != null) {
                client.endGroupMaintenance(groupName);
            } else {
                client.endClusterMaintenance();
            }
        } else {
            String msg = "Unknown cluster management action: " + action;
            System.err.println(msg);
            throw new ServletException(msg);
        }
    } catch (Exception e) {
%>
<fmt:bundle basename="org.wso2.carbon.cluster.mgt.admin.ui.i18n.Resources">
    <fmt:message key="error.msg"><fmt:param value="<%= e.getMessage()%>"/></fmt:message>
</fmt:bundle>
<%
    }
%>