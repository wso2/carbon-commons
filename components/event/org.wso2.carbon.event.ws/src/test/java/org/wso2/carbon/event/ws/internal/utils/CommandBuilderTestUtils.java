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
package org.wso2.carbon.event.ws.internal.utils;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.AxisFault;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axiom.soap.impl.llom.soap12.SOAP12Factory;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.factory.OMXMLBuilderFactory;

import javax.xml.stream.XMLStreamException;

@Deprecated
public abstract class CommandBuilderTestUtils {

    public static MessageContext getMCWithSOAP11Envelope() throws AxisFault {
        MessageContext messageContext = new MessageContext();
        SOAPFactory factory = new SOAP11Factory();
        SOAPEnvelope envelope = factory.createSOAPEnvelope();
        messageContext.setEnvelope(envelope);
        return messageContext;
    }

    public static MessageContext getMCWithSOAP12Envelope() throws AxisFault {
        MessageContext messageContext = new MessageContext();
        SOAPFactory factory = new SOAP12Factory();
        SOAPEnvelope envelope = factory.createSOAPEnvelope();
        messageContext.setEnvelope(envelope);
        return messageContext;
    }

    public static SOAPEnvelope payloadToSOAP11Envelope(String payload) throws XMLStreamException {
        OMElement ele = AXIOMUtil.stringToOM(payload);
        SOAPFactory factory = new SOAP11Factory();
        return OMXMLBuilderFactory.createStAXSOAPModelBuilder(factory,
                ele.getXMLStreamReader()).getSOAPEnvelope();
    }

    public static SOAPEnvelope payloadToSOAP12Envelope(String payload) throws XMLStreamException {
        OMElement ele = AXIOMUtil.stringToOM(payload);
        SOAPFactory factory = new SOAP12Factory();
        return OMXMLBuilderFactory.createStAXSOAPModelBuilder(factory,
                ele.getXMLStreamReader()).getSOAPEnvelope();
    }
}
