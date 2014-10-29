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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@page import="org.wso2.carbon.transport.mgt.ui.TransportAdminClient"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<script type="text/javascript" src="global-params.js"></script>
<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="../carbon/admin/js/main.js"></script>


<%
String backendServerURL;
ConfigurationContext configContext;
String cookie;
TransportAdminClient client = null;
String serviceName = null;
String transport = null;

backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
configContext = (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
client = new TransportAdminClient(cookie, backendServerURL,configContext);

serviceName = request.getParameter("serviceName");
transport = request.getParameter("transport");
	
try	{
	 client.removeExposedTransports(serviceName,transport);
%>
	 <script>
          location.href = 'service_transport.jsp?serviceName=<%=serviceName%>';
     </script>
<%

}catch (Exception e) {
      CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
%>
	  <script type="text/javascript">
	    location.href = "../admin/error.jsp";
	  </script>
<%
	return;
}
%>