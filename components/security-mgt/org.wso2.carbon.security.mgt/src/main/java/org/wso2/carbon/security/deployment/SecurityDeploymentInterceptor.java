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
package org.wso2.carbon.security.deployment;

import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEvent;
import org.apache.axis2.engine.AxisObserver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;
import org.apache.neethi.PolicyEngine;
import org.apache.neethi.PolicyReference;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.core.Resources;
import org.wso2.carbon.core.persistence.PersistenceFactory;
import org.wso2.carbon.core.persistence.PersistenceUtils;
import org.wso2.carbon.core.persistence.file.ModuleFilePersistenceManager;
import org.wso2.carbon.core.persistence.file.ServiceGroupFilePersistenceManager;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.security.SecurityConstants;
import org.wso2.carbon.security.SecurityScenario;
import org.wso2.carbon.security.SecurityScenarioDatabase;
import org.wso2.carbon.security.SecurityServiceHolder;
import org.wso2.carbon.security.util.RahasUtil;
import org.wso2.carbon.security.util.ServerCrypto;
import org.wso2.carbon.security.util.ServicePasswordCallbackHandler;
import org.wso2.carbon.security.util.XmlConfiguration;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.PreAxisConfigurationPopulationObserver;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * This is a deployment interceptor which handles service specific security configurations on
 * service deployment events. It is also published as an OSGi service, so that Carbon core can
 * add this to the AxisConfiguration.
 *
 * NOTE: This is a special type of AxisObserver, which can be used only within an OSGi framework
 * hence should not be added to the axis2.xml directly. If done so, it will throw NPEs, since
 * the registry & userRealm references are set through the OSGi decalative service framework.
 *
 * @scr.component name="org.wso2.carbon.security.deployment.SecurityDeploymentInterceptor"
 * immediate="true"
 * @scr.reference name="registry.service"
 *                interface="org.wso2.carbon.registry.core.service.RegistryService"
 *                cardinality="1..1" policy="dynamic" bind="setRegistryService"
 *                unbind="unsetRegistryService"
 * @scr.reference name="user.realmservice.default" interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 */

public class SecurityDeploymentInterceptor implements AxisObserver {
    private static final Log log = LogFactory.getLog(SecurityDeploymentInterceptor.class);

    private PersistenceFactory persistenceFactory;
    private ServiceGroupFilePersistenceManager serviceGroupFilePM;
    private ModuleFilePersistenceManager moduleFilePM;

    protected void activate(ComponentContext ctxt) {
        BundleContext bundleCtx = ctxt.getBundleContext();
        try {
	    PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            carbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);

            loadSecurityScenarios(SecurityServiceHolder.getRegistryService().getConfigSystemRegistry(),
                                  bundleCtx);
        } catch (Exception e) {
            String msg = "Cannot load security scenarios";
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }

        try {
            addKeystores();
        } catch (Exception e) {
            String msg = "Cannot add keystores";
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
        // Publish the OSGi service
        Dictionary props = new Hashtable();
        props.put(CarbonConstants.AXIS2_CONFIG_SERVICE, AxisObserver.class.getName());
        bundleCtx.registerService(AxisObserver.class.getName(), this, props);

        PreAxisConfigurationPopulationObserver preAxisConfigObserver =
                new PreAxisConfigurationPopulationObserver() {
                    public void createdAxisConfiguration(AxisConfiguration axisConfiguration) {
                    	init(axisConfiguration);
                        axisConfiguration.addObservers(SecurityDeploymentInterceptor.this);
                    }
                };
        bundleCtx.registerService(PreAxisConfigurationPopulationObserver.class.getName(),
                                  preAxisConfigObserver, null);

        // Publish an OSGi service to listen tenant configuration context creation events
        Dictionary properties = new Hashtable();
        properties.put(CarbonConstants.AXIS2_CONFIG_SERVICE,
                       Axis2ConfigurationContextObserver.class.getName());
        bundleCtx.registerService(Axis2ConfigurationContextObserver.class.getName(),
                                                new SecurityDeploymentListener(), properties);
    }
    
