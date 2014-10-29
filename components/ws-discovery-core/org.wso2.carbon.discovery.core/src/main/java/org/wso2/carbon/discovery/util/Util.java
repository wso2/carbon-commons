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

package org.wso2.carbon.discovery.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisObserver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.discovery.DiscoveryConstants;
import org.wso2.carbon.discovery.DiscoveryException;
import org.wso2.carbon.discovery.TargetServiceObserver;
import org.wso2.carbon.discovery.workers.MessageSender;
import org.wso2.carbon.utils.NetworkUtils;

import javax.xml.namespace.QName;
import java.net.SocketException;
import java.net.URI;
import java.util.List;

/**
 * Generic utility methods to manipulate data structures
 */
public class Util {

    private static final Log log = LogFactory.getLog(Util.class);

    /**
     * Get the WS-D types of an Axis2 service. By default the port types of the
     * service are considered as WS-D types.
     *
     * @param axisService The Axis2 service
     * @return a WS-D type to be associated with the service
     */
    public static QName getTypes(AxisService axisService) {
        QName portType;
        String localPart;
        if (axisService.getParameter(WSDL2Constants.INTERFACE_LOCAL_NAME) != null) {
            localPart = (String) axisService.getParameter(
                    WSDL2Constants.INTERFACE_LOCAL_NAME).getValue();
        } else {
            localPart = axisService.getName() + Java2WSDLConstants.PORT_TYPE_SUFFIX;
        }
        portType = new QName(axisService.getTargetNamespace(), localPart);
        return portType;
    }

    /**
     * Convert an object array into a string array. This method relies on the
     * Object#toString() method to convert an object into a string. This is most
     * suitable for converting arrays of URI instances and QName instances into
     * string arrays.
     *
     * @param array An array of any objects - must not be null
     * @return An array of strings representing the object array
     */
    public static String[] toStringArray(Object[] array) {
        String[] values = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            values[i] = array[i].toString();
        }
        return values;
    }

    /**
     * Convert an array of strings into an array of QName instances. The string
     * values should be in a format which can be reliably converted into the QName
     * type using the QName.valueOf() method.
     *
     * @param array An array of strings - must not be null
     * @return An array of QName instances
     */
    public static QName[] toQNameArray(String[] array) {
        QName[] qnames = new QName[array.length];
        for (int i = 0; i < array.length; i++) {
            qnames[i] = QName.valueOf(array[i]);
        }
        return qnames;
    }

    /**
     * Convert an array of strings into an array of URI instances. The string
     * values should be in a format which can be reliably converted into the URI
     * type using the URI.create() method.
     *
     * @param array An array of strings - must not be null
     * @return An array of URI instances
     */
    public static URI[] toURIArray(String[] array) {
        URI[] uris = new URI[array.length];
        for (int i = 0; i < array.length; i++) {
            uris[i] = URI.create(array[i].trim());
        }
        return uris;
    }

    /**
     * Engage the TargetServiceObserver on the given AxisConfiguration. This will enable
     * service discovery.
     *
     * @param axisConf AxisConfiguration instance
     */
    public static void registerServiceObserver(AxisConfiguration axisConf) {
        TargetServiceObserver targetServiceObserver = new TargetServiceObserver();
        targetServiceObserver.init(axisConf);
        axisConf.addObservers(targetServiceObserver);
    }

    /**
     * Disengage the TargetServiceObserver from the given AxisConfiguration. This will
     * disable service discovery. If sendBye is set to 'true' this method will also send BYE
     * messages to the discovery proxy before disabling the TargetServiceObserver. This method
     * expects the discovery proxy parameter is available in the AxisConfiguration. Without
     * that it will not send BYE messages even if sendBye is set to 'true'.
     *
     * @param axisConf AxisConfiguration instance
     * @param sendBye true if BYE messages should be sent for all the deployed services
     */
    public static void unregisterServiceObserver(AxisConfiguration axisConf, boolean sendBye) {
        if (sendBye) {
            Parameter discoveryProxyParam = axisConf.getParameter(DiscoveryConstants.DISCOVERY_PROXY);
            if (discoveryProxyParam != null) {
                MessageSender messageSender = new MessageSender();
                try {
                    for (AxisService axisService : axisConf.getServices().values()) {
                        messageSender.sendBye(axisService, (String) discoveryProxyParam.getValue());
                    }
                } catch (DiscoveryException e) {
                    log.error("Cannot send the bye message", e);
                }
            }
        }

        List<AxisObserver> observers = axisConf.getObserversList();
        AxisObserver serviceObserver = null;
        // Locate the TargetServiceObserver instance registered earlier
        for (AxisObserver o : observers) {
            if (o instanceof TargetServiceObserver) {
                serviceObserver = o;
                break;
            }
        }

        if (serviceObserver != null) {            
            observers.remove(serviceObserver);
        }
    }

    public static String getWsdlInformation(String serviceName,
                                              AxisConfiguration axisConfig) throws AxisFault {
        String ip;
        try {
            ip = NetworkUtils.getLocalHostname();
        } catch (SocketException e) {
            throw new AxisFault("Cannot get local host name", e);
        }

        TransportInDescription transportInDescription = axisConfig.getTransportIn("http");

        if (transportInDescription == null) {
            transportInDescription = axisConfig.getTransportIn("https");
        }

        if (transportInDescription != null) {
            EndpointReference[] epr =
                    transportInDescription.getReceiver().getEPRsForService(serviceName, ip);
            String wsdlUrlPrefix = epr[0].getAddress();
            if (wsdlUrlPrefix.endsWith("/")) {
                wsdlUrlPrefix = wsdlUrlPrefix.substring(0, wsdlUrlPrefix.length() - 1);
            }
            return wsdlUrlPrefix + "?wsdl";
        }
        return null;
    }
}
