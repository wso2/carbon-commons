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

package org.wso2.carbon.discovery.util;

import org.wso2.carbon.registry.core.Registry;
import org.apache.axis2.context.ConfigurationContext;

/**
 * Keeps references to various registries and configuration context
 * instanced during the life time of this module.
 */
public class ConfigHolder {

    private static final ConfigHolder INSTANCE = new ConfigHolder();

    private ConfigurationContext clientCfgCtx;
    private ConfigurationContext serverCfgCtx;

    private ConfigHolder() {

    }

    public static ConfigHolder getInstance() {
        return INSTANCE;
    }

    public ConfigurationContext getClientConfigurationContext() {
        return clientCfgCtx;
    }

    public void setClientConfigurationContext(ConfigurationContext clientCfgCtx) {
        this.clientCfgCtx = clientCfgCtx;
    }

    public ConfigurationContext getServerConfigurationContext() {
        return serverCfgCtx;
    }

    public void setServerConfigurationContext(ConfigurationContext serverCfgCtx) {
        this.serverCfgCtx = serverCfgCtx;
    }


}
