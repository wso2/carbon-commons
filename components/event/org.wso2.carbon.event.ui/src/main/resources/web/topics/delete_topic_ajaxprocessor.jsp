<%@ page import="org.apache.axis2.client.Options" %>
<%@ page import="org.apache.axis2.client.ServiceClient" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.event.client.broker.BrokerClient" %>
<%@ page import="org.wso2.carbon.event.stub.internal.TopicManagerAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.ui.UIUtils" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="java.rmi.RemoteException" %>
<%@ page import="org.wso2.carbon.event.stub.internal.xsd.Subscription" %>
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

    String message = "";
    String topic = request.getParameter("topic");
    session.removeAttribute("topicWsSubscriptions");

    try {
        Subscription[] wsSubscriptionsForTopic = stub.getWsSubscriptionsForTopic(topic);
        Subscription[] jmsSubscriptionsForTopic = stub.getJMSSubscriptionsForTopic(topic);
        if ((wsSubscriptionsForTopic != null && wsSubscriptionsForTopic.length > 0) ||
                (jmsSubscriptionsForTopic != null && jmsSubscriptionsForTopic.length > 0)) {
            message = "Error, Subscriptions exists for this topic, Please remove all subscriptions before deleting the topic";
        } else {
            stub.removeTopic(topic);
            message = " Topic removed successfully";
        }
    } catch (RemoteException e) {
        message = "Error in deleting topic  " + e;
%> <%=message%>
<%
    }
%><%=message%>
