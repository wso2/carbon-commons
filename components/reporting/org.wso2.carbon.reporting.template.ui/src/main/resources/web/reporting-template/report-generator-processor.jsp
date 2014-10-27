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
<%@ page import="javax.activation.DataHandler" %>
<%@ page buffer="256kb" %>
<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    ReportTemplateClient client = null;
    String errorString = "";
    try {
        client = new ReportTemplateClient(configContext, serverURL, cookie);
    } catch (Exception e) {
%>
<script type="text/javascript">
   CARBON.showErrorDialog(<%=e.getMessage()%>);
</script>
<%
    }
    String reportName = request.getParameter("reportName");
    String reportType = request.getParameter("reportType");

    String downloadFileName =  null;

    if (reportType.equals("pdf")) {
            response.setContentType("application/pdf");
            downloadFileName = reportName + ".pdf";
        } else if (reportType.equals("xls")) {
            response.setContentType("application/vnd.ms-excel");
            downloadFileName = reportName + ".xls";
        } else if (reportType.equals("html")) {
            response.setContentType("text/html");
        }

        if (downloadFileName != null) {
            response.setHeader("Content-Disposition", "attachment; filename=\"" + downloadFileName + "\"");
        }
            DataHandler dataHandler = null;

            if (client!= null) {
              dataHandler  = client.generateReport(reportName, reportType);
            }
            ServletOutputStream outputStream = response.getOutputStream();
            if (dataHandler != null) {
                dataHandler.writeTo(outputStream);
            }
%>
<script type="text/javascript">
    location.href = "index.jsp";
</script>





