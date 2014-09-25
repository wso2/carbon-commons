/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.core.internal.delivery.inmemory;

import org.wso2.carbon.event.core.delivery.DeliveryManagerFactory;
import org.wso2.carbon.event.core.delivery.DeliveryManager;
import org.wso2.carbon.event.core.delivery.MatchingManagerFactory;
import org.wso2.carbon.event.core.util.EventBrokerConstants;
import org.wso2.carbon.event.core.internal.util.JavaUtil;
import org.wso2.carbon.event.core.exception.EventBrokerConfigurationException;
import org.apache.axiom.om.OMElement;

import javax.xml.namespace.QName;
import java.util.concurrent.*;

/**
 * factory class for inmemory Delivary manager
 */
public class InMemoryDeliveryManagerFactory implements DeliveryManagerFactory {

    public static final String EB_DM_MIN_SPARE_THREADS = "minSpareThreads";
    public static final String EB_DM_MAX_THREADS = "maxThreads";
    public static final String EB_DM_MAX_QUEUED_REQUESTS = "maxQueuedRequests";
    public static final String EB_DM_KEEP_ALIVE_TIME = "keepAliveTime";
    public static final String EB_DM_TOPIC_STORAGE_PATH = "topicStoragePath";

    public DeliveryManager getDeliveryManger(OMElement config) throws EventBrokerConfigurationException {

        int minSpareThreads = Integer.parseInt(JavaUtil.getValue(config, EB_DM_MIN_SPARE_THREADS));
        int maxThreads = Integer.parseInt(JavaUtil.getValue(config, EB_DM_MAX_THREADS));
        int maxQueuedRequests =
                Integer.parseInt(JavaUtil.getValue(config, EB_DM_MAX_QUEUED_REQUESTS));
        long keepAliveTime = Integer.parseInt(JavaUtil.getValue(config, EB_DM_KEEP_ALIVE_TIME));
        String topicStoragePath = JavaUtil.getValue(config, EB_DM_TOPIC_STORAGE_PATH);


        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(maxQueuedRequests);

        ExecutorService executor = new ThreadPoolExecutor(minSpareThreads, maxThreads,
                keepAliveTime, TimeUnit.NANOSECONDS, queue);

        InMemoryDeliveryManager delivaryManager = new InMemoryDeliveryManager(executor, topicStoragePath);

        // creates the matching manager
        OMElement matchingManagerElement =
                config.getFirstChildWithName(new QName(EventBrokerConstants.EB_CONF_NAMESPACE,
                        EventBrokerConstants.EB_CONF_ELE_MATCHING_MANAGER));
        MatchingManagerFactory factory =
                (MatchingManagerFactory) JavaUtil.getObject(matchingManagerElement);
        delivaryManager.setMatchingManager(factory.getMatchingManager(matchingManagerElement));

        return delivaryManager;
    }


}
