<!--
~ Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"%>
<%@ page import="org.wso2.carbon.idp.mgt.ui.bean.CertData" %>
<%@ page import="org.wso2.carbon.idp.mgt.ui.bean.TrustedIdPBean" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.wso2.carbon.idp.mgt.ui.util.IdentityProviderMgtUIUtil" %>
<%@ page import="org.wso2.carbon.idp.mgt.stub.dto.TrustedIdPDTO" %>

<carbon:breadcrumb label="identity.providers" resourceBundle="org.wso2.carbon.idp.mgt.ui.i18n.Resources"
                    topPage="true" request="<%=request%>" />
<jsp:include page="../dialog/display_messages.jsp"/>

<script type="text/javascript" src="../admin/js/main.js"></script>

<%
    String idPName = request.getParameter("idPName");
    if(idPName != null && idPName.equals("")){
        idPName = null;
    }
    boolean primary = false;
    String realmId = null;
    CertData certData = null;
    List<String> claims = null;
    Map<String,String> claimMappings = null;
    List<String> roles = null;
    Map<String,String> roleMappings = null;
    String tokenEndpointAlias = null;
    boolean isSAML2SSOEnabled = false;
    String idpEntityId = null;
    String spEntityId = null;
    String ssoUrl = null;
    boolean isAuthnRequestSigned = false;
    boolean isSLOEnabled = false;
    boolean isLogoutRequestSigned = false;
    String logoutUrl = null;
    boolean isAuthnResponseSigned = false;
    String authzUrl = null;
    boolean isOIDCEnabled = false;
    String tokenUrl = null;
    String clientId = null;
    String clientSecret = null;
    TrustedIdPBean bean = (TrustedIdPBean)session.getAttribute("trustedIdPBean");
    TrustedIdPDTO[] tenantIdPList = (TrustedIdPDTO[])session.getAttribute("tenantIdPList");
    if(tenantIdPList == null){
    %>
        <script type="text/javascript">
            location.href = "idp-mgt-list-load.jsp?callback=idp-mgt-edit.jsp";
        </script>
    <%
        return;
    }
    if(idPName != null && bean != null){
        idPName = bean.getIdPName();
        primary = bean.isPrimary();
        realmId = bean.getHomeRealmId();
        certData = bean.getCertData();
        claims = bean.getClaims();
        claimMappings = bean.getClaimMappings();
        roles = bean.getRoles();
        roleMappings = bean.getRoleMappings();
        tokenEndpointAlias = bean.getTokenEndpointAlias();
        isSAML2SSOEnabled = bean.isSAML2SSOEnabled();
        idpEntityId = bean.getIdpEntityId();
        spEntityId = bean.getSpEntityId();
        ssoUrl = bean.getSSOUrl();
        isAuthnRequestSigned = bean.isAuthnRequestSigned();
        isSLOEnabled = bean.isLogoutEnabled();
        logoutUrl = bean.getLogoutRequestUrl();
        isLogoutRequestSigned = bean.isLogoutRequestSigned();
        isAuthnResponseSigned = bean.isAuthnResponseSigned();
        isOIDCEnabled = bean.isOIDCEnabled();
        authzUrl = bean.getAuthzEndpointUrl();
        tokenUrl = bean.getTokenEndpointUrl();
        clientId = bean.getClientId();
        clientSecret = bean.getClientSecret();
    }
    if(idPName == null){
        idPName = "";
    }
    String primaryDisabled = "", primaryChecked = "";
    if(bean != null){
        if(primary){
            primaryChecked = "checked=\'checked\'";
            primaryDisabled = "disabled=\'disabled\'";
        }
    } else {
        if(tenantIdPList.length > 0){
            if(primary){
                primaryDisabled = "disabled=\'disabled\'";
                primaryChecked = "checked=\'checked\'";
            }
        } else {
            primaryDisabled = "disabled=\'disabled\'";
            primaryChecked = "checked=\'checked\'";
        }
    }
    if(realmId == null){
        realmId = "";
    }
    if(tokenEndpointAlias == null){
        tokenEndpointAlias = IdentityProviderMgtUIUtil.getOAuth2TokenEPURL(request);;
    }
    String saml2SSOEnabledChecked = "";
    if(bean != null){
        if(isSAML2SSOEnabled){
            saml2SSOEnabledChecked = "checked=\'checked\'";
        }
    }
    if(idpEntityId == null){
        idpEntityId = "";
    }
    if(spEntityId == null){
        spEntityId = "";
    }
    if(ssoUrl == null) {
        ssoUrl = IdentityProviderMgtUIUtil.getSAML2SSOUrl(request);
    }
    String authnRequestSignedChecked = "";
    if(bean != null){
        if(isAuthnRequestSigned){
            authnRequestSignedChecked = "checked=\'checked\'";
        }
    }
    String sloEnabledChecked = "";
    if(bean != null){
        if(isSLOEnabled){
            sloEnabledChecked = "checked=\'checked\'";
        }
    }
    if(logoutUrl == null) {
        logoutUrl = "";
    }
    String logoutRequestSignedChecked = "";
    if(bean != null){
        if(isLogoutRequestSigned){
            logoutRequestSignedChecked = "checked=\'checked\'";
        }
    }
    String authnResponseSignedChecked = "";
    if(bean != null){
        if(isAuthnResponseSigned){
            authnResponseSignedChecked = "checked=\'checked\'";
        }
    }
    String oidcEnabledChecked = "";
    if(bean != null){
        if(isOIDCEnabled){
            oidcEnabledChecked = "checked=\'checked\'";
        }
    }
    if(authzUrl == null){
        authzUrl = IdentityProviderMgtUIUtil.getOAuth2AuthzEPURL(request);
    }
    if(tokenUrl == null){
        tokenUrl = IdentityProviderMgtUIUtil.getOAuth2TokenEPURL(request);
    }
    if(clientId == null){
        clientId = "";
    }
    if(clientSecret == null){
        clientSecret = "";
    }
