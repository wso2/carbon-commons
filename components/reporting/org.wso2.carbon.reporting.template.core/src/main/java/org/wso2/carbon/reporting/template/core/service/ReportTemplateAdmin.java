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


package org.wso2.carbon.reporting.template.core.service;


import org.jaxen.JaxenException;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.template.core.client.DatasourceClient;
import org.wso2.carbon.reporting.template.core.client.ReportingClient;
import org.wso2.carbon.reporting.template.core.factory.ClientFactory;
import org.wso2.carbon.reporting.template.core.handler.metadata.ChartMetaDataHandler;
import org.wso2.carbon.reporting.template.core.handler.metadata.CompositeReportMetaDataHandler;
import org.wso2.carbon.reporting.template.core.handler.metadata.MetadataFinder;
import org.wso2.carbon.reporting.template.core.handler.metadata.TableReportMetaDataHandler;
import org.wso2.carbon.reporting.template.core.handler.report.chart.*;
import org.wso2.carbon.reporting.template.core.handler.report.common.CompositeReportJrxmlHandler;
import org.wso2.carbon.reporting.template.core.handler.report.table.TableTemplateJrxmlHandler;
import org.wso2.carbon.reporting.template.core.util.chart.ChartReportDTO;
import org.wso2.carbon.reporting.template.core.util.common.ReportConstants;
import org.wso2.carbon.reporting.template.core.util.table.TableReportDTO;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.sql.SQLException;


public class ReportTemplateAdmin extends AbstractAdmin {


    public void addNewTableReport(TableReportDTO tableReport) throws ReportingException {
        try {
            new TableTemplateJrxmlHandler(tableReport).addTableReport();
        } catch (XMLStreamException e) {
            throw new ReportingException(e.getMessage(), e);
        } catch (FileNotFoundException e) {
            throw new ReportingException(e.getMessage(), e);
        }
    }

    public void addNewChartReport(ChartReportDTO chartReport) throws ReportingException {
        try {
            if (chartReport.getReportType().equalsIgnoreCase(ReportConstants.BAR_CHART_TYPE))
                new BarChartJrxmlHandler(chartReport).addBarChartReport();
            else if (chartReport.getReportType().equalsIgnoreCase(ReportConstants.LINE_CHART_TYPE)) {
                new LineChartJrxmlHandler(chartReport).addLineChartReport();
            } else if (chartReport.getReportType().equalsIgnoreCase(ReportConstants.AREA_CHART_TYPE)) {
                new AreaChartJrxmlHandler(chartReport).addAreaChartReport();
            } else if (chartReport.getReportType().equalsIgnoreCase(ReportConstants.STACKED_BAR_CHART_TYPE)) {
                new StackedBarChartJrxmlHandler(chartReport).addStackedBarChartReport();
            } else if (chartReport.getReportType().equalsIgnoreCase(ReportConstants.STACKED_AREA_CHART_TYPE)) {
                new StackedAreaChartJrxmlHandler(chartReport).addStackedAreaChartReport();
            } else if (chartReport.getReportType().equalsIgnoreCase(ReportConstants.XY_BAR_CHART_TYPE)) {
                new XYBarChartJrxmlHandler(chartReport).addXYBarChartReport();
            } else if (chartReport.getReportType().equalsIgnoreCase(ReportConstants.XY_LINE_CHART_TYPE)) {
                new XYLineChartJrxmlHandler(chartReport).addXYLineChartReport();
            } else if (chartReport.getReportType().equalsIgnoreCase(ReportConstants.XY_AREA_CHART_TYPE)) {
                new XYAreaChartJrxmlHandler(chartReport).addXYAreaChartReport();
            } else if (chartReport.getReportType().equalsIgnoreCase(ReportConstants.PIE_CHART_TYPE)) {
                new PieChartJrxmlHandler(chartReport).addPieChartReport();
            } else {
                throw new ReportingException("Unsupported chart format report found..");
            }
        } catch (XMLStreamException e) {
            throw new ReportingException(e.getMessage(), e);
        } catch (FileNotFoundException e) {
            throw new ReportingException(e.getMessage(), e);
        }
    }

