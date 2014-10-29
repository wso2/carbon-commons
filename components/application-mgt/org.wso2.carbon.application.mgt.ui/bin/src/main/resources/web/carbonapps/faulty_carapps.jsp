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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>

<%@ page import="org.wso2.carbon.application.mgt.ui.ApplicationAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    String pageNumber = CharacterEncoder.getSafeText(request.getParameter("pageNumber"));
    if (pageNumber == null) {
        pageNumber = "0";
    }
    int pageNumberInt = 0;
    try {
        pageNumberInt = Integer.parseInt(pageNumber);
    } catch (NumberFormatException ignored) {
    }
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String[] faultyCarbonAppList = null;
    ApplicationAdminClient client;
    int numberFaultyApps = 0;

    try {

        client = new ApplicationAdminClient(cookie,
                                             backendServerURL, configContext, request.getLocale());
        faultyCarbonAppList = client.getAllFaultyApps();
        if(faultyCarbonAppList != null){
            numberFaultyApps = faultyCarbonAppList.length;
        }

    } catch (Exception e) {
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
        return;
    }

%>

<div id="middle">
    <div id="workArea">
        <fmt:bundle basename="org.wso2.carbon.application.mgt.ui.i18n.Resources">
            <carbon:breadcrumb
                    label="faulty.carapps"
                    resourceBundle="org.wso2.carbon.application.mgt.ui.i18n.Resources"
                    topPage="false"
                    request="<%=request%>"/>
            <h2><fmt:message key="faulty.carapps"/></h2>
        </fmt:bundle>
        <%
            int numberOfPages;
            if (faultyCarbonAppList == null && numberFaultyApps == 0) {
        %>
        <fmt:bundle basename="org.wso2.carbon.application.mgt.ui.i18n.Resources">
            <p><fmt:message key="no.faulty.carapps.found"/></p>
        </fmt:bundle>
        <%
                return;
            }
            numberOfPages=1;
        %>
        <fmt:bundle basename="org.wso2.carbon.application.mgt.ui.i18n.Resources">
            <script type="text/javascript">
                var allCarbonappsSelected = false;
                function showError(divId) {
                    if (document.getElementById(divId).style.visibility == 'visible') {
                        document.getElementById(divId).style.visibility = 'hidden';
                    } else {
                        document.getElementById(divId).style.visibility = 'visible';
                    }
                }

                function deleteFaultyCarbonApps() {
                    var selected = false;
                    if (document.faultyCarbonAppsForm.carbonAppFileName[0] != null) { // there is more than 1 sg
                        for (var j = 0; j < document.faultyCarbonAppsForm.carbonAppFileName.length; j++) {
                            selected = document.faultyCarbonAppsForm.carbonAppFileName[j].checked;
                            if (selected) break;
                        }
                    } else if (document.faultyCarbonAppsForm.carbonAppFileName != null) { // only 1 sg
                        selected = document.faultyCarbonAppsForm.carbonAppFileName.checked;
                    }
                    if (!selected) {
                        CARBON.showInfoDialog('<fmt:message key="select.faulty.carapps.to.be.deleted"/>');
                        return;
                    }
                    if (allCarbonappsSelected) {
                        CARBON.showConfirmationDialog("<fmt:message key="delete.selected.faulty.carapps.prompt"><fmt:param value="<%= numberFaultyApps%>"/></fmt:message>",
                                                      function() {
                                                          location.href = 'delete_faulty_carbon_apps.jsp?deleteAllCarbonApps=true';
                                                      });
                    } else {
                        CARBON.showConfirmationDialog("<fmt:message key="delete.all.faulty.carapps.prompt"/>", function() {
                            document.faultyCarbonAppsForm.submit();
                        });
                    }
                }

                function selectAllInThisPage(isSelected) {
                    allCarbonappsSelected = false;
                    if (document.faultyCarbonAppsForm.carbonAppFileName[0] != null) { // there is more than 1 sg
                        if (isSelected) {
                            for (var j = 0; j < document.faultyCarbonAppsForm.carbonAppFileName.length; j++) {
                                document.faultyCarbonAppsForm.carbonAppFileName[j].checked = true;
                            }
                        } else {
                            for (j = 0; j < document.faultyCarbonAppsForm.carbonAppFileName.length; j++) {
                                document.faultyCarbonAppsForm.carbonAppFileName[j].checked = false;
                            }
                        }
                    } else if (document.faultyCarbonAppsForm.carbonAppFileName != null) { // only 1 sg
                        document.faultyCarbonAppsForm.carbonAppFileName.checked = isSelected;
                    }
                }

                function selectAllInAllPages() {
                    selectAllInThisPage(true);
                    allCarbonappsSelected = true;
                }

                function resetVars() {
                    allCarbonappsSelected = false;

                    var isSelected = false;
                    if (document.faultyCarbonAppsForm.carbonAppFileName[0] != null) { // there is more than 1 sg
                        for (var j = 0; j < document.faultyCarbonAppsForm.carbonAppFileName.length; j++) {
                            if (document.faultyCarbonAppsForm.carbonAppFileName[j].checked) {
                                isSelected = true;
                            }
                        }
                    } else if (document.faultyCarbonAppsForm.carbonAppFileName != null) { // only 1 sg
                        if (document.faultyCarbonAppsForm.carbonAppFileName.checked) {
                            isSelected = true;
                        }
                    }
                }
            </script>
            <carbon:itemGroupSelector selectAllInPageFunction="selectAllInThisPage(true)"
                                      selectAllFunction="selectAllInAllPages()"
                                      selectNoneFunction="selectAllInThisPage(false)"
                                      addRemoveFunction="deleteFaultyCarbonApps()"
                                      addRemoveButtonId="delete1"/>
            <carbon:paginator pageNumber="<%=0%>" numberOfPages="<%=numberOfPages%>"
                              page="faulty_services.jsp" pageNumberParameterName="pageNumber"/>
            <p>&nbsp;</p>


            <form action="delete_faulty_carbon_apps.jsp" name="faultyCarbonAppsForm">

                <table class="styledLeft" id="faultyCarbonAppsTable">
                    <thead>
                    <tr>
                        <th>&nbsp;</th>
                        <th><fmt:message key="faulty.carapp.file"/></th>
                        <th><fmt:message key="fault.reason"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <%

                        int count = 0;

                        for (String carbonApp : faultyCarbonAppList) {
                            if (carbonApp != null) {
                                count++;
                               String faultReason=client.getFaultException(carbonApp);
                    %>
                    <tr>
                        <td>
                            <input type="checkbox" name="carbonAppFileName"
                                   value="<%=carbonApp%>"
                                   onclick="resetVars()"/>
                        </td>
                        <td width="300px">
                            <%=carbonApp%>
                        </td>
                        <td>
                            <%=faultReason%>
                        </td>
                    </tr>
                    <%
                            }
                        }
                    %>
                    </tbody>
                </table>
            </form>
            <p>&nbsp;</p>
            <carbon:paginator pageNumber="<%=0%>" numberOfPages="<%=numberOfPages%>"
                              page="faulty_services.jsp" pageNumberParameterName="pageNumber"/>

            <carbon:itemGroupSelector selectAllInPageFunction="selectAllInThisPage(true)"
                                      selectAllFunction="selectAllInAllPages()"
                                      selectNoneFunction="selectAllInThisPage(false)"
                                      addRemoveFunction="deleteFaultyCarbonApps()"
                                      addRemoveButtonId="delete2"/>
        </fmt:bundle>
        <script type="text/javascript">
            alternateTableRows('faultyCarbonAppsTable', 'tableEvenRow', 'tableOddRow');
        </script>
    </div>
</div>
