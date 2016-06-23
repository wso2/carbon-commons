<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<%@page import="org.wso2.carbon.event.client.broker.BrokerClient" %>

<%@page import="org.wso2.carbon.event.client.stub.generated.SubscriptionDetails" %>

<%@page import="org.wso2.carbon.event.ui.UIUtils" %>
<%@page import="org.apache.axiom.om.OMAbstractFactory" %>
<%@page import="org.apache.axiom.om.OMFactory" %>
<%@page import="javax.xml.namespace.QName" %>
<%@page import="org.apache.axiom.om.OMElement" %>


<%@page import="java.util.Date" %>
<%@page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="org.wso2.carbon.event.stub.internal.xsd.Subscription" %>
<fmt:bundle basename="org.wso2.carbon.eventing.ui.i18n.Resources">


<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="eventing.js"></script>
<script type="text/javascript" src="js/subscriptions.js"></script>
<script type="text/javascript" src="js/eventing_utils.js"></script>


<!--Local js includes-->
<script type="text/javascript" src="js/treecontrol.js"></script>
<!--EditArea javascript syntax hylighter -->
<script language="javascript" type="text/javascript" src="../editarea/edit_area_full.js"></script>
<!--Yahoo includes for dom event handling-->
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>
<script type="text/javascript">
    YAHOO.util.Event.onAvailable("JScript", function() {
        editAreaLoader.init({
            id : "JScript"        // textarea id
            ,syntax: "js"            // syntax to be uses for highgliting
            ,start_highlight: true        // to display with highlight mode on start-up
            ,allow_resize: "both"
            ,min_height:250
        });
    });
    function handleFocus(obj, txt) {
        if (obj.value == txt) {
            obj.value = '';
            YAHOO.util.Dom.removeClass(obj, 'defaultText');

        }
    }
    function handleBlur(obj, txt) {
        if (obj.value == '') {
            obj.value = txt;
            YAHOO.util.Dom.addClass(obj, 'defaultText');
        }
    }
    YAHOO.util.Event.onDOMReady(
            function() {
                document.getElementById("hhid").value = "HH";
                document.getElementById("mmid").value = "mm";
                document.getElementById("ssid").value = "ss";
            }
            )


</script>

<%-- YUI Calendar includes--%>
<link rel="stylesheet" type="text/css" href="../yui/build/fonts/fonts-min.css"/>
<link rel="stylesheet" type="text/css" href="../yui/build/calendar/assets/skins/sam/calendar.css"/>
<script type="text/javascript" src="../yui/build/calendar/calendar-min.js"></script>

 <script type="text/javascript">
     function validateInputs(){
       var hours = document.getElementById("hhid");
       var minutes = document.getElementById("mmid");
       var seconds = document.getElementById("ssid");
       if(hours.value != "HH"){
           if (isNaN(hours.value)) {
                CARBON.showErrorDialog("Invalid Hour specified");
                return;
            }else if(hours.value >23 ){
               CARBON.showErrorDialog("Invalid Hour specified");
                return;
           }
       }
         if(minutes.value != "mm"){
           if (isNaN(minutes.value)) {
                CARBON.showErrorDialog("Invalid Minutes specified");
                return;
            }else if(minutes.value > 59){
               CARBON.showErrorDialog("Invalid Minutes specified");
                return;
           }
       }
          if(seconds.value != "ss"){
           if (isNaN(seconds.value)) {
                CARBON.showErrorDialog("Invalid Seconds");
                return;
            } else if(seconds.value > 59){
                CARBON.showErrorDialog("Invalid Seconds");
                return;
           }
       }
        performRenew(document.getElementById('renewSub'));

     }

     function performRenew(form) {
         form.submit();
     }

 </script>

<style type="text/css">

    #cal1Container {
        display: none;
        position: absolute;
        font-size: 12px;
        z-index: 1
    }

    .defaultText {
        color: #666666;
        font-style: italic;
    }
</style>

