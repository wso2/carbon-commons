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

package org.wso2.carbon.event.core.internal.util;

import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.api.UserRealmService;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.event.core.qpid.QpidServerDetails;

@Deprecated
public class EventBrokerHolder {

    private static EventBrokerHolder instance = new EventBrokerHolder();

    private RegistryService registryService;
    private QpidServerDetails qpidServerDetails;
    private UserRealmService realmService;
    private ConfigurationContextService configurationContextService;

    private EventBrokerHolder() {
    }

    public static EventBrokerHolder getInstance(){
        return instance;
    }

    public RegistryService getRegistryService(){
        return this.registryService;
    }

    public void registerRegistryService(RegistryService registryService){
        this.registryService = registryService;
    }

    public QpidServerDetails getQpidServerDetails(){
        return this.qpidServerDetails;
    }

    public void registerQpidServerDetails(QpidServerDetails qpidServerDetails){
        this.qpidServerDetails = qpidServerDetails;
    }

    public void registerRealmService(UserRealmService realmService){
        this.realmService = realmService;
    }

    public UserRealmService getRealmService(){
        return this.realmService;
    }

    public ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public void registerConfigurationContextService(ConfigurationContextService configurationContextService) {
        this.configurationContextService = configurationContextService;
    }

    public int getTenantId() {
        return CarbonContext.getThreadLocalCarbonContext().getTenantId();

    }

    public String getTenantDomain() {
        return CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }



}
