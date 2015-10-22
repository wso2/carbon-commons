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
package org.wso2.carbon.event.ws.internal.builders.utils;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultSubCode;
import org.apache.axiom.soap.SOAPFaultText;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.ws.internal.builders.exceptions.BuilderException;
import org.wso2.carbon.event.ws.internal.builders.exceptions.InvalidMessageException;
import org.wso2.carbon.event.ws.internal.util.EventingConstants;
import org.wso2.carbon.event.core.subscription.Subscription;

@Deprecated
public class BuilderUtils {

    private static final Log log = LogFactory.getLog(BuilderUtils.class);

    /**
     * Obtain the endpoint from a WS-Adressing address.
     * @param address The address element
     * @return The endpoint
     */
    public static String getEndpointFromWSAAddress(OMElement address) {
        if (address == null || address.getText() == null) {
            log.error("Invalid WSA Address");
            throw new BuilderException("Invalid WSA Address");
        }
        return address.getText().trim();
    }

    /**
     * <S:Envelope>
     * <S:Header>
     * <wsa:Action>
     * http://schemas.xmlsoap.org/ws/2004/08/addressing/fault
     * </wsa:Action>
     * <!-- Headers elided for clarity.  -->
     * </S:Header>
     * <S:Body>
     * <S:Fault>
     * <S:Code>
     * <S:Value>[Code]</S:Value>
     * <S:Subcode>
     * <S:Value>[Subcode]</S:Value>
     * </S:Subcode>
     * </S:Code>
     * <S:Reason>
     * <S:Text xml:lang="en">[Reason]</S:Text>
     * </S:Reason>
     * <S:Detail>
     * [Detail]
     * </S:Detail>
     * </S:Fault>
     * </S:Body>
     * </S:Envelope>
     *
     * @param code The code of the fault
     * @param subCode The sub code
     * @param reason The fault that occured
     * @param detail Additional details
     * @param isSOAP11 Whether the fault message should comply with the SOAP 1.1 spec or not.
     * @return The fault soap envelope
     */
    public static SOAPEnvelope genFaultResponse(String code,
                                         String subCode,
                                         String reason,
                                         String detail,
                                         boolean isSOAP11) {
        SOAPFactory soapFactory;
        if (isSOAP11) {
            soapFactory = OMAbstractFactory.getSOAP11Factory();
            SOAPEnvelope message = soapFactory.getDefaultFaultEnvelope();
            SOAPFaultReason soapFaultReason = soapFactory.createSOAPFaultReason();
            soapFaultReason.setText(reason);
            message.getBody().getFault().setReason(soapFaultReason);
            SOAPFaultCode soapFaultCode = soapFactory.createSOAPFaultCode();
            QName qNameSubCode = new QName(EventingConstants.WSE_EVENTING_NS, subCode,
                    EventingConstants.WSE_EVENTING_PREFIX);
            soapFaultCode.setText(qNameSubCode);
            message.getBody().getFault().setCode(soapFaultCode);
            return message;
        } else {
            soapFactory = OMAbstractFactory.getSOAP12Factory();
            SOAPEnvelope message = soapFactory.getDefaultFaultEnvelope();
            SOAPFaultDetail soapFaultDetail = soapFactory.createSOAPFaultDetail();
            soapFaultDetail.setText(detail);
            message.getBody().getFault().setDetail(soapFaultDetail);
            SOAPFaultReason soapFaultReason = soapFactory.createSOAPFaultReason();
            SOAPFaultText soapFaultText = soapFactory.createSOAPFaultText();
            soapFaultText.setText(reason);
            soapFaultReason.addSOAPText(soapFaultText);
            message.getBody().getFault().setReason(soapFaultReason);
            SOAPFaultCode soapFaultCode = soapFactory.createSOAPFaultCode();
            SOAPFaultValue soapFaultValue = soapFactory.createSOAPFaultValue(soapFaultCode);
            soapFaultValue.setText(code);
            soapFaultCode.setValue(soapFaultValue);
            SOAPFaultSubCode soapFaultSubCode = soapFactory.createSOAPFaultSubCode(soapFaultCode);
            SOAPFaultValue soapFaultValueSub = soapFactory.createSOAPFaultValue(soapFaultSubCode);
            QName qNameSubCode = new QName(EventingConstants.WSE_EVENTING_NS, subCode,
                    EventingConstants.WSE_EVENTING_PREFIX);
            soapFaultValueSub.setText(qNameSubCode);
            soapFaultSubCode.setValue(soapFaultValueSub);
            soapFaultCode.setSubCode(soapFaultSubCode);
            message.getBody().getFault().setCode(soapFaultCode);
            return message;
        }
    }

    public static Subscription createSubscription(String endpoint, String filterDialect,
                                                  String topic) throws InvalidMessageException {
        Subscription subscription = new Subscription();
        if (endpoint == null) {
            throw new InvalidMessageException("Endpoint not found in the subscription request");
        }
        subscription.setEventSinkURL(endpoint);
        if (topic == null) {
            throw new InvalidMessageException("Error in creating subscription. Topic not defined");
        }
        
        subscription.setTopicName(topic);
        return subscription;
    }
}
