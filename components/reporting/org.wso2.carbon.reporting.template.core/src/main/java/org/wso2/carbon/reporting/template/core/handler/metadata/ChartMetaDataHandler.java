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
import org.wso2.carbon.reporting.template.core.util.chart.ChartReportDTO;
import org.wso2.carbon.reporting.template.core.util.chart.DataDTO;
import org.wso2.carbon.reporting.template.core.util.chart.SeriesDTO;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;

/*
Handles the metadata information about the chart report type
 */
public class ChartMetaDataHandler extends AbstractMetaDataHandler {
    private ChartReportDTO chartReport;
    private OMElement chartReportElement;
    private OMElement reportElement;
    private String reportType;


    private static final String DS_NAME = "dsName";
    private static final String CHART_REPORT = "chartReport";
    private static final String TABLE = "table";
    private static final String COLUMN = "field";
    private static final String XDATA = "xData";
    private static final String YDATA = "yData";
    private static final String SERIES = "series";
    private static final String ID = "id";


    private static Log log = LogFactory.getLog(ChartMetaDataHandler.class);


    public ChartMetaDataHandler(ChartReportDTO tableReport) throws ReportingException {
        super();
        this.chartReport = tableReport;
    }

    public ChartMetaDataHandler() throws ReportingException {
        super();
    }

    public void updateChartReportMetaData() throws ReportingException {
        removeChartMetadata(chartReport.getReportName());
        addChartMetadata();
    }


    private void removeChartMetadata(String reportName) {
        Iterator iterator = reportsElement.getChildElements();
        boolean isChartFound = false;

        while (iterator.hasNext()) {
            OMElement reportElement = (OMElement) iterator.next();
            String reportType = reportElement.getAttributeValue(new QName(TYPE));
            if (reportType.contains("chart")) {
                Iterator chartIterator = reportElement.getChildElements();
                while (chartIterator.hasNext()) {
                    OMElement chartReportElement = (OMElement) chartIterator.next();
                    String chartName = chartReportElement.getAttributeValue(new QName(NAME));
                    if (chartName.equalsIgnoreCase(reportName)) {
                        reportElement.detach();
                        isChartFound = true;
                        return;
                    }
                }

            }

            if (isChartFound) break;
        }
    }


    private void addChartMetadata() throws ReportingException {
        createReportElement();
        createChartReportElement();
        SeriesDTO[] allSeries = chartReport.getCategorySeries();

        for (SeriesDTO series : allSeries) {
            createSeriesElement(series);
        }
        saveMetadata();
    }

