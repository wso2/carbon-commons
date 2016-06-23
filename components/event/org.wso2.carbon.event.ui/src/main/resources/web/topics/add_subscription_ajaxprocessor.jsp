<%@ page import="org.apache.axis2.client.Options" %>
<%@ page import="org.apache.axis2.client.ServiceClient" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.event.client.broker.BrokerClient" %>
<%@ page import="org.wso2.carbon.event.client.broker.BrokerClientException" %>
<%@ page import="org.wso2.carbon.event.stub.internal.TopicManagerAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.ui.UIConstants" %>
<%@ page import="org.wso2.carbon.event.ui.UIUtils" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="java.text.ParseException" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
    //ignore methods other than post
    if (!request.getMethod().equalsIgnoreCase("POST")) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }
    String topic = request.getParameter("topic");
    if (topic != null) {
        String eventSink = request.getParameter("subURL");
        String expirationTime = request.getParameter("expirationTime");

        String hours = request.getParameter("hours");
        if ((hours == null) || (hours.equals("hh"))) {
            hours = "00";
        }
        String minites = request.getParameter("minutes");
        if ((minites == null) || (minites.equals("mm"))) {
            minites = "00";
        }
        String seconds = request.getParameter("seconds");
        if ((seconds == null) || (seconds.equals("ss"))) {
            seconds = "00";
        }
        String subscriptionMode = request.getParameter("subMode");
        if (subscriptionMode != null) {
            if (subscriptionMode.equals(UIConstants.SUBSCRIPTION_MODE_1)) {
                if (topic.endsWith("/")) {
                    topic = topic + "*";
                } else {
                    topic = topic + "/*";
                }
            } else if (subscriptionMode.equals(UIConstants.SUBSCRIPTION_MODE_2)) {
                if (topic.endsWith("/")) {
                    topic = topic + "#";
                } else {
                    topic = topic + "/#";
                }
            } else if (subscriptionMode.equals(UIConstants.SUBSCRIPTION_MODE_3)) {
                topic = "/*";
            } else if (subscriptionMode.equals(UIConstants.SUBSCRIPTION_MODE_4)) {
                topic = "/#";
            }
        }

        BrokerClient brokerClient = UIUtils.getBrokerClient(config, session, request);
        String message = "";
        if ((eventSink != null) && (eventSink != "")) {
            topic = topic.trim();

            String expirationDateTime = "";
            try {
                long time = -1;
                if (expirationTime != null && expirationTime.trim().length() > 0) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss");
                    expirationDateTime = expirationTime.trim() + "/" + hours + "/" + minites + "/" + seconds;
                    time = dateFormat.parse(expirationDateTime).getTime();

                }

                eventSink = eventSink.trim();

                brokerClient.subscribe(topic, eventSink, time, null);
                message = "Subscribed to " + eventSink + " using " + topic + " Successfully";
            } catch (BrokerClientException e) {
                message = e.getErrorMessage();
            } catch (ParseException e) {
                message = "Error: Expiration date/time(" + expirationDateTime + ") is invalid.";
            }

%>
<%=message%>
<%
} else {
    message = "Error: Topic and Event Sink Must not be null";

%>
<%=message%>
<%
        }


    }

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

    session.removeAttribute("topicWsSubscriptions");
    session.removeAttribute("topicJMSSubscriptions");


    try {
        session.setAttribute("topicWsSubscriptions", stub.getWsSubscriptionsForTopic(topic));
        session.setAttribute("topicJMSSubscriptions", stub.getJMSSubscriptionsForTopic(topic));
    } catch (Exception e) {
        String message = e.getMessage();
%><%=message%><%
    }
%>