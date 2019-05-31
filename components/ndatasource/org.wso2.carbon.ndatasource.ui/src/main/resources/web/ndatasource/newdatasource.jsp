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
<%@page import="org.wso2.carbon.ndatasource.ui.NDataSourceClientConstants"%>
<%@page import="org.wso2.carbon.ndatasource.ui.config.DSXMLConfiguration"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%@ page import="org.wso2.carbon.ndatasource.ui.config.RDBMSDSXMLConfiguration"%>
<%@ page import="org.wso2.carbon.ndatasource.ui.stub.core.services.xsd.WSDataSourceMetaInfo_WSDataSourceDefinition"%>
<%@ page import="org.wso2.carbon.ndatasource.ui.stub.core.services.xsd.WSDataSourceMetaInfo" %>
<%@ page import="org.wso2.carbon.ndatasource.ui.stub.core.services.xsd.WSDataSourceInfo" %>
<%@ page import="org.wso2.carbon.ndatasource.ui.stub.core.xsd.JNDIConfig" %>
<%@ page import="org.wso2.carbon.ndatasource.ui.stub.core.xsd.JNDIConfig_EnvEntry" %>
<%@ page import="org.wso2.carbon.ndatasource.ui.NDataSourceAdminServiceClient" %>
<%@ page import="org.wso2.carbon.ndatasource.ui.NDataSourceHelper" %>
<%@ page import="org.wso2.carbon.ndatasource.ui.config.RDBMSDSXMLConfiguration" %>
<%@ page import="org.wso2.carbon.ndatasource.ui.config.RDBMSDSXMLConfiguration.DataSourceProperty" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.owasp.encoder.Encode" %>

<script type="text/javascript" src="global-params.js"></script>
<script type="text/javascript" src="dscommon.js"></script>
<!--Yahoo includes for dom event handling-->
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>

<!--EditArea javascript syntax hylighter -->
<script language="javascript" type="text/javascript" src="../editarea/edit_area_full.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<fmt:bundle basename="org.wso2.carbon.ndatasource.ui.i18n.Resources">
<%
	String dsProvider = "default"; 	
	WSDataSourceInfo dataSourceInfo = null;
	WSDataSourceMetaInfo dataSourceMetaInfo = null;
	WSDataSourceMetaInfo_WSDataSourceDefinition dataSourceDefinition = null;
		
	String dataSourceName = request.getParameter("dsName");
	String description = request.getParameter("description");
	boolean editMode = (("true".equals(request.getParameter("edit"))) ? true : false );
	String type = "RDBMS";
	boolean isSystem = false;
	String dataSourceclassName = null;
	String dsproviderPropertiesEditMode = null;
	String givenDataSourceProps = null;
	String driverClassName = null;
	String url = null;
	String username = null;
	String password = null;
	String jndiConfigName = null;
	String givenJNDIProps = null;
	String jndiPropertiesEditMode = null;
	boolean isUseDataSourceFactory = false;
	Boolean defaultAutoCommit = false;
	Boolean defaultReadOnly = false;
	String defaultTransactionIsolation = null;
	String defaultCatalog = null;
	Integer maxActive = null;
	Integer minIdle = null;
	Integer maxIdle = null;
	Integer initialSize = null;
	Integer maxWait = null;
	Boolean testOnBorrow = false;
	Boolean testOnReturn = false;
	Boolean testWhileIdle = false;
	String validationquery = null;
	String validatorClassName = null;
	Integer timeBetweenEvictionRunsMillis = null;
	Integer numTestsPerEvictionRun = null;
	Integer minEvictableIdleTimeMillis = null;
	Boolean accessToUnderlyingConnectionAllowed = false;
	Boolean removeAbandoned = false;
	Integer removeAbandonedTimeout = null;
	Boolean logAbandoned = false;
	String connectionProperties = null;
	String initSQL = null;
	String jdbcInterceptors = null;
	Long validationInterval = null;
	Boolean fairQueue = false;
	Boolean jmxEnabled = false;
	Integer abandonWhenPercentageFull = null;
	Long maxAge = null;
	Boolean useEquals =false;
	Integer suspectTimeout = null;
	Integer validationQueryTimeout = null;
	Boolean alternateUsernameAllowed = false;
    String rdbmsEngineType = "#";
	
	if (dataSourceName != null && !dataSourceName.equals("")) {
		NDataSourceAdminServiceClient client = NDataSourceAdminServiceClient.getInstance(config, session);
		dataSourceInfo = client.getDataSource(dataSourceName);
	} else {
		dataSourceName = "";
	}
	//edit mode
	if (dataSourceInfo != null && editMode) {
		dataSourceMetaInfo = dataSourceInfo.getDsMetaInfo();
		dataSourceDefinition = dataSourceMetaInfo.getDefinition();
		type = dataSourceMetaInfo.getDefinition().getType();
		type = (type == null) ? "" : type;
		isSystem = dataSourceMetaInfo.getSystem();
		description = dataSourceMetaInfo.getDescription();
				
		String configuration = dataSourceDefinition.getDsXMLConfiguration();
				
		if (type.equals("RDBMS")) {
			RDBMSDSXMLConfiguration rdbmsCon = (RDBMSDSXMLConfiguration)NDataSourceHelper.unMarshal(type, configuration);
			dataSourceclassName = rdbmsCon.getDataSourceClassName();
			if (dataSourceclassName != null && !("".equals(dataSourceclassName))) {
				dsProvider = NDataSourceClientConstants.RDBMS_EXTERNAL_DATASOURCE_PROVIDER;
				dsproviderPropertiesEditMode = "true";
				List <DataSourceProperty> dataSourceProperties = rdbmsCon.getDataSourceProps();
				Iterator<DataSourceProperty> iterator = dataSourceProperties.iterator();
				DataSourceProperty currentProperty = iterator.next();
				givenDataSourceProps = currentProperty.getName() + "," + currentProperty.getValue();
				while (iterator.hasNext()) {
					currentProperty = iterator.next();
					givenDataSourceProps = givenDataSourceProps + "::" + currentProperty.getName() + "," + currentProperty.getValue();
				}
			} else {
				dsProvider = "default";
				driverClassName = rdbmsCon.getDriverClassName();
				url = rdbmsCon.getUrl();
                rdbmsEngineType = NDataSourceHelper.getRDBMSEngine(url);
				username = rdbmsCon.getUsername();
				password = rdbmsCon.getPassword().getValue();
			}
			//datasource properties
			defaultAutoCommit = rdbmsCon.getDefaultAutoCommit();
			defaultReadOnly = rdbmsCon.getDefaultReadOnly();
			defaultTransactionIsolation = rdbmsCon.getDefaultTransactionIsolation();
			defaultCatalog = rdbmsCon.getDefaultCatalog();
			maxActive = rdbmsCon.getMaxActive();
			minIdle = rdbmsCon.getMinIdle();
			maxIdle = rdbmsCon.getMaxIdle();
			initialSize = rdbmsCon.getInitialSize();
			maxWait = rdbmsCon.getMaxWait();
			testOnBorrow = rdbmsCon.getTestOnBorrow();
			testOnReturn = rdbmsCon.getTestOnReturn();
			testWhileIdle = rdbmsCon.getTestWhileIdle();
			validationquery = rdbmsCon.getValidationQuery();
			validatorClassName = rdbmsCon.getValidatorClassName();;
			timeBetweenEvictionRunsMillis = rdbmsCon.getTimeBetweenEvictionRunsMillis();
			numTestsPerEvictionRun = rdbmsCon.getNumTestsPerEvictionRun();
			minEvictableIdleTimeMillis = rdbmsCon.getMinEvictableIdleTimeMillis();
			accessToUnderlyingConnectionAllowed = rdbmsCon.getAccessToUnderlyingConnectionAllowed();
			removeAbandoned = rdbmsCon.getRemoveAbandoned();
			removeAbandonedTimeout = rdbmsCon.getRemoveAbandonedTimeout();
			logAbandoned = rdbmsCon.getLogAbandoned();
			connectionProperties = rdbmsCon.getConnectionProperties();
			initSQL = rdbmsCon.getInitSQL();
			jdbcInterceptors = rdbmsCon.getJdbcInterceptors();
			validationInterval = rdbmsCon.getValidationInterval();
			fairQueue = rdbmsCon.getFairQueue(); 
			jmxEnabled = rdbmsCon.getJmxEnabled();
			abandonWhenPercentageFull = rdbmsCon.getAbandonWhenPercentageFull();
			maxAge = rdbmsCon.getMaxAge();
			useEquals = rdbmsCon.getUseEquals();
			suspectTimeout = rdbmsCon.getSuspectTimeout();
			validationQueryTimeout = rdbmsCon.getValidationQueryTimeout();
			alternateUsernameAllowed = rdbmsCon.getAlternateUsernameAllowed(); 
		}
	
		//load JNDI configuration 
		JNDIConfig jndiConfig = dataSourceMetaInfo.getJndiConfig();
		if (jndiConfig != null) {
			jndiConfigName = jndiConfig.getName();
			isUseDataSourceFactory = jndiConfig.getUseDataSourceFactory();
			JNDIConfig_EnvEntry[] envEntries = jndiConfig.getEnvironment();
			if (envEntries != null && envEntries.length > 0) {
				if (envEntries[0] != null){
					jndiPropertiesEditMode = "true";
					givenJNDIProps = envEntries[0].getName() + "," + envEntries[0].getValue();
				}
				for (int i = 1; i < envEntries.length; i++) {
					givenJNDIProps = givenJNDIProps + "::" + envEntries[i].getName() + "," + envEntries[i].getValue();
				} 
			}  
		}
	}
	
	//Data Source Provider is changed
	if (request.getParameter("dsProvider") != null) {
		dsProvider = request.getParameter("dsProvider");
	}
	dsProvider = (dsProvider == null) ? "default" : dsProvider;
	
	description = (description == null) ? "" : description;
	dataSourceclassName = (dataSourceclassName == null) ? "" : dataSourceclassName;
	driverClassName = (driverClassName == null) ? "" : driverClassName;
	url = (url == null) ? "" : url;
	username = (username == null) ? "" : username;
	password = (password == null) ? "" : password;
	jndiConfigName = (jndiConfigName == null) ? "" : jndiConfigName;
	defaultCatalog = (defaultCatalog == null) ? "" : defaultCatalog;
	validationquery = (validationquery == null) ? "" : validationquery;
	validatorClassName = (validatorClassName == null) ? "" : validatorClassName;
	connectionProperties = (connectionProperties == null) ? "" : connectionProperties;
	initSQL = (initSQL == null) ? "" : initSQL;
	jdbcInterceptors = (jdbcInterceptors == null) ? "" : jdbcInterceptors;
	defaultAutoCommit = (defaultAutoCommit == null) ? false : defaultAutoCommit;
	defaultReadOnly = (defaultReadOnly == null) ? false : defaultReadOnly;
	testOnBorrow = (testOnBorrow == null) ? false : testOnBorrow;
	testOnReturn = (testOnReturn == null) ? false : testOnReturn;
	testWhileIdle = (testWhileIdle == null) ? false : testWhileIdle;
	accessToUnderlyingConnectionAllowed = (accessToUnderlyingConnectionAllowed == null) ? false : accessToUnderlyingConnectionAllowed;
	removeAbandoned = (removeAbandoned == null) ? false : removeAbandoned;
	logAbandoned = (logAbandoned == null) ? false : logAbandoned;
	fairQueue = (fairQueue == null) ? false : fairQueue;
	jmxEnabled = (jmxEnabled == null) ? false : jmxEnabled;
	alternateUsernameAllowed = (alternateUsernameAllowed == null) ? false : alternateUsernameAllowed;
	useEquals = (useEquals == null) ? false : useEquals;
	%>
