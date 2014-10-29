<%@ page contentType="text/html; charset=iso-8859-1" language="java" %>

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.reporting.template.ui.client.ReportTemplateClient" %>

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

<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    String dsName = request.getParameter("dsName");
    try {
        ReportTemplateClient client = new ReportTemplateClient(configContext, serverURL, cookie);
        String[] results = client.getTableNames(dsName);
        if (null != results && results.length > 0) {
%>

<div id="returnedResults">
    <select id="tableName" name="tableName"  onchange="tableChanged()">
        <%
            int count = 0;
            for (String result : results) {
        %>

        <option value="<%=result%>" <%= count == 0 ? "selected=\"selected\"" : ""%>>
            <%=result%>
        </option>
        <%
                count++;
            }
        %>
    </select>

</div>
<%
} else {
%>
<div id="returnedResults">
    <font color="#8b0000"> <i>No Tables</i></font>
</div>
<% }
} catch (Exception e) {
%>
<div id="returnedResults">
    <font color="#8b0000"> <i>No Tables</i></font>
</div>

<% }
%>
