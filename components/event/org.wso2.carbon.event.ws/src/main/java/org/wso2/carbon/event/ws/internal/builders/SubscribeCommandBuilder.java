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
package org.wso2.carbon.event.ws.internal.builders;

import java.util.Calendar;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.axis2.databinding.types.Duration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.ws.internal.builders.exceptions.BuilderException;
import org.wso2.carbon.event.ws.internal.builders.exceptions.InvalidExpirationTimeException;
import org.wso2.carbon.event.ws.internal.builders.exceptions.InvalidMessageException;
import org.wso2.carbon.event.ws.internal.builders.utils.BuilderUtils;
import org.wso2.carbon.event.ws.internal.util.EventingConstants;
import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.event.core.subscription.EventFilter;

@Deprecated
public class SubscribeCommandBuilder extends CommandBuilderConstants {

    private static final Log log = LogFactory.getLog(SubscribeCommandBuilder.class);
    private SOAPFactory factory;

    public SubscribeCommandBuilder(MessageContext messageCtx) {
        factory = (SOAPFactory) messageCtx.getEnvelope().getOMFactory();
    }

    /**
     * (01) <s12:Envelope
     * (02)     xmlns:s12="http://www.w3.org/2003/05/soap-envelope"
     * (03)     xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing"
     * (04)     xmlns:wse="http://schemas.xmlsoap.org/ws/2004/08/eventing"
     * (05)     xmlns:ew="http://www.example.com/warnings" >
     * (06)   <s12:Header>
     * (07)     <wsa:Action>
     * (08)       http://schemas.xmlsoap.org/ws/2004/08/eventing/Subscribe
     * (09)     </wsa:Action>
     * (10)     <wsa:MessageID>
     * (11)       uuid:e1886c5c-5e86-48d1-8c77-fc1c28d47180
     * (12)     </wsa:MessageID>
     * (13)     <wsa:ReplyTo>
     * (14)      <wsa:Address>http://www.example.com/MyEvEntsink</wsa:Address>
     * (15)      <wsa:ReferenceProperties>
     * (16)        <ew:MySubscription>2597</ew:MySubscription>
     * (17)      </wsa:ReferenceProperties>
     * (18)     </wsa:ReplyTo>
     * (19)     <wsa:To>http://www.example.org/oceanwatch/EventSource</wsa:To>
     * (20)   </s12:Header>
     * (21)   <s12:Body>
     * (22)     <wse:Subscribe>
     * (23)       <wse:EndTo>
     * (24)         <wsa:Address>
     * (25)           http://www.example.com/MyEventSink
     * (26)         </wsa:Address>
     * (27)         <wsa:ReferenceProperties>
     * (28)           <ew:MySubscription>2597</ew:MySubscription>
     * (29)         </wsa:ReferenceProperties>
     * (30)       </wse:EndTo>
     * (31)       <wse:Delivery>
     * (32)         <wse:NotifyTo>
     * (33)           <wsa:Address>
     * (34)             http://www.other.example.com/OnStormWarning
     * (35)           </wsa:Address>
     * (36)           <wsa:ReferenceProperties>
     * (37)             <ew:MySubscription>2597</ew:MySubscription>
     * (38)           </wsa:ReferenceProperties>
     * (39)         </wse:NotifyTo>
     * (40)       </wse:Delivery>
     * (41)       <wse:Expires>2004-06-26T21:07:00.000-08:00</wse:Expires>
     * (42)       <wse:Filter xmlns:ow="http://www.example.org/oceanwatch"
     * (43)           Dialect="http://www.example.org/topicFilter" >
     * (44)         weather.storms
     * (45)       </wse:Filter>
     * (46)     </wse:Subscribe>
     * (47)   </s12:Body>
     * (48) </s12:Envelope>
     *
     * @param envelope The soap envelope containing the subscription request
     * @return The created subscription
     * @throws InvalidMessageException
     * @throws InvalidExpirationTimeException
     */
    public Subscription toSubscription(SOAPEnvelope envelope)
            throws InvalidMessageException, InvalidExpirationTimeException {
        Subscription subscription = null;
        OMElement notifyToElem;
        if (envelope == null) {
            log.error("No SOAP envelope was provided.");
            throw new BuilderException("No SOAP envelope was provided.");
        }
        OMElement elem = null;
        if (envelope.getBody() != null) {
            elem = envelope.getBody().getFirstChildWithName(SUBSCRIBE_QNAME);
        }
        if (elem != null) {
            OMElement deliveryElem = elem.getFirstChildWithName(DELIVERY_QNAME);
            if (deliveryElem != null) {
                notifyToElem = deliveryElem.getFirstChildWithName(NOTIFY_TO_QNAME);
                if (notifyToElem != null) {
                    String ep = BuilderUtils.getEndpointFromWSAAddress(
                            notifyToElem.getFirstElement());
                    if (ep != null) {
                        subscription = new Subscription();
                        subscription.setEventSinkURL(ep);
                    }
                } else {
                    log.error("NotifyTo element not found in the subscription message.");
                    throw new InvalidMessageException(
                            "NotifyTo element not found in the subscription message.");
                }
            } else {
                log.error("Delivery element is not found in the subscription message.");
                throw new InvalidMessageException(
                        "Delivery element is not found in the subscription message.");
            }

            OMElement filterElem = elem.getFirstChildWithName(FILTER_QNAME);
            if (subscription != null && filterElem != null) {
                OMAttribute dialectAttribute = filterElem.getAttribute(ATT_DIALECT);
                if (dialectAttribute != null && dialectAttribute.getAttributeValue() != null) {
                    subscription.setEventFilter(
                            new EventFilter(dialectAttribute.getAttributeValue(),
                                    filterElem.getText().trim()));
                } else {
                    log.error("Error in creating subscription. Filter dialect not defined.");
                    throw new BuilderException(
                            "Error in creating subscription. Filter dialect not defined.");
                }
            } else if (subscription == null) {
                log.error("Error in creating subscription.");
                throw new BuilderException("Error in creating subscription.");
            }
            OMElement expiryElem = elem.getFirstChildWithName(EXPIRES);
            if (expiryElem != null) {
                Calendar calendarExpires;
                try {
                    String expiryText = expiryElem.getText().trim();
                    if (expiryText.startsWith("P")) {
                        calendarExpires = Calendar.getInstance();
                        Duration duration = ConverterUtil.convertToDuration(expiryText);
                        calendarExpires.add(Calendar.YEAR, duration.getYears());
                        calendarExpires.add(Calendar.MONTH, duration.getMonths());
                        calendarExpires.add(Calendar.DAY_OF_MONTH, duration.getDays());
                        calendarExpires.add(Calendar.HOUR_OF_DAY, duration.getHours());
                        calendarExpires.add(Calendar.MINUTE, duration.getMinutes());
                        calendarExpires.add(Calendar.SECOND, (int)duration.getSeconds());
                    } else {
                        calendarExpires = ConverterUtil.convertToDateTime(expiryText);
                    }
                } catch (Exception e) {
                    log.error("Error converting the expiration date.", e);
                    throw new InvalidExpirationTimeException(
                            "Error converting the expiration date.", e);
                }
                Calendar calendarNow = Calendar.getInstance();
                if (calendarNow.before(calendarExpires)) {
                    subscription.setExpires(calendarExpires);
                } else {
                    log.error("The expiration time has passed.");
                    throw new InvalidExpirationTimeException("The expiration time has passed.");
                }
            }

            OMElement scriptElement = elem.getFirstChildWithName(new QName(EventingConstants.WSE_EXTENDED_EVENTING_NS, EventingConstants.EVENTING_EXECUTABLE_SCRIPT_ELEMENT));
            if (scriptElement != null) {
                subscription.getProperties().put(EventingConstants.EVENTING_EXECUTABLE_SCRIPT_ELEMENT, scriptElement.getText());
            }
        } else {
            log.error("Subscribe element is required as the payload of the subscription message.");
            throw new InvalidMessageException(
                    "Subscribe element is required as the payload of the subscription message.");
        }
        return subscription;
    }

