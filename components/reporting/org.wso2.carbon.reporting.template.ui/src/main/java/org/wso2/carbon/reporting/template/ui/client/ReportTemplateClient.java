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


package org.wso2.carbon.reporting.template.ui.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.reporting.stub.ReportingResourcesSupplierStub;
import org.wso2.carbon.reporting.template.stub.ReportTemplateAdminReportingExceptionException;
import org.wso2.carbon.reporting.template.stub.ReportTemplateAdminStub;
import org.wso2.carbon.reporting.template.stub.ReportTemplateAdminStub.TableReportDTO;
import org.wso2.carbon.reporting.template.stub.ReportTemplateAdminStub.ColumnDTO;

import javax.activation.DataHandler;
import java.awt.*;
import java.rmi.RemoteException;
import java.util.ArrayList;


public class ReportTemplateClient {

    private ReportTemplateAdminStub templateAdminStub;
    private ReportingResourcesSupplierStub resourceStub;

    public ReportTemplateClient(ConfigurationContext configCtx, String backendServerURL,
                                String cookie) throws Exception {
        String serviceURL = backendServerURL + "ReportTemplateAdmin";
        templateAdminStub = new ReportTemplateAdminStub(configCtx, serviceURL);
        ServiceClient client = templateAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        String resourceServiceURL = backendServerURL + "ReportingResourcesSupplier";


        resourceStub = new ReportingResourcesSupplierStub(configCtx, resourceServiceURL);
        client = resourceStub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

    }

    public String[] getDatasourceNames() throws AxisFault {
        try {
            return templateAdminStub.getAllDatasourceNames();
        } catch (RemoteException e) {
            throw new AxisFault("Unable to get the names of datasources available");
        } catch (ReportTemplateAdminReportingExceptionException e) {
            throw new AxisFault("Unable to get the names of datasources available");
        }
    }

    public String[] getTableNames(String dsName) throws AxisFault {
        try {
            return templateAdminStub.getTableNames(dsName);
        } catch (RemoteException e) {
            throw new AxisFault("Unable to retrive the table names from data source " + dsName);
        } catch (ReportTemplateAdminReportingExceptionException e) {
            throw new AxisFault("Unable to retrive the table names from data source " + dsName);
        }
    }

    public String[] getFieldNames(String dsName, String tableName) throws AxisFault {
        try {
            return templateAdminStub.getColumnNames(dsName, tableName);
        } catch (RemoteException e) {
            throw new AxisFault("Unable to retrieve the column information of table " +
                    tableName + ", datasource " + dsName);
        } catch (ReportTemplateAdminReportingExceptionException e) {
            throw new AxisFault("Unable to retrieve the column information of table " +
                    tableName + ", datasource " + dsName);
        }
    }


    public TableReportDTO createTableDataInformation(String reportName, String dsName, String tableName,
                                                     String fieldsStr, String primaryField) {
        TableReportDTO tableReport = new TableReportDTO();
        tableReport.setReportName(reportName);
        tableReport.setDsName(dsName);

        String[] fields = fieldsStr.split(",");

        for (String field : fields) {
            field = field.trim();
            ColumnDTO column = new ColumnDTO();
            column.setColumnFamilyName(tableName);
            column.setColumnName(field);
            if (field.equalsIgnoreCase(primaryField)) {
                column.setPrimaryColumn(true);
            }
            tableReport.addColumns(column);
        }
        return tableReport;
    }

    public String[] getAvailableFontNames() {
        GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
        return e.getAvailableFontFamilyNames();
    }

    public String[] getAlignments() {
        String[] alignments = {"Left", "Center", "Right", "Justified"};
        return alignments;
    }

    public void addNewReport(TableReportDTO report)
            throws ReportTemplateAdminReportingExceptionException, RemoteException {
        templateAdminStub.addNewTableReport(report);
    }

    public void addNewReport(ReportTemplateAdminStub.ChartReportDTO report)
            throws ReportTemplateAdminReportingExceptionException, RemoteException {
        templateAdminStub.addNewChartReport(report);
    }

    public String[] getReportTypes() {
        String[] types = {"pdf", "xls", "html"};
        return types;
    }

    public DataHandler generateReport(String reportName, String format)
            throws ReportTemplateAdminReportingExceptionException, RemoteException {
        return templateAdminStub.generateReport(reportName, format);
    }

    public String isValidNumberAxis(String chartType, String dsname, String tablename, String[] fields)
            throws ReportTemplateAdminReportingExceptionException, RemoteException {
        return templateAdminStub.validateFields(chartType, dsname, tablename, fields);
    }

    public void addNewCompositeReport(String[] reports, String compositeRepName)
            throws ReportTemplateAdminReportingExceptionException, RemoteException {
        templateAdminStub.addCompositeReport(reports, compositeRepName);
    }


    public String[] getAllTemplateFiles() throws ReportTemplateAdminReportingExceptionException {
        String[] reports;
        try {
            reports = resourceStub.getAllReports();
            ArrayList<String> templateReports = new ArrayList<String>();
            for (String aReport : reports) {
                if (isReportTemplate(aReport)) {
                    if (!templateAdminStub.isCompositeReport(aReport)) {
                        templateReports.add(aReport);
                    }
                }
            }
            String[] temp = new String[templateReports.size()];
            return templateReports.toArray(temp);
        } catch (Exception e) {
            throw new ReportTemplateAdminReportingExceptionException(e.getMessage());
        }
    }

    public boolean isReportTemplate(String reportName)
            throws ReportTemplateAdminReportingExceptionException, RemoteException {
        return templateAdminStub.isReportExists(reportName);
    }


}
