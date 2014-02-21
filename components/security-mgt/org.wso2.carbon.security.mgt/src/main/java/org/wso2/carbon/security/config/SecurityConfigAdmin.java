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

package org.wso2.carbon.security.config;

import edu.emory.mathcs.backport.java.util.Arrays;
import javax.cache.Cache;
//import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;
import org.apache.neethi.PolicyEngine;
import org.apache.neethi.PolicyReference;
import org.apache.rampart.policy.RampartPolicyBuilder;
import org.apache.rampart.policy.RampartPolicyData;
import org.apache.rampart.policy.model.CryptoConfig;
import org.apache.rampart.policy.model.KerberosConfig;
import org.apache.rampart.policy.model.RampartConfig;
import org.apache.ws.secpolicy.WSSPolicyException;
import org.apache.ws.secpolicy.model.SecureConversationToken;
import org.apache.ws.secpolicy.model.Token;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.core.Resources;
import org.wso2.carbon.core.persistence.PersistenceException;
import org.wso2.carbon.core.persistence.PersistenceFactory;
import org.wso2.carbon.core.persistence.PersistenceUtils;
import org.wso2.carbon.core.persistence.file.ModuleFilePersistenceManager;
import org.wso2.carbon.core.persistence.file.ServiceGroupFilePersistenceManager;
import org.wso2.carbon.core.util.*;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.security.*;
import org.wso2.carbon.security.config.service.KerberosConfigData;
import org.wso2.carbon.security.config.service.SecurityConfigData;
import org.wso2.carbon.security.config.service.SecurityScenarioData;
import org.wso2.carbon.security.pox.POXSecurityHandler;
import org.wso2.carbon.security.util.*;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.utils.ServerException;
import org.wso2.carbon.utils.deployment.GhostDeployerUtils;

import javax.security.auth.callback.CallbackHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.security.KeyStore;
import java.util.*;

/**
 * Admin service for configuring Security scenarios
 */
public class SecurityConfigAdmin {

    private static Log log = LogFactory.getLog(SecurityConfigAdmin.class);

    public static final String USER = "rampart.config.user";
    private AxisConfiguration axisConfig = null;
    private CallbackHandler callback = null;
    private Registry registry =  null;
    private UserRegistry govRegistry = null;
    private UserRealm realm = null;

    private PersistenceFactory persistenceFactory;

    private ServiceGroupFilePersistenceManager serviceGroupFilePM;

    private ModuleFilePersistenceManager moduleFilePM;

    public SecurityConfigAdmin(AxisConfiguration config) throws SecurityConfigException{
        this.axisConfig = config;

        try {
            persistenceFactory = PersistenceFactory.getInstance(config);
            serviceGroupFilePM = persistenceFactory.getServiceGroupFilePM();
            moduleFilePM = persistenceFactory.getModuleFilePM();
        } catch (Exception e) {
            log.error("Error creating an PersistenceFactory instance", e);
            throw new SecurityConfigException("Error creating an PersistenceFactory instance", e);
        }
        try {
            this.registry = SecurityServiceHolder.getRegistryService().getConfigSystemRegistry();
            this.govRegistry = SecurityServiceHolder.getRegistryService().getGovernanceSystemRegistry();
        } catch (Exception e) {
            String msg = "Error when retrieving a registry instance";
            log.error(msg);
            throw new SecurityConfigException(msg,e);
        }
    }

    public SecurityConfigAdmin(AxisConfiguration config, Registry reg, CallbackHandler cb) {
        this.axisConfig = config;
        this.registry = reg;
        this.callback = cb;

        try {
            persistenceFactory = PersistenceFactory.getInstance(config);
            serviceGroupFilePM = persistenceFactory.getServiceGroupFilePM();
            moduleFilePM = persistenceFactory.getModuleFilePM();
        } catch (Exception e) {
            log.error("Error creating an PersistenceFactory instance", e);
            // TODO : handle this exception properly. - kasung
//            throw new SecurityConfigException("Error creating an PersistenceFactory instance", e);
        }
        try {
            this.govRegistry = SecurityServiceHolder.getRegistryService().getGovernanceSystemRegistry(
                    ((UserRegistry)reg).getTenantId());
        } catch (Exception e) {
            // TODO : handle this exception properly.
            log.error("Error when obtaining the governance registry instance.");
        }
    }

    public SecurityConfigAdmin(UserRealm realm, Registry registry, AxisConfiguration config) throws SecurityConfigException{
        this.axisConfig = config;
        this.registry = registry;
        this.realm = realm;

        try {
            persistenceFactory = PersistenceFactory.getInstance(config);
            serviceGroupFilePM = persistenceFactory.getServiceGroupFilePM();
            moduleFilePM = persistenceFactory.getModuleFilePM();
        } catch (Exception e) {
            log.error("Error creating an PersistenceFactory instance", e);
            throw new SecurityConfigException("Error creating an PersistenceFactory instance", e);
        }
        try {
            this.govRegistry = SecurityServiceHolder.getRegistryService().getGovernanceSystemRegistry(
                    ((UserRegistry)registry).getTenantId());
        } catch (Exception e) {
            log.error("Error when obtaining the governance registry instance.");
            throw new SecurityConfigException(
                    "Error when obtaining the governance registry instance.", e);
        }
    }

    public SecurityScenarioData getSecurityScenario(String sceneId) throws SecurityConfigException {
        SecurityScenarioData data = null;
        SecurityScenario scenario = SecurityScenarioDatabase.get(sceneId);
        if (scenario != null) {
            data = new SecurityScenarioData();
            data.setCategory(scenario.getCategory());
            data.setDescription(scenario.getDescription());
            data.setScenarioId(scenario.getScenarioId());
            data.setSummary(scenario.getSummary());
        }
        return data;
    }

    public SecurityScenarioData getCurrentScenario(String serviceName)
            throws SecurityConfigException {

        AxisService service = axisConfig.getServiceForActivation(serviceName);
        String serviceGroupId = null;
        try {
            SecurityScenarioData data = null;
            if (service == null) {
                // try to find it from the transit ghost map
                try {
                    service = GhostDeployerUtils
                            .getTransitGhostServicesMap(axisConfig).get(serviceName);
                } catch (AxisFault axisFault) {
                    log.error("Error while reading Transit Ghosts map", axisFault);
                }
                if (service == null) {
                    throw new SecurityConfigException("AxisService is Null");
                }
            }

            serviceGroupId = service.getAxisServiceGroup().getServiceGroupName();
            boolean isTransactionStarted = serviceGroupFilePM.isTransactionStarted(serviceGroupId);
            if(!isTransactionStarted) {
                serviceGroupFilePM.beginTransaction(serviceGroupId);
            }
            String serviceXPath = PersistenceUtils.getResourcePath(service);
            String policyResourcePath = serviceXPath+"/"+Resources.POLICIES+"/"+Resources.POLICY;

            if (!serviceGroupFilePM.elementExists(serviceGroupId, policyResourcePath)) {
                if(service.getPolicySubject() != null &&
                        service.getPolicySubject().getAttachedPolicyComponents() != null){
                    Iterator iterator = service.getPolicySubject().
                                                    getAttachedPolicyComponents().iterator();
                    if(!iterator.hasNext()){
                        if(!isTransactionStarted) {
                            serviceGroupFilePM.rollbackTransaction(serviceGroupId); //no need to commit because no writes happened.
                        }
                        return data;
                    }
                }
            }


            /**
             * First check whether there's a custom policy engaged from registry. If it is not
             * the case, we check whether a default scenario is applied.
             */
            Parameter param = service.getParameter(SecurityConstants.SECURITY_POLICY_PATH);
            if (param != null) {
                data = new SecurityScenarioData();
                data.setPolicyRegistryPath((String) param.getValue());
                data.setScenarioId(SecurityConstants.POLICY_FROM_REG_SCENARIO);
            } else {
                SecurityScenario scenario = this.readCurrentScenario(serviceName);
                if (scenario != null) {
                    data = new SecurityScenarioData();
                    data.setCategory(scenario.getCategory());
                    data.setDescription(scenario.getDescription());
                    data.setScenarioId(scenario.getScenarioId());
                    data.setSummary(scenario.getSummary());
                }
            }
            if(!isTransactionStarted) {
                serviceGroupFilePM.commitTransaction(serviceGroupId);
            }
            return data;
        } catch (Exception e) {
            log.error("Error while reading persisted data", e);
            serviceGroupFilePM.rollbackTransaction(serviceGroupId);
            throw new SecurityConfigException("readingSecurity");
        }
    }

    public String[] getRequiredModules(String serviceName, String moduleId) throws Exception {
        SecurityScenarioData securityScenarioData = getCurrentScenario(serviceName);

        if (securityScenarioData != null) {
            SecurityScenario securityScenario = SecurityScenarioDatabase.get(securityScenarioData
                    .getScenarioId());
            String[] moduleNames = (String[]) securityScenario.modules
                    .toArray(new String[securityScenario.modules.size()]);
            return moduleNames;
        }

        return null;
    }

