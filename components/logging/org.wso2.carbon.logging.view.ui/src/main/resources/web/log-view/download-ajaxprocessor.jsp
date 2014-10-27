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

<%@page import="java.io.OutputStreamWriter"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.logging.view.ui.LogViewerClient" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.CarbonUtils" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<script type="text/javascript" src="js/logviewer.js"></script>
<script type="text/javascript" src="../admin/dialog/js/dialog.js"></script>
<%
	String backendServerURL = CarbonUIUtil
			.getServerURL(config.getServletContext(), session);
	ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
			.getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
	String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
	LogViewerClient logViewerClient;
	String message = "";
	String tenantDomain ="";
	String serviceName = "WSO2 Stratos Manager";
	String downloadFile = request.getParameter("logFile");
	try {
		out.clear();
		out = pageContext.pushBody();
		out.clearBuffer();
		ServletOutputStream outputStream = response.getOutputStream();
		response.setContentType("application/txt");
		response.setHeader("Content-Disposition", "attachment;filename=" + downloadFile.replaceAll("\\s", "_"));
		logViewerClient = new LogViewerClient(cookie, backendServerURL, configContext);
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
		tenantDomain = request.getParameter("tenantDomain");
		serviceName = request.getParameter("serviceName");
		tenantDomain = (tenantDomain == null) ? "" : tenantDomain;
		serviceName = (serviceName == null) ? "WSO2 Stratos Manager" : serviceName;
		int fileSize = logViewerClient.getLineNumbers(downloadFile) + 1;
	    System.out.println("fileSize "+fileSize);
		int pages = (int) Math.ceil((double) fileSize / 2000);
	
		for (int i = 0; i < pages; i++) {
			int start = (i * 2000) + 1;
			int end = start + 2000;
			String logIndex = Integer.toString(fileSize);
			String logs[] = logViewerClient.getLogLinesFromFile(downloadFile, fileSize,
					start, end);
			for (String logMessage : logs) {
				outputStreamWriter.write(logMessage + "\n");
			}

		}
		outputStreamWriter.flush();
		outputStream.flush();
		out.flush();
        outputStreamWriter.close();
        outputStream.close();
        out.close();
	} catch (Exception e) {
		CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request,
				e);
%>
        <script type="text/javascript">
               location.href = "../admin/error.jsp";
        </script>
<%
	return;
	}
%>
