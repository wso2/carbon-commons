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
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.application.mgt.ui.ApplicationAdminClient" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.application.mgt.stub.types.carbon.ApplicationMetadata" %>
<%@ page import="org.wso2.carbon.application.mgt.stub.types.carbon.ServiceGroupMetadata" %>
<%@ page import="org.wso2.carbon.application.mgt.stub.types.carbon.RegistryMetadata" %>
<%@ page import="org.wso2.carbon.application.mgt.stub.types.carbon.Association" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    String appName = CharacterEncoder.getSafeText(request.getParameter("appName"));
    request.setAttribute("appName", appName);

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
            .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    String BUNDLE = "org.wso2.carbon.application.mgt.ui.i18n.Resources";
    ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    ApplicationMetadata metadata = null;

    try {
        ApplicationAdminClient client = new ApplicationAdminClient(cookie,
                backendServerURL, configContext, request.getLocale());
        metadata = client.getAppData(appName);
    } catch (Exception e) {
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
    }

    boolean foundDataServices = false;
    boolean foundOtherServices = false;

%>


<fmt:bundle basename="org.wso2.carbon.application.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="carbonapps.application.dashboard"
                       resourceBundle="org.wso2.carbon.application.mgt.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

<script type="text/javascript">

//    function deleteArtifact(artifactName, artifactType, action) {
        <%--CARBON.showConfirmationDialog("<fmt:message key="confirm.delete.artifact"/>" , function(){--%>
//            document.appInfoForm.action = action + "?artifactName="
                    <%--+ artifactName + "&artifactType=" + artifactType + "&appName=<%= appName%>";--%>
//            document.appInfoForm.submit();
//        });
//    }

//    function deleteModule(moduleName, moduleVersion) {
        <%--CARBON.showConfirmationDialog("<fmt:message key="confirm.delete.artifact"/>" , function(){--%>
//            document.appInfoForm.action = "delete_artifact.jsp?artifactName="
                    <%--+ moduleName + "&moduleVersion=" + moduleVersion + "&appName=<%= appName%>";--%>
//            document.appInfoForm.submit();
//        });
//    }

</script>

    <div id="middle">
        <h2><fmt:message key="carbonapps.application.dashboard"/> : <%= appName%></h2>

        <div id="workArea">
            <form action="" name="appInfoForm" method="post">
                <%
                    if (metadata != null) {
                        ServiceGroupMetadata[] serviceGroups = metadata.getServiceGroups();
                        if (serviceGroups != null && serviceGroups.length > 0) {
                            for (ServiceGroupMetadata serviceGroup : serviceGroups) {
                                if ("service/dataservice".equals(serviceGroup.getSgType())) {
                                    foundDataServices = true;
                                } else {
                                    foundOtherServices = true;
                                }
                            }
                            if (foundOtherServices) {

                %>

                <table class="styledLeft" id="servicesTable" width="60%">
                    <thead>
                    <tr>
                        <th width="50%"><img src="images/service.gif" alt="" style="vertical-align:middle;">&nbsp;<fmt:message key="carbonapps.service.groups"/></th>
                        <th><fmt:message key="carbonapps.services"/></th>
                        <%--<th><fmt:message key="carbonapps.actions"/></th>--%>
                    </tr>
                    </thead>
                    <tbody>
                    <%
                        for (ServiceGroupMetadata serviceGroup : serviceGroups) {
                            if (!"service/dataservice".equals(serviceGroup.getSgType())) {
                    %>
                    <tr>
                        <td rowspan="<%= serviceGroup.getServices().length%>">
                            <a href="../service-mgt/list_service_group.jsp?serviceGroupName=<%= serviceGroup.getSgName()%>"><%= serviceGroup.getSgName()%></a>
                        </td>
                        <%
                            int serviceCount = 0;
                            for (String service : serviceGroup.getServices()) {
                                if (serviceCount != 0) {
                        %>
                        <tr>
                        <%
                            }
                        %>
                            <td><a href="../service-mgt/service_info.jsp?serviceName=<%= service%>"><%= service%></a></td>
                        <% if (serviceCount != 0) {%>
                        </tr>
                    <%
                                    }
                                    serviceCount++;
                                }
                            }
                        }
                    %>
                    </tbody>
                </table>

                <%
                    }
                    if (foundDataServices) { %>
                <p>&nbsp;&nbsp;</p>
                <table class="styledLeft" id="dataservicesTable" width="60%">
                    <thead>
                    <tr>
                        <th width="50%"><img src="../data_service/images/type.gif" alt="" style="vertical-align:middle;">&nbsp;<fmt:message key="carbonapps.ds.service.groups"/></th>
                        <th><fmt:message key="carbonapps.ds.services"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <%
                        for (ServiceGroupMetadata serviceGroup : serviceGroups) {
                            if ("service/dataservice".equals(serviceGroup.getSgType())) {
                    %>
                    <tr>
                        <td rowspan="<%= serviceGroup.getServices().length%>">
                            <a href="../service-mgt/list_service_group.jsp?serviceGroupName=<%= serviceGroup.getSgName()%>"><%= serviceGroup.getSgName()%></a>
                        </td>
                        <%
                                int serviceCount = 0;
                                for (String service : serviceGroup.getServices()) {
                                    if (serviceCount != 0) {
                        %>
                        <tr>
                        <%
                                    }
                        %>
                            <td><a href="../service-mgt/service_info.jsp?serviceName=<%= service%>"><%= service%></a></td>
                        <%          if (serviceCount != 0) {%>
                        </tr>
                    <%
                                    }
                                    serviceCount++;
                                }
                            }
                        }
                    %>
                    </tbody>
                </table>
                <%
                            }
                        }
                    RegistryMetadata[] regMeta = metadata.getRegistryArtifacts();
                    if (regMeta != null && regMeta.length > 0) {
                %>
                <p>&nbsp;&nbsp;</p>
                <table class="styledLeft" id="RegistryArtifactsTable" width="100%">
                    <thead>
                    <tr>
                        <th width="15%"><nobr><fmt:message key="carbonapps.registry.artifact"/></nobr></th>
                        <th width="20%"><fmt:message key="carbonapps.registry.resources"/></th>
                        <th width="20%"><fmt:message key="carbonapps.registry.collections"/></th>
                        <th width="20%"><fmt:message key="carbonapps.registry.dumps"/></th>
                        <th width="25%"><fmt:message key="carbonapps.registry.associations"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <%
                        for (RegistryMetadata meta : regMeta) {
                    %>
                    <tr>
                        <td><%= meta.getArtifactName()%></td>
                        <td style="padding:0; !important">
                            <table style="padding:0;border:0; !important">
                                <%
                                    if (meta.getResources() != null) {
                                        for (String path : meta.getResources()) {
                                %>
                                <tr><td style="padding:0;border:0; !important"><nobr><%= path%></nobr></td></tr>
                                <%
                                        }
                                    }
                                %>
                            </table>
                        </td>
                        <td>
                            <table style="padding:0;border:0; !important">
                                <%
                                    if (meta.getCollections() != null) {
                                        for (String path : meta.getCollections()) {
                                %>
                                <tr><td style="padding:0;border:0; !important"><nobr><%= path%></nobr></td></tr>
                                <%
                                        }
                                    }
                                %>
                            </table>
                        </td>
                        <td>
                            <table style="padding:0;border:0; !important">
                                <%
                                    if (meta.getDumps() != null) {
                                        for (String path : meta.getDumps()) {
                                %>
                                <tr><td style="padding:0;border:0; !important"><nobr><%= path%></nobr></td></tr>
                                <%
                                        }
                                    }
                                %>
                            </table>
                        </td>
                        <td>
                            <table style="padding:0;border:0; !important">
                                <%
                                    if (meta.getAssociations() != null) {
                                        for (Association association : meta.getAssociations()) {
                                %>
                                <tr><td style="padding:0;border:0; !important"><nobr><%= association.getSourcePath()%> : <%= association.getTargetPath()%></nobr></td></tr>
                                <%
                                        }
                                    }
                                %>
                            </table>
                        </td>
                    </tr>
                    <%
                        }
                    %>
                    </tbody>
                </table>
                <%
                    }
                    String[] registryFilters = metadata.getRegistryFilters();
                    if (registryFilters != null && registryFilters.length > 0) {
                %>
                <p>&nbsp;&nbsp;</p>
                <table class="styledLeft" id="RegistryFiltersTable" width="40%">
                    <thead>
                    <tr>
                        <th><img src="images/filter.gif" alt="" style="vertical-align:middle;">&nbsp;<fmt:message key="carbonapps.registry.filters"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <%
                        for (String filter : registryFilters) {
                    %>
                    <tr>
                        <td><%= filter%></td>
                    </tr>
                    <%
                        }
                    %>
                    </tbody>
                </table>
                <%
                    }
                    String[] registryHandlers = metadata.getRegistryHandlers();
                    if (registryHandlers != null && registryHandlers.length > 0) {
                %>
                <p>&nbsp;&nbsp;</p>
                <table class="styledLeft" id="RegistryHandlersTable" width="40%">
                    <thead>
                    <tr>
                        <th><img src="images/handler.gif" alt="" style="vertical-align:middle;">&nbsp;<fmt:message key="carbonapps.registry.handlers"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <%
                        for (String handler : registryHandlers) {
                    %>
                    <tr>
                        <td><%= handler%></td>
                    </tr>
                    <%
                        }
                    %>
                    </tbody>
                </table>
                <%
                        }
                    }
                    if (CarbonUIUtil.isContextRegistered(config, "/war-apps/")) {
                %>
                <jsp:include page="../war-apps/index.jsp"/>
                <jsp:include page="../war-apps/jaxws_info.jsp"/>
                <%
                    }

                    if (CarbonUIUtil.isContextRegistered(config, "/synapse-apps/")) {
                %>
                <jsp:include page="../synapse-apps/index.jsp"/>
                <%
                    }

                    if (CarbonUIUtil.isContextRegistered(config, "/bpel-apps/")) {
                %>
                <jsp:include page="../bpel-apps/index.jsp"/>
                <%
                    }

                    if (CarbonUIUtil.isContextRegistered(config, "/humantask-apps/")) {
                %>
                <jsp:include page="../humantask-apps/index.jsp"/>
                <%
                    }
                %>
            </form>
        </div>
    </div>

</fmt:bundle>
