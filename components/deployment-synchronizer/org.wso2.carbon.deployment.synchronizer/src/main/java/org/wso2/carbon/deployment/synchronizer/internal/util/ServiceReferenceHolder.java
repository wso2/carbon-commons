/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.deployment.synchronizer.internal.util;

import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.eventing.services.EventingService;
import org.wso2.carbon.utils.ConfigurationContextService;

public final class ServiceReferenceHolder {

    private static ConfigurationContextService configurationContextService;
    private static RegistryService registryService;
    private static EventingService eventingService;

    private ServiceReferenceHolder() {

    }

    public static ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public static void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        ServiceReferenceHolder.configurationContextService = configurationContextService;
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static void setRegistryService(RegistryService registryService) {
        ServiceReferenceHolder.registryService = registryService;
    }

    public static EventingService getEventingService() {
        return eventingService;
    }

    public static void setEventingService(EventingService eventingService) {
        ServiceReferenceHolder.eventingService = eventingService;
    }
}
