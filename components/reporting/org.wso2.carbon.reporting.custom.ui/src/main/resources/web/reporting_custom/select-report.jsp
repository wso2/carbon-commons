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
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.reporting.custom.ui.i18n.Resources">

<carbon:breadcrumb label="select.report.type"
                   resourceBundle="org.wso2.carbon.reporting.custom.ui.i18n.Resources"
                   topPage="false" request="<%=request%>"/>


<div id="middle">
<h2>Select Report Type</h2>

<div id="workArea">

    <form id="select-type" name="select-type" action="" method="POST">
        <table id="reportOptions" class="styledLeft" cellpadding="2" cellspacing="2">
            <thead>
            <tr>
                <th colspan="6"><span style="float: left; position: relative; margin-top: 2px;">
                            <fmt:message key="select.report.type"/></span>
                </th>
            </tr>
            </thead>
            <tbody>

            <%
                boolean isTemplate = true;
                try {
                    Class.forName("org.wso2.carbon.reporting.template.ui.client.ReportTemplateClient");
                } catch (ClassNotFoundException e) {
                    isTemplate = false;
                }
                if (isTemplate) {
            %>
            <tr>
                <td colspan="4"><label><a href="../reporting-template/add-table-report.jsp"><fmt:message
                        key="table.type.report"/></a></label>
                </td>
                <td valign="middle">
                    <a href="images/table.png" title="Click to enlarge">
                        <img src="images/table.png" height="180" width="180" align="middle"/>
                    </a>
                </td>
                <td valign="middle"> Table report can be generated from preferred datasource. You can specify the datasource,column names
                    of your table,
                    formatting of your table design, etc with few steps and get your data in table report.</td>
            </tr>

            <tr>
                <td colspan="4"><label><a
                        href="../reporting-template/add-chart-report.jsp?reportType=bar_chart_type_report"><fmt:message
                        key="bar.chart.type"/></a></label>
                </td>
                <td valign="middle">
                    <a href="images/barchart.png" title="Click to enlarge">
                        <img src="images/barchart.png" height="180" width="180" align="middle">
                    </a>
                </td>
                <td valign="middle">  Barchart report which contains one or many series can be created. You can customize the number of
                    series, the data you want to plot, etc
                    with few steps and get your bar chart report.</td>
            </tr>

            <tr>
                <td colspan="4"><label><a
                        href="../reporting-template/add-chart-report.jsp?reportType=xy_bar_chart_type_report"><fmt:message
                        key="xy.bar.chart.report"/></a></label>
                </td>
                <td valign="middle">
                    <a href="images/xy_barchart.png" title="Click to enlarge">
                        <img src="images/xy_barchart.png" height="180" width="180" align="middle">
                    </a>
                </td>
                <td valign="middle"> XY barchart report which contains one or many series can be created. You can customize the number of
                    series, the data you want to plot, etc
                    with few steps and get your XY bar chart report. The chart plot requires both x and y axis to be in
                    numeric fields.</td>
            </tr>

            <tr>
                <td colspan="4">
                    <label><a
                            href="../reporting-template/add-chart-report.jsp?reportType=stacked_bar_chart_type_report"><fmt:message
                            key="stacked.bar.chart.type"/></a></label>
                </td>
                <td valign="middle">
                    <a href="images/stacked_bar_chart.png" title="Click to enlarge">
                        <img src="images/stacked_bar_chart.png" height="180" width="180" align="middle">
                    </a>
                </td>
                <td valign="middle">Stacked barchart report which contains one or many series can be created. As name explains, the
                    series will be stacked top of series.
                    You design your report by selecting the series, and defining your own formatting in few steps.</td>
            </tr>


            <tr>
                <td colspan="4"><label><a
                        href="../reporting-template/add-chart-report.jsp?reportType=line_chart_type_report"><fmt:message
                        key="line.chart.type"/></a></label>
                </td>
                <td valign="middle">
                    <a href="images/line_chart.png" title="Click to enlarge">
                        <img src="images/line_chart.png" height="180" width="180" align="middle">
                    </a>

                </td>
                   <td valign="middle">Line chart report which contains one or many series can be created. You can customize the number of
                    series, the data you want to plot, etc
                    with few steps and get your line chart report.</td>
            </tr>

            <tr>
                <td colspan="4"><label><a
                        href="../reporting-template/add-chart-report.jsp?reportType=xy_line_chart_type_report"><fmt:message
                        key="xy.line.chart.report"/></a></label>
                </td>
                <td valign="middle">
                    <a href="images/xy_line_chart.png" title="Click to enlarge">
                        <img src="images/xy_line_chart.png" height="180" width="180" align="middle">
                    </a>
                </td>
                <td valign="middle">
                    XY line chart report which contains one or many series can be created. You can customize the number
                    of series, the data you want to plot, etc
                    with few steps and get your XY line chart report. The chart plot requires both x and y axis to be in
                    numeric fields.
                </td>
            </tr>

            <tr>
                <td colspan="4"><label><a
                        href="../reporting-template/add-chart-report.jsp?reportType=area_chart_type_report"><fmt:message
                        key="area.chart.type"/></a></label>
                </td>
                <td valign="middle">
                    <a href="images/areachart.png" title="Click to enlarge">
                        <img src="images/areachart.png" height="180" width="180" align="middle">
                    </a>
                </td>
                <td valign="middle">
                     Area chart report which contains one or many series can be created. You can customize the number of
                    series, the data you want to plot, etc
                    with few steps and get your area chart report.
                </td>
            </tr>


            <tr>
                <td colspan="4"><label><a
                        href="../reporting-template/add-chart-report.jsp?reportType=xy_area_chart_type_report"><fmt:message
                        key="xy.area.chart.report"/></a></label>
                </td>
                <td valign="middle">
                    <a href="images/xy_area_chart.png" title="Click to enlarge">
                        <img src="images/xy_area_chart.png" height="180" width="180" align="middle">
                    </a>
                </td>
                <td valign="middle">
                    XY Area chart report which contains one or many series can be created. You can customize the number
                    of series, the data you want to plot, etc
                    with few steps and get your XY Area chart report. The chart plot requires both x and y axis to be in
                    numeric fields.
                </td>
            </tr>

            <tr>
                <td colspan="4">
                    <label><a
                            href="../reporting-template/add-chart-report.jsp?reportType=stacked_area_chart_type_report"><fmt:message
                            key="stacked.area.chart.type"/></a></label>
                </td>
                <td valign="middle">

                    <a href="images/stacked_area_chart.png" title="Click to enlarge">
                        <img src="images/stacked_area_chart.png" height="180" width="180" align="middle">
                    </a>
                 </td>
                <td valign="middle">
                     Stacked area chart report which contains one or many series can be created. As name explains, the
                    series will be stacked top of series.
                    You design your report by selecting the series, and defining your own formatting in few steps.
                </td>
            </tr>


            <tr>
                <td colspan="4"><label><a
                        href="../reporting-template/add-chart-report.jsp?reportType=pie_chart_type_report"><fmt:message
                        key="pie.chart.type"/></a></label>
                </td>
                <td valign="middle">
                    <a href="images/pie-chart.png" title="Click to enlarge">
                        <img src="images/pie-chart.png" height="180" width="180" align="middle">
                    </a>
                </td>
                <td valign="middle">
                      Pie chart report which can be generated from your data. You can design your report by
                    providing more formatting details for your report in few steps.
                </td>
            </tr>

            <tr>
                <td colspan="4"><label><a href="../reporting-template/add-composite-report.jsp"><fmt:message
                        key="composite.report"/></a></label>
                </td>
                <td valign="middle">

                    <a href="images/composite.png" title="Click to enlarge">
                        <img src="images/composite.png" height="180" width="180" align="middle">
                    </a>
                </td>
                <td valign="middle">
                    You can obtain form a master report of composing your selected reports you have defined and
                    produce a combination of report formats mentioned.
                </td>
            </tr>

            <%
                }
            %>
            <tr>
                <td colspan="4" width="180px"><label><a href="upload-jrxml.jsp"><fmt:message
                        key="custom.report"/></a></label>
                </td>
                <td valign="middle">
                    <a href="images/custom.jpeg" title="Click to enlarge">
                        <img src="images/custom.jpeg" height="180" width="180" border="1px" align="middle">
                    </a>
                </td>
                <td valign="middle">
                     You can define your own .jrxml file and upload the that inorder to generate your report.
                </td>
            </tr>

            </tbody>
        </table>

    </form>
</div>
</div>
<script type="text/javascript">
    alternateTableRows('reportOptions', 'tableEvenRow', 'tableOddRow');
</script>

</fmt:bundle>