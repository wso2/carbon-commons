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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.UserAdminClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.Util" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.PaginatedNamesBean" %>
<%@ page import="java.util.*" %>
<%@ page import="org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.UserAdminUIConstants" %>
<%@ page import="org.wso2.carbon.user.mgt.stub.types.carbon.UserRealmInfo" %>
<%@ page import="java.text.MessageFormat" %>
<script type="text/javascript" src="../userstore/extensions/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<jsp:include page="../dialog/display_messages.jsp"/>
<title>WSO2 Carbon - Security Configuration</title>
<%

    boolean error = false;
    boolean newFilter = false;
    boolean doUserList = true;
    boolean showFilterMessage = false;
    boolean multipleUserStores = false;
    String forwardTo = "user-mgt.jsp";
               
    FlaggedName[] datas = null;
    FlaggedName exceededDomains = null;
    String[] claimUris = null;
    FlaggedName[] users = null;
    String[] domainNames = null;
    int pageNumber = 0;
    int cachePages = 3;
    int noOfPageLinksToDisplay = 5;
    int numberOfPages = 0;
    Map<Integer, PaginatedNamesBean>  flaggedNameMap = null;

    String BUNDLE = "org.wso2.carbon.userstore.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());    

    // remove session data
    session.removeAttribute("userBean");
    session.removeAttribute(UserAdminUIConstants.USER_DISPLAY_NAME);
    session.removeAttribute(UserAdminUIConstants.USER_LIST_UNASSIGNED_ROLE_CACHE);
    session.removeAttribute(UserAdminUIConstants.USER_LIST_UNASSIGNED_ROLE_CACHE_EXCEEDED);
    session.removeAttribute(UserAdminUIConstants.USER_LIST_ASSIGNED_ROLE_CACHE);
    session.removeAttribute(UserAdminUIConstants.USER_LIST_ASSIGNED_ROLE_CACHE_EXCEEDED);
    session.removeAttribute(UserAdminUIConstants.USER_LIST_ADD_USER_ROLE_CACHE);
    session.removeAttribute(UserAdminUIConstants.USER_LIST_ADD_USER_ROLE_CACHE_EXCEEDED);
    session.removeAttribute(UserAdminUIConstants.USER_LIST_ASSIGN_ROLE_FILTER);
    session.removeAttribute(UserAdminUIConstants.USER_LIST_UNASSIGNED_ROLE_FILTER);
    session.removeAttribute(UserAdminUIConstants.USER_LIST_VIEW_ROLE_FILTER);
	session.removeAttribute(UserAdminUIConstants.USER_LIST_CACHE);

     // retrieve session attributes
    String currentUser = (String) session.getAttribute("logged-user");
    UserRealmInfo userRealmInfo = (UserRealmInfo) session.getAttribute(UserAdminUIConstants.USER_STORE_INFO);
    multipleUserStores = userRealmInfo.getMultipleUserStore();
    java.lang.String errorAttribute = (java.lang.String) session.getAttribute(UserAdminUIConstants.DO_USER_LIST);

    String claimUri = request.getParameter("claimUri");
    exceededDomains = (FlaggedName) session.getAttribute(UserAdminUIConstants.USER_LIST_CACHE_EXCEEDED);

    //  search filter
    String selectedDomain = request.getParameter("domain");
    if(selectedDomain == null || selectedDomain.trim().length() == 0){
        selectedDomain = (String) session.getAttribute(UserAdminUIConstants.USER_LIST_DOMAIN_FILTER);
        if (selectedDomain == null || selectedDomain.trim().length() == 0) {
            selectedDomain = UserAdminUIConstants.ALL_DOMAINS;
        }
    } else {
        newFilter = true;
    }

    session.setAttribute(UserAdminUIConstants.USER_LIST_DOMAIN_FILTER, selectedDomain.trim());

    String filter = request.getParameter(UserAdminUIConstants.USER_LIST_FILTER);
    if (filter == null || filter.trim().length() == 0) {
        filter = (java.lang.String) session.getAttribute(UserAdminUIConstants.USER_LIST_FILTER);
        if (filter == null || filter.trim().length() == 0) {
            filter = "*";
        }
    } else {
        if(filter.contains(UserAdminUIConstants.DOMAIN_SEPARATOR)){
            selectedDomain = UserAdminUIConstants.ALL_DOMAINS;
            session.removeAttribute(UserAdminUIConstants.USER_LIST_DOMAIN_FILTER);
        }
        newFilter = true;
    }

    String modifiedFilter = filter.trim();
    if(!UserAdminUIConstants.ALL_DOMAINS.equalsIgnoreCase(selectedDomain)){
        modifiedFilter = selectedDomain + UserAdminUIConstants.DOMAIN_SEPARATOR + filter;
        modifiedFilter = modifiedFilter.trim();
    }

    session.setAttribute(UserAdminUIConstants.USER_LIST_FILTER, filter.trim());

    // check page number
    String pageNumberStr = request.getParameter("pageNumber");
    if (pageNumberStr == null) {
        pageNumberStr = "0";
    }

    if(userRealmInfo != null){
        claimUris = userRealmInfo.getRequiredUserClaims();
    }

    try {
        pageNumber = Integer.parseInt(pageNumberStr);
    } catch (NumberFormatException ignored) {
        // page number format exception
    }
    
    flaggedNameMap  = (Map<Integer, PaginatedNamesBean>) session.getAttribute(UserAdminUIConstants.USER_LIST_CACHE);
    if(flaggedNameMap != null){
        PaginatedNamesBean bean = flaggedNameMap.get(pageNumber);
        if(bean != null){
            users = bean.getNames();
            if(users != null && users.length > 0){
                numberOfPages = bean.getNumberOfPages();
                doUserList = false;
            }
        }
    }

    if (errorAttribute != null) {
        error = true;
        session.removeAttribute(UserAdminUIConstants.DO_USER_LIST);
    }

    if ((doUserList || newFilter) && !error) { // don't call the back end if some kind of message is showing
        try {
            java.lang.String cookie = (java.lang.String) session
                    .getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            java.lang.String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                                                                          session);
            ConfigurationContext configContext = (ConfigurationContext) config
                    .getServletContext()
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            UserAdminClient client =  new UserAdminClient(cookie, backendServerURL, configContext);
            if (userRealmInfo == null) {
                userRealmInfo = client.getUserRealmInfo();
                session.setAttribute(UserAdminUIConstants.USER_STORE_INFO, userRealmInfo);
            }

            if(userRealmInfo != null){
                claimUris = userRealmInfo.getRequiredUserClaims();
            }

            if (filter.length() > 0) {
                datas = client.listAllUsers(modifiedFilter, -1);
                List<FlaggedName> dataList = new ArrayList<FlaggedName>(Arrays.asList(datas));
                exceededDomains = dataList.remove(dataList.size() - 1);
                session.setAttribute(UserAdminUIConstants.USER_LIST_CACHE_EXCEEDED, exceededDomains);
                if (dataList == null || dataList.size() == 0) {
                    session.removeAttribute(UserAdminUIConstants.USER_LIST_FILTER);
                    showFilterMessage = true;
                }

                if(dataList != null){
                    flaggedNameMap = new HashMap<Integer, PaginatedNamesBean>();
                    int max = pageNumber + cachePages;
                    for(int i = (pageNumber - cachePages); i < max ; i++){
                        if(i < 0){
                            max++;
                            continue;
                        }
                        PaginatedNamesBean bean  =  Util.retrievePaginatedFlaggedName(i,dataList);
                        flaggedNameMap.put(i, bean);
                        if(bean.getNumberOfPages() == i + 1){
                            break;
                        }
                    }
                    users = flaggedNameMap.get(pageNumber).getNames();
                    numberOfPages = flaggedNameMap.get(pageNumber).getNumberOfPages();
                    session.setAttribute(UserAdminUIConstants.USER_LIST_CACHE, flaggedNameMap);
                }
            }
            
        } catch (Exception e) {
            String message =  MessageFormat.format(resourceBundle.getString("error.while.user.filtered"),
                    e.getMessage());
%>
        <script type="text/javascript">

            jQuery(document).ready(function () {
                CARBON.showErrorDialog('<%=message%>', null);
            });
        </script>
<%
        }
    }

    if(userRealmInfo != null){
        domainNames = userRealmInfo.getDomainNames();
        if(domainNames != null){
            List<String> list = new ArrayList<String>(Arrays.asList(domainNames));
            list.add(UserAdminUIConstants.ALL_DOMAINS);
            domainNames = list.toArray(new String[list.size()]);
        }
    }
