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
package org.wso2.carbon.ntask.core;

import org.wso2.carbon.ntask.common.TaskException;

/**
 * This interface represents the contract that must be implemented to retrieve
 * the location that a given task should be scheduled.
 */
public interface TaskLocationResolver {

    /**
     * Returns the location the given task should be scheduled in.
     * 
     * @param ctx The task context, which contains environmental information on
     *            other tasks etc..
     * @param taskInfo The task information of the task to be scheduled
     * @return The location of the task to be scheduled
     * @throws TaskException
     */
    public int getLocation(TaskServiceContext ctx, TaskInfo taskInfo) throws TaskException;

}
