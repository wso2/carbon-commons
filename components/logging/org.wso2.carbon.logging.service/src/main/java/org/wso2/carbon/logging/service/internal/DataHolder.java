/*
 * Copyright 2005,2014 WSO2, Inc. http://www.wso2.org
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

package org.wso2.carbon.logging.service.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.databridge.agent.thrift.AgentHolder;
import org.wso2.carbon.databridge.agent.thrift.Agent;

public class DataHolder {
    private static DataHolder dataHolder = new DataHolder();
    private RealmService realmService;
    private Registry registry;
    private ConfigurationContext contextService;
	private Agent agent;
	private boolean isAgentInitialized;

	public Agent getAgent() {
		if (!isAgentInitialized && agent == null) {
			synchronized (this) {
				if (!isAgentInitialized && agent == null) {
					agent = AgentHolder.getOrCreateAgent();
					isAgentInitialized = true;
				}
			}
		}
		return agent;
	}

	public void setAgent(Agent agent) {
		this.agent = agent;
	}
    private DataHolder() {
    }

    public static DataHolder getInstance() {
        return dataHolder;
    }

    public RealmService getRealmService() {
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    public ConfigurationContext getServerConfigContext() {
        return this.contextService;
    }

    public void setServerConfigContext(ConfigurationContext configContext) {
        this.contextService = configContext;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registryParam) {
        registry = registryParam;
    }
}
