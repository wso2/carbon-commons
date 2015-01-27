/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.logging.service.data;

/**
 *
 */
public class LogData {
    // these are global settings set by the user
    private String logLevel;
    private String logPattern;
    private String logFile;
    private AppenderData selectedAppenderData;
    private LoggerData selectedLoggerData;
    private AppenderData[] appenderData = new AppenderData[0];
    private LoggerData[] loggerData = new LoggerData[0];

    public LogData() {
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getLogPattern() {
        return logPattern;
    }

    public void setLogPattern(String logPattern) {
        this.logPattern = logPattern;
    }

    public String getLogFile() {
        return logFile;
    }

    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    public AppenderData[] getAppenderData() {
        return appenderData;
    }

    public void setAppenderData(AppenderData[] appenderData) {
        this.appenderData = appenderData;
    }

    public AppenderData getSelectedAppenderData() {
        return selectedAppenderData;
    }

    public void setSelectedAppenderData(AppenderData selectedAppenderData) {
        this.selectedAppenderData = selectedAppenderData;
    }

    public LoggerData getSelectedLoggerData() {
        return selectedLoggerData;
    }

    public void setSelectedLoggerData(LoggerData selectedLoggerData) {
        this.selectedLoggerData = selectedLoggerData;
    }

    public LoggerData[] getLoggerData() {
        return loggerData;
    }

    public void setLoggerData(LoggerData[] loggerData) {
        this.loggerData = loggerData;
    }
}
