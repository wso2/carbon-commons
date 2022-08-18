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
package org.wso2.carbon.ntask.core.impl.remote;

import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.ClusteringCommand;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.common.TaskException.Code;
import org.wso2.carbon.ntask.core.Task;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.TaskRepository;
import org.wso2.carbon.ntask.core.impl.LocalTaskActionListener;
import org.wso2.carbon.ntask.core.impl.RegistryBasedTaskRepository;
import org.wso2.carbon.ntask.core.internal.TasksDSComponent;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.remotetasks.stub.admin.common.RemoteTaskAdmin;
import org.wso2.carbon.remotetasks.stub.admin.common.xsd.DeployedTaskInformation;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class represents a remote task manager implementation.
 */
public class RemoteTaskManager implements TaskManager {

    private static final Log log = LogFactory.getLog(RemoteTaskManager.class);

    public static final String REMOTE_TASK_SERVER_ADDRESS = "task.server.remote.address";

    public static final String REMOTE_TASK_SERVER_USERNAME = "task.server.remote.username";

    public static final String REMOTE_TASK_SERVER_PASSWORD = "task.server.remote.password";

    public static final String TASK_CLIENT_DISPATCH_ADDRESS = "task.client.dispatch.address";

    public static final String REMOTE_TASK_ID_REPO_PROP = "REMOTE_TASK_ID_REPO_PROP";

    private TaskRepository taskRepository;

    private RemoteTaskAdmin remoteTaskAdmin;

    private static Map<String, Integer> runningTasksMap = new ConcurrentHashMap<String, Integer>();

    public RemoteTaskManager(TaskRepository taskRepository, RemoteTaskAdmin remoteTaskAdmin) {
        this.taskRepository = taskRepository;
        this.remoteTaskAdmin = remoteTaskAdmin;
    }

    public RemoteTaskAdmin getRemoteTaskAdmin() {
        return remoteTaskAdmin;
    }

    public TaskRepository getTaskRepository() {
        return taskRepository;
    }

    public int getTenantId() {
        return this.getTaskRepository().getTenantId();
    }

    public String getTaskType() {
        return this.getTaskRepository().getTasksType();
    }

    @Override
    public void initStartupTasks() throws TaskException {
        for (TaskInfo taskInfo : this.getAllTasks()) {
            try {
                this.scheduleTask(taskInfo.getName());
            } catch (Exception e) {
                log.error("Error in scheduling task '" + taskInfo.getName() 
                        + "': " + e.getMessage(), e);
            }
        }
    }

    private void setRemoteTaskIdForTask(String taskName, String remoteTaskId) throws TaskException {
        this.getTaskRepository().setTaskMetadataProp(taskName, REMOTE_TASK_ID_REPO_PROP,
                remoteTaskId);
    }

    @Override
    public void scheduleTask(String taskName) throws TaskException {
        Registry registry = RegistryBasedTaskRepository.getRegistry();
        try {
            registry.beginTransaction();
            String remoteTaskId = RemoteTaskUtils.createRemoteTaskMapping(this.getTenantId(),
                    this.getTaskType(), taskName);
            this.setRemoteTaskIdForTask(taskName, remoteTaskId);
            TaskInfo taskInfo = this.getTaskRepository().getTask(taskName);
            registry.commitTransaction();
            try {
                this.getRemoteTaskAdmin().addRemoteSystemTask(
                        RemoteTaskUtils.convert(taskInfo, this.getTaskType(), remoteTaskId,
                        this.getTenantId()), this.getTenantId());
                } catch (Exception e) {
                    throw new TaskException(e.getMessage(), Code.UNKNOWN, e);
            }
        } catch (RegistryException e) {
            log.error("Error in  retrieving registry : " + e.getMessage(), e);
        }
    }

    @Override
    public void rescheduleTask(String taskName) throws TaskException {
        this.deleteTask(taskName, false);
        this.scheduleTask(taskName);
    }

    @Override
    public boolean deleteTask(String taskName) throws TaskException {
        return this.deleteTask(taskName, true);
    }

    private String getRemoteTaskId(String taskName) throws TaskException {
        return this.getTaskRepository().getTaskMetadataProp(taskName, REMOTE_TASK_ID_REPO_PROP);
    }

    private boolean deleteTask(String taskName, boolean removeFromRepo) throws TaskException {
        try {
            boolean result = this.getRemoteTaskAdmin().deleteRemoteSystemTask(
                    RemoteTaskUtils.remoteTaskNameFromTaskInfo(this.getTaskType(), taskName),
                    this.getTenantId());
            Registry registry = RegistryBasedTaskRepository.getRegistry();
            registry.beginTransaction();
            /* remove the remote task id mapping */
            String remoteTaskId = this.getRemoteTaskId(taskName);
            if (remoteTaskId != null) {
                RemoteTaskUtils.removeRemoteTaskMapping(remoteTaskId);
            }
            if (removeFromRepo) {
                result &= this.getTaskRepository().deleteTask(taskName);
            }
            registry.commitTransaction();
            return result;
        } catch (Exception e) {
            throw new TaskException(e.getMessage(), Code.UNKNOWN, e);
        }
    }

