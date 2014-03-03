/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.datasource;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.datasource.DataSourceInformation;
import org.apache.synapse.commons.datasource.factory.DataSourceInformationFactory;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.registry.core.RegistryConstants;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.Properties;

/**
 *
 */
public class MiscellaneousHelper {

    private static final Log log = LogFactory.getLog(MiscellaneousHelper.class);

    public static Properties loadProperties(OMElement element) {

        if (log.isDebugEnabled()) {
            log.debug("Loading properties from : " + element);
        }
        String xml = "<!DOCTYPE properties   [\n" +
                "\n" +
                "<!ELEMENT properties ( comment?, entry* ) >\n" +
                "\n" +
                "<!ATTLIST properties version CDATA #FIXED \"1.0\">\n" +
                "\n" +
                "<!ELEMENT comment (#PCDATA) >\n" +
                "\n" +
                "<!ELEMENT entry (#PCDATA) >\n" +
                "\n" +
                "<!ATTLIST entry key CDATA #REQUIRED>\n" +
                "]>" + element.toString();
        final Properties properties = new Properties();
        InputStream in = null;
        try {
            in = new ByteArrayInputStream(xml.getBytes());
            properties.loadFromXML(in);
            return properties;
        } catch (IOException e) {
            handleException("IOError loading properties from : " + element, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }

        }
        return properties;
    }

    public static Properties loadProperties(InputStream inputStream) {
        return loadProperties(getOMElement(inputStream));
    }

    public static OMElement createOMElement(Properties properties) {
        if (log.isDebugEnabled()) {
            log.debug("Properties : " + properties);
        }
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            properties.storeToXML(baos, "");
            String propertyS = new String(baos.toByteArray());
            String correctedS = propertyS.substring(propertyS.indexOf("<properties>"),
                    propertyS.length());
            String inLined = "<!DOCTYPE properties   [\n" +
                    "\n" +
                    "<!ELEMENT properties ( comment?, entry* ) >\n" +
                    "\n" +
                    "<!ATTLIST properties version CDATA #FIXED \"1.0\">\n" +
                    "\n" +
                    "<!ELEMENT comment (#PCDATA) >\n" +
                    "\n" +
                    "<!ELEMENT entry (#PCDATA) >\n" +
                    "\n" +
                    "<!ATTLIST entry key CDATA #REQUIRED>\n" +
                    "]>";
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(
                    new StringReader(inLined + correctedS));
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            return builder.getDocumentElement();

        } catch (XMLStreamException e) {
            handleException("Error Creating a OMElement from properties : " + properties, e);
        } catch (IOException e) {
            handleException("IOError Creating a OMElement from properties : " + properties, e);
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException ignored) {
                }

            }
        }
        return null;
    }

    public static DataSourceInformation validateAndCreateDataSourceInformation(String name,
                                                                               OMElement element) {
        validateName(name);
        validateElement(element);
        Properties properties = MiscellaneousHelper.loadProperties(element);

        if (properties == null) {
            handleException("Invalid properties");
        }
        if (properties != null && properties.isEmpty()) {
            handleException("Empty property");
        }
        if (log.isDebugEnabled()) {
            log.debug("Properties " + properties);
        }
        DataSourceInformation information =
                DataSourceInformationFactory.createDataSourceInformation(name, properties);
        validateDataSourceDescription(information);
        if (log.isDebugEnabled()) {
            log.debug("DataSource Description : " + information);
        }
        return information;
    }

    public static DataSourceInformation validateAndCreateDataSourceInformation(String name,
                                                                               InputStream inputStream) {
        return validateAndCreateDataSourceInformation(name, getOMElement(inputStream));
    }

    private static void handleException(String msg, Throwable e) {
        log.error(msg, e);
        throw new IllegalArgumentException(msg, e);
    }

    public static void validateName(String name) {
        if (name == null || "".equals(name)) {
            handleException("Name is null or empty");
        } else if (name.contains(RegistryConstants.PATH_SEPARATOR)) {
            handleException("Name must not contain the character: " + RegistryConstants.PATH_SEPARATOR);
        }
    }

    public static void handleException(String msg) {
        log.error(msg);
        throw new RuntimeException(msg);
    }

    public static void validateDataSourceDescription(DataSourceInformation description) {
        if (description == null) {
            handleException("DataSource Description can not be found.");
        }
    }

    public static void validateElement(OMElement element) {
        if (element == null) {
            handleException("DataSource Description OMElement can not be found.");
        }
    }

    public static OMElement getOMElement(InputStream input) {
        BufferedInputStream inputStream = new BufferedInputStream(input);
        try {
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            return builder.getDocumentElement();

        } catch (Exception ignored) {
        } finally {
            try {
                inputStream.close();
            } catch (IOException ignored) {
            }

        }
        return null;

    }

    public static byte[] toByte(OMElement element) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            (element).serialize(baos);
            return baos.toByteArray();
        } catch (XMLStreamException e) {
            handleException("Error when serializing OMNode " + element, e);
        }
        return null;
    }

    public static OMElement encryptPassword(String name, OMElement dsEle) throws CryptoException {
        String passwordProp = "synapse.datasources." + name + ".password";
        Properties props = loadProperties(dsEle);

        //Encrypting the password field
        CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
        String password = props.getProperty(passwordProp);

        if (!StringUtils.isEmpty(password)) {
            String encryptedPassword = cryptoUtil.encryptAndBase64Encode(password.getBytes());

            //serializing the properties back to an OMElement
            props.setProperty(passwordProp, encryptedPassword);
        }
        return createOMElement(props);
    }

    public static OMElement decryptPassword(String name, OMElement dsEle) throws CryptoException {
        String passwordProp = "synapse.datasources." + name + ".password";
        Properties props = loadProperties(dsEle);

        //Decrypting the password field
        CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
        String encryptedPassword = props.getProperty(passwordProp);

        if (!StringUtils.isEmpty(encryptedPassword)) {
            byte[] decryptedPassword = cryptoUtil.base64DecodeAndDecrypt(encryptedPassword);

            //serializing the properties back to an OMElement
            props.setProperty(passwordProp, new String(decryptedPassword));
        }
        return createOMElement(props);
    }
}
