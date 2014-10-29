/*all validation functions required by the .jsp files*/


function validateEmptyById(fldId) {
    var fld = document.getElementById(fldId);
    var error = "";
    var value = fld.value;
    if (value.length == 0) {
        error = fld.name + " ";
        return error;
    }

    value = value.replace(/^\s+/, "");
    if (value.length == 0) {
        error = fld.name + "(contains only spaces) ";
        return error;
    }

    return error;
}


function isAtleastOneCheckedIfExisting(fldname) {
    var foundOne = false;
    var elems = document.getElementsByName(fldname);

    if (elems.length == 0) {
        foundOne = true;
    } else {
        var counter = 0;
        for (counter = 0; counter < elems.length; counter++) {
            if (elems[counter].checked == true)
                foundOne = true;
        }
    }
    return foundOne;
}

function isAtleastOneChecked(fldname) {
    var foundOne = false;
    var elems = document.getElementsByName(fldname);

    var counter = 0;
    for (counter = 0; counter < elems.length; counter++) {
        if (elems[counter].checked == true)
            foundOne = true;
    }
    return foundOne;
}

function doSelectAll(targetfldname) {
    var elems = document.getElementsByName(targetfldname);
    for (var counter = 0; counter < elems.length; counter++) {
        if(!elems[counter].disabled){
            elems[counter].checked = true;
        }
    }
}

function doUnSelectAll(targetfldname) {
    var elems = document.getElementsByName(targetfldname);
    for (var counter = 0; counter < elems.length; counter++) {
        if(!elems[counter].disabled){
            elems[counter].checked = false;
        }
    }
}

function validateStorePassword(fld1name) {
    var error = "";
    var invalid = "&";
    var pw1 = document.getElementById(fld1name).value;

    // check for spaces
    if (pw1 != null && pw1.length > 0) {
        if (pw1.indexOf(invalid) > -1) {
            error = "Sorry, invalid charactor in password";
            return error;
        }
    }
    return error;
}

function checkStorePasswordRetype(fld1name, fld2name) {
    var error = "";
    var invalid = "&";
    var pw1 = document.getElementById(fld1name).value;
    var pw2 = document.getElementById(fld2name).value;
    // check for a value in both fields. 

    if (pw1 != pw2) {
        error = "Password and Password Repeat do not match. Please re-enter.";
        return error;
    }

    return error;
}
