<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.wso2.carbon.java2wsdl.ui.client.Util" %>
<%@ page import="javax.xml.namespace.QName" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type="text/javascript" src="../carbon/global-params.js"></script>
<script type="text/javascript" src="../yui/build/utilities/utilities.js"></script>
<fmt:bundle basename="org.wso2.carbon.java2wsdl.ui.i18n.Resources">
<carbon:breadcrumb label="java2wsdl"
		resourceBundle="org.wso2.carbon.java2wsdl.ui.i18n.Resources"
		topPage="true" request="<%=request%>" />
<%
    String backendServerURL = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),
            session);
    String dynamicJS = "";
    OMElement omElement = Util.getJava2WsdlOptions();
    Iterator arguments = omElement.getChildrenWithLocalName("argument");

%>

<script type="text/javascript">

    wso2.wsf.Util.initURLs();

    var frondendURL = wso2.wsf.Util.getServerURL() + "/";

    function doValidation() {
        var divArray = document.getElementsByName('jarResourceDiv');
        var len = divArray.length;
        var hasValues = true;
        for (var j = 0; j < len; j++) {
            var children = divArray[j].childNodes.length;
            for (var k = 0; k < children; k++) {
                var child = divArray[j].childNodes[k];
                if (child != undefined && child.nodeType == 1 && child.getAttribute("type") == "file") {
                    var nodeValue = child.value;
                    if (nodeValue == undefined || nodeValue == "") {
                        hasValues = false;
                        break;
                    } else if ("jar" != nodeValue.substring(nodeValue.length-3)) {
                        CARBON.showWarningDialog('<fmt:message key="error.selectJar"/>');
                        return false;
                    }
                }
            }
        }
        if (!hasValues) {
            CARBON.showWarningDialog('<fmt:message key="error.selectResources"/>');
            return false;
        }

        var cn = document.getElementById("id_cn");
        if (cn.value == '') {
            CARBON.showWarningDialog('<fmt:message key="error.missingServiceImplementation"/>');
            return false;
        }

        return true;
    }

    var count = 1;

    function addLibraryFileuplod(objDiv) {
        var divElem = document.createElement('div');
        var idAttr = document.createAttribute('id');
        idAttr.value = "jarResourceDiv" + count;
        divElem.attributes.setNamedItem(idAttr);
        var nameAttr = document.createAttribute('name');
        nameAttr.value = "jarResourceDiv";
        divElem.attributes.setNamedItem(nameAttr);

        var elem = document.createElement('input');
        nameAttr = document.createAttribute('name');
        nameAttr.value = "jarResourcejava2wsdl" + count;
        idAttr = document.createAttribute('id');
        idAttr.value = "jarResourcejava2wsdl" + count;
        var sizeAttr = document.createAttribute('size');
        sizeAttr.value = "50";
        var typeAttr = document.createAttribute('type');
        typeAttr.value = "file";
        elem.attributes.setNamedItem(nameAttr);
        elem.attributes.setNamedItem(idAttr);
        elem.attributes.setNamedItem(sizeAttr);
        elem.attributes.setNamedItem(typeAttr);
        divElem.appendChild(elem);

        var blankLabelElem = document.createElement('label');
        blankLabelElem.innerHTML = "&nbsp;";
        divElem.appendChild(blankLabelElem);

        elem = document.createElement('input');
        elem.onclick = function(){
            var parent = this.parentNode;
            parent.parentNode.removeChild(parent);
        };
        typeAttr = document.createAttribute('type');
        typeAttr.value = "button";
        var valueAttr = document.createAttribute('value');
        valueAttr.value = "-";
        var classAttr = document.createAttribute('class');
        classAttr.value = "button";
        idAttr = document.createAttribute('id');
        idAttr.value = "jarResourcejava2wsdlRemove" + count;
        elem.attributes.setNamedItem(typeAttr);
        elem.attributes.setNamedItem(valueAttr);
        elem.attributes.setNamedItem(classAttr);
        elem.attributes.setNamedItem(idAttr);
        divElem.appendChild(elem);

        objDiv.appendChild(divElem);
        count ++;
    }

    var options = new Object();

    function submit() {
        var valid = doValidation();
        if (valid) {
            submitFormAsync();
        }
    }

    function startJava2WSDL(optionsObj, uuids) {
        populateOptions();
        var bodyXML = '<req:java2wsdlWithResources xmlns:req="http://java2wsdl.carbon.wso2.org">\n';
        for (var o in optionsObj) {
            bodyXML += '<options><![CDATA[' + '-' + o.substring(3) + ']]></options>\n';
            var oVal = optionsObj[o];
            if (oVal != null && oVal.length != 0) {
                bodyXML += '<options><![CDATA[' + oVal + ']]></options>\n';
            }
        }
        for (var i = 0; i < uuids.length; i ++) {
            bodyXML += '<uuids>' + uuids[i] + '</uuids>';
        }
        bodyXML += '</req:java2wsdlWithResources>';
        var generate_button = document.getElementById("generate_button");
        generate_button.disabled = true;
        var callURL = frondendURL + "Java2WSDLService/java2wsdlWithResources" ;
        wso2.wsf.Util.cursorWait();
        new wso2.wsf.WSRequest(callURL, "urn:java2wsdlWithResources", bodyXML, java2wsdlCallback, [], java2wsdlOnErrorCallback);
    }

    function java2wsdlOnErrorCallback() {
        var generate_button = document.getElementById("generate_button");
        generate_button.disabled = false;
        this.defaultError.call(this);
    }

    function java2wsdlCallback() {
        //response will be a link and it will call the download dialog
        var data = this.req.responseXML;
        var responseTextValue = getResponseValue(data);
        var generate_button = document.getElementById("generate_button");
        generate_button.disabled = false;
        window.open(responseTextValue);
    }

    function getResponseValue(responseXML) {
        var returnElementList = responseXML.getElementsByTagName("ns:return");
        // Older browsers might not recognize namespaces (e.g. FF2)
        if (returnElementList.length == 0)
            returnElementList = responseXML.getElementsByTagName("return");
        var returnElement = returnElementList[0];

        return returnElement.firstChild.nodeValue;
    }

    var callback =
    {
        upload:handleUpload
    };

    function handleUpload(o) {
        var responseText = o.responseText;
        if (responseText) {
            var index = responseText.indexOf("<pre>");
            if (index < 0)
                index = responseText.indexOf("<PRE>");
            var endIndex = responseText.indexOf("</pre>");
            if (endIndex < 0)
                endIndex = responseText.indexOf("</PRE>");
            var uuidString ='';
            if (index < 0)
                uuidString = responseText;
            else
                uuidString = responseText.substring(index + 5, endIndex);
            
            var uuids = new Array();
            uuids = uuidString.split(",");
            startJava2WSDL(options, uuids);
        } else {
            CARBON.showErrorDialog('<fmt:message key="error.errorUploadingFile"/>');
        }
    }

    function submitFormAsync() {
        var form = document.getElementById("form");
        YAHOO.util.Connect.setForm(form, true, true);
        YAHOO.util.Connect.asyncRequest("POST", form.getAttribute("action"), callback, null);
    }
