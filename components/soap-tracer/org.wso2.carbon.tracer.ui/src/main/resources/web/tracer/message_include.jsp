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
<%@ page import="org.wso2.carbon.tracer.stub.types.carbon.MessagePayload" %>
<%@ page import="java.util.ResourceBundle" %>
<!--Yahoo includes for dom event handling-->
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>

<!--EditArea javascript syntax hylighter -->
<script language="javascript" type="text/javascript" src="../editarea/edit_area_full.js"></script>

<script type="text/javascript">
    YAHOO.util.Event.onDOMReady(function() {
        editAreaLoader.init({
            id : "requestBlock"        // textarea id
            ,syntax: "xml"            // syntax to be uses for highgliting
            ,start_highlight: true        // to display with highlight mode on start-up
            ,allow_resize: "both"
            ,min_height:250
            ,font_size:8
        });
        editAreaLoader.init({
            id : "reponseBlock"       
            ,syntax: "xml"
            ,start_highlight: true
            ,allow_resize: "both"
            ,min_height:250
            ,font_size:8
        });
    });
</script>

<fmt:bundle basename="org.wso2.carbon.tracer.ui.i18n.Resources">
    <%
        response.setHeader("Cache-Control", "no-cache");
        MessagePayload messagePayload = (MessagePayload) request.getAttribute("messagePayload");
    %>

    <table class="styledLeft">
        <thead>
            <tr>
                <th width="50%"><fmt:message key="request"/></th>
                <th width="50%"><fmt:message key="response"/></th>
            </tr>
        </thead>
        <tr>
            <td width="50%" style="vertical-align:top !important; padding-top:0 !important;">
                <%
                    ResourceBundle resourceBundle =
                            ResourceBundle.getBundle("org.wso2.carbon.tracer.ui.i18n.Resources",
                                                     request.getLocale());
                    String requestMsg =
                            messagePayload != null && messagePayload.getRequest() != null ?
                            messagePayload.getRequest() : resourceBundle.getString("no.requests.received");
                %>
                    <textarea id="requestBlock" style="border: 0px solid rgb(204, 204, 204); width: 99%;
                    height: 275px; margin-top: 5px;" name="requestBlock" rows="50"><%= requestMsg %></textarea>
            </td>
            <td width="50%" style="vertical-align:top !important;  padding-top:0 !important;">
                <%
                    String responseMsg =
                            messagePayload != null && messagePayload.getResponse() != null ?
                            messagePayload.getResponse() : resourceBundle.getString("no.responses.sent");
                %>
                    <textarea id="reponseBlock" style="border: 0px solid rgb(204, 204, 204); width: 99%;
                     height: 275px; margin-top: 5px;" name="reponseBlock" rows="50"><%= responseMsg %></textarea>
            </td>
        </tr>
    </table>

</fmt:bundle>