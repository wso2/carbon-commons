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
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.reporting.template.ui.client.ReportTemplateClient" %>
<%@ page import="org.apache.axis2.AxisFault" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.reporting.template.ui.i18n.Resources">

<carbon:breadcrumb label="add.report.step1"
                   resourceBundle="org.wso2.carbon.reporting.template.ui.i18n.Resources"
                   topPage="false" request="<%=request%>"/>

<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<%

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    ReportTemplateClient client = null;
    String message = "";
    String[] datasources = null;
    String errorString = "";
    if (null != request.getParameter("success") && request.getParameter("success").equalsIgnoreCase("false")) {
        errorString = request.getParameter("errorString").toString();
    }
    String dsName = "";
    String tableName = "";
    String repType = request.getParameter("reportType");
    String heading = "";
    if (repType != null) {
        if (repType.equalsIgnoreCase("bar_chart_type_report"))
            heading = "Bar Chart Report";
        else if (repType.equalsIgnoreCase("line_chart_type_report"))
            heading = "Line Chart Report";
        else if (repType.equalsIgnoreCase("area_chart_type_report"))
            heading = "Area Chart Report";
        else if (repType.equalsIgnoreCase("stacked_bar_chart_type_report"))
            heading = "Stacked Bar Chart Report";
        else if (repType.equalsIgnoreCase("stacked_area_chart_type_report"))
            heading = "Stacked Area Chart Report";
        else if (repType.equalsIgnoreCase("xy_bar_chart_type_report"))
            heading = "XY Bar Chart Report";
        else if (repType.equalsIgnoreCase("xy_line_chart_type_report"))
            heading = "XY Line Chart Report";
        else if (repType.equalsIgnoreCase("xy_area_chart_type_report"))
            heading = "XY Area Chart Report";
        else if (repType.equalsIgnoreCase("pie_chart_type_report"))
            heading = "Pie Chart Report";

    }
    try {
        client = new ReportTemplateClient(configContext, serverURL, cookie);
        datasources = client.getDatasourceNames();
        if (!errorString.trim().equals("")) {
%>
<script type="text/javascript">
    CARBON.showErrorDialog('<%=errorString%>');
</script>
<% }
} catch (Exception e) {
    errorString = e.getMessage();
%>
<script type="text/javascript">
    CARBON.showErrorDialog(<%=e.getMessage()%>, function() {
        location.href = "../reporting_custom/select-report.jsp";
    });
</script>
<%
    }
%>

<script type="text/javascript">
var seriesCount = 0;

function submitChartReportData() {
    var msg = checkValidity();
    if (msg != '') {
        CARBON.showErrorDialog(msg);
    } else {
        var report_type = document.getElementById('reportType').value;
        var dsName = document.getElementById('datasource').value;
        var tableName = document.getElementById('tableName').value;
        var xAxisFields = getFields('xData');
        var yAxisFields = getFields('yData');

        new Ajax.Request('ajaxprocessor-chartdata.jsp', {
                    method: 'post',
                    parameters: {
                        reportType: report_type,
                        datasource: dsName,
                        tableName: tableName,
                        xAxisFields: xAxisFields,
                        yAxisFields:yAxisFields
                    },
                    onSuccess: function(transport) {
                        var allPage = transport.responseText;
                        var divText = '<div id="msg">';
                        var closeDivText = '</div>';
                        var temp = allPage.indexOf(divText, 0);
                        var startIndex = temp + divText.length;
                        var endIndex = allPage.indexOf(closeDivText, temp);
                        var msg = allPage.substring(startIndex, endIndex);
                        if (msg.indexOf('Error', 0) != -1) {
                            CARBON.showErrorDialog(msg);
                            nextButtonValidate();
                        } else {
                            document.chartreport.action = 'ChartDataProcessor';
                            document.getElementById('noSeries').value = seriesCount;
                            document.chartreport.submit();
                        }
                    }
                    ,
                    onFailure: function(transport) {
                        document.getElementById('middle').style.cursor = '';
                        CARBON.showErrorDialog(transport.responseText);
                    }
                });
    }
}

function cancelTableData() {
    location.href = "../reporting_custom/select-report.jsp";
}

