<%@ page import="org.apache.axis2.client.Options" %>
<%@ page import="org.apache.axis2.client.ServiceClient" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.event.stub.internal.TopicManagerAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.stub.internal.xsd.TopicRolePermission" %>
<%@ page import="org.wso2.carbon.event.ui.UIUtils" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.regex.Pattern" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!--Local js includes-->
<script type="text/javascript" src="js/treecontrol.js"></script>
<script type="text/javascript" src="js/topics.js"></script>
<script type="text/javascript" src="js/eventing_utils.js"></script>

<link href="css/tree-styles.css" media="all" rel="stylesheet"/>
<link href="css/dsxmleditor.css" media="all" rel="stylesheet"/>

<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="eventing.js"></script>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

<fmt:bundle basename="org.wso2.carbon.event.ui.i18n.Resources">
<carbon:breadcrumb
        label="add.subtopic"
        resourceBundle="org.wso2.carbon.event.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>

<script type="text/javascript">

    jQuery(document).ready(function () {
        // changing pagination links when topic name text changes
        jQuery('#topic').keyup(function () {
            changeAllLinks();
        });

        // changing pagination links when role search text changes
        jQuery('#search').keyup(function () {
            changeAllLinks();
        });

        // updating permissions to the sessions. the checkboxes mentioned here are the publish and consume permission checkboxes
        jQuery('.checkboxChanged').click(function () {
            var $element = jQuery(this);
            var role = $element.attr('role');
            // prop is used because when unchecked, attr gives undefined
            var checked = $element.prop('checked');
            var action = $element.attr('permission');

            jQuery.ajax({
                url: "update_topic_role_permissions_to_session_ajaxprocessor.jsp",
                data: {role: role, checked: checked, action: action},
                success: function (data) {
                    //do nothing
                }
            });
        });
    });

    // changes links in pagination with search text and topic name
    function changeAllLinks() {
        jQuery('#permissionTable').find('tr td a').each(
                function () {
                    var href = jQuery(this).attr('href');
                    var topicName;
                    var searchTerm;

                    var parameters = href.match(/topicName=(.*?)\&searchTerm=(.*?)$/);
                    if (parameters) {
                        topicName = parameters[1];
                        searchTerm = parameters[2];
                    }
                    href = href.replace("&topicName=" + topicName, "&topicName=" + jQuery('#topic').val());
                    href = href.replace("&searchTerm=" + searchTerm, "&searchTerm=" + jQuery('#search').val());
                    jQuery(this).attr('href', href);
                }
        );
    }

    // searching a role
    function searchRole() {
        var searchTerm = jQuery('#search').val();
        var topicName = jQuery('#topic').val();
        var topicPath = jQuery('#existingTopic').val();
        var splitted = window.location.href.split("add_subtopic.jsp?");
        window.location.assign(splitted[0] + "add_subtopic.jsp?topicPath=" + topicPath + "&topicName=" +
                topicName + "&searchTerm=" + searchTerm);
    }

</script>

<%
    String topicPath = request.getParameter("topicPath");
    if (topicPath == null) {
%>
<script type="text/javascript">
    location.href = 'topics.jsp';</script>
<%
        return;
    }

    Pattern pattern = Pattern.compile("topicPath=" + topicPath + "$");
    if (pattern.matcher(request.getAttribute("javax.servlet.forward.query_string").toString()).matches()) {
        session.removeAttribute("topicRolePermissions");
    }

    // Get topic name and search term from the request. If they are not found in the request, use the default ones.
    String message = request.getParameter("message");
    String topicNameFromRequest = request.getParameter("topicName");
    String topicName = topicNameFromRequest == null ? "" : topicNameFromRequest;
    String searchTermFromRequest = request.getParameter("searchTerm");
    String searchTerm = searchTermFromRequest == null ? "*" : searchTermFromRequest;
    String concatenatedParams = "topicPath=" + topicPath + "&topicName=" + topicName + "&searchTerm=" + searchTerm;
    if (message != null) {
%><h3><%=message%>
</h3><%
    }
    // Get the permissions given to user roles which are stored in the session
    ArrayList<TopicRolePermission> topicRolePermissions = (ArrayList<TopicRolePermission>) session.getAttribute("topicRolePermissions");

    if (!(topicRolePermissions != null && topicRolePermissions.size() > 0)) {

        // If the permissions are not found in the Session, store them to the session
        topicRolePermissions = new ArrayList<TopicRolePermission>();
        session.setAttribute("topicRolePermissions", topicRolePermissions);

        // Obtaining all existing user roles
        String[] userRoles;
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        //Server URL which is defined in the server.xml
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                session) + "TopicManagerAdminService.TopicManagerAdminServiceHttpsSoap12Endpoint";
        TopicManagerAdminServiceStub stub = new TopicManagerAdminServiceStub(configContext, serverURL);

        String cookie = (String) session.getAttribute(org.wso2.carbon.utils.ServerConstants.ADMIN_SERVICE_COOKIE);

        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        try {
            userRoles = stub.getUserRoles();

            TopicRolePermission topicRolePermission;
            for (String role : userRoles) {
                topicRolePermission = new TopicRolePermission();
                topicRolePermission.setRoleName(role);
                topicRolePermission.setAllowedToSubscribe(false);
                topicRolePermission.setAllowedToPublish(false);
                topicRolePermissions.add(topicRolePermission);
            }

        } catch (Exception e) {
%>
<script type="text/javascript">
    CARBON.showErrorDialog('<%= e.getMessage()%>');
</script>
<%
            return;
        }
    }

    //Select the roles according to the submitted search term
    ArrayList<TopicRolePermission> selectedTopicRolePermissions = new ArrayList<TopicRolePermission>();
    if ("*".equals(searchTerm) || "".equals(searchTerm)) {
        selectedTopicRolePermissions = topicRolePermissions;
    } else {
        for (TopicRolePermission permission : topicRolePermissions) {
            if (permission.getRoleName().toLowerCase().contains(searchTerm.toLowerCase())) {
                selectedTopicRolePermissions.add(permission);
            }
        }
    }

    //Obtain values needed to handle pagination when displaying role permissions.
    int rolesCountPerPage = 20;
    int pageNumber = 0;
    int numberOfPages = 1;
    long totalRoleCount;
    ArrayList<TopicRolePermission> filteredRoleList = new ArrayList<TopicRolePermission>();

    String pageNumberAsStr = request.getParameter("pageNumber");
    if (pageNumberAsStr != null) {
        pageNumber = Integer.parseInt(pageNumberAsStr);
    }

    if (selectedTopicRolePermissions.size() > 0) {
        totalRoleCount = selectedTopicRolePermissions.size();
        numberOfPages = (int) Math.ceil(((float) totalRoleCount) / rolesCountPerPage);
        filteredRoleList = UIUtils.getFilteredRoleList(selectedTopicRolePermissions, pageNumber * rolesCountPerPage, rolesCountPerPage);
    }
