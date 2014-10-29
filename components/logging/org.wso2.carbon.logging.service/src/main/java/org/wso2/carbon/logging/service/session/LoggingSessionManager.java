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

package org.wso2.carbon.logging.service.session;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;

public class LoggingSessionManager {
    public static void setSessionObject(String name, Object obj) {
        MessageContext messageContext = MessageContext.getCurrentMessageContext();
        if (messageContext != null) {
            ServiceContext serviceContext = messageContext.getServiceContext();
            if (serviceContext != null) {
                serviceContext.setProperty(name, obj);
            }
        }
    }

    public static Object getSessionObject(String name) {
        MessageContext messageContext = MessageContext.getCurrentMessageContext();
        if (messageContext != null) {
            ServiceContext serviceContext = messageContext.getServiceContext();
            if (serviceContext != null) {
                return serviceContext.getProperty(name);
            }
        }
        return null;
    }
}
