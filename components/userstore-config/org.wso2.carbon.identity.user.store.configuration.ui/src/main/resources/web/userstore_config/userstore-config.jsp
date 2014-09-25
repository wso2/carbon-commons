<!--
/*
* Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
-->

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.user.store.configuration.stub.api.Properties" %>
<%@ page import="org.wso2.carbon.identity.user.store.configuration.stub.api.Property" %>
<%@ page import="org.wso2.carbon.identity.user.store.configuration.ui.UserStoreUIConstants" %>
<%@ page import="org.wso2.carbon.identity.user.store.configuration.ui.client.UserStoreConfigAdminServiceClient" %>
<%@ page import="org.wso2.carbon.identity.user.store.configuration.ui.utils.UserStoreMgtDataKeeper" %>
<%@ page import="org.wso2.carbon.identity.user.store.configuration.ui.utils.UserStoreUIUtils" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="java.util.Iterator" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<jsp:include page="../dialog/display_messages.jsp"/>
<fmt:bundle basename="org.wso2.carbon.identity.user.store.configuration.ui.i18n.Resources">
<carbon:breadcrumb
        label="add.userstore"
        resourceBundle="org.wso2.carbon.identity.user.store.configuration.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>
<%
    String BUNDLE = "org.wso2.carbon.identity.user.store.configuration.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
%>

<%!
    private String propertyValue;
    private String propertyName;
    private Properties properties;
    private Property[] mandatories;
    private Property[] optionals;
    private Property[] advancedProperties;
    private String forwardTo;
    private String domain;
    private String className;
    private Boolean isEditing;
    private int isBoolean;
    private String existingDomains;
    private int i;

    private int isBoolean(String value) {
        int i = -123;
        if (value.equalsIgnoreCase("true")) {
            i = 1;
        } else if (value.equalsIgnoreCase("false")) {
            i = 0;
        }
        return i;
    }

%><%
    if (request.getParameter("domain") != null) {
        domain = CharacterEncoder.getSafeText(request.getParameter("domain"));
    }

    if (request.getParameter("className") != null) {
        className = CharacterEncoder.getSafeText(request.getParameter("className"));
    }
    String selectedClassApplied = null;
    String description = null;
    int rank;
    String[] classApplies = new String[0];

    if (className.equals("0")) {
        selectedClassApplied = request.getParameter("classApplied");       //add
        isEditing = false;
    } else {
        selectedClassApplied = className;                                  //edit
        isEditing = true;
    }


    if (selectedClassApplied == null || selectedClassApplied.trim().length() == 0) {
        selectedClassApplied = UserStoreUIConstants.RWLDAP_USERSTORE_MANAGER;
    } else {

    }

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    UserStoreConfigAdminServiceClient userStoreConfigAdminServiceClient = new UserStoreConfigAdminServiceClient(cookie, backendServerURL, configContext);
    classApplies = userStoreConfigAdminServiceClient.getAvailableUserStoreClasses();
    Iterator<String> iterator = UserStoreMgtDataKeeper.getAvailableDomainNames().iterator();
    existingDomains = "";
    while (iterator.hasNext()) {
        existingDomains = existingDomains + "\"" + iterator.next().toUpperCase() + "\",";
    }
    existingDomains = "[" + existingDomains + "\"PRIMARY\"]";

    if (!domain.equals("0")) {

        //Get the defined properties of user store manager
        Map<String, String> tempProperties = UserStoreMgtDataKeeper.getUserStoreManager(domain);
        className = tempProperties.get(UserStoreUIConstants.CLASS);
        description = tempProperties.get(UserStoreUIConstants.DESCRIPTION);

        String forwardTo = "index.jsp";
        properties = UserStoreUIUtils.mergePropertyValues(userStoreConfigAdminServiceClient.getUserStoreProperties(className), tempProperties);
        mandatories = properties.getMandatoryProperties();
        optionals = properties.getOptionalProperties();
        advancedProperties = properties.getAdvancedProperties();


    } else {
        if ((session.getAttribute(UserStoreUIConstants.DOMAIN)) != null) {
            domain = (String) session.getAttribute(UserStoreUIConstants.DOMAIN);
        }

        if ((session.getAttribute(UserStoreUIConstants.DESCRIPTION)) != null) {
            description = (String) session.getAttribute(UserStoreUIConstants.DESCRIPTION);
        }

        properties = userStoreConfigAdminServiceClient.getUserStoreProperties(selectedClassApplied);
        mandatories = properties.getMandatoryProperties();
        optionals = properties.getOptionalProperties();
        advancedProperties = properties.getAdvancedProperties();
    }
%>

<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="resources/js/main.js"></script>
<!--Yahoo includes for dom event handling-->
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>
<link href="../entitlement/css/entitlement.css" rel="stylesheet" type="text/css" media="all"/>


<script type="text/javascript">
    jQuery(document).ready(function () {
        jQuery('#domainId').keyup(function () {
                    $('#userStoreTypeSub strong').html(
                            $(this).val()
                    );
                }
        );
    });


    var allPropertiesSelected = false;
    function doSubmit() {
        if (doValidationDomainNameOnly() && doValidationDomainExistence()) {
            if (doValidationMandatoryProperties()) {
                document.dataForm.action = "userstore-config-finish.jsp";
                document.dataForm.submit();
            }
        }
    }

    function doUpdate() {
        if (doValidateUpdate()) {
            if (doValidationMandatoryProperties()) {
                document.dataForm.action = "userstore-config-finish.jsp";
                document.dataForm.submit();
            }
        }
        else if (doValidationDomainNameOnly() && doValidationDomainExistence()) {
            if (doValidationMandatoryProperties()) {
                document.dataForm.action = "userstore-config-finish.jsp";
                document.dataForm.submit();
            }
        }
    }

    function selectAllInThisPage(isSelected) {
        allPropertiesSelected = false;
        if (document.dataForm.userStores != null &&
                document.dataForm.userStores [0] != null) { // there is more than 1 service
            if (isSelected) {
                for (var j = 0; j < document.dataForm.userStores.length; j++) {
                    document.dataForm.userStores [j].checked = true;
                }
            } else {
                for (j = 0; j < document.dataForm.userStores.length; j++) {
                    document.dataForm.userStores [j].checked = false;
                }
            }
        } else if (document.dataForm.userStores != null) { // only 1 service
            document.dataForm.userStores.checked = isSelected;
        }
        return false;
    }

    function resetVars() {
        allPropertiesSelected = false;

        var isSelected = false;
        if (document.dataForm.userStores != null) { // there is more than 1 service
            for (var j = 0; j < document.dataForm.userStores.length; j++) {
                if (document.dataForm.userStores [j].checked) {
                    isSelected = true;
                }
            }
        } else if (document.dataForm.userStores != null) { // only 1 service
            if (document.dataForm.userStores.checked) {
                isSelected = true;
            }
        }
        return false;
    }

    function deleteUserStores() {
        var selected = false;
        if (document.dataForm.userStores[0] != null) { // there is more than 1 policy
            for (var j = 0; j < document.dataForm.userStores.length; j++) {
                selected = document.dataForm.userStores[j].checked;
                if (selected) break;
            }
        } else if (document.dataForm.userStores != null) { // only 1 policy
            selected = document.dataForm.userStores.checked;
        }
        if (!selected) {
            CARBON.showInfoDialog('<fmt:message key="select.user.stores.to.be.deleted"/>');
            return;
        }
        if (allPropertiesSelected) {
            CARBON.showConfirmationDialog("<fmt:message key="delete.all.user.stores.prompt"/>", function () {
                document.dataForm.action = "remove-policy.jsp";
                document.dataForm.submit();
            });
        } else {
            CARBON.showConfirmationDialog("<fmt:message key="delete.user.stores.on.page.prompt"/>", function () {
                document.dataForm.action = "remove-policy.jsp";
                document.dataForm.submit();
            });
        }
    }

    function doCancel() {
        location.href = "index.jsp";
    }

    function removeRow(link) {
        link.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode.
                removeChild(link.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode);
    }

    function createNewPropertyRow(name, value) {
        var rowIndex = jQuery(document.getElementById('propertiesTable').rows[document.
                getElementById('propertiesTable').rows.length - 1]).attr('data-value');
        var index = parseInt(rowIndex, 10) + 1;
        jQuery('#propertiesTable > tbody:last').append('<tr data-value="' + index + '"><td><table class="oneline-listing"><tr></tr><tr>' +
                '<td><input type="text" name="expropertyName_' + index + '" id="expropertyName_' + index + '" value="name"/></td>' +
                '<td><input type="text" name="expropertyValue_' + index + '" id="expropertyValue_' + index + '" value="value"/></td>' +
                '</tr><tr></tr></table></td></tr>');
    }

    function createNewMandatoryPropertyRow() {
        var rowIndex = jQuery(document.getElementById('mandatoryPropertiesTable').rows[document.
                getElementById('mandatoryPropertiesTable').rows.length - 1]).attr('data-value');
        var index = parseInt(rowIndex, 10) + 1;
        jQuery('#mandatoryPropertiesTable > tbody:last').append('<tr data-value="' + index + '"><td><table class="oneline-listing"><tr></tr><tr>' +
                '<td><input type="text" name="mpropertyName_' + index + '" id="mpropertyName_' + index + '" value="<%=""%>"/></td>' +
                '<td><input type="text" name="mpropertyValue_' + index + '" id="mpropertyValue_' + index + '" value="<%=""%>"/></td>' +
                '</tr><tr></tr></table></td></tr>');
    }


    function getCategoryType() {
        <%
        session.setAttribute(UserStoreUIConstants.DESCRIPTION,request.getParameter(UserStoreUIConstants.DESCRIPTION));
        session.setAttribute(UserStoreUIConstants.DOMAIN,request.getParameter(UserStoreUIConstants.DOMAIN));
        %>
        document.dataForm.submit();
    }

    function doValidationDomainNameOnly() {

        var value = document.getElementsByName("domainId")[0].value;
        if (value == '') {
            CARBON.showWarningDialog('<fmt:message key="domain.name.is.required"/>');
            return false;
        }
        if (value.indexOf("_") >= 0) {
            CARBON.showWarningDialog('<fmt:message key="cannot.contain.character"/>');
            return false;
        }
        return true;
    }

    function doValidationDomainExistence() {
        var domainsArray = <%=existingDomains%>;
        var value = document.getElementsByName("domainId")[0].value;
        if ($.inArray(value.toUpperCase(), domainsArray) != -1) {
            CARBON.showWarningDialog('<fmt:message key="domain.already.exists"/>');
            return false;
        }
        return true;
    }

    function doValidateUpdate() {
        var value = document.getElementsByName("domainId")[0].value.toUpperCase();
        var domain = "<%=domain.toUpperCase()%>";
        if (value.localeCompare(domain)) {
            return false;
        }
        return true;
    }

    function doValidationMandatoryProperties() {
        var length = <%=mandatories.length%>;
        for (var j = 1; j <= length; j++) {
            if (document.getElementsByName("propertyValue_" + j)[0].value.trim().length == 0) {
                CARBON.showWarningDialog(document.getElementsByName("propertyName_" + j)[0].value + " " + '<fmt:message key="is.required"/>');
                return false;
            }
        }
        return true;
    }


</script>

<div id="middle">
<h2><fmt:message key="user.store.manager.configuration"/></h2>

<div id="workArea">
<form id="dataForm" name="dataForm" method="post" action="">
<div class="sectionSeperator"><fmt:message key="user.store.manager.configuration"/></div>
<div class="sectionSub">
    <table id="mainTable">
        <tr>
            <td class="leftCol-small">
                <fmt:message key="user.store.manager.class"/>
            </td>
            <td>
                <select id="classApplied" name="classApplied" onchange="getCategoryType();">
                    <%
                        for (String classApply : classApplies) {
                            if (selectedClassApplied != null && classApply.equals(selectedClassApplied)) {
                    %>
                    <option value="<%=classApply%>" selected="selected"><%=classApply%>
                    </option>
                    <%
                    } else {
                    %>
                    <option value="<%=classApply%>"><%=classApply%>
                    </option>
                    <%
                            }
                        }
                    %>
                </select>


                <div class="sectionHelp">
                    <fmt:message key="user.store.manager.properties.define"/>.
                </div>
            </td>
        </tr>
        <tr>
            <td class="leftCol-med"><fmt:message key="domain.name"/><span class="required">*</span></td>
            <%
                if (domain != null && domain.trim().length() > 0 && !domain.equals("0")) {
            %>
            <td><input type="text" name="domainId" id="domainId" width="" value="<%=domain%>"/></td>
            <td><input type="hidden" name="previousDomainId" id="previousDomainId" value="<%=domain%>"/></td>
            <%
            } else {
            %>
            <td><input type="text" name="domainId" id="domainId"/></td>
            <td><input type="hidden" name="previousDomainId" id="previousDomainId" value=""/></td>
            <%
                }
            %>
        </tr>
        <tr>
            <td><fmt:message key="description"/></td>
            <%
                if (description != null && description.trim().length() > 0) {
            %>
            <td><textarea name="description" id="description" class="text-box-big"><%=description%>
            </textarea>
            </td>
            <%
            } else {
            %>
            <td><textarea type="text" name="description" id="description" class="text-box-big"></textarea>
            </td>
            <%
                }
            %>
        </tr>
    </table>
</div>
    <%--END Basic information section --%>

    <%--**********************--%>
    <%--**********************--%>
    <%--START properties--%>
    <%--**********************--%>
    <%--**********************--%>
<div class="sectionSeperator" id="userStoreTypeSub"><%="Define Properties For "%><strong></strong></div>
<div class="sectionSub">
        <%--MandatoryProperties--%>
    <%if (mandatories[0] != null) {%>
    <table id="mandatoryPropertiesTable" style="width: 100%;margin-top:10px;">
        <tr data-value="0">
            <td>
                <table class="oneline-listing" style="width: 100%;margin-top:10px;">

                    <thead>
                    <tr>
                        <th style="width:20%" style="text-align:left;"><fmt:message
                                key='property.name'/></th>
                        <th style="width:30%" style="text-align:left;"><fmt:message
                                key='property.value'/></th>
                        <th style="width:50%" style="text-align:left;"><fmt:message
                                key='description'/></th>

                    </tr>
                    </thead>
                    <tbody>
                    <tr></tr>
                    <%
                        i = 1;

                        isBoolean = -123;
                        for (int j = 0; j < mandatories.length; j++) {
                            propertyName = mandatories[j].getName();
                            propertyValue = mandatories[j].getValue();
                            if (mandatories[j].getDescription() != null) {
                                description = mandatories[j].getDescription();
                            }

                            if (propertyValue != null) {
                                isBoolean = isBoolean(propertyValue);
                            }

                            String name = "propertyName_" + i;
                            String value = "propertyValue_" + i;
                    %>
                    <tr>
                        <%
                            if (propertyName != null && propertyName.trim().length() > 0) {

                        %>
                        <td class="leftCol-med" width="50%" style="text-align:left;"><%=propertyName%><span
                                class="required">*</span></td>
                        <input type="hidden" name=<%=name%> id=<%=name%> value="<%=propertyName%>"/>

                        <%
                        } else {
                        %>

                        <%
                            }
                        %>

                        <td style="width:30%" style="text-align:left;">
                            <%
                                if (propertyValue != null) {

                                    if (isBoolean == 1) { %>
                            <input type="checkbox" name=<%=value%> id=<%=value%>
                                   class="checkbox" checked/>
                            <%
                            } else if (isBoolean == 0) { %>
                            <input type="checkbox" name=<%=value%> id=<%=value%>
                                   class="checkbox"/>
                            <%
                            } else if (propertyName.endsWith("password") || propertyName.endsWith("Password")) { %>
                            <input type="password" name=<%=value%> id=<%=value%> style="width:95%"
                                   value="<%=propertyValue%>"/>
                            <%
                            } else {
                            %>
                            <input type="text" name=<%=value%> id=<%=value%> style="width:95%"
                                   value="<%=propertyValue%>"/>
                            <%
                                }
                            %>


                            <%
                                } else {

                                }
                            %>
                        </td>
                        <td class="sectionHelp" width="50%" style="text-align:left; !important">
                            <%=description%>
                        </td>

                    </tr>
                    <%
                            i++;

                        }
                    %>

                    </tbody>
                </table>
            </td>
        </tr>
    </table>
    <%
        } else {

        }
    %>
</div>


    <%--Define optional properties--%>

<script type="text/javascript">
    jQuery(document).ready(function () {

        jQuery("#optionalPropertyRow").hide();
        jQuery("#advancedPropertyRow").hide();
        /*Hide (Collapse) the toggle containers on load use show() instead of hide() 	in the 			above code if you want to keep the content section expanded. */

        jQuery("h2.trigger").click(function () {
            if (jQuery(this).next().is(":visible")) {
                this.className = "active trigger";
            } else {
                this.className = "trigger";
            }

            jQuery(this).next().slideToggle("fast");
            return false; //Prevent the browser jump to the link anchor
        });
    });
