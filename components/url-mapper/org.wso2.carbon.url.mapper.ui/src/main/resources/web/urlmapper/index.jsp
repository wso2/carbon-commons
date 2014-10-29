<!--
 ~ Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>
        <%@ page import="org.apache.axis2.context.ConfigurationContext" %>
        <%@ page import="org.wso2.carbon.CarbonConstants" %>
      	<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
		<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
        <%@ page import="org.wso2.carbon.utils.ServerConstants" %>
        <%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
      <script type="text/javascript" src="js/mapping_validator.js"></script>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
        <%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<!--         <script type="text/javascript" src="../admin/dialog/js/dialog.js"></script> -->
        <%@ page import="org.wso2.carbon.utils.CarbonUtils" %>
        <%@ page import="org.wso2.carbon.url.mapper.ui.UrlMapperServiceClient" %>
<%
	String requestType = request.getParameter("type");
	String carbonEndpoint = request.getParameter("carbonEndpoint");
	String appType = request.getParameter("apptype");
	String servletContext = request.getParameter("servletContext");
	String backendServerURL = CarbonUIUtil
			.getServerURL(config.getServletContext(), session);
	ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
			.getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
	String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
	
	UrlMapperServiceClient hostAdmin = new UrlMapperServiceClient(cookie, backendServerURL,
			configContext);
	String hosts[] = null;
	String port= hostAdmin.getHttpPort ();
	String prefix = hostAdmin.getPrefix();
	String referance ="";
	boolean isJaxrs = false;
	String jaxContext = "";
	try {
		if ("service".equalsIgnoreCase(appType)) {
			String urlParts[] = carbonEndpoint.split(":\\d{4}");
			if (urlParts.length>1) {
				referance = urlParts[1];
			}
			hosts = hostAdmin.getHostForEpr(carbonEndpoint);
			
		} else {
		   referance = carbonEndpoint;
		   hosts = hostAdmin.getHostForWebApp(carbonEndpoint);
		}
	} catch (Exception e) {
		CarbonUIMessage.sendCarbonUIMessage(e.getLocalizedMessage(), CarbonUIMessage.ERROR,
				request, e);
%>
<script type="text/javascript">
               location.href = "../admin/error.jsp";
        </script>
<%
	return;
	}
%>

<fmt:bundle basename="org.wso2.carbon.url.mapper.ui.i18n.Resources"> 
	<carbon:breadcrumb label="url.mapping"
		resourceBundle="org.wso2.carbon.url.mapper.ui.i18n.Resources"
		topPage="true" request="<%=request%>" />
		<div id="middle">
		<script type="text/javascript">

			function    showSucessMessage(msg,myepr,inputVal) {
				var failMsg = new RegExp("Failed to");
                var deleteMsg = new RegExp("is successfully deleted");
                var suffix = '<%=hostAdmin.getPrefix()%>';
                if (msg.match(failMsg)) //if match sucess
				{
                    msg += "";
                    CARBON.showErrorDialog(msg);
				} else if(msg.match(deleteMsg)) {
                    msg = inputVal + " " + msg;
                    CARBON.showInfoDialog(msg, function(){
                    document.location.href = "index.jsp?&carbonEndpoint=" + myepr + "&apptype=<%=appType%>&servletContext=<%=servletContext%>";
                    });
                } else {
                    msg = inputVal + suffix + " " + msg;
                    CARBON.showInfoDialog(msg, function(){
                    document.location.href = "index.jsp?&carbonEndpoint=" + myepr + "&apptype=<%=appType%>&servletContext=<%=servletContext%>";
                    });
                }
			}
		</script>
		<script type="text/javascript">
		  
   function add(myepr){
        CARBON.showInputDialog("Enter URL Mapping name :\n",function(inputVal){
            var reason = checkMappingAvailability(inputVal);
            if(reason == "") {
                jQuery.ajax({
                                type: "POST",
                                url: "contextMapper_ajaxprocessor.jsp",
                                data: "type=add&carbonEndpoint=" + myepr + "&userEndpoint=" + inputVal +
                                            "&endpointType=Endpoint_1&apptype=<%=appType%>&servletContext=<%=servletContext%>",
                                success: function(msg){
                                    showSucessMessage(msg,myepr,inputVal);
                                }
                            });
            } else {
                CARBON.showWarningDialog(reason);
            }
        });
    }   
</script>

 <script type="text/javascript">
   function edit(myepr,host){
        CARBON.showInputDialog("The Mapping you are editing is: " + host + "\n",function(inputVal){
        var reason = checkMappingAvailability(inputVal);
        if(reason == "") {
            jQuery.ajax({
                            type: "POST",
                            url: "contextMapper_ajaxprocessor.jsp",
                            data: "type=edit&carbonEndpoint=" + myepr + "&userEndpoint=" + inputVal +
    "&oldHost=" + host + "&endpointType=Endpoint_1&apptype=<%=appType%>&servletContext=<%=servletContext%>",
                            success: function(msg){
                            	showSucessMessage(msg,myepr,inputVal);
                            }
                        });
        } else {
            CARBON.showWarningDialog(reason);
        }
        });
    }   
