/**
 *  Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.ntask.core.impl.clustered.rpc;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.common.TaskException.Code;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.impl.clustered.ClusteredTaskManager;
import org.wso2.carbon.ntask.core.internal.TasksDSComponent;

import java.io.Serializable;
import java.util.concurrent.Callable;

/**
 * Base class for cluster RPC calls.
 */
public abstract class TaskCall<V> implements Callable<V>, Serializable {
    
    private static final long serialVersionUID = 1L;

    private int tenantId;
    
    private String taskType;
    
    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }
    
    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }
    
    public int getTenantId() {
        return tenantId;
    }
    
    public String getTaskType() {
        return taskType;
    }
    
    @Override
    public V call() throws Exception {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(
                    this.getTenantId(), true);
            TaskManager tm = TasksDSComponent.getTaskService().getTaskManager(
                    this.getTaskType());
            if (tm instanceof ClusteredTaskManager) {
                return this.doWork((ClusteredTaskManager) tm);
            } else {
                throw new TaskException("Invalid task manager type, expected " +
                		"'clustered' type, got: " + tm, Code.CONFIG_ERROR);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
    
    public abstract V doWork(ClusteredTaskManager tm) throws Exception;
    
}
