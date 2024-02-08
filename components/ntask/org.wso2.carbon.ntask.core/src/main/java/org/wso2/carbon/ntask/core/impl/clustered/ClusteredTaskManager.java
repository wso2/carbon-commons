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
import org.wso2.carbon.ntask.core.*;
import org.wso2.carbon.ntask.core.impl.AbstractQuartzTaskManager;
import org.wso2.carbon.ntask.core.impl.clustered.rpc.*;
import org.wso2.carbon.ntask.core.internal.TasksDSComponent;
import org.wso2.carbon.ntask.core.service.TaskService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

/**
 * This class represents a clustered task manager, which is used when tasks are
 * distributed across a cluster.
 */
public class ClusteredTaskManager extends AbstractQuartzTaskManager {

    private static final String TASK_MEMBER_LOCATION_META_PROP_ID = "TASK_MEMBER_LOCATION_META_PROP_ID";
    private static final int TASKSERVICE_AVAILABILITY_CHECK_RETRY_COUNT = 10;
    private final TaskService taskService;

    private static final Log log = LogFactory.getLog(ClusteredTaskManager.class);

    public ClusteredTaskManager(TaskRepository taskRepository) throws TaskException {
        super(taskRepository);
        this.taskService = TasksDSComponent.getTaskService();
    }

    public int getTenantId() {
        return this.getTaskRepository().getTenantId();
    }

    public String getTaskType() {
        return this.getTaskRepository().getTasksType();
    }

    public ClusterGroupCommunicator getClusterComm() throws TaskException {
        return ClusterGroupCommunicator.getInstance(this.getTaskType());
    }

    public void initStartupTasks() throws TaskException {
        if (this.isLeader()) {
            try {
                this.scheduleMissingTasks();
            } catch (TaskException e) {
                log.error("Encountered error(s) in scheduling missing tasks ["
                        + this.getTaskType() + "][" + this.getTenantId() + "]:-\n" +
                        e.getMessage() + "\n");
            }
        }
    }

    public void scheduleMissingTasks() throws TaskException {
        List<List<TaskInfo>> tasksInServers = this.getAllRunningTasksInServers();
        List<TaskInfo> scheduledTasks = new ArrayList<TaskInfo>();
        for (List<TaskInfo> entry : tasksInServers) {
            scheduledTasks.addAll(entry);
        }
        /* add already finished tasks */
        scheduledTasks.addAll(this.getAllFinishedTasks());
        List<TaskInfo> allTasks = this.getAllTasks();
        List<TaskInfo> missingTasks = new ArrayList<TaskInfo>(allTasks);
        missingTasks.removeAll(scheduledTasks);
        StringBuilder errors = new StringBuilder();
        boolean error = false;
        for (TaskInfo task : missingTasks) {
            try {
                this.scheduleTask(task.getName());
            } catch (Exception e) {
                errors.append(e.getMessage() + "\n");
                error = true;
            }
        }
        if (error) {
            throw new TaskException(errors.toString(), Code.UNKNOWN);
        }
    }

    /**
     * Schedules tasks for the given member id
     *
     * @param memberId cluster member identifier
     * @throws TaskException
     */
    public void scheduleMissingTasks(String memberId) throws TaskException {
        List<TaskInfo> scheduledTasks = this.getAllRunningTasksInOtherMembers(memberId);
        // add already finished tasks
        scheduledTasks.addAll(this.getAllFinishedTasks());
        List<TaskInfo> allTasks = this.getAllTasks();
        List<TaskInfo> missingTasks = new ArrayList<TaskInfo>(allTasks);
        missingTasks.removeAll(scheduledTasks);
        StringBuilder errors = new StringBuilder();
        boolean error = false;
        for (TaskInfo task : missingTasks) {
            try {
                if (this.isTaskServiceAvailable(task.getName())) {
                    this.scheduleTask(task.getName());
                } else {
                    throw new TaskException("The task service not available for the task: " + task.getName(),
                            Code.UNKNOWN);
                }
            } catch (Exception e) {
                errors.append(e.getMessage() + "\n");
                error = true;
            }
        }
        if (error) {
            throw new TaskException(errors.toString(), Code.UNKNOWN);
        }
    }

    /**
     * Returns the state of the task service for the task server at the task schedule time
     *
     * @param taskName name of the task
     * @return true if the task service is activated for the task server at the task schedule time
     */
    private boolean isTaskServiceAvailable(String taskName) {
        int count = taskService.getServerConfiguration().getRetryCount();
        long retryInterval = taskService.getServerConfiguration().getRetryInterval();
        while (count > 0) {
            try {
                TaskState taskState = getTaskState(taskName);
                if (taskState.equals(TaskState.NONE)) {
                    this.scheduleTask(taskName);
                }
                return true;
            } catch (TaskException e) {
                try {
                    Thread.sleep(retryInterval);
                } catch (InterruptedException ex) {
                    //ignore
                }
            }
            count--;
        }
        return false;
    }

