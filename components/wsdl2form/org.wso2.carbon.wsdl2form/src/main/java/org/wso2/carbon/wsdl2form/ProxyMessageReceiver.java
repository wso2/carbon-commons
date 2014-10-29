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
package org.wso2.carbon.wsdl2form;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.AxisBinding;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.receivers.AbstractMessageReceiver;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This will proxy the incoming traffic to the original server. Use only http and SOAP11 protocol.
 */
public class ProxyMessageReceiver extends AbstractMessageReceiver {

    private static Log log = LogFactory.getLog(ProxyMessageReceiver.class);

    private ConfigurationContext configCtx;

    public ProxyMessageReceiver(ConfigurationContext configCtx) {
        this.configCtx = configCtx;
    }


    protected void invokeBusinessLogic(MessageContext inMessage) throws AxisFault {
        SOAPEnvelope env = inMessage.getEnvelope();
        AxisService axisServce = inMessage.getAxisService();
        axisServce.addParameter(WSDL2FormGenerator.LAST_TOUCH_TIME,
                Long.valueOf(System.currentTimeMillis()));
        Map endpoints = axisServce.getEndpoints();
        Set set = endpoints.keySet();
        AxisEndpoint endpoint = null;
        for (Iterator iterator = set.iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            endpoint = (AxisEndpoint) endpoints.get(key);
            String endpointURL = endpoint.getEndpointURL();
            if (endpointURL.indexOf("http://") > -1) {
                AxisBinding binding = endpoint.getBinding();
                String wsoapVersion =
                        (String) binding.getProperty(WSDL2Constants.ATTR_WSOAP_VERSION);
                String inSOAPNamespace = env.getNamespace().getNamespaceURI();
                if (!inSOAPNamespace.equals(wsoapVersion)) {
                    continue;
                }
                break;
            }

        }
        if (endpoint == null) {
            String msg = ProxyMessageReceiver.class.getName() + " cannot find real endpoint";
            log.error(msg);
            throw new AxisFault(msg);
        }
        //Create ServiceClient with deafult config context.
        ServiceClient client = new ServiceClient(configCtx, null);
        OMElement element = env.getBody().getFirstElement();
        if (element != null) {
            element.detach();
        }
        Options options = new Options();
        options.setTo(new EndpointReference(endpoint.getEndpointURL()));
        options.setAction(inMessage.getWSAAction());
        options.setSoapVersionURI(env.getNamespace().getNamespaceURI());
        options.setProperty(HTTPConstants.CHUNKED, Boolean.FALSE);
        options.setTimeOutInMilliSeconds(1000*60*10);
        client.setOptions(options);
        client.disengageModule(Constants.MODULE_ADDRESSING);
        int i = inMessage.getAxisOperation().getAxisSpecificMEPConstant();

        MessageContext outMsgContext = null;
        if (i == WSDLConstants.MEP_CONSTANT_IN_OUT) {
            outMsgContext = MessageContextBuilder.createOutMessageContext(inMessage);
            outMsgContext.getOperationContext().addMessageContext(outMsgContext);
        }

        try {
            if (i == WSDLConstants.MEP_CONSTANT_IN_OUT) {
                client.sendReceive(element);
            } else {
                client.sendRobust(element);
            }
        } catch (AxisFault axisFault) {
            env = getResponseEnvelope(client);
            if (env != null && env.getBody().hasFault()) {
                throw new AxisFault(env.getBody().getFault());
            }
            String msg = ProxyMessageReceiver.class.getName() + " proxy encountered an error";
            log.error(msg);
            throw new AxisFault(msg);
        }

        if ((env = getResponseEnvelope(client)) != null && outMsgContext != null) {
            outMsgContext.setEnvelope(env);
            AxisEngine.send(outMsgContext);
        }
        //No body.
    }

    private SOAPEnvelope getResponseEnvelope(ServiceClient client) throws AxisFault {
        OperationContext operationContext = client.getLastOperationContext();
        MessageContext messageContext =
                operationContext.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
        if (messageContext != null) {
            return messageContext.getEnvelope();
        }
        return null;
    }

}