%>

<script>
    <% if(claims != null){ %>
    var claimRowId = <%=claims.size()-1%>;
    <% } else { %>
    var claimRowId = -1;
    <% } %>
    <% if(roles != null){ %>
        var roleRowId = <%=roles.size()-1%>;
    <% } else { %>
        var roleRowId = -1;
    <% } %>
    jQuery(document).ready(function(){
        jQuery('#saml2SSOLinkRow').hide();
        jQuery('#oauth2LinkRow').hide();
        jQuery('h2.trigger').click(function(){
            if (jQuery(this).next().is(":visible")) {
                this.className = "active trigger";
            } else {
                this.className = "trigger";
            }
            jQuery(this).next().slideToggle("fast");
            return false; //Prevent the browser jump to the link anchor
        })
        jQuery('#publicCertDeleteLink').click(function(){
            $(jQuery('#publicCertDiv')).toggle();
            var input = document.createElement('input');
            input.type = "hidden";
            input.name = "deletePublicCert";
            input.id = "deletePublicCert";
            input.value = "true";
            document.forms['idp-mgt-edit-form'].appendChild(input);
        })
        jQuery('#claimAddLink').click(function(){
            claimRowId++;
            jQuery('#claimAddTable').append(jQuery('<tr><td><input type="text" id="claimrowid_'+claimRowId+'" name="claimrowname_'+claimRowId+'"/></td>' +
                    '<td><a onclick="deleteClaimRow(this)" class="icon-link" '+
                    'style="background-image: url(images/delete.gif)">'+
                    'Delete'+
                    '</a></td></tr>'));
            if($(jQuery('#claimAddTable tr')).length == 2){
                $(jQuery('#claimAddTable')).toggle();
            }
        })
        jQuery('#claimMappingDeleteLink').click(function(){
            $(jQuery('#claimMappingDiv')).toggle();
            var input = document.createElement('input');
            input.type = "hidden";
            input.name = "deleteClaimMappings";
            input.id = "deleteClaimMappings";
            input.value = "true";
            document.forms['idp-mgt-edit-form'].appendChild(input);
        })
        jQuery('#roleAddLink').click(function(){
            roleRowId++;
            jQuery('#roleAddTable').append(jQuery('<tr><td><input type="text" id="rolerowid_'+roleRowId+'" name="rolerowname_'+roleRowId+'"/></td>' +
                    '<td><a onclick="deleteRoleRow(this)" class="icon-link" '+
                    'style="background-image: url(images/delete.gif)">'+
                    'Delete'+
                    '</a></td></tr>'));
            if($(jQuery('#roleAddTable tr')).length == 2){
                $(jQuery('#roleAddTable')).toggle();
            }
        })
        jQuery('#roleMappingDeleteLink').click(function(){
            $(jQuery('#roleMappingDiv')).toggle();
            var input = document.createElement('input');
            input.type = "hidden";
            input.name = "deleteRoleMappings";
            input.id = "deleteRoleMappings";
            input.value = "true";
            document.forms['idp-mgt-edit-form'].appendChild(input);
        })
    })
    var deleteClaimRows = [];
    function deleteClaimRow(obj){
        if(jQuery(obj).parent().prev().children()[0].value != ''){
            deleteClaimRows.push(jQuery(obj).parent().prev().children()[0].value);
        }
        jQuery(obj).parent().parent().remove();
        if($(jQuery('#claimAddTable tr')).length == 1){
            $(jQuery('#claimAddTable')).toggle();
        }
    }
    var deletedRoleRows = [];
    function deleteRoleRow(obj){
       if(jQuery(obj).parent().prev().children()[0].value != ''){
            deletedRoleRows.push(jQuery(obj).parent().prev().children()[0].value);
        }
        jQuery(obj).parent().parent().remove();
        if($(jQuery('#roleAddTable tr')).length == 1){
            $(jQuery('#roleAddTable')).toggle();
        }
    }
    function idpMgtUpdate(){
        if(doValidation()){
            var allDeletedClaimStr = "";
            for(var i = 0;i<deleteClaimRows.length;i++){
                if(i < deleteClaimRows.length-1){
                    allDeletedClaimStr += deleteClaimRows[i] + ", ";
                } else {
                    allDeletedClaimStr += deleteClaimRows[i] + "?";
                }
            }
            var allDeletedRoleStr = "";
            for(var i = 0;i<deletedRoleRows.length;i++){
                if(i < deletedRoleRows.length-1){
                    allDeletedRoleStr += deletedRoleRows[i] + ", ";
                } else {
                    allDeletedRoleStr += deletedRoleRows[i] + "?";
                }
            }

            if(jQuery('#deletePublicCert').val() == 'true'){
                var confirmationMessage = 'Are you sure you want to delete the public certificate of ' +
                        jQuery('#idPName').val() + '?';
                if(jQuery('#certFile').val() != ''){
                    confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                }
                CARBON.showConfirmationDialog(confirmationMessage,
                        function (){
                            if(allDeletedClaimStr != "") {
                                CARBON.showConfirmationDialog('Are you sure you want to delete the claim URI(s) ' +
                                        allDeletedClaimStr,
                                        function(){
                                            if(allDeletedRoleStr != "") {
                                                CARBON.showConfirmationDialog('Are you sure you want to delete the ' +
                                                        'role(s) ' + allDeletedRoleStr,
                                                        function(){
                                                            if(jQuery('#deleteClaimMappings').val() == 'true'){
                                                                var confirmationMessage = 'Are you sure you want to ' +
                                                                        'delete the Claim URI Mappings of ' +
                                                                        jQuery('#idPName').val() + '?';
                                                                if(jQuery('#claimMappingFile').val() != ''){
                                                                    confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                                }
                                                                CARBON.showConfirmationDialog(confirmationMessage,
                                                                        function(){
                                                                            if(jQuery('#deleteRoleMappings').val() == 'true'){
                                                                                var confirmationMessage = 'Are you sure you want to ' +
                                                                                        'delete the Role Mappings of ' +
                                                                                        jQuery('#idPName').val() + '?';
                                                                                if(jQuery('#roleMappingFile').val() != ''){
                                                                                    confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                                                }
                                                                                CARBON.showConfirmationDialog(confirmationMessage,
                                                                                        function(){
                                                                                            doEditFinish();
                                                                                        },
                                                                                        function(){
                                                                                            location.href = "idp-mgt-edit.jsp?idPName=<%=idPName%>";
                                                                                        });
                                                                            } else {
                                                                                doEditFinish();
                                                                            }
                                                                        },
                                                                        function(){
                                                                            location.href = "idp-mgt-edit.jsp?idPName=<%=idPName%>";
                                                                        });
                                                            } else {
                                                                if(jQuery('#deleteRoleMappings').val() == 'true'){
                                                                    var confirmationMessage = 'Are you sure you want to ' +
                                                                            'delete the Role Mappings of ' +
                                                                            jQuery('#idPName').val() + '?';
                                                                    if(jQuery('#roleMappingFile').val() != ''){
                                                                        confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                                    }
                                                                    CARBON.showConfirmationDialog(confirmationMessage,
                                                                            function(){
                                                                                doEditFinish();
                                                                            },
                                                                            function(){
                                                                                location.href = "idp-mgt-edit.jsp?idPName=<%=idPName%>";
                                                                            });
                                                                } else {
                                                                    doEditFinish();
                                                                }
                                                            }
                                                        },
                                                        function(){
                                                            location.href = "idp-mgt-edit.jsp?idPName=<%=idPName%>";
                                                        });
                                            } else {
                                                if(jQuery('#deleteClaimMappings').val() == 'true'){
                                                    var confirmationMessage = 'Are you sure you want to ' +
                                                            'delete the Claim URI mappings of ' +
                                                            jQuery('#idPName').val() + '?';
                                                    if(jQuery('#claimMappingFile').val() != ''){
                                                        confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                    }
                                                    CARBON.showConfirmationDialog(confirmationMessage,
                                                            function(){

                                                            },
                                                            function(){
                                                                location.href = "idp-mgt-edit.jsp?idPName=<%=idPName%>";
                                                            });
                                                } else {
                                                    if(jQuery('#deleteRoleMappings').val() == 'true'){
                                                        var confirmationMessage = 'Are you sure you want to ' +
                                                                'delete the Role Mappings of ' +
                                                                jQuery('#idPName').val() + '?';
                                                        if(jQuery('#roleMappingFile').val() != ''){
                                                            confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                        }
                                                        CARBON.showConfirmationDialog(confirmationMessage,
                                                                function(){
                                                                    doEditFinish();
                                                                },
                                                                function(){
                                                                    location.href = "idp-mgt-edit.jsp?idPName=<%=idPName%>";
                                                                });
                                                    } else {
                                                        doEditFinish();
                                                    }
                                                }
                                            }
                                        },
                                        function(){
                                            location.href = "idp-mgt-edit.jsp?idPName=<%=idPName%>";
                                        });
                            } else {
                                if(allDeletedRoleStr != "") {
                                    CARBON.showConfirmationDialog('Are you sure you want to delete the ' +
                                            'role(s) ' + allDeletedRoleStr,
                                            function(){
                                                if(jQuery('#deleteClaimMappings').val() == 'true'){
                                                    var confirmationMessage = 'Are you sure you want to ' +
                                                            'delete the Claim URI mappings of ' +
                                                            jQuery('#idPName').val() + '?';
                                                    if(jQuery('#claimMappingFile').val() != ''){
                                                        confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                    }
                                                    CARBON.showConfirmationDialog(confirmationMessage,
                                                            function(){

                                                            },
                                                            function(){
                                                                location.href = "idp-mgt-edit.jsp?idPName=<%=idPName%>";
                                                            });
                                                } else {
                                                    if(jQuery('#deleteRoleMappings').val() == 'true'){
                                                        var confirmationMessage = 'Are you sure you want to ' +
                                                                'delete the Role Mappings of ' +
                                                                jQuery('#idPName').val() + '?';
                                                        if(jQuery('#roleMappingFile').val() != ''){
                                                            confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                        }
                                                        CARBON.showConfirmationDialog(confirmationMessage,
                                                                function(){
                                                                    doEditFinish();
                                                                },
                                                                function(){
                                                                    location.href = "idp-mgt-edit.jsp?idPName=<%=idPName%>";
                                                                });
                                                    } else {
                                                        doEditFinish();
                                                    }
                                                }
                                            },
                                            function(){
                                                location.href = "idp-mgt-edit.jsp?idPName=<%=idPName%>";
                                            });
                                } else {
                                    if(jQuery('#deleteClaimMappings').val() == 'true'){
                                        var confirmationMessage = 'Are you sure you want to ' +
                                                'delete the Claim URI mappings of ' +
                                                jQuery('#idPName').val() + '?';
                                        if(jQuery('#claimMappingFile').val() != ''){
                                            confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                        }
                                        CARBON.showConfirmationDialog(confirmationMessage,
                                                function(){
                                                    if(jQuery('#deleteRoleMappings').val() == 'true'){
                                                        var confirmationMessage = 'Are you sure you want to ' +
                                                                'delete the Role Mappings of ' +
                                                                jQuery('#idPName').val() + '?';
                                                        if(jQuery('#roleMappingFile').val() != ''){
                                                            confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                        }
                                                        CARBON.showConfirmationDialog(confirmationMessage,
                                                                function(){
                                                                    doEditFinish();
                                                                },
                                                                function(){
                                                                    location.href = "idp-mgt-edit.jsp?idPName=<%=idPName%>";
                                                                });
                                                    } else {
                                                        doEditFinish();
                                                    }
                                                },
                                                function(){
                                                    location.href = "idp-mgt-edit.jsp?idPName=<%=idPName%>";
                                                });
                                    } else {
                                        if(jQuery('#deleteRoleMappings').val() == 'true'){
                                            var confirmationMessage = 'Are you sure you want to ' +
                                                    'delete the Role Mappings of ' +
                                                    jQuery('#idPName').val() + '?';
                                            if(jQuery('#roleMappingFile').val() != ''){
                                                confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                            }
                                            CARBON.showConfirmationDialog(confirmationMessage,
                                                    function(){
                                                        doEditFinish();
                                                    },
                                                    function(){
                                                        location.href = "idp-mgt-edit.jsp?idPName=<%=idPName%>";
                                                    });
                                        } else {
                                            doEditFinish();
                                        }
                                    }
                                }
                            }
                        },
                        function(){
                            location.href = "idp-mgt-edit.jsp?idPName=<%=idPName%>";
                        });
            } else {
                if(allDeletedClaimStr != "") {
                    CARBON.showConfirmationDialog('Are you sure you want to delete the claim URI(s) ' +
                            allDeletedClaimStr,
                            function(){
                                if(allDeletedRoleStr != "") {
                                    CARBON.showConfirmationDialog('Are you sure you want to delete the ' +
                                            'role(s) ' + allDeletedRoleStr,
                                            function(){
                                                if(jQuery('#deleteClaimMappings').val() == 'true'){
                                                    var confirmationMessage = 'Are you sure you want to ' +
                                                            'delete the Claim URI mappings of ' +
                                                            jQuery('#idPName').val() + '?';
                                                    if(jQuery('#claimMappingFile').val() != ''){
                                                        confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                    }
                                                    CARBON.showConfirmationDialog(confirmationMessage,
                                                            function(){

                                                            },
                                                            function(){
                                                                location.href = "idp-mgt-edit.jsp?idPName=<%=idPName%>";
                                                            });
                                                } else {
                                                    if(jQuery('#deleteRoleMappings').val() == 'true'){
                                                        var confirmationMessage = 'Are you sure you want to ' +
                                                                'delete the Role Mappings of ' +
                                                                jQuery('#idPName').val() + '?';
                                                        if(jQuery('#roleMappingFile').val() != ''){
                                                            confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                        }
                                                        CARBON.showConfirmationDialog(confirmationMessage,
                                                                function(){
                                                                    doEditFinish();
                                                                },
                                                                function(){
                                                                    location.href = "idp-mgt-edit.jsp?idPName=<%=idPName%>";
                                                                });
                                                    } else {
                                                        doEditFinish();
                                                    }
                                                }
                                            },
                                            function(){
                                                location.href = "idp-mgt-edit.jsp?idPName=<%=idPName%>";
                                            });
                                } else {
                                    if(jQuery('#deleteClaimMappings').val() == 'true'){
                                        var confirmationMessage = 'Are you sure you want to ' +
                                                'delete the Claim URI mappings of ' +
                                                jQuery('#idPName').val() + '?';
                                        if(jQuery('#claimMappingFile').val() != ''){
                                            confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                        }
                                        CARBON.showConfirmationDialog(confirmationMessage,
                                                function(){
                                                    if(jQuery('#deleteRoleMappings').val() == 'true'){
                                                        var confirmationMessage = 'Are you sure you want to ' +
                                                                'delete the Role Mappings of ' +
                                                                jQuery('#idPName').val() + '?';
                                                        if(jQuery('#roleMappingFile').val() != ''){
                                                            confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                        }
                                                        CARBON.showConfirmationDialog(confirmationMessage,
                                                                function(){
                                                                    doEditFinish();
                                                                },
                                                                function(){
                                                                    location.href = "idp-mgt-edit.jsp?idPName=<%=idPName%>";
                                                                });
                                                    } else {
                                                        doEditFinish();
                                                    }
                                                },
                                                function(){
                                                    location.href = "idp-mgt-edit.jsp?idPName=<%=idPName%>";
                                                });
                                    } else {
                                        if(jQuery('#deleteRoleMappings').val() == 'true'){
                                            var confirmationMessage = 'Are you sure you want to ' +
                                                    'delete the Role Mappings of ' +
                                                    jQuery('#idPName').val() + '?';
                                            if(jQuery('#roleMappingFile').val() != ''){
                                                confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                            }
                                            CARBON.showConfirmationDialog(confirmationMessage,
                                                    function(){
                                                        doEditFinish();
                                                    },
                                                    function(){
                                                        location.href = "idp-mgt-edit.jsp?idPName=<%=idPName%>";
                                                    });
                                        } else {
                                            doEditFinish();
                                        }
                                    }
                                }
                            },
                            function(){
                                location.href = "idp-mgt-edit.jsp?idPName=<%=idPName%>";
                            });
                } else {
                    if(allDeletedRoleStr != "") {
                        CARBON.showConfirmationDialog('Are you sure you want to delete the ' +
                                'role(s) ' + allDeletedRoleStr,
                                function(){
                                    if(jQuery('#deleteClaimMappings').val() == 'true'){
                                        var confirmationMessage = 'Are you sure you want to ' +
                                                'delete the Claim URI mappings of ' +
                                                jQuery('#idPName').val() + '?';
                                        if(jQuery('#claimMappingFile').val() != ''){
                                            confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                        }
                                        CARBON.showConfirmationDialog(confirmationMessage,
                                                function(){
                                                    if(jQuery('#deleteRoleMappings').val() == 'true'){
                                                        var confirmationMessage = 'Are you sure you want to ' +
                                                                'delete the Role Mappings of ' +
                                                                jQuery('#idPName').val() + '?';
                                                        if(jQuery('#roleMappingFile').val() != ''){
                                                            confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                        }
                                                        CARBON.showConfirmationDialog(confirmationMessage,
                                                                function(){
                                                                    doEditFinish();
                                                                },
                                                                function(){
                                                                    location.href = "idp-mgt-edit.jsp?idPName=<%=idPName%>";
                                                                });
                                                    } else {
                                                        doEditFinish();
                                                    }
                                                },
                                                function(){
                                                    location.href = "idp-mgt-edit.jsp?idPName=<%=idPName%>";
                                                });
                                    } else {
                                        if(jQuery('#deleteRoleMappings').val() == 'true'){
                                            var confirmationMessage = 'Are you sure you want to ' +
                                                    'delete the Role Mappings of ' +
                                                    jQuery('#idPName').val() + '?';
                                            if(jQuery('#roleMappingFile').val() != ''){
                                                confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                            }
                                            CARBON.showConfirmationDialog(confirmationMessage,
                                                    function(){
                                                        doEditFinish();
                                                    },
                                                    function(){
                                                        location.href = "idp-mgt-edit.jsp?idPName=<%=idPName%>";
                                                    });
                                        } else {
                                            doEditFinish();
                                        }
                                    }
                                },
                                function(){
                                    location.href = "idp-mgt-edit.jsp?idPName=<%=idPName%>";
                                });
                    } else {
                        if(jQuery('#deleteClaimMappings').val() == 'true'){
                            var confirmationMessage = 'Are you sure you want to ' +
                                    'delete the Claim URI mappings of ' +
                                    jQuery('#idPName').val() + '?';
                            if(jQuery('#claimMappingFile').val() != ''){
                                confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                            }
                            CARBON.showConfirmationDialog(confirmationMessage,
                                    function(){
                                        if(jQuery('#deleteRoleMappings').val() == 'true'){
                                            var confirmationMessage = 'Are you sure you want to ' +
                                                    'delete the Role Mappings of ' +
                                                    jQuery('#idPName').val() + '?';
                                            if(jQuery('#roleMappingFile').val() != ''){
                                                confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                            }
                                            CARBON.showConfirmationDialog(confirmationMessage,
                                                    function(){
                                                        doEditFinish();
                                                    },
                                                    function(){
                                                        location.href = "idp-mgt-edit.jsp?idPName=<%=idPName%>";
                                                    });
                                        } else {
                                            doEditFinish();
                                        }
                                    },
                                    function(){
                                        location.href = "idp-mgt-edit.jsp?idPName=<%=idPName%>";
                                    });
                        } else {
                            if(jQuery('#deleteRoleMappings').val() == 'true'){
                                var confirmationMessage = 'Are you sure you want to ' +
                                        'delete the Role Mappings of ' +
                                        jQuery('#idPName').val() + '?';
                                if(jQuery('#roleMappingFile').val() != ''){
                                    confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                }
                                CARBON.showConfirmationDialog(confirmationMessage,
                                        function(){
                                            doEditFinish();
                                        },
                                        function(){
                                            location.href = "idp-mgt-edit.jsp?idPName=<%=idPName%>";
                                        });
                            } else {
                                doEditFinish();
                            }
                        }
                    }
                }
            }
        }
    }
    function doEditFinish(){
        jQuery('#primary').removeAttr('disabled');
        <% if(idPName == null || idPName.equals("")){ %>
        jQuery('#idp-mgt-edit-form').attr('action','idp-mgt-add-finish.jsp');
        <% } %>
        jQuery('#idp-mgt-edit-form').submit();
    }
    function idpMgtCancel(){
        location.href = "idp-mgt-list.jsp"
    }
    function doValidation() {
        var reason = "";
        reason = validateEmpty("idPName");
        if (reason != "") {
            CARBON.showWarningDialog("Name of IdP cannot be empty");
            return false;
        }
        for(var i=0; i <= claimRowId; i++){
            if(document.getElementsByName('claimrowname_'+i)[0] != null){
                reason = validateEmpty('claimrowname_'+i);
                if(reason != ""){
                    CARBON.showWarningDialog("Claim URI strings cannot be of zero length");
                    return false;
                }
            }
        }
        for(var i=0; i <= roleRowId; i++){
            if(document.getElementsByName('rolerowname_'+i)[0] != null){
                reason = validateEmpty('rolerowname_'+i);
                if(reason != ""){
                    CARBON.showWarningDialog("Role name strings cannot be of zero length");
                    return false;
                }
            }
        }
        return true;
    }
