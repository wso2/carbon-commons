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

import com.hazelcast.core.*;

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
public class ClusterGroupCommunicator implements MembershipListener {

    public static final String NTASK_P2P_COMM_EXECUTOR = "__NTASK_P2P_COMM_EXECUTOR__";

    private static final String TASK_SERVER_STARTUP_COUNTER = "__TASK_SERVER_STARTUP_COUNTER__";

    private static final int MISSING_TASKS_ON_ERROR_RETRY_COUNT = 3;

    private static final String CARBON_TASKS_MEMBER_ID_MAP = "__CARBON_TASKS_MEMBER_ID_MAP__";

    public static final String TASK_SERVER_COUNT_SYS_PROP = "task.server.count";

    private static final Log log = LogFactory.getLog(ClusterGroupCommunicator.class);

    private TaskService taskService;

    private HazelcastInstance hazelcast;

    private static Map<String, ClusterGroupCommunicator> communicatorMap = new HashMap<String, ClusterGroupCommunicator>();

    private Map<String, Member> membersMap;

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
        this.hazelcast = TasksDSComponent.getHazelcastInstance();
        if (this.getHazelcast() == null) {
            throw new TaskException("ClusterGroupCommunicator cannot initialize, " +
            		"Hazelcast is not initialized", Code.CONFIG_ERROR);
        }
        this.getHazelcast().getCluster().addMembershipListener(this);
        this.refreshMembers();
    }
    
    private void refreshMembers() {
    	/* create a distributed map to track the members in the current group */
        this.membersMap = this.getHazelcast().getMap(CARBON_TASKS_MEMBER_ID_MAP + "#" + taskType);
        /* check and remove expired members */
        this.checkAndRemoveExpiredMembers();
    }
    
    public void addMyselfToGroup() {
    	Member member = this.getHazelcast().getCluster().getLocalMember();
        /* add myself to the queue */
        this.membersMap.put(this.getIdFromMember(member), member);
        /* increment the task server count */
        this.getHazelcast().getAtomicLong(this.getStartupCounterName()).incrementAndGet();
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
        Set<Member> existingMembers = this.getHazelcast().getCluster().getMembers();
        Iterator<Map.Entry<String, Member>> itr = this.membersMap.entrySet().iterator();
        List<String> removeList = new ArrayList<>();
        Map.Entry<String, Member> currentEntry;
        while (itr.hasNext()) {
            currentEntry = itr.next();
            if (!existingMembers.contains(currentEntry.getValue())) {
                removeList.add(currentEntry.getKey());
            }
        }
        for (String key : removeList) {
            this.membersMap.remove(key);
        }
    }

    public String getStartupCounterName() {
        return TASK_SERVER_STARTUP_COUNTER + this.getTaskType();
    }

    public String getTaskType() {
        return taskType;
    }

    private String getIdFromMember(Member member) {
        return member.getUuid();
    }

    private Member getMemberFromId(String id) throws TaskException {
        Member member = this.membersMap.get(id);
        if (member == null) {
            /* this is probably because of an edge case, where a member has just gone away when we
             * trying to access this member, this must be handled in the upper layers by retrying
             * the root operation */
            throw new TaskException("The member with id: " + id + " does not exist", Code.UNKNOWN);
        }
        return member;
    }

    public void checkServers() throws TaskException {
        int serverCount = this.getTaskService().getServerConfiguration().getTaskServerCount();
        if (serverCount != -1) {
            log.info("Waiting for " + serverCount + " [" + this.getTaskType() + "] task executor nodes...");
            try {
                /* with this approach, lets say the server count is 3, and after all 3 server comes up, 
                 * and tasks scheduled, if two nodes go away, and one comes up, it will be allowed to start,
                 * even though there aren't 3 live nodes, which would be the correct approach, if the whole
                 * cluster goes down, then, you need again for all 3 of them to come up */
                while (this.getHazelcast().getAtomicLong(this.getStartupCounterName()).get() < serverCount) {
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                throw new TaskException("Error in waiting for task [" + this.getTaskType() + "] executor nodes: " +
                        e.getMessage(), Code.UNKNOWN, e);
            }
            log.info("All task servers activated for [" + this.getTaskType() + "].");
        }
    }

    public TaskService getTaskService() {
        return taskService;
    }

    public HazelcastInstance getHazelcast() {
        return hazelcast;
    }

    public synchronized List<String> getMemberIds() throws TaskException {
        return new ArrayList<String>(this.membersMap.keySet());
    }

    public String getMemberId() {
        return this.getIdFromMember(this.getHazelcast().getCluster().getLocalMember());
    }

    public boolean isLeader() {
        if (this.getHazelcast().getLifecycleService().isRunning()) {
        	String id;
        	/* as per documentation, getMembers return the oldest member first */
            for (Member member : this.getHazelcast().getCluster().getMembers()) {
            	id = this.getIdFromMember(member);
            	if (this.membersMap.containsKey(id)) {
            		return this.getMemberId().equals(id);
            	}
            }
        }
        return false;
    }

    public <V> V sendReceive(String memberId, TaskCall<V> taskCall) throws TaskException {
        IExecutorService es = this.getHazelcast().getExecutorService(NTASK_P2P_COMM_EXECUTOR);
        Future<V> taskExec = es.submitToMember(taskCall, this.getMemberFromId(memberId));
        try {
            return taskExec.get();
        } catch (Exception e) {
            throw new TaskException("Error in cluster message send-receive: " + e.getMessage(),
                    Code.UNKNOWN, e);
        }
    }

    @Override
    public void memberAdded(MembershipEvent event) {
        if (this.getHazelcast().getLifecycleService().isRunning()) {
            String id = this.getIdFromMember(event.getMember());
            this.membersMap.put(id, event.getMember());
            try {
                if (this.isLeader()) {
                    log.info("Task [" + this.getTaskType() + "] member joined [" + event.getMember().toString()
                            + "], rescheduling missing tasks...");
                    this.scheduleAllMissingTasks(id);
                }
            } catch (TaskException e) {
                log.error("Error in scheduling missing tasks [" + this.getTaskType() + "]: " + e.getMessage(), e);
            }
        }
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

    @Override
    public void memberRemoved(MembershipEvent event) {
        if (this.getHazelcast().getLifecycleService().isRunning()) {
            String id = this.getIdFromMember(event.getMember());
            this.membersMap.remove(id);
            try {
                if (this.isLeader()) {
                    log.info("Task [" + this.getTaskType() + "] member departed [" + event.getMember().toString()
                            + "], rescheduling missing tasks...");
                    this.scheduleAllMissingTasks(null);
                }
            } catch (TaskException e) {
                log.error("Error in scheduling missing tasks [" + this.getTaskType() + "]: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
    	/* ignored */
    }

    public Map<String, Member> getMemberMap() {
        return membersMap;
    }

}
