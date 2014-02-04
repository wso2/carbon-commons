/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.discovery.ui.client;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Options;
import org.wso2.carbon.discovery.stub.types.DiscoveryAdminStub;
import org.wso2.carbon.discovery.stub.types.mgt.DiscoveryProxyDetails;
import org.wso2.carbon.discovery.stub.types.mgt.TargetServiceDetails;
import org.wso2.carbon.discovery.stub.types.mgt.ProbeDetails;

import javax.xml.namespace.QName;
import java.util.*;
import java.net.URI;

public class DiscoveryAdminClient {

    private static final String BUNDLE = "org.wso2.carbon.discovery.ui.i18n.Resources";
    private ResourceBundle bundle;
    public DiscoveryAdminStub stub;

    public DiscoveryAdminClient(ConfigurationContext configCtx, String backendServerURL,
                                   String cookie, Locale locale) throws AxisFault {
        bundle = ResourceBundle.getBundle(BUNDLE, locale);

        String serviceURL = backendServerURL + "DiscoveryAdmin";
        stub = new DiscoveryAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public void addDiscoveryProxy(String name, String url, String policy) throws Exception {
        if (name == null || "".equals(name)) {
            throw new Exception("The name has not been specified for the discovery proxy");
        }

        if (url == null || "".equals(url)) {
            throw new Exception("The discovery proxy URL has not been specified");
        }

        DiscoveryProxyDetails pd = new DiscoveryProxyDetails();
        pd.setName(name);
        pd.setUrl(url);

        if (policy != null && !"".equals(policy)) {
            pd.setPolicy(getPolicy(policy));
        }
        stub.addDiscoveryProxy(pd);
    }

    private String getPolicy(String policy) {
        String prefix = "/_system/config/";
        if (policy.startsWith(prefix)) {
            return policy.substring(prefix.length());
        }
        return policy;
    }

    public Map<String,DiscoveryProxyDetails> getDiscoveryProxies() throws Exception {
       DiscoveryProxyDetails[] proxyData = stub.getDiscoveryProxies();
        if (proxyData == null || proxyData.length == 0 || proxyData[0] == null) {
            return null;
        }

        Map<String,DiscoveryProxyDetails> proxyMap = new TreeMap<String,DiscoveryProxyDetails>();
        for (DiscoveryProxyDetails proxy : proxyData) {
            String key = proxy.getName();
            proxyMap.put(key, proxy);
        }
        return proxyMap;
    }

    public void removeDiscoveryProxy(String name) throws Exception {
        if (name == null || "".equals(name)) {
            return;
        }

        stub.removeDiscoveryProxy(name);
    }

    public DiscoveryProxyDetails getDiscoveryProxy(String name) throws Exception {
        return stub.getDiscoveryProxy(name);
    }

    public void updateDiscoveryProxy(String name, String url, String policy) throws Exception {
        if (name == null || "".equals(name)) {
            throw new Exception("The name has not been specified for the discovery proxy");
        }

        if (url == null || "".equals(url)) {
            throw new Exception("The discovery proxy URL has not been specified");
        }

        DiscoveryProxyDetails pd = new DiscoveryProxyDetails();
        pd.setName(name);
        pd.setUrl(url);
        if (policy != null && !"".equals(policy)) {
            pd.setPolicy(getPolicy(policy));
        }
        stub.updateDiscoveryProxy(pd);
    }

    public TargetServiceDetails[] probeDiscoveryProxy(String name,
                                                  QName[] types, URI[] scopes) throws Exception {
        if (name == null || "".equals(name)) {
            throw new Exception("The name has not been specified for the discovery proxy");
        }

        ProbeDetails probe = new ProbeDetails();
        if (types != null) {
            String[] typeValues = new String[types.length];
            for (int i = 0; i < types.length; i++) {
                typeValues[i] = types[i].toString();
            }
            probe.setTypes(typeValues);
        }

        if (scopes != null) {
            String[] scopeValues = new String[scopes.length];
            for (int i = 0; i < scopes.length; i++) {
                scopeValues[i] = scopes[i].toString();
            }
            probe.setScopes(scopeValues);
        }

        TargetServiceDetails[] details = stub.probeDiscoveryProxy(name, probe);
        if (details == null || details.length == 0 || details[0] == null) {
            return null;
        }
        return details;
    }

    public TargetServiceDetails resolveTargetService(String name,
                                                     String serviceId) throws Exception {

        if (name == null || "".equals(name)) {
            throw new Exception("The name has not been specified for the discovery proxy");
        }

        if (serviceId == null || "".equals(serviceId)) {
            throw new Exception("The service ID has not been specified");
        }

        return stub.resolveTargetService(name, serviceId);
    }
}
