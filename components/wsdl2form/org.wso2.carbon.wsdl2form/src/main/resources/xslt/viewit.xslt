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
    <xsl:param name="service-name" select="services/service[1]/@name"/>

    <!-- Paths to external resources can be specified here. -->
    <xsl:param name="xslt-location" select="'/xslt/formatxml.xslt'"/>
    <xsl:param name="image-path" select="'images/tryit/'"/>
    <xsl:param name="enable-header" select="'false'"/>
    <xsl:param name="enable-footer" select="'false'"/>
    <xsl:param name="message-direction" select="'request'" />
    <xsl:param name="message" />
    <xsl:param name="operation-name" />

    <xsl:param name="message-type" select="'SOAP12'" />

    <!-- Allows some html to be inserted immediately before the body. -->
    <xsl:param name="breadcrumbs" />

    <xsl:param name="viewit-documentation" select="false()" />

    <xsl:variable name="operation" select="services/service[@name=$service-name and @type=$message-type]/operations/operation[@name=$operation-name]" />

    <xsl:variable name="message-elements">
        <xsl:choose>
            <xsl:when test="$message-direction='request'">
                  <xsl:value-of select="$operation/signature/params" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$operation/signature/returns" />                 
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>

    <xsl:variable name="message-payload">
        <xsl:variable name="body">
             <xsl:choose>
                 <xsl:when test="$message-type='SOAP12'">
                     <xsl:value-of select="$message[local-name() = 'Body' and namespace-uri() = 'http://schemas.xmlsoap.org/soap/envelope/']"/>
                 </xsl:when>
                 <xsl:when test="$message-type='SOAP12'">
                     <xsl:value-of select="$message[local-name() = 'Body' and namespace-uri() = 'http://www.w3.org/2003/05/soap-envelope']"/>
                 </xsl:when>
                 <xsl:otherwise>
                     <xsl:value-of select="$message[local-name() = 'Body' and namespace-uri() = 'http://schemas.xmlsoap.org/soap/envelope/']"/>
                 </xsl:otherwise>
             </xsl:choose>
        </xsl:variable>
        <xsl:variable name="wrapper-element">
            <xsl:value-of select="$message-elements/@wrapper-element" />
        </xsl:variable>
        <xsl:variable name="wrapper-element-ns">
             <xsl:value-of select="$message-elements/@wrapper-element-ns" />
        </xsl:variable>
        <xsl:value-of select="$body[local-name() = $wrapper-element and namespace-uri() = $wrapper-element-ns]" />
    </xsl:variable>

    <xsl:template match="/">
        <xsl:apply-templates select="services/service[@name=$service-name][1]"/>
    </xsl:template>

    <xsl:template match="service">
        <div id="wsdl2form-view-container">
            <div id="wsdl2form-view-content">
                  <xsl:call-template name="body-template" />
            </div>
        </div>
    </xsl:template>

    <xsl:template match="operation">
            <xsl:variable name="name">
                <xsl:call-template name="xml-name-to-javascript-name">
                    <xsl:with-param name="name" select="@name"/>
                </xsl:call-template>
            </xsl:variable>
            <div class="params" id="params_{$name}">
                <table class="ops">
                    <xsl:if test="$viewit-documentation">
                        <tr>
                            <td colspan="2">
                                <xsl:if test="documentation/node()">
                                    <div class="operationDocumentation">
                                        <xsl:copy-of select="documentation/node()"/>
                                    </div>
                                    </xsl:if>
                            </td>
                        </tr>
                    </xsl:if>
                    <xsl:for-each select="$message-elements/param">
                        <xsl:variable name="param-name">
                            <xsl:value-of select="@name" />
                        </xsl:variable>
                        <xsl:variable name="param-value">
                            <xsl:value-of select="$message-payload/child::node()[name() = $param-name]"/>
                        </xsl:variable>
                        <tr>
                            <xsl:choose>
                                <!-- this parameter represents expandable parameters -->
                                <xsl:when test="@token = '#any'">
                                    <td class="label"><div>(additional parameters)</div></td>
                                    <td class="param">
                                        <div><xsl:value-of select="$param-value[1]" /></div>
                                        <!-- TODO expandable fields of additional parameters -->
                                    </td>
                                </xsl:when>

                                <!-- this parameter represents a boolean (checkbox) -->
                                <xsl:when test="@type = 'boolean'">
                                    <td class="label">
                                        <xsl:value-of select="@name"/>
                                    </td>
                                    <td class="param">
                                        <div id="arrayparams_{$name}_{@name}">
                                            <xsl:for-each select="$param-value">
                                                <div>
                                                    <xsl:value-of select="." />
                                                </div>
                                            </xsl:for-each>
                                        </div>
                                    </td>
                                </xsl:when>

                                <!-- this parameter represents a QName (separate namespace and QName fields) -->
                                <xsl:when test="@type = 'QName'">
                                    <td class="label">
                                        <xsl:value-of select="@name"/>
                                    </td>
                                    <td class="param">
                                        <div id="arrayparams_{$name}_{@name}">
                                            <xsl:for-each select="$param-value">
                                                <div>
                                                    <xsl:value-of select="." />
                                                </div>
                                            </xsl:for-each>
                                        </div>
                                    </td>
                                </xsl:when>

                                <!-- this parameter represents an enumeration (<select>) -->
                                <xsl:when test="enumeration">
                                    <td class="label">
                                        <xsl:value-of select="@name"/>
                                    </td>
                                    <td class="param">
                                        <div id="arrayparams_{$name}_{@name}">
                                            <xsl:for-each select="$param-value">
                                                <div>
                                                    <xsl:value-of select="." />
                                                </div>
                                            </xsl:for-each>
                                        </div>
                                    </td>
                                </xsl:when>

                                <!-- this parameter represents a type exposed as a <textarea> -->
                                <xsl:otherwise>
                                    <!--<xsl:variable name="prefix">
                                        <xsl:if test="@type-namespace = 'http://www.w3.org/2001/XMLSchema'">xs:</xsl:if>
                                    </xsl:variable>
                                    <xsl:variable name="restriction">
                                        <xsl:if test="@restriction-of">
                                            <xsl:if test="@restriction-namespace = 'http://www.w3.org/2001/XMLSchema'">xs:</xsl:if>
                                            <xsl:value-of select="@restriction-of"/>
                                            <xsl:text> restriction</xsl:text>
                                        </xsl:if>
                                    </xsl:variable>-->
                                    <td class="label">
                                        <xsl:value-of select="@name"/>
                                    </td>
                                    <td class="param">
                                        <div id="viewit_arrayparams_{$name}_{@name}">
                                            <xsl:for-each select="$param-value">
                                                <div>
                                                    <xsl:value-of select="." />
                                                </div>
                                            </xsl:for-each>
                                        </div>
                                    </td>
                                </xsl:otherwise>
                            </xsl:choose>
                        </tr>
                    </xsl:for-each>
                 </table>
            </div>
    </xsl:template>

    <xsl:template name="xml-name-to-javascript-name">
        <xsl:param name="name"/>
        <xsl:value-of select="translate($name,'.-/','___')"/>
    </xsl:template>

    <xsl:template name="body-template">
        <!-- insert breadcrumbs -->
            <xsl:value-of select="$breadcrumbs" disable-output-escaping="yes"/>
            <!-- header -->
            <xsl:if test="$enable-header='true'">
             <div id="viewit_header">
            	 <nobr>
                	<h1>
                        <xsl:choose>
                            <xsl:when test="$message-type='request'">
                            Request View of <xsl:value-of select="$service-name"/> service.
                            </xsl:when>
                            <xsl:otherwise>
                            Response View of <xsl:value-of select="$service-name"/> service.
                            </xsl:otherwise>
                        </xsl:choose>
                    </h1>
                </nobr>
             </div>
            </xsl:if>
            <!-- end of header -->
            <div id="viewit_body">
                <div id="viewit_middle">
                    <table id="viewit_middle-content">
                        <tr>
                            <td>
                                 <table id="viewit_content-table" style="width: 100%;">
                                    <tr>
                                        <td class="content-top"><img src="{$image-path}spacer.gif"/></td>
                                        <td class="content-top-right"><img src="{$image-path}spacer.gif"/></td>
                                    </tr>
                                    <tr>
                                        <td class="content">
                                            <xsl:apply-templates select="operations/operation[@name=$operation-name][1]"/>
                                        </td>
                                        <td class="content-right"><img src="{$image-path}spacer.gif"/></td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <td class="bottom">
                                <img src="{$image-path}bottom-right.gif"/>
                            </td>
                        </tr>
                    </table>
                </div>
            </div>
            <!-- footer -->
            <xsl:if test="$enable-footer='true'">
              <div id="viewit_footer">
                <p>© <a href="http://wso2.com/">WSO2 Inc.</a></p>
              </div>
            </xsl:if>
            <!-- end of footer -->
    </xsl:template>

    <!-- template for inserting CSS -->
    <xsl:template name="css">
        <!-- css is embedded rather than linked so that the $image-path can be altered dynamically -->
        <style type="text/css">
            body {
                margin: 0px;
                padding: 0px;
                font-family: "Lucida Grande","Lucida Sans","Microsoft Sans Serif","Lucida Sans Unicode",verdana,sans-serif,"trebuchet ms";
                font-size: 10pt;
            }

            p { }
            td { }
            a:link { }
            a:visited { }
            a:hover { }
            a:active { }

            a img {
                border: 0px;
            }

            /* header styles */
            div#viewit_header {
                height: 70px;
                background-image: url(<xsl:value-of select="$image-path"/>gradient-rule-wide.gif);
                background-repeat: no-repeat;
                background-color: #7e8e35;
                background-position: bottom left;
                color:white;
            }
            div#viewit_header h1 {
                margin: 0px 0px 0px 0px;
                padding: 20px 0px 0px 40px;
                font-size: 18pt;
                font-weight: normal;
            }
            /* body styles */
            div#viewit_body {
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

            /* middle styles */
            div#viewit_middle {
                margin-left: 35px;
                margin-top: 15px;
                margin-right: 50px;
                margin-right: 20px;
                margin-bottom: 0px;
            }
            /* tabs styles */
            table#viewit_middle-content {
                padding: 0px;
                margin: 0px;
                border-collapse: collapse;
                width: 93%;
            }
            table#viewit_middle-content tr td {
                padding: 0px;
                vertical-align:top;
            }
            table#viewit_middle-content tr td.left-tabs {
                background-image: url(<xsl:value-of select="$image-path"/>left-tabs-bg.gif);
                background-position: top right;
                background-repeat: repeat-y;
                background-attachment: scroll;
                width: 5%;
                vertical-align: top;
            }
            table#viewit_middle-content tr td.bottom-left {
                background-image: url(<xsl:value-of select="$image-path"/>bottom-left.gif);
                background-position: top right;
                background-repeat: no-repeat;
                background-attachment: scroll;
                height: 16px;
                vertical-align: top;
            }
            table#viewit_middle-content tr td.bottom {
                background-image: url(<xsl:value-of select="$image-path"/>bottom.gif);
                background-position: top left;
                background-repeat: repeat-x;
                background-attachment: scroll;
                height: 16px;
                text-align: right;
                vertical-align: top;
            }

            table#viewit_content-table {
                padding: 0px;
                margin: 0px;
                border-collapse: collapse;
            }
            table#viewit_content-table tr td.content {
                padding: 0px 10px 10px 10px;
            }
            table#viewit_content-table tr td.content-top {
                background-image: url(<xsl:value-of select="$image-path"/>content-top.gif);
                background-position: left top;
                background-repeat: repeat-x;
                background-attachment: scroll;
                height: 21px;
            }
            table#viewit_content-table tr td.content-top-right {
                background-image: url(<xsl:value-of select="$image-path"/>content-top-right.gif);
                background-position: left top;
                background-repeat: no-repeat;
                background-attachment: scroll;
                height: 21px;
                width: 12px;
            }

            /* footer styles */
            div#viewit_footer {
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

            /* parameter form styles */
            table#viewit_content-table div.params {
                display:none;
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


