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

package org.wso2.carbon.event.ws.internal.notify;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.ws.internal.util.EventingConstants;
import org.wso2.carbon.event.ws.internal.WSEventBrokerHolder;
import org.wso2.carbon.event.core.subscription.EventDispatcher;
import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.event.core.Message;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.ConfigurationContextService;

@Deprecated
public class WSEventDispatcher implements EventDispatcher {

     private static Log log = LogFactory.getLog(WSEventDispatcher.class);

    public void notify(Message message, Subscription subscription) {

        String endpoint = subscription.getEventSinkURL();

        String topic = subscription.getTopicName();
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace topicNs = factory.createOMNamespace(
                EventingConstants.NOTIFICATION_NS_URI,
                EventingConstants.NOTIFICATION_NS_PREFIX);
        OMElement topicEle = factory.createOMElement(EventingConstants.WSE_EN_TOPIC, topicNs);
        topicEle.setText(topic);
        
        OMElement domainElement = null;
        String tenantDomain = message.getProperty(MultitenantConstants.TENANT_DOMAIN_HEADER_NAME);
        if (tenantDomain != null) {
            OMNamespace domainNs = factory.createOMNamespace(MultitenantConstants.TENANT_DOMAIN_HEADER_NAMESPACE, null);
            domainElement = factory.createOMElement(MultitenantConstants.TENANT_DOMAIN_HEADER_NAME, domainNs);
            domainElement.setText(tenantDomain);
        }



        OMElement payload = message.getMessage().cloneOMElement();

        try {
            sendNotification(topicEle, domainElement, payload, endpoint);
        } catch (Exception e) {
            log.error("Unable to send message", e);
        }
    }

    protected synchronized void sendNotification(OMElement topicHeader,
                                    OMElement tenantDomainHeader,
                                    OMElement payload,
                                    String endpoint)
            throws AxisFault {
        // The parameter args is used as a mechanism to pass any argument into this method, which
        // is used by the implementations that extend the behavior of the default Carbon Event
        // Dispatcher.
        ConfigurationContextService configurationContextService =
                WSEventBrokerHolder.getInstance().getConfigurationContextService();

        ServiceClient serviceClient =
                new ServiceClient(configurationContextService.getClientConfigContext(), null);

        Options options = new Options();
        options.setTo(new EndpointReference(endpoint));
        options.setAction(EventingConstants.WSE_PUBLISH);
        serviceClient.setOptions(options);
        serviceClient.addHeader(topicHeader);

        if (tenantDomainHeader != null){
            serviceClient.addHeader(tenantDomainHeader);
        }

        serviceClient.fireAndForget(payload);
    }

    protected void sendNotification(OMElement topicHeader,
                                    OMElement payload,
                                    String endpoint)
            throws AxisFault {
        // The parameter args is used as a mechanism to pass any argument into this method, which
        // is used by the implementations that extend the behavior of the default Carbon Event
        // Dispatcher.
        ServiceClient serviceClient = new ServiceClient();

        Options options = new Options();
        options.setTo(new EndpointReference(endpoint));
        options.setAction(EventingConstants.WSE_PUBLISH);
        serviceClient.setOptions(options);
        serviceClient.addHeader(topicHeader);

        serviceClient.fireAndForget(payload);
    }
}
