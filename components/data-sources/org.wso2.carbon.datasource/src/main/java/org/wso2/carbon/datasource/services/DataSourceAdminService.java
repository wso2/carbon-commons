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

package org.wso2.carbon.datasource.services;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.datasource.DataSourceInformation;
import org.apache.synapse.commons.datasource.serializer.DataSourceInformationSerializer;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.datasource.*;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 *
 */
public class DataSourceAdminService extends AbstractAdmin{

    private DataSourceInformationManager dataSourceInformationManager =
            DataSourceManagementHandler.getInstance().getTenantDataSourceInformationManager();

    private static final Log log = LogFactory.getLog(DataSourceAdminService.class);
    
    private static final String DATASOURCE_EXTENSION_NS =
            "http://www.wso2.org/products/wso2commons/datasource";

    private static final OMFactory FACTORY = OMAbstractFactory.getOMFactory();

    public void addDataSourceInformation(String name,
                                         OMElement element) throws DataSourceManagementException {
        DataSourceInformation information =
                MiscellaneousHelper.validateAndCreateDataSourceInformation(name, element);

        if (isContains(information.getAlias())) {
            throw new DataSourceManagementException("A data source with name " +
                    information.getAlias() + " is already there.");
        }

        try {
            dataSourceInformationManager.addDataSourceInformation(information);
            dataSourceInformationManager.persistDataSourceInformation(name.trim(), element);
        } catch (Exception e) {
            try {
                dataSourceInformationManager.removeDataSourceInformation(name.trim());
            } catch (Exception ignored) {
            }
            throw new DataSourceManagementException("Error adding a data source : " +
                    e.getMessage(), e);
        }
    }

    public boolean testConnection(String name,
                                  OMElement element) throws DataSourceManagementException {
        try {
            return dataSourceInformationManager.testConnection(
                    MiscellaneousHelper.validateAndCreateDataSourceInformation(name, element));
        } catch (Exception e) {
            throw new DataSourceManagementException("Error testing connection : " +
                    e.getMessage(), e);
        }
    }

    public void removeDataSourceInformation(String name) throws DataSourceManagementException {
        MiscellaneousHelper.validateName(name);
        try {
            dataSourceInformationManager.removeDataSourceInformation(name);
            dataSourceInformationManager.removeDataSourceInformationFromRegistry(name.trim());
        } catch (Exception e) {
            throw new DataSourceManagementException("Error deleting a data source : " +
                    e.getMessage(), e);
        }
    }

    public OMElement getDataSourceInformation(String name) throws DataSourceManagementException {
        MiscellaneousHelper.validateName(name);
        try {
            DataSourceInformation information =
                    dataSourceInformationManager.getDataSourceInformation(name);
            MiscellaneousHelper.validateDataSourceDescription(information);
            Properties properties = DataSourceInformationSerializer.serialize(information);
            if (properties.isEmpty()) {
                handleException("Empty Properties");
            }
            OMElement element = MiscellaneousHelper.createOMElement(properties);
            MiscellaneousHelper.validateElement(element);
            if (log.isDebugEnabled()) {
                log.debug("Returning a datasource : " + element);
            }
            return element;
        } catch (Exception e) {
            throw new DataSourceManagementException("Error loading a data source : " +
                    e.getMessage(), e);
        }

    }

    public void setConfigurationProperties(String name,
                                           OMElement element) throws DataSourceManagementException {
        MiscellaneousHelper.validateName(name);
        MiscellaneousHelper.validateElement(element);
        try {
            dataSourceInformationManager.configure(MiscellaneousHelper.loadProperties(element));
        } catch (Exception e) {
            throw new DataSourceManagementException("Error configuring " +
                    "a data source repository : " + e.getMessage(), e);
        }
    }

    public OMElement getAllDataSourceInformation() throws DataSourceManagementException {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement root =
                factory.createOMElement(new QName(DATASOURCE_EXTENSION_NS,
                        "datasourceExtension", "datasource"));

        OMNamespace nullNS = FACTORY.createOMNamespace("", "");
        try {
            Iterator<DataSourceInformation> iterator =
                    dataSourceInformationManager.getAllDataSourceInformation();
            while (iterator.hasNext()) {
                DataSourceInformation information = iterator.next();
                if (information != null) {
                    OMElement element = factory.createOMElement(
                            (new QName(DATASOURCE_EXTENSION_NS, "datasource", "datasource")));
                    element.addAttribute(factory.createOMAttribute("name", nullNS,
                            information.getAlias()));
                    root.addChild(element);
                }
            }
        } catch (Exception e) {
            log.error(e);
            throw new DataSourceManagementException("Error loading all data sources : " +
                    e.getMessage(), e);
        }
        if (log.isDebugEnabled()) {
            log.debug("All DataSources : " + root);
        }
        return root;
    }

    public void editDataSourceInformation(String name, OMElement element)
            throws DataSourceManagementException {

        MiscellaneousHelper.validateName(name);
        DataSourceInformation editingDSI = dataSourceInformationManager.getDataSourceInformation(name);
        try {
            dataSourceInformationManager.removeDataSourceInformation(name);
        } catch (Exception ignore) {
            //ignore
        }
        try {
            addDataSourceInformation(name, element);
            List<String> inactiveDataSourceList = getInactiveDataSourceList();
            if (inactiveDataSourceList != null && inactiveDataSourceList.contains(name)) {
                inactiveDataSourceList.remove(name);
                dataSourceInformationManager.setInactiveDataSourceList(inactiveDataSourceList);
            }
        } catch (Exception e) {
            dataSourceInformationManager.addDataSourceInformation(editingDSI);
            String message = "Error editing a data source. restoring " +
                    "the existing one.";
            log.error(message, e);
            throw new DataSourceManagementException(message, e);
        }
    }

    public boolean isContains(String name) throws DataSourceManagementException {
        MiscellaneousHelper.validateName(name);
        try {
            return dataSourceInformationManager.isContains(name);
        } catch (Exception e) {
            throw new DataSourceManagementException("Error looking up a data source : " +
                    e.getMessage(), e);
        }
    }

    private static void handleException(String msg) throws DataSourceManagementException {
        log.error(msg);
        throw new DataSourceManagementException(msg);
    }

    public List<String> getInactiveDataSourceList() {
        return dataSourceInformationManager.getInactiveDataSourceList();
    }

}
