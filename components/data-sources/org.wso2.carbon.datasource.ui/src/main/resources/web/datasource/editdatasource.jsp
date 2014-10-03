<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
-->
<%@ page import="org.apache.synapse.commons.datasource.DataSourceConstants" %>
<%@ page import="org.apache.synapse.commons.datasource.DataSourceInformation" %>
<%@ page import="org.wso2.carbon.datasource.ui.DataSourceClientConstants" %>
<%@ page import="org.wso2.carbon.datasource.ui.DataSourceManagementHelper" %>
<%@ page import="org.wso2.carbon.datasource.ui.DatasourceManagementClient" %>
<%@ page import="org.wso2.securevault.secret.SecretInformation" %>
<%@ page import="java.util.Properties" %>
<%@ page import="org.apache.synapse.commons.datasource.DataSourceInformation" %>
<%@ page import="org.apache.synapse.commons.datasource.DataSourceConstants" %>
<%@ page import="org.wso2.securevault.secret.SecretInformation" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type="text/javascript" src="global-params.js"></script>
<script type="text/javascript" src="dscommon.js"></script>
<%
    String name = request.getParameter("alias");
    if (name == null || "".equals(name)) {
        throw new ServletException("Name is empty");
    }

    DatasourceManagementClient client;
    try {
        client = DatasourceManagementClient.getInstance(config, session);

        name = name.trim();
        DataSourceInformation information = client.getDataSourceInformation(name);
        if (information != null) {
            boolean isBasicDS = !DataSourceInformation.PER_USER_POOL_DATA_SOURCE.equals(information.getType());
            boolean isInMemory = !DataSourceConstants.PROP_REGISTRY_JNDI.equals(information.getRepositoryType());
            String icFactory = "";
            String providerPort = "";
            String providerUrl = "";
            if (!isInMemory) {
                StringBuffer buffer = new StringBuffer();
                buffer.append(DataSourceConstants.PROP_SYNAPSE_PREFIX_DS);
                buffer.append(DataSourceConstants.DOT_STRING);
                buffer.append(name.trim());
                buffer.append(DataSourceConstants.DOT_STRING);
                // The prefix for root level jndiProperties
                String rootPrefix = buffer.toString();
                Properties jndiEnv = information.getProperties();
                icFactory = DataSourceManagementHelper.getProperty(jndiEnv, rootPrefix + DataSourceConstants.PROP_IC_FACTORY, "");
                providerUrl = DataSourceManagementHelper.getProperty(jndiEnv, rootPrefix + DataSourceConstants.PROP_PROVIDER_URL, "");
                providerPort = DataSourceManagementHelper.getProperty(jndiEnv, rootPrefix + DataSourceConstants.PROP_PROVIDER_PORT, "");
            }
            String isolation = DataSourceManagementHelper.toStringIsolation(information.getDefaultTransactionIsolation());
            String displayStyle = isInMemory ? "display:none;" : "";
            String providerPortDisplayStyle = displayStyle;
            String providerUrlDisplayStyle = displayStyle;
            String validationQuery = information.getValidationQuery();
            SecretInformation secretInformation = information.getSecretInformation();
            if (validationQuery == null) {
                validationQuery = "";
            }
            if (!isInMemory) {
                if (providerPort != null && !"".equals(providerPort)) {
                    providerPortDisplayStyle = "";
                } else {
                    providerUrlDisplayStyle = "";
                }
            }
%>
<fmt:bundle basename="org.wso2.carbon.datasource.ui.i18n.Resources">
<carbon:breadcrumb label="edit.datasource"
                   resourceBundle="org.wso2.carbon.datasource.ui.i18n.Resources"
                   topPage="false" request="<%=request%>"/>
<form method="post" name="dseditform" id="dseditform"
      action="savedatasource.jsp">

<div id="middle">
<h2><fmt:message key="edit.datasource"/> <%=name%>
</h2>

<div id="workArea">
<table class="styledLeft noBorders" cellspacing="0" cellpadding="0" border="0">
<thead>
<tr>
    <th colspan="3"><fmt:message key="edit.datasource"/> <%=name%>
    </th>
</tr>
</thead>
<tbody>
<tr>
    <td style="width:170px;"><fmt:message key="name"/><span class='required'>*</span></td>
    <td align="left">
        <input id="alias" name="alias" class="longInput" value="<%=name%>" disabled="true"/>
        <input id="alias_hidden" name="alias_hidden" type="hidden" value="<%=name%>"/>
    </td>
</tr>
<tr>
    <td><fmt:message key="driver"/><span class='required'>*</span></td>
    <td align="left">
        <input id="driver" name="driver" class="longInput"
               value="<%=information.getDriver()==null?"":information.getDriver()%>"/>
    </td>
</tr>
<tr>
    <td><fmt:message key="url"/><span class='required'>*</span></td>
    <td align="left">
        <input id="url" name="url" class="longInput"
               value="<%=information.getUrl()==null?"":information.getUrl()%>"/>
    </td>
</tr>
<tr>
    <td><fmt:message key="user.name"/></td>
    <td align="left">
        <input id="user" name="user" class="longInput"
               value="<%=secretInformation == null || secretInformation.getUser()==null?"":secretInformation.getUser()%>"/>
    </td>
</tr>
<tr>
    <td><fmt:message key="password"/></td>
    <td align="left">
        <input id="password" name="password" type="password" class="longInput"
               value="<%=secretInformation == null || secretInformation.getAliasSecret()==null?"":secretInformation.getAliasSecret()%>"/>

    </td>
</tr>
<tr>
    <td colspan="2" class="middle-header"><fmt:message key="data.source.pool.implementation"/></td>
</tr>
<tr>
    <td><fmt:message key="pool.type"/></td>
    <td>
        <% if (isBasicDS) {%>
        <input type="radio" name="dstype" value="basicds" checked="true"/>
        <fmt:message key="basicdatasource"/>

        <input type="radio" name="dstype" value="peruserds"/>
        <fmt:message key="peruserpooldatasource"/>
        <% } else {%>
        <input type="radio" name="dstype" value="basicds"/>
        <fmt:message key="basicdatasource"/>
        <input type="radio" name="dstype" value="peruserds" checked="true"/>
        <fmt:message key="peruserpooldatasource"/>
        <%} %>
    </td>
</tr>

<tr>
    <td colspan="2" class="middle-header"><fmt:message key="data.source.repository.type"/></td>
</tr>
<tr>
    <td><fmt:message key="repository.type"/></td>
    <td>
        <% if (isInMemory) {%>
        <input type="radio" name="dsrepotype" value="InMemory" checked="true"
               onclick="setRepository('InMemory');"/>
        <fmt:message key="inmemory"/>

        <input type="radio" name="dsrepotype" value="JNDI" onclick="setRepository('JNDI');"/>
        <fmt:message key="jndi"/>
        <input type="hidden" name="dsrepotype_hidden" id="dsrepotype_hidden" value="InMemory"/>
        <% } else {%>
        <input type="radio" name="dsrepotype" value="InMemory"
               onclick="setRepository('InMemory');"/>
        <fmt:message key="inmemory"/>

        <input type="radio" name="dsrepotype" value="JNDI" checked="true"
               onclick="setRepository('JNDI');"/>
        <fmt:message key="jndi"/>
        <input type="hidden" name="dsrepotype_hidden" id="dsrepotype_hidden" value="JNDI"/>
        <%} %>
    </td>
</tr>

<tr id="jndiICFactory" style="<%=displayStyle%>">
    <td><fmt:message key="ic.factory"/></td>
    <td>
        <input id="icFactory" name="icFactory" type="text" value="<%=icFactory%>"/>
    </td>
</tr>
<tr id="jndiProviderType" style="<%=displayStyle%>">
    <td colspan="2">
                <%if (providerUrl == null || "".equals(providerUrl)) {%>
        <input type="radio" name="providerType" id="providerType" value="port"
               onclick="setProviderType('port');" checked="true"/>
            <fmt:message key="provider.port"/>
        <input type="radio" name="providerType" id="providerType" value="url"
               onclick="setProviderType('url');"/>
            <fmt:message key="provider.url"/>
        <input type="hidden" name="providerType_hidden" id="providerType_hidden" value="port"/>
                <%} else { %>
        <input type="radio" name="providerType" id="providerType" value="port"
               onclick="setProviderType('port');"/>
            <fmt:message key="provider.port"/>
        <input type="radio" name="providerType" id="providerType" value="url"
               onclick="setProviderType('url');" checked="true"/>
            <fmt:message key="provider.url"/>
        <input type="hidden" name="providerType_hidden" id="providerType_hidden" value="url"/>
                <%}%>
    <td>
</tr>
<tr id="jndiProviderPort" style="<%=providerPortDisplayStyle%>">
    <td><fmt:message key="provider.port"/></td>
    <td>
        <input id="providerPort" name="providerPort" type="text" value="<%=providerPort%>"/>
    </td>
</tr>
<tr id="jndiProviderUrl" style="<%=providerUrlDisplayStyle%>">
    <td><fmt:message key="provider.url"/></td>
    <td>
        <input id="providerUrl" name="providerUrl" type="text" value="<%=providerUrl%>"/>
    </td>
</tr>
<tr>
    <td colspan="2" class="middle-header"><fmt:message
            key="data.source.configuration.parameters"/></td>
</tr>

<tr>
    <td><fmt:message key="max.active"/></td>
    <td align="left">
        <input id="maxActive" name="maxActive" type="text" value="<%=information.getMaxActive()%>"/>
    </td>
</tr>
<tr>
    <td><fmt:message key="max.idle"/></td>
    <td align="left">
        <input id="maxIdle" name="maxIdle" type="text" value="<%=information.getMaxIdle()%>"/>
    </td>
</tr>
<tr>
    <td><fmt:message key="max.wait"/><label><fmt:message key="measurement.milliseconds"/></label>
    </td>
    <td align="left">
        <input id="maxWait" name="maxWait" type="text" value="<%=information.getMaxWait()%>"/>
    </td>
</tr>
<tr>
    <td><fmt:message key="min.idle"/></td>
    <td align="left">
        <input id="minIdle" name="minIdle" type="text" value="<%=information.getMinIdle()%>"/>
    </td>
</tr>
<tr>
    <td><fmt:message key="initial.size"/></td>
    <td align="left">
        <input id="initialsize" name="initialsize" type="text"
               value="<%=information.getInitialSize()%>"/>
    </td>
</tr>
<tr>
    <td><fmt:message key="max.open.statements"/></td>
    <td align="left">
        <input id="maxopenstatements" name="maxopenstatements" type="text"
               value="<%=information.getMaxOpenPreparedStatements()%>"/>
    </td>
</tr>
<tr>
    <td><fmt:message key="transaction.isolation"/></td>
    <td align="left">

        <select id="isolation" name="isolation">
            <% if ("TRANSACTION_NONE".equals(isolation)) {%>
            <option value="TRANSACTION_NONE" selected="true">TRANSACTION_NONE</option>
            <% } else {%>
            <option value="TRANSACTION_NONE">TRANSACTION_NONE</option>
            <%} %>
            <% if ("TRANSACTION_UNKNOWN".equals(isolation)) {%>
            <option value="TRANSACTION_UNKNOWN" selected="true">TRANSACTION_UNKNOWN</option>
            <% } else {%>
            <option value="TRANSACTION_UNKNOWN">TRANSACTION_UNKNOWN</option>
            <%} %>
            <% if ("TRANSACTION_READ_COMMITTED".equals(isolation)) {%>
            <option value="TRANSACTION_READ_COMMITTED" selected="true">TRANSACTION_READ_COMMITTED
            </option>
            <% } else {%>
            <option value="TRANSACTION_READ_COMMITTED">TRANSACTION_READ_COMMITTED</option>
            <%} %>
            <% if ("TRANSACTION_READ_UNCOMMITTED".equals(isolation)) {%>
            <option value="TRANSACTION_READ_UNCOMMITTED" selected="true">
                TRANSACTION_READ_UNCOMMITTED
            </option>
            <% } else {%>
            <option value="TRANSACTION_READ_UNCOMMITTED">TRANSACTION_READ_UNCOMMITTED</option>
            <%} %>
            <% if ("TRANSACTION_REPEATABLE_READ".equals(isolation)) {%>
            <option value="TRANSACTION_REPEATABLE_READ" selected="true">
                TRANSACTION_REPEATABLE_READ
            </option>
            <% } else {%>
            <option value="TRANSACTION_REPEATABLE_READ">TRANSACTION_REPEATABLE_READ</option>
            <%} %>
            <% if ("TRANSACTION_SERIALIZABLE".equals(isolation)) {%>
            <option value="TRANSACTION_SERIALIZABLE" selected="true">TRANSACTION_SERIALIZABLE
            </option>
            <% } else {%>
            <option value="TRANSACTION_SERIALIZABLE">TRANSACTION_SERIALIZABLE</option>
            <%} %>
        </select>
    </td>
</tr>
<tr>
    <td><fmt:message key="auto.commit"/></td>
    <td align="left">
        <select id="autocommit" name="autocommit">
            <% if (information.isDefaultAutoCommit()) {%>
            <option value="true" selected="true"><fmt:message
                    key="true"/></option>
            <option value="false"><fmt:message
                    key="false"/></option>
            <%} else {%>
            <option value="false" selected="true"><fmt:message
                    key="false"/></option>
            <option value="true"><fmt:message
                    key="true"/></option>
            <%}%>
        </select>
    </td>
</tr>
<tr>
    <td><fmt:message key="pool.statements"/></td>
    <td align="left">
        <select id="poolstatements" name="poolstatements">
            <% if (information.isPoolPreparedStatements()) {%>
            <option value="true" selected="true"><fmt:message
                    key="true"/></option>
            <option value="false"><fmt:message
                    key="false"/></option>
            <%} else {%>
            <option value="false" selected="true"><fmt:message
                    key="false"/></option>
            <option value="true"><fmt:message
                    key="true"/></option>
            <%}%>
        </select>
    </td>
</tr>
<tr>
    <td><fmt:message key="remove.abandoned"/></td>
    <td align="left">
        <select id="removeAbandoned" name="removeAbandoned">
            <% if (information.isRemoveAbandoned()) {%>
            <option value="true" selected="true"><fmt:message
                    key="true"/></option>
            <option value="false"><fmt:message
                    key="false"/></option>
            <%} else {%>
            <option value="false" selected="true"><fmt:message
                    key="false"/></option>
            <option value="true"><fmt:message
                    key="true"/></option>
            <%}%>
        </select>
    </td>
</tr>
<tr>
    <td><fmt:message key="remove.abandoned.timeout"/><label><fmt:message
            key="measurement.milliseconds"/></label></td>
    <td align="left">
        <input id="removeAbandonedTimeout" name="removeAbandonedTimeout" type="text"
               value="<%=information.getRemoveAbandonedTimeout()%>"/>
    </td>
</tr>
<tr>
    <td><fmt:message key="log.abandoned"/></td>
    <td align="left">
        <select id="logAbandoned" name="logAbandoned">
            <% if (information.isLogAbandoned()) {%>
            <option value="true" selected="true"><fmt:message
                    key="true"/></option>
            <option value="false"><fmt:message
                    key="false"/></option>
            <%} else {%>
            <option value="false" selected="true"><fmt:message
                    key="false"/></option>
            <option value="true"><fmt:message
                    key="true"/></option>
            <%}%>
        </select>
    </td>
</tr>
<tr>
    <td><fmt:message key="test.on.borrow"/></td>
    <td align="left">
        <select id="testonborrow" name="testonborrow">
            <% if (information.isTestOnBorrow()) {%>
            <option value="true" selected="true"><fmt:message
                    key="true"/></option>
            <option value="false"><fmt:message
                    key="false"/></option>
            <%} else {%>
            <option value="false" selected="true"><fmt:message
                    key="false"/></option>
            <option value="true"><fmt:message
                    key="true"/></option>
            <%}%>
        </select>
    </td>
</tr>
<tr>
    <td><fmt:message key="test.while.idle"/></td>
    <td align="left">
        <select id="testwhileidle" name="testwhileidle">
            <% if (information.isTestWhileIdle()) {%>
            <option value="true" selected="true"><fmt:message
                    key="true"/></option>
            <option value="false"><fmt:message
                    key="false"/></option>
            <%} else {%>
            <option value="false" selected="true"><fmt:message
                    key="false"/></option>
            <option value="true"><fmt:message
                    key="true"/></option>
            <%}%>
        </select>
    </td>
</tr>
<tr>
    <td><fmt:message key="validation.query"/></td>
    <td align="left">
        <input id="validationquery" name="validationquery" type="text"
               value="<%=validationQuery%>"/>
        <input class="button" id="testConnectionButton" name="testConnectionButton" type="button"
               value="Test Connection"
               onclick="testConnection('<fmt:message key="ds.name.cannotfound.msg"/>', '<fmt:message
                       key="ds.name.invalid.msg"/>', '<fmt:message
                       key="ds.driver.cannotfound.msg"/>','<fmt:message
                       key="ds.url.cannotfound.msg"/>','<fmt:message
                       key="ds.testquery.cannotfound.msg"/>','<fmt:message
                       key="ds.healthy.connection"/>');"/>
    </td>
</tr>

<tr>
    <td class="buttonRow" colspan="3">
        <input type="hidden" name="saveMode" id="saveMode" value="edit"/>
        <input class="button" type="button"
               value="<fmt:message key="save"/>"
               onclick="dsSave('<fmt:message key="ds.name.cannotfound.msg"/>','<fmt:message
                       key="ds.name.invalid.msg"/>','<fmt:message
                       key="ds.driver.cannotfound.msg"/>','<fmt:message
                       key="ds.url.cannotfound.msg"/>',document.dseditform); return false;"/>
        <input class="button" type="button" value="<fmt:message key="cancel"/>"
               onclick="document.location.href='index.jsp'"/>
    </td>
</tr>

</table>
<script type="text/javascript">
    autoSelect();
</script>
</div>
</div>
</form>
</fmt:bundle>
<%
    }
} catch (Throwable e) {
    request.getSession().setAttribute(DataSourceClientConstants.EXCEPTION, e);
%>
<script type="text/javascript">
    jQuery(document).ready(function () {
        CARBON.showErrorDialog('<%=e.getMessage()%>');
    });
</script>
<%
        return;
    }
%>