    @Override
    public void pauseTask(String taskName) throws TaskException {
        try {
            this.getRemoteTaskAdmin().pauseRemoteSystemTask(
                    RemoteTaskUtils.remoteTaskNameFromTaskInfo(this.getTaskType(), taskName),
                    this.getTenantId());
        } catch (Exception e) {
            throw new TaskException(e.getMessage(), Code.UNKNOWN, e);
        }
    }

    @Override
    public void resumeTask(String taskName) throws TaskException {
        try {
            this.getRemoteTaskAdmin().resumeRemoteSystemTask(
                    RemoteTaskUtils.remoteTaskNameFromTaskInfo(this.getTaskType(), taskName),
                    this.getTenantId());
        } catch (Exception e) {
            throw new TaskException(e.getMessage(), Code.UNKNOWN, e);
        }
    }

    @Override
    public void registerTask(TaskInfo taskInfo) throws TaskException {
        String taskName = taskInfo.getName();
        /*
         * get the remote task id if the task is already existing, because this
         * id will be overridden when the task is updated
         */
        String remoteTaskId = this.getRemoteTaskId(taskName);
        this.getTaskRepository().addTask(taskInfo);
        if (remoteTaskId != null) {
            /* restore the remote task id */
            this.setRemoteTaskIdForTask(taskName, remoteTaskId);
        }
    }

    public TaskState getTaskStateRemote(String taskName) throws TaskException {
        try {
            DeployedTaskInformation depTaskInfo = this.getRemoteTaskAdmin().getRemoteSystemTask(
                    RemoteTaskUtils.remoteTaskNameFromTaskInfo(this.getTaskType(), taskName),
                    this.getTenantId());
            if (depTaskInfo == null) {
                return TaskState.NONE;
            }
            TaskState ts = TaskState.valueOf(depTaskInfo.getStatus());
            return ts;
        } catch (Exception e) {
            throw new TaskException(e.getMessage(), Code.UNKNOWN, e);
        }
    }

    private ClusteringAgent getClusteringAgent() throws TaskException {
        ConfigurationContextService configCtxService = TasksDSComponent
                .getConfigurationContextService();
        if (configCtxService == null) {
            throw new TaskException("ConfigurationContextService not available "
                    + "for notifying the cluster", Code.UNKNOWN);
        }
        ConfigurationContext configCtx = configCtxService.getServerConfigContext();
        ClusteringAgent agent = configCtx.getAxisConfiguration().getClusteringAgent();
        if (log.isDebugEnabled()) {
            log.debug("Clustering Agent: " + agent);
        }
        return agent;
    }

    private TaskState getTaskStateFromLocalCluster(String taskName) throws TaskException {
        /* first check local server */
        if (this.isTaskRunning(taskName)) {
            return TaskState.BLOCKED;
        }
        ClusteringAgent agent = this.getClusteringAgent();
        if (agent == null) {
            return TaskState.UNKNOWN;
        }
        TaskStatusMessage msg = new TaskStatusMessage();
        msg.setTaskName(taskName);
        msg.setTaskType(this.getTaskType());
        msg.setTenantId(this.getTenantId());
        try {
            List<ClusteringCommand> result = agent.sendMessage(msg, true);
            TaskStatusResult status;
            for (ClusteringCommand entry : result) {
                status = (TaskStatusResult) entry;
                if (status.isRunning()) {
                    return TaskState.BLOCKED;
                }
            }
            return TaskState.NORMAL;
        } catch (ClusteringFault e) {
            throw new TaskException(e.getMessage(), Code.UNKNOWN, e);
        }
    }

    @Override
    public TaskState getTaskState(String taskName) throws TaskException {
        TaskState taskState = this.getTaskStateRemote(taskName);
        if (taskState == TaskState.NORMAL) {
            taskState = this.getTaskStateFromLocalCluster(taskName);
        }
        return taskState;
    }

    @Override
    public TaskInfo getTask(String taskName) throws TaskException {
        return this.getTaskRepository().getTask(taskName);
    }

    @Override
    public List<TaskInfo> getAllTasks() throws TaskException {
        return this.getTaskRepository().getAllTasks();
    }

    @Override
    public boolean isTaskScheduled(String taskName) throws TaskException {
        return this.getTaskState(taskName) != TaskState.NONE;
    }

    private boolean isMyTaskTypeRegistered() {
        return TasksDSComponent.getTaskService().getRegisteredTaskTypes().contains(this.getTaskType());
    }
    
