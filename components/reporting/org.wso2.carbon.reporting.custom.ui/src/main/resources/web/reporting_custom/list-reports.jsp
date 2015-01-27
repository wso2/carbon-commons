<%
    boolean isTemplate = true;
    try {
        Class.forName("org.wso2.carbon.reporting.template.ui.client.ReportTemplateClient");
    } catch (ClassNotFoundException e) {
        isTemplate = false;
    }
%>

<%@ page import="org.wso2.carbon.reporting.custom.ui.client.DBReportingServiceClient" %>
<%@ page import="org.wso2.carbon.reporting.custom.ui.client.ReportResourceSupplierClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<% if (isTemplate) {
%>
<%@ page import="org.wso2.carbon.reporting.custom.ui.client.ReportTemplateClient" %>
<%
    }
%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>

<fmt:bundle basename="org.wso2.carbon.reporting.ui.i18n.Resources">
    <script type="text/javascript" src="../js/reporting-commons.js"></script>
    <script type="text/javascript" src="../ajax/js/prototype.js"></script>

    <%
        ReportResourceSupplierClient resourcesSupplier = ReportResourceSupplierClient.getInstance(config, session);
        String[] reports = new String[0];
        String error2 = "Failed to get report list";

        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        try {
            reports = resourcesSupplier.getAllReports();
        } catch (Exception e) {
            request.setAttribute(CarbonUIMessage.ID, new CarbonUIMessage(error2, e.getMessage(), e));
    %>
    <jsp:forward page="../admin/error.jsp"/>
    <%
        }

    %>

    <carbon:breadcrumb
            label="report.manage.menu.text"
            resourceBundle="org.wso2.carbon.reporting.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>
    <br/>

    <div id="middle">
        <h2><fmt:message key="available.report"/></h2>

        <div id="workArea">
            <br/>

            <form action="../dbreport" id="report-form" name="reportConfig" method="post"
                  target="_blank">
                <table class="styledLeft" id="table1">
                    <thead>
                    <th class="tableOddRow" colspan="3">
                        <fmt:message key="report.templates"/>
                    </th>
                    </thead>
                    <tbody>
                    <%
                        if (reports != null && reports.length > 0) {
                            for (int i = 0; i < reports.length; i++) {
                                if (reports[i] != null) {
                    %>
                    <tr>
                        <td>
                            <label>
                                <%
                                    String directTo = "";
                                    if (isTemplate) {
                                        ReportTemplateClient client = null;
                                        try {
                                            client = new ReportTemplateClient(configContext, serverURL, cookie);
                                            if (client.isReportTemplate(reports[i])) {
                                                directTo = "../reporting-template/template-report-generator.jsp?reportName=" + reports[i];
                                            } else {
                                                directTo = "report-details.jsp?reportName=" + reports[i];
                                            }
                                        } catch (Exception e) {
                                            directTo = "report-details.jsp?reportName=" + reports[i];
                                        }
                                    } else {
                                        directTo = "report-details.jsp?reportName=" + reports[i];
                                    }

                                %>
                                <a id="reportName<%=i%>" name="reportName"
                                   href="<%=directTo%>"><%=reports[i]%>
                                </a>
                            </label>
                        </td>
                        <td style="vertical-align:top !important;"><a class="edit-icon-link"
                                                                      href="template-editer.jsp?reportName=<%=reports[i]%>">Edit</a>
                            <a onclick="deleteRow('<%=reports[i]%>','Do you want to delete')"
                               class="delete-icon-link" href="#" style="margin-left:50px">Delete</a>
                        </td>
                    </tr>
                    <%
                            }
                        }
                    } else {
                    %>
                    <tr>
                        <td colspan="2"><fmt:message key="report.notemplates"/></td>

                    </tr>
                    <%
                        }
                    %>
                    </tbody>
                </table>

            </form>
        </div>
    </div>
    <script type="text/javascript">
        alternateTableRows('table1', 'tableEvenRow', 'tableOddRow');
        alternateTableRows('table2', 'tableEvenRow', 'tableOddRow');
    </script>
</fmt:bundle>
