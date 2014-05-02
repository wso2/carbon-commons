/*
* Copyright 2004,2013 The Apache Software Foundation.
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
package org.wso2.carbon.discovery.cxf.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.discovery.cxf.CXFServiceInfo;

import java.util.LinkedList;
import java.util.Queue;

public class CxfDiscoveryDataHolder {

    private static CxfDiscoveryDataHolder instance = new CxfDiscoveryDataHolder();

    private ConfigurationContext mainServerConfigContext;
    private Queue<CXFServiceInfo> initialMessagesList;

    private ConfigurationContext clientCfgCtx;
    private ConfigurationContext serverCfgCtx;


    public static CxfDiscoveryDataHolder getInstance() {
        return instance;
    }

    private CxfDiscoveryDataHolder() {

    }

    public void setMainServerConfigContext(ConfigurationContext mainServerConfigContext) {
        this.mainServerConfigContext = mainServerConfigContext;
    }

    public ConfigurationContext getMainServerConfigContext() {
        return mainServerConfigContext;
    }

    public Queue<CXFServiceInfo> getInitialMessagesList() {
        if (initialMessagesList == null) {
            initialMessagesList = new LinkedList<CXFServiceInfo>();
        }
        return initialMessagesList;
    }

    public void setInitialMessagesList(Queue<CXFServiceInfo> initialMessagesList) {
        this.initialMessagesList.addAll(initialMessagesList);
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
