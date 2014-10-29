var perviousICFac = '';
var perviousProviderURL = '';
var perviousProviderPort = '';


function addServiceParamRow(key, value, table, delFunction) {
    addRowForSP(key, value, table, delFunction);
 }

function addRowForSP(prop1, prop2, table, delFunction) {
    var tableElement = document.getElementById(table);
    var param1Cell = document.createElement('td');
    var inputElem = document.createElement('input');
    inputElem.type = "text";
    inputElem.name = "spName";
    inputElem.value = prop1;
    param1Cell.appendChild(inputElem); //'<input type="text" name="spName" value="'+prop1+' />';


    var param2Cell = document.createElement('td');
    inputElem = document.createElement('input');
    inputElem.type = "text";
    inputElem.name = "spValue";
    inputElem.value = prop2;
    param2Cell.appendChild(inputElem);

    var delCell = document.createElement('td');
    delCell.innerHTML='<a id="deleteLink" href="#" onClick="' + delFunction + '(this.parentNode.parentNode.rowIndex)" alt="Delete" class="icon-link" style="background-image:url(../admin/images/delete.gif);">Delete</a>';

    var rowtoAdd = document.createElement('tr');
    rowtoAdd.appendChild(param1Cell);
    rowtoAdd.appendChild(param2Cell);
    rowtoAdd.appendChild(delCell);

    tableElement.tBodies[0].appendChild(rowtoAdd);
    tableElement.style.display = "";

    alternateTableRows(tableElement, 'tableEvenRow', 'tableOddRow');
}

function showPropConfigurations() {
	  var pwdMngrSymbolMax =  document.getElementById('dsProperties');
	  var configFields = document.getElementById('dsPropFields');
	  if(configFields.style.display == 'none') {
	    pwdMngrSymbolMax.setAttribute('style','background-image:url(images/minus.gif);');
	    configFields.style.display = '';
	  } else {
	      pwdMngrSymbolMax.setAttribute('style','background-image:url(images/plus.gif);');
	      configFields.style.display = 'none';
	  }
}

function showJNDIConfigurations() {
	var pwdMngrSymbolMax =  document.getElementById('jndiconfigheader');
	var configFields = document.getElementById('jndiconfig');
	if(configFields.style.display == 'none') {
	   pwdMngrSymbolMax.setAttribute('style','background-image:url(images/minus.gif);');
	   configFields.style.display = '';
	} else {
	   pwdMngrSymbolMax.setAttribute('style','background-image:url(images/plus.gif);');
	   configFields.style.display = 'none';
	}
}