    /**
     * (01) <s12:Envelope
     * (02)     xmlns:s12="http://www.w3.org/2003/05/soap-envelope"
     * (03)     xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing"
     * (04)     xmlns:wse="http://schemas.xmlsoap.org/ws/2004/08/eventing"
     * (05)     xmlns:ew="http://www.example.com/warnings"
     * (06)     xmlns:ow="http://www.example.org/oceanwatch" >
     * (07)   <s12:Header>
     * (08)     <wsa:Action>
     * (09) http://schemas.xmlsoap.org/ws/2004/08/eventing/SubscribeResponse
     * (10)     </wsa:Action>
     * (11)     <wsa:RelatesTo>
     * (12)       uuid:e1886c5c-5e86-48d1-8c77-fc1c28d47180
     * (13)     </wsa:RelatesTo>
     * (14)     <wsa:To>http://www.example.com/MyEventSink</wsa:To>
     * (15)     <ew:MySubscription>2597</ew:MySubscription>
     * (16)   </s12:Header>
     * (17)   <s12:Body>
     * (18)     <wse:SubscribeResponse>
     * (19)       <wse:SubscriptionManager>
     * (20)         <wsa:Address>
     * (21)           http://www.example.org/oceanwatch/SubscriptionManager
     * (22)         </wsa:Address>
     * (23)         <wsa:ReferenceParameters>
     * (24)           <wse:Identifier>
     * (25)             uuid:22e8a584-0d18-4228-b2a8-3716fa2097fa
     * (26)           </wse:Identifier>
     * (27)         </wsa:ReferenceParameters>
     * (28)       </wse:SubscriptionManager>
     * (29)       <wse:Expires>2004-07-01T00:00:00.000-00:00</wse:Expires>
     * (30)     </wse:SubscribeResponse>
     * (31)   </s12:Body>
     * (32) </s12:Envelope>
     * Generate the subscription responce message
     *
     * @param subscription The subscription to which the response should be created
     * @return The response envelope.
     */
    public SOAPEnvelope fromSubscription(Subscription subscription) {
        SOAPEnvelope message = factory.getDefaultEnvelope();
        EndpointReference subscriptionManagerEPR =
                new EndpointReference(subscription.getEventSinkURL());
        subscriptionManagerEPR.addReferenceParameter(new QName(EventingConstants.WSE_EVENTING_NS,
                EventingConstants.WSE_EN_IDENTIFIER, EventingConstants.WSE_EVENTING_PREFIX),
                subscription.getId());
        OMNamespace eventingNamespace = factory.createOMNamespace(EventingConstants.WSE_EVENTING_NS,
                EventingConstants.WSE_EVENTING_PREFIX);
        OMElement subscribeResponseElement = factory.createOMElement(
                EventingConstants.WSE_EN_SUBSCRIBE_RESPONSE, eventingNamespace);
        try {
            OMElement subscriptionManagerElement = EndpointReferenceHelper.toOM(
                    subscribeResponseElement.getOMFactory(),
                    subscriptionManagerEPR,
                    new QName(EventingConstants.WSE_EVENTING_NS,
                            EventingConstants.WSE_EN_SUBSCRIPTION_MANAGER,
                            EventingConstants.WSE_EVENTING_PREFIX),
                    AddressingConstants.Submission.WSA_NAMESPACE);
            subscribeResponseElement.addChild(subscriptionManagerElement);
            OMElement expiresElement =
                    factory.createOMElement(EventingConstants.WSE_EN_EXPIRES, eventingNamespace);
            if (subscription.getExpires() != null) {
                factory.createOMText(expiresElement,
                        ConverterUtil.convertToString(subscription.getExpires()));
            } else {
                factory.createOMText(expiresElement, "*");
            }
            subscribeResponseElement.addChild(expiresElement);
            message.getBody().addChild(subscribeResponseElement);
        } catch (AxisFault axisFault) {
            log.error("Unable to create subscription response", axisFault);
            throw new BuilderException("Unable to create subscription response", axisFault);
        }
        return message;
    }
}