    public void updateTableReport(TableReportDTO tableReport, String jrxmlFileName) throws ReportingException {
        try {
            new TableTemplateJrxmlHandler(tableReport, jrxmlFileName).updateTableReport();
        } catch (XMLStreamException e) {
            throw new ReportingException(e.getMessage(), e);
        } catch (FileNotFoundException e) {
            throw new ReportingException(e.getMessage(), e);
        }
    }


    public TableReportDTO getTableReport(String jrxmlFileName) throws ReportingException {
        try {
            TableTemplateJrxmlHandler handler = new TableTemplateJrxmlHandler(new TableReportDTO(), jrxmlFileName);
            TableReportDTO report = handler.createTableReportObject();
            return report;
        } catch (XMLStreamException e) {
            throw new ReportingException(e.getMessage(), e);
        } catch (FileNotFoundException e) {
            throw new ReportingException(e.getMessage(), e);
        }
    }

//    public TableReportDTO getTableReportInstance() throws ReportingException {
//        return ReportTemplateFactory.getDefaultTableReportInstance();
//    }


    public DataHandler generateReport(String reportName, String type) throws ReportingException {
        ReportingClient client = ClientFactory.getReportingClient();
        return client.generateReport(reportName, type);
    }



    public String[] getAllDatasourceNames() throws ReportingException {
            DatasourceClient dsClient = ClientFactory.getDSClient();
            return dsClient.getDataSourceNames();
    }

    public String[] getTableNames(String dsName) throws ReportingException {
            DatasourceClient dsClient = ClientFactory.getDSClient();
            try {
                return dsClient.getTableNames(dsName);
            } catch (SQLException e) {
                throw new ReportingException("SQL syntax is not coorect for datasource " + dsName, e);
            }
    }

    public String[] getColumnNames(String dsName, String tableName) throws ReportingException {
            DatasourceClient dsClient = ClientFactory.getDSClient();
            try {
                return dsClient.getColumnNames(dsName, tableName);
            } catch (SQLException e) {
                throw new ReportingException("SQL syntax error while retrieving the columnames of table " + tableName + " from data source name " + dsName, e);
            }
    }

    public boolean isReportExists(String reportName) throws ReportingException {
        return MetadataFinder.isMetaDataExists(reportName);
    }

    public String validateFields(String chartType, String dsName, String tableName, String[] yAxis) throws ReportingException {
            DatasourceClient dsClient = ClientFactory.getDSClient();
            return dsClient.isNumberFields(dsName, tableName, yAxis);
    }

    public void addCompositeReport(String[] templateNames, String compositeReportName) throws ReportingException {
        try {
            new CompositeReportJrxmlHandler(templateNames, compositeReportName).addCompositeReport();
        } catch (JaxenException e) {
            throw new ReportingException(e.getMessage(), e);
        } catch (XMLStreamException e) {
             throw new ReportingException(e.getMessage(), e);
        } catch (FileNotFoundException e) {
             throw new ReportingException(e.getMessage(), e);
        }
    }

    public void deleteReport(String reportname) throws ReportingException {
       String type  = MetadataFinder.findReportType(reportname);
       if(type.equalsIgnoreCase(ReportConstants.TABLE_TYPE)){
          new TableReportMetaDataHandler().removeMetaData(reportname);
       }else if(type.equalsIgnoreCase(ReportConstants.COMPOSITE_TYPE)){
           new CompositeReportMetaDataHandler().removeMetaData(reportname);
       }else {
           new ChartMetaDataHandler().removeMetaData(reportname);
       }
    }


    public boolean isCompositeReport(String reportName) throws ReportingException {
      String type =  MetadataFinder.findReportType(reportName);
        return type.equalsIgnoreCase(ReportConstants.COMPOSITE_TYPE);
    }

}