    public void init(AxisConfiguration axisConfig) {

        try {
            persistenceFactory = PersistenceFactory.getInstance(axisConfig);
            serviceGroupFilePM = persistenceFactory.getServiceGroupFilePM();
            moduleFilePM = persistenceFactory.getModuleFilePM();
        } catch (AxisFault e) {
            log.error("Error while adding PersistenceFactory parameter to axisConfig", e);
        }
    }

    public void moduleUpdate(AxisEvent event, AxisModule module) {
        // This method will not be used
    }

    public void serviceGroupUpdate(AxisEvent event, AxisServiceGroup serviceGroup) {
        // This method will not be used
    }

    public void serviceUpdate(AxisEvent axisEvent, AxisService axisService) {
        int eventType = axisEvent.getEventType();
        String serviceGroupId = axisService.getAxisServiceGroup().getServiceGroupName();

        if (eventType == AxisEvent.SERVICE_DEPLOY) {
            try {
                boolean isTransactionStarted = serviceGroupFilePM.isTransactionStarted(serviceGroupId);
                if(!isTransactionStarted) {
                    serviceGroupFilePM.beginTransaction(serviceGroupId);
                }
                String policyResourcePath = PersistenceUtils.getResourcePath(axisService)+
                        "/"+Resources.POLICIES+"/"+Resources.POLICY;

                List policies = serviceGroupFilePM.getAll(serviceGroupId, policyResourcePath);

                //removing security scenarioID parameter from axis service if exists
                Parameter scenarioIDParam = axisService.getParameter(SecurityConstants.SCENARIO_ID_PARAM_NAME);
                if (scenarioIDParam != null) {
                    axisService.removeParameter(scenarioIDParam);
                }

                SecurityScenario scenario = null;
                if(policies == null || policies.size() == 0){
                    if(axisService.getPolicySubject() != null &&
                            axisService.getPolicySubject().getAttachedPolicyComponents() != null){
                        Iterator iterator = axisService.getPolicySubject().
                                                        getAttachedPolicyComponents().iterator();
                        String policyId = null;
                        while(iterator.hasNext()){
                            PolicyComponent currentPolicyComponent = (PolicyComponent) iterator.next();
                            if (currentPolicyComponent instanceof Policy) {
                                policyId = ((Policy) currentPolicyComponent).getId();
                            } else if (currentPolicyComponent instanceof PolicyReference) {
                                policyId = ((PolicyReference) currentPolicyComponent).getURI().substring(1);
                            }
                            if(policyId != null){
                                scenario = SecurityScenarioDatabase.getByWsuId(policyId);
                                if(scenario == null){
                                    // if there is no security scenario id,  put default id
                                    SecurityScenario securityScenario = new SecurityScenario();
                                    securityScenario.setScenarioId(SecurityConstants.CUSTOM_SECURITY_SCENARIO);
                                    securityScenario.setWsuId(policyId);
                                    securityScenario.setGeneralPolicy(false);
                                    securityScenario.setSummary(SecurityConstants.CUSTOM_SECURITY_SCENARIO_SUMMARY);
                                    SecurityScenarioDatabase.put(policyId, securityScenario);
                                    scenario = securityScenario;
                                }
                            }
                        }
                    } else {
                        // do nothing, because no policy
                        if(!isTransactionStarted) {
                            serviceGroupFilePM.rollbackTransaction(serviceGroupId);
                        }
                        return;
                    }
                } else {
                    for (Object node : policies) {
                        OMElement policyWrapperElement = (OMElement) node;
                        Policy policy = null;
                        try{
                            policy = PolicyEngine.getPolicy(policyWrapperElement.
                                    getFirstChildWithName(new QName(PolicyEngine.POLICY_NAMESPACE,
                                    PolicyEngine.POLICY)));
                        } catch (Exception e){
                            // just ignore, we want to make sure this is security policy    
                        }
                        if(policy != null){
                            String policyId = PersistenceUtils.getPolicyUUIDFromWrapperOM(policyWrapperElement);
                            if (policyId != null) {
                                scenario = SecurityScenarioDatabase.getByWsuId(policyId);
                                if (scenario != null) {
                                    break;
                                } else {
                                    // if there is no security scenario id,  put default id
                                    // before that check whether this is actually a security policy
                                    // by comparing UUID that is used to persist them

                                    SecurityScenario securityScenario = new SecurityScenario();
                                    securityScenario.setScenarioId(SecurityConstants.CUSTOM_SECURITY_SCENARIO);
                                    securityScenario.setWsuId(policyId);
                                    securityScenario.setGeneralPolicy(false);
                                    securityScenario.setSummary(SecurityConstants.CUSTOM_SECURITY_SCENARIO_SUMMARY);
                                    scenario = securityScenario;
                                    if(!("RMPolicy".equals(policyId) || "WSO2CachingPolicy".equals(policyId)
                                            || "WSO2ServiceThrottlingPolicy".equals(policyId))){
                                        SecurityScenarioDatabase.put(policyId, securityScenario);    
                                    }
                                }
                            } else {
                                log.error("Policy UUID is not found though a policy element exist.");
                            }
                        }
                    }
                }

                if (scenario != null) {
                    applySecurityParameters(axisService, scenario);
                }

                if(!isTransactionStarted) {
                    serviceGroupFilePM.commitTransaction(serviceGroupId);
                }
            } catch (Exception e) {
                String msg = "Cannot handle service DEPLOY event for service: " +
                             axisService.getName();
                log.error(msg, e);
                serviceGroupFilePM.rollbackTransaction(serviceGroupId);
                throw new RuntimeException(msg, e);
            }
		} else if (eventType == AxisEvent.SERVICE_REMOVE) {

			try {
				//TODO: https://wso2.org/jira/browse/WSAS-1602
//				UserRealm userRealm = SecurityServiceHolder.getRegistryService().getUserRealm(
//						tenantId);
//				AuthorizationManager acAdmin = userRealm.getAuthorizationManager();
//				String resourceName = serviceGroupId + "/" + axisService.getName();
//				String[] roles = acAdmin.getAllowedRolesForResource(resourceName,
//						UserCoreConstants.INVOKE_SERVICE_PERMISSION);
//				for (int i = 0; i < roles.length; i++) {
//					acAdmin.clearRoleAuthorization(roles[i], resourceName,
//							UserCoreConstants.INVOKE_SERVICE_PERMISSION);
//				}
			} catch (Exception e) {
				throw new RuntimeException(
						"Error while removing security while undeploying the service "
								+ axisService.getName(), e);
			}
		}

    }

