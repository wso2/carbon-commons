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
<%@ page import="org.wso2.carbon.statistics.ui.Utils" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>

<fmt:bundle basename="org.wso2.carbon.statistics.ui.i18n.Resources">
<carbon:breadcrumb
        label="system.statistics"
        resourceBundle="org.wso2.carbon.statistics.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>

<script type="text/javascript" src="js/statistics.js"></script>
<script type="text/javascript" src="js/graphs.js"></script>

<script type="text/javascript" src="../admin/js/jquery.flot.js"></script>
<script type="text/javascript" src="../admin/js/excanvas.js"></script>

<%
    int statRefreshInterval = 6000;
    statRefreshInterval = Utils.getPositiveIntegerValue(session, request, statRefreshInterval, "statRefreshInterval");

    int responseTimeGraphWidth = 500;
    responseTimeGraphWidth = Utils.getPositiveIntegerValue(session, request, responseTimeGraphWidth, "responseTimeGraphWidth");

    int memoryGraphWidth = 500;
    memoryGraphWidth = Utils.getPositiveIntegerValue(session, request, memoryGraphWidth, "memoryGraphWidth");

    int responseTimeGraphXScale = 50;
    responseTimeGraphXScale = Utils.getPositiveIntegerValue(session, request, responseTimeGraphXScale, "responseTimeGraphXScale");

    int memoryGraphXScale = 25;
    memoryGraphXScale = Utils.getPositiveIntegerValue(session, request, memoryGraphXScale, "memoryGraphXScale");

    boolean isSuperTenant = CarbonUIUtil.isSuperTenant(request);
%>
<script id="source" type="text/javascript">
    jQuery.noConflict();
    var responseTimeGraphWidth = <%= responseTimeGraphWidth%>;
    var memoryGraphWidth = <%= memoryGraphWidth %>;

    var responseTimeXScale = <%= responseTimeGraphXScale %>;
    var memoryXScale = <%= memoryGraphXScale%>;

    initStats(responseTimeXScale, memoryXScale);

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

    function drawMemoryGraph() {
        jQuery.plot(jQuery("#memoryGraph"), [
            {
                label: "<fmt:message key="used"/>",
                data: graphUsedMemory.get(),
                lines: { show: true, fill: true }
            },
            {
                label: "<fmt:message key="allocated"/>",
                data: graphTotalMemory.get(),
                lines: { show: true, fill: true }
            }
        ], {
            xaxis: {
                ticks: graphUsedMemory.tick(),
                min: 0
            },
            yaxis: {
                ticks: 10,
                min: 0
            }
        });
    }

    function draw() {
        drawResponseTimeGraph();
        drawMemoryGraph();
    }

    function checkMinValues() {
        var statRefreshInterval = document.statsConfigForm.statRefreshInterval.value;
        if (statRefreshInterval < 500) {
            document.statsConfigForm.statRefreshInterval.value = 500;
        }
        var respTimeGraphXScale = document.statsConfigForm.responseTimeGraphXScale.value;
        if (respTimeGraphXScale < 10) {
            document.statsConfigForm.responseTimeGraphXScale.value = 10;
        }
        var memoryGraphXScale = document.statsConfigForm.memoryGraphXScale.value;
        if (memoryGraphXScale < 10) {
            document.statsConfigForm.memoryGraphXScale.value = 10;
        }
        var responseTimeGraphWidth = document.statsConfigForm.responseTimeGraphWidth.value;
        if (responseTimeGraphWidth < 250) {
            document.statsConfigForm.responseTimeGraphWidth.value = 250;
        }
        var memoryGraphWidth = document.statsConfigForm.memoryGraphWidth.value;
        if (memoryGraphWidth < 250) {
            document.statsConfigForm.memoryGraphWidth.value = 250;
        }
    }

    function restoreDefaultValues() {
        CARBON.showConfirmationDialog("<fmt:message key="restore.defaults.prompt"/>", function() {
            document.statsConfigForm.statRefreshInterval.value = 6000;
            document.statsConfigForm.responseTimeGraphXScale.value = 50;
            document.statsConfigForm.memoryGraphXScale.value = 50;
            document.statsConfigForm.responseTimeGraphWidth.value = 500;
            document.statsConfigForm.memoryGraphWidth.value = 500;
            document.statsConfigForm.submit();
        });
    }
