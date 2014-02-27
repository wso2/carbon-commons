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

/**
 * This OSGi service is used for authenticating a user and to be used in by other components in the same carbon environment.
 */
public interface AuthenticationService {

    /**
     * Authenticate the given user
     * If the password is similar to the shared key, it assumes that the request has came from the authenticated user.
     * Otherwise, the user is authenticated based on provided username and password
     *
     * @param username The name of the user to be authenticated
     * @param password The password of the user to be authenticated.
     * @return <code>true</code> if the user is authenticated
     * @throws AuthenticationException for errors if the authentication cannot be done.
     */
    public boolean authenticate(String username, String password) throws AuthenticationException;
}
