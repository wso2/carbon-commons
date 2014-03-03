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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.datasource.DataSourceConstants;
import org.apache.synapse.commons.datasource.DataSourceInformation;
import org.wso2.securevault.secret.SecretInformation;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 *
 */
public class DataSourceManagementHelper {

    private static final Log log = LogFactory.getLog(DataSourceManagementHelper.class);

    /**
     * Factory method to create a DataSourceInformation from HttpServletResuet
     *
     * @param request HttpServletRequest instance
     * @return A DataSourceInformation
     * @throws javax.servlet.ServletException Throws for any error during DataSourceInformation creation
     */
    public static DataSourceInformation createDataSourceInformation(
            HttpServletRequest request) throws ServletException {

        ResourceBundle bundle = ResourceBundle.
                getBundle("org.wso2.carbon.datasource.ui.i18n.Resources",
                        request.getLocale());
        String alias = request.getParameter("alias");
        if (alias == null || "".equals(alias)) {
            alias = request.getParameter("alias_hidden");
            if (alias == null || "".equals(alias)) {
                handleException(bundle.getString("ds.name.cannotfound.msg"));
            }
        }

        String diver = request.getParameter("driver");
        if (diver == null || "".equals(diver)) {
            handleException(bundle.getString("ds.driver.cannotfound.msg"));
        }

        String url = request.getParameter("url");
        if (url == null || "".equals(url)) {
            handleException(bundle.getString("ds.url.cannotfound.msg"));
        }
        DataSourceInformation dataSourceInformation = new DataSourceInformation();

        dataSourceInformation.setAlias(alias.trim());
        dataSourceInformation.setDatasourceName(alias.trim());
        dataSourceInformation.setDriver(diver.trim());
        dataSourceInformation.setUrl(url.trim());

        String user = request.getParameter("user");
        if (user != null && !"".equals(user)) {
            SecretInformation secretInfo;
            if (dataSourceInformation.getSecretInformation() == null) {
                secretInfo = new SecretInformation();
            } else {
                secretInfo = dataSourceInformation.getSecretInformation();
            }
            secretInfo.setUser(user.trim());
            dataSourceInformation.setSecretInformation(secretInfo);
        }
        String password = request.getParameter("password");
        if (password != null && !"".equals(password)) {
            SecretInformation secretInfo;
            if (dataSourceInformation.getSecretInformation() == null) {
                secretInfo = new SecretInformation();
            } else {
                secretInfo = dataSourceInformation.getSecretInformation();
            }
            secretInfo.setAliasSecret(password.trim());
            dataSourceInformation.setSecretInformation(secretInfo);
        }
        String dstype = request.getParameter("dstype");
        if ("peruserds".equals(dstype)) {
            dataSourceInformation.setType("PerUserPoolDataSource");
        }
//        String dsName = request.getParameter("dsName");
//        if (dsName != null && !"".equals(dsName)) {
//            dataSourceInformation.setDatasourceName(dsName.trim());
//        }
        String dsrepotype = request.getParameter("dsrepotype");
        if ("JNDI".equals(dsrepotype)) {
            dataSourceInformation.setRepositoryType(DataSourceConstants.PROP_REGISTRY_JNDI);
            StringBuffer buffer = new StringBuffer();
            buffer.append(DataSourceConstants.PROP_SYNAPSE_PREFIX_DS);
            buffer.append(DataSourceConstants.DOT_STRING);

            buffer.append(alias.trim());
            buffer.append(DataSourceConstants.DOT_STRING);
            // The prefix for root level jndiProperties
            String rootPrefix = buffer.toString();
            // setting naming provider
            Properties jndiEvn = new Properties();

            String icFactory = request.getParameter("icFactory");
            String providerUrl = request.getParameter("providerUrl");
            String providerPort = request.getParameter("providerPort");
            String providerType = request.getParameter("providerType");
            if (icFactory != null && !"".equals(icFactory)) {
                jndiEvn.setProperty(rootPrefix + DataSourceConstants.PROP_IC_FACTORY,
                        icFactory.trim());
            }
            if ("url".equals(providerType)) {
                if (providerUrl != null && !"".equals(providerUrl)) {
                    jndiEvn.setProperty(rootPrefix +
                            DataSourceConstants.PROP_PROVIDER_URL, providerUrl.trim());
                }
            } else {
                if (providerPort != null && !"".equals(providerPort)) {
                    jndiEvn.setProperty(rootPrefix +
                            DataSourceConstants.PROP_PROVIDER_PORT, providerPort.trim());
                }
            }
            dataSourceInformation.setProperties(jndiEvn);
        }
        String autocommit = request.getParameter("autocommit");
        if (autocommit != null && !"".equals(autocommit)) {
            dataSourceInformation.setDefaultAutoCommit(Boolean.parseBoolean(autocommit.trim()));
        }
        String isolation = request.getParameter("isolation");
        if (isolation != null && !"".equals(isolation)) {
            if ("TRANSACTION_NONE".equals(isolation)) {
                dataSourceInformation.setDefaultTransactionIsolation(Connection.TRANSACTION_NONE);
            } else if ("TRANSACTION_READ_COMMITTED".equals(isolation.trim())) {
                dataSourceInformation.setDefaultTransactionIsolation(
                        Connection.TRANSACTION_READ_COMMITTED);
            } else if ("TRANSACTION_READ_UNCOMMITTED".equals(isolation.trim())) {
                dataSourceInformation.setDefaultTransactionIsolation(
                        Connection.TRANSACTION_READ_UNCOMMITTED);
            } else if ("TRANSACTION_REPEATABLE_READ".equals(isolation.trim())) {
                dataSourceInformation.setDefaultTransactionIsolation(
                        Connection.TRANSACTION_REPEATABLE_READ);
            } else if ("TRANSACTION_SERIALIZABLE".equals(isolation.trim())) {
                dataSourceInformation.setDefaultTransactionIsolation(
                        Connection.TRANSACTION_SERIALIZABLE);
            }
        }
        String maxActive = request.getParameter("maxActive");
        if (maxActive != null && !"".equals(maxActive) && !maxActive.contains("int")) {
            try {
                dataSourceInformation.setMaxActive(Integer.parseInt(maxActive.trim()));
            } catch (NumberFormatException e) {
                handleException(bundle.getString("invalid.maxActive"));
            }
        }
        String maxIdle = request.getParameter("maxIdle");
        if (maxIdle != null && !"".equals(maxIdle) && !maxIdle.contains("int")) {
            try {
                dataSourceInformation.setMaxIdle(Integer.parseInt(maxIdle.trim()));
            } catch (NumberFormatException e) {
                handleException(bundle.getString("invalid.maxidle"));
            }
        }
        String maxopenstatements = request.getParameter("maxopenstatements");
        if (maxopenstatements != null && !"".equals(maxopenstatements) &&
                !maxopenstatements.contains("int")) {
            try {
                dataSourceInformation.setMaxOpenPreparedStatements(
                        Integer.parseInt(maxopenstatements.trim()));
            } catch (NumberFormatException e) {
                handleException(bundle.getString("invalid.MaxOpenStatements"));
            }
        }
        String maxWait = request.getParameter("maxWait");
        if (maxWait != null && !"".equals(maxWait) && !maxWait.contains("long")) {
            try {
                dataSourceInformation.setMaxWait(Long.parseLong(maxWait.trim()));
            } catch (NumberFormatException e) {
                handleException(bundle.getString("invalid.MaxWait"));
            }
        }
        String minIdle = request.getParameter("minIdle");
        if (minIdle != null && !"".equals(minIdle) && !minIdle.contains("int")) {
            try {
                dataSourceInformation.setMinIdle(Integer.parseInt(minIdle.trim()));
            } catch (NumberFormatException e) {
                handleException(bundle.getString("invalid.MinIdle"));
            }
        }
        String initialsize = request.getParameter("initialsize");
        if (initialsize != null && !"".equals(initialsize) && !initialsize.contains("int")) {
            try {
                dataSourceInformation.setInitialSize(Integer.parseInt(initialsize.trim()));
            } catch (NumberFormatException e) {
                handleException(bundle.getString("invalid.Initialsize"));
            }
        }
        String poolstatements = request.getParameter("poolstatements");
        if (poolstatements != null && !"".equals(poolstatements)) {
            dataSourceInformation.setPoolPreparedStatements(
                    Boolean.parseBoolean(poolstatements.trim()));
        }
        String testonborrow = request.getParameter("testonborrow");
        if (testonborrow != null && !"".equals(testonborrow)) {
            dataSourceInformation.setTestOnBorrow(Boolean.parseBoolean(testonborrow.trim()));
        }
        String testwhileidle = request.getParameter("testwhileidle");
        if (testwhileidle != null && !"".equals(testwhileidle)) {
            dataSourceInformation.setTestWhileIdle(Boolean.parseBoolean(testwhileidle.trim()));
        }
        String validationquery = request.getParameter("validationquery");
        if (validationquery != null && !"".equals(validationquery)) {
            dataSourceInformation.setValidationQuery(validationquery.trim());
        }

        return dataSourceInformation;
    }

    public static String toStringIsolation(int isolation) {
        switch (isolation) {
            case Connection.TRANSACTION_NONE: {
                return "TRANSACTION_NONE";
            }
            case Connection.TRANSACTION_READ_COMMITTED: {
                return "TRANSACTION_READ_COMMITTED";
            }
            case Connection.TRANSACTION_READ_UNCOMMITTED: {
                return "TRANSACTION_READ_UNCOMMITTED";
            }
            case Connection.TRANSACTION_REPEATABLE_READ: {
                return "TRANSACTION_REPEATABLE_READ";
            }
            case Connection.TRANSACTION_SERIALIZABLE: {
                return "TRANSACTION_SERIALIZABLE";
            }
            default: {
                return "TRANSACTION_UNKNOWN";
            }
        }
    }

    public static String getProperty(Properties properties, String name, String defaultValue) {
        if (properties != null) {
            String value = properties.getProperty(name, defaultValue);
            if (value == null) {
                return "";
            }
            return value.trim();
        }
        return defaultValue;
    }

    private static void handleException(String msg) {
        log.error(msg);
        throw new IllegalArgumentException(msg);
    }
}
