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

function addRemoteServerConfig(logType) {
    sessionAwareFunction(function() {
        jQuery.noConflict();

        var serverUrl = getSingleInputValue('remoteServerUrl');
        var timeout = getSingleInputValue('connectTimeoutMillis');
        var username = getSingleInputValue('remoteUsername');
        var password = getSingleInputValue('remotePassword');
        var keystoreLocation = getSingleInputValue('keystoreLocation');
        var keystorePassword = getSingleInputValue('keystorePassword');
        var truststoreLocation = getSingleInputValue('truststoreLocation');
        var truststorePassword = getSingleInputValue('truststorePassword');
        var verifyHostname = true;
        if (document.getElementById('verify-hostname-option').checked !== true) {
            verifyHostname = false;
        }

        jQuery.post('process_add_remote_server_config-ajaxprocessor.jsp',
            {
                url: serverUrl,
                connectTimeoutMillis: timeout,
                verifyHostname: verifyHostname,
                logType: logType,
                remoteUsername: username,
                remotePassword: password,
                keystoreLocation: keystoreLocation,
                keystorePassword: keystorePassword,
                truststoreLocation: truststoreLocation,
                truststorePassword: truststorePassword
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

    var logType = true;
    if (document.getElementById('log-option')) {
        logType = jQuery.trim(document.getElementById('log-option').value);
    }
    var yesCallbackWrapper = function() {
        yesCallback(logType);
    }
    CARBON.showConfirmationDialog(message,yesCallbackWrapper, null);
}

function loadPage() {
    sessionAwareFunction(function() {
        jQuery.noConflict()
        jQuery("#addRemoteServerConfig").load('add_remote_server_config-ajaxprocessor.jsp');
    });
}

function resetConfig(logType) {
    sessionAwareFunction(function() {
        jQuery.noConflict();
        jQuery.post('process_add_remote_server_config-ajaxprocessor.jsp',
            {
                reset: true,
                logType: logType.toUpperCase()
            },
            function(data) {
                CARBON.showInfoDialog(data);
                clearRemoteServerConfigInputs(logType);
            });
    });
}

function getConfigData(logType) {
    sessionAwareFunction(function() {
        jQuery.noConflict();
        jQuery.post('process_add_remote_server_config-ajaxprocessor.jsp',
            {
                get: true,
                logType: logType.toUpperCase()
            },
            function(data) {
                setRemoteServerConfigInputs(logType, data);
            });
    });
}

function clearRemoteServerConfigInputs(logType) {

    var inputIdList = [
        "remoteServerUrl",
        "connectTimeoutMillis",
        "remoteUsername",
        "remotePassword",
        "keystoreLocation",
        "keystorePassword",
        "truststoreLocation",
        "truststorePassword"
    ]
    for (inputId of inputIdList) {
        resetSingleInput(inputId);
    }

    var element = jQuery('#' + 'verify-hostname-option');
    if (element) {
        element.prop('checked', false);
    }
}


function setRemoteServerConfigInputs(logType, remoteServerLoggerData) {

    var inputIdValueMap = {
        "remoteServerUrl": remoteServerLoggerData.url,
        "connectTimeoutMillis": remoteServerLoggerData.connectTimeoutMillis,
        "remoteUsername": remoteServerLoggerData.username,
        "remotePassword": remoteServerLoggerData.password,
        "keystoreLocation": remoteServerLoggerData.keystoreLocation,
        "keystorePassword": remoteServerLoggerData.keystorePassword,
        "truststoreLocation": remoteServerLoggerData.truststoreLocation,
        "truststorePassword": remoteServerLoggerData.truststorePassword
    }

    for (inputId in inputIdValueMap) {
        setSingleInput(inputId, inputIdValueMap[inputId]);
    }

    var element = jQuery('#' + 'verify-hostname-option');
    if (element) {
        if (remoteServerLoggerData.verifyHostname) {
            element.prop('checked', true);
        }
    }
}

function logTypeSelectionChanged(logType) {
    sessionAwareFunction(function() {
        jQuery.noConflict();

        var remoteServerLoggerData = null;
        getConfigData(logType);

        if (remoteServerLoggerData) {
            setRemoteServerConfigInputs(logType, remoteServerLoggerData);
        } else {
            clearRemoteServerConfigInputs(logType);
        }
    });
}

function resetSingleInput (inputId) {

    var element = document.getElementById(inputId);
    if (element) {
        element.value = '';
    }
}

function setSingleInput (inputId, value) {

    var element = document.getElementById(inputId);
    if (element) {
        element.value = value;
    }
}

function getSingleInputValue (inputId) {

    var element = document.getElementById(inputId);
    if (element) {
        return element.value;
    }
    return null;
}

// This is called to load default configurations for the first time
logTypeSelectionChanged("AUDIT");
