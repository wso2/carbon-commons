/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.logging.service.data;

/**
 * Author: amila Date: Jun 27, 2006
 */
public class AppenderData {
    private String name;
    private String pattern;
    private String logFile;
    private boolean isFileAppender;
    private String sysLogHost;
    private String facility;
    private String threshold;
    private boolean isSysLogAppender;

    public AppenderData() {
    }

    public AppenderData(String name, String pattern, String logFile,
                        boolean isFileAppender) {
        this.name = name;
        this.pattern = pattern;
        this.logFile = logFile;
        this.isFileAppender = isFileAppender;
    }

    public AppenderData(String name,
                        String pattern,
                        String logFile,
                        boolean fileAppender,
                        String sysLogHost,
                        String facility,
                        String threshold,
                        boolean sysLogAppender) {
        this.name = name;
        this.pattern = pattern;
        this.logFile = logFile;
        this.isFileAppender = fileAppender;
        this.sysLogHost = sysLogHost;
        this.facility = facility;
        this.threshold = threshold;
        this.isSysLogAppender = sysLogAppender;
    }

    public boolean getIsFileAppender() {
        return isFileAppender;
    }

    public void setIsFileAppender(boolean fileAppender) {
        isFileAppender = fileAppender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getLogFile() {
        return logFile;
    }

    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    public String getSysLogHost() {
        return sysLogHost;
    }

    public void setSysLogHost(String sysLogHost) {
        this.sysLogHost = sysLogHost;
    }

    public String getFacility() {
        return facility;
    }

    public void setFacility(String facility) {
        this.facility = facility;
    }

    public String getThreshold() {
        return threshold;
    }

    public void setThreshold(String threshold) {
        this.threshold = threshold;
    }

    public boolean getIsSysLogAppender() {
        return isSysLogAppender;
    }

    public void setIsSysLogAppender(boolean sysLogAppender) {
        isSysLogAppender = sysLogAppender;
    }
}
