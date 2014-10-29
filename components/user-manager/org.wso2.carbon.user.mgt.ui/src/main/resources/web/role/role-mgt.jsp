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
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@page import="org.wso2.carbon.user.mgt.ui.UserAdminClient"%>
<%@page import="org.wso2.carbon.utils.ServerConstants"%>
<%@page import="org.wso2.carbon.ui.util.CharacterEncoder"%>
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

<%
    String BUNDLE = "org.wso2.carbon.userstore.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    boolean error = false;
    boolean newFilter = false;
    boolean doRoleList = true;
    boolean showFilterMessage = false;
    boolean multipleUserStores = false;
    List<FlaggedName> datasList= null;
    FlaggedName[] roles = null;
    FlaggedName exceededDomains = null;
    String[] domainNames = null;    
    int pageNumber = 0;
    int cachePages = 3;
    int noOfPageLinksToDisplay = 5;
    int numberOfPages = 0;
    Map<Integer, PaginatedNamesBean> flaggedNameMap = null;
    UserRealmInfo userRealmInfo = null;

    // clear session data
    session.removeAttribute("roleBean");
    session.removeAttribute(UserAdminUIConstants.ROLE_READ_ONLY);
    session.removeAttribute(UserAdminUIConstants.ROLE_LIST_UNASSIGNED_USER_CACHE);
    session.removeAttribute(UserAdminUIConstants.ROLE_LIST_UNASSIGNED_USER_CACHE_EXCEEDED);
    session.removeAttribute(UserAdminUIConstants.ROLE_LIST_ASSIGNED_USER_CACHE);
    session.removeAttribute(UserAdminUIConstants.ROLE_LIST_ASSIGNED_USER_CACHE_EXCEEDED);
    session.removeAttribute(UserAdminUIConstants.ROLE_LIST_ADD_ROLE_USER_CACHE);
    session.removeAttribute(UserAdminUIConstants.ROLE_LIST_ADD_ROLE_USER_CACHE_EXCEEDED);
    session.removeAttribute(UserAdminUIConstants.ROLE_LIST_ASSIGN_USER_FILTER);
    session.removeAttribute(UserAdminUIConstants.ROLE_LIST_UNASSIGNED_USER_FILTER);
    session.removeAttribute(UserAdminUIConstants.ROLE_LIST_VIEW_USER_FILTER);
    session.removeAttribute(UserAdminUIConstants.ROLE_LIST_CACHE);

    // search filter
    String selectedDomain = request.getParameter("domain");
    if(selectedDomain == null || selectedDomain.trim().length() == 0){
        selectedDomain = (String) session.getAttribute(UserAdminUIConstants.ROLE_LIST_DOMAIN_FILTER);
        if (selectedDomain == null || selectedDomain.trim().length() == 0) {
            selectedDomain = UserAdminUIConstants.ALL_DOMAINS;
        }
    } else {
        newFilter = true;
    }

    session.setAttribute(UserAdminUIConstants.ROLE_LIST_DOMAIN_FILTER, selectedDomain.trim());
    
    String filter = request.getParameter(UserAdminUIConstants.ROLE_LIST_FILTER);
    if (filter == null || filter.trim().length() == 0) {
        filter = (String) session.getAttribute(UserAdminUIConstants.ROLE_LIST_FILTER);
        if (filter == null || filter.trim().length() == 0) {
            filter = "*";
        }
    } else {
        if(filter.contains(UserAdminUIConstants.DOMAIN_SEPARATOR)){
            selectedDomain = UserAdminUIConstants.ALL_DOMAINS;
            session.removeAttribute(UserAdminUIConstants.ROLE_LIST_DOMAIN_FILTER);
        }
        newFilter = true;
    }


    String modifiedFilter = filter.trim();
    if(!UserAdminUIConstants.ALL_DOMAINS.equalsIgnoreCase(selectedDomain)){
        modifiedFilter = selectedDomain + UserAdminUIConstants.DOMAIN_SEPARATOR + filter;
        modifiedFilter = modifiedFilter.trim();
    }

    session.setAttribute(UserAdminUIConstants.ROLE_LIST_FILTER, filter.trim());

    String currentUser = (String) session.getAttribute("logged-user");
    userRealmInfo = (UserRealmInfo)session.getAttribute(UserAdminUIConstants.USER_STORE_INFO);
    multipleUserStores = userRealmInfo.getMultipleUserStore();
    String errorAttribute = (String) session.getAttribute(UserAdminUIConstants.DO_ROLE_LIST);
    exceededDomains = (FlaggedName) session.getAttribute(UserAdminUIConstants.ROLE_LIST_CACHE_EXCEEDED);

    // check page number
    String pageNumberStr = request.getParameter("pageNumber");
    if (pageNumberStr == null) {
        pageNumberStr = "0";
    }

    try {
        pageNumber = Integer.parseInt(pageNumberStr);
    } catch (NumberFormatException ignored) {
        // page number format exception
    }

    flaggedNameMap  = (Map<Integer, PaginatedNamesBean>) session.getAttribute(UserAdminUIConstants.ROLE_LIST_CACHE);
    if(flaggedNameMap != null){
        PaginatedNamesBean bean = flaggedNameMap.get(pageNumber);
        if(bean != null){
            roles = bean.getNames();
            if(roles != null && roles.length > 0){
                numberOfPages = bean.getNumberOfPages();
                doRoleList = false;
            }
        }
    }

    if (errorAttribute != null) {
        error = true;
        session.removeAttribute(UserAdminUIConstants.DO_ROLE_LIST);
    }

    if ((doRoleList || newFilter) && !error) {

        try {
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            UserAdminClient client = new UserAdminClient(cookie, backendServerURL, configContext);
            
            boolean sharedRoleEnabled = client.isSharedRolesEnabled();
            session.setAttribute(UserAdminUIConstants.SHARED_ROLE_ENABLED, sharedRoleEnabled);

            if (filter.length() > 0) {
                FlaggedName[] datas = client.getAllRolesNames(modifiedFilter, -1);
                datasList = new ArrayList<FlaggedName>(Arrays.asList(datas));
                exceededDomains = datasList.remove(datasList.size() - 1);
                session.setAttribute(UserAdminUIConstants.ROLE_LIST_CACHE_EXCEEDED, exceededDomains);
                datas = datasList.toArray(new FlaggedName[datasList.size()]);
                if (datas == null || datas.length == 0) {
                    session.removeAttribute(UserAdminUIConstants.ROLE_LIST_FILTER);
                    showFilterMessage = true;
                }
            }
            if(userRealmInfo == null){
                userRealmInfo = client.getUserRealmInfo();
                session.setAttribute(UserAdminUIConstants.USER_STORE_INFO, userRealmInfo);
            }

            if(datasList != null){
                flaggedNameMap = new HashMap<Integer, PaginatedNamesBean>();
                int max = pageNumber + cachePages;
                for(int i = (pageNumber - cachePages); i < max ; i++){
                    if(i < 0){
                        max++;
                        continue;
                    }
                    PaginatedNamesBean bean  =  Util.retrievePaginatedFlaggedName(i, datasList);
                    flaggedNameMap.put(i, bean);
                    if(bean.getNumberOfPages() == i + 1){
                        break;
                    }
                }
                roles = flaggedNameMap.get(pageNumber).getNames();
                numberOfPages = flaggedNameMap.get(pageNumber).getNumberOfPages();
                session.setAttribute(UserAdminUIConstants.ROLE_LIST_CACHE, flaggedNameMap);
            }
        } catch (Exception e) {
            String message =  MessageFormat.format(resourceBundle.getString("error.while.role.filtered"),
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
            list.add(UserAdminUIConstants.INTERNAL_DOMAIN);
            domainNames = list.toArray(new String[list.size()]);
        }
    }
%>
<fmt:bundle basename="org.wso2.carbon.userstore.ui.i18n.Resources">
<carbon:breadcrumb label="roles"
		resourceBundle="org.wso2.carbon.userstore.ui.i18n.Resources"
		topPage="false" request="<%=request%>" />
		
    <script type="text/javascript">

        function deleteUserGroup(role) {
            function doDelete(){
                var roleName = role;
                location.href = 'delete-role.jsp?roleName=' + roleName +'&userType=internal';
            }
            CARBON.showConfirmationDialog('<fmt:message key="confirm.delete.role"/> ' + role + '?', doDelete, null);
        }

        <%if (showFilterMessage) {%>
        jQuery(document).ready(function () {
            CARBON.showInfoDialog('<fmt:message key="no.roles.filtered"/>', null, null);
        });
        <%}%>
        /*function doDelete(){
            location.href = 'delete-role.jsp?roleName=' + this.role+'&userType=internal';
        }*/
    </script>
    <script type="text/javascript">

        function updateUserGroup(role) {
                var roleName = role;
                location.href = 'rename-role.jsp?roleName=' + roleName;
        }

    </script>
    <div id="middle">
        <h2><fmt:message key="roles"/></h2>

        <div id="workArea">

            <form name="filterForm" method="post" action="role-mgt.jsp">
                <table class="styledLeft noBorders">
				<thead>
					<tr>
						<th colspan="2"><fmt:message key="role.search"/></th>
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
                        <td class="leftCol-big" style="padding-right: 0 !important;"><fmt:message key="list.roles"/></td>
                        <td>
                            <input type="text" name="<%=UserAdminUIConstants.ROLE_LIST_FILTER%>"
                                   value="<%=filter%>"/>

                            <input class="button" type="submit"
                                   value="<fmt:message key="role.search"/>"/>
                        </td>

                    </tr>
				</tbody>
                </table>
            </form>
            <p>&nbsp;</p>

            <carbon:paginator pageNumber="<%=pageNumber%>"
                              numberOfPages="<%=numberOfPages%>"
                              noOfPageLinksToDisplay="<%=noOfPageLinksToDisplay%>"
                              page="role-mgt.jsp" pageNumberParameterName="pageNumber"/>

            <table class="styledLeft" id="roleTable">
                <%
                    if (roles != null && roles.length > 0) {
                %>
                <thead>
                <tr>
                    <th><fmt:message key="name"/></th>
                    <%--<%if(hasMultipleUserStores){%>
                    <th><fmt:message key="domainName"/></th>
                    <%}
                    %>--%>
                    <th><fmt:message key="actions"/></th>
                </tr>
                </thead>
                <%
                    }
                %>
                <tbody>
                <%
                         for (FlaggedName data : roles) {
                            if (data != null) { //Confusing!!. Sometimes a null object comes. Maybe a bug in Axis!!
                                if(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME.equals(data.getItemName())) {
                                    continue;
                                }
                                if(userRealmInfo.getAdminRole().equals(data.getItemName()) &&
                                        !userRealmInfo.getAdminUser().equals(currentUser)){
                                    continue;
                                }
                            String roleName = CharacterEncoder.getSafeText(data.getItemName());
                            String disPlayName = CharacterEncoder.getSafeText(data.getItemDisplayName());
                            if(disPlayName == null){
                                disPlayName = roleName;
                            }
                %>
                <tr>
                    <td><%=disPlayName%>
                        <%if(!data.getEditable()){ %> <%="(Read-Only)"%> <% } %>
                    </td>
                   <%-- <%if(hasMultipleUserStores){%>
                    	<td>
                            <%if(data.getDomainName() != null){%>
                            <%data.getDomainName();%>
                            <%} %>
                        </td>
                    <%}%>--%>
                    <td>
                    <%if(!data.getShared()){ %>
                    <% if(data.getItemName().equals(userRealmInfo.getAdminRole()) == false && data.getItemName().equals(userRealmInfo.getEveryOneRole()) == false && data.getEditable()){%>
<a href="#" onclick="updateUserGroup('<%=roleName%>')" class="icon-link" style="background-image:url(images/edit.gif);"><fmt:message key="rename"/></a>
                    <% }  %>
                    <% if(!data.getItemName().equals(userRealmInfo.getAdminRole())) {%>
<a href="edit-permissions.jsp?roleName=<%=roleName%>" class="icon-link" style="background-image:url(images/edit.gif);"><fmt:message key="edit.permissions"/></a>
                    <% }
                    }%>
                    
                    <% if (!userRealmInfo.getEveryOneRole().equals(data.getItemName()) && data.getEditable()) { %>
<a href="edit-users.jsp?roleName=<%=roleName%>&<%=UserAdminUIConstants.ROLE_READ_ONLY%>=<%=!data.getEditable()%>" class="icon-link" style="background-image:url(images/edit.gif);"><fmt:message key="edit.users"/></a>
                    <% } %>
                     <% if (!userRealmInfo.getEveryOneRole().equals(data.getItemName())) { %>
                        <a href="view-users.jsp?roleName=<%=roleName%>&<%=UserAdminUIConstants.ROLE_READ_ONLY%>=<%=!data.getEditable()%>"
                           class="icon-link" style="background-image:url(images/view.gif);"><fmt:message key="view.users"/></a>
                      <% } %>
                    <%if(!data.getShared()){ %>

                    <% if(data.getItemName().equals(userRealmInfo.getAdminRole()) == false && data.getItemName().equals(userRealmInfo.getEveryOneRole()) == false && data.getEditable()){%>
<a href="#" onclick="deleteUserGroup('<%=roleName%>')" class="icon-link" style="background-image:url(images/delete.gif);"><fmt:message key="delete"/></a>
                    <% }}  %>

                    </td>
                </tr>

                <%
                            }
                        }
               %>
                </tbody>
            </table>

            <carbon:paginator pageNumber="<%=pageNumber%>"
                              numberOfPages="<%=numberOfPages%>"
                              noOfPageLinksToDisplay="<%=noOfPageLinksToDisplay%>"
                              page="role-mgt.jsp" pageNumberParameterName="pageNumber"/>

            <%
                if (roles != null && roles.length > 0 && exceededDomains != null) {
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
                                message = resourceBundle.getString("more.roles.others").replace("{0}",arg);
                            } else{
                                message = resourceBundle.getString("more.roles.primary");
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
            message = resourceBundle.getString("more.roles").replace("{0}",arg);
        %>
        <strong><%=message%></strong>
        <%
                        }
                    }
                }
            %>

<table width="100%" border="0" cellpadding="0" cellspacing="0">
<% if(multipleUserStores || !userRealmInfo.getPrimaryUserStoreInfo().getReadOnly()){%>
            <tr>
                <td>
<a href="add-step1.jsp" class="icon-link" style="background-image:url(images/add.gif);"><fmt:message key="add.new.role"/></a>
                </td>
            </tr>
<% } %>
        <tr>
                 <td>
<a href="add-step1.jsp?roleType=<%=UserAdminUIConstants.INTERNAL_ROLE%>" class="icon-link" style="background-image:url(images/add.gif);"><fmt:message key="add.new.internal.role"/></a>
</td>        
    </tr>
        </table>
        </div>
    </div>
    <script type="text/javascript">
        alternateTableRows('roleTable', 'tableEvenRow', 'tableOddRow');
    </script>
</fmt:bundle>