function dsChanged() {
    var dsName = document.getElementById('datasource').value;

    document.chartreport.action = 'add-chart-report.jsp';
    var reportName = document.getElementById("reportName").value;

    var reportType = document.getElementById("reportType").value;
    var url = 'add-chart-report.jsp?reportType=' + reportType + '&selectedDsName=' + dsName + '&reportName=' + reportName;
    location.href = url;
}

function tableChanged() {
    var dsName = document.getElementById('datasource').value;
    var tableName = document.getElementById('tableName').value;

    document.chartreport.action = 'add-chart-report.jsp';
    var reportName = document.getElementById("reportName").value;
    var reportType = document.getElementById("reportType").value;
    var url = 'add-chart-report.jsp?reportType=' + reportType + '&selectedDsName=' + dsName + '&selectedTableName=' + tableName + '&reportName=' + reportName;
    location.href = url;
}

function nextButtonValidate() {
    var repName = document.getElementById('reportName').value;

    var tableName = '';
    if (null != document.getElementById('tableName')) {
        tableName = document.getElementById('tableName').value;
    }
    var dsName = '';
    if (null != document.getElementById('datasource')) {
        dsName = document.getElementById('datasource').value;
    }

    var isAllSeriesName = true;
    for (var j = 0; j < seriesCount; j++) {
        var elementid = 'series_' + (j + 1) + '_name';
        if (null != document.getElementById(elementid) && document.getElementById(elementid).value == '') {
            isAllSeriesName = false;
            break;
        }
    }

    if (repName == '' || tableName == '' || dsName == '' || !isAllSeriesName) {
        document.getElementById('save').disabled = 'disabled';
    } else {
        document.getElementById('save').disabled = '';
    }
}

function addSeries() {
    document.chartreport.action = 'add-chart-report.jsp';
    seriesCount = seriesCount + 1;
    var d = document.getElementById('contentTableBody');
    var html = '<tr>';
<%if(repType != null && !repType.equalsIgnoreCase("pie_chart_type_report")){ %>
    html = html + '<td colspan="3" class="middle-header">' + '<fmt:message key="series"/>' + ' - ' + seriesCount + '</td>';
<%
}
%>
    html = html + '</tr>';

<%if(repType != null && !repType.equalsIgnoreCase("pie_chart_type_report")){ %>
    html = html + '<tr>';
    html = html + '<td>' + '<fmt:message key="series.name"/> ' + '<span class = "required" >*</span></td>';
    html = html + '<td><input id ="series_' + seriesCount + '_name" name="series_' + seriesCount + '_name"  onkeyup="nextButtonValidate()" onmousemove="nextButtonValidate()" onfocus="nextButtonValidate()" onblur="nextButtonValidate()"></td>';
    html = html + '</tr>';
<%
}
%>
    html = html + '<tr >';
    html = html + '<td class = "leftCol-small" >';
<%if(repType != null && repType.equalsIgnoreCase("pie_chart_type_report")){%>
    html = html + '<fmt:message key="key.field"/>';
<%
}
else {
%>
    html = html + '<fmt:message key="x.axis"/>';
<%
}
%>
    html = html + '<span class = "required" >*</span>';
    html = html + '</td>';
    html = html + '<td>';
    html = html + getComboBox(seriesCount, 'xData');
    html = html + '</td>';
    html = html + '</tr>';
    html = html + '<tr >';
    html = html + '<td class = "leftCol-small" >';
<%if(repType != null && repType.equalsIgnoreCase("pie_chart_type_report")){%>
    html = html + '<fmt:message key="value.field"/>';
<%
}
else {
%>
    html = html + '<fmt:message key="y.axis"/>';
<%
}
%>
    html = html + '<span class = "required" >*</span>';
    html = html + '</td>';
    html = html + '<td>';
    html = html + getComboBox(seriesCount, 'yData');
    html = html + '</td>';
    html = html + '</tr>';
    d.innerHTML = d.innerHTML + html;

    nextButtonValidate();
}

function getComboBox(seriesId, axisType) {
    var html = '<select id="series_' + seriesId + '_' + axisType + '" name="series_' + seriesId + '_' + axisType + '">';
    for (i = 0; i < fields.length; i++) {
        var opt = '<option value="' + fields[i] + '">' + fields[i] + '</option>';
        html = html + opt;
    }
    html = html + '</select>';
    return html;
}

