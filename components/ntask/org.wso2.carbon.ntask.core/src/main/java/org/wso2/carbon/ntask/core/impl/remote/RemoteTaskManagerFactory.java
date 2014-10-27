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

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.common.TaskException.Code;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.TaskManagerFactory;
import org.wso2.carbon.ntask.core.TaskManagerId;
import org.wso2.carbon.ntask.core.TaskRepository;
import org.wso2.carbon.ntask.core.impl.RegistryBasedTaskRepository;
import org.wso2.carbon.ntask.core.internal.TasksDSComponent;
import org.wso2.carbon.ntask.core.service.TaskService.TaskServiceConfiguration;
import org.wso2.carbon.remotetasks.stub.admin.common.RemoteTaskAdmin;
import org.wso2.carbon.remotetasks.stub.admin.common.RemoteTaskAdminStub;

import java.util.ArrayList;
import java.util.List;

/**
 * This represents the remote task manager factory class.
 */
public class RemoteTaskManagerFactory implements TaskManagerFactory {

    private static RemoteTaskAdminStub remoteTaskAdmin;

    @Override
    public TaskManager getTaskManager(TaskManagerId tmId) throws TaskException {
        TaskRepository taskRepo = new RegistryBasedTaskRepository(tmId.getTenantId(),
                tmId.getTaskType());
        return new RemoteTaskManager(taskRepo, getRemoteTaskAdmin());
    }

    @Override
    public List<TaskManager> getStartupSchedulingTaskManagersForType(String taskType)
            throws TaskException {
        return new ArrayList<TaskManager>();
    }

    @Override
    public List<TaskManager> getAllTenantTaskManagersForType(String taskType) throws TaskException {
        return null;
    }

    private static RemoteTaskAdmin getRemoteTaskAdmin() throws TaskException {
        if (remoteTaskAdmin == null) {
            synchronized (RemoteTaskManagerFactory.class) {
                if (remoteTaskAdmin == null) {
                    TaskServiceConfiguration serverConfig = TasksDSComponent.getTaskService()
                            .getServerConfiguration();
                    String username = serverConfig.getRemoteServerUsername();
                    String password = serverConfig.getRemoteServerPassword();
                    String taskServerAddress = serverConfig.getRemoteServerAddress();
                    try {
                        remoteTaskAdmin = new RemoteTaskAdminStub(taskServerAddress
                                + "/services/RemoteTaskAdmin");
                    } catch (AxisFault e) {
                        throw new TaskException(e.getMessage(), Code.UNKNOWN, e);
                    }
                    HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
                    auth.setUsername(username);
                    auth.setPassword(password);
                    auth.setPreemptiveAuthentication(true);
                    remoteTaskAdmin
                            ._getServiceClient()
                            .getOptions()
                            .setProperty(
                                    org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE,
                                    auth);
                    remoteTaskAdmin._getServiceClient().getOptions().setCallTransportCleanup(true);
                }
            }
        }
        return remoteTaskAdmin;
    }

}
