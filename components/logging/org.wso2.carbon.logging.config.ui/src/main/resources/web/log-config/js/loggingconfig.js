/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

function addRemoteServerConfig(url, ) {
    sessionAwareFunction(function() {
        jQuery.noConflict();

        var serverUrl = null;
        if (document.getElementById('remoteServerUrl')) {
            serverUrl = jQuery.trim(document.getElementById('remoteServerUrl').value);
        }

        var timeout = null;
        if (document.getElementById('connectTimeoutMillis')) {
            timeout = jQuery.trim(document.getElementById('connectTimeoutMillis').value);
        }

        var auditLogTypeStatus = false;
        if (document.getElementById('audit-log-option').checked === true) {
            auditLogTypeStatus = true;
        }

        var carbonLogTypeStatus = false;
        if (document.getElementById('carbon-log-option').checked === true) {
            carbonLogTypeStatus = true;
        }
        jQuery.post('process_add_remote_server_config-ajaxprocessor.jsp',
            {
                url: serverUrl,
                connectTimeoutMillis: timeout,
                auditLogType: auditLogTypeStatus,
                carbonLogType: carbonLogTypeStatus
            }, function(data) {
                CARBON.showInfoDialog(data);
            });
    });
}
function showAdvancedConfigurations() {
	var advancedConfHeader =  document.getElementById('advancedConfigHeader');
	var configFields = document.getElementById('advancedConfig');
	if(configFields.style.display == 'none') {
	   advancedConfHeader.setAttribute('style','background-image:url(images/minus.gif);');
	   configFields.style.display = '';
	} else {
	   advancedConfHeader.setAttribute('style','background-image:url(images/plus.gif);');
	   configFields.style.display = 'none';
	}
}

function showConfirmationDialogBox(message, yesCallback){
   jQuery.noConflict();
   CARBON.showConfirmationDialog(message,yesCallback, null);
}

function loadPage() {
    sessionAwareFunction(function() {
        jQuery.noConflict()
        jQuery("#addRemoteServerConfig").load('add_remote_server_config-ajaxprocessor.jsp');
    });
}
