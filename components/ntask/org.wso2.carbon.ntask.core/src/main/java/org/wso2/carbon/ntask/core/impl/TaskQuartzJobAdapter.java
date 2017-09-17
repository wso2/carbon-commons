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
package org.wso2.carbon.ntask.core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ntask.common.TaskConstants;
import org.wso2.carbon.ntask.core.Task;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.internal.TasksDSComponent;

import java.util.Date;
import java.util.Map;

/**
 * This class represents an adapter class used to wrap a Task in a Quartz Job.
 */
public class TaskQuartzJobAdapter implements Job {

    private static final Log log = LogFactory.getLog(TaskQuartzJobAdapter.class);
    
    public TaskQuartzJobAdapter() {
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(JobExecutionContext ctx) throws JobExecutionException {
        if (log.isDebugEnabled()) {
            if (ctx.isRecovering()) {
                log.debug("Job: " + ctx.getJobDetail().getKey() + " is recovering at " + new Date());
            } else {
                log.debug("Job: " + ctx.getJobDetail().getKey() + " is starting at " + new Date());
            }
        }

        /* if task execution node is not fully started yet, ignore this trigger */
        if (!TasksDSComponent.getTaskService().isServerInit()) {
            if (log.isDebugEnabled()) {
                log.debug("Ignoring task triggered before server startup: " + ctx.getJobDetail());
            }
            return;
        }
        JobDataMap dataMap = ctx.getJobDetail().getJobDataMap();
        String taskClassName = dataMap.getString(TaskConstants.TASK_CLASS_NAME);
        if (taskClassName == null) {
            throw new JobExecutionException("The task class is missing in the job data map");
        }
        try {
            Task task = (Task) Class.forName(taskClassName).newInstance();
            Map<String, String> properties = (Map<String, String>) dataMap
                    .get(TaskConstants.TASK_PROPERTIES);
            task.setProperties(properties);
            int tenantId = Integer.parseInt(properties.get(TaskInfo.TENANT_ID_PROP));
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
                task.init();
                task.execute();
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        } catch (Throwable e) {
            String msg = "Error in executing task: " + e.getMessage();
            log.error(msg, e);
            throw new JobExecutionException(msg, e);
        }
    }

}