<%if (editMode) { %>
	<carbon:breadcrumb label="edit.data.source" resourceBundle="org.wso2.carbon.ndatasource.ui.i18n.Resources"
                   topPage="false"
                   request="<%=request%>"/>
 <%} else { %>
 	<carbon:breadcrumb label="new.data.source" resourceBundle="org.wso2.carbon.ndatasource.ui.i18n.Resources"
                   topPage="false"
                   request="<%=request%>"/>
 <%} %>
 <script type="text/javascript">
 function addJNDIProps() {
    //check to see if there are empty fields left
    var theTable = document.getElementById('jndiPropertyTable');
    var inputs = theTable.getElementsByTagName('input');
    for(var i=0; i<inputs.length; i++){
        if(inputs[i].value == ""){
            CARBON.showErrorDialog("<fmt:message key="empty.key.or.value"/>");
            return;
        }
    }
    addServiceParamRow("", "", "jndiPropertyTable", "deleteJNDIPropRow");
    if(document.getElementById('jndiPropertyTable').style.display == "none"){
        document.getElementById('jndiPropertyTable').style.display = "";
    }
}

function addDataSourceProperties() {
	//check to see if there are empty fields left
    var theTable = document.getElementById('dsPropertyTable');
    var inputs = theTable.getElementsByTagName('input');
    for(var i=0; i<inputs.length; i++){
        if(inputs[i].value == ""){
            CARBON.showErrorDialog("<fmt:message key="empty.key.or.value"/>");
            return;
        }
    }
    addServiceParamRow("", "", "dsPropertyTable", "deleteDataSourcePropRow");
    if(document.getElementById('dsPropertyTable').style.display == "none"){
        document.getElementById('dsPropertyTable').style.display = "";
    }
}

function populateDataSourceProperties() {
	//edit mode
	var str = '<%=givenDataSourceProps%>'; 
	if (str == 'null' && document.getElementById('dsproviderProperties') != null) {
		str = document.getElementById('dsproviderProperties').value;
	}
	if (str != '' && str != 'null') {
		var params;
        params = str.split("::");
        var i, param;
        for (i = 0; i < params.length; i++) {
            param = params[i].split(",");
            addServiceParamRow(param[0], param[1], "dsPropertyTable", "deleteDataSourcePropRow");
        }
    }
}

function populateJNDIProperties() {
	//edit mode
	var str = '<%=givenJNDIProps%>'; 
	if (str == 'null' && document.getElementById('jndiProperties') != null) {
		str = document.getElementById('jndiProperties').value;
	}
	if (str != '' && str != 'null') {
		var params;
        params = str.split("::");
        var i, param;
        for (i = 0; i < params.length; i++) {
            param = params[i].split(",");
            addServiceParamRow(param[0], param[1], "jndiPropertyTable", "deleteJNDIPropRow");
        }
    }
}

function deleteJNDIPropRow(index) {
    CARBON.showConfirmationDialog("<fmt:message key="confirm.property.deletion"/>" , function() {
        document.getElementById('jndiPropertyTable').deleteRow(index);
        if (document.getElementById('jndiPropertyTable').rows.length == 1) {
            document.getElementById('jndiPropertyTable').style.display = 'none';
        }
    });
}

