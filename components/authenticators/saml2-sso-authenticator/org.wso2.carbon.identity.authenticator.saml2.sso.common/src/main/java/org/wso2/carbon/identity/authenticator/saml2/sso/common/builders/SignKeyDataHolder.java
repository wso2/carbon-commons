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
package org.wso2.carbon.identity.authenticator.saml2.sso.common.builders;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;

import javax.crypto.SecretKey;

import org.apache.xml.security.signature.XMLSignature;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.CredentialContextSet;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.x509.X509Credential;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public class SignKeyDataHolder implements X509Credential {

    private String signatureAlgorithm = null;
    private X509Certificate[] issuerCerts = null;
    private PrivateKey issuerPK = null;

    public SignKeyDataHolder() throws Exception {

        try {
                String keyAlias = ServerConfiguration.getInstance().getFirstProperty("Security.KeyStore.KeyAlias");
                KeyStoreManager keyMan = KeyStoreManager.getInstance(MultitenantConstants.SUPER_TENANT_ID);
                Certificate[] certificates = keyMan.getPrimaryKeyStore().getCertificateChain(keyAlias);

                issuerPK = keyMan.getDefaultPrivateKey();
                issuerCerts = new X509Certificate[certificates.length];
                
                int i = 0;
                for (Certificate certificate : certificates) {
                    issuerCerts[i++] = (X509Certificate) certificate;
                }

                signatureAlgorithm = XMLSignature.ALGO_ID_SIGNATURE_RSA;

                String pubKeyAlgo = issuerCerts[0].getPublicKey().getAlgorithm();
                if (pubKeyAlgo.equalsIgnoreCase("DSA")) {
                    signatureAlgorithm = XMLSignature.ALGO_ID_SIGNATURE_DSA;
                }

        } catch (Exception e) {
        	throw new Exception("Error while reading the key", e);
        }

    }

    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }

    public Collection<X509CRL> getCRLs() {
        return null;
    }

    public X509Certificate getEntityCertificate() {
        return issuerCerts[0];
    }

    public Collection<X509Certificate> getEntityCertificateChain() {
        return Arrays.asList(issuerCerts);
    }

    public CredentialContextSet getCredentalContextSet() {
        // TODO Auto-generated method stub
        return null;
    }

    public Class<? extends Credential> getCredentialType() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getEntityId() {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<String> getKeyNames() {
        // TODO Auto-generated method stub
        return null;
    }

    public PrivateKey getPrivateKey() {
        return issuerPK;
    }

    public PublicKey getPublicKey() {
        return issuerCerts[0].getPublicKey();
    }

    public SecretKey getSecretKey() {
        // TODO Auto-generated method stub
        return null;
    }

    public UsageType getUsageType() {
        // TODO Auto-generated method stub
        return null;
    }

}

