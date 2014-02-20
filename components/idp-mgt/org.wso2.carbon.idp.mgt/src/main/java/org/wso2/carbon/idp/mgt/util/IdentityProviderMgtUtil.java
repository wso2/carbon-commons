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

import org.apache.axiom.om.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.idp.mgt.exception.IdentityProviderMgtException;
import org.wso2.carbon.idp.mgt.internal.IdentityProviderMgtServiceComponent;
import org.wso2.carbon.idp.mgt.persistence.JDBCPersistenceManager;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserStoreException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

public class IdentityProviderMgtUtil {

    private static final Log log = LogFactory.getLog(IdentityProviderMgtUtil.class);

    public static boolean validateURI(String uriString) throws IdentityProviderMgtException {
        try {
            new URL(uriString);
        }  catch (MalformedURLException e) {
            throw new IdentityProviderMgtException(e);
        }
        return true;
    }

    public static Connection getDBConnection() throws IdentityProviderMgtException{
        try {
            Connection conn = JDBCPersistenceManager.getInstance().getDBConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            return conn;
        } catch (SQLException e) {
            log.error("Error occurred while getting Realm DB connection", e);
            throw new IdentityProviderMgtException("Error occurred while getting Realm DB connection");
        }
    }

    /**
     * Helper method to hexify a byte array.
     * TODO:need to verify the logic
     *
     * @param bytes
     * @return  hexadecimal representation
     */
    public static String hexify(byte bytes[]) {

        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        StringBuffer buf = new StringBuffer(bytes.length * 2);

        for (int i = 0; i < bytes.length; ++i) {
            buf.append(hexDigits[(bytes[i] & 0xf0) >> 4]);
            buf.append(hexDigits[bytes[i] & 0x0f]);
        }

        return buf.toString();
    }

    public static String generatedThumbPrint(String publicCert) throws IdentityProviderMgtException {
        MessageDigest digestValue = null;
        try {
            digestValue = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new IdentityProviderMgtException(e.getMessage(), e);
        }
        byte[] der = Base64.decode(publicCert);
        digestValue.update(der);
        byte[] digestInBytes = digestValue.digest();
        String publicCertThumbprint = IdentityProviderMgtUtil.hexify(digestInBytes);
        return publicCertThumbprint;
    }

    /**
     * Get the tenant id of the given tenant domain.
     *
     * @param tenantDomain Tenant Domain
     * @return Tenant Id of domain user belongs to.
     * @throws IdentityProviderMgtException Error when getting tenant id from tenant domain
     */
    public static int getTenantIdOfDomain(String tenantDomain) throws IdentityProviderMgtException {
        int tenantId = MultitenantConstants.SUPER_TENANT_ID;
        if (tenantDomain != null) {
            try {
                TenantManager tenantManager = IdentityProviderMgtServiceComponent.getRealmService().getTenantManager();
                tenantId = tenantManager.getTenantId(tenantDomain);
            } catch (UserStoreException e) {
                String errorMsg = "Error when getting the tenant id from the tenant domain : " + tenantDomain;
                log.error(errorMsg, e);
                throw new IdentityProviderMgtException(errorMsg);
            }
        }
        return tenantId;
    }

    public static String getKeyStoreFileName(String filePath) {
        String name = null;
        int index = filePath.lastIndexOf('/');
        if (index != -1) {
            name = filePath.substring(index + 1);
        } else {
            index = filePath.lastIndexOf(File.separatorChar);
            if (index != -1) {
                name = filePath.substring(filePath.lastIndexOf(File.separatorChar));
            } else {
                name = filePath;
            }
        }
        return name;
    }

    public static boolean containsEntry(Map<String,String> map, Map.Entry entry){
        Object key = entry.getKey();
        Object value = entry.getValue();
        if(map.containsKey(key) && map.containsValue(value)){
            return true;
        }
        return false;
    }

