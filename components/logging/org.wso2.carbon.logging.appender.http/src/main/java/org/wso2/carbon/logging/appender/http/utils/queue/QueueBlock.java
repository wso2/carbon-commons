/*
 *
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.wso2.carbon.logging.appender.http.utils.queue;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class QueueBlock {

    private static final String QUEUE_BLOCK_SUB_DIRECTORY_PATH ="blocks";
    private static final String QUEUE_BLOCK_FILE_EXTENSION = ".pqbd"; // persistent queue block data
    private static final int METADATA_BLOCK_LENGTH = 8;
    private static final int APPENDER_OFFSET_VALUE_METADATA_INDEX = 0;
    private static final int TAILER_OFFSET_VALUE_METADATA_INDEX = 4;
    private static final int MESSAGE_LENGTH_BIT_COUNT = 4;
    private final RandomAccessFile file;
    private final MappedByteBuffer buffer;
    private final String queueDirectoryPath;
    private final String fileName;
    private int currentAppenderIndex;
    private int currentTailerIndex;
    private final Map<Integer,byte[]> lastPeekedItems;

    private final ReentrantReadWriteLock resetLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = resetLock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = resetLock.writeLock();

    public QueueBlock(final String queueDirectoryPath, final String fileName, final long length)
            throws PersistentQueueException {

        this.queueDirectoryPath = queueDirectoryPath;
        this.fileName = fileName;
        try {
            String queueBlocksDirectoryPath = queueDirectoryPath + "/" + QUEUE_BLOCK_SUB_DIRECTORY_PATH;
            Files.createDirectories(Paths.get(queueBlocksDirectoryPath));
            this.file = new RandomAccessFile(queueBlocksDirectoryPath + "/" + fileName
                    + QUEUE_BLOCK_FILE_EXTENSION, "rw");
            this.file.setLength(length);
            this.buffer = file.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, length);
            this.lastPeekedItems = new HashMap<>();
            setValuesToDefault();
        } catch (IOException e) {
            throw new PersistentQueueException(
                    PersistentQueueException.PersistentQueueErrorTypes.QUEUE_BLOCK_CREATION_FAILED,
                    "Error: Unable to create metadata file", e);
        }
    }

    // this constructor is to be used when loading a block from disk
    private QueueBlock(final String queueDirectoryPath, final String fileName) throws PersistentQueueException {

        this.queueDirectoryPath = queueDirectoryPath;
        this.fileName = fileName;
        try {
            String filePath = queueDirectoryPath + "/" + QUEUE_BLOCK_SUB_DIRECTORY_PATH + "/" + fileName
                    + QUEUE_BLOCK_FILE_EXTENSION;
            RandomAccessFile file = new RandomAccessFile(filePath, "rw");
            this.file = file;
            this.buffer = file.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, file.length());
            this.currentAppenderIndex = buffer.getInt(APPENDER_OFFSET_VALUE_METADATA_INDEX);
            this.currentTailerIndex = buffer.getInt(TAILER_OFFSET_VALUE_METADATA_INDEX);
            this.lastPeekedItems = new HashMap<>();
        } catch (IOException e) {
            throw new PersistentQueueException(
                    PersistentQueueException.PersistentQueueErrorTypes.QUEUE_BLOCK_CREATION_FAILED,
                    "Error: Unable to create metadata file", e);
        }
    }

    public static QueueBlock loadBlock(String directoryPath, String fileName) throws PersistentQueueException {

        String queueBlockFilePath = directoryPath + "/" + QUEUE_BLOCK_SUB_DIRECTORY_PATH + "/" + fileName
                + QUEUE_BLOCK_FILE_EXTENSION;
        if(!Files.exists(Paths.get(queueBlockFilePath))){
            throw new PersistentQueueException(
                    PersistentQueueException.PersistentQueueErrorTypes.QUEUE_BLOCK_FILE_NOT_FOUND,
                    "Error: Unable to find queue block data file.");
        }
        return new QueueBlock(directoryPath, fileName);
    }

    public boolean canAppend(int length){

        try {
            readLock.lock();
            buffer.position(currentAppenderIndex);
            return buffer.remaining() >= (MESSAGE_LENGTH_BIT_COUNT + length);
        }
        finally {
            readLock.unlock();
        }
    }

    public boolean hasUnprocessedItems(){

        try {
            readLock.lock();
            return currentTailerIndex < currentAppenderIndex;
        }
        finally {
            readLock.unlock();
        }
    }

    public synchronized boolean append(byte[] data){

        try {
            // read lock used as append operation and consume operation work on different pointers. Therefore, no
            // need to synchronize them.
            readLock.lock();
            if(canAppend(data.length)){
                buffer.position(currentAppenderIndex);
                buffer.putInt(data.length);
                buffer.put(data);
                currentAppenderIndex += (MESSAGE_LENGTH_BIT_COUNT + data.length); // 4 bytes for the length
                buffer.putInt(APPENDER_OFFSET_VALUE_METADATA_INDEX, currentAppenderIndex);
                return true;
            }
            return false;
        }
        finally {
            readLock.unlock();
        }
    }

    public synchronized byte[] consume(){

        try {
            // read lock used as append operation and consume operation work on different pointers. Therefore, no
            // need to synchronize them.
            readLock.lock();
            if (hasUnprocessedItems()) {
                byte[] data = peekNextItem();
                lastPeekedItems.remove(currentTailerIndex);
                currentTailerIndex += (MESSAGE_LENGTH_BIT_COUNT + data.length); // 4 bytes for the length
                buffer.putInt(TAILER_OFFSET_VALUE_METADATA_INDEX, currentTailerIndex);
                return data;
            }
            return null;
        }
        finally {
            readLock.unlock();
        }
    }

    public byte[] peekNextItem(){
        try {
            readLock.lock();
            if(lastPeekedItems.containsKey(currentTailerIndex)){
                return lastPeekedItems.get(currentTailerIndex);
            }
            if (hasUnprocessedItems()) {
                buffer.position(currentTailerIndex);
                int length = buffer.getInt();
                byte[] data = new byte[length];
                buffer.get(data);
                lastPeekedItems.put(currentTailerIndex, data);
                return data;
            }
            return null;
        }
        finally {
            readLock.unlock();
        }
    }

    public synchronized void delete() throws PersistentQueueException {

        try {
            writeLock.lock();
            String queueBlockDataFilePath = queueDirectoryPath + "/" + QUEUE_BLOCK_SUB_DIRECTORY_PATH + "/" + fileName
                    + QUEUE_BLOCK_FILE_EXTENSION;
            this.close();
            try {
                Files.deleteIfExists(Paths.get(queueBlockDataFilePath));
            } catch (IOException e) {
                throw new PersistentQueueException(
                        PersistentQueueException.PersistentQueueErrorTypes.QUEUE_BLOCK_DELETION_FAILED,
                        "Error: Unable to delete meta data file", e);
            }
        }
        finally {
            writeLock.unlock();
        }
    }

    public String getFileName() {

        return fileName;
    }

    public long getLength() throws PersistentQueueException {

        try {
            return file.length();
        } catch (IOException e) {
            throw new PersistentQueueException(
                    PersistentQueueException.PersistentQueueErrorTypes.QUEUE_BLOCK_LENGTH_CALCULATION_FAILED,
                    "Error: Unable to get length of meta data file", e);
        }
    }

    public void close() throws PersistentQueueException {

        try {
            writeLock.lock();
            buffer.force();
            try {
                file.close();
            } catch (IOException e) {
                throw new PersistentQueueException(
                        PersistentQueueException.PersistentQueueErrorTypes.QUEUE_BLOCK_CLOSE_FAILED,
                        "Error: Unable to close meta data file", e);
            }
        }
        finally {
            writeLock.unlock();
        }
    }

    public void setValuesToDefault() {

        try {
            writeLock.lock();
            this.currentAppenderIndex = this.currentTailerIndex = METADATA_BLOCK_LENGTH;
            this.buffer.putInt(APPENDER_OFFSET_VALUE_METADATA_INDEX, currentAppenderIndex);
            this.buffer.putInt(TAILER_OFFSET_VALUE_METADATA_INDEX, currentTailerIndex);
        }
        finally {
            writeLock.unlock();
        }
    }
}
