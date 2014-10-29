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
<%@ page import="org.wso2.carbon.logging.admin.ui.LoggingAdminClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.logging.admin.stub.types.carbon.SyslogData" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<!-- This page is included to display messages which are set to request scope or session scope -->
<jsp:include page="../dialog/display_messages.jsp"/>
<fmt:bundle basename="org.wso2.carbon.logging.admin.ui.i18n.Resources">
<%
	String backendServerURL = CarbonUIUtil.getServerURL(
				config.getServletContext(), session);
		ConfigurationContext configContext = (ConfigurationContext) config
				.getServletContext().getAttribute(
						CarbonConstants.CONFIGURATION_CONTEXT);

		String cookie = (String) session
				.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
		LoggingAdminClient client;
		SyslogData syslogData;
		String syslogURL = "";
		String syslogPort = "";
		String realm = "";
		String userName = "";
		String password = "";
		boolean isStratosApp = false;
		try {
			client = new LoggingAdminClient(cookie, backendServerURL,
					configContext);
			syslogData = client.getSysLogData();
			isStratosApp = client.isStratosService();
			syslogURL = syslogData.getUrl();
			syslogPort = syslogData.getPort();
			realm = syslogData.getRealm();
			userName = syslogData.getUserName();
			password = syslogData.getPassword();
		} catch (Exception e) {
%>
<jsp:forward page="../admin/error.jsp?<%=e.getMessage()%>"/>
<%
	return;
		}
    if (isStratosApp) {	
%>
<table>
    <tr class="formRow">
        <td width="10px">
            <input checked="true" value="true" id="persistLogId" tabindex="1"
                   type="checkbox" name="persist"/>
        </td>
        <td><fmt:message key="persist.all.configuration.changes"/></td>
    </tr>
</table>
<p>&nbsp;</p>
<table id="globalLogConfigTbl" class="styledLeft">
    <thead>
    <tr>
        <th><fmt:message key="syslog.porperty.configuration"/></th>
    </tr>
    </thead>
		<tr>
			<td class="formRow">
				<table class="normal">
					<tr>
						<td><fmt:message key="syslog.log.url" /><font
                            color="red">*</font></td>
						<td><input value="<%=syslogURL%>" size="50" id="syslogURL"
							tabindex="2" type="text" name="syslogURL" onfocus="areaOnFocus('syslogURL', 'eg :- http://127.0.0.1/logs/')" onblur="areaOnBlur('syslogURL', 'eg :- http://127.0.0.1/logs/')"/></td>
					</tr>
					<tr>
						<td><fmt:message key="port" /><font
                            color="red">*</font></td>
						<td><input value="<%=syslogPort%>" size="50" id="syslogPort"
							tabindex="2" type="text" name="syslogPort" onfocus="areaOnFocus('syslogPort', 'eg :- 80')" onblur="areaOnBlur('syslogPort', 'eg :- 80')" /></td>
					</tr>
					<tr>
						<td><fmt:message key="realm" /><font
                            color="red">*</font></td>
						<td><input value="<%=realm%>" size="50" id="realm"
							tabindex="2" type="text" name="realm" onfocus="areaOnFocus('realm', 'eg :- Stratos')" onblur="areaOnBlur('realm', 'eg :- Stratos')" /></td>
					</tr>
					<tr>
						<td><fmt:message key="user.name" /></td>
						<td><input value="<%=userName%>" size="50" id="userName"
							tabindex="2" type="text" name="userName" /></td>
					</tr>
					<tr>
						<td><fmt:message key="password" /></td>
						<td><input value="<%=password%>" size="50" id="password"
							tabindex="2" type="password" name="password" /></td>
					</tr>
				</table></td>
		</tr>

		<tr>
        <td class="buttonRow">
            <input id="syslogUpdate"
                   onclick="showConfirmationDialogBox('<fmt:message key="syslog.log.update.confirm"/>', syslogUpdateConfig);return false;"
                   value="<fmt:message key="update"/>"
                   tabindex="4"
                   type="button" name="updateSyslog" class="button"/>

            &nbsp;
<!--             <input id="restoreGlobalConfig" -->
<%--                    onclick="showConfirmationDialogBox('<fmt:message key="global.log.restore.confirm"/>', restoreLog4jConfigToDefaults);return false;" --%>
<%--                    value="<fmt:message key="restore.defaults"/>" --%>
<!--                    tabindex="8" -->
<!--                    type="button" name="restoreDefaults" class="button"/> -->

<!--         </td> -->
    </tr>
</table>
<%
		}
%>
</fmt:bundle>