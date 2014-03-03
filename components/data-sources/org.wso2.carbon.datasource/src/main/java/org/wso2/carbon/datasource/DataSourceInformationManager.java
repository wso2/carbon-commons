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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.SynapseCommonsException;
import org.apache.synapse.commons.datasource.DataSourceInformation;
import org.apache.synapse.commons.datasource.DataSourceInformationRepository;
import org.apache.synapse.commons.datasource.DataSourceInformationRepositoryListener;
import org.apache.synapse.commons.datasource.DataSourceRepositoryManager;
import org.apache.synapse.commons.datasource.factory.DataSourceFactory;
import org.apache.synapse.commons.datasource.factory.DataSourceInformationRepositoryFactory;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Data source information manager class.
 */
public class DataSourceInformationManager {

    private final static Log log = LogFactory.getLog(DataSourceInformationManager.class);

    private Registry registry;

    private DataSourceInformationRepository repository;

    private final static String ROOT_PATH = RegistryResources.COMPONENTS
            + "org.wso2.carbon.datasource";

    private final List<String> inactiveDataSourceList = new ArrayList<String>();

    public void populateDataSourceInformation() {
        try {
            if (this.getRegistry().resourceExists(ROOT_PATH)) {
                Resource resource = this.getRegistry().get(ROOT_PATH);
                if (!(resource instanceof Collection)) {
                    return;
                }
                Collection collection = (Collection) resource;
                int length = collection.getChildCount();
                if (length <= 0) {
                    return;
                }
                String[] datasources = collection.getChildren();
                for (String ds : datasources) {

                    if (ds == null) {
                        continue;
                    }

                    Resource child = this.getRegistry().get(ds);
                    if (child == null) {
                        continue;
                    }

                    InputStream in = child.getContentStream();
                    if (in == null) {
                        continue;
                    }

                    String alias = getResourceName(ds.trim());
                    if (alias == null || "".equals(alias)) {
                        return;
                    }

                    OMElement dsEle = MiscellaneousHelper.getOMElement(in);
                    try {
                        //Decrypting datasource password
                        dsEle = MiscellaneousHelper.decryptPassword(alias, dsEle);
                    } catch (CryptoException e) {
                        handleException("Error decrypting datasource password", e);
                    }

                    DataSourceInformation information = MiscellaneousHelper.
                            validateAndCreateDataSourceInformation(alias, dsEle);

                    try {
                        if (this.getRepository() == null) {
                            this.repository = DataSourceInformationRepositoryFactory.
                                    createDataSourceInformationRepository(new Properties());
                        }
                        this.getRepository().addDataSourceInformation(information);
                    } catch (SynapseCommonsException e) {
                        inactiveDataSourceList.add(alias);
                        log.error("Error while creating DataSource Information Repository " , e);
                    }

                    child.discard();

                }
                resource.discard();
            } else {
                CollectionImpl collection = new CollectionImpl();
                collection.setPath(ROOT_PATH);
                this.getRegistry().put(ROOT_PATH, collection);
            }
        } catch (RegistryException e) {
            handleException("Error during initializing DataSources based " +
                    "on persisted configuration", e);
        }
    }

    public void shutDown() {
        DataSourceInformationRepositoryListener listener =
                this.getRepository().getRepositoryListener();
        if (listener instanceof DataSourceRepositoryManager) {
            ((DataSourceRepositoryManager) listener).clear();
        }
    }

    public void addDataSourceInformation(DataSourceInformation information) {
        this.getRepository().addDataSourceInformation(information);
    }

