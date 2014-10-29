var perviousICFac = '';
var perviousProviderURL = '';
var perviousProviderPort = '';

function dsSave(namemsg, invalidnamemsg, drivermsg, urlmsg, form) {

    if (!isDSValid(namemsg, invalidnamemsg, drivermsg, urlmsg)) {
        return false;
    }
    form.submit();
    return true;
}

function isDSValid(namemsg, invalidnamemsg, drivermsg, urlmsg) {

    var name = document.getElementById('alias').value;
    if (name == null || name == '') {
        CARBON.showWarningDialog(namemsg);
        return false;
    }

    var iChars = "!@#$%^&*()+=[]\\\';,/{}|\":<>?";
    for (var i = 0; i < name.length; i++) {
        if (iChars.indexOf(name.charAt(i)) != -1) {
            CARBON.showWarningDialog(invalidnamemsg);
            return false;
        }
    }

    if (document.getElementById('driver').value == '') {
        CARBON.showWarningDialog(drivermsg);
        return false;
    }
    if (document.getElementById('url').value == '') {
        CARBON.showWarningDialog(urlmsg);
        return false;
    }
    return true;
}

function clearStatus(id) {
    var textbox = document.getElementById(id);
    var textValue = textbox.value;
    if (textValue.indexOf('int') >= 0 || textValue.indexOf('long') >= 0){
      textbox.value = '';
    } 
    return true;
}
function IsEmptyCheck(aTextField) {
    return (aTextField.value.length == 0) ||
           (aTextField.value == null);
}

function setRepository(type) {
    var jndiICFactory = document.getElementById("jndiICFactory");
    var jndiProviderType = document.getElementById("jndiProviderType");
    var dsrepotype_hidden = document.getElementById("dsrepotype_hidden");
    dsrepotype_hidden.value = type;
    if (jndiICFactory != undefined && jndiICFactory != null) {
        if ('InMemory' == type) {
            jndiICFactory.style.display = "none";
            jndiProviderType.style.display = "none";
            var jndiProviderPort = document.getElementById("jndiProviderPort");
            var jndiProviderUrl = document.getElementById("jndiProviderUrl");
            jndiProviderPort.style.display = "none";
            jndiProviderUrl.style.display = "none";
        } else if ("JNDI" == type) {
            jndiICFactory.style.display = "";
            jndiProviderType.style.display = "";
            setProviderType(getCheckedValue(document.getElementById("providerType")));
        }
    }
    return true;
}

function setProviderType(type) {
    var jndiProviderPort = document.getElementById("jndiProviderPort");
    var jndiProviderUrl = document.getElementById("jndiProviderUrl");
    var providerType_hidden = document.getElementById("providerType_hidden");
    providerType_hidden.value = type;
    if ("port" == type) {
        jndiProviderPort.style.display = "";
        jndiProviderUrl.style.display = "none";
    } else {
        jndiProviderPort.style.display = "none";
        jndiProviderUrl.style.display = "";
    }
    return true;
}

function getCheckedValue(radioObj) {
    if (!radioObj) {
        return "";
    }
    var radioLength = radioObj.length;
    if (radioLength == undefined) {
        if (radioObj.checked) {
            return radioObj.value;
        } else {
            return "";
        }
    }
    for (var i = 0; i < radioLength; i++) {
        if (radioObj[i].checked) {
            return radioObj[i].value;
        }
    }
    return "";
}

function forward(destinationJSP) {
    location.href = destinationJSP;
}

function deleteRow(name, msg) {
    CARBON.showConfirmationDialog(msg + "' " + name + " ' ?", function() {
        document.location.href = "deletedatasource.jsp?" + "alias=" + name;
    });
}

function editRow(name) {

    document.location.href = "editdatasource.jsp?" + "alias=" + name;
}

function goBackOnePage(){
     history.go(-1);
}

function testConnection(namemsg, invalidnamemsg, drivermsg, urlmsg, validquerymsg, succcessmsg) {

    if (!isDSValid(namemsg, invalidnamemsg, drivermsg, urlmsg)) {
        return false;
    }

    if (trim(document.getElementById('validationquery').value) == '') {
        CARBON.showConfirmationDialog(validquerymsg, function () {
            doTestConnection(succcessmsg);
        });
    } else {
        doTestConnection(succcessmsg);
    }
    return false;
}

function doTestConnection(successmsg) {
    var url = '../datasource/validateconnection-ajaxprocessor.jsp?validationquery=' + document.getElementById('validationquery').value
            + "&alias=" + document.getElementById('alias').value + "&driver=" + document.getElementById('driver').value +
              "&password=" + document.getElementById('password').value + "&user=" + document.getElementById('user').value +
              "&url=" + document.getElementById('url').value;
    jQuery.post(url, ({}),
            function(data, status) {
                if (status != "success") {
                    CARBON.showWarningDialog("Error Occurred!");
                } else {
                    var returnValue = trim(data);
                    if (returnValue != null && returnValue != undefined && returnValue != "" && returnValue != "true") {
                        CARBON.showErrorDialog(returnValue);
                        return false;
                    } else {
                       CARBON.showInfoDialog(successmsg);
                        return false;
                    }
                }
            });
}

function trim(stringValue) {
      return stringValue.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
}

function autoSelect(){
    setProviderType(document.getElementById("providerType_hidden").value);
    setRepository(document.getElementById("dsrepotype_hidden").value);
}