function deleteDataSourcePropRow(index) {
    CARBON.showConfirmationDialog("<fmt:message key="confirm.property.deletion"/>" , function() {
        document.getElementById('dsPropertyTable').deleteRow(index);
        if (document.getElementById('dsPropertyTable').rows.length == 1) {
            document.getElementById('dsPropertyTable').style.display = 'none';
        }
    });
}

function extractDataSourceProps() {
	var i;
    var str = '';
    var dsPropertyTable = document.getElementById("dsPropertyTable");
    for(var j= 1;j<dsPropertyTable.rows.length;j++){
        var parmName = dsPropertyTable.rows[j].getElementsByTagName("input")[0].value;
        var parmValue = dsPropertyTable.rows[j].getElementsByTagName("input")[1].value;
        if(parmName == "" || parmValue == ""){
        	return;
        }
        if (j == 1) {
            str += parmName + ',' + parmValue;
        }else{
            str += '::' + parmName + ',' + parmValue;                
        }
    }
    document.dscreationform.dsproviderProperties.value = str;
  }

function extractJndiProps() {
    var i;
    var str = '';
    var jndiPropertyTable = document.getElementById("jndiPropertyTable");
    for(var j= 1;j<jndiPropertyTable.rows.length;j++){
        var parmName = jndiPropertyTable.rows[j].getElementsByTagName("input")[0].value;
        var parmValue = jndiPropertyTable.rows[j].getElementsByTagName("input")[1].value;
        if(parmName == "" || parmValue == ""){
            return;
        }
        if (j == 1) {
            str += parmName + ',' + parmValue;
        }else{
            str += '::' + parmName + ',' + parmValue;                
        }
    }

    document.dscreationform.jndiProperties.value = str;
}

function changeDataSourceProvider (obj, document) {
	var selectedDSProvider =  obj[obj.selectedIndex].value;
	var dsName = document.getElementById("dsName").value;
	var description = document.getElementById("description").value;
	var editMode = document.getElementById("editMode").value
	var query = 'dsProvider='+selectedDSProvider;
	
	if (dsName != null && dsName != "") {
		query = query + '&dsName='+dsName;
	}
	if (description != null && description != ""){
		query = query + '&description='+description;
	}
	if (editMode != null && editMode == "true") {
		query = query + '&edit='+editMode;
	}
	location.href = 'newdatasource.jsp?'+query;	
}

function dsSave(namemsg, invalidnamemsg, drivermsg, urlmsg, customdsmsg, form) {
	
	document.getElementById('configContent').value = editAreaLoader.getValue("configuration");

    if (!isDSValid(namemsg, invalidnamemsg, drivermsg, urlmsg, customdsmsg)) {
        return false;
    }
    
    if (document.getElementById("jndiPropertyTable") != null) {
    	extractJndiProps();
    } 
    
    if (document.getElementById("dsPropertyTable") != null) {
    	extractDataSourceProps();
    }
      
    form.submit();
    return true;
}

function showConfigEditor() {
	document.getElementById("rdbmsConfigUI").style.display = 'none';
	document.getElementById("configEditor").style.display = "";
	document.getElementById("showConfigTR").style.display = 'none';
}

function changeConfigView(obj, document) {
	var selectedDsType =  obj[obj.selectedIndex].value;
	if (selectedDsType == 'rdbms') {
		document.getElementById("rdbmsConfigUI").style.display = '';
		document.getElementById("configEditor").style.display = 'none';
		document.getElementById("customDSTypeRow").style.display = 'none';
		document.getElementById("configView").value = "false";
		document.getElementById("dsType").value = "RDBMS";
	} else {
		editAreaLoader.init({
	        id : "configuration"        // textarea id
	        ,syntax: "xml"            // syntax to be uses for highgliting
	        ,start_highlight: true        // to display with highlight mode on start-up
	        ,allow_resize: "both"
	        ,min_height:250
	    });
		document.getElementById("rdbmsConfigUI").style.display = 'none';
		document.getElementById("configEditor").style.display = "";
		document.getElementById("customDSTypeRow").style.display = "";
		document.getElementById("configView").value = "true";
		document.getElementById("dsType").value = "Custom";
	}
}

function loadConfigView() {
	var editMode = document.getElementById("editMode").value;
	var dsType = document.getElementById("dsType").value;
	if (editMode == 'false' || dsType == 'RDBMS') {
		document.getElementById("configEditor").style.display = 'none';
		document.getElementById("rdbmsConfigUI").style.display = '';
		document.getElementById("customDSTypeRow").style.display = 'none';
		document.getElementById("configView").value = "false";
	} else {
		editAreaLoader.init({
	        id : "configuration"        // textarea id
	        ,syntax: "xml"            // syntax to be uses for highgliting
	        ,start_highlight: true        // to display with highlight mode on start-up
	        ,allow_resize: "both"
	        ,min_height:250
	    });
		document.getElementById("rdbmsConfigUI").style.display = 'none';
		document.getElementById("configEditor").style.display = "";
		document.getElementById("customDSTypeRow").style.display = "";
		document.getElementById("configView").value = "true";
	}
}

function displayPasswordField() {
	if(document.getElementById('changePassword').checked) {
		document.getElementById('newPasswordRow').style.display = "";
	} else {
		document.getElementById('newPasswordRow').style.display = "none";
	}
}

 function setJDBCValues(obj, document) {
     var selectedValue = obj[obj.selectedIndex].value;
     var jdbcUrl = selectedValue.substring(0, selectedValue.indexOf("#"));
     var driverClass = selectedValue.substring(selectedValue.indexOf("#") + 1, selectedValue.length);
     document.getElementById('url').value = jdbcUrl;
     document.getElementById('driver').value = driverClass;
 }

</script>
<form method="post" name="dscreationform" id="dscreationform"
      action="savedatasource.jsp" >

<div id="middle">
<h2>
<%if (editMode) { %>
      <%if (isSystem) { %>
        <fmt:message key="view.data.source"/>
      <%} else { %>
        <fmt:message key="edit.data.source"/>
      <%} %>
      <%} else { %>
       <fmt:message key="new.data.source"/>
<%} %>
</h2>

<div id="workArea">
<table class="styledLeft noBorders" cellspacing="0" cellpadding="0" border="0" style="border-bottom:none">
<thead>
    <tr>
        <th colspan="3">
        <%if (editMode) { %>
        	<%if (isSystem) { %>
        		<fmt:message key="view.data.source"/>
        	<%} else { %>
        		<fmt:message key="edit.data.source"/>
        	<%} %>
        <%} else { %>
        	<fmt:message key="new.data.source"/>
        <%} %>
        </th>
    </tr>
</thead>
<tbody>
<tr>
<td style="width:170px;"><fmt:message key="dsType"/><span class='required'>*</span></td>
	<td align="left">
		 <select id="dsTypeSelector" name="dsTypeSelector"
                        onchange="changeConfigView(this,document)">
                    <% if (!editMode) { %>
	                    <option value="rdbms" selected="selected">RDBMS</option>
	                    <option value="Custom">Custom</option>
                    <% } else if (type.equals(NDataSourceClientConstants.RDBMS_DTAASOURCE_TYPE)) {%>
                    	<option value="rdbms" selected="selected">RDBMS</option>
                    <% } else { %>
	                    <option value="rdbms">RDBMS</option>
	                    <option value="Custom" selected="selected">Custom</option>
                    <% } %>
         </select>
		<input type="hidden" id="dsType" name="dsType" value="<%=type %>"/>
		<input type="hidden" id="configView" name="configView"/>
    </td>
