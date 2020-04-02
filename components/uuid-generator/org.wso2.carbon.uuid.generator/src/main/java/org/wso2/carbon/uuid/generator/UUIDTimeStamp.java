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
package org.wso2.carbon.uuid.generator;

import java.io.IOException;
import java.util.Random;

public final class UUIDTimeStamp {

    /**
     * For UUID version 1, timestamp is
     * represented by Coordinated Universal Time (UTC) as a count of 100-
     * nanosecond intervals since 00:00:00.00, 15 October 1582 (the date of
     * Gregorian reform to the Christian calendar). But System.longTimeMillis() returns time from january 1st 1970.
     * So, this is the offset between the two.
     */
    private final static long timeStampOffset = 0x01b21dd213814000L;

    /**
     * Multiplier to use time with maximum resolution of 1 msec.
     */
    private final static int clockMultiplier = 10000;

    private final static long clockMultiplierL = 10000L;

    private final static long maxClockAdvance = 100L;

    protected final Random random;

    private int clockSequence;

    private long lastSystemTimestamp = 0L;

    private long lastUsedTimestamp = 0L;

    protected final UUIDTimestampSynchronizer timestampSynchronizer;

    private long firstTimestamp = Long.MAX_VALUE;

    private int clockCounter = 0;

    private final static int MAX_WAIT_COUNT = 50;

    public UUIDTimeStamp(Random rnd, UUIDTimestampSynchronizer sync) throws IOException {

        random = rnd;
        timestampSynchronizer = sync;
        initializeCounters(rnd);
        lastSystemTimestamp = 0L;
        lastUsedTimestamp = 0L;

        if (sync != null) {
            long lastSaved = sync.initialize();
            if (lastSaved > lastUsedTimestamp) {
                lastUsedTimestamp = lastSaved;
            }
        }

        firstTimestamp = 0L;
    }

    private void initializeCounters(Random rnd) {

        clockSequence = rnd.nextInt();
        clockCounter = (clockSequence >> 16) & 0xFF;
    }

    public int getClockSequence() {

        return (clockSequence & 0xFFFF);
    }

    public final synchronized long getTimestamp() {

        long currentTimeMillis = System.currentTimeMillis();

        if (currentTimeMillis < lastSystemTimestamp) {
            lastSystemTimestamp = currentTimeMillis;
        }

        if (currentTimeMillis <= lastUsedTimestamp) {
            if (clockCounter < clockMultiplier) {
                currentTimeMillis = lastUsedTimestamp;
            } else {
                long timeDiff = lastUsedTimestamp - currentTimeMillis;
                long originalTime = currentTimeMillis;
                currentTimeMillis = lastUsedTimestamp + 1L;

                initializeCounters(random);

                if (timeDiff >= maxClockAdvance) {
                    wait(originalTime, timeDiff);
                }
            }
        } else {

            clockCounter &= 0xFF;
        }

        lastUsedTimestamp = currentTimeMillis;

        if (timestampSynchronizer != null && currentTimeMillis >= firstTimestamp) {
            try {
                firstTimestamp = timestampSynchronizer.update(currentTimeMillis);
            } catch (IOException e) {
                throw new RuntimeException("Failed to synchronize timestamp: " + e);
            }
        }

        currentTimeMillis *= clockMultiplierL;
        currentTimeMillis += timeStampOffset;

        // Plus add the clock counter:
        currentTimeMillis += clockCounter;
        // and then increase
        ++clockCounter;
        return currentTimeMillis;
    }

    private final static void wait(long startTime, long actDiff) {

        long diffRatio = actDiff / maxClockAdvance;
        long delay;

        if (diffRatio < 2L) { // 200 msecs or less
            delay = 1L;
        } else if (diffRatio < 10L) { // 1 second or less
            delay = 2L;
        } else if (diffRatio < 600L) { // 1 minute or less
            delay = 3L;
        } else {
            delay = 5L;
        }
        long waitTime = startTime + delay;
        int counter = 0;
        do {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
            }
            delay = 1L;
            if (++counter > MAX_WAIT_COUNT) {
                break;
            }
        } while (System.currentTimeMillis() < waitTime);
    }
}
