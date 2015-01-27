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


public class SeriesDTO {
    private String name;
    private DataDTO xdata;
    private DataDTO ydata;
    private String color;

    public SeriesDTO(){
        name = "";
        xdata = new DataDTO();
        ydata = new DataDTO();
        color = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataDTO getXdata() {
        return xdata;
    }

    public void setXdata(DataDTO xdata) {
        this.xdata = xdata;
    }

    public DataDTO getYdata() {
        return ydata;
    }

    public void setYdata(DataDTO ydata) {
        this.ydata = ydata;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