</script>

<div id="middle">
    <h2><fmt:message key="system.statistics"/></h2>

    <div id="workArea">
        <div id="result"></div>
        <script type="text/javascript">
            jQuery.noConflict()
            var refresh;
            function refreshStats() {
                var url = "system_stats_ajaxprocessor.jsp";
                jQuery("#result").load(url, null, function (responseText, status, XMLHttpRequest) {
                    if (status != "success") {
                        stopRefreshStats();
                        document.getElementById('result').innerHTML = responseText;
                    }
                });
            }
            function stopRefreshStats() {
                if (refresh) {
                    clearInterval(refresh);
                }
            }
            jQuery(document).ready(function() {
                refreshStats();
                refresh = setInterval("refreshStats()", <%= statRefreshInterval %>);
            });
        </script>

        <p>&nbsp;</p>

        <form action="index.jsp" method="post" name="statsConfigForm">
            <table width="100%" class="styledLeft" style="margin-left: 0px;">
                <thead>
                <tr>
                    <th colspan="4"><fmt:message key="statistics.configuration"/></th>
                </tr>
                </thead>
                <tr>
                    <td width="20%"><fmt:message key="statistics.refresh.interval"/></td>
                    <td colspan="3">
                        <input type="text" value="<%= statRefreshInterval%>"
                               name="statRefreshInterval"
                               size="5" maxlength="5"/>
                    </td>
                </tr>
                <tr>
                    <td colspan="4">&nbsp;</td>
                </tr>

                <tr>
                    <td colspan="2" width="50%"><strong><fmt:message key="response.time.graph"/></strong>
                    </td>
                    <%
                        if (isSuperTenant) {
                    %>
                    <td colspan="2" width="50%"><strong><fmt:message key="memory.graph"/></strong></td>
                    <%
                        }
                    %>
                </tr>
                <tr>
                    <td width="20%">
                        <fmt:message key="x.scale"/>
                    </td>
                    <td width="30%">
                        <input type="text" size="5" value="<%= responseTimeGraphXScale%>"
                               name="responseTimeGraphXScale"
                               maxlength="4"/>
                    </td>
                    <%
                        if (isSuperTenant) {
                    %>
                    <td width="20%">
                        <fmt:message key="x.scale"/>
                    </td>

                    <td width="30%">
                        <input type="text" size="5" value="<%= memoryGraphXScale%>"
                               name="memoryGraphXScale"
                               maxlength="4"/>
                    </td>

                    <%
                        }
                    %>
                </tr>
                <tr>
                    <td width="20%">
                        <fmt:message key="x.width"/>
                    </td>
                    <td width="30%">
                        <input type="text" size="5" value="<%= responseTimeGraphWidth%>"
                               name="responseTimeGraphWidth"
                               maxlength="4"/>
                    </td>
                    <%
                        if (isSuperTenant) {
                    %>
                    <td width="20%">
                        <fmt:message key="x.width"/>
                    </td>
                    <td width="30%">
                        <input type="text" size="5" value="<%= memoryGraphWidth%>"
                               name="memoryGraphWidth"
                               maxlength="4"/>
                    </td>
                    <%
                        }
                    %>
                </tr>
                <tr>
                    <td colspan="4">
                        &nbsp;
                    </td>
                </tr>
                <tr>
                    <td colspan="4" class="buttonRow">
                        <input type="button" class="button" value="<fmt:message key="update"/>"
                               id="updateStats" onclick="checkMinValues();document.statsConfigForm.submit()"/>&nbsp;&nbsp;
                        <input type="reset" class="button" value="<fmt:message key="reset"/>"/>&nbsp;&nbsp;
                        <input type="button" class="button" value="<fmt:message key="restore.defaults"/>"
                               id="restoreDefaults" onclick="restoreDefaultValues()"/>&nbsp;&nbsp;
                    </td>
                </tr>
            </table>
        </form>
    </div>
</div>
</fmt:bundle>
