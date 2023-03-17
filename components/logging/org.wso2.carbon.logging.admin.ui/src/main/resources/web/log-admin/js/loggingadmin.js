/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function getSelectedValue(comboBoxName) {
    var comboBox = document.getElementById(comboBoxName);
    return comboBox[comboBox.selectedIndex].value;
}

function updateLogger(loggerName, logLevelId) {
    sessionAwareFunction(function() {
        jQuery.noConflict();

        var logLevelWidget = document.getElementById(logLevelId);
        var logLevel = logLevelWidget[logLevelWidget.selectedIndex].value;

        jQuery.post('update_logger-ajaxprocessor.jsp',
                    {loggerName: loggerName,
                     logLevel:logLevel
                    },
                    function(data){
                         CARBON.showInfoDialog(data);
                    });
    });
}

function updateAuditServerUrlConfig(loggerName, logLevelId) {
    sessionAwareFunction(function() {
        jQuery.noConflict();

        var logLevelWidget = document.getElementById(logLevelId);
        var logLevel = logLevelWidget[logLevelWidget.selectedIndex].value;

        jQuery.post('update_logger-ajaxprocessor.jsp',
                    {loggerName: loggerName,
                     logLevel:logLevel
                    },
                    function(data){
                         CARBON.showInfoDialog(data);
                    });
    });
}

function showLoggers(beginsWith) {
    sessionAwareFunction(function() {
        jQuery.noConflict();
        var filterText = jQuery.trim(document.getElementById('filterText').value);
        jQuery("#loggers").load('loggers-ajaxprocessor.jsp?filterStr=' + filterText + '&beginsWith=' + beginsWith);
    });
}

function addLogger() {
    sessionAwareFunction(function() {
        jQuery.noConflict();

        var loggerName = null;
        if (document.getElementById('loggerName')) {
            loggerName = jQuery.trim(document.getElementById('loggerName').value);
        }

        var loggerClass = null;
        if (document.getElementById('loggerClass')) {
            loggerClass = jQuery.trim(document.getElementById('loggerClass').value);
        }

        var logLevel = null;
        if (document.getElementById("loggingLevelCombo")) {
            logLevel = getSelectedValue("loggingLevelCombo");
        }

        jQuery.post('process_add_logger-ajaxprocessor.jsp',
            {loggerName: loggerName,
             loggerClass: loggerClass,
             logLevel: logLevel
            }, function(data){
                CARBON.showInfoDialog(data);
            });
    });
}

function showConfirmationDialogBox(message, yesCallback){
   jQuery.noConflict();
   CARBON.showConfirmationDialog(message,yesCallback, null);
}

function loadPage() {
    sessionAwareFunction(function() {
        jQuery.noConflict()
        jQuery("#addLoggerSettings").load('add_loggers-ajaxprocessor.jsp');
        jQuery("#loggers").load('loggers-ajaxprocessor.jsp');
        jQuery("#addAuditServerUrl").load('add_audit_server_url-ajaxprocessor.jsp');
    });
}
