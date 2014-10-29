<%--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 --%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.tracer.ui.TracerAdminClient" %>
<%@ page import="org.wso2.carbon.tracer.stub.types.carbon.MessageInfo" %>
<%@ page import="org.wso2.carbon.tracer.stub.types.carbon.MessagePayload" %>
<%@ page import="org.wso2.carbon.tracer.stub.types.carbon.TracerServiceInfo" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ taglib uri="http://ajaxtags.org/tags/ajax" prefix="ajax" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://ajaxtags.org/tags/ajax" prefix="ajax" %>

<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="../ajax/js/scriptaculous/scriptaculous.js"></script>
<script type="text/javascript" src="../ajax/js/overlibmws/overlibmws.js"></script>
<script type="text/javascript" src="../ajax/js/overlibmws/overlibmws_crossframe.js"></script>
<script type="text/javascript" src="../ajax/js/overlibmws/overlibmws_iframe.js"></script>
<script type="text/javascript" src="../ajax/js/overlibmws/overlibmws_hide.js"></script>
<script type="text/javascript" src="../ajax/js/overlibmws/overlibmws_shadow.js"></script>
<script type="text/javascript" src="../ajax/js/ajax/ajaxtags.js"></script>
<script type="text/javascript" src="../ajax/js/ajax/ajaxtags_controls.js"></script>
<script type="text/javascript" src="../ajax/js/ajax/ajaxtags_parser.js"></script>

<c:set var="contextPath" scope="request">../ajax</c:set>
<div id="content"/>

<carbon:breadcrumb
        label="soap.message.tracer"
        resourceBundle="org.wso2.carbon.tracer.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>

<script type="text/javascript">
    function showHideMessagesDiv() {
        if (document.getElementById('messagesDiv').style.display == '') {
            document.getElementById('messagesDiv').style.display = 'none';
        } else {
            document.getElementById('messagesDiv').style.display = '';
        }
    }
</script>

<div id="middle">
<fmt:bundle basename="org.wso2.carbon.tracer.ui.i18n.Resources">
<h2><fmt:message key="soap.message.tracer"/></h2>
</fmt:bundle>

<div id="workArea">
<%
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    TracerAdminClient client;
    TracerServiceInfo tracerInfo;
    boolean isTracerOn;
    try {
        client = new TracerAdminClient(cookie, backendServerURL, configContext, request.getLocale());
        tracerInfo = client.getMessages(250, null);
        MessagePayload messagePayload = tracerInfo.getLastMessage();
        MessageInfo[] messageIDs = tracerInfo.getMessageInfo();
        isTracerOn = tracerInfo.getFlag().equalsIgnoreCase("ON");
        request.setAttribute("messageIDs", messageIDs);
        request.setAttribute("messagePayload", messagePayload);
    } catch (Exception e) {
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
        <jsp:include page="../admin/error.jsp"/>
<%
        return;
    }
%>

<fmt:bundle basename="org.wso2.carbon.tracer.ui.i18n.Resources">

    <table>
        <tr>
            <td width="50px"><nobr><fmt:message key="enable.soap.tracing.prompt"/></nobr></td>
            <td>
                <select id="monitorSetting">
                    <%
                        if (isTracerOn) {
                    %>
                    <option value="ON" selected="true"><fmt:message key="yes"/></option>
                    <option value="OFF"><fmt:message key="no"/></option>
                    <%
                    } else {
                    %>
                    <option value="ON"><fmt:message key="yes"/></option>
                    <option value="OFF" selected="true"><fmt:message key="no"/></option>
                    <%
                        }
                    %>
                </select>
                &nbsp;&nbsp;&nbsp;<font color="red"><fmt:message key="warning"/></font>
                <fmt:message key="tracing.warning.msg"/>
                <script type="text/javascript">
                    function changeMonitoringPostFunction() {
                        showHideMessagesDiv();
                    }

                    function changeMonitoringErrorFunction() {
                        CARBON.showErrorDialog('<fmt:message key="change.monitoring.error"/>');
                    }

                    function changeMonitoringPreFunction() {
                    }
                </script>
                <div id="monitorOutput"/>
                <ajax:select baseUrl="monitor_ajaxprocessor.jsp"
                             source="monitorSetting"
                             target="monitorOutput"
                             parameters="flag={monitorSetting}"
                             preFunction="changeMonitoringPreFunction"
                             errorFunction="changeMonitoringErrorFunction"
                             postFunction="changeMonitoringPostFunction"/>
            </td>
        </tr>
    </table>
    <p>&nbsp;</p>

    <div id="messagesDiv"
         style="<%= isTracerOn? "display:''" : "display:none;" %>">
        <jsp:include page="messages_include.jsp"/>
    </div>
    </div>
    </div>
</fmt:bundle>
