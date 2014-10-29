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

import net.sf.jasperreports.engine.JRException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.core.services.DBReportingService;
import org.wso2.carbon.reporting.core.services.JrxmlFileUploader;
import org.wso2.carbon.reporting.core.services.ReportingResourcesSupplier;
import org.wso2.carbon.reporting.template.core.handler.database.DataSourceHandler;
import org.wso2.carbon.reporting.template.core.handler.metadata.ChartMetaDataHandler;
import org.wso2.carbon.reporting.template.core.handler.metadata.CompositeReportMetaDataHandler;
import org.wso2.carbon.reporting.template.core.handler.metadata.MetadataFinder;
import org.wso2.carbon.reporting.template.core.handler.metadata.TableReportMetaDataHandler;
import org.wso2.carbon.reporting.template.core.util.chart.ChartReportDTO;
import org.wso2.carbon.reporting.template.core.util.common.ReportConstants;
import org.wso2.carbon.reporting.template.core.util.table.TableReportDTO;
import org.wso2.carbon.reporting.util.Column;
import org.wso2.carbon.reporting.util.ReportDataSource;
import org.wso2.carbon.reporting.util.ReportParamMap;
import org.wso2.carbon.reporting.util.Row;

import javax.activation.DataHandler;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/*
  This class communicates with reporting core back-end
*/
public class ReportingClient {
    private JrxmlFileUploader fileUploaderService;
    private DBReportingService dbReportingService;
    private ReportingResourcesSupplier resourceSupplierService;

    private static Log log = LogFactory.getLog(ReportingClient.class);

    public ReportingClient() {
        fileUploaderService = new JrxmlFileUploader();
        dbReportingService = new DBReportingService();
        resourceSupplierService = new ReportingResourcesSupplier();
    }


    public void uploadJrxmlFile(String filename, String fileContent) throws ReportingException {
        try {
            fileUploaderService.uploadJrxmlFile(filename, fileContent);
        } catch (JRException e) {
            log.error(e.getMessage(), e);
            throw new ReportingException(e.getMessage(), e);
        } catch (ReportingException e) {
            log.error(e.getMessage(), e);
            throw e;
        }

    }

    private DataHandler generateTableReport(String reportName, String type) throws ReportingException {
        TableReportDTO tableReport = new TableReportMetaDataHandler().getTableReportMetaData(reportName);
        Map[] data = null;
        data = new DataSourceHandler().createMapDataSource(tableReport);
        ReportDataSource dataSource = getReportDataSource(data);
        ReportParamMap[] maps = new ReportParamMap[1];
        ReportParamMap map = new ReportParamMap();
        map.setParamKey("TableDataSource");
        map.setDataSource(dataSource);
        maps[0] = map;
        DataHandler dataHandler = null;
        try {
            byte[] reportResource = dbReportingService.getJRDataSourceReport(dataSource, reportName, maps, type);
            dataHandler = new DataHandler(reportResource, "application/octet-stream");
            return dataHandler;
        } catch (JRException e) {
            log.error(e.getMessage(), e);
            throw new ReportingException(e.getMessage(), e);
        }
    }


    public DataHandler generateReport(String reportName, String type) throws ReportingException {
        String reportType = MetadataFinder.findReportType(reportName);
        if (reportType != null) {
            if (reportType.equalsIgnoreCase(ReportConstants.TABLE_TYPE)) {
                return generateTableReport(reportName, type);
            }
            if (reportType.equalsIgnoreCase(ReportConstants.COMPOSITE_TYPE)) {
                return generateCompositeReport(reportName, type);
            } else {
                return generateChartReport(reportName, type);
            }
        } else {
            log.error("Couldn't find report type from the meta data for report -" + reportName);
            throw new ReportingException("Couldn't find report type from the meta data for report -" + reportName);
        }
    }


