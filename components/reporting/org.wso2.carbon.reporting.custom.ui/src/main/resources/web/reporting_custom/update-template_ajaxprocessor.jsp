<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.reporting.custom.ui.client.ReportResourceSupplierClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<%
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String error = "Failed to upload report template";
    ReportResourceSupplierClient client = ReportResourceSupplierClient.getInstance(config, session);
    try {
        client.updateReport(request);
    } catch (Exception e) {
        e.printStackTrace();
        request.setAttribute(CarbonUIMessage.ID, new CarbonUIMessage(error, e.getMessage(), e));
%>
<jsp:forward page="../admin/error.jsp"/>
<%
    }

%>
%>