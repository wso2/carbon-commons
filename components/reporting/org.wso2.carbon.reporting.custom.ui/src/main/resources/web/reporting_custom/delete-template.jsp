<%@ page import="org.wso2.carbon.reporting.custom.ui.client.ReportResourceSupplierClient" %>
<%@ page import="org.wso2.carbon.reporting.custom.ui.client.ReportTemplateClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<script type="text/javascript" src="global-params.js"></script>
<script type="text/javascript" src="../js/reporting-commons.js"
<%
    String name = request.getParameter("reportName");
    if (name == null || "".equals(name)) {
        throw new ServletException("Name is empty");
    }
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ReportResourceSupplierClient client;

    try {
        client = ReportResourceSupplierClient.getInstance(config, session);
        name = name.trim();

        ReportTemplateClient templateClient =  new ReportTemplateClient(configContext,serverURL, cookie);
        if(templateClient.isReportTemplate(name)){
            templateClient.deleteReportInfo(name);
        }
           client.deleteReportTemplate(name);
%>
<script type="text/javascript">
    location.href = "../reporting_custom/list-reports.jsp?region=region5&item=reporting_list";
</script>
<%
} catch (Throwable e) {
    e.printStackTrace();
%>
<script type="text/javascript">
    jQuery(document).ready(function() {
        CARBON.showErrorDialog('<%=e.getMessage()%>', function() {
            location.href = "../reporting_custom/list-reports.jsp?region=region5&item=reporting_list";
        }, function () {
            location.href = "../reporting_custom/list-reports.jsp?region=region5&item=reporting_list";
        });
    });
</script>
<%
        return;
    }
%>
<script type="text/javascript">
    location.href = "../reporting_custom/list-reports.jsp?region=region5&item=reporting_list";
</script>
