/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.discovery.messages;

/**
 * Represents a WS-Discovery notification message. WS-Discovery specification
 * outlines two basic types of notifications, namely 'Hello' messages and 'Bye'
 * messages. Each notification contains a TargetService instance among other
 * crucial metadata.
 */
public class Notification {
    
    protected int type;
    protected TargetService targetService;

    public Notification(int type, TargetService targetService) {
        this.type = type;
        this.targetService = targetService;
    }

    public TargetService getTargetService() {
        return targetService;
    }

    public int getType() {
        return type;
    }
}
