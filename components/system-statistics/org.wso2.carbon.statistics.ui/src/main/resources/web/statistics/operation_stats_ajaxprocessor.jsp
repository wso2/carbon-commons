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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.statistics.ui.StatisticsAdminClient" %>
<%@ page import="org.wso2.carbon.statistics.stub.types.carbon.OperationStatistics" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>

<%
    response.setHeader("Cache-Control", "no-cache");

    String serviceName = CharacterEncoder.getSafeText(request.getParameter("serviceName"));
    String opName = CharacterEncoder.getSafeText(request.getParameter("opName"));

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    StatisticsAdminClient client = new StatisticsAdminClient(cookie, backendServerURL,
                                                             configContext, request.getLocale());

    OperationStatistics opStats;
    try {
        opStats = client.getOperationStatistics(serviceName, opName);
    } catch (Exception e) {
        if(e.getCause().getMessage().toLowerCase().indexOf("you are not authorized") == -1){
            response.setStatus(500);
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
            <jsp:include page="../admin/error.jsp"/>
<%
        }
        return;
    }
%>
<fmt:bundle basename="org.wso2.carbon.statistics.ui.i18n.Resources">
    <table class="styledLeft" id="opStatsTable" width="100%" style="margin-left: 0px;">
        <thead>
        <tr>
            <th colspan="2" align="left"><fmt:message key="statistics"/></th>
        </tr>
        </thead>
        <tr class="tableOddRow">
            <td width="30%"><fmt:message key="request.count"/></td>
            <td><%=opStats.getTotalRequestCount() %>
            </td>
        </tr>
        <tr class="tableEvenRow">
            <td><fmt:message key="response.count"/></td>
            <td><%=opStats.getTotalResponseCount() %>
            </td>
        </tr>
        <tr class="tableOddRow">
            <td><fmt:message key="fault.count"/></td>
            <td><%=opStats.getTotalFaultCount() %>
            </td>
        </tr>
        <tr class="tableEvenRow">
            <td><fmt:message key="maximum.response.time"/></td>
            <td><%=opStats.getMaxResponseTime()%> ms</td>
        </tr>
        <tr class="tableOddRow">
            <td><fmt:message key="minimum.response.time"/></td>
            <td>
                <%if (opStats.getMinResponseTime() == 0 && opStats.getTotalResponseCount() > 0) { %>
                &lt; 1.00 ms
                <%} else {%>
                <%=opStats.getMinResponseTime() %> ms
                <%}%>
            </td>
        </tr>
        <tr class="tableEvenRow">
            <td><fmt:message key="average.response.time"/></td>
            <td><%=((float) Math.round(opStats.getAvgResponseTime() * 1000)) / 1000%> ms</td>
        </tr>
        <tr>
            <td colspan="2">&nbsp;</td>
        </tr>
        <tr>
            <td colspan="2" align="left"><u><fmt:message key="average.response.time.vs.time"/></u>
            </td>
        </tr>
        <tr>
            <td colspan="2">
                <div id="responseTimeGraph" style="width:500px;height:300px;"></div>
            </td>
        </tr>
        <script type="text/javascript">
            jQuery.noConflict();
            function drawResponseTimeGraph() {
                jQuery.plot(jQuery("#responseTimeGraph"), [
                    {
                        data: graphAvgResponse.get(),
                        lines: { show: true, fill: true }
                    }
                ], {
                    xaxis: {
                        ticks: graphAvgResponse.tick(),
                        min: 0
                    },
                    yaxis: {
                        ticks: 10,
                        min: 0
                    }
                });
            }
            try {
                graphAvgResponse.add(<%= opStats.getAvgResponseTime()%>);
                drawResponseTimeGraph();
            } catch (e) {
            } // ignored
        </script>
    </table>
</fmt:bundle>
