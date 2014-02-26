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
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.transport.jms.stub.types.carbon.TransportParameter" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@page import="org.wso2.carbon.transport.jms.ui.JMSTransportAdminClient"%>
<%@ page import="java.util.Map" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<script type="text/javascript" src="global-params.js"></script>

<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="../carbon/admin/js/main.js"></script>

<%
String backendServerURL;
ConfigurationContext configContext;
String cookie;
JMSTransportAdminClient client;
List<TransportParameter> transportInDataList;
List<TransportParameter> transportOutDataList;
TransportParameter[] transportInData;
TransportParameter[] transportOutData;
Map<String, String[]> map;
TransportParameter param;
String serviceName;

backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
configContext = (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
client = new JMSTransportAdminClient(cookie, backendServerURL,configContext);
	
map = request.getParameterMap();
serviceName = request.getParameter("_service");

transportInDataList = new ArrayList<TransportParameter>();
transportOutDataList = new ArrayList<TransportParameter>();

boolean updateSender = false;
final String TRANSPORT_SENDER_SUFFIX = "_trpSender";

if (map.containsKey(TRANSPORT_SENDER_SUFFIX)) {
    updateSender = true;
}
	
for (String key : map.keySet()) {
    if (TRANSPORT_SENDER_SUFFIX.equals(key)) {
        continue;
    }

	if (key.startsWith("ConFacName_")) {
        String connFacName = map.get(key)[0];
        String initial = map.get("initial_" + connFacName)[0];
        String url = map.get("url_" + connFacName)[0];
        String jndi = map.get("jndi_" + connFacName)[0];
        param = new TransportParameter();
        param.setName(connFacName);
        String paramValue = "<parameter name=\""+ connFacName + "\">\n" +
                "<parameter name=\"java.naming.factory.initial\">" + initial +"</parameter>\n" +
                "<parameter name=\"java.naming.provider.url\">" + url +"</parameter>\n" +
                "<parameter name=\"transport.jms.ConnectionFactoryJNDIName\">" + jndi +"</parameter>\n" +
            "</parameter>";
        param.setValue(paramValue);
        param.setParamElement(paramValue);

        if (updateSender) {
            transportOutDataList.add(param);
        } else {
            transportInDataList.add(param);
        }
    }
}
	
transportInData = transportInDataList.toArray(new TransportParameter[transportInDataList.size()]);
transportOutData = transportOutDataList.toArray(new TransportParameter[transportOutDataList.size()]);
	
try {
	if (serviceName!=null){
        if (!updateSender) {
            client.updateServiceSpecificInParameters(serviceName,transportInData);
        } else {
            client.updateServiceSpecificOutParameters(serviceName, transportOutData);
        }
%>
	<script>
	    location.href = '../transport-mgt/service_transport.jsp?serviceName=<%=serviceName%>&ordinal=2';
	</script>
<%
	}
	else {
        if (updateSender) {
            client.updateGloballyDefineOutParameters(transportOutData);
        } else {
            client.updateGloballyDefinedInParameters(transportInData);
        }
%>
	<script>
	    location.href = '../transport-mgt/index.jsp';
	</script>
<%
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