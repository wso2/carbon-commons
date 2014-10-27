<?xml version="1.0" encoding="UTF-8"?>
<!--
  Copyright 2008 WSO2, Inc. http://www.wso2.org

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

  Created by: Jonathan Marsh <jonathan@wso2.com>

-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:output method="html" indent="yes"/>

    <!-- This stylesheet only supports a single service at a time.
         If no service name is specified in this parameter, the first one is used.  -->
    <xsl:param name="service" select="services/service[1]/@name"/>

    <xsl:param name="operation"/>

    <!-- Paths to external resources can be specified here. -->
    <!--<xsl:param name="wsrequest-location" select="'/WSRequest.js'"/>-->
    <!--<xsl:param name="jquery-location" select="'../admin/js/jquery.js'"/>-->
    <!--<xsl:param name="global-params-location" select="'../carbon/global-params.js'"/>-->
    <xsl:param name="proxyAddress" select="'../admin/jsp/WSRequestXSSproxy_ajaxprocessor.jsp'"/>
    <xsl:param name="image-path" select="'images/tryit/'"/>
    <xsl:param name="enable-header" select="'false'"/>
    <xsl:param name="enable-footer" select="'false'"/>
    <xsl:param name="full-page" select="false()" />

    <!--<xsl:param name="mockit-service-stub-location" />-->
    <xsl:param name="task-id" />

    <!-- Toggle between DOM and E4X treatment of XML objects. -->
    <xsl:param name="e4x" select="false()"/>

    <!-- Allows some html to be inserted immediately before the body. -->
    <xsl:param name="breadcrumbs" />

    <!-- For non-WSO2 services, the link to alternate endpoints might not be valid.
         Set this parameter to 'false' to disable that link. -->
    <xsl:param name="show-alternate" select="'true'"/>

    <xsl:variable name="operations" select="services/service[@name=$service][1]/operations/operation[boolean($operation) and @name = $operation] |
                                            services/service[@name=$service][1]/operations/operation[not($operation)]"/>

    <xsl:variable name="service-name">
        <xsl:call-template name="service-name-to-javascript-name">
            <xsl:with-param name="name" select="$service"/>
        </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="original-service-name">
        <xsl:value-of select="$service"/>
    </xsl:variable>

    <xsl:template match="/">
        <xsl:apply-templates select="services/service[@name=$service][1]"/>
    </xsl:template>

    <xsl:template match="service">
        <xsl:choose>
            <xsl:when test="$full-page">
                <html>
                    <head>
                        <title>Try the <xsl:value-of select="$original-service-name"/> service</title>
                        <xsl:call-template name="header-template" />
                    </head>
                    <body>
                        <xsl:call-template name="body-template" />
                    </body>
                 </html>
            </xsl:when>
            <xsl:otherwise>
                <div id="wsdl2form-container">
                    <div id="wsdl2form-js">
                        <xsl:call-template name="header-template" />
                    </div>
                    <div id="wsdl2form-content">
                          <xsl:call-template name="body-template" />
                    </div>
                </div>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="parameter-view">
         <xsl:for-each select="operations/operation">
            <xsl:variable name="name">
                <xsl:call-template name="xml-name-to-javascript-name">
                    <xsl:with-param name="name" select="@name"/>
                </xsl:call-template>
            </xsl:variable>
            <div class="params" id="params_{$name}">
                <table class="ops">
                    <xsl:for-each select="signature/params/param">
                        <tr>
                            <xsl:choose>
                                <!-- this parameter represents expandable parameters -->
                                <xsl:when test="@token = '#any'">
                                    <td class="label"><div>(additional parameters)</div></td>
                                    <td class="param">
                                        <input type="text" id="input_{$name}_additionalParameters" class="emptyfield" value="xs:anyType" onfocus="prepareInput(event)" onblur="restoreInput(event,'(xs:anyType)')" />
                                        <!-- TODO expandable fields of additional parameters -->
                                    </td>
                                </xsl:when>

                                <!-- this parameter represents a boolean (checkbox) -->
                                <xsl:when test="@type = 'boolean'">
                                    <td class="label">
                                        <xsl:value-of select="@name"/>
                                        <xsl:if test="@minOccurs &lt; 1 or @maxOccurs &gt; 1 or @maxOccurs = 'unbounded'"><sub>(<xsl:value-of select="@minOccurs"/>..<xsl:choose><xsl:when test="@maxOccurs = 'unbounded'">*</xsl:when><xsl:otherwise><xsl:value-of select="@maxOccurs"/></xsl:otherwise></xsl:choose>)</sub></xsl:if></td>
                                    <td class="param">
                                        <div id="arrayparams_{$name}_{@name}">
                                            <!-- first child is a hidden template for cloning additional array items -->
                                            <div style="display:none">
                                                <input type="checkbox" id="input_{$name}_{@name}_" title="An [xs:boolean] value representing {@name}"/><span class="typeannotation"> (xs:boolean)</span>
                                            </div>
                                            <div>
                                                <input type="checkbox" id="input_{$name}_{@name}_0" title="An [xs:boolean] value representing {@name}"/><span class="typeannotation"> (xs:boolean)</span>
                                            </div>
                                        </div>
                                        <xsl:if test="@maxOccurs &gt; 1 or @maxOccurs = 'unbounded'">
                                            <input type="button" value="Add {@name}" onclick="addArrayItem(event)"></input>
                                            <input type="button" value="Remove {@name}" onclick="removeArrayItem(event)" disabled="disabled"></input>
                                        </xsl:if>
                                    </td>
                                </xsl:when>

                                <!-- this parameter represents a QName (separate namespace and QName fields) -->
                                <xsl:when test="@type = 'QName'">
                                    <td class="label">
                                        <xsl:value-of select="@name"/>
                                        <xsl:if test="@minOccurs &lt; 1 or @maxOccurs &gt; 1 or @maxOccurs = 'unbounded'"><sub>(<xsl:value-of select="@minOccurs"/>..<xsl:choose><xsl:when test="@maxOccurs = 'unbounded'">*</xsl:when><xsl:otherwise><xsl:value-of select="@maxOccurs"/></xsl:otherwise></xsl:choose>)</sub></xsl:if></td>
                                    <td class="param">
                                        <div id="arrayparams_{$name}_{@name}">
                                            <!-- first child is a hidden template for cloning additional array items -->
                                            <div style="display:none">
                                                <textarea id="input_{$name}_{@name}_ns_" class="emptyfield" onfocus="prepareInput(event)" onblur="restoreInput(event,'(namespace URI)')" title="The [namespace URI] value corresponding to the xs:QName">(namespace URI)</textarea>
                                                <xsl:text> </xsl:text>
                                                <textarea id="input_{$name}_{@name}_" class="emptyfield" onfocus="prepareInput(event)" onblur="restoreInput(event,'(xs:QName)')" title="An [xs:QName] value representing prefix and localName of {@name}">(xs:QName)</textarea>
                                            </div>
                                            <div>
                                                <textarea id="input_{$name}_{@name}_ns_0" class="emptyfield" onfocus="prepareInput(event)" onblur="restoreInput(event,'(namespace URI)')" title="The [namespace URI] value corresponding to the xs:QName">(namespace URI)</textarea>
                                                <xsl:text> </xsl:text>
                                                <textarea id="input_{$name}_{@name}_0" class="emptyfield" onfocus="prepareInput(event)" onblur="restoreInput(event,'(xs:QName)')" title="An [xs:QName] value representing prefix and localName of {@name}">(xs:QName)</textarea>
                                            </div>
                                        </div>
                                        <xsl:if test="@maxOccurs &gt; 1 or @maxOccurs = 'unbounded'">
                                            <input type="button" value="Add {@name}" onclick="addArrayItem(event)"></input>
                                            <input type="button" value="Remove {@name}" onclick="removeArrayItem(event)" disabled="disabled"></input>
                                        </xsl:if>
                                    </td>
                                </xsl:when>

                                <!-- this parameter represents an enumeration (<select>) -->
                                <xsl:when test="enumeration">
                                    <td class="label">
                                        <xsl:value-of select="@name"/>
                                    </td>
                                    <td class="param">
                                        <select id="input_{$name}_{@name}_0">
                                            <xsl:for-each select="enumeration">
                                                <option value="{@value}"><xsl:value-of select="@value"/></option>
                                            </xsl:for-each>
                                        </select>
                                    </td>
                                </xsl:when>

                                <!-- this parameter represents a type exposed as a <textarea> -->
                                <xsl:otherwise>
                                    <xsl:variable name="prefix">
                                        <xsl:if test="@type-namespace = 'http://www.w3.org/2001/XMLSchema'">xs:</xsl:if>
                                    </xsl:variable>
                                    <xsl:variable name="restriction">
                                        <xsl:if test="@restriction-of">
                                            <xsl:if test="@restriction-namespace = 'http://www.w3.org/2001/XMLSchema'">xs:</xsl:if>
                                            <xsl:value-of select="@restriction-of"/>
                                            <xsl:text> restriction</xsl:text>
                                        </xsl:if>
                                    </xsl:variable>
                                    <td class="label"><div>
                                        <xsl:value-of select="@name"/>
                                        <xsl:if test="@minOccurs &lt; 1 or @maxOccurs &gt; 1 or @maxOccurs = 'unbounded'"><sub>(<xsl:value-of select="@minOccurs"/>..<xsl:choose><xsl:when test="@maxOccurs = 'unbounded'">*</xsl:when><xsl:otherwise><xsl:value-of select="@maxOccurs"/></xsl:otherwise></xsl:choose>)</sub></xsl:if></div></td>
                                    <td class="param">
                                        <div id="arrayparams_{$name}_{@name}">
                                            <!-- first child is a hidden template for cloning additional array items -->
                                            <div style="display:none">
                                                <textarea id="input_{$name}_{@name}_" class="emptyfield" onfocus="prepareInput(event)" onblur="restoreInput(event,'({$prefix}{@type}{$restriction})')" title="A [{$prefix}{@type}{$restriction}] value representing the {@name}">(<xsl:value-of select="$prefix"/><xsl:value-of select="@type"/><xsl:value-of select="$restriction"/>)</textarea>
                                                <img src="{$image-path}expand.gif" class="cornerExpand" onclick="expand(event)" title="Increase typing space"/>
                                            </div>
                                            <div>
                                                <textarea id="input_{$name}_{@name}_0" class="emptyfield" onfocus="prepareInput(event)" onblur="restoreInput(event,'({$prefix}{@type}{$restriction})')" title="A [{$prefix}{@type}{$restriction}] value representing the {@name}">(<xsl:value-of select="$prefix"/><xsl:value-of select="@type"/><xsl:value-of select="$restriction"/>)</textarea>
                                                <img src="{$image-path}expand.gif" class="cornerExpand" onclick="expand(event)" title="Increase typing space"/>
                                            </div>
                                        </div>
                                        <xsl:if test="@maxOccurs &gt; 1 or @maxOccurs = 'unbounded'">
                                            <input type="button" value="Add {@name}" onclick="addArrayItem(event)"></input>
                                            <input type="button" value="Remove {@name}" onclick="removeArrayItem(event)" disabled="disabled"></input>
                                        </xsl:if>
                                    </td>
                                </xsl:otherwise>
                            </xsl:choose>
                        </tr>
                    </xsl:for-each>
                    <tr>
                         <td style="padding-top: 1em">
                             <input type="button" id="button_{$name}" value="{$name} >>" onclick="do_{$name}()" title="Invoke the {$name} operation"></input>
                         </td>
                         <td style="padding-top: 1em">
                             <div id="console_{$name}" class="output">

                             </div>
                         </td>
                     </tr>
                 </table>
            </div>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="return-type">
        <xsl:text>/* </xsl:text>
        <xsl:if test="@maxOccurs = 'unbounded' or @maxOccurs > 1">array of </xsl:if>
        <xsl:call-template name="xml-name-to-javascript-name">
            <xsl:with-param name="name" select="@type"/>
        </xsl:call-template>
        <xsl:text>*/</xsl:text>
    </xsl:template>

    <!-- do some simple name mapping, replacing '.' and '-' with '_' -->
    <xsl:template name="service-name-to-javascript-name">
        <xsl:param name="name"/>
        <xsl:if test="contains(',break,else,new,var,case,finally,return,void,catch,for,switch,while,continue,function,this,with,default,if,throw,delete,in,try,do,instanceof,typeof,abstract,enum,int,short,boolean,export,interface,static,byte,extends,long,super,char,final,native,synchronized,class,float,package,throws,const,goto,private,transient,debugger,implements,protected,volatile,double,import,public,null,true,false,',concat(',',$name,','))">_</xsl:if>
        <xsl:value-of select="translate($name,'.-/','___')"/>
    </xsl:template>

    <xsl:template name="operation-name-to-javascript-name">
        <xsl:param name="name"/>
        <xsl:if test="contains(',endpoint,getAddress,onreadystatechange,password,readyState,setAddress,username,',concat(',',$name,','))">_</xsl:if>
        <xsl:value-of select="translate($name,'.-/','___')"/>
    </xsl:template>

    <xsl:template name="xml-name-to-javascript-name">
        <xsl:param name="name"/>
        <xsl:value-of select="translate($name,'.-/','___')"/>
    </xsl:template>

    <xsl:template name="browser-compatibility">
        var browser = WSRequest.util._getBrowser();

        // Workaround for IE, which treats ids as case insensitive, compliments of Mike Bulman.
        if (browser == "ie" || browser == "ie7") {
            document._getElementById = document.getElementById;
            document.getElementById = function(id) {
                var a = [];
                var o = document._getElementById(id);
                if (!o) return o;
                while (o &amp;&amp; o.id != id) {
                    a.push({i:o.id,e:o});
                    o.id='';
                    o = document._getElementById(id);
                }
            for (j=0,jj=a.length; j&lt;jj; j++) a[j].e.id = a[j].i;
                a = null;
                return o;
            }
        }
    </xsl:template>

    <xsl:template name="header-template">
        <xsl:call-template name="css"/>
            <!--<script type="text/javascript" src="{$wsrequest-location}"></script>
            <script type="text/javascript" src="{$global-params-location}"></script>
            script type="text/javascript" src="{$jquery-location}"></script-->
            <xsl:text>
