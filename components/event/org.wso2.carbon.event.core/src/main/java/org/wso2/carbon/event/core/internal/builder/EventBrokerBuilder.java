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

package org.wso2.carbon.event.core.internal.builder;

import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.event.core.EventBrokerFactory;
import org.wso2.carbon.event.core.exception.EventBrokerConfigurationException;
import org.wso2.carbon.event.core.util.EventBrokerConstants;
import org.wso2.carbon.utils.ServerConstants;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.namespace.QName;
import java.io.*;

/**
 * Start point to build the event broker. This class read the event broker file and calls the
 * event broker factory class to create event broker
 */
public class EventBrokerBuilder {

    public static EventBroker createEventBroker() throws EventBrokerConfigurationException {
        OMElement ebConfig = loadConfigXML();
        if (!ebConfig.getQName().equals(
                new QName(EventBrokerConstants.EB_CONF_NAMESPACE, EventBrokerConstants.EB_CONF_ELE_ROOT))) {
            throw new EventBrokerConfigurationException("Invalid root element in event broker config");
        }

        OMElement eventBroker =
                ebConfig.getFirstChildWithName(new QName(EventBrokerConstants.EB_CONF_NAMESPACE,
                        EventBrokerConstants.EB_CONF_ELE_EVENT_BROKER));
        String className =
                eventBroker.getAttributeValue(new QName(null, EventBrokerConstants.EB_CONF_ATTR_CLASS));

        try {
            Class brokerImplClass = Class.forName(className);
            EventBrokerFactory eventBrokerFactory =
                    (EventBrokerFactory) brokerImplClass.newInstance();
            return eventBrokerFactory.getEventBroker(eventBroker);
        } catch (ClassNotFoundException e) {
            throw new EventBrokerConfigurationException("Can not load the class " + className, e);
        } catch (IllegalAccessException e) {
            throw new EventBrokerConfigurationException("Can not access the class " + className, e);
        } catch (InstantiationException e) {
            throw new EventBrokerConfigurationException("Can not instantiate the class " + className, e);
        }


    }


    /**
     * Helper method to load the event config
     *
     * @return OMElement representation of the event config
     */
    private static OMElement loadConfigXML() throws EventBrokerConfigurationException {

        String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);
        String path = carbonHome + File.separator + "repository" + File.separator + "conf" + File.separator + EventBrokerConstants.EB_CONF;
        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(new File(path)));
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            OMElement omElement = builder.getDocumentElement();
            omElement.build();
            return omElement;
        } catch (FileNotFoundException e) {
            throw new EventBrokerConfigurationException(EventBrokerConstants.EB_CONF
                    + "cannot be found in the path : " + path, e);
        } catch (XMLStreamException e) {
            throw new EventBrokerConfigurationException("Invalid XML for " + EventBrokerConstants.EB_CONF
                    + " located in the path : " + path, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ingored) {
                throw new EventBrokerConfigurationException("Can not close the input stream");
            }
        }
    }
}