</script>
<h2 class="trigger  active"><a
        href="#"><fmt:message key="optional"/></a></h2>

<div class="toggle_container" style="padding:0;margin-bottom:10px;width: 100%" id="optionalPropertyRow">

        <%--Optional properties--%>
    <%if (optionals[0] != null) {%>
    <table id="propertiesTable" style="width: 100%;margin-top:10px;">
        <tr data-value="0">
            <td>
                <table class="oneline-listing" style="width: 100%;margin-top:10px;">
                    <thead>
                    <tr>
                        <th style="width:20%" style="text-align:left;"><fmt:message
                                key='property.name'/></th>
                        <th style="width:30%" style="text-align:left;"><fmt:message
                                key='property.value'/></th>
                        <th style="width:50%" style="text-align:left;"><fmt:message
                                key='description'/></th>
                    </tr>
                    </thead>
                    <%

                        isBoolean = -123;

                        for (int x = 0; x < optionals.length; x++) {
                            propertyName = optionals[x].getName();
                            propertyValue = optionals[x].getValue();

                            if (optionals[x].getDescription() != null) {
                                description = optionals[x].getDescription();
                            }

                            if (propertyValue != null) {
                                isBoolean = isBoolean(propertyValue);
                            }
                            String name = "propertyName_" + i;
                            String value = "propertyValue_" + i;
                    %>
                    <tr>
                        <%
                            if (propertyName != null && propertyName.trim().length() > 0) {

                        %>
                        <td class="leftCol-med" width="50%" style="text-align:left;" id="<%=name%>"><%=propertyName%>
                        </td>
                        <input type="hidden" name=<%=name%> id=<%=name%> value="<%=propertyName%>"/>
                        </td>
                        <td style="width:30%" style="text-align:left;">
                            <%
                                if (propertyValue != null) {
                                    if (isBoolean == 1) { %>
                            <input type="checkbox" name=<%=value%> id=<%=value%>
                                   class="checkbox" checked/>

                            <%
                            } else if (isBoolean == 0) { %>
                            <input type="checkbox" name=<%=value%> id=<%=value%>
                                   class="checkbox"/>
                            <%

                            } else {
                            %>
                            <input type="text" name=<%=value%> id=<%=value%> style="width:95%"
                                   value="<%=propertyValue%>"/>
                            <%
                                    }
                                } else {
                                    //property value null
                                }
                            %>
                        </td>
                        <td class="sectionHelp" width="50%" style="text-align:left; !important">
                            <%=description%>
                        </td>

                    </tr>
                    <%
                                i++;
                            } else {
                                //no property name
                            }
                        }
                    %>
                </table>
            </td>
        </tr>

    </table>
    <%
        } else {
            //no optional properties
        }
    %>