    public void disableSecurityOnService(String serviceName) throws SecurityConfigException {
        AxisService service = axisConfig.getServiceForActivation(serviceName);
        if (service == null) {
            throw new SecurityConfigException("AxisService is Null");
        }
        
        String serviceGroupId = service.getAxisServiceGroup().getServiceGroupName();
        boolean isProxyService = PersistenceUtils.isProxyService(service);
        try {           
            String serviceXPath = PersistenceUtils.getResourcePath(service);

            boolean transactionStarted1 = serviceGroupFilePM.isTransactionStarted(serviceGroupId);
            if (!transactionStarted1) {
                serviceGroupFilePM.beginTransaction(serviceGroupId);
            }

            //persist
            String policyElementPath = serviceXPath+"/"+Resources.POLICIES+"/"+Resources.POLICY;
            if (log.isDebugEnabled()) {
                log.debug("Removing " + policyElementPath);
            }

            if (!serviceGroupFilePM.elementExists(serviceGroupId, policyElementPath)) {
                if(!transactionStarted1) {
                    serviceGroupFilePM.rollbackTransaction(serviceGroupId);
                }
                return;
            }
            SecurityScenario scenario = readCurrentScenario(serviceName);
            if (scenario == null) {
                if(!transactionStarted1) {
                    serviceGroupFilePM.rollbackTransaction(serviceGroupId);
                }
                return;
            }

            String secPolicyPath = policyElementPath+
                    PersistenceUtils.
                    getXPathTextPredicate(Resources.ServiceProperties.POLICY_UUID, scenario.getWsuId()) ;
            if (serviceGroupFilePM.elementExists(serviceGroupId, secPolicyPath)) {
                serviceGroupFilePM.delete(serviceGroupId, secPolicyPath);
            }

            if (isProxyService) {   //if proxy, delete the policy in registry as well.
                String registrySecPolicyPath = PersistenceUtils.getRegistryResourcePath(service)
                        + RegistryResources.POLICIES + scenario.getWsuId();
                if (registry.resourceExists(registrySecPolicyPath)) {
                    registry.delete(registrySecPolicyPath);
                }
            }

            String[] moduleNames = scenario.getModules().toArray(
                    new String[scenario.getModules().size()]);

            // disengage modules
            for (String moduleName : moduleNames) {
                AxisModule module = service.getAxisConfiguration().getModule(moduleName);
                service.disengageModule(module);

                String version = Resources.ModuleProperties.UNDEFINED;
                if (module.getVersion() != null) {
                    version = module.getVersion().toString();
                }

                String modPath = serviceXPath +
                        "/" + Resources.ModuleProperties.MODULE_XML_TAG +
                        PersistenceUtils.getXPathAttrPredicate(
                                Resources.NAME, module.getName())+        //checks name
                        PersistenceUtils.getXPathAttrPredicate(
                                Resources.VERSION, version)+              //checks version
                        PersistenceUtils.getXPathAttrPredicate(
                                Resources.ModuleProperties.TYPE, Resources.Associations.ENGAGED_MODULES);
                                                                          //checks the module is of type engagedModules
                serviceGroupFilePM.delete(serviceGroupId, modPath);
            }
            if (!transactionStarted1) {
                serviceGroupFilePM.commitTransaction(serviceGroupId);
            }
            registry.commitTransaction();

            // remove poicy
            SecurityServiceAdmin admin = new SecurityServiceAdmin(axisConfig, registry);
            admin.removeSecurityPolicyFromAllBindings(service, scenario.getWsuId());

            String scenarioId = scenario.getScenarioId();
            String resourceUri = SecurityConstants.SECURITY_POLICY + "/" + scenarioId;

            // unpersist data
            try {
                boolean transactionStarted = serviceGroupFilePM.isTransactionStarted(serviceGroupId);
                if (!transactionStarted) {
                    serviceGroupFilePM.beginTransaction(serviceGroupId);
                }
//                registry.removeAssociation(resourceUri, servicePath,
//                        SecurityConstants.ASSOCIATION_SERVICE_SECURING_POLICY);

                serviceGroupFilePM.delete(serviceGroupId,
                        serviceXPath +
                        "/" + Resources.Associations.ASSOCIATION_XML_TAG +
                        PersistenceUtils.getXPathAttrPredicate(Resources.Associations.DESTINATION_PATH, resourceUri) );

                AuthorizationManager acAdmin = realm.getAuthorizationManager();
                String resourceName = serviceGroupId+"/"+serviceName;
                String[] roles = acAdmin.getAllowedRolesForResource(
                        resourceName,
                        UserCoreConstants.INVOKE_SERVICE_PERMISSION);
                for (int i = 0; i < roles.length; i++) {
                    acAdmin.clearRoleAuthorization(roles[i], resourceName,
                            UserCoreConstants.INVOKE_SERVICE_PERMISSION);
                }

                List kss = serviceGroupFilePM.getAssociations(
                        serviceGroupId, serviceXPath, SecurityConstants.ASSOCIATION_PRIVATE_KEYSTORE);

                for (Object ks : kss) {
                    ((OMNode) ks).detach();
                }

                List tkss = serviceGroupFilePM.getAssociations(
                        serviceGroupId, serviceXPath, SecurityConstants.ASSOCIATION_TRUSTED_KEYSTORE);
                
                for (Object tks : tkss) {
                    ((OMNode) tks).detach();
                }

                if ((roles == null || roles.length ==0 ) ||
                        (kss == null || kss.isEmpty() ) ||
                        (tkss == null || tkss.isEmpty() )) {
                    serviceGroupFilePM.setMetaFileModification(serviceGroupId);
                }

                // remove the policy path parameter if it is set..
                String paramXPath = serviceXPath+"/"+Resources.ParameterProperties.PARAMETER+
                        PersistenceUtils.getXPathAttrPredicate(
                                Resources.NAME, SecurityConstants.SECURITY_POLICY_PATH);
                if (serviceGroupFilePM.elementExists(serviceGroupId, paramXPath)) {
                    serviceGroupFilePM.delete(serviceGroupId, paramXPath);
                    serviceGroupFilePM.setMetaFileModification(serviceGroupId);
                }
                if (!transactionStarted) {
                    serviceGroupFilePM.commitTransaction(serviceGroupId);
                }
            } catch (Exception e) {
                String msg = "Unable to remove persisted data.";
                log.error(msg);
                serviceGroupFilePM.rollbackTransaction(serviceGroupId);
                throw new AxisFault(msg, e);
            }

            Parameter param = new Parameter();
            param.setName(WSHandlerConstants.PW_CALLBACK_REF);
            service.removeParameter(param);

            Parameter param2 = new Parameter();
            param2.setName("disableREST"); // TODO Find the constant
            service.removeParameter(param2);

            Parameter pathParam = service.getParameter(SecurityConstants.SECURITY_POLICY_PATH);
            String policyPath = null;
            if (pathParam != null) {
                policyPath = (String) pathParam.getValue();
                service.removeParameter(pathParam);
            }

            //removing security scenarioID parameter from axis service
            Parameter scenarioIDParam = service.getParameter(SecurityConstants.SCENARIO_ID_PARAM_NAME);
            if (scenarioIDParam != null) {
                service.removeParameter(scenarioIDParam);
            }

            // unlock transports
            Policy policy = this.loadPolicy(scenarioId, policyPath);
            if (isHttpsTransportOnly(policy)) {
                try {
                    boolean transactionStarted = serviceGroupFilePM.isTransactionStarted(serviceGroupId);
                    if (!transactionStarted) {
                        serviceGroupFilePM.beginTransaction(serviceGroupId);
                    }

                    persistenceFactory.getServicePM().deleteServiceProperty(service, Resources.ServiceProperties.IS_UT_ENABLED);

//                    List<String> transports = getAllTransports();
//                    setServiceTransports(serviceName, transports);

                    // Fire the transport binding added event
//                    AxisEvent event = new AxisEvent(CarbonConstants.AxisEvent.TRANSPORT_BINDING_ADDED,
//                            service);
//                    axisConfig.notifyObservers(event, service);

                    persistenceFactory.getServicePM().setServiceProperty(service,
                            Resources.ServiceProperties.EXPOSED_ON_ALL_TANSPORTS, Boolean.FALSE.toString());

//                    for (String trans : transports) {
//                        if (trans.endsWith("https")) {
//                            continue;
//                        }
                        //in registry - kasung
//                        String transPath = RegistryResources.TRANSPORTS + trans + "/listener";
//                        if (registry.resourceExists(transPath)) {
//                            persistenceFactory.getServicePM().updateServiceAssociation(service,
//                                    transPath, Resources.Associations.EXPOSED_TRANSPORTS);
//                        } else {
//                            String msg = "Transport path " + transPath + " does not exist in the registry";
//                            log.error(msg);
//                            serviceGroupFilePM.rollbackTransaction(serviceGroupId);
//                            throw new AxisFault(msg);
//                        }
//                    }

                    if (!transactionStarted) {
                        serviceGroupFilePM.commitTransaction(serviceGroupId);
                    }

                } catch (Exception e) {
                    String msg = "Service with name " + serviceName + " not found.";
                    log.error(msg);
                    serviceGroupFilePM.rollbackTransaction(serviceGroupId);
                    throw new AxisFault(msg, e);
                }
            }
            // finally update the ghost file if GD is used..
            if (service.getFileName() != null) {
                updateSecScenarioInGhostFile(service.getFileName().getPath(), serviceName, null);
            }
        } catch (AxisFault e) {
            log.error(e.getMessage(), e);
            serviceGroupFilePM.rollbackTransaction(serviceGroupId);
            //why don't we throw a exception here? - kasung
        } catch (SecurityConfigException e) {
            log.error(e.getMessage(), e);
            serviceGroupFilePM.rollbackTransaction(serviceGroupId);
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            serviceGroupFilePM.rollbackTransaction(serviceGroupId);
            throw new SecurityConfigException("removingPolicy", e);
        }
    }


