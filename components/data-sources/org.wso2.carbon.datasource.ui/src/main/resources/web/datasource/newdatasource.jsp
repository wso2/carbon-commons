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
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type="text/javascript" src="global-params.js"></script>
<script type="text/javascript" src="dscommon.js"></script>

<fmt:bundle basename="org.wso2.carbon.datasource.ui.i18n.Resources">
<carbon:breadcrumb label="new.data.source"
                   resourceBundle="org.wso2.carbon.datasource.ui.i18n.Resources"
                   topPage="false"
                   request="<%=request%>"/>
<form method="post" name="dscreationform" id="dscreationform"
      action="savedatasource.jsp">

<div id="middle">
<h2><fmt:message key="new.data.source"/></h2>

<div id="workArea">
<table class="styledLeft noBorders" cellspacing="0" cellpadding="0" border="0">
<thead>
<tr>
    <th colspan="3"><fmt:message key="new.data.source"/></th>
</tr>
</thead>
<tbody>
<tr>
    <td style="width:170px;"><fmt:message key="name"/><span class='required'>*</span></td>
    <td align="left">
        <input id="alias" name="alias" class="longInput"/>
    </td>
</tr>
<tr>
    <td><fmt:message key="driver"/><span class='required'>*</span></td>
    <td align="left">
        <input id="driver" name="driver" class="longInput"/>
    </td>
</tr>
<tr>
    <td><fmt:message key="url"/><span class='required'>*</span></td>
    <td align="left">
        <input id="url" name="url" class="longInput"/>
    </td>
</tr>
<tr>
    <td><fmt:message key="user.name"/></td>
    <td align="left">
        <input id="user" name="user" class="longInput"/>
    </td>
</tr>
<tr>
    <td><fmt:message key="password"/></td>
    <td align="left">
        <input id="password" name="password" type="password" class="longInput"/>
    </td>
</tr>
<tr>
    <td colspan="2" class="middle-header"><fmt:message key="datasource.pool.implementation"/></td>
</tr>
<tr>
    <td><fmt:message key="pool.type"/></td>
    <td>
        <input type="radio" name="dstype" value="basicds" checked="true"/>
        <fmt:message key="basicdatasource"/>

        <input type="radio" name="dstype" value="peruserds"/>
        <fmt:message key="peruserpooldatasource"/>
    </td>
</tr>

<tr>
    <td colspan="2" class="middle-header"><fmt:message key="data.source.repository.type"/></td>
</tr>
<tr>
    <td><fmt:message key="repository.type"/></td>
    <td>
        <input type="radio" name="dsrepotype" value="InMemory" checked="true"
               onclick="setRepository('InMemory')"/>
        <fmt:message key="inmemory"/>

        <input type="radio" name="dsrepotype" value="JNDI" onclick="setRepository('JNDI')"/>
        <fmt:message key="jndi"/>
        <input type="hidden" name="dsrepotype_hidden" id="dsrepotype_hidden" value="InMemory"/>
    </td>
</tr>
<tr id="jndiICFactory" style="display:none;">
    <td><fmt:message key="ic.factory"/></td>
    <td>
        <input id="icFactory" name="icFactory" type="text" value=""/>
    </td>
</tr>
<tr id="jndiProviderType" style="display:none;">
    <td colspan="2">
        <input type="radio" name="providerType" id="providerType" value="port"
               onclick="setProviderType('port')" checked="true"/>
            <fmt:message key="provider.port"/>
        <input type="radio" name="providerType" id="providerType" value="url"
               onclick="setProviderType('url')"/>
            <fmt:message key="provider.url"/>
        <input type="hidden" name="providerType_hidden" id="providerType_hidden" value="port"/>
    <td>
</tr>
<tr id="jndiProviderPort" style="display:none;">
    <td><fmt:message key="provider.port"/></td>
    <td>
        <input id="providerPort" name="providerPort" type="text" value=""/>
    </td>
</tr>
<tr id="jndiProviderUrl" style="display:none;">
    <td><fmt:message key="provider.url"/></td>
    <td>
        <input id="providerUrl" name="providerUrl" type="text" value=""/>
    </td>
</tr>
<tr>
    <td colspan="2" class="middle-header"><fmt:message
            key="data.source.configuration.parameters"/></td>

</tr>

<tr>
    <td><fmt:message key="max.active"/></td>
    <td align="left">
        <input id="maxActive" name="maxActive" onclick="clearStatus('maxActive')" type="text"
               value="( int )"/>
    </td>
</tr>
<tr>
    <td><fmt:message key="max.idle"/></td>
    <td align="left">
        <input id="maxIdle" name="maxIdle" onclick="clearStatus('maxIdle')" type="text"
               value="( int )"/>
    </td>
</tr>
<tr>
    <td><fmt:message key="max.wait"/><label><fmt:message
            key="measurement.milliseconds"/></label></td>
    <td align="left">
        <input id="maxWait" name="maxWait" onclick="clearStatus('maxWait')" type="text"
               value="( long )"/>
    </td>
