<%@ page import="org.wso2.carbon.endpoint.ui.client.EndpointAdminClient" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%
    String endpointName = request.getParameter("endpointName");
    String endpointAddr = request.getParameter("endpointAddress");

    if (endpointName == null || "".equals(endpointName)) {
        request.setAttribute("wsd.endpoint.error", "invalid.name");
    } else if (endpointAddr == null || "".equals(endpointAddr)) {
        request.setAttribute("wsd.endpoint.error", "invalid.address");
    } else {
        try {
            ConfigurationContext configCtx = (ConfigurationContext) config.
                    getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            EndpointAdminClient endpointClient =
                    new EndpointAdminClient(cookie, backendServerURL, configCtx);
            String endpoint = "<endpoint xmlns=\"http://ws.apache.org/ns/synapse\" name=\"" +
                    endpointName + "\"><address uri=\"" + endpointAddr + "\"/></endpoint>";
            endpointClient.addEndpoint(endpoint);
            request.setAttribute("wsd.endpoint.success", "true");
        } catch (Exception e) {
            request.setAttribute("wsd.endpoint.error", "unexpected.error");
            String cause;
            if (e.getCause() != null) {
                cause = e.getCause().getMessage();
            } else {
                cause = e.getMessage();
            }
            request.setAttribute("wsd.endpoint.error.cause", cause);
        }
    }
%>