%>
<fmt:bundle basename="org.wso2.carbon.userstore.ui.i18n.Resources">
    <carbon:breadcrumb label="users"
                       resourceBundle="org.wso2.carbon.userstore.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <script type="text/javascript">

        function deleteUser(user) {
            function doDelete() {
                var userName = user;
                location.href = 'delete-finish.jsp?username=' + userName;
            }

            CARBON.showConfirmationDialog("<fmt:message key="confirm.delete.user"/> \'" + user + "\'?", doDelete, null);
        }

        <%if (showFilterMessage == true) {%>
        jQuery(document).ready(function () {
            CARBON.showInfoDialog('<fmt:message key="no.users.filtered"/>', null, null);
        });
        <%}%>
    </script>

    <div id="middle">
        <h2><fmt:message key="users"/></h2>

        <div id="workArea">
            <form name="filterForm" method="post" action="user-mgt.jsp">
                <table class="styledLeft noBorders">
				<thead>
					<tr>
						<th colspan="2"><fmt:message key="user.search"/></th>
					</tr>
				</thead>
				<tbody>

                <%
                   if(domainNames != null && domainNames.length > 0){
                %>
                <tr>
                    <td class="leftCol-big" style="padding-right: 0 !important;"><fmt:message key="select.domain.search"/></td>
                    <td><select id="domain" name="domain">
                        <%
                            for(String domainName : domainNames) {
                                if(selectedDomain.equals(domainName)) {
                        %>
                            <option selected="selected" value="<%=domainName%>"><%=domainName%></option>
                        <%
                                } else {
                        %>
                            <option value="<%=domainName%>"><%=domainName%></option>
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

                    <tr>
                <%
                    if(CarbonUIUtil.isContextRegistered(config, "/identity-mgt/") && !multipleUserStores){
                %>
                        <td class="leftCol-big" style="padding-right: 0 !important;"><fmt:message key="list.users.claim"/></td>
                        <td>
                            <input type="text" name="<%=UserAdminUIConstants.USER_LIST_FILTER%>"
                                   value="<%=filter%>"/>
                       
                            <input class="button" type="submit"
                                   value="<fmt:message key="user.search"/>"/>
                        </td>
                <%
                    } else {
                %>
                        <td class="leftCol-big" style="padding-right: 0 !important;"><fmt:message key="list.users"/></td>
                        <td>
                            <input type="text" name="<%=UserAdminUIConstants.USER_LIST_FILTER%>"
                                   value="<%=filter%>"/>
                      
                            <input class="button" type="submit"
                                   value="<fmt:message key="user.search"/>"/>
                        </td>
                <%
                    }
                %>
                    </tr>
                <%--<%
                    if(CarbonUIUtil.isContextRegistered(config, "/identity-mgt/") && !multipleUserStores){
                %>
                    <tr>
                        <td><fmt:message key="claim.uri"/></td>
                        <td><select id="claimUri" name="claimUri">
                            <option value="Select" selected="selected">Select</option>
                            <%
                                if(claimUris != null){

                                    for(String claim : claimUris) {
                                        if(claimUri != null && claim.equals(claimUri)) {
                            %>
                                    <option selected="selected" value="<%=claim%>"><%=claim%></option>
                            <%
                                        } else {
                            %>
                                    <option value="<%=claim%>"><%=claim%></option>
                            <%
                                        }
                                    }
                                }
                            %>
                        </select>
                        </td>
                    </tr>
                <%
                    }
                %>--%>
				</tbody>
                </table>
            </form>
            <p>&nbsp;</p>

            <carbon:paginator pageNumber="<%=pageNumber%>"
                              numberOfPages="<%=numberOfPages%>"
                              noOfPageLinksToDisplay="<%=noOfPageLinksToDisplay%>"
                              page="user-mgt.jsp" pageNumberParameterName="pageNumber"/>

            <table class="styledLeft" id="userTable">

                <%
                    if (users != null && users.length > 0) {
                %>
                <thead>
                <tr>
                    <th class="leftCol-big"><fmt:message key="name"/></th>
                    <th><fmt:message key="actions"/></th>
                </tr>
                </thead>
                <%
                    }
                %>
                <tbody>
                <%
                    if (users != null) {
                        for (int i=0;i<users.length;i++) {
                            if (users[i] != null) { //Confusing!!. Sometimes a null object comes. Maybe a bug Axis!!
                                if (users[i].getItemName().equals(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME)) {
                                    continue;
                                }
                                String  userName = CharacterEncoder.getSafeText(users[i].getItemName());
                                String disPlayName = CharacterEncoder.getSafeText(users[i].getItemDisplayName());
                                if(disPlayName == null || disPlayName.trim().length() == 0){
                                    disPlayName = userName;
                                }
                %>
                <tr>
                    <td><%=disPlayName%>
                        <%if(!users[i].getEditable()){ %> <%="(Read-Only)"%> <% } %>
                    </td>
                    <td>

                        <%
                            if(userRealmInfo.getAdminUser().equals(userName) &&
                                    !userRealmInfo.getAdminUser().equals(currentUser)){
                                continue;
                            }
                        %>
                        <%
                            if (!Util.getUserStoreInfoForUser(userName, userRealmInfo).getPasswordsExternallyManaged() &&      // TODO
                                CarbonUIUtil.isUserAuthorized(request,
                                         "/permission/admin/configure/security/usermgt/passwords") &&
                                    users[i].getEditable()) { //if passwords are managed externally do not allow to change passwords.
                        %>
                        <a href="change-passwd.jsp?username=<%=userName%>&disPlayName=<%=disPlayName%>" class="icon-link"
                           style="background-image:url(../admin/images/edit.gif);"><fmt:message
                                key="change.password"/></a>
                        <%
                            }
                        %>

                        <%
                            if(CarbonUIUtil.isUserAuthorized(request, "/permission/admin/configure/security")){
                        %>
                        <a href="edit-user-roles.jsp?username=<%=userName%>&disPlayName=<%=disPlayName%>" class="icon-link"
                           style="background-image:url(../admin/images/edit.gif);"><fmt:message
                                key="edit.roles"/></a>
                        <%
                            }
                        %>

                        <%
                            if(CarbonUIUtil.isUserAuthorized(request, "/permission/admin/configure/security")){
                        %>
                        <a href="view-roles.jsp?username=<%=userName%>&disPlayName=<%=disPlayName%>" class="icon-link"
                           style="background-image:url(images/view.gif);"><fmt:message
                                key="view.roles"/></a>
                        <%
                            }
                        %>


                        <%
                            if (CarbonUIUtil.isUserAuthorized(request,
                                "/permission/admin/configure/security/usermgt/users") && !userName.equals(currentUser)
                                && !userName.equals(userRealmInfo.getAdminUser()) &&
                                    users[i].getEditable()) {
                        %>
                        <a href="#" onclick="deleteUser('<%=userName%>')" class="icon-link"
                           style="background-image:url(images/delete.gif);"><fmt:message
                                key="delete"/></a>
                        <%
                                }
                        %>


                        <%
                            if (CarbonUIUtil.isContextRegistered(config, "/identity-authorization/" ) &&
                                    CarbonUIUtil.isUserAuthorized(request, "/permission/admin/configure/security/")) {
                        %>
                            <a href="../identity-authorization/permission-root.jsp?userName=<%=userName%>&fromUserMgt=true"
                               class="icon-link"
                               style="background-image:url(../admin/images/edit.gif);"><fmt:message key="authorization"/></a>
                        <%
                            }
                         %>



                        <%
                            if (CarbonUIUtil.isContextRegistered(config, "/userprofile/")
                                && CarbonUIUtil.isUserAuthorized(request,
                                                                 "/permission/admin/configure/security/usermgt/profiles")) {
                        %>
                        <a href="../userprofile/index.jsp?username=<%=userName%>&disPlayName=<%=disPlayName%>&fromUserMgt=true"
                           class="icon-link"
                           style="background-image:url(../userprofile/images/my-prof.gif);">User
                                                                                            Profile</a>
                        <%
                            }
                        %>

                    </td>
                </tr>
                <%
                            }
                        }
                    }
                %>
                </tbody>
            </table>
            <carbon:paginator pageNumber="<%=pageNumber%>"
                              numberOfPages="<%=numberOfPages%>"
                              noOfPageLinksToDisplay="<%=noOfPageLinksToDisplay%>"
                              page="user-mgt.jsp" pageNumberParameterName="pageNumber"/>

            <p>&nbsp;</p>

            <%
                if (users != null && users.length > 0 && exceededDomains != null) {
                    if(exceededDomains.getItemName() != null || exceededDomains.getItemDisplayName() != null){
                        String message = null;
                        if(exceededDomains.getItemName() != null && exceededDomains.getItemName().equals("true")){
                            if(exceededDomains.getItemDisplayName() != null && !exceededDomains.getItemDisplayName().equals("")){
                                String arg = "";
                                String[] domains = exceededDomains.getItemDisplayName().split(":");
                                for(int i=0;i<domains.length;i++){
                                    arg += "\'"+domains[i]+"\'";
                                    if(i < domains.length - 2){
                                        arg += ", ";
                                    }else if(i == domains.length - 2){
                                        arg += " and ";
                                    }
                                }
                                message = resourceBundle.getString("more.users.others").replace("{0}",arg);
                            } else{
                                message = resourceBundle.getString("more.users.primary");
                            }
        %>
                <strong><%=message%></strong>
        <%
                        }else if(exceededDomains.getItemDisplayName() != null && !exceededDomains.getItemDisplayName().equals("")){
                            String[] domains = exceededDomains.getItemDisplayName().split(":");
                            String arg = "";
                            for(int i=0;i<domains.length;i++){
                                arg += "\'"+domains[i]+"\'";
                                if(i < domains.length - 2){
                                    arg += ", ";
                                }else if(i == domains.length - 2){
                                    arg += " and ";
                                }
                            }
                            message = resourceBundle.getString("more.users").replace("{0}",arg);
        %>
                <strong><%=message%></strong>
        <%
                        }
                     }
                }
            %>

            <%
                if ((multipleUserStores || !userRealmInfo.getPrimaryUserStoreInfo().getReadOnly())
                        && userRealmInfo.getPrimaryUserStoreInfo().getExternalIdP() == null
                        && CarbonUIUtil.isUserAuthorized(request,
                                "/permission/admin/configure/security/usermgt/users")) {
            %>
            <table width="100%" border="0" cellpadding="0" cellspacing="0" style="margin-top:2px;">
                <tr>
                    <td class="addNewSecurity">
                        <a href="add-step1.jsp" class="icon-link"
                           style="background-image:url(images/add.gif);"><fmt:message
                                key="add.new.user"/></a>
                    </td>
                </tr>

                <%
                    if (!multipleUserStores && userRealmInfo.getBulkImportSupported()) {
                %>
                <tr>
                    <td class="addNewSecurity">
                        <a href="bulk-import.jsp" class="icon-link"
                           style="background-image:url(images/bulk-import.gif);"><fmt:message
                                key="bulk.import.user"/></a>
                    </td>
                </tr>

                <%
                    }
                %>

            </table>

            <%
                }
            %>


        </div>
    </div>
    <script language="text/JavaScript">
        alternateTableRows('userTable', 'tableEvenRow', 'tableOddRow');
    </script>
</fmt:bundle>