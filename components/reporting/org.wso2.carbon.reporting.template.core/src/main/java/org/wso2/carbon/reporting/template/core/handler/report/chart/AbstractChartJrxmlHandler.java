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


package org.wso2.carbon.reporting.template.core.handler.report.chart;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.template.core.client.ReportingClient;
import org.wso2.carbon.reporting.template.core.factory.ClientFactory;
import org.wso2.carbon.reporting.template.core.handler.metadata.ChartMetaDataHandler;
import org.wso2.carbon.reporting.template.core.handler.report.common.AbstractJrxmlHandler;
import org.wso2.carbon.reporting.template.core.util.Resource;
import org.wso2.carbon.reporting.template.core.util.chart.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/*
This class handles the common information about all charts report.
If a new chart report template is added, that must be a child class of this.

 */
public abstract class AbstractChartJrxmlHandler extends AbstractJrxmlHandler {
    private ChartReportDTO chart;

    private static Log log = LogFactory.getLog(AbstractChartJrxmlHandler.class);

    public AbstractChartJrxmlHandler(ChartReportDTO report, String jrxmlFileName)
            throws XMLStreamException, FileNotFoundException, ReportingException {
        super(report, jrxmlFileName);
        chart = report;
    }

    public AbstractChartJrxmlHandler(String reportName)
            throws XMLStreamException, ReportingException, FileNotFoundException {
        super(reportName);
    }


    protected void handleChartFormat(String chartText) throws JaxenException {
        handleChartBackgroundColor(chartText);
        handleChartTitle(chartText);
        handleChartSubTitle(chartText);
    }