    public void runTask(String taskName) throws TaskException {
        if (this.isMyTaskTypeRegistered()) {
            TasksDSComponent.executeTask(new TaskExecution(taskName));
        } else {
            throw new TaskException("Task type: '" + this.getTaskType() + 
                    "' is not registered in the current task node", Code.TASK_NODE_NOT_AVAILABLE);
        }
    }

    public static void addRunningTask(String runningTaskId) {
        synchronized (runningTasksMap) {
            Integer value = runningTasksMap.get(runningTaskId);
            if (value != null) {
                value++;
            } else {
                value = 1;
            }
            runningTasksMap.put(runningTaskId, value);
        }
    }

    public static void removeRunningTask(String runningTaskId) {
        synchronized (runningTasksMap) {
            Integer value = runningTasksMap.get(runningTaskId);
            if (value != null) {
                value--;
                if (value <= 0) {
                    runningTasksMap.remove(runningTaskId);
                } else {
                    runningTasksMap.put(runningTaskId, value);
                }
            }
        }
    }

    public static boolean isRunningTaskExist(String runningTaskId) {
        return runningTasksMap.containsKey(runningTaskId);
    }

    public boolean isTaskRunning(String taskName) throws TaskException {
        return isRunningTaskExist(this.generateRunningTaskId(taskName));
    }

    private String generateRunningTaskId(String taskName) {
        return this.getTenantId() + "#" + this.getTaskType() + "#" + taskName;
    }

    private class TaskExecution implements Runnable {

        private String taskName;

        public TaskExecution(String taskName) {
            this.taskName = taskName;
        }

        public String getTaskName() {
            return taskName;
        }

        private boolean isTaskRunningInCluster() throws TaskException {
            return getTaskStateFromLocalCluster(this.getTaskName()) == TaskState.BLOCKED;
        }

        @Override
        public void run() {
            String runningTaskId = generateRunningTaskId(this.getTaskName());
            /*
             * the try/catch/finally ordering is there for a reason, do not mess
             * with it!
             */
            try {
                TaskInfo taskInfo = getTaskRepository().getTask(this.getTaskName());
                if (taskInfo.getTriggerInfo().isDisallowConcurrentExecution()
                        && this.isTaskRunningInCluster()) {
                    /*
                     * this check is done here instead of outside is because,
                     * the repository lookup is anyway happens here and it is
                     * expensive for it to be done again
                     */
                    return;
                }
                try {
                    addRunningTask(runningTaskId);
                    Task task = (Task) Class.forName(taskInfo.getTaskClass()).newInstance();
                    task.setProperties(taskInfo.getProperties());
                    try {
                        PrivilegedCarbonContext.startTenantFlow();
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(
                                getTenantId(), true);
                        task.init();
                        task.execute();
                    } finally {
                        PrivilegedCarbonContext.endTenantFlow();
                    }
                } finally {
                    removeRunningTask(runningTaskId);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

    }

    /**
     * This class represents the cluster message for retrieving a task status.
     */
    public static class TaskStatusMessage extends ClusteringMessage {

        private static final long serialVersionUID = 8904018070655665868L;

        private int tenantId;

        private String taskType;

        private String taskName;

        private TaskStatusResult result;

        public int getTenantId() {
            return tenantId;
        }

        public void setTenantId(int tenantId) {
            this.tenantId = tenantId;
        }

        public String getTaskType() {
            return taskType;
        }

        public void setTaskType(String taskType) {
            this.taskType = taskType;
        }

        public String getTaskName() {
            return taskName;
        }

        public void setTaskName(String taskName) {
            this.taskName = taskName;
        }

        @Override
        public ClusteringCommand getResponse() {
            return this.result;
        }

        @Override
        public void execute(ConfigurationContext ctx) throws ClusteringFault {
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(this.getTenantId(), true);
                TaskManager tm = TasksDSComponent.getTaskService().getTaskManager(
                        this.getTaskType());
                if (tm instanceof RemoteTaskManager) {
                    this.result = new TaskStatusResult();
                    this.result.setRunning(((RemoteTaskManager) tm).isTaskRunning(this
                            .getTaskName()));
                }
            } catch (Exception e) {
                throw new ClusteringFault(e.getMessage(), e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

    }

    /**
     * This class represents a clustering message result for task statuses.
     */
    public static class TaskStatusResult extends ClusteringCommand {

        private static final long serialVersionUID = 4982249263193601405L;

        private boolean running;

        public boolean isRunning() {
            return running;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        @Override
        public void execute(ConfigurationContext ctx) throws ClusteringFault {
        }

    }

    @Override
    public void registerLocalTaskActionListener(LocalTaskActionListener listener, String taskName) {
        //Do nothing since this is the remote task manager and there are no local tasks associated to it.
    }

}
