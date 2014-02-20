/*
*Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

/**
 * @scr.component name="identity.provider.mgt.service.component"" immediate="true"
 *
 */
package org.wso2.carbon.idp.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.idp.mgt.persistence.JDBCPersistenceManager;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="idp.mgt.dscomponent" immediate=true
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService" cardinality="1..1"
 * policy="dynamic" bind="setRealmService"
 * unbind="unsetRealmService"
 */
public class IdentityProviderMgtServiceComponent {

    private static Log log = LogFactory.getLog(IdentityProviderMgtServiceComponent.class);

    private static RealmService realmService = null;

    protected void activate(ComponentContext ctxt) {
        try {
            BundleContext bundleCtx = ctxt.getBundleContext();

            IdPMgtTenantMgtListener idPMgtTenantMgtListener = new IdPMgtTenantMgtListener();
            ServiceRegistration tenantMgtListenerSR = bundleCtx.registerService(TenantMgtListener.class.getName(), idPMgtTenantMgtListener, null);
            if(tenantMgtListenerSR != null){
                log.debug("Identity Provider Management TenantMgtListener registered");
            } else {
                log.error("Identity Provider Management TenantMgtListener could not be registered");
            }

            ServiceRegistration userOperationListenerSR = bundleCtx.registerService(UserOperationEventListener.class.getName(), new IdPMgtUserStoreListener(), null);
            if(userOperationListenerSR != null){
                log.debug("Identity Provider Management UserStoreListener is enabled");
            } else {
                log.error("Identity Provider Management UserStoreListener could not be registered");
            }

            // initialize the identity persistence manager, if it is not already initialized.
            JDBCPersistenceManager jdbcPersistenceManager = JDBCPersistenceManager.getInstance();
            jdbcPersistenceManager.initializeDatabase();

            log.debug("Identity Provider Management bundle is activated");
        } catch (Throwable e){
            log.error("Error while activating Identity Provider Management bundle", e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        log.debug("Identity Provider Management bundle is deactivated");
    }

    public static RealmService getRealmService() {
        return realmService;
    }

    protected void setRealmService(RealmService rlmService) {
        realmService = rlmService;
    }

    protected void unsetRealmService(RealmService realmService) {
        realmService = null;
    }
}
