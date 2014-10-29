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
<%@ page import="org.wso2.carbon.logging.admin.ui.LoggingAdminClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.logging.admin.stub.types.carbon.LoggerData" %>

<%
    response.setHeader("Cache-Control", "no-cache");
    
    String filterStr = request.getParameter("filterStr");
    if (filterStr != null) {
        filterStr = filterStr.trim();
    }
    boolean beginsWith = false;
    if (request.getParameter("beginsWith") != null) {
        beginsWith = Boolean.valueOf(request.getParameter("beginsWith"));
    }

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    LoggingAdminClient client;
    LoggerData[] allLoggerData;
    try {
        client = new LoggingAdminClient(cookie, backendServerURL, configContext);
        allLoggerData = client.getAllLoggerData(beginsWith, filterStr);
        if (allLoggerData == null || allLoggerData.length == 0) {
%>
    <fmt:bundle basename="org.wso2.carbon.logging.admin.ui.i18n.Resources">
            <p><fmt:message key="no.loggers.found"/></p>
    </fmt:bundle>
<%
            return;
        }
    } catch (Exception e) {
%>
<jsp:forward page="../admin/error.jsp?<%=e.getMessage()%>"/>
<%
        return;
    }
%>

<fmt:bundle basename="org.wso2.carbon.logging.admin.ui.i18n.Resources">
    <table class="styledLeft" cellspacing="1" id="loggersTable" style="margin-left: 0px;">

        <thead>
        <tr>
            <th width="40%"><fmt:message key="logger"/></th>
            <th width="40%"><fmt:message key="parent.logger"/></th>
            <th><fmt:message key="effective.level"/></th>
            <th><fmt:message key="additivity"/></th>
        </tr>
        </thead>

        <tbody>

        <%
            String[] logLevels = client.getLogLevels();
            for (LoggerData loggerData : allLoggerData) {
                String loggerName = loggerData.getName();
                String parentName = loggerData.getParentName();
                boolean additivity = loggerData.getAdditivity();
                String logLevel = loggerData.getLevel();
        %>

        <tr>
            <td width="40%"><%=loggerName%></td>
            <td width="40%"><%=parentName%></td>
            <td>
                <select id='<%=loggerName%>LogLevel'
                        onchange="updateLogger('<%=loggerName%>',
                                                  '<%=loggerName%>LogLevel',
                                                  '<%=loggerName%>Additivity');">

                    <%
                        for (String logLevelType : logLevels) {
                            if (logLevelType.equals(logLevel)) {
                    %>

                    <option value="<%=logLevelType%>" selected="true"><%=logLevelType%>
                    </option>

                    <%
                    } else {
                    %>

                    <option value="<%=logLevelType%>"><%=logLevelType%>
                    </option>

                    <% }
                    }
                    %>
                </select>
            </td>
            <td>
                <select id="<%= loggerName%>Additivity"
                        onchange="updateLogger('<%=loggerName%>',
                                                  '<%=loggerName%>LogLevel',
                                                  '<%= loggerName%>Additivity');">
                    <%
                        if (additivity) {
                    %>
                    <option selected="true" value="true"><fmt:message key="true"/></option>
                    <option value="false"><fmt:message key="false"/></option>
                    <%
                    } else {
                    %>
                    <option value="true"><fmt:message key="true"/></option>
                    <option selected="true" value="false"><fmt:message key="false"/></option>
                    <%
                        }
                    %>
                </select>
            </td>
        </tr>

        <%
            }
        %>

        </tbody>

    </table>

    <script type="text/javascript">
        alternateTableRows('loggersTable', 'tableEvenRow', 'tableOddRow');
    </script>
</fmt:bundle>