    public void persistDataSourceInformation(String name, OMElement element) {
        MiscellaneousHelper.validateName(name);
        MiscellaneousHelper.validateElement(element);

        try {
            element = MiscellaneousHelper.encryptPassword(name, element);
        } catch (CryptoException e) {
            handleException("Error encrypting datasource password", e);
        }

        byte[] value = MiscellaneousHelper.toByte(element);
        if (value != null) {
            String dataSourcePath = ROOT_PATH + RegistryConstants.PATH_SEPARATOR + name.trim();
            try {
                Resource resource;
                if (this.getRegistry().resourceExists(dataSourcePath)) {
                    resource = this.getRegistry().get(dataSourcePath);
                } else {
                    resource = new ResourceImpl();
                }
                if (resource != null) {
                    resource.setContent(value);
                    this.getRegistry().put(dataSourcePath, resource);
                }
            } catch (RegistryException e) {
                handleException("Error persisting DataSource Information", e);
            }
        }
    }
    
    public DataSourceInformation removeDataSourceInformation(String name) {
        if (this.getRepository().getDataSourceInformation(name) != null) {
            return this.getRepository().removeDataSourceInformation(name);
        }
        return null;
    }

    public void removeDataSourceInformationFromRegistry(String name) {
        String dataSourcePath = ROOT_PATH + RegistryConstants.PATH_SEPARATOR + name;
        try {
            if (this.getRegistry().resourceExists(dataSourcePath)) {
                this.getRegistry().delete(dataSourcePath);
            }
        } catch (RegistryException e) {
            handleException("Error occurred when removing data source configuration" +
                    " from registry ", e);
        }
    }

    public void configure(Properties properties) {
        this.getRepository().configure(properties);
    }

    public Iterator<DataSourceInformation> getAllDataSourceInformation() throws
            DataSourceManagementException {
        return this.getRepository().getAllDataSourceInformation();
    }

    public DataSourceInformation getDataSourceInformation(String name) {
        return this.getRepository().getDataSourceInformation(name);
    }

    public boolean isContains(String name) {
        return this.getRepository().getDataSourceInformation(name) != null;
    }

    public boolean testConnection(DataSourceInformation information) {

        if (information == null) {
            handleException("DataSourceInformation cannot be found.");
        }

        DataSource dataSource = DataSourceFactory.createDataSource(information);

        if (dataSource == null) {
            handleException("DataSource cannot be created or" +
                    " found for DataSource Information " + information);
        }

        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            if (connection != null) {
                String validationQuery = information.getValidationQuery();
                if (validationQuery != null && !"".equals(validationQuery)) {
                    PreparedStatement ps = null;
                    try {
                        ps = connection.prepareStatement(validationQuery.trim());
                        if (ps != null) {
                            ps.execute();
                            ps.close();
                        }
                    } catch (SQLException e) {
                        handleException("Error during executing validation query : " +
                                e.getMessage(), e);
                    } finally {
                        if (ps != null) {
                            try {
                                ps.close();
                            } catch (SQLException ignored) {
                            }
                        }
                    }
                }
                connection.close();
            }
        } catch (SQLException e) {
            handleException("Error during executing validation query : " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignored) {

                }
            }
        }

        return true;
    }

    private String getResourceName(String path) {
        if (path != null) {
            String correctedPath = path;
            if (path.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                correctedPath = path.substring(0,
                        path.lastIndexOf(RegistryConstants.PATH_SEPARATOR));
            }
            if (correctedPath.indexOf(RegistryConstants.PATH_SEPARATOR) < 0) {
                return correctedPath;
            } else {
                return correctedPath.substring(
                        correctedPath.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1,
                        correctedPath.length());
            }
        }
        return "";
    }

    private static void handleException(String msg, Throwable throwable) {
        log.error(msg, throwable);
        throw new RuntimeException(msg, throwable);
    }

    private static void handleException(String msg) {
        log.error(msg);
        throw new RuntimeException(msg);
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public DataSourceInformationRepository getRepository() {
        return repository;
    }

    public void setRepository(DataSourceInformationRepository repository) {
        this.repository = repository;
    }

    public List<String> getInactiveDataSourceList() {
        return this.inactiveDataSourceList;
    }

    public void setInactiveDataSourceList(List<String> inactiveDataSourceList) {
         this.inactiveDataSourceList.addAll(inactiveDataSourceList);
    }
}
