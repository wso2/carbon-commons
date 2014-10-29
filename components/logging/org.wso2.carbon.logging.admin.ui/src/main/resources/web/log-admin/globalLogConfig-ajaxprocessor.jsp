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
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.logging.admin.ui.LoggingAdminClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.logging.admin.stub.types.carbon.LogData" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<!-- This page is included to display messages which are set to request scope or session scope -->
<jsp:include page="../dialog/display_messages.jsp"/>
<fmt:bundle basename="org.wso2.carbon.logging.admin.ui.i18n.Resources">
<%
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    LoggingAdminClient client;
    LogData globalLogData;
    try {
        client = new LoggingAdminClient(cookie, backendServerURL, configContext);
        globalLogData = client.getSysLog();
     } catch (Exception e) {
%>
<jsp:forward page="../admin/error.jsp?<%=e.getMessage()%>"/>
<%
        return;
    }
%>
<table>
    <tr class="formRow">
        <td width="10px">
            <input checked="true" value="true" id="persistLogId" tabindex="1"
                   type="checkbox" name="persist"/>
        </td>
        <td><fmt:message key="persist.all.configuration.changes"/></td>
    </tr>
</table>
<p>&nbsp;</p>
<table id="globalLogConfigTbl" class="styledLeft">
    <thead>
    <tr>
        <th><fmt:message key="global.log4j.configuration"/></th>
    </tr>
    </thead>
    <tr>
        <td class="formRow">
            <table class="normal">
                <tr>
                    <td width="10%"><fmt:message key="log.level"/></td>
                    <td>
                        <select tabindex="1" id="globalLogLevel"
                                name="logLevel">

                            <%
                                String[] logLevels = client.getLogLevels();
                                String globalLogLevel = globalLogData.getLogLevel();
                                for (String logLevelType : logLevels) {
                                    if (logLevelType.equals(globalLogLevel)) {
                            %>

                            <option value="<%=logLevelType%>" selected="true">
                                <%=logLevelType%>
                            </option>

                            <%
                            } else {
                            %>
                            <option value="<%=logLevelType%>">
                                <%=logLevelType%>
                            </option>
                            <% }
                            }
                            %>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td>
                        <fmt:message key="log.pattern"/>
                    </td>
                    <td>
                        <input value="<%=globalLogData.getLogPattern()%>"
                               size="50"
                               id="globalLogPattern" tabindex="2" type="text"
                               name="logPattern"/>
                    </td>
                </tr>
            </table>
        </td>
    </tr>

    <tr>
        <td class="buttonRow">
            <input id="globalLog4jUpdate"
                   onclick="showConfirmationDialogBox('<fmt:message key="global.log.update.confirm"/>', globalLog4jUpdateConfig);return false;"
                   value="<fmt:message key="update"/>"
                   tabindex="4"
                   type="button" name="updateLog4jGlobal" class="button"/>

            &nbsp;
            <input id="restoreGlobalConfig"
                   onclick="showConfirmationDialogBox('<fmt:message key="global.log.restore.confirm"/>', restoreLog4jConfigToDefaults);return false;"
                   value="<fmt:message key="restore.defaults"/>"
                   tabindex="8"
                   type="button" name="restoreDefaults" class="button"/>

        </td>
    </tr>
</table>
</fmt:bundle>