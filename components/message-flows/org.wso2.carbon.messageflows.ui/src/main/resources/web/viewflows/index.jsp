<!--
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
 -->
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.viewflows.stub.FlowsAdminServiceStub" %>
<%@ page import="org.wso2.carbon.viewflows.stub.types.AxisConfigData" %>
<%@ page import="org.wso2.carbon.viewflows.stub.types.PhaseOrderData" %>
<%@ page import="org.wso2.carbon.viewflows.stub.types.PhaseData" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.apache.axis2.client.ServiceClient" %>
<%@ page import="org.apache.axis2.client.Options" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<link href="extensions/core/css/handler_flow.css" rel="stylesheet" type="text/css" media="all"/>

<%
    String operationName = request.getParameter("opName");
    String serviceName = request.getParameter("serviceName");

    //Obtaining the client-side ConfigurationContext instance.
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
            .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    //Server URL which is defined in the server.xml
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(),
            session) + "FlowsAdminService";

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    AxisConfigData configData;
    try {
        FlowsAdminServiceStub stub = new FlowsAdminServiceStub(configContext, serverURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        if (serviceName != null && operationName != null) {
            configData = stub.getOperationAxisConfigData(serviceName, operationName);
        } else {
            configData = stub.getAxisConfigData();
        }
    } catch (Exception e) {
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
        %>
        <jsp:include page="../admin/error.jsp"/>
        <%
        return;
    }
%>

<fmt:bundle basename="org.wso2.carbon.viewflows.ui.i18n.Resources">
<carbon:breadcrumb label="message.flows"
		resourceBundle="org.wso2.carbon.viewflows.ui.i18n.Resources"
		topPage="true" request="<%=request%>" />

    <div id="middle">
        <h2><fmt:message key="message.flows"/> (<fmt:message key="graphical.view"/>)</h2>

        <div id="workArea">
            <p>
                <% if (serviceName != null && operationName != null) {%>
                    <a href="text_view.jsp?serviceName=<%=serviceName %>&opName=<%= operationName%>&retainlastbc=true"
                       class="icon-link" style="background-image:url(extensions/core/images/text-view.gif);">
                        <fmt:message key="show.text.view"/>
                    </a>
                <%
                    } else {
                %>
                    <a href="text_view.jsp?retainlastbc=true" class="icon-link"
                       style="background-image:url(extensions/core/images/text-view.gif);">
                        <fmt:message key="show.text.view"/>
                    </a>
                <% } %>
            </p>

            <p>&nbsp;</p><br/>

            <div>
                <%@ include file="in_flow.jsp" %>
                <br/>
                <%@ include file="out_flow.jsp" %>
                <br/>
                <%@ include file="in_fault_flow.jsp" %>
                <br/>
                <%@ include file="out_fault_flow.jsp" %>
                <br/>
            </div>
        </div>
    </div>

</fmt:bundle>