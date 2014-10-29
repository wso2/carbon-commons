/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.um.ws.api;

import java.util.Map;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.proxy.AuthenticationAdminClient;
import org.wso2.carbon.um.ws.api.internal.UserMgtWSAPIDSComponent;
import org.wso2.carbon.um.ws.api.stub.RealmConfigurationDTO;
import org.wso2.carbon.um.ws.api.stub.RemoteUserRealmServiceStub;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.claim.ClaimMapping;
import org.wso2.carbon.user.core.profile.ProfileConfiguration;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;

public class WSRealm implements UserRealm {

    private static Log log = LogFactory.getLog(WSRealm.class);

    private RealmConfiguration realmConfig = null;
    private WSUserStoreManager userStoreMan = null;
    private WSAuthorizationManager authzMan = null;
    private WSClaimManager claimManager = null;
    private WSProfileConfigurationManager profileManager = null;
    private static String sessionCookie = null;
    private int tenantId = -1;
    private RemoteUserRealmServiceStub stub = null;

    public WSRealm() {

    }

    /**
     * Initialize WSRealm by Carbon
     * @see org.wso2.carbon.user.core.UserRealm#init(org.wso2.carbon.user.api.RealmConfiguration, java.util.Map, int)
     */
    public void init(RealmConfiguration configBean, Map<String, Object> properties, int tenantId)
                                                                                                 throws UserStoreException {
        ConfigurationContext configCtxt =
                                          (ConfigurationContext) UserMgtWSAPIDSComponent.
                                                                                        getCcServiceInstance().
                                                                                        getClientConfigContext();
        init(configBean, configCtxt);
    }

    /**
     * Initialize WSRealm by Carbon
     * @see org.wso2.carbon.user.core.UserRealm#init(org.wso2.carbon.user.api.RealmConfiguration, java.util.Map, int)
     */
    public void init(RealmConfiguration configBean, Map<String, ClaimMapping> claimMapping,
                     Map<String, ProfileConfiguration> profileConfigs, int tenantId)
                                                                                    throws UserStoreException {
        ConfigurationContext configCtxt =
                                          (ConfigurationContext) UserMgtWSAPIDSComponent.
                                                                                        getCcServiceInstance().
                                                                                        getClientConfigContext();
        init(configBean, configCtxt);
    }
    

    /**
     * Initialize WSRealm by Non-carbon environment
     *
     */
    public void init(RealmConfiguration configBean, ConfigurationContext configCtxt)
                                                                             throws UserStoreException {
        realmConfig = configBean;

        if (sessionCookie == null) {
            synchronized (WSRealm.class) {
                if (sessionCookie == null) {
                    login();
                }
            }
        }

        if (sessionCookie == null) {
            throw new UserStoreException("Cannot create session for WSRealm");
        }

        init((String) realmConfig.getRealmProperty(WSRemoteUserMgtConstants.SERVER_URL),
             sessionCookie, configCtxt);
    }
    
    /**
     * Initialize WSRealm by Non-carbon environment
     *
     */
    void init(String url, String cookie, ConfigurationContext configCtxt)
                                                                                 throws UserStoreException {
        try {

            stub = new RemoteUserRealmServiceStub(configCtxt, url + "RemoteUserRealmService");

            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                               this
                                   .getSessionCookie());
        } catch (AxisFault e) {
            throw new UserStoreException();
        }

        userStoreMan = new WSUserStoreManager(url, cookie, configCtxt);
        authzMan = new WSAuthorizationManager(url, cookie, configCtxt);
        claimManager = new WSClaimManager(url, cookie, configCtxt);
        profileManager = new WSProfileConfigurationManager(url, cookie, configCtxt);
    }

    public UserStoreManager getUserStoreManager() throws UserStoreException {
        return userStoreMan;
    }

    public AuthorizationManager getAuthorizationManager() throws UserStoreException {
        return authzMan;
    }

    public ClaimManager getClaimManager() throws UserStoreException {
        return claimManager;
    }

    public ProfileConfigurationManager getProfileConfigurationManager() throws UserStoreException {
        return profileManager;
    }

    public RealmConfiguration getRealmConfiguration() throws UserStoreException {
        try {
            RealmConfigurationDTO realmConfig = stub.getRealmConfiguration();
            return WSRealmUtil.convertToRealmConfiguration(realmConfig);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new UserStoreException("Error getting realm config", e);
        }
    }

    public void cleanUp() throws UserStoreException {
        throw new UserStoreException(new UnsupportedOperationException("Not implemented"));
    }

    public void login() throws UserStoreException {
        String userName = realmConfig.getRealmProperty(WSRemoteUserMgtConstants.USER_NAME);
        String password = realmConfig.getRealmProperty(WSRemoteUserMgtConstants.PASSWORD);
        try {
            ConfigurationContext configContext = (ConfigurationContext) UserMgtWSAPIDSComponent
                    .getCcServiceInstance().getClientConfigContext();
            AuthenticationAdminClient client = new AuthenticationAdminClient(configContext,
                                                                             realmConfig.getRealmProperty(WSRemoteUserMgtConstants.SERVER_URL),
                                                                             sessionCookie, null, false);
            boolean isLogin = client.login(userName, password, "127.0.0.1"); // TODO
            if (isLogin) {
                sessionCookie = client.getAdminCookie();
            }
        } catch (Exception e) {
            throw new UserStoreException("Error" + e.getMessage(), e);
        }
    }

    public void lastAccess() {
    }

    public String getSessionCookie() {
        return sessionCookie;
    }

    protected int getTenantId() {
        return tenantId;
    }
}
