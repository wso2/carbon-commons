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
import org.wso2.carbon.reporting.template.core.util.table.TableReportDTO;

import javax.xml.namespace.QName;
import java.util.List;


public class TableFontStyleHandler {
    private TableReportDTO tableReport;
    private OMDocument document;

    public TableFontStyleHandler(TableReportDTO tableReport, OMDocument document){
        this.tableReport = tableReport;
        this.document = document;
    }
    public void updateTableFontStyles() throws JaxenException {
        updateTableFontProperties();
        updateTableColorProperties();
        updateTableTextAlignment();
    }


    private void updateTableFontProperties() throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:title//a:band//a:componentElement" +
                "//b:table//b:column//a:font");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        xpathExpression.addNamespace("b", "http://jasperreports.sourceforge.net/jasperreports/components");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);
        int colNo = 0;
        for (int i = 0; i < nodeList.size(); i++) {
            OMElement fontElement = (OMElement) nodeList.get(i);
            updateFontAttributes(fontElement, tableReport.getColumns()[colNo].getColumHeader());

            i++;
            fontElement = (OMElement) nodeList.get(i);
            updateFontAttributes(fontElement, tableReport.getColumns()[colNo].getColumnFooter());

            i++;
            fontElement = (OMElement) nodeList.get(i);
            updateFontAttributes(fontElement, tableReport.getColumns()[colNo].getTableCell());

            colNo++;
        }
    }

    private void updateTableColorProperties() throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:title//a:band//a:componentElement" +
                "//b:table//b:column//a:reportElement");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        xpathExpression.addNamespace("b", "http://jasperreports.sourceforge.net/jasperreports/components");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);
        int colNo = 0;
        for (int i = 0; i < nodeList.size(); i++) {
            OMElement reportElement = (OMElement) nodeList.get(i);
            reportElement.getAttribute(new QName("forecolor")).setAttributeValue(tableReport.getColumns()[colNo].
                    getColumHeader().getFontColor());
            reportElement.getAttribute(new QName("backcolor")).setAttributeValue(tableReport.getColumns()[colNo].
                    getColumHeader().getBackgroundColour());

            i++;
            reportElement = (OMElement) nodeList.get(i);
            reportElement.getAttribute(new QName("forecolor")).setAttributeValue(tableReport.getColumns()[colNo].
                    getColumnFooter().getFontColor());
            reportElement.getAttribute(new QName("backcolor")).setAttributeValue(tableReport.getColumns()[colNo].
                    getColumnFooter().getBackgroundColour());

            i++;
            reportElement = (OMElement) nodeList.get(i);
            reportElement.getAttribute(new QName("forecolor")).setAttributeValue(tableReport.getColumns()[colNo].
                    getTableCell().getFontColor());
            reportElement.getAttribute(new QName("backcolor")).setAttributeValue(tableReport.getColumns()[colNo].
                    getTableCell().getBackgroundColour());

            colNo++;
        }
    }


    private void updateTableTextAlignment() throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:title//a:band//a:componentElement//b:table//b:column" +
                "//a:textElement");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        xpathExpression.addNamespace("b", "http://jasperreports.sourceforge.net/jasperreports/components");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);
        int colNo = 0;
        for (int i = 0; i < nodeList.size(); i++) {
            OMElement textElement = (OMElement) nodeList.get(i);
            textElement.getAttribute(new QName("textAlignment")).setAttributeValue(tableReport.getColumns()[colNo].
                    getColumHeader().getAlignment());

            i++;
            textElement = (OMElement) nodeList.get(i);
            textElement.getAttribute(new QName("textAlignment")).setAttributeValue(tableReport.getColumns()[colNo].
                    getColumnFooter().getAlignment());

            i++;
            textElement = (OMElement) nodeList.get(i);
            textElement.getAttribute(new QName("textAlignment")).setAttributeValue(tableReport.getColumns()[colNo].
                    getTableCell().getAlignment());

            colNo++;
        }
    }

    private void updateFontAttributes(OMElement fontElement, FontStyleDTO style) {
        fontElement.getAttribute(new QName("fontName")).
                setAttributeValue(style.getFontName());
        fontElement.getAttribute(new QName("size")).
                setAttributeValue(String.valueOf(style.getFontSize()));
        fontElement.getAttribute(new QName("isBold")).
                setAttributeValue(String.valueOf(style.isBold()));
        fontElement.getAttribute(new QName("isItalic")).
                setAttributeValue(String.valueOf(style.isItalic()));
        fontElement.getAttribute(new QName("isUnderline")).
                setAttributeValue(String.valueOf(style.isUnderLine()));
        fontElement.getAttribute(new QName("isStrikeThrough")).
                setAttributeValue(String.valueOf(style.isStrikeThough()));
    }

     public TableReportDTO setTableFontStyle() throws JaxenException {
        setTableFontProperties();
        setTableColorProperties();
        setTableTextAlignment();
        return tableReport;
     }

    private void setTableFontProperties() throws JaxenException {
       AXIOMXPath xpathExpression = new AXIOMXPath("//a:title//a:band//a:componentElement//b:table" +
               "//b:column//a:font");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        xpathExpression.addNamespace("b", "http://jasperreports.sourceforge.net/jasperreports/components");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);
        int colNo = 0;
        for (int i = 0; i < nodeList.size(); i++) {
            OMElement fontElement = (OMElement) nodeList.get(i);
            setFontAttributes(fontElement, tableReport.getColumns()[colNo].getColumHeader());

            i++;
            fontElement = (OMElement) nodeList.get(i);
            setFontAttributes(fontElement, tableReport.getColumns()[colNo].getColumnFooter());

            i++;
            fontElement = (OMElement) nodeList.get(i);
            setFontAttributes(fontElement, tableReport.getColumns()[colNo].getTableCell());

            colNo++;
        }
    }

    private void setFontAttributes(OMElement fontElement, FontStyleDTO style){
        style.setFontName(fontElement.getAttributeValue(new QName("fontName")));
        style.setFontSize(Integer.parseInt(fontElement.
                getAttributeValue(new QName("size"))));
        style.setBold(Boolean.parseBoolean(fontElement.
                getAttributeValue(new QName("isBold"))));
        style.setItalic(Boolean.parseBoolean(fontElement.
                getAttributeValue(new QName("isItalic"))));
        style.setUnderLine(Boolean.parseBoolean(fontElement.
                getAttributeValue(new QName("isUnderline"))));
        style.setStrikeThough(Boolean.parseBoolean(fontElement.
                getAttributeValue(new QName("isStrikeThrough"))));
    }

    private void setTableColorProperties() throws JaxenException {
       AXIOMXPath xpathExpression = new AXIOMXPath("//a:title//a:band//a:componentElement//b:table" +
               "//b:column//a:reportElement");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        xpathExpression.addNamespace("b", "http://jasperreports.sourceforge.net/jasperreports/components");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);
        int colNo = 0;
        for (int i = 0; i < nodeList.size(); i++) {
            OMElement reportElement = (OMElement) nodeList.get(i);
            tableReport.getColumns()[colNo].getColumHeader().setFontColor(reportElement.
                    getAttributeValue(new QName("forecolor")));
            tableReport.getColumns()[colNo].getColumHeader().setBackgroundColour(reportElement.
                    getAttributeValue(new QName("backcolor")));

            i++;
            reportElement = (OMElement) nodeList.get(i);
            tableReport.getColumns()[colNo].getColumnFooter().setFontColor(reportElement.
                    getAttributeValue(new QName("forecolor")));
            tableReport.getColumns()[colNo].getColumnFooter().setBackgroundColour(reportElement.
                    getAttributeValue(new QName("backcolor")));

            i++;
            reportElement = (OMElement) nodeList.get(i);
            tableReport.getColumns()[colNo].getTableCell().setFontColor(reportElement.
                    getAttributeValue(new QName("forecolor")));
            tableReport.getColumns()[colNo].getTableCell().setBackgroundColour(reportElement.
                    getAttributeValue(new QName("backcolor")));

            colNo++;
        }
    }

private void setTableTextAlignment() throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:title//a:band//a:componentElement//b:table" +
                "//b:column//a:textElement");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        xpathExpression.addNamespace("b", "http://jasperreports.sourceforge.net/jasperreports/components");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);
        int colNo = 0;
        for (int i = 0; i < nodeList.size(); i++) {
            OMElement textElement = (OMElement) nodeList.get(i);
            tableReport.getColumns()[colNo].getColumHeader().setAlignment(textElement.
                    getAttributeValue(new QName("textAlignment")));

            i++;
            textElement = (OMElement) nodeList.get(i);
            tableReport.getColumns()[colNo].getColumnFooter().setAlignment(textElement.
                    getAttributeValue(new QName("textAlignment")));

            i++;
            textElement = (OMElement) nodeList.get(i);
            tableReport.getColumns()[colNo].getTableCell().setAlignment(textElement.
                    getAttributeValue(new QName("textAlignment")));

            colNo++;
        }
    }

}
