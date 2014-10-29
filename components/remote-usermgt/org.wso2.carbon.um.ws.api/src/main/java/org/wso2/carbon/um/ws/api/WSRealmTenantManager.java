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
package org.wso2.carbon.um.ws.api;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.authenticator.proxy.AuthenticationAdminClient;
import org.wso2.carbon.core.util.AnonymousSessionUtil;
import org.wso2.carbon.um.ws.api.internal.UserMgtWSAPIDSComponent;
import org.wso2.carbon.um.ws.api.stub.RemoteTenantManagerServiceStub;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.user.core.tenant.TenantManager;

/**
 * This is the Tenant manager used with WSRealm.
 */
public class WSRealmTenantManager implements TenantManager {

    private static Log log = LogFactory.getLog(AnonymousSessionUtil.class);

    private RemoteTenantManagerServiceStub stub;
    private String userName = null;
    private String password = null;
    private String url = null;

    public WSRealmTenantManager(String userName, String password, String url)
            throws java.lang.Exception {
        this.userName = userName;
        this.password = password;
        this.url = url;
        stub = this.getStub();
    }

    public void activateTenant(int tenantId) throws UserStoreException {
        try {
            getStub().activateTenant(tenantId);
        } catch (Exception e) {
            handleException("", e);
        }
    }

    public int addTenant(Tenant tenant) throws UserStoreException {
        try {
            return getStub().addTenant(this.tenantToADBTenant(tenant));
        } catch (Exception e) {
            handleException("", e);
        }
        return -1;
    }

    public void deactivateTenant(int tenantId) throws UserStoreException {
        try {
            getStub().deactivateTenant(tenantId);
        } catch (Exception e) {
            handleException("", e);
        }
    }

    public void deleteTenant(int tenantId) throws UserStoreException {

        try {
            getStub().deleteTenant(tenantId);
        } catch (Exception e) {
            handleException("", e);
        }
    }

    public Tenant[] getAllTenants() throws UserStoreException {
        try {
            org.wso2.carbon.um.ws.api.stub.Tenant[] tenats = stub.getAllTenants();
            if (tenats != null) {
                Tenant[] ts = new Tenant[tenats.length];
                for (int i = 0; i < tenats.length; i++) {
                    ts[i] = this.ADBTenantToTenant(tenats[i]);
                }
                return ts;
            }
        } catch (Exception e) {
            handleException("", e);
        }
        return new Tenant[0];
    }

    //TODO:implement methods
    public org.wso2.carbon.user.api.Tenant[] getAllTenantsForTenantDomainStr(String s)
            throws org.wso2.carbon.user.api.UserStoreException {
        return new org.wso2.carbon.user.api.Tenant[0];
    }

    public String getDomain(int tenantId) throws UserStoreException {
        try {
            return getStub().getDomain(tenantId);
        } catch (Exception e) {
            handleException("", e);
        }
        return null;
    }

    public String getSuperTenantDomain() throws UserStoreException {
        try {
            return getStub().getSuperTenantDomain();
        } catch (Exception e) {
            handleException("", e);
        }
        return null;
    }

