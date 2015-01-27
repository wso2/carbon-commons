<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
 <%@ page import="org.wso2.carbon.ndatasource.ui.NDataSourceAdminServiceClient" %>
 
<script type="text/javascript" src="global-params.js"></script>
<script type="text/javascript" src="dscommon.js"></script>
<fmt:bundle basename="org.wso2.carbon.ndatasource.ui.i18n.Resources">
 <%
String dataSourceName = request.getParameter("name");
if (dataSourceName == null || "".equals(dataSourceName)) {
    throw new ServletException("Name is empty");
}

NDataSourceAdminServiceClient client = NDataSourceAdminServiceClient.getInstance(config, session);
dataSourceName = dataSourceName.trim();
client.deleteDataSource(dataSourceName);
%>
	<script type="text/javascript">
        forward("index.jsp");
    </script>
</fmt:bundle>