<%@ page import="org.wso2.carbon.reporting.custom.ui.client.ReportResourceSupplierClient" %>
<%@ page import="org.wso2.carbon.reporting.custom.ui.client.JrxmlFileUploaderClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>

<fmt:bundle basename="org.wso2.carbon.reporting.ui.i18n.Resources">
    <carbon:breadcrumb
            label="report.details.text"
            resourceBundle="org.wso2.carbon.reporting.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

<script src="../editarea/edit_area_full.js" type="text/javascript"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="../js/reporting-commons.js"></script>
<script type="text/javascript">

    YAHOO.util.Event.onDOMReady(function() {
        editAreaLoader.init({
                                id : "payload"
                                ,syntax: "xml"
                                ,start_highlight: true
                            });
    });
</script>
    <%
        String reportName = request.getParameter("reportName");
        String reportCode = null;
        String error = "Failed to upload template";
        String error2= "Failed get report template";
        ReportResourceSupplierClient resourcesSupplier = ReportResourceSupplierClient.getInstance(config, session);
        JrxmlFileUploaderClient uploaderClient = JrxmlFileUploaderClient.getInstance(config, session);

        try {
            reportCode = resourcesSupplier.getReportResources(null, reportName);
        } catch (Exception e) {
            request.setAttribute(CarbonUIMessage.ID, new CarbonUIMessage(error2, e.getMessage(), e));
    %>
    <jsp:forward page="../admin/error.jsp"/>
    <%
        }
    %>
    <%
        try {
            uploaderClient.uploadJrxmlFile(reportName, reportCode);
        } catch (Exception e) {
            request.setAttribute(CarbonUIMessage.ID, new CarbonUIMessage(error, e.getMessage(), e));
    %>
    <jsp:forward page="../admin/error.jsp"/>
    <%
        }
    %>

<div id="middle">
    <h2><fmt:message key="report.editor"/> - <%=reportName%>
    </h2>

    <div id="workArea">
        <form id="report-template-editor">
            <textarea id="payload" cols="150" rows="25">
                <%=reportCode%>
            </textarea>
            <input class="button" type="button" onclick="saveReport('<%=request.getParameter("reportName")%>')" value="Save"/>

            <input type="button" onclick="document.location.href='list-reports.jsp?region=region5&item=reporting_list'" value="Cancel"
                   class="button">
        </form>
    </div>
</div>
    </fmt:bundle>