    private void loadSecurityScenarios(Registry registry,
                                       BundleContext bundleContext) throws Exception {

        // TODO: Load into all tenant DBs
        // Load security scenarios
        URL resource = bundleContext.getBundle().getResource("/scenarios/scenario-config.xml");
        XmlConfiguration xmlConfiguration = new XmlConfiguration(resource.openStream(),
                                                                 SecurityConstants.SECURITY_NAMESPACE);

        OMElement[] elements = xmlConfiguration.getElements("//ns:Scenario");
        try {
            boolean transactionStarted = Transaction.isStarted();
            if (!transactionStarted) {
                registry.beginTransaction();
            }

            for (OMElement scenarioEle : elements) {
                SecurityScenario scenario = new SecurityScenario();
                String scenarioId = scenarioEle.getAttribute(SecurityConstants.ID_QN)
                        .getAttributeValue();

                scenario.setScenarioId(scenarioId);
                scenario.setSummary(scenarioEle.getFirstChildWithName(SecurityConstants.SUMMARY_QN)
                        .getText());
                scenario.setDescription(scenarioEle.getFirstChildWithName(
                        SecurityConstants.DESCRIPTION_QN).getText());
                scenario.setCategory(scenarioEle.getFirstChildWithName(SecurityConstants.CATEGORY_QN)
                        .getText());
                scenario.setWsuId(scenarioEle.getFirstChildWithName(SecurityConstants.WSUID_QN)
                        .getText());
                scenario.setType(scenarioEle.getFirstChildWithName(SecurityConstants.TYPE_QN).getText());

                String resourceUri = SecurityConstants.SECURITY_POLICY + "/" + scenarioId;

                for (Iterator modules = scenarioEle.getFirstChildWithName(SecurityConstants.MODULES_QN)
                        .getChildElements(); modules.hasNext();) {
                    String module = ((OMElement) modules.next()).getText();
                    scenario.addModule(module);
                }

                // Save it in the DB
                SecurityScenarioDatabase.put(scenarioId, scenario);

                // Store the scenario in the Registry
                if (!scenarioId.equals(SecurityConstants.SCENARIO_DISABLE_SECURITY) &&
                        !scenarioId.equals(SecurityConstants.POLICY_FROM_REG_SCENARIO)) {
                    Resource scenarioResource = new ResourceImpl();
                    scenarioResource.
                            setContentStream(bundleContext.getBundle().
                                    getResource("scenarios/" + scenarioId + "-policy.xml").openStream());
                    scenarioResource.setMediaType("application/policy+xml");
                    if (!registry.resourceExists(resourceUri)) {
                        registry.put(resourceUri, scenarioResource);
                    }

                    // Cache the resource in-memory in order to add it to the newly created tenants
                    SecurityServiceHolder.addPolicyResource(resourceUri, scenarioResource);
                }
            }
            if (!transactionStarted) {
                registry.commitTransaction();
            }
        } catch (Exception e) {
            registry.rollbackTransaction();
            throw e;
        }
    }

