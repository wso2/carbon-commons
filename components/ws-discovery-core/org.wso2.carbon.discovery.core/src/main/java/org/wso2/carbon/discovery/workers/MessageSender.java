/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.discovery.workers;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.JavaUtils;
import org.apache.neethi.Policy;
import org.apache.rampart.RampartMessageData;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.discovery.DiscoveryConstants;
import org.wso2.carbon.discovery.DiscoveryException;
import org.wso2.carbon.discovery.DiscoveryOMUtils;
import org.wso2.carbon.discovery.config.Config;
import org.wso2.carbon.discovery.messages.Notification;
import org.wso2.carbon.discovery.messages.TargetService;
import org.wso2.carbon.discovery.util.ConfigHolder;
import org.wso2.carbon.discovery.util.DiscoveryMgtUtils;
import org.wso2.carbon.discovery.util.Util;
import org.wso2.carbon.registry.core.Registry;

import javax.xml.namespace.QName;
import java.net.URI;

public class MessageSender {

    public void sendHello(AxisService service, String discoveryProxyEPR) throws DiscoveryException {
        sendNotification(service, discoveryProxyEPR, DiscoveryConstants.NOTIFICATION_TYPE_HELLO);
    }

    public void sendBye(AxisService service, String discoveryProxyEPR) throws DiscoveryException {
        sendNotification(service, discoveryProxyEPR, DiscoveryConstants.NOTIFICATION_TYPE_BYE);
    }

    private void sendNotification(AxisService service,
                                  String discoveryProxyEPR, int notificationType) throws DiscoveryException {

        if (!isDiscoverable(service)) {
            return;
        }

        Config config = getDiscoveryConfig(service);

        try {
            // create the service client object before getting the eprs
            // in order to get the EPRs
            ServiceClient serviceClient = initServiceClient(discoveryProxyEPR, notificationType,
                    service.getAxisConfiguration());

            // create the hello/bye message and send
            String uniqueID = getServiceID(config, service);
            EndpointReference endpointReference = new EndpointReference(uniqueID);
            TargetService targetService = new TargetService(endpointReference);

            targetService.setTypes(new QName[] { Util.getTypes(service) });

            URI[] scopes = new URI[config.getScopes().size()];
            for (int i = 0; i < config.getScopes().size(); i++) {
                scopes[i] = new URI(config.getScopes().get(i));
            }
            targetService.setScopes(scopes);

            String[] eprs = service.getEPRs();
            URI[] xAddres = new URI[eprs.length];
            for (int i = 0; i < eprs.length; i++) {
                String epr = eprs[i];
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
                        getNameForService(service));
                serviceClient.addStringHeader(new QName(
                        DiscoveryConstants.DISCOVERY_HEADER_ELEMENT_NAMESPACE,
                        DiscoveryConstants.DISCOVERY_HEADER_WSDL_URI,
                        DiscoveryConstants.DISCOVERY_HEADER_ELEMENT_NAMESPACE_PREFIX),
                        Util.getWsdlInformation(service.getName(), service.getAxisConfiguration()));
            }

            Notification notification = new Notification(notificationType, targetService);
            serviceClient.fireAndForget(DiscoveryOMUtils.toOM(notification,
                    OMAbstractFactory.getOMFactory()));
            serviceClient.cleanup();

        } catch (Exception e) {
            throw new DiscoveryException("Error while sending the WS-Discovery notification " +
                    "for the service " + getNameForService(service), e);
        }
    }

    private boolean isDiscoverable(AxisService service) {
        boolean isAdminService = JavaUtils.isTrueExplicitly(
                service.getParameterValue("adminService"));
        boolean isHiddenService = JavaUtils.isTrueExplicitly(
                service.getParameterValue("hiddenService"));
        boolean isUndiscoverableService = JavaUtils.isTrueExplicitly(
                service.getParameterValue(DiscoveryConstants.UNDISCOVERABLE_SERVICE));

        // do not sent the notifications for either hidden or admin services
        if (isAdminService || isHiddenService ||
                isUndiscoverableService || service.isClientSide()){
            return false;
        }
        return true;
    }

    private Config getDiscoveryConfig(AxisService service) {
        Parameter parameter = service.getParameter(DiscoveryConstants.WS_DISCOVERY_PARAMS);
        Config config;
        if (parameter != null) {
            OMElement element = parameter.getParameterElement();

            // For a ProxyService, the parameter defined as XML, is returned as a string value.
            // Until this problem is solved, we'll be adopting the approach below, to make an
            // attempt to construct the parameter element.
            if (element == null) {
                if (parameter.getValue() != null) {
                    try {
                        String wrappedElementText = "<wrapper>" + parameter.getValue() + "</wrapper>";
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

    private String getServiceID(Config config, AxisService service) throws Exception {

        String uniqueID = config.getUniqueId();
        if (uniqueID == null) {
            // Get the unique id from the registry.
            // Use the service to get hold of the Carbon context of the tenant
            // to which the service belongs
            Registry registry = (Registry) PrivilegedCarbonContext.getThreadLocalCarbonContext().
                    getRegistry(RegistryType.SYSTEM_CONFIGURATION);
            uniqueID = DiscoveryMgtUtils.getExistingServiceIdOrUpdate(getNameForService(service),
                    UIDGenerator.generateURNString(), registry);
        }
        return uniqueID;
    }

    private String getNameForService(AxisService service) {
        return service.getName().replace("/", "-");
    }

    private ServiceClient initServiceClient(String epr, int notificationType,
                                            AxisConfiguration axisConf) throws Exception {

        ConfigurationContext cfgCtx = ConfigHolder.getInstance().getClientConfigurationContext();
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
}