function isDSValid(namemsg, invalidnamemsg, drivermsg, urlmsg, customdsmsg) {

    var name = document.getElementById('dsName').value;
    if (name == null || name == '') {
        CARBON.showWarningDialog(namemsg);
        return false;
    }
    
    var dsType = document.getElementById('dsType').value;
    var customDsType = document.getElementById('customDsType').value;
    if (dsType != 'RDBMS' && (customDsType == null || customDsType == '')) {
    	CARBON.showWarningDialog(customdsmsg);
        return false;
    }

    var iChars = "!@#$%^&*()+=[]\\\';,/{}|\":<>?";
    for (var i = 0; i < name.length; i++) {
        if (iChars.indexOf(name.charAt(i)) != -1) {
            CARBON.showWarningDialog(invalidnamemsg);
            return false;
        }
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

function forward(destinationJSP) {
    location.href = destinationJSP;
}

function deleteRow(name, msg) {
    CARBON.showConfirmationDialog(msg + "' " + name + " ' ?", function() {
        document.location.href = "deletedatasource.jsp?" + "name=" + name;
    });
}

function editRow(name) {

    document.location.href = "newdatasource.jsp?" + "dsName=" + name + "&edit=true";
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
		var password = document.getElementById('password').value;
	} else {
		var dsclassname = document.getElementById('dsclassname').value;
		var dsproviderProperties = document.getElementById('dsproviderProperties').value;
	}
	var requestUrl = '../ndatasource/validateconnection-ajaxprocessor.jsp?&dsName=' + document.getElementById('dsName').value+'&dsProviderType='+dsProvider+
    	'&dsclassname='+dsclassname+'&dsclassname='+dsclassname+'&dsproviderProperties='+dsproviderProperties+'&driver='+driver+
    	'&url='+encodeURIComponent(url)+'&username='+username+'&password='+password+'&dsType='+datasourceType+'&customDsType='+datasourceCustomType;
    jQuery.post(requestUrl, ({}),
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

function ValidateProperties() {
	if (document.getElementById("dsType").value == 'RDBMS') {
		if (document.getElementById("maxActive").value < 0) {
			CARBON.showErrorDialog("Please enter a positive value for maxActive");
			return false;
		}
		if (document.getElementById("maxIdle").value < 0) {
			CARBON.showErrorDialog("Please enter a positive value for maxIdle");
			return false;
		}
		if (document.getElementById("minIdle").value < 0) {
			CARBON.showErrorDialog("Please enter a positive value for minIdle");
			return false;
		}
		if (document.getElementById("initialSize").value < 0) {
			CARBON.showErrorDialog("Please enter a positive value for initialSize");
			return false;
		}
		if (document.getElementById("maxWait").value < 0) {
			CARBON.showErrorDialog("Please enter a positive value for maxWait");
			return false;
		}
		if (document.getElementById("timeBetweenEvictionRunsMillis").value < 0) {
			CARBON.showErrorDialog("Please enter a positive value for timeBetweenEvictionRunsMillis");
			return false;
		}
		if (document.getElementById("numTestsPerEvictionRun").value < 0) {
			CARBON.showErrorDialog("Please enter a positive value for numTestsPerEvictionRun");
			return false;
		}
		if (document.getElementById("minEvictableIdleTimeMillis").value < 0) {
			CARBON.showErrorDialog("Please enter a positive value for minEvictableIdleTimeMillis");
			return false;
		}
		if (document.getElementById("removeAbandonedTimeout").value < 0) {
			CARBON.showErrorDialog("Please enter a positive value for removeAbandonedTimeout");
			return false;
		}
		if (document.getElementById("validationInterval").value < 0) {
			CARBON.showErrorDialog("Please enter a positive value for validationInterval");
			return false;
		}
		if (document.getElementById("abandonWhenPercentageFull").value < 0) {
			CARBON.showErrorDialog("Please enter a positive value for abandonWhenPercentageFull");
			return false;
		}
		if (document.getElementById("maxAge").value < 0) {
			CARBON.showErrorDialog("Please enter a positive value for maxAge");
			return false;
		}
		if (document.getElementById("suspectTimeout").value < 0) {
			CARBON.showErrorDialog("Please enter a positive value for suspectTimeout");
			return false;
		}
		if (document.getElementById("validationQueryTimeout").value < 0) {
        	CARBON.showErrorDialog("Please enter a positive value for Validation Query Timeout");
        	return false;
        }
	}
	return true;
}

function disableForm(){
	if(document.getElementById("isSystem").value == 'true') {
		document.getElementById("description").readOnly = true;
		document.getElementById("customDsType").readOnly = true;
		if (document.getElementById("configuration") != null) {
			document.getElementById("configuration").readOnly = true;
		}
		editAreaLoader.execCommand('configuration', 'set_editable', !editAreaLoader.execCommand('configuration', 'is_editable'));
		if (document.getElementById("driver") != null) {
			document.getElementById("driver").readOnly = true;
		}
		if (document.getElementById("url") != null) {
			document.getElementById("url").readOnly = true;
		}
		if (document.getElementById("username") != null) {
			document.getElementById("username").readOnly = true;
		}
		if (document.getElementById("password") != null) {
			document.getElementById("password").readOnly = true;
		}
		if (document.getElementById("dsclassname") != null) {
			document.getElementById("dsclassname").readOnly = true;
		}
		if (document.getElementById("dsType").value == 'RDBMS' ) {
			document.getElementById("jndiname").readOnly = true;
			document.getElementById("useDataSourceFactory").readOnly = true;
			document.getElementById("defaultCatalog").readOnly = true;
			document.getElementById("maxActive").readOnly = true;
			document.getElementById("maxIdle").readOnly = true;
			document.getElementById("minIdle").readOnly = true;
			document.getElementById("initialSize").readOnly = true;
			document.getElementById("maxWait").readOnly = true;
			document.getElementById("validationquery").readOnly = true;
			document.getElementById("validatorClassName").readOnly = true;
			document.getElementById("timeBetweenEvictionRunsMillis").readOnly = true;
			document.getElementById("numTestsPerEvictionRun").readOnly = true;
			document.getElementById("minEvictableIdleTimeMillis").readOnly = true;
			document.getElementById("removeAbandonedTimeout").readOnly = true;
			document.getElementById("connectionProperties").readOnly = true;
			document.getElementById("initSQL").readOnly = true;
			document.getElementById("jdbcInterceptors").readOnly = true;
			document.getElementById("validationInterval").readOnly = true;
			document.getElementById("abandonWhenPercentageFull").readOnly = true;
			document.getElementById("maxAge").readOnly = true;
			document.getElementById("useEquals").readOnly = true;
			document.getElementById("suspectTimeout").readOnly = true;
			document.getElementById("validationQueryTimeout").readOnly = true;
			
			document.getElementById("datasourceProvider").disabled = true;
			document.getElementById("defaultTransactionIsolation").disabled = true;
			document.getElementById("testOnBorrow").disabled = true;
			document.getElementById("testOnReturn").disabled = true;
			document.getElementById("testWhileIdle").disabled = true;
			document.getElementById("accessToUnderlyingConnectionAllowed").disabled = true;
			document.getElementById("removeAbandoned").disabled = true;
			document.getElementById("logAbandoned").disabled = true;
			document.getElementById("fairQueue").disabled = true;
			document.getElementById("jmxEnabled").disabled = true;
			document.getElementById("useEquals").disabled = true;
			document.getElementById("alternateUsernameAllowed").disabled = true;
	}
	} 
}