    public static String getEncodedIdPCertFromThumb(String thumb, int tenantId, String tenantDomain)
            throws IdentityProviderMgtException {

        KeyStoreManager keyMan = KeyStoreManager.getInstance(tenantId);
        try {
            String trustStoreName = null;
            KeyStore trustStore = null;
            if(MultitenantConstants.SUPER_TENANT_ID == tenantId){
                trustStore = keyMan.getPrimaryKeyStore();
            } else {
                trustStoreName = tenantDomain.trim().replace(".", "-") + "-idp-mgt-truststore.jks";
                trustStore = keyMan.getKeyStore(trustStoreName);
            }
            Certificate cert = null;
            MessageDigest sha = null;
            sha = MessageDigest.getInstance("SHA-1");
            for (Enumeration e = trustStore.aliases(); e.hasMoreElements();) {
                String alias = (String) e.nextElement();
                Certificate[] certs = trustStore.getCertificateChain(alias);
                if (certs == null || certs.length == 0) {
                    // no cert chain, so lets check if getCertificate gives us a result.
                    cert = trustStore.getCertificate(alias);
                    if (cert == null) {
                        return null;
                    }
                } else {
                    cert = certs[0];
                }
                if (!(cert instanceof X509Certificate)) {
                    continue;
                }
                sha.reset();
                sha.update(cert.getEncoded());
                byte[] data = sha.digest();
                if (thumb.equals(IdentityProviderMgtUtil.hexify(data))) {
                    return Base64.encode(cert.getEncoded());
                }
            }
        } catch (Exception e) {
            String msg = "Error occurred while retrieving IdP public certificate for tenant";
            log.error(msg + " " + tenantDomain, e);
            throw new IdentityProviderMgtException(msg);
        }
        return null;
    }

    public static String getEncodedIdPCertFromAlias(String alias, int tenantId, String tenantDomain) throws IdentityProviderMgtException{

        KeyStoreManager keyMan = KeyStoreManager.getInstance(tenantId);
        try {
            String trustStoreName = null;
            KeyStore trustStore = null;
            if(MultitenantConstants.SUPER_TENANT_ID == tenantId){
                trustStore = keyMan.getPrimaryKeyStore();
            } else {
                trustStoreName = tenantDomain.trim().replace(".", "-") + "-idp-mgt-truststore.jks";
                trustStore = keyMan.getKeyStore(trustStoreName);
            }
            Certificate cert = trustStore.getCertificate(alias);
            if(cert != null){
                return Base64.encode(cert.getEncoded());
            }
        } catch (Exception e) {
            String msg = "Error occurred while retrieving IdP public certificate for tenant";
            log.error(msg + " " + tenantDomain, e);
            throw new IdentityProviderMgtException(msg);
        }
        return null;
    }

    public static void importCertToStore(String alias, String certData, int tenantId, String tenantDomain) throws IdentityProviderMgtException {

        KeyStoreManager keyMan = KeyStoreManager.getInstance(tenantId);
        try {
            String trustStoreName = null;
            KeyStore trustStore = null;
            if(MultitenantConstants.SUPER_TENANT_ID == tenantId){
                trustStore = keyMan.getPrimaryKeyStore();
                trustStoreName = getKeyStoreFileName(
                        ServerConfiguration.getInstance().getFirstProperty(
                                RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_FILE));
            } else {
                trustStoreName = tenantDomain.trim().replace(".", "-") + "-idp-mgt-truststore.jks";
                trustStore = keyMan.getKeyStore(trustStoreName);
            }
            byte[] bytes = Base64.decode(certData);
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            X509Certificate cert;
            cert = (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(bytes));

            if (trustStore.getCertificate(alias) != null) {
                String msg = "Certificate with alias " + alias + " already exists for tenant";
                log.error(msg + " " + tenantDomain);
                throw new IdentityProviderMgtException(msg);
            }
            trustStore.setCertificateEntry(alias, cert);
            keyMan.updateKeyStore(trustStoreName, trustStore);

        } catch (Exception e) {
            String msg = "Error occurred while importing IdP public certificate for tenant";
            log.error(msg + " " + tenantDomain, e);
            throw new IdentityProviderMgtException(msg);
        }
    }

