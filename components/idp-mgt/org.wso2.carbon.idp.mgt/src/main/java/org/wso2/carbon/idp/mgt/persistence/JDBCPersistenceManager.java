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
package org.wso2.carbon.idp.mgt.persistence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.idp.mgt.config.ConfigParser;
import org.wso2.carbon.idp.mgt.exception.IdentityProviderMgtException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.namespace.QName;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This class is used for handling trusted IdP meta data persistence in a JDBC Store.
 * It reads the data source properties from the JNDI name given in trusted-idp-config.xml.
 * During the server start-up, it checks whether the database is created, if not it creates one.
 * This is implemented as a singleton. An instance of this class can be obtained through
 * JDBCPersistenceManager.getInstance() method.
 */
public class JDBCPersistenceManager {

    private static Log log = LogFactory.getLog(JDBCPersistenceManager.class);
    private static JDBCPersistenceManager instance;
    private DataSource dataSource;

    /**
     * Get an instance of the JDBCPersistenceManager.
     * It implements a lazy initialization with double checked locking,
     * because it is initialized first by identity.core module during the start up.
     *
     * @return JDBCPersistenceManager instance
     * @throws IdentityProviderMgtException Error when reading the data source configurations
     */
    public static JDBCPersistenceManager getInstance() throws IdentityProviderMgtException {
        if (instance == null) {
            synchronized (JDBCPersistenceManager.class) {
                if (instance == null) {
                    instance = new JDBCPersistenceManager();
                }
            }
        }
        return instance;
    }

    private JDBCPersistenceManager() throws IdentityProviderMgtException {
        initDataSource();
    }

    private void initDataSource() throws IdentityProviderMgtException {
        try {
            String dataSourceName = ConfigParser.getInstance().getConfigElement("JDBCPersistenceManager")
                    .getFirstChildWithName(new QName(ConfigParser.TRUSTED_IDP_DEFAULT_NAMESPACE, "DataSource"))
                    .getFirstChildWithName(new QName(ConfigParser.TRUSTED_IDP_DEFAULT_NAMESPACE, "Name"))
                    .getText();
            Context ctx = new InitialContext();
            dataSource = (DataSource) ctx.lookup(dataSourceName);
        }  catch (NamingException e) {
            String errorMsg = "Error when looking up the IdP-Mgt data source";
            log.error(errorMsg, e);
            throw new IdentityProviderMgtException(errorMsg);
        }  catch (Exception e) {
            String errorMsg = "Error occurred while reading " + ConfigParser.TRSUTED_IDP_CONGIG;
            log.error(errorMsg, e);
            throw new IdentityProviderMgtException(errorMsg);
        }
    }

    public void initializeDatabase() throws IdentityProviderMgtException {
        TrustedIdPDBInitializer dbInitializer = new TrustedIdPDBInitializer(dataSource);
        try {
            dbInitializer.createTrustedIdPDB();
        } catch (Exception e) {
            String errorMsg = "Error when creating the Trusted IdP metadata store";
            log.error(errorMsg, e);
            throw new IdentityProviderMgtException(errorMsg);
        }
    }

    /**
     * Returns a database connection for Teusted IdP metadata store.
     *
     * @return Database connection
     * @throws IdentityProviderMgtException Exception when getting DB connection on the data source.
     */
    public Connection getDBConnection() throws IdentityProviderMgtException {
        try {
            Connection dbConnection = dataSource.getConnection();
            dbConnection.setAutoCommit(false);
            dbConnection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            return dbConnection;
        } catch (SQLException e) {
            String errorMsg = "Error occurred while trying to get a database connection from IdP-Mgt data source";
            log.error(errorMsg, e);
            throw new IdentityProviderMgtException(errorMsg);
        }
    }

}
