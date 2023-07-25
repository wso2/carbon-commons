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
<style>
    .labelField {
        margin-top: 2px;
        display: inline-block;
    }
    .sectionHelp {
        margin-top: 1px;
        color: #555;
    }
    .sectionTop {
        margin-top: 5px;
    }
</style>

<script type="text/javascript" src="js/loggingconfig.js"></script>
<fmt:bundle basename="org.wso2.carbon.logging.remote.config.ui.i18n.Resources">

<table class="styledLeft">
    <thead>
        <tr>
            <th><fmt:message key="log.type"/></th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>
                <input type="checkbox" id="audit-log-option" name="log-type-selector" checked="checked">
                <label for="audit-log-option"><fmt:message  key="audit.logs"/></label>
                <div class="sectionHelp" style="display:inline-block">
                    <fmt:message key='audit.logs.helper'/>
                </div>
            </td>
        </tr>
        <tr>
            <td>
                <input type="checkbox" id="api-log-option" name="log-type-selector">
                <label for="api-log-option"><fmt:message  key="api.logs"/></label>
                <div class="sectionHelp" style="display:inline-block">
                    <fmt:message key='api.logs.helper'/>
                </div>
            </td>
        </tr>
        <tr>
            <td>
                <input type="checkbox" id="carbon-log-option" name="log-type-selector">
                <label for="carbon-log-option"><fmt:message  key="carbon.logs"/></label>
                <div class="sectionHelp" style="display:inline-block">
                    <fmt:message key='carbon.logs.helper'/>
                </div>
            </td>
        </tr>
    </tbody>
</table>
<br/>
<table class="styledLeft">
    <thead>
    <tr>
        <th><fmt:message key="configure.remote.server.url"/></th>
    </tr>
    </thead>
    <tr>
        <td class="formRow">
            <table class="normal sectionTop">
                <tr>
                    <td width="40%" class="leftCol-med labelField"><fmt:message key="remote.server.url"/><span class="required">*</span></td>
                    <td>
                        <input value="" size="50"
                               id="remoteServerUrl"
                               tabindex="6" type="url" autofocus/>
                       <div class="sectionHelp">
                           <fmt:message key='help.remoteServerUrl'/>
                       </div>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
    <tr>
        <td colspan="2" class="middle-header">
        	<a onclick="showAdvancedConfigurations()" class="icon-link" style="background-image:url(images/plus.gif);"
                             href="#advancedConfig" id="advancedConfigHeader"></a>
        	<fmt:message key="advanced.config.header"/>
        </td>
    </tr>
    <tr id="advancedConfig" style="display:none">
        <td class="formRow">
            <table class="normal sectionTop" id="showAdvancedConfigurations">
                <tr>
                    <td class="leftCol-med labelField"><fmt:message key="remote.server.timeout"/></td>
                    <td>
                        <input value="" size="25"
                               id="connectTimeoutMillis"
                               tabindex="6" type="text" white-list-patterns="^0*[1-9][0-9]*$"/>
                        <div class="sectionHelp">
                           <fmt:message key='help.remoteServerTimeout'/>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td class="leftCol-med labelField"><fmt:message key="remote.server.verifyHostname"/></td>
                    <td>
                        <input type="checkbox" id="verify-hostname-option" name="verify-hostname-selector" checked="checked">
                    </td>
                </tr>
                <tr>
                    <td/>
                    <td/>
                </tr>
                <tr>
                    <td class="leftCol-med labelField">
                        <b><i><fmt:message key="remote.server.basic.auth"/></i></b>
                    </td>
                    <td/>
                </tr>
                <tr>
                    <td class="leftCol-med labelField"><fmt:message key="remote.server.username"/></td>
                    <td>
                        <input value="" size="25"
                               id="remoteUsername"
                               tabindex="6" type="text"/>
                        <div class="sectionHelp">
                           <fmt:message key='help.remoteServerUsername'/>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td class="leftCol-med labelField"><fmt:message key="remote.server.password"/></td>
                    <td>
                        <input value="" size="25"
                               id="remotePassword"
                               tabindex="6" type="password"/>
                        <div class="sectionHelp">
                           <fmt:message key='help.remoteServerPassword'/>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td class="leftCol-med labelField">
                        <b><i><fmt:message key="remote.server.ssl.auth"/></i></b>
                    </td>
                    <td/>
                </tr>
                <tr>
                    <td class="leftCol-med labelField"><fmt:message key="remote.server.ssl.keystore.location"/></td>
                    <td>
                        <input value="" size="50"
                               id="keystoreLocation"
                               tabindex="6" type="text"/>
                        <div class="sectionHelp">
                           <fmt:message key='help.remoteServerKeystoreLocation'/>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td class="leftCol-med labelField"><fmt:message key="remote.server.ssl.keystore.password"/></td>
                    <td>
                        <input value="" size="25"
                               id="keystorePassword"
                               tabindex="6" type="password"/>
                        <div class="sectionHelp">
                           <fmt:message key='help.remoteServerKeystorePassword'/>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td class="leftCol-med labelField"><fmt:message key="remote.server.ssl.truststore.location"/></td>
                    <td>
                        <input value="" size="50"
                               id="truststoreLocation"
                               tabindex="6" type="text"/>
                        <div class="sectionHelp">
                           <fmt:message key='help.remoteServerTruststoreLocation'/>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td class="leftCol-med labelField"><fmt:message key="remote.server.ssl.truststore.password"/></td>
                    <td>
                        <input value="" size="25"
                               id="truststorePassword"
                               tabindex="6" type="password"/>
                        <div class="sectionHelp">
                           <fmt:message key='help.remoteServerTruststorePassword'/>
                        </div>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
    <tr>
        <td class="buttonRow">
            <input value="<fmt:message key="update"/>" tabindex="11" type="button"
                   class="button"
                   id="addRemoteServerConfig"
                   onclick="showConfirmationDialogBox('<fmt:message key="remote.server.config.add.confirm"/>', addRemoteServerConfig)"/>
        </td>
    </tr>
</table>
<br/>
<table class="styledLeft">
    <thead>
    <tr>
        <th><fmt:message key="restore.default"/></th>
    </tr>
    </thead>
    <tr>
        <td class="buttonRow">
            <input type="button" tabindex="12" value="<fmt:message key="reset"/>" class="button" id="resetConfig"
                   onclick="showConfirmationDialogBox('<fmt:message key="remote.server.config.restore.confirm"/>',
                   resetConfig)"/>
        </td>
    </tr>
</table>

</fmt:bundle>
