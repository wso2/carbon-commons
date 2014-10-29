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
<%@ page import="org.wso2.carbon.application.mgt.ui.ApplicationAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<!-- This page is included to display messages which are set to request scope or session scope -->
<jsp:include page="../dialog/display_messages.jsp"/>

<%
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    String BUNDLE = "org.wso2.carbon.application.mgt.ui.i18n.Resources";
    ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    String[] appList = null;
    ApplicationAdminClient client = null;

    try {
        client = new ApplicationAdminClient(cookie,
                                            backendServerURL, configContext, request.getLocale());
        appList = client.getAllApps();
    } catch (Exception e) {
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
    }

%>

<fmt:bundle basename="org.wso2.carbon.application.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="carbonapps.list.headertext"
                       resourceBundle="org.wso2.carbon.application.mgt.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>
    <carbon:jsi18n
		resourceBundle="org.wso2.carbon.application.mgt.ui.i18n.JSResources"
		request="<%=request%>" />

<script type="text/javascript">

    function deleteApplication(appName) {
        CARBON.showConfirmationDialog("<fmt:message key="confirm.delete.app"/>" , function(){
            document.applicationsForm.action = "delete_artifact.jsp?appName=" + appName;
            document.applicationsForm.submit();
        });
    }

    function restartServerCallback() {
        var url = "../server-admin/proxy_ajaxprocessor.jsp?action=restart";
        jQuery.noConflict();
        jQuery("#output").load(url, null, function (responseText, status, XMLHttpRequest) {
            if (jQuery.trim(responseText) != '') {
                CARBON.showWarningDialog(responseText);
                return;
            }
            if (status != "success") {
                CARBON.showErrorDialog(jsi18n["restart.error"]);
            } else {
                CARBON.showInfoDialog(jsi18n["restart.in.progress.message"]);
            }
        });
    }

    function restartServer() {
        jQuery(document).ready(function() {
            CARBON.showConfirmationDialog(jsi18n["restart.message"], restartServerCallback, null);
        });
    }

</script>

    <div id="middle">
        <h2><fmt:message key="carbonapps.list.headertext"/></h2>

        <div id="workArea">
            <form action="" name="applicationsForm" method="post">
                <%
                    if (appList != null && appList.length > 0) {
                            String appVersion = null;
                %>
                <table class="styledLeft" id="appTable" width="100%">
                    <thead>
                    <tr>
                        <th><fmt:message key="carbonapps.applications"/></th>
                        <th><fmt:message key="carbonapps.version"/></th>
                        <th colspan="2"><fmt:message key="carbonapps.actions"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <%
                        for (String appName : appList) {
                            appVersion = client.getAppData(appName).getAppVersion();
                    %>
                    <tr>
                        <td><a href="./application_info.jsp?appName=<%= appName%>"><%= appName%></a></td>
                        <%
                            if (appVersion != null) {
                        %>
                        <td>
                            <%=appVersion%>
                        </td>
                        <%
                            }
                        %>
                        <td><a href="#" class="icon-link-nofloat" style="background-image:url(images/delete.gif);" onclick="deleteApplication('<%= appName%>');" title="<%= bundle.getString("carbonapps.delete.this.row")%>"><%= bundle.getString("carbonapps.delete")%></a></td>
                        <td><a href="download-ajaxprocessor.jsp?cappName=<%= appName%>" class="icon-link-nofloat" style="background-image:url(images/download.gif);" title="<%= bundle.getString("download.capp")%>"><%= bundle.getString("download")%></a></td>
                    </tr>
                    <%
                        }
                    %>
                    </tbody>
                </table>
                <%
                } else {
                %>
                <label><fmt:message key="carbonapps.no.apps"/></label>
                <%
                    }
                %>
            </form>
        </div>
    </div>

    <%--<%--%>
        <%--if (request.getParameter("restart") != null && request.getParameter("restart").equals("true")) {--%>
    <%--%>--%>
    <%--<script type="text/javascript">--%>
        <%--restartServer();--%>
    <%--</script>--%>
    <%--<%--%>
        <%--}--%>
    <%--%>--%>

</fmt:bundle>