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

package org.wso2.carbon.event.core.exception;

/**
 * this exceptions is thrown to any problem which occures at the message processing time.
 */
public class EventBrokerException extends Exception{

    public EventBrokerException() {
    }

    public EventBrokerException(String message) {
        super(message);
    }

    public EventBrokerException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventBrokerException(Throwable cause) {
        super(cause);
    }
}
