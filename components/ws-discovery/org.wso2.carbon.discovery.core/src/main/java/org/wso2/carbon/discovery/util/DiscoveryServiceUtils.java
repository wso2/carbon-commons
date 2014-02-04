/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.discovery.util;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.discovery.DiscoveryConstants;
import org.wso2.carbon.discovery.DiscoveryException;
import org.wso2.carbon.discovery.messages.Probe;
import org.wso2.carbon.discovery.messages.TargetService;
import org.wso2.carbon.discovery.search.DiscoveryServiceFilter;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.*;

/**
 * Utility class for adding, updating and removing service artifacts stored in the
 * governance registry. This class directly interacts with the governance API of the
 * Carbon governance components create and manage services discovered through WS-D.
 */
public class DiscoveryServiceUtils {

    private static Registry getRegistry() throws DiscoveryException {
        return (Registry) PrivilegedCarbonContext.getThreadLocalCarbonContext().
                getRegistry(RegistryType.SYSTEM_GOVERNANCE);
    }

    /**
     * Add a discovered target service to the governance registry. This method will
     * also create the endpoint artifacts for the service and attach them with the
     * related service artifacts. The service artifact will be given an auto generated
     * name.
     *
     * @param service WS-D target service to be added to the registry
     * @throws Exception if an error occurs while saving the artifacts to the registry
     */
    public static void addService(TargetService service, Map<String,
            String> headerMap) throws Exception {
        ServiceManager serviceManager = new ServiceManager(getRegistry());
        String serviceId = service.getEpr().getAddress();

        //This part is use to remove the prefix of Service ID
        if(serviceId.startsWith(DiscoveryConstants.EPR_ADDRESS_PREFIX)){
            serviceId = serviceId.replace(DiscoveryConstants.EPR_ADDRESS_PREFIX, "");
        }

        // Delete the existing stuff and start fresh
        Service oldService = serviceManager.getService(serviceId);
        String serviceName = null;
        if (oldService != null) {
            // TODO: Change this once the necessary improvements are in the governance API
            serviceName = oldService.getQName().getLocalPart();
            serviceManager.removeService(serviceId);
        }

        // Create a new service (Use the discovery namespace)
        if (serviceName == null) {
            if(headerMap.containsKey(DiscoveryConstants.DISCOVERY_HEADER_SERVICE_NAME)){
                serviceName = headerMap.
                        get(DiscoveryConstants.DISCOVERY_HEADER_SERVICE_NAME).replace("/", "_");
            } else {
                serviceName = DiscoveryConstants.SERVICE_NAME_PREFIX +
                        new GregorianCalendar().getTimeInMillis();
            }
        }

        Service newService = serviceManager.newService(new QName(
                DiscoveryConstants.WS_DISCOVERY_NAMESPACE, serviceName));
        newService.setId(serviceId);

        // Set the version if provided
        if (service.getMetadataVersion() != -1) {
            newService.addAttribute(DiscoveryConstants.ATTR_METADATA_VERSION,
                    String.valueOf(service.getMetadataVersion()));
        }

         // Store other service metadata (scopes, types, x-addresses)
        QName[] types = service.getTypes();
        String typeList = "";
        if (types != null && types.length > 0) {
            for(int i=0;i<types.length;i++){
                typeList =typeList.concat(types[i].toString());
                if(i != types.length-1){
                    typeList = typeList.concat(",");
                }
            }
        }
        newService.setAttribute(DiscoveryConstants.ATTR_TYPES, typeList);

        URI[] scopes = service.getScopes();
        String scopeList = "";
        if (scopes != null && scopes.length > 0) {
            for(int i=0;i<scopes.length;i++){
                scopeList = scopeList.concat(scopes[i].toString());
                if(i != scopes.length-1){
                    scopeList = scopeList.concat(",");
                }
            }
        }
        newService.setAttribute(DiscoveryConstants.ATTR_SCOPES, scopeList);

        boolean activate = false;
        if(headerMap.containsKey(DiscoveryConstants.DISCOVERY_HEADER_WSDL_URI) &&
                headerMap.get(DiscoveryConstants.DISCOVERY_HEADER_WSDL_URI) != ""){
            newService.setAttribute("interface_wsdlURL",
                    headerMap.get(DiscoveryConstants.DISCOVERY_HEADER_WSDL_URI));
            activate = true;
        } else {
            //If the WSDL available then the endpoint will add by the WSDL
            URI[] uris = service.getXAddresses();
            String[] endpoints = new String[uris.length];
            for(int i=0;i<uris.length;i++){
                endpoints[i] = ":" + uris[i].toString();
            }
            if (uris != null && uris.length > 0) {
                newService.setAttributes(DiscoveryConstants.ATTR_ENDPOINTS, endpoints);
                activate = true;
            }
        }

        // One hot discovered service coming thru....
        serviceManager.addService(newService);
        if (activate) {
           newService.activate();
        }
    }

    public static void deactivateService(TargetService targetService) throws Exception {
        ServiceManager serviceManager = new ServiceManager(getRegistry());
        String serviceId = targetService.getEpr().getAddress();
        //This part is use to remove the prefix of Service ID
        if(serviceId.startsWith(DiscoveryConstants.EPR_ADDRESS_PREFIX)){
            serviceId = serviceId.replace(DiscoveryConstants.EPR_ADDRESS_PREFIX, "");
        }

        Service service = serviceManager.getService(serviceId);
        if (service == null) {
            throw new DiscoveryException("Error while updating discovery metadata. No service " +
                                "exists with the ID: " + serviceId);
        }

        service.deactivate();
    }

    public static void removeServiceEndpoints(TargetService service) throws Exception {
        TargetService oldService = getService(service.getEpr());

        if (oldService == null) {
            throw new DiscoveryException("Error while updating discovery metadata. No service " +
                    "exists with the ID: " + service.getEpr().getAddress());
        }

        // When marking an existing service as inactive try to hold on to
        // the old metadata of the service
        if (service.getScopes() == null) {
            service.setScopes(oldService.getScopes());
        }
        if (service.getTypes() == null) {
            service.setTypes(oldService.getTypes());
        }
        if (service.getMetadataVersion() == -1) {
            service.setMetadataVersion(oldService.getMetadataVersion());
        }

        if (service.getXAddresses() != null && oldService.getXAddresses() != null) {
            // According to the spec this is the set of addresses on
            // which the service is NO LONGER AVAILABLE.
            // We should remove them from the registry.
            List<URI> existingAddresses = new ArrayList<URI>();
            for (URI xAddr : oldService.getXAddresses()) {
                existingAddresses.add(xAddr);
            }

            for (URI xAddr : service.getXAddresses()) {
                existingAddresses.remove(xAddr);
            }

            if (existingAddresses.size() > 0) {
                service.setXAddresses(existingAddresses.
                        toArray(new URI[existingAddresses.size()]));
            } else {
                service.setXAddresses(null);
            }
        }

        addService(service, Collections.<String, String>emptyMap());
    }

    public static void remove(TargetService service) throws Exception {
        ServiceManager serviceManager = new ServiceManager(getRegistry());
        if (serviceManager.getService(service.getEpr().getAddress()) != null) {
            serviceManager.removeService(service.getEpr().getAddress());
        }
    }

    /**
     * Find the WS-D target service identified by the given WS-D identifier.
     *
     * @param epr WS-D service identifier
     * @return a TargetService instance with the given ID or null if no such service exists
     * @throws Exception if an error occurs while accessing the registry
     */
    public static TargetService getService(EndpointReference epr) throws Exception {
        ServiceManager serviceManager = new ServiceManager(getRegistry());

        String serviceId = epr.getAddress();
        //This part is use to remove the prefix of Service ID
        if(serviceId.startsWith(DiscoveryConstants.EPR_ADDRESS_PREFIX)){
            serviceId = serviceId.replace(DiscoveryConstants.EPR_ADDRESS_PREFIX, "");
        }

        Service service = serviceManager.getService(serviceId);
        if (service == null) {
            return null;
        }

        return getTargetService(service);
    }

    /**
     * Search the service artifacts stored in the registry and find the set of services
     * that satisfy the conditions stated in the given WS-D probe. If the probe does not
     * impose any restrictions on the result set, all the services in the registry will
     * be returned.
     *
     * @param probe a WS-D probe describing the search criteria
     * @return an array of TargetService instances matching the probe or null
     * @throws Exception if an error occurs while accessing the registry
     */
    public static TargetService[] findServices(Probe probe) throws Exception {        
        ServiceManager serviceManager = new ServiceManager(getRegistry());
        DiscoveryServiceFilter filter = new DiscoveryServiceFilter(probe);

        // Check whether the inactive services should be skipped when searching
        AxisConfiguration axisConfig;
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
        ConfigurationContext mainCfgCtx = ConfigHolder.getInstance().getServerConfigurationContext();
        if (tenantDomain != MultitenantConstants.SUPER_TENANT_DOMAIN_NAME) {
            axisConfig = TenantAxisUtils.getTenantAxisConfiguration(tenantDomain, mainCfgCtx);
        } else {
            axisConfig = mainCfgCtx.getAxisConfiguration();
        }
        
        Parameter parameter = axisConfig.getParameter(DiscoveryConstants.SKIP_INACTIVE_SERVICES);
        filter.setSkipInactiveServices(parameter == null || "true".equals(parameter.getValue()));

        Service[] services = serviceManager.findServices(filter);
        if (services != null && services.length > 0) {
            TargetService[] targetServices = new TargetService[services.length];
            for (int i = 0; i < services.length; i++) {
                targetServices[i] = getTargetService(services[i]);
            }
            return targetServices;
        }
        return null;
    }

    private static TargetService getTargetService(Service service) throws Exception {

        EndpointReference epr;
        String eprAttr = service.getAttribute(DiscoveryConstants.ATTR_EPR);
        if (eprAttr != null) {
            epr  = EndpointReferenceHelper.fromString(eprAttr);
        } else {
            epr = new EndpointReference(service.getId());
        }

        TargetService targetService = new TargetService(epr);
        String types = service.getAttribute(DiscoveryConstants.ATTR_TYPES);
        if (types != null) {
            targetService.setTypes(Util.toQNameArray(types.split(",")));
        }

        String scopes = service.getAttribute(DiscoveryConstants.ATTR_SCOPES);
        if (scopes != null) {
            targetService.setScopes(Util.toURIArray(scopes.split(",")));
        }

        String[] endpoints = service.getAttributes(DiscoveryConstants.ATTR_ENDPOINTS);
        URI[] uris = new URI[endpoints.length];
        for(int i=0;i<endpoints.length;i++){
            uris[i] = URI.create(endpoints[i].substring(1));
        }
        if (endpoints != null) {
            targetService.setXAddresses(uris);
        }

        String mdv = service.getAttribute(DiscoveryConstants.ATTR_METADATA_VERSION);
        if (mdv != null) {
            targetService.setMetadataVersion(Integer.valueOf(mdv));
        } else {
            // Set a default metadata version
            targetService.setMetadataVersion(1);
        }

        return targetService;
    }
}
