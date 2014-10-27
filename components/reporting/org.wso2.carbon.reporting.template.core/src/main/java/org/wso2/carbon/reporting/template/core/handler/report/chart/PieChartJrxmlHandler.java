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

import org.jaxen.JaxenException;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.template.core.util.Template;
import org.wso2.carbon.reporting.template.core.util.chart.ChartReportDTO;
import org.wso2.carbon.reporting.template.core.util.chart.SeriesDTO;
import org.wso2.carbon.reporting.template.core.util.common.ReportConstants;

import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;


public class PieChartJrxmlHandler extends AbstractChartJrxmlHandler{
    private ChartReportDTO chartReport;

     public PieChartJrxmlHandler(ChartReportDTO chartReport) throws XMLStreamException,
             FileNotFoundException, ReportingException {
         super(chartReport, Template.DEFAULT_PIE_CHART_TEMPLATE.getTemplateName());
         this.chartReport = chartReport;
         this.chartReport.setReportType(ReportConstants.PIE_CHART_TYPE);
     }

     public PieChartJrxmlHandler(String reportName) throws XMLStreamException,
             FileNotFoundException, ReportingException {
         super(reportName);
     }

    public void addPieChartReport() throws ReportingException {
      if(validateSeries()){
          addChartReport("pieChart", "piePlot", "","", "", "", chartReport);
      }
        else {
          throw new ReportingException("Only one series can be shown by a pie-chart "+ chartReport.getReportName());
      }
    }



    protected void handleFields(String chartText, ChartReportDTO chartReport, String chartDatasetText,
                                String chartSeriesText, String xExprtext, String yExpreText)
            throws JaxenException {
                SeriesDTO series = chartReport.getCategorySeries()[0];
                series.getXdata().setFieldId("1");
                series.getYdata().setFieldId("2");
          return;
    }

     protected void handleLabels(String chartText, String chartPlot, ChartReportDTO chartReport)
             throws JaxenException {
         //In the pie chart no labels are required to be present
         return;
     }

    private boolean validateSeries(){
        SeriesDTO[] serieses = chartReport.getCategorySeries();
        return serieses.length == 1;
    }
}
