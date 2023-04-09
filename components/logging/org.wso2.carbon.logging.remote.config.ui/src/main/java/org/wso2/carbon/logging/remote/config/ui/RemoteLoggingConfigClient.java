/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.logging.remote.config.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.logging.remote.config.stub.RemoteLoggingConfigStub;

/**
 * This is the Admin client used for updating Log4J2 appenders related to remote server configuration.
 */
public class RemoteLoggingConfigClient {
    private static final Log log = LogFactory.getLog(RemoteLoggingConfigClient.class);

    public RemoteLoggingConfigStub stub;

    public RemoteLoggingConfigClient(String cookie, String backendServerURL,
                               ConfigurationContext configCtx) throws AxisFault {

        String serviceURL = backendServerURL + "LoggingConfig";
        stub = new RemoteLoggingConfigStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public void addRemoteServerConfig(String url, String connectTimeoutMillis, boolean auditLogTypeStatus,
            boolean apiLogTypeStatus, boolean carbonLogTypeStatus) throws Exception {
        try {
            stub.addRemoteServerConfig(url, connectTimeoutMillis, auditLogTypeStatus, apiLogTypeStatus,
                    carbonLogTypeStatus);
        } catch (Exception e) {
            String msg = "Error occurred while adding remote server configuration.";
            log.error(msg, e);
            throw e;
        }
    }
}