    private DataHandler generateCompositeReport(String reportName, String reportType) throws ReportingException {
        LinkedHashMap<String, String> report = new CompositeReportMetaDataHandler().getCompositeReport(reportName);
        ArrayList<ReportParamMap> mapList = new ArrayList<ReportParamMap>();

        int i = 0;
        for (String aReportName : report.keySet()) {
            String aReportType = MetadataFinder.findReportType(aReportName);
            Map[] data = null;
            if (aReportType.equalsIgnoreCase(ReportConstants.TABLE_TYPE)) {
                TableReportDTO tableReport = new TableReportMetaDataHandler().getTableReportMetaData(aReportName);
                data = new DataSourceHandler().createMapDataSource(tableReport);
            } else {
                ChartReportDTO chartReport = new ChartMetaDataHandler().getChartReportMetaData(aReportName);
                data = new DataSourceHandler().createMapDataSource(chartReport);
            }

            ReportDataSource dataSource = getReportDataSource(data);
            ReportParamMap map = new ReportParamMap();
            map.setParamKey(report.get(aReportName));
            map.setDataSource(dataSource);
            mapList.add(map);

            i++;
        }


        ReportParamMap[] maps = new ReportParamMap[mapList.size()];
        maps = mapList.toArray(maps);
        DataHandler dataHandler = null;
        try {
            byte[] data = dbReportingService.getJRDataSourceReport(null, reportName, maps, reportType);
            dataHandler = new DataHandler(data, "application/octet-stream");
            return dataHandler;
        } catch (JRException e) {
            log.error(e.getMessage(), e);
            throw new ReportingException(e.getMessage(), e);
        }

    }

    private DataHandler generateChartReport(String reportName, String type) throws ReportingException {
        ChartReportDTO chartReport = new ChartMetaDataHandler().getChartReportMetaData(reportName);
        Map[] data = null;
        data = new DataSourceHandler().createMapDataSource(chartReport);
        ReportDataSource dataSource = getReportDataSource(data);
        ReportParamMap[] maps = new ReportParamMap[1];
        ReportParamMap map = new ReportParamMap();
        map.setParamKey("TableDataSource");
        map.setDataSource(dataSource);
        maps[0] = map;
        DataHandler dataHandler = null;
        try {
            byte[] reportData = dbReportingService.getJRDataSourceReport(dataSource, reportName, maps, type);
            dataHandler = new DataHandler(reportData, "application/octet-stream");
            return dataHandler;
        } catch (JRException e) {
            log.error(e.getMessage(), e);
            throw new ReportingException(e.getMessage(), e);
        }
    }


    private ReportDataSource getReportDataSource(Map[] mapDataSource) {
        ReportDataSource dataSource = new ReportDataSource();
        ArrayList<Column> columnArrayList = new ArrayList<Column>();
        ArrayList<Row> rowArrayList = new ArrayList<Row>();
        for (Map aMapData : mapDataSource) {
            Row row = new Row();

            Iterator it = aMapData.entrySet().iterator();
            while (it.hasNext()) {
                Column column = new Column();
                Map.Entry pairs = (Map.Entry) it.next();
                column.setKey(pairs.getKey().toString());
                Object valueObj = pairs.getValue();
                if (valueObj instanceof Integer) {
                    Integer number = (Integer) valueObj;
                    column.setValue(String.valueOf(number));
                    column.setType("java.lang.Integer");
                } else if (valueObj instanceof Double) {
                    Double number = (Double) valueObj;
                    column.setValue(String.valueOf(number));
                    column.setType("java.lang.Double");
                } else {
                    column.setValue(pairs.getValue().toString());
                    column.setType("java.lang.String");
                }
                columnArrayList.add(column);
            }
            row.setColumns(columnArrayList.toArray(new Column[columnArrayList.size()]));
            rowArrayList.add(row);
        }
        dataSource.setRows(rowArrayList.toArray(new Row[rowArrayList.size()]));
        return dataSource;
    }

    public InputStream getJrxmlResource(String filename) throws ReportingException {
        String content = resourceSupplierService.getJRXMLFileContent(null, filename);
        InputStream is = new ByteArrayInputStream(content.getBytes());
        return is;
    }

    public void uploadImage(String fileName, String reportName, DataHandler imageContent) throws ReportingException {
        fileUploaderService.uploadLogo(fileName, reportName, imageContent);
    }


}
