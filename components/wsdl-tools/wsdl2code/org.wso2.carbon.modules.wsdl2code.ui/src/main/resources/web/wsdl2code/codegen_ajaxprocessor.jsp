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

<%@page import="org.apache.axis2.context.ConfigurationContext" %>
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.wsdl2code.ui.WSDL2CodeClient" %>
<%
    WSDL2CodeClient wsdl2CodeClient;
    String codegenOptions = CharacterEncoder.getSafeText(request.getParameter("optionsString"));
    String type= CharacterEncoder.getSafeText(request.getParameter("type"));

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(
                    CarbonConstants.CONFIGURATION_CONTEXT);

    out.clear();
    out = pageContext.pushBody();
    out.clearBuffer();
    
    try {
        wsdl2CodeClient = new WSDL2CodeClient(configContext, backendServerURL, cookie);

        String[] options = codegenOptions.split(",");

        String workDir = (String) configContext.getProperty(ServerConstants.WORK_DIR);
        for(int i=0; i < options.length ; i++) {
            if ( "-uri".equals(options[i]) && i+1 < options.length ) {
                if (options[i+1] != null && options[i+1].startsWith("/extra/")) {
                    options[i+1] = workDir + options[i+1];
                }
            }
        }

        if(type!=null && type.equalsIgnoreCase("cxf")){
            wsdl2CodeClient.codeGenForCXF(options,response);
        }else{
            wsdl2CodeClient.codeGen(options, response);
        }
        out.close();
    } catch (Exception e) {
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
        response.setStatus(302);
        response.setHeader("Location", "../admin/error.jsp");

%>
<script type="text/javascript">
    location.href = "../admin/error.jsp";
</script>
<%
    }
%>
