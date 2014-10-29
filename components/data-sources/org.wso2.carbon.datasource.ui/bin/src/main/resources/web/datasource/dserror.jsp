<%@ page import="org.wso2.carbon.datasource.ui.DataSourceClientConstants" %>
<%--
 * Copyright 2006,2007 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
--%>
<%--<%@ page isErrorPage="true" %>--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<script type="text/javascript" src="global-params.js"></script>


<%
    Throwable cause = (Throwable) request.getSession().getAttribute(DataSourceClientConstants.EXCEPTION);

%>
<fmt:bundle basename="org.wso2.carbon.datasource.ui.i18n.Resources">
    <div id="content">


        <div id="simple-content">
            <div class="page_title"><fmt:message key="sorry.an.error.occured"/></div>
            <% if (cause != null) {
                request.removeAttribute(DataSourceClientConstants.EXCEPTION);
                String errorMsg = cause.getMessage();

                StackTraceElement[] trace = cause.getStackTrace();
            %>

            <div><fmt:message key="error"/> <%=errorMsg%>
            </div>
            <div>
                <% if (trace.length > 0) {
                %>
                <fmt:message key="the.following.technical.information.is.also.available.regarding.this.error"/>
                <br><br>
                <%
                    for (StackTraceElement aTrace : trace) {
                        if (aTrace != null) {
                %>
                <%=aTrace.toString()%><br>
                <%
                            }
                        }
                    }
                } else {

                    String errorMsg = request.getParameter(DataSourceClientConstants.ERROR_MSG);
                    if (errorMsg != null) {
                %>
                <div><fmt:message key="error"/> <%=errorMsg%>
                </div>
                <%
                } else {
                %>

                <fmt:message key="further.information.is.not.available.at.this.time"/>
                <% }
                }%>
            </div>
        </div>

    </div>
</fmt:bundle>

