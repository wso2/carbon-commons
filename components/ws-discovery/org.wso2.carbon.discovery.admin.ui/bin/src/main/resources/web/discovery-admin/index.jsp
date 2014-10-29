<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.discovery.admin.ui.DiscoveryAdminClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.discovery.stub.types.mgt.ServiceDiscoveryConfig" %>
<%@ page import="java.net.MalformedURLException" %>
<%@ page import="java.net.URL" %>
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

<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>

<fmt:bundle basename="org.wso2.carbon.discovery.admin.ui.i18n.Resources">
    <carbon:breadcrumb
            label="wsd.admin.menu.text"
            resourceBundle="org.wso2.carbon.discovery.admin.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.discovery.admin.ui.i18n.JSResources"
            request="<%=request%>"
            i18nObjectName="wsdi18n"/>

<script type="text/javascript">
    function enableDiscovery() {
        var url = document.getElementById('wsd.proxy.url').value;
        if (url == null || url == '') {
            CARBON.showErrorDialog(wsdi18n['wsd.admin.invalid.url']);
            return false;
        }

        var form = document.getElementById('wsd_form');
        CARBON.showConfirmationDialog(wsdi18n['wsd.admin.confirm.enable'], function() {
            form.action = 'index.jsp?action=enable';
            form.submit();
            return true;
        });
        return false;
    }

    function disableDiscovery() {
        var form = document.getElementById('wsd_form');
        CARBON.showConfirmationDialog(wsdi18n['wsd.admin.confirm.disable'], function() {
            form.action = 'index.jsp?action=disable';
            form.submit();
            return true;
        });
        return false;
    }

    function getProxyStatus(url) {
        jQuery.get("testConnection-ajaxprocessor.jsp", {'url' : url},
            function(data) {
                var result = data.replace(/^\s+|\s+$/g, '');
                var div = document.getElementById('proxyStatus');
                if (result == 'success') {
                    div.innerHTML = '<font color="green">Online</font>';
                } else {
                    div.innerHTML = '<font color="red">Offline</font>';
                }
            });
    }

    function testURL(url) {
        if (url == null || url == '') {
            CARBON.showErrorDialog(wsdi18n['wsd.admin.invalid.url']);
            return false;
        } else {
            jQuery.get("testConnection-ajaxprocessor.jsp", {'url' : url},
                function(data) {
                    var result = data.replace(/^\s+|\s+$/g, '');
                    if (result == 'success') {
                        CARBON.showInfoDialog(wsdi18n['wsd.admin.url.success']);
                        return true;
                    } else if (result == 'unknown_host') {
                        CARBON.showErrorDialog(wsdi18n['wsd.admin.invalid.host']);
                        return false;
                    } else if (result == 'malformed') {
                        CARBON.showErrorDialog(wsdi18n['wsd.admin.url.malformed']);
                        return false;
                    } else if (result == 'ssl_error') {
                        CARBON.showErrorDialog(wsdi18n['wsd.admin.ssl.error']);
                        return false;
                    } else if (result == 'unsupported') {
                        CARBON.showErrorDialog(wsdi18n['wsd.admin.invalid.scheme']);
                        return false;
                    } else {
                        CARBON.showErrorDialog(wsdi18n['wsd.admin.url.failure']);
                        return false;
                    }
                });
        }
    }
</script>

