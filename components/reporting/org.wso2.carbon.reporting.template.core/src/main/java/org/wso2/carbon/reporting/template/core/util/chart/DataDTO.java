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


public class DataDTO {
    private String dsTableName;
    private String dsColumnName;
    private String fieldId;

    public DataDTO(){
        dsTableName = "";
        dsColumnName = "";
        fieldId = "";
    }
    public String getDsTableName() {
        return dsTableName;
    }

    public void setDsTableName(String dsTableName) {
        this.dsTableName = dsTableName;
    }

    public String getDsColumnName() {
        return dsColumnName;
    }

    public void setDsColumnName(String dsColumnName) {
        this.dsColumnName = dsColumnName;
    }

     public boolean equals(Object obj) {
         if(obj instanceof DataDTO){
             DataDTO anotherData = (DataDTO)obj;
             if(dsTableName.equalsIgnoreCase(anotherData.getDsTableName())){
                 if(dsColumnName.equalsIgnoreCase(anotherData.getDsColumnName())){
                     return true;
                 }
             }
         }
         return false;
     }

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }
}
