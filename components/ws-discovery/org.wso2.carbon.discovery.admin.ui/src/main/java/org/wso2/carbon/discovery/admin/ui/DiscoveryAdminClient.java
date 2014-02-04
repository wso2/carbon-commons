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

package org.wso2.carbon.discovery.admin.ui;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Options;
import org.wso2.carbon.discovery.stub.types.DiscoveryAdminStub;
import org.wso2.carbon.discovery.stub.types.mgt.ServiceDiscoveryConfig;

import java.util.ResourceBundle;
import java.util.Locale;

public class DiscoveryAdminClient {

    private static final String BUNDLE = "org.wso2.carbon.discovery.admin.ui.i18n.Resources";
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

    public ServiceDiscoveryConfig getDiscoveryConfig() throws Exception {
        return stub.getServiceDiscoveryConfig();
    }

    public void enableServiceDiscovery(String proxyURL) throws Exception {
        if (proxyURL == null || "".equals(proxyURL)) {
            throw new Exception("Discovery proxy URL has not been specified");
        }

        stub.enableServiceDiscovery(proxyURL);
    }

    public void disableServiceDiscovery(boolean sendBye) throws Exception {
        stub.disableServiceDiscovery(sendBye);
    }
}