</tr>
<tr id="customDSTypeRow" style="display:none">
	<td style="width:170px;"><fmt:message key="customDSType"/><span class='required'>*</span></td>
	<td>
		<%if(!editMode) { %>
			<input id="customDsType" name="customDsType"/>
		<%} else { %>
			<input id="customDsType" name="customDsType" value="<%=Encode.forHtml(type) %>"/>
		<%} %>
	</td>
</tr>
<tr>
	<td style="width:170px;"><fmt:message key="name"/><span class='required'>*</span></td>
    <td align="left">
    	 <input type="hidden" id="isSystem" name="isSystem" value="<%=isSystem %>"/>
    	 <%if (editMode) { %>
            <input id="dsName" name=dsName class="longInput" value="<%=Encode.forHtml(dataSourceName) %>" readonly>
        <%} else { %>
        	<input id="dsName" name=dsName class="longInput" value="<%=Encode.forHtml(dataSourceName) %>">
        <%} %>
        <input type="hidden" id="editMode" name="editMode" value="<%=editMode %>"/>
    </td>
</tr>
<tr>
    <td style="width:170px;"><fmt:message key="description"/></td>
    <td align="left">
        <input id="description" name="description" class="longInput" value="<%=Encode.forHtml(description) %>" />
    </td>
</tr>
</tbody>
</table>
<table id="rdbmsConfigUI" class="styledLeft noBorders" cellspacing="0" cellpadding="0" border="0" style="border-top:none;display:none">
<tbody>
<tr>
    <td style="width:170px;"><fmt:message key="datasource.provider"/><span class='required'>*</span></td>
    <td align="left">
        <select id="datasourceProvider" name="datasourceProvider"
                        onchange="changeDataSourceProvider(this,document)">
                    <% if (dsProvider.equals("default")) { %>
                    <option value="default" selected="selected">default</option>
                    <% } else { %>
                    <option value="default">default</option>
                    <% } %>
                    <% if(dsProvider.equals(NDataSourceClientConstants.RDBMS_EXTERNAL_DATASOURCE_PROVIDER)) { %>
                   	<option value="<%=NDataSourceClientConstants.RDBMS_EXTERNAL_DATASOURCE_PROVIDER%>"
                            selected="selected"><%=NDataSourceClientConstants.RDBMS_EXTERNAL_DATASOURCE_PROVIDER%></option>
                   	<% } else { %>
                   	<option value="<%=NDataSourceClientConstants.RDBMS_EXTERNAL_DATASOURCE_PROVIDER%>">
                        <%=NDataSourceClientConstants.RDBMS_EXTERNAL_DATASOURCE_PROVIDER%></option>
                   	<% } %>
         </select>
         <input type="hidden" id="dsProviderType" name="dsProviderType" value="<%=Encode.forJavaScript(dsProvider)%>"/>
         <input type="hidden" id="dsproviderProperties" name="dsproviderProperties" class="longInput"/>
         <input type="hidden" id="dsproviderPropertiesHidden" name="dsproviderPropertiesHidden" class="longInput" value="<%=dsproviderPropertiesEditMode %>"/>
    </td>
</tr>
<% if ("default".equals(dsProvider)) { %>
<tr>
    <td class="leftCol-small" style="white-space: nowrap;"><label><fmt:message key="datasource.engine"/><font
            color="red">*</font></label></td>
    <td>
        <select name="databaseEngine" id="databaseEngine"
                onchange="javascript:setJDBCValues(this,document);return false;">

            <%if (("#".equals(rdbmsEngineType)|| rdbmsEngineType.equals(""))) {%>
            <option value="#" selected="selected">--SELECT--</option>
            <%} else {%>
            <option value="#">--SELECT--</option>
            <%}%>

            <%if ("mysql".equals(rdbmsEngineType)) {%>
            <option selected="selected"
                    value="jdbc:mysql://[machine-name/ip]:[port]/[database-name]#com.mysql.jdbc.Driver">
                MySQL
            </option>
            <%} else {%>
            <option value="jdbc:mysql://[machine-name/ip]:[port]/[database-name]#com.mysql.jdbc.Driver">
                MySQL
            </option>
            <%}%>

            <%if ("derby".equals(rdbmsEngineType)) {%>
            <option selected="selected"
                    value="jdbc:derby:[path-to-data-file]#org.apache.derby.jdbc.EmbeddedDriver">
                Apache Derby
            </option>
            <%} else {%>
            <option value="jdbc:derby:[path-to-data-file]#org.apache.derby.jdbc.EmbeddedDriver">
                Apache Derby
            </option>
            <%}%>

            <%if ("mssqlserver".equals(rdbmsEngineType)) {%>
            <option selected="selected"
                    value="jdbc:sqlserver://[HOST]:[PORT1433];databaseName#com.microsoft.sqlserver.jdbc.SQLServerDriver">
                Microsoft SQL Server
            </option>
            <%} else {%>
            <option value="jdbc:sqlserver://[HOST]:[PORT1433];databaseName=[DB]#com.microsoft.sqlserver.jdbc.SQLServerDriver">
                Microsoft SQL Server
            </option>
            <%}%>

            <%if ("oracle".equals(rdbmsEngineType)) {%>
            <option selected="selected"
                    value="jdbc:oracle:[drivertype]:[username/password]@[host]:[port]/[database]#oracle.jdbc.driver.OracleDriver">
                Oracle
            </option>
            <%} else {%>
            <option value="jdbc:oracle:[drivertype]:[username/password]@[host]:[port]/[database]#oracle.jdbc.driver.OracleDriver">
                Oracle
            </option>
            <%}%>

            <%if ("db2".equals(rdbmsEngineType)) {%>
            <option selected="selected" value="jdbc:db2:[database]#com.ibm.db2.jcc.DB2Driver">IBM
                                                                                              DB2
            </option>
            <%} else {%>
            <option value="jdbc:db2:[database]#com.ibm.db2.jcc.DB2Driver">IBM DB2</option>
            <%}%>

            <%if ("hsqldb".equals(rdbmsEngineType)) {%>
            <option selected="selected" value="jdbc:hsqldb:[path]#org.hsqldb.jdbcDriver">HSQLDB
            </option>
            <%} else {%>
            <option value="jdbc:hsqldb:[path]#org.hsqldb.jdbcDriver">HSQLDB</option>
            <%}%>
            <%if ("informix-sqli".equals(rdbmsEngineType)) {%>
            <option selected="selected"
                    value="jdbc:informix-sqli://[HOST]:[PORT]/[database]:INFORMIXSERVER=[server-name]#com.informix.jdbc.IfxDriver">
                Informix
            </option>
            <%} else {%>
            <option value="jdbc:informix-sqli://[HOST]:[PORT]/[database]:INFORMIXSERVER=[server-name]#com.informix.jdbc.IfxDriver">
                Informix
            </option>
            <%}%>

            <%if ("postgresql".equals(rdbmsEngineType)) {%>
            <option selected="selected"
                    value="jdbc:postgresql://[HOST]:[PORT5432]/[database]#org.postgresql.Driver">
                PostgreSQL
            </option>
            <%} else {%>
            <option value="jdbc:postgresql://[HOST]:[PORT5432]/[database]#org.postgresql.Driver">
                PostgreSQL
            </option>
            <%}%>

            <%if ("sybase".equals(rdbmsEngineType)) {%>
            <option selected="selected"
                    value="jdbc:sybase:Tds:[HOST]:[PORT2048]/[database]#com.sybase.jdbc3.jdbc.SybDriver">
                Sybase ASE
            </option>
            <%} else {%>
            <option value="jdbc:sybase:Tds:[HOST]:[PORT2048]/[database]#com.sybase.jdbc3.jdbc.SybDriver">
                Sybase ASE
            </option>
            <%}%>

            <%if ("h2".equals(rdbmsEngineType)) {%>
            <option selected="selected" value="jdbc:h2:tcp:[HOST]:[PORT]/[database]#org.h2.Driver">
                H2
            </option>
            <%} else {%>
            <option value="jdbc:h2:tcp:[HOST]:[PORT]/[database]#org.h2.Driver">H2</option>
            <%}%>

            <%if ("Generic".equals(rdbmsEngineType)) {%>
            <option selected="selected" value="Generic#Generic">Generic</option>
            <%} else {%>
            <option value="Generic#Generic">Generic</option>
            <%}%>
        </select>
    </td>
</tr>
<tr>
    <td><fmt:message key="driver"/><span class='required'>*</span></td>
    <td align="left">
        <input id="driver" name="driver" class="longInput" value="<%=Encode.forHtml(driverClassName) %>"/>
    </td>
</tr>
<tr>
    <td><fmt:message key="url"/><span class='required'>*</span></td>
    <td align="left">
        <input id="url" name="url" class="longInput" value="<%=Encode.forHtml(url) %>"/>
    </td>
</tr>
<tr>
    <td><fmt:message key="user.name"/></td>
    <td align="left">
        <input id="username" name="username" class="longInput" value="<%=Encode.forHtml(username) %>"/>
    </td>
</tr>
<% if (!editMode) { %>
	<tr>
    <td><fmt:message key="password"/></td>
    <td align="left">
        <input id="password" name="password" type="password" class="longInput" autocomplete="off"/>
    </td>
	</tr>
<%} else if (!isSystem){%>
	<tr>
		<td><label for="changePassword"><fmt:message  key="change.password"/></label></td>
		<td><input type="checkbox" id="changePassword" name="changePassword" onclick="displayPasswordField()"/>
		</td>
	</tr>
<%}%> 
	
<tr id="newPasswordRow" style="display:none">
	<td><label for="changePassword"><fmt:message  key="password"/></label></td>
	<td><input type="password" id="newPassword" name="newPassword" class="longInput" autocomplete="off"/></td>
</tr>
	

<% } else if (NDataSourceClientConstants.RDBMS_EXTERNAL_DATASOURCE_PROVIDER.equals(dsProvider)){ %>
<tr>
    <td><fmt:message key="datasource.className"/><span class='required'>*</span></td>
    <td align="left">
        <input id="dsclassname" name="dsclassname" class="longInput" value="<%=Encode.forHtml(dataSourceclassName) %>"/>
    </td>
</tr>
<tr>
	<td>
       <fmt:message key="datasource.properties"/>
    </td>
    <td >
		<div id="nameValueAdd">
		<%if (!isSystem) { %>
			<a class="icon-link"
	           href="#addNameLink"
	           onclick="addDataSourceProperties();"
	           style="background-image: url(../admin/images/add.gif);"><fmt:message key="jndi.properties.add"/></a>
	    <%} %>
	    	<div style="clear:both;"></div>
	    </div>
	    <div>
	         <table cellpadding="0" cellspacing="0" border="0" class="styledLeft"
	                          id="dsPropertyTable"
	                          style="display:none;">
	                <thead>
	                    <tr>
	                        <th style="width:40%"><fmt:message key="prop.name"/></th>
	                        <th style="width:40%"><fmt:message key="prop.value"/></th>
	                        <th style="width:20%"><fmt:message key="prop.action"/></th>
	                    </tr>
	                </thead>
	         		<tbody>
	         		</tbody>
	          </table>
	          
	     </div>
	</td>
</tr>
<% } %>
<!-- JNDI NEW -->
<tr>
    <td colspan="2" class="middle-header">
    	<a onclick="showJNDIConfigurations()" class="icon-link" style="background-image:url(images/plus.gif);"
                         href="#jndiConfig" id="jndiconfigheader"></a>
    	<fmt:message key="datasource.jndi.config"/>
    </td>
