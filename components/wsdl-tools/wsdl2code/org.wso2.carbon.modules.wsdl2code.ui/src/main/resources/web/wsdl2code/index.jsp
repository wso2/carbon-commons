<%--
 Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.

 WSO2 Inc. licenses this file to you under the Apache License,
 Version 2.0 (thewindow.location "License"); you may not use this file except
 in compliance with the License.
 You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 --%>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.wsdl2code.ui.WSDL2CodeClient" %>
<%@ page import="org.wso2.carbon.wsdl2code.ui.client.Util" %>
<%@ page import="javax.xml.namespace.QName" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.wso2.carbon.wsdl2code.ui.endpoints.EndPointsSetter" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%
    String backendServerURL = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),
            session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String generateClient = request.getParameter("generateClient");
    String endPointsStr = "";

    //For CXF web applicationsc
    String generateType= request.getParameter("resultType");
    String wsdl =null;
    String wadl =null;
    String generateMethod=null;
    if(generateType!=null && generateType.equalsIgnoreCase("cxf")){
        generateMethod=request.getParameter("api");
        if(generateMethod.equalsIgnoreCase("jaxws")){
            wsdl =generateClient;
        }else if(generateMethod.equalsIgnoreCase("jaxrs")){
            wadl =generateClient;
        }
    }
    if(generateType ==null){
       endPointsStr = request.getParameter("endpoints"); 
    }

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    WSDL2CodeClient wsdl2CodeClient;
    String dynamicJS = "";
    String downloadPath = null;

    try {
        wsdl2CodeClient = new WSDL2CodeClient(configContext, backendServerURL, cookie);
    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
%>
<script type="text/javascript">
    location.href = "../admin/error.jsp";
</script>
<%
        return;
    }

%>
<link href="../yui/build/container/assets/skins/sam/container.css" rel="stylesheet" type="text/css"
      media="all"/>
<script type="text/javascript" src="../carbon/global-params.js"></script>
<script type="text/javascript" src="../yui/build/utilities/utilities.js"></script>
<script type="text/javascript" src="../yui/build/container/container-min.js"></script>
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/dragdrop/dragdrop-min.js"></script>
<style type="text/css">
    .yui-skin-sam .yui-panel .hd {
        background: #C5D2EB repeat-x scroll;
        color: #000000;
        font-size: 93%;
        font-weight: bold;
        line-height: 2;
        padding: 0 10px;
    }
</style>

<fmt:bundle basename="org.wso2.carbon.wsdl2code.ui.i18n.Resources">
<carbon:breadcrumb
        label="wsdl2java"
        resourceBundle="org.wso2.carbon.wsdl2code.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>
<script type="text/javascript">

var options = new Object();

wso2.wsf.Util.initURLs();

var frondendURL = wso2.wsf.Util.getServerURL() + "/";

//TODO need to do validation here
function doValidation() {

}

function startCodegen(optionsObj) {

    <%if(generateClient!=null && generateType!=null && generateType.equalsIgnoreCase("cxf")){ %>
    startCodegenForCXF(optionsObj);
    return;
    <%}%>

    var genClientScenario = false;
    <%if (generateClient != null) { %>
    genClientScenario = true;
    <%}%>

    if (!genClientScenario && (document.getElementById("id_uri") == null ||
            document.getElementById("id_uri").value == "")) {
        CARBON.showWarningDialog('<fmt:message key="error.uri.field.empty"/>');
        return false;
    }

    if (document.getElementById("id_uri") != null) {
        var idUriElement = document.getElementById("id_uri").value;
        if (idUriElement != null && idUriElement.length > 0) {
            if (idUriElement.substring(0, 4) != "http" && idUriElement.substring(1, 6) != "extra") {
                CARBON.showWarningDialog('<fmt:message key="error.codegenFile.wrong"/>');
                return false;
            }
        }
    }

    populateOptions();
    var optionsString = "";

    <%if (generateClient != null) { %>
    optionsString += "-uri,<%= generateClient%>,";
    <%}%>

    for (var o in optionsObj) {
        optionsString += '-' + o.substring(3) + ',';
        var oVal = optionsObj[o];
        if (oVal != null && oVal.length != 0) {
            optionsString += oVal + ',';
        }
    }
    var size = optionsString.length - 1;
    optionsString = optionsString.substring(0, (optionsString.length - 1));

//        var generate_button = document.getElementById("generate_button");
//        generate_button.disabled = true;
//        wso2.wsf.Util.cursorWait();

    location.href = '../wsdl2code/codegen_ajaxprocessor.jsp?optionsString='+optionsString;
}

