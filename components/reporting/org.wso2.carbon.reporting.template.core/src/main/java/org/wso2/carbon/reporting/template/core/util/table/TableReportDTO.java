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


package org.wso2.carbon.reporting.template.core.util.table;


import org.wso2.carbon.reporting.template.core.util.common.Report;
import org.wso2.carbon.reporting.template.core.util.common.ReportConstants;
import org.wso2.carbon.reporting.template.core.util.common.ReportHeaderInformationDTO;

public class TableReportDTO implements Report {
    private String outLineColor;
    private double outLineThickness;
    private String reportName;
    private String reportType;
    private boolean periodicGeneration;
    private String periodicGenerationDate;
    private String timeSeperation;
    private ReportHeaderInformationDTO reportHeaderInformation;
    private String backgroundColour;
    private ColumnDTO[] columns;
    private String dsName;

    public TableReportDTO(){
        setReportType(ReportConstants.TABLE_TYPE);
        outLineColor = "#000000";
        outLineThickness = 0.5;
    }

    public String getOutLineColor() {
        return outLineColor;
    }

    public void setOutLineColor(String outLineColor) {
        this.outLineColor = outLineColor;
    }

    public double getOutLineThickness() {
        return outLineThickness;
    }

    public void setOutLineThickness(double outLineThickness) {
        this.outLineThickness = outLineThickness;
    }


    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public boolean isPeriodicGeneration() {
        return periodicGeneration;
    }

    public void setPeriodicGeneration(boolean periodicGeneration) {
        this.periodicGeneration = periodicGeneration;
    }

    public String getPeriodicGenerationDate() {
        return periodicGenerationDate;
    }

    public void setPeriodicGenerationDate(String periodicGenerationDate) {
        this.periodicGenerationDate = periodicGenerationDate;
    }

    public String getTimeSeperation() {
        return timeSeperation;
    }

    public void setTimeSeperation(String timeSeperation) {
        this.timeSeperation = timeSeperation;
    }

    public ReportHeaderInformationDTO getReportHeaderInformation() {
        return reportHeaderInformation;
    }

    public void setReportHeaderInformation(ReportHeaderInformationDTO reportHeaderInformation) {
        this.reportHeaderInformation = reportHeaderInformation;
    }

    public String getBackgroundColour() {
        return backgroundColour;
    }

    public void setBackgroundColour(String backgroundColour) {
        this.backgroundColour = backgroundColour;
    }

    public ColumnDTO[] getColumns() {
        return columns;
    }

    public void setColumns(ColumnDTO[] columns) {
        this.columns = columns;
    }

    public String getDsName() {
        return dsName;
    }

    public void setDsName(String dsName) {
        this.dsName = dsName;
    }
}
