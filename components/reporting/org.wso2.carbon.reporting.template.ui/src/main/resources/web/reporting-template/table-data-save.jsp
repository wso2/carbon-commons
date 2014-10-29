<!--
~ Copyright 2010 WSO2, Inc. (http://wso2.com)
~
~ Licensed under the Apache License, Version 2.0 (the "License");
~ you may not use this file except in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing, software
~ distributed under the License is distributed on an "AS IS" BASIS,
~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
~ See the License for the specific language governing permissions and
~ limitations under the License.
-->

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.reporting.template.ui.client.ReportTemplateClient" %>
<%@ page import="org.apache.axis2.AxisFault" %>
<%@ page import="org.wso2.carbon.reporting.template.stub.ReportTemplateAdminStub.*" %>
<%@ page import="java.util.Enumeration" %>
<%@ page buffer="256kb" %>
<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    ReportTemplateClient client;
    String errorString = "";
    try {
        client = new ReportTemplateClient(configContext, serverURL, cookie);
    } catch (Exception e) {

        errorString = e.getMessage();

%>
<script type="text/javascript">
    CARBON.showErrorDialog('<%=errorString%>', function(){
         location.href = "add-table-report.jsp";
    });
</script>
<%
        return;
    }
    String reportName = request.getParameter("reportName");
    String dsName = request.getParameter("datasource");
    String tableName=request.getParameter("tableName");
    String selectedFields = request.getParameter("selectedFields");
    String primaryField = request.getParameter("primaryField");
    TableReportDTO report = client.createTableDataInformation(reportName, dsName, tableName, selectedFields, primaryField);
    session.setAttribute("table-report", report);
%>
<script type="text/javascript">
    location.href = "table-report-format.jsp";
</script>




