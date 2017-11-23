/**
 *  Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.ntask.core.service.impl;

import org.wso2.carbon.ntask.core.service.TaskService.TaskServerMode;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * This represents the task service XML based configuration.
 */
@XmlRootElement(name = "tasks-configuration")
public class TaskServiceXMLConfiguration {

    private TaskServerMode taskServerMode;

    private String taskClientDispatchAddress;

    private String remoteServerAddress;

    private String remoteServerUsername;

    private String remoteServerPassword;

    public TaskServerMode getTaskServerMode() {
        return taskServerMode;
    }

    public void setTaskServerMode(TaskServerMode taskServerMode) {
        this.taskServerMode = taskServerMode;
    }

    @XmlElement(nillable = true)
    public String getTaskClientDispatchAddress() {
        return taskClientDispatchAddress;
    }

    public void setTaskClientDispatchAddress(String taskClientDispatchAddress) {
        this.taskClientDispatchAddress = taskClientDispatchAddress;
    }

    @XmlElement(nillable = true)
    public String getRemoteServerAddress() {
        return remoteServerAddress;
    }

    public void setRemoteServerAddress(String remoteServerAddress) {
        this.remoteServerAddress = remoteServerAddress;
    }

    @XmlElement(nillable = true)
    public String getRemoteServerUsername() {
        return remoteServerUsername;
    }

    public void setRemoteServerUsername(String remoteServerUsername) {
        this.remoteServerUsername = remoteServerUsername;
    }

    @XmlElement(nillable = true)
    public String getRemoteServerPassword() {
        return remoteServerPassword;
    }

    public void setRemoteServerPassword(String remoteServerPassword) {
        this.remoteServerPassword = remoteServerPassword;
    }
}
