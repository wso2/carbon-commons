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
 *
 * NOTE: The logic in this class is copied from https://github.com/cowtowncoder/java-uuid-generator/, all credits
 * goes to the original authors of the project  https://github.com/cowtowncoder/java-uuid-generator/.
 */
package org.wso2.carbon.uuid.generator.util;

public class Util {

    public final static long createClockSeqInLong(byte[] buffer, int offset) {

        long hi = ((long) gatherInt(buffer, offset)) << 32;
        long lo = (((long) gatherInt(buffer, offset + 4)) << 32) >>> 32;
        return hi | lo;
    }

    public static long createUUIDSecondHalfInLong(long l2) {

        l2 = ((l2 << 2) >>> 2); // remove 2 MSB
        l2 |= (2L << 62); // set 2 MSB to '10'
        return l2;
    }

    private final static int gatherInt(byte[] buffer, int offset) {

        return (buffer[offset] << 24) | ((buffer[offset + 1] & 0xFF) << 16)
                | ((buffer[offset + 2] & 0xFF) << 8) | (buffer[offset + 3] & 0xFF);
    }

}
