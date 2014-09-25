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
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.viewflows.stub.types.AxisConfigData" %>
<%@ page import="org.wso2.carbon.viewflows.stub.FlowsAdminServiceStub" %>
<%@ page import="org.wso2.carbon.viewflows.stub.types.PhaseOrderData" %>
<%@ page import="org.wso2.carbon.viewflows.stub.types.PhaseData" %>
<%@ page import="org.wso2.carbon.viewflows.stub.types.HandlerData" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.apache.axis2.client.ServiceClient" %>
<%@ page import="org.apache.axis2.client.Options" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>


<link href="extensions/core/css/handler_flow.css" rel="stylesheet" type="text/css" media="all"/>

<%
    String flow = request.getParameter("flow").trim();
    String phase = request.getParameter("phase").trim();
    String flowHeader = "";

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

        configData = stub.getAxisConfigData();
    } catch (Exception e) {
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
        %>
        <jsp:include page="../admin/error.jsp"/>
        <%
        return;
    }
    PhaseOrderData phaseData = null;
    if (flow.equalsIgnoreCase("in")) {
        phaseData = configData.getInflowPhaseOrder();
        flowHeader = "In";
    } else if (flow.equalsIgnoreCase("out")) {
        phaseData = configData.getOutflowPhaseOrder();
        flowHeader = "Out";
    } else if (flow.equalsIgnoreCase("inFault")) {
        phaseData = configData.getInfaultflowPhaseOrder();
        flowHeader = "In Fault";
    } else if (flow.equalsIgnoreCase("outFault")) {
        phaseData = configData.getOutfaultPhaseOrder();
        flowHeader = "Out Fault";
    }

    PhaseData currentPhase = null;
    if (phaseData != null) {
        for (PhaseData aPhase : phaseData.getPhases()) {
            if (aPhase.getName().equalsIgnoreCase(phase)) {
                currentPhase = aPhase;
                break;
            }
        }
    }

    String flowImgName = "outflow";
    if (flow.indexOf("out") == -1) {
        flowImgName = "inflow";
    }
%>

<fmt:bundle basename="org.wso2.carbon.viewflows.ui.i18n.Resources">
<carbon:breadcrumb label="phase.handlers"
		resourceBundle="org.wso2.carbon.viewflows.ui.i18n.Resources"
		topPage="false" request="<%=request%>" />

    <div id="middle">
        <h2><%= flowHeader%> <fmt:message key="flow"/> : <%= phase%> <fmt:message key="phase.handlers"/></h2>

        <div id="workArea">

            <form action="">
                <div>
                    <%
                        if (currentPhase == null || currentPhase.getHandlers() == null || currentPhase.getHandlers().length == 0) {
                    %>
                    <p><b>
                        <i><fmt:message key="no.handlers.present"/></i>
                    </b></p>
                    <% } else { %>
                    <table border="0" cellspacing="0" cellpadding="0" id="flowChain">
                        <tr>
                            <td>&#160;</td>
                        </tr>
                        <tr>
                            <td>
                                <table border="0" cellspacing="0" cellpadding="0">
                                    <tr>
                                        <td><img
                                                src="extensions/core/images/handlerChain_leftmost_<%= flowImgName%>.gif"/>
                                        </td>
                                        <%
                                            int position = 0;
                                            for (HandlerData handlerData : currentPhase.getHandlers()) {
                                                position++;
                                                String text = handlerData.getName() +
                                                          (handlerData.getPhaseLast() ? "<br/>(phaseLast)" : "");
                                                if ((position % 2) == 1) {
                                                    if (position != 1) {
                                        %>

                                        <td><img src="extensions/core/images/handler_01_left.gif"
                                                 border="0px"/></td>

                                        <% } %>
                                        <td id="handler_01_BG"
                                            title="<%= handlerData.getClassName() %>">
                                            <nobr><%=text%></nobr>
                                        </td>
                                        <td><img src="extensions/core/images/handler_01_right.gif"
                                                 border="0px"/></td>

                                        <% } else if ((position % 2) == 0) {
                                            if (position != 1) {
                                        %>

                                        <td><img src="extensions/core/images/handler_02_left.gif"
                                                 border="0px"/></td>

                                        <% } %>
                                        <td id="handler_02_BG"
                                            title="<%= handlerData.getClassName()%>">
                                            <nobr><%= text %></nobr>
                                        </td>
                                        <td><img src="extensions/core/images/handler_02_right.gif"
                                                 border="0px"/></td>
                                        <%
                                                }
                                            } %>
                                        <td><img
                                                src="extensions/core/images/handlerChain_rightmost_<%= flowImgName%>.gif">
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <td>&#160;</td>
                        </tr>
                    </table>
                    <% } %>
                </div>
            </form>

        </div>
    </div>

</fmt:bundle>