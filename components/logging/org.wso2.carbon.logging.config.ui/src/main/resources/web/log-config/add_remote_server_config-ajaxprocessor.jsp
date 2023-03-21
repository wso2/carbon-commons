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
<fmt:bundle basename="org.wso2.carbon.logging.config.ui.i18n.Resources">

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
                    <tr>
                        <td width="40%" class="leftCol-med labelField"><fmt:message key="remote.server.timeout"/></td>
                        <td>
                            <input value="" size="25"
                                   id="connectTimeoutMillis"
                                   tabindex="6" type="text" white-list-patterns="^0*[1-9][0-9]*$"/>
                            <div class="sectionHelp">
                               <fmt:message key='help.remoteServerTimeout'/>
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
</fmt:bundle>