<%!
    private boolean isURLValid(String url) {
        try {
            URL u = new URL(url);
            if (!"http".equals(u.getProtocol()) && !"https".equals(u.getProtocol())) {
                return false;
            }
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
%>

<%
    ServiceDiscoveryConfig discoveryConfig;
    String errorURL = null;

    try {
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
                getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        DiscoveryAdminClient client = new DiscoveryAdminClient(configContext, backendServerURL, cookie, request.getLocale());

        boolean submitted = Boolean.parseBoolean(request.getParameter("formSubmitted"));
        if (submitted) {
            String action = request.getParameter("action");
            if ("enable".equals(action)) {
                String proxyURL = request.getParameter("proxy_url");
                if (proxyURL == null || "".equals(proxyURL)) {
%>
    <script type="text/javascript">
        CARBON.showErrorDialog(wsdi18n['wsd.admin.invalid.url']);
    </script>
<%
                } else if (!isURLValid(proxyURL)) {
                    errorURL = proxyURL;
%>
    <script type="text/javascript">
        CARBON.showErrorDialog(wsdi18n['wsd.admin.invalid.url']);
    </script>
<%
                } else {
                    client.enableServiceDiscovery(proxyURL);
                }
            } else if ("disable".equals(action)) {
                boolean sendBye = request.getParameter("send_bye") != null;
                client.disableServiceDiscovery(sendBye);
            }
        }

        discoveryConfig = client.getDiscoveryConfig();
        if (errorURL != null) {
            discoveryConfig.setProxyURL(errorURL);
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

    <div id="middle">
        <h2><fmt:message key="wsd.admin.control.panel"/></h2>
        <div id="workArea">                        
            <form id="wsd_form" action="" method="POST">
                <input type="hidden" name="formSubmitted" value="true"/>
                <table class="styledLeft" id="wsdProxyTable">
                    <thead>
                        <tr>
                            <th colspan="2"><fmt:message key="wsd.admin.proxy.config"/></th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td><fmt:message key="wsd.admin.status"/></td>
                            <td>
                                <%
                                    if (discoveryConfig.getEnabled()) {
                                %>
                                    <font color="green"><fmt:message key="wsd.admin.enabled"/></font>
                                <%
                                    } else {
                                %>
                                    <font color="red"><fmt:message key="wsd.admin.disabled"/></font>
                                <%
                                    }
                                %>
                            </td>
                        </tr>
                        <tr>
                            <td><fmt:message key="wsd.admin.proxy.url"/></td>
                            <td>
                                <input id="wsd.proxy.url" type="text" name="proxy_url" size="60"
                                        value="<%=discoveryConfig.getProxyURL() != null ? discoveryConfig.getProxyURL() : ""%>"/>
                                <button id="test.button" class="button" onclick="testURL(document.getElementById('wsd.proxy.url').value); return false;">
                                    <fmt:message key="wsd.admin.test.conn"/>
                                </button>
                            </td>
                        </tr>
                        <%
                            if (discoveryConfig.getEnabled()) {
                        %>
                        <tr>
                            <td><fmt:message key="wsd.admin.proxy.status"/></td>
                            <td>
                                <div id="proxyStatus"></div>
                                <script type="text/javascript">
                                    getProxyStatus(document.getElementById('wsd.proxy.url').value);
                                </script>
                            </td>
                        </tr>
                        <tr>
                            <td><fmt:message key="wsd.admin.send.bye"/></td>
                            <td><input type="checkbox" name="send_bye"/></td>
                        </tr>
                        <%
                            }
                        %>
                        <tr>
                            <td colspan="2" class="buttonRow">
                                <%
                                    if (discoveryConfig.getEnabled()) {
                                %>
                                <button class="button" onclick="disableDiscovery(); return false;"><fmt:message key="wsd.admin.disable"/></button>
                                <%
                                    } else {
                                %>
                                <button class="button" onclick="enableDiscovery(); return false;"><fmt:message key="wsd.admin.enable"/></button>
                                <%
                                    }
                                %>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </form>
        </div>
    </div>

    <%
        if (discoveryConfig.getEnabled()) {
    %>
        <script type="text/javascript">
            var input = document.getElementById('wsd.proxy.url');
            input.setAttribute('readonly', 'true');            
        </script>
    <%
        } else {
    %>
        <script type="text/javascript">
            var input = document.getElementById('wsd.proxy.url');
            input.removeAttribute('readonly');
        </script>
    <%
        }
    %>

    <script type="text/javascript">
        alternateTableRows('wsdProxyTable', 'tableEvenRow', 'tableOddRow');
    </script>

</fmt:bundle>
