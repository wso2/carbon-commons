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
<%@ page import="org.wso2.carbon.discovery.stub.types.mgt.TargetServiceDetails" %>
<%@ page import="org.wso2.carbon.discovery.stub.types.mgt.DiscoveryProxyDetails" %>
<%@ page import="javax.xml.namespace.QName" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.net.MalformedURLException" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.HashSet" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>

<fmt:bundle basename="org.wso2.carbon.discovery.ui.i18n.Resources">
    <carbon:breadcrumb
            label="wsd.view.service.text"
            resourceBundle="org.wso2.carbon.discovery.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.discovery.ui.i18n.JSResources"
            request="<%=request%>"
            i18nObjectName="wsdi18n"/>

    <script type="text/javascript">
        function goBack(proxy) {
            window.location.href = 'view_services.jsp?proxy=' + proxy + '&ordinal=1';
        }

        function addEndpoint(proxy, service) {
            var inputBox = document.getElementById('endpoint_name');
            if (!inputBox) {
                return false;
            }

            var name = inputBox.value;
            if (name == null || name == '') {
                CARBON.showErrorDialog(wsdi18n['wsd.specify.endpoint.name']);
                return false;
            }

            endpoint_form.action = 'view_service.jsp?proxy=' + proxy + '&service=' + service;
            endpoint_form.submit();
            return true;
        }

        function addProxy(proxy, service) {
            var inputBox = document.getElementById('proxy_name');
            if (!inputBox) {
                return false;
            }

            var name = inputBox.value;
            if (name == null || name == '') {
                CARBON.showErrorDialog(wsdi18n['wsd.specify.proxy.svc.name']);
                return false;
            }

            proxy_form.action = 'view_service.jsp?proxy=' + proxy + '&service=' + service;
            proxy_form.submit();
            return true;
        }

        function showProxyInfo() {
            var body = document.getElementById('discovery_proxy_info');
            if (!body) {
                return;
            }

            var link = document.getElementById('proxy_info_link');

            if (body.style.display == 'none') {
                body.style.display = '';
                link.style.backgroundImage = 'url(../admin/images/up-arrow.gif)';
            } else {
                body.style.display = 'none';
                link.style.backgroundImage = 'url(../admin/images/down-arrow.gif)';
            }
        }

        function showEndpointForm() {
            var body = document.getElementById('create_endpoints_form');
            if (!body) {
                return;
            }

            var link = document.getElementById('create_endpoints_link');

            if (body.style.display == 'none') {
                body.style.display = '';
                link.style.backgroundImage = 'url(../admin/images/up-arrow.gif)';
            } else {
                body.style.display = 'none';
                link.style.backgroundImage = 'url(../admin/images/down-arrow.gif)';
            }
        }

        function showProxyForm() {
            var body = document.getElementById('create_proxy_form');
            if (!body) {
                return;
            }

            var link = document.getElementById('create_proxy_link');

            if (body.style.display == 'none') {
                body.style.display = '';
                link.style.backgroundImage = 'url(../admin/images/up-arrow.gif)';
            } else {
                body.style.display = 'none';
                link.style.backgroundImage = 'url(../admin/images/down-arrow.gif)';
            }
        }
    </script>

    <%!
        private String getHostNames(String[] uris) {
            String s = "";
            if (uris == null || uris.length == 0 || uris[0] == null) {
                return s;
            }

            Set<String> set = new HashSet<String>();
            for (String u : uris) {
                try {
                    URL url = new URL(u);
                    set.add(url.getHost());
                } catch (MalformedURLException e) {

                }
            }

            for (String key : set) {
                if ("".equals(s)) {
                    s += key;
                } else {
                    s += ", " + key;
                } 
            }
            return s;
        }

        private String getProtocols(String[] uris) {
            String s = "";
            if (uris == null || uris.length == 0 || uris[0] == null) {
                return s;
            }

            Set<String> set = new HashSet<String>();
            for (String u : uris) {
                try {
                    URL url = new URL(u);
                    set.add(url.getProtocol());
                } catch (MalformedURLException e) {

                }
            }

            for (String key : set) {
                if ("".equals(s)) {
                    s += key;
                } else {
                    s += ", " + key;
                }
            }
            return s;
        }
    %>

    <%
        boolean submitted = Boolean.valueOf(request.getParameter("formSubmitted"));
        boolean endpointSaveFailed = false;
        String endpointName = null;

        boolean proxyFormSubmitted = Boolean.valueOf(request.getParameter("proxyFormSubmitted"));
        boolean proxySaveFailed = false;
        String proxyServiceName = null;
        boolean isDynamic = false;

        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        DiscoveryAdminClient client;

        if (submitted) {
            endpointName = request.getParameter("endpointName");
    %>
    <jsp:include page="create_endpoint.jsp"/>
    <%
            String endpointError = (String) request.getAttribute("wsd.endpoint.error");
            if (endpointError != null) {
                endpointSaveFailed = true;
                if ("invalid.name".equals(endpointError)) {
    %>
    <script type="text/javascript">
        YAHOO.util.Event.onDOMReady(function(){CARBON.showErrorDialog(wsdi18n['wsd.specify.endpoint.name']);});
    </script>
    <%
                } else if ("invalid.address".equals(endpointError)) {
    %>
    <script type="text/javascript">
        YAHOO.util.Event.onDOMReady(function(){CARBON.showErrorDialog(wsdi18n['wsd.specify.endpoint.addr']);});
    </script>
    <%
                } else if ("unexpected.error".equals(endpointError)) {
                    String cause = (String) request.getAttribute("wsd.endpoint.error.cause");
    %>
    <script type="text/javascript">
        YAHOO.util.Event.onDOMReady(function(){CARBON.showErrorDialog('Endpoint creation failed. <%=cause%>');});
    </script>
    <%
                }
            } else if (Boolean.parseBoolean((String) request.getAttribute("wsd.endpoint.success"))) {
    %>
    <script type="text/javascript">
        YAHOO.util.Event.onDOMReady(function() {
            CARBON.showInfoDialog(wsdi18n['wsd.new.endpoint'] + ' <%=endpointName%> ' + wsdi18n['wsd.create.success']);
        });
    </script>
    <%
            }
        }

        if (proxyFormSubmitted) {
            proxyServiceName = request.getParameter("proxyServiceName");
            isDynamic = Boolean.valueOf(request.getParameter("isDynamic"));
    %>
    <jsp:include page="create_proxy_service.jsp"/>
    <%
            String proxyError = (String) request.getAttribute("wsd.proxy.svc.error");
            if (proxyError != null) {
                proxySaveFailed = true;
                if ("invalid.name".equals(proxyError)) {
    %>
    <script type="text/javascript">
        CARBON.showErrorDialog(wsdi18n['wsd.specify.proxy.svc.name']);
    </script>
    <%
                } else if ("invalid.address".equals(proxyError)) {
    %>
    <script type="text/javascript">
        CARBON.showErrorDialog(wsdi18n['wsd.specify.proxy.svc.addr']);
    </script>
    <%
                } else if ("malformed.url".equals(proxyError)) {
    %>
    <script type="text/javascript">
        YAHOO.util.Event.onDOMReady(function() {
            CARBON.showErrorDialog(wsdi18n['wsd.proxy.svc.endpoint.invalid']);
        });
    </script>
    <%
                } else if ("unexpected.error".equals(proxyError)) {
                    String cause = (String) request.getAttribute("wsd.proxy.svc.error.cause");
    %>
    <script type="text/javascript">
        YAHOO.util.Event.onDOMReady(function() {
            CARBON.showErrorDialog(wsdi18n['wsd.proxy.svc.add.failed'] + ' <%=cause%>');
        });
    </script>
    <%
                }
            } else if (Boolean.parseBoolean((String) request.getAttribute("wsd.proxy.svc.created"))) {
    %>
    <script type="text/javascript">
        YAHOO.util.Event.onDOMReady(function() {
            CARBON.showInfoDialog(wsdi18n['wsd.proxy.svc.add.success'] + ' <%=proxyServiceName%>');
        });
    </script>
    <%
            }
        }

        String serviceId = request.getParameter("service");
        if (serviceId == null) {
            session.setAttribute("discoveryError", "wsd.service.id.null");
    %>
        <script type="text/javascript">
            window.location.href = 'index.jsp';
        </script>
    <%
        }

        String proxyName = request.getParameter("proxy");
        if (proxyName == null) {
            session.setAttribute("discoveryError", "wsd.proxy.name.null");
    %>
        <script type="text/javascript">
            window.location.href = 'index.jsp';
        </script>
    <%
        }

        TargetServiceDetails service = null;
        DiscoveryProxyDetails proxy = null;

        try {
            client = new DiscoveryAdminClient(configContext, backendServerURL, cookie,
                                                      request.getLocale());
            service = client.resolveTargetService(proxyName, serviceId);
            proxy = client.getDiscoveryProxy(proxyName);
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
        <h2><fmt:message key="wsd.target.service"/> <%=serviceId%></h2>
        <div id="workArea">
            <table class="styledLeft" id="serviceTable">
                <thead>
                    <tr>
                        <th colspan="2"><fmt:message key="wsd.service.info"/></th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        if (service != null) {
                    %>
                    <tr>
                        <td class="leftCol-small"><fmt:message key="wsd.service.id"/></td>
                        <td><%=service.getServiceId()%></td>
                    </tr>
                    <tr>
                        <td><fmt:message key="wsd.service.scopes"/></td>
                        <td>
                            <ul style="line-height:1.5">
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
                    </tr>
                    <tr>
                        <td><fmt:message key="wsd.service.types"/></td>
                        <td>
                            <%
                                String[] typeValues = service.getTypes();
                                String t = "";
                                if (typeValues != null && typeValues.length > 0 && typeValues[0] != null) {
                                    for (String s : typeValues) {
                                        QName qname = QName.valueOf(s);
                                        if (!"".equals(t)) {
                                            t += ", ";   
                                        }
                                        t += qname.getLocalPart();
                                        if (qname.getNamespaceURI() != null) {
                                            t += " (" + qname.getNamespaceURI() + ")";
                                        }
                                    }
                                }
                            %>
                            <%=t%>
                        </td>
                    </tr>
                    <tr>
                        <td><fmt:message key="wsd.service.endpoints"/></td>
                        <td>
                            <ul style="line-height:1.5">
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
                    <tr>
                        <td><fmt:message key="wsd.service.version"/></td>
                        <td><%=service.getVersion()%></td>
                    </tr>
                    <tr>
                        <td><fmt:message key="wsd.service.servers"/></td>
                        <td><%=getHostNames(service.getAddresses())%></td>
                    </tr>
                    <tr>
                        <td><fmt:message key="wsd.service.protocols"/></td>
                        <td><%=getProtocols(service.getAddresses())%></td>
                    </tr>
                    <%
                        } else {
                    %>
                    <tr>
                        <td colspan="2"><fmt:message key="wsd.no.service.info"/></td>
                    </tr>
                    <%
                        }
                    %>
                </tbody>
            </table>
            <p>&nbsp;</p>
            <table class="styledLeft" id="proxyTable">
                <thead>
                    <tr>
                        <th colspan="2">
                            <a id="proxy_info_link" class="icon-link" href="#"
                               style="background-image: url(../admin/images/down-arrow.gif);"
                               onclick="showProxyInfo();"><fmt:message key="wsd.proxy.info"/></a>
                        </th>
                    </tr>
                </thead>
                <tbody style="display:none" id="discovery_proxy_info">
                    <%
                        if (proxy != null) {
                    %>
                    <tr>
                        <td class="leftCol-small"><fmt:message key="wsd.proxy.name"/></td>
                        <td><%=proxy.getName()%></td>
                    </tr>
                    <tr>
                        <td><fmt:message key="wsd.proxy.url"/></td>
                        <td><%=proxy.getUrl()%></td>
                    </tr>
                    <tr>
                        <td><fmt:message key="wsd.proxy.status"/></td>
                        <td>
                            <%
                                if (proxy.getOnline()) {
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
                    </tr>
                    <%
                        } else {
                    %>
                    <tr>
                        <td colspan="2"><fmt:message key="wsd.no.proxy.info"/></td>
                    </tr>
                    <%
                        }
                    %>
                </tbody>
            </table>
            <%
                if (service != null && CarbonUIUtil.isContextRegistered(config, "/endpoints/")) {
                    String[] addrValues = service.getAddresses();
                    if (addrValues != null && addrValues.length > 0 && addrValues[0] != null) {
            %>
            <p>&nbsp;</p>
            <form action="" method="POST" id="endpoint_form">
                <input type="hidden" name="formSubmitted" value="true"/>
                <table class="styledLeft" id="endpointTable">
                    <thead>
                        <tr>
                            <th colspan="2">
                                <a id="create_endpoints_link" class="icon-link" href="#"
                                   style="background-image: url(../admin/images/down-arrow.gif);"
                                   onclick="showEndpointForm();"><fmt:message key="wsd.create.endpoints"/></a>
                            </th>
                        </tr>
                    </thead>
                    <tbody id="create_endpoints_form" style="display:none;">
                        <tr>
                            <td class="leftCol-small"><fmt:message key="wsd.endpoint.name"/></td>
                            <td><input type="text" size="25" name="endpointName" id="endpoint_name"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="wsd.endpoint.addr"/></td>
                            <td>
                                <select name="endpointAddress">
                                    <%
                                        for (String addr : addrValues) {
                                    %>
                                    <option value="<%=addr%>"><%=addr%></option>
                                    <%
                                        }
                                    %>
                                </select>
                            </td>
                        </tr>
                    <tr>
                        <td class="buttonRow" colspan="2">
                            <button class="button" onclick="addEndpoint('<%=proxyName%>', '<%=serviceId%>'); return false;"><fmt:message key="wsd.save"/></button>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </form>
            <%
                    }
                }
            %>
            <%
                if (service != null && CarbonUIUtil.isContextRegistered(config, "/proxyservices/")) {
                    String[] addrValues = service.getAddresses();
                    if (addrValues != null && addrValues.length > 0 && addrValues[0] != null) {
            %>
            <p>&nbsp;</p>
            <form action="" method="POST" id="proxy_form">
                <input type="hidden" name="proxyFormSubmitted" value="true"/>
                <table class="styledLeft" id="proxyServiceTable">
                    <thead>
                        <tr>
                            <th colspan="2">
                                <a id="create_proxy_link" class="icon-link" href="#"
                                   style="background-image: url(../admin/images/down-arrow.gif);"
                                   onclick="showProxyForm();"><fmt:message key="wsd.create.proxy.service"/></a>
                            </th>
                        </tr>
                    </thead>
                    <tbody id="create_proxy_form" style="display:none;">
                        <tr>
                            <td class="leftCol-small"><fmt:message key="wsd.proxy.service.name"/></td>
                            <td><input type="text" size="25" name="proxyServiceName" id="proxy_name"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="wsd.dynamic.proxy.service"/></td>
                            <td><input type="checkbox" name="isDynamic" value="true" id="is_dynamic"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="wsd.proxy.target.address"/></td>
                            <td>
                                <select name="proxyServiceAddress">
                                    <%
                                        for (String addr : addrValues) {
                                    %>
                                    <option value="<%=addr%>"><%=addr%></option>
                                    <%
                                        }
                                    %>
                                </select>
                            </td>
                        </tr>
                    <tr>
                        <td class="buttonRow" colspan="2">
                            <button class="button" onclick="addProxy('<%=proxyName%>', '<%=serviceId%>'); return false;"><fmt:message key="wsd.save"/></button>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </form>
            <%
                    }
                }
            %>
            <p>&nbsp;</p>
            <button class="button" onclick="goBack('<%=proxyName%>');"><fmt:message key="wsd.done"/></button>
        </div>
    </div>

    <%
        if (submitted) {
    %>
    <script type="text/javascript">
        showEndpointForm();
    </script>
    <%
            if (endpointSaveFailed && endpointName != null) {
    %>
    <script type="text/javascript">
        document.getElementById('endpoint_name').value = '<%=endpointName%>';
    </script>
    <%
            }
        }
    %>

    <%
        if (proxyFormSubmitted) {
    %>
    <script type="text/javascript">
        showProxyForm();
    </script>
    <%
            if (proxySaveFailed) {
                if (proxyServiceName != null) {
    %>
    <script type="text/javascript">
        document.getElementById('proxy_name').value = '<%=proxyServiceName%>';
    </script>
    <%
                }
                if (isDynamic) {
    %>
    <script type="text/javascript">
        document.getElementById('is_dynamic').checked = true;
    </script>
    <%
                }
            }
        }
    %>

    <script type="text/javascript">
        alternateTableRows('serviceTable', 'tableEvenRow', 'tableOddRow');
        alternateTableRows('proxyTable', 'tableEvenRow', 'tableOddRow');
        var table = document.getElementById('endpointTable');
        if (table) {
            alternateTableRows('endpointTable', 'tableEvenRow', 'tableOddRow');
        }
    </script>

</fmt:bundle>