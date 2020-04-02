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

import org.wso2.carbon.uuid.generator.util.UUIDGeneratorConstants;
import org.wso2.carbon.uuid.generator.util.Util;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

/**
 * Class responsible for constructing and generating a time based UUID.
 */
public class TimeBasedUUIDGenerator extends UUIDGenerator {

    long address;
    //Last 8 bytes of the UUID (node id + clock sequence)
    protected final long uuidSecondHalf;

    protected final UUIDTimeStamp uuidTimeStamp;

    public TimeBasedUUIDGenerator(UUIDTimeStamp timeStamp) {

        byte[] timeBaseduuidByteAray = new byte[16];
        constructNodeId();
        insertToByteArray(timeBaseduuidByteAray, 10);
        int clockSequence = timeStamp.getClockSequence();
        //set clock_seq_hi.The high field of the clock sequence.
        timeBaseduuidByteAray[UUIDGeneratorConstants.CLOCK_SEQUENCE_HI_BYTE_OFFSET_IN_UUID] =
                (byte) (clockSequence >> 8);
        //set clock_seq_low.
        timeBaseduuidByteAray[UUIDGeneratorConstants.CLOCK_SEQUENCE_LOW_BYTE_OFFSET_IN_UUID] = (byte) clockSequence;
        long clockSeqInLong = Util.createClockSeqInLong(timeBaseduuidByteAray, 8);
        uuidSecondHalf = Util.createUUIDSecondHalfInLong(clockSeqInLong);
        uuidTimeStamp = timeStamp;
    }

    @Override
    public UUID generate() {

        final long rawTimestamp = uuidTimeStamp.getTimestamp();
        // Time field components are kind of shuffled, need to slice:
        int clockHi = (int) (rawTimestamp >>> 32);
        int clockLo = (int) rawTimestamp;
        // and dice
        int midhi = (clockHi << 16) | (clockHi >>> 16);
        // need to squeeze in type (4 MSBs in byte 6, clock hi)
        midhi &= ~0xF000; // remove high nibble of 6th byte
        midhi |= 0x1000; // type 1
        long midhiL = (long) midhi;
        midhiL = ((midhiL << 32) >>> 32); // to get rid of sign extension
        // and reconstruct
        long l1 = (((long) clockLo) << 32) | midhiL;
        // last detail: must force 2 MSB to be '10'
        return new UUID(l1, uuidSecondHalf);
    }

    private byte[] constructNodeId() {

        Random random = new SecureRandom();
        byte[] nodeIdByteArray = new byte[6];
        synchronized (random) {
            random.nextBytes(nodeIdByteArray);
        }
        nodeIdByteArray[0] |= (byte) 0x01;
        createBinaryAddress(nodeIdByteArray);
        return nodeIdByteArray;
    }

    private void createBinaryAddress(byte[] nodeIdByteArray) {

        if (nodeIdByteArray.length != 6) {
            throw new NumberFormatException("Node ID address has to consist of 6 bytes");
        }
        long l = nodeIdByteArray[0] & 0xFF;
        for (int i = 1; i < 6; ++i) {
            l = (l << 8) | (nodeIdByteArray[i] & 0xFF);
        }
        address = l;
    }

    private void insertToByteArray(byte[] array, int pos) {

        if (pos < 0 || (pos + 6) > array.length) {
            throw new IllegalArgumentException("Illegal offset (" + pos + "), need room for 6 bytes");
        }
        int i = (int) (address >> 32);
        array[pos++] = (byte) (i >> 8);
        array[pos++] = (byte) i;
        i = (int) address;
        array[pos++] = (byte) (i >> 24);
        array[pos++] = (byte) (i >> 16);
        array[pos++] = (byte) (i >> 8);
        array[pos] = (byte) i;
    }
}
