<%--
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
--%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page import="org.wso2.carbon.logging.admin.ui.LoggingAdminClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.logging.admin.stub.types.carbon.LogData" %>
<%@ page import="org.wso2.carbon.logging.admin.stub.types.carbon.AppenderData" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%!private String[] getAppenderNames(String[] allNames) {
		List<String> resultList = new ArrayList<String>();
		for (String currName : allNames) {
			boolean isAdded = false;
			for (String name : resultList) {
				if (name.equals(currName)) {
					isAdded = true;
				}
			}
			if (!isAdded) {
				resultList.add(currName);
			}
		}
		return resultList.toArray(new String[resultList.size()]);
	}%>
<%
	response.setHeader("Cache-Control", "no-cache");
	String selectedAppenderName = request.getParameter("appenderName");
	String backendServerURL = CarbonUIUtil
			.getServerURL(config.getServletContext(), session);
	ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
			.getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

	String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
	LoggingAdminClient client;
	LogData globalLogData;
	AppenderData appenderData;
	String[] allAppenderNames;
	String[] appenderNames;
	try {
		client = new LoggingAdminClient(cookie, backendServerURL, configContext);
		//Getting appender name list
		//TODO Add the method to the backend service
		globalLogData = client.getSysLog();
		AppenderData[] appenderDataArray = globalLogData.getAppenderData();
		allAppenderNames = new String[appenderDataArray.length];
		for (int i = 0; i < appenderDataArray.length; i++) {
			allAppenderNames[i] = appenderDataArray[i].getName();
		}
		appenderNames = getAppenderNames(allAppenderNames);
		appenderData = client.getAppenderData(selectedAppenderName);
	} catch (Exception e) {
%>
<jsp:forward page="../admin/error.jsp?<%=e.getMessage()%>"/>
<%
	return;
	}
%>
<script type="text/javascript" src="js/loggingadmin.js"></script>
<fmt:bundle basename="org.wso2.carbon.logging.admin.ui.i18n.Resources">

    <table class="styledLeft">
        <thead>
        <tr>
            <th><fmt:message key="configure.log4j.appenders"/></th>
        </tr>
        </thead>
        <tr>
            <td class="formRow">
                <table class="normal">
                    <tr>
                        <td width="10%"><fmt:message key="name"/></td>
                        <td>
                            <select onchange="getAppenderData();return false;" tabindex="5"
                                    id="appenderCombo">
                                <%
                                	String appenderPattern = appenderData.getPattern();
                                		if (appenderData.getIsFileAppender()) {
                                			appenderPattern = client.removeSyslogPattern(appenderPattern);
                                		}
                                		for (String appenderName : appenderNames) {

                                			if (appenderName.equals(appenderData.getName())) {
                                %>
                                <option selected="true" value="<%=appenderName%>"><%=appenderName%>
                                </option>
                                <%
                                	} else {
                                %>
                                <option value="<%=appenderName%>"><%=appenderName%>
                                </option>
                                <%
                                	}
                                		}
                                %>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td width="10%"><fmt:message key="log.pattern"/></td>
                        <td>
                            <input value="<%=appenderPattern%>" size="50"
                                   id="appenderLogPattern"
                                   tabindex="6" type="text"/>
                        </td>
                    </tr>
                    <tr>
                        <td><fmt:message key="threshold"/></td>
                        <td>
                            <select tabindex="7" id="appenderThresholdCombo">
                                <%
                                	String[] logLevels = client.getLogLevels();
                                		String appenderThreshold = appenderData.getThreshold();
                                		for (String logLevelType : logLevels) {
                                			if (logLevelType.equals(appenderThreshold)) {
                                %>
                                <option value="<%=logLevelType%>" selected="true"><%=logLevelType%>
                                </option>
                                <%
                                	} else {
                                %>
                                <option value="<%=logLevelType%>"><%=logLevelType%>
                                </option>
                                <%
                                	}
                                		}
                                %>
                            </select>
                        </td>
                    </tr>
                    <%
                    	if (appenderData.getIsFileAppender()) {
                    %>
                    <tr>
                        <td><fmt:message key="log.file"/></td>
                        <td>
                            <input value="<%=appenderData.getLogFile()%>" size="50"
                                   id="appenderLogFile"
                                   tabindex="8" type="text"/>
                        </td>
                    </tr>
                    <%
                    	} else if (appenderData.getIsSysLogAppender()) {
                    %>
                    <tr>
                        <td><fmt:message key="sys.log.host"/></td>
                        <td>
                            <input value="<%=appenderData.getSysLogHost()%>" size="50"
                                   id="appenderSysLogHost" tabindex="9" type="text"/>
                        </td>
                    </tr>
                    <tr>
                        <td><fmt:message key="facility"/></td>
                        <td>
                            <select tabindex="10" id="appenderFacilityCombo">

                                <%
                                	Map facilityMap = client.getAppenderFacilities();
                                			for (Iterator itr = facilityMap.keySet().iterator(); itr.hasNext();) {
                                				String facilityKey = (String) itr.next();
                                				String facilityValue = (String) facilityMap.get(facilityKey);
                                				if (facilityKey.equals(appenderData.getFacility())) {
                                %>

                                <option value="<%=facilityKey%>" selected="true"><%=facilityValue%>
                                </option>

                                <%
                                	} else {
                                %>

                                <option value="<%=facilityKey%>"><%=facilityValue%>
                                </option>

                                <%
                                	}
                                			}
                                %>
                            </select>
                        </td>
                    </tr>
                    <%
                    	}
                    %>
                </table>
            </td>
        </tr>
        <tr>
            <td class="buttonRow">
                <input value="<fmt:message key="update"/>" tabindex="11" type="button"
                       class="button"
                       id="appenderUpdate"
                       onclick="showConfirmationDialogBox('<fmt:message key="appender.log.update.confirm"/>',updateAppender)"/>
            </td>
        </tr>
    </table>
</fmt:bundle>