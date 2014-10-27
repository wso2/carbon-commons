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


package org.wso2.carbon.reporting.template.core.handler.report.common;

import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.template.core.factory.ClientFactory;
import org.wso2.carbon.reporting.template.core.internal.ReportingTemplateComponent;
import org.wso2.carbon.reporting.template.core.util.*;
import org.wso2.carbon.reporting.template.core.util.common.*;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import java.io.*;
import java.util.Iterator;
import java.util.List;


/*
This class handles the common information about all the default report templates.
The Header information customization, back ground customization, font customization, etc.
If any new Jrxml Handler is going to be implemented in addition for the default templates
it's expected to extend this class.

-------Report---------
|                    |
|  Header            | --> Handled by Abstract Jrxml handler
|--------------------|
|                    |
|  Body              | --> Specific Jrxml Handlers
|                    |
|                    |
|--------------------|


*/


public abstract class AbstractJrxmlHandler {
    private Report report;
    protected OMDocument document;

    public AbstractJrxmlHandler(Report report, String jrxmlFileName) throws XMLStreamException,
            FileNotFoundException, ReportingException {
        this.report = report;
        createXMLObjectModel(jrxmlFileName);
    }

    public AbstractJrxmlHandler(String reportName) throws ReportingException, XMLStreamException,
            FileNotFoundException {
        retrieveXMLObject(reportName);
    }

    private void createXMLObjectModel(String jrxmlFile) throws FileNotFoundException,
        XMLStreamException, ReportingException {
        XMLInputFactory xif = XMLInputFactory.newInstance();
        xif.setProperty("javax.xml.stream.isCoalescing", true);
        InputStream jrxmlInputStream = getJrxmlFileContent(jrxmlFile);
        XMLStreamReader reader = xif.createXMLStreamReader(jrxmlInputStream);

        StAXOMBuilder builder = new StAXOMBuilder(reader);

        document = builder.getDocument();
    }

