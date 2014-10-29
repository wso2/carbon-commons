<%--<!--
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
 -->--%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %><%@
        page import="org.apache.axis2.context.ConfigurationContext" %><%@
        page import="org.wso2.carbon.CarbonConstants" %><%@
        page import="org.wso2.carbon.statistics.ui.StatisticsAdminClient" %><%@
        page import="org.wso2.carbon.statistics.stub.types.carbon.SystemStatistics" %><%@
        page import="org.wso2.carbon.ui.CarbonUIUtil" %><%@
        page import="org.wso2.carbon.utils.ServerConstants" %><%@
        page import="org.wso2.carbon.statistics.ui.Utils" %><%@
        page import="org.wso2.carbon.ui.CarbonUIMessage" %><%
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
    try {
        systemStats = client.getSystemStatistics();
    } catch (Exception e) {
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
        return;
    }
%>
<stats>
    <statistic name="Used Memory">
        <value><%= systemStats.getUsedMemory().getValue()%></value>
    </statistic>
    <statistic name="Total Memory">
        <value><%= systemStats.getTotalMemory().getValue()%></value>
    </statistic>
</stats>