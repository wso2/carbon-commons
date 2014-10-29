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
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.reporting.template.ui.client.ReportTemplateClient" %>
<%@ page import="org.apache.axis2.AxisFault" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.reporting.template.ui.i18n.Resources">
<script type="text/javascript" src="../ajax/js/prototype.js"></script>


<script type="text/javascript">

    function submitTableReportData() {
        document.tablereport.action = 'table-data-save.jsp';
        var reportName = document.getElementById("reportName").value;
        if (reportName == '') {
            CARBON.showErrorDialog('Please enter report name');
            return false;
        }

        var counter = 0,
                i = 0,
                fieldsStr = '',
                input_obj = document.getElementsByTagName('input');

        for (i = 0; i < input_obj.length; i++) {
            if (input_obj[i].type === 'checkbox' && input_obj[i].checked === true) {
                counter++;
                fieldsStr = fieldsStr + ',' + input_obj[i].value;
            }
        }
        if (counter > 0) {
            fieldsStr = fieldsStr.substr(1);
        }
        else {
            alert('There is no checked checkbox');
            return false;
        }
        document.getElementById('selectedFields').value = fieldsStr;

        document.tablereport.submit();
        return true;
    }

    function cancelTableData() {
        location.href = "../reporting_custom/select-report.jsp";
    }

    function dsChanged() {
        var dsName = document.getElementById('datasource').value;

        new Ajax.Request('tableInfo-ajaxprocessor.jsp', {
                    method: 'post',
                    parameters: {dsName:dsName},
                    onSuccess: function(transport) {
                        var allPage = transport.responseText;
                        var divText = '<div id="returnedResults">';
                        var closeDivText = '</div>';
                        var temp = allPage.indexOf(divText, 0);
                        var startIndex = temp + divText.length;
                        var endIndex = allPage.indexOf(closeDivText, temp);
                        var queryResults = allPage.substring(startIndex, endIndex);
                        document.getElementById('tableResult').innerHTML = queryResults;
                        var tableName = '';
                        if (null != document.getElementById('tableName')) {
                            tableName = document.getElementById('tableName').value;
                        }
                        tableChangedWithDSName(dsName, tableName);
                        nextButtonValidate();
                    },
                    onFailure: function(transport) {
                        document.getElementById('middle').style.cursor = '';
                        CARBON.showErrorDialog(transport.responseText);
                    }
                });

    }

    function tableChangedWithDSName(dsName, tableName) {
        new Ajax.Request('tableFieldsInfo-ajaxprocessor.jsp', {
                    method: 'post',
                    parameters: {dsName:dsName,
                        tableName: tableName},
                    onSuccess: function(transport) {
                        var allPage = transport.responseText;
                        var divText = '<div id="returnedResults">';
                        var closeDivText = '</div>';
                        var temp = allPage.indexOf(divText, 0);
                        var startIndex = temp + divText.length;
                        var endIndex = allPage.indexOf(closeDivText, temp);
                        document.getElementById('fieldResults').innerHTML = allPage.substring(startIndex, endIndex);

                        var div2Text = '<div id="returnedResultsPrimaryField">';
                        var temp2 = allPage.indexOf(div2Text, endIndex);
                        var start2Index = temp2 + div2Text.length;
                        var end2Index = allPage.indexOf(closeDivText, temp2);
                        document.getElementById('primaryFieldDiv').innerHTML = allPage.substring(start2Index, end2Index);

                        nextButtonValidate();
                    },
                    onFailure: function(transport) {
                        document.getElementById('middle').style.cursor = '';
                        CARBON.showErrorDialog(transport.responseText);
                    }
                });
    }

    function nextButtonValidate() {
        var repName = document.getElementById('reportName').value;

        var tableName = '';
        if (null != document.getElementById('tableName')) {
            tableName = document.getElementById('tableName').value;
        }
        var fields = '';
        if (null != document.getElementById('primaryField')) {
            fields = 'yes';
        }
        if (tableName == '' || fields == '' || repName == '') {
            document.getElementById('save').disabled = 'disabled';
        } else {
            document.getElementById('save').disabled = '';
        }
    }

    function tableChanged() {
        var dsName = document.getElementById('datasource').value;
        var tableName = document.getElementById('tableName').value;

        new Ajax.Request('tableFieldsInfo-ajaxprocessor.jsp', {
                    method: 'post',
                    parameters: {dsName:dsName,
                        tableName: tableName},
                    onSuccess: function(transport) {
                        var allPage = transport.responseText;
                        var divText = '<div id="returnedResults">';
                        var closeDivText = '</div>';
                        var temp = allPage.indexOf(divText, 0);
                        var startIndex = temp + divText.length;
                        var endIndex = allPage.indexOf(closeDivText, temp);
                        document.getElementById('fieldResults').innerHTML = allPage.substring(startIndex, endIndex);

                        var div2Text = '<div id="returnedResultsPrimaryField">';
                        var temp2 = allPage.indexOf(div2Text, endIndex);
                        var start2Index = temp2 + div2Text.length;
                        var end2Index = allPage.indexOf(closeDivText, temp2);
                        document.getElementById('primaryFieldDiv').innerHTML = allPage.substring(start2Index, end2Index);

                        nextButtonValidate();
                    },
                    onFailure: function(transport) {
                        document.getElementById('middle').style.cursor = '';
                        CARBON.showErrorDialog(transport.responseText);
                    }
                });
    }

</script>

