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
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.template.core.util.chart.ChartReportDTO;
import org.wso2.carbon.reporting.template.core.util.chart.DataDTO;
import org.wso2.carbon.reporting.template.core.util.chart.SeriesDTO;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 This class handles the common processing of report types of XY Chart reports
 If any new XY chart reports are needed to be added, use implement a child class from this class.
 */
public abstract class AbstractXYChartJrxmlHandler extends AbstractChartJrxmlHandler {
    private ChartReportDTO chart;

    public AbstractXYChartJrxmlHandler(ChartReportDTO report, String jrxmlFileName) throws XMLStreamException,
            FileNotFoundException, ReportingException {
        super(report, jrxmlFileName);
        chart = report;
    }

    public AbstractXYChartJrxmlHandler(String reportName) throws XMLStreamException,
            FileNotFoundException, ReportingException {
        super(reportName);
    }


    public void addXYChartReport(String chartText, String chartPlot, ChartReportDTO ChartReport)
            throws ReportingException {
        addChartReport(chartText, chartPlot, "xyDataset", "xySeries", "xValueExpression", "yValueExpression", chart);
    }


    protected void handleFields(String chartText, ChartReportDTO chartReport, String chartDatasetText,
                                String chartSeriesText, String xExprtext, String yExpreText)
            throws JaxenException {
        AXIOMXPath xpathExpression = new AXIOMXPath("//a:subDataset//a:field");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        OMElement documentElement = document.getOMDocumentElement();
        List nodeList = xpathExpression.selectNodes(documentElement);
        OMElement numberField = (OMElement) nodeList.get(0);


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
                setSeriesName(series.getName(), getCategorySeriesElement(chartText,
                        chartDatasetText, chartSeriesText));
            } else {
                String xFieldId = "";
                String yFieldId = "";
                String id = addedXdata.get(series.getXdata());
                if (id == null || id.equalsIgnoreCase("")) {
                    OMElement xdataField = numberField.cloneOMElement();
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

}
