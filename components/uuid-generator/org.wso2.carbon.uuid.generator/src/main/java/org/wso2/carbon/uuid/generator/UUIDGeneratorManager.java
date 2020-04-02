/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.uuid.generator;

import java.io.IOException;

/**
 * This is the manager class which can be used to retrieve UUID generators of specific type.
 */
public class UUIDGeneratorManager {

    protected static UUIDTimeStamp synchronizedTime;

    /**
     * Static method to retrieve the time based UUID generator.
     *
     * @return {@link TimeBasedUUIDGenerator}
     */
    public static TimeBasedUUIDGenerator getTimeBasedUUIDGenerator() {

        return new TimeBasedUUIDGenerator(synchronizedTime());
    }

    private static synchronized UUIDTimeStamp synchronizedTime() {

        if (synchronizedTime == null) {
            try {
                synchronizedTime = new UUIDTimeStamp(new java.util.Random(System.currentTimeMillis()), null);
            } catch (IOException e) {
                throw new IllegalArgumentException(
                        "Failed to create UUIDTimeStamp with synchronize: " + e.getMessage(), e);
            }
        }
        return synchronizedTime;
    }
}
