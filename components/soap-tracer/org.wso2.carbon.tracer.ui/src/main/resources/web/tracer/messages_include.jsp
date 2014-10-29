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
<%@ page import="org.wso2.carbon.tracer.stub.types.carbon.MessageInfo" %>
<%@ page import="org.wso2.carbon.tracer.stub.types.carbon.MessagePayload" %>
<%@ page import="org.wso2.carbon.tracer.stub.types.carbon.TracerServiceInfo" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ taglib uri="http://ajaxtags.org/tags/ajax" prefix="ajax" %>

<fmt:bundle basename="org.wso2.carbon.tracer.ui.i18n.Resources">

     <table class="styledLeft" width="100%">
        <thead>
        <tr>
            <th colspan="2"><fmt:message key="configuration"/></th>
        </tr>
        </thead>
        <tr class="tableOddRow">
            <td width="200px"><fmt:message key="filter"/></td>
            <td>
                <input type="text" id="filterText" size="50"/>
                <input type="button" value="<fmt:message key="search"/>" id="filterSearch"
                       class="button"/>
                <input type="button" value="<fmt:message key="clear"/>" id="filterClear"
                       class="button"/>

                <script type="text/javascript">
                    function clearTxt() {
                        document.getElementById('filterText').value = "";
                    }
                </script>

                <ajax:htmlContent baseUrl="filter_ajaxprocessor.jsp"
                                  source="filterSearch"
                                  target="messagesDiv"
                                  eventType="click"
                                  parameters="filter={filterText}"/>
                <ajax:htmlContent baseUrl="filter_ajaxprocessor.jsp"
                                  source="filterClear"
                                  target="messagesDiv"
                                  eventType="click"
                                  postFunction="clearTxt"
                                  parameters=""/>
            </td>
        </tr>
    </table>
    <p>&nbsp;</p>

    <%
        response.setHeader("Cache-Control", "no-cache");
        MessageInfo[] messageIDs = (MessageInfo[]) request.getAttribute("messageIDs");
        MessagePayload messagePayload = (MessagePayload) request.getAttribute("messagePayload");
    %>
    <table class="styledLeft" width="100%">
        <thead>
        <tr>
            <th colspan="2">
                <fmt:message key="messages"/>&nbsp;&nbsp;&nbsp;
            </th>
        </tr>
        </thead>
        <tr>
            <td colspan="2" style="padding:0 !important;">
                <select id="messageIDs" size="10" style="width:100%">
                    <%
                        if (messageIDs != null) {
                            int i = 0;
                            for (MessageInfo msgInfo : messageIDs) {
                                if (msgInfo != null) {
                                    SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
                                    String timestamp = formatter.format(msgInfo.getTimestamp().getTime());
                                    String msg = msgInfo.getServiceId() + "." +
                                                 msgInfo.getOperationName() +
                                                 " [" + msgInfo.getMessageSequence() + "] - " + timestamp;
                                    String id = msgInfo.getServiceId() + "," +
                                                msgInfo.getOperationName() + "," +
                                                msgInfo.getMessageSequence();
                                    if (i == 0) {
                    %>
                    <option selected="true" value="<%= id%>"><%= msg%>
                    </option>
                    <%
                    } else {
                    %>
                    <option value="<%= id%>"><%= msg%>
                    </option>
                    <%
                                    }
                                    i++;
                                }
                            }
                        }
                    %>
                </select>
                <script type="text/javascript">
                    function errorFunction() {
                        CARBON.showErrorDialog('<fmt:message key="cannot.get.messages"/>');
                    }
                </script>
                <ajax:htmlContent baseUrl="message_ajaxprocessor.jsp"
                                  source="messageIDs"
                                  target="messagesOutput"
                                  eventType="change"
                                  errorFunction="errorFunction"
                                  parameters="messageUUID={messageIDs}"/>
            </td>
        </tr>
        <tr>
            <td colspan="1" width="50%">
                <a href="index.jsp" class="icon-link"
                   style="background-image:url(images/refresh.gif);">
                    <fmt:message key="refresh"/>
                </a>
            </td>
            <td colspan="1" width="50%">
                <a href="clear_soap_messages.jsp" class="icon-link"
                   style="background-image:url(images/delete.gif);">
                    <fmt:message key="clear.all.messages"/>
                </a>
            </td>
        </tr>
    </table>
    <p>&nbsp;</p>
    <div id="messagesOutput">
        <%
            request.setAttribute("messagePayload", messagePayload);
        %>
        <jsp:include page="message_include.jsp"/>
    </div>
</fmt:bundle>