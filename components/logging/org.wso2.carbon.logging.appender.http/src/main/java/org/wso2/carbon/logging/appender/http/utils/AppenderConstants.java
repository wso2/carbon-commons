/*
 *
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.logging.appender.http.utils;

public class AppenderConstants {
    public static final int SCHEDULER_CORE_POOL_SIZE = 10;
    public static final int SCHEDULER_DELAY = 10;
    public static final int SCHEDULER_INITIAL_DELAY = 10;
    public static final int SCHEDULER_TERMINATION_DELAY = 10;

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BASIC_AUTH_PREFIX = "Basic ";
    public static final String HTTPS = "https";
    public static final String QUEUE_DIRECTORY_PATH = "/temp/logsQueue";

//    public static final String ERROR_ERROR_SERIALIZING_LOG_EVENT = "Error serializing log event";
}
