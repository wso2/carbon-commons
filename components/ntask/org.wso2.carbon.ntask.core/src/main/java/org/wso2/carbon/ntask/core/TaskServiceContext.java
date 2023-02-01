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
package org.wso2.carbon.ntask.core;

import org.wso2.carbon.ntask.common.TaskException;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

/**
 * This class represents a runtime context of the task service.
 */
public class TaskServiceContext {

    private TaskRepository taskRepo;

    private List<String> memberIds;

    private static final String LOCAL_MEMBER_IDENTIFIER = "localMemberIdentifier";

    public int getTenantId() {
        return this.taskRepo.getTenantId();
    }

    public String getTaskType() {
        return this.taskRepo.getTasksType();
    }

    public List<TaskInfo> getTasks() throws TaskException {
        return this.taskRepo.getAllTasks();
    }

    public int getServerCount() {
        return this.memberIds.size();
    }
    
    public InetSocketAddress getServerAddress(int index) {
        // Converting to an empty method with the Hazelcast removal effort. 
        // Need to remove these empty methods later in another effort after analysing unused methods and modules in project repositories.
        return null;
    }

    public String getServerIdentifier(int index) {
        // Converting to an empty method with the Hazelcast removal effort. 
        // Need to remove these empty methods later in another effort after analysing unused methods and modules in project repositories.
        return null;
    }
}