</script> 
 <script type="text/javascript">
   function deleteHost(myepr,host){
	   CARBON.showConfirmationDialog('<fmt:message key="select.webapps.to.be.deleted"/>' + " " + host + "?",function(){
            jQuery.ajax({
                            type: "POST",
                            url: "contextMapper_ajaxprocessor.jsp",
                            data: "type=delete&carbonEndpoint=" + myepr +"&userEndpoint=" + host
    + "&endpointType=Endpoint_1&apptype=<%=appType%>&servletContext=<%=servletContext%>",
                            success: function(msg){
                            	showSucessMessage(msg,myepr,host);
                            }
                        });
        });
    }   
</script> 

		
		<h2>
			<fmt:message key="url.mapping" />
		</h2>
		<div id="workArea">
		<b><fmt:message
					key="url.mapping.for" /> <%=referance%>
			</b><br/><br/>
                <%
                if (hosts == null || hosts.length == 0) {
                %>
                    <fmt:message key="no.mappings.found" />
                <%
                } else {
                %>
                    <table class="styledLeft">
                        <thead>
                            <tr>
                                <th><b><fmt:message key="host.name" /> </b></th>
                            <% if("service".equalsIgnoreCase(appType)) {
                            %>
                                <th colspan="5"><b><fmt:message key="action" /> </b></th>
                            <% } else { %>
                                 <th colspan="3"><b><fmt:message key="action" /> </b></th>
                            <%}%>
                            </tr>
                        </thead>
                    <%
                    int index = -1;
                    for (String host : hosts) {
                        ++index;
                        if (index % 2 != 0) {
                    %>
                        <tr>
                            <%
                                } else {
                            %>
                            <tr bgcolor="#eeeffb">
                                <%
                                }
                                if (hosts == null || hosts.length == 0) {
                                    %>
                                    <td colspan="3"><fmt:message key="no.mappings.found" /></td>
                                    <%
                                } else {
                                    if("jaxWebapp".equalsIgnoreCase(appType)) {

                                        String url = "http://"+host+":"+port + servletContext;
                                        %>
                                        <td>
                                            <%=host%>
                                        </td>
                                        <td>
                                            <a href="<%=url%>" target="_blank"
                                            style='background:url(images/goto_url.gif) no-repeat;padding-left:20px;display:block;white-space: nowrap;height:16px;'>
                                                Find Services
                                            </a>
                                        </td>
                                        <%
                                    } else if("service".equalsIgnoreCase(appType)) {
                                        String url = "http://"+host+":" + port + "/";
                                        %>
                                        <td>
                                            <%=host%>
                                        </td>
                                        <td>
                                            <a href="<%=url + "?wsdl"%>" class="icon-link" target="_blank" style="background-image:url(images/wsdl.gif);" >
                                                <fmt:message key="wsdl.one"/>
                                            </a>
                                        </td>
                                        <td>
                                            <a href="<%=url + "?wsdl2"%>" class="icon-link" target="_blank" style="background-image:url(images/wsdl.gif);" >
                                                <fmt:message key="wsdl.two"/>
                                            </a>
                                        </td>
                                        <td>
                                            <a href="<%=url + "?tryit"%>" class="icon-link" target="_blank" style="background-image:url(images/tryit.gif);" >
                                                <fmt:message key="try.this.service"/>
                                            </a>
                                        </td>
                                        <%
                                    } else if("jaggeryWebapp".equalsIgnoreCase(appType) || "webapp".equalsIgnoreCase(appType)) {
                                        String url = "http://" + host + ":" + port + "/";
                                        %>
                                        <td>
                                            <%=host%>
                                        </td>
                                        <td>
                                            <a href="<%=url%>" target="_blank"
                                                style='background:url(images/goto_url.gif) no-repeat;padding-left:20px;display:block;white-space: nowrap;height:16px;'>
                                                <fmt:message key="go.to.url"/>
                                            </a>
                                        </td>
                                        <%
                                    }


                                %>

                                <td>
                                    <a class="icon-link"
                                    style="background-image: url(images/edit.gif);"
                                    onclick="edit('<%=carbonEndpoint%>','<%=host%>');"
                                    title="Edit"><fmt:message key="edit" /></a>
                                </td>
                                <td>
                                    <a class="icon-link"
                                    style="background-image: url(images/delete.gif);"
                                    onclick="deleteHost('<%=carbonEndpoint%>','<%=host%>');"
                                    title="Delete"><fmt:message key="delete" /></a>
                                </td>
                                <%
                                }
                                %>
                            </tr>
                <%
                    }
                }
                %>

        <%
            if(!hostAdmin.isMappingLimitExceeded(carbonEndpoint)) {
        %>
            <tr>
                <td td colspan="2">
                    <a class="icon-link"
                    style="background-image:url(images/add.gif);" onclick="add('<%=carbonEndpoint%>');" title="Add Service Specific Url">
                    Add New Mapping
                    </a>
                </td>
            </tr>
        <%
            }
        %>

             </table>
		
		 </div></div>
</fmt:bundle>