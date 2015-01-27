/*
 log.js contains scripts need to handle log information.
 */
function getTenantSpecificIndex () {
	var tenantDomain = document.getElementById("tenantDomain").value;
	var pageNumber = document.getElementById("pageNumber").value;
	location.href = "url_mapper_view.jsp?tenantDomain="+tenantDomain+"&pageNumber="+pageNumber;
}
function checkMappingAvailability(urlmapping) {
    var reason = validateEmpty(urlmapping);
    if (reason == "") {
        reason += checkMapping(urlmapping);
    }
    return reason;
}

function validateEmpty(urlmapping) {
    var mapping = urlmapping.toString();
    var error = "";
    if (mapping.length == 0) {
        error = "Url Mapping is empty. Please provide a valid mapping.";
        return error;
    }
    mapping = mapping.replace(/^\s+/, "");
    if (mapping.length == 0) {
        error = "Url Mapping contain only white spaces. Please provide a valid mapping.";
        return error;
    }

    return error;
}

function checkMapping(urlmapping)
{
    var error = "";
    var domain = urlmapping.toString();
    var lastIndexOfDot = domain.lastIndexOf(".");
    var indexOfDot = domain.indexOf(".");
    var illegalChars = /([^a-zA-Z0-9\._\-])/; // allow only letters and numbers . - _and period
    //var extension = domain.substring(lastIndexOfDot, domain.length);
    /*if (extension.indexOf("-trial") >= 0 || extension.indexOf("-unverified") >= 0) {
        // we are not allowing to create a domain with -trial or -unverified is in the extension
        error = "The url mapping you entered is not valid. Please enter a valid url mapping.";
    }
    else if ((lastIndexOfDot <= 0)) {
        error = "Invalid domain: " + domain + ". You should have an extension to your domain.";
    }*/

    if ((indexOfDot == 0) && (lastIndexOfDot == (domain.length-1))) {
        error = "Invalid mapping, starting with '.' and ending with '.' ";
    } else if (indexOfDot == 0) {
        error = "Invalid mapping, starting with '.'";
    } else if (lastIndexOfDot == (domain.length-1)) {
        error = "Invalid mapping, ending with '.'"
    } else if (illegalChars.test(domain)) {
        error = "The domain only allows letters, numbers, '.', '-' and '_'. <br />";
    }
    return error;
}


function getTenantSpecificIndex () {
	var tenantDomain = document.getElementById("tenantDomain").value;
	var pageNumber = document.getElementById("pageNumber").value;
	location.href = "url_mapper_view.jsp?tenantDomain="+tenantDomain+"&pageNumber="+pageNumber;
}


