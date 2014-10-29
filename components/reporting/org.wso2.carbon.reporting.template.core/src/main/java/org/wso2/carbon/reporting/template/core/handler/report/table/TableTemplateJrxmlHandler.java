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


import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.template.core.factory.ClientFactory;
import org.wso2.carbon.reporting.template.core.client.ReportingClient;
import org.wso2.carbon.reporting.template.core.handler.metadata.TableReportMetaDataHandler;
import org.wso2.carbon.reporting.template.core.handler.report.common.AbstractJrxmlHandler;
import org.wso2.carbon.reporting.template.core.util.Resource;
import org.wso2.carbon.reporting.template.core.util.table.TableReportDTO;
import org.wso2.carbon.reporting.template.core.util.Template;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.util.List;


public class TableTemplateJrxmlHandler extends AbstractJrxmlHandler {
    private TableReportDTO tableReport;

    private TableStructureHandler tableStructureHandler;
    private TableFontStyleHandler tableFontStyleHandler;

    private static Log log = LogFactory.getLog(TableTemplateJrxmlHandler.class);

    public TableTemplateJrxmlHandler(TableReportDTO tableReport) throws XMLStreamException,
            FileNotFoundException, ReportingException {
        super(tableReport, Template.DEFAULT_TABLE_TEMPLATE.getTemplateName());
        this.tableReport = tableReport;
        this.tableStructureHandler = new TableStructureHandler(tableReport, document);
        this.tableFontStyleHandler = new TableFontStyleHandler(tableReport, document);
    }

    public TableTemplateJrxmlHandler(TableReportDTO tableReport, String jrxmlFileName)
            throws XMLStreamException, FileNotFoundException, ReportingException {
        super(tableReport, jrxmlFileName);
        this.tableReport = tableReport;
        this.tableStructureHandler = new TableStructureHandler(tableReport, document);
        this.tableFontStyleHandler = new TableFontStyleHandler(tableReport, document);
    }

    public TableTemplateJrxmlHandler(String jrxmlFileName) throws XMLStreamException,
            ReportingException, FileNotFoundException {
        super(jrxmlFileName);
    }

    public void addTableReport() throws ReportingException {
        try {
            this.handleReportFormat();
            this.handleFields();
            this.handleTableBody();
            this.handleTableOutlines();
            //writeJrxmlFile(tableReport.getReportName() + ".jrxml");
            ReportingClient client = ClientFactory.getReportingClient();
            new TableReportMetaDataHandler(tableReport).updateTableReportMetaData();
            client.uploadJrxmlFile(tableReport.getReportName(), getFileContent());
            Resource logo = tableReport.getReportHeaderInformation().getLogo();
            if (logo != null && logo.getFileName() != null && !logo.getFileName().equalsIgnoreCase("")) {
                if (logo.getDataHandler() != null) {
                    client.uploadImage(logo.getFileName(), tableReport.getReportName(), logo.getDataHandler());
                } else {
                    throw new ReportingException("No data for uploaded image found...");
                }
            }
        } catch (JaxenException e) {
            throw new ReportingException(e.getMessage(), e);
        }
    }


    /*
   Actually this method should retrive the user saved jrxml and edit the content,
    but the methods are based for the default template only not for the processed template.

    */
    public void updateTableReport() throws ReportingException {
        try {
            this.handleHeaderInformation();
            this.handleBackgroundColor();
            this.handleFields();
            this.handleTableBody();
            this.handleTableOutlines();
            writeJrxmlFile(tableReport.getReportName());
            ReportingClient client = ClientFactory.getReportingClient();
            client.uploadJrxmlFile(tableReport.getReportName(), getFileContent());
        } catch (JaxenException e) {
            log.error(e);
        }
    }

    private void handleTableBody() throws JaxenException {
        this.tableStructureHandler.updateNumberOfColumns();
        this.tableFontStyleHandler.updateTableFontStyles();
        this.updateTableNames();

    }

    private void updateTableNames() throws JaxenException {
        updateTableHeaderNames();
        updateTableFooterNames();
        updateTableCellNames();
    }

