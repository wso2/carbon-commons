<%@ page import="org.wso2.carbon.event.ui.UIConstants" %>
<%@ page import="org.owasp.encoder.Encode" %>


<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<script src="../yui/build/yahoo/yahoo-min.js" type="text/javascript"></script>
<script src="../yui/build/utilities/utilities.js" type="text/javascript"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<!--Yahoo includes for dom event handling-->
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>

<!--Yahoo includes for animations-->
<script src="../yui/build/animation/animation-min.js" type="text/javascript"></script>

<!--Yahoo includes for menus-->
<link rel="stylesheet" type="text/css" href="../yui/build/menu/assets/skins/sam/menu.css"/>
<script type="text/javascript" src="../yui/build/container/container_core-min.js"></script>
<script type="text/javascript" src="../yui/build/menu/menu-min.js"></script>

<!--EditArea javascript syntax hylighter -->
<script language="javascript" type="text/javascript" src="../editarea/edit_area_full.js"></script>

<!--Local js includes-->
<script type="text/javascript" src="js/treecontrol.js"></script>
<script type="text/javascript" src="js/topics.js"></script>
<script type="text/javascript" src="js/eventing_utils.js"></script>

<link href="css/tree-styles.css" media="all" rel="stylesheet"/>
<link href="css/dsxmleditor.css" media="all" rel="stylesheet"/>


<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="eventing.js"></script>
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>
<script src="../admin/js/widgets.js" type="text/javascript"></script>
<script type="text/javascript">
    enableDefaultText("hhid");
    enableDefaultText("mmid");
    enableDefaultText("ssid");
</script>
<%-- YUI Calendar includes--%>
<link rel="stylesheet" type="text/css" href="../yui/build/fonts/fonts-min.css"/>
<link rel="stylesheet" type="text/css" href="../yui/build/calendar/assets/skins/sam/calendar.css"/>
<script type="text/javascript" src="../yui/build/calendar/calendar-min.js"></script>

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
<%
    String topic = request.getParameter("topic");
    if (topic == null) {
%>
<script type="text/javascript">
    location.href = 'topics.jsp';</script>
<%
        return;
    }
%>
<fmt:bundle basename="org.wso2.carbon.event.ui.i18n.Resources">
    <jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
    <carbon:breadcrumb
            label="add.subscription"
            resourceBundle="org.wso2.carbon.event.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
    <div id="middle">
        <div id="workArea">
            <h2><fmt:message key="subscribe"/></h2>

            <table id="userAdd" class="styledLeft"
                   style="width:100%; margin-bottom:10px; margin-top:10px; ">
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
                                <td colspan="3"><input id="topicId" type="text" name="topic"
                                                       value="<%=Encode.forHtmlAttribute(topic)%>" readonly="true"/></td>
                            </tr>
                            <tr>
                                <td class="leftCol-med"><fmt:message key="subscription.mode"/>
                                    :<span class="required">*</span></td>
                                <td>
                                    <select id="subscriptionModes">
                                        <%
                                            if (topic.equals("/")) {
                                        %>
                                        <option value="<%=UIConstants.SUBSCRIPTION_MODE_3%>"><%=UIConstants.SUBSCRIPTION_MODE_3_DESCRIPTION%>
                                        </option>
                                        <option value="<%=UIConstants.SUBSCRIPTION_MODE_4%>"><%=UIConstants.SUBSCRIPTION_MODE_4_DESCRIPTION%>
                                        </option>

                                        <%
                                        } else {
                                        %>
                                        <option value="<%=UIConstants.SUBSCRIPTION_MODE_0%>"><%=UIConstants.SUBSCRIPTION_MODE_0_DESCRIPTION%>
                                        </option>
                                        <option value="<%=UIConstants.SUBSCRIPTION_MODE_1%>"><%=UIConstants.SUBSCRIPTION_MODE_1_DESCRIPTION%>
                                        </option>
                                        <option value="<%=UIConstants.SUBSCRIPTION_MODE_2%>"><%=UIConstants.SUBSCRIPTION_MODE_2_DESCRIPTION%>
                                        </option>
                                        <%
                                            } %>
                                    </select>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-med">Event Sink URL<span
                                        class="required">*</span></td>
                                <td colspan="3"><input type="text" id="subURL" style="width:500px"
                                                       name="eventSinkURL"
                                                       title="Endpoint reference to where matching messages are sent eg. http://yourhost:7777/services/MessageCollector"/>
                                </td>
                            </tr>
                            <tr>
                                <td>Expiration Time</td>
                                <td style="width:170px;vertical-align:top !important">
                                    <div style="height:30px;">Date:</div>
                                    <input type="text" id="expirationTime" name="expirationTime"
                                           onclick="clearTextIn(this)" onblur="fillTextIn(this)"
                                           value=""
                                           title="Active Time Period for this Subscription, if -1 never expires"/>
                                    <a style="cursor:pointer" onclick="showCalendar()"><img
                                            src="../admin/images/calendar.gif" border="0"
                                            align="top"/> </a>
                                    eg:(2012/5/20)
                                    <div class="yui-skin-sam">
                                        <div id="cal1Container" style="display:none;"></div>
                                    </div>
                                </td>
                                <td style="width:170px;vertical-align:top !important">
                                    <div style="height:30px;"
                                            >Time:
                                    </div>
                                    <input type="text" id="hhid" alt="hh"
                                           style="width:40px;float:left;margin-right:7px;"
                                           value=""
                                           title="Set expiration time in 24-hour time format"/>
                                    <input type="text" id="mmid" alt="mm"
                                           style="width:40px;float:left;margin-right:7px;"
                                           value=""/>
                                    <input type="text" id="ssid" alt="ss"
                                           style="width:40px;float:left;margin-right:7px" value=""/>
                                    eg:(15/30/00)
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
                        <input type="submit" value="Subscribe" class="button"
                               onClick="performSubscribe()"/>
                        <input type="button" value="Cancel" class="button"
                               onClick="location.href='topics.jsp';"/>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</fmt:bundle>