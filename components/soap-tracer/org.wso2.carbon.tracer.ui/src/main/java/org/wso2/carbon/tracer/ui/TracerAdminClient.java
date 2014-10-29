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
package org.wso2.carbon.tracer.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.tracer.stub.TracerAdminStub;
import org.wso2.carbon.tracer.stub.types.carbon.MessagePayload;
import org.wso2.carbon.tracer.stub.types.carbon.TracerServiceInfo;

import java.rmi.RemoteException;
import java.text.MessageFormat;import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 */
public class TracerAdminClient {

    private static final Log log = LogFactory.getLog(TracerAdminClient.class);
    public TracerAdminStub stub;
    private static final String BUNDLE = "org.wso2.carbon.tracer.ui.i18n.Resources";
    private final ResourceBundle resourceBundle;

    public TracerAdminClient(String cookie,
                             String backendServerURL,
                             ConfigurationContext configCtx,
                             Locale locale) throws AxisFault {
        String serviceURL = backendServerURL + "TracerAdmin";
        stub = new TracerAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        resourceBundle = ResourceBundle.getBundle(BUNDLE, locale);
    }

    public TracerServiceInfo getMessages(int numberOfMessages, String filter) throws
                                                                              RemoteException {
        try {
            TracerServiceInfo tracerServiceInfo = stub.getMessages(numberOfMessages, filter);
            MessagePayload message = tracerServiceInfo.getLastMessage();
            escapeHtml(message);
            return tracerServiceInfo;
        } catch (Exception e) {
            handleException(resourceBundle.getString("cannot.get.the.list.of.tracer.messages"), e);
        }
        return null;
    }

    public TracerServiceInfo setMonitoring(String flag) throws RemoteException {
        try {
            return stub.setMonitoring(flag);
        } catch (Exception e) {
            handleException(MessageFormat.format(resourceBundle.getString("cannot.set.tracer.monitoring.status"),
                                                 flag), e);
        }
        return null;
    }

    public void clearAllSoapMessages() throws RemoteException {
        try {
            stub.clearAllSoapMessages();
        } catch (RemoteException e) {
            handleException(resourceBundle.getString("cannot.clear.all.soap.messages"), e);
        }
    }

    public MessagePayload getMessage(String serviceName,
                                     String operationName,
                                     long messageSequence) throws RemoteException {
        try {
            MessagePayload message = stub.getMessage(serviceName, operationName, messageSequence);
            escapeHtml(message);
            return message;
        } catch (Exception e) {
            handleException(MessageFormat.format(resourceBundle.getString("cannot.get.tracer.message"),
                                                 messageSequence, serviceName, operationName), e);
        }
        return null;

    }

    private void escapeHtml(MessagePayload message) {
        if (message != null) {
            if (message.getRequest() != null) {
                String req = StringEscapeUtils.escapeHtml(removeXmlProlog(message.getRequest()));
                message.setRequest(req);
            }
            if (message.getResponse() != null) {
                String resp = StringEscapeUtils.escapeHtml(removeXmlProlog(message.getResponse()));
                message.setResponse(resp);
            }
        }
    }

    private String removeXmlProlog(String xml) {
        xml = xml.trim();
        if (xml.indexOf("<?xml") == 0) {
            xml = xml.substring(xml.indexOf(">") + 1);
        }
        return xml;
    }

    private void handleException(String msg, Exception e) throws RemoteException {
        log.error(msg, e);
        throw new RemoteException(msg, e);
    }
}