function startCodegenForCXF(optionsObj) {

    var genClientScenario = false;
    <%if (generateClient != null) { %>
    genClientScenario = true;
    <%}%>

    if (!genClientScenario && (document.getElementById("id_uri") == null ||
            document.getElementById("id_uri").value == "")) {
        CARBON.showWarningDialog('<fmt:message key="error.uri.field.empty"/>');
        return false;
    }

    if (document.getElementById("id_uri") != null) {
        var idUriElement = document.getElementById("id_uri").value;
        if (idUriElement != null && idUriElement.length > 0) {
            if (idUriElement.substring(0, 4) != "http" && idUriElement.substring(1, 6) != "extra") {
                CARBON.showWarningDialog('<fmt:message key="error.codegenFile.wrong"/>');
                return false;
            }
        }
    }

    populateOptions();
    var optionsString = "";

    <%if (generateClient != null) { %>
    optionsString += "-<%= generateMethod%>,";
    <%}%>

    <%if (wsdl != null) { %>
    optionsString += "-Service,<%= wsdl%>,";
    <%}else if(wadl!=null){%>
    optionsString += "-Service,<%= wadl%>,";
    <%}%>

    for (var o in optionsObj) {
        optionsString += '-' + o.substring(3) + ',';
        var oVal = optionsObj[o];
        if (oVal != null && oVal.length != 0) {
            optionsString += oVal + ',';
        }
    }
    var size = optionsString.length - 1;
    optionsString = optionsString.substring(0, (optionsString.length - 1));


//        var generate_button = document.getElementById("generate_button");
//        generate_button.disabled = true;
//        wso2.wsf.Util.cursorWait();

    location.href = '../wsdl2code/codegen_ajaxprocessor.jsp?optionsString='+optionsString+'&type=cxf';
}

//    function wsdl2codeOnErrorCallback(data) {
//        var generate_button = document.getElementById("generate_button");generate_button.disabled = false;
////        this.defaultError.call(this);
//        CARBON.showErrorDialog(data.responseText, null, null);
//        wso2.wsf.Util.cursorClear();
//    }
//
//    function startCodegenCallback(downLocation) {
//        var generate_button = document.getElementById("generate_button");
//        generate_button.disabled = false;
//        wso2.wsf.Util.cursorClear();
//        window.location = downLocation.toString();
//    }

var callback =
{
    upload:handleUpload
};

function getResponseValue(responseXML) {
    var returnElementList = responseXML.getElementsByTagName("ns:return");
    // Older browsers might not recognize namespaces (e.g. FF2)
    if (returnElementList.length == 0)
        returnElementList = responseXML.getElementsByTagName("return");
    var returnElement = returnElementList[0];

    return returnElement.firstChild.nodeValue;
}

function handleUpload(o) {
    var responseText = o.responseText;
    if (responseText) {
        var index = responseText.indexOf("<pre>");

        responseText = responseText.replace( new RegExp("<pre[^>]*>"),"");
        responseText = responseText.replace( new RegExp("</pre>"),"");
        var uuid = responseText;

        var divObj = document.getElementById("divCodegenFileupload");
        if (divObj) {
            divObj.innerHTML = "";
            divObj.style.display = "none";
        }
        divObj = document.getElementById(this.upload.codegenParentTextId);
        divObj.value = uuid;
    } else {
        CARBON.showWarningDialog('<fmt:message key="error.fileUploadFailed"/>');
    }
}

