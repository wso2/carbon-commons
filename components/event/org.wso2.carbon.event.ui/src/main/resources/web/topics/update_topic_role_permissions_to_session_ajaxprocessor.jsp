<%@ page import="java.util.ArrayList" %>
<%@ page import="org.wso2.carbon.event.stub.internal.xsd.TopicRolePermission" %>
<%
    ArrayList<TopicRolePermission> topicRolePermissions = (ArrayList<TopicRolePermission>) session.getAttribute("topicRolePermissions");
    String role = request.getParameter("role");
    String checked = request.getParameter("checked");
    String action = request.getParameter("action");

    for(TopicRolePermission permission: topicRolePermissions){
        if(permission.getRoleName().equals(role)){
            if("subscribe".equals(action)){
                permission.setAllowedToSubscribe(Boolean.valueOf(checked));
            } else if ("publish".equals(action)){
                permission.setAllowedToPublish(Boolean.valueOf(checked));
            }
        }
    }
%>