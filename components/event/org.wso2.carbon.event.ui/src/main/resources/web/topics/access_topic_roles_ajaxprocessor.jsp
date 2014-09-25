<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="org.wso2.carbon.event.ui.UIUtils" %>
<%@ page import="org.wso2.carbon.event.client.broker.BrokerClient" %>
<%@ page import="org.apache.axis2.client.Options" %>
<%@ page import="org.apache.axis2.client.ServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.event.stub.internal.TopicManagerAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.client.broker.BrokerClientException" %>
<%
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
            .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
//Server URL which is defined in the server.xml
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(),
            session) + "TopicManagerAdminService.TopicManagerAdminServiceHttpsSoap12Endpoint";
    TopicManagerAdminServiceStub stub = new TopicManagerAdminServiceStub(configContext, serverURL);

    String cookie = (String) session.getAttribute(org.wso2.carbon.utils.ServerConstants.ADMIN_SERVICE_COOKIE);

    ServiceClient client = stub._getServiceClient();
    Options option = client.getOptions();
    option.setManageSession(true);
    option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

    session.removeAttribute("userRoles");

    try {
        session.setAttribute("userRoles", stub.getUserRoles());
    } catch (Exception e) {
        String message = e.getMessage();
%><%=message%><%
    }
%>