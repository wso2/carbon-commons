<%@ page import="org.apache.axis2.client.Options" %>
<%@ page import="org.apache.axis2.client.ServiceClient" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.event.stub.internal.TopicManagerAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.stub.internal.xsd.TopicRolePermission" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<!--Yahoo includes for dom event handling-->
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>

<!--Yahoo includes for animations-->
<script src="../yui/build/animation/animation-min.js" type="text/javascript"></script>

<!--Yahoo includes for menus-->
<link rel="stylesheet" type="text/css" href="../yui/build/menu/assets/skins/sam/menu.css"/>
<script type="text/javascript" src="../yui/build/container/container_core-min.js"></script>
<script type="text/javascript" src="../yui/build/menu/menu-min.js"></script>

<!--EditArea javascript syntax hylighter -->
<script language="javascript" type="text/javascript" src="../editarea/edit_area_full.js"></script>

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

<%
    if (request.getParameter("topicPath") == null) {
%>
<script type="text/javascript">
    location.href = 'topics.jsp';</script>
<%
        return;
    }

    String[] userRoles = null;
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
    } catch (Exception e) {
%>
<script type="text/javascript">
    CARBON.showErrorDialog('<%= e.getMessage()%>');

</script>
<%
        return;
    }
    String topic = request.getParameter("topicPath");
    TopicRolePermission[] defaultRolePermissions = null;
    if (userRoles != null) {

        defaultRolePermissions = new TopicRolePermission[userRoles.length];
        TopicRolePermission topicRolePermissions;
        int roleIndex = 0;
        for (String role : userRoles) {
            topicRolePermissions = new TopicRolePermission();
            topicRolePermissions.setRoleName(role);
            topicRolePermissions.setAllowedToPublish(true);
            topicRolePermissions.setAllowedToSubscribe(true);
            defaultRolePermissions[roleIndex] = topicRolePermissions;
            roleIndex++;
        }
    }

%>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

<fmt:bundle basename="org.wso2.carbon.event.ui.i18n.Resources">
    <carbon:breadcrumb
            label="add.subtopic"
            resourceBundle="org.wso2.carbon.event.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>
    <div id="middle">
        <div id="workArea">
            <h2><fmt:message key="add.sub.topic"/></h2>

            <table style="width:100%;margin-bottom:20px;" class="yui-skin-sam">
                <tbody>
                <tr>
                    <td>
                        <input class="longInput" id="existingTopic" type="hidden"
                                           readonly="true"
                                           value="<%=topic%>">
                        <strong><fmt:message key="parent.topic"/>:</strong> <%=topic%>
                    </td>
                </tr>
                </tbody>
            </table>
            <div style="clear:both"></div>
            <table class="styledLeft" style="width:100%">
                <thead>
                <th colspan="3">
                    <fmt:message key="subtopic.details"/>

                </th>
                </thead>
                <tbody>
                <tr>
                    <td><fmt:message key="subtopic.name"/></td>
                    <td><input type="text" id="newTopic"></td>
                </tr>
                <tr>
                    <td class="formRow" colspan="2">
                        <h4>Permissions</h4>
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
                                if (defaultRolePermissions != null) {
                                    for (TopicRolePermission topicRolePermission : defaultRolePermissions) {
                            %>
                            <tr>
                                <td><%=topicRolePermission.getRoleName()%>
                                </td>
                                <td><input type="checkbox"
                                           id="<%=topicRolePermission.getRoleName()%>^subscribe"
                                           value="subscribe" <% if (topicRolePermission.getAllowedToSubscribe()) { %>
                                           checked <% } %></td>
                                <td><input type="checkbox"
                                           id="<%=topicRolePermission.getRoleName()%>^publish"
                                           value="publish"  <% if (topicRolePermission.getAllowedToPublish()) { %>
                                           checked <% } %></td>
                            </tr>
                            <%
                                    }
                                }
                            %>

                            </tbody>
                        </table>
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