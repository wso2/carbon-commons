<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
-->
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.statistics.stub.types.carbon.SystemStatistics" %>
<%@ page import="org.wso2.carbon.statistics.ui.StatisticsAdminClient" %>
<%@ page import="org.wso2.carbon.statistics.ui.Utils" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>

<%
    response.setHeader("Cache-Control", "no-cache");

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    StatisticsAdminClient client = new StatisticsAdminClient(cookie, backendServerURL,
            configContext, request.getLocale());

    int responseTimeGraphWidth = 500;
    responseTimeGraphWidth = Utils.getPositiveIntegerValue(session, request, responseTimeGraphWidth, "responseTimeGraphWidth");

    int memoryGraphWidth = 500;
    memoryGraphWidth = Utils.getPositiveIntegerValue(session, request, memoryGraphWidth, "memoryGraphWidth");

    SystemStatistics systemStats;

    boolean isSuperTenant = CarbonUIUtil.isSuperTenant(request);

    try {
        systemStats = client.getSystemStatistics();
    } catch (Exception e) {
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
        return;
    }
%>

<fmt:bundle basename="org.wso2.carbon.statistics.ui.i18n.Resources">


    <table width="100%">
        <tr>
            <td width=<%= isSuperTenant ? "49%" : "98%"%>>
                <!-- Service Summary -->
                <table class="styledLeft" width="100%" id="serviceSummaryTable" style="margin-left: 0px;">
                    <thead>
                    <tr>
                        <th colspan="2"><fmt:message key="service.summary"/></th>
                    </tr>
                    </thead>
                    <tr class="tableOddRow">
                        <td width="30%"><fmt:message key="average.response.time"/></td>
                        <td><%= ((float) Math.round(systemStats.getAvgResponseTime() * 1000)) / 1000 %>
                            ms
                        </td>
                    </tr>
                    <tr class="tableEvenRow">
                        <td><fmt:message key="minimum.response.time"/></td>
                        <td>
                            <% if (systemStats.getMinResponseTime() <= 0 &&
                                   systemStats.getTotalResponseCount() > 0) {%>
                            &lt; 1.00 ms
                            <% } else {%>
                            <%= systemStats.getMinResponseTime()%> ms
                            <% }%>
                        </td>
                    </tr>
                    <tr class="tableOddRow">
                        <td><fmt:message key="maximum.response.time"/></td>
                        <td><%= systemStats.getMaxResponseTime()%> ms</td>
                    </tr>
                    <tr class="tableEvenRow">
                        <td><fmt:message key="total.request.count"/></td>
                        <td><%= systemStats.getTotalRequestCount()%>
                        </td>
                    </tr>
                    <tr class="tableOddRow">
                        <td><fmt:message key="total.response.count"/></td>
                        <td><%= systemStats.getTotalResponseCount()%>
                        </td>
                    </tr>
                    <tr class="tableEvenRow">
                        <td><fmt:message key="total.fault.count"/></td>
                        <td><%= systemStats.getTotalFaultCount()%>
                        </td>
                    </tr>
                    <tr class="tableOddRow">
                        <td><fmt:message key="active.services"/></td>
                        <td><%= systemStats.getServices()%>
                        </td>
                    </tr>
                </table>
            </td>
            <td width="2%">&nbsp;</td>
            <td width=<%= isSuperTenant ? "49%" : "0"%>>
                <%
                    if (isSuperTenant) {
                %>
                <!-- Server Summary -->
                <table class="styledLeft" width="100%" id="serverSummaryTable" style="margin-left: 0px;">
                    <thead>
                    <tr>
                        <th colspan="2"><fmt:message key="server"/></th>
                    </tr>
                    </thead>
                    <tr class="tableOddRow">
                        <td width="30%"><fmt:message key="host"/></td>
                        <td><%= systemStats.getServerName()%>
                        </td>
                    </tr>
                    <tr class="tableEvenRow">
                        <td><fmt:message key="server.start.time"/></td>
                        <td><%= systemStats.getServerStartTime()%>
                        </td>
                    </tr>
                    <tr class="tableOddRow">
                        <td><fmt:message key="system.up.time"/></td>
                        <td><%= systemStats.getSystemUpTime()%>
                        </td>
                    </tr>
                    <tr class="tableEvenRow">
                        <td><fmt:message key="memory.allocated"/></td>
                        <td><%= systemStats.getTotalMemory().getValue()%><%= systemStats.getTotalMemory().getUnit()%>
                        </td>
                    </tr>
                    <tr class="tableOddRow">
                        <td><fmt:message key="memory.usage"/></td>
                        <td><%= systemStats.getUsedMemory().getValue()%><%= systemStats.getUsedMemory().getUnit()%>
                        </td>
                    </tr>
                </table>

                <%
                    }
                %>
            </td>

        </tr>
        <tr>
            <td colspan="3">&nbsp;</td>
        </tr>
        <tr>
            <td width="49%">
                <table class="styledLeft" id="responseTimePlot" width="100%" style="margin-left: 0px;">
                    <thead>
                    <tr>
                        <th><fmt:message key="average.response.time.vs.time.units"/></th>
                    </tr>
                    </thead>
                    <tr>
                        <td>
                            <div id="responseTimeGraph"
                                 style="width:<%= responseTimeGraphWidth%>px;height:300px;"></div>
                        </td>
                    </tr>
                </table>
            </td>
            <td width="2%">&nbsp;</td>
            <td width="49%">
                <%
                    if (isSuperTenant) {
                %>

                <table class="styledLeft" id="memoryUsagePlot" width="100%">
                    <thead>
                    <tr>
                        <th><fmt:message key="memory.vs.time.units"/></th>
                    </tr>
                    </thead>
                    <tr>
                        <td>
                            <div id="memoryGraph"
                                 style="width:<%= memoryGraphWidth%>px;height:300px;"></div>
                        </td>
                    </tr>
                </table>

                <%
                    }
                %>
            </td>
        </tr>
        <script type="text/javascript">

            <%
                if (isSuperTenant) {
            %>
            graphUsedMemory.add(<%= systemStats.getUsedMemory().getValue()%>);
            graphTotalMemory.add(<%= systemStats.getTotalMemory().getValue()%>);
            graphAvgResponse.add(<%= systemStats.getAvgResponseTime()%>);
            drawResponseTimeGraph();
            drawMemoryGraph();

            <%
            }else{
            %>
            graphAvgResponse.add(<%= systemStats.getAvgResponseTime()%>);           
            drawResponseTimeGraph();

            <%
            }
            %>

        </script>
    </table>


</fmt:bundle>