    public void scheduleTask(String taskName) throws TaskException {
        String taskLockId = this.getTaskType() + "_" + this.getTenantId() + "_" + taskName;
        // remove hazelcast
        String memberId = this.getMemberIdFromTaskName(taskName, true);
        this.setServerLocationOfTask(taskName, memberId);
        this.scheduleTask(memberId, taskName);

    }

    public void rescheduleTask(String taskName) throws TaskException {
        String memberId = this.getMemberIdFromTaskName(taskName, true);
        this.setServerLocationOfTask(taskName, memberId);
        this.rescheduleTask(memberId, taskName); 
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
        try {
            String memberId = this.getMemberIdFromTaskName(taskName, false);
            return this.getTaskState(memberId, taskName);
        } catch (TaskException e) {
            if (e.getCode().equals(Code.NO_TASK_EXISTS)) {
                return TaskState.NONE;
            } else {
                throw e;
            }
        } catch (Exception e) {
            throw new TaskException("Error in getting task state: " + taskName + " : "
                    + e.getMessage(), Code.UNKNOWN, e);
        }
    }

    public boolean deleteTask(String taskName) throws TaskException {
        boolean result = true;
        String memberId = null;
        String taskLockId = this.getTaskType() + "_" + this.getTenantId() + "_" + taskName;
        try {
            memberId = this.getMemberIdFromTaskName(taskName, false);
            String localMemberId = getMemberId();
            // deletion of tasks which are scheduled in other nodes are skipped
            if (localMemberId.equals(memberId)) {
                result = this.deleteTask(memberId, taskName);
                // delete from repository has to be done here, because, this would be the local node
                // with read/write registry access, and the remote node will not have write access
                result &= this.getTaskRepository().deleteTask(taskName);
            } else {
                result = false;
                log.info("The task " + taskName + " is not scheduled in this node, hence deletion is skipped.");
            }
        } catch (TaskException e) {
            /* if the task is not scheduled anywhere, we can ignore this delete request to the
             * remote server */
            if (!Code.NO_TASK_EXISTS.equals(e.getCode())) {
                throw e;
            }
        } catch (Exception e) {
            throw new TaskException("Error in deleting task: " + taskName + " : " + e.getMessage(),
                                    Code.UNKNOWN, e);
        }
        return result;        
    }

    public void pauseTask(String taskName) throws TaskException {
        String memberId = this.getMemberIdFromTaskName(taskName, false);
        this.pauseTask(memberId, taskName);
        TaskUtils.setTaskPaused(this.getTaskRepository(), taskName, true);
    }

    public void resumeTask(String taskName) throws TaskException {
        String memberId = this.getMemberIdFromTaskName(taskName, false);
        this.resumeTask(memberId, taskName);
        TaskUtils.setTaskPaused(this.getTaskRepository(), taskName, false);
    }

    @Override
    public void registerTask(TaskInfo taskInfo) throws TaskException {
        /* if the task registration already exists, we have to make sure we save the current location
         * of the task, since this is a task registration update, we will want to schedule the task
         * in the same server as earlier */
        String taskLockId = this.getTaskType() + "_" + this.getTenantId() + "_" + taskInfo.getName();
        // removed hazelcast
        String locationId = this.getTaskRepository().getTaskMetadataProp(
                taskInfo.getName(), TASK_MEMBER_LOCATION_META_PROP_ID);
        this.registerLocalTask(taskInfo);
        if (locationId != null) {
            this.getTaskRepository().setTaskMetadataProp(taskInfo.getName(),
                    TASK_MEMBER_LOCATION_META_PROP_ID, locationId);
        }

    }

    @Override
    public TaskInfo getTask(String taskName) throws TaskException {
        return this.getTaskRepository().getTask(taskName);
    }

    @Override
    public List<TaskInfo> getAllTasks() throws TaskException {
        return this.getTaskRepository().getAllTasks();
    }

    public int getServerCount() throws TaskException {
        return this.getMemberIds().size();
    }

    private TaskServiceContext getTaskServiceContext() throws TaskException {
        // removed hazelcast
        return null;
    }

    private String locateMemberForTask(String taskName) throws TaskException {
        int location = getTaskLocation(taskName);
        List<String> ids;
        try {
            ids = this.getMemberIds();
        } catch (Exception e) {
            throw new TaskException("Error in getting member ids: " + e.getMessage(), Code.UNKNOWN,
                    e);
        }
        int index = location % ids.size();
        return ids.get(index);
    }

    private int getTaskLocation(String taskName) throws TaskException {
        TaskInfo taskInfo = this.getTask(taskName);
        TaskLocationResolver locationResolver;
        try {
            locationResolver = (TaskLocationResolver) Class.forName(
                    taskInfo.getLocationResolverClass()).newInstance();
        } catch (Exception e) {
            throw new TaskException(e.getMessage(), Code.UNKNOWN, e);
        }
        TaskServiceContext ctx = this.getTaskServiceContext();
        if (ctx.getServerCount() == 0) {
            throw new TaskException("No available task nodes for resolving a task location", 
                    Code.TASK_NODE_NOT_AVAILABLE);
        }
        Map<String, String> props = taskInfo.getLocationResolverProperties();
        if (props == null) {
        	props = new HashMap<String, String>();
        }
        locationResolver.init(props);
        return locationResolver.getLocation(ctx, taskInfo);
    }

