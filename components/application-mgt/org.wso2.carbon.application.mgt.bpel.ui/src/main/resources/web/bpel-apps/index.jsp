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
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.application.mgt.bpel.stub.types.carbon.BPELAppMetadata" %>
<%@ page import="org.wso2.carbon.application.mgt.bpel.ui.BPELAppAdminClient" %>
<%@ page import="org.wso2.carbon.application.mgt.bpel.stub.types.carbon.PackageMetadata" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<!-- This page is included to display messages which are set to request scope or session scope -->
<jsp:include page="../dialog/display_messages.jsp"/>

<%
    String appName = (String) request.getAttribute("appName");

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext()
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    String BUNDLE = "org.wso2.carbon.application.mgt.bpel.ui.i18n.Resources";
    ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    BPELAppMetadata bpelMetadata = null;

    try {
        BPELAppAdminClient client = new BPELAppAdminClient(cookie,
                backendServerURL, configContext, request.getLocale());
        bpelMetadata = client.getBPELAppData(appName);
    } catch (Exception e) {
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
    }

%>

<fmt:bundle basename="org.wso2.carbon.application.mgt.bpel.ui.i18n.Resources">
<%
    if (bpelMetadata != null) {
        PackageMetadata[] packages = bpelMetadata.getPackages();
        if (packages != null && packages.length > 0) {

%>
    <p>&nbsp;&nbsp;</p>
    <table class="styledLeft" id="bpelTable" width="60%">
        <thead>
        <tr>
            <th width="50%"><img src="../bpel/images/bpel-packages.gif" alt="" style="vertical-align:middle;">&nbsp;<fmt:message key="carbonapps.bpel.packages"/></th>
            <th><img src="../bpel/images/bpel-process.gif" alt="" style="vertical-align:middle;">&nbsp;<fmt:message key="carbonapps.bpel.processes"/></th>
        </tr>
        </thead>
        <tbody>
        <%
            for (PackageMetadata packageMetadata : packages) {
        %>
        <tr>
            <td rowspan="<%= packageMetadata.getProcessList().length%>">
                <%= packageMetadata.getPackageName()%>
            </td>
        <%
                int processCount = 0;
                for (String process : packageMetadata.getProcessList()) {
                     if (processCount != 0) {
        %>
        <tr>
        <%
                     }
        %>
            <td><a href="../bpel/process_view.jsp?pid=<%= process%>"><%= process%></a></td>

        <%           if (processCount != 0) {%>

        </tr>
        <%
                     }
                     processCount++;
                }
            }
        %>
        </tbody>
    </table>
<%
        }
    }
%>

</fmt:bundle>
