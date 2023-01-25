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
package org.wso2.carbon.ntask.core.impl.clustered;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.common.TaskException.Code;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.impl.clustered.rpc.TaskCall;
import org.wso2.carbon.ntask.core.internal.TasksDSComponent;
import org.wso2.carbon.ntask.core.service.TaskService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * This class represents the cluster group communicator used by clustered task
 * managers.
 */
public class ClusterGroupCommunicator {

    public static final String NTASK_P2P_COMM_EXECUTOR = "__NTASK_P2P_COMM_EXECUTOR__";

    private static final String TASK_SERVER_STARTUP_COUNTER = "__TASK_SERVER_STARTUP_COUNTER__";

    private static final int MISSING_TASKS_ON_ERROR_RETRY_COUNT = 3;

    private static final String CARBON_TASKS_MEMBER_ID_MAP = "__CARBON_TASKS_MEMBER_ID_MAP__";

    public static final String TASK_SERVER_COUNT_SYS_PROP = "task.server.count";

    private static final Log log = LogFactory.getLog(ClusterGroupCommunicator.class);

    private TaskService taskService;

    private static Map<String, ClusterGroupCommunicator> communicatorMap = new HashMap<String, ClusterGroupCommunicator>();

    private String taskType;

    public static ClusterGroupCommunicator getInstance(String taskType) throws TaskException {
        if (communicatorMap.containsKey(taskType)) {
            return communicatorMap.get(taskType);
        } else {
            synchronized (communicatorMap) {
                if (!communicatorMap.containsKey(taskType)) {
                    communicatorMap.put(taskType, new ClusterGroupCommunicator(taskType));
                }
                return communicatorMap.get(taskType);
            }
        }
    }

    private ClusterGroupCommunicator(String taskType) throws TaskException {
        this.taskType = taskType;
        this.taskService = TasksDSComponent.getTaskService();
        throw new TaskException("ClusterGroupCommunicator cannot initialize, " +
            		"Hazelcast is not initialized", Code.CONFIG_ERROR);
    }
    
    private void refreshMembers() {
        this.checkAndRemoveExpiredMembers();
    }
    
    public void addMyselfToGroup() {
    	// Converting to an empty method with the Hazelcast removal effort. 
        // Need to remove these empty methods later in another effort after analysing unused methods and modules in project repositories.
    }
    
    /**
     * This method checks and removes older non-existing nodes from the map. This can happen
     * when there are multiple servers, for example a server which has ntask component and another server
     * which just has hazelcast. When the server with ntask shutsdown, the other server still contain the
     * queue, and that queue contain the member id of the earlier server which had ntask. So at startup,
     * we have to check the queue and see if there are non-existing members by comparing it to the current
     * list of active members.
     */
    private void checkAndRemoveExpiredMembers() {
        // Converting to an empty method with the Hazelcast removal effort. 
        // Need to remove these empty methods later in another effort after analysing unused methods and modules in project repositories.
    }

    public String getStartupCounterName() {
        return TASK_SERVER_STARTUP_COUNTER + this.getTaskType();
    }

    public String getTaskType() {
        return taskType;
    }

    public void checkServers() throws TaskException {
        int serverCount = this.getTaskService().getServerConfiguration().getTaskServerCount();
        if (serverCount != -1) {
            log.info("Waiting for " + serverCount + " [" + this.getTaskType() + "] task executor nodes...");
            log.info("All task servers activated for [" + this.getTaskType() + "].");
        }
    }

    public TaskService getTaskService() {
        return taskService;
    }

    public synchronized List<String> getMemberIds() throws TaskException {
        // Converting to an empty method with the Hazelcast removal effort. 
        // Need to remove these empty methods later in another effort after analysing unused methods and modules in project repositories.
        return null;
    }

    public boolean isLeader() {
        return false;
    }

    public <V> V sendReceive(String memberId, TaskCall<V> taskCall) throws TaskException {
        // Converting to an empty method with the Hazelcast removal effort. 
        // Need to remove these empty methods later in another effort after analysing unused methods and modules in project repositories.
        return null;
    }

    private void scheduleAllMissingTasks(String memberId) throws TaskException {
        for (TaskManager tm : getTaskService().getAllTenantTaskManagersForType(this.getTaskType())) {
            if (tm instanceof ClusteredTaskManager) {
                this.scheduleMissingTasksWithRetryOnError((ClusteredTaskManager) tm, memberId);
            }
        }
    }

    private void scheduleMissingTasksWithRetryOnError(ClusteredTaskManager tm, String memberId) {
        int count = MISSING_TASKS_ON_ERROR_RETRY_COUNT;
        while (count > 0) {
            try {
                if (memberId == null) {
                    tm.scheduleMissingTasks();
                } else {
                    tm.scheduleMissingTasks(memberId);
                }
                break;
            } catch (TaskException e) {
            	boolean retry = (count > 1);
                log.error("Encountered error(s) in scheduling missing tasks ["
                        + tm.getTaskType() + "][" + tm.getTenantId() + "]:- \n" +
                        e.getMessage() + "\n" + (retry ? "Retrying [" +
                        ((MISSING_TASKS_ON_ERROR_RETRY_COUNT - count) + 1) + "]..." : "Giving up."));
                if (retry) {
                   /* coming up is a retry operation, lets do some cleanup */
        		   this.cleanupTaskCluster();
                }
            }
            count--;
        }
    }
    
    /**
     * Cleanup up possible inconsistencies that can happen because of cluster instability,
     * e.g. cluster messages being lost etc..
     */
    private void cleanupTaskCluster() {
    	this.refreshMembers();
    }
}
