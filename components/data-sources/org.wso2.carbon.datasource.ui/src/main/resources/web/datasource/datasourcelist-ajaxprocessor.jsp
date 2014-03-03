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
<%@page import="java.util.Map.Entry"%>
<%@ page import="org.apache.synapse.commons.datasource.DataSourceInformation" %>
<%@ page import="org.wso2.carbon.datasource.ui.DatasourceManagementClient" %>
<%@ page import="java.util.Map" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%
    Map <String,String> dataSources = null;
    DatasourceManagementClient client;
    try {
        client = DatasourceManagementClient.getInstance(config, session);
        dataSources = client.getAllDataSourceInformations();
%>
<% for (Map.Entry<String,String> entry : dataSources.entrySet()) { %>
<%if("Active".equalsIgnoreCase(entry.getValue())){ %>
<% DataSourceInformation info = client.getDataSourceInformation(entry.getKey());%>
<option value="<%=info.getDatasourceName()%>"><%=info.getDatasourceName()%></option>
<%} %>
<% } %>
<%
} catch (Throwable e) {
%><%=e.getMessage()%><%
    }

%>