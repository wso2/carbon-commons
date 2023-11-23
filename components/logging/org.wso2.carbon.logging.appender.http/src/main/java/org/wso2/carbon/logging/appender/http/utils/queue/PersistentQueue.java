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

import com.google.gson.JsonArray;
import org.apache.commons.lang.SerializationException;
import org.wso2.carbon.logging.appender.http.utils.queue.exception.PersistentQueueException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * This class implements a file based queue which can be used to store objects in serialized form in a persistent
 * manner and consumed later.
 * @param <T> type of the object to be stored in the queue.
 */
public class PersistentQueue<T extends Serializable> implements AutoCloseable {

    private static final String QUEUE_BLOCK_LIST_KEY = "QUEUE_BLOCKS_LIST";
    private static final String QUEUE_METADATA_FILE_NAME = "queue_metadata.pq";
    private static final String QUEUE_BLOCK_FILE_NAME = "qb_%s";
    private final String queueDirectoryPath;
    private final long maxDiskSpaceInBytes;
    private final long maxBatchSizeInBytes;
    private MetadataFileHandler queueMetaDataHandler;
    private QueueBlock appenderBlock, tailerBlock;
    private long currentDiskUsage;
    private boolean isDiskFull = false;

    private final Object lock = new Object();

    /**
     * Constructor of the PersistentQueue class.
     * @param queueDirectoryPath path of the queue directory.
     * @param maxDiskSpaceInBytes maximum disk space that can be used by the queue.
     * @param maxBatchSizeInBytes maximum size of a single queue block.
     * @throws PersistentQueueException if an error occurs while creating the queue.
     */
    public PersistentQueue(String queueDirectoryPath, long maxDiskSpaceInBytes, long maxBatchSizeInBytes)
            throws PersistentQueueException {

        this.queueDirectoryPath = queueDirectoryPath;
        this.maxDiskSpaceInBytes = maxDiskSpaceInBytes;
        this.maxBatchSizeInBytes = maxBatchSizeInBytes;
        init();
    }

    /**
     * Enqueues the given object to the queue.
     * @param object object to be enqueued.
     * @throws PersistentQueueException if an error occurs while enqueuing the object.
     */
    public synchronized void enqueue(T object) throws PersistentQueueException {

        byte[] data = serialize(object);
        if (data.length == 0) {
            throw new PersistentQueueException(
                    PersistentQueueException.PersistentQueueErrorTypes.EMPTY_OBJECT,
                    "Unable to serialize object.");
        }
        if (!appenderBlock.append(data)) {
            if (!appenderBlock.getFileName().equals(tailerBlock.getFileName())) {
                appenderBlock.close();
            }
            appenderBlock = createNewBlock();
            appenderBlock.append(data);
        }
    }

    /**
     * Dequeues the next object from the queue.
     * @return dequeued object.
     * @throws PersistentQueueException if an error occurs while dequeue the object.
     */
    public synchronized T dequeue() throws PersistentQueueException {

        byte[] readData = null;
        synchronized (lock) {
            if (tailerBlock.hasUnprocessedItems()) { // queue block has remaining data
                readData = tailerBlock.consume();
                if (!tailerBlock.hasUnprocessedItems()
                        && appenderBlock.getFileName().equals(tailerBlock.getFileName())) {
                    // if the queue is empty, reset the final block to be reused later
                    tailerBlock.setValuesToDefault();
                }
            } else if (queueMetaDataHandler.getAsJsonArray(QUEUE_BLOCK_LIST_KEY).isPresent() &&
                    queueMetaDataHandler.getAsJsonArray(QUEUE_BLOCK_LIST_KEY).get().size() > 1) {
                // queue block doesn't have data remaining but there are more blocks to consume
                tailerBlock = loadNextBlock();
                if (tailerBlock != null) {
                    readData = tailerBlock.consume();
                }
            }
        }
        return readData != null ? (T) deserialize(readData) : null;
    }

    /**
     * Peeks the next object from the queue without removing it.
     * @return peeked object.
     * @throws PersistentQueueException if an error occurs while peeking the object.
     */
    public T peek() throws PersistentQueueException {

        byte[] readData = null;
        synchronized (lock) {
            if (tailerBlock.hasUnprocessedItems()) { // queue block has remaining data
                readData = tailerBlock.peekNextItem();
            } else if (queueMetaDataHandler.getAsJsonArray(QUEUE_BLOCK_LIST_KEY).isPresent() &&
                    queueMetaDataHandler.getAsJsonArray(QUEUE_BLOCK_LIST_KEY).get().size() > 1) {
                // queue block doesn't have data remaining but there are more blocks to consume
                tailerBlock = loadNextBlock();
                if (tailerBlock != null) {
                    readData = tailerBlock.peekNextItem();
                }
            }
        }
        return readData != null ? (T) deserialize(readData) : null;
    }

