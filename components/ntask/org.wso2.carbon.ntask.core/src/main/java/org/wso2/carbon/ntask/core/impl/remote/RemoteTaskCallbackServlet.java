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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.internal.TasksDSComponent;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class represents a servlet used to listen for requests from a remote
 * task server.
 */
public class RemoteTaskCallbackServlet extends HttpServlet {

    private final Log log = LogFactory.getLog(RemoteTaskCallbackServlet.class);

    private static final long serialVersionUID = -8777558000344655739L;

    public static final String REMOTE_SYSTEM_TASK_HEADER_ID = "REMOTE_SYSTEM_TASK_ID";

    public RemoteTaskCallbackServlet() {
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) {
        this.doPost(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) {
        try {
            String remoteTaskId = req.getHeader(REMOTE_SYSTEM_TASK_HEADER_ID);
            if (remoteTaskId == null) {
                return;
            }
            /* if task execution node is not fully started yet, ignore this remote trigger */
            if (!TasksDSComponent.getTaskService().isServerInit()) {
                if (log.isDebugEnabled()) {
                    log.debug("Ignoring remote task triggered before server startup: " + remoteTaskId);
                }
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("Remote Task Request Received: " + remoteTaskId);
            }
            Object[] taskInfo = RemoteTaskUtils.lookupRemoteTask(remoteTaskId);
            int tenantId = (Integer) taskInfo[0];
            String taskType = (String) taskInfo[1];
            String taskName = (String) taskInfo[2];
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
                TaskManager tm = TasksDSComponent.getTaskService().getTaskManager(taskType);
                if (!(tm instanceof RemoteTaskManager)) {
                    log.error("The server is not running in remote task mode, "
                            + "the current task manager type used is '" + tm.getClass() + "'");
                    return;
                }
                ((RemoteTaskManager) tm).runTask(taskName);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        } catch (Exception e) {
            log.error("Error in executing remote task request: " + e.getMessage(), e);
        }
    }

}
