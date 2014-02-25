/*
 * Copyright 2005-2008 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.transport.jms.ui;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.transport.jms.stub.JMSTransportAdminStub;
import org.wso2.carbon.transport.jms.stub.types.carbon.TransportParameter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.lang.Exception;
import java.util.*;

/**
 *
 */
public class JMSTransportAdminClient {

    private static final Log log = LogFactory.getLog(JMSTransportAdminClient.class);
    private JMSTransportAdminStub stub;

    /**
     * Instantiates JMSTransportAdminClient
     *
     * @param cookie For session management
     * @param backendServerURL URL of the back end server where JMSTransportAdmin is running.
     * @param configCtx ConfigurationContext
     * @throws AxisFault on error
     */
    public JMSTransportAdminClient(String cookie, String backendServerURL,
                                   ConfigurationContext configCtx) throws AxisFault {
        String serviceURL = backendServerURL + "JMSTransportAdmin";
        stub = new JMSTransportAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    /**
     * This method returns service specific JMS transport related parameters for a given service.
     *
     * @param serviceName Service name corresponding to the parameters requested.
     * @return TransportParameter[]
     * @throws AxisFault on error
     */
    public TransportParameter[] getServiceSpecificInParameters(String serviceName) throws AxisFault {
        try {
            TransportParameter[] params = stub.getServiceSpecificInParameters(serviceName);
            if (params == null || params.length == 0 || params[0] == null) {
                return null;
            }
            return params;
        } catch (Exception e) {
            handleException(
                    "Error while retreiving service specific transport parameters for service "
                            + serviceName, e);
        }
        return null;
    }

    public TransportParameter[] getServiceSpecificOutParameters(String serviceName) throws AxisFault {
        try {
            TransportParameter[] params = stub.getServiceSpecificOutParameters(serviceName);
            if (params == null || params.length == 0 || params[0] == null) {
                return null;
            }
            return params;
        } catch (Exception e) {
            handleException(
                    "Error while retreiving service specific transport parameters for service "
                            + serviceName, e);
        }
        return null;
    }

    /**
     * Returns globally defined parameters for the JMS transport.
     *
     * @return TransportParameter[]
     * @throws AxisFault on error
     */
    public TransportParameter[] getGloballyDefinedInParameters() throws AxisFault {
        try {
            TransportParameter[] params = stub.getGloballyDefinedInParameters();
            if (params == null || params.length == 0 || params[0] == null) {
                return null;
            }
            return params;
        } catch (Exception e) {
            handleException("Error while retreiving globally defined transport parameters", e);
        }
        return null;
    }

    public TransportParameter[] getGloballyDefinedOutParameters() throws AxisFault {
        try {
            TransportParameter[] params = stub.getGloballyDefinedOutParameters();
            if (params == null || params.length == 0 || params[0] == null) {
                return null;
            }
            return params;
        } catch (Exception e) {
            handleException("Error while retreiving globally defined transport parameters", e);
        }
        return null;
    }

    public void enableTransportListener(TransportParameter[] inParams) throws AxisFault {
        try {
            if (inParams != null) {
                stub.updateGloballyDefinedInParameters(inParams);
            } else {
                stub.updateGloballyDefinedOutParameters(new TransportParameter[]{});
            }
        } catch (Exception e) {
            handleException("Error while enabling JMS transport listener", e);
        }
    }

    public void enableTransportSender(TransportParameter[] outParams) throws AxisFault {
        try {
            if (outParams != null) {
                stub.updateGloballyDefinedOutParameters(outParams);
            } else {
                stub.updateGloballyDefinedOutParameters(new TransportParameter[]{});
            }
        } catch (Exception e) {
            handleException("Error while enabling JMS transport sender", e);
        }
    }

    public void disableTransportListener() throws AxisFault {
        try {
            stub.disableTransportListener();
        } catch (Exception e) {
            handleException("Error while disabling transport", e);
        }
    }

    public void disableTransportSender() throws AxisFault {
        try {
            stub.disableTransportSender();
        } catch (Exception e) {
            handleException("Error while disabling transport", e);
        }
    }



    /**
     * Updates globally defined JMS transport related parameters.
     *
     * @param params Updated set of transport parameters.
     * @throws AxisFault on error
     */
    public void updateGloballyDefinedInParameters(TransportParameter[] params) throws AxisFault {
        boolean hasUpdated = false;
        OMElement paramElement;
        OMElement tmpElement;
        TransportParameter[] globalParamCache;

        try {
            globalParamCache = getGloballyDefinedInParameters();

            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    TransportParameter transportParameter = params[i];
                    TransportParameter param = getTransportParameter(transportParameter.getName(),
                            globalParamCache);
                    if (param != null) {
                        try {
                            paramElement = AXIOMUtil.stringToOM(transportParameter
                                    .getParamElement());
                            tmpElement = AXIOMUtil.stringToOM(param.getParamElement());
                        } catch (XMLStreamException e) {
                            log	.error("Invalid parameters found while updating JMS transport parameters globally",	e);
                            return;
                        }
                        if (!paramElement.toString().equals(tmpElement.toString())) {
                            hasUpdated = true;
                            break;
                        }
                    }
                }

                if (hasUpdated) {
                    stub.updateGloballyDefinedInParameters(params);
                }
            }
        } catch (Exception e) {
            handleException("Error while updating globaltransport parameters", e);
        }
    }

    public void updateGloballyDefineOutParameters(TransportParameter[] params) throws AxisFault {
        boolean hasUpdated = false;
        OMElement paramElement;
        OMElement tmpElement;
        TransportParameter[] globalParamCache;

        try {
            globalParamCache = getGloballyDefinedOutParameters();

            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    TransportParameter transportParameter = params[i];
                    TransportParameter param = getTransportParameter(transportParameter.getName(),
                            globalParamCache);
                    if (param != null) {
                        try {
                            paramElement = AXIOMUtil.stringToOM(transportParameter
                                    .getParamElement());
                            tmpElement = AXIOMUtil.stringToOM(param.getParamElement());
                        } catch (XMLStreamException e) {
                            log	.error("Invalid parameters found while updating JMS transport parameters globally",	e);
                            return;
                        }
                        if (!paramElement.toString().equals(tmpElement.toString())) {
                            hasUpdated = true;
                            break;
                        }
                    }
                }

                if (hasUpdated) {
                    stub.updateGloballyDefinedOutParameters(params);
                }
            }
        } catch (Exception e) {
            handleException("Error while updating globaltransport parameters", e);
        }
    }

    /**
     * Updates service specific JMS transport related parameters.
     *
     * @param serviceName Name of the corresponding service name.
     * @param params Updated set of transport parameters.
     * @throws AxisFault on error
     */
    public void updateServiceSpecificInParameters(String serviceName, TransportParameter[] params)
            throws AxisFault {
        boolean hasUpdated = false;
        TransportParameter[] cache;
        OMElement paramElement;
        OMElement tmpElement;

        try {
            if (params != null && params.length > 0) {
                cache = getServiceSpecificInParameters(serviceName);
                if (cache != null) {
                    for (int i = 0; i < params.length; i++) {
                        TransportParameter transportParameter = params[i];
                        TransportParameter param = getTransportParameter(transportParameter
                                .getName(), cache);
                        if (param != null) {
                            try {
                                paramElement = AXIOMUtil.stringToOM(transportParameter
                                        .getParamElement());
                                tmpElement = AXIOMUtil.stringToOM(param.getParamElement());
                            } catch (XMLStreamException e) {
                                log	.error("Invalid parameters found while updating JMS transport parameters",e);
                                return;
                            }
                            if (!paramElement.toString().equals(tmpElement.toString())) {
                                hasUpdated = true;
                                break;
                            }
                        }
                    }
                } else {
                    hasUpdated = true;
                }

                if (hasUpdated) {
                    stub.updateServiceSpecificInParameters(serviceName, params);
                }
            }
        } catch (Exception e) {
            handleException("Error while updating service specific transport parameters", e);
        }
    }

    public void updateServiceSpecificOutParameters(String serviceName, TransportParameter[] params)
            throws AxisFault {
        boolean hasUpdated = false;
        TransportParameter[] cache;
        OMElement paramElement;
        OMElement tmpElement;

        try {
            if (params != null && params.length > 0) {
                cache = getServiceSpecificOutParameters(serviceName);
                if (cache != null) {
                    for (int i = 0; i < params.length; i++) {
                        TransportParameter transportParameter = params[i];
                        TransportParameter param = getTransportParameter(transportParameter
                                .getName(), cache);
                        if (param != null) {
                            try {
                                paramElement = AXIOMUtil.stringToOM(transportParameter
                                        .getParamElement());
                                tmpElement = AXIOMUtil.stringToOM(param.getParamElement());
                            } catch (XMLStreamException e) {
                                log	.error("Invalid parameters found while updating JMS transport parameters",e);
                                return;
                            }
                            if (!paramElement.toString().equals(tmpElement.toString())) {
                                hasUpdated = true;
                                break;
                            }
                        }
                    }
                } else {
                    hasUpdated = true;
                }

                if (hasUpdated) {
                    stub.updateServiceSpecificOutParameters(serviceName, params);
                }
            }
        } catch (Exception e) {
            handleException("Error while updating service specific transport parameters", e);
        }
    }

    /**
     * @param paramName Name of the parameter.
     * @param cache Parameter cache
     * @return TransportParameter
     */
    private TransportParameter getTransportParameter(String paramName, TransportParameter[] cache) {
        for (int i = 0; i < cache.length; i++) {
            TransportParameter transportParameter = cache[i];
            if (transportParameter.getName().equals(paramName)) {
                return transportParameter;
            }
        }
        return null;
    }

    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }

    public String getInitialFactory(TransportParameter param) {
        try {
            OMElement paramElement = AXIOMUtil.stringToOM(param.getValue());
            Iterator children = paramElement.getChildElements();
            while (children.hasNext()) {
                OMElement child = (OMElement) children.next();
                if ("java.naming.factory.initial".equals(child.getAttributeValue(new QName("name")))) {
                    return child.getText();
                }
            }
            return null;
        } catch (XMLStreamException e) {
            log.warn("Error while parsing the parameter element", e);
            return null;
        }
    }

    public String getURL(TransportParameter param) {

        try {
            OMElement paramElement = AXIOMUtil.stringToOM(param.getValue());
            Iterator children = paramElement.getChildElements();
            while (children.hasNext()) {
                OMElement child = (OMElement) children.next();
                if ("java.naming.provider.url".equals(child.getAttributeValue(new QName("name")))) {
                    return child.getText();
                }
            }
            return null;
        } catch (XMLStreamException e) {
            log.warn("Error while parsing the parameter element", e);
            return null;
        }
    }

    public String getJNDIName(TransportParameter param) {

        try {
            OMElement paramElement = AXIOMUtil.stringToOM(param.getValue());
            Iterator children = paramElement.getChildElements();
            while (children.hasNext()) {
                OMElement child = (OMElement) children.next();
                if ("transport.jms.ConnectionFactoryJNDIName".equals(child.getAttributeValue(new QName("name")))) {
                    return child.getText();
                }
            }
            return null;
        } catch (XMLStreamException e) {
            log.warn("Error while parsing the parameter element", e);
            return null;
        }
    }

    public Map<String, String> getFactoryParameters(TransportParameter factory) {
        Map<String,String> props = new HashMap<String,String>();
        try {
            OMElement paramElement = AXIOMUtil.stringToOM(factory.getParamElement());
            Iterator children = paramElement.getChildElements();
            while (children.hasNext()) {
                OMElement child = (OMElement) children.next();
                String name = child.getAttributeValue(new QName("name"));
                if (name == null || "".equals(name)) {
                    continue;
                }
                String value = child.getText();
                props.put(name, value);
            }
        } catch (XMLStreamException e) {
            log.error("Error while parsing the parameter element", e);
        }

        return props;
    }

    public Map<String,String> getDisplayParameters(TransportParameter factory) {
        Map<String,String> displayProps = new HashMap<String,String>();
        try {
            OMElement paramElement = AXIOMUtil.stringToOM(factory.getParamElement());
            Iterator childElements = paramElement.getChildElements();
            QName qname = new QName("name");
            while (childElements.hasNext()) {
                OMElement child = (OMElement) childElements.next();
                String name = child.getAttributeValue(qname);
                if (name == null || "".equals(name)) {
                    continue;
                } else if ("java.naming.factory.initial".equals(name) ||
                        "java.naming.provider.url".equals(name) ||
                        "transport.jms.ConnectionFactoryJNDIName".equals(name) ||
                        "transport.jms.ConnectionFactoryType".equals(name)) {
                    displayProps.put(name, child.getText());
                }
            }
        } catch (XMLStreamException e) {
            log.error("Error while parsing the parameter element");
        }
        return displayProps;
    }

    /**
     * Find an return the specified connection factory configuration
     *
     * @param name name of the JMS connection factory to be found
     * @param service name of the service to which this factory is bound or null of it is global
     * @param listener true for listener and false for sender
     * @return a TransportParameter object for the connection factory or null
     *
     * @throws Exception on error
     */
    public TransportParameter getConnectionFactory(String name, String service,
                                                   boolean listener) throws Exception {
        TransportParameter[] params;
        if (service == null) {
            if (listener) {
                params = getGloballyDefinedInParameters();
            } else {
                params = getGloballyDefinedOutParameters();
            }
        } else {
            if (listener) {
                params = getServiceSpecificInParameters(service);
            } else {
                params = getServiceSpecificOutParameters(service);
            }
        }

        if (params != null) {
            for (TransportParameter tp : params) {
                if (tp.getName().equals(name)) {
                    return tp;
                }
            }
        }
        return null;
    }

    public void addConnectionFactory(String factoryName, String service, Map<String,String> params,
                                     boolean listener) throws Exception {

        if (getConnectionFactory(factoryName, null, listener) != null) {
            throw new Exception("A JMS connection factory already exists by the name " + factoryName);
        }

        removeDefaults(params);
        stub.addConnectionFactory(getParameter(factoryName, params), service, listener);
    }

    private void removeDefaults(Map<String,String> params) {
        Map<String,String> defaults = getDefaults();
        List<String> removableKeys = new ArrayList<String>();
        for (String key : params.keySet()) {
            String value = params.get(key);
            String def = defaults.get(key);
            if (def != null && def.equals(value)) {
                removableKeys.add(key);
            }
        }

        for (String key : removableKeys) {
            params.remove(key);
        }
    }

    public void updateConnectionFactory(String factoryName, String service, Map<String,String> params,
                                     boolean listener) throws Exception {

        removeDefaults(params);
        stub.addConnectionFactory(getParameter(factoryName, params), service, listener);
    }

    public void removeConnectionFactory(String factoryName, String service,
                                        boolean listener) throws Exception {
        
        stub.removeConnectionFactory(factoryName, service, listener);
    }

    public TransportParameter getParameter(String factoryName, Map<String,String> params) {
        String paramElement = "<parameter name=\""+ factoryName + "\">\n";
        for (String key : params.keySet()) {
            if (params.get(key) == null || params.get(key).equals("")) {
                continue;
            }
            paramElement += "<parameter name=\"" + key + "\">" + params.get(key) +  "</parameter>\n";
        }
        paramElement += "</parameter>";
        return getParameter(factoryName, paramElement);
    }

    private TransportParameter getParameter(String factoryName, String paramElement) {
        TransportParameter param = new TransportParameter();
        param.setName(factoryName);
        param.setValue(paramElement);
        param.setParamElement(paramElement);
        return param;
    }

    private Map<String,String> getDefaults() {
        Map<String,String> defaults = new HashMap<String,String>();
        defaults.put("transport.Transactionality", "none");
        defaults.put("transport.jms.IdleTaskLimit", "10");
        defaults.put("transport.jms.InitialReconnectDuration", "10000");
        defaults.put("transport.jms.CacheLevel", "auto");
        defaults.put("transport.jms.MaxConcurrentConsumers", "1");
        defaults.put("transport.jms.ReconnectProgressFactor", "2");
        defaults.put("transport.jms.ConcurrentConsumers", "1");
        defaults.put("transport.jms.MaxReconnectDuration", "3600000");
        defaults.put("transport.jms.MaxMessagesPerTask", "-1");
        defaults.put("transport.jms.DestinationType", "queue");
        defaults.put("transport.jms.ReceiveTimeout", "10000");
        defaults.put("transport.jms.JMSSpecVersion", "1.1");
        defaults.put("transport.jms.DefaultReplyDestinationType", "queue");
        return defaults;
    }

}