    /**
     * Checks if the queue is empty.
     * @return boolean if the queue is empty.
     */
    public boolean isEmpty() {

        return getQueueBlocksMetaDataArray().size() == 1
                && !tailerBlock.hasUnprocessedItems();
    }

    /**
     * Checks if the queue is full.
     * @return boolean if the queue is full.
     */
    public boolean isFull() {

        return this.isDiskFull;
    }

    /**
     * Loops through each file in the queue directory and calculates the disk usage of the queue.
     * @return disk usage of the queue.
     * @throws PersistentQueueException if an error occurs while calculating the disk usage.
     */
    public long calculateDiskUsage() throws PersistentQueueException {

        try {
            Path folder = Paths.get(this.queueDirectoryPath);
            try (Stream<Path> paths = Files.walk(folder)) {
                return paths.filter(p -> p.toFile().isFile())
                        .mapToLong(p -> p.toFile().length())
                        .sum();
            }
        } catch (IOException e) {
            throw new PersistentQueueException(
                    PersistentQueueException.PersistentQueueErrorTypes.QUEUE_DISK_USAGE_CALCULATION_FAILED,
                    "Unable to calculate disk usage.", e);
        }
    }

    /**
     * Returns the fraction of the disk space used by the queue.
     * @return fraction of the disk space used by the queue.
     */
    public float getUsedSpaceFraction() {

        return isDiskSpaceFull() ? 1 : (float) currentDiskUsage / maxDiskSpaceInBytes;
    }

    private void init() throws PersistentQueueException {

        File queueDirectory = new File(this.queueDirectoryPath);
        if (!queueDirectory.exists() && !queueDirectory.mkdirs()) {
            throw new PersistentQueueException(
                    PersistentQueueException.PersistentQueueErrorTypes.QUEUE_DIRECTORY_CREATION_FAILED,
                    "Unable to create queue directory");
        }
        this.queueMetaDataHandler = new MetadataFileHandler(
                this.queueDirectoryPath + "/" + QUEUE_METADATA_FILE_NAME);
        initMetaData();
    }

    private void initMetaData() throws PersistentQueueException {

        QueueBlock.initClass(this.queueDirectoryPath);
        // handling the initialization without existing metadata file
        if (!this.queueMetaDataHandler.isInitialized()) {
            queueMetaDataHandler.addJsonArray(QUEUE_BLOCK_LIST_KEY, new JsonArray());
            currentDiskUsage = calculateDiskUsage();
            createNewBlock();
        }
        loadAppenderBlock();
        loadTailerBlock();
        currentDiskUsage = calculateDiskUsage();
        this.queueMetaDataHandler.setIsInitialized(true);
    }

    /**
     * Closes the queue.
     * @throws PersistentQueueException if an error occurs while closing the queue.
     */
    @Override
    public void close() throws PersistentQueueException {

        try {
            this.appenderBlock.close();
            if (!appenderBlock.getFileName().equals(tailerBlock.getFileName())) {
                this.tailerBlock.close();
            }
        } catch (PersistentQueueException e) {
            throw new PersistentQueueException(
                    PersistentQueueException.PersistentQueueErrorTypes.QUEUE_CLOSE_FAILED ,
                    "Unable to close queue", e);
        } finally {
            this.queueMetaDataHandler.close();
        }
    }

    // appender should be loaded first to allow equality check when tailer is being loaded
    private void loadAppenderBlock() throws PersistentQueueException {

        JsonArray queueBlocks = getQueueBlocksMetaDataArray();
        String appenderBlockName = queueBlocks.get(queueBlocks.size() - 1).getAsString();
        this.appenderBlock = QueueBlock.loadBlock(this.queueDirectoryPath, appenderBlockName);
    }

    private void loadTailerBlock() throws PersistentQueueException {

        String tailerBlockName = getQueueBlocksMetaDataArray().get(0).getAsString();
        if (appenderBlock.getFileName().equals(tailerBlockName)) {
            this.tailerBlock = appenderBlock;
        } else {
            this.tailerBlock = QueueBlock.loadBlock(this.queueDirectoryPath, tailerBlockName);
        }
    }