    private void updateTableHeaderNames() throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:title//a:band//a:componentElement//b:table//b:column" +
                "//b:columnHeader//a:text");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        xpathExpression.addNamespace("b", "http://jasperreports.sourceforge.net/jasperreports/components");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);
        for (int i = 0; i < nodeList.size(); i++) {
            OMElement textElement = (OMElement) nodeList.get(i);
            textElement.setText("");
            OMFactory factory = document.getOMFactory();
            OMText cdataField = factory.createOMText(textElement, tableReport.getColumns()[i].getColumnHeaderName(),
                    OMText.CDATA_SECTION_NODE);
            textElement.addChild(cdataField);
        }
    }

    private void updateTableFooterNames() throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:title//a:band//a:componentElement//b:table//b:column" +
                "//b:columnFooter//a:text");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        xpathExpression.addNamespace("b", "http://jasperreports.sourceforge.net/jasperreports/components");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);
        for (int i = 0; i < nodeList.size(); i++) {
            OMElement textElement = (OMElement) nodeList.get(i);
            textElement.setText("");
            OMFactory factory = document.getOMFactory();
            OMText cdataField = factory.createOMText(textElement, tableReport.getColumns()[i].getColumnFooterName(),
                    OMText.CDATA_SECTION_NODE);
            textElement.addChild(cdataField);
        }
    }


    private void updateTableCellNames() throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:title//a:band//a:componentElement//b:table//b:column" +
                "//b:detailCell//a:textFieldExpression");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        xpathExpression.addNamespace("b", "http://jasperreports.sourceforge.net/jasperreports/components");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);

        for (int i = 0; i < nodeList.size(); i++) {
            OMElement textFieldExpression = (OMElement) nodeList.get(i);
            int fieldNo = i + 1;
            textFieldExpression.setText("");
            OMFactory factory = document.getOMFactory();
            OMText cdataField = factory.createOMText(textFieldExpression, "$F{" + fieldNo + "}",
                    OMText.CDATA_SECTION_NODE);
            textFieldExpression.addChild(cdataField);
        }
    }

    private void handleTableOutlines() throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:style//a:box//a:pen");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);
        OMElement penElement = (OMElement) nodeList.get(0);
        penElement.getAttribute(new QName("lineWidth")).setAttributeValue(String.format("%.1g%n",
                tableReport.getOutLineThickness()));
        penElement.getAttribute(new QName("lineColor")).setAttributeValue(tableReport.getOutLineColor());
    }

    private void handleFields() throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:subDataset//a:field");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);
        OMElement aField = (OMElement) nodeList.get(0);
        int reportFields = nodeList.size();
        int requiredFields = tableReport.getColumns().length;

        xpathExpression = new AXIOMXPath("//a:subDataset");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        xpathExpression.addNamespace("b", "http://jasperreports.sourceforge.net/jasperreports/components");
        nodeList = xpathExpression.selectNodes(documentElement);
        OMElement subDatasetNode = (OMElement) nodeList.get(0);

        int additionalFields = requiredFields - reportFields;
        for (int i = 0; i < additionalFields; i++) {
            OMElement anotherField = aField.cloneOMElement();
            anotherField.getAttribute(new QName("name")).setAttributeValue(String.valueOf(reportFields + i + 1));
            subDatasetNode.addChild(anotherField);
        }
    }

    private void setTableNames() throws JaxenException {
        setTableHeaderNames();
        setTableFooterNames();
    }

    private void setTableHeaderNames() throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:title//a:band//a:componentElement//b:table//b:column" +
                "//b:columnHeader//a:text");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        xpathExpression.addNamespace("b", "http://jasperreports.sourceforge.net/jasperreports/components");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);

        for (int i = 0; i < nodeList.size(); i++) {
            OMElement textElement = (OMElement) nodeList.get(i);
            tableReport.getColumns()[i].setColumnHeaderName(textElement.getText());
        }
    }

    private void setTableFooterNames() throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:title//a:band//a:componentElement//b:table//b:column" +
                "//b:columnFooter//a:text");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        xpathExpression.addNamespace("b", "http://jasperreports.sourceforge.net/jasperreports/components");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);
        String[] names = new String[nodeList.size()];

        for (int i = 0; i < nodeList.size(); i++) {
            OMElement textElement = (OMElement) nodeList.get(i);
            tableReport.getColumns()[i].setColumnFooterName(textElement.getText());
        }
    }


    public TableReportDTO createTableReportObject() {
        try {
            setHeaderInformation();
            setBackgroundColor();
            setTableBody();
            setTableOutlines();
            return tableReport;
        } catch (JaxenException ex) {
            ex.printStackTrace();
            return null;
        }
    }


    private TableReportDTO setTableBody() throws JaxenException {
        tableStructureHandler.setColumnStructure();
        tableFontStyleHandler.setTableFontStyle();
        setTableNames();
        return tableReport;
    }


    private TableReportDTO setTableOutlines() throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:style//a:box//a:pen");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);
        OMElement penElement = (OMElement) nodeList.get(0);

        tableReport.setOutLineThickness(Double.parseDouble(penElement.getAttributeValue(new QName("lineWidth"))));
        tableReport.setOutLineColor(penElement.getAttributeValue(new QName("lineColor")));

        return tableReport;
    }

}

