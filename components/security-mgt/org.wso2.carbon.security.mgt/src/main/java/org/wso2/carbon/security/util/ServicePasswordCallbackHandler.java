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

package org.wso2.carbon.security.util;

import org.apache.axiom.om.OMAttribute;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.rampart.policy.model.KerberosConfig;
import org.apache.ws.security.WSPasswordCallback;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.core.persistence.PersistenceException;
import org.wso2.carbon.core.persistence.PersistenceFactory;
import org.wso2.carbon.core.persistence.file.ServiceGroupFilePersistenceManager;
import org.wso2.carbon.core.util.AnonymousSessionUtil;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.security.SecurityConfigException;
import org.wso2.carbon.security.SecurityConstants;
import org.wso2.carbon.security.SecurityServiceHolder;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.TenantUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;
import java.security.KeyStore;

/**
 * The password callback handler to be used to enable UsernameToken
 * authentication for services.
 */
public class ServicePasswordCallbackHandler implements CallbackHandler {
    private static final Log log = LogFactory.getLog(ServicePasswordCallbackHandler.class);

    private String serviceGroupId = null;
    private String serviceId = null;
    //private Map<String, UserListData> users = new HashMap<String, UserListData>();
    private Registry registry = null;
    private UserRealm realm = null;
    private String serviceXPath = null;
    private String registryServicePath = null;
    private PersistenceFactory persistenceFactory;