    private void handleChartBackgroundColor(String chartText) throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:title//a:band//a:" + chartText + "//a:chart//a:reportElement");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);
        OMElement element = (OMElement) nodeList.get(0);
        element.getAttribute(new QName("backcolor")).setAttributeValue(chart.getChartBackColor());
    }

    private void handleChartTitle(String chartText) throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:title//a:band//a:" + chartText
                + "//a:chart//a:chartTitle//a:titleExpression");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);
        OMElement element = (OMElement) nodeList.get(0);
        element.setText("");
        OMFactory factory = document.getOMFactory();
        OMText cdataField = factory.createOMText(element, "\"" + chart.getTitle() + "\"",
                OMText.CDATA_SECTION_NODE);
        element.addChild(cdataField);
    }

    private void handleChartSubTitle(String chartText) throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:title//a:band//a:" + chartText +
                "//a:chart//a:chartSubtitle//a:subtitleExpression");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);
        OMElement element = (OMElement) nodeList.get(0);
        element.setText("");
        OMFactory factory = document.getOMFactory();
        OMText cdataField = factory.createOMText(element, "\"" + chart.getSubTitle() + "\"",
                OMText.CDATA_SECTION_NODE);
        element.addChild(cdataField);
    }

    protected void addChartReport(String chartText, String chartPlot, String chartDatasetText,
                                  String chartSeriesText, String xExprtext, String yExpreText, ChartReportDTO chartReport)
            throws ReportingException {
        try {
            this.handleReportFormat();
            this.handleChartFormat(chartText);
            this.handleFields(chartText, chartReport, chartDatasetText, chartSeriesText, xExprtext, yExpreText);
            this.handleLabels(chartText, chartPlot, chartReport);
           // writeJrxmlFile(chartReport.getReportName() + ".jrxml");
            ReportingClient client = ClientFactory.getReportingClient();
            new ChartMetaDataHandler(chartReport).updateChartReportMetaData();
            client.uploadJrxmlFile(chartReport.getReportName(), getFileContent());
            Resource logo = chartReport.getReportHeaderInformation().getLogo();
            if (logo != null && logo.getFileName() != null && !logo.getFileName().equalsIgnoreCase("")) {
                if (logo.getDataHandler() != null) {
                    client.uploadImage(logo.getFileName(), chartReport.getReportName(), logo.getDataHandler());
                } else {
                    log.error("No data for uploaded image found...");
                    throw new ReportingException("No data for uploaded image found...");
                }
            }
        } catch (JaxenException e) {
            throw new ReportingException(e.getMessage(), e);
        }

    }


    protected void handleFields(String chartText, ChartReportDTO chartReport, String chartDatasetText,
                                String chartSeriesText, String xExprtext, String yExpreText)
            throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:subDataset//a:field");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);
        OMElement stringField = (OMElement) nodeList.get(0);
        OMElement numberField = (OMElement) nodeList.get(1);


        xpathExpression = new AXIOMXPath("//a:subDataset");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        documentElement = document.getOMDocumentElement();
        OMElement subDataSetElement = (OMElement) xpathExpression.selectNodes(documentElement).get(0);

        Map<DataDTO, String> addedXdata = new HashMap<DataDTO, String>();
        int fieldId = 3;
        for (int i = 0; i < chartReport.getCategorySeries().length; i++) {
            SeriesDTO series = chartReport.getCategorySeries()[i];
            if (i == 0) {
                addedXdata.put(series.getXdata(), "1");
                series.getXdata().setFieldId("1");
                series.getYdata().setFieldId("2");
                setSeriesName(series.getName(), getCategorySeriesElement(chartText, chartDatasetText,
                        chartSeriesText));
            } else {
                String xFieldId = "";
                String yFieldId = "";
                String id = addedXdata.get(series.getXdata());
                if (id == null || id.equalsIgnoreCase("")) {
                    OMElement xdataField = stringField.cloneOMElement();
                    xFieldId = String.valueOf(fieldId);
                    xdataField.getAttribute(new QName("name")).setAttributeValue(String.valueOf(fieldId));
                    subDataSetElement.addChild(xdataField);
                    fieldId++;
                } else {
                    xFieldId = id;
                }
                OMElement ydataField = numberField.cloneOMElement();
                ydataField.getAttribute(new QName("name")).setAttributeValue(String.valueOf(fieldId));
                subDataSetElement.addChild(ydataField);
                yFieldId = String.valueOf(fieldId);
                fieldId++;

                addCategorySeries(xFieldId, yFieldId, series.getName(), chartText, chartDatasetText,
                        chartSeriesText, xExprtext, yExpreText);
                series.getXdata().setFieldId(xFieldId);
                series.getYdata().setFieldId(yFieldId);
            }
        }
    }

    protected OMElement getCategorySeriesElement(String chartText, String chartDatasetText,
                                                 String chartSeriesText) throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:title//a:" + chartText + "//a:" + chartDatasetText +
                "//a:" + chartSeriesText);
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);
        OMElement aCategorySeries = (OMElement) nodeList.get(0);
        return aCategorySeries;
    }

    protected void addCategorySeries(String xField, String yFiled, String seriesName, String chartText,
                                     String chartDatasetText, String chartSeriesText, String xExpreText,
                                     String yExpreText)
            throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:title//a:" + chartText + "//a:" + chartDatasetText +
                "//a:" + chartSeriesText);
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);
        OMElement aCategorySeries = (OMElement) nodeList.get(0);

        OMElement anotherSeries = aCategorySeries.cloneOMElement();
        setSeriesName(seriesName, anotherSeries);
        setCategoryExpr(xField, anotherSeries, xExpreText);
        setValueExpr(yFiled, anotherSeries, yExpreText);

        xpathExpression = new AXIOMXPath("//a:title//a:" + chartText + "//a:" + chartDatasetText);
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        documentElement = document.getOMDocumentElement();
        nodeList = xpathExpression.selectNodes(documentElement);
        OMElement dataSet = (OMElement) nodeList.get(0);
        dataSet.addChild(anotherSeries);

    }


    protected void setSeriesName(String name, OMElement categorySeriesElement) throws JaxenException {

        OMElement aSeriesExpr = null;
        Iterator iterator = categorySeriesElement.getChildren();
        while (iterator.hasNext()) {
            Object temp = iterator.next();
            if (temp instanceof OMElement) {
                OMElement element = (OMElement) temp;
                if (element.getLocalName().equalsIgnoreCase("seriesExpression")) {
                    aSeriesExpr = element;
                    break;
                }
            }
        }

        aSeriesExpr.setText("");
        Iterator removeChildIter = aSeriesExpr.getChildren();
        while (removeChildIter.hasNext()) {
            Object temp = removeChildIter.next();
            if (temp instanceof OMText) {
                ((OMText) temp).discard();
            }
        }
        OMFactory factory = document.getOMFactory();
        OMText cdataField = factory.createOMText(aSeriesExpr, "\"" + name + "\"", OMText.CDATA_SECTION_NODE);
        aSeriesExpr.addChild(cdataField);

    }


    private void setCategoryExpr(String XField, OMElement categorySeriesElement, String xExpressionText)
            throws JaxenException {
        Iterator iter = categorySeriesElement.getChildrenWithName(new QName(xExpressionText));
        OMElement aCatExpr = (OMElement) iter.next();

        aCatExpr.setText("");
        OMFactory factory = document.getOMFactory();
        OMText cdataField = factory.createOMText(aCatExpr, "$F{" + XField + "}", OMText.CDATA_SECTION_NODE);
        aCatExpr.addChild(cdataField);

    }


    private void setValueExpr(String YField, OMElement categorySeriesElement, String yExpressionText)
            throws JaxenException {
        Iterator iter = categorySeriesElement.getChildrenWithName(new QName(yExpressionText));
        OMElement aValueExpr = (OMElement) iter.next();

        aValueExpr.setText("");
        OMFactory factory = document.getOMFactory();
        OMText cdataField = factory.createOMText(aValueExpr, "$F{" + YField + "}", OMText.CDATA_SECTION_NODE);
        aValueExpr.addChild(cdataField);

    }

    protected void handleLabels(String chartText, String chartPlot, ChartReportDTO chartReport)
            throws JaxenException {
        setCategoryAxisLabel(chartText, chartPlot, chartReport);
        setValueAxisLabel(chartText, chartPlot, chartReport);
    }

    private void setCategoryAxisLabel(String chartText, String chartPlotText, ChartReportDTO chartReport)
            throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:title//a:" + chartText + "//a:" + chartPlotText +
                "//a:categoryAxisLabelExpression");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);
        OMElement label = (OMElement) nodeList.get(0);

        label.setText("");
        OMFactory factory = document.getOMFactory();
        OMText cdataField = factory.createOMText(label, "\"" + chartReport.getxAxisLabel() + "\"",
                OMText.CDATA_SECTION_NODE);
        label.addChild(cdataField);

    }

    private void setValueAxisLabel(String chartText, String chartPlotText, ChartReportDTO chartReport)
            throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:title//a:" + chartText + "//a:" + chartPlotText +
                "//a:valueAxisLabelExpression");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);
        OMElement label = (OMElement) nodeList.get(0);

        label.setText("");
        OMFactory factory = document.getOMFactory();
        OMText cdataField = factory.createOMText(label, "\"" + chartReport.getyAxisLabel() + "\"",
                OMText.CDATA_SECTION_NODE);
        label.addChild(cdataField);

    }


}
