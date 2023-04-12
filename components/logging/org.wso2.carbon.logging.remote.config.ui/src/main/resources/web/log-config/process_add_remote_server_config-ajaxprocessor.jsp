<%--
~ Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
--%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.logging.remote.config.ui.RemoteLoggingConfigClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="java.util.regex.Pattern" %>
<%@ page import="java.util.regex.Pattern" %>
<%@ page import="java.util.regex.Pattern" %>
<fmt:bundle basename="org.wso2.carbon.logging.remote.config.ui.i18n.Resources">
    <%
        response.setHeader("Cache-Control", "no-cache");
        String url = request.getParameter("url");
        String connectTimeoutMillis = request.getParameter("connectTimeoutMillis");
        boolean auditLogType = Boolean.parseBoolean(request.getParameter("auditLogType"));
        boolean carbonLogType = Boolean.parseBoolean(request.getParameter("carbonLogType"));

        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        RemoteLoggingConfigClient client;
        client = new RemoteLoggingConfigClient(cookie, backendServerURL, configContext);
        try {
            if (url == null || url.isEmpty()) {
    %>
    <fmt:message key="remote.server.url.empty"/>
    <%
            } else if (!Pattern.matches("^(http|https)://.*$", url)) {
    %>
    <fmt:message key="remote.server.url.invalid"/>
    <%
            } else if (!auditLogType && !carbonLogType) {
    %>
    <fmt:message key="remote.server.log.type.not.selected"/>
    <%
            } else if (connectTimeoutMillis == null || connectTimeoutMillis.isEmpty()) {
                client.addRemoteServerConfig(url, connectTimeoutMillis, auditLogType, carbonLogType);
    %>
    <fmt:message key="successfully.added.remote.server.configuration"/>
    <%
            } else {
                try {
                    Integer.parseInt(connectTimeoutMillis);
                    client.addRemoteServerConfig(url, connectTimeoutMillis, auditLogType, carbonLogType);
    %>
    <fmt:message key="successfully.added.remote.server.configuration"/>
    <%
                } catch (NumberFormatException e) {
    %>
    <fmt:message key="remote.server.timeout.invalid"/>
    <%
                }
            }
        } catch (Exception e) {
    %>
    <fmt:message key="add.remote.server.config.failed"/>
    <%
        }
    %>
</fmt:bundle>
