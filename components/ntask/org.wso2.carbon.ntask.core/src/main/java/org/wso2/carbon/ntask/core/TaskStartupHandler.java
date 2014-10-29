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

import org.wso2.carbon.core.ServerStartupHandler;
import org.wso2.carbon.ntask.core.service.TaskService;

/**
 * A server startup handler implementation which is used as a listener to know when to start
 * scheduling the tasks.
 */
public class TaskStartupHandler implements ServerStartupHandler {

    private TaskService taskService;

    public TaskStartupHandler(TaskService taskService) {
        this.taskService = taskService;
    }

    public TaskService getTaskService() {
        return taskService;
    }

    @Override
    public void invoke() {
        this.getTaskService().serverInitialized();
    }

}
