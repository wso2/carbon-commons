<%@ page import="org.wso2.carbon.reporting.custom.ui.beans.FormParameters" %>
<%@ page import="org.wso2.carbon.reporting.custom.ui.client.ReportResourceSupplierClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>


<%
    ReportResourceSupplierClient resourcesSupplier = ReportResourceSupplierClient.getInstance(config, session);
    FormParameters[] formParameters = new FormParameters[0];
    String error = "Failed to get report parameters";
    String name = request.getParameter("reportName");
    try {
        formParameters = resourcesSupplier.getReportParam(name);
    } catch (Exception e) {
        request.setAttribute(CarbonUIMessage.ID, new CarbonUIMessage(error, e.getMessage(), e));
%>
    <jsp:forward page="../admin/error.jsp"/>
<%
    }

%>


<%

    if (formParameters.length > 0) {
%>
<table class="styledLeft" style="width:auto">
    <thead>
        <th colspan="3">Required Parameters</th>
    </thead>
    <tbody>
    <%

        for (FormParameters reportParameters : formParameters) {
            if (reportParameters != null) {

                String paramName = reportParameters.getFormName();
                String paramValue = reportParameters.getFormValue();
                String conCat = paramName + "/" + paramValue;

    %>
    <tr>
        <td style="text-align:right !important">
        <%=paramName%><span class="required">*</span>
        </td>
        <td>
            <input type="text" name="input_param" id="<%=conCat%>"></td>
        <%--<td>--%>
            <%--<%=paramValue%>--%>
        <%--</td>--%>
    </tr>

    <%
            }
        }
    %>
    </tbody>
</table>
<%
    }
%>

