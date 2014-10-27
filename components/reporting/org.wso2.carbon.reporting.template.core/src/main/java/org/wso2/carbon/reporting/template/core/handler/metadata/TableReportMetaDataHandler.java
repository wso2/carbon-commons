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


package org.wso2.carbon.reporting.template.core.handler.metadata;


import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.template.core.util.common.ReportConstants;
import org.wso2.carbon.reporting.template.core.util.table.ColumnDTO;
import org.wso2.carbon.reporting.template.core.util.table.TableReportDTO;

import javax.xml.namespace.QName;
import java.util.Iterator;

/*
handles the table report meta data information
*/
public class TableReportMetaDataHandler extends AbstractMetaDataHandler {
    private static final String TABLEREPORT = "tablereport";
    private static final String COLUMN = "column";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DATA_TABLE = "table";
    private static final String COLUMN_DATA_FIELD = "field";
    private static final String PRIMARY_COLUMN = "primary";
    private static final String PRIMARY_COLUMN_TRUE = "true";
    private static final String PRIMARY_COLUMN_FALSE = "false";
    private static final String DS_NAME = "dsName";

    private OMElement reportElement;
    private TableReportDTO tableReport;
    private OMElement tableReportElement;

    private static Log log = LogFactory.getLog(TableReportMetaDataHandler.class);

    public TableReportMetaDataHandler(TableReportDTO tableReport) throws ReportingException {
        super();
        this.tableReport = tableReport;
    }

    public TableReportMetaDataHandler() throws ReportingException {
        super();
    }

    public void updateTableReportMetaData() throws ReportingException {
        removeTableMetadata(tableReport.getReportName());
        addTableMetadata();
    }

    private void removeTableMetadata(String reportName) {
        Iterator iterator = reportsElement.getChildElements();
        boolean isTableFound = false;

        while (iterator.hasNext()) {
            OMElement reportElement = (OMElement) iterator.next();
            String reportType = reportElement.getAttributeValue(new QName(TYPE));
            if (reportType.equalsIgnoreCase(ReportConstants.TABLE_TYPE)) {
                Iterator tableIterator = reportElement.getChildElements();
                while (tableIterator.hasNext()) {
                    OMElement tableReportElement = (OMElement) tableIterator.next();
                    String tableName = tableReportElement.getAttributeValue(new QName(NAME));
                    if (tableName.equalsIgnoreCase(reportName)) {
                        reportElement.detach();
                        isTableFound = true;
                        return;
                    }
                }
            }

            if (isTableFound) break;
        }
    }

    private void addTableMetadata() throws ReportingException {
        createReportElement();
        createTableReportElement();
        ColumnDTO[] columns = tableReport.getColumns();
        int id = 0;
        for (ColumnDTO column : columns) {
            createTableReportColumnData(id, column);
            id++;
        }
        saveMetadata();
    }