function submitFormAsync(codegenParentTextId) {
    var codegenFile = document.getElementById('codegenFile');
    var nodeValue = codegenFile.value;
    if (nodeValue == undefined || nodeValue == "") {
        CARBON.showWarningDialog('<fmt:message key="error.codegenFile.empty"/>');
        return false;
    }
    handleUpload.codegenParentTextId = codegenParentTextId;
    var form = document.getElementById("codegenFileUpload");
    YAHOO.util.Connect.setForm(form, true, true);
    YAHOO.util.Connect.asyncRequest("POST", form.getAttribute("action"), callback, null);
    hideDiv();
}

function hideDiv() {
    var divObj = document.getElementById("divCodegenFileupload");
    if (divObj) {
        divObj.innerHTML = "";
        divObj.style.display = "none";
    }
}

function showYUIPanel(hd, bd, width, innerPannelId, containerDivId) {
    var container = document.getElementById(containerDivId);
    container.innerHTML = "";
    var yuiBody = '<div class="hd">' + hd + '</div>' +
            '<div class="bd">' + bd + '</div>' +
            '<div class="ft"></div>';
    var yuiHolder = document.createElement("div");
    yuiHolder.setAttribute("id", innerPannelId);
    yuiHolder.innerHTML = yuiBody;
    container.appendChild(yuiHolder);

    var panel1 = new YAHOO.widget.Panel(innerPannelId, {
        width:width,
        zIndex:"500",
        visible:false,
        fixedcenter: true,
        close:true,
        draggable:true,
        constraintoviewport:true });
    panel1.render();

    container.style.display = "inline";

    panel1.show();
}

function codeGenFileUploadeHelper(codegenParentTextId, executor) {
    var submit = '<fmt:message key="submit"/>';
    var cancel = '<fmt:message key="cancel"/>';
    var uploadFile = '<fmt:message key="uploadFile"/>';
    var uploadFileHelp = ' ';
    if(executor == "wsdl") {
        uploadFileHelp = '<fmt:message key="uploadFileHelp"/>'
    }

    var innerHTML = "<div id='formset'><form method='post' id='codegenFileUpload' name='codegenFileUpload' " +
            "action='../../fileupload/"+executor+"' enctype='multipart/form-data' target='self'><fieldset>" +
            "<legend>" + uploadFile + "</legend><div><input type='file' size='40' name='codegenFile'" +
            " id='codegenFile'/></div><div><p>" + uploadFileHelp + "</p></div><div>"+
            "<input type='button' value=" + submit + " onclick=\"submitFormAsync('" +
            codegenParentTextId +
            "')\"/><input type='button' value=" + cancel + " onclick='hideDiv()'/></div></fieldset></form></div>";
    var header = '<fmt:message key="uploadFileTitle"/>';
    showYUIPanel(header, innerHTML, "500px", "innerCodegenId", "divCodegenFileupload");
}
</script>

