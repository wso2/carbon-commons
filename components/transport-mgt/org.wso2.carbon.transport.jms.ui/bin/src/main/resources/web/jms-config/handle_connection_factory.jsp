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
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.transport.jms.stub.types.carbon.TransportParameter" %>
<%@page import="org.wso2.carbon.transport.jms.ui.JMSTransportAdminClient"%>
<%@ page import="java.util.Map" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.HashSet" %>

<script type="text/javascript" src="global-params.js"></script>
<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="../carbon/admin/js/main.js"></script>

<%
    String backendServerURL;
    ConfigurationContext configContext;
    String cookie;
    JMSTransportAdminClient client;
    String serviceName;
    String backLocation;
    Map<String,String[]> paramsMap = request.getParameterMap();
    Map<String,String> factoryMap = new HashMap<String, String>();

    backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    configContext = (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    client = new JMSTransportAdminClient(cookie, backendServerURL,configContext);

    boolean update = false;
    boolean sender = false;
    boolean delete = false;

    if ("true".equals(request.getParameter("_trpSender"))) {
        sender = true;
    }

    if ("true".equals(request.getParameter("_update"))) {
        update = true;
    }

    if ("true".equals(request.getParameter("_deleteFac"))) {
        delete = true;
    }

    String connFacName = request.getParameter("conn.fac.name");
    if (delete) {
        connFacName = request.getParameter("_oriFacName");    
    }

    serviceName = request.getParameter("_service");
    backLocation = request.getParameter("_backLocation");

    Set<String> reservedNames = new HashSet<String>();
    reservedNames.add("_trpSender");
    reservedNames.add("_update");
    reservedNames.add("conn.fac.name");
    reservedNames.add("_service");
    reservedNames.add("_backLocation");
    reservedNames.add("_oriFacName");
    reservedNames.add("_deleteFac");

    for (String key : paramsMap.keySet()) {
        if (reservedNames.contains(key)) {
            continue;
        }
        factoryMap.put(key, paramsMap.get(key)[0].trim());
    }

    try {
        if (delete) {
            // We are deleting an existing parameter
            client.removeConnectionFactory(connFacName, serviceName, !sender);
        } else if (!update) {
            // We are adding a new parameter
            client.addConnectionFactory(connFacName, serviceName, factoryMap, !sender);
        } else {
            // We are updating an existing parameter
            client.updateConnectionFactory(connFacName, serviceName, factoryMap, !sender);
        }
        %>

    <script type="text/javascript">
        window.location.href = '<%=backLocation%>';
    </script>
<%
    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
%>
	<script type="text/javascript">
		 window.location.href = "../admin/error.jsp";
	</script>
<%
	return;
    }
%>