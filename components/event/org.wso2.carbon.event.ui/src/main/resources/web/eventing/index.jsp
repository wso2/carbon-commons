<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page import="org.wso2.carbon.event.client.broker.BrokerClient" %>
<%@ page import="org.wso2.carbon.event.client.stub.generated.GetSubscriptionsResponse" %>
<%@ page import="org.wso2.carbon.event.client.stub.generated.SubscriptionDetails" %>
<%@ page import="org.wso2.carbon.event.ui.UIUtils" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.HashMap" %>

<fmt:bundle basename="org.wso2.carbon.eventing.ui.i18n.Resources">

    <link type="text/css" href="../eventing/css/subscriptions.css" rel="stylesheet"/>
    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>
    <script type="text/javascript" src="eventing.js"></script>

    <%
        String message = request.getParameter("message");
        if (message != null) {
    %><h3><%=message%>
</h3><br/><br/><%
    }

    int pageNumberInt = 0;
    String pageNumberAsStr = request.getParameter("pageNumber");
    if (pageNumberAsStr != null) {
        pageNumberInt = Integer.parseInt(pageNumberAsStr);
    }

    String parameters = "serviceTypeFilter=" + "&serviceGroupSearchString=";
    BrokerClient brokerClient = UIUtils.getBrokerClient(config, session, request);

    String subscriptionId = request.getParameter("subscriptionId");
    String isRenew = request.getParameter("isRenew");

    if (subscriptionId != null && isRenew != null && isRenew.trim().equals("true")) {
        String renewDuration = request.getParameter("renewDuration");
        try {
            int hours = Integer.parseInt(renewDuration);
            if (hours > 0) {
                long expireTime = System.currentTimeMillis() + Integer.parseInt(renewDuration) * 60 * 60 * 1000;
                brokerClient.renewSubscription(subscriptionId, expireTime);
%>
    <script type="text/javascript">CARBON.showInfoDialog('Renewed subscription <%=subscriptionId %> till <%=new Date(expireTime).toString()%>');</script>
    <%
    } else {
    %>
    <script type="text/javascript">CARBON.showErrorDialog('Error in entered renew duration: <%=renewDuration%> ; Please enter a positive integer value for renew duration ');</script>
    <%
        }

    } catch (NumberFormatException e) {
    %>
    <script type="text/javascript">CARBON.showErrorDialog('Error in entered renew duration: <%=renewDuration%> ; Please enter an integer value for renew duration ');</script>
    <%
        }

    } else if (subscriptionId != null) {
        brokerClient.unsubscribe(subscriptionId);
    %>
    <script type="text/javascript">CARBON.showInfoDialog('Unsubscribed from <%=subscriptionId %> successfully', function() {
        location.href = 'index.jsp';
    });
    </script>
    <%
    %><b></b><%
    }

    GetSubscriptionsResponse allSubscriptions = brokerClient.getAllSubscriptions(10, null, pageNumberInt * 10);
    SubscriptionDetails[] details = allSubscriptions.getSubscriptionDetail();
    if (details != null && details.length > 0) {
        HashMap<String,SubscriptionDetails> subscriptionDetailsMap = new HashMap<String, SubscriptionDetails>();
        for(SubscriptionDetails subscriptionDetail : details){
            subscriptionDetailsMap.put(subscriptionDetail.getSubscriptionId(),subscriptionDetail);
        }
        session.removeAttribute("subscriptionDetailsMap");
        session.setAttribute("subscriptionDetailsMap",subscriptionDetailsMap);
    }

    int pageCount = (int) Math.ceil(((float) allSubscriptions.getAllRequestCount()) / 10);
    if (pageCount <= 0) {
        //this is to make sure it works with defualt values
        pageCount = 1;
    }
%>
    <div id="middle">
        <div id="workArea">
            <h3>Available Subscriptions</h3>
            <carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=pageCount%>"
                              page="index.jsp" pageNumberParameterName="pageNumber"
                              resourceBundle="org.wso2.carbon.event.ui.i18n.Resources"
                              prevKey="prev" nextKey="next"
                              parameters="<%=parameters%>"/>

            <table class="styledLeft" <%if (details == null ) {%>style="display:none"<%}%> id="subscriptionsTable">
                <thead>
                <tr>
                    <th>Event Sink</th>
                    <th>Topic</th>
                    <th>Expiration Time</th>
                    <th width="30%"></th>
                </tr>
                </thead>
                <tbody>
                <%


                    if (details != null) {
                        for (SubscriptionDetails detail : details) {
                            String subId = detail.getSubscriptionId().replace("urn:uuid:", "");
                %>
                <tr>
                    <td><%=detail.getEventSinkAddress()%>
                    </td>
                    <td><%=detail.getTopic()%>
                    </td>
                    <td>
                        <%=(detail.getSubscriptionEndingTime() != null ? new Date(detail.getSubscriptionEndingTime().getTimeInMillis()) : "")%>
                    </td>
                    <td>
                        <form name="input" id="<%=subId%>" action="" method="get">
                            <input type="HIDDEN" name="isRenew" value="false"/>
                            <input type="HIDDEN" name="subscriptionId"
                                   value="<%=detail.getSubscriptionId()%>"/>
                            <input type="HIDDEN" name="renewDuration" value="12">
                            <a style="background-image: url(../admin/images/delete.gif);"
                               class="icon-link"
                               onclick="doUnsubscribe('<%=subId%>')">Unsubscribe</a>
                            <%--<a style="background-image: url(images/refresh.gif);" class="icon-link"
                               onClick="show_prompt(document.getElementById('<%=subId%>'))">Renew</a>--%>
                            <a style="background-image: url(images/refresh.gif);" class="icon-link" href="renewSubscriptions.jsp?isRenew=true&subId=<%=detail.getSubscriptionId()%>">Renew</a>
                        </form>
                    </td>
                </tr>
                <%
                        }
                    }
                %>
                </tbody>
            </table>
            <div id="noSubscriptionsDiv" class="noDataDiv"
                 <%if (details != null) {%>style="display:none"<%}%>>
                No Subscriptions Defined
            </div>
        </div>
    </div>



    <script type="text/javascript">
        alternateTableRows('expiredsubscriptions', 'tableEvenRow', 'tableOddRow');
        alternateTableRows('validsubscriptions', 'tableEvenRow', 'tableOddRow');
    </script>

</fmt:bundle>