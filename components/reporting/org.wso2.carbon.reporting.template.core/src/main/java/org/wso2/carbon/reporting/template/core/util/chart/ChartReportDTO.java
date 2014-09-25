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

package org.wso2.carbon.reporting.template.core.util.chart;

import org.wso2.carbon.reporting.template.core.util.common.Report;
import org.wso2.carbon.reporting.template.core.util.common.ReportHeaderInformationDTO;


public class ChartReportDTO implements Report{
    private String title;
    private String subTitle;
    private String reportName;
    private String reportType;
    private boolean periodicGeneration;
    private String periodicGenerationDate;
    private String timeSeperation;
    private ReportHeaderInformationDTO reportHeaderInformation;
    private String backgroundColour;
    private String chartBackColor;
    private String dsName;
    private SeriesDTO[] categorySeries;
    private String xAxisLabel;
    private String yAxisLabel;

    public ChartReportDTO(){
     this.reportType = "";
    }

    @Override
    public String getReportType() {
        return reportType;
    }

    @Override
    public void setReportType(String reportType) {
      this.reportType = reportType;
    }

    @Override
    public boolean isPeriodicGeneration() {
        return periodicGeneration;
    }

    @Override
    public void setPeriodicGeneration(boolean periodicGeneration) {
       this.periodicGeneration = periodicGeneration;
    }

    @Override
    public String getPeriodicGenerationDate() {
       return periodicGenerationDate;
    }

    @Override
    public void setPeriodicGenerationDate(String periodicGenerationDate) {
       this.periodicGenerationDate = periodicGenerationDate;
    }

    @Override
    public String getTimeSeperation() {
      return timeSeperation;
    }

    @Override
    public void setTimeSeperation(String timeSeperation) {
       this.timeSeperation = timeSeperation;
    }

    @Override
    public ReportHeaderInformationDTO getReportHeaderInformation() {
        return reportHeaderInformation;
    }

    @Override
    public void setReportHeaderInformation(ReportHeaderInformationDTO reportHeaderInformation) {
       this.reportHeaderInformation = reportHeaderInformation;
    }

    @Override
    public String getBackgroundColour() {
       return backgroundColour;
    }

    @Override
    public void setBackgroundColour(String backgroundColour) {
        this.backgroundColour = backgroundColour;
    }

    @Override
    public String getReportName() {
        return reportName;
    }

    @Override
    public void setReportName(String reportName) {
      this.reportName = reportName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
//        if(title == null){
//           this.title = "";
//        }else{
//          this.title = title;
//        }
        this.title = title;

    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
//        if(subTitle == null){
//            this.subTitle = "";
//        }else {
//            this.subTitle = subTitle;
//        }
        this.subTitle = subTitle;
    }

    public String getDsName() {
        return dsName;
    }

    public void setDsName(String dsName) {
        this.dsName = dsName;
    }

    public SeriesDTO[] getCategorySeries() {
        return categorySeries;
    }

    public void setCategorySeries(SeriesDTO[] categorySeries) {
        this.categorySeries = categorySeries;
    }

    public String getChartBackColor() {
        return chartBackColor;
    }

    public void setChartBackColor(String chartBackColor) {
        this.chartBackColor = chartBackColor;
    }

    public String getxAxisLabel() {
        return xAxisLabel;
    }

    public void setxAxisLabel(String xAxisLabel) {
//        if(xAxisLabel == null){
//            this.xAxisLabel = "";
//        }else {
//        this.xAxisLabel = xAxisLabel;
//        }
        this.xAxisLabel = xAxisLabel;

    }

    public String getyAxisLabel() {
        return yAxisLabel;
    }

    public void setyAxisLabel(String yAxisLabel) {
//        if(yAxisLabel == null){
//            this.yAxisLabel = "";
//        }else {
//            this.yAxisLabel = yAxisLabel;
//        }
        this.yAxisLabel = yAxisLabel;
    }
}