function getFields(xydata) {
    var fields = '';
    for (var k = 0; k < seriesCount; k++) {
        var elementid = 'series_' + (k + 1) + '_' + xydata;
        if (null != document.getElementById(elementid)) {
            fields = fields + document.getElementById(elementid).value + '&#&';
        }
    }
    fields = fields.substring(0, fields.length - 3);
    return fields;
}


function checkValidity() {
    var msg = '';
    var reportName = document.getElementById("reportName").value;
    if (reportName == '') {
        msg = 'Please enter a report name.\n';
    }
<%if(repType != null && !repType.equalsIgnoreCase("pie_chart_type_report")){ %>
    for (i = 0; i < seriesCount; i++) {
        if (document.getElementById("series_" + (i + 1) + "_name").value == '') {
            msg = msg + " Please enter a series name for Series - " + (i + 1) + "\n";
        }
    }
<%
}
%>
    return msg;
}
</script>


<%!
    private String getFieldsComboBox(String[] fields, String seriesId, String axisType) {
        String html = "<select id=\"series_" + seriesId + "_" + axisType + "\" name=\"series_" + seriesId + "_" + axisType + "\">\n";

        for (int i = 0; i < fields.length; i++) {
            html = html + "<option value=\"" + fields[i] + "\" ";
            if (i == 0) {
                html = html + "selected = \"selected\">" + fields[i] + "\n";
            } else {
                html = html + ">" + fields[i] + "\n";
            }
            html = html + "</option>\n";
        }

        html = html + "</select>\n";
        return html;
    }
%>
<div id="middle">
<h2>Add <%=heading%> - Step 1</h2>

<div id="workArea">

<form id="chartreport" name="chartreport" action="" method="POST">
<table class="styledLeft">
<thead>
<tr>
    <th><span style="float: left; position: relative; margin-top: 2px;">
                            <fmt:message key="table.report.information"/></span>
    </th>
</tr>
</thead>
<tbody>


