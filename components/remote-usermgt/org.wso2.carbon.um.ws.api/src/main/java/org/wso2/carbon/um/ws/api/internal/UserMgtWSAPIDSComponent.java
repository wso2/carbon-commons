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
package org.wso2.carbon.um.ws.api.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.um.ws.api.*;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="remote.um.api.component" immediate="true"
 * @scr.reference name="config.context.service"
 *                interface="org.wso2.carbon.utils.ConfigurationContextService"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setConfigurationContextService"
 *                unbind="unsetConfigurationContextService"
 */
public class UserMgtWSAPIDSComponent {

    private static Log log = LogFactory.getLog(UserMgtWSAPIDSComponent.class);
    private static ConfigurationContextService ccServiceInstance = null;

    protected void activate(ComponentContext ctxt) {
        log.debug("Remote User Mgt bundle is activated ");
    }

    protected void deactivate(ComponentContext ctxt) {

    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        UserMgtWSAPIDSComponent.ccServiceInstance = contextService;
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        UserMgtWSAPIDSComponent.ccServiceInstance = null;
    }

    public static ConfigurationContextService getCcServiceInstance() {
        return ccServiceInstance;
    }
    
}
