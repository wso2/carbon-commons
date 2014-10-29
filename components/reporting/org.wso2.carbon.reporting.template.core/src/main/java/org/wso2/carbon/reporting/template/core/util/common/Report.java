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

package org.wso2.carbon.reporting.template.core.util.common;


public interface Report {
     public String getReportType();

    public void setReportType(String reportType);

    public boolean isPeriodicGeneration();

    public void setPeriodicGeneration(boolean periodicGeneration);

    public String getPeriodicGenerationDate();

    public void setPeriodicGenerationDate(String periodicGenerationDate);

    public String getTimeSeperation();

    public void setTimeSeperation(String timeSeperation);


    public ReportHeaderInformationDTO getReportHeaderInformation();

    public void setReportHeaderInformation(ReportHeaderInformationDTO reportHeaderInformation);


    public String getBackgroundColour();

    public void setBackgroundColour(String backgroundColour);

    public String getReportName();

    public void setReportName(String reportName);
}
