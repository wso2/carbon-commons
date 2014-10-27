<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.axiom.om.impl.builder.StAXOMBuilder" %>
<%@ page import="org.wso2.carbon.event.client.broker.BrokerClient" %>
<%@ page import="org.wso2.carbon.event.ui.UIUtils" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.io.ByteArrayInputStream" %>

<%

    BrokerClient brokerClient = UIUtils.getBrokerClient(config, session, request);
    String topic = request.getParameter("topic");
    String textMsg = request.getParameter("xmlMessage");
    session.setAttribute("errorTopic", topic);
    session.setAttribute("xmlMessage", textMsg);
    String status = null;
    OMElement message = null;
    try {
        StAXOMBuilder builder = new StAXOMBuilder(new ByteArrayInputStream(textMsg.getBytes()));
        message = builder.getDocumentElement();
    } catch (Exception e) {
        status = e.toString();
        CarbonUIMessage.sendCarbonUIMessage(status, CarbonUIMessage.WARNING, request);
    }
    try {
        brokerClient.publish(topic, message);
    } catch (Exception axisFault) {
        status = axisFault.toString();
        CarbonUIMessage.sendCarbonUIMessage(status, CarbonUIMessage.WARNING, request);
    }
    if (status == null) {
        status = "Successfully published the message :" + textMsg + "to the topic :" + topic;
        CarbonUIMessage.sendCarbonUIMessage(status, CarbonUIMessage.INFO, request);
    }
%>
<script type="text/javascript">
    location.href = "index.jsp";
</script>


