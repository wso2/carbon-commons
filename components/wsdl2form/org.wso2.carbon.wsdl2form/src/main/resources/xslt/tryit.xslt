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

    <xsl:import href="/xslt/javascript-literals.xslt"/>
    <xsl:output method="html" indent="yes"
                doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
                doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>

    <!-- This stylesheet only supports a single service at a time.
         If no service name is specified in this parameter, the first one is used.  -->
    <xsl:param name="service" select="services/service[1]/@name"/>

    <!-- Paths to external resources can be specified here. -->
    <xsl:param name="js-csrf-protection" select="'../carbon/admin/js/csrfPrevention.js'"/>
    <xsl:param name="js-global-params" select="'../carbon/global-params.js'"/>
    <xsl:param name="js-WSRequest" select="'?wsdl2form&amp;contentType=text/javascript&amp;resource=js/WSRequest.js'"/>
    <xsl:param name="js-jQuery" select="'?wsdl2form&amp;contentType=text/javascript&amp;resource=js/jquery-1.5.2.min.js'"/>
    <xsl:param name="js-jQueryUI" select="'?wsdl2form&amp;contentType=text/javascript&amp;resource=js/jquery-ui-1.8.11.custom.min.js'"/>
    <xsl:param name="js-corners" select="'?wsdl2form&amp;contentType=text/javascript&amp;resource=extras/jquery.corner.js'"/>
    <xsl:param name="js-editArea" select="'?wsdl2form&amp;contentType=text/javascript&amp;resource=editarea/edit_area_full.js'"/>
    <xsl:param name="proxyAddress" select="'../admin/jsp/WSRequestXSSproxy_ajaxprocessor.jsp'"/>
    <xsl:param name="xslt-location" select="'?wsdl2form&amp;contentType=text/xml&amp;resource=xslt/prettyprinter.xslt'"/>
    <xsl:param name="enable-header" select="'false'"/>
    <xsl:param name="enable-footer" select="'false'"/>
    <xsl:param name="js-service-stub" />
    <xsl:param name="css-images" select="'?wsdl2form&amp;contentType=image/png&amp;resource=images/'"/>
    <xsl:param name="css-jQueryUI" select="'?wsdl2form&amp;contentType=text/css&amp;resource=smoothness/jquery-ui-1.8.11.custom.css'"/>
    <xsl:param name="services-path" />

    <!-- Toggle between DOM and E4X treatment of XML objects. -->
    <xsl:param name="e4x" select="false()"/>

    <!-- Allows some html to be inserted immediately before the body. -->
    <xsl:param name="breadcrumbs" />

    <!-- Within a browser XSS restrictions prevent endpoint access outside the domain
         from which this page was obtained.  This page does some endpoint rewriting and
         hiding as a result.  This behavior can be disabled for use in situations (e.g.
         IE viewing a local file) where XSS restrictions don't apply. -->
    <xsl:param name="fixendpoints" select="'true'"/>
    <!-- For non-WSO2 services, the link to alternate endpoints might not be valid.
         Set this parameter to 'false' to disable that link. -->
    <xsl:param name="show-alternate" select="'true'"/>

    <xsl:template match="/">
        <xsl:apply-templates select="services/service[@name=$service][1]"/>
    </xsl:template>

    <xsl:template match="service">
        <html>
            <head>
                <title>Try the <xsl:value-of select="$service"/> service</title>
                <xsl:call-template name="header-template" />
            </head>
            <body>
                <xsl:call-template name="body-template" />
            </body>
         </html>
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
                for (var j=0,jj=a.length; j&lt;jj; j++) a[j].e.id = a[j].i;
                a = null;
                return o;
            }
        }

        var formatxslt = null;
        $.get('<xsl:value-of select="$xslt-location"/>', null, function(data) {
               formatxslt = data;
               init();
        }, "xml");

    </xsl:template>

    <xsl:template name="header-template">
        <xsl:call-template name="css"/>
        <script type="text/javascript" src="{$js-csrf-protection}"></script>
        <xsl:text>
        </xsl:text>
        <script type="text/javascript" src="{$js-WSRequest}"></script>
        <xsl:text>
        </xsl:text>
        <script type="text/javascript" src="{$js-jQuery}"></script>
        <xsl:text>
        </xsl:text>
        <script type="text/javascript" src="{$js-jQueryUI}"></script>
        <xsl:text>
        </xsl:text>
        <script type="text/javascript" src="{$js-corners}"></script>
        <xsl:text>
        </xsl:text>
        <script type="text/javascript" src="{$js-editArea}"></script>
        <xsl:text>
        </xsl:text>
        <script type="text/javascript" src="{$js-global-params}"></script>
        <xsl:text>
        </xsl:text>
        <!-- Calculate the source of the stub, including whether it's e4x or not -->
        <xsl:variable name="e4x-param">
            <xsl:if test="$e4x">; e4x=1</xsl:if>
        </xsl:variable>

        <xsl:variable name="src-tryit-service">
            <xsl:value-of select="$js-service-stub"/><xsl:if test="$e4x">&amp;lang=e4x</xsl:if>
        </xsl:variable>
        <script type="text/javascript{$e4x-param}" src="{$src-tryit-service}"></script>
        <xsl:text>
        </xsl:text>
        <script type="text/javascript{$e4x-param}">
            <xsl:call-template name="browser-compatibility"/>
            <xsl:call-template name="javascript-functions"/>
            <xsl:call-template name="update-service-info"/>
            <xsl:call-template name="tryit-ui-functions"/>
        </script>
    </xsl:template>

    <xsl:template name="body-template">
        <div id="container">
            <div id="header-toolbar"></div>
            <div class="page-margin"></div>
            <div id="middle">
                <div id="left">
                    <div id="priority-operations">
                        <div class="title closed title-corners">
                            <div class="toggle-icon closed"></div>
                            <a href="#" class="toggle-link closed toggle-link-corners">Priority Operations</a>
                        </div>
                        <div class="dynamic-content dynamic-content-corners"></div>
                    </div>
                    <div id="all-operations">
                        <div class="title opened title-corners">
                            <div class="toggle-icon opened"></div>
                            <a href="#" class="toggle-link opened toggle-link-corners">All Operations</a>
                        </div>
                        <div class="dynamic-content dynamic-content-corners">
                            <xsl:for-each select="operations/operation">
                                <div class="operation-wrapper">
                                    <a class="operation-name" href="#"><xsl:value-of select="@name"/></a>
                                    <div class="prior-icon-wrapper"></div>
                                </div>
                            </xsl:for-each>
                        </div>
                    </div>
                </div>
                <div class="center-margin"></div>
                <div id="center-wrapper">
                    <div id="service-info-wrapper" class="center-wrapper">
                        <div id="service-info" class="center-wrapper">
                            <div id="service-name-wrapper" class="center-wrapper"><xsl:value-of select="$service"/></div>
                            <div style="clear:both;"></div>
                            <xsl:if test="documentation/node()">
                                <div id="service-docs" class="top-info center-wrapper">
                                    <div class="title closed title-corners">
                                        <div class="toggle-icon closed"></div>
                                        <a href="#" class="toggle-link toggle-link-corners">Service Infomation</a>
                                    </div>
                                    <div class="dynamic-content dynamic-content-corners"></div>
                                </div>
                                <div style="clear:both;"></div>
                            </xsl:if>
                            <xsl:if test="operations/operation/binding-details/policy/@type = 'UTOverTransport'">
                                <div id="security-configs" class="top-info center-wrapper">
                                    <div class="title closed title-corners">
                                        <div class="toggle-icon closed"></div>
                                        <a href="#" class="toggle-link toggle-link-corners">Security</a>
                                    </div>
                                    <div class="dynamic-content dynamic-content-corners">
                                        <div id="username-wrapper"><label for="username">Username : </label><input type="text"
                                                                                                                   id="username"/>
                                        </div>
                                        <div id="password-wrapper"><label for="password">Password : </label><input type="password"
                                                                                                                   id="password"/>
                                        </div>
                                    </div>
                                </div>
                                <div style="clear:both;"></div>
                            </xsl:if>
                            <div id="endpoint-configs" class="top-info center-wrapper">
                                <div class="title closed title-corners">
                                    <div class="toggle-icon closed"></div>
                                    <a href="#" class="toggle-link toggle-link-corners"></a>
                                </div>
                                <div class="dynamic-content dynamic-content-corners">
                                    <div id="endpoints-wrapper">
                                        <div id="endpoint-selector-title">Select a different Endpoint</div>
                                        <div id="endpoint-selector">
                                            <label for="endpoint-list">Select an endpoint : </label>
                                            <select id="endpoint-list">
                                                <xsl:for-each select="/services/service[@name=$service]">
                                                    <xsl:sort select="@type = 'SOAP12'" order="descending"/>
                                                    <xsl:sort select="@type = 'SOAP11'" order="descending"/>
                                                    <xsl:sort select="@address" order="ascending"/>
                                                    <option value="{@endpoint}"><xsl:value-of select="@endpoint"/></option>
                                                </xsl:for-each>
                                            </select>
                                        </div>
                                        <div id="endpoint-modifier">
                                            <label for="modified-endpoint">Change the address for the selected endpoint
                                                : </label>
                                            <input id="modified-endpoint" type="text" value="{@address}"/>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div style="clear:both;"></div>
                        </div>
                    </div>
                    <div style="clear:both;"></div>
                    <div id="tryit-messages-wrapper" class="center-wrapper">
                        <div class="center-padding"></div>
                        <div id="tryit-messages" class="content">
                            <!--<div class="tryit-message error-message">
                                <div class="message-text">Error while accessing the service endpoint.
                                </div>
                                <div class="hide-button"><a href="#">Hide</a></div>
                            </div>
                            <div style="clear :both;"></div>-->
                            <xsl:if test="operations/operation/binding-details/policy/@type != 'UTOverTransport'">
                                <div id="security-policy" class="tryit-message warning-message">
                                    <div class="message-text">
                                        <strong>Warning!</strong> This service requires credentials beyond what this try-it can provide (<xsl:value-of select="operations/operation/binding-details/policy/@type"/>).
                                    </div>
                                    <div class="hide-button"><a href="#">Hide</a></div>
                                </div>
                                <div style="clear :both;"></div>
                            </xsl:if>
                            <div id="proxy-warning" class="tryit-message warning-message">
                                <div class="message-text">Private proxy protocol will be attempted as cross-domain browser restrictions might be enforced for this endpoint.</div>
                                <div class="hide-button"><a href="#">Hide</a></div>
                            </div>
                            <div style="clear :both;"></div>
                            <div id="alternate-tryit" class="tryit-message info-message">
                                <div class="message-text">Try an alternate <a id="alternate-url"/></div>
                                <div class="hide-button"><a href="#">Hide</a></div>
                            </div>
                            <div style="clear :both;"></div>
                            <div id="rest-url" class="tryit-message info-message">
                                <div class="message-text"></div>
                                <div class="hide-button"><a href="#">Hide</a></div>
                            </div>
                            <div style="clear :both;"></div>
                            <div id="security-enabled" class="tryit-message info-message">
                                <div class="message-text">This service has been secured with UTOverTransport</div>
                                <div class="hide-button"><a href="#">Hide</a></div>
                            </div>
                            <div style="clear :both;"></div>
                        </div>
                        <div class="center-padding"></div>
                    </div>
                    <div id="center" class="center-wrapper">
                        <div class="center-padding"></div>
                        <div id="content-wrapper" class="content">
                            <div id="content-infobar" class="content-wrapper">
                                <div id="operation-name" class="title closed">
                                    <div id="operation-name-icon" class="toggle-icon closed"></div>
                                    <a href="#" class="toggle-link closed">myOperation</a>
                                </div>
                                <div id="loading-icon-wrapper">
                                    <div id="loading-icon"></div>
                                </div>
                                <div id="operation-description" class="dynamic-content"></div>
                            </div>
                            <div id="content-toolbar-top" class="content-wrapper">
                                <div class="main-buttons">
                                    <div class="invoke"></div>
                                </div>
                                <div id="switch-view">
                                    <div id="switch-view-h" class="switch-view"></div>
                                    <div id="switch-view-v" class="switch-view"></div>
                                </div>
                            </div>
                            <div id="content" class="content-wrapper">
                                <div class="content-padding"></div>
                                <div id="content-inner">
                                    <div id="request-editor-wrapper" class="editor-wrapper">
                                        <div class="editor-title">Request</div>
                                        <textarea id="request-editor" class="editor"></textarea>
                                    </div>
                                    <div id="response-editor-wrapper" class="editor-wrapper">
                                        <div class="editor-title">Response</div>
                                        <textarea id="response-editor" class="editor"></textarea>
                                    </div>
                                </div>
                                <div class="content-padding"></div>
                            </div>
                            <div id="content-toolbar-bottom" class="content-wrapper">
                                <div class="main-buttons">
                                    <div class="invoke"></div>
                                </div>
                            </div>
                        </div>
                        <div class="center-padding"></div>
                    </div>
                </div>
            </div>
            <div id="footer"></div>
            <div class="page-margin"></div>
        </div>
    </xsl:template>

    <xsl:template name="update-service-info">
        <xsl:if test="documentation/node()">
            <xsl:variable name="documentation">
                <xsl:variable name="doc-string">
                    <xsl:call-template name="xml-to-string">
                        <xsl:with-param name="node-set" select="documentation/node()"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:call-template name="escape-string">
                    <xsl:with-param name="text" select="$doc-string"/>
                </xsl:call-template>
            </xsl:variable>
        services['<xsl:value-of select="@name"/>'].$.documentation = '<xsl:value-of select="normalize-space($documentation)"/>';</xsl:if>
        <xsl:for-each select="operations/operation">
            <xsl:if test="documentation/node()">
                <xsl:variable name="documentation">
                <xsl:variable name="doc-string">
                    <xsl:call-template name="xml-to-string">
                        <xsl:with-param name="node-set" select="documentation/node()"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:call-template name="escape-string">
                    <xsl:with-param name="text" select="$doc-string"/>
                </xsl:call-template>
            </xsl:variable>
        services['<xsl:value-of select="$service"/>']['operations']['<xsl:value-of select="@name"/>'].documentation = '<xsl:value-of select="normalize-space($documentation)"/>';</xsl:if>
        <xsl:if test="binding-details/policy/@type">
        services['<xsl:value-of select="$service"/>']['operations']['<xsl:value-of select="@name"/>'].security = '<xsl:value-of select="binding-details/policy/@type"/>';</xsl:if>
        </xsl:for-each>

        var service = services['<xsl:value-of select="@name"/>'];
        var operationName = '<xsl:value-of select="operations/operation[1]/@name"/>';
        var operation;
        var activeOperation = null;
        service.$.proxyAddress = '<xsl:value-of select="$proxyAddress"/>';
        service.$.endpoint = '<xsl:value-of select="@endpoint"/>';
    </xsl:template>

    <xsl:template name="tryit-ui-functions">
        var VIEW = {
            H : "horizontal",
            V : "vertical"
        };

        var setEditorContent = function(editor, content) {
            if(editor == "request-editor") {
                if(!content || content == "") content = "&lt;body/&gt;";
                else content = "&lt;body&gt;" + content + "&lt;/body&gt;";
            } else if(!content) {
                content = "";
            }
            editAreaLoader.setValue(editor, prettyPrintXML(content));
            editAreaLoader.execCommand(editor, 'set_word_wrap', true);
            editAreaLoader.execCommand(editor, 'set_word_wrap', false);
        };

        var getEditorContent = function(editor) {
            var content = editAreaLoader.getValue(editor);
            if(editor == "request-editor")
                content = content.replace(/^[\t\r\s\n]*(&lt;body&gt;)|(&lt;\/body&gt;)[\t\r\s\n]*$|^[\t\r\s\n]*(&lt;body\/&gt;)[\t\r\s\n]*$/g, "");
            return content;
        };

        var activeView = VIEW.H;

        var switchView = function(view) {
            activeView = view;
            $(".switch-view").button("enable");
            $(".editor-wrapper").show();
            var width = contentInner;
            var offset = 6;
            if (view === VIEW.V) {
                $(".editor-wrapper").width(width);
                $(".editor").width(width - offset);
                $("#frame_request-editor").width(width);
                $("#frame_response-editor").width(width);
                $("#switch-view-v").button("disable");
            } else {
                width = width / 2;
                $(".editor-wrapper").width(width);
                $(".editor").width(width - offset);
                $("#frame_request-editor").width(width);
                $("#frame_response-editor").width(width);
                $("#switch-view-h").button("disable");
            }
            corner();
            $(document).scrollTop(0);
        };

        var corner = function() {
            $(".jquery-corner").remove();
            if ($.browser.msie) {
                $(".ui-corner-all").corner("4px").css("padding", "1px");
                $(".ui-corner-left").corner("4px left").css("padding", "1px");
                $(".ui-corner-right").corner("4px right").css("padding", "1px");
            }
            $("#center").corner("4px");
            $(".tryit-message").corner("4px");
            $(".dynamic-content-corners").corner("4px bottom");
            $(".operation-wrapper").corner("4px");
            $(".title-corners.opened").corner("4px top");
            $(".title-corners.closed").corner("4px");
            $(".toggle-icon").corner("2px");
            $(".prior-icon-wrapper").corner("2px");
        };

        var pageMargin = 10;
        var centerMargin = 10;
        var centerPadding = 5;
        var contentPadding = 5;
        var left = 200;
        var minWidth = 900;
        var contentInner;
        var resizePage = function() {
            var width = $(window).width();
            var height = $(window).height();
            width = width > minWidth ? width : minWidth;
            var middle = width - 2 * pageMargin;
            var centerWrapper = middle - centerMargin - left;
            var center = centerWrapper;
            var contentWrapper = center - 2 * centerPadding;
            var content = contentWrapper;
            contentInner = content - 2 * contentPadding;
            $("#container").width(width);
            $("#middle").width(middle);
            $("#left").width(left);
            $("#center-wrapper").width(centerWrapper);
            $(".center-wrapper").width(center);
            $(".content").width(contentWrapper);
            $(".content-wrapper").width(content);
            $("#content-inner").width(contentInner);
            $("#modified-endpoint").width(width - 530);
            corner();
            switchView(activeView);
        };

        var init = function() {
            $(document).ready(function() {
                $("#switch-view-h").button({
                    label : "Horizontal"
                }).click(function() {
                    switchView(VIEW.H);
                });

                $("#switch-view-v").button({
                    label : "Vertical"
                }).click(function() {
                    switchView(VIEW.V);
                });

                $(".invoke").button({
                    label : "Send"
                }).click(function() {
                    $(".invoke").button("disable");
                    activeOperation = operationName;
                    operation.payload = getEditorContent("request-editor");
                    operation.response = null;
                    operation.callback = function(payload) {
                        $("#loading-icon-wrapper").hide();
                        service.operations[activeOperation].response = payload;
                        if(activeOperation === operationName) setEditorContent("response-editor", payload);
                        activeOperation = null;
                        $(".invoke").button("enable");
                    };
                    operation.onError = function(error) {
                        $("#loading-icon-wrapper").hide();
						if(typeof error === "string") {
							error = createTryitError(error);
						} else if(error.detail) {
							error = error.detail;
						} else {
							error = createTryitError(error.reason);
						}
                        service.operations[activeOperation].response = error;
                        if(activeOperation === operationName) setEditorContent("response-editor", error);
                        activeOperation = null;
                        $(".invoke").button("enable");
                    };

                    // set credentials
                    if(operation.security === "UTOverTransport") {
                        service.$.username = $("#username").val();
                        service.$.password = $("#password").val();
                    }
                    $("#loading-icon-wrapper").show();
                    operation(cleanPayload(operation.payload));
                });

                $("#switch-view").buttonset();

                $("#operation-name > a").toggle(
                        function() {
                            $("#operation-description").show();
                            $("#operation-name .toggle-icon").removeClass("closed").addClass("opened");
                            corner();
                        },
                        function() {
                            $("#operation-description").hide();
                            $("#operation-name .toggle-icon").removeClass("opened").addClass("closed");
                            corner();
                        });

                $(".hide-button > a").click(function() {
                    $(this).parent().parent().hide();
                    if ($(".tryit-message:visible").length  &lt;= 0) {
                        $("#tryit-messages-wrapper").hide();
                    }
                    corner();
                });

                $(".toggle-link, .toggle-icon").click(function() {
                    var container = $(this).parent().parent();
                    if ($(this).hasClass("opened")) {
                        $(".dynamic-content", container).hide();
                        $(".toggle-link", container).removeClass("opened").addClass("closed");
                        $(".toggle-icon", container).removeClass("opened").addClass("closed");
                        $(".title", container).removeClass("opened").addClass("closed")
                                .uncorner().corner("4px");
                    } else {
                        $(".dynamic-content", container).show();
                        $(".toggle-link", container).removeClass("closed").addClass("opened");
                        $(".toggle-icon", container).removeClass("closed").addClass("opened");
                        $(".title", container).removeClass("closed").addClass("opened")
                                .uncorner().corner("4px top");
                    }
                });

                $(".operation-wrapper").mouseleave(function() {
                    $(this).removeClass("operation-hover");
                }).mouseenter(function() {
                    $(this).addClass("operation-hover");
                });

                $("#all-operations .prior-icon-wrapper").click(function() {
                    var that = $(this);
                    var clone = that.parent().clone();
                    clone.removeClass("operation-hover").mouseleave(function() {
                        $(this).removeClass("operation-hover");
                    }).mouseenter(function() {
                        $(this).addClass("operation-hover");
                    });
                    $(".operation-name", clone).click(function() {
                        $(".operation-wrapper").removeClass("selected");
                        $(this).parent().addClass("selected");
                        selectOperation($.trim($(this).text()));
                        $("#all-operations .operation-wrapper > .operation-name").filter(function() {
                             return $.trim($(this).text()) === operationName;
                        }).parent().addClass("selected");
                    });
                    $(".prior-icon-wrapper", clone).click(function() {
                        that.show();
                        $(this).parent().remove();
                    });
                    $("#priority-operations .dynamic-content").append(clone);
                    that.hide();
                });

                $("#all-operations .operation-name").click(function() {
                    $(".operation-wrapper").removeClass("selected");
                    $(this).parent().addClass("selected");
                    selectOperation($.trim($(this).text()));
                    $("#priority-operations .operation-wrapper > .operation-name").filter(function() {
                         return $.trim($(this).text()) === operationName;
                    }).parent().addClass("selected");
                });

                $("#endpoint-list").change(function() {
                    selectEndpoint($(this).val());
                });
                $("#modified-endpoint").change(function() {
                    changeAddress($(this).val());
                });

                $(".page-margin").width(pageMargin);
                $(".center-margin").width(centerMargin);
                $(".center-padding").width(centerPadding);
                $(".content-padding").width(contentPadding);
                $("#left").width(left);
                $(".operation-name").width(left - 28);

                var documentation = $.trim(service.$.documentation);
                if(documentation &amp;&amp; documentation !== "") {
                    $("#service-docs .dynamic-content").html(service.$.documentation);
                } else {
                    hideInfo("service-docs");
                }

                editAreaLoader.init({
                    id : "request-editor",
                    syntax: "xml",
                    start_highlight: true,
                    allow_toggle : false,
                    font_size : 8,
                    change_callback : "showRestTemplate"
                });
                editAreaLoader.init({
                    id : "response-editor",
                    syntax: "xml",
                    start_highlight: true,
                    allow_toggle : false,
                    font_size : 8
                });

                resizePage();

                selectEndpoint($("#endpoint-list").val());
                selectOperation(operationName);
                $("#all-operations .operation-wrapper > .operation-name").filter(function() {
                     return $.trim($(this).text()) === operationName;
                }).parent().addClass("selected");
                // Check that the state of the endpoint data matches what the browser displayes in the form.<xsl:if test="$fixendpoints='true'">
                fixEndpoints();</xsl:if>
                $(window).resize(resizePage);
             });
        };
    </xsl:template>

    <xsl:template name="javascript-functions">

        /*
         * prettyPrintXML : serialize XML in pretty-print mode
         *
         *    doc: doc to serialize.
         */
        function prettyPrintXML(doc) {
            if(!doc) return null;
            var output;
            if(typeof doc === "string") {
                doc = cleanPayload(doc);
                if(!doc) return null;
                doc = WSRequest.util.xml2DOM(doc);
            }
            if ($.browser.msie) {
                output = doc.transformNode(formatxslt);
            } else {
                var oProcessor = new XSLTProcessor();
                oProcessor.importStylesheet(formatxslt);
                output = oProcessor.transformToDocument(doc);
            }
	    output = output ? output : doc;
            return $.trim(WSRequest.util._serializeToString(output));
        }

		function createTryitError(error) {
			return '&lt;TryitClient xmlns="http://tryit.carbon.wso2.org"&gt;' + 
						'&lt;Reason&gt;' + error + '&lt;/Reason&gt;' + 
					'&lt;/TryitClient&gt;';
		}
        <xsl:if test="$fixendpoints='true'">/*
         * fixEndpoints : cross-domain sharing violations may occur if the WSDL contains fixed IPs
         *                but "localhost" is used to fetch the try-it page. The common case is where
         *                the fixed IP actually is equivalent to localhost or 127.0.0.1 but XSS isn't
         *                smart enough to determine this.  This function (1) removes
         *                endpoints that aren't reachable from this tryit (namely https vs http),
         *                (2) generates a link a tryit page from which those endpoints can be invoked,
         *                and (3) rewrites the domain (excluding port) of the endpoints to match the

         *                page domain.
         */
        function fixEndpoints() {
            var pageUrl = document.URL;
            var pageScheme = WebService.utils.scheme(pageUrl);
            var linkFixed = false;
            // only attempt fixup if we're from an http/https domain ('file:' works fine on IE without fixup)
            if (pageScheme == "http" || pageScheme == "https") {
                var pageDomain = WebService.utils.domain(pageUrl);
                // start at the end and count down so we don't mess up indices as we delete options
                $("#endpoint-list > option").each(function() {
                    var endpointAddress = service.$.getAddress($.trim($(this).val()));
                    var endpointScheme = WebService.utils.scheme(endpointAddress);
                    if ((endpointScheme == 'http' || endpointScheme == 'https') &amp;&amp; endpointScheme != pageScheme) {
                        // schemes don't match; if we haven't already added a link to the other
                        //    try-it, add it now.
                        if (!linkFixed) {
                            var endpointPort;
                            if (endpointScheme == 'http') {
                                endpointPort = HTTP_PORT;
                            } else {
                                endpointPort = HTTPS_PORT;
                            }
                            $("#alternate-url").text(pageScheme == "http" ? "https" : "http").attr("href",
                                    pageUrl.replace(WebService.utils.scheme(pageUrl), endpointScheme).
                                    replace(WebService.utils.domainPort(pageUrl), endpointPort));
                            linkFixed = true;
                        }
                        var currentEndpointAddress = $("#modified-endpoint").val();
                        var currentEndpointScheme = WebService.utils.scheme(currentEndpointAddress);
                        if (currentEndpointScheme != endpointScheme) {
                            // and remove access to this endpoint from this page.
                            //Commented the following as a fix to JIRA ESBJAVA-2058
                            //$(this).remove();
                        }
                    }
                });
            }
            <xsl:if test="$show-alternate='true'">// If we didn't detect the necessity to present an alternate tryit link, remove it from view.
            if (linkFixed) showMessage("alternate-tryit");
            else hideMessage("alternate-tryit");</xsl:if>
        }</xsl:if>

        function showRestTemplate() {
            var details = service.$._endpointDetails[service.$.endpoint];
            if (details.type == 'HTTP') {
                var options = services.$._setOptions(details, operationName);
                if ((options["HTTPMethod"] == null || options["HTTPMethod"] == 'GET') &amp;&amp; details.fitsInURLParams[operationName]) {
                    var restURL = WSRequest.util._buildHTTPpayload(options, details.address, cleanXML(getEditorContent("request-editor")))["url"];
                    $("#rest-url .message-text").html("Note: This operation is also available via HTTP GET at &lt;a href='" + restURL + "'>" + restURL + "&lt;/a>.");
                    showMessage("rest-url");
                    return;
                }
            }
            hideMessage("rest-url");
        }

        /*
         * selectEndpoint : When the user chooses a binding, tell the stub that's what we'll use for
         *                  future invocations.  Also display the endpoint address associated
         *                  with that binding in the address textarea.
         */
        function selectEndpoint(endpoint) {
            service.$.endpoint = endpoint;
            var address = service.$.getAddress(endpoint);
            $("#endpoint-configs .toggle-link").text("Using Endpoint - " + endpoint);
            $("#modified-endpoint").val(address);
            changeAddress(address);
        }

        /*
         * changeAddress : When the user edits the address manually, tell the stub this new value
         *                 is now to be associated with the currently selected binding.
         */
        function changeAddress(address) {
            hideMessage("proxy-warning");
            var endpoint = service.$.endpoint;
            if (address != null) {
                service.$.setAddress(endpoint, address);
                var xss = address.substring(0, address.indexOf("/", 8)) !=
                          document.URL.substring(0, document.URL.indexOf("/", 8));
                if(xss) showMessage("proxy-warning");
                showRestTemplate();
            }
        }

        function selectOperation(name) {
            var op = service["operations"][operationName];
            op.payload = getEditorContent("request-editor");
            op.response = getEditorContent("response-editor");

            operationName = name;
            op = operation = service["operations"][name];
            op.payload = op.payload ? op.payload : op.payloadXML();

            setEditorContent("request-editor", op.payload);
            setEditorContent("response-editor", op.response);

            showRestTemplate();

            $("#operation-name .toggle-link").text(name);
            var documentation = $.trim(op.documentation);
            if(documentation &amp;&amp; documentation !== "") {
                $("#operation-description").html(op.documentation);
            }

            if(op.security) {
                if(op.security == "UTOverTransport") {
                    hideMessage("security-policy");
                    showInfo("security-configs");
                    showMessage("security-enabled");
                } else {
                    hideMessage("security-enabled");
                    hideInfo("security-info");
                    showMessage("security-policy");
                }
            } else {
                hideInfo("security-info");
                hideMessage("security-policy");
                hideMessage("security-enabled");
            }
        }

        function cleanPayload(payload) {
            if(!payload || payload === "") return null;
            return cleanXML(payload);
        }

        function cleanXML(payload) {
            return payload.replace(/[\t\r\s]*[\n][\t\r\s]*/g, "");
        }

        function showMessage(messageID) {
            $("#tryit-messages-wrapper").show();
            $("#" + messageID).show();
        }

        function hideMessage(messageID) {
            $("#" + messageID).hide();
        }

        function showInfo(infoID) {
            if(!$("#" + infoID + " .title").hasClass("opened")) $("#" + infoID + " .toggle-link").trigger("click");
        }

        function hideInfo(infoID) {
            if(!$("#" + infoID + " .title").hasClass("closed")) $("#" + infoID + " .toggle-link").trigger("click");
        }
    </xsl:template>

    <!-- template for inserting CSS -->
    <xsl:template name="css">
        <link media="all" type="text/css" rel="stylesheet" href="{$css-jQueryUI}"/>
        <style type="text/css">
            body {
                padding: 0;
                margin: 0;
                font-size: 80%;
                font-family: arial, sans-serif;
            }

            #container {
                float: left;
                background: #fff;
            }

            #header-toolbar {
                height: 10px;
                overflow: hidden;
            }

            .page-margin {
                float: left;
                height: 1px;
            }

            .center-margin {
                float: left;
                height: 1px;
            }

            .center-padding {
                height: 1px;
                float: left;
            }

            .content-padding {
                height: 1px;
                float: left;
            }

            #middle {
                float: left;
            }

            #left {
                float: left;
            }

            #center-wrapper {
                float: left;
                margin: 0 0 10px 0;
            }

            #center {
                padding: 5px 0;
                background: #a0a0a0;
                float: left;
            }

            #content-wrapper {
                float: left;
            }

            #right {
                height: 300px;
                float: left;
                background: #003333;
            }

            #content-infobar {
                float: left;
            }

            #content-toolbar-top, #content-toolbar-bottom {
                padding: 5px 0;
                float: left;
                background: #333333;
            }

            #content {
                float: left;
                background: #eee;
            }

            #content-inner {
                float: left;
            }

            .editor-wrapper {
                float: left;
                padding: 0 0 10px 0;
            }

            .editor {
                height: 400px;
                width: 300px;
                float: left;
            }

            #request-editor-wrapper {
            }

            #response-editor-wrapper {
            }

            #switch-view {
                float: right;
                margin: 0;
                padding: 0 5px 0 0;
            }

            #switch-view .ui-button-text, .main-buttons .ui-button .ui-button-text {
                line-height: 0px;
                padding: 10px;
                font-size: 80%;
                font-weight: bold;
                color: #000000;
            }

            #switch-view .switch-view {
                float: left;
                margin: 0;
                padding : 0;
                width: 84px;
                background : #E6E6E6;
            }

            .main-buttons {
                float: left;
                padding: 0 0 0 5px;
            }

            .main-buttons .invoke {
                width: 55px;
                background : #E6E6E6;
            }

            .editor-title {
                font-weight: bold;
                padding: 5px;
            }

            #service-info {
                float: left;
            }

            #loading-icon-wrapper {
                float: right;
                clear: right;
                display : none;
            }

            #loading-icon {
                height: 24px;
                width: 24px;
                background: url(<xsl:value-of select="$css-images"/>ajax-loader.gif) no-repeat 0 0;
            }

            #operation-name {
                height: 20px;
                float: left;
                padding: 4px 0 0 0;
                font-weight: bold;
            }

            #operation-description {
                clear: left;
                display: none;
                padding: 0 5px 10px 15px;
                text-align: justify;
            }

            .toggle-icon {
                width: 10px;
                height: 10px;
                float: left;
                margin: 3px 3px 0 0;
                overflow: hidden;
                cursor: pointer;
            }

            .title div.opened {
                background: url(<xsl:value-of select="$css-images"/>minus.png) no-repeat 1px 1px #fff;
            }

            .title div.closed {
                background: url(<xsl:value-of select="$css-images"/>plus.png) no-repeat 1px 1px #fff;
            }

            .toggle-link {
                float: left;
                height : 20px;
            }

            .top-info {
                float: left;
                margin: 0 0 5px 0;
            }

            .top-info .title {
                margin: 0;
                font-weight: bold;
            }

            .top-info .dynamic-content {
                margin: 0;
                padding: 5px 10px;
                clear: left;
                text-align: justify;
                display: none;
                background: #fff;
            }

            #service-info-wrapper {
                float: left;
                padding: 0;
            }

            #priority-operations {
                margin: 0 0 5px 0;
            }

            #all-operations {
                height: 200px;
            }

            .title {
                font-weight: bold;
                padding: 4px 0 0 5px;
                height: 20px;
            }

            .title-corners {
                background: #aaaaaa;
            }

            .dynamic-content {
                padding: 4px;
            }

            .dynamic-content-corners {
                border: solid #aaa;
                border-right-width: 1px;
                border-bottom-width: 1px;
                border-left-width: 1px;
                border-top: none;
                background: #eee;
            }

            .operation-wrapper {
                clear: left;
                height: 18px;
                padding: 2px 4px;
                font-weight: bold;
                position: relative;
                overflow: hidden;
            }

            body a {
                color: #000;
            }

            .active-operation-wrapper {
                background: #990000;
            }

            .active-operation-wrapper a {
                color: #fff;
            }

            .prior-icon-wrapper {
                height: 16px;
                width: 16px;
                cursor: pointer;
                top: 3px;
                right: 4px;
                position: absolute;
            }

            #all-operations .prior-icon-wrapper {
                background: url(<xsl:value-of select="$css-images"/>prior.png) no-repeat 1px 3px transparent;
            }

            #priority-operations .prior-icon-wrapper {
                background: url(<xsl:value-of select="$css-images"/>deprior.png) no-repeat 1px 3px transparent;
            }

            #priority-operations .dynamic-content {
                display: none;
            }

            .operation-name {
                height: 20px;
                overflow: hidden;
                float: left;
            }

            #username-wrapper, #password-wrapper {
                width: 300px;
                height: 19px;
                display: inline;
                padding: 4px 10px 4px 0;
            }

            #username, #password, #modified-endpoint, #endpoint-list {
                border: 1px solid #aaa;
                font: inherit;
            }

            #endpoint-selector, #endpoint-modifier {
                padding: 4px 0 0 0;
            }

            #endpoint-selector-title {
                font-weight: bold;
            }

            #modified-endpoint {
                width: 400px;
            }

            #tryit-messages-wrapper {
                float: left;
            }

            #tryit-messages {
                float: left;
                position: static;
            }

            .tryit-message {
                margin: 0 0 4px 0;
                padding: 4px 5px;
                float: left;
                clear: left;
                position: relative;
            }

            .error-message {
                background: url(<xsl:value-of select="$css-images"/>error.png) no-repeat 10px 5px #e9e9e9;
                color:#d47e00;
                padding-left:40px;
            }

            .warning-message {
            	background: url(<xsl:value-of select="$css-images"/>warrning.png) no-repeat 10px 5px #e9e9e9;
                color:#000;
                padding-left:40px;
                
            }

            .info-message {
                background: url(<xsl:value-of select="$css-images"/>info.png) no-repeat 10px 5px #e9e9e9;
                color:#000;
                padding-left:40px;
            }

            .message-text {
                padding: 0 40px 0 0;
                font-weight: bold;
            }

            .hide-button {
                position: absolute;
                right: 4px;
                top: 4px;
            }

            .hide-button a {
                font-weight: bold;
                color: #2d3d6c;
            }

            #all-operations .dynamic-content .operation-hover,
            #priority-operations .dynamic-content .operation-hover {
                background: #666666;
            }

            .operation-hover a {
                color: #fff;
            }

            #service-name-wrapper {
                height: 20px;
                padding: 0 0 5px 0;
                font-weight: bold;
                font-size: medium;
            }

            #all-operations .selected, #priority-operations .selected {
                background : #9babc0;
            }

            #all-operations .selected a, #priority-operations .selected a {
                color : #fff;
            }
        </style>
    </xsl:template>
</xsl:stylesheet>


