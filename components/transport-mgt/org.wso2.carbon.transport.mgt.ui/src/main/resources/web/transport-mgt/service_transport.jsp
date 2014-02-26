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
<%@ page import="org.wso2.carbon.transport.mgt.ui.TransportAdminClient" %>
<%@ page import="org.wso2.carbon.transport.mgt.stub.types.carbon.TransportSummary" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>

<script type="text/javascript" src="global-params.js"></script>

<%
    String backendServerURL;
    ConfigurationContext configContext;
    String cookie;
    TransportAdminClient client;
    TransportSummary[] summary;
    TransportSummary[] serviceSummary;
    String serviceName;
    List<TransportSummary> unexposedTransports = new ArrayList<TransportSummary>();

    backendServerURL = CarbonUIUtil.getServerURL(config	.getServletContext(), session);
    configContext = (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    client = new TransportAdminClient(cookie, backendServerURL,	configContext);

    serviceName = request.getParameter("serviceName");

    try {
        summary = client.listTransports(serviceName);
        serviceSummary = client.listExposedTransports(serviceName);

        for (TransportSummary trp : summary) {
            boolean isExposed = false;
            if (serviceSummary != null) {
                for (TransportSummary exposedTrp : serviceSummary) {
                    if (exposedTrp.getProtocol().equals(trp.getProtocol())) {
                        isExposed = true;
                    }
                }
            }

            if (!isExposed) {
                unexposedTransports.add(trp);
            }
        }

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

<fmt:bundle basename="org.wso2.carbon.transport.mgt.ui.i18n.Resources">
    <carbon:breadcrumb
            label="transport.mgmt"
            resourceBundle="org.wso2.carbon.transport.mgt.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>" />

    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>

    <div id="middle">
        <h2 id="listTransport"><fmt:message key="transport.mgmt"/></h2>
        <div id="workArea">
            <script type="text/javascript">
                function removeTransport(transport,count) {
                    if(count>1)
                    {
                        CARBON.showConfirmationDialog('<fmt:message key="remove.transport.msg1"/>'+ transport +' <fmt:message key="remove.transport.msg2"/>',
                                function() {
                                    location.href = "remove_transport.jsp?serviceName=<%=serviceName%>&transport=" + transport;
                                }, null);
                    }
                    else
                    {
                        CARBON.showInfoDialog('<fmt:message key="cannot.remove.transport"/>');
                    }
                }
            </script>
            <form action="add_transport.jsp">
                <table class="styledLeft" id="availableTransports" width="100%">
                    <thead>
                    <tr>
                        <th align="left" colspan="2"><fmt:message key="transport.add.transports"/></th>
                    </tr>
                    </thead>

                    <%
                        if (unexposedTransports.size() > 0) {
                    %>

                    <tr>
                        <td width="50%"><fmt:message key="transport.protocol"/></td>
                        <td width="50%">
                            <input type="hidden" id="service" name="service" value="<%=serviceName%>"/>
                            <select id="protocol" name="protocol">
                                <%
                                    for (TransportSummary data : unexposedTransports) {
                                %>
                                <option value="<%=data.getProtocol()%>"><%=data.getProtocol()%></option>
                                <%
                                    }
                                %>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <input type="submit" value="<fmt:message key="transport.add"/>"/>
                        </td>
                    </tr>
                    <%
                    } else {
                    %>
                    <tr>
                        <td colspan="2">
                            <fmt:message key="transport.all.transports.added"/>
                        </td>
                    </tr>
                    <%
                        }
                    %>
                </table>
                <br/><br/>
            </form>
            <form action="">
                <table class="styledLeft" id="exposedTransports" width="100%">
                    <thead>
                    <tr>
                        <th align="left" colspan="2"><fmt:message key="transport.exposed.transports"/></th>
                    </tr>
                    </thead>


                    <%
                        if (serviceSummary != null) {
                    %>
                    <thead>
                        <tr>
                            <th align="left" width="50%"><fmt:message key="transport.protocol"/></th>
                            <th align="left" width="50%"><fmt:message key="transport.action"/></th>
                        </tr>
                    </thead>
                    <%
                        for (TransportSummary data : serviceSummary) {
                    %>

                    <tr>
                        <%
                            boolean isNonRemovable = data.getNonRemovable();
                        %>

                        <td width="50%"><%=data.getProtocol()%></td>
                        <td width="50%">
                            <%
                                if (!isNonRemovable) {
                            %>
                            <a title="Remove Transport"
                               onclick="removeTransport('<%=data.getProtocol()%>','<%=serviceSummary.length%>');return false;"
                               href="#">
                                <img src="images/delete.gif" alt="<fmt:message key='remove.transport'/>"/>
                            </a>
                            <%
                                }

                                String uiContext = data.getProtocol() + "-config";
                                boolean useTrpSpecificUI = CarbonUIUtil.isContextRegistered(config, "/" + uiContext + "/");
                                if (!useTrpSpecificUI) {
                            %>
                            <a title="Configure Transport" href="./service_config.jsp?serviceName=<%=serviceName%>&transport=<%=data.getProtocol()%>">
                                <img src="images/transport-config.gif" alt="<fmt:message key='configure.transport'/>"/>
                            </a>
                            <%
                            } else {
                            %>
                            <a title="Configure Transport" href="../<%=uiContext%>/service_config.jsp?serviceName=<%=serviceName%>">
                                <img src="images/transport-config.gif" alt="<fmt:message key='configure.transport'/>"/>
                            </a>
                            <%
                                }
                            %>
                        </td>
                    </tr>
                    <%
                        }
                    } else {
                    %>
                    <tr>
                        <td colspan="2"><fmt:message key="no.exposed.transports"/></td>
                    </tr>
                    <%
                        }
                    %>
                </table>
                <div>
                    <table>
                        <tr>
                            <td><br/><input class="button" type="reset"
                                            value="<fmt:message key="transport.finish"/>"
                                            onclick="javascript:document.location.href='../service-mgt/service_info.jsp?serviceName=<%=serviceName%>&ordinal=1'"/ >
                            </td>
                        </tr>
                    </table>
                </div>
            </form>
        </div>
    </div>
</fmt:bundle>
