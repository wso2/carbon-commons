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

import org.wso2.carbon.reporting.template.core.util.Resource;

public class ReportHeaderInformationDTO {
    private Resource logo;
    private String title;
    private FontStyleDTO titleFont;

    public ReportHeaderInformationDTO(){
        logo = new Resource();
        title = "";
        titleFont = new FontStyleDTO();
    }
    public Resource getLogo() {
        return logo;
    }

    public void setLogo(Resource logo) {
        this.logo = logo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public FontStyleDTO getTitleFont() {
        return titleFont;
    }

    public void setTitleFont(FontStyleDTO titleFont) {
        this.titleFont = titleFont;
    }

    public boolean equals(Object obj){
       if(obj instanceof ReportHeaderInformationDTO){
           ReportHeaderInformationDTO anotherRepHeader = (ReportHeaderInformationDTO)obj;
           boolean  equals = logo.equals(anotherRepHeader.getLogo()) &
                   title.equalsIgnoreCase(anotherRepHeader.getTitle()) &
                   titleFont.equals(anotherRepHeader.getTitleFont());
           return equals;
       }
        else{
           return false;
       }
    }



}
