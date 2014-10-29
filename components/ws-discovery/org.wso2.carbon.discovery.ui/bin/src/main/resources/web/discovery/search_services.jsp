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

<fmt:bundle basename="org.wso2.carbon.discovery.ui.i18n.Resources">

<%
    String proxyName = (String) request.getAttribute("proxyName");
    if (proxyName == null) {
        session.setAttribute("discoveryError", "The discovery proxy name has not been specified");
%>
    <script type="text/javascript">
        window.location.href = 'index.jsp';
    </script>
<%
    }

    String currentQuery = "";
    if (request.getAttribute("currentQuery") != null) {
        currentQuery = (String) request.getAttribute("currentQuery");
        request.removeAttribute("currentQuery");
    }
%>

<script type="text/javascript">
    function doSearch(proxy) {
        search_form.action = 'view_services.jsp?proxy=' + proxy;
        search_form.submit();
        return true;
    }
</script>

<form action="" method="POST" id="search_form">
    <input type="hidden" name="proxy" value="<%=proxyName%>"/>
    <table class="styledLeft">
        <thead>
            <tr>
                <th><fmt:message key="wsd.search.scopes"/></th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>
                    <table cellpadding="0" cellspacing="0">
                        <tr>
                            <td style="border:none; padding-left:0px !important"><fmt:message key="wsd.service.scopes"/></td>
                            <td style="border:none;"><input type="text" size="60" name="searchScopes" value="<%=currentQuery%>"/></td>
                            <td style="border:none;"><button class="button" onclick="doSearch('<%=proxyName%>'); return false;"><fmt:message key="wsd.go"/></button> </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </tbody>
    </table>
</form>

</fmt:bundle>