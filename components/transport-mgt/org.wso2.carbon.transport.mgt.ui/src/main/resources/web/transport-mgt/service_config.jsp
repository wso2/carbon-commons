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
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>

<script type="text/javascript" src="global-params.js"></script>

<%
    String backendServerURL;
    ConfigurationContext configContext;
    String cookie;
    TransportAdminClient client;
    TransportParameter[] transportInData;
    String transport = request.getParameter("transport");
    String service = request.getParameter("serviceName");

    backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    configContext = (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    client = new TransportAdminClient(cookie, backendServerURL,configContext);

    try {
        transportInData = client.getServiceSpecificInParameters(transport, service);
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

<fmt:bundle basename="org.wso2.carbon.transport.mgt.ui.i18n.Resources">
<carbon:breadcrumb
        label="Update Listener"
        resourceBundle="org.wso2.carbon.transport.mgt.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>" />

<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="../carbon/admin/js/main.js"></script>

<div id="middle">
<h2 id="listTransport"><fmt:message key="transport.mgmt"/></h2>
<div id="workArea">

<script type="text/javascript">
    function setType(chk,hidden) {
        var val = document.getElementById(chk).checked;
        var hiddenElement = document.getElementById(hidden);

        if (val){
            hiddenElement.value="true";
        }else {
            hiddenElement.value="false";
        }
    }

    function addNewParameter() {
        var inputBox = document.getElementById("addNewParameter");
        var paramName = inputBox.value;
        if (paramName == "") {
            CARBON.showWarningDialog("<fmt:message key='transport.add.empty.param'/>");
            return;
        }

        var paramElements = document.getElementsByName(paramName);
        if (paramElements.length > 0) {
            CARBON.showWarningDialog("<fmt:message key='transport.add.duplicate.param'/>");
            return;
        }

        var tabBody=document.getElementById("paramTableBody");
        var row=document.createElement("TR");
        var cell1 = document.createElement("TD");
        var cell2 = document.createElement("TD");
        var textnode1=document.createTextNode(paramName);
        var textArea=document.createElement("TEXTAREA");
        textArea.setAttribute("rows", "3");
        textArea.setAttribute("cols", "60");
        textArea.setAttribute("name", paramName);
        textArea.appendChild(document.createTextNode("Edit Parameter Value"));

        var delButton = document.createElement("A");
        delButton.setAttribute('onclick', 'deleteRow(this)');
        delButton.appendChild(document.createTextNode('Remove'));
        delButton.setAttribute('href', '');

        cell1.appendChild(textnode1);
        cell2.appendChild(textArea);
        cell2.appendChild(delButton);
        row.appendChild(cell1);
        row.appendChild(cell2);
        tabBody.appendChild(row);
        inputBox.value = "";
    }

    function deleteRow(r) {
        var i = r.parentNode.parentNode.rowIndex;
        document.getElementById('configTransport').deleteRow(i);
    }
</script>

<form action="update_transport.jsp">

    <input type="hidden" name="_transport" value="<%=transport%>"/>
    <input type="hidden" name="_service" value="<%=service%>"/>

    <table class="styledLeft" id="configTransport" width="100%">
        <tbody id="paramTableBody">
            <%
                if (transportInData != null && transportInData.length>0) {
            %>
            <tr>
                <td colspan="2" style="border-left: 0px !important; border-right: 0px !important; border-top: 0px !important; padding-left: 0px !important;">
                    <h4><strong><%=transport.toUpperCase()%> Listener</strong></h4>
                </td>
            </tr>

            <tr>
                <td class="sub-header"><fmt:message key="transport.parameter.name"/></td>
                <td class="sub-header"><fmt:message key="transport.parameter.value"/></td>
            </tr>

            <%
                for (TransportParameter currentParam : transportInData) {
                    if (currentParam == null) {
                        continue;
                    }
            %>
            <tr>
                <td><%=currentParam.getName()%></td>
                <%
                if (!"port".equals(currentParam.getName())) {
                    String chkName = currentParam.getName()+"_chk";
                    if ("true".equalsIgnoreCase(currentParam.getValue().trim())) {
                %>
                <td>
                    <input type='checkbox' name='<%=chkName%>' value='<%=chkName%>' id='<%=chkName%>' checked='checked' onclick="setType('<%=chkName%>','<%=currentParam.getName()%>')" />
                    <input type="hidden" name="<%=currentParam.getName()%>" id="<%=currentParam.getName()%>" value="true"/>
                </td>
                <%
                } else if ("false".equalsIgnoreCase(currentParam.getValue().trim())){
                %>
                <td>
                    <input type='checkbox' name='<%=chkName%>' id='<%=chkName%>' value='<%=chkName%>' onclick="setType('<%=chkName%>','<%=currentParam.getName()%>')" />
                    <input type='hidden' name='<%=currentParam.getName()%>' id='<%=currentParam.getName()%>' value='false' />
                </td>
                <%} else {
                %>
                <td><textarea rows="3" cols="60" name="<%=currentParam.getName()%>"><%=currentParam.getValue()%></textarea>
                </td>
                <%}
                } else { %>
                <td><%=currentParam.getValue()%></td>
                <input type="hidden" name="<%=currentParam.getName()%>" value="<%=currentParam.getValue()%>"/>
                <%} %>
            </tr>
            <%} %>

            <%} else { %>
             <tr>
                <td colspan="2"><fmt:message key="no.params.defined"/></td>
            </tr>
            <%}%>

        </tbody>

        <tr>
            <td colspan="2" class="buttonRow">
                <input type="submit" value="<fmt:message key="transport.update"/>" class="button"/>
                <input class="button" type="reset" value="<fmt:message key="transport.cancel"/>"  onclick="javascript:document.location.href='../transport-mgt/service_transport.jsp?serviceName=<%=service%>'"/ >
            </td>
        </tr>

    </table>
</form>
<p>&nbsp;</p>
<p>&nbsp;</p>
<table class="styledLeft" id="addParameter">
    <tr>
        <td colspan="2" class="sub-header"><fmt:message key="transport.add.new.parameter"/></td>
    </tr>
    <tr>
        <td>
            <fmt:message key="transport.parameter.name"/>
        </td>
        <td class="buttonRow">
            <input type="text" id="addNewParameter" size="50"/>
            <button class="button" onClick='addNewParameter()'><fmt:message key="transport.add"/></button>
        </td>
    </tr>
</table>

</div>
</div>
</fmt:bundle>
