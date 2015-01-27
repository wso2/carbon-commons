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
package org.wso2.carbon.ntask.core.impl.standalone;

import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.TaskManagerFactory;
import org.wso2.carbon.ntask.core.TaskManagerId;
import org.wso2.carbon.ntask.core.TaskRepository;
import org.wso2.carbon.ntask.core.impl.RegistryBasedTaskRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * This represents the standalone task manager factory class.
 */
public class StandaloneTaskManagerFactory implements TaskManagerFactory {

    @Override
    public TaskManager getTaskManager(TaskManagerId tmId) throws TaskException {
        /* the best effort is made to not to cache the task managers, since the tenant loading/unloading/
         * relocation would make the stored cache managers invalid, and won't be gc'ed */
        return this.createTaskManager(tmId);
    }

    protected TaskManager createTaskManager(TaskManagerId tmId) throws TaskException {
        TaskRepository taskRepo = new RegistryBasedTaskRepository(tmId.getTenantId(),
                tmId.getTaskType());
        return new StandaloneTaskManager(taskRepo);
    }

    @Override
    public List<TaskManager> getStartupSchedulingTaskManagersForType(String taskType)
            throws TaskException {
        List<TaskManagerId> tmIds = RegistryBasedTaskRepository
                .getAllTenantTaskManagersForType(taskType);
        List<TaskManager> result = new ArrayList<TaskManager>();
        for (TaskManagerId tmId : tmIds) {
            result.add(this.createTaskManager(tmId));
        }
        return result;
    }

    @Override
    public List<TaskManager> getAllTenantTaskManagersForType(String taskType) throws TaskException {
        List<TaskManagerId> tmIds = RegistryBasedTaskRepository
                .getAllTenantTaskManagersForType(taskType);
        List<TaskManager> result = new ArrayList<TaskManager>();
        for (TaskManagerId tmId : tmIds) {
            result.add(this.createTaskManager(tmId));
        }
        return result;
    }

}