<div id="middle">
    <% if(generateClient!=null && generateType!=null && generateType.equals("cxf")){ %>
    <h2><fmt:message key="webapp.client"/></h2>
    <% }else{ %>
    <h2><fmt:message key="wsdl2java"/></h2>
    <% } %>

    <div id="divCodegenFileupload" style="display:none;" class="yui-skin-sam"></div>
    <%
        OMElement omElement=null;
        if (generateClient != null && (generateType==null || generateType.equals("axis2"))) {
            omElement = Util.getCodegenOptions(
                    "/org/wso2/carbon/wsdl2code/ui/client/generate-client-options.xml");
        }else if(generateClient != null && generateType.equals("cxf")){
            if(wadl !=null){
                omElement = Util.getCodegenOptions(
                        "/org/wso2/carbon/wsdl2code/ui/client/generate-client-cxf-jaxrs-options.xml");
            }else if(wsdl !=null){
                omElement = Util.getCodegenOptions(
                        "/org/wso2/carbon/wsdl2code/ui/client/generate-client-cxf-jaxws-options.xml");
            }
        } else {
            omElement =
                    Util.getCodegenOptions("/org/wso2/carbon/wsdl2code/ui/client/codegen-options.xml");
        }
        Iterator arguments = omElement.getChildrenWithLocalName("argument");
    %>


    <div id="workArea">
    <table width="100%">
        <thead>
        <tr>
            <th colspan="2"><fmt:message key="maven"/></th>
        </tr>
        </thead>
        <tbody>
            <tr>
                <td>
                    <table class="styledLeft" width="100%">
                        <thead>
                        <tr>
                            <th width="10%"><fmt:message key="option"/></th>
                            <th width="60%"><fmt:message key="description"/></th>
                            <th><fmt:message key="selectValue"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        <!--- Maven Configurations starts here -->

                        <tr>
                            <td>groupId</td>
                            <td>Group Id for maven configuration</td>
                            <td><input type="text" class="toolsClass" value="WSO2" name="gid" id="id_gid"></td>
                        </tr>

                        <% dynamicJS = dynamicJS + "var obj_id_gid = document.getElementById('id_gid');\n" +
                                "if (obj_id_gid.value != '') {\n" +
                                "    options['id_gid'] = obj_id_gid.value;\n" +
                                "}" ;
                        %>

                        <tr>
                            <td>artifactId</td>
                            <td>Artifact Id for maven configuration</td>
                            <td><input type="text" class="toolsClass" value="WSO2-Axis2-Client" name="aid" id="id_aid"></td>
                        </tr>

                        <% dynamicJS = dynamicJS + "var obj_id_aid = document.getElementById('id_aid');\n" +
                                "if (obj_id_aid.value != '') {\n" +
                                "    options['id_aid'] = obj_id_aid.value;\n" +
                                "}" ;
                        %>

                        <tr>
                            <td>version</td>
                            <td>Version for maven configuration</td>
                            <td><input type="text" class="toolsClass" value="0.0.1-SNAPSHOT" name="vn" id="id_vn"></td>
                        </tr>

                        <% dynamicJS = dynamicJS + "var obj_id_vn = document.getElementById('id_vn');\n" +
                                "if (obj_id_vn.value != '') {\n" +
                                "    options['id_vn'] = obj_id_vn.value;\n" +
                                "}" ;
                        %>


                        <!-- finished maven configurations-->
                        </tbody>
                    </table>
                </td>
            </tr>
        </tbody>
    </table>
        <table width="100%">
            <thead>
            <tr>
                <% if(generateClient!=null && generateType!=null && generateType.equals("cxf")){ %>
                <th colspan="2"><fmt:message key="options"/></th>
                <%}else{ %>
                <th colspan="2"><fmt:message key="wsdl2codeOptions"/></th>
                <%}%>
            </tr>
            </thead>
            <tr>
                <td>
                    <table class="styledLeft" width="100%">
                        <thead>
                        <tr>
                            <th width="10%"><fmt:message key="option"/></th>
                            <th width="60%"><fmt:message key="description"/></th>
                            <th><fmt:message key="selectValue"/></th>
                        </tr>
                        </thead>
                        <tbody>

                        <%
                            while (arguments.hasNext()) {
                                OMElement argument = (OMElement) arguments.next();
                                String uiType = argument.getAttributeValue(
                                        new QName(null, "uiType"));
                                String name = argument.getFirstChildWithName(
                                        new QName(null, "name")).getText();
                                String description = argument.getFirstChildWithName(
                                        new QName(null, "description")).getText();
                                String mandatory = argument.getAttributeValue(
                                        new QName(null, "mandatory"));
                                if (!"skip".equals(uiType)) {
                        %>
                        <tr>
                            <td>-<%=name%>
                                <% if (mandatory != null && "true".equals(mandatory)) {%>
                                <font color="red">*</font>
                                <%}%>
                            </td>
                            <td><%=description%>
                            </td>
                            <td>
                                <%
                                    name = "id_" + name;
                                    if ("text".equals(uiType)) {
                                        String uploadFile = argument.getAttributeValue(
                                                new QName(null, "uploadFile"));
                                        if (uploadFile != null && "true".equals(uploadFile)) {
                                            String executor;
                                            if ("id_uri".equals(name)) {
                                                executor = "wsdl";
                                            }
                                            else {
                                                executor = "tools";
                                            }

                                %>
                                <input class="toolsClass" type="text" size="37" id="<%=name%>"/>
                                <input type="button" width="20px" id="<%=name%>_button"
                                       value="..."
                                       onclick="codeGenFileUploadeHelper('<%=name%>', '<%=executor%>');"/>
                                <%
                                } else {
                                %>
                                <input class="toolsClass" type="text" size="40" id="<%=name%>"/>
                                <%
                                    }
                                    String setFocus = argument.getAttributeValue(
                                            new QName(null, "setFocus"));
                                    if (setFocus != null && "true".equals(setFocus)) {
                                %>
                                <script type="text/javascript">
                                    document.getElementById('<%=name%>').focus();
                                </script>

                                <%
                                    }
                                    dynamicJS = dynamicJS + "var obj_" + name + "= document.getElementById('" +
                                            name + "');\n" +
                                            "if (obj_" + name + ".value != '') {\n" +
                                            "    options['" + name + "'] = obj_" + name + ".value;\n" +
                                            "}\n";
                                } else if ("text-area".equals(uiType)) {

                                %>
                                <textarea class="toolsClass" style="height:100px;width:345px"
                                          id="<%=name%>"></textarea>
                                <%
                                    dynamicJS = dynamicJS + "var obj_" + name + " = document.getElementById('" +
                                            name + "');\n" +
                                            "if (obj_" + name + ".value != '') {\n" +
                                            "    options['" + name + "'] = obj_" + name + ".value;\n" +
                                            "}\n";
                                } else if ("check".equals(uiType)) {

                                %>
                                <input class="toolsClass" type="checkbox" id="<%=name%>"/>
                                <%
                                    dynamicJS = dynamicJS + "var obj_" + name + " = document.getElementById('" +
                                            name + "');\n" +
                                            "if (obj_" + name + ".checked) {\n" +
                                            "    options['" + name + "'] = \"\";\n" +
                                            "}\n";
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
                                        dynamicJS = dynamicJS + "var obj_" + name + " = document.getElementById('" +
                                                name + "');\n" +
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
                        
                        <% if (generateType == null && endPointsStr != null) { %>
                            <tr>
                            <td>-pn</td>
                            <td>Port name. Choose a specific port when there are multiple ports in the wsdl </td>
                            <td>
                                
                                <% EndPointsSetter parcer = new EndPointsSetter();%>
                                <%= parcer.getEndPoints(endPointsStr) %>

                                <% dynamicJS = dynamicJS + "var obj_id_pn = document.getElementById('id_pn'); " +
                                        "options['id_pn'] = obj_id_pn[obj_id_pn.selectedIndex].value;" ; %>

                            </td>            
                            </tr>
                            <%  } %>


                        </tbody>
                    </table>
                    <div class="buttonrow" style="padding-top:10px">
                        <script type="text/javascript">
                            function populateOptions() {
                                <%=dynamicJS%>
                            }
                        </script>
                        <input type="button" class="button"
                               value='<fmt:message key="generate"/> &gt;'
                               onclick="startCodegen(options);"
                               id="generate_button"/>
                    </div>
                </td>
            </tr>
        </table>
    </div>
</div>
</fmt:bundle>
