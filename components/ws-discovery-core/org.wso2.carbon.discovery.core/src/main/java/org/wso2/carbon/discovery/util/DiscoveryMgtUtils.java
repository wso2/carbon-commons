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

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.JavaUtils;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.rampart.RampartMessageData;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.core.util.ParameterUtil;
import org.wso2.carbon.discovery.DiscoveryConstants;
import org.wso2.carbon.discovery.DiscoveryException;
import org.wso2.carbon.discovery.client.DiscoveryClient;
import org.wso2.carbon.discovery.messages.TargetService;
import org.wso2.carbon.registry.api.*;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.net.*;

/**
 * Utility class which handles WS-Discovery management activities such as managing
 * WS-D proxy configurations and contacting remote WS-Discovery proxies to obtain
 * service information. All the configurations managed by this class are stored in
 * the configuration registry of Carbon.
 */
public class DiscoveryMgtUtils {

    private static final String DISCOVERY_CONFIG_ROOT =
            "repository/components/org.wso2.carbon.discovery.core/";

    private static final String DISCOVERY_PROXY_ROOT = DISCOVERY_CONFIG_ROOT + "proxies/";
    private static final String DISCOVERY_SERVICE_ID_LIST = "ServiceIDList";
    private static final String DISCOVERY_CLIENT_POLICY = "DISCOVERY_CLIENT_POLICY";

    private static final String DISCOVERY_PUBLISHER_CONFIG = "DiscoveryPublisherConfig";
    private static final String DISCOVERY_PUBLISHER_STATUS = "publisher.status";

    private static final String DISCOVERY_PROXY_NAME = "ProxyName";

    /**
     * Add a new WS-Discovery proxy configuration. A proxy configuration must contain
     * a name and a valid URL via which the respective WS-D proxy can be contacted. This
     * method does not allow overwriting existing proxy configuration. In such cases an
     * exception will be thrown.
     *
     * @param pd WS-Discovery proxy description
     * @param registry Configuration registry where information should be saved
     * @throws DiscoveryException if the proxy description is invalid
     * @throws RegistryException if an error occurs while accessing the registry
     */
    public static void addDiscoveryProxy(DiscoveryProxyDetails pd, Registry registry)
            throws DiscoveryException, RegistryException {

        if (pd.getName() == null || "".equals(pd.getName())) {
            throw new DiscoveryException("No name specified for the discovery proxy");
        }

        String path = DISCOVERY_PROXY_ROOT + pd.getName();
        if (registry.resourceExists(path)) {
            throw new DiscoveryException("The discovery proxy named " + pd.getName() +
                    " already exists");
        }

        if (pd.getUrl() == null || "".equals(pd.getUrl())) {
            throw new DiscoveryException("No URL specified for the discovery proxy");
        }

        if (pd.getPolicy() != null && !registry.resourceExists(pd.getPolicy())) {
            throw new DiscoveryException("The policy resource: " + pd.getPolicy() +
                    " does not exist");
        }

        URL url;
        try {
            url = new URL(pd.getUrl());
        } catch (MalformedURLException e) {
            throw new DiscoveryException("Invalid URL specified for the discovery proxy", e);
        }

        Resource proxy = registry.newResource();
        proxy.setContent(url.toString());
        proxy.setProperty(DISCOVERY_PROXY_NAME, pd.getName());
        registry.put(path, proxy);

        if (pd.getPolicy() != null) {
            registry.addAssociation(path, pd.getPolicy(), DISCOVERY_CLIENT_POLICY);
        }
    }

