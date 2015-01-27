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
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>

<%
    String groupName = request.getParameter("groupName");
    if(groupName == null){
        groupName = (String) session.getAttribute("groupName");
        session.removeAttribute("groupName");
    }
    String memberHostName = request.getParameter("memberHostName");
    if(memberHostName == null){
        memberHostName = (String) session.getAttribute("memberHostName");
        session.removeAttribute("memberHostName");
    }
    String backendURL = request.getParameter("backendURL");
    if(backendURL == null){
        backendURL = (String) session.getAttribute("backendURL");
        session.removeAttribute("backendURL");
    }
    String loginFailed = request.getParameter("loginFailed");
    if(loginFailed == null){
        loginFailed = (String) session.getAttribute("loginFailed");
        session.removeAttribute("loginFailed");
    }
%>

<carbon:breadcrumb
		label="sign.in"
		resourceBundle="org.wso2.carbon.cluster.mgt.admin.ui.i18n.Resources"
		topPage="false"
		request="<%=request%>" />
<div id="middle">
    <div id="workArea">
        <fmt:bundle basename="org.wso2.carbon.cluster.mgt.admin.ui.i18n.Resources">
            <h2>
                <fmt:message key="login.to.member">
                    <fmt:param value="<%= memberHostName%>"/>
                    <fmt:param value="<%= groupName%>"/>
                </fmt:message>
            </h2>

            <%
                if(loginFailed != null){
            %>
            <script type="text/javascript">
                CARBON.showWarningDialog('<fmt:message key="login.to.member.failed"/>');
            </script>
            <%
                }
            %>
            <form action="connect_member.jsp" method="post">
                <input type="hidden" name="backendURL" value="<%= backendURL%>"/>
                <input type="hidden" name="groupName" value="<%= groupName%>"/>
                <input type="hidden" name="memberHostName" value="<%= memberHostName%>"/>
                <table class="styledLeft">
                    <tr>
                        <td width="10%" class="formRow"><fmt:message key="username"/></td>
                        <td class="formRow"><input type="text" name="username"/></td>
                    </tr>
                    <tr>
                        <td width="10%" class="formRow"><fmt:message key="password"/></td>
                        <td class="formRow"><input type="password" name="password"/></td>
                    </tr>
                    <tr>
                        <td width="10%" class="buttonRow">&nbsp;</td>
                        <td class="buttonRow"><input type="submit" class="button" value="<fmt:message key="sign.in"/>"/>
                        </td>
                    </tr>
                </table>
            </form>
        </fmt:bundle>
    </div>
</div>