<carbon:breadcrumb label="add.report.step1"
                   resourceBundle="org.wso2.carbon.reporting.template.ui.i18n.Resources"
                   topPage="false" request="<%=request%>"/>

<%

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    ReportTemplateClient client = null;
    String message = "";
    String[] datasources = null;
    String errorString = "";
    String dsName = "";
    String tableName = "";
    try {
        client = new ReportTemplateClient(configContext, serverURL, cookie);
        datasources = client.getDatasourceNames();
        if (datasources == null || datasources.length == 0) {
            errorString = "No data source found! Please add a data source!";
%>
<script type="text/javascript">
    CARBON.showErrorDialog('<%=errorString%>', function() {
        location.href = "../reporting_custom/select-report.jsp";
    });
    // document.getElementById('save').disabled = true;
</script>

<% }
} catch (Exception e) {
    errorString = e.getMessage();
%>
<script type="text/javascript">
    CARBON.showErrorDialog('<%=errorString%>', function() {
        location.href = "../reporting_custom/select-report.jsp";
    });
</script>
<%
    }
%>

<div id="middle">
<h2>Add Table Report - Step 1</h2>

<div id="workArea">

<form id="tablereport" name="tablereport" action="" method="POST">
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
                <tbody>

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
                        <select id="datasource" name="datasource" onchange="dsChanged()">
                            <%
                                for (int i = 0; i < datasources.length; i++) {
                                    String datasource = datasources[i];

                            %>
                            <option value="<%=datasource%>" <%=datasource.equalsIgnoreCase(dsName) ? "selected=\"selected\"" : ""%>>
                                <%=datasource%>
                            </option>
                            <% }%>
                        </select>

                        <% }
                        %>
                    </td>
                </tr>

                <tr>
                    <td class="leftCol-small"><fmt:message key="table.name"/><span
                            class="required"> *</span>
                    </td>
                    <td>
                        <div id="tableResult">
                            <% String[] tableNames = null;
                                if (!dsName.isEmpty()) {
                                    try {
                                        tableNames = client.getTableNames(dsName);
                                    } catch (AxisFault e) {
                                        errorString = e.getMessage();
                                    }
                                    if (tableNames != null && tableNames.length > 0) { %>


                            <select id="tableName" name="tableName" onchange="tableChanged()">
                                <%
                                    tableName = request.getParameter("selectedTableName");
                                    if (tableName == null || tableName.equals("")) {
                                        tableName = tableNames[0];
                                    }
                                    for (int i = 0; i < tableNames.length; i++) {
                                        String aTableName = tableNames[i];

                                %>
                                <option value="<%=aTableName%>" <%= aTableName.equalsIgnoreCase(tableName) ? "selected=\"selected\"" : ""%> >

                                    <%=aTableName%>
                                </option>
                                <% }
                                %>
                            </select>


                            <% } else {
                                errorString = "No Tables found in datasource " + dsName;
                            %>
                            <script type="text/javascript">
                                CARBON.showErrorDialog('<%=errorString%>');
                            </script>
                            <% }
                            }
                            %>
                        </div>
                    </td>
                </tr>

                <tr>
                    <td class="leftCol-small"><fmt:message key="field.names"/><span
                            class="required"> *</span>
                    </td>
                    <td>
                        <div id="fieldResults">
                            <% String[] fieldNames = null;
                                if ((null != dsName && !dsName.isEmpty()) && (null != tableName && !tableName.isEmpty())) {
                                    try {
                                        fieldNames = client.getFieldNames(dsName, tableName);
                                    } catch (AxisFault e) {
                                        errorString = e.getMessage();
                                    }
                                    if (fieldNames != null && fieldNames.length > 0) {
                                        for (int i = 0; i < fieldNames.length; i++) {

                                            String aField = fieldNames[i];
                            %>
                            <input type="checkbox" name="<%=aField%>"
                                   value="<%=aField%>" <%=i == 0 ? "checked" : ""%>/><%=aField%><br/>

                            <% }
                            } else {
                                errorString = "No fields found in table " + tableName;
                            %>
                            <script type="text/javascript">
                                CARBON.showErrorDialog('<%=errorString%>', function() {
                                    location.href = "../reporting_custom/select-report.jsp";
                                });
                            </script>
                            <% }
                            }
                            %>
                        </div>
                    </td>

                </tr>

                <input id="selectedFields" name="selectedFields" type="hidden"/>
                <input id="selectedDsName" name="selectedDsName" type="hidden"/>
                <input id="selectedTableName" name="selectedTableName" type="hidden"/>
                <tr>
                    <td class="leftCol-small"><fmt:message key="primary.field"/><span
                            class="required"> *</span>
                    </td>
                    <td>
                        <div id='primaryFieldDiv'>
                            <%
                                if (fieldNames != null && fieldNames.length > 0) {
                            %>

                            <select id="primaryField" name="primaryField">
                                <%
                                    for (int i = 0; i < fieldNames.length; i++) {

                                        String aField = fieldNames[i];
                                %>
                                <option value="<%=aField%>" <%=i == 0 ? "selected=\"selected\"" : ""%>>
                                    <%=aField%>
                                </option>

                                <% }
                                %>
                            </select>
                            <% }
                            %>
                        </div>
                    </td>
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
            <input type="button" value="<fmt:message key="next"/>"
                   class="button" name="save" id="save"
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
<script type="text/javascript">
    nextButtonValidate();
</script>

</fmt:bundle>