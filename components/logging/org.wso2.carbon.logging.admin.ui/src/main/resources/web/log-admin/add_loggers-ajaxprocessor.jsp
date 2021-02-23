<%--
~ Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@ page import="org.wso2.carbon.logging.admin.ui.LoggingAdminClient" %>
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
    LoggingAdminClient client = new LoggingAdminClient(cookie, backendServerURL, configContext);
    String[] logLevels = client.getLogLevels();
%>

<script type="text/javascript" src="js/loggingadmin.js"></script>
<fmt:bundle basename="org.wso2.carbon.logging.admin.ui.i18n.Resources">

    <table class="styledLeft">
        <thead>
        <tr>
            <th><fmt:message key="add.log4j2.loggers"/></th>
        </tr>
        </thead>
        <tr>
            <td class="formRow">
                <table class="normal">
                    <tr>
                        <td width="10%"><fmt:message key="logger.name"/></td>
                        <td>
                            <input value="" size="50"
                                   id="loggerName"
                                   tabindex="6" type="text"/>
                        </td>
                    </tr>
                    <tr>
                        <td width="10%"><fmt:message key="logger.class"/></td>
                        <td>
                            <input value="" size="50"
                                   id="loggerClass"
                                   tabindex="6" type="text"/>
                        </td>
                    </tr>
                    <tr>
                        <td><fmt:message key="logging.level"/></td>
                        <td>
                            <select tabindex="7" id="loggingLevelCombo">
                               <%
                                   for (String logLevel: logLevels) {
                               %>
                                <option selected="true" value="<%=logLevel%>"><%=logLevel%>
                                </option>
                                <%
                                    }
                                %>
                            </select>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td class="buttonRow">
                <input value="<fmt:message key="update"/>" tabindex="11" type="button"
                       class="button"
                       id="addLogger"
                       onclick="showConfirmationDialogBox('<fmt:message key="logger.add.confirm"/>', addLogger)"/>
            </td>
        </tr>
    </table>
</fmt:bundle>
