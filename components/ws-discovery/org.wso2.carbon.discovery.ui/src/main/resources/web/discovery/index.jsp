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
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.discovery.ui.client.DiscoveryAdminClient" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.discovery.stub.types.mgt.DiscoveryProxyDetails" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>

<fmt:bundle basename="org.wso2.carbon.discovery.ui.i18n.Resources">
    <carbon:breadcrumb
            label="wsd.menu.text"
            resourceBundle="org.wso2.carbon.discovery.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.discovery.ui.i18n.JSResources"
            request="<%=request%>"
            i18nObjectName="wsdi18n"/>

    <style type="text/css">
        a.icon-disabled{
            color:#acacac !important;    
        }
        a.icon-disabled:hover{
            color:#acacac !important;
        }
    </style>
    
    <script type="text/javascript">
        function deleteProxy(proxy) {
            CARBON.showConfirmationDialog(wsdi18n['wsd.confirm.proxy.delete'] + ' '  + proxy + '?', function() {
                location.href = "remove_proxy.jsp?proxy=" + proxy;
            });
        }

        function editProxy(proxy) {
            window.location.href = 'add_proxy.jsp?proxy=' + proxy;
        }

        function viewServices(proxy) {
            window.location.href = 'view_services.jsp?proxy=' + proxy;
        }

        function proxyOffline(proxy) {
            CARBON.showInfoDialog(wsdi18n['wsd.proxy.1'] + proxy + ' ' + wsdi18n['wsd.offline']);
        }
    </script>

    <%
        session.removeAttribute("lastSearch");

        if (session.getAttribute("discoveryInfo") != null) {
            String info = (String) session.getAttribute("discoveryInfo");
    %>
        <script type="text/javascript">
            YAHOO.util.Event.onDOMReady(function(){CARBON.showInfoDialog(wsdi18n['<%=info%>']);});
        </script>
    <%
        } else if (session.getAttribute("discoveryError") != null) {
            String error = (String) session.getAttribute("discoveryError");
    %>
        <script type="text/javascript">
            YAHOO.util.Event.onDOMReady(function(){CARBON.showErrorDialog(wsdi18n['<%=error%>']);});
        </script>
    <%
        }

        session.removeAttribute("discoveryInfo");
        session.removeAttribute("discoveryError");

        Map<String, DiscoveryProxyDetails> proxyMap = null;
        try {
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            DiscoveryAdminClient client = new DiscoveryAdminClient(
                    configContext, backendServerURL, cookie, request.getLocale());
            proxyMap = client.getDiscoveryProxies();
            
        } catch (Exception e) {
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
    %>
        <script type="text/javascript">
               location.href = "../admin/error.jsp";
        </script>
    <%
        }
    %>

    <div id="middle">
        <h2><fmt:message key="wsd.control.panel"/></h2>
        <div id="workArea">
            <p>
                <fmt:message key="wsd.control.panel.intro"/>
            </p>
            <p>&nbsp;</p>
            
            <table class="styledLeft" id="wsdProxyTable">
                <thead>
                    <tr>
                        <th><fmt:message key="wsd.proxy.name"/></th>
                        <th><fmt:message key="wsd.proxy.url"/></th>
                        <th><fmt:message key="wsd.proxy.status"/></th>
                        <th><fmt:message key="wsd.proxy.actions"/></th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        if (proxyMap != null) {
                            for (String key : proxyMap.keySet()) {
                                DiscoveryProxyDetails pd = proxyMap.get(key);
                    %>
                    <tr>
                        <td width="20%"><%=key%></td>
                        <td width="50%"><%=pd.getUrl()%></td>
                        <td>
                            <%
                                if (pd.getOnline()) {
                            %>
                            <span style="color:green;"><fmt:message key="wsd.online"/></span>
                            <%
                                } else {
                            %>
                            <span style="color:red;"><fmt:message key="wsd.offline"/></span>
                            <%
                                }
                            %>
                        </td>
                        <td style="width: 180px; padding-left: 0px ! important;">
                            <div>
                                <%
                                    if (pd.getOnline()) {
                                %>
                                <a href="#" onclick="viewServices('<%=key%>')" class="icon-link"
                                    style="background-image:url(../admin/images/view.gif);">View</a>
                                <%
                                    } else {
                                %>
                                <a class="icon-link icon-disabled"
                                    style="background-image:url(../admin/images/view-disabled.gif);cursor:default ">View</a>
                                <%
                                    }
                                %>
                                <a href="#" onclick="editProxy('<%=key%>')" class="icon-link"
                                    style="background-image:url(../admin/images/edit.gif);">Edit</a>
                                <a href="#" onclick="deleteProxy('<%=key%>')" class="icon-link"
                                    style="background-image:url(../admin/images/delete.gif);">Delete</a>
                            </div>    
                        </td>
                    </tr>
                    <%
                            }
                        } else {
                    %>
                    <tr><td colspan="4"><fmt:message key="wsd.no.proxies"/></td></tr>
                    <%
                        }
                    %>
                </tbody>
            </table>
            <table cellpadding="0" cellspacing="0">
                <tbody>
                    <tr><td>&nbsp;</td></tr>
                    <tr>
                        <td>
                            <a class="icon-link"
                               href="add_proxy.jsp"
                               style="background-image: url(../admin/images/add.gif);"><fmt:message key="wsd.add.proxy"/></a>
                        </td>
                    </tr>
                </tbody>
            </table>

        </div>
    </div>

    <script type="text/javascript">
        alternateTableRows('wsdProxyTable', 'tableEvenRow', 'tableOddRow');
    </script>

</fmt:bundle>
