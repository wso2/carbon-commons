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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.tracer.ui.TracerAdminClient" %>
<%@ page import="org.wso2.carbon.tracer.stub.types.carbon.MessageInfo" %>
<%@ page import="org.wso2.carbon.tracer.stub.types.carbon.MessagePayload" %>
<%@ page import="org.wso2.carbon.tracer.stub.types.carbon.TracerServiceInfo" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>

<%
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    TracerAdminClient client;
    TracerServiceInfo tracerInfo;
    try {
        client = new TracerAdminClient(cookie, backendServerURL, configContext, request.getLocale());
        tracerInfo = client.getMessages(250, request.getParameter("filter"));
        MessagePayload messagePayload = tracerInfo.getLastMessage();
        MessageInfo[] messageIDs = tracerInfo.getMessageInfo();
        request.setAttribute("messageIDs", messageIDs);
        request.setAttribute("messagePayload", messagePayload);
    } catch (Exception e) {response.setStatus(500);
        response.getWriter().write(e.getMessage());
        return;
    }
%>
<jsp:include page="messages_include.jsp"/>