<tr>
    <td>
        <table class="normal-nopadding">
            <tbody id="contentTableBody">

            <tr>
                <td width="180px"><fmt:message key="report.name"/> <span
                        class="required">*</span></td>
                <%
                    String tempReportName = request.getParameter("reportName");
                    if (tempReportName == null) {
                        tempReportName = "";
                    }
                %>
                <td><input name="reportName"
                           id="reportName" value="<%=tempReportName%>" onkeyup="nextButtonValidate()"
                           onmousemove="nextButtonValidate()" onfocus="nextButtonValidate()"
                           onblur="nextButtonValidate()"/>
                </td>
            </tr>

            <tr>
                <td class="leftCol-small"><fmt:message key="datasource.name"/><span
                        class="required"> *</span>
                </td>
                <td>
                    <% if (datasources != null && datasources.length > 0) {
                        dsName = request.getParameter("selectedDsName");
                        if (dsName == null || dsName.equals("")) {
                            dsName = datasources[0];
                        }
                    %>
                    </thead>
                    <select id="datasource" name="datasource" onchange="dsChanged();">
                        <%
                            for (int i = 0; i < datasources.length; i++) {
                                String datasource = datasources[i];

                        %>
                        <option value="<%=datasource%>" <%=datasource.equalsIgnoreCase(dsName) ? "selected=\"selected\"" : ""%>>
                            <%=datasource%>
                        </option>
                        <% }%>
                    </select>

                    <% } else {
                        errorString = "No datasource found";
                    %>
                    <script type="text/javascript">
                        CARBON.showErrorDialog('<%=errorString%>', function() {
                            location.href = "../reporting_custom/select-report.jsp";
                        });
                    </script>
                    <% } %>
                </td>
            </tr>

            <tr>
                <td class="leftCol-small"><fmt:message key="table.name"/><span
                        class="required"> *</span>
                </td>
                <td>
                    <% String[] tableNames = null;
                        if (null != dsName && !dsName.isEmpty()) {
                            try {
                                tableNames = client.getTableNames(dsName);
                            } catch (AxisFault e) {
                                errorString = e.getMessage();
                            }
                            if (tableNames != null && tableNames.length > 0) { %>

                    <select id="tableName" name="tableName" onchange="javascript:tableChanged();">
                        <%
                            tableName = request.getParameter("selectedTableName");
                            if (tableName == null || tableName.equals("")) {
                                tableName = tableNames[0];
                            }
                            for (int i = 0; i < tableNames.length; i++) {
                                String aTableName = tableNames[i];

                        %>
                        <option value="<%=aTableName%>" <%= aTableName.equalsIgnoreCase(tableName) ? "selected=\"selected\"" : ""%>>
                            <%=aTableName%>
                        </option>
                        <% }%>
                    </select>

                    <% } else {
                        errorString = "No Tables found in datasource " + dsName;
                    %>
                    <font color="#8b0000"> <i>No Tables</i></font>
                    <script type="text/javascript">
                        CARBON.showErrorDialog('<%=errorString%>');
                    </script>
                    <% }
                    }
                    %>

                </td>
                <% if (repType != null && !repType.equalsIgnoreCase("pie_chart_type_report") && (datasources != null && datasources.length > 0)
                        && (tableNames != null && tableNames.length > 0)) { %>
                <td><a style="background-image: url(../admin/images/add.gif);"
                       class="icon-link spacer-bot" onclick="addSeries()"><fmt:message
                        key="add.series"/></a></td>
                <%
                    }
                %>
            </tr>
            <% String[] fieldNames = null;
            %>
            <script>
                var fields = new Array();
            </script>

            <% if ((null != dsName && !dsName.isEmpty()) && (null != tableName && !tableName.isEmpty())) {
                try {
                    fieldNames = client.getFieldNames(dsName, tableName); %>
            <script>
                fields = new Array(<%
                                for(int i = 0; i < fieldNames.length; i++) {
                                   out.print("\""+fieldNames[i]+"\"");
                                 if(i+1 < fieldNames.length) {
                                     out.print(",");
                                }
                                }
                            %>);
            </script>
            <% } catch (AxisFault e) {
                errorString = e.getMessage();
            %>
            <script type="text/javascript">
                CARBON.showErrorDialog('<%=errorString%>', function() {
                    location.href = "../reporting_custom/select-report.jsp";
                });
            </script>
            <%
                    }
                }

            %>

                <%--<tr>--%>
                <%--<td colspan="3" class="middle-header"><fmt:message key="series"/> - 1</td>--%>
                <%--</tr>--%>
                <%--<tr>--%>
                <%--<td><fmt:message key="series.name"/></td>--%>
                <%--<td><input name="series_1_name" id="series_1_name"></td>--%>
                <%--</tr>--%>

                <%--<tr>--%>
                <%--<td class="leftCol-small"><fmt:message key="x.axis"/><span--%>
                <%--class="required"> *</span>--%>
                <%--</td>--%>
                <%--<td>--%>
                <%--<%--%>
                <%--String comboBox = getFieldsComboBox(fieldNames, "1", "xData");--%>
                <%--%>--%>
                <%--<%=comboBox%>--%>
                <%--</td>--%>

                <%--</tr>--%>

                <%--<tr>--%>
                <%--<td class="leftCol-small"><fmt:message key="y.axis"/><span--%>
                <%--class="required"> *</span>--%>
                <%--</td>--%>
                <%--<td>--%>
                <%--<%--%>
                <%--comboBox = getFieldsComboBox(fieldNames, "1", "yData");--%>
                <%--%>--%>
                <%--<%=comboBox%>--%>
                <%--</td>--%>

                <%--</tr>--%>


                <%--<input id="selectedFields" name="selectedFields" type="hidden"/>--%>
            <%

            %>
            <input id="selectedDsName" name="selectedDsName" type="hidden"/>
            <input id="selectedTableName" name="selectedTableName" type="hidden"/>
            <input id="reportType" name="reportType" type="hidden" value="<%=repType%>"/>
            <input id="noSeries" name="noSeries" type="hidden"/>
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
            <input type="button" value="<fmt:message key="next"/>"
                   class="button" name="save" id="save"
                   onclick="javascript:submitChartReportData();"/>
            <input type="button" value="<fmt:message key="cancel"/>"
                   name="cancel" class="button"
                   onclick="javascript:cancelTableData();"/>
        </td>
    </tr>
    </tbody>

</table>
</form>
</div>
</div>
<script type="text/javascript">
    addSeries();
    nextButtonValidate();
</script>

</fmt:bundle>