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

import java.util.Map;

import com.hazelcast.core.HazelcastInstance;

import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskLocationResolver;
import org.wso2.carbon.ntask.core.TaskServiceContext;
import org.wso2.carbon.ntask.core.internal.TasksDSComponent;

/**
 * This class represents a TaskLocationResolver implementation, which assigns a location to the task
 * in a round robin fashion.
 */
public class RoundRobinTaskLocationResolver implements TaskLocationResolver {

    private static final String ROUND_ROBIN_TASK_RESOLVER_ID = "__ROUND_ROBIN_TASK_RESOLVER_ID__";

    @Override
	public void init(Map<String, String> properties) throws TaskException {		
	}
    
    @Override
    public int getLocation(TaskServiceContext ctx, TaskInfo taskInfo) throws TaskException {
        HazelcastInstance hz = TasksDSComponent.getHazelcastInstance();
        if (hz == null) {
            /* this cannot happen, because the task location resolvers are used in clustered mode */
            return 0;
        }
        return (int) Math.abs(hz.getAtomicLong(ROUND_ROBIN_TASK_RESOLVER_ID + ctx.getTaskType()).incrementAndGet());
    }

}
