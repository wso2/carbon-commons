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

import org.wso2.carbon.ntask.common.TaskConstants;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.common.TaskException.Code;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskLocationResolver;
import org.wso2.carbon.ntask.core.TaskServiceContext;

/**
 * This class represents a TaskLocationResolver implementation, which always returns a fixed location.
 */
public class FixedLocationResolver implements TaskLocationResolver {

    public FixedLocationResolver() {
    }

    @Override
    public int getLocation(TaskServiceContext ctx, TaskInfo taskInfo) throws TaskException {
        String value = taskInfo.getProperties().get(TaskConstants.FIXED_LOCATION_RESOLVER_PARAM);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (Exception e) {
                throw new TaskException("The task propery '"
                        + TaskConstants.FIXED_LOCATION_RESOLVER_PARAM
                        + "' must be an integer in task '" + taskInfo.getName() + "'",
                        Code.CONFIG_ERROR);
            }
        } else {
            throw new TaskException("The task property '"
                    + TaskConstants.FIXED_LOCATION_RESOLVER_PARAM
                    + "' is missing which is required for FixedLocationResolver in task '"
                    + taskInfo.getName() + "'", Code.CONFIG_ERROR);
        }
    }

}