    /**
     * Gets all the tasks running on the other task server members of the cluster
     *
     * @param memberId cluster member identifier
     * @return task list of the running tasks
     * @throws TaskException
     */
    public List<TaskInfo> getAllRunningTasksInOtherMembers(String memberId) throws TaskException {
        List<TaskInfo> results = new ArrayList<>();
        List<String> ids = this.getMemberIds();
        for (String id : ids) {
            if (!id.equals(memberId)) {
                results.addAll(this.getRunningTasksInServer(id));
            }
        }
        return results;
    }

    public List<List<TaskInfo>> getAllRunningTasksInServers() throws TaskException {
        List<List<TaskInfo>> result = new ArrayList<List<TaskInfo>>();
        List<String> ids = this.getMemberIds();
        for (String id : ids) {
            result.add(this.getRunningTasksInServer(id));
        }
        return result;
    }

    @Override
    public boolean isTaskScheduled(String taskName) throws TaskException {
        return this.getTaskState(taskName) != TaskState.NONE;
    }

    public <V> V sendReceive(String memberId, TaskCall<V> taskCall)
            throws TaskException {
        /* the tenant domain and task type are populated here, instead of giving them in
         * the constructor of each TaskClusterCall classes */
        taskCall.setTenantId(this.getTenantId());
        taskCall.setTaskType(this.getTaskType());
        return this.getClusterComm().sendReceive(memberId, taskCall);
    }

    public List<String> getMemberIds() throws TaskException {
        return this.getClusterComm().getMemberIds();
    }

    public String getMemberId() throws TaskException {
        // removed hazelcast
        return null;
    }

    public boolean isLeader() throws TaskException {
        return this.getClusterComm().isLeader();
    }

    public String getMemberIdFromTaskName(String taskName, boolean createIfNotExists)
            throws TaskException {
        String location = this.getServerLocationOfTask(taskName);
        if (location == null || !this.getMemberIds().contains(location)) {
            if (createIfNotExists) {
                location = this.locateMemberForTask(taskName);
            } else {
                throw new TaskException("The task server cannot be located for task: " + taskName,
                        Code.NO_TASK_EXISTS);
            }
        }
        return location;
    }

    public List<TaskInfo> getRunningTasksInServer(String memberId) throws TaskException {
        return this.sendReceive(memberId, new RunningTasksInServerCall());
    }

    public List<TaskInfo> getFinalRunningTasksInServer() throws Exception {
        return getAllLocalRunningTasks();
    }

    public TaskState getTaskState(String memberId, String taskName) throws Exception {
        return this.sendReceive(memberId, new TaskStateCall(taskName));
    }

    public TaskState getFinalTaskState(String taskName) throws Exception {
        return getLocalTaskState(taskName);
    }

    public void scheduleTask(String memberId, String taskName) throws TaskException {
        this.sendReceive(memberId, new ScheduleTaskCall(taskName));
    }

    public void finalScheduleTask(String taskName) throws Exception {
        this.scheduleLocalTask(taskName);        
    }

    public void rescheduleTask(String memberId, String taskName) throws TaskException {
        this.sendReceive(memberId, new RescheduleTaskCall(taskName));
    }

    public void finalRescheduleTask(String taskName) throws Exception {
        rescheduleLocalTask(taskName);
    }

    public boolean deleteTask(String memberId, String taskName) throws Exception {
        return this.sendReceive(memberId, new DeleteTaskCall(taskName));
    }

    public boolean finalDeleteTask(String taskName) throws Exception {
        return deleteLocalTask(taskName, false);
    }

    public void pauseTask(String memberId, String taskName) throws TaskException {
        this.sendReceive(memberId, new PauseTaskCall(taskName));
    }

    public void finalPauseTask(String taskName) throws Exception {
        pauseLocalTask(taskName);
    }

    public void resumeTask(String memberId, String taskName) throws TaskException {
        this.sendReceive(memberId, new ResumeTaskCall(taskName));
    }

    public void finalResumeTask(String taskName) throws Exception {
        resumeLocalTask(taskName);
    }

    private void setServerLocationOfTask(String taskName, String memberId) throws TaskException {
        this.getTaskRepository().setTaskMetadataProp(taskName, TASK_MEMBER_LOCATION_META_PROP_ID,
                memberId);
    }

    private String getServerLocationOfTask(String taskName) throws TaskException {
        return this.getTaskRepository().getTaskMetadataProp(taskName,
                TASK_MEMBER_LOCATION_META_PROP_ID);
    }

    public void deleteLocalTasks() throws TaskException {
        super.deleteLocalTasks();
    }

}
