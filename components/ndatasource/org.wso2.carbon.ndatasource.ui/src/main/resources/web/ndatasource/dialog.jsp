<!--
~ Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<script type="text/javascript" src="dscommon.js"></script>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<fmt:bundle basename="org.wso2.carbon.ndatasource.ui.i18n.Resources">
    <%
        String message = request.getParameter("message");
        String type = request.getParameter("type");
    %>

    <script type="text/javascript">
        <%if(type!=null) { if("existing".equals(type)){%>
        CARBON.showWarningDialog("<fmt:message key="cannot.add.a.data.source"/>"+"<fmt:message key="a.datasource.with.name"/> " + '<%=message%>' + " <fmt:message key="already.exists"/>",
                function() {
                    goBackOnePage();
                }, function () {
                    goBackOnePage();
                });
        <%} else if("issystem".equals(type)) {%>
        CARBON.showWarningDialog("<fmt:message key="cannot.add.a.data.source"/>",
                function() {
                    goBackOnePage();
                }, function () {
                    goBackOnePage();
                });

        <%} else if("error".equals(type)) {%>
        CARBON.showErrorDialog("<%=message%>", function () {
            goBackOnePage();
        }, function () {
            goBackOnePage();
        });
        <%}}%>
    </script>
</fmt:bundle>