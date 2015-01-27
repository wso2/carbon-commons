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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.reporting.template.ui.i18n.Resources">


<script type="text/javascript">

    function submitTableReportData() {
        document.generate.action = '../report-generator';
        document.generate.submit();
        return true;
    }

    function cancelTableData() {
        location.href = "../reporting_custom/list-reports.jsp?region=region5&item=reporting_list.jsp";
    }

</script>

<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    ReportTemplateClient client = null;
    String errorString = "";
    String reportName = request.getParameter("reportName");
    try {
        client = new ReportTemplateClient(configContext, serverURL, cookie);
    } catch (Exception e) {

%>
<script type="text/javascript">
    CARBON.showErrorDialog('<%=e.getMessage()%>');
</script>
<%
    }
%>

<div id="middle">
<h2>Generate Report</h2>

<div id="workArea">

<form id="generate" name="generate" action="" method="POST">
<table class="styledLeft">
<thead>
<tr>
    <th><span style="float: left; position: relative; margin-top: 2px;">
                          <%=reportName%></span>
    </th>
</tr>
</thead>
<tbody>


<tr>
    <td>
        <table class="normal-nopadding">
            <tbody>

            <tr>
                <td width="180px"><fmt:message key="report.type"/> <span
                        class="required">*</span></td>
                <% String[] types = client.getReportTypes();
                %>
                <td><select name="reportType"
                           id="reportType"/>
                    <%
                        for(int i=0; i<types.length; i++){
                         String type = types[i];
                            %>
                     <option value="<%=type%>" <%=i == 0 ? "selected=\"selected\"" : ""%>>
                            <%=type%>
                       </option>
                      <%  }
                    %>
                </td>
                <input type="hidden" name="reportName" id="reportName" value="<%=reportName%>"/>
            </tr>

    </tbody>
  </table>

  </td>
    </tr>

            </tbody>
        </table>


        <table class="normal-nopadding">
            <tbody>

            <tr>
                <td class="buttonRow" colspan="2">
                    <input type="button" value="<fmt:message key="generate"/>"
                           class="button" name="generate"
                           onclick="submitTableReportData();"/>
                    <input type="button" value="<fmt:message key="cancel"/>"
                           name="cancel" class="button"
                           onclick="cancelTableData();"/>
                </td>
            </tr>
            </tbody>

        </table>
</form>
</div>
</div>


</fmt:bundle>