    //todo there's a API change here. apparently only security component uses this. If not, change the invocations accordingly.
    public ServicePasswordCallbackHandler(PersistenceFactory persistenceFactory, String serviceGroupId, String serviceId, String serviceXPath,
                                          String registryServicePath, Registry registry, UserRealm realm)
            throws RegistryException, SecurityConfigException {
        this.registry = registry;
        this.serviceId = serviceId;
        this.serviceGroupId = serviceGroupId;
        this.realm = realm;
        this.serviceXPath = serviceXPath;
        this.registryServicePath = registryServicePath;
        this.persistenceFactory = persistenceFactory;
    }

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        try { 
            for (int i = 0; i < callbacks.length; i++) {
                if (callbacks[i] instanceof WSPasswordCallback) {
                    WSPasswordCallback passwordCallback = (WSPasswordCallback) callbacks[i];

                    String username = passwordCallback.getIdentifer();
                    String receivedPasswd = null;
                    switch (passwordCallback.getUsage()) {

                    case WSPasswordCallback.SIGNATURE:
                    case WSPasswordCallback.DECRYPT:
                        String password = getPrivateKeyPassword(username);
                        if (password == null) {
                            throw new UnsupportedCallbackException(callbacks[i],
                                    "User not available " + "in a trusted store");
                        }

                        passwordCallback.setPassword(password);

                        break;
                    case WSPasswordCallback.KERBEROS_TOKEN:
                        passwordCallback.setPassword(getServicePrincipalPassword());
                        break;
                    case WSPasswordCallback.USERNAME_TOKEN_UNKNOWN:

                        receivedPasswd = passwordCallback.getPassword();
                        try {
                            if (receivedPasswd != null
                                    && this.authenticateUser(username, receivedPasswd)) {
                                // do nothing things are fine
                            } else {
                                throw new UnsupportedCallbackException(callbacks[i], "check failed");
                            }
                        } catch (Exception e) {
                            throw new UnsupportedCallbackException(callbacks[i],
                                    "Check failed : System error");
                        }

                        break;
                    default:

                        /*
                         * When the password is null WS4J reports an error
                         * saying no password available for the user. But its
                         * better if we simply report authentication failure
                         * Therefore setting the password to be the empty string
                         * in this situation.
                         */

                        passwordCallback.setPassword(receivedPasswd);
                        break;

                    }

                } else {
                    throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
                }
            }
        } catch (UnsupportedCallbackException e) {
            if(log.isDebugEnabled()){
                log.debug(e.getMessage(), e); //logging invlaid passwords and attempts
                throw e;
            }
            throw e;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            //can't build an unsupported exception.
            throw new UnsupportedCallbackException(null, e.getMessage());
        }
    }

    private String getServicePrincipalPassword() throws PersistenceException, SecurityConfigException {
        String kerberosPath = this.serviceXPath + "/" + RampartConfigUtil.KERBEROS_CONFIG_RESOURCE;
        String servicePrincipalPasswordResource = kerberosPath + "/@" + KerberosConfig.SERVICE_PRINCIPLE_PASSWORD;

        ServiceGroupFilePersistenceManager sfpm = persistenceFactory.getServiceGroupFilePM();
        if (!sfpm.elementExists(serviceGroupId, servicePrincipalPasswordResource)) {
            String msg = "Unable to find service principle password registry resource in path "
                    + servicePrincipalPasswordResource;
            log.error(msg);
            throw new SecurityConfigException(msg);
        }

        OMAttribute principalPassword = sfpm.getAttribute(serviceGroupId,
                servicePrincipalPasswordResource);
        if (principalPassword != null) {
            String password = principalPassword.getAttributeValue();
            if (password != null) {
                password = getDecryptedPassword(password);
                return password;
            } else {
                StringBuilder msg = new StringBuilder("Retrieved principal password is null in registry path ")
                        .append(servicePrincipalPasswordResource).append(" for property ")
                        .append(KerberosConfig.SERVICE_PRINCIPLE_PASSWORD);
                log.error(msg.toString());
                throw new SecurityConfigException(msg.toString());
            }
        } else {
            StringBuilder msg = new StringBuilder("Retrieved principal resource is null in service metafile")
                        .append(servicePrincipalPasswordResource).append(" for property ")
                        .append(KerberosConfig.SERVICE_PRINCIPLE_PASSWORD);
            log.error(msg.toString());
            throw new SecurityConfigException(msg.toString());
        }
    }

    private String getDecryptedPassword(String encryptedString) throws SecurityConfigException {
        CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
        try {
            return new String(cryptoUtil.base64DecodeAndDecrypt(encryptedString));
        } catch (CryptoException e) {
            String msg = "Unable to decode and decrypt password string.";
            log.error(msg, e);
            throw new SecurityConfigException(msg, e);
        }
    }

    public boolean authenticateUser(String user, String password) throws Exception {
       
        boolean isAuthenticated = false;
        boolean isAuthorized = false;
        String tenantAwareUserName = TenantUtils.getTenantAwareUsername(user);
        try {

//			UserRealm realm = AnonymousSessionUtil.getRealmByUserName(
//					SecurityServiceHolder.getRegistryService(),
//					SecurityServiceHolder.getRealmService(), user);

			isAuthenticated = realm.getUserStoreManager().authenticate(
					tenantAwareUserName, password);

			if (isAuthenticated) {

				int index = tenantAwareUserName.indexOf("/");
				if (index < 0) {
					String domain = UserCoreUtil.getDomainFromThreadLocal();
					if (domain != null) {
						tenantAwareUserName = domain + "/" + tenantAwareUserName;
					}
				}

				isAuthorized = realm.getAuthorizationManager()
						.isUserAuthorized(tenantAwareUserName,
								serviceGroupId + "/" + serviceId,
								UserCoreConstants.INVOKE_SERVICE_PERMISSION);
			}

            return isAuthorized;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    private String getPrivateKeyPassword(String username) throws IOException, Exception {

        String password = null;
        int tenantId = ((UserRegistry)registry).getTenantId();
        UserRegistry govRegistry = SecurityServiceHolder.getRegistryService().
                getGovernanceSystemRegistry(tenantId);
        try {
            KeyStoreManager keyMan = KeyStoreManager.getInstance(tenantId);
            if (govRegistry.resourceExists(SecurityConstants.KEY_STORES)) {
                Collection collection = (Collection) govRegistry.get(SecurityConstants.KEY_STORES);
                String[] ks = collection.getChildren();

                for (int i = 0; i < ks.length; i++) {

                    String fullname = ks[i];
                    //get the primary keystore, only if it is super tenant.
                    if (tenantId == MultitenantConstants.SUPER_TENANT_ID && fullname
                            .equals(RegistryResources.SecurityManagement.PRIMARY_KEYSTORE_PHANTOM_RESOURCE)) {
                        KeyStore store = keyMan.getPrimaryKeyStore();
                        if (store.containsAlias(username)) {
                            password = keyMan.getPrimaryPrivateKeyPasssword();
                            break;
                        }
                    } else {
                        String name = fullname.substring(fullname.lastIndexOf("/") + 1);
                        KeyStore store = null;
                        //Not all the keystores encrypted using primary keystore password. So, some of the keystores will fail while loading
                        try {
                            store = keyMan.getKeyStore(name);
                        } catch (Exception e) {
                            log.debug("Failed to load keystore " + name, e);
                        }
                        if (store.containsAlias(username)) {
                            Resource resource = (Resource) govRegistry.get(ks[i]);
                            CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
                            String encryptedPassword = resource
                                    .getProperty(SecurityConstants.PROP_PRIVATE_KEY_PASS);
                            password = new String(cryptoUtil
                                    .base64DecodeAndDecrypt(encryptedPassword));
                            break;
                        }
                    }

                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        } 
        
        return password;
    }

}
