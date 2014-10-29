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
<%@ page import="org.wso2.carbon.datasource.ui.DataSourceClientConstants" %>
<%@ page import="org.wso2.carbon.datasource.ui.DatasourceManagementClient" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.Map" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type="text/javascript" src="global-params.js"></script>
<script type="text/javascript" src="dscommon.js"></script>

<fmt:bundle basename="org.wso2.carbon.datasource.ui.i18n.Resources">
    <carbon:breadcrumb resourceBundle="org.wso2.carbon.datasource.ui.i18n.Resources"
                       topPage="true" request="<%=request%>" label="data.sources.header"/>
    <div id="middle">

        <h2><fmt:message key="data.sources.header"/></h2>

        <div id="workArea">
            <%
                DatasourceManagementClient client;
                try {
                    client = DatasourceManagementClient.getInstance(config, session);
                    Map<String, String> descriptions = client.getAllDataSourceInformations();
                    if (descriptions != null && !descriptions.isEmpty()) {


            %>
            <p><fmt:message key="available.defined.data.sources"/></p>
            <br/>
            <table id="myTable" class="styledLeft">
                <thead>
                    <tr>
                        <th><fmt:message key="th.data.source"/></th>
                        <th><fmt:message key="th.status"/></th>
                        <th><fmt:message key="th.action"/></th>
                    </tr>
                </thead>
                <tbody>

                    <%
                        for (String name : descriptions.keySet()) {
                            if (name != null) {
                    %>
                    <tr id="tr_<%=name%>">

                        <td>
                            <%=name%>
                        </td>
                        <td>
                            <%=descriptions.get(name)%>
                        </td>
                        <td ><a href="#" class="edit-icon-link" onclick="editRow('<%=name%>')"><fmt:message key="datasource.edit"/></a><a href="#" class="delete-icon-link" onclick="deleteRow('<%=name%>','<fmt:message key="ds.delete.waring"/>')"><fmt:message key="datasource.delete"/></a>
                    
                    </tr>
                    <%
                            }
                        }
                    %>
                </tbody>
            </table>
            <%} else {%>
            <p><fmt:message key="no.datasources.msg"/></p>
            <br/>
            <%}%>
            <div style="height:30px;">
                <a href="javascript:document.location.href='newdatasource.jsp'" class="add-icon-link"><fmt:message key="add.data.source"/></a>
            </div>

        </div>
    </div> 
    <%

    } catch (Exception e) {
    	CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);    
        %>
        	<script type="text/javascript">
                    window.location.href = "../admin/error.jsp";
                </script>
        <%    
    }

    %>
    <script type="text/javascript">
        alternateTableRows('myTable', 'tableEvenRow', 'tableOddRow');
    </script>
</fmt:bundle>
   
