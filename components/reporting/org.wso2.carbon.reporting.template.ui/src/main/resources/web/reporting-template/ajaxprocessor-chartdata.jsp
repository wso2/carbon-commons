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


<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.reporting.template.stub.ReportTemplateAdminStub" %>
<%@ page import="org.wso2.carbon.reporting.template.ui.client.ReportTemplateClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.reporting.template.stub.ReportTemplateAdminReportingExceptionException" %>


<%

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);


    ReportTemplateClient client;
    String msg = "";
    String errorString = "";
    try {
        client = new ReportTemplateClient(configContext, serverURL, cookie);

        String reportType = request.getParameter("reportType");
        String dsName = request.getParameter("datasource");
        String tableName = request.getParameter("tableName");
        String xFields = request.getParameter("xAxisFields");
        String yFields = request.getParameter("yAxisFields");
        if (null == xFields) {
            msg = "No X-Axis Fields are available";
        } else {
            String[] xFieldsArray = xFields.trim().split("&#&");

            if (null != yFields) {
                String[] yFieldsArray = yFields.trim().split("&#&");

                if (reportType.contains("xy")) {
                    msg = client.isValidNumberAxis(reportType, dsName, tableName, xFieldsArray);
                }
                if (msg.isEmpty()) {
                    msg = client.isValidNumberAxis(reportType, dsName, tableName, yFieldsArray);
                    if (!msg.trim().isEmpty()) {
                        msg = "Y-Axis Fields are not compatible for the chart type : \n" + msg;
                    }
                } else {
                    msg = "X-Axis Fields are not compatible for the chart type : \n" + msg;
                }
            } else {
                msg = "No Y-Axis Fields are available";
            }

        }
    } catch (Exception e) {
        msg = e.getMessage();
    }
%>
i<%
    if (!msg.equals("")) {
%>
<div id="msg">
    Error - <%=msg%>
</div>

<%
    }else {
%>
   <div id="msg">
    Success
</div>
<%
    }
%>


