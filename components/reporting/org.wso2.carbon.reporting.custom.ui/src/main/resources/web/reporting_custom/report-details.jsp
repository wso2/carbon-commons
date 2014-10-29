<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%@ page import="org.wso2.carbon.reporting.custom.ui.client.DBReportingServiceClient" %>
<%@ page import="org.wso2.carbon.reporting.custom.ui.beans.FormParameters" %>
<%@ page import="org.wso2.carbon.reporting.custom.ui.client.ReportResourceSupplierClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="../carbon/admin/js/main.js"></script>

<script type="text/javascript" src="../js/reporting-commons.js"></script>

<fmt:bundle basename="org.wso2.carbon.reporting.ui.i18n.Resources">
    <carbon:breadcrumb
            label="report.details.text"
            resourceBundle="org.wso2.carbon.reporting.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
    <%
        DBReportingServiceClient dbReportingServiceClient = DBReportingServiceClient.getInstance(config, session);
        ReportResourceSupplierClient resourcesSupplier = ReportResourceSupplierClient.getInstance(config, session);
        FormParameters[] formParameters = new FormParameters[0];
        String name = request.getParameter("reportName");
        String[] dataSources = new String[0];
        String error1 = "Failed to get data sources";

        try {
            dataSources = dbReportingServiceClient.getCarbonDataSourceNames();
        } catch (Exception e) {
            request.setAttribute(CarbonUIMessage.ID, new CarbonUIMessage(error1, e.getMessage(), e));

    %>
    <jsp:forward page="../admin/error.jsp"/>
    <%
        }
    %>
    <%
        try {
            formParameters = resourcesSupplier.getReportParam(name);
        } catch (Exception e) {
            request.setAttribute(CarbonUIMessage.ID, new CarbonUIMessage(error1, e.getMessage(), e));
    %>
    <jsp:forward page="../admin/error.jsp"/>
    <%
        }
    %>
    <div id="middle">
        <h2><fmt:message key="generate.report"/>  <%=name%>
        </h2>

        <div id="workArea">
            <form action="#" id="selectedForm">
                <%
                    if (formParameters.length > 0) {
                %>
                <table class="styledLeft" width="100%" style="margin-top:20px;" id="table1">
                    <thead>
                    <th colspan="3"><fmt:message key="report.parameters"/></th>
                    </thead>
                    <tbody>
                    <%
                        for (FormParameters reportParameters : formParameters) {
                            if (reportParameters != null) {
                                String paramName = reportParameters.getFormName();
                                String paramValue = reportParameters.getFormValue();
                    %>
                    <tr>
                        <td class="leftCol-small" style="text-align:left !important">
                            <%=paramName%><span class="required">*</span>
                        </td>
                        <td>
                            <%
                                if (paramValue.equals("java.util.Date")) {
                            %>
                            <script type="text/javascript">

                                jQuery(document).ready(function(){
                                    jQuery("#<%=paramName%>").datepicker();
                                })
                            </script>
                            <a class="icon-link"
                               style="background-image: url( ../admin/images/calendar.gif);"
                               onclick="jQuery('#<%=paramName%>').datepicker('show');" href="#"></a>
                            <input type="text" name="input_param" id="<%=paramName%>"></td>
                        <%
                        } else {
                        %>
                        <input type="text" style="margin-left: 30px;" name="input_param" id="<%=paramName%>"></td>
                        <%
                            }
                        %>
                    </tr>

                    <%
                            }
                        }
                    %>
                    </tbody>
                </table>
                <%
                    }
                %>
            </form>
            <form action="../dbreport" name="reportConfig" method="post" target="_blank">
                <input type="hidden" id="hidden_param" name="hidden_param" value="">
                <input type="hidden" id="reportName" name="reportName" value="<%=name%>">
                <table class="styledLeft" width="100%" style="margin-top:20px;" id="table2">
                    <thead>
                    <tr class="tableOddRow">
                        <th colspan="2"><fmt:message key="report.datasource"/> </th>
                    </tr>
                    </thead>
                    <tbody>
                    <%
                        if (dataSources != null && dataSources.length > 0) {
                    %>
                    <tr class="tableEvenRow">
                        <td class="leftCol-small">
                            <label>Data Source</label>
                        </td>
                        <td>
                            <label>
                                <select name="dataSource" id="dataSource" style="float:left;">
                                    <%
                                        for (int j = 0; j < dataSources.length; j++) {
                                            if (dataSources[j] != null) {
                                    %>
                                    <option id="<%=dataSources[j]%>"><%=dataSources[j]%>
                                    </option>
                                    <%
                                            }
                                        }
                                    %>
                                </select>
                            </label>
                        </td>
                    </tr>
                    <%
                    } else {
                    %>
                    <tr>
                        <td colspan="2"><fmt:message key="report.nodata"/> </td>
                    </tr>
                    <%
                        }
                    %>
                    </tbody>
                </table>
                <%
                    if (name != null && dataSources != null) {
                %>

                <table class="styledLeft" width="100%" style="margin-top:20px;" id="table3">
                    <thead>
                    <th colspan="2">Report Formats</th>
                    </thead>
                    <tbody>
                    <tr class="tableEvenRow">
                        <td class="leftCol-small">
                            <label><fmt:message key="report.types"/></label>
                        </td>
                        <td>
                            <label>
                                <select name="reportType" id="reportType" style="float:left;">

                                    <option id="pdf">pdf</option>
                                    <option id="excel">excel</option>
                                </select>
                            </label>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow" colspan="2"><label>
                            <input type="button" name="Generate Report" class="button"
                                   value="Generate Report" onclick="submitReport()">
                        </label>
                        </td>
                    </tr>
                    </tbody>
                </table>
                <%

                    }
                %>
            </form>
            <script type="text/javascript">
                alternateTableRows('table1', 'tableEvenRow', 'tableOddRow');
                alternateTableRows('table2', 'tableEvenRow', 'tableOddRow');
            </script>
        </div>
    </div>
</fmt:bundle>