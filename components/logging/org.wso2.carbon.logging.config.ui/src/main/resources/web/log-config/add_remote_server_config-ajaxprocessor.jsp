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

<%@ page import="org.wso2.carbon.logging.config.ui.LoggingConfigClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%
	response.setHeader("Cache-Control", "no-cache");
	String backendServerURL = CarbonUIUtil
			.getServerURL(config.getServletContext(), session);
	ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
			.getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

	String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    LoggingConfigClient client = new LoggingConfigClient(cookie, backendServerURL, configContext);
    String[] logLevels = client.getLogLevels();
%>

<script type="text/javascript" src="js/loggingconfig.js"></script>
<fmt:bundle basename="org.wso2.carbon.logging.config.ui.i18n.Resources">

    <table class="styledLeft">
        <thead>
        <tr>
            <th><fmt:message key="configure.remote.server.url"/></th>
        </tr>
        </thead>
        <tr>
            <td class="formRow">
                <table class="normal">
                    <tr>
                        <td width="40%"><fmt:message key="remote.server.url"/></td>
                        <td>
                            <input value="" size="50"
                                   id="remoteServerUrl"
                                   tabindex="6" type="url"/>
                        </td>
                    </tr>
                    <tr>
                        <td width="40%"><fmt:message key="remote.server.timeout"/></td>
                        <td>
                            <input value="" size="50"
                                   id="connectTimeoutMillis"
                                   tabindex="6" type="url"/>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td class="buttonRow">
                <input value="<fmt:message key="update"/>" tabindex="11" type="button"
                       class="button"
                       id="addRemoteServerConfig"
                       onclick="showConfirmationDialogBox('<fmt:message key="remote.server.config.add.confirm"/>', addRemoteServerConfig)"/>
            </td>
        </tr>
    </table>
</fmt:bundle>