    //TODO:implement methods
    public String[] getAllTenantDomainStrOfUser(String s)
            throws org.wso2.carbon.user.api.UserStoreException {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int addTenant(org.wso2.carbon.user.api.Tenant tenant)
            throws org.wso2.carbon.user.api.UserStoreException {
        return addTenant((Tenant) tenant);
    }

    public void updateTenant(org.wso2.carbon.user.api.Tenant tenant)
            throws org.wso2.carbon.user.api.UserStoreException {
        updateTenant((Tenant) tenant);
    }

    public Tenant getTenant(int tenantId) throws UserStoreException {
        try {
            return this.ADBTenantToTenant(getStub().getTenant(tenantId));
        } catch (Exception e) {
            handleException("", e);
        }
        return null;
    }

    public int getTenantId(String domain) throws UserStoreException {
        try {
            return getStub().getTenantId(domain);
        } catch (Exception e) {
            handleException("", e);
        }
        return 0;
    }

    public boolean isTenantActive(int tenantId) throws UserStoreException {
        try {
            return getStub().isTenantActive(tenantId);
        } catch (Exception e) {
            handleException("", e);
        }
        return false;
    }

    public void setBundleContext(BundleContext bundleContext) {
    }

    public void updateTenant(Tenant tenant) throws UserStoreException {
        try {
            getStub().updateTenant(this.tenantToADBTenant(tenant));
        } catch (Exception e) {
            handleException("", e);
        }
    }

    private String[] handleException(String msg, Exception e) throws UserStoreException {
        log.error(msg, e);
        throw new UserStoreException(msg, e);
    }

    private Tenant ADBTenantToTenant(org.wso2.carbon.um.ws.api.stub.Tenant stubTenant) {
        Tenant tenant = new Tenant();
        tenant.setActive(stubTenant.getActive());
        tenant.setAdminName(stubTenant.getAdminName());
        tenant.setAdminPassword(stubTenant.getAdminPassword());
        tenant.setDomain(stubTenant.getDomain());
        tenant.setEmail(stubTenant.getEmail());
        tenant.setId(stubTenant.getId());
        return tenant;
    }

    private org.wso2.carbon.um.ws.api.stub.Tenant tenantToADBTenant(Tenant tenant) {
        org.wso2.carbon.um.ws.api.stub.Tenant stubTenant = new org.wso2.carbon.um.ws.api.stub.Tenant();
        stubTenant.setActive(tenant.isActive());
        stubTenant.setAdminName(tenant.getAdminName());
        stubTenant.setAdminPassword(tenant.getAdminPassword());
        stubTenant.setDomain(tenant.getDomain());
        stubTenant.setEmail(tenant.getEmail());
        stubTenant.setId(tenant.getId());
        return stubTenant;
    }

    private RemoteTenantManagerServiceStub getStub() throws Exception {
        if (stub == null) {
            stub = new RemoteTenantManagerServiceStub(UserMgtWSAPIDSComponent
                    .getCcServiceInstance().getClientConfigContext(), url
                    + "RemoteTenantManagerService");
            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            LoginSender sender = new LoginSender();
            String sessionCookie = sender.login();
            if (sessionCookie == null) {
                log.error("WSRealmTenantManager cannot login to server");
                throw new Exception("WSRealmTenantManager cannot login to server");
            }
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                    sessionCookie);
            //Timer timer = new Timer();
           // timer.scheduleAtFixedRate(sender, 10000, 10000);
        }
        return stub;
    }

    /**
     * This method was added to TenantManager interface to support tenant management with LDAP.
     * Hence no implementation currently in this context.
     */
    public void initializeExistingPartitions() {
        
    }

    private class LoginSender extends TimerTask {

        public void run() {
            try {
                synchronized (stub) {
                    String sessionCookie = login();
                    ServiceClient client = stub._getServiceClient();
                    Options option = client.getOptions();
                    option.setManageSession(true);
                    option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                            sessionCookie);
                }
            } catch (UserStoreException e) {
                log.error("Error login in tenant manager", e);
            }
        }

        public String login() throws UserStoreException {
            try {
                synchronized (stub) {
                    AuthenticationAdminClient client = new AuthenticationAdminClient(
                            UserMgtWSAPIDSComponent.getCcServiceInstance().getClientConfigContext(),
                            url, null, null, false);
                    boolean isLogin = client.login(userName, password, "127.0.0.1");
                    if (isLogin) {
                        return client.getAdminCookie();
                    } else {
                        log.error("Error login in tenant manager");
                        throw new UserStoreException("Error login in tenant manager");
                    }
                }
            } catch (Exception e) {
                log.error("Error login in tenant manager", e);
                throw new UserStoreException("Error" + e.getMessage(), e);
            }
        }

    }
}