    private KerberosConfigData readKerberosConfigurations(AxisService service) throws SecurityConfigException {

        String kerberosXPath = getKerberosConfigXPath(service);
        String serviceGroupId = service.getAxisServiceGroup().getServiceGroupName();
        try {
            boolean isTransactionStarted = serviceGroupFilePM.isTransactionStarted(serviceGroupId);
            if(!isTransactionStarted) {
                serviceGroupFilePM.beginTransaction(serviceGroupId);
            }
            // First check whether an element path already exists
            if (!serviceGroupFilePM.elementExists(serviceGroupId, kerberosXPath)) {
                if(!isTransactionStarted) {
                    serviceGroupFilePM.rollbackTransaction(serviceGroupId); //no need to commit because no writes happened.
                }
                 return null;
            }
            OMElement kerberosElement = (OMElement) serviceGroupFilePM.get(serviceGroupId, kerberosXPath);

            KerberosConfigData kerberosConfigData = new KerberosConfigData();
            kerberosConfigData.setServicePrincipleName(kerberosElement.
                    getAttributeValue(new QName(KerberosConfig.SERVICE_PRINCIPLE_NAME)));
            String encryptedString = kerberosElement.
                    getAttributeValue(new QName(KerberosConfig.SERVICE_PRINCIPLE_PASSWORD));

            CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
            try {
                kerberosConfigData.setServicePrinciplePassword
                        (new String(cryptoUtil.base64DecodeAndDecrypt(encryptedString)));
            } catch (CryptoException e) {
                String msg = "Unable to decode and decrypt password string.";
                log.warn(msg, e);
            }
            if(!isTransactionStarted) {
                serviceGroupFilePM.commitTransaction(serviceGroupId);
            }
            return kerberosConfigData;
        } catch (PersistenceException e) {
            String msg = "An error occurred while retrieving kerberos configuration data for service "
                    + service.getName();
            log.error(msg, e);
            serviceGroupFilePM.rollbackTransaction(serviceGroupId);
            throw new SecurityConfigException(msg);
        }
    }

    private String getKerberosConfigXPath(AxisService service) {
        return PersistenceUtils.getResourcePath(service)+"/"+RampartConfigUtil.KERBEROS_CONFIG_RESOURCE;
    }

    /**
     * <kerberos service.principal.name="xxx" service.principal.name="yyy"/>
     * @param service
     * @param kerberosConfigData
     * @throws SecurityConfigException
     */
    protected void persistsKerberosData(AxisService service, KerberosConfigData kerberosConfigData)
            throws SecurityConfigException {

        String kerberosXPath = getKerberosConfigXPath(service);
        ServiceGroupFilePersistenceManager sfpm = persistenceFactory.getServiceGroupFilePM();
        String serviceGroupId = service.getAxisServiceGroup().getServiceGroupName();
        try {
            boolean isTransactionStarted = sfpm.isTransactionStarted(serviceGroupId);
            if(!isTransactionStarted) {
                sfpm.beginTransaction(serviceGroupId);
            }

            // First check whether an element path already exists
            if (sfpm.elementExists(serviceGroupId, kerberosXPath)) {
                sfpm.delete(serviceGroupId, kerberosXPath);
            }

            //<kerberos service.principal.name="xxx" service.principal.name="yyy"/>
            OMElement kerberosElement = OMAbstractFactory.getOMFactory().
                    createOMElement(RampartConfigUtil.KERBEROS_CONFIG_RESOURCE, null);
            kerberosElement.addAttribute(KerberosConfig.SERVICE_PRINCIPLE_NAME,
                    kerberosConfigData.getServicePrincipleName(), null);
            kerberosElement.addAttribute(KerberosConfig.SERVICE_PRINCIPLE_PASSWORD,
                    getEncryptedPassword(kerberosConfigData.getServicePrinciplePassword()), null);
            sfpm.put(serviceGroupId, kerberosElement, PersistenceUtils.getResourcePath(service));

            if(!isTransactionStarted) {
                sfpm.commitTransaction(serviceGroupId);
            }
        } catch (PersistenceException e) {
            log.error("Error adding kerberos parameters to registry.", e);
            sfpm.rollbackTransaction(serviceGroupId);
            throw new SecurityConfigException("Unable to add kerberos parameters to registry.", e);
        }
    }

    private String getEncryptedPassword(String password) throws SecurityConfigException {
        CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
        try {
            return cryptoUtil.encryptAndBase64Encode(password.getBytes());
        } catch (CryptoException e) {
            String msg = "Unable to encrypt and encode password string.";
            log.error(msg, e);
            throw new SecurityConfigException(msg, e);
        }
    }

    private String getRegistryServicePath(AxisService service) {

        return RegistryResources.SERVICE_GROUPS
                + service.getAxisServiceGroup().getServiceGroupName()
                + RegistryResources.SERVICES + service.getName();
    }

    public void activateUsernameTokenAuthentication(String serviceName, String[] userGroups)
            throws SecurityConfigException {

        // TODO Remove

    }

    public void applySecurity(String serviceName, String scenarioId, KerberosConfigData kerberosConfigurations)
            throws SecurityConfigException {

        if (kerberosConfigurations == null) {
            log.error("Kerberos configurations provided are invalid.");
            throw new SecurityConfigException("Kerberos configuration parameters are null. " +
                    "Please specify valid kerberos configurations.");
        }

        AxisService service = axisConfig.getServiceForActivation(serviceName);
        if (service == null) {
            throw new SecurityConfigException("nullService");
        }
        
        String serviceGroupId = service.getAxisServiceGroup().getServiceGroupName();
        try {

            // Begin registry transaction
            boolean transactionStarted = serviceGroupFilePM.isTransactionStarted(serviceGroupId);
            if (!transactionStarted) {
                serviceGroupFilePM.beginTransaction(serviceGroupId);
            }

            // Disable security if already a policy is applied
            this.disableSecurityOnService(serviceName); //todo fix the method

            boolean isRahasEngaged = false;
            applyPolicy(service, scenarioId, null, null, null, kerberosConfigurations);

            isRahasEngaged = engageModules(scenarioId, serviceName, service);

            if (!isRahasEngaged) {
                log.info("Rahas engaged to service - " + serviceName);
            }

            disableRESTCalls(serviceName, scenarioId);

            persistsKerberosData(service, kerberosConfigurations);

            if (!transactionStarted) {
                serviceGroupFilePM.commitTransaction(serviceGroupId);
            }
            
//            Cache cache = CacheManager.getInstance().getCache(POXSecurityHandler.POX_ENABLED);
//            cache.remove(serviceName);
            this.getPOXCache().remove(serviceName);
            
//            CacheInvalidator invalidator = SecurityMgtServiceComponent.getCacheInvalidator();
//            try {
//                invalidator.invalidateCache(POXSecurityHandler.POX_ENABLED, new StringCacheKey(serviceName));
//            } catch (CacheException e1) {
//                String msg = "Failed to propagate changes immediately. It will take time to update nodes in cluster";
//                log.error(msg, e1);
//                throw new SecurityConfigException(msg, e1);
//            }
            
        } catch (PersistenceException e) {
            StringBuilder str = new StringBuilder("Error persisting security scenario ").
                    append(scenarioId).append(" for service ").append(serviceName);
            log.error(str.toString(),e);
            serviceGroupFilePM.rollbackTransaction(serviceGroupId);
            throw new SecurityConfigException(str.toString(), e);
        }
    }

    public void applySecurity(String serviceName, String scenarioId, String policyPath,
                              String[] trustedStores, String privateStore,
                              String[] userGroups) throws SecurityConfigException {
        // TODO: If this method is too time consuming, it is better to not start
        // transactions in
        // here. Most of the operations invoked in here, are already
        // transactional.

        AxisService service = axisConfig.getServiceForActivation(serviceName);
        if (service == null) {
            throw new SecurityConfigException("nullService");
        }
        
        String serviceGroupId = service.getAxisServiceGroup().getServiceGroupName();

        try {
            if (userGroups != null) {
                Arrays.sort(userGroups);
                if (Arrays.binarySearch(userGroups, CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME) > -1) {
                    log
                            .error("Security breach. A user is attempting to enable anonymous for UT access");
                    throw new SecurityConfigException("Invalid data provided"); // obscure error message
                }
            }

            boolean transactionStarted = serviceGroupFilePM.isTransactionStarted(serviceGroupId);
            if (!transactionStarted) {
                serviceGroupFilePM.beginTransaction(serviceGroupId);
            }
            boolean registryTransactionStarted = Transaction.isStarted();
            if(!registryTransactionStarted) {
                registry.beginTransaction();         //is this really needed?
            }

            this.disableSecurityOnService(serviceName);

            // if the service is a ghost service, load the actual service
            if (GhostDeployerUtils.isGhostService(service)) {
                try {
                    service = GhostDeployerUtils.deployActualService(axisConfig, service);
                } catch (AxisFault axisFault) {
                    log.error("Error while loading actual service from Ghost", axisFault);
                }
            }

            boolean isRahasEngaged = false;
            applyPolicy(service, scenarioId, policyPath, trustedStores, privateStore);
            isRahasEngaged = engageModules(scenarioId, serviceName, service);
            disableRESTCalls(serviceName, scenarioId);
            persistData(service, scenarioId, privateStore, trustedStores, userGroups, isRahasEngaged);
            if (!transactionStarted) {
                serviceGroupFilePM.commitTransaction(serviceGroupId);
            }
            if(!registryTransactionStarted) {
                registry.commitTransaction();
            }
            // finally update the ghost file if GD is used..
            if (service.getFileName() != null) {
                updateSecScenarioInGhostFile(service.getFileName().getPath(), serviceName, scenarioId);
            }
            
            this.getPOXCache().remove(serviceName);
            Cache<String, String> cache = getPOXCache();
            if(cache != null){
            	cache.remove(serviceName);
            }

            //Adding the security scenario ID parameter to the axisService
            //This parameter can be used to get the applied security scenario
            //without reading the service meta data file.
            try {
                Parameter param = new Parameter();
                param.setName(SecurityConstants.SCENARIO_ID_PARAM_NAME);
                param.setValue(scenarioId);
                service.addParameter(param);
            } catch (AxisFault axisFault) {
                log.error("Error while adding Scenario ID parameter",axisFault);
            }
            
            try
            {
                AxisModule rahas = service.getAxisConfiguration().getModule("rahas");
                if(!"scenario1".equals(scenarioId))
                {
                    service.disengageModule(rahas);
                    service.engageModule(rahas);
                }
            }
            catch(AxisFault e1)
            {
                String msg = "Failed to propagate changes immediately. It will take time to update nodes in cluster";
                log.error(msg, e1);
                throw new SecurityConfigException(msg, e1);
            }

        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
            serviceGroupFilePM.rollbackTransaction(serviceGroupId);
            try {
                registry.rollbackTransaction();
            } catch (RegistryException ex) {
                log.error("Error while rollback", ex);
            }
        } catch (PersistenceException e) {
            log.error(e.getMessage(), e);
            serviceGroupFilePM.rollbackTransaction(serviceGroupId);
            try {
                registry.rollbackTransaction();
            } catch (RegistryException ex) {
                log.error("Error while rollback", ex);
            }
        }
    }

    protected void applyPolicy(AxisService service, String scenarioId, String policyPath, String[] trustedStores,
             String privateStore) throws SecurityConfigException {

        applyPolicy(service, scenarioId, policyPath, trustedStores, privateStore, null);

    }

    protected void applyPolicy(AxisService service, String scenarioId, String policyPath,
                               String[] trustedStores, String privateStore, KerberosConfigData kerberosConfig)
            throws SecurityConfigException {

        String serviceGroupId = service.getAxisServiceGroup().getServiceGroupName();
        try {

            String registryServicePath = getRegistryServicePath(service);

            String serviceXPath = PersistenceUtils.getResourcePath(service);

            CallbackHandler handler = null;
            if (callback == null) {
                handler = new ServicePasswordCallbackHandler(persistenceFactory, serviceGroupId, service.getName(), serviceXPath,
                        registryServicePath, registry, realm);
            } else {
                handler = this.callback;
            }

            Parameter param = new Parameter();
            param.setName(WSHandlerConstants.PW_CALLBACK_REF);
            param.setValue(handler);
            service.addParameter(param);

            Properties props = getServerCryptoProperties(privateStore, trustedStores);
            RampartConfig rampartConfig = new RampartConfig();
            // rampartConfig.setTokenStoreClass(SimpleTokenStore.class.getName());
            populateRampartConfig(rampartConfig, props, kerberosConfig);
            Policy policy = loadPolicy(scenarioId, policyPath);

            if (rampartConfig != null) {
                policy.addAssertion(rampartConfig);
            }

            // if the policy is from registry, add the policy path as a service parameter
            if (policyPath != null &&
                    scenarioId.equals(SecurityConstants.POLICY_FROM_REG_SCENARIO)) {
                Parameter pathParam = new Parameter(SecurityConstants.SECURITY_POLICY_PATH,
                        policyPath);
                //pathParam.setLocked(true); this causes errors at proxy redeployments 
                service.addParameter(pathParam);
                persistenceFactory.getServicePM().updateServiceParameter(service, pathParam);
//                persistParameter(serviceGroupId, pathParam, serviceXPath);
            }

            if (isHttpsTransportOnly(policy)) {
                setServiceTransports(service.getName(), getHttpsTransports());
                try {
                    boolean transactionStarted = serviceGroupFilePM.isTransactionStarted(serviceGroupId);
                    if (!transactionStarted) {
                        serviceGroupFilePM.beginTransaction(serviceGroupId);
                    }

                    serviceGroupFilePM.put(serviceGroupId,
                            OMAbstractFactory.getOMFactory().createOMAttribute(
                                    Resources.ServiceProperties.IS_UT_ENABLED,
                                    null, Boolean.TRUE.toString()),
                            serviceXPath);
                    serviceGroupFilePM.put(serviceGroupId,
                            OMAbstractFactory.getOMFactory().createOMAttribute(
                                    Resources.ServiceProperties.EXPOSED_ON_ALL_TANSPORTS,
                                    null, Boolean.FALSE.toString()),
                            serviceXPath);

                    List exposedTransports = serviceGroupFilePM.getAssociations(serviceGroupId, serviceXPath,
                            Resources.Associations.EXPOSED_TRANSPORTS);

                    boolean isExists = false;
                    // TODO : Handle generally as axis2 parameters
                    for (Object node : exposedTransports) {
                        OMElement assoc = (OMElement) node;
                        String transport = assoc.getAttributeValue(new QName(Resources.Associations.DESTINATION_PATH));
                        if (transport.endsWith("https")) {
                            isExists = true;
                            continue;
                        }
                        if (registry.resourceExists(transport)) {
                            assoc.detach();     //todo do this via a call to persistence layer
                            serviceGroupFilePM.setMetaFileModification(serviceGroupId);
                        } else {
                            String msg = "Transport resource " + transport + " not available in Registry";
                            log.error(msg);
                            throw new AxisFault(msg);
                        }
                    }

                    if (!isExists) {
                        String transportResourcePath = RegistryResources.TRANSPORTS + "https" + "/listener";
                        if (registry.resourceExists(transportResourcePath)) {
                            serviceGroupFilePM.put(serviceGroupId,
                                    PersistenceUtils.createAssociation(transportResourcePath,
                                            Resources.Associations.EXPOSED_TRANSPORTS),
                                    serviceXPath);
                        } else {
                            String msg = "Transport resource " + transportResourcePath + " not available in Registry";
                            log.error(msg);
                            throw new AxisFault(msg);
                        }
                    }
                    if (!transactionStarted) {
                        serviceGroupFilePM.commitTransaction(serviceGroupId);
                    }
                } catch (Exception e) {
                    String msg = "Service with name " + service.getName() + " not found.";
                    log.error(msg);
                    serviceGroupFilePM.rollbackTransaction(serviceGroupId);
                    throw new AxisFault(msg, e);
                }

            } else {
                setServiceTransports(service.getName(), getAllTransports());
            }

            SecurityServiceAdmin secAdmin = new SecurityServiceAdmin(axisConfig, registry);
            secAdmin.addSecurityPolicyToAllBindings(service, policy);
            /*
             * ServiceAdmin serviceAdmin =
             * SecurityServiceTrackers.getServiceAdmin(); Map endPointMap =
             * service.getEndpoints(); for (Object o : endPointMap.entrySet()) {
             * Map.Entry entry = (Map.Entry) o; AxisEndpoint point =
             * (AxisEndpoint) entry.getValue(); AxisBinding binding =
             * point.getBinding(); String bindingName =
             * binding.getName().getLocalPart();
             * serviceAdmin.setBindingPolicy(service.getBindingName(),
             * bindingName, policy .toString()); }
             */
        } catch (ServerException e) {
            log.error(e.getMessage(), e);
            serviceGroupFilePM.rollbackTransaction(serviceGroupId);
            throw new SecurityConfigException(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            serviceGroupFilePM.rollbackTransaction(serviceGroupId);
            throw new SecurityConfigException(e.getMessage(), e);
        }
    }

    protected boolean engageModules(String scenarioId, String serviceName, AxisService axisService)
            throws SecurityConfigException {
        boolean isRahasEngaged = false;
        SecurityScenario securityScenario = SecurityScenarioDatabase.get(scenarioId);
        String[] moduleNames = (String[]) securityScenario.modules
                .toArray(new String[securityScenario.modules.size()]);

        String serviceGroupId = axisService.getAxisServiceGroup().getServiceGroupName();
        String serviceXPath = PersistenceUtils.getResourcePath(axisService);

        // handle each module required
        try {
            try {
                boolean transactionStarted = serviceGroupFilePM.isTransactionStarted(serviceGroupId);
                if (!transactionStarted) {
                    serviceGroupFilePM.beginTransaction(serviceGroupId);
                }

                List assocs = serviceGroupFilePM.getAll(serviceGroupId, serviceXPath+
                        "/"+Resources.ModuleProperties.MODULE_XML_TAG +
                        PersistenceUtils.getXPathAttrPredicate(
                                Resources.ModuleProperties.TYPE, Resources.Associations.ENGAGED_MODULES));
                for (String modName : moduleNames) {
                    AxisModule module = axisService.getAxisConfiguration().getModule(modName);
//                    String path = RegistryResources.MODULES + modName + "/" + module.getVersion();
                    
                    boolean isFound = false;
                    for (Object node : assocs) {
                        OMElement tempAssoc = (OMElement) node;
                        String tempModeName = tempAssoc.getAttributeValue(new QName(Resources.NAME));
                        String tempModeVersion = tempAssoc.getAttributeValue(new QName(Resources.VERSION));

                        if (modName.equals(tempModeName) && module.getVersion().toString().equals(tempModeVersion)) {
                            isFound = true;
                            break;
                        }
                    }

                    if (!isFound) {
                        //associations for modules is a little different
                        String modulePath = Resources.ModuleProperties.VERSION_XPATH+
                                PersistenceUtils.getXPathAttrPredicate(Resources.ModuleProperties.VERSION_ID,
                                        module.getVersion().toString());

                        if(moduleFilePM.elementExists(modName, modulePath)) {
                            OMElement moduleElement = OMAbstractFactory.getOMFactory().createOMElement(
                                    Resources.ModuleProperties.MODULE_XML_TAG, null);
                            moduleElement.addAttribute(Resources.NAME, module.getName(), null);
                            if (module.getVersion() != null) {
                                moduleElement.addAttribute(Resources.VERSION, module.getVersion().toString(), null);
                            }
                            moduleElement.addAttribute("type", Resources.Associations.ENGAGED_MODULES, null);

                            serviceGroupFilePM.put(serviceGroupId, moduleElement, serviceXPath);
                        }
                    }
                    // engage at axis2
                    axisService.disengageModule(module);
                    axisService.engageModule(module);
                    if (modName.equalsIgnoreCase("rahas")) {
                        isRahasEngaged = true;
                    }
                }
                if (!transactionStarted) {
                    serviceGroupFilePM.commitTransaction(serviceGroupId);
                }
            } catch (PersistenceException e) {
                serviceGroupFilePM.rollbackTransaction(serviceGroupId);
                String msg = "Unable to engage modules.";
                log.error(msg);
                throw new AxisFault(msg, e);
            }
        } catch (AxisFault e) {
            log.error(e);
            serviceGroupFilePM.rollbackTransaction(serviceGroupId);
            throw new SecurityConfigException(e.getMessage(), e);
        }
        return isRahasEngaged;
    }

    protected void disableRESTCalls(String serviceName, String scenrioId)
            throws SecurityConfigException {

        if (scenrioId.equals(SecurityConstants.USERNAME_TOKEN_SCENARIO_ID)) {
            return;
        }

        try {
            AxisService service = axisConfig.getServiceForActivation(serviceName);
            if (service == null) {
                throw new SecurityConfigException("nullService");
            }

            Parameter param = new Parameter();
            param.setName("disableREST"); // TODO Find the constant
            param.setValue(Boolean.TRUE.toString());
            service.addParameter(param);

        } catch (AxisFault e) {
            log.error(e);
            throw new SecurityConfigException("disablingREST", e);
        }

    }

    protected void persistData(AxisService service, String scenarioId, String privateStore,
            String[] trustedStores, String[] userGroups, boolean isRahasEngaged)
            throws SecurityConfigException {
        String serviceGroupId = service.getAxisServiceGroup().getServiceGroupName();
        try {
            String serviceXPath = PersistenceUtils.getResourcePath(service);

            boolean isTransactionStarted = serviceGroupFilePM.isTransactionStarted(serviceGroupId);
            if(!isTransactionStarted) {
                serviceGroupFilePM.beginTransaction(serviceGroupId);
            }

            // registry.addAssociation(resourceUri, servicePath,
            // SecurityConstants.ASSOCIATION_SERVICE_SECURING_POLICY);

            if (privateStore != null) {
                String ksPath = SecurityConstants.KEY_STORES + "/" + privateStore;
                if (govRegistry.resourceExists(ksPath)) {
                    OMElement assoc = PersistenceUtils.createAssociation(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                            ksPath, SecurityConstants.ASSOCIATION_PRIVATE_KEYSTORE);
                    serviceGroupFilePM.put(serviceGroupId, assoc, serviceXPath);
                } else if (KeyStoreUtil.isPrimaryStore(privateStore)) {
                    OMElement assoc = PersistenceUtils.createAssociation(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                            RegistryResources.SecurityManagement.PRIMARY_KEYSTORE_PHANTOM_RESOURCE,
                            SecurityConstants.ASSOCIATION_PRIVATE_KEYSTORE);
                    serviceGroupFilePM.put(serviceGroupId, assoc, serviceXPath);
                } else {
                    throw new SecurityConfigException("Missing key store " + privateStore);
                }
            }

            if (trustedStores != null) {
                for (String storeName : trustedStores) {
                    String ksPath = SecurityConstants.KEY_STORES + "/" + storeName;
                    if (govRegistry.resourceExists(ksPath)) {
                        OMElement assoc = PersistenceUtils.createAssociation(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                ksPath, SecurityConstants.ASSOCIATION_TRUSTED_KEYSTORE);
                        serviceGroupFilePM.put(serviceGroupId, assoc, serviceXPath);
                    } else if (KeyStoreUtil.isPrimaryStore(storeName)) {
                        OMElement assoc = PersistenceUtils.createAssociation(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                RegistryResources.SecurityManagement.PRIMARY_KEYSTORE_PHANTOM_RESOURCE,
                                SecurityConstants.ASSOCIATION_TRUSTED_KEYSTORE);
                        serviceGroupFilePM.put(serviceGroupId, assoc, serviceXPath);
                    } else {
                        throw new SecurityConfigException("Missing key store" + storeName);
                    }
                }
            } 


            if (userGroups != null) {
                for (String value : userGroups) {
                    AuthorizationManager acAdmin = realm.getAuthorizationManager();

                        acAdmin.authorizeRole(value, serviceGroupId+"/"+service.getName(),
                                UserCoreConstants.INVOKE_SERVICE_PERMISSION);
                }
            }
            if (isRahasEngaged) {
                setRahasParameters(service, privateStore);
            } else {
                removeRahasParameters(service);
            }

            if(!isTransactionStarted) {
                serviceGroupFilePM.commitTransaction(serviceGroupId);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            serviceGroupFilePM.rollbackTransaction(serviceGroupId);
            throw new SecurityConfigException(e.getMessage(), e);
        }
    }

    /**
     * Load the security policy from Registry according to the scenarioId. If the scenario is
     * "policyFromRegistry", it gets the policy from the given path.
     *
     * @param scenarioId - String id of the scenario
     * @param policyPath - regIdentifier:path
     *                          regIdentifier (conf: or gov:) is used to identify the registry
     *                          path is the policy path in the selected registry
     * @return - Policy object
     * @throws SecurityConfigException - Error while loading policy
     */
    public Policy loadPolicy(String scenarioId, String policyPath) throws SecurityConfigException {

        try {
            Registry registryToLoad = registry;
            String resourceUri = SecurityConstants.SECURITY_POLICY + "/" + scenarioId;
            if (policyPath != null &&
                    scenarioId.equals(SecurityConstants.POLICY_FROM_REG_SCENARIO)) {
                resourceUri = policyPath.substring(policyPath.lastIndexOf(':') + 1);
                String regIdentifier = policyPath.substring(0, policyPath.lastIndexOf(':'));
                if (SecurityConstants.GOVERNANCE_REGISTRY_IDENTIFIER.equals(regIdentifier)) {
                    registryToLoad = govRegistry;
                }
            }
            Resource resource = registryToLoad.get(resourceUri);
            InputStream in = resource.getContentStream();

            XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(in);
            StAXOMBuilder builder = new StAXOMBuilder(parser);

            OMElement policyElement = builder.getDocumentElement();
            if (policyPath != null &&
                    scenarioId.equals(SecurityConstants.POLICY_FROM_REG_SCENARIO)) {
                OMAttribute att = policyElement.getAttribute(SecurityConstants.POLICY_ID_QNAME);
                if (att != null) {
                    att.setAttributeValue(SecurityConstants.POLICY_FROM_REG_SCENARIO);
                }
            }

            return PolicyEngine.getPolicy(policyElement);
        } catch (Exception e) {
            log.error("loadingPolicy", e);
            throw new SecurityConfigException("loadingPolicy", e);
        }

    }

    public void populateRampartConfig(RampartConfig rampartConfig, Properties props)
             throws SecurityConfigException {

        populateRampartConfig(rampartConfig, props, null);
    }

    public void populateRampartConfig(RampartConfig rampartConfig, Properties props,
                                      KerberosConfigData kerberosConfigurations)
            throws SecurityConfigException {
        if (rampartConfig != null) {

            if (kerberosConfigurations != null) {

                Properties kerberosProperties = new Properties();
                kerberosProperties.setProperty(KerberosConfig.SERVICE_PRINCIPLE_NAME,
                        kerberosConfigurations.getServicePrincipleName());

                KerberosConfig kerberosConfig = new KerberosConfig();
                kerberosConfig.setProp(kerberosProperties);

                // Set system wide kerberos configurations

                String carbonSecurityConfigurationPath = RampartConfigUtil.getCarbonSecurityConfigurationPath();
                if (carbonSecurityConfigurationPath != null) {

                    String krbFile = carbonSecurityConfigurationPath + File.separatorChar
                            + KerberosConfigData.KERBEROS_CONFIG_FILE_NAME;

                    File krbFileObject = new File(krbFile);

                    if (!krbFileObject.exists()) {
                        throw new SecurityConfigException("Kerberos configuration file not found at " + krbFile);
                    }

                    log.info("Setting " + KerberosConfigData.KERBEROS_CONFIG_FILE_SYSTEM_PROPERTY +
                            " to kerberos configuration file " + krbFile);

                    System.setProperty(KerberosConfigData.KERBEROS_CONFIG_FILE_SYSTEM_PROPERTY, krbFile);

                } else {
                    throw new SecurityConfigException("Could not retrieve carbon home");
                }

                rampartConfig.setKerberosConfig(kerberosConfig);


            } else {

                if (!props.isEmpty()) {
                    // Encryption crypto config
                    {
                        CryptoConfig encrCryptoConfig = new CryptoConfig();
                        encrCryptoConfig.setProvider(ServerCrypto.class.getName());
                        encrCryptoConfig.setProp(props);
                        encrCryptoConfig.setCacheEnabled(true);
                        encrCryptoConfig.setCryptoKey(ServerCrypto.PROP_ID_PRIVATE_STORE);
                        rampartConfig.setEncrCryptoConfig(encrCryptoConfig);
                    }

                    {
                        CryptoConfig signatureCryptoConfig = new CryptoConfig();
                        signatureCryptoConfig.setProvider(ServerCrypto.class.getName());
                        signatureCryptoConfig.setProp(props);
                        signatureCryptoConfig.setCacheEnabled(true);
                        signatureCryptoConfig.setCryptoKey(ServerCrypto.PROP_ID_PRIVATE_STORE);
                        rampartConfig.setSigCryptoConfig(signatureCryptoConfig);
                    }
                }

                rampartConfig.setEncryptionUser(WSHandlerConstants.USE_REQ_SIG_CERT);
                rampartConfig.setUser(props.getProperty(SecurityConstants.USER));

                // Get ttl and timeskew params from axis2 xml
                int ttl = RampartConfig.DEFAULT_TIMESTAMP_TTL;
                int timeSkew = RampartConfig.DEFAULT_TIMESTAMP_MAX_SKEW;

                rampartConfig.setTimestampTTL(Integer.toString(ttl));
                rampartConfig.setTimestampMaxSkew(Integer.toString(timeSkew));
                //adding this class as default one.
                //rampartConfig.setTokenStoreClass("org.wso2.carbon.identity.sts.store.DBTokenStore");

                //this will check for TokenStoreClassName property under Security in carbon.xml
                //if it is not found, default token store class will be set
                String tokenStoreClassName = ServerConfiguration.getInstance().getFirstProperty("Security.TokenStoreClassName");
                if(tokenStoreClassName==null){
                    rampartConfig.setTokenStoreClass(SecurityTokenStore.class.getName());
                }else{
                    rampartConfig.setTokenStoreClass(tokenStoreClassName);
                }
            }
        }
    }

    public Properties getServerCryptoProperties(String privateStore, String[] trustedCertStores)
            throws Exception {
        Properties props = new Properties();

        int tenantId = ((UserRegistry) registry).getTenantId();
        UserRegistry govRegistry = SecurityServiceHolder.getRegistryService().
                getGovernanceSystemRegistry(tenantId);

        if (trustedCertStores != null && trustedCertStores.length > 0) {
            StringBuilder trustString = new StringBuilder();
            for (String trustedCertStore : trustedCertStores) {
                trustString.append(trustedCertStore).append(",");
            }

            if (trustedCertStores.length != 0) {
                props.setProperty(ServerCrypto.PROP_ID_TRUST_STORES, trustString.toString());
            }
        }

        if (privateStore != null) {
            props.setProperty(ServerCrypto.PROP_ID_PRIVATE_STORE, privateStore);

            KeyStoreManager keyMan = KeyStoreManager.getInstance(tenantId);
            KeyStore ks = keyMan.getKeyStore(privateStore);

            String privKeyAlias = KeyStoreUtil.getPrivateKeyAlias(ks);
            props.setProperty(ServerCrypto.PROP_ID_DEFAULT_ALIAS, privKeyAlias);
            props.setProperty(USER, privKeyAlias);
        }

        if (privateStore != null || (trustedCertStores != null && trustedCertStores.length > 0)) {
            //Set the tenant-ID in the properties
            props.setProperty(ServerCrypto.PROP_ID_TENANT_ID,
                              new Integer(tenantId).toString());
        }

        return props;
    }

    /**
     * Expose this service only via the specified transport
     * 
     * @param serviceId service name
     * @param transportProtocols transport protocols to expose
     * @throws AxisFault axisfault
     * @throws org.wso2.carbon.security.SecurityConfigException ex
     */
    public void setServiceTransports(String serviceId, List<String> transportProtocols)
            throws SecurityConfigException, AxisFault {

        AxisService axisService = axisConfig.getServiceForActivation(serviceId);

        if (axisService == null) {
            throw new SecurityConfigException("nullService");
        }

        ArrayList<String> transports = new ArrayList<String>();
        for (int i = 0; i < transportProtocols.size(); i++) {
            transports.add(transportProtocols.get(i));
        }
        axisService.setExposedTransports(transports);

        if (log.isDebugEnabled()) {
            log.debug("Successfully add selected transport bindings to service " + serviceId);
        }
    }

    /**
     * Check the policy to see whether the service should only be exposed in
     * HTTPS
     * 
     * @param policy
     *            service policy
     * @return returns true if the service should only be exposed in HTTPS
     * @throws org.wso2.carbon.security.SecurityConfigException ex
     */
    public boolean isHttpsTransportOnly(Policy policy) throws SecurityConfigException {

        // When there is a transport binding sec policy assertion,
        // the service should be exposed only via HTTPS
        boolean httpsRequired = false;

        try {
            Iterator alternatives = policy.getAlternatives();
            if (alternatives.hasNext()) {
                List it = (List) alternatives.next();

                RampartPolicyData rampartPolicyData = RampartPolicyBuilder.build(it);
                if (rampartPolicyData.isTransportBinding()) {
                    httpsRequired = true;
                } else if (rampartPolicyData.isSymmetricBinding()) {
                    Token encrToken = rampartPolicyData.getEncryptionToken();
                    if (encrToken instanceof SecureConversationToken) {
                        Policy bsPol = ((SecureConversationToken) encrToken).getBootstrapPolicy();
                        Iterator alts = bsPol.getAlternatives();
                        if (alts.hasNext()) {
                        }
                        List bsIt = (List) alts.next();
                        RampartPolicyData bsRampartPolicyData = RampartPolicyBuilder.build(bsIt);
                        httpsRequired = bsRampartPolicyData.isTransportBinding();
                    }
                }
            }
        } catch (WSSPolicyException e) {
            log.error(e);
            throw new SecurityConfigException(e.getMessage(), e);
        }

        return httpsRequired;
    }

    /**
     * Get "https" transports in the AxisConfig
     * 
     * @return list
     */
    public List<String> getHttpsTransports() {

        List<String> httpsTransports = new ArrayList<String>();
        for (Iterator iter = axisConfig.getTransportsIn().keySet().iterator(); iter.hasNext();) {
            String transport = (String) iter.next();
            if (transport.toLowerCase().indexOf(SecurityConstants.HTTPS_TRANSPORT) != -1) {
                httpsTransports.add(transport);
            }
        }
        return httpsTransports;
    }
    /**
     * Get all transports in AxisConfig
     *
     * @return list of all transports
     */
    public List<String> getAllTransports() {

        List<String> allTransports = new ArrayList<String>();
        for (Iterator iter = axisConfig.getTransportsIn().keySet().iterator(); iter.hasNext();) {
            String transport = (String) iter.next();
            allTransports.add(transport);
        }
        return allTransports;
    }
                 
    public SecurityConfigData getSecurityConfigData(String serviceName, String scenarioId,
                                                    String policyPath) throws SecurityConfigException {

        SecurityConfigData data = null;
        AxisService service = axisConfig.getServiceForActivation(serviceName);
        String serviceGroupId = service.getAxisServiceGroup().getServiceGroupName();
        try {
            if (scenarioId == null) {
                return data;
            }

            /**
             * Scenario ID can either be a default one (out of 15) or "policyFromRegistry", which
             * means the current scenario refers to a custom policy from registry. If that is the
             * case, we can't read the current scenario from the WSU ID. Therefore, we don't
             * check the scenario ID. In default cases, we check it.
             */
            if (scenarioId.equals(SecurityConstants.POLICY_FROM_REG_SCENARIO)) {
                Parameter param = service.getParameter(SecurityConstants.SECURITY_POLICY_PATH);
                if (param == null || !policyPath.equals(param.getValue())) {
                    return data;
                }
            } else {
                SecurityScenario scenario = readCurrentScenario(serviceName);
                if (scenario == null || !scenario.getScenarioId().equals(scenarioId)) {
                    return data;
                }
            }

            boolean isTransactionStarted = serviceGroupFilePM.isTransactionStarted(serviceGroupId);
            if(!isTransactionStarted) {
                serviceGroupFilePM.beginTransaction(serviceGroupId);
            }
            data = new SecurityConfigData();

            //may be we don't need this in the new persistence model
            String serviceXPath = PersistenceUtils.getResourcePath(service);
            AuthorizationManager acReader = realm.getAuthorizationManager();
            String[] roles = acReader.getAllowedRolesForResource(
                    serviceGroupId+"/"+serviceName,
                    UserCoreConstants.INVOKE_SERVICE_PERMISSION);

            data.setUserGroups(roles);

            List pvtStores = serviceGroupFilePM.getAssociations(serviceGroupId, serviceXPath,
                    SecurityConstants.ASSOCIATION_PRIVATE_KEYSTORE);

            if (pvtStores.size() > 0) {
                String temp = ((OMElement) pvtStores.get(0)).
                        getAttributeValue(new QName(Resources.Associations.DESTINATION_PATH));
                if (temp.startsWith("//")) {
                    temp = temp.substring(1);
                }
                if (temp.equals(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                        RegistryResources.SecurityManagement.PRIMARY_KEYSTORE_PHANTOM_RESOURCE)) {
                    ServerConfiguration config = ServerConfiguration.getInstance();
                    String file = new File(config.getFirstProperty(RegistryResources
                            .SecurityManagement.SERVER_PRIMARY_KEYSTORE_FILE)).getAbsolutePath();
                    String name = KeyStoreUtil.getKeyStoreFileName(file);
                    data.setPrivateStore(name);
                } else {
                    temp = temp.substring(temp.lastIndexOf("/") + 1);
                    data.setPrivateStore(temp);
                }
            }

            List tstedStores = serviceGroupFilePM.getAssociations(serviceGroupId, serviceXPath,
                    SecurityConstants.ASSOCIATION_TRUSTED_KEYSTORE);
            String[] trustedStores = new String[tstedStores.size()];
            for (int i = 0; i < tstedStores.size(); i++) {
                String temp = ((OMElement) tstedStores.get(0)).
                        getAttributeValue(new QName(Resources.Associations.DESTINATION_PATH));
                if (temp.startsWith("//")) {
                    temp = temp.substring(1);
                }
                if (temp.equals(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                        RegistryResources.SecurityManagement.PRIMARY_KEYSTORE_PHANTOM_RESOURCE)) {
                    ServerConfiguration config = ServerConfiguration.getInstance();
                    String file = new File(config.getFirstProperty(RegistryResources
                            .SecurityManagement.SERVER_PRIMARY_KEYSTORE_FILE)).getAbsolutePath();
                    String name = KeyStoreUtil.getKeyStoreFileName(file);
                    trustedStores[i] = name;
                } else {
                    temp = temp.substring(temp.lastIndexOf("/") + 1);
                    trustedStores[i] = temp;
                }
            }
            data.setTrustedKeyStores(trustedStores);

            KerberosConfigData kerberosData = this.readKerberosConfigurations(service);
            data.setKerberosConfigurations(kerberosData);

            if(!isTransactionStarted) {
                serviceGroupFilePM.commitTransaction(serviceGroupId);
            }
            return data;
        } catch (PersistenceException e) {
            // TODO Auto-generated catch block
            log.error(e.getMessage(), e);
            serviceGroupFilePM.rollbackTransaction(serviceGroupId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            serviceGroupFilePM.rollbackTransaction(serviceGroupId);
        }
        return data;
    }

    public SecurityScenario readCurrentScenario(String serviceName) throws SecurityConfigException {
        SecurityScenario scenario = null;

        AxisService service = axisConfig.getServiceForActivation(serviceName);
        String serviceGroupId = null;
        try {
            if (service == null) {
                // try to find it from the transit ghost map
                try {
                    service = GhostDeployerUtils
                            .getTransitGhostServicesMap(axisConfig).get(serviceName);
                } catch (AxisFault axisFault) {
                    log.error("Error while reading Transit Ghosts map", axisFault);
                }
                if (service == null) {
                    throw new SecurityConfigException("AxisService is Null" + service);
                }
            }
            serviceGroupId = service.getAxisServiceGroup().getServiceGroupName();

            // persist
            boolean isTransactionStarted = serviceGroupFilePM.isTransactionStarted(serviceGroupId);
            if(!isTransactionStarted) {
                serviceGroupFilePM.beginTransaction(serviceGroupId);
            }

            String servicePath = PersistenceUtils.getResourcePath(service);
            String policyElementPath = servicePath+"/"+Resources.POLICIES+"/"+Resources.POLICY;

            if (!serviceGroupFilePM.elementExists(serviceGroupId, policyElementPath)) {

                if(service.getPolicySubject() != null &&
                        service.getPolicySubject().getAttachedPolicyComponents() != null){
                    Iterator iterator = service.getPolicySubject().
                                                    getAttachedPolicyComponents().iterator();
                    if(!iterator.hasNext()){
                        if(!isTransactionStarted) {
                            serviceGroupFilePM.rollbackTransaction(serviceGroupId); //no need to commit because no writes happened.
                        }
                        return scenario;
                    }
                }                
            } else {
                // if there are no policies under the collection, no need to proceed
                List policyElements = serviceGroupFilePM.getAll(serviceGroupId, policyElementPath);
                if (policyElements.size() == 0) {
                    if(!isTransactionStarted) {
                        serviceGroupFilePM.rollbackTransaction(serviceGroupId);
                    }
                    return scenario;
                }
            }
            if(!isTransactionStarted) {
                serviceGroupFilePM.commitTransaction(serviceGroupId);
            }

            // after this point, we are going to do some policy related operations in the
            // AxisService object. Therefore, if the existing service is a ghost service, deploy
            // the actual one
            if (GhostDeployerUtils.isGhostService(service)) {
                service = GhostDeployerUtils.deployActualService(axisConfig, service);
            }

            scenario = null;

            Map endPointMap = service.getEndpoints();
            for (Object o : endPointMap.entrySet()) {
                SecurityScenario epSecurityScenario = null;

                Map.Entry entry = (Map.Entry) o;
                AxisEndpoint point = (AxisEndpoint) entry.getValue();
                AxisBinding binding = point.getBinding();
                java.util.Collection policies = binding.getPolicySubject()
                        .getAttachedPolicyComponents();
                Iterator policyComponents = policies.iterator();
                String policyId = null;
                while (policyComponents.hasNext()) {
                    PolicyComponent currentPolicyComponent = (PolicyComponent) policyComponents
                            .next();
                    if (currentPolicyComponent instanceof Policy) {
                        policyId = ((Policy) currentPolicyComponent).getId();
                    } else if (currentPolicyComponent instanceof PolicyReference) {
                        policyId = ((PolicyReference) currentPolicyComponent).getURI().substring(1);
                    }
                    if(policyId != null){
                        // Check whether this is a security scenario
                        epSecurityScenario = SecurityScenarioDatabase.getByWsuId(policyId);
                    }
                }

                // If a scenario is NOT applied to at least one non HTTP
                // binding,
                // we consider the service unsecured.
                if (epSecurityScenario == null) {
                    if (!binding.getName().getLocalPart().contains("HttpBinding")) {
                        scenario = epSecurityScenario;
                        break;
                    }
                } else {
                    scenario = epSecurityScenario;
                }
            }

            // If the binding level policies are not present, check whether there is a policy attached
            // at the service level. This is a fix for Securing Proxy Services.
            if(scenario == null){
                java.util.Collection policies = service.getPolicySubject()
                        .getAttachedPolicyComponents();
                Iterator policyComponents = policies.iterator();
                String policyId = null;
                while (policyComponents.hasNext()) {
                    PolicyComponent currentPolicyComponent = (PolicyComponent) policyComponents
                            .next();
                    if (currentPolicyComponent instanceof Policy) {
                        policyId = ((Policy) currentPolicyComponent).getId();
                    } else if (currentPolicyComponent instanceof PolicyReference) {
                        policyId = ((PolicyReference) currentPolicyComponent).getURI().substring(1);
                    } else {
                        continue;
                    }
                    if(policyId != null){
                        // Check whether this is a security scenario
                        scenario = SecurityScenarioDatabase.getByWsuId(policyId);
                    }
                }
            }

            return scenario;
        } catch (Exception e) {
            log.error("Error while reading Security Scenario", e);
            serviceGroupFilePM.rollbackTransaction(serviceGroupId);
            throw new SecurityConfigException("readingSecurity", e);
        }

    }

    /**
     * If the given service is in ghost state, force the actual service deployment. This method
     * is useful when we want to make sure the actual service is in the system before we do some
     * operation on the service.
     *
     * @param serviceName - name of the service
     */
    public void forceActualServiceDeployment(String serviceName) {
        AxisService service = axisConfig.getServiceForActivation(serviceName);
        if (service == null) {
            // try to find it from the transit ghost map
            try {
                service = GhostDeployerUtils
                        .getTransitGhostServicesMap(axisConfig).get(serviceName);
            } catch (AxisFault axisFault) {
                log.error("Error while reading Transit Ghosts map", axisFault);
            }
        }
        if (service != null && GhostDeployerUtils.isGhostService(service)) {
            // if the service is a ghost service, load the actual service
            try {
                GhostDeployerUtils.deployActualService(axisConfig, service);
            } catch (AxisFault axisFault) {
                log.error("Error while loading actual service from Ghost", axisFault);
            }
        }
    }

    private void setRahasParameters(AxisService axisService, String privateKeyStore)
            throws PersistenceException, AxisFault {
        // TODO add to the registry and persist sturr
        Properties cryptoProps = new Properties();

        String serviceGroupId = axisService.getAxisServiceGroup().getServiceGroupName();
        String serviceName = axisService.getName();
        String serviceXPath = PersistenceUtils.getResourcePath(axisService);

        List pvtStores = serviceGroupFilePM.getAssociations(serviceGroupId, serviceXPath,
                SecurityConstants.ASSOCIATION_PRIVATE_KEYSTORE);
        List tstedStores = serviceGroupFilePM.getAssociations(serviceGroupId, serviceXPath,
                SecurityConstants.ASSOCIATION_TRUSTED_KEYSTORE);

        if (pvtStores != null && pvtStores.size() > 0) {
            String keyAlias = null;
            ServerConfiguration serverConfig = ServerConfiguration.getInstance();
            keyAlias = serverConfig.getFirstProperty("Security.KeyStore.KeyAlias");
            cryptoProps.setProperty(ServerCrypto.PROP_ID_PRIVATE_STORE, privateKeyStore);
            cryptoProps.setProperty(ServerCrypto.PROP_ID_DEFAULT_ALIAS, keyAlias);
        }
        StringBuilder trustStores = new StringBuilder();

        for (Object node : tstedStores) {
            OMElement assoc = (OMElement) node;
            String tstedStore = assoc.getAttributeValue(new QName(Resources.Associations.DESTINATION_PATH));
            String name = tstedStore.substring(tstedStore.lastIndexOf("/"));
            trustStores.append(name).append(",");
        }

        cryptoProps.setProperty(ServerCrypto.PROP_ID_TRUST_STORES, trustStores.toString());

        try {
            setServiceParameterElement(serviceName, RahasUtil.getSCTIssuerConfigParameter(
                    ServerCrypto.class.getName(), cryptoProps, -1, null, true, true));
            setServiceParameterElement(serviceName, RahasUtil.getTokenCancelerConfigParameter());
            OMElement serviceElement = (OMElement) serviceGroupFilePM.get(serviceGroupId, serviceXPath);

            serviceElement.addAttribute(SecurityConstants.PROP_RAHAS_SCT_ISSUER, Boolean.TRUE.toString(), null);
            serviceGroupFilePM.setMetaFileModification(serviceGroupId);
        } catch (Exception e) {
            throw new AxisFault("Could not configure Rahas parameters", e);
        }

    }

    private void persistParameter(String serviceGroupId, Parameter parameter,
                                  String serviceXPath) throws Exception {
        boolean transactionStarted = serviceGroupFilePM.isTransactionStarted(serviceGroupId);
        if (!transactionStarted) {
            serviceGroupFilePM.beginTransaction(serviceGroupId);
        }
        String paramName = parameter.getName();
        if (paramName != null && paramName.trim().length() != 0) {
            if (parameter.getParameterElement() == null && parameter.getValue() != null
                    && parameter.getValue() instanceof String) {
                parameter = ParameterUtil.createParameter(paramName.trim(),
                        (String) parameter.getValue(), parameter.isLocked());
            }
            if (parameter.getParameterElement() != null) {
                OMElement paramElemenet = parameter.getParameterElement().cloneOMElement();
                if(paramElemenet.getAttribute(new QName(Resources.NAME)) == null) {
                    paramElemenet.addAttribute(Resources.NAME, parameter.getName(), null);
                }
                serviceGroupFilePM.put(serviceGroupId, paramElemenet, serviceXPath);
            }
        }
        if (!transactionStarted) {
            serviceGroupFilePM.commitTransaction(serviceGroupId);
        }
    }

    private void removeRahasParameters(AxisService axisService) throws AxisFault {
        String serviceGroupId = axisService.getAxisServiceGroup().getServiceGroupName();
        String serviceXPath = PersistenceUtils.getResourcePath(axisService);
        try {
            if (serviceGroupFilePM.elementExists(serviceGroupId, serviceXPath)) {
                OMElement serviceElement = (OMElement) serviceGroupFilePM.get(serviceGroupId, serviceXPath);
                if(serviceElement.getAttribute(new QName(SecurityConstants.PROP_RAHAS_SCT_ISSUER)) != null) {
                    serviceElement.removeAttribute(
                            serviceElement.getAttribute(new QName(SecurityConstants.PROP_RAHAS_SCT_ISSUER)));
                }
            }
        } catch (Exception e) {
            throw new AxisFault("Could not configure Rahas parameters", e);
        }
    }

    private void setServiceParameterElement(String serviceName, Parameter parameter)
            throws AxisFault {
        AxisService axisService = axisConfig.getServiceForActivation(serviceName);

        if (axisService == null) {
            throw new AxisFault("Invalid service name '" + serviceName + "'");
        }

        Parameter p = axisService.getParameter(parameter.getName());
        if (p != null) {
            if (!p.isLocked()) {
                axisService.addParameter(parameter);
            }
        } else {
            axisService.addParameter(parameter);
        }

    }

    /**
     * Opens the relevant ghost file and adds the security scenario id as a parameter in the
     * service. If the scenario id is null, remove existing security attribute from the file.
     *
     * @param servicePath - Absolute path fo the service file
     * @param serviceName - AxisService name
     * @param scenarioId - Security scenario Id
     */
    private void updateSecScenarioInGhostFile(String servicePath,
                                              String serviceName, String scenarioId) {
        File ghostFile = GhostDeployerUtils.getGhostFile(servicePath, axisConfig);
        if (ghostFile != null && ghostFile.exists()) {
            FileInputStream ghostInStream = null;
            FileOutputStream ghostOutStream = null;
            try {
                // read the ghost file and create an OMElement
                ghostInStream = new FileInputStream(ghostFile);
                OMElement ghostSGElement = new StAXOMBuilder(ghostInStream).getDocumentElement();
                // iterate through all services
                Iterator itr = ghostSGElement
                        .getChildrenWithLocalName(CarbonConstants.GHOST_SERVICE);
                while (itr.hasNext()) {
                    OMElement serviceElm = (OMElement) itr.next();
                    String nameFromFile = serviceElm
                            .getAttributeValue(new QName(CarbonConstants.GHOST_ATTR_NAME));
                    if (nameFromFile != null && nameFromFile.equals(serviceName)) {
                        OMAttribute secAtt = serviceElm.getAttribute(new QName(CarbonConstants
                                .GHOST_ATTR_SECURITY_SCENARIO));
                        // if the correct service is found, update the security scenario attribute
                        if (scenarioId == null) {
                            if (secAtt != null) {
                                serviceElm.removeAttribute(secAtt);
                            } else {
                                return;
                            }
                        } else {
                            if (secAtt == null) {
                                serviceElm.addAttribute(CarbonConstants
                                        .GHOST_ATTR_SECURITY_SCENARIO, scenarioId, null);
                            } else {
                                secAtt.setAttributeValue(scenarioId);
                            }
                        }
                    }
                }
                // serialize the modified OMElement
                ghostOutStream = new FileOutputStream(ghostFile);
                ghostSGElement.serialize(ghostOutStream);
                ghostOutStream.flush();
            } catch (Exception e) {
                log.error("Error while reading ghost file for service : " + servicePath);
            } finally {
                try {
                    if (ghostInStream != null) {
                        ghostInStream.close();
                    }
                    if (ghostOutStream != null) {
                        ghostOutStream.close();
                    }
                } catch (IOException e) {
                    log.error("Error while closing the file output stream", e);
                }
            }
        }
    }

    /**
     * Returns the default "POX_ENABLED" cache
     *
     */
    private Cache<String, String> getPOXCache() {
        CacheManager manager = Caching.getCacheManagerFactory().getCacheManager(POXSecurityHandler.POX_CACHE_MANAGER);
        Cache<String, String> cache = manager.getCache(POXSecurityHandler.POX_ENABLED);
    	return cache;
    }

}
