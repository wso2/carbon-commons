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
package org.wso2.carbon.transaction.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.transaction.manager.exception.TransactionManagerException;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import java.util.ArrayList;
import java.util.List;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "transactionmanager.component", 
         immediate = true)
public class TransactionManagerComponent {

    private static Log log = LogFactory.getLog(TransactionManagerComponent.class);

    private static TransactionManager txManager;

    private static UserTransaction userTransaction;

    private static RealmService realmService;

    /* class level lock for controlling synchronized access to static variables */
    private static Object txManagerComponentLock = new Object();

    @Activate
    protected void activate(ComponentContext ctxt) throws TransactionManagerException {
        BundleContext bundleContext = ctxt.getBundleContext();
        bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(), new TransactionManagerAxis2ConfigurationContextObserver(), null);
        // Register transaction-manager with JNDI for all available tenants.
        List<Integer> tenants = this.getAllTenantIds();
        for (int tid : tenants) {
            bindTransactionManagerWithJNDIForTenant(tid);
        }
        if (log.isDebugEnabled()) {
            log.debug("Transaction Manager bundle is activated ");
        }
        bundleContext.registerService(TransactionManagerDummyService.class.getName(), new TransactionManagerDummyService(), null);
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Transaction Manager bundle is deactivated ");
        }
    }

    @Reference(
             name = "transactionmanager", 
             service = javax.transaction.TransactionManager.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetTransactionManager")
    protected void setTransactionManager(TransactionManager txManager) {
        synchronized (txManagerComponentLock) {
            if (log.isDebugEnabled()) {
                log.debug("Setting the Transaction Manager Service");
            }
            TransactionManagerComponent.txManager = txManager;
        }
    }

    protected void unsetTransactionManager(TransactionManager txManager) {
        synchronized (txManagerComponentLock) {
            if (log.isDebugEnabled()) {
                log.debug("Unsetting the Transaction Manager Service");
            }
            TransactionManagerComponent.txManager = null;
        }
    }

    public static TransactionManager getTransactionManager() {
        return txManager;
    }

    @Reference(
             name = "usertransaction", 
             service = javax.transaction.UserTransaction.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetUserTransaction")
    protected void setUserTransaction(UserTransaction userTransaction) {
        synchronized (txManagerComponentLock) {
            if (log.isDebugEnabled()) {
                log.debug("Setting the UserTransaction Service");
            }
            TransactionManagerComponent.userTransaction = userTransaction;
        }
    }

    protected void unsetUserTransaction(UserTransaction userTransaction) {
        synchronized (txManagerComponentLock) {
            if (log.isDebugEnabled()) {
                log.debug("Unsetting the UserTransaction Service");
            }
            TransactionManagerComponent.userTransaction = null;
        }
    }

    public static UserTransaction getUserTransaction() {
        return userTransaction;
    }

    private List<Integer> getAllTenantIds() throws TransactionManagerException {
        try {
            List<Integer> tids = new ArrayList<Integer>();
            RealmService realmService = TransactionManagerComponent.getRealmService();
            if (realmService != null) {
                Tenant[] tenants = TransactionManagerComponent.getRealmService().getTenantManager().getAllTenants();
                for (Tenant tenant : tenants) {
                    tids.add(tenant.getId());
                }
                tids.add(MultitenantConstants.SUPER_TENANT_ID);
                return tids;
            } else {
                tids.add(MultitenantConstants.SUPER_TENANT_ID);
                return tids;
            }
        } catch (UserStoreException e) {
            log.error(e);
            throw new TransactionManagerException("Error in listing all the tenants", e);
        }
    }

    @Reference(
             name = "user.realmservice.default", 
             service = org.wso2.carbon.user.core.service.RealmService.class, 
             cardinality = ReferenceCardinality.OPTIONAL, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        TransactionManagerComponent.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {
        TransactionManagerComponent.realmService = null;
    }

    public static RealmService getRealmService() {
        return TransactionManagerComponent.realmService;
    }

    protected static void bindTransactionManagerWithJNDIForTenant(int tid) {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tid);
        try {
            Context currentCtx = PrivilegedCarbonContext.getThreadLocalCarbonContext().getJNDIContext();
            Context javaCtx = null;
            try {
                javaCtx = (Context) currentCtx.lookup("java:comp");
            } catch (NameNotFoundException ignore) {
            // ignore
            }
            if (javaCtx == null) {
                currentCtx = currentCtx.createSubcontext("java:comp");
            }
            Object txManager = null, userTx = null;
            try {
                txManager = currentCtx.lookup("java:comp/TransactionManager");
            } catch (NameNotFoundException ignore) {
            // ignore
            }
            try {
                userTx = currentCtx.lookup("java:comp/UserTransaction");
            } catch (NameNotFoundException ignore) {
            // ignore
            }
            if (txManager == null) {
                currentCtx.bind("TransactionManager", getTransactionManager());
            }
            if (userTx == null) {
                currentCtx.bind("UserTransaction", getUserTransaction());
            }
        } catch (Exception e) {
            log.error("Error in binding transaction manager for tenant: " + tid, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
}
