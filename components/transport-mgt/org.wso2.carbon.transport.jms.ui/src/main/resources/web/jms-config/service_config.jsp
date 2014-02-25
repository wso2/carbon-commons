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
<%@ page import="org.wso2.carbon.transport.jms.stub.types.carbon.TransportParameter" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@page import="org.wso2.carbon.transport.jms.ui.JMSTransportAdminClient"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<script type="text/javascript" src="global-params.js"></script>

<%
String backendServerURL;
ConfigurationContext configContext;
String cookie;
JMSTransportAdminClient client = null;
TransportParameter[] transportInData =null;
TransportParameter[] transportOutData;
String serviceName = null;
boolean showUpdateButton = false;

serviceName = request.getParameter("serviceName");
backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
configContext = (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
client = new JMSTransportAdminClient(cookie, backendServerURL,configContext);

try {
	transportInData = client.getServiceSpecificInParameters(serviceName);
    transportOutData = client.getServiceSpecificOutParameters(serviceName);
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

<fmt:bundle basename="org.wso2.carbon.transport.jms.ui.i18n.Resources">
<carbon:breadcrumb 
		label="jms"
		resourceBundle="org.wso2.carbon.transport.jms.ui.i18n.Resources"
		topPage="false" 
		request="<%=request%>" />
		
<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="../carbon/admin/js/main.js"></script>

<div id="middle">
    <h2 id="listTransport"><fmt:message key="transport.mgmt"/></h2>
    <div id="workArea">
       <form action="update_transport.jsp">                       
           <input type="hidden" name="_service" value="<%=serviceName%>" />
           <table class="styledLeft" id="jmsTransport" width="100%">             
                <%
                if (transportInData != null && transportInData.length>0) {
                    showUpdateButton = true;
                %>      
		              <tr>
			             <td colspan="2" style="border-left: 0px !important; border-right: 0px !important; padding-left: 0px !important;">
			                   <h5> <fmt:message key="transport.protocol"/>: <strong>jms</strong></h5>			            
			             </td>			              
		              </tr>

		              <tr>
                          <td colspan="2"><strong><fmt:message key="transport.sender"/></strong></td>
                      </tr>
                      <tr>
			              <td class="sub-header"><fmt:message key="transport.parameter.name"/></td>
			              <td class="sub-header"><fmt:message key="transport.parameter.value"/></td>
		              </tr>

	             <% 
	                for (TransportParameter currentParam : transportInData) {
	             %>
	                  <tr>
	                      <td><%=currentParam.getName()%></td>
	                      <td><textarea rows="5" cols="70" name="<%=currentParam.getName()%>"><%=currentParam.getValue()%></textarea>
	                      </td>
	                  </tr>
	                <%} %>

                       <tr>
                           <td colspan="2">&nbsp;</td>
                       </tr>

                   <%} %>

                <%
                if (transportOutData != null && transportOutData.length>0) {
                    showUpdateButton = true;
                %>

		              <tr>
                          <td colspan="2"><strong><fmt:message key="transport.sender"/></strong></td>
                      </tr>
                      <tr>
			              <td class="sub-header"><fmt:message key="transport.parameter.name"/></td>
			              <td class="sub-header"><fmt:message key="transport.parameter.value"/></td>
		              </tr>

	             <%
                    String trpSenderSuffix = "_trpSender";
                    for (TransportParameter currentParam : transportOutData) {
	             %>
	                  <tr>
	                      <td><%=currentParam.getName()%></td>
	                      <td><textarea rows="5" cols="70" name="<%=currentParam.getName()+trpSenderSuffix%>"><%=currentParam.getValue()%></textarea>
	                      </td>
	                  </tr>
	                <%} %>

                   <%} %>
                   <%
                       if (showUpdateButton) {
                   %>
                        <tr>
                          <td colspan="2" class="buttonRow">
                            <input type="submit" value="<fmt:message key="transport.parameter.update"/>" class="button"/>
                            <input class="button" type="reset" value="<fmt:message key="transport.parameter.cancel"/>"  onclick="javascript:document.location.href='../transport-mgt/service_transport.jsp?serviceName=<%=serviceName%>&ordinal=2'"/ >
                          </td>
                        </tr>
                   <%
                       }
                   %>
           </table>
       </form>
    </div>
</div>
</fmt:bundle>