    private void createReportElement() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        reportElement = fac.createOMElement(new QName(REPORT));
        reportElement.addAttribute(TYPE, chartReport.getReportType(), null);
        reportElement.addAttribute(DS_NAME, chartReport.getDsName(), null);
        reportsElement.addChild(reportElement);
    }


    private void createChartReportElement() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        chartReportElement = fac.createOMElement(new QName(CHART_REPORT));
        chartReportElement.addAttribute("name", chartReport.getReportName(), null);
        reportElement.addChild(chartReportElement);
    }

    private void createSeriesElement(SeriesDTO series) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMElement seriesElement = fac.createOMElement(new QName(SERIES));
        seriesElement.addAttribute(NAME, series.getName(), null);

        OMElement xDataElement = getDataElement(series.getXdata(), XDATA);
        seriesElement.addChild(xDataElement);

        OMElement yDataElement = getDataElement(series.getYdata(), YDATA);
        seriesElement.addChild(yDataElement);

        chartReportElement.addChild(seriesElement);
    }

    private OMElement getDataElement(DataDTO data, String elementName) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMElement dataElement = fac.createOMElement(new QName(elementName));
        dataElement.addAttribute(ID, data.getFieldId(), null);

        OMElement table = fac.createOMElement(new QName(TABLE));
        table.setText(data.getDsTableName());
        dataElement.addChild(table);

        OMElement field = fac.createOMElement(new QName(COLUMN));
        field.setText(data.getDsColumnName());
        dataElement.addChild(field);

        return dataElement;
    }


    public ChartReportDTO getChartReportMetaData(String reportName) throws ReportingException {
        chartReport = new ChartReportDTO();
        chartReport.setReportName(reportName);
        chartReportElement = isChartReportMetaDataFound();

        if (chartReportElement != null) {
            chartReport.setReportType(reportType);
            chartReport.setDsName(getDsName());
            ArrayList<SeriesDTO> seriesArrayList = getSeries();
            SeriesDTO[] serieses = new SeriesDTO[seriesArrayList.size()];
            serieses = seriesArrayList.toArray(serieses);
            chartReport.setCategorySeries(serieses);
            return chartReport;
        } else {
            log.error("No meta data found for chart type report " + reportName);
            throw new ReportingException("No meta data found for chart type report " + reportName);
        }
    }


    private ArrayList<SeriesDTO> getSeries() {
        Iterator iterator = chartReportElement.getChildren();
        OMElement specificChart = (OMElement) iterator.next();

        Iterator seriesIter = specificChart.getChildren();
        ArrayList<SeriesDTO> seriesArrayList = new ArrayList<SeriesDTO>();
        while (seriesIter.hasNext()) {
            OMElement seriesElement = (OMElement) seriesIter.next();
            String seriesName = seriesElement.getAttribute(new QName(NAME)).getAttributeValue();
            SeriesDTO series = new SeriesDTO();
            series.setName(seriesName);
            setData(series, seriesElement);
            seriesArrayList.add(series);
        }

        return seriesArrayList;
    }


    private void setData(SeriesDTO series, OMElement seriesElement) {
        Iterator iterator = seriesElement.getChildren();

        DataDTO xData = getData((OMElement) iterator.next());
        series.setXdata(xData);

        DataDTO yData = getData((OMElement) iterator.next());
        series.setYdata(yData);

    }

    private DataDTO getData(OMElement element) {
        DataDTO data = new DataDTO();
        data.setFieldId(element.getAttributeValue(new QName(ID)));
        Iterator iterator = element.getChildren();
        data.setDsTableName(((OMElement) iterator.next()).getText());
        data.setDsColumnName(((OMElement) iterator.next()).getText());
        return data;
    }

    private String getDsName() {
        Iterator iterator = reportsElement.getChildElements();
        boolean isTableFound = false;

        while (iterator.hasNext()) {
            OMElement reportElement = (OMElement) iterator.next();
            String reportType = reportElement.getAttributeValue(new QName(TYPE));
            String dsName = reportElement.getAttributeValue(new QName(DS_NAME));
            if (reportType.equalsIgnoreCase(this.reportType)) {
                Iterator chartIterator = reportElement.getChildElements();
                while (chartIterator.hasNext()) {
                    OMElement chartReportElement = (OMElement) chartIterator.next();
                    String chartName = chartReportElement.getAttributeValue(new QName(NAME));
                    if (chartName.equalsIgnoreCase(chartReport.getReportName())) {
                        return dsName;
                    }
                }
            }
        }
        return null;
    }


    private OMElement isChartReportMetaDataFound() {
        Iterator iterator = reportsElement.getChildElements();
        boolean isChartFound = false;

        while (iterator.hasNext()) {
            OMElement reportElement = (OMElement) iterator.next();
            String reportType = reportElement.getAttributeValue(new QName(TYPE));
            if (reportType.contains("chart")) {
                Iterator chartIterator = reportElement.getChildElements();
                while (chartIterator.hasNext()) {
                    OMElement chartReportElement = (OMElement) chartIterator.next();
                    String chartName = chartReportElement.getAttributeValue(new QName(NAME));
                    if (chartName.equalsIgnoreCase(chartReport.getReportName())) {
                        this.reportType = reportType;
                        return reportElement;
                    }
                }

            }

        }
        return null;
    }


    public void removeMetaData(String reportName) throws ReportingException {
        removeChartMetadata(reportName);
        saveMetadata();
    }
}
