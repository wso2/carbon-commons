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

<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.cluster.mgt.admin.ui.ClusterAdminClient" %>
<%@ page import="org.wso2.carbon.cluster.mgt.admin.stub.types.carbon.Group" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<script type="text/javascript" src="js/clustermgt.js"></script>

<%
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ClusterAdminClient client;
    Group[] groups;
    try {
        client = new ClusterAdminClient(cookie, backendServerURL, configContext, request.getLocale());
        groups = client.getGroups();
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
		label="clustermgt.groups"
		resourceBundle="org.wso2.carbon.cluster.mgt.admin.ui.i18n.Resources"
		topPage="true"
		request="<%=request%>" />

<div id="middle">
    <div id="workArea">
        <div id="output"></div>  <%-- Needed by jQuery--%>
        <fmt:bundle basename="org.wso2.carbon.cluster.mgt.admin.ui.i18n.Resources">
            <h2><fmt:message key="cluster.groups"/></h2>
        </fmt:bundle>
        <%
            if (groups == null || groups.length == 0) {
        %>
        <fmt:bundle basename="org.wso2.carbon.cluster.mgt.admin.ui.i18n.Resources">
            <p><fmt:message key="no.cluster.groups.found"/></p>
        </fmt:bundle>
        <%
                return;
            }
        %>
        <fmt:bundle basename="org.wso2.carbon.cluster.mgt.admin.ui.i18n.Resources">
            <table class="styledLeft" id="groupsTable">
                <thead>
                <tr>
                    <th width="20%"><fmt:message key="name"/></th>
                    <th width="10%"><fmt:message key="members"/></th>
                    <th><fmt:message key="description"/></th>
                </tr>
                </thead>
                <tbody>
                <% for (Group group : groups) {%>
                <tr>
                    <td width="20%">
                        <a href="group_info.jsp?groupName=<%= group.getName()%>">
                            <%= group.getName()%>
                        </a>
                    </td>
                    <td width="10%">
                        <%= group.getNumberOfMembers()%>
                    </td>
                    <td>
                        <%= group.getDescription()%>
                    </td>
                </tr>
                <% } %>
                </tbody>
            </table>

            <p>&nbsp;</p>
            <h2><fmt:message key="actions"/></h2>
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
                        <fmt:message key="graceful.cluster.shutdown.explanation"/>
                    </td>
                    <td width="50%">
                        <fmt:message key="forced.cluster.shutdown.explanation"/>
                    </td>
                </tr>
                <tr>
                    <td width="50%" class="buttonRow">
                        <a href="#"
                           onclick="shutdownClusterGracefully();return false;"
                           style="cursor:pointer;">
                            <img src="images/graceful-shutdown.gif"
                                 alt="<fmt:message key="graceful.shutdown"/>"/>
                            <fmt:message key="graceful.shutdown"/>
                        </a>
                    </td>
                    <td width="50%" class="buttonRow">
                        <a href="#" onclick="shutdownCluster();return false;"
                           style="cursor:pointer;">
                            <img src="images/shutdown.gif"
                                 alt="<fmt:message key="forced.shutdown"/>"/>
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
                        <fmt:message key="graceful.cluster.restart.explanation"/>
                    </td>
                    <td width="50%">
                        <fmt:message key="forced.cluster.restart.explanation"/>
                    </td>
                </tr>
                <tr>
                    <td width="50%" class="buttonRow">
                        <a href="#"
                           onclick="restartClusterGracefully();return false;"
                           style="cursor:pointer;">
                            <img src="images/graceful-restart.gif"
                                 alt="<fmt:message key="graceful.restart"/>"/>
                            <fmt:message key="graceful.restart"/>
                        </a>
                    </td>
                    <td width="50%" class="buttonRow">
                        <a href="#" onclick="restartCluster();return false;"
                           style="cursor:pointer;">
                            <img src="images/restart.gif"
                                 alt="<fmt:message key="forced.restart"/>"/>
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
                        <fmt:message key="start.cluster.maintenance.explanation"/>
                    </td>
                    <td width="50%">
                        <fmt:message key="end.cluster.maintenance.explanation"/>
                    </td>
                </tr>
                <tr>
                    <td width="50%" class="buttonRow">
                        <a href="#"
                           onclick="startClusterMaintenance();return false;"
                           style="cursor:pointer;">
                            <img src="images/start-maintenance.gif"
                                 alt="<fmt:message key="start.maintenance"/>"/>
                            <fmt:message key="start.maintenance"/>
                        </a>
                    </td>
                    <td width="50%" class="buttonRow">
                        <a href="#" onclick="endClusterMaintenance();return false;"
                           style="cursor:pointer;">
                            <img src="images/end-maintenance.gif"
                                 alt="<fmt:message key="end.maintenance"/>"/>
                            <fmt:message key="end.maintenance"/>
                        </a>
                    </td>
                </tr>
                </tbody>
            </table>
        </fmt:bundle>
        <script type="text/javascript">
            alternateTableRows('groupsTable', 'tableEvenRow', 'tableOddRow');
        </script>
    </div>
</div>