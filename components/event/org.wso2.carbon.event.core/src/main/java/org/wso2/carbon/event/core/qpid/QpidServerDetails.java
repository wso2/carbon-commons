/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.event.core.qpid;

import org.wso2.carbon.utils.ServerConstants;

@Deprecated
public class QpidServerDetails {

    private static final String DOMAIN_NAME_SEPARATOR = "@";
    private static final String DOMAIN_NAME_SEPARATOR_INTERNAL = "!";

    private static final String SECURITY_RESOURCES_DIR = "/repository/resources/security/";
    private static final String KEYSTORE_FILE = "wso2carbon.jks";
    private static final String TRUSTSTORE_FILE = "client-truststore.jks";

    private String accessKey;
    private String clientID;
    private String virtualHostName;
    private String hostName;
    private String port;
    private boolean sslOnly;

    public QpidServerDetails(String accessKey,
                             String clientID,
                             String virtualHostName,
                             String hostName,
                             String port){
        this(accessKey, clientID, virtualHostName, hostName, port, false);
    }

    public QpidServerDetails(String accessKey,
                             String clientID,
                             String virtualHostName,
                             String hostName,
                             String port,
                             boolean sslOnly) {
        this.accessKey = accessKey;
        this.clientID = clientID;
        this.virtualHostName = virtualHostName;
        this.hostName = hostName;
        this.port = port;
        this.sslOnly=sslOnly;
    }

     public String getTCPConnectionURL(String username, String password) {

        // Qpid uses @ to seperate the user name and the client id so
        // we need to replace it with !
        username = username.replace(DOMAIN_NAME_SEPARATOR, DOMAIN_NAME_SEPARATOR_INTERNAL);

         // these are the properties which needs to be passed when ssl is enabled
         String KEY_STORE_PATH = System.getProperty(ServerConstants.CARBON_HOME) + SECURITY_RESOURCES_DIR + KEYSTORE_FILE;
         String TRUST_STORE_PATH = System.getProperty(ServerConstants.CARBON_HOME) + SECURITY_RESOURCES_DIR + TRUSTSTORE_FILE;
         String SSL_PASSWORD = "wso2carbon";

         if (sslOnly) {
             //"amqp://admin:admin@carbon/carbon?brokerlist='tcp://{hostname}:{port}?ssl='true'&trust_store='{trust_store_path}'&trust_store_password='{trust_store_pwd}'&key_store='{keystore_path}'&key_store_password='{key_store_pwd}''";

             return new StringBuffer()
                     .append("amqp://").append(username).append(":").append(password)
                     .append("@").append(this.clientID)
                     .append("/").append(this.virtualHostName)
                     .append("?brokerlist='tcp://").append(this.hostName).append(":").append(this.port).append("?ssl='true'&trust_store='").append(TRUST_STORE_PATH)
                     .append("'&trust_store_password='").append(SSL_PASSWORD).append("'&key_store='").append(KEY_STORE_PATH)
                     .append("'&key_store_password='").append(SSL_PASSWORD).append("''")
                     .toString();
         } else {
            // amqp://{username}:{password}@carbon/carbon?brokerlist='tcp://{hostname}:{port}'

            return new StringBuffer()
                .append("amqp://").append(username).append(":").append(password)
                .append("@").append(this.clientID)
                .append("/").append(this.virtualHostName)
                .append("?brokerlist='tcp://").append(this.hostName).append(":").append(this.port).append("'")
                .toString();
         }
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getVirtualHostName() {
        return virtualHostName;
    }

    public void setVirtualHostName(String virtualHostName) {
        this.virtualHostName = virtualHostName;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