</div>

    <%--Advanced properties--%>
<%if (advancedProperties[0] != null) {%>
<h2 class="trigger  active"><a
        href="#"><fmt:message key="advanced"/></a></h2>

<div class="toggle_container" style="padding:0;margin-bottom:10px;width: 100%" id="advancedPropertyRow">



    <table id="propertiesTable" style="width: 100%;margin-top:10px;">
        <tr data-value="0">
            <td>
                <table class="oneline-listing" style="width: 100%;margin-top:10px;">
                    <thead>
                    <tr>
                        <th style="width:20%" style="text-align:left;"><fmt:message
                                key='property.name'/></th>
                        <th style="width:80%" style="text-align:left;"><fmt:message
                                key='property.value'/></th>
                    </tr>
                    </thead>
                    <%

                        isBoolean = -123;

                        for (int x = 0; x < advancedProperties.length; x++) {
                            propertyName = advancedProperties[x].getName();
                            propertyValue = advancedProperties[x].getValue();

                            if (advancedProperties[x].getDescription() != null) {
                                description = advancedProperties[x].getDescription();
                            }

                            if (propertyValue != null) {
                                isBoolean = isBoolean(propertyValue);
                            }
                            String name = "propertyName_" + i;
                            String value = "propertyValue_" + i;
                    %>
                    <tr>
                        <%
                            if (propertyName != null && propertyName.trim().length() > 0) {

                        %>
                        <td class="leftCol-med" width="50%" style="text-align:left;" id="<%=name%>"><%=propertyName%>
                        </td>
                        <input type="hidden" name=<%=name%> id=<%=name%> value="<%=propertyName%>"/>
                        </td>
                        <td style="width:30%" style="text-align:left;">
                            <%
                                if (propertyValue != null) {
                                    if (isBoolean == 1) { %>
                            <input type="checkbox" name=<%=value%> id=<%=value%>
                                   class="checkbox" checked/>

                            <%
                            } else if (isBoolean == 0) { %>
                            <input type="checkbox" name=<%=value%> id=<%=value%>
                                   class="checkbox"/>
                            <%

                            } else {
                            %>
                            <input type="text" name=<%=value%> id=<%=value%> style="width:95%"
                                   value="<%=propertyValue%>"/>
                            <%
                                    }
                                } else {
                                    //property value null
                                }
                            %>
                        </td>
                    </tr>
                    <%
                                i++;
                            } else {
                                //no property name
                            }
                        }
                    %>
                </table>
            </td>
        </tr>

    </table>
    <%
        } else {
            //no advanced properties
        }
    %>
    <input type="hidden" name="defaultProperties" id="defaultProperties" value=<%=i%>/>

</div>



    <%--********************--%>
    <%--********************--%>
    <%--END properties--%>
    <%--********************--%>
    <%--********************--%>
    <%--********************--%>


</form>
<div class="buttonRow">
    <%if (isEditing) { %>
    <input type="button" onclick="doUpdate();" value="<fmt:message key="update"/>"
           class="button"/>
    <%} else {%>
    <input type="button" onclick="doSubmit();" value="<fmt:message key="add"/>"
           class="button"/>
    <%}%>
    <input type="button" onclick="doCancel();" value="<fmt:message key="cancel" />"
           class="button"/>
</div>


</div>
</div>
</fmt:bundle>
