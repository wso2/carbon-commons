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
package org.wso2.carbon.ntask.core.impl.clustered;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.common.TaskException.Code;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskRepository;
import org.wso2.carbon.ntask.core.TaskServiceContext;
import org.wso2.carbon.ntask.core.TaskUtils;
import org.wso2.carbon.ntask.core.impl.AbstractQuartzTaskManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a clustered task manager, which is used when tasks are
 * distributed across a cluster.
 */
public class ClusteredTaskManager extends AbstractQuartzTaskManager {

    private static final Log log = LogFactory.getLog(ClusteredTaskManager.class);

    /**
     * List to maintain newly registered tasks to use when removing invalid tasks remaining in the database.
     */
    private static List<String> newlyRegisteredTasks = new ArrayList<>();

    public ClusteredTaskManager(TaskRepository taskRepository) throws TaskException {
        super(taskRepository);
    }

    public int getTenantId() {
        return this.getTaskRepository().getTenantId();
    }

    public String getTaskType() {
        return this.getTaskRepository().getTasksType();
    }

    /**
     * {@inheritDoc}
     */
    public void initStartupTasks() throws TaskException {
        cleanupLeftoverTasks();
    }

    /**
     * Method to clean up tasks leftover from a previous setup.
     *
     * @throws TaskException if there is an error retrieving/deleting tasks
     */
    private void cleanupLeftoverTasks() throws TaskException {
        List<TaskInfo> allTasks = this.getAllTasks();
        List<String> deletedTasks = new ArrayList<>();
        String taskName;
        for (TaskInfo taskInfo : allTasks) {
            taskName = taskInfo.getName();
            if (!newlyRegisteredTasks.contains(taskName)) {
                deleteTask(taskName);
                deletedTasks.add(taskName);
            }
        }
        if (!deletedTasks.isEmpty()) {
            for (String deletedTask : deletedTasks) {
                log.info("Deleted leftover task: " + deletedTask);
            }
        }
    }

    public void scheduleTask(String taskName) throws TaskException {
        this.scheduleLocalTask(taskName);
    }

    /**
     * {@inheritDoc}
     */
    public void scheduleTask(String taskName, boolean requestsRecovery) throws TaskException {
        this.scheduleLocalTask(taskName, requestsRecovery);
    }

    public void rescheduleTask(String taskName) throws TaskException {
        this.rescheduleLocalTask(taskName);
    }

    public Map<String, TaskState> getAllTaskStates() throws TaskException {
        try {
            List<TaskInfo> tasks = this.getAllTasks();
            Map<String, TaskState> result = new HashMap<String, TaskState>();
            for (TaskInfo task : tasks) {
                result.put(task.getName(), this.getTaskState(task.getName()));
            }
            return result;
        } catch (Exception e) {
            throw new TaskException("Error in getting all task states: " + e.getMessage(),
                    Code.UNKNOWN, e);
        }
    }

    public TaskState getTaskState(String taskName) throws TaskException {
        return getLocalTaskState(taskName);
    }

    public boolean deleteTask(String taskName) throws TaskException {
        boolean result = true;
        result = this.deleteLocalTask(taskName, false);
        /* the delete from repository has to be done here, because, this would be the admin node
         * with read/write registry access, and the target slave will not have write access */
        result &= this.getTaskRepository().deleteTask(taskName);
        return result;        
    }

    public void pauseTask(String taskName) throws TaskException {
        this.pauseLocalTask(taskName);
        TaskUtils.setTaskPaused(this.getTaskRepository(), taskName, true);
    }

    public void resumeTask(String taskName) throws TaskException {
        this.resumeLocalTask(taskName);
        TaskUtils.setTaskPaused(this.getTaskRepository(), taskName, false);
    }

    @Override
    public void registerTask(TaskInfo taskInfo) throws TaskException {
        this.registerLocalTask(taskInfo);
        newlyRegisteredTasks.add(taskInfo.getName());
    }

    @Override
    public TaskInfo getTask(String taskName) throws TaskException {
        return this.getTaskRepository().getTask(taskName);
    }

    @Override
    public List<TaskInfo> getAllTasks() throws TaskException {
        return this.getTaskRepository().getAllTasks();
    }

    private TaskServiceContext getTaskServiceContext() throws TaskException {
        TaskServiceContext context = new TaskServiceContext(this.getTaskRepository());
        return context;
    }

    @Override
    public boolean isTaskScheduled(String taskName) throws TaskException {
        return this.getTaskState(taskName) != TaskState.NONE;
    }

    public List<TaskInfo> getFinalRunningTasksInServer() throws Exception {
        return getAllLocalRunningTasks();
    }

    public TaskState getFinalTaskState(String taskName) throws Exception {
        return getLocalTaskState(taskName);
    }

    public void finalScheduleTask(String taskName) throws Exception {
        this.scheduleLocalTask(taskName);        
    }

    public void finalRescheduleTask(String taskName) throws Exception {
        rescheduleLocalTask(taskName);
    }

    public boolean finalDeleteTask(String taskName) throws Exception {
        return deleteLocalTask(taskName, false);
    }

    public void finalPauseTask(String taskName) throws Exception {
        pauseLocalTask(taskName);
    }

    public void finalResumeTask(String taskName) throws Exception {
        resumeLocalTask(taskName);
    }

    public void deleteLocalTasks() throws TaskException {
        super.deleteLocalTasks();
    }

}
