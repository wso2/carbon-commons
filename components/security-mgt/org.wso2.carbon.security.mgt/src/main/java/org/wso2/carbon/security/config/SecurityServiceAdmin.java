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
package org.wso2.carbon.security.config;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisBinding;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.PolicyInclude;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.core.Resources;
import org.wso2.carbon.core.persistence.PersistenceFactory;
import org.wso2.carbon.core.persistence.PersistenceUtils;
import org.wso2.carbon.core.persistence.file.ModuleFilePersistenceManager;
import org.wso2.carbon.core.persistence.file.ServiceGroupFilePersistenceManager;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.utils.ServerException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SecurityServiceAdmin {

    protected AxisConfiguration axisConfig = null;

    private static Log log = LogFactory.getLog(SecurityServiceAdmin.class);

    private PersistenceFactory persistenceFactory;

    private ServiceGroupFilePersistenceManager serviceGroupFilePM;

    private ModuleFilePersistenceManager moduleFilePM;

    public SecurityServiceAdmin(AxisConfiguration config) throws ServerException {
        this.axisConfig = config;
        try {
            persistenceFactory = PersistenceFactory.getInstance(config);
            serviceGroupFilePM = persistenceFactory.getServiceGroupFilePM();
            moduleFilePM = persistenceFactory.getModuleFilePM();
        } catch (Exception e) {
            log.error("Error creating an PersistenceFactory instance", e);
            throw new ServerException("Error creating an PersistenceFactory instance", e);
        }
    }

    public SecurityServiceAdmin(AxisConfiguration config, Registry registry) {
	    this.axisConfig = config;
        try {
            persistenceFactory = PersistenceFactory.getInstance(config);
            serviceGroupFilePM = persistenceFactory.getServiceGroupFilePM();
            moduleFilePM = persistenceFactory.getModuleFilePM();
        } catch (Exception e) {
            log.error("Error creating an PersistenceFactory instance", e);
        }
    }

    /**
     * This method add Policy to service at the Registry. Does not add the
     * policy to Axis2. To all Bindings available
     * 
     * @param axisService Service
     * @param policy Policy
     * @throws org.wso2.carbon.utils.ServerException se
     */
    public void addSecurityPolicyToAllBindings(AxisService axisService, Policy policy)
	    throws ServerException {
        String serviceGroupId = axisService.getAxisServiceGroup().getServiceGroupName();
        boolean isProxyService = PersistenceUtils.isProxyService(axisService);
	try {
        if (policy.getId() == null) {
            // Generate an ID
            policy.setId(UUIDGenerator.getUUID());
        }
        boolean transactionStarted = serviceGroupFilePM.isTransactionStarted(serviceGroupId);
        if (!transactionStarted) {
            serviceGroupFilePM.beginTransaction(serviceGroupId);
        }

        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        String serviceXPath = PersistenceUtils.getResourcePath(axisService);

        String policiesXPath = serviceXPath+"/"+Resources.POLICIES;

        String policyResourcePath = policiesXPath+
                "/"+Resources.POLICY+
                PersistenceUtils.getXPathTextPredicate(Resources.ServiceProperties.POLICY_UUID, policy.getId());
        if (!serviceGroupFilePM.elementExists(serviceGroupId, policyResourcePath)) {

//            todo use persistenceFactory.getServicePM().persistServicePolicy method instead of below code chunk - kasung

            OMElement policyWrapperElement = omFactory.createOMElement(Resources.POLICY, null);
            policyWrapperElement.addAttribute(Resources.ServiceProperties.POLICY_TYPE,
                    "" + PolicyInclude.BINDING_POLICY, null);

            OMElement idElement = omFactory.createOMElement(Resources.ServiceProperties.POLICY_UUID, null);
            idElement.setText("" + policy.getId());
            policyWrapperElement.addChild(idElement);

            OMElement policyElementToPersist = PersistenceUtils.createPolicyElement(policy);
            policyWrapperElement.addChild(policyElementToPersist);

            if (!serviceGroupFilePM.elementExists(serviceGroupId, policiesXPath)) {
                serviceGroupFilePM.put(serviceGroupId,
                        omFactory.createOMElement(Resources.POLICIES, null), serviceXPath);
            }
            serviceGroupFilePM.put(serviceGroupId, policyWrapperElement, policiesXPath);

//           todo the old impl doesn't add the policyUUID to the binding elements. Is it ok?
//            if (!serviceGroupFilePM.elementExists(serviceGroupId, serviceXPath+
//                    PersistenceUtils.getXPathTextPredicate(
//                            Resources.ServiceProperties.POLICY_UUID, policy.getId() ))) {
//                serviceGroupFilePM.put(serviceGroupId, idElement.cloneOMElement(), serviceXPath); + path to bindings?
//            }
        }

        //to registry if proxy
        if(isProxyService) {
            String registryServicePath = RegistryResources.SERVICE_GROUPS
                    + axisService.getAxisServiceGroup().getServiceGroupName()
                    + RegistryResources.SERVICES + axisService.getName();

            persistenceFactory.getServicePM().persistPolicyToRegistry(policy,
                    "" + PolicyInclude.BINDING_POLICY, registryServicePath);
        }

        Map endPointMap = axisService.getEndpoints();
        List<String> lst = new ArrayList<String>();
        for (Object o : endPointMap.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            AxisEndpoint point = (AxisEndpoint) entry.getValue();
            AxisBinding binding = point.getBinding();
            String bindingName = binding.getName().getLocalPart();

            //only UTOverTransport is allowed for HTTP
            if (bindingName.endsWith("HttpBinding") &&
                (!policy.getAttributes().containsValue("UTOverTransport"))){
                    continue;
             }

            binding.getPolicySubject().attachPolicy(policy);
            if (lst.contains(bindingName)) {
                continue;
            } else {
                lst.add(bindingName);
            }
            // Add the new policy to the registry
        }
        Iterator<String> ite = lst.iterator();
        while (ite.hasNext()) {
            String bindingName = ite.next();

            //only UTOverTransport is allowed for HTTP
            if (bindingName.endsWith("HttpBinding") &&
                    (!policy.getAttributes().containsValue("UTOverTransport"))){
                continue;
            }

            String bindingElementPath = serviceXPath+
                    "/"+Resources.ServiceProperties.BINDINGS+
                    "/"+Resources.ServiceProperties.BINDING_XML_TAG+
                    PersistenceUtils.getXPathAttrPredicate(Resources.NAME, bindingName);

            if (!serviceGroupFilePM.elementExists(serviceGroupId, bindingElementPath+
                    "/"+Resources.ServiceProperties.POLICY_UUID+
                    PersistenceUtils.getXPathTextPredicate(null, policy.getId())) ) {

                OMElement bindingElement;
                if (serviceGroupFilePM.elementExists(serviceGroupId, bindingElementPath)) {
                    bindingElement = (OMElement) serviceGroupFilePM.get(serviceGroupId, bindingElementPath);
                } else {
                    bindingElement = omFactory.createOMElement(Resources.ServiceProperties.BINDINGS, null);
                }

                OMElement idElement = omFactory.createOMElement(Resources.ServiceProperties.POLICY_UUID, null);
                idElement.setText("" + policy.getId());
                bindingElement.addChild(idElement.cloneOMElement());

                serviceGroupFilePM.put(serviceGroupId, bindingElement, serviceXPath+"/"+Resources.ServiceProperties.BINDINGS);
            }
        }
        if (!transactionStarted) {
            serviceGroupFilePM.commitTransaction(serviceGroupId);
        }
	    // at axis2
	} catch (Exception e) {
        log.error(e);
        serviceGroupFilePM.rollbackTransaction(serviceGroupId);
	    throw new ServerException("addPoliciesToService");
	}
    }

    public void removeSecurityPolicyFromAllBindings(AxisService axisService, String uuid)
	    throws ServerException {
        //todo apparently policyFromRegistry in bindings didn't got removed1 - kasung
        String serviceGroupId = axisService.getAxisServiceGroup().getServiceGroupName();
	try {
        String serviceXPath = PersistenceUtils.getResourcePath(axisService);

        Map endPointMap = axisService.getEndpoints();
        List<String> lst = new ArrayList<String>();
        for (Object o : endPointMap.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            AxisEndpoint point = (AxisEndpoint) entry.getValue();
            AxisBinding binding = point.getBinding();
            if(binding.getPolicySubject().getAttachedPolicyComponent(uuid) != null){
                binding.getPolicySubject().detachPolicyComponent(uuid);            
                String bindingName = binding.getName().getLocalPart();
                if (lst.contains(bindingName)) {
                    continue;
                } else {
                    lst.add(bindingName);
                }
            }
            // Add the new policy to the registry
        }
        boolean transactionStarted = serviceGroupFilePM.isTransactionStarted(serviceGroupId);
        if (!transactionStarted) {
            serviceGroupFilePM.beginTransaction(serviceGroupId);
        }
        for (String bindingName : lst) {
            String bindingElementPath = serviceXPath +
                    "/" + Resources.ServiceProperties.BINDINGS +
                    "/" + Resources.ServiceProperties.BINDING_XML_TAG +
                    PersistenceUtils.getXPathAttrPredicate(Resources.NAME, bindingName);
            serviceGroupFilePM.delete(serviceGroupId, bindingElementPath +
                    "/" + Resources.ServiceProperties.POLICY_UUID +
                    PersistenceUtils.getXPathTextPredicate(null, uuid));
        }
        if (!transactionStarted) {
            serviceGroupFilePM.commitTransaction(serviceGroupId);
        }
        // at axis2
	} catch (Exception e) {
        log.error(e.getMessage(), e);
        serviceGroupFilePM.rollbackTransaction(serviceGroupId);
	    throw new ServerException("addPoliciesToService");
	}
    }

    public void setServiceParameterElement(String serviceName, Parameter parameter)
	    throws AxisFault {
	AxisService axisService = axisConfig.getService(serviceName);

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

}