</script>

<div id="middle">
    <h2><fmt:message key="java2wsdl"/></h2>

    <div id="workArea">
        <table width="100%">
            <tr>
                <td>
                    <form id="form" method="post" enctype="multipart/form-data"
                          action="../../fileupload/tools">

                        <table class="styledLeft">
                            <thead>
                            <tr>
                                <th><fmt:message key="addResources"/></th>
                            </tr>
                            </thead>
                            <tr>
                                <td class="formRow">
                                    <table class="normal">
                                        <tr>

                                            <td>
                                                <label><fmt:message key="resources"/></label>
                                            </td>
                                            <td>
                                                <div id="java2wsdlResourceDivId">
                                                    <div name="jarResourceDiv" id="jarResourceDiv">
                                                        <input type="file" id="jarResourcejava2wsdl"
                                                               name="jarResourcejava2wsdl"
                                                               size="50"/>
                                                        <input type="button" class="button"
                                                               value="+"
                                                               onClick="addLibraryFileuplod(document.getElementById('java2wsdlResourceDivId'));return false;"/>
                                                    </div>
                                                </div>
                                            </td>

                                        </tr>
                                    </table>
                                </td>
                            </tr>
                        </table>
                    </form>
                </td>
            </tr>
            <tr>
                <td>
                    <h3><fmt:message key="selectOptions"/></h3>
                    <table class="styledLeft" id="java2wsdlOptionsTable" width="100%">
                        <thead>
                        <tr>
                            <th><fmt:message key="option"/></th>
                            <th><fmt:message key="description"/></th>
                            <th><fmt:message key="selectValue"/></th>
                        </tr>
                        </thead>
                        <tbody>

                        <%
                            while (arguments.hasNext()) {
                                OMElement argument = (OMElement) arguments.next();
                                String uiType = argument.getAttributeValue(
                                        new QName(null, "uiType"));
                                String mandatory = argument.getAttributeValue(
                                        new QName(null, "mandatory"));
                                String name = argument.getFirstChildWithName(
                                        new QName(null, "name")).getText();
                                String description = argument.getFirstChildWithName(
                                        new QName(null, "description")).getText();
                                if (!"skip".equals(uiType)) {
                        %>
                        <tr>
                            <%
                                if (mandatory != null && "true".equals(mandatory)) {
                            %>
                            <td><%=name%><font color="red">*</font></td>
                            <%
                            } else {
                            %>
                            <td><%=name%>
                            </td>
                            <%
                                }
                            %>
                            <td><%=description%>
                            </td>
                            <td>
                                <%
                                    name = "id_" + name;
                                    if ("text".equals(uiType)) {
                                        dynamicJS = dynamicJS + "var obj_" + name +
                                                "= document.getElementById('" + name + "');\n" +
                                                "if (obj_" + name + ".value != '') {\n" +
                                                "    options['" + name + "'] = obj_" + name + ".value;\n" +
                                                "}\n";
                                %>
                                <input class="toolsClass" type="text" size="40" id="<%=name%>"/>
                                <%
                                } else if ("text-area".equals(uiType)) {
                                    dynamicJS = dynamicJS + "var obj_" + name +
                                            "= document.getElementById('" + name + "');\n" +
                                            "if (obj_" + name + ".value != '') {\n" +
                                            "    options['" + name + "'] = obj_" + name + ".value;\n" +
                                            "}\n";
                                %>
                                <textarea class="toolsClass" style="height:100px;width:345px"
                                          id="<%=name%>"></textarea>
                                <%
                                } else if ("check".equals(uiType)) {
                                    dynamicJS = dynamicJS + "var obj_" + name +
                                            " = document.getElementById('" + name + "');\n" +
                                            "if (obj_" + name + ".checked) {\n" +
                                            "    options['" + name + "'] = \"\";\n" +
                                            "}\n";
                                %>
                                <input class="toolsClass" type="checkbox" id="<%=name%>"/>
                                <%
                                } else if ("option".equals(uiType)) {

                                %>
                                <select class="toolsClass" id="<%=name%>">
                                    <%
                                        OMElement values = argument.getFirstChildWithName(
                                                new QName(null, "values"));
                                        Iterator iterator =
                                                values.getChildrenWithLocalName("value");
                                        while (iterator.hasNext()) {
                                            OMElement value = (OMElement) iterator.next();
                                            String valueText = value.getText();
                                    %>
                                    <option value="<%=valueText%>"><%=valueText%>
                                    </option>
                                    <%
                                        }
                                    %>
                                </select>
                                <%
                                        dynamicJS = dynamicJS + "var obj_" + name +
                                                " = document.getElementById('" + name + "');\n" +
                                                "options['" + name + "'] = obj_" + name + "[obj_" + name +
                                                ".selectedIndex].value;\n";
                                    }
                                %>
                            </td>
                        </tr>
                        <%
                            }
                            }
                        %>
                        </tbody>
                    </table>
                </td>
            </tr>
            <tr height="10">
               <td/>
            </tr>
            <tr>
                <td>
                    <script type="text/javascript">
                        alternateTableRows('java2wsdlOptionsTable', 'tableEvenRow', 'tableOddRow');
                    </script>
                    <div class="buttonrow">
                        <script type="text/javascript">
                            function populateOptions() {
                            <%=dynamicJS%>
                            }
                        </script>
                        <input type="button" class="button" value='<fmt:message key="generate"/> &gt;'
                               onclick="submit();" id="generate_button"/>
                    </div>
                </td>
            </tr>
        </table>
    </div>
</div>
</fmt:bundle>
