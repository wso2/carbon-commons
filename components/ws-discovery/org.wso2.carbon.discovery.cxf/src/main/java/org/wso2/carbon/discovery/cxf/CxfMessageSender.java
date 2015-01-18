/*
* Copyright 2004,2013 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.discovery.cxf;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.rampart.RampartMessageData;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.discovery.DiscoveryConstants;
import org.wso2.carbon.discovery.DiscoveryException;
import org.wso2.carbon.discovery.DiscoveryOMUtils;
import org.wso2.carbon.discovery.config.Config;
import org.wso2.carbon.discovery.cxf.internal.CxfDiscoveryDataHolder;
import org.wso2.carbon.discovery.messages.Notification;
import org.wso2.carbon.discovery.messages.TargetService;
import org.wso2.carbon.discovery.util.DiscoveryMgtUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.namespace.QName;
import java.net.ConnectException;
import java.net.URI;
import java.util.Map;

public class CxfMessageSender {

    Log log = LogFactory.getLog(CxfMessageSender.class);

    public void sendHello(CXFServiceInfo serviceBean, Parameter discoveryParams) throws
            DiscoveryException {
        sendNotification(serviceBean, discoveryParams, DiscoveryConstants.NOTIFICATION_TYPE_HELLO);
    }

    public void sendBye(CXFServiceInfo serviceBean, Parameter discoveryParams) throws
            DiscoveryException {
        sendNotification(serviceBean, discoveryParams, DiscoveryConstants.NOTIFICATION_TYPE_BYE);
    }

    private void sendNotification(CXFServiceInfo serviceBean,
                                  Parameter discoveryParams, int notificationType) throws DiscoveryException {

        AxisConfiguration axisConfig = getAxisConfig();
        if (axisConfig == null) {
            //server is in tenant creation mode. Put the message to the queue.
            CxfDiscoveryDataHolder.getInstance().getInitialMessagesList().add(serviceBean);
            return;
        }

        Config config = getDiscoveryConfig(discoveryParams);
        Parameter discoveryProxyParam = DiscoveryMgtUtils.getDiscoveryParam(axisConfig);
        if (discoveryProxyParam == null) {
            return;
        }
        String discoveryEPR = (String) discoveryProxyParam.getValue();

        try {

            // create the service client object before getting the eprs
            // in order to get the EPRs
            ServiceClient serviceClient = initServiceClient(discoveryEPR, notificationType, axisConfig);

            // create the hello/bye message and send
            String uniqueID = getServerID(serviceBean, config);
            EndpointReference endpointReference = new EndpointReference(uniqueID);
            TargetService targetService = new TargetService(endpointReference);

            targetService.setTypes(new QName[] { serviceBean.getType() });

            URI[] scopes = new URI[config.getScopes().size()];
            for (int i = 0; i < config.getScopes().size(); i++) {
                scopes[i] = new URI(config.getScopes().get(i));
            }
            targetService.setScopes(scopes);

            Object[] eprs = serviceBean.getxAddrs().toArray();
            URI[] xAddres = new URI[eprs.length];
            for (int i = 0; i < eprs.length; i++) {
                String epr = (String) eprs[i];
                if (epr.endsWith("/")) {
                    epr = epr.substring(0, epr.length() - 1);
                }
                xAddres[i] = new URI(epr);
            }
            targetService.setXAddresses(xAddres);
            targetService.setMetadataVersion(config.getMetadataVersion());

            if(notificationType == DiscoveryConstants.NOTIFICATION_TYPE_HELLO){
                serviceClient.addStringHeader(
                        new QName(DiscoveryConstants.DISCOVERY_HEADER_ELEMENT_NAMESPACE,
                                DiscoveryConstants.DISCOVERY_HEADER_SERVICE_NAME,
                                DiscoveryConstants.DISCOVERY_HEADER_ELEMENT_NAMESPACE_PREFIX),
                        serviceBean.getServiceName());
                serviceClient.addStringHeader(new QName(
                        DiscoveryConstants.DISCOVERY_HEADER_ELEMENT_NAMESPACE,
                        DiscoveryConstants.DISCOVERY_HEADER_WSDL_URI,
                        DiscoveryConstants.DISCOVERY_HEADER_ELEMENT_NAMESPACE_PREFIX),
                        serviceBean.getWsdlURI());
            }

            Notification notification = new Notification(notificationType, targetService);
            serviceClient.fireAndForget(DiscoveryOMUtils.toOM(notification,
                    OMAbstractFactory.getOMFactory()));
            serviceClient.cleanup();

        } catch (Exception e) {
            if (e.getCause() instanceof ConnectException) {
                log.error("Error while connecting to Discovery Service. " +
                        "Connection Refused - " + discoveryEPR);
            }

            throw new DiscoveryException("Error while sending the WS-Discovery notification " +
                    "for the service " + serviceBean.getServiceName(), e);
        }
    }

    private Config getDiscoveryConfig(Parameter discoveryParams) {
        Config config;
        if (discoveryParams != null) {
            OMElement element = discoveryParams.getParameterElement();

            if (element == null) {
                if (discoveryParams.getValue() != null) {
                    try {
                        String wrappedElementText = "<wrapper>" + discoveryParams.getValue() + "</wrapper>";
                        element = AXIOMUtil.stringToOM(wrappedElementText);
                    } catch (Exception ignored) { }
                } else {
                    return getDefaultConfig();
                }
            }

            config = Config.fromOM(element);
        } else {
            return getDefaultConfig();
        }
        return config;
    }

    private Config getDefaultConfig() {
        Config config = new Config();
        config.setMetadataVersion(DiscoveryConstants.DISCOVERY_DEFAULT_METADATA_VERSION);
        config.addScope(DiscoveryConstants.DISCOVERY_DEFAULT_SCOPE);
        return config;
    }

    private String getServerID(CXFServiceInfo serviceBean, Config config) throws Exception {

        String uniqueID = config.getUniqueId();
        if (uniqueID == null) {
            // Get the unique id from the registry.
            // Use the service to get hold of the Carbon context of the tenant
            // to which the service belongs
            Registry registry = (Registry) PrivilegedCarbonContext.getThreadLocalCarbonContext().
                    getRegistry(RegistryType.SYSTEM_CONFIGURATION);
            uniqueID = DiscoveryMgtUtils.getExistingServiceIdOrUpdate(getNameForService(serviceBean.getServiceName()),
                    UIDGenerator.generateURNString(), registry);
        }
        return uniqueID;
    }

    private String getNameForService(String serviceName) {
        return serviceName.replace("/", "");
    }

    private ServiceClient initServiceClient(String epr, int notificationType,
                                            AxisConfiguration axisConf) throws Exception {

        ConfigurationContext cfgCtx = CxfDiscoveryDataHolder.getInstance().getClientConfigurationContext();
        ServiceClient serviceClient = new ServiceClient(cfgCtx, null);
        serviceClient.setTargetEPR(new EndpointReference(epr));
        if (notificationType == DiscoveryConstants.NOTIFICATION_TYPE_HELLO) {
            serviceClient.getOptions().setAction(DiscoveryConstants.WS_DISCOVERY_HELLO_ACTION);
        } else {
            serviceClient.getOptions().setAction(DiscoveryConstants.WS_DISCOVERY_BYE_ACTION);
        }

        serviceClient.engageModule("addressing");

        Registry registry = (Registry)PrivilegedCarbonContext.getThreadLocalCarbonContext().getRegistry(
                RegistryType.SYSTEM_CONFIGURATION);
        Policy policy = DiscoveryMgtUtils.getClientSecurityPolicy(registry);
        if (policy != null) {
            serviceClient.engageModule("rampart");
            serviceClient.getOptions().setProperty(
                    RampartMessageData.KEY_RAMPART_POLICY, policy);
        }

        return serviceClient;
    }

    private AxisConfiguration getAxisConfig() {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        ConfigurationContext mainConfigContext = getMainConfigurationContext();
        if (mainConfigContext == null) {
            return null;
        }

        AxisConfiguration axisConfig;
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            axisConfig = mainConfigContext.getAxisConfiguration();
        } else {
            //Accessing tenant axisConfiguration directly will cause the tenant to load even if another
            // tenant creation is already in progress. That would cause massive issues.
            Map<String, ConfigurationContext> tenantConfigContexts =
                    TenantAxisUtils.getTenantConfigurationContexts(getMainConfigurationContext());
            ConfigurationContext tenantConfigCtx = tenantConfigContexts.get(tenantDomain);
            if (tenantConfigCtx == null) {
                return null;
            }
            axisConfig = tenantConfigCtx.getAxisConfiguration();
        }

        return axisConfig;
    }

    private ConfigurationContext getMainConfigurationContext() {
        ConfigurationContext cc = CxfDiscoveryDataHolder.getInstance().getMainServerConfigContext();
        if (cc != null) {
            return cc;
        }

        return null;
//        ConfigurationContextService configurationContextService = (ConfigurationContextService) PrivilegedCarbonContext.
//                getThreadLocalCarbonContext().getOSGiService(ConfigurationContextService.class);
//        return configurationContextService.getServerConfigContext();
    }

}
