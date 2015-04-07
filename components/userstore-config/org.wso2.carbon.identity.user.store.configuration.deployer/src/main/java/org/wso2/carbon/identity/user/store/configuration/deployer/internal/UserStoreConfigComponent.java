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
package org.wso2.carbon.identity.user.store.configuration.deployer.internal;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.user.store.configuration.deployer.UserStoreConfgurationContextObserver;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component 
 *                name="identity.user.store.org.wso2.carbon.identity.user.store.configuration.component"
 *                immediate="true"
 * @scr.reference name="user.realmservice.default"
 *                interface="org.wso2.carbon.user.core.service.RealmService" cardinality="1..1"
 *                policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 * @scr.reference name="config.context.service"
 *                interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 *                policy="dynamic" bind="setConfigurationContextService"
 *                unbind="unsetConfigurationContextService"
 */
public class UserStoreConfigComponent {
    private static Log log = LogFactory.getLog(UserStoreConfigComponent.class);
    private static RealmService realmService = null;

    /**
     * @param ctxt
     */
    protected void activate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.info("Identity userstore bundle is activated.");
        }
//        BundleContext bundleCtx = ctxt.getBundleContext();
//        Dictionary properties = new Hashtable();
//        properties.put(CarbonConstants.AXIS2_CONFIG_SERVICE,
//                Axis2ConfigurationContextObserver.class.getName());
//        bundleCtx.registerService(Axis2ConfigurationContextObserver.class.getName(),
//                new UserStoreConfgurationContextObserver(), properties);
    }

    /**
     * @param ctxt
     */
    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.info("Identity Userstore-Config bundle is deactivated");
        }
    }

    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.info("Setting the Realm Service");
        }
        UserStoreConfigComponent.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.info("Unsetting the Realm Service");
        }
        UserStoreConfigComponent.realmService = null;
    }

    public static RealmService getRealmService() {
        return realmService;
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        if (log.isDebugEnabled()) {
            log.info("Setting the ConfigurationContextService");
        }
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        if (log.isDebugEnabled()) {
            log.info("Unsetting the ConfigurationContextService");
        }
    }

}
