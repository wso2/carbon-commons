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

package org.wso2.carbon.event.core.internal.topic.registry;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.event.core.exception.EventBrokerConfigurationException;
import org.wso2.carbon.event.core.internal.util.JavaUtil;
import org.wso2.carbon.event.core.topic.TopicManager;
import org.wso2.carbon.event.core.topic.TopicManagerFactory;

public class RegisistryTopicManagerFactory implements TopicManagerFactory{

    public static final String EB_ELE_TOPIC_STORAGE_PATH = "topicStoragePath";

    public TopicManager getTopicManager(OMElement config) throws EventBrokerConfigurationException {

        String topicStoragePath = JavaUtil.getValue(config, EB_ELE_TOPIC_STORAGE_PATH);
        return new RegistryTopicManager(topicStoragePath);
    }
}
