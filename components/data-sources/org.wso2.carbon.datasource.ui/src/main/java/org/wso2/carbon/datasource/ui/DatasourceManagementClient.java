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

package org.wso2.carbon.datasource.ui;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.datasource.DataSourceInformation;
import org.apache.synapse.commons.datasource.factory.DataSourceInformationFactory;
import org.apache.synapse.commons.datasource.serializer.DataSourceInformationSerializer;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.datasource.ui.stub.DataSourceAdminStub;
import org.wso2.carbon.datasource.ui.stub.DataSourceManagementException;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.rmi.RemoteException;
import java.util.*;

/**
 * 
 */
public class DatasourceManagementClient {

    private static final Log log = LogFactory.getLog(DatasourceManagementClient.class);
    private static final String DATASOURCE_EXTENSION_NS =
            "http://www.wso2.org/products/wso2commons/datasource";

    private static final QName ROOT_QNAME = new QName(DATASOURCE_EXTENSION_NS,
            "datasourceExtension", "datasource");

    private DataSourceAdminStub stub;

    private DatasourceManagementClient(String cookie,
                                       String backendServerURL,
                                       ConfigurationContext configCtx) throws AxisFault {

        String serviceURL = backendServerURL + "DataSourceAdmin";
        stub = new DataSourceAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

    }

    public static DatasourceManagementClient getInstance(ServletConfig config, HttpSession session)
            throws AxisFault, DataSourceManagementException {

        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(
                        CarbonConstants.CONFIGURATION_CONTEXT);

        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        return new DatasourceManagementClient(cookie, backendServerURL, configContext);
    }

    public void addDataSourceInformation(DataSourceInformation dataSourceInformation)
            throws DataSourceManagementException, RemoteException {

        validateDataSourceInformation(dataSourceInformation);
        if (log.isDebugEnabled()) {
            log.debug("Going to add DatasourceInformation :" + dataSourceInformation);
        }

        Properties properties = DataSourceInformationSerializer.serialize(dataSourceInformation);
        if (properties.isEmpty()) {
            handleException("Empty Properties");
        }


        OMElement element = createOMElement(properties);
        validateDataSourceElement(element);
        if (log.isDebugEnabled()) {
            log.debug("DataSourceconfiguration :" + element);
        }
        stub.addDataSourceInformation(dataSourceInformation.getAlias(), element);

    }

    public void deleteDatasourceInformation(String name) throws DataSourceManagementException,
            RemoteException {

        validateName(name);
        if (log.isDebugEnabled()) {
            log.debug("Going to delete a DatasourceInformation with name : " + name);
        }
        stub.removeDataSourceInformation(name);
    }

    public void editDatasourceInformation(DataSourceInformation information)
            throws DataSourceManagementException, RemoteException {

        validateDataSourceInformation(information);
        if (log.isDebugEnabled()) {
            log.debug("Going to Edit DataSourceInformation :" + information);
        }
        Properties properties = DataSourceInformationSerializer.serialize(information);
        if (properties.isEmpty()) {
            handleException("Empty Properties");
        }

        OMElement datasourceElement = createOMElement(properties);
        validateDataSourceElement(datasourceElement);
        if (log.isDebugEnabled()) {
            log.debug("DataSourceconfiguration :" + datasourceElement);
        }
        stub.editDataSourceInformation(information.getAlias(), datasourceElement);
    }

