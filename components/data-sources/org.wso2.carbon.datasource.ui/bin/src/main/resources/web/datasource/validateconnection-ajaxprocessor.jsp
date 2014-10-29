<%@ page import="org.wso2.carbon.datasource.ui.DatasourceManagementClient" %>
<%@ page import="org.wso2.carbon.datasource.ui.DataSourceManagementHelper" %>
<%@ page import="org.apache.synapse.commons.datasource.DataSourceInformation" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%
    String alias = request.getParameter("alias");
    boolean canAdd ;
    if (alias != null && !"".equals(alias)) {
        DatasourceManagementClient client;
        try {
            DataSourceInformation dataSourceInformation = DataSourceManagementHelper.createDataSourceInformation(request);
            client = DatasourceManagementClient.getInstance(config, session);
            canAdd = client.testConnection(dataSourceInformation);
%>
<%=canAdd%>
<%
} catch (Throwable e) {
%><%=e.getMessage()%><%
        }
    }
%>


