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
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.transport.mgt.ui.TransportAdminClient" %>
<%@page import="org.wso2.carbon.transport.mgt.stub.types.carbon.TransportParameter"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>

<script type="text/javascript" src="global-params.js"></script>
<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="../carbon/admin/js/main.js"></script>

<%
String backendServerURL;
ConfigurationContext configContext;
String cookie;
TransportAdminClient client;
TransportParameter[] transportInData;
TransportParameter[] transportOutData;
Map<String, String[]> map;
TransportParameter param;
List<TransportParameter> transportInDataList;
List<TransportParameter> transportOutDataList;

backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
configContext = (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
client = new TransportAdminClient(cookie, backendServerURL,configContext);

map = request.getParameterMap();
transportInDataList = new ArrayList<TransportParameter>();
transportOutDataList = new ArrayList<TransportParameter>();

String paramValue;

final String TRANSPORT_SENDER_SUFFIX = "_trpSender";
final String TRANSPORT_NAME = "_transport";
final String SERVICE_NAME = "_service";

String transport = null;
String serviceName = null;
boolean updateSender = false;

if (map.containsKey(TRANSPORT_SENDER_SUFFIX)) {
    updateSender = true;
}

if (map.containsKey(TRANSPORT_NAME)) {
    transport = map.get(TRANSPORT_NAME)[0].trim();
}

if (map.containsKey(SERVICE_NAME)) {
    serviceName = map.get(SERVICE_NAME)[0].trim();
}

for (String key : map.keySet()) {
    if (key.endsWith("_chk") || TRANSPORT_SENDER_SUFFIX.equals(key) ||
            TRANSPORT_NAME.equals(key) || SERVICE_NAME.equals(key)) {
        continue;
    }

    paramValue = map.get(key)[0].trim();
    //escaping values submitted by UI before creating xml configuration,
    paramValue = StringEscapeUtils.escapeXml(paramValue);
    param = new TransportParameter();
    param.setName(key);
    param.setValue(paramValue);
    if (paramValue.startsWith("<parameter ") && paramValue.endsWith("</parameter>")) {
        param.setParamElement(paramValue);   
    } else {
        param.setParamElement("<parameter name=\"" + key + "\">" + paramValue + "</parameter>");
    }

    if (updateSender) {
        transportOutDataList.add(param);
    } else {
        transportInDataList.add(param);
    }
}


try {
    if (serviceName == null) {
        if (!updateSender) {
            transportInData = transportInDataList.toArray(new TransportParameter[transportInDataList.size()]);
            client.updateGloballyDefinedInParameters(transport, transportInData);
        } else {
            transportOutData = transportOutDataList.toArray(new TransportParameter[transportOutDataList.size()]);
            client.updateGloballyDefinedOutParameters(transport, transportOutData);
        }

        %>
             <script type="text/javascript">
                    location.href = '../transport-mgt/index.jsp';
            </script>

        <%

    } else {
        if (!updateSender) {
            transportInData = transportInDataList.toArray(new TransportParameter[transportInDataList.size()]);
            client.updateServiceSpecificInParameters(transport, serviceName, transportInData);
        } else {
            transportOutData = transportOutDataList.toArray(new TransportParameter[transportOutDataList.size()]);
            client.updateServiceSpecificOutParameters(transport, serviceName, transportOutData);
        }

        %>
            <script type="text/javascript">
                    location.href = '../transport-mgt/service_transport.jsp?serviceName=<%=serviceName%>';
            </script>
        <%
    }

%>

<%
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