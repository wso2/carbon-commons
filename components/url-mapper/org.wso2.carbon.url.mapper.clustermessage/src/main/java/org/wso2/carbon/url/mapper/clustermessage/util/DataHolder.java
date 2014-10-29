/*
*  Copyright (c), WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*
*/
package org.wso2.carbon.url.mapper.clustermessage.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.tomcat.api.CarbonTomcatService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 *   DataHolder for cluster message component
 */
public class DataHolder {

    private static final Log log = LogFactory.getLog(DataHolder.class);
    private static DataHolder dataHolder = new DataHolder();
    private RegistryService registryService;
    private CarbonTomcatService carbonTomcatService;
    private ConfigurationContextService configurationContextService;

    private DataHolder() {
    }

    public static DataHolder getInstance() {
        return dataHolder;
    }

    public void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        this.configurationContextService = configurationContextService;
    }

    public ConfigurationContextService getConfigurationContextService() {
        return this.configurationContextService;
    }

    public void setCarbonTomcatService(CarbonTomcatService carbonTomcatService) {
         this.carbonTomcatService = carbonTomcatService;
    }

    public CarbonTomcatService getCarbonTomcatService() {
        return this.carbonTomcatService;
    }

    public void registerRetrieverServices(BundleContext bundleContext) throws Exception {
        ConfigurationContextService configCtxSvc = this.getConfigurationContextService();
    }
}