</script>

<fmt:bundle basename="org.wso2.carbon.idp.mgt.ui.i18n.Resources">
    <div id="middle">
        <h2>
            <fmt:message key='identity.providers'/>
        </h2>
        <div id="workArea">
            <form id="idp-mgt-edit-form" name="idp-mgt-edit-form" method="post" action="idp-mgt-edit-finish.jsp" enctype="multipart/form-data" >
            <div class="sectionSeperator togglebleTitle"><fmt:message key='identity.provider.info'/></div>
            <div class="sectionSub">
                <table class="carbonFormTable">
                    <tr>
                        <td class="leftCol-med labelField"><fmt:message key='name'/>:<span class="required">*</span></td>
                        <td>
                            <input id="idPName" name="idPName" type="text" value="<%=idPName%>" autofocus/>
                            <div class="sectionHelp">
                                <fmt:message key='name.help'/>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med labelField">
                            <label for="primary"><fmt:message key='primary'/></label>
                        </td>
                        <td>
                            <div class="sectionCheckbox">
                                <input id="primary" name="primary" type="checkbox" <%=primaryDisabled%> <%=primaryChecked%>/>
                                <div class="sectionHelp">
                                    <fmt:message key='primary.help'/>
                                </div>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med labelField"><fmt:message key='home.realm.id'/>:</td>
                        <td>
                            <input id="realmId" name="realmId" type="text" value="<%=realmId%>" autofocus/>
                            <div class="sectionHelp">
                                <fmt:message key='home.realm.id.help'/>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med labelField"><fmt:message key='certificate'/>:</td>
                        <td>
                            <input id="certFile" name="certFile" type="file" />
                            <div class="sectionHelp">
                                <fmt:message key='certificate.help'/>
                            </div>
                            <div id="publicCertDiv">
                                <% if(certData != null) { %>
                                <a id="publicCertDeleteLink" class="icon-link" style="background-image:url(images/delete.gif);"><fmt:message key='public.cert.delete'/></a>
                                    <table class="styledLeft">
                                        <thead><tr><th><fmt:message key='issuerdn'/></th>
                                            <th><fmt:message key='subjectdn'/></th>
                                            <th><fmt:message key='notafter'/></th>
                                            <th><fmt:message key='notbefore'/></th>
                                            <th><fmt:message key='serialno'/></th>
                                            <th><fmt:message key='version'/></th>
                                        </tr></thead>
                                        <tbody>
                                            <tr><td><%=certData.getIssuerDN()%></td>
                                                <td><%=certData.getSubjectDN()%></td>
                                                <td><%=certData.getNotAfter()%></td>
                                                <td><%=certData.getNotBefore()%></td>
                                                <td><%=certData.getSerialNumber()%></td>
                                                <td><%=certData.getVersion()%></td>
                                            </tr>
                                        </tbody>
                                    </table>
                                <% } %>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med labelField"><fmt:message key='claims'/>:</td>
                        <td>
                            <a id="claimAddLink" class="icon-link" style="background-image:url(images/add.gif);"><fmt:message key='add.claim'/></a>
                            <div style="clear:both"/>
                            <div class="sectionHelp">
                                <fmt:message key='claims.help'/>
                            </div>
                            <table class="styledLeft" id="claimAddTable" style="display:none">
                                <thead><tr><th class="leftCol-big"><fmt:message key='idp.claim'/></th><th><fmt:message key='actions'/></th></tr></thead>
                                <tbody>
                                <% if(claims != null && !claims.isEmpty()){ %>
                                <script>
                                    $(jQuery('#claimAddTable')).toggle();
                                </script>
                                <% for(int i = 0; i < claims.size(); i++){ %>
                                <tr>
                                    <td><input type="text" value="<%=claims.get(i)%>" id="claimrowid_<%=i%>" name="claimrowname_<%=i%>"/></td>
                                    <td>
                                        <a title="<fmt:message key='delete.claim'/>"
                                           onclick="deleteClaimRow(this);return false;"
                                           href="#"
                                           class="icon-link"
                                           style="background-image: url(images/delete.gif)">
                                            <fmt:message key='delete'/>
                                        </a>
                                    </td>
                                </tr>
                                <% } %>
                                <% } %>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med labelField"><fmt:message key='claim.mappings'/>:</td>
                        <td>
                            <input id="cliamMappingFile" name="claimMappingFile" type="file" />
                            <div class="sectionHelp">
                                <fmt:message key='claim.mappings.help'/>
                            </div>
                            <% if(claimMappings != null && !claimMappings.isEmpty()){ %>
                            <div id="claimMappingDiv">
                                <a id="claimMappingDeleteLink" class="icon-link" style="background-image:url(images/delete.gif);"><fmt:message key='claim.mapping.delete'/></a>
                                <table class="styledLeft">
                                    <thead><tr><th class="leftCol-big"><fmt:message key='idp.claim'/></th><th><fmt:message key='tenant.claim'/></th></tr></thead>
                                    <tbody>
                                    <% for(Map.Entry<String,String> entry:claimMappings.entrySet()){ %>
                                    <tr><td><%=entry.getKey()%></td><td><%=entry.getValue()%></td></tr>
                                    <% } %>
                                    </tbody>
                                </table>
                            </div>
                            <% } %>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med labelField"><fmt:message key='roles'/>:</td>
                        <td>
                            <a id="roleAddLink" class="icon-link" style="background-image:url(images/add.gif);"><fmt:message key='add.role'/></a>
                            <div style="clear:both"/>
                            <div class="sectionHelp">
                                <fmt:message key='roles.help'/>
                            </div>
                            <table class="styledLeft" id="roleAddTable" style="display:none">
                                <thead><tr><th class="leftCol-big"><fmt:message key='idp.role'/></th><th><fmt:message key='actions'/></th></tr></thead>
                                <tbody>
                                <% if(roles != null && !roles.isEmpty()){ %>
                                    <script>
                                        $(jQuery('#roleAddTable')).toggle();
                                    </script>
                                    <% for(int i = 0; i < roles.size(); i++){ %>
                                        <tr>
                                            <td><input type="text" value="<%=roles.get(i)%>" id="rolerowid_<%=i%>" name="rolerowname_<%=i%>"/></td>
                                            <td>
                                                <a title="<fmt:message key='delete.role'/>"
                                                   onclick="deleteRoleRow(this);return false;"
                                                   href="#"
                                                   class="icon-link"
                                                   style="background-image: url(images/delete.gif)">
                                                    <fmt:message key='delete'/>
                                                </a>
                                            </td>
                                        </tr>
                                    <% } %>
                                <% } %>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med labelField"><fmt:message key='role.mappings'/>:</td>
                        <td>
                            <input id="roleMappingFile" name="roleMappingFile" type="file" />
                            <div class="sectionHelp">
                                <fmt:message key='role.mappings.help'/>
                            </div>
                            <% if(roleMappings != null && !roleMappings.isEmpty()){ %>
                                <div id="roleMappingDiv">
                                    <a id="roleMappingDeleteLink" class="icon-link" style="background-image:url(images/delete.gif);"><fmt:message key='role.mapping.delete'/></a>
                                    <table class="styledLeft">
                                        <thead><tr><th class="leftCol-big"><fmt:message key='idp.role'/></th><th><fmt:message key='tenant.role'/></th></tr></thead>
                                        <tbody>
                                            <% for(Map.Entry<String,String> entry:roleMappings.entrySet()){ %>
                                                <tr><td><%=entry.getKey()%></td><td><%=entry.getValue()%></td></tr>
                                            <% } %>
                                        </tbody>
                                    </table>
                                </div>
                            <% } %>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med labelField"><fmt:message key='token.endpoint.alias'/>:</td>
                        <td>
                            <input id="tokenEndpointAlias" name="tokenEndpointAlias" type="text" value="<%=tokenEndpointAlias%>" autofocus/>
                            <div class="sectionHelp">
                                <fmt:message key='token.endpoint.alias.help'/>
                            </div>
                        </td>
                    </tr>
                </table>
            </div>

            <h2 id="saml2_sso_head"  class="sectionSeperator trigger active">
                <a href="#"><fmt:message key="saml2.web.sso.config"/></a>
            </h2>
            <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="saml2SSOLinkRow">
                <table class="carbonFormTable">
                    <tr>
                        <td class="leftCol-med labelField">
                            <label for="saml2SSOEnabled"><fmt:message key='saml2.sso.enabled'/></label>
                        </td>
                        <td>
                            <div class="sectionCheckbox">
                                <input id="saml2SSOEnabled" name="saml2SSOEnabled" type="checkbox" <%=saml2SSOEnabledChecked%>/>
                                <div class="sectionHelp">
                                    <fmt:message key='saml2.sso.enabled.help'/>
                                </div>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med labelField"><fmt:message key='idp.entity.id'/>:</td>
                        <td>
                            <input id="idpEntityId" name="idpEntityId" type="text" value="<%=idpEntityId%>"/>
                            <div class="sectionHelp">
                                <fmt:message key='idp.entity.id.help'/>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med labelField"><fmt:message key='sp.entity.id'/>:</td>
                        <td>
                            <input id="spEntityId" name="spEntityId" type="text" value="<%=spEntityId%>"/>
                            <div class="sectionHelp">
                                <fmt:message key='sp.entity.id.help'/>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med labelField"><fmt:message key='sso.url'/>:</td>
                        <td>
                            <input id="ssoUrl" name="ssoUrl" type="text" value="<%=ssoUrl%>"/>
                            <div class="sectionHelp">
                                <fmt:message key='sso.url.help'/>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med labelField">
                            <label for="authnRequestSigned"><fmt:message key='authn.request.signed'/></label>
                        </td>
                        <td>
                            <div class="sectionCheckbox">
                                <input id="authnRequestSigned" name="authnRequestSigned" type="checkbox" <%=authnRequestSignedChecked%>/>
                                <div class="sectionHelp">
                                    <fmt:message key='authn.request.signed.help'/>
                                </div>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med labelField">
                            <label for="sloEnabled"><fmt:message key='logout.enabled'/></label>
                        </td>
                        <td>
                            <div class="sectionCheckbox">
                                <input id="sloEnabled" name="sloEnabled" type="checkbox" <%=sloEnabledChecked%>/>
                                <div class="sectionHelp">
                                    <fmt:message key='logout.enabled.help'/>
                                </div>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med labelField"><fmt:message key='logout.url'/>:</td>
                        <td>
                            <input id="logoutUrl" name="logoutUrl" type="text" value="<%=logoutUrl%>"/>
                            <div class="sectionHelp">
                                <fmt:message key='logout.url.help'/>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med labelField">
                            <label for="logoutRequestSigned"><fmt:message key='logout.request.signed'/></label>
                        </td>
                        <td>
                            <div class="sectionCheckbox">
                                <input id="logoutRequestSigned" name="logoutRequestSigned" type="checkbox" <%=logoutRequestSignedChecked%>/>
                                <div class="sectionHelp">
                                    <fmt:message key='logout.request.signed.help'/>
                                </div>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med labelField">
                            <label for="authnResponseSigned"><fmt:message key='authn.response.signed'/></label>
                        </td>
                        <td>
                            <div class="sectionCheckbox">
                                <input id="authnResponseSigned" name="authnResponseSigned" type="checkbox" <%=authnResponseSignedChecked%>/>
                                <div class="sectionHelp">
                                    <fmt:message key='authn.response.signed.help'/>
                                </div>
                            </div>
                        </td>
                    </tr>
                </table>
            </div>

            <h2 id="oauth2_head" class="sectionSeperator trigger active">
                <a href="#"><fmt:message key="oidc.config"/></a>
            </h2>
            <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="oauth2LinkRow">
                <table class="carbonFormTable">
                    <tr>
                        <td class="leftCol-med labelField">
                            <label for="oidcEnabled"><fmt:message key='oidc.enabled'/></label>
                        </td>
                        <td>
                            <div class="sectionCheckbox">
                                <input id="oidcEnabled" name="oidcEnabled" type="checkbox" <%=oidcEnabledChecked%>/>
                                <div class="sectionHelp">
                                    <fmt:message key='oidc.enabled.help'/>
                                </div>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med labelField"><fmt:message key='authz.endpoint'/>:</td>
                        <td>
                            <input id="authzUrl" name="authzUrl" type="text" value="<%=authzUrl%>"/>
                            <div class="sectionHelp">
                                <fmt:message key='authz.endpoint.help'/>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med labelField"><fmt:message key='token.endpoint'/>:</td>
                        <td>
                            <input id="tokenUrl" name="tokenUrl" type="text" value="<%=tokenUrl%>"/>
                            <div class="sectionHelp">
                                <fmt:message key='token.endpoint.help'/>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med labelField"><fmt:message key='client.id'/>:</td>
                        <td>
                            <input id="clientId" name="clientId" type="text" value="<%=clientId%>"/>
                            <div class="sectionHelp">
                                <fmt:message key='client.id.help'/>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med labelField"><fmt:message key='client.secret'/>:</td>
                        <td>
                            <input id="clientSecret" name="clientSecret" type="text" value="<%=clientSecret%>"/>
                            <div class="sectionHelp">
                                <fmt:message key='client.secret.help'/>
                            </div>
                        </td>
                    </tr>
                </table>
            </div>

            <!-- sectionSub Div -->
            <div class="buttonRow">
                <% if(bean != null){ %>
                    <input type="button" value="<fmt:message key='update'/>" onclick="idpMgtUpdate();"/>
                <% } else { %>
                    <input type="button" value="<fmt:message key='register'/>" onclick="idpMgtUpdate();"/>
                <% } %>
                <input type="button" value="<fmt:message key='cancel'/>" onclick="idpMgtCancel();"/>
            </div>
            </form>
        </div>
    </div>

</fmt:bundle>