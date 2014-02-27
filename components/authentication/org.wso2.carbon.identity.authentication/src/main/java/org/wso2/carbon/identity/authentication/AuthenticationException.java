/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.identity.authentication;

import org.apache.commons.logging.Log;

/**
 * Represent the authentication failures. Each exception and error message is logged and thrown.
 */
public class AuthenticationException extends RuntimeException {

    /**
     * Logs the given message and create a RuntimeException object
     *
     * @param msg Error Message
     * @param log Logger who need to consume message
     */
    public AuthenticationException(String msg, Log log) {
        super(msg);
        log.error(msg);
    }

    /**
     * Logs the given message and the root error and create a RuntimeException object
     *
     * @param msg       Error Message
     * @param throwable Root Error
     * @param log       Logger who need to consume message
     */
    public AuthenticationException(String msg, Throwable throwable, Log log) {
        super(msg, throwable);
        log.error(msg, throwable);
    }
}
