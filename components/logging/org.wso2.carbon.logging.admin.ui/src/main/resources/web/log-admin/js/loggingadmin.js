//Checks the browser support for the localstorage functions
var has_localStorage_support = !!window.localStorage
    && typeof localStorage.getItem === 'function'
    && typeof localStorage.setItem === 'function'
    && typeof localStorage.removeItem === 'function';

function getSelectedValue(comboBoxName) {
    var comboBox = document.getElementById(comboBoxName);
    return comboBox[comboBox.selectedIndex].value;
}

function areaOnFocus(element, inputText)
{
     if( document.getElementById(element).value == inputText)
     {
    	 document.getElementById(element).value='';
     }
}

function areaOnBlur(element, inputText)
{
     if( document.getElementById(element).value=='')
     {
    	 document.getElementById(element).value = inputText;
     }
}

function setSelectedValue(comboBoxName, selectedValue) {
    var comboBox = document.getElementById(comboBoxName);
    var len = comboBox.options.length;
    for (var i = 0; i < len; i += 1) {
        if (comboBox.options[i].value == selectedValue) {
            comboBox.selectedIndex = i;
        }
    }
}

function updateLogger(loggerName, logLevelId, additivityId) {
    sessionAwareFunction(function() {
        jQuery.noConflict();
        var persist = document.getElementById('persistLogId').checked;

        var logLevelWidget = document.getElementById(logLevelId);
        var logLevel = logLevelWidget[logLevelWidget.selectedIndex].value;

        var additivityWidget = document.getElementById(additivityId);
        var additivity = additivityWidget[additivityWidget.selectedIndex].value;

        jQuery.post('update_logger-ajaxprocessor.jsp',
                    {loggerName: loggerName,
                      logLevel:logLevel,
                      additivity: additivity,
                      persist: persist
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

function getAppenderData() {
    sessionAwareFunction(function() {
        jQuery.noConflict();
        var appenderName = getSelectedValue("appenderCombo");
        jQuery("#appenderSettings").load('appenders-ajaxprocessor.jsp?appenderName=' + appenderName);
    });
}

function updateAppender() {
    sessionAwareFunction(function() {
        jQuery.noConflict();
        var appenderName = getSelectedValue("appenderCombo");
        var logPattern = jQuery.trim(document.getElementById('appenderLogPattern').value);
        var threshold = getSelectedValue("appenderThresholdCombo");

        var logFile = null;
        if (document.getElementById('appenderLogFile')) {
            logFile = jQuery.trim(document.getElementById('appenderLogFile').value);
        }

        var sysLogHost = null;
        if (document.getElementById('appenderSysLogHost')) {
            sysLogHost = jQuery.trim(document.getElementById('appenderSysLogHost').value);
        }

        var facility = null;
        if (document.getElementById("appenderFacilityCombo")) {
            facility = getSelectedValue("appenderFacilityCombo");
        }
        var persist = document.getElementById('persistLogId').checked;

        jQuery.post('update_appender_ajaxprocessor.jsp',
                    {appenderName: appenderName,
                     logPattern: logPattern,
                     threshold: threshold,
                     logFile: logFile,
                     sysLogHost: sysLogHost,
                     facility: facility,
                     persist: persist
                    }, function(data){
                         CARBON.showInfoDialog(data);
                    });
    });
}

function syslogUpdateConfig(){
    sessionAwareFunction(function() {
        jQuery.noConflict();
        var syslogURL = document.getElementById('syslogURL').value;
        var syslogPort = document.getElementById('syslogPort').value;
        var realm = document.getElementById('realm').value;
        var userName = document.getElementById('userName').value;
        var password = document.getElementById('password').value;
        
        jQuery.post('update_syslog-ajaxprocessor.jsp',
            {syslogURL: syslogURL,
        	 syslogPort:syslogPort,
        	 realm: realm,
        	 userName:userName,
        	 password: password
            },
            function(data){
                CARBON.showInfoDialog(data);
                loadPage();
            });
    });    
}

function globalLog4jUpdateConfig(){
    sessionAwareFunction(function() {
        jQuery.noConflict();
        var persist = document.getElementById('persistLogId').checked;
        var logLevel = getSelectedValue("globalLogLevel");
        var logPattern = jQuery.trim(document.getElementById('globalLogPattern').value);
        setPersistState(persist)

        jQuery.post('updateLog4jGlobal-ajaxprocessor.jsp',
            {logLevel: logLevel,
             logPattern:logPattern,
              persist: persist
            },
            function(data){
                CARBON.showInfoDialog(data);
                loadPage();
            });
    });    
}

function restoreLog4jConfigToDefaults(){
    sessionAwareFunction(function() {
        jQuery.noConflict();
        jQuery.post('restoreDefaults-ajaxprocessor.jsp',
            {},
            function(data){
                CARBON.showInfoDialog(data);
                loadPage();
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
        jQuery("#globlaLog4jConfig").load('globalLogConfig-ajaxprocessor.jsp', loadPersistState);
        jQuery("#syslogConfig").load('syslogConfig-ajaxprocessor.jsp');
        jQuery("#appenderSettings").load('appenders-ajaxprocessor.jsp');
        jQuery("#loggers").load('loggers-ajaxprocessor.jsp');
    });
}

/**
* Loads the state of persist configuration checkbox
*
**/
function loadPersistState() {
    if (has_localStorage_support) {
      jQuery.noConflict();
      var persist = localStorage.getItem("persist");
      if (persist != null) {
        jQuery("#persistLogId").prop("checked", persist == "true" ? true : false);
      }
    }
}

/**
* Stores the state of persist configuration checkbox
*
**/
function setPersistState(state) {
    if (has_localStorage_support) {
      localStorage.setItem("persist", state);
    }
}