    // logic copied from org.apache.commons.lang.SerializationUtils function
    // this is to be replaced with the use of the library in future
    private static void serialize(Serializable obj, OutputStream outputStream) {

        if (outputStream == null) {
            throw new IllegalArgumentException("The OutputStream must not be null");
        } else {
            ObjectOutputStream out = null;
            try {
                out = new ObjectOutputStream(outputStream);
                out.writeObject(obj);
            } catch (IOException e) {
                throw new SerializationException(e);
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException ignored) {
                }
            }
        }
    }

    // logic copied from org.apache.commons.lang.SerializationUtils function
    // this is to be replaced with the use of the library in future
    private static byte[] serialize(Serializable obj) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
        serialize(obj, baos);
        return baos.toByteArray();
    }

    // logic copied from org.apache.commons.lang.SerializationUtils function
    // this is to be replaced with the use of the library in future
    private static Object deserialize(InputStream inputStream) {

        if (inputStream == null) {
            throw new IllegalArgumentException("The InputStream must not be null");
        } else {
            ObjectInputStream objectInputStream = null;
            Object obj;
            try {
                objectInputStream = new ObjectInputStream(inputStream);
                obj = objectInputStream.readObject();
            } catch (ClassNotFoundException | IOException e) {
                throw new SerializationException(e);
            } finally {
                try {
                    if (objectInputStream != null) {
                        objectInputStream.close();
                    }
                } catch (IOException ignored) {
                }
            }
            return obj;
        }
    }

    // logic copied from org.apache.commons.lang.SerializationUtils function
    // this is to be replaced with the use of the library in future
    private static Object deserialize(byte[] objectData) {

        if (objectData == null) {
            throw new IllegalArgumentException("The byte[] must not be null");
        } else {
            ByteArrayInputStream bais = new ByteArrayInputStream(objectData);
            return deserialize(bais);
        }
    }

    private QueueBlock createNewBlock() throws PersistentQueueException {

        if (isDiskSpaceFull()) {
            this.isDiskFull = true;
            throw new PersistentQueueException(
                    PersistentQueueException.PersistentQueueErrorTypes.QUEUE_DISK_SPACE_LIMIT_EXCEEDED,
                    "Queue disk usage limit reached.");
        }
        String newBlockName = String.format(QUEUE_BLOCK_FILE_NAME, System.currentTimeMillis());
        QueueBlock newBlock = new QueueBlock(this.queueDirectoryPath, newBlockName, maxBatchSizeInBytes);
        JsonArray queueBlocks = getQueueBlocksMetaDataArray();
        queueBlocks.add(newBlockName);
        this.queueMetaDataHandler.addJsonArray(QUEUE_BLOCK_LIST_KEY, queueBlocks);
        updateDiskUsage(true);
        return newBlock;
    }

    private boolean isDiskSpaceFull() {

        return (currentDiskUsage + maxBatchSizeInBytes) > maxDiskSpaceInBytes;
    }

    private QueueBlock loadNextBlock() throws PersistentQueueException {

        JsonArray queueBlocks = getQueueBlocksMetaDataArray();
        QueueBlock consumedTailerBlock = tailerBlock;
        QueueBlock nextBlock = null;
        queueBlocks.remove(0);
        if (queueBlocks.size() > 0) {
            String tailerBlockName = queueBlocks.get(0).getAsString();
            nextBlock = QueueBlock.loadBlock(this.queueDirectoryPath, tailerBlockName);
        }
        consumedTailerBlock.delete();
        this.isDiskFull = false;
        this.queueMetaDataHandler.addJsonArray(QUEUE_BLOCK_LIST_KEY, queueBlocks);
        updateDiskUsage(false);
        return nextBlock;
    }

    private JsonArray getQueueBlocksMetaDataArray() {

        AtomicReference<JsonArray> queueBlocksRef = new AtomicReference<>();
        this.queueMetaDataHandler.getAsJsonArray(QUEUE_BLOCK_LIST_KEY) // QUEUE_BLOCK_LIST_KEY always exists.
                .ifPresent(queueBlocksRef::set);
        return queueBlocksRef.get();
    }

    private void updateDiskUsage(boolean incrementWithDefaultIfFailed) throws PersistentQueueException {

        try {
            currentDiskUsage = calculateDiskUsage();
        } catch (PersistentQueueException e) {
            currentDiskUsage += incrementWithDefaultIfFailed ? maxBatchSizeInBytes : -maxBatchSizeInBytes;
            throw e;
        }
    }
}
