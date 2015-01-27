var groupName;

function connectToBackend(backendURL) {
    jQuery.noConflict();
    var url = "connect_member_ajaxprocessor.jsp?backendURL=" + backendURL;
    jQuery("#output").load(url, null, function (responseText, status, XMLHttpRequest) {
        if (status != "success") {
            CARBON.showErrorDialog('Error occurred while connecting to ' + backendURL + ". " + responseText);
        } else {
            CARBON.showInfoDialog('Successfully connected to ' + backendURL);
        }
    });
}

function shutdownGroupGracefully(_groupName) {
    jQuery.noConflict();
    groupName = _groupName;
    CARBON.showConfirmationDialog('Do you really want to gracefully shutdown the '+ groupName +' group?',shutdownGroupGracefullyCallback,null);
}

function shutdownGroupGracefullyCallback() {
    var url = "proxy_ajaxprocessor.jsp?action=shutdownGracefully&groupName=" + groupName;
    jQuery.noConflict();
    jQuery("#output").load(url, null, function (responseText, status, XMLHttpRequest) {
        if (status != "success") {
            CARBON.showErrorDialog('Error occurred while gracefully shutting down the '+ groupName +' group');
        } else {
            CARBON.showInfoDialog('The '+ groupName +' is being gracefully shutdown.');
        }
    });
}

function shutdownGroup(_groupName) {
    jQuery.noConflict();
    groupName = _groupName;
    CARBON.showConfirmationDialog('Do you really want to shutdown the '+ groupName +' group?',shutdownGroupCallback,null);
}

function shutdownGroupCallback() {
    var url = "proxy_ajaxprocessor.jsp?action=shutdown&groupName=" + groupName;
    jQuery.noConflict();
    jQuery("#output").load(url, null, function (responseText, status, XMLHttpRequest) {
        if (status != "success") {
            CARBON.showErrorDialog('Error occurred while shutting down the group');
        } else {
            CARBON.showInfoDialog('The group is being shutdown.');
        }
    });
}

function restartGroupGracefully(_groupName) {
    jQuery.noConflict();
    groupName = _groupName;
    CARBON.showConfirmationDialog('Do you really want to gracefully restart the ' + groupName + ' group?',restartGroupGracefullyCallback,null);
}

function restartGroupGracefullyCallback() {
    var url = "proxy_ajaxprocessor.jsp?action=restartGracefully&groupName=" + groupName;
    jQuery.noConflict();
    jQuery("#output").load(url, null, function (responseText, status, XMLHttpRequest) {
        if (jQuery(responseText).text().replace(/ /g,'') != '') {
            CARBON.showWarningDialog(responseText);
            return;
        }
        if (status != "success") {
            CARBON.showErrorDialog('Error occurred while gracefully restarting the ' + groupName +' group.');
        } else {
            CARBON.showInfoDialog('The '+ groupName +' group is being gracefully restarted.');
        }
    });
}

function restartGroup(_groupName) {
    jQuery.noConflict();
    groupName = _groupName;
    CARBON.showConfirmationDialog('Do you really want to restart the ' + groupName + ' group?',restartGroupCallback,null);
}

function restartGroupCallback() {
    var url = "proxy_ajaxprocessor.jsp?action=restart&groupName=" + groupName;
    jQuery.noConflict();
    jQuery("#output").load(url, null, function (responseText, status, XMLHttpRequest) {
        if (jQuery(responseText).text().replace(/ /g,'') != '') {
            CARBON.showWarningDialog(responseText);
            return;
        }
        if (status != "success") {
            CARBON.showErrorDialog('Error occurred while restarting group ' + groupName);
        } else {
            CARBON.showInfoDialog('The '+ groupName +' group is being restarted.');
        }
    });
}

function shutdownClusterGracefully() {
    jQuery.noConflict();
    CARBON.showConfirmationDialog('Do you really want to gracefully shutdown the cluster?',shutdownClusterGracefullyCallback,null);
}

function shutdownClusterGracefullyCallback() {
    var url = "proxy_ajaxprocessor.jsp?action=shutdownGracefully";
    jQuery.noConflict();
    jQuery("#output").load(url, null, function (responseText, status, XMLHttpRequest) {
        if (status != "success") {
            CARBON.showErrorDialog('Error occurred while gracefully shutting down the cluster');
        } else {
            CARBON.showInfoDialog('The cluster is being gracefully shutdown.');
        }
    });
}

function shutdownCluster() {
    jQuery.noConflict();
    CARBON.showConfirmationDialog('Do you really want to shutdown cluster?',shutdownClusterCallback,null);
}

function shutdownClusterCallback() {
    var url = "proxy_ajaxprocessor.jsp?action=shutdown";
    jQuery.noConflict();
    jQuery("#output").load(url, null, function (responseText, status, XMLHttpRequest) {
        if (status != "success") {
            CARBON.showErrorDialog('Error occurred while shutting down the cluster');
        } else {
            CARBON.showInfoDialog('The cluster is being shutdown.');
        }
    });
}