    /**
     * Get the previously assigned unique ID of the given service. If such an ID is
     * not already assigned to the given service then the provided unique ID will be
     * assigned to the service and the association will be recorded in the registry.
     *
     * @param name Name of the service
     * @param uniqueId Unique ID to be assigned in case an ID is not specified already
     * @param registry Configuration registry where service ID list is stored
     * @return the Unique ID assigned to the service
     * @throws DiscoveryException if the registry is not available
     * @throws RegistryException if an error occurs while accessing the registry
     */
    public static String getExistingServiceIdOrUpdate(String name, String uniqueId, Registry registry)
            throws DiscoveryException, RegistryException {

        Resource list;
        String path = DISCOVERY_CONFIG_ROOT + DISCOVERY_SERVICE_ID_LIST;

        if (registry.resourceExists(path)) {
            list = registry.get(path);
            String existingId = list.getProperty(name);
            if (existingId == null) {
                existingId = uniqueId;
                list.setProperty(name, uniqueId);
                registry.put(path, list);
            }
            list.discard();
            return existingId;
        } else {
            list = registry.newResource();
            list.setProperty(name, uniqueId);
            registry.put(path, list);
            list.discard();
            return uniqueId;
        }
    }

    /**
     * Remove the named WS-D proxy configuration from the registry
     *
     * @param name Name of the proxy configuration to be removed
     * @param registry Configuration registry where proxy information is saved
     * @throws DiscoveryException if the named configuration does not exist
     * @throws RegistryException if an error occurs while accessing the registry
     */
    public static void removeDiscoveryProxy(String name, Registry registry)
            throws DiscoveryException, RegistryException {

        String path = DISCOVERY_PROXY_ROOT + name;
        if (registry.resourceExists(path)) {
            Association[] associations = registry.getAssociations(path, DISCOVERY_CLIENT_POLICY);
            if (associations != null) {
                // We should also remove any associations.
                // Otherwise if a new resource is added with the same name the old associations
                // are restored.
                for (Association a : associations) {
                    registry.removeAssociation(path, a.getDestinationPath(),
                            DISCOVERY_CLIENT_POLICY);
                }
            }
            registry.delete(path);
        } else {
            throw new DiscoveryException("The discovery proxy named " + name + " does not exist");
        }
    }

    /**
     * Get the named WS-D proxy configuration
     *
     * @param name Name of the proxy configuration
     * @param registry Configuration registry where proxy information is saved
     * @return a proxy description or null if no such configuration exists
     * @throws DiscoveryException If the registry is unavailable
     * @throws RegistryException If an error occurs while accessing the registry
     */
    public static DiscoveryProxyDetails getDiscoveryProxy(String name, Registry registry)
            throws DiscoveryException, RegistryException {

        String path = DISCOVERY_PROXY_ROOT + name;
        if (!registry.resourceExists(path)) {
            return null;
        }

        Resource proxy = registry.get(path);
        DiscoveryProxyDetails pd = new DiscoveryProxyDetails();
        pd.setName(name);
        pd.setUrl(new String((byte[]) proxy.getContent()));
        pd.setOnline(isOnline(pd.getUrl()));

        Association[] policies = registry.getAssociations(path, DISCOVERY_CLIENT_POLICY);
        if (policies != null && policies.length == 1) {
            pd.setPolicy(policies[0].getDestinationPath());
        }
        
        proxy.discard();
        return pd;
    }

    /**
     * Returns all existing WS-D proxy configurations
     *
     * @param registry Configuration registry where proxy information is saved
     * @return an array of proxy configuration or null if no proxies are configured
     * @throws DiscoveryException if the registry is unavailable
     * @throws RegistryException if an error occurs while accessing the registry
     */
    public static DiscoveryProxyDetails[] getDiscoveryProxies(Registry registry) throws DiscoveryException,
            RegistryException {

        if (!registry.resourceExists(DISCOVERY_PROXY_ROOT)) {
            return null;
        }

        Collection proxies = (Collection) registry.get(DISCOVERY_PROXY_ROOT);
        String[] children = proxies.getChildren();
        proxies.discard();

        if (children != null && children.length > 0) {
            DiscoveryProxyDetails[] details = new DiscoveryProxyDetails[children.length];

            for (int i = 0; i < children.length; i++) {
                Resource proxy = registry.get(children[i]);
                DiscoveryProxyDetails pd = new DiscoveryProxyDetails();
                pd.setName(proxy.getProperty(DISCOVERY_PROXY_NAME));
                pd.setUrl(new String((byte[]) proxy.getContent()));
                pd.setOnline(isOnline(pd.getUrl()));
                Association[] policies = registry.getAssociations(children[i],
                        DISCOVERY_CLIENT_POLICY);
                if (policies != null && policies.length == 1) {
                    pd.setPolicy(policies[0].getDestinationPath());
                }
                details[i] = pd;
                proxy.discard();
            }
            return details;
        }
        return null;
    }

