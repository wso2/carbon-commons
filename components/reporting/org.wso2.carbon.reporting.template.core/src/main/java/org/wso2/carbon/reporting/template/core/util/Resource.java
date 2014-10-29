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

import javax.activation.DataHandler;


public class Resource {
    private String fileName;
    private DataHandler dataHandler;

    public Resource() {
        fileName = "";
    }

    public Resource(String fileName, DataHandler dataHandler) {
        this.fileName = fileName;
        this.dataHandler = dataHandler;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setDataHandler(DataHandler dataHandler) {
        this.dataHandler = dataHandler;
    }

    public String getFileName() {
        return fileName;
    }

    public DataHandler getDataHandler() {
        return dataHandler;
    }


    public boolean equals(Object obj){
       if(obj instanceof  Resource){
           Resource resource = (Resource)obj;
           boolean  equals = fileName.equalsIgnoreCase(resource.getFileName()) &
                   dataHandler.equals(resource.getDataHandler());
           return equals;
       }
        else{
           return false;
       }
    }
}