    public static void updateCertToStore(String oldAlias, String newAlias, String certData, int tenantId, String tenantDomain) throws IdentityProviderMgtException {

        KeyStoreManager keyMan = KeyStoreManager.getInstance(tenantId);
        try {
            String trustStoreName = null;
            KeyStore trustStore = null;
            if(MultitenantConstants.SUPER_TENANT_ID == tenantId){
                trustStore = keyMan.getPrimaryKeyStore();
                trustStoreName = getKeyStoreFileName(
                        ServerConfiguration.getInstance().getFirstProperty(
                                RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_FILE));
            } else {
                trustStoreName = tenantDomain.trim().replace(".", "-") + "-idp-mgt-truststore.jks";
                trustStore = keyMan.getKeyStore(trustStoreName);
            }

            byte[] bytes = Base64.decode(certData);
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            X509Certificate cert;
            cert = (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(bytes));

            if (trustStore.getCertificate(oldAlias) != null) {
                if(log.isDebugEnabled()){
                    log.debug("Deleting existing certificate with alias " + oldAlias + " for tenant " + tenantDomain);
                }
                trustStore.deleteEntry(oldAlias);
            } else {
                if(log.isDebugEnabled()){
                    log.debug("No certificates found with alias" + oldAlias + " for tenant " + tenantDomain);
                }
            }
            trustStore.setCertificateEntry(newAlias, cert);
            keyMan.updateKeyStore(trustStoreName, trustStore);

        } catch (Exception e) {
            String msg = "Error occurred while importing IdP public certificate for tenant";
            log.error(msg + " " + tenantDomain, e);
            throw new IdentityProviderMgtException(msg);
        }
    }

    public static void deleteCertFromStore(String alias, int tenantId, String tenantDomain) throws IdentityProviderMgtException {

        KeyStoreManager keyMan = KeyStoreManager.getInstance(tenantId);
        try {
            String trustStoreName = null;
            KeyStore trustStore = null;
            if(MultitenantConstants.SUPER_TENANT_ID == tenantId){
                trustStore = keyMan.getPrimaryKeyStore();
                trustStoreName = getKeyStoreFileName(
                        ServerConfiguration.getInstance().getFirstProperty(
                                RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_FILE));
            } else {
                trustStoreName = tenantDomain.trim().replace(".", "-") + "-idp-mgt-truststore.jks";
                trustStore = keyMan.getKeyStore(trustStoreName);
            }

            if (trustStore.getCertificate(alias) == null) {
                log.warn("Certificate with alias " + alias + " does not exist in tenant key store " + trustStore);
            } else {
                trustStore.deleteEntry(alias);
                keyMan.updateKeyStore(trustStoreName, trustStore);
            }
        } catch (Exception e) {
            String msg = "Error occurred while deleting IdP public certificate for tenant";
            log.error(msg + " " + tenantDomain, e);
            throw new IdentityProviderMgtException(msg);
        }
    }

    public static String convertListToString(List<String> list){
        String returnString = null;
        if(list != null && !list.isEmpty()){
            returnString = "";
            for(int i=0; i < list.size(); i++){
                if(list.get(i) != null && !list.get(i).equals("")){
                    if(i != list.size()-1){
                        returnString += list.get(i) + " ";
                    } else {
                        returnString += list.get(i);
                    }
                }
            }
        }
        return returnString;
    }

    public static Certificate getCertificate(String encodedCert) throws IdentityProviderMgtException {

        try {
            byte[] bytes = Base64.decode(encodedCert);
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(bytes));
            return cert;
        } catch (Exception e) {
            log.error("Error occurred while decoding and reconstructing Certificate", e);
            throw new IdentityProviderMgtException("Error occurred while decoding and reconstructing Certificate");
        }
    }

    public static void closeConnection(Connection dbConnection) {
        if (dbConnection != null) {
            try {
                dbConnection.close();
            } catch (SQLException e) {
                log.error("Error occurred while closing database connection" + e);
            }
        }
    }

    public static void rollBack(Connection dbConnection) {
        try {
            if (dbConnection != null) {
                dbConnection.rollback();
            }
        } catch (SQLException e) {
            log.error("Error occurred while rolling back transactions", e);
        }
    }

}
