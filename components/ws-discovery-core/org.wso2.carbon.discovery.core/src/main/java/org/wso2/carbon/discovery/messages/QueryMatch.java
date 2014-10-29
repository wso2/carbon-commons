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
 * Represents a query match returned by a WS-Discovery proxy. A query match can either
 * be a probe match or a resolve match. It can consist of 0 or more target services.
 */
public class QueryMatch {

    private int resultType;
    private TargetService[] targetServices;

    public QueryMatch(int resultType, TargetService[] targetServices) {
        this.resultType = resultType;
        this.targetServices = targetServices;
    }

    public int getResultType() {
        return resultType;
    }

    public TargetService[] getTargetServices() {
        return targetServices;
    }
}
