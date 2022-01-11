<%@ page import="org.wso2.carbon.ndatasource.ui.NDataSourceAdminServiceClient"%>
<%@ page import="org.wso2.carbon.ndatasource.ui.NDataSourceHelper"%>
<%@ page import="org.wso2.carbon.ndatasource.ui.stub.core.services.xsd.WSDataSourceMetaInfo"%>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%
    String dsName = request.getParameter("dsName");
	
    boolean canAdd ;
    if (dsName != null && !"".equals(dsName)) {
    	NDataSourceAdminServiceClient client;
        try {
        	client = NDataSourceAdminServiceClient.getInstance(config, session);
        	WSDataSourceMetaInfo dataSourceMetaInformation = NDataSourceHelper
					.createWSDataSourceMetaInfo(request, client);
            canAdd = client.testDataSourceConnection(dataSourceMetaInformation);
%>
<%=canAdd%>
<%
} catch (Throwable e) {
%><%=Encode.forHtml(e.getMessage())%><%
        }
    }
%>