%>
    <div id="middle">
        <div id="workArea">
            <h2><fmt:message key="add.sub.topic"/></h2>

            <table style="width:100%;margin-bottom:20px;" class="yui-skin-sam">
                <tbody>
                <tr>
                    <td>
                        <input class="longInput" id="existingTopic" type="hidden"
                               readonly="true"
                               value="<%=Encode.forHtml(topicPath)%>">
                        <strong><fmt:message key="parent.topic"/>:</strong> <%=Encode.forHtml(topicPath)%>
                    </td>
                </tr>
                </tbody>
            </table>

            <table id="topicAddTable" class="styledLeft" style="width:100%">
                <thead>
                <tr>
                    <th colspan="2">Enter Subtopic Name</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td class="leftCol-big"><fmt:message key="topic"/><span
                            class="required">*</span></td>
                    <td><input type="text" id="topic" value=<%=topicName%>></td>
                </tr>
                </tbody>
            </table>

            <p>&nbsp;</p>

            <table id="permissionTable" class="styledLeft" style="width:100%">
                <thead>
                <tr>
                    <th colspan="2"><fmt:message key="permissions"/></th>
                </tr>
                </thead>

                <tbody>
                <tr>
                    <td class="leftCol-big"><fmt:message key="search.label"/></td>
                    <td>
                        <input type="text" id="search" value="<%=searchTerm%>"/>
                        <input id="searchButton" class="button" type="button" onclick="searchRole()" value="Search"/>
                    </td>
                </tr>
                <tr>
                    <td class="leftCol-big" colspan="2">
                        <input type="hidden" name="pageNumber" value="<%=pageNumber%>"/>

                        <div class="paginatorWrapper">
                            <carbon:paginator pageNumber="<%=pageNumber%>" numberOfPages="<%=numberOfPages%>"
                                              page="add_subtopic.jsp" pageNumberParameterName="pageNumber"
                                              resourceBundle="org.wso2.carbon.event.ui.i18n.Resources"
                                              prevKey="prev" nextKey="next"
                                              parameters="<%=concatenatedParams%>"/>
                        </div>
                        <table class="styledLeft" style="width:100%" id="permissionsTable">
                            <thead>
                            <tr>
                                <th><fmt:message key="role"/></th>
                                <th><fmt:message key="subscribe"/></th>
                                <th><fmt:message key="publish"/></th>
                            </tr>
                            </thead>
                            <tbody>
                            <%
                                if (filteredRoleList.size() <= 0) {
                            %>
                            <script type="text/javascript">
                                CARBON.showInfoDialog('No matching roles found');
                            </script>
                            <%
                                }
                                for (TopicRolePermission rolePermission : filteredRoleList) {
                            %>
                            <tr>
                                <td><%=rolePermission.getRoleName()%>
                                </td>
                                <td><input type="checkbox"
                                           class="checkboxChanged"
                                           role="<%=rolePermission.getRoleName()%>"
                                           permission="subscribe"
                                           id="<%=rolePermission.getRoleName()%>^subscribe"
                                           value="subscribe" <% if (rolePermission.getAllowedToSubscribe()) { %>
                                           checked <% } %></td>
                                <td><input type="checkbox"
                                           class="checkboxChanged"
                                           role="<%=rolePermission.getRoleName()%>"
                                           permission="publish"
                                           id="<%=rolePermission.getRoleName()%>^publish"
                                           value="publish"  <% if (rolePermission.getAllowedToPublish()) { %>
                                           checked <% } %></td>
                            </tr>
                            <%
                                }
                            %>

                            </tbody>
                        </table>
                        <div class="paginatorWrapper">
                            <carbon:paginator pageNumber="<%=pageNumber%>" numberOfPages="<%=numberOfPages%>"
                                              page="add_subtopic.jsp" pageNumberParameterName="pageNumber"
                                              resourceBundle="org.wso2.carbon.event.ui.i18n.Resources"
                                              prevKey="prev" nextKey="next"
                                              parameters="<%=concatenatedParams%>"/>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td colspan="2" class="buttonRow"><input type="button" class="button"
                                                             value="<fmt:message key="add.topic"/>"
                                                             onclick="addTopicFromManage()"/></td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</fmt:bundle>