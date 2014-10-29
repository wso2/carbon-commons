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

package org.wso2.carbon.event.core.subscription;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.event.core.Message;

/**
 * this interface can be used to receive the Events from the EventBroker interface
 */
public interface EventDispatcher {

    /**
     * notify with the subscription details. implementation can use the subscription
     * details to send the message.
     * @param message
     * @param subscription
     */
    public void notify(Message message, Subscription subscription);
}