    protected OMElement getComponentElement(String componentName) throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:title//a:band//a:" + componentName);
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);
        OMElement element = (OMElement) nodeList.get(0);
        return element;

    }

    protected OMElement getSubDataSetElement() throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:subDataset");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);
        OMElement element = (OMElement) nodeList.get(0);
        return element;
    }


    private void retrieveXMLObject(String jrxmlFile) throws FileNotFoundException,
            XMLStreamException, ReportingException {
        XMLInputFactory xif = XMLInputFactory.newInstance();

        InputStream jrxmlInputStream = getJrxmlFileContent(jrxmlFile);
        XMLStreamReader reader = xif.createXMLStreamReader(jrxmlInputStream);
        xif.setProperty("javax.xml.stream.isCoalescing", false);

        StAXOMBuilder builder = new StAXOMBuilder(reader);

        document = builder.getDocument();
    }

    private InputStream getJrxmlFileContent(String jrxmlFileName) throws ReportingException {

        for (Template aTemplate : Template.values()) {
            if (jrxmlFileName.equalsIgnoreCase(aTemplate.getTemplateName())) {
                return getTemplateResource(aTemplate.getTemplateName());
            }
        }
        return ClientFactory.getReportingClient().getJrxmlResource(jrxmlFileName);
    }

    public OMDocument getOMDocument() {
        return document;
    }


    private InputStream getTemplateResource(String templateName) throws ReportingException {
        String localPath = "templates/";
        String jrXmlPath = ReportConstants.REPORT_BASE_PATH + localPath + templateName + ".jrxml";

        Registry registry = null;
        try {
            registry = ReportingTemplateComponent.getRegistryService().getConfigSystemRegistry();
        } catch (RegistryException e) {
            throw new ReportingException(e.getMessage(), e);
        }
        Resource resource;
        InputStream reportDefinitionOmStream;
        try {
            resource = registry.get(jrXmlPath);
            reportDefinitionOmStream = resource.getContentStream();
            return reportDefinitionOmStream;
        } catch (RegistryException e) {
            throw new ReportingException(templateName + " getting  failed from registry", e);
        }
    }

    protected void handleHeaderInformation() throws JaxenException {
        boolean isImageExists = handleLogoImage();
        handleTitle(isImageExists);
        handleReportName();
    }

    private void handleReportName() {
        OMElement documentElement = document.getOMDocumentElement();
        documentElement.getAttribute(new QName("name")).setAttributeValue(report.getReportName());
    }

    private void handleTitle(boolean isImageExists) throws JaxenException {

        ReportHeaderInformationDTO reportHeader = report.getReportHeaderInformation();
        FontStyleDTO titleStyle = reportHeader.getTitleFont();
        OMElement documentElement = document.getOMDocumentElement();

        Iterator iterator = documentElement.getChildrenWithLocalName("style");

        while (iterator.hasNext()) {
            OMElement element = (OMElement) iterator.next();
            if (element.getAttributeValue(new QName("name")).equalsIgnoreCase("Title")) {
                element.getAttribute(new QName("forecolor")).
                        setAttributeValue(titleStyle.getFontColor());
                element.getAttribute(new QName("backcolor")).
                        setAttributeValue(titleStyle.getBackgroundColour());
                element.getAttribute(new QName("fontName")).
                        setAttributeValue(titleStyle.getFontName());
                element.getAttribute(new QName("fontSize")).
                        setAttributeValue(String.valueOf(titleStyle.getFontSize()));
                element.getAttribute(new QName("isBold")).
                        setAttributeValue(String.valueOf(titleStyle.isBold()));
                element.getAttribute(new QName("isItalic")).
                        setAttributeValue(String.valueOf(titleStyle.isItalic()));
                element.getAttribute(new QName("isUnderline")).
                        setAttributeValue(String.valueOf(titleStyle.isUnderLine()));
                element.getAttribute(new QName("isStrikeThrough")).
                        setAttributeValue(String.valueOf(titleStyle.isStrikeThough()));
                break;
            }
        }

        //Setting the alignment
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:title//a:band//a:staticText");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        OMElement textFiledNode = null;
        List nodeList = xpathExpression.selectNodes(documentElement);
        for (int i = 0; i < nodeList.size(); i++) {
            OMElement aTextFieldNode = (OMElement) nodeList.get(i);
            Iterator reportElementIter = aTextFieldNode.getChildrenWithLocalName("reportElement");
            while (reportElementIter.hasNext()) {
                String styleValue = ((OMElement) reportElementIter.next()).getAttributeValue(new QName("style"));
                if (styleValue != null && styleValue.equalsIgnoreCase("Title")) {
                    textFiledNode = aTextFieldNode;
                    i = nodeList.size();
                    break;
                }
            }
        }

        Iterator textElements = textFiledNode.getChildrenWithLocalName("textElement");
        while (textElements.hasNext()) {
            ((OMElement) textElements.next()).getAttribute(new QName("textAlignment")).
                    setAttributeValue(titleStyle.getAlignment());
        }

        //setting the title text
        Iterator textExp = textFiledNode.getChildrenWithLocalName("text");
        while (textExp.hasNext()) {
            OMElement textField = (OMElement) textExp.next();
            textField.setText("");
            OMFactory factory = document.getOMFactory();
            OMText cdataField = factory.createOMText(textField, reportHeader.getTitle(),
                    OMText.CDATA_SECTION_NODE);
            textField.addChild(cdataField);
        }

        if (!isImageExists) {
            xpathExpression = new AXIOMXPath("//a:title//a:band//a:staticText//a:reportElement");
            xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
            List list = xpathExpression.selectNodes(documentElement);
            OMElement element = (OMElement) list.get(0);
            element.getAttribute(new QName("width")).setAttributeValue("555");
            element.getAttribute(new QName("x")).setAttributeValue("0");
        }

    }

    private boolean handleLogoImage() throws JaxenException {

        AXIOMXPath xpathExpression = new AXIOMXPath("//a:title//a:band//a:image//a:imageExpression");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);
        OMElement element = (OMElement) nodeList.get(0);
        element.setText("");
        if (report.getReportHeaderInformation().getLogo() != null &&
                !report.getReportHeaderInformation().getLogo().
                        getFileName().equalsIgnoreCase("")) {
            OMFactory factory = document.getOMFactory();
            OMText cdataField = factory.createOMText(element, "\"" +
                    report.getReportHeaderInformation().
                            getLogo().getFileName() + "\"", OMText.CDATA_SECTION_NODE);
            element.addChild(cdataField);
        }

        xpathExpression = new AXIOMXPath("//a:title//a:band//a:image//a:hyperlinkTooltipExpression");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        documentElement = document.getOMDocumentElement();
        nodeList = xpathExpression.selectNodes(documentElement);
        element = (OMElement) nodeList.get(0);
        element.setText("");
        if (report.getReportHeaderInformation().getLogo() != null &&
                !report.getReportHeaderInformation().getLogo().
                        getFileName().equalsIgnoreCase("")) {
            OMFactory factory1 = document.getOMFactory();
            OMText cdataField1 = factory1.createOMText(element, "\"" +
                    report.getReportHeaderInformation().getLogo().
                            getFileName() + "\"", OMText.CDATA_SECTION_NODE);
            element.addChild(cdataField1);
        }

        if (report.getReportHeaderInformation().getLogo() == null ||
                report.getReportHeaderInformation().getLogo().getFileName().equalsIgnoreCase("")) {
            xpathExpression = new AXIOMXPath("//a:title//a:band//a:image");
            xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
            documentElement = document.getOMDocumentElement();
            nodeList = xpathExpression.selectNodes(documentElement);
            element = (OMElement) nodeList.get(0);
            element.detach();
            return false;
        } else {
            return true;
        }
    }


    /**
     * This method does the main processing of header information and back ground information.
     * This method sould be called in the adding new report in the specific jrxml handler
     */


    protected void handleReportFormat() throws JaxenException {
        handleHeaderInformation();
        handleBackgroundColor();
    }

    protected void handleBackgroundColor() throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:background//a:band//a:staticText//a:reportElement");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);
        OMElement element = (OMElement) nodeList.get(0);
        element.getAttribute(new QName("backcolor")).setAttributeValue(report.getBackgroundColour());
    }

    protected void setBackgroundColor() throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:background//a:band//a:staticText//a:reportElement");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);
        OMElement element = (OMElement) nodeList.get(0);
        String backColor = element.getAttributeValue(new QName("backcolor"));
        report.setBackgroundColour(backColor);
    }

    protected void setHeaderInformation() throws JaxenException {
        this.setTitle();
        this.setLogoImage();
    }


    private void setTitle() throws JaxenException {
        ReportHeaderInformationDTO reportHeaderInformation = new ReportHeaderInformationDTO();
        FontStyleDTO titleFont = new FontStyleDTO();
        OMElement documentElement = document.getOMDocumentElement();
        Iterator iterator = documentElement.getChildrenWithLocalName("style");

        while (iterator.hasNext()) {
            OMElement element = (OMElement) iterator.next();
            if (element.getAttributeValue(new QName("name")).equalsIgnoreCase("Title")) {
                titleFont.setFontColor(element.
                        getAttributeValue(new QName("forecolor")));
                titleFont.setBackgroundColour(element.
                        getAttributeValue(new QName("backcolor")));
                titleFont.setFontName(element.
                        getAttributeValue(new QName("fontName")));
                titleFont.setFontSize(Integer.parseInt(element.
                        getAttributeValue(new QName("fontSize"))));
                titleFont.setBold(Boolean.parseBoolean(element.
                        getAttributeValue(new QName("isBold"))));
                titleFont.setItalic(Boolean.parseBoolean(element.
                        getAttributeValue(new QName("isItalic"))));
                titleFont.setStrikeThough(Boolean.parseBoolean(element.
                        getAttributeValue(new QName("isStrikeThrough"))));
                titleFont.setUnderLine(Boolean.parseBoolean(element.
                        getAttributeValue(new QName("isUnderline"))));
                break;
            }
        }

        AXIOMXPath xpathExpression = new AXIOMXPath("//a:title//a:band//a:staticText");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        OMElement textFiledNode = null;
        List nodeList = xpathExpression.selectNodes(documentElement);
        for (int i = 0; i < nodeList.size(); i++) {
            OMElement aTextFieldNode = (OMElement) nodeList.get(i);
            Iterator reportElementIter = aTextFieldNode.getChildrenWithLocalName("reportElement");
            while (reportElementIter.hasNext()) {
                String styleValue = ((OMElement) reportElementIter.next()).getAttributeValue(new QName("style"));
                if (styleValue != null && styleValue.equalsIgnoreCase("Title")) {
                    textFiledNode = aTextFieldNode;
                    i = nodeList.size();
                    break;
                }
            }
        }

        Iterator textElements = textFiledNode.getChildrenWithLocalName("textElement");
        while (textElements.hasNext()) {
            String textAlignment = ((OMElement) textElements.next()).
                    getAttributeValue(new QName("textAlignment"));
            titleFont.setAlignment(textAlignment);
        }

        Iterator textExp = textFiledNode.getChildrenWithLocalName("text");
        while (textExp.hasNext()) {
            String titleText = ((OMElement) textExp.next()).getText();
            reportHeaderInformation.setTitle(titleText);
        }

        reportHeaderInformation.setTitleFont(titleFont);
        report.setReportHeaderInformation(reportHeaderInformation);
    }


    private void setLogoImage() throws JaxenException {
        ReportHeaderInformationDTO reportHeaderInformation = report.getReportHeaderInformation();
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:title//a:band//a:image//a:imageExpression");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);
        OMElement element = (OMElement) nodeList.get(0);
        String image = element.getText();

        org.wso2.carbon.reporting.template.core.util.Resource resource =
                new org.wso2.carbon.reporting.template.core.util.Resource();
        resource.setFileName(image);
        reportHeaderInformation.setLogo(resource);

    }

    protected void writeJrxmlFile(String jrxmlFilename) {
        try {
            File file = new File(jrxmlFilename + ".jrxml");
            FileWriter fstream = new FileWriter(file);
            BufferedWriter out = new BufferedWriter(fstream);
            XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
            document.serialize(writer, true);
            writer.flush();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    protected String getFileContent() {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream);
            document.serialize(writer, true);
            return outputStream.toString();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return "";
        }
    }

}