</xsl:text>
            <!-- Calculate the source of the stub, including whether it's e4x or not -->
            <xsl:variable name="e4x-param">
                <xsl:if test="$e4x">; e4x=1</xsl:if>
            </xsl:variable>

            <!--<xsl:variable name="src-mockit-service">
                <xsl:value-of select="$mockit-service-stub-location"/><xsl:if test="$e4x">&amp;lang=e4x</xsl:if>
            </xsl:variable>-->
            <!--<script type="text/javascript{$e4x-param}" src="{$src-mockit-service}"></script>-->
            <xsl:text>
</xsl:text>
            <xsl:text>
</xsl:text>
            <script type="text/javascript{$e4x-param}">
<xsl:call-template name="browser-compatibility"/>

        /*
         *  init: called during onload.  Dynamically resets or restores the page as needed.
         */
        function init() {

            // If the URL has a fragment id, see if it matches an operation name; if so, open
            //    the tab of that operation.
            var requestedOperation = "";
            if (document.URL.indexOf('#') >= 0)
                requestedOperation = document.URL.substring(document.URL.lastIndexOf('#') + 1);
            if (document.getElementById("operation_" + requestedOperation) == null) {
                requestedOperation = '<xsl:value-of select="operations/operation[1]/@name"/>';
            }
            selectOperation(requestedOperation);

            // Restore the state of each input field.  An empty input field displays the type of the
            //    input expected (lightly greyed out).  The state of whether a field is really empty
            //    or just containing this type hint must be carried somewhere else, so we use the
            //    classes "emptyfield" and "nonemptyfield" to distinguish this case.  The type
            //    is retained in the title of the field, and can be extracted there if a value is
            //    deleted.  A page refresh may cause some of the values to be retained by the
            //    browser, which will cause the displayed value and style to be unsynchronized
            //    with the class attribute.  This code resynchronizes the classes and values.
            var textareas = document.getElementsByTagName("textarea");
            for (var i in textareas) {
                if (textareas[i].className == "emptyfield") {
                    // extract the type from the title.
                    var type = textareas[i].title;
                    type = type.substring(type.indexOf("[")+1, type.indexOf("]"));
                    type = "(" + type + ")";
                    if (textareas[i].value == '')
                        textareas[i].value = type;
                    else if (textareas[i].value != type)
                        textareas[i].className = 'nonemptyfield';
                }
            }
        }

        <xsl:call-template name="do-operation-functions"/>
        <xsl:call-template name="logging-functions"/>
        <xsl:call-template name="form-behavior-functions"/>

        window.onload = init;

        </script>
    </xsl:template>

    <xsl:template name="body-template">
        <!-- insert breadcrumbs -->
            <xsl:value-of select="$breadcrumbs" disable-output-escaping="yes"/>
            <!-- header -->
            <xsl:if test="$enable-header='true'">
             <div id="header">
            	 <nobr>
                	<h1>Try the <xsl:value-of select="$original-service-name"/> service.</h1>
                </nobr>
             </div>
            </xsl:if>
            <!-- end of header -->
            <div id="body">
                <div id="middle">
                    <input type="hidden" id="mockit_taskID" value="{$task-id}"/>
                    <table id="middle-content">
                        <tr>
                            <td>
                                 <table id="content-table" style="width: 100%;">
                                    <tr>
                                        <td class="content">
                                            <xsl:call-template name="parameter-view"/>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </div>
            </div>
            <!-- footer -->
            <xsl:if test="$enable-footer='true'">
              <div id="footer">
                <p>© 2007-2008 <a href="http://wso2.com/">WSO2 Inc.</a></p>
              </div>
            </xsl:if>
            <!-- end of footer -->
    </xsl:template>

    <xsl:template name="do-operation-functions">
        <!-- Create a function (responding to a button click) that collects the data
             in the form, formats it as parameters, and calls the stub asynchronously
             for each operation. -->
        <xsl:for-each select="operations/operation">
            <xsl:variable name="name"><!-- operation name -->
                <xsl:call-template name="xml-name-to-javascript-name">
                    <xsl:with-param name="name" select="@name"/>
                </xsl:call-template>
            </xsl:variable>
        /*
         *  do_<xsl:value-of select="$name"/> : collect form data into parameters and call the '<xsl:value-of select="$name"/>' operation asynchronously.
         */
        function do_<xsl:value-of select="$name"/>(preview)
        {
            // First clear the log.
            if (!preview) clearlog("console_<xsl:value-of select="$name"/>");

            <xsl:for-each select="signature/params/param">
                <xsl:variable name="adjusted-name">
                    <xsl:choose>
                        <xsl:when test="@token = '#any'">input_<xsl:value-of select="$name"/>_additionalParameters</xsl:when>
                        <xsl:otherwise>input_<xsl:value-of select="$name"/>_<xsl:value-of select="@name"/>_0</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
            // turn the '<xsl:value-of select="$adjusted-name"/>' form item into a parameter
            var paramInput = document.getElementById('<xsl:value-of select="$adjusted-name"/>');
            <xsl:choose>
                <!-- for enumerations, populate the parameter value from the select/option. -->
                <xsl:when test="enumeration">var param_<xsl:value-of select="@name"/> = paramInput.value</xsl:when>
                <!-- for arrays, collect up a number of inputs into an array. -->
                <xsl:when test="@maxOccurs &gt; 1 or @maxOccurs = 'unbounded'">param_<xsl:value-of select="@name"/> = new Array();
            var arrayDiv = document.getElementById('arrayparams_<xsl:value-of select="$name"/>_<xsl:value-of select="@name"/>');
            var arrayLength = arrayDiv.getElementsByTagName("div").length;
            var arrayItem;

            // iterate through each div representing an item in the array.
            for (var i=1; i&lt;arrayLength; i++) {
                arrayItems = arrayDiv.getElementsByTagName("div").item(i).getElementsByTagName("*");
                <!-- Handle an array of booleans (checkboxes) differently than other values (textarea). -->
                <xsl:choose>
                    <!-- for boolean parameters, extract the checkbox value -->
                    <xsl:when test="@type = 'boolean'">var param_<xsl:value-of select="@name"/> = param_<xsl:value-of select="@name"/>.concat(arrayItems.item(0).checked ? true : false);</xsl:when>
                    <!-- for QName parameters, build a namespace object -->
                    <xsl:when test="@type = 'QName'">if (arrayItems.item(1).className != 'emptyfield') {
                        var param_<xsl:value-of select="@name"/> = param_<xsl:value-of select="@name"/>.concat({ "uri" : (arrayItems.item(0).value.indexOf('(') >= 0 ? null : arrayItems.item(0).value),  "localName" : arrayItems.item(1).value });
                    }</xsl:when>
                    <!-- otherwise it's a normal textarea  -->
                    <xsl:otherwise>if (arrayItems.item(0).className != 'emptyfield')
                    var param_<xsl:value-of select="@name"/> = param_<xsl:value-of select="@name"/>.concat(arrayItems.item(0).value);</xsl:otherwise>
                </xsl:choose>
            }</xsl:when>
                <!-- for QName parameters, build a namespace object -->
                <xsl:when test="@type = 'QName'">if (paramInput.className!='emptyfield') {
                var param_<xsl:value-of select="@name"/>_ns = document.getElementById('input_<xsl:value-of select="$name"/>_<xsl:value-of select="@name"/>_ns_0').value;
                var param_<xsl:value-of select="@name"/> = { "uri" : (param_<xsl:value-of select="@name"/>_ns.indexOf('(') >= 0 ? null : param_<xsl:value-of select="@name"/>_ns),  "localName" : paramInput.value };
            } else {
                var param_<xsl:value-of select="@name"/> = null;
            }</xsl:when>

                <!-- for optional parameters, check for a value before adding a parameter. -->
                <xsl:when test="@minOccurs &lt; 1">
                    <xsl:choose>
                        <!-- for boolean parameters, extract the checkbox value -->
                        <xsl:when test="@type = 'boolean'">var param_<xsl:value-of select="@name"/> = (paramInput.checked ? "true" : "false");</xsl:when>
                        <!-- otherwise it's a normal textarea  -->
                        <xsl:otherwise>var param_<xsl:value-of select="@name"/> = paramInput.className=='emptyfield' ? null : paramInput.value;</xsl:otherwise>
                    </xsl:choose>
                </xsl:when>

                <!-- for boolean parameters, extract the checkbox value -->
                <xsl:when test="@type = 'boolean'">var param_<xsl:value-of select="@name"/> = (paramInput.checked ? "true" : "false");</xsl:when>

                <!-- otherwise it's a normal textarea  -->
                <xsl:otherwise>param_<xsl:value-of select="@name"/> = paramInput.className=='emptyfield' ? "" : paramInput.value;</xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>

            var paramInput = document.getElementById('mockit_taskID');
            var taskID = paramInput.value;

            var payload = <xsl:value-of select="$service-name"/>.<xsl:call-template name="operation-name-to-javascript-name">
                <xsl:with-param name="name" select="$name"/>
            </xsl:call-template>_payload(<xsl:for-each select="signature/params/param">param_<xsl:value-of select="@name"/>
                <xsl:if test="position() != last()">, </xsl:if>
            </xsl:for-each>);

            $.post('<xsl:value-of select="$proxyAddress"/>', { taskID : taskID, payload : payload },
               function(response){
                 log ("console_<xsl:value-of select="$name"/>", response);
               }, 'html');
        }
    </xsl:for-each>
    </xsl:template>

    <xsl:template name="logging-functions">
        /*
         *  log : Serialize the result of an operation (successful or not) into a specified log div.
         *
         *    consoleid: the id of an element into which the data should be placed.
         *    data: the data (can be of any type) to display.
         *    detail: fault detail information if available separately from the data.
         */
        function log(consoleid, data, detail) {
            var console = document.getElementById(consoleid);
            // Type could be dynamically determined - run a smart serializer on it.
            console.innerHTML = data;

            // If there's a separate fault detail, add it into the log (hidden, with a link to make
            //    it visible on request.)
            if (detail != null)
                    console.innerHTML += "\n&lt;a class='showDetail' href='#' onclick='this.style.display=\"none\";this.parentNode.lastChild.style.display=\"block\"'>[detail]&lt;/a>\n&lt;div class='faultDetail'&gt;" + detail + "&lt;/div&gt;";
        }
        /*
         *  clearlog : indicate that an asynchronous operation is in progress.
         *
         *    consoleid: the id of an element into which the data should be placed.
         */
        function clearlog(consoleid) {
            var console = document.getElementById(consoleid);
            console.innerHTML = "&lt;img src='<xsl:value-of select="$image-path"/>wso2pulse.gif' alt='invoking operation' title='Invoking service...' style='position:relative; top:-8px;'/&gt;";
        }
    </xsl:template>

    <xsl:template name="form-behavior-functions">
        /*
         * prepareInput : A user is about to type into an empty parameter field.  Clear out
         *                the type hint.
         *
         *     e: event triggering this call.
         */
        function prepareInput(e) {
            var thisInput = sourceElement(e);
            if (thisInput.className == "emptyfield") {
                thisInput.value = "";
                thisInput.className = "nonemptyfield";
            }
        }

        /*
         * restoreInput : A user has finished typing into an empty parameter field.  If he
         *                left it empty, restore the type hint.
         *
         *     e: event triggering this call.
         *     hint: value of the type hint text
         */
        function restoreInput(e, hint) {
            var thisInput = sourceElement(e);
            if (thisInput.value == "") {
                thisInput.value = hint;
                thisInput.className = "emptyfield";
            }
        }

        /*
         * expand : Expand or contract the size of a parameter textarea.
         *
         *     e: event triggering this call.
         */
        function expand(e) {
            thisExpando = sourceElement(e);
            thisInput = thisExpando.parentNode.getElementsByTagName("*").item(0);
            if (thisExpando.className == "cornerExpand") {
                // increase the height and width of the textarea, and change the icon to "collapse".
                thisInput.style.width = "80%";
                thisInput.style.height = "6em";
                thisExpando.className = "cornerCollapse";
                thisExpando.title = "Reduce typing space";
                thisExpando.src = "<xsl:value-of select="$image-path"/>collapse.gif";
            } else {
                // decrease the height and width of the textarea, and change the icon to "expand".
                thisInput.style.width = "";
                thisInput.style.height = "1.7em";
                thisExpando.className = "cornerExpand";
                thisExpando.title = "Increase typing space";
                thisExpando.src = "<xsl:value-of select="$image-path"/>expand.gif";
            }
        }

        /*
         * toggleconfig : Generic function toggling a display between two elements.
         *
         *     toHide: id of the element to hide
         *     toShow: id of the element to show
         */
        function toggleconfig(toHide, toShow) {
            document.getElementById(toHide).style.display = "none";
            document.getElementById(toShow).style.display = "block";

            // bug fix for relative items that might not update correctly.
            if (browser == "ie" || browser == "ie7") {
                selectOperation();
            }
        }

        /*
         * addArrayItem : User wants an additional input field to accomodate array values
         *
         *     e: event triggering this call.
         */
        function addArrayItem(e) {
            // find the div grouping the array items together
            var arrayDiv = sourceElement(e).parentNode.getElementsByTagName("div").item(0);
            // find the element representing the last item
            var lastIndex = arrayDiv.getElementsByTagName("div").length - 1;
            // the first item [0] in an array is a hidden template for new items, clone it,
            // add it to the end, make it visible, and give it a unique identifier.
            var newItem = arrayDiv.getElementsByTagName("div").item(0).cloneNode(true);
            newItem.style.display = "block";
            newItem.getElementsByTagName("*").item(0).id += lastIndex;
            arrayDiv.appendChild(newItem);
            // since we just added one, there must be more than one now, so enable the "remove" button.
            sourceElement(e).parentNode.lastChild.disabled = false;
        }

        /*
         * removeArrayItem : User wants to delete an input field from his array values
         *
         *     e: event triggering this call.
         */
        function removeArrayItem(e) {
            // find the div grouping the array items together
            var arrayDiv = sourceElement(e).parentNode.getElementsByTagName("div").item(0);
            // find the element representing the last item, and delete it.
            var lastIndex = arrayDiv.getElementsByTagName("div").length - 1;
            arrayDiv.removeChild(arrayDiv.getElementsByTagName("div").item(lastIndex));
            // if we're down to a single textarea, disable the "remove" button.
            if (lastIndex &lt;= 2) {
                sourceElement(e).disabled = true;
            }
        }

        /*
         * sourceElement: Cross-browser function for determining the source element of
         *                an event.
         *
         *     e: event triggering this call.
         */
        function sourceElement(e) {
            if (browser == "ie" || browser == "ie7") {
                return window.event.srcElement;
            } else {
                var node = e.target;
                while(node.nodeType != 1)
                    node = node.parentNode;
                return node;
            }
        }

        /*
         * selectOperation : Present one of the operations to the user as a form.
         *
         *    op: name of the operation to present.  If omitted, refresh the last selected operation.
         */
        var currentOperationName = "";
        var currentOperation = "";
        var currentOperationLabel = "";
        function selectOperation(op) {
            // hide the currently presented operation, skipping it if this is the first time.
            if (currentOperation != "") {
                currentOperation.style.display = "none";
                currentOperationLabel.className = "operation";
            }

            // save the current operation name for automatic cleanup
            if (op == null)
                op = currentOperationName;
            else currentOperationName = op;

            // show the requested operation
            currentOperation = document.getElementById("params_" + op);
            currentOperation.style.display = "block";

            // change the style of the operation tab
            currentOperationLabel = document.getElementById("operation_" + op);
            currentOperationLabel.className = "operation-selected";
        }

    </xsl:template>

    <!-- template for inserting CSS -->
    <xsl:template name="css">
        <!-- css is embedded rather than linked so that the $image-path can be altered dynamically -->
        <style type="text/css">
            /* header styles */
            div#header {
                height: 70px;
                background-image: url(<xsl:value-of select="$image-path"/>gradient-rule-wide.gif);
                background-repeat: no-repeat;
                background-color: #7e8e35;
                background-position: bottom left;
                color:white;
            }
            div#header h1 {
                margin: 0px 0px 0px 0px;
                padding: 20px 0px 0px 40px;
                font-size: 18pt;
                font-weight: normal;
            }
            /* body styles */
            div#body {
                background-image: url(<xsl:value-of select="$image-path"/>header-bg.gif);
                background-position: top left;
                background-repeat: no-repeat;
            }
            div.documentation {
                padding-left: 40px;
                padding-top: 10px;
                padding-bottom: 20px;
                width: 90%;
            }

            /* soapActionDiv styles */
            div#soapActionDiv {
                margin: 0em 40px 0em 40px;
                padding-top:1em;
            }
            div#soapActionDiv div {
                margin-left: 3em;
            }

            /* middle styles */
            div#middle {
                margin-left: 35px;
                margin-top: 15px;
                margin-right: 50px;
                margin-right: 20px;
                margin-bottom: 0px;
                width : auto;
            }
            /* tabs styles */
            div#middle table#middle-content {
                padding: 0px;
                margin: 0px;
                border-collapse: collapse;
                width: 93%;
            }
            div#middle table#middle-content tr td {
                padding: 0px;
                vertical-align:top;
            }
            div#middle table#middle-content tr td.left-tabs {
                background-image: url(<xsl:value-of select="$image-path"/>left-tabs-bg.gif);
                background-position: top right;
                background-repeat: repeat-y;
                background-attachment: scroll;
                width: 5%;
                vertical-align: top;
            }
            div#middle table#middle-content tr td.bottom-left {
                background-image: url(<xsl:value-of select="$image-path"/>bottom-left.gif);
                background-position: top right;
                background-repeat: no-repeat;
                background-attachment: scroll;
                height: 16px;
                vertical-align: top;
            }
            div#middle table#middle-content tr td.bottom {
                background-image: url(<xsl:value-of select="$image-path"/>bottom.gif);
                background-position: top left;
                background-repeat: repeat-x;
                background-attachment: scroll;
                height: 16px;
                text-align: right;
                vertical-align: top;
            }

            div#middle table#operations {
                padding: 0px;
                margin: 0px;
                border-collapse: collapse;
            }
            div#middle table#operations tr td {
                vertical-align: top;
            }
            div#middle table#operations tr.operation-top td.operation-left {
                background-position: left top;
                background-repeat: no-repeat;
                background-attachment: scroll;
                height: 1px;
            }
            div#middle table#operations tr.operation-top td.operation-right {
                width: 26px;
                background-image: url(<xsl:value-of select="$image-path"/>operation-top-right.gif);
                background-position: left top;
                background-repeat: repeat-x;
                background-attachment: scroll;
                height: 1px;
            }

            div#middle table#operations tr.operation-selected td.operation-left {
                background-image: url(<xsl:value-of select="$image-path"/>operation-selected-bg.gif);
                background-position: left bottom;
                background-repeat: no-repeat;
                background-attachment: scroll;
                padding-bottom: 10px;
                padding-left: 15px;
                margin: 0px;
                padding-top: 5px;
            }
            div#middle table#operations tr.operation-selected td.operation-left a {
                color: #666;
                font-weight: bold;
                text-decoration: none;
                cursor: text;
                font-size: 10pt;
            }
            div#middle table#operations tr.operation-selected td.operation-right {
                width: 26px;
                background-image: url(<xsl:value-of select="$image-path"/>operation-selected-bg-right.gif);
                background-position: left bottom;
                background-repeat: no-repeat;
                background-attachment: scroll;
                background-color: #fff;
            }

            div#middle table#operations tr.operation td.operation-left {
                background-image: url(<xsl:value-of select="$image-path"/>operations-bg.gif);
                background-position: left bottom;
                background-repeat: no-repeat;
                background-attachment: scroll;
                padding-bottom: 10px;
                padding-left: 15px;
                margin: 0px;
                padding-top: 5px;
                font-size: 10pt;
            }
            div#middle table#operations tr.operation td.operation-left a{
                color: #000;
                font-size: 10pt;
                font-weight: bold;
                text-decoration: none;
            }
            div#middle table#operations tr.operation td.operation-left a:hover{
                font-size: 10pt;
                text-decoration: underline;
            }
            div#middle table#operations tr.operation td.operation-left a:visited {
                font-size: 10pt;
                color: #894f7b;
            }
            div#middle table#operations tr.operation td.operation-right {
                width: 26px;
                background-image: url(<xsl:value-of select="$image-path"/>operations-bg-right.gif);
                background-position: left bottom;
                background-repeat: no-repeat;
                background-attachment: scroll;
            }

            div#middle table#content-table {
                padding: 0px;
                margin: 0px;
                border-collapse: collapse;
            }
            div#middle table#content-table tr td.content {
                padding: 0px 10px 10px 10px;
            }
            div#middle table#content-table tr td.content-top {
                background-image: url(<xsl:value-of select="$image-path"/>content-top.gif);
                background-position: left top;
                background-repeat: repeat-x;
                background-attachment: scroll;
                height: 21px;
            }

            div#middle table#content-table tr td.content-top-right {
                background-image: url(<xsl:value-of select="$image-path"/>content-top-right.gif);
                background-position: left top;
                background-repeat: no-repeat;
                background-attachment: scroll;
                height: 21px;
                width: 12px;
            }

            /* footer styles */
            div#footer {
                margin-top: 30px;
                clear: both;
                height: 40px;
                text-align:center;
                color: white;
                font-weight:bold;
                background-color: #7e8e35;
                background-image: url(<xsl:value-of select="$image-path"/>gradient-rule-wide.gif);
                background-repeat: no-repeat;
                padding-left: 40px;
                padding-top: 16px;
                font-size: 8pt;
            }

            table.ops .operationDocumentation {
                margin-bottom: 1em;
            }
            table.ops td {
                padding: 0px 5px;
                font-size: 10pt;
                margin:0px;
            }
            table.ops td.label {
                text-align: right;
                vertical-align:top
            }
            table.ops td.label div {
                margin-right:1em;
                margin-top:3px;
            }
            table.ops td.param {
                width:90%;
            }
            table.ops textarea.nonemptyfield {
                height: 1.7em;
                overflow-x:hidden;
                overflow-y:auto;
                margin:0px;
                border: 1px solid #CCCCCC;
                width: 15em;
            }
            table.ops textarea.emptyfield {
                height: 1.7em;
                color:#CCC;
                overflow-x:hidden;
                overflow-y:auto;
                margin:0px;
                border: 1px solid #CCCCCC;
                width: 15em;
            }
            table.ops .typeannotation {
                color:#CCC;
            }
            table.ops .output {
                font-family: monospace;
                font-size:10pt;
                padding-top: 10px;
                padding-left: 10px;
            }
            table.ops .cornerExpand, table.ops .cornerCollapse {
                position:relative;
                top: 8px;
                left: -8px;
                cursor:pointer
            }
            table.ops .showDetail {
                display:block;
                margin-top:1em;
                font-family: "Lucida Grande","Lucida Sans","Microsoft Sans Serif", "Lucida Sans Unicode",verdana,sans-serif,"trebuchet ms"
            }
            table.ops .faultDetail {
                display:none;
                margin-top:1em;
            }

            /* styles for pretty-printed XML
                .fx-block (block of XML - element, multi-line text)
                .fx-elnm (element name)
                .fx-atnm (attribute name)
                .fx-att (attribute value)
                .fx-text (text content)
                .fx-cmk (comment markup)
                .fx-com (comment text)
                .fx-ns (namespace name)
                .fx-nsval (namespace value)
            */
            .fx-block {
                font-family: "Lucida Grande","Lucida Sans","Microsoft Sans Serif", "Lucida Sans Unicode",verdana,sans-serif,"trebuchet ms";
                font-size:13px;
                color:#555;
                line-height:140%;
                margin-left:1em;
                text-indent:-1em;
                margin-right:1em;
            }
            .fx-elnm { color:#005; }
            .fx-atnm { color:#500; }
            .fx-att { color:black }
            .fx-att a:link { color:black; text-decoration: none}
            .fx-att a:hover { color:black; text-decoration:underline}
            .fx-att a:active { color:black; text-decoration:underline}
            .fx-att a:visited { color:black; text-decoration:none }
            .fx-text { color:black; }
            pre.fx-text { margin-left:-1em; text-indent:0em; line-height:15px; }
            .fx-cmk {
                margin-left:1em;
                text-indent:-1em;
                margin-right:1em;
                color:#050;
            }
            .fx-com { color:#050;}
            .fx-ns { color:#505}
            .fx-nsval {color:#505}
            .fx-nsval a:link { color:#505; text-decoration: none}
            .fx-nsval a:hover { color:#505; text-decoration:underline}
            .fx-nsval a:active { color:#505; text-decoration:underline}
            .fx-nsval a:visited { color:#505; text-decoration:none}
        </style>
    </xsl:template>
</xsl:stylesheet>


