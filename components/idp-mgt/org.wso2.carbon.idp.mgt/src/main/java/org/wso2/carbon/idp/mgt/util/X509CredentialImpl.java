/*
*Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.idp.mgt.util;

import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.CredentialContextSet;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.x509.X509Credential;

import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Collection;

public class X509CredentialImpl implements X509Credential {

    PublicKey publicKey = null;
    X509Certificate certificate = null;

    public X509CredentialImpl(X509Certificate cert){
        publicKey = cert.getPublicKey();
        certificate = cert;
    }

    @Override
    public X509Certificate getEntityCertificate() {
        return certificate;
    }

    @Override
    public PublicKey getPublicKey() {
        return publicKey;
    }

    @Override
    public Collection<X509Certificate> getEntityCertificateChain() {
        return null;
    }

    @Override
    public Collection<X509CRL> getCRLs() {
        return null;
    }

    @Override
    public String getEntityId() {
        return null;
    }

    @Override
    public UsageType getUsageType() {
        return null;
    }

    @Override
    public Collection<String> getKeyNames() {
        return null;
    }

    @Override
    public PrivateKey getPrivateKey() {
        return null;
    }

    @Override
    public SecretKey getSecretKey() {
        return null;
    }

    @Override
    public CredentialContextSet getCredentalContextSet() {
        return null;
    }

    @Override
    public Class<? extends Credential> getCredentialType() {
        return null;
    }
}
