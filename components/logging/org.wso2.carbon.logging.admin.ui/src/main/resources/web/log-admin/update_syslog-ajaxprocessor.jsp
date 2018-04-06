<%--
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
--%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.logging.admin.ui.LoggingAdminClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<fmt:bundle basename="org.wso2.carbon.logging.admin.ui.i18n.Resources">
    <%
        response.setHeader("Cache-Control", "no-cache");
        String syslogURL = request.getParameter("syslogURL");
        String syslogPort = request.getParameter("syslogPort");
        String realm = request.getParameter("realm");
        String userName = request.getParameter("userName");
        String password = request.getParameter("password");
        
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        LoggingAdminClient client;
        client = new LoggingAdminClient(cookie, backendServerURL, configContext);
        try {
            client.updateSyslogConfig(syslogURL,syslogPort,realm,userName,password);
    %>
    <p><fmt:message key="successfully.updated.syslog.config"/></p>
    <%
    } catch (Exception e) {
    %>
    <p><fmt:message key="syslog.config.update.failed"/></p>
    <%
        }
    %>
</fmt:bundle>