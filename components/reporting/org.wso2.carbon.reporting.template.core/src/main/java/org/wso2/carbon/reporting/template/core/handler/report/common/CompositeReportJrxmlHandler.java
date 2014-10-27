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

import org.apache.axiom.om.*;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.template.core.client.ReportingClient;
import org.wso2.carbon.reporting.template.core.factory.ClientFactory;
import org.wso2.carbon.reporting.template.core.handler.metadata.CompositeReportMetaDataHandler;
import org.wso2.carbon.reporting.template.core.util.Template;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.List;


public class CompositeReportJrxmlHandler extends AbstractJrxmlHandler {

    private String[] subReports;
    private String compositeReportName;
    private String[] dataSourceNames;

    private int COMPONENT_HEIGHT = 20;
    private int COMPONENT_MARGIN = 5;

    public CompositeReportJrxmlHandler(String[] subReports, String compositeReportName) throws
            XMLStreamException, ReportingException, FileNotFoundException {

        super(Template.DEFAULT_COMPOSITE_TEMPLATE.getTemplateName());

        if (subReports == null || subReports.length < 1) {
                throw new ReportingException("There should be atleast one sub report name to " +
                        "form a composite report");
        }
        this.subReports = subReports;
        this.compositeReportName = compositeReportName;
        this.dataSourceNames = new String[this.subReports.length];
    }

    public void addCompositeReport() throws JaxenException, ReportingException {
        addDataSourcesParams();
        addSubReportSection();
        writeJrxmlFile(compositeReportName);
         ReportingClient client = ClientFactory.getReportingClient();
         client.uploadJrxmlFile(compositeReportName,getFileContent());
         new CompositeReportMetaDataHandler(subReports, dataSourceNames, compositeReportName).
                 addCompositeReportMetaData();
    }

    private void addDataSourcesParams() throws JaxenException {
        OMElement documentElement = document.getOMDocumentElement();
        OMElement tempDocument = documentElement.cloneOMElement();


        refreshReport();
        int count = 0;
        for (String aSubReport : subReports) {
            count++;
            if (count != 1) addParam("TableDataSource" + count, "net.sf.jasperreports.engine.JRDataSource");
            dataSourceNames[count-1] = "TableDataSource"+count;
            addParam(aSubReport, "net.sf.jasperreports.engine.JasperReport");
        }
        reloadContent(tempDocument);
    }

    private void addSubReportSection() throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:title//a:band");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);
        OMElement parent = (OMElement) nodeList.get(0);

        xpathExpression = new AXIOMXPath("//a:title//a:band//a:subreport");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        nodeList = xpathExpression.selectNodes(documentElement);
        OMElement element = (OMElement) nodeList.get(0);

        OMElement temp = element.cloneOMElement();
        int yPos = 0;
        int count = 0;
        for (String aReportName : subReports) {
            count++;

            if (count == 1) {
                updateSubReportExpression(element, aReportName);

            } else {
                OMElement newSubReportElement = temp.cloneOMElement();
                updateSubReportPosition(newSubReportElement, yPos);
                updateSubReportParamName(newSubReportElement, "TableDataSource" + count);
                updateSubReportExpression(newSubReportElement, aReportName);
                parent.addChild(newSubReportElement);
            }
            yPos = yPos + COMPONENT_HEIGHT + COMPONENT_MARGIN;
        }
    }

    private void refreshReport() throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:title");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);
        OMElement element = (OMElement) nodeList.get(0);
        element.detach();
    }

    private void addParam(String dsname, String className) throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:parameter");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);
        OMElement element = (OMElement) nodeList.get(0);

        OMElement newParam = element.cloneOMElement();
        newParam.getAttribute(new QName("name")).setAttributeValue(dsname);
        newParam.getAttribute(new QName("class")).setAttributeValue(className);

        documentElement.addChild(newParam);
    }

    private void reloadContent(OMElement loadedElement) throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:title");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        List nodeList = xpathExpression.selectNodes(loadedElement);
        OMElement element = (OMElement) nodeList.get(0);

        document.getOMDocumentElement().addChild(element);
    }


    private void updateSubReportPosition(OMElement subReportElement, int yPos) throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:reportElement");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        List nodeList = xpathExpression.selectNodes(subReportElement);
        OMElement repElement = (OMElement) nodeList.get(0);
        repElement.getAttribute(new QName("y")).setAttributeValue(String.valueOf(yPos));
    }


    private void updateSubReportParamName(OMElement subReportElement, String paramName) throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:subreportParameter//a:subreportParameterExpression");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        List nodeList = xpathExpression.selectNodes(subReportElement);
        OMElement repExp = (OMElement) nodeList.get(0);
        repExp.setText("");


        OMFactory factory = document.getOMFactory();
        OMText cdataField = factory.createOMText(repExp, "$P{" + paramName + "}", OMText.CDATA_SECTION_NODE);
        repExp.addChild(cdataField);
    }

    private void updateSubReportExpression(OMElement subReportElement, String subReportName) throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:subreportExpression");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        List nodeList = xpathExpression.selectNodes(subReportElement);
        OMElement element = (OMElement) nodeList.get(0);
        element.setText("");

        OMFactory factory = document.getOMFactory();
        OMText cdataField = factory.createOMText(element,"$P{" + subReportName  + "}", OMText.CDATA_SECTION_NODE);
        element.addChild(cdataField);
    }


}
