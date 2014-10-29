<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.deployment.synchronizer.ui.client.DeploymentSyncAdminClient" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>

<%
    try {
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        DeploymentSyncAdminClient client = new DeploymentSyncAdminClient(
                configContext, backendServerURL, cookie, request.getLocale());
        String mode = request.getParameter("mode");

        long timestamp;
        if ("checkout".equals(mode)) {
            timestamp = client.getLastCheckoutTime();
        } else {
            timestamp = client.getLastCommitTime();
        }

        if (timestamp != -1) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MMMMM-dd hh:mm:ss aaa");
            out.write(dateFormat.format(new Date(timestamp)));
        } else {
            out.write("---");
        }


    } catch (Exception e) {
        out.write("error");
    }
%>