    private void addKeystores() throws Exception {
        Registry registry = SecurityServiceHolder.getRegistryService().getGovernanceSystemRegistry();
        try {
            boolean transactionStarted = Transaction.isStarted();
            if (!transactionStarted) {
                registry.beginTransaction();
            }
            if (!registry.resourceExists(SecurityConstants.KEY_STORES)) {
                Collection kstores = registry.newCollection();
                registry.put(SecurityConstants.KEY_STORES, kstores);

                Resource primResource = registry.newResource();
                if (!registry.resourceExists(RegistryResources.SecurityManagement.PRIMARY_KEYSTORE_PHANTOM_RESOURCE)) {
                    registry.put(RegistryResources.SecurityManagement.PRIMARY_KEYSTORE_PHANTOM_RESOURCE,
                                 primResource);
                }
            }
            if (!transactionStarted) {
                registry.commitTransaction();
            }
        } catch (Exception e) {
            registry.rollbackTransaction();
            throw e;
        }
    }

    private void applySecurityParameters(AxisService service, SecurityScenario secScenario) {
        try {
            String serviceGroupId = service.getAxisServiceGroup().getServiceGroupName();
            String serviceName = service.getName();
            ServiceGroupFilePersistenceManager sfpm = persistenceFactory.getServiceGroupFilePM();

            String registryServicePath = RegistryResources.SERVICE_GROUPS
                    + service.getAxisServiceGroup().getServiceGroupName()
                    + RegistryResources.SERVICES + serviceName;
            
			AxisModule rahas = service.getAxisConfiguration()
					.getModule("rahas");
			if (!"scenario1".equals(secScenario.getScenarioId())) {
				service.disengageModule(rahas);
				service.engageModule(rahas);
			}
            
            String serviceXPath = Resources.ServiceProperties.ROOT_XPATH
                    + PersistenceUtils.getXPathAttrPredicate(Resources.NAME, serviceName);

            UserRealm userRealm = (UserRealm) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                    .getUserRealm();
            UserRegistry govRegistry = (UserRegistry) PrivilegedCarbonContext
                    .getThreadLocalCarbonContext().getRegistry(RegistryType.SYSTEM_GOVERNANCE);

            ServicePasswordCallbackHandler handler = new ServicePasswordCallbackHandler(
                    persistenceFactory, serviceGroupId, serviceName, serviceXPath,
                    registryServicePath, govRegistry, userRealm);

            Parameter param = new Parameter();
            param.setName(WSHandlerConstants.PW_CALLBACK_REF);
            param.setValue(handler);
            service.addParameter(param);

            if (!SecurityConstants.USERNAME_TOKEN_SCENARIO_ID.equals(secScenario.getScenarioId())) {
                Parameter param2 = new Parameter();
                param2.setName("disableREST"); // TODO Find the constant
                param2.setValue(Boolean.TRUE.toString());
                service.addParameter(param2);
            }

            Parameter allowRolesParameter = service.getParameter("allowRoles");
            
            if(allowRolesParameter!= null && allowRolesParameter.getValue() != null){

                AuthorizationManager manager = userRealm.getAuthorizationManager();
                String resourceName = serviceGroupId + "/" + serviceName;
                String[] roles = manager.getAllowedRolesForResource(resourceName,
                                                    UserCoreConstants.INVOKE_SERVICE_PERMISSION);
                if(roles != null){
                    for (String role : roles) {
                        manager.clearRoleAuthorization(role, resourceName,
                                                    UserCoreConstants.INVOKE_SERVICE_PERMISSION);
                    }
                }

                String value = (String) allowRolesParameter.getValue();
                String[] allowRoles = value.split(",") ;
                if(allowRoles != null){
                    for(String role : allowRoles){
                        userRealm.getAuthorizationManager().authorizeRole(role, resourceName,
                                                        UserCoreConstants.INVOKE_SERVICE_PERMISSION);
                    }
                }
            }

            OMElement serviceElement = (OMElement) sfpm.get(serviceGroupId, serviceXPath);
            if (serviceElement != null &&
                serviceElement.getAttributeValue(new QName(SecurityConstants.PROP_RAHAS_SCT_ISSUER)) != null) {


                Object[] pvtStores = sfpm.getAll(serviceGroupId, Resources.ServiceProperties.ROOT_XPATH+
                        PersistenceUtils.getXPathAttrPredicate(Resources.NAME, serviceName)+
                        "/"+Resources.Associations.ASSOCIATION_XML_TAG+
                        PersistenceUtils.getXPathAttrPredicate(Resources.Associations.TYPE,
                                SecurityConstants.ASSOCIATION_PRIVATE_KEYSTORE)).
                        toArray();


                Properties cryptoProps = new Properties();

                if (pvtStores != null && pvtStores.length > 0) {
                    ServerConfiguration serverConfig = ServerConfiguration.getInstance();
                    String pvtStore = serverConfig.getFirstProperty("Security.KeyStore.Location");
                    String keyAlias = null;
                    String name = null;                    
                    keyAlias = serverConfig.getFirstProperty("Security.KeyStore.KeyAlias");
                    name = pvtStore.substring(pvtStore.lastIndexOf("/") + 1);
                    cryptoProps.setProperty(ServerCrypto.PROP_ID_PRIVATE_STORE, name);
                    cryptoProps.setProperty(ServerCrypto.PROP_ID_DEFAULT_ALIAS, keyAlias);
                    service.addParameter(RahasUtil.getSCTIssuerConfigParameter(ServerCrypto.class.getName(),
                            cryptoProps, -1, null, true, true));
                    service.addParameter(RahasUtil.getTokenCancelerConfigParameter());

                } else {
                    throw new Exception("Cannot start Rahas");
                }

            }
        } catch (Throwable e) {
            String msg = "Cannot apply security parameters";
            log.error(msg, e);
        }
    }

    public void addParameter(Parameter param) throws AxisFault {
        // This method will not be used
    }

    public void deserializeParameters(OMElement parameterElement) throws AxisFault {
        // This method will not be used
    }

    public Parameter getParameter(String name) {
        // This method will not be used
        return null;
    }

    public ArrayList getParameters() {
        // This method will not be used
        return new ArrayList();
    }

    public boolean isParameterLocked(String parameterName) {
        // This method will not be used
        return false;
    }

    public void removeParameter(Parameter param) throws AxisFault {
        // This method will not be used
    }

    protected void setRegistryService(RegistryService registryService) {
        SecurityServiceHolder.setRegistryService(registryService);
    }

    protected void setRealmService(RealmService realmService) {
        SecurityServiceHolder.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        SecurityServiceHolder.setRealmService(null);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        SecurityServiceHolder.setRegistryService(registryService);    // TODO: Serious OSGi bug here. FIXME Thilina
    }
}
