/*
* Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.idp.mgt.ui.util;

import org.apache.axiom.om.util.Base64;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.wso2.carbon.idp.mgt.stub.dto.TrustedIdPDTO;
import org.wso2.carbon.idp.mgt.ui.bean.CertData;
import org.wso2.carbon.idp.mgt.ui.bean.TrustedIdPBean;
import org.wso2.carbon.ui.CarbonUIUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdentityProviderMgtUIUtil {

    public static boolean validateURI(String uriString) throws MalformedURLException {
        new URL(uriString);
        return true;
    }

    public static CertData getCertData(String encodedCert) throws CertificateException {
        byte[] bytes = Base64.decode(encodedCert);
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(bytes));
        Format formatter = new SimpleDateFormat("dd/MM/yyyy");
        return fillCertData(cert, formatter);

    }

    public static Map<String,String> getMappings(String[] mappings){
        Map<String,String> mappingsMap = new HashMap<String,String>();
        for(String mapping:mappings){
            String[] split = mapping.split(":");
            mappingsMap.put(split[0], split[1]);
        }
        return mappingsMap;
    }

    public static String getSAML2SSOUrl(HttpServletRequest request) {
        String adminConsoleURL = CarbonUIUtil.getAdminConsoleURL(request);
        String endpointURL = adminConsoleURL.substring(0, adminConsoleURL.indexOf("/carbon"));
        return (endpointURL + "/samlsso");
    }

    public static String getOAuth2TokenEPURL(HttpServletRequest request) {
        String adminConsoleURL = CarbonUIUtil.getAdminConsoleURL(request);
        String endpointURL = adminConsoleURL.substring(0, adminConsoleURL.indexOf("/carbon"));
        return (endpointURL + "/oauth2/token");
    }

    public static String getOAuth2AuthzEPURL(HttpServletRequest request) {
        String adminConsoleURL = CarbonUIUtil.getAdminConsoleURL(request);
        String endpointURL = adminConsoleURL.substring(0, adminConsoleURL.indexOf("/carbon"));
        return (endpointURL + "/oauth2/authorize");
    }

    public static TrustedIdPDTO getFormData(HttpServletRequest request) throws Exception {

        if (ServletFileUpload.isMultipartContent(request)) {
            ServletRequestContext servletContext = new ServletRequestContext(request);
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            List items =  upload.parseRequest(servletContext);
            String idPName = null;
            boolean primary = false;
            String realmId = null;
            String oldPublicCert = null;
            String publicCert = null;
            String deletePublicCert = null;
            String[] oldClaimMappings = null;
            String[] claimMappings = null;
            String deleteClaimMapping = null;
            List<String> oldClaims = null;
            List<String> newClaims = new ArrayList<String>();
            List<String> claimsTempList = new ArrayList<String>();
            String[] oldRoleMappings = null;
            String[] roleMappings = null;
            String deleteRoleMapping = null;
            List<String> oldRoles = null;
            List<String> newRoles = new ArrayList<String>();
            List<String> rolesTempList = new ArrayList<String>();
            String tokenEndpointAlias = null;
            boolean isSAML2SSOEnabled = false;
            String idpEntityId = null;
            String spEntityId = null;
            String ssoUrl = null;
            boolean isAuthnRequestSigned = false;
            boolean isLogoutEnabled = false;
            String logoutUrl = null;
            boolean isLogoutRequestSigned = false;
            boolean isAuthnResponseSigned = false;
            boolean isOIDCEnabled = false;
            String authzUrl = null;
            String tokenUrl = null;
            String clientId = null;
            String clientSecret = null;
            TrustedIdPBean oldBean = (TrustedIdPBean)request.getSession().getAttribute("trustedIdPBean");
            TrustedIdPDTO oldDTO = (TrustedIdPDTO)request.getSession().getAttribute("trustedIdPDTO");
            if(oldBean != null){
                oldClaims = oldBean.getClaims();
                oldRoles = oldBean.getRoles();
            }
            if(oldDTO != null){
                oldPublicCert = oldDTO.getPublicCert();
                oldClaimMappings = oldDTO.getClaimMappings();
                oldRoleMappings = oldDTO.getRoleMappings();
            }
            if(oldClaims != null && !oldClaims.isEmpty()){
                for(int i = 0; i < oldClaims.size(); i++){
                    newClaims.add("");
                }
            }
            if(oldRoles != null && !oldRoles.isEmpty()){
                for(int i = 0; i < oldRoles.size(); i++){
                    newRoles.add("");
                }
            }
            TrustedIdPDTO trustedIdPDTO = new TrustedIdPDTO();
            for (Object item : items) {
                DiskFileItem diskFileItem = (DiskFileItem) item;
                String name = diskFileItem.getFieldName();
                if (name.equals("idPName")) {
                    FileItem fileItem = diskFileItem;
                    byte[] idPNameArray = fileItem.get();
                    if(idPNameArray != null && idPNameArray.length > 0){
                        idPName = new String(idPNameArray);
                    }
                } else if(name.equals("primary")){
                    FileItem fileItem = diskFileItem;
                    byte[] primaryArray = fileItem.get();
                    if(primaryArray != null && primaryArray.length > 0){
                        String primaryString = new String(primaryArray);
                        if(primaryString.equals("on")){
                            primary = true;
                        } else {
                            primary = false;
                        }
                    }
                } else if (name.equals("realmId")) {
                    FileItem fileItem = diskFileItem;
                    byte[] realmIdArray = fileItem.get();
                    if(realmIdArray != null && realmIdArray.length > 0){
                        realmId = new String(realmIdArray);
                    }
                } else if(name.equals("certFile")){
                    FileItem fileItem = diskFileItem;
                    byte[] publicCertArray = fileItem.get();
                    if(publicCertArray != null && publicCertArray.length > 0){
                        publicCert = Base64.encode(publicCertArray);
                    }
                } else if(name.equals("deletePublicCert")){
                    FileItem fileItem = diskFileItem;
                    byte[] deletePublicCertArray = fileItem.get();
                    if(deletePublicCertArray != null && deletePublicCertArray.length > 0){
                        deletePublicCert = new String(deletePublicCertArray);
                    }
                } else if(name.startsWith("claimrowname_")){
                    int rowId = Integer.parseInt(name.substring(name.indexOf("_")+1));
                    FileItem fileItem = diskFileItem;
                    byte[] claimsArray = fileItem.get();
                    if(oldClaims != null && rowId < oldClaims.size()){
                        newClaims.remove(rowId);
                        newClaims.add(rowId, new String(claimsArray));
                    } else {
                        if(claimsArray != null && claimsArray.length > 0){
                            claimsTempList.add(new String(claimsArray));
                        }
                    }
                } else if(name.equals("claimMappingFile")){
                    FileItem fileItem = diskFileItem;
                    byte[] claimMappingsArray = fileItem.get();
                    if(claimMappingsArray != null && claimMappingsArray.length > 0){
                        String claimMappingsString = new String(claimMappingsArray);
                        if(claimMappingsString != null){
                            claimMappingsString = claimMappingsString.replaceAll("\\s","");
                            claimMappings = claimMappingsString.split(",");
                        }
                    }
                } else if(name.equals("deleteClaimMappings")){
                    FileItem fileItem = diskFileItem;
                    byte[] deleteClaimMappingsArray = fileItem.get();
                    if(deleteClaimMappingsArray != null && deleteClaimMappingsArray.length > 0){
                        deleteClaimMapping = new String(deleteClaimMappingsArray);
                    }
                } else if(name.startsWith("rolerowname_")){
                    int rowId = Integer.parseInt(name.substring(name.indexOf("_")+1));
                    FileItem fileItem = diskFileItem;
                    byte[] rolesArray = fileItem.get();
                    if(oldRoles != null && rowId < oldRoles.size()){
                        newRoles.remove(rowId);
                        newRoles.add(rowId, new String(rolesArray));
                    } else {
                        if(rolesArray != null && rolesArray.length > 0){
                            rolesTempList.add(new String(rolesArray));
                        }
                    }
                } else if(name.equals("roleMappingFile")){
                    FileItem fileItem = diskFileItem;
                    byte[] roleMappingsArray = fileItem.get();
                    if(roleMappingsArray != null && roleMappingsArray.length > 0){
                        String roleMappingsString = new String(roleMappingsArray);
                        if(roleMappingsString != null){
                            roleMappingsString = roleMappingsString.replaceAll("\\s","");
                            roleMappings = roleMappingsString.split(",");
                        }
                    }
                } else if(name.equals("deleteRoleMappings")){
                    FileItem fileItem = diskFileItem;
                    byte[] deleteRoleMappingsArray = fileItem.get();
                    if(deleteRoleMappingsArray != null && deleteRoleMappingsArray.length > 0){
                        deleteRoleMapping = new String(deleteRoleMappingsArray);
                    }
                } else if (name.equals("tokenEndpointAlias")) {
                    FileItem fileItem = diskFileItem;
                    byte[] tokenEndpointAliasArray = fileItem.get();
                    if(tokenEndpointAliasArray != null && tokenEndpointAliasArray.length > 0){
                        tokenEndpointAlias = new String(tokenEndpointAliasArray);
                    }
                } else if(name.equals("saml2SSOEnabled")){
                    FileItem fileItem = diskFileItem;
                    byte[] saml2SSOEnabledArray = fileItem.get();
                    if(saml2SSOEnabledArray != null && saml2SSOEnabledArray.length > 0){
                        String saml2SSOEnabledString = new String(saml2SSOEnabledArray);
                        if(saml2SSOEnabledString.equals("on")){
                            isSAML2SSOEnabled = true;
                        } else {
                            isSAML2SSOEnabled = false;
                        }
                    }
                } else if (name.equals("idpEntityId")) {
                    FileItem fileItem = diskFileItem;
                    byte[] idpEntityIdArray = fileItem.get();
                    if(idpEntityIdArray != null && idpEntityIdArray.length > 0){
                        idpEntityId = new String(idpEntityIdArray);
                    }
                } else if (name.equals("spEntityId")) {
                    FileItem fileItem = diskFileItem;
                    byte[] spEntityIdArray = fileItem.get();
                    if(spEntityIdArray != null && spEntityIdArray.length > 0){
                        spEntityId = new String(spEntityIdArray);
                    }
                } else if(name.equals("ssoUrl")){
                    FileItem fileItem = diskFileItem;
                    byte[] ssoUrlArray = fileItem.get();
                    if(ssoUrlArray != null && ssoUrlArray.length > 0){
                        ssoUrl = new String(ssoUrlArray);
                    }
                } else if(name.equals("authnRequestSigned")){
                    FileItem fileItem = diskFileItem;
                    byte[] authnRequestSignedArray = fileItem.get();
                    if(authnRequestSignedArray != null && authnRequestSignedArray.length > 0){
                        String authnRequestSignedString = new String(authnRequestSignedArray);
                        if(authnRequestSignedString.equals("on")){
                            isAuthnRequestSigned = true;
                        } else {
                            isAuthnRequestSigned = false;
                        }
                    }
                } else if(name.equals("sloEnabled")){
                    FileItem fileItem = diskFileItem;
                    byte[] sloEnabledArry = fileItem.get();
                    if(sloEnabledArry != null && sloEnabledArry.length > 0){
                        String sloEnabledString = new String(sloEnabledArry);
                        if(sloEnabledString.equals("on")){
                            isLogoutEnabled = true;
                        } else {
                            isLogoutEnabled = false;
                        }
                    }
                } else if (name.equals("logoutUrl")) {
                    FileItem fileItem = diskFileItem;
                    byte[] logoutUrlArray = fileItem.get();
                    if(logoutUrlArray != null && logoutUrlArray.length > 0){
                        logoutUrl = new String(logoutUrlArray);
                    }
                } else if(name.equals("logoutRequestSigned")){
                    FileItem fileItem = diskFileItem;
                    byte[] logoutRequestSignedArray = fileItem.get();
                    if(logoutRequestSignedArray != null && logoutRequestSignedArray.length > 0){
                        String logoutRequestSignedString = new String(logoutRequestSignedArray);
                        if(logoutRequestSignedString.equals("on")){
                            isLogoutRequestSigned = true;
                        } else {
                            isLogoutRequestSigned = false;
                        }
                    }
                } else if(name.equals("authnResponseSigned")){
                    FileItem fileItem = diskFileItem;
                    byte[] authnResponseSignedArray = fileItem.get();
                    if(authnResponseSignedArray != null && authnResponseSignedArray.length > 0){
                        String authnResponseSignedString = new String(authnResponseSignedArray);
                        if(authnResponseSignedString.equals("on")){
                            isAuthnResponseSigned = true;
                        } else {
                            isAuthnResponseSigned = false;
                        }
                    }
                } else if(name.equals("oidcEnabled")){
                    FileItem fileItem = diskFileItem;
                    byte[] oidcEnabledArray = fileItem.get();
                    if(oidcEnabledArray != null && oidcEnabledArray.length > 0){
                        String oidcEnabledString = new String(oidcEnabledArray);
                        if(oidcEnabledString.equals("on")){
                            isOIDCEnabled = true;
                        } else {
                            isOIDCEnabled = false;
                        }
                    }
                } else if (name.equals("authzUrl")) {
                    FileItem fileItem = diskFileItem;
                    byte[] authzUrlArray = fileItem.get();
                    if(authzUrlArray != null && authzUrlArray.length > 0){
                        authzUrl = new String(authzUrlArray);
                    }
                } else if (name.equals("tokenUrl")) {
                    FileItem fileItem = diskFileItem;
                    byte[] tokenUrlArray = fileItem.get();
                    if(tokenUrlArray != null && tokenUrlArray.length > 0){
                        tokenUrl = new String(tokenUrlArray);
                    }
                } else if (name.equals("clientId")) {
                    FileItem fileItem = diskFileItem;
                    byte[] clientIdArray = fileItem.get();
                    if(clientIdArray != null && clientIdArray.length > 0){
                        clientId = new String(clientIdArray);
                    }
                } else if (name.equals("clientSecret")) {
                    FileItem fileItem = diskFileItem;
                    byte[] clientSecretArray = fileItem.get();
                    if(clientSecretArray != null && clientSecretArray.length > 0){
                        clientSecret = new String(clientSecretArray);
                    }
                }
            }
            newClaims.addAll(claimsTempList);
            newRoles.addAll(rolesTempList);

            trustedIdPDTO.setIdPName(idPName);
            trustedIdPDTO.setPrimary(primary);
            if(oldPublicCert != null && publicCert == null &&
                    deletePublicCert != null && deletePublicCert.equals("false")){
                publicCert = oldPublicCert;
            }
            trustedIdPDTO.setHomeRealmId(realmId);
            trustedIdPDTO.setPublicCert(publicCert);
            trustedIdPDTO.setClaims(newClaims.toArray(new String[newClaims.size()]));
            if(oldClaimMappings != null && claimMappings == null &&
                    deleteClaimMapping != null && deleteClaimMapping.equals("false")){
                claimMappings = oldClaimMappings;
            }
            trustedIdPDTO.setClaimMappings(claimMappings);
            trustedIdPDTO.setRoles(newRoles.toArray(new String[newRoles.size()]));
            if(oldRoleMappings != null && roleMappings == null &&
                    deleteRoleMapping != null && deleteRoleMapping.equals("false")){
                roleMappings = oldRoleMappings;
            }
            trustedIdPDTO.setRoleMappings(roleMappings);
            trustedIdPDTO.setTokenEndpointAlias(tokenEndpointAlias);
            trustedIdPDTO.setSAML2SSOEnabled(isSAML2SSOEnabled);
            trustedIdPDTO.setIdpEntityId(idpEntityId);
            trustedIdPDTO.setSpEntityId(spEntityId);
            trustedIdPDTO.setSSOUrl(ssoUrl);
            trustedIdPDTO.setAuthnRequestSigned(isAuthnRequestSigned);
            trustedIdPDTO.setLogoutEnabled(isLogoutEnabled);
            trustedIdPDTO.setLogoutRequestUrl(logoutUrl);
            trustedIdPDTO.setLogoutRequestSigned(isLogoutRequestSigned);
            trustedIdPDTO.setAuthnResponseSigned(isAuthnResponseSigned);
            trustedIdPDTO.setOIDCEnabled(isOIDCEnabled);
            trustedIdPDTO.setClientId(clientId);
            trustedIdPDTO.setClientSecret(clientSecret);
            trustedIdPDTO.setAuthzEndpointUrl(authzUrl);
            trustedIdPDTO.setTokenEndpointUrl(tokenUrl);

            return trustedIdPDTO;
        } else {
            throw new Exception("Invalid Content Type: Not multipart/form-data");
        }
    }

    private static CertData fillCertData(X509Certificate cert, Format formatter) throws CertificateEncodingException {

        CertData certData = new CertData();
        certData.setSubjectDN(cert.getSubjectDN().getName());
        certData.setIssuerDN(cert.getIssuerDN().getName());
        certData.setSerialNumber(cert.getSerialNumber());
        certData.setVersion(cert.getVersion());
        certData.setNotAfter(formatter.format(cert.getNotAfter()));
        certData.setNotBefore(formatter.format(cert.getNotBefore()));
        certData.setPublicKey(Base64.encode(cert.getPublicKey().getEncoded()));
        return certData;
    }
}