function restartClusterGracefully() {
    jQuery.noConflict();
    CARBON.showConfirmationDialog('Do you really want to gracefully restart the cluster?',restartClusterGracefullyCallback,null);
}

function restartClusterGracefullyCallback() {
    var url = "proxy_ajaxprocessor.jsp?action=restartGracefully";
    jQuery.noConflict();
    jQuery("#output").load(url, null, function (responseText, status, XMLHttpRequest) {
        if (jQuery(responseText).text().replace(/ /g,'') != '') {
            CARBON.showWarningDialog(responseText);
            return;
        }
        if (status != "success") {
            CARBON.showErrorDialog('Error occurred while gracefully restarting the cluster.');
        } else {
            CARBON.showInfoDialog('The cluster is being gracefully restarted.');
        }
    });
}

function restartCluster() {
    jQuery.noConflict();
    CARBON.showConfirmationDialog('Do you really want to restart the cluster?',restartClusterCallback,null);
}

function restartClusterCallback() {
    var url = "proxy_ajaxprocessor.jsp?action=restart";
    jQuery.noConflict();
    jQuery("#output").load(url, null, function (responseText, status, XMLHttpRequest) {
        if (jQuery(responseText).text().replace(/ /g,'') != '') {
            CARBON.showWarningDialog(responseText);
            return;
        }
        if (status != "success") {
            CARBON.showErrorDialog('Error occurred while restarting cluster ');
        } else {
            CARBON.showInfoDialog('The cluster is being restarted.');
        }
    });
}

function startClusterMaintenance(){
    jQuery.noConflict();
    CARBON.showConfirmationDialog('Do you really want to start cluster maintenance?',startClusterMaintenanceCallback,null);
}

function startClusterMaintenanceCallback(){
    var url = "proxy_ajaxprocessor.jsp?action=startMaintenance";
    jQuery.noConflict();
    jQuery("#output").load(url, null, function (responseText, status, XMLHttpRequest) {
        if (jQuery(responseText).text().replace(/ /g,'') != '') {
            CARBON.showWarningDialog(responseText);
            return;
        }
        if (status != "success") {
            CARBON.showErrorDialog('Error occurred while starting cluster maintenance');
        } else {
            CARBON.showInfoDialog('Cluster has been switched to maintenance mode');
        }
    });
}

function endClusterMaintenance(){
   jQuery.noConflict();
   CARBON.showConfirmationDialog('Do you really want to end cluster maintenance?',endClusterMaintenanceCallback,null);
}

function endClusterMaintenanceCallback(){
    var url = "proxy_ajaxprocessor.jsp?action=endMaintenance";
    jQuery.noConflict();
    jQuery("#output").load(url, null, function (responseText, status, XMLHttpRequest) {
        if (jQuery(responseText).text().replace(/ /g,'') != '') {
            CARBON.showWarningDialog(responseText);
            return;
        }
        if (status != "success") {
            CARBON.showErrorDialog('Error occurred while ending cluster maintenance');
        } else {
            CARBON.showInfoDialog('Cluster has been switched to normal mode');
        }
    });
}

function startGroupMaintenance(_groupName){
    jQuery.noConflict();
    groupName = _groupName;
    CARBON.showConfirmationDialog('Do you really want to start group maintenance?',startGroupMaintenanceCallback,null);
}

function startGroupMaintenanceCallback(){
    var url = "proxy_ajaxprocessor.jsp?action=startMaintenance&groupName=" + groupName;
    jQuery.noConflict();
    jQuery("#output").load(url, null, function (responseText, status, XMLHttpRequest) {
        if (jQuery(responseText).text().replace(/ /g,'') != '') {
            CARBON.showWarningDialog(responseText);
            return;
        }
        if (status != "success") {
            CARBON.showErrorDialog('Error occurred while starting group maintenance');
        } else {
            CARBON.showInfoDialog('Group ' + groupName + '  has been switched to maintenance mode');
        }
    });
}

function endGroupMaintenance(_groupName){
   jQuery.noConflict();
   groupName = _groupName;
   CARBON.showConfirmationDialog('Do you really want to end group maintenance?',endGroupMaintenanceCallback,null);
}

function endGroupMaintenanceCallback(){
    var url = "proxy_ajaxprocessor.jsp?action=endMaintenance&groupName=" + groupName;
    jQuery.noConflict();
    jQuery("#output").load(url, null, function (responseText, status, XMLHttpRequest) {
        if (jQuery(responseText).text().replace(/ /g,'') != '') {
            CARBON.showWarningDialog(responseText);
            return;
        }
        if (status != "success") {
            CARBON.showErrorDialog('Error occurred while ending group maintenance');
        } else {
            CARBON.showInfoDialog('Group '+ groupName +' has been switched to normal mode');
        }
    });
}