<div id="middle">
    <div id="workArea">
        <h3>Renew Subscription</h3>

        <%
            String isRenew = request.getParameter("isRenew");
            String subscriptionId = request.getParameter("subId");
            String originalExpirationTime = "";
            String eventSink = "";
            String topic = "";

            Subscription[] topicWsSubscriptions = (Subscription[]) session.getAttribute("topicWsSubscriptions");
            Subscription matchingSubscription = null;
            for(Subscription subscription : topicWsSubscriptions){
                  if(subscription.getId().equals(subscriptionId)){
                      matchingSubscription = subscription;
                      break;
                  }
            }
//            HashMap<String, SubscriptionDetails> subscriptionDetailsHashMap = (HashMap<String, SubscriptionDetails>) session.getAttribute("subscriptionDetailsMap");

//            SubscriptionDetails detail = subscriptionDetailsHashMap.get(subscriptionId);
            if (matchingSubscription != null) {
                eventSink = matchingSubscription.getEventSinkURL();
                topic = matchingSubscription.getTopicName();
                if (matchingSubscription.getExpires() != null && matchingSubscription.getExpires().getTime() != null) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
                    originalExpirationTime = format.format(matchingSubscription.getExpires().getTime());
                }
            }


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
                String message = "";
                if (topic != null && eventSink != null) {
                    topic = topic.trim();


                    long time = -1;
                    if (expirationTime != null && expirationTime.trim().length() > 0) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss");
                        time = dateFormat.parse(expirationTime.trim() + "/" + hours + "/" + minites + "/" + seconds).getTime();
                    }

                    eventSink = eventSink.trim();
                }

            }
        %>

        <form name="input" action="renew_subscription_ajaxprocessor.jsp" method="post" id="renewSub">
            <table style="width:100%" id="userAdd" class="styledLeft">
                <thead>
                <tr>
                    <th>Enter Subscription Details</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td class="formRaw">
                        <table class="normal-nopadding" style="width:100%">
                            <tbody>
                            <tr>
                                <td class="leftCol-med">Topic<span class="required">*</span></td>
                                <td colspan="3"><input type="text" name="topic" class="initE" readonly="true"
                                                       value="<%=topic%>"/></td>
                            </tr>
                            <tr>
                                <td class="leftCol-med">Event Sink URL<span class="required">*</span></td>
                                <td colspan="3"><input type="text" style="width:500px" name="eventSinkURL"
                                                       class="initE" readonly="true" value="<%=eventSink%>"/></td>
                            </tr>
                            <tr>
                                <td>Expiration Time</td>
                                <td style="width:170px;">
                                    Date:<br/>
                                    <input type="text" id="expirationTime" name="expirationTime" class="initE"
                                           onclick="clearTextIn(this)" onblur="fillTextIn(this)"
                                           value="<%=originalExpirationTime%>"
                                           title="Active Time Period for this Subscription, if -1 never expires"/>
                                    <a style="cursor:pointer" onclick="showCalendar()"><img
                                            src="../admin/images/calendar.gif" border="0" align="top"/> </a>

                                    <div class="yui-skin-sam">
                                        <div id="cal1Container" style="display:none;"></div>
                                    </div>
                                </td>
                                <td style="width:150px">
                                    Time:<br/>
                                    <input type="text" id="hhid" name="hours" onFocus="handleFocus(this,'HH')"
                                           onBlur="handleBlur(this,'HH');" class="defaultText" style="width:30px;"/>
                                    <input type="text" id="mmid" name="minites" onFocus="handleFocus(this,'mm')"
                                           onBlur="handleBlur(this,'mm');" class="defaultText" style="width:30px;"/>
                                    <input type="text" id="ssid" name="seconds" onFocus="handleFocus(this,'ss')"
                                           onBlur="handleBlur(this,'ss');" class="defaultText" style="width:30px;"/>
                                </td>
                                <td>

                                </td>
                            </tr>

                            </tbody>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td class="buttonRow">
                        <input type="HIDDEN" name="subId"
                               value="<%=subscriptionId%>"/>
                        <input type="button" value="Renew"
                               onClick="validateInputs()"/>
                    </td>
                </tr>
                </tbody>
            </table>
        </form>
    </div>
</div>
</fmt:bundle>