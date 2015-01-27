/*
 * Copyright WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.url.mapper.internal.util;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.tomcat.api.CarbonTomcatService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * Holds data belonging to this url-mapper component
 */
public class DataHolder {
    private static DataHolder dataHolder = new DataHolder();
    private RealmService realmService;
    private CarbonTomcatService carbonTomcatService;
    private Registry registry;
    private ConfigurationContext contextService;

    public static DataHolder getInstance() {
        return dataHolder;
    }

    private DataHolder() {
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    public RealmService getRealmService() {
        return realmService;
    }
    
    public void setServerConfigContext(ConfigurationContext configContext) {
         this.contextService = configContext;
    }

    public ConfigurationContext getServerConfigContext() {
        return this.contextService;
    }

    public void setCarbonTomcatService(CarbonTomcatService carbonTomcatService) {
        this.carbonTomcatService = carbonTomcatService;
    }

    public CarbonTomcatService getCarbonTomcatService() {
        return this.carbonTomcatService;
    }

    public void setRegistry(Registry registryParam) {
        registry = registryParam;
    }

    public Registry getRegistry() {
        return registry;
    }
}
