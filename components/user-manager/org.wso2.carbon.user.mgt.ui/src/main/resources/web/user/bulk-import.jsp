<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.UserAdminClient" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.user.mgt.stub.types.carbon.UserStoreInfo" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.UserAdminUIConstants" %>
<%@ page import="org.wso2.carbon.user.mgt.stub.types.carbon.UserRealmInfo" %>

<script type="text/javascript" src="extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<jsp:include page="../dialog/display_messages.jsp"/>

<fmt:bundle basename="org.wso2.carbon.userstore.ui.i18n.Resources">

    <carbon:breadcrumb label="system.user.store"
                       resourceBundle="org.wso2.carbon.userstore.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>
    <%
        UserRealmInfo userRealmInfo = null;
        UserStoreInfo userStoreInfo = null;
        try {

            userRealmInfo = (UserRealmInfo) session.getAttribute(UserAdminUIConstants.USER_STORE_INFO);
            if (userRealmInfo == null) {
                String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
                String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
                ConfigurationContext configContext =
                        (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
                UserAdminClient client = new UserAdminClient(cookie, backendServerURL, configContext);
                userRealmInfo = client.getUserRealmInfo();
                session.setAttribute(UserAdminUIConstants.USER_STORE_INFO, userRealmInfo);
            }

            userStoreInfo = userRealmInfo.getPrimaryUserStoreInfo(); // Bulk import enable when only one user store

        } catch (Exception e) {
            CarbonUIMessage uiMsg = new CarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
    %>
    <jsp:include page="../admin/error.jsp"/>
    <%
            return;
        }
    %>
    <script type="text/javascript">
        function doValidation() {
            var error = "";
            error = validateEmpty("usersFile");
            if (error.length > 0) {
                CARBON.showWarningDialog("Users file cannot be empty.");
                return false;
            }

            var e = document.getElementById("domain");
            var passwordRegEx = "<%=userStoreInfo.getPasswordRegEx()%>";
            if (e != null) {

                var selectedDomainValue = e.options[e.selectedIndex].text.toUpperCase()
                var pwd = "pwd_";

                var passwordRegExElm = document.getElementById(pwd + selectedDomainValue);

                if (passwordRegExElm != null) {
                    passwordRegEx = document.getElementById(pwd + selectedDomainValue).value;
                } else {
                    passwordRegEx = document.getElementById("pwd_primary_null").value;
                }

            } else {

                passwordRegEx = document.getElementById("pwd_primary_null").value;

            }

            error = validatePasswordOnCreation("password", "password", passwordRegEx);
            if (error != "") {
                if (error == "Empty Password") {
                    CARBON.showWarningDialog("<fmt:message key="enter.the.same.password.twice"/>");
                } else if (error == "Invalid Character") {
                    CARBON.showWarningDialog("<fmt:message key="invalid.character.in.password"/>");
                } else if (error == "No conformance") {
                    CARBON.showWarningDialog("<fmt:message key="password.conformance"/>");
                }
                return false;
            }
            return true;
        }
    </script>
    <div id="middle">
        <h2><fmt:message key="bulk.import.user"/></h2>

        <div id="workArea">
            <form method="post" action="bulk-import-finish.jsp" name="dataForm"
                  enctype="multipart/form-data" onsubmit="return doValidation();">
                <table class="styledLeft" id="userAdd" width="60%">
                    <thead>
                    <tr>
                        <th><fmt:message key="enter.file.details"/></th>
                    </tr>
                    </thead>
                    <tr>
                        <td class="formRaw">
                            <table class="normal">
                                <tr>
                                    <td><fmt:message key="users.file"/><font color="red">*</font>
                                    </td>
                                    <td><input id="browseField" type="file" name="usersFile"
                                               size="50"/></td>
                                </tr>
                                <tr>
                                    <td><fmt:message key="default.password"/><font
                                            color="red">*</font></td>
                                    <td>
                                        <input type="password" name="password" style="width:150px"/>
                                        <font color="red">This password expires after 24
                                                          hours</font>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input type="submit" class="button"
                                   value="<fmt:message key="finish"/>"/>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </div>
    <script type="text/javascript">
        alternateTableRows('internal', 'tableEvenRow', 'tableOddRow');
        alternateTableRows('external', 'tableEvenRow', 'tableOddRow');
    </script>
</fmt:bundle>