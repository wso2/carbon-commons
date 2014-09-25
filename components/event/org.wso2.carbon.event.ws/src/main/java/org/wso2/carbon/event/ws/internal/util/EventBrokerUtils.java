/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.event.ws.internal.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.llom.soap12.SOAP12Factory;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.jaxen.JaxenException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.ws.internal.exception.EventBrokerException;
import org.wso2.carbon.event.ws.internal.exception.WSEventException;

import java.util.regex.Matcher;

public class EventBrokerUtils {


    public static MessageContext createMessageContext(OMElement payload,
                                                      OMElement topic,
                                                      int tenantId) throws EventBrokerException {
        MessageContext mc = new MessageContext();
        mc.setConfigurationContext(new ConfigurationContext(new AxisConfiguration()));
        PrivilegedCarbonContext.getCurrentContext(mc).setTenantId(tenantId);
        SOAPFactory soapFactory = new SOAP12Factory();
        SOAPEnvelope envelope = soapFactory.getDefaultEnvelope();
        envelope.getBody().addChild(payload);
        if (topic != null) {
            envelope.getHeader().addChild(topic);
        }
        try {
            mc.setEnvelope(envelope);
        } catch (Exception e) {

            throw new EventBrokerException("Unable to generate event.", e);
        }
        return mc;
    }


    public static String getSecureTopicRegistryPath(String topicName){
        if(!topicName.startsWith("/")){
            topicName = "/" + topicName;
        }
        topicName = topicName.replaceAll("/", "_").replaceAll("_+", "_");
        return new StringBuffer().append("eventing/SecureTopic").append("/").append(topicName).toString();
    }

    public static String getLoggedInUserName() {
        String userName = "";
        if (CarbonContext.getThreadLocalCarbonContext().getTenantId() != 0) {
            userName = CarbonContext.getThreadLocalCarbonContext().getUsername() + "@"
                    + CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        } else {
            userName = CarbonContext.getThreadLocalCarbonContext().getUsername();
        }
        return userName.trim();
    }

    public static boolean isSystemDefinedUser(String user){
        return user.equals("admin");
    }

    public static boolean isSystemAllowedRole(String role){
        return role.equals("admin");
    }

    public static String extractTopicFromMessage(MessageContext mc) throws WSEventException {
        String topic = null;
        if (mc.getTo() != null && mc.getTo().getAddress() != null) {
            String toaddress = mc.getTo().getAddress();
            if (toaddress.contains("/publish/")) {
                Matcher matcher = EventingConstants.TO_ADDRESS_PATTERN.matcher(toaddress);
                if (matcher.matches()) {
                    topic = matcher.group(1);
                }
            }
        }

        if ((topic == null) || (topic.trim().length() == 0)) {
            try {
                AXIOMXPath topicXPath = new AXIOMXPath(
                        "s11:Header/ns:" + EventingConstants.TOPIC_HEADER_NAME
                                + " | s12:Header/ns:" + EventingConstants.TOPIC_HEADER_NAME);
                topicXPath.addNamespace("s11", "http://schemas.xmlsoap.org/soap/envelope/");
                topicXPath.addNamespace("s12", "http://www.w3.org/2003/05/soap-envelope");
                topicXPath.addNamespace("ns", EventingConstants.TOPIC_HEADER_NS);

                OMElement topicNode = (OMElement) topicXPath.selectSingleNode(mc.getEnvelope());
                if (topicNode != null) {
                    topic = topicNode.getText();
                }
            } catch (JaxenException e) {
                throw new WSEventException("can not process the xpath ", e);
            }
        }
        return topic;
    }
}
