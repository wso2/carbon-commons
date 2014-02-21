/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.identity.authenticator.saml2.sso.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.authenticator.saml2.sso.SAML2SSOAuthenticatorException;
import org.wso2.carbon.identity.authenticator.saml2.sso.internal.SAML2SSOAuthBEDataHolder;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

public class Util {

    private static boolean bootStrapped = false;
    private static Log log = LogFactory.getLog(Util.class);

    /**
     * Constructing the XMLObject Object from a String
     *
     * @param authReqStr
     * @return Corresponding XMLObject which is a SAML2 object
     * @throws org.wso2.carbon.identity.authenticator.saml2.sso.SAML2SSOAuthenticatorException
     */
    public static XMLObject unmarshall(String authReqStr) throws SAML2SSOAuthenticatorException {
        XMLObject response;

        try {
            doBootstrap();
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(new ByteArrayInputStream(authReqStr.trim()
                    .getBytes()));
            Element element = document.getDocumentElement();
            UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
            Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
            response = unmarshaller.unmarshall(element);
            // Check for duplicate samlp:Response
            NodeList list = response.getDOM().getElementsByTagNameNS( SAMLConstants.SAML20P_NS,"Response");
            if (list.getLength() > 0) {
                log.error("Invalid schema for the SAML2 reponse");
                throw new SAML2SSOAuthenticatorException("Error occured while processing saml2 response");
            }
            return response;
        } catch (ParserConfigurationException e) {
            log.error(e.getMessage());
            throw new SAML2SSOAuthenticatorException("Error occured while processing saml2 response");
        } catch (SAXException e) {
            log.error(e.getMessage());
            throw new SAML2SSOAuthenticatorException("Error occured while processing saml2 response");
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new SAML2SSOAuthenticatorException("Error occured while processing saml2 response");
        } catch (UnmarshallingException e) {
            log.error(e.getMessage());
            throw new SAML2SSOAuthenticatorException("Error occured while processing saml2 response");
        }

    }

    /**
     * This method is used to initialize the OpenSAML2 library. It calls the bootstrap method, if it
     * is not initialized yet.
     */
    public static void doBootstrap() {
        if (!bootStrapped) {
            try {
                DefaultBootstrap.bootstrap();
                bootStrapped = true;
            } catch (ConfigurationException e) {
                log.error("Error in bootstrapping the OpenSAML2 library", e);
            }
        }
    }

    /**
     * Get the X509CredentialImpl object for a particular tenant
     * @param domainName domain name
     * @return X509CredentialImpl object containing the public certificate of that tenant
     * @throws org.wso2.carbon.identity.authenticator.saml2.sso.SAML2SSOAuthenticatorException Error when creating X509CredentialImpl object
     */
    public static X509CredentialImpl getX509CredentialImplForTenant(String domainName)
            throws SAML2SSOAuthenticatorException {

        int tenantID = MultitenantConstants.SUPER_TENANT_ID;
        RegistryService registryService = SAML2SSOAuthBEDataHolder.getInstance().getRegistryService();
        RealmService realmService = SAML2SSOAuthBEDataHolder.getInstance().getRealmService();

        // get the tenantID
        if (!domainName.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            try {
                tenantID = realmService.getTenantManager().getTenantId(domainName);
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                String errorMsg = "Error getting the TenantID for the domain name";
                log.error(errorMsg, e);
                throw new SAML2SSOAuthenticatorException(errorMsg, e);
            }
        }

        KeyStoreManager keyStoreManager = null;
        // get an instance of the corresponding Key Store Manager instance
        keyStoreManager = KeyStoreManager.getInstance(tenantID);

        X509CredentialImpl credentialImpl = null;
        try {
            if (tenantID != MultitenantConstants.SUPER_TENANT_ID) {    // for non zero tenants, load private key from their generated key store
                KeyStore keystore = keyStoreManager.getKeyStore(generateKSNameFromDomainName(domainName));
                java.security.cert.X509Certificate cert = (java.security.cert.X509Certificate) keystore.getCertificate(domainName);
                credentialImpl = new X509CredentialImpl(cert);
            } else {    // for tenant zero, load the cert corresponding to given alias in authenticators.xml
                String alias = SAML2SSOAuthBEDataHolder.getInstance().getIdPCertAlias();
                java.security.cert.X509Certificate cert = null;
                if (alias != null) {
                    cert = (X509Certificate) keyStoreManager.getPrimaryKeyStore().getCertificate(alias);
                    if(cert == null){
                        String errorMsg = "Cannot find a certificate with the alias " + alias +
                                " in the default key store. Please check the 'KeyAlias' property in" +
                                " the SSO configuration of the authenticators.xml";
                        log.error(errorMsg);
                        throw new SAML2SSOAuthenticatorException(errorMsg);
                    }
                } else { // if the idpCertAlias is not given, use the default certificate.
                    cert = keyStoreManager.getDefaultPrimaryCertificate();
                }
                credentialImpl = new X509CredentialImpl(cert);
            }
        } catch (Exception e) {
            String errorMsg = "Error instantiating an X509CredentialImpl object for the public cert.";
            log.error(errorMsg, e);
            throw new SAML2SSOAuthenticatorException(errorMsg, e);
        }
        return credentialImpl;
    }

    /**
     * Generate the key store name from the domain name
     * @param tenantDomain tenant domain name
     * @return key store file name
     */
    private static String generateKSNameFromDomainName(String tenantDomain) {
        String ksName = tenantDomain.trim().replace(".", "-");
        return (ksName + ".jks");
    }

}