    public Map<String, String> getAllDataSourceInformations() throws
            DataSourceManagementException, RemoteException {

        OMElement element = stub.getAllDataSourceInformation();
        Set<String> getInactiveDataSourceList = null;
        if(stub.getInactiveDataSourceList() != null) {
            getInactiveDataSourceList = new HashSet<String>(Arrays.asList(stub.getInactiveDataSourceList()));
        }
        if (log.isDebugEnabled()) {
            log.debug("All datasources configurations :" + element);
        }
        
        Map<String, String> allDataSourceMap = new HashMap();
        if (element == null) {
            return allDataSourceMap;
        }

        OMElement datasourceRoot = element.getFirstChildWithName(ROOT_QNAME);
        if (datasourceRoot == null) {
            return allDataSourceMap;
        }
        Iterator iterator = datasourceRoot.getChildElements();
        while (iterator.hasNext()) {
            OMElement datasourceElement = (OMElement) iterator.next();
            if (datasourceElement != null) {
                String name = datasourceElement.getAttributeValue(new QName("", "name", ""));
                if (name != null && !"".equals(name)) {
                    if (getInactiveDataSourceList != null && getInactiveDataSourceList.contains(name)) {
                        allDataSourceMap.put(name, "Error");
                    } else {
                        allDataSourceMap.put(name, "Active");
                    }
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("All datasources Descriptions :" + allDataSourceMap);
        }
        return allDataSourceMap;
    }


    public DataSourceInformation getDataSourceInformation(String name)
            throws DataSourceManagementException, RemoteException {

        validateName(name);
        if (log.isDebugEnabled()) {
            log.debug("Going to retrieve a DataSourceDescription for give name :" + name);
        }

        OMElement returnElement = stub.getDataSourceInformation(name);
        validateDataSourceElement(returnElement);

        OMElement element = returnElement.getFirstElement();
        return validateAndCreate(name, element);
    }

    public boolean isContains(String name) throws DataSourceManagementException, RemoteException {
        validateName(name);
        return stub.isContains(name);
    }

    public boolean testConnection(DataSourceInformation dataSourceInformation)
            throws DataSourceManagementException, RemoteException {
        validateDataSourceInformation(dataSourceInformation);
        if (log.isDebugEnabled()) {
            log.debug("Going to add DatasourceInformation :" + dataSourceInformation);
        }

        Properties properties = DataSourceInformationSerializer.serialize(dataSourceInformation);
        if (properties.isEmpty()) {
            handleException("Empty Properties");
        }


        OMElement element = createOMElement(properties);
        validateDataSourceElement(element);
        if (log.isDebugEnabled()) {
            log.debug("DataSourceconfiguration :" + element);
        }
        return stub.testConnection(dataSourceInformation.getAlias(), element);
    }

    private static void validateDataSourceInformation(DataSourceInformation description) {

        if (description == null) {
            handleException("DataSourceDescription can not be found.");
        }
    }

    private static void validateDataSourceElement(OMElement datasourceElement) {

        if (datasourceElement == null) {
            handleException("DataSourceDescription OMElement can not be found.");
        }
    }

    private static void validateName(String name) {
        if (name == null || "".equals(name)) {
            handleException("Name is null or empty");
        }
    }

    private static void handleException(String msg) {
        log.error(msg);
        throw new IllegalArgumentException(msg);
    }

    private static void handleException(String msg, Throwable e) {
        log.error(msg, e);
        throw new IllegalArgumentException(msg, e);
    }

    private static Properties loadProperties(OMElement element) {

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
            handleException("IOError loading properties from : " + element);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException inored) {
                }
            }

        }
        return properties;
    }

    private static OMElement createOMElement(Properties properties) {

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

    private static DataSourceInformation validateAndCreate(String name,
                                                           OMElement element) throws AxisFault {
        validateName(name);
        validateDataSourceElement(element);
        Properties properties = loadProperties(element);
        if (log.isDebugEnabled()) {
            log.debug("Properties " + properties);
        }
        if (properties.isEmpty()) {
            handleException("Empty property");
        }

        DataSourceInformation information =
                DataSourceInformationFactory.createDataSourceInformation(name, properties);
        validateDataSourceInformation(information);
        if (log.isDebugEnabled()) {
            log.debug("DataSource Description : " + information);
        }
        return information;
    }

    public List<String> getInactiveDataSourceList() throws RemoteException {
        List<String> getInactiveDataSourceList = new ArrayList<String>();
        if(stub.getInactiveDataSourceList() != null) {
            getInactiveDataSourceList = new ArrayList<String>(Arrays.asList(stub.getInactiveDataSourceList()));
        }

        return getInactiveDataSourceList;
    }

}
