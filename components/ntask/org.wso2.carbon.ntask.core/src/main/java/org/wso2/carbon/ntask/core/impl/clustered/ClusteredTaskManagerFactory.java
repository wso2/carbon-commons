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

import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.TaskManagerId;
import org.wso2.carbon.ntask.core.TaskRepository;
import org.wso2.carbon.ntask.core.impl.RegistryBasedTaskRepository;
import org.wso2.carbon.ntask.core.impl.standalone.StandaloneTaskManagerFactory;
import org.wso2.carbon.ntask.core.internal.TasksDSComponent;

/**
 * This represents the clustered task manager factory class.
 */
public class ClusteredTaskManagerFactory extends StandaloneTaskManagerFactory {

    public ClusteredTaskManagerFactory() {
        if (!isClusteringEnabled()) {
            throw new IllegalStateException("Clustering is not initialized to use Clustered Task Managers");
        }
    }
    
    @Override
    protected TaskManager createTaskManager(TaskManagerId tmId) throws TaskException {
        TaskRepository taskRepo = new RegistryBasedTaskRepository(tmId.getTenantId(),
                tmId.getTaskType());
        return new ClusteredTaskManager(taskRepo);
    }
    
    public static boolean isClusteringEnabled() {
        return TasksDSComponent.getConfigurationContextService().
                getServerConfigContext().getAxisConfiguration().getClusteringAgent() != null;
    }

}
