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

package org.wso2.carbon.reporting.template.core.handler.database;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.template.core.client.DatasourceClient;
import org.wso2.carbon.reporting.template.core.factory.ClientFactory;
import org.wso2.carbon.reporting.template.core.util.chart.ChartReportDTO;
import org.wso2.carbon.reporting.template.core.util.chart.DataDTO;
import org.wso2.carbon.reporting.template.core.util.chart.SeriesDTO;
import org.wso2.carbon.reporting.template.core.util.table.ColumnDTO;
import org.wso2.carbon.reporting.template.core.util.table.TableReportDTO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DataSourceHandler {
   private static Log log = LogFactory.getLog(DataSourceHandler.class);


     public Map[] createMapDataSource(TableReportDTO tableReport) throws ReportingException {
         String dsName = tableReport.getDsName();
         ColumnDTO[] columns = tableReport.getColumns();

       String[] fields = getTableFields(columns);
       //assuming the report is dedicated to one
       String tableName = columns[0].getColumnFamilyName();
       String query = getQueryString(tableName, fields);

         DatasourceClient client = ClientFactory.getDSClient();
         try {
             Connection connection= client.getConnection(dsName);
             ResultSet resultSet =client.queryDatasource(query, connection);
             Map[] mapData = getMapReportTableData(resultSet, fields);
             connection.close();
             return mapData;
         } catch (SQLException e) {
             log.error("SQL error while querying the data for "+ dsName+" to fill report" + tableReport.getReportName(), e);
             throw new ReportingException("SQL error while querying the data for "+ dsName+" to fill report" + tableReport.getReportName(), e);
         }
     }

    private String[] getTableFields(ColumnDTO[] columns){
        String[] columnNames = new String[columns.length];
        int id = 0;
        for(ColumnDTO column: columns){
            columnNames[id] = column.getColumnName();
             id++;
        }
        return columnNames;
    }


     private String[] getChartFields(SeriesDTO[] series){
        ArrayList<String> fieldNames = new ArrayList<String>();
        for(SeriesDTO aSeries: series){
            DataDTO xData = aSeries.getXdata();
            if(!fieldNames.contains(xData.getDsColumnName())){
                fieldNames.add(xData.getDsColumnName());
            }

            DataDTO yData = aSeries.getYdata();
            if(!fieldNames.contains(yData.getDsColumnName())){
                fieldNames.add(yData.getDsColumnName());
            }
        }
        String[] fieldArray = new String[fieldNames.size()];
        return fieldNames.toArray(fieldArray);
    }



    private String getQueryString(String tableName, String[] columnNames){
       String query = "SELECT ";
        for(String columNames : columnNames){
            query = query +columNames+" , ";
        }

        query = query.substring(0, query.length()-3);   //to remove the last comma
        query = query + " FROM " +tableName;
        return query;

    }

    private Map[] getMapReportTableData(ResultSet resultSet, String[] columns) throws SQLException {
        ArrayList<HashMap> maps = new ArrayList<HashMap>();
      while (resultSet.next()){
       HashMap<String, String> rowData = new HashMap<String, String>();
          for(int i=0; i< columns.length; i++){
              String value = resultSet.getString(columns[i]);
              String key = String.valueOf(i+1);
              rowData.put(key, value);
          }
       maps.add(rowData);
      }
     return maps.toArray(new HashMap[maps.size()]);
    }

    private Map[] getMapReportChartData(ResultSet resultSet, SeriesDTO[] series) throws SQLException, ReportingException {
        ArrayList<HashMap> maps = new ArrayList<HashMap>();
      while (resultSet.next()){
       HashMap<String, Object> rowData = new HashMap<String, Object>();
          for(int i=0; i< series.length; i++){
              DataDTO xData = series[i].getXdata();
              String value = resultSet.getString(xData.getDsColumnName());
              String key = String.valueOf(xData.getFieldId());
              rowData.put(key, value);

              DataDTO yData = series[i].getYdata();
              value = resultSet.getString(yData.getDsColumnName());
              Number yNumber = getNumber(value);
              if(yNumber == null){
                  log.error("Y axis can be only number! It can't hold other values");
                  throw new ReportingException("Y axis can be only number! It can't hold other values");
              }
              key = String.valueOf(yData.getFieldId());
              rowData.put(key, yNumber);
          }
       maps.add(rowData);
      }
     return maps.toArray(new HashMap[maps.size()]);
    }

     private Map[] getMapReportXYChartData(ResultSet resultSet, SeriesDTO[] series) throws SQLException, ReportingException {
        ArrayList<HashMap> maps = new ArrayList<HashMap>();
      while (resultSet.next()){
       HashMap<String, Object> rowData = new HashMap<String, Object>();
          for(int i=0; i< series.length; i++){
              DataDTO xData = series[i].getXdata();
              String key = String.valueOf(xData.getFieldId());
              String value = resultSet.getString(xData.getDsColumnName());
              Number xNumber = getNumber(value);
               if(xNumber == null){
                  log.error("X axis can be only number! It can't hold other values");
                  throw new ReportingException("X axis can be only number! It can't hold other values");
              }
              rowData.put(key, xNumber);

              DataDTO yData = series[i].getYdata();
              value = resultSet.getString(yData.getDsColumnName());
              Number yNumber = getNumber(value);
              if(yNumber == null){
                  log.error("Y axis can be only number! It can't hold other values");
                  throw new ReportingException("Y axis can be only number! It can't hold other values");
              }
              key = String.valueOf(yData.getFieldId());
              rowData.put(key, yNumber);
          }
       maps.add(rowData);
      }
     return maps.toArray(new HashMap[maps.size()]);
    }

     private Number getNumber(String strNumber) {
        try {
            Integer intValue = Integer.parseInt(strNumber);
            return intValue;
        } catch (NumberFormatException ex) {
            try {
                Double doubleValue = Double.parseDouble(strNumber);
                return doubleValue;
            } catch (NumberFormatException dEx) {
                return null;
            }
        }
    }

    public Map[] createMapDataSource(ChartReportDTO chartReport) throws ReportingException {
         String dsName = chartReport.getDsName();
         SeriesDTO[] series = chartReport.getCategorySeries();

       String[] fields = getChartFields(series);

       //assuming the report is getting data from one table
       String tableName = series[0].getXdata().getDsTableName();
       String query = getQueryString(tableName, fields);

         DatasourceClient client = ClientFactory.getDSClient();
         try {
             Connection connection= client.getConnection(dsName);
             ResultSet resultSet =client.queryDatasource(query, connection);
             Map[] mapData = null;
             if(chartReport.getReportType().contains("xy")){
                mapData = getMapReportXYChartData(resultSet, series);
             }
             else{
                 mapData = getMapReportChartData(resultSet, series);
             }
             connection.close();
             return mapData;
         } catch (SQLException e) {
             throw new ReportingException("SQL error while querying the data for "+ dsName+" to fill report" + chartReport.getReportName());
         }
     }


}
