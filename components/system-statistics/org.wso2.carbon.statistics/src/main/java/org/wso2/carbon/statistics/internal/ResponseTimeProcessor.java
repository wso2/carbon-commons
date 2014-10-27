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
package org.wso2.carbon.statistics.internal;

/**
 * A utility class to compute response times.
 */
public class ResponseTimeProcessor {
    private long maxResponseTime = 0;
    private long minResponseTime = -1;
    private double avgResponseTime = 0;
    private double totalresponseTime;

    public synchronized void addResponseTime(long responseTime,
                                             long requestCount) {
        if (maxResponseTime < responseTime) {
            maxResponseTime = responseTime;
        }

        if (minResponseTime > responseTime) {
            minResponseTime = responseTime;
        }

        if (minResponseTime == -1) {
            minResponseTime = responseTime;
        }

        totalresponseTime = totalresponseTime + responseTime;
        avgResponseTime = totalresponseTime / requestCount;
    }

    public long getMaxResponseTime() {
        return maxResponseTime;
    }

    public double getAvgResponseTime() {
        return avgResponseTime;
    }

    public long getMinResponseTime() {
        return minResponseTime;
    }
}
