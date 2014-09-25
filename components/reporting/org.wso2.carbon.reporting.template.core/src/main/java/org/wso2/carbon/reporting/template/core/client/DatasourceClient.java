/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.reporting.template.core.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.CarbonDataSource;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.template.core.factory.ClientFactory;
import org.wso2.carbon.reporting.template.core.internal.ReportingTemplateComponent;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DatasourceClient {

    private static final String DATASOURCE_EXTENSION_NS =
            "http://www.wso2.org/products/wso2commons/datasource";

    private static Log log = LogFactory.getLog(DatasourceClient.class);


    /**
     * get all data source names available
     *
     * @return names of the datasources available in the server
     * @throws ReportingException will occurred if no datasources found.
     */
    public String[] getDataSourceNames() throws ReportingException {
        DataSourceService dataSourceService =
                ReportingTemplateComponent.getCarbonDataSourceService();
        ArrayList<String> dsnames = new ArrayList<String>();
        if (dataSourceService != null) {
            try {
                List<CarbonDataSource> dataSources = dataSourceService.getAllDataSources();
                for (CarbonDataSource aDataSource : dataSources) {
                    dsnames.add(aDataSource.getDSMInfo().getName());
                }
            } catch (DataSourceException e) {
                log.error(e.getMessage(), e);
                throw new ReportingException(e.getMessage(), e);
            }
            return dsnames.toArray(new String[dsnames.size()]);
        } else {
            log.error("No datasource service found");
            throw new ReportingException("No datasource service found");
        }
    }


    /**
     * get the tables exists in the data source name provided
     *
     * @param dsName name of the data source
     * @return names of the table available in the given data source
     * @throws ReportingException will occurred if error occurred while getting connection for data source
     * @throws SQLException       will occurred if any error in the SQL syntax
     */
    public String[] getTableNames(String dsName) throws ReportingException, SQLException {
        ArrayList<String> tableNames = new ArrayList<String>();
        Connection connection = getConnection(dsName);
        DatabaseMetaData metaData = connection.getMetaData();
        String[] types = {"TABLE"};

        ResultSet resultSet = metaData.getTables(null, null, "%", types);
        while (resultSet.next()) {
            tableNames.add(resultSet.getString(3));
        }
        connection.close();
        return tableNames.toArray(new String[tableNames.size()]);

    }


    /**
     * get Connection for the given datasource
     * This is to avoid having multiple connection
     *
     * @param dsName name of the data source
     * @return Connection of  given data source
     * @throws ReportingException will occurred if any datasources found.
     */
    public Connection getConnection(String dsName) throws ReportingException {
        Connection connection = null;
        DataSourceService dataSourceService =
                ReportingTemplateComponent.getCarbonDataSourceService();
        if (dataSourceService != null) {

            try {
                CarbonDataSource carbonDataSource = dataSourceService.getDataSource(dsName);
                connection = ((DataSource) carbonDataSource.getDSObject()).getConnection();
            } catch (DataSourceException e) {
                log.error(e.getMessage(), e);
                throw new ReportingException(e.getMessage(), e);
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new ReportingException("Failed to get data source connection for "
                        + "\"" + dsName + "\"", e);
            }
        }
        return connection;
    }

    public String[] getColumnNames(String dsName, String tableName) throws ReportingException, SQLException {
        Connection connection = getConnection(dsName);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName);

        ResultSetMetaData metadata = resultSet.getMetaData();
        int columnCount = metadata.getColumnCount();

        String[] columns = new String[columnCount];
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metadata.getColumnName(i);
            columns[i - 1] = columnName;
        }
        connection.close();
        return columns;
    }


    public ResultSet queryDatasource(String query, Connection connection) throws ReportingException, SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);
        return resultSet;
    }


    /**
     * retrieves the meta data about the column names in the given table and dsname
     *
     * @param dsName    name of the data source
     * @param tableName name of the table in the data source
     * @return HashMap of column_name (key) and type_of_column (value)
     * @throws ReportingException will occurred if any sql syntax problem found.
     */
    public HashMap<String, String> getMetaData(String dsName, String tableName) throws ReportingException {
        HashMap<String, String> metaData = new HashMap<String, String>();
        Connection connection = getConnection(dsName);
        try {
            DatabaseMetaData dBMetaData = connection.getMetaData();
            ResultSet resultSet = dBMetaData.getColumns(null, null, tableName, null);
            while (resultSet.next()) {
                metaData.put(resultSet.getString("COLUMN_NAME").toLowerCase(), resultSet.getString("TYPE_NAME"));
            }
            return metaData;
        } catch (SQLException e) {
            log.error("Error while retrieving the meta data of Table: " + tableName + " from DB :" + dsName);
            throw new ReportingException("Error while retrieving the meta data of Table: " + tableName + " from DB :" + dsName);
        }

    }


    /**
     * if all numeric columns then empty string will be returned else
     * error message will be returned with name of first occurance of not numeric value
     *
     * @param dsName    name of the data source
     * @param tablename name of the table in the data source
     * @param field,    array of column names of the table which supposed to checked whether it's a numeric field
     * @return HashMap of column_name (key) and type_of_column (value)
     * @throws ReportingException will occurred if any sql syntax problem found.
     */

    public String isNumberFields(String dsName, String tablename, String[] field) throws ReportingException {
        DatasourceClient client = ClientFactory.getDSClient();
        HashMap<String, String> metaData = client.getMetaData(dsName, tablename);
        for (int i = 0; i < field.length; i++) {
            String type = metaData.get(field[i].toLowerCase());

            if (!(type.equalsIgnoreCase("integer") |
                    type.equalsIgnoreCase("int") |
                    type.equalsIgnoreCase("smallint") |
                    type.equalsIgnoreCase("numeric") |
                    type.equalsIgnoreCase("decimal") |
                    type.equalsIgnoreCase("real") |
                    type.equalsIgnoreCase("double precision") |
                    type.equalsIgnoreCase("float"))) {
                return "Field - " + field[i] + " is not a numeric value field.";

            }
        }
        return "";
    }


}