</tr>
<tr>
    <td><fmt:message key="min.idle"/></td>
    <td align="left">
        <input id="minIdle" name="minIdle" onclick="clearStatus('minIdle')" type="text"
               value="( int )"/><label>
    </td>
</tr>
<tr>
    <td><fmt:message key="initial.size"/></td>
    <td align="left">
        <input id="initialsize" name="initialsize" onclick="clearStatus('initialsize')" type="text"
               value="( int )"/><label>
    </td>
</tr>
<tr>
    <td><fmt:message key="max.open.statements"/></td>
    <td align="left">
        <input id="maxopenstatements" name="maxopenstatements"
               onclick="clearStatus('maxopenstatements')" type="text" value="( int )"/>
    </td>
</tr>
<tr>
    <td><fmt:message key="transaction.isolation"/></td>
    <td align="left">
        <select id="isolation" name="isolation">
            <option value="TRANSACTION_UNKNOWN">TRANSACTION_UNKNOWN</option>
            <option value="TRANSACTION_NONE">TRANSACTION_NONE</option>
            <option value="TRANSACTION_READ_COMMITTED">TRANSACTION_READ_COMMITTED</option>
            <option value="TRANSACTION_READ_UNCOMMITTED">TRANSACTION_READ_UNCOMMITTED</option>
            <option value="TRANSACTION_REPEATABLE_READ">TRANSACTION_REPEATABLE_READ</option>
            <option value="TRANSACTION_SERIALIZABLE">TRANSACTION_SERIALIZABLE</option>
        </select>
    </td>
</tr>
<tr>
    <td><fmt:message key="auto.commit"/></td>
    <td align="left">
        <select id="autocommit" name="autocommit">
            <option value="true"><fmt:message key="true"/></option>
            <option value="false"><fmt:message key="false"/></option>
        </select>
    </td>
</tr>
<tr>
    <td><fmt:message key="pool.statements"/></td>
    <td align="left">
        <select id="poolstatements" name="poolstatements">
            <option value="true"><fmt:message key="true"/></option>
            <option value="false"><fmt:message key="false"/></option>
        </select>
    </td>
</tr>
<tr>
    <td><fmt:message key="remove.abandoned"/></td>
    <td align="left">
        <select id="removeAbandoned" name="removeAbandoned">
            <option value="false"><fmt:message key="false"/></option>
            <option value="true"><fmt:message key="true"/></option>
        </select>
    </td>
</tr>
<tr>
    <td><fmt:message key="remove.abandoned.timeout"/><label><fmt:message
            key="measurement.milliseconds"/></label></td>
    <td align="left">
        <input id="removeAbandonedTimeout" name="removeAbandonedTimeout"
               onclick="clearStatus('removeAbandonedTimeout')" type="text"
               value="( long )"/>
    </td>
</tr>
<tr>
    <td><fmt:message key="log.abandoned"/></td>
    <td align="left">
        <select id="logAbandoned" name="logAbandoned">
            <option value="false"><fmt:message key="false"/></option>
            <option value="true"><fmt:message key="true"/></option>
        </select>
    </td>
</tr>
<tr>
    <td><fmt:message key="test.on.borrow"/></td>
    <td align="left">
        <select id="testonborrow" name="testonborrow">
            <option value="true"><fmt:message key="true"/></option>
            <option value="false"><fmt:message key="false"/></option>
        </select>
    </td>
</tr>
<tr>
    <td><fmt:message key="test.while.idle"/></td>
    <td align="left">
        <select id="testwhileidle" name="testwhileidle">
            <option value="false"><fmt:message key="false"/></option>
            <option value="true"><fmt:message key="true"/></option>
        </select>
    </td>
</tr>
<tr>
    <td><fmt:message key="validation.query"/></td>
    <td align="left">
        <input id="validationquery" name="validationquery" type="text"/>
        <input class="button" id="testConnectionButton" name="testConnectionButton" type="button"
               value="Test Connection"
               onclick="testConnection('<fmt:message key="ds.name.cannotfound.msg"/>','<fmt:message
                       key="ds.name.invalid.msg"/>','<fmt:message
                       key="ds.driver.cannotfound.msg"/>','<fmt:message
                       key="ds.url.cannotfound.msg"/>','<fmt:message
                       key="ds.testquery.cannotfound.msg"/>','<fmt:message
                       key="ds.healthy.connection"/>')"/></td>
</tr>
<tr>
    <td class="buttonRow" colspan="3">
        <input class="button" type="button"
               value="<fmt:message key="add"/>"
               onclick="dsSave('<fmt:message key="ds.name.cannotfound.msg"/>','<fmt:message
                       key="ds.name.invalid.msg"/>','<fmt:message
                       key="ds.driver.cannotfound.msg"/>','<fmt:message
                       key="ds.url.cannotfound.msg"/>',document.dscreationform); return false;"/>
        <input class="button" type="reset" value="<fmt:message key="cancel"/>"
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
