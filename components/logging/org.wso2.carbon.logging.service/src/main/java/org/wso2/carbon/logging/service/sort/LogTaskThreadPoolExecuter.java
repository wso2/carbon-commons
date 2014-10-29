/*
 * Copyright 2005,2014 WSO2, Inc. http://www.wso2.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.logging.service.sort;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class LogTaskThreadPoolExecuter {
    private static final Log log = LogFactory.getLog(LogTaskThreadPoolExecuter.class);
    private final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(5);
    private ThreadPoolExecutor threadPool = null;
    private int poolSize = 5;
    private int maxPoolSize = 5;
    private long keepAliveTime = 10;

    public LogTaskThreadPoolExecuter() {
        threadPool = new ThreadPoolExecutor(poolSize, maxPoolSize, keepAliveTime,
                TimeUnit.SECONDS, queue);
    }

    public void runTask(Runnable task) {
        threadPool.execute(task);
        log.info("Task Count : " + queue.size());
    }
}