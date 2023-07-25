<%--
~ Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
--%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type="text/javascript" src="js/loggingconfig.js"></script>
<fmt:bundle basename="org.wso2.carbon.logging.remote.config.ui.i18n.Resources">
    <carbon:breadcrumb label="logging.management"
                       resourceBundle="org.wso2.carbon.logging.remote.config.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>

    <div id="middle">

        <h2><fmt:message key="remote.logging.configuration"/></h2>

        <div id="workArea">

            <fieldset style="border: medium none ;">

                <script type="text/javascript">
                    jQuery(document).ready(function() {
                        loadPage();
                    });
                </script>

                <p>&nbsp;</p>

                <div id="addRemoteServerConfig">
                </div>

                <p>&nbsp;</p>
            </fieldset>
        </div>
    </div>
</fmt:bundle>
