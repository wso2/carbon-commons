/*
*Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.idp.mgt.config;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.idp.mgt.exception.IdentityProviderMgtException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ServerConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.*;

public class ConfigParser {

    private static Log log = LogFactory.getLog(ConfigParser.class);

    public static final String TRSUTED_IDP_CONGIG = "trusted-idp-config.xml";
    public static final String TRUSTED_IDP_DEFAULT_NAMESPACE = "http://wso2.org/projects/carbon/trusted-idp-config.xml";

    private static String configFilePath;
    private static ConfigParser parser;
    private static Map<String, Object> configuration = new HashMap<String, Object>();
    // To enable attempted thread-safety using double-check locking
    private static Object lock = new Object();

    private OMElement rootElement;

    private ConfigParser() throws IdentityProviderMgtException {
        try {
            buildConfiguration();
        } catch (Exception e) {
            String message = "Error while loading Trusted IdP Configurations";
            log.error(message, e);
            throw new IdentityProviderMgtException(message, e);
        }
    }

    public static ConfigParser getInstance() throws IdentityProviderMgtException {
        if (parser == null) {
            synchronized (lock) {
                if (parser == null) {
                    parser = new ConfigParser();
                }
            }
        }
        return parser;
    }

    public static ConfigParser getInstance(String filePath) throws IdentityProviderMgtException {
        configFilePath = filePath;
        return getInstance();
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    private void buildConfiguration() throws XMLStreamException, IOException {
        InputStream inStream = null;
        StAXOMBuilder builder = null;
        try {
            if (configFilePath != null) {
                File trustedIdPConfigXml = new File(configFilePath);
                if (trustedIdPConfigXml.exists()) {
                    inStream = new FileInputStream(trustedIdPConfigXml);
                }
            } else {

                File trustedIdPConfigXml = new File(CarbonUtils.getCarbonSecurityConfigDirPath(), TRSUTED_IDP_CONGIG);
                if (trustedIdPConfigXml.exists()) {
                    inStream = new FileInputStream(trustedIdPConfigXml);
                }
            }
            if (inStream == null) {
                String message = "Trusted IdP configuration not found";
                log.error(message);
                throw new FileNotFoundException(message);
            }

            builder = new StAXOMBuilder(inStream);
            rootElement = builder.getDocumentElement();
            Stack<String> nameStack = new Stack<String>();
            readChildElements(rootElement, nameStack);
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException e) {
                log.warn("Error closing the input stream after reading trusted-idp-config.xml", e);
            }
        }
    }

    private void readChildElements(OMElement serverConfig, Stack<String> nameStack) {
        for (Iterator childElements = serverConfig.getChildElements(); childElements.hasNext(); ) {
            OMElement element = (OMElement) childElements.next();
            nameStack.push(element.getLocalName());
            if (elementHasText(element)) {
                String key = getKey(nameStack);
                Object currentObject = configuration.get(key);
                String value = replaceSystemProperty(element.getText());
                if (currentObject == null) {
                    configuration.put(key, value);
                } else if (currentObject instanceof ArrayList) {
                    ArrayList list = (ArrayList) currentObject;
                    if (!list.contains(value)) {
                        list.add(value);
                    }
                } else {
                    if (!value.equals(currentObject)) {
                        ArrayList arrayList = new ArrayList(2);
                        arrayList.add(currentObject);
                        arrayList.add(value);
                        configuration.put(key, arrayList);
                    }
                }
            }
            readChildElements(element, nameStack);
            nameStack.pop();
        }
    }

    private String getKey(Stack<String> nameStack) {
        StringBuffer key = new StringBuffer();
        for (int i = 0; i < nameStack.size(); i++) {
            String name = nameStack.elementAt(i);
            key.append(name).append(".");
        }
        key.deleteCharAt(key.lastIndexOf("."));
        return key.toString();
    }

    private boolean elementHasText(OMElement element) {
        String text = element.getText();
        return text != null && text.trim().length() != 0;
    }

    private String replaceSystemProperty(String text) {
        int indexOfStartingChars = -1;
        int indexOfClosingBrace;

        // The following condition deals with properties.
        // Properties are specified as ${system.property},
        // and are assumed to be System properties
        while (indexOfStartingChars < text.indexOf("${")
                && (indexOfStartingChars = text.indexOf("${")) != -1
                && (indexOfClosingBrace = text.indexOf("}")) != -1) { // Is a property used?
            String sysProp = text.substring(indexOfStartingChars + 2, indexOfClosingBrace);
            String propValue = System.getProperty(sysProp);
            if (propValue != null) {
                text = text.substring(0, indexOfStartingChars) + propValue
                        + text.substring(indexOfClosingBrace + 1);
            }
            if (sysProp.equals(ServerConstants.CARBON_HOME)) {
                if (System.getProperty(ServerConstants.CARBON_HOME).equals(".")) {
                    text = new File(".").getAbsolutePath() + File.separator + text;
                }
            }
        }
        return text;
    }

    /**
     * Returns the element with the provided local part
     *
     * @param localPart local part name
     * @return Corresponding OMElement
     */
    public OMElement getConfigElement(String localPart) {
        return rootElement.getFirstChildWithName(new QName(TRUSTED_IDP_DEFAULT_NAMESPACE, localPart));
    }

}
