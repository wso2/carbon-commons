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
<%@ page import="org.wso2.carbon.transport.jms.ui.JMSTransportAdminClient" %>
<%@ page import="org.wso2.carbon.transport.jms.stub.types.carbon.TransportParameter" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Iterator" %>

<script type="text/javascript" src="global-params.js"></script>

<%
    String backendServerURL;
    ConfigurationContext configContext;
    String cookie;
    JMSTransportAdminClient client;
    TransportParameter[] transportInData;

    backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    configContext = (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    client = new JMSTransportAdminClient(cookie, backendServerURL,configContext);

    try {
        transportInData = client.getGloballyDefinedInParameters();
    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
%>
<script type="text/javascript">
    location.href = "../admin/error.jsp";
</script>
<%
        return;
    }
%>

<fmt:bundle basename="org.wso2.carbon.transport.jms.ui.i18n.Resources">
    <carbon:breadcrumb
            label="Enable"
            resourceBundle="org.wso2.carbon.transport.jms.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>" />

    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>

    <div id="middle">
        <h2 id="listTransport"><fmt:message key="transport.mgmt"/></h2>
        <div id="workArea">

            <script type="text/javascript">

                function enableTransport() {
                    CARBON.showConfirmationDialog("<fmt:message key='transport.listener.enable.message'/>",
                            function() {
                                location.href='./enable_transport.jsp';
                            }, null);
                }
            </script>

            <table class="styledLeft" id="jmsTransport" width="100%">
                <tbody id="paramTableBody">
                <tr>
                    <td colspan="2" style="border-left: 0px !important; border-right: 0px !important; border-top: 0px !important; padding-left: 0px !important;">
                        <h4><strong><fmt:message key="transport.listener"/></strong></h4>
                    </td>
                </tr>

                <tr>
                    <td class="sub-header"><fmt:message key="transport.jms.conn.fac.name"/></td>
                    <td class="sub-header"><fmt:message key="transport.jms.conn.fac.params"/></td>
                    <td class="sub-header"></td>
                </tr>
                <%
                    if (transportInData != null && transportInData.length>0) {
                        for (TransportParameter currentParam : transportInData) {
                            Map<String,String> factorySettings = client.getDisplayParameters(currentParam);
                %>
                <tr>
                    <td>
                        <%=currentParam.getName()%>
                    </td>
                    <td>
                        <ul>
                <%
                    String initialFactory;
                    if ((initialFactory =factorySettings.get("java.naming.factory.initial")) != null) {
                %>
                            <li><fmt:message key="transport.jms.factory.initial"/> := <%=initialFactory%></li>
                <%
                    }
                %>
                <%
                    String providerUrl;
                    if ((providerUrl =factorySettings.get("java.naming.provider.url")) != null) {
                %>
                            <li><fmt:message key="transport.jms.conn.url"/> := <%=providerUrl%></li>
                <%
                    }
                %>
                <%
                    String jndiName;
                    if ((jndiName =factorySettings.get("transport.jms.ConnectionFactoryJNDIName")) != null) {
                %>
                            <li><fmt:message key="transport.jms.jndi.name"/> := <%=jndiName%></li>
                <%
                    }
                %>
                <%
                    String factoryType;
                    if ((factoryType =factorySettings.get("transport.jms.ConnectionFactoryType")) != null) {
                %>
                            <li>Connection Factory Type := <%=factoryType%></li>
                <%
                    }
                %>
                        </ul>
                    </td>
                    <td>
                        <a class="icon-link" style="background-image: url(./images/tranport-config.gif);" href="add_connection_factory.jsp?isUpdate=true&facName=<%=currentParam.getName()%>"> Edit/Remove </a>
                    </td>
                </tr>
                <%
                    }
                } else { %>
                <tr id="noParamsDefined">
                    <td colspan="3"><fmt:message key="no.params.defined"/></td>
                </tr>
                <% } %>
                </tbody>
                <tr>
                    <td colspan="3" class="buttonRow">
                        <input name="enableBtn" type="button" value="Enable" class="button" onclick="javascript:enableTransport()"/>
                        <input name="cancelBtn" class="button" type="reset" value="Cancel"  onclick="javascript:window.history.go(-1); return false;"/>
                    </td>
                </tr>

            </table>
        </div>
        <a class="icon-link" style="background-image: url(../admin/images/add.gif);" href="add_connection_factory.jsp"> <fmt:message key="transport.jms.factory.add"/> </a>

    </div>
</fmt:bundle>