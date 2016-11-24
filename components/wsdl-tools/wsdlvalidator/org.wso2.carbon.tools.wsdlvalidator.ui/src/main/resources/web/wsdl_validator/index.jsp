<%--
 * Copyright 2006,2007 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
--%>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<!-- This page is included to display messages which are set to request scope or session scope -->
<jsp:include page="../dialog/display_messages.jsp"/>

<fmt:bundle basename="org.wso2.carbon.wsdlvalidator.ui.i18n.Resources">
<carbon:breadcrumb
		label="wsdlvalidator.breadcrumbtext"
		resourceBundle="org.wso2.carbon.wsdlvalidator.ui.i18n.Resources"
		topPage="true"
		request="<%=request%>" />

<!-- Required CSS -->
<script type="text/javascript" src="../carbon/global-params.js"></script>
<script type="text/javascript" src="../yui/build/utilities/utilities.js"></script>

<script language="JavaScript" type="text/javascript">

    wso2.wsf.Util.initURLs();

    var frondendURL = wso2.wsf.Util.getServerURL() + "/";

    function clearText() {
        document.getElementById("fileResult").value = "";
        document.getElementById("urlResult").value = "";
    }

    var fileCallback =
        {
        success:handleSuccessForFile,
        failure:handleFailureForFile,
        upload:handleSuccessForFile
    };

    var urlCallback =
        {
        success:handleSuccessForURL,
        failure:handleFailureForURL,
        upload:handleSuccessForURL
    };

    function handleSuccessForFile(o) {
        var browser = WSRequest.util._getBrowser();
        document.getElementById("urlResult").style.display = "none";

        var value;
        if (browser == "ie" || browser == "ie7") {
            var value2 = "";
            if (o.responseXML.documentElement.childNodes[0].childNodes[1] != null) {
                value2 = o.responseXML.documentElement.childNodes[0].childNodes[1].firstChild.nodeValue;
            }
            value = o.responseXML.documentElement.childNodes[0].childNodes[0].firstChild.nodeValue + value2;
            document.getElementById("fileResult").innerHTML = value;
        } else {
            value = o.responseXML.documentElement.firstChild.textContent;
            document.getElementById("fileResult").innerHTML = value;
        }

        if (value == "WSDL DOCUMENT IS VALID") {
            document.getElementById("fileResult").style.color = "#008000";
        } else {
            document.getElementById("fileResult").style.color = "#ff0000";
        }

        document.getElementById("fileResult").style.display = "";
    }

    function handleFailureForFile(o) {
        CARBON.showErrorDialog("<fmt:message key="wsdlvalidator.failed"/>");
    }

    function handleSuccessForURL(o) {
        var browser = WSRequest.util._getBrowser();
        document.getElementById("fileResult").style.display = "none";

        var value;
        if (browser == "ie" || browser == "ie7") {
            var value2 = "";
            if (o.responseXML.documentElement.childNodes[0].childNodes[1] != null) {
                value2 = o.responseXML.documentElement.childNodes[0].childNodes[1].firstChild.nodeValue;
            }
            value = o.responseXML.documentElement.childNodes[0].childNodes[0].firstChild.nodeValue + value2;
            document.getElementById("urlResult").innerHTML = value;
        } else {
            value = o.responseXML.documentElement.firstChild.textContent;
            document.getElementById("urlResult").innerHTML = value;
        }
        if (value == "WSDL DOCUMENT IS VALID") {
            document.getElementById("urlResult").style.color = "#008000";
        } else {
            document.getElementById("urlResult").style.color = "#ff0000";
        }

        document.getElementById("urlResult").style.display = "";
    }

    function handleFailureForURL(o) {
        CARBON.showErrorDialog("<fmt:message key="wsdlvalidator.failed"/>");
    }

    function submitFormAsync(formId, isFileUpload) {
        clearText();
        var form = document.getElementById(formId);

        if ((isFileUpload) && (document.getElementById("filedata").value == "")) {
            CARBON.showWarningDialog("<fmt:message key="wsdlvalidator.invalid.file"/>");
        } else if ((!isFileUpload) && (document.getElementById("url").value == "")) {
            CARBON.showWarningDialog("<fmt:message key="wsdlvalidator.invalid.url"/>");
        } else {
            if (isFileUpload) {
                YAHOO.util.Connect.setForm(form, true, true);
                YAHOO.util.Connect.asyncRequest("POST", frondendURL + "WSDLValidatorService/validateFromFile", fileCallback, null);
            } else {
                YAHOO.util.Connect.setForm(form);
                YAHOO.util.Connect.asyncRequest("POST", frondendURL + "WSDLValidatorService/validateFromUrl", urlCallback, null);
            }
        }
    }

    function noEnter(e) {
        var keynum = "";
        if (window.event) // IE
        {
            keynum = e.keyCode;

            if (keynum == 13) {
                e.cancelBubble = true;
                e.returnValue = false;
            }
        }
        else if (e.which) // Netscape/Firefox/Opera
        {
            keynum = e.which;
            if (keynum == 13) {
                e.preventDefault();
            }
        }

    }

    jQuery(document).ready(clearText);

</script>

    <div id="middle">
        <h2><fmt:message key="wsdlvalidator.headertext"/></h2>

        <div id="workArea">
            <form id="wsdlvalidatorform1" action="" method="post" enctype="multipart/form-data" target="uploadFrame">
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th><fmt:message key="wsdlvalidator.upload.wsdl"/></th>
                    </tr>
                    </thead>
                    <tr>
                        <td class="formRow">
                            <table class="normal">
                                <tr>
                                    <td style="width:150px;"><label><fmt:message key="wsdlvalidator.wsdl.location"/></label></td>
                                    <td><input size="50" type="file" id="filedata" name="filedata" onkeydown="noEnter(event);"/></td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input type="button" id="doUpload" name="doUpload" value="<fmt:message key="wsdlvalidator.validate.file"/>"
                                   onclick="submitFormAsync('wsdlvalidatorform1',true);"/>
                        </td>
                    </tr>
                </table>
            </form>
            <br>
            <div>
                <div id="fileResult" style="display: none;"></div>
            </div>
            <br>
            <form id="wsdlvalidatorform2" action="" method="post" enctype="multipart/form-data" target="uploadFrame">
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th><fmt:message key="wsdlvalidator.read.wsdl.url"/></th>
                    </tr>
                    </thead>
                    <tr>
                        <td class="formRow">
                            <table class="normal">
                                <td style="width:150px;"><fmt:message key="wsdlvalidator.wsdl.url"/></td>
                                <td><input size="50" type="text" id="url" name="url" onkeydown="noEnter(event);"/></td>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input type="button" value="<fmt:message key="wsdlvalidator.validate.url"/>"
                                   onclick="submitFormAsync('wsdlvalidatorform2',false);"/>
                        </td>
                    </tr>
                </table>
            </form>
            <br>
            <div>
                <div id="urlResult" style="display: none;"></div>
            </div>
            <br>
        </div>
    </div>
</fmt:bundle>
