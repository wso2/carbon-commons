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
<%@page import="org.wso2.carbon.CarbonConstants"%>
<%@page import="org.apache.axis2.context.ConfigurationContext"%>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="org.wso2.carbon.security.ui.client.SecurityAdminClient" %>
<%
    String forwardTo = null;
    String serviceName = (String) session.getAttribute("serviceName");
    String keyStore = request.getParameter("keyStore");
    String BUNDLE = "org.wso2.carbon.security.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    try {
        String cookie = (String)session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        SecurityAdminClient client = new SecurityAdminClient(cookie, backendServerURL, configContext);
        client.disableSecurityOnService(serviceName);
        //TODO clear session params here
        String message = resourceBundle.getString("security.disable");
        forwardTo = "index.jsp?serviceName="+serviceName;
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.INFO, request);
    } catch (Exception e) {
	    String message = MessageFormat.format(resourceBundle.getString("security.cannot.disable"),
                new Object[]{e.getMessage()});
        
	    forwardTo = "index.jsp?serviceName="+serviceName;
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
    }
%>

<%@page import="java.util.ResourceBundle"%>
<%@page import="java.text.MessageFormat"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>
