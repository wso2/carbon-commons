/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.logging.view.data;

/**
 * LogEvent Model for Log Viewer
 */
public class LogEvent {

    private String key;
    private String tenantId;
    private String serverName;
    private String appName;
    private String logTime;
    private String logger;
    private String priority;
    private String message;
    private String ip;
    private String stacktrace;
    private String instance;

    public LogEvent() {

    }

    public String getKey() {

        return key;
    }

    public void setKey(String key) {

        this.key = key;
    }

    public String getTenantId() {

        return tenantId;
    }

    public void setTenantId(String tenantId) {

        this.tenantId = tenantId;
    }

    public String getServerName() {

        return serverName;
    }

    public void setServerName(String serverName) {

        this.serverName = serverName;
    }

    public String getAppName() {

        return appName;
    }

    public void setAppName(String appName) {

        this.appName = appName;
    }

    public String getLogTime() {

        return logTime;
    }

    public void setLogTime(String logTime) {

        this.logTime = logTime;
    }

    public String getLogger() {

        return logger;
    }

    public void setLogger(String logger) {

        this.logger = logger;
    }

    public String getPriority() {

        return priority;
    }

    public void setPriority(String priority) {

        this.priority = priority;
    }

    public String getMessage() {

        return message;
    }

    public void setMessage(String message) {

        this.message = message;
    }

    public String getIp() {

        return ip;
    }

    public void setIp(String ip) {

        this.ip = ip;
    }

    public String getStacktrace() {

        return stacktrace;
    }

    public void setStacktrace(String stacktrace) {

        this.stacktrace = stacktrace;
    }

    public String getInstance() {

        return instance;
    }

    public void setInstance(String instance) {

        this.instance = instance;
    }
}
