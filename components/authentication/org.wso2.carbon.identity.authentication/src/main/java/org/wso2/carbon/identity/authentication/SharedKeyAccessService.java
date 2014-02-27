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
 * This is an OSGi service used for passing the access key that should be used by
 * other Carbon components to log in to the another component in the same carbon environment and that can be accessed
 * both remotely and locally.Access key is generated when the authentication bundle is activated.
 */
public interface SharedKeyAccessService {

    /**
     * Returns the shared key created by the authentication service
     *
     * @return <code>String</code> representing the key
     */
    String getSharedKey();
}
