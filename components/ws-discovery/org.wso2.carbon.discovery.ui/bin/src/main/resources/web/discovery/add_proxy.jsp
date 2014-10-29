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
<%@ page import="org.wso2.carbon.discovery.stub.types.mgt.DiscoveryProxyDetails" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.discovery.ui.client.DiscoveryAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.net.MalformedURLException" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.discovery.ui.i18n.Resources">
    <carbon:breadcrumb
            label="wsd.config.proxy.text"
            resourceBundle="org.wso2.carbon.discovery.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.discovery.ui.i18n.JSResources"
            request="<%=request%>"
            i18nObjectName="wsdi18n"/>

    <jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

    <script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>
    <script type="text/javascript" src="../yui/build/event/event-min.js"></script>
    <script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
    <script type="text/javascript" src="../resources/js/resource_util.js"></script>
    <script type="text/javascript" src="../yui/build/utilities/utilities.js"></script>
    <script type="text/javascript" src="../ajax/js/prototype.js"></script>

    <script type="text/javascript">
        function cancelOpearation() {
            window.location.href = 'index.jsp?ordinal=1';
        }

        function addProxy() {
            var name = document.getElementById('proxy_name').value;
            if (name == null || name == '') {
                CARBON.showErrorDialog(wsdi18n['wsd.specify.proxy.name']);
                return false;
            }

            var illegalChars = /\W/;
            if (illegalChars.test(name)) {
                CARBON.showErrorDialog(wsdi18n['wsd.illegal.proxy.name']);
                return false;
            }

            var url = document.getElementById('proxy_url').value;
            if (url == null || url == '') {
                CARBON.showErrorDialog(wsdi18n['wsd.specify.proxy.url']);
                return false;
            }

            proxy_form.action = "add_proxy.jsp";
            proxy_form.submit();
            return true;
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
        boolean submitted = Boolean.valueOf(request.getParameter("formSubmitted"));
        boolean editMode;
        String proxyName, proxyURL = null, policy = null;

        if (submitted) {
            proxyName = request.getParameter("proxyName");
            proxyURL = request.getParameter("proxyURL");
            policy = request.getParameter("wsSecPolicyKeyID");

            editMode = Boolean.valueOf(request.getParameter("editMode"));

            if (proxyName == null || "".equals(proxyName)) {
    %>
        <script type="text/javascript">
            CARBON.showErrorDialog(wsdi18n['wsd.specify.proxy.name']);
        </script>
    <%
            } else if (proxyURL == null || "".equals(proxyURL)) {
    %>
        <script type="text/javascript">
            CARBON.showErrorDialog(wsdi18n['wsd.specify.proxy.url']);
        </script>
    <%
            } else if (!isURLValid(proxyURL)) {
    %>
        <script type="text/javascript">
            CARBON.showErrorDialog(wsdi18n['wsd.illegal.proxy.url']);
        </script>
    <%
            } else {

                try {
                    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
                    ConfigurationContext configContext =
                            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
                    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
                    DiscoveryAdminClient client = new DiscoveryAdminClient(
                            configContext, backendServerURL, cookie, request.getLocale());
                    if (editMode) {
                        client.updateDiscoveryProxy(proxyName, proxyURL, policy);
                    } else {
                        client.addDiscoveryProxy(proxyName, proxyURL, policy);
                    }
                    session.setAttribute("discoveryInfo", "wsd.proxy.save.success");
    %>
        <script type="text/javascript">
            window.location.href = 'index.jsp?region=region1&item=ws_discovery_menu';
        </script>
    <%
                } catch (Exception e) {
                    // The exception could be due to a backend server issue
                    // or an input error (eg: adding an already existing proxy)
                    // So we need to display an error message and try to proceed
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
            }

        } else {
            editMode = request.getParameter("proxy") != null;
            if (editMode) {
                proxyName = request.getParameter("proxy");
                try {
                    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
                    ConfigurationContext configContext =
                            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
                    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
                    DiscoveryAdminClient client = new DiscoveryAdminClient(
                            configContext, backendServerURL, cookie, request.getLocale());
                    DiscoveryProxyDetails pd = client.getDiscoveryProxy(proxyName);
                    if (pd == null) {
                        throw new Exception("The discovery proxy named " + proxyName + " does not exist");                        
                    }
                    proxyURL = pd.getUrl();
                    if (pd.getPolicy() != null) {
                        policy = pd.getPolicy();
                    } else {
                        policy = "";
                    }

                } catch (Exception e) {
                    // Backend server cannot be contacted
                    // Show error page
                    CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
    %>
        <script type="text/javascript">
               location.href = "../admin/error.jsp";
        </script>
    <%
                }
            } else {
                proxyName = "";
                proxyURL = "";
                policy = "";
            }
        }
    %>

    <div id="middle">
        <h2><fmt:message key="wsd.config.proxy"/></h2>
        <div id="workArea">
            <p>
                <fmt:message key="wsd.add.proxy.intro"/>
            </p>
            <p>&nbsp;</p>
            <form id="proxy_form" action="" method="POST">
                <input type="hidden" name="formSubmitted" value="true"/>
                <input type="hidden" name="editMode" value="<%=editMode%>"/>

                <table class="styledLeft">
                    <thead>
                        <tr>
                            <th colspan="2"><fmt:message key="wsd.proxy.settings"/></th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td><fmt:message key="wsd.proxy.name"/> <span class="required">*</span></td>
                            <td><input id="proxy_name" type="text" name="proxyName" size="40" value="<%=proxyName%>"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="wsd.proxy.url"/> <span class="required">*</span></td>
                            <td><input id="proxy_url" type="text" name="proxyURL" size="60" value="<%=proxyURL%>"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="wsd.sec.policy"/></td>
                            <td style="padding-left:0px !important">
                                <table class="normal">
                                    <tr>
                                        <td style="padding-left:5px !important">
                                            <input type="text" id="wsSecPolicyKeyID"
                                                   name="wsSecPolicyKeyID" size="60"
                                                   value="<%=policy%>" readonly="true"/>
                                        </td>
                                        <td>
                                            <a href="#"
                                               class="registry-picker-icon-link"
                                               style="padding-left:20px"
                                               onclick="showResourceTree('wsSecPolicyKeyID', '/_system/config');"><fmt:message key="wsd.browse"/></a>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2" class="buttonRow">
                                <button class="button" onclick="addProxy(); return false;"><fmt:message key="wsd.save"/></button>
                                <button class="button" onclick="cancelOpearation(); return false;"><fmt:message key="wsd.cancel"/></button>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </form>
        </div>
    </div>

    <%
        if (editMode) {
    %>
        <script type="text/javascript">
            document.getElementById('proxy_name').readOnly = 'true';
        </script>
    <%
        }
    %>

</fmt:bundle>