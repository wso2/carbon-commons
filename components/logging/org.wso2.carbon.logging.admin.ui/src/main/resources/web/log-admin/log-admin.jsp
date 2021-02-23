<%--
~ Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<script type="text/javascript" src="js/loggingadmin.js"></script>
<fmt:bundle basename="org.wso2.carbon.logging.admin.ui.i18n.Resources">
    <carbon:breadcrumb label="logging.management"
                       resourceBundle="org.wso2.carbon.logging.admin.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>

    <div id="middle">

        <h2><fmt:message key="logging.configuration"/></h2>

        <div id="workArea">

            <fieldset style="border: medium none ;">

                <script type="text/javascript">
                    jQuery(document).ready(function() {
                        loadPage();
                    });
                </script>

                <div id="addLoggerSettings">
                </div>

                <p>&nbsp;</p>

                <div id="loggerSettings">
                    <table class="styledLeft" width="100%">
                        <thead>
                        <tr>
                            <th><fmt:message key="configure.log4j2.loggers"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td>
                                <table class="normal">
                                    <tr>
                                        <td width="15%">
                                            <nobr><fmt:message key="filter.loggers.by"/></nobr>
                                        </td>
                                        <td>
                                            <input size="46" id="filterText" type="text"/>
                                            &nbsp;
                                            <input onclick="showLoggers('true');return false;"
                                                   value="<fmt:message key="starts.with"/>"
                                                   type="button"
                                                   class="button"/>
                                            &nbsp;
                                            <input onclick="showLoggers('false'); return false;"
                                                   value="<fmt:message key="contains"/>"
                                                   type="button"
                                                   class="button"/>
                                        </td>
                                    </tr>
                                </table>
                                <p>&nbsp;</p>
                                <div id="loggers">
                                    <p><fmt:message key="loading"/></p>
                                </div>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </fieldset>
        </div>
    </div>
</fmt:bundle>