    private void createReportElement() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        reportElement = fac.createOMElement(new QName(REPORT));
        reportElement.addAttribute(TYPE, ReportConstants.TABLE_TYPE, null);
        reportElement.addAttribute(DS_NAME, tableReport.getDsName(), null);
        reportsElement.addChild(reportElement);
    }


    private void createTableReportElement() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        tableReportElement = fac.createOMElement(new QName(TABLEREPORT));
        tableReportElement.addAttribute("name", tableReport.getReportName(), null);
        reportElement.addChild(tableReportElement);
    }

    private void createTableReportColumnData(int id, ColumnDTO column) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMElement columnElement = fac.createOMElement(new QName(COLUMN));

        OMElement columnId = fac.createOMElement(new QName(COLUMN_ID));
        columnId.setText(String.valueOf(id));
        columnElement.addChild(columnId);

        OMElement columnTable = fac.createOMElement(new QName(COLUMN_DATA_TABLE));
        columnTable.setText(column.getColumnFamilyName());
        columnElement.addChild(columnTable);

        OMElement columnField = fac.createOMElement(new QName(COLUMN_DATA_FIELD));
        columnField.setText(column.getColumnName());
        columnElement.addChild(columnField);

        OMElement primaryColElement = fac.createOMElement(new QName(PRIMARY_COLUMN));
        if (column.isPrimaryColumn()) {
            primaryColElement.setText(PRIMARY_COLUMN_TRUE);
        } else {
            primaryColElement.setText(PRIMARY_COLUMN_FALSE);
        }
        columnElement.addChild(primaryColElement);

        tableReportElement.addChild(columnElement);
    }

    public TableReportDTO getTableReportMetaData(String reportName) throws ReportingException {
        tableReport = new TableReportDTO();
        tableReport.setReportName(reportName);
        tableReport.setReportType(ReportConstants.TABLE_TYPE);

        tableReportElement = isTableReportMetaDataFound();
        if (tableReportElement != null) {
            tableReport.setDsName(getDsName());
            int columnCount = getColumnsCount();
            ColumnDTO[] columns = new ColumnDTO[columnCount];
            initColumns(columns);
            int id = 0;
            for (ColumnDTO column : columns) {
                setColumnProperties(column, id);
                id++;
            }
            tableReport.setColumns(columns);
            return tableReport;
        } else {
            log.error("No meta data found for the report " + reportName);
            throw new ReportingException("No meta data found for the report " + reportName);
        }
    }

    private void initColumns(ColumnDTO[] cols) {
        for (int i = 0; i < cols.length; i++) {
            cols[i] = new ColumnDTO();
        }
    }

    private OMElement isTableReportMetaDataFound() {
        Iterator iterator = reportsElement.getChildElements();
        boolean isTableFound = false;

        while (iterator.hasNext()) {
            OMElement reportElement = (OMElement) iterator.next();
            String reportType = reportElement.getAttributeValue(new QName(TYPE));
            if (reportType.equalsIgnoreCase(ReportConstants.TABLE_TYPE)) {
                Iterator tableIterator = reportElement.getChildElements();
                while (tableIterator.hasNext()) {
                    OMElement tableReportElement = (OMElement) tableIterator.next();
                    String tableName = tableReportElement.getAttributeValue(new QName(NAME));
                    if (tableName.equalsIgnoreCase(tableReport.getReportName())) {
                        return tableReportElement;
                    }
                }
            }
        }
        return null;

    }

    private String getDsName() {
        Iterator iterator = reportsElement.getChildElements();
        boolean isTableFound = false;

        while (iterator.hasNext()) {
            OMElement reportElement = (OMElement) iterator.next();
            String reportType = reportElement.getAttributeValue(new QName(TYPE));
            String dsName = reportElement.getAttributeValue(new QName(DS_NAME));
            if (reportType.equalsIgnoreCase(ReportConstants.TABLE_TYPE)) {
                Iterator tableIterator = reportElement.getChildElements();
                while (tableIterator.hasNext()) {
                    OMElement tableReportElement = (OMElement) tableIterator.next();
                    String tableName = tableReportElement.getAttributeValue(new QName(NAME));
                    if (tableName.equalsIgnoreCase(tableReport.getReportName())) {
                        return dsName;
                    }
                }
            }
        }
        return null;
    }


    private void setColumnProperties(ColumnDTO column, int colId) {
        OMElement columnElement = getColumnElement(colId);

        Iterator colTableIter = columnElement.getChildrenWithName(new QName(COLUMN_DATA_TABLE));
        OMElement colTable = (OMElement) colTableIter.next();
        column.setColumnFamilyName(colTable.getText());

        Iterator colFieldIter = columnElement.getChildrenWithName(new QName(COLUMN_DATA_FIELD));
        OMElement colField = (OMElement) colFieldIter.next();
        column.setColumnName(colField.getText());

        Iterator primaryIter = columnElement.getChildrenWithName(new QName(PRIMARY_COLUMN));
        OMElement primaryEle = (OMElement) primaryIter.next();
        if (primaryEle.getText().equalsIgnoreCase(PRIMARY_COLUMN_TRUE)) column.setPrimaryColumn(true);
        else column.setPrimaryColumn(false);
    }


    private OMElement getColumnElement(int id) {
        Iterator iterator = tableReportElement.getChildrenWithName(new QName(COLUMN));
        OMElement columnElement = null;

        while (iterator.hasNext()) {
            columnElement = (OMElement) iterator.next();
            Iterator colIdIter = columnElement.getChildrenWithName(new QName(COLUMN_ID));
            OMElement colId = (OMElement) colIdIter.next();
            if (Integer.parseInt(colId.getText()) == id) {
                return columnElement;
            }
        }
        return null;
    }


    private int getColumnsCount() {
        Iterator iterator = tableReportElement.getChildElements();
        int count = 0;
        while (iterator.hasNext()) {
            count++;
            iterator.next();
        }
        return count;
    }

    public void removeMetaData(String reportName) throws ReportingException {
        removeTableMetadata(reportName);
        saveMetadata();
    }
}
