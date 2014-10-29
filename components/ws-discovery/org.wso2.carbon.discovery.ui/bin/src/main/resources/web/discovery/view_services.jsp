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
<%@ page import="org.wso2.carbon.discovery.stub.types.mgt.TargetServiceDetails" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.discovery.ui.client.DiscoveryAdminClient" %>
<%@ page import="javax.xml.namespace.QName" %>
<%@ page import="java.net.URI" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.discovery.ui.i18n.Resources">
    <carbon:breadcrumb
            label="wsd.view.services.text"
            resourceBundle="org.wso2.carbon.discovery.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.discovery.ui.i18n.JSResources"
            request="<%=request%>"
            i18nObjectName="wsdi18n"/>

    <script type="text/javascript">
        function goBack() {
            window.location.href = 'index.jsp?region=region1&item=ws_discovery_menu';
        }
    </script>
    <style type="text/css">
        .linkListing li{
            margin-bottom:5px;
            list-style:circle !important;
        }
        .linkListing{
            margin-left:12px;
        }
    </style>
    <%
        String proxyName = request.getParameter("proxy");
        if (proxyName == null) {
            session.setAttribute("discoveryError", "wsd.proxy.name.null");
    %>
        <script type="text/javascript">
            window.location.href = 'index.jsp';
        </script>
    <%
        }

        request.setAttribute("proxyName", proxyName);

        TargetServiceDetails[] services = null;

        QName[] types = null;
        URI[] scopes = null;
        boolean abortSearch = false;

        String searchScopes = request.getParameter("searchScopes");
        if (searchScopes == null && session.getAttribute("lastSearch") != null) {
            searchScopes = (String) session.getAttribute("lastSearch");
        }

        if (searchScopes != null && !"".equals(searchScopes)) {
            request.setAttribute("currentQuery", searchScopes);
            session.setAttribute("lastSearch", searchScopes);
            
            String[] scopeValues = searchScopes.split(",");
            scopes = new URI[scopeValues.length];
            try {
                for (int i = 0; i < scopes.length; i++) {
                    scopes[i] = URI.create(scopeValues[i].trim());
                }
            } catch (Exception e) {
                abortSearch = true;
    %>
        <script type="text/javascript">
            CARBON.showErrorDialog(wsdi18n['wsd.invalid.query']);
        </script>
    <%
                scopes = null;
            }
        }

        try {
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            DiscoveryAdminClient client = new DiscoveryAdminClient(
                    configContext, backendServerURL, cookie, request.getLocale());
            if (!abortSearch) {
                services = client.probeDiscoveryProxy(proxyName, types, scopes);
            }
        } catch (Exception e) {
            String cause;
            if (e.getCause() != null) {
                cause = e.getCause().getMessage();
            } else {
                cause = e.getMessage();
            }
    %>
        <script type="text/javascript">
               CARBON.showErrorDialog('<%=cause%>');
        </script>
    <%
        }
    %>

    <div id="middle">
        <h2><fmt:message key="wsd.target.services"/> (<fmt:message key="wsd.proxy.info.open"/> <%=proxyName%>)</h2>
        <div id="workArea">
            <p>
                <fmt:message key="wsd.view.services.intro"/><%=proxyName%>
            </p>
            <p>&nbsp;</p>
            <jsp:include page="search_services.jsp"/>
            <p>&nbsp;</p>
            <p><fmt:message key="wsd.displaying"/> <%=services == null ? 0 : services.length%> <fmt:message key="wsd.services"/></p>
            <p>&nbsp;</p>
            <table class="styledLeft" id="serviceTable">
                <thead>
                    <tr>
                        <th><fmt:message key="wsd.service.id"/></th>
                        <th><fmt:message key="wsd.service.scopes"/></th>
                        <th><fmt:message key="wsd.service.endpoints"/></th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        if (services == null) {
                    %>
                        <tr>
                            <td colspan="3"><fmt:message key="wsd.no.services"/></td>
                        </tr>
                    <%
                        } else {
                            for (TargetServiceDetails service : services) {
                    %>
                            <tr>
                                <td width="35%" style="vertical-align:top !important;padding-top:5px;"><a href="view_service.jsp?service=<%=service.getServiceId()%>&proxy=<%=proxyName%>"><%=service.getServiceId()%></a></td>
                                <td style="vertical-align:top !important">
                                    <ul style="line-height:1.5" class="linkListing">
                                        <%
                                            String[] scopeValues = service.getScopes();
                                            if (scopeValues != null && scopeValues.length > 0 && scopeValues[0] != null) {
                                                for (String s : scopeValues) {
                                        %>
                                        <li><%=s%></li>
                                        <%
                                                }
                                            }
                                        %>
                                    </ul>
                                </td>
                                <td style="vertical-align:top !important">
                                    <ul style="line-height:1.5" class="linkListing">
                                        <%
                                            String[] addrValues = service.getAddresses();
                                            if (addrValues != null && addrValues.length > 0 && addrValues[0] != null) {
                                                for (String s : addrValues) {
                                        %>
                                        <li><%=s%></li>
                                        <%
                                                }
                                            }
                                        %>
                                    </ul>
                                </td>
                            </tr>
                    <%
                            }
                        }
                    %>
                </tbody>
            </table>
            <p>&nbsp;</p>
            <button class="button" onclick="goBack(); return false;"><fmt:message key="wsd.done"/></button>
        </div>
    </div>

    <script type="text/javascript">
        alternateTableRows('serviceTable', 'tableEvenRow', 'tableOddRow');
    </script>

</fmt:bundle>