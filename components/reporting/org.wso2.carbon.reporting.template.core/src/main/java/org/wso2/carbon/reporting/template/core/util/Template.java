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


package org.wso2.carbon.reporting.template.core.util;

public enum Template {

   DEFAULT_TABLE_TEMPLATE("table-template"),
   DEFAULT_BAR_CHART_TEMPLATE ("bar-chart-template"),
   DEFAULT_LINE_CHART_TEMPLATE ("line-chart-template"),
   DEFAULT_STACKED_BAR_CHART_TEMPLATE ("stacked-bar-chart-template"),
   DEFAULT_STACKED_AREA_CHART_TEMPLATE ("stacked-area-chart-template"),
   DEFAULT_AREA_CHART_TEMPLATE ("area-chart-template"),
   DEFAULT_XY_BAR_CHART_TEMPLATE ("xy-bar-chart-template"),
   DEFAULT_XY_LINE_CHART_TEMPLATE ("xy-line-chart-template"),
   DEFAULT_XY_AREA_CHART_TEMPLATE ("xy-area-chart-template"),
   DEFAULT_PIE_CHART_TEMPLATE ("pie-chart-template"),
   DEFAULT_COMPOSITE_TEMPLATE("composite-master");

    private String templateName;

    private Template(String templateName){
     this.templateName = templateName;
    }

    public String getTemplateName(){
        return templateName;
    }
}
