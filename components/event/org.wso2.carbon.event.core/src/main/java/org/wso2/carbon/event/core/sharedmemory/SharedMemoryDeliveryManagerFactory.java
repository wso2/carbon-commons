/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.event.core.sharedmemory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.wso2.carbon.event.core.delivery.DeliveryManagerFactory;
import org.wso2.carbon.event.core.delivery.DeliveryManager;
import org.wso2.carbon.event.core.internal.util.JavaUtil;
import org.wso2.carbon.event.core.exception.EventBrokerConfigurationException;
import org.apache.axiom.om.OMElement;

/**
 * factory class for shared memory Delivery manager
 */
public class SharedMemoryDeliveryManagerFactory implements DeliveryManagerFactory {

    public static final String EB_DM_MIN_SPARE_THREADS = "minSpareThreads";
    public static final String EB_DM_MAX_THREADS = "maxThreads";
    public static final String EB_DM_MAX_QUEUED_REQUESTS = "maxQueuedRequests";
    public static final String EB_DM_KEEP_ALIVE_TIME = "keepAliveTime";
    public static final String EB_DM_TOPIC_STORAGE_PATH = "topicStoragePath";

    public DeliveryManager getDeliveryManger(OMElement config) throws EventBrokerConfigurationException {

        int minSpareThreads = Integer.parseInt(JavaUtil.getValue(config, EB_DM_MIN_SPARE_THREADS));
        int maxThreads = Integer.parseInt(JavaUtil.getValue(config, EB_DM_MAX_THREADS));
        int maxQueuedRequests = Integer.parseInt(JavaUtil.getValue(config, EB_DM_MAX_QUEUED_REQUESTS));
        int keepAliveTime = Integer.parseInt(JavaUtil.getValue(config, EB_DM_KEEP_ALIVE_TIME));

        String topicStoragePath = JavaUtil.getValue(config, EB_DM_TOPIC_STORAGE_PATH);
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(maxQueuedRequests);
        ExecutorService executor = new ThreadPoolExecutor(minSpareThreads, maxThreads, keepAliveTime, TimeUnit.NANOSECONDS, queue);
        SharedMemoryDeliveryManager deliveryManager = new SharedMemoryDeliveryManager(executor, topicStoragePath);

        return deliveryManager;
    }


}
