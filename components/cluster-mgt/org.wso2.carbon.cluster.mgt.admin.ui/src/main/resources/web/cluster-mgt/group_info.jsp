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
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>

<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.cluster.mgt.admin.ui.ClusterAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.cluster.mgt.admin.stub.types.carbon.GroupMember" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<script type="text/javascript" src="js/clustermgt.js"></script>

<%
    String groupName = request.getParameter("groupName");
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ClusterAdminClient client;
    GroupMember[] members;
    try {
        client = new ClusterAdminClient(cookie, backendServerURL, configContext, request.getLocale());
        members = client.getMembers(groupName);
    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
%>
        <script type="text/javascript">
               location.href = "../admin/error.jsp";
        </script>
<%
        return;
    }
%>

<carbon:breadcrumb
		label="members"
		resourceBundle="org.wso2.carbon.cluster.mgt.admin.ui.i18n.Resources"
		topPage="false"
		request="<%=request%>" />

<div id="middle">
    <div id="workArea">
        <div id="output"></div>  <%-- Needed by jQuery--%>
        <fmt:bundle basename="org.wso2.carbon.cluster.mgt.admin.ui.i18n.Resources">
            <h2><fmt:message key="group.members"><fmt:param value="<%= groupName%>"/></fmt:message></h2>
        </fmt:bundle>
        <%
            if (members == null || members.length == 0) {
        %>
        <fmt:bundle basename="org.wso2.carbon.cluster.mgt.admin.ui.i18n.Resources">
            <p>
                <fmt:message key="no.members.in.group">
                    <fmt:param value="<%= groupName%>"/>
                </fmt:message>
            </p>
        </fmt:bundle>
        <%
                return;
            }
        %>
        <fmt:bundle basename="org.wso2.carbon.cluster.mgt.admin.ui.i18n.Resources">
            <table width="100%">
                <tr>
                    <td colspan="3">
                            <table class="styledLeft" width="100%" id="membersTable">
                                <thead>
                                <tr>
                                    <th width="15%"><fmt:message key="host.name"/></th>
                                    <th width="10%">
                                        <nobr><fmt:message key="http.port"/></nobr>
                                    </th>
                                    <th width="10%">
                                        <nobr><fmt:message key="https.port"/></nobr>
                                    </th>
                                    <th>
                                        <nobr><fmt:message key="actions"/></nobr>
                                    </th>
                                </tr>
                                </thead>
                                <tbody>
                                <%  int i = 0;
                                    for (GroupMember member : members) {
                                %>
                                <form name="loginForm<%= i%>" action="login_to_member.jsp" method="post">
                                    <input type="hidden" name="backendURL"
                                           value="<%= member.getBackendServerURL()%>"/>
                                    <input type="hidden" name="groupName"
                                           value="<%= groupName %>"/>
                                    <input type="hidden" name="memberHostName"
                                           value="<%= member.getHostName()%>"/>
                                    <tr>
                                        <td width="15%">
                                            <%= member.getHostName()%>
                                        </td>
                                        <td width="10%">
                                            <%= member.getHttpPort()%>
                                        </td>
                                        <td width="10%">
                                            <%= member.getHttpsPort()%>
                                        </td>
                                        <td>
                                            <% if (member.getMgConsoleURL() != null) { %>
                                            <a href="<%= member.getMgConsoleURL()%>"
                                               class="icon-link" target="_blank"
                                               style="background-image:url(images/mgt-console.gif);">
                                                <fmt:message key="management.console"/>
                                            </a>
                                            <% } %>
                                            <% if (member.getBackendServerURL() != null) { %>
                                            <a href="#" class="icon-link"
                                               onclick="document.loginForm<%= i%>.submit();"
                                               style="background-image:url(images/connect-backend.gif);">
                                                <fmt:message key="connect.to.backend"/>
                                            </a>
                                            <%    i++;
                                                }
                                            %>
                                        </td>
                                    </tr>
                                </form>
                                <% } %>
                                </tbody>
                            </table>
                    </td>
                </tr>
                <tr><td>&nbsp;</td></tr>
                <tr>
                    <td width="100%">
                        <table class="styledLeft" id="shutDown" width="100%">
                            <thead>
                            <tr>
                                <th colspan="2"><fmt:message key="shutdown"/></th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td width="50%">
                                    <strong><fmt:message key="graceful.shutdown"/></strong></td>
                                <td width="50%">
                                    <strong><fmt:message key="forced.shutdown"/></strong></td>
                            </tr>
                            <tr>
                                <td width="50%">
                                    <fmt:message key="graceful.group.shutdown.explanation"/>
                                </td>
                                <td width="50%">
                                    <fmt:message key="forced.group.shutdown.explanation"/>
                                </td>
                            </tr>
                            <tr>
                                <td width="50%" class="buttonRow">
                                    <a href="#" onclick="shutdownGroupGracefully('<%= groupName%>');return false;"
                                       style="cursor:pointer;">
                                        <img src="images/graceful-shutdown.gif" alt="<fmt:message key="graceful.shutdown"/>"/>
                                        <fmt:message key="graceful.shutdown"/>
                                    </a>
                                </td>
                                <td width="50%" class="buttonRow">
                                    <a href="#" onclick="shutdownGroup('<%= groupName%>');return false;" style="cursor:pointer;">
                                        <img src="images/shutdown.gif" alt="<fmt:message key="forced.shutdown"/>"/>
                                        <fmt:message key="forced.shutdown"/>
                                    </a>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                        <table class="styledLeft" id="restart" width="100%">
                            <thead>
                            <tr>
                                <th colspan="2"><fmt:message key="restart"/></th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td width="50%"><strong><fmt:message
                                        key="graceful.restart"/></strong></td>
                                <td width="50%"><strong><fmt:message key="forced.restart"/></strong>
                                </td>
                            </tr>
                            <tr>
                                <td width="50%">
                                    <fmt:message key="graceful.group.restart.explanation"/>
                                </td>
                                <td width="50%">
                                    <fmt:message key="forced.group.restart.explanation"/>
                                </td>
                            </tr>
                            <tr>
                                <td width="50%" class="buttonRow">
                                    <a href="#" onclick="restartGroupGracefully('<%= groupName%>');return false;"
                                       style="cursor:pointer;">
                                        <img src="images/graceful-restart.gif" alt="<fmt:message key="graceful.restart"/>"/>
                                        <fmt:message key="graceful.restart"/>
                                    </a>
                                </td>
                                <td width="50%" class="buttonRow">
                                    <a href="#" onclick="restartGroup('<%= groupName%>');return false;" style="cursor:pointer;">
                                        <img src="images/restart.gif" alt="<fmt:message key="forced.restart"/>"/>
                                        <fmt:message key="forced.restart"/>
                                    </a>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                        <table class="styledLeft" id="maintenance" width="100%">
                            <thead>
                            <tr>
                                <th colspan="2"><fmt:message key="maintenance"/></th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td width="50%">
                                    <strong><fmt:message key="start.maintenance"/></strong>
                                </td>
                                <td width="50%">
                                    <strong><fmt:message key="end.maintenance"/></strong>
                                </td>
                            </tr>
                            <tr>
                                <td width="50%">
                                    <fmt:message key="start.group.maintenance.explanation"/>
                                </td>
                                <td width="50%">
                                    <fmt:message key="end.group.maintenance.explanation"/>
                                </td>
                            </tr>
                            <tr>
                                <td width="50%" class="buttonRow">
                                    <a href="#"
                                       onclick="startGroupMaintenance('<%= groupName%>');return false;"
                                       style="cursor:pointer;">
                                        <img src="images/start-maintenance.gif"
                                             alt="<fmt:message key="start.maintenance"/>"/>
                                        <fmt:message key="start.maintenance"/>
                                    </a>
                                </td>
                                <td width="50%" class="buttonRow">
                                    <a href="#" onclick="endGroupMaintenance('<%= groupName%>');return false;"
                                       style="cursor:pointer;">
                                        <img src="images/end-maintenance.gif"
                                             alt="<fmt:message key="end.maintenance"/>"/>
                                        <fmt:message key="end.maintenance"/>
                                    </a>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </td>
                </tr>
            </table>
        </fmt:bundle>
        <script type="text/javascript">
            alternateTableRows('membersTable', 'tableEvenRow', 'tableOddRow');
        </script>
    </div>
</div>