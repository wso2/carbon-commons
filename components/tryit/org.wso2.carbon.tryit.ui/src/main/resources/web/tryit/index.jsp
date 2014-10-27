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
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<script type="text/javascript" src="global-params.js"></script>
<%
    String backendServerURL = CarbonUIUtil.getAdminConsoleURL(request).split("/carbon/")[0]+"/services/";
%>
<fmt:bundle basename="org.wso2.carbon.tryit.ui.i18n.Resources">
<script>

    wso2.wsf.Util.initURLs();
    
    var frontendURL = wso2.wsf.Util.getServerURL() + "/";

    function validateAndSubmitTryit() {
        var inputObj = document.getElementById('tryitFileName');
        if (inputObj.value == "") {
            CARBON.showWarningDialog('<fmt:message key="tryit.error.msg"/>');
            return;
        }

        var resourcePath = getAppContext();

        var proxyAddress = getProxyAddress();

        var bodyXml = '<req:generateTryit xmlns:req="http://org.wso2.wsf/tools">\n' +
                      '<url><![CDATA[' + inputObj.value + ']]></url>\n' +
                      '<hostName><![CDATA[' + HOST + ']]></hostName>\n' +
                      '</req:generateTryit>\n';
        var callURL = wso2.wsf.Util.getBackendServerURL(frontendURL, "<%=backendServerURL%>") + "ExternalTryitService" ;
        wso2.wsf.Util.cursorWait();
        new wso2.wsf.WSRequest(callURL, "urn:generateTryit", bodyXml, wcserviceClientCallback, [2], undefined, proxyAddress);

    }

    function wcserviceClientCallback() {
        var data = this.req.responseXML;
        var returnElementList = data.getElementsByTagName("ns:return");
        // Older browsers might not recognize namespaces (e.g. FF2)
        if (returnElementList.length == 0)
            returnElementList = data.getElementsByTagName("return");
        var responseTextValue = returnElementList[0].firstChild.nodeValue;
        window.open(responseTextValue);
    }

    function getAppContext() {
        var urlSegments = document.location.href.split("/");
        return urlSegments[3];
    }
</script>
<carbon:breadcrumb
		label="tryit.headertext"
		resourceBundle="org.wso2.carbon.tryit.ui.i18n.Resources"
		topPage="true" 
		request="<%=request%>" />
		
    <div id="middle">
        <h2><fmt:message key="tryit.headertext"/></h2>

        <div id="workArea">
            <form id="tryitFormId"
                  method="post"
                  target="globalIFrame">

                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th><fmt:message key="tryit.instructions"/></th>
                    </tr>
                    </thead>
                    <tr>
                        <td class="formRow">
                            <table class="normal">
                                <tr>
                                    <td>
                                        <label><fmt:message key="tryit.enterurl"/></label>
                                    </td>
                                    <td>
                                        <input type="text" id="tryitFileName"
                                               name="tryitFileName"
                                               size="50"/>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input type="button" id="tryitButtonId" class="button"
                                   value="<fmt:message key="tryit.tryitbtn"/>" onclick="validateAndSubmitTryit()"/>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
        <div style="padding-top:20px">
            <fmt:message key="limitation.message"/>
        </div>
    </div>
    <script type="text/javascript">
        wso2.wsf.XSLTHelper.init();
    </script>
</fmt:bundle>    
