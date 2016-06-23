<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page import="org.wso2.carbon.event.client.broker.BrokerClient" %>
<%@ page import="org.wso2.carbon.event.ui.UIUtils" %>
<%@ page import="java.text.SimpleDateFormat" %>


<%
    //ignore methods other than post
    if (!request.getMethod().equalsIgnoreCase("POST")) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }
    String isRenew = request.getParameter("isRenew");
    String subscriptionId = request.getParameter("subId");
    String eventSink = "";
    String topic = "";

    if (isRenew == null) {

        String hours = request.getParameter("hours");
        if ((hours == null) || (hours.equals("HH"))) {
            hours = "00";
        }
        String minites = request.getParameter("minites");
        if ((minites == null) || (minites.equals("mm"))) {
            minites = "00";
        }
        String seconds = request.getParameter("seconds");
        if ((seconds == null) || (seconds.equals("ss"))) {
            seconds = "00";
        }
        String expirationTime = request.getParameter("expirationTime");

        BrokerClient brokerClient = UIUtils.getBrokerClient(config, session, request);
        String message;
        if (topic != null && eventSink != null) {

            long time = -1;
            if (expirationTime != null && expirationTime.trim().length() > 0) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss");
                time = dateFormat.parse(expirationTime.trim() + "/" + hours + "/" + minites + "/" + seconds).getTime();
            }
            try {
                brokerClient.renewSubscription(subscriptionId, time);

                message = "Subscription renewed successfully";
%>
<script type="text/javascript">
    location.href = "../topics/topics.jsp?status='<%=message%>'"
</script>
<%
} catch (Exception e) {
    message = "Error while renewing the subscription ";
%>
<script type="text/javascript">
    location.href = "../topics/topics.jsp?status='<%=message%>'"
</script>
<%
            }
        }

    }
%>