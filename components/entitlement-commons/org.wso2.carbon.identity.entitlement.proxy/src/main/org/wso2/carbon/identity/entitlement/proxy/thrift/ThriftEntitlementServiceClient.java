/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.identity.entitlement.proxy.thrift;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.wso2.carbon.identity.entitlement.proxy.*;
import org.wso2.carbon.identity.entitlement.proxy.exception.EntitlementProxyException;
import org.wso2.carbon.identity.entitlement.proxy.generatedCode.EntitlementException;
import org.wso2.carbon.identity.entitlement.proxy.generatedCode.EntitlementThriftClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ThriftEntitlementServiceClient extends AbstractEntitlementServiceClient {

    private String trustStore = System.getProperty(ProxyConstants.TRUST_STORE);
    private String trustStorePass = System.getProperty(ProxyConstants.TRUST_STORE_PASSWORD);
    private String serverUrl;
    private String userName;
    private String password;
    private String thriftHost;
    private int thriftPort;
    private boolean reuseSession = true;

    private Map<String, Authenticator> authenticators = new ConcurrentHashMap<String, Authenticator>();

    public ThriftEntitlementServiceClient(String serverUrl, String username, String password, String thriftHost, int thriftPort, boolean reuseSession){
        this.serverUrl = serverUrl;
        this.userName = username;
        this.password = password;
        this.thriftHost =  thriftHost;
        this.thriftPort = thriftPort;
        this.reuseSession = reuseSession;
    }

    @Override
    public String getDecision(Attribute[] attributes, String appId) throws Exception {
        String xacmlRequest = XACMLRequetBuilder.buildXACML3Request(attributes);
        EntitlementThriftClient.Client client = getThriftClient(appId);
        Authenticator authenticator = getAuthenticator(serverUrl, userName,
                password);
        return getDecision(xacmlRequest,client,authenticator.getSessionId(false));
    }

    @Override
    public boolean subjectCanActOnResource(String subjectType, String alias, String actionId,
                                           String resourceId, String domainId, String appId) throws Exception {
        Attribute subjectAttribute = new Attribute("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject", subjectType, ProxyConstants.DEFAULT_DATA_TYPE, alias);
        Attribute actionAttribute = new Attribute("urn:oasis:names:tc:xacml:3.0:attribute-category:action", "urn:oasis:names:tc:xacml:1.0:action:action-id", ProxyConstants.DEFAULT_DATA_TYPE, actionId);
        Attribute resourceAttribute = new Attribute("urn:oasis:names:tc:xacml:3.0:attribute-category:resource", "urn:oasis:names:tc:xacml:1.0:resource:resource-id", ProxyConstants.DEFAULT_DATA_TYPE, resourceId);
        Attribute environmentAttribute = new Attribute("urn:oasis:names:tc:xacml:3.0:attribute-category:environment", "urn:oasis:names:tc:xacml:1.0:environment:environment-id", ProxyConstants.DEFAULT_DATA_TYPE, domainId);
        Attribute[] tempArr = {subjectAttribute, actionAttribute, resourceAttribute, environmentAttribute};
        String xacmlRequest = XACMLRequetBuilder.buildXACML3Request(tempArr);
        EntitlementThriftClient.Client client = getThriftClient(appId);
        Authenticator authenticator = getAuthenticator(serverUrl, userName, password);
        return (getDecision(xacmlRequest,client,authenticator.getSessionId(false))).contains("Permit");
    }

    @Override
    public boolean subjectCanActOnResource(String subjectType, String alias, String actionId,
                                           String resourceId, Attribute[] attributes, String domainId, String appId) throws Exception {
        Attribute[] attrs = new Attribute[attributes.length + 4];
        attrs[0] = new Attribute("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject", subjectType, ProxyConstants.DEFAULT_DATA_TYPE, alias);
        for (int i = 0; i < attributes.length; i++) {
            attrs[i + 1] = new Attribute("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject", attributes[i].getType(),
                    attributes[i].getId(), attributes[i].getValue());
        }
        attrs[attrs.length - 3] = new Attribute("urn:oasis:names:tc:xacml:3.0:attribute-category:action", "urn:oasis:names:tc:xacml:1.0:action:action-id", ProxyConstants.DEFAULT_DATA_TYPE, actionId);
        attrs[attrs.length - 2] = new Attribute("urn:oasis:names:tc:xacml:3.0:attribute-category:resource", "urn:oasis:names:tc:xacml:1.0:resource:resource-id", ProxyConstants.DEFAULT_DATA_TYPE, resourceId);
        attrs[attrs.length - 1] = new Attribute("urn:oasis:names:tc:xacml:3.0:attribute-category:environment", "urn:oasis:names:tc:xacml:1.0:environment:environment-id", ProxyConstants.DEFAULT_DATA_TYPE, domainId);
        String xacmlRequest = XACMLRequetBuilder.buildXACML3Request(attrs);
        EntitlementThriftClient.Client client = getThriftClient(appId);
        Authenticator authenticator = getAuthenticator(serverUrl,userName,password);
        return (getDecision(xacmlRequest,client,authenticator.getSessionId(false))).contains("Permit");
    }

    @Override
    public List<String> getResourcesForAlias(String alias, String appId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getActionableResourcesForAlias(String alias, String appId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getActionsForResource(String alias, String resources, String appId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getActionableChildResourcesForAlias(String alias, String parentResource,
                                                           String action, String appId) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    private String getDecision(String xacmlRequest, EntitlementThriftClient.Client client, String sessionId) throws EntitlementProxyException{
        try {
            return client.getDecision(xacmlRequest, sessionId);
        } catch (TException e) {
            throw new EntitlementProxyException("Error while getting decision from PDP using ThriftEntitlementServiceClient", e);
        } catch (EntitlementException e) {
            throw new EntitlementProxyException("Error while getting decision from PDP using ThriftEntitlementServiceClient", e);
        }
    }

    private Authenticator getAuthenticator(String serverUrl, String userName, String password)
            throws Exception {
        if(reuseSession){
            if (authenticators.containsKey(serverUrl)) {
                return authenticators.get(serverUrl);
            }
        }
        Authenticator authenticator = new Authenticator(userName, password, serverUrl + "thriftAuthenticator");
        authenticators.put(serverUrl, authenticator);
        return authenticator;
    }

    private EntitlementThriftClient.Client getThriftClient(String appId) throws Exception {

        TSSLTransportFactory.TSSLTransportParameters param = new TSSLTransportFactory.TSSLTransportParameters();
        param.setTrustStore(trustStore, trustStorePass);
        TTransport transport;
        transport = TSSLTransportFactory.getClientSocket(thriftHost, thriftPort, ProxyConstants.THRIFT_TIME_OUT, param);
        TProtocol protocol = new TBinaryProtocol(transport);
        EntitlementThriftClient.Client client = new EntitlementThriftClient.Client(protocol);
        return client;
    }
}
