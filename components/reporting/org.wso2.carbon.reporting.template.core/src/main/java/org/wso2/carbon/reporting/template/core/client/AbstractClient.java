/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.reporting.template.core.client;

import org.wso2.carbon.reporting.template.core.internal.ReportingTemplateComponent;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.NetworkUtils;

import java.net.SocketException;

public class AbstractClient {

    protected String getBackendServerURLHTTP() throws SocketException {
        String contextRoot = ReportingTemplateComponent.getConfigurationContextService().getServerConfigContext().getContextRoot();
        return "http://" + NetworkUtils.getLocalHostname() + ":" +
                CarbonUtils.getTransportPort(ReportingTemplateComponent.getConfigurationContextService(), "http") + contextRoot;

    }

    protected String getBackendServerURLHTTPS() throws SocketException {
        String contextRoot = ReportingTemplateComponent.getConfigurationContextService().getServerConfigContext().getContextRoot();
        return "https://" + NetworkUtils.getLocalHostname() + ":" +
                CarbonUtils.getTransportPort(ReportingTemplateComponent.getConfigurationContextService(), "https") + contextRoot;

    }
}
