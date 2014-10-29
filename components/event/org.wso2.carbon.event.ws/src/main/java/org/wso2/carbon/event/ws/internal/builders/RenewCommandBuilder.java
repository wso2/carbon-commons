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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.axis2.databinding.types.Duration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.ws.internal.builders.exceptions.BuilderException;
import org.wso2.carbon.event.ws.internal.builders.exceptions.InvalidExpirationTimeException;
import org.wso2.carbon.event.ws.internal.builders.exceptions.InvalidMessageException;
import org.wso2.carbon.event.ws.internal.util.EventingConstants;
import org.wso2.carbon.event.core.subscription.Subscription;

public class RenewCommandBuilder extends CommandBuilderConstants {

    private static final Log log = LogFactory.getLog(SubscribeCommandBuilder.class);
    private SOAPFactory factory;

    public RenewCommandBuilder(MessageContext messageCtx) {
        factory = (SOAPFactory) messageCtx.getEnvelope().getOMFactory();
    }

    /**
     * (01) <s12:Envelope
     * (02)     xmlns:s12="http://www.w3.org/2003/05/soap-envelope"
     * (03)     xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing"
     * (04)     xmlns:wse="http://schemas.xmlsoap.org/ws/2004/08/eventing"
     * (05)     xmlns:ow="http://www.example.org/oceanwatch" >
     * (06)   <s12:Header>
     * (07)     <wsa:Action>
     * (08)       http://schemas.xmlsoap.org/ws/2004/08/eventing/Renew
     * (09)     </wsa:Action>
     * (10)     <wsa:MessageID>
     * (11)       uuid:bd88b3df-5db4-4392-9621-aee9160721f6
     * (12)     </wsa:MessageID>
     * (13)     <wsa:ReplyTo>
     * (14)      <wsa:Address>http://www.example.com/MyEventSink</wsa:Address>
     * (15)     </wsa:ReplyTo>
     * (16)     <wsa:To>
     * (17)       http://www.example.org/oceanwatch/SubscriptionManager
     * (18)     </wsa:To>
     * (19)     <wse:Identifier>
     * (20)       uuid:22e8a584-0d18-4228-b2a8-3716fa2097fa
     * (21)     </wse:Identifier>
     * (22)   </s12:Header>
     * (23)   <s12:Body>
     * (24)     <wse:Renew>
     * (25)       <wse:Expires>2004-06-26T21:07:00.000-08:00</wse:Expires>
     * (26)     </wse:Renew>
     * (27)   </s12:Body>
     * (28) </s12:Envelope>
     *
     * @param envelope The soap envelope containing the renew subscription request
     * @return The subscription to renew
     * @throws InvalidMessageException
     * @throws InvalidExpirationTimeException
     */
    public Subscription toSubscription(SOAPEnvelope envelope) throws InvalidMessageException,
        InvalidExpirationTimeException {
        if (envelope == null) {
            log.error("No SOAP envelope was provided.");
            throw new BuilderException("No SOAP envelope was provided.");
        }
        Subscription subscription = new Subscription();
        OMElement elem = null;
        if (envelope.getHeader() != null) {
            elem = envelope.getHeader().getFirstChildWithName(IDENTIFIER);
        }
        if (elem == null) {
            log.error(
                    "Subscription Identifier is required as a header of the subscription message.");
            throw new InvalidMessageException(
                    "Subscription Identifier is required as a header of the subscription message.");
        }
        String id = elem.getText().trim();
        subscription.setId(id);
        
        OMElement renewElem = envelope.getBody().getFirstChildWithName(RENEW);
        if (renewElem != null) {
            OMElement expiryElem = renewElem.getFirstChildWithName(EXPIRES);
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
                    log.error("Error converting the expiration date", e);
                    throw new InvalidExpirationTimeException(
                            "Error converting the expiration date", e);
                }
                Calendar calendarNow = Calendar.getInstance();
                if (calendarNow.before(calendarExpires)) {
                    subscription.setExpires(calendarExpires);
                } else {
                    log.error("The expiration time has passed");
                    throw new InvalidExpirationTimeException("The expiration time has passed");
                }

                subscription.setExpires(calendarExpires);
            } else {
                log.error("The expiration time was not given");
                throw new InvalidExpirationTimeException("The expiration time was not given");
            }
        }
        return subscription;
    }

    /**
     * (01) <s12:Envelope
     * (02)     xmlns:s12="http://www.w3.org/2003/05/soap-envelope"
     * (03)     xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing"
     * (04)     xmlns:wse="http://schemas.xmlsoap.org/ws/2004/08/eventing"
     * (05)     xmlns:ow="http://www.example.org/oceanwatch" >
     * (06)   <s12:Header>
     * (07)     <wsa:Action>
     * (08)      http://schemas.xmlsoap.org/ws/2004/08/eventing/RenewResponse
     * (09)     </wsa:Action>
     * (10)     <wsa:RelatesTo>
     * (11)       uuid:bd88b3df-5db4-4392-9621-aee9160721f6
     * (12)     </wsa:RelatesTo>
     * (13)     <wsa:To>http://www.example.com/MyEventSink</wsa:To>
     * (14)   </s12:Header>
     * (15)   <s12:Body>
     * (16)     <wse:RenewResponse>
     * (17)       <wse:Expires>2004-06-26T12:00:00.000-00:00</wse:Expires>
     * (18)     </wse:RenewResponse>
     * (19)   </s12:Body>
     * (20) </s12:Envelope>
     *
     * @param subscription The subscription which must be renewed.
     * @return The response envelope for the renew request.
     */
    public SOAPEnvelope fromSubscription(Subscription subscription) {
        SOAPEnvelope message = factory.getDefaultEnvelope();
        OMNamespace eventingNamespace = factory.createOMNamespace(EventingConstants.WSE_EVENTING_NS,
                EventingConstants.WSE_EVENTING_PREFIX);
        OMElement renewResponseElement =
                factory.createOMElement(EventingConstants.WSE_EN_RENEW_RESPONSE, eventingNamespace);
        OMElement expiresElement =
                factory.createOMElement(EventingConstants.WSE_EN_EXPIRES, eventingNamespace);
        factory.createOMText(expiresElement,
                ConverterUtil.convertToString(subscription.getExpires()));
        renewResponseElement.addChild(expiresElement);
        message.getBody().addChild(renewResponseElement);
        return message;
    }
}
