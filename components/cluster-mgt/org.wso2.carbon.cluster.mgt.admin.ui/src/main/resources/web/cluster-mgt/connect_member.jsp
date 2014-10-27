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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.cluster.mgt.admin.ui.AuthenticationAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%

    String backendURL = request.getParameter("backendURL");

    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String username = request.getParameter("username");
    String password = request.getParameter("password");

    boolean loggedIn;
    try {
        loggedIn = new AuthenticationAdminClient(backendURL,
                                                 configContext, request.getLocale()).login(request,
                                                                                           username,
                                                                                           password);
    } catch (Exception e) {
        session.setAttribute("loginFailed", "true");
        session.setAttribute("memberHostName", request.getParameter("memberHostName"));
        session.setAttribute("groupName", request.getParameter("groupName"));
        session.setAttribute("backendURL", backendURL);
%>
        <jsp:include page="login_to_member.jsp"/>
<%
        return;
    }
    if (!loggedIn) {
        session.setAttribute("loginFailed", "true");
        session.setAttribute("memberHostName", request.getParameter("memberHostName"));
        session.setAttribute("groupName", request.getParameter("groupName"));
        session.setAttribute("backendURL", backendURL);
%>
        <jsp:include page="login_to_member.jsp"/>
<%
        return;
    } else {
        session.setAttribute(CarbonConstants.SERVER_URL, backendURL);
        CarbonUIUtil.removeMenuDefinition("cluster_menu",request);
%>
<div id="middle">
    <div id="workArea">
        <h2>WSO2 Carbon Server (<%= backendURL %>)</h2>
        <jsp:include page="../server-admin/system_status_ajaxprocessor.jsp"/>
    </div>
</div>
<%
    }
%>