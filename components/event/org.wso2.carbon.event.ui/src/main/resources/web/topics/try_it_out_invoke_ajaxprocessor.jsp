<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.axiom.om.impl.builder.StAXOMBuilder" %>
<%@ page import="org.wso2.carbon.event.client.broker.BrokerClient" %>
<%@ page import="org.wso2.carbon.event.ui.UIUtils" %>
<%@ page import="java.io.ByteArrayInputStream" %>

<%

    BrokerClient brokerClient = UIUtils.getBrokerClient(config, session, request);
    String topic = request.getParameter("topic");
    String textMsg = request.getParameter("xmlMessage");
    session.setAttribute("errorTopic", topic);
    session.setAttribute("xmlMessage", textMsg);
    OMElement message;
    String messageToBePrinted = null;
    StAXOMBuilder builder = null;
    try {
        builder = new StAXOMBuilder(new ByteArrayInputStream(textMsg.getBytes()));
        message = builder.getDocumentElement();
        if (message != null) {
            brokerClient.publish(topic, message);
        } else {
            messageToBePrinted = "Error: Failed to get document element from message " + textMsg;
        }
    } catch (Exception e) {
        messageToBePrinted = "Error: while publishing the message " + e.getMessage();
    }

    if (messageToBePrinted == null) {
        messageToBePrinted = "Successfully published the message to the topic :" + topic;
    }
%>
<%=messageToBePrinted%>