</tr>
<tr id="jndiconfig" style="display:none">
    <td colspan="2">
        <table id="showJNDIConfigurations" width="100%">
        	<tr>
    <td style="width:170px;"><fmt:message key="jndi.name"/></td>
    <td align="left">
    	<input id="jndiname" name="jndiname" class="longInput" value="<%=Encode.forHtml(jndiConfigName) %>" />
    	<input type="hidden" id="jndiPropertiesHidden" name="jndiPropertiesHidden" class="longInput" value="<%=jndiPropertiesEditMode %>"/>
    </td>
</tr>
<tr>
	<td><label for="useDataSourceFactory"><fmt:message  key="jndi.use.data.source.factory"/></label></td>
	<td><% if (NDataSourceClientConstants.RDBMS_EXTERNAL_DATASOURCE_PROVIDER.equals(dsProvider)) { %>
	<input type="checkbox" id="useDataSourceFactory" name="useDataSourceFactory" disabled="disabled"/>
	<%} else if (isUseDataSourceFactory) {%>
		<input type="checkbox" id="useDataSourceFactory" name="useDataSourceFactory" checked/>
	<%} else {%>
		<input type="checkbox" id="useDataSourceFactory" name="useDataSourceFactory"/>
	<% } %>
	<input type="hidden" id="jndiProperties" name="jndiProperties" class="longInput"/></td>
</tr>

<tr>
	<td>
       <fmt:message key="jndi.properties"/>
    </td>
    <td >
		<div id="nameValueAdd">
		<%if (!isSystem) { %>
			<a class="icon-link"
	           href="#addNameLink"
	           onclick="addJNDIProps();"
	           style="background-image: url(../admin/images/add.gif);"><fmt:message key="jndi.properties.add"/></a>
	    <%} %>
	    	<div style="clear:both;"></div>
	    </div>
	    <div>
	         <table cellpadding="0" cellspacing="0" border="0" class="styledLeft"
	                          id="jndiPropertyTable"
	                          style="display:none;">
	                <thead>
	                    <tr>
	                        <th style="width:40%"><fmt:message key="prop.name"/></th>
	                        <th style="width:40%"><fmt:message key="prop.value"/></th>
	                        <th style="width:20%"><fmt:message key="prop.action"/></th>
	                    </tr>
	                </thead>
	         		<tbody></tbody>
	          </table>
	    </div>
	    
	  </td>
</tr>
        	
        </table>
    </td>
</tr>

<tr>
    <td colspan="2" class="middle-header">
    	<a onclick="showPropConfigurations()" class="icon-link" style="background-image:url(images/plus.gif);"
                         href="#dsProperties" id="dsProperties"></a>
    	<fmt:message key="data.source.configuration.parameters"/>
    </td>
