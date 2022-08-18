/**
 *  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.ntask.core;

/**
 * This class represents an identifier for a task manager.
 */
public class TaskManagerId {

    private int tenantId;

    private String taskType;

    public TaskManagerId(int tenantId, String taskType) {
        this.tenantId = tenantId;
        this.taskType = taskType;
    }

    public int getTenantId() {
        return tenantId;
    }

    public String getTaskType() {
        return taskType;
    }

    @Override
    public int hashCode() {
        return (this.getTaskType() + ":" + this.getTenantId()).hashCode();
    }

    @Override
    public boolean equals(Object rhs) {
        return this.hashCode() == rhs.hashCode();
    }

    @Override
    public String toString() {
        return this.getTaskType() + ":" + this.getTenantId();
    }

}
