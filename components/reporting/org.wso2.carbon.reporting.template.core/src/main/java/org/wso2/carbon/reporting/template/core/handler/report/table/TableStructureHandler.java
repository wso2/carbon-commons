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

package org.wso2.carbon.reporting.template.core.handler.report.table;

import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.reporting.template.core.util.common.FontStyleDTO;
import org.wso2.carbon.reporting.template.core.util.table.ColumnDTO;
import org.wso2.carbon.reporting.template.core.util.table.TableReportDTO;

import javax.xml.namespace.QName;
import java.util.List;


public class TableStructureHandler {
    private TableReportDTO tableReport;
    private OMDocument document;

    public TableStructureHandler(TableReportDTO tableReport, OMDocument document) {
        this.tableReport = tableReport;
        this.document = document;
    }

    public void updateNumberOfColumns() throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:title//a:band//a:componentElement" +
                "//b:table//b:column");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        xpathExpression.addNamespace("b", "http://jasperreports.sourceforge.net/jasperreports/components");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);
        int noOFColumns = tableReport.getColumns().length;
        if (noOFColumns != nodeList.size()) {
            int additionalColumns = noOFColumns - nodeList.size();
            addColumn(additionalColumns);
        }
    }

    private void addColumn(int addColumns) throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:title//a:band//a:componentElement" +
                "//b:table//b:column");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        xpathExpression.addNamespace("b", "http://jasperreports.sourceforge.net/jasperreports/components");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);
        OMElement aColumn = (OMElement) nodeList.get(0);

        xpathExpression = new AXIOMXPath("//a:title//a:band//a:componentElement//b:table");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        xpathExpression.addNamespace("b", "http://jasperreports.sourceforge.net/jasperreports/components");
        nodeList = xpathExpression.selectNodes(documentElement);
        OMElement table = (OMElement) nodeList.get(0);

        for (int i = 0; i < addColumns; i++) {
            OMElement anotherColumn = aColumn.cloneOMElement();
            table.addChild(anotherColumn);
        }

        this.updateColumnPositions();
    }

    private void updateColumnPositions() throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:title//a:band//a:componentElement" +
                "//b:table//b:column");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        xpathExpression.addNamespace("b", "http://jasperreports.sourceforge.net/jasperreports/components");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);

        int noColumns = nodeList.size();
        int aColumnWidth = Math.round(555 / noColumns);
        int lastColumnWdith = 555 - (aColumnWidth * (noColumns - 1));

        for (int i = 0; i < noColumns; i++) {
            OMElement column = (OMElement) nodeList.get(i);
            if (i != noColumns - 1) {
                column.getAttribute(new QName("width")).setAttributeValue(String.valueOf(aColumnWidth));
            } else {
                column.getAttribute(new QName("width")).setAttributeValue(String.valueOf(lastColumnWdith));
            }
        }

        AXIOMXPath reportExpression = new AXIOMXPath("//a:title//a:band//a:componentElement" +
                "//b:table//b:column//a:reportElement");
        reportExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        reportExpression.addNamespace("b", "http://jasperreports.sourceforge.net/jasperreports/components");
        List reportElementList = reportExpression.selectNodes(documentElement);

        for (int i = 0; i < reportElementList.size(); i++) {
            OMElement reportElement = (OMElement) reportElementList.get(i);
            if (i < reportElementList.size() - 3) {
                reportElement.getAttribute(new QName("width")).
                        setAttributeValue(String.valueOf(aColumnWidth));
            } else {
                reportElement.getAttribute(new QName("width")).
                        setAttributeValue(String.valueOf(lastColumnWdith));
            }
        }
    }

    public TableReportDTO setColumnStructure() throws JaxenException {
        setNumberColumns();
        return tableReport;
    }

    private void setNumberColumns() throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:title//a:band//a:componentElement" +
                "//b:table//b:column");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        xpathExpression.addNamespace("b", "http://jasperreports.sourceforge.net/jasperreports/components");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);
        int numberCols = nodeList.size();

        ColumnDTO[] columns = new ColumnDTO[numberCols];
        for (int i = 0; i < numberCols; i++) {
            ColumnDTO aColumn = new ColumnDTO();
            FontStyleDTO aColumnHeader = new FontStyleDTO();
            FontStyleDTO aColumnFooter = new FontStyleDTO();
            FontStyleDTO aColumnCell = new FontStyleDTO();
            aColumn.setColumHeader(aColumnHeader);
            aColumn.setColumnFooter(aColumnFooter);
            aColumn.setTableCell(aColumnCell);
            columns[i] = aColumn;
        }
        tableReport.setColumns(columns);
    }

}