</tr>
<tr id="dsPropFields" style="display:none">
    <td colspan="2">
        <table id="showPropConfigurations">
        	<tr>
			    <td><fmt:message key="default.auto.commit"/></td>
			    <td align="left">
			        <select id="defaultAutoCommit" name="defaultAutoCommit">
			        	<% if (defaultAutoCommit) {%>
			        	 	<option value="false"><fmt:message key="false"/></option>
			             	<option value="true" selected="true"><fmt:message key="true"/></option>
			             <%} else { %>
			             	<option value="false" selected="true"><fmt:message key="false"/></option>
			             	<option value="true" ><fmt:message key="true"/></option>
			             <%} %>
			 		</select>
			    </td>
			</tr>
			<tr>
			    <td><fmt:message key="default.read.only"/></td>
			    <td align="left">
			        <select id="defaultReadOnly" name="defaultReadOnly">
			        	<% if (defaultReadOnly) {%>
			        		<option value="false"><fmt:message key="false"/></option>
			            	<option value="true" selected="true"><fmt:message key="true"/></option>
			             <%} else { %>
			             	<option value="false" selected="true"><fmt:message key="false"/></option>
			            	<option value="true" ><fmt:message key="true"/></option>
			             <%} %>
			        </select>
			    </td>
			</tr>
			<tr>
			    <td><fmt:message key="default.transaction.isolation"/></td>
			    <td align="left">
			        <select id="defaultTransactionIsolation" name="defaultTransactionIsolation">
			        	<%if ("NONE".equals(defaultTransactionIsolation)) { %>
			            	<option value="NONE" selected="true">NONE</option>
			            <%} else { %>
			            	<option value="NONE">NONE</option>
			            <%} %>
			            <%if ("READ_COMMITTED".equals(defaultTransactionIsolation)) { %>
			            	<option value="READ_COMMITTED" selected="true">READ_COMMITTED</option>
			            <%} else { %>
			            	<option value="READ_COMMITTED">READ_COMMITTED</option>
			            <%} %>
			            <%if ("READ_UNCOMMITTED".equals(defaultTransactionIsolation)) { %>
			            	<option value="READ_UNCOMMITTED" selected="true">READ_UNCOMMITTED</option>
			            <%} else { %>
			            	<option value="READ_UNCOMMITTED">READ_UNCOMMITTED</option>
			            <%} %>
			            <%if ("REPEATABLE_READ".equals(defaultTransactionIsolation)) { %>
			            	<option value="REPEATABLE_READ" selected="true">REPEATABLE_READ</option>
			            <%} else { %>
			             	<option value="REPEATABLE_READ">REPEATABLE_READ</option>
			            <%} %>
			            <%if ("SERIALIZABLE".equals(defaultTransactionIsolation)) { %>
			            	<option value="SERIALIZABLE" selected="true">SERIALIZABLE</option>
			            <%} else { %>
			             	<option value="SERIALIZABLE">SERIALIZABLE</option>
			            <%} %>
			        </select>
			    </td>
			</tr>
			<tr>
			    <td><fmt:message key="default.catalogn"/></td>
			    <td align="left">
			        <input id="defaultCatalog" name="defaultCatalog" type="text" value="<%=Encode.forHtml(defaultCatalog) %>"/>
			    </td>
			</tr>
			<tr>
			    <td><fmt:message key="max.active"/></td>
			    <td align="left">
			    	<%if (maxActive != null) { %>
			        	<input id="maxActive" name="maxActive" type="text"
			               value="<%=maxActive %>"/>
			         <%} else { %>
			        	<input id="maxActive" name="maxActive" onclick="clearStatus('maxActive')" type="text"
			               value="( int )"/>
			         <%} %>
			    </td>
			</tr>
			<tr>
			    <td><fmt:message key="max.idle"/></td>
			    <td align="left">
			    	<%if (maxIdle != null) { %>
			        	<input id="maxIdle" name="maxIdle" type="text"
			               value="<%=maxIdle %>"/>
			        <%} else { %>
			        	<input id="maxIdle" name="maxIdle" onclick="clearStatus('maxIdle')" type="text"
			               value="( int )"/>
			        <%} %>
			    </td>
			</tr>
			<tr>
			    <td><fmt:message key="min.idle"/></td>
			    <td align="left">
			    	<%if (minIdle != null) { %>
			        	<input id="minIdle" name="minIdle" onclick="clearStatus('minIdle')" type="text"
			               value="<%=minIdle %>"/>
			        <%} else { %>
			        	<input id="minIdle" name="minIdle" onclick="clearStatus('minIdle')" type="text"
			               value="( int )"/>
			        <%} %>
			    </td>
			</tr>
			<tr>
			    <td><fmt:message key="initial.size"/></td>
			    <td align="left">
			    	<%if (initialSize != null) { %>
			        	<input id="initialSize" name="initialSize" onclick="clearStatus('initialSize')" type="text"
			               value="<%=initialSize %>"/>
			         <%} else { %>
			         	<input id="initialSize" name="initialSize" onclick="clearStatus('initialSize')" type="text"
			               value="( int )"/>
			         <%} %>
			    </td>
			</tr>
			<tr>
			    <td><fmt:message key="max.wait"/><label><fmt:message
			                                            key="measurement.milliseconds"/></label></td>
			    <td align="left">
			    	<%if (maxWait != null) { %>
			        	<input id="maxWait" name="maxWait" onclick="clearStatus('maxWait')" type="text"
			               value="<%=maxWait %>"/>
			        <%} else { %>
			        	<input id="maxWait" name="maxWait" onclick="clearStatus('maxWait')" type="text"
			               value="( int )"/>
			        <%} %>
			    </td>
			</tr>
			<tr>
			    <td><fmt:message key="test.on.borrow"/></td>
			    <td align="left">
			        <select id="testOnBorrow" name="testOnBorrow">
			        	<%if (testOnBorrow) { %>
			        		<option value="false"><fmt:message key="false"/></option>
			        		<option value="true" selected="true"><fmt:message key="true"/></option>
			        	<%} else { %>
			        		<option value="false" selected="true"><fmt:message key="false"/></option>
			            	<option value="true"><fmt:message key="true"/></option>
			            <%} %>
			    	</select>
			    </td>
			</tr>
			<tr>
			    <td><fmt:message key="test.on.return"/></td>
			    <td align="left">
			        <select id="testOnReturn" name="testOnReturn">
			        	<%if (testOnReturn) { %>
			        		<option value="false"><fmt:message key="false"/></option>
			            	<option value="true" selected="true"><fmt:message key="true"/></option>
			            <%} else { %>
			            	<option value="false" selected="true"><fmt:message key="false"/></option>
			            	<option value="true"><fmt:message key="true"/></option>
			             <%} %>
			        </select>
			    </td>
			</tr>
			<tr>
			    <td><fmt:message key="test.while.idle"/></td>
			    <td align="left">
			        <select id="testWhileIdle" name="testWhileIdle">
			        	<%if (testWhileIdle) { %>
			        		<option value="false"><fmt:message key="false"/></option>
			            	<option value="true" selected="true"><fmt:message key="true"/></option>
			            <%} else { %>
			            	<option value="false" selected="true"><fmt:message key="false"/></option>
			            	<option value="true"><fmt:message key="true"/></option>
			            <%} %>
			        </select>
			    </td>
			</tr>
			<tr>
			    <td><fmt:message key="validation.query"/></td>
			    <td align="left">
			    <input id="validationquery" name="validationquery" type="text" value="<%=Encode.forHtml(validationquery) %>"/>
			</tr>
			<tr>
			    <td><fmt:message key="validation.class.name"/></td>
			    <td align="left">
			    <input id="validatorClassName" name="validatorClassName" type="text" value="<%=Encode.forHtml(validatorClassName) %>"/>
			</tr>
			<tr>
			    <td><fmt:message key="time.between.eviction.runs.millis"/></td>
			    <td align="left">
			    	<%if (timeBetweenEvictionRunsMillis != null) { %>
			        	<input id="timeBetweenEvictionRunsMillis" name="timeBetweenEvictionRunsMillis" onclick="clearStatus('timeBetweenEvictionRunsMillis')" type="text"
			               value="<%=timeBetweenEvictionRunsMillis %>"/>
			        <%} else { %>
			        	<input id="timeBetweenEvictionRunsMillis" name="timeBetweenEvictionRunsMillis" onclick="clearStatus('timeBetweenEvictionRunsMillis')" type="text"
			               value="( int )"/>
			        <%} %>
			    </td>
			</tr>
			<tr>
			    <td><fmt:message key="num.tests.per.eviction.run"/></td>
			    <td align="left">
			    	<%if (numTestsPerEvictionRun != null) { %>
			        	<input id="numTestsPerEvictionRun" name="numTestsPerEvictionRun" onclick="clearStatus('numTestsPerEvictionRun')" type="text"
			               value="<%=numTestsPerEvictionRun %>"/>
			         <%} else { %>
			         	<input id="numTestsPerEvictionRun" name="numTestsPerEvictionRun" onclick="clearStatus('numTestsPerEvictionRun')" type="text"
			               value="( int )"/>
			         <%} %>
			    </td>
			</tr>
			<tr>
			    <td><fmt:message key="min.evictable.idle.time.millis"/></td>
			    <td align="left">
			    	<%if (minEvictableIdleTimeMillis != null) { %>
			        	<input id="minEvictableIdleTimeMillis" name="minEvictableIdleTimeMillis" onclick="clearStatus('minEvictableIdleTimeMillis')" type="text"
			               value="<%=minEvictableIdleTimeMillis %>"/>
			         <%} else { %>
			         	<input id="minEvictableIdleTimeMillis" name="minEvictableIdleTimeMillis" onclick="clearStatus('minEvictableIdleTimeMillis')" type="text"
			               value="( int )"/>
			         <%} %>
			    </td>
			</tr>
			<tr>
			    <td style="width:230px;"><fmt:message key="access.to.underlying.connection.allowed"/></td>
			    <td align="left">
			        <select id="accessToUnderlyingConnectionAllowed" name="accessToUnderlyingConnectionAllowed">
			        	<%if (accessToUnderlyingConnectionAllowed) { %>
				            <option value="false"><fmt:message key="false"/></option>
				            <option value="true" selected="true"><fmt:message key="true"/></option>
				        <%} else { %>
				        	<option value="false" selected="true"><fmt:message key="false"/></option>
				            <option value="true"><fmt:message key="true"/></option>
				        <%} %>
			       </select>
			    </td>
			</tr>
			<tr>
			    <td><fmt:message key="remove.abandoned"/></td>
			    <td align="left">
			        <select id="removeAbandoned" name="removeAbandoned">
			        	<%if (removeAbandoned) { %>
				            <option value="false"><fmt:message key="false"/></option>
				            <option value="true" selected="true"><fmt:message key="true"/></option>
			            <%} else { %>
			            	<option value="false" selected="true"><fmt:message key="false"/></option>
				            <option value="true"><fmt:message key="true"/></option>
			            <%} %>
			        </select>
			    </td>
			</tr>
			<tr>
			    <td><fmt:message key="remove.abandoned.timeout"/></td>
			    <td align="left">
			    	<%if (removeAbandonedTimeout != null) { %>
			        	<input id="removeAbandonedTimeout" name="removeAbandonedTimeout" onclick="clearStatus('removeAbandonedTimeout')" type="text"
			               value="<%=removeAbandonedTimeout %>"/>
			        <%} else { %>
			        	<input id="removeAbandonedTimeout" name="removeAbandonedTimeout" onclick="clearStatus('removeAbandonedTimeout')" type="text"
			               value="( int )"/>
			        <%} %>
			    </td>
			</tr>
			<tr>
			    <td><fmt:message key="log.abandoned"/></td>
			    <td align="left">
			        <select id="logAbandoned" name="logAbandoned">
			        	<%if (logAbandoned) { %>
				            <option value="false"><fmt:message key="false"/></option>
				            <option value="true" selected="true"><fmt:message key="true"/></option>
			            <%} else { %>
				            <option value="false" selected="true"><fmt:message key="false"/></option>
				            <option value="true"><fmt:message key="true"/></option>
			             <%} %>
			        </select>
			    </td>
			</tr>
			<tr> 
			    <td><fmt:message key="connection.properties"/></td>
			    <td align="left">
			    <input id="connectionProperties" name="connectionProperties" type="text" value="<%=Encode.forHtml(connectionProperties) %>"/>
			</tr>
			<tr>
    			<td style="width:230px;"><fmt:message key="init.sql"/></td>
    			<td align="left">
    				<input id="initSQL" name="initSQL" type="text" value="<%=Encode.forHtml(initSQL) %>"/>
			</tr>
			<tr>
    			<td><fmt:message key="jdbc.interceptors"/></td>
    			<td align="left">
    				<input id="jdbcInterceptors" name="jdbcInterceptors" type="text" value="<%=Encode.forHtml(jdbcInterceptors) %>"/>
			</tr>
			<tr>
    			<td><fmt:message key="validation.interval"/></td>
    			<td align="left">
    				<%if (validationInterval != null) { %>
        				<input id="validationInterval" name="validationInterval" onclick="clearStatus('validationInterval')" type="text"
               				value="<%=validationInterval %>"/>
               		<%} else { %>
               			<input id="validationInterval" name="validationInterval" onclick="clearStatus('validationInterval')" type="text"
              				 value="( long )"/>
               		<%} %>
    			</td>
			</tr>
			<tr>
    			<td><fmt:message key="fair.queue"/></td>
			    <td align="left">
			        <select id="fairQueue" name="fairQueue">
			        	<%if (fairQueue) { %>
				            <option value="false"><fmt:message key="false"/></option>
				            <option value="true" selected="true"><fmt:message key="true"/></option>
			            <%} else { %>
				            <option value="false" selected="true"><fmt:message key="false"/></option>
				            <option value="true"><fmt:message key="true"/></option>
			            <%} %>
			        </select>
			    </td>
			</tr>
			<tr>
    			<td><fmt:message key="jmx.enabled"/></td>
			    <td align="left">
			        <select id="jmxEnabled" name="jmxEnabled">
			        	<%if (jmxEnabled) { %>
			            <option value="false"><fmt:message key="false"/></option>
			            <option value="true" selected="true"><fmt:message key="true"/></option>
			            <%} else { %>
			            <option value="false" selected="true"><fmt:message key="false"/></option>
			            <option value="true"><fmt:message key="true"/></option>
			            <%} %>
			        </select>
			    </td>
			</tr>
			<tr>
    			<td><fmt:message key="abandon.when.percentage.full"/></td>
    			<td align="left">
    				<%if (abandonWhenPercentageFull != null) { %>
        				<input id="abandonWhenPercentageFull" name="abandonWhenPercentageFull" onclick="clearStatus('abandonWhenPercentageFull')" type="text"
               				value="<%=abandonWhenPercentageFull %>"/>
               		<%} else { %>
               			<input id="abandonWhenPercentageFull" name="abandonWhenPercentageFull" onclick="clearStatus('abandonWhenPercentageFull')" type="text"
               				value="( int )"/>
               		 <%} %>
    			</td>
			</tr>
			<tr>
    			<td><fmt:message key="max.age"/></td>
    			<td align="left">
    				<%if (maxAge != null) { %>
        				<input id="maxAge" name="maxAge" onclick="clearStatus('maxAge')" type="text"
               				value="<%=maxAge %>"/>
               		<%} else { %>
               			<input id="maxAge" name="maxAge" onclick="clearStatus('maxAge')" type="text"
               				value="( long )"/>
               		<%} %>
    			</td>
			</tr>
			<tr>
    			<td><fmt:message key="use.equals"/></td>
			    <td align="left">
			        <select id="useEquals" name="useEquals">
			        	<%if (useEquals) {%>
				            <option value="false"><fmt:message key="false"/></option>
				            <option value="true" selected="true"><fmt:message key="true"/></option>
			            <%} else { %>
				            <option value="false" selected="true"><fmt:message key="false"/></option>
				            <option value="true"><fmt:message key="true"/></option>
				        <%} %>
			        </select>
			    </td>
			</tr>
			<tr>
    			<td><fmt:message key="suspect.timeout"/></td>
    			<td align="left">
    				<%if (suspectTimeout != null) { %>
        				<input id="suspectTimeout" name="suspectTimeout" onclick="clearStatus('suspectTimeout')" type="text"
               				value="<%=suspectTimeout %>"/>
               		<%} else { %>
               			<input id="suspectTimeout" name="suspectTimeout" onclick="clearStatus('suspectTimeout')" type="text"
               				value="( int )"/>
               		<%} %>
    			</td>
			</tr>
			<tr>
    			<td><fmt:message key="alternate.username.allowed"/></td>
			    <td align="left">
			        <select id="alternateUsernameAllowed" name="alternateUsernameAllowed">
			        	<%if (alternateUsernameAllowed) { %>
				            <option value="false"><fmt:message key="false"/></option>
				            <option value="true" selected="true"><fmt:message key="true"/></option>
			            <%} else { %>
				            <option value="false" selected="true"><fmt:message key="false"/></option>
				            <option value="true"><fmt:message key="true"/></option>
			            <%} %>
			        </select>
			    </td>
			</tr>
			<tr>
                <td><fmt:message key="validation.query.timeout"/></td>
                <td align="left">
                	<%if (validationQueryTimeout != null) { %>
                    	<input id="validationQueryTimeout" name="validationQueryTimeout" onclick="clearStatus('validationQueryTimeout')" type="text"
                           	value="<%=validationQueryTimeout %>"/>
                    <%} else { %>
                        <input id="validationQueryTimeout" name="validationQueryTimeout" onclick="clearStatus('validationQueryTimeout')" type="text"
                           	value="( int )"/>
                    <%} %>
                </td>
            </tr>
        </table>
    </td>
