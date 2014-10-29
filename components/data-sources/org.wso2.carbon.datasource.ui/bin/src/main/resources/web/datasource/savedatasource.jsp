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
<%@ page import="org.wso2.carbon.datasource.ui.DataSourceClientConstants" %>
<%@ page import="org.wso2.carbon.datasource.ui.DataSourceManagementHelper" %>
<%@ page import="org.wso2.carbon.datasource.ui.DatasourceManagementClient" %>
<%@ page import="org.apache.synapse.commons.datasource.DataSourceInformation" %>

<script type="text/javascript" src="global-params.js"></script>
<script type="text/javascript" src="dscommon.js"></script>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:bundle basename="org.wso2.carbon.datasource.ui.i18n.Resources">
    <% String saveMode = request.getParameter("saveMode");
        boolean edit = "edit".equals(saveMode);
        DatasourceManagementClient client;
        boolean canAdd = true;
        String name = "";
        try {
            DataSourceInformation dataSourceInformation = DataSourceManagementHelper.createDataSourceInformation(request);
            client = DatasourceManagementClient.getInstance(config, session);
            name = dataSourceInformation.getAlias();
            if (edit) {
                client.editDatasourceInformation(dataSourceInformation);
                request.getSession().setAttribute(
                        DataSourceClientConstants.DATASOURCE_KEY + dataSourceInformation.getAlias().trim(), dataSourceInformation);
            } else {
                canAdd = !client.isContains(dataSourceInformation.getAlias());
                if (canAdd) {
                    client.addDataSourceInformation(dataSourceInformation);
                    request.getSession().setAttribute(
                            DataSourceClientConstants.DATASOURCE_KEY + dataSourceInformation.getAlias().trim(), dataSourceInformation);
                }
            }
            if (!canAdd) {%>

    <script type="text/javascript">
        jQuery(document).ready(function() {
            CARBON.showWarningDialog("<fmt:message key="cannot.add.a.data.source"/>"+"<fmt:message key="a.datasource.with.name"/> " + '<%=name%>' + " <fmt:message key="already.exists"/>", function() {
                 goBackOnePage();
            }, function () {
                 goBackOnePage();
            });
        });
    </script>
    <% } else { %>
    <script type="text/javascript">
        forward("index.jsp");
    </script>
    <% }
    } catch (Throwable e) {
        request.getSession().setAttribute(DataSourceClientConstants.EXCEPTION, e);
    %>
    <script type="text/javascript">
        jQuery(document).ready(function() {
            CARBON.showErrorDialog("<%=e.getMessage()%>", function () {
                goBackOnePage();
            }, function () {
                goBackOnePage();
            });
        });

    </script>
    <%
        } %>

</fmt:bundle>
 
   