    /**
     * Check whether the named WS-D proxy configuration exists in the registry
     *
     * @param name The name of the proxy to be tested
     * @param registry Configuration registry where proxy information is saved
     * @return true if the configuration exists and false otherwise
     * @throws DiscoveryException if the registry is unavailable
     * @throws RegistryException if an error occurs while accessing the registry
     */
    public static boolean discoveryProxyExists(String name, Registry registry) throws DiscoveryException,
            RegistryException {

        return registry.resourceExists(DISCOVERY_PROXY_ROOT + name);
    }

    /**
     * Probe the specified WS-D proxy to obtain target service details
     *
     * @param name Name of the proxy to be queried
     * @param pd Probe description
     * @param registry Configuration registry where proxy information is saved
     * @return an array of target service descriptions or null
     * @throws DiscoveryException if the proxy configuration is invalid or the proxy is unavailable
     * @throws RegistryException if an error occurs while accessing the registry
     */
    public static TargetService[] probeDiscoveryProxy(String name, ProbeDetails pd, Registry registry)
            throws DiscoveryException, RegistryException {

        QName[] types = null;
        URI[] scopes = null;

        DiscoveryProxyDetails proxy = getDiscoveryProxy(name, registry);
        if (proxy == null) {
            throw new DiscoveryException("No discovery proxy exists by the name " + name);
        }

        if (pd.getTypes() != null) {
            types = Util.toQNameArray(pd.getTypes());
        }

        if (pd.getScopes() != null) {
            scopes = Util.toURIArray(pd.getScopes());
        }

        DiscoveryClient client = new DiscoveryClient(
                ConfigHolder.getInstance().getClientConfigurationContext(), proxy.getUrl());
        if (proxy.getPolicy() != null) {
            client.engageModule("rampart");
            client.setProperty(RampartMessageData.KEY_RAMPART_POLICY,
                    getPolicy(proxy.getPolicy(), registry));
        }
        TargetService[] services = client.probe(types, scopes, pd.getRule());
        client.cleanup();
        return services;
    }

    /**
     * Resolve the specified service ID against the given proxy
     *
     * @param name Name of the proxy to be queried
     * @param id Service ID to be resolved
     * @param registry Configuration registry where proxy information is saved
     * @return a target service description or null
     * @throws DiscoveryException if the proxy configuration is invalid or the proxy is unavailable
     * @throws RegistryException if an error occurs while accessing the registry
     */
    public static TargetService resolveService(String name, String id, Registry registry)
            throws DiscoveryException, RegistryException {

        DiscoveryProxyDetails proxy = getDiscoveryProxy(name, registry);
        if (proxy == null) {
            throw new DiscoveryException("No discovery proxy exists by the name " + name);
        }

        DiscoveryClient client = new DiscoveryClient(
                ConfigHolder.getInstance().getClientConfigurationContext(), proxy.getUrl());
        if (proxy.getPolicy() != null) {
            client.engageModule("rampart");
            client.setProperty(RampartMessageData.KEY_RAMPART_POLICY,
                    getPolicy(proxy.getPolicy(), registry));
        }
        TargetService service = client.resolve(id);
        client.cleanup();
        return service;
    }

    /**
     * Check whether service discovery is enabled in the configuration. This method first checks
     * whether the DiscoveryConstants.DISCOVERY_PROXY parameter is set in the given AxisConfiguration.
     * If not it checks whether service discovery status is set to 'true' in the configuration
     * registry. If discovery is enabled in the registry configuration, this method will also
     * add the corresponding parameter to AxisConfiguration.
     *
     * @param axisConfig AxisConfiguration
     * @return service discovery status
     * @throws RegistryException if an error occurs while accessing the registry
     */
    public static boolean isServiceDiscoveryEnabled(AxisConfiguration axisConfig) throws RegistryException {
        Parameter parameter = getDiscoveryParam(axisConfig);
        if (parameter != null) {
            return true;
        }

        String path = DISCOVERY_CONFIG_ROOT + DISCOVERY_PUBLISHER_CONFIG;

        Registry registry = PrivilegedCarbonContext.getThreadLocalCarbonContext().
                getRegistry(RegistryType.SYSTEM_CONFIGURATION);
        if (registry.resourceExists(path)) {
            Resource publisherConfig = registry.get(path);
            String status = publisherConfig.getProperty(DISCOVERY_PUBLISHER_STATUS);
            publisherConfig.discard();
            boolean enabled = JavaUtils.isTrueExplicitly(status);

            if (enabled) {
                String discoveryProxyURL = getDiscoveryProxyURL(registry);
                try {
                    Parameter discoveryProxyParam =
                            ParameterUtil.createParameter(DiscoveryConstants.DISCOVERY_PROXY,
                                                          discoveryProxyURL);
                    axisConfig.addParameter(discoveryProxyParam);
                } catch (AxisFault axisFault) {
                    axisFault.printStackTrace();  //TODO
                }
            }
            return enabled;
        }

        return false;
    }

    /**
     * Get the URL of the remote WS-Discovery proxy. This method first attempts to get the URL
     * from the AxisConfiguration. If discovery is not enabled, it will attempt to retrieve the
     * URL from the configuration registry.
     *
     * @param axisConfig AxisConfiguration instance
     * @return URL of the proxy or null
     * @throws RegistryException on error
     */
    public static String getDiscoveryProxyURL(AxisConfiguration axisConfig) throws RegistryException {
        Parameter parameter = getDiscoveryParam(axisConfig);
        if (parameter != null) {
            return parameter.getValue().toString();
        }

        Registry registry = PrivilegedCarbonContext.getThreadLocalCarbonContext().
                getRegistry(RegistryType.SYSTEM_CONFIGURATION);
        return getDiscoveryProxyURL(registry);
    }

    public static Parameter getDiscoveryParam(AxisConfiguration axisConfig) {
        return axisConfig.getParameter(DiscoveryConstants.DISCOVERY_PROXY);
    }

    private static String getDiscoveryProxyURL(Registry registry) throws RegistryException {
        String path = DISCOVERY_CONFIG_ROOT + DISCOVERY_PUBLISHER_CONFIG;

        if (registry.resourceExists(path)) {
            Resource publisherConfig = registry.get(path);
            String url = new String((byte[]) publisherConfig.getContent());
            publisherConfig.discard();
            return url;
        } else {
            return null;
        }
    }

    /**
     * Save the WS-Discovery service publisher configuration to the registry. This
     * configuration contains the remote discovery proxy URL and the current status
     * of the publisher (ie enabled/disabled)
     *
     * @param proxyURL URL of the discovery proxy
     * @param enabled true if the publisher is enabled and false otherwise
     * @param registry Configuration registry
     * @throws RegistryException on error
     */
    public static void persistPublisherConfiguration(String proxyURL, boolean enabled,
                                                     Registry registry) throws RegistryException {

        Resource publisherResource;
        String path = DISCOVERY_CONFIG_ROOT + DISCOVERY_PUBLISHER_CONFIG;

        if (registry.resourceExists(path)) {
            publisherResource = registry.get(path);
        } else {
            publisherResource = registry.newResource();
        }

        publisherResource.setContent(proxyURL);
        publisherResource.setProperty(DISCOVERY_PUBLISHER_STATUS, String.valueOf(enabled));
        registry.put(path, publisherResource);
        publisherResource.discard();
    }

    /**
     * Get the client side security policy for the remote discovery proxy. This method first checks
     * whether a resource named DiscoveryConstants.DISCOVERY_CLIENT_POLICY is available at the
     * root of the configuration registry. If not it attempts to load the policy from a file named
     * DiscoveryConstants.DISCOVERY_CLIENT_POLICY which should reside in the configuration file
     * directory of Carbon.
     *
     * @param registry Configuration registry
     * @return a Policy instance or null
     * @throws RegistryException If the registry cannot be accessed
     * @throws XMLStreamException If the policy XML cannot be parsed
     */
    public static Policy getClientSecurityPolicy(Registry registry) throws RegistryException, 
            XMLStreamException {
        //TODO: Improve this logic so that user can put the security policy anywhere in the registry

        InputStream in;
        Resource policyResource = null;
        if (registry.resourceExists(DiscoveryConstants.DISCOVERY_CLIENT_POLICY)) {
            policyResource = registry.get(DiscoveryConstants.DISCOVERY_CLIENT_POLICY);
            in = policyResource.getContentStream();
        } else {
            String file = CarbonUtils.getCarbonConfigDirPath() + File.separator +
                DiscoveryConstants.DISCOVERY_CLIENT_POLICY;
            try {
                in = new FileInputStream(file);
            } catch (FileNotFoundException ignored) {
                return null;
            }
        }

        Policy policy = null;
        if (in != null) {
            StAXOMBuilder builder = new StAXOMBuilder(in);
            policy = PolicyEngine.getPolicy(builder.getDocumentElement());
        }

        if (policyResource != null) {
            policyResource.discard();
        }
        return policy;
    }

    /**
     * Construct a target service description using the given TargetService instance
     *
     * @param service  the TargetService object - must not be null
     * @return a target service description
     */
    public static TargetServiceDetails getServiceDetails(TargetService service) {
        TargetServiceDetails details = new TargetServiceDetails();
        details.setServiceId(service.getEpr().getAddress());
        if (service.getTypes() != null) {
            details.setTypes(Util.toStringArray(service.getTypes()));
        }
        if (service.getScopes() != null) {
            details.setScopes(Util.toStringArray(service.getScopes()));
        }
        if (service.getXAddresses() != null) {
            details.setAddresses(Util.toStringArray(service.getXAddresses()));
        }
        if (service.getMetadataVersion() != -1) {
            details.setVersion(service.getMetadataVersion());
        }
        return details;
    }

    private static boolean isOnline(String urlString) {
        try {
            URL url = new URL(urlString);
            URL hostUrl = new URL(url.getProtocol(), url.getHost(), url.getPort(), "");
            hostUrl.getContent();
            return true;
        } catch (MalformedURLException e) {
            return false;
        } catch (UnknownHostException e) {
            return false;
        } catch (ConnectException e) {
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    private static Policy getPolicy(String key, Registry registry)
            throws DiscoveryException, RegistryException {

        if (!registry.resourceExists(key)) {
            throw new DiscoveryException("Policy resource " + key + " does not exist");
        }

        Resource policy = registry.get(key);
        ByteArrayInputStream in = new ByteArrayInputStream((byte[]) policy.getContent());
        try {
            StAXOMBuilder builder = new StAXOMBuilder(in);
            Policy secPolicy = PolicyEngine.getPolicy(builder.getDocumentElement());
            policy.discard();
            return secPolicy;
        } catch (XMLStreamException e) {
            policy.discard();
            throw new DiscoveryException("Error while loading the policy from resource " + key, e);
        }
    }

}