</tr>
</tbody>
</table>
<table id="configEditor" class="styledLeft noBorders" cellspacing="0" cellpadding="0" border="0" style="border-top:none;display:none;">
	<tr>
    <td style="width:170px"><fmt:message key="configuration"/></td>
    <td align="left">
        <textarea id="configuration" name="configuration" 
        style="background-color:lavender; width:99%;height:470px;*height:500px;
                      font-family:verdana;
                      font-size:11px;
                      color: darkblue;
                      border:solid 1px #9fc2d5;
                      overflow-x:auto;
                      overflow-y:auto"><%if (!(type.equals(NDataSourceClientConstants.RDBMS_DTAASOURCE_TYPE)) && (dataSourceDefinition != null)) { %><%=NDataSourceHelper.prettifyXML((dataSourceDefinition.getDsXMLConfiguration().trim())) %>
        <%} %></textarea>
        <textarea id="configContent" name="configContent" style="display:none"></textarea>
    </td>
</tr>
</table>
<table class="styledLeft noBorders" cellspacing="0" cellpadding="0" border="0">
<tr>    
   <td class="buttonRow" colspan="3">
   		<%if(type.equals("RDBMS")) { %>
   		<div id="connectionTestMsgDiv" style="display: none;"></div>
   		<input class="button" id="testConnectionButton" name="testConnectionButton" type="button" value="Test Connection" onclick="var val = isDSValid('<fmt:message key="ds.name.cannotfound.msg"/>','<fmt:message key="ds.name.invalid.msg"/>',
   		'<fmt:message key="ds.driver.cannotfound.msg"/>','<fmt:message key="ds.url.cannotfound.msg"/>'); if (val) {testConnection()};return false;"/>
   		<script type="text/javascript">
            function displayMsg(msg) {
            	var successMsg  =  new RegExp("true");
            	if (msg.search(successMsg)==-1) //if match failed
            	{
            		CARBON.showErrorDialog(msg);
            	} else {
            		CARBON.showInfoDialog("Connection is healthy");
            	}

            }

            function testConnection() {
            	var password;
            	if (document.getElementById("jndiPropertyTable") != null) {
                	extractJndiProps();
                } 
                if (document.getElementById("dsPropertyTable") != null) {
                	extractDataSourceProps();
                }
            	var query = document.getElementById('dsName').value;
            	var dsProvider = document.getElementById('dsProviderType').value;
            	var datasourceType = document.getElementById('dsType').value;
            	var datasourceCustomType = document.getElementById('customDsType').value;
            	if (dsProvider == 'default') {
            		var driver = document.getElementById('driver').value;
            		var url = document.getElementById('url').value;
            		var username = document.getElementById('username').value;
                    username = encodeURIComponent(username);
            	} else {
            		var dsclassname = document.getElementById('dsclassname').value;
            		var dsproviderProperties = document.getElementById('dsproviderProperties').value;
            	}
            	dsProvider = escape(dsProvider);
            	
            	         	
                var url = 'validateconnection-ajaxprocessor.jsp?&dsName=' + document.getElementById('dsName').value+'&driver='+driver+
           	'&url='+encodeURIComponent(url)+'&username='+username+'&dsType=' + datasourceType+'&customDsType='+datasourceCustomType+'&dsProviderType='+dsProvider+
    	'&dsclassname='+dsclassname+'&dsclassname='+dsclassname+'&dsproviderProperties='+dsproviderProperties+'&editMode='+<%=editMode%>;
    	
    		var editMode = document.getElementById("editMode").value;
			if (editMode == null || editMode == "false") {
				password = document.getElementById('password').value;
                password = encodeURIComponent(password);
				url = url + '&password='+password;
			} else {
				var changePassword = "false";
				password = document.getElementById('newPassword').value;
                password = encodeURIComponent(password);
				if (document.getElementById('changePassword') != null) {
					changePassword = document.getElementById('changePassword').checked;
				}
				url = url + '&changePassword='+changePassword + '&newPassword='+password;
			}
                jQuery('#connectionTestMsgDiv').load(url, displayMsg);
                return false;
            }
        </script>
   		<%} %>
        <%if (!isSystem) { %>
        <input class="button" type="button"
               value="<fmt:message key="save"/>"
               onclick="var val = ValidateProperties(); if (val) {dsSave('<fmt:message key="ds.name.cannotfound.msg"/>','<fmt:message key="ds.name.invalid.msg"/>','<fmt:message key="ds.driver.cannotfound.msg"/>','<fmt:message key="ds.url.cannotfound.msg"/>','<fmt:message key="custom.ds.type.name.cannotfound.msg"/>',document.dscreationform)}; return false;"/>
        
        <input class="button" type="reset" value="<fmt:message key="cancel"/>"
               onclick="document.location.href='index.jsp'"/>
        <%} else {%>
        <input class="button" type="reset" value="<fmt:message key="back"/>"
               onclick="document.location.href='index.jsp'"/>
        <%} %>
    </td>
</tr>
</table>
<script type="text/javascript">
	document.getElementById('configuration').innerHTML = format_xml(document.getElementById('configuration').value);
	populateDataSourceProperties();
	populateJNDIProperties();
	disableForm();
	loadConfigView();
</script>
</div>
</div>
</form>
</fmt:bundle>
