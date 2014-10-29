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


import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.template.core.util.Template;
import org.wso2.carbon.reporting.template.core.util.chart.ChartReportDTO;
import org.wso2.carbon.reporting.template.core.util.common.ReportConstants;

import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;

public class LineChartJrxmlHandler extends AbstractChartJrxmlHandler{
    private ChartReportDTO chartReport;

     public LineChartJrxmlHandler(ChartReportDTO chartReport) throws XMLStreamException,
             FileNotFoundException, ReportingException {
         super(chartReport, Template.DEFAULT_LINE_CHART_TEMPLATE.getTemplateName());
         this.chartReport = chartReport;
         this.chartReport.setReportType(ReportConstants.LINE_CHART_TYPE);
     }

    public void addLineChartReport() throws ReportingException {
       addChartReport("lineChart", "linePlot", "categoryDataset","categorySeries",
               "categoryExpression", "valueExpression", chartReport);
    }

     public LineChartJrxmlHandler(String reportName) throws XMLStreamException,
             FileNotFoundException, ReportingException {
         super(reportName);
    }


}
