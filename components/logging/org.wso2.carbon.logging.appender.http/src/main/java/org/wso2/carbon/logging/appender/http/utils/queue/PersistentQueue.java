package org.wso2.carbon.logging.appender.http.utils.queue;

import com.google.gson.JsonArray;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class PersistentQueue<T extends Serializable> {

    private static PersistentQueue instance;
    private final String QUEUE_BLOCK_LIST_KEY = "QUEUE_BLOCKS_LIST";
    private final String queueDirectoryPath;
    private final long maxDiskSpaceInBytes;
    private final long maxBatchSizeInBytes;
    private MetadataFileHandler queueMetaDataHandler;
    private QueueBlock appenderBlock, tailerBlock;

    // return singleton instance of the queue
    public static PersistentQueue getInstance(String queueDirectoryPath, long maxDiskSpaceInBytes, long maxBatchSizeInBytes)
            throws PersistentQueueException {

        synchronized (PersistentQueue.class) {
            if (instance == null) {
                instance = new PersistentQueue(queueDirectoryPath, maxDiskSpaceInBytes, maxBatchSizeInBytes);
            }
            return instance;
        }
    }
    private PersistentQueue(String queueDirectoryPath, long maxDiskSpaceInBytes, long maxBatchSizeInBytes)
            throws PersistentQueueException {

        this.queueDirectoryPath = queueDirectoryPath;
        this.maxDiskSpaceInBytes = maxDiskSpaceInBytes;
        this.maxBatchSizeInBytes = maxBatchSizeInBytes;
        init();
    }

    public void enqueue(T object) throws PersistentQueueException {

        byte[] data = serializeObject(object);
        if(data.length == 0) {
            throw new RuntimeException("Error: Unable to serialize object.");
        }
        if (!appenderBlock.append(data)) {
            if(!appenderBlock.getFileName().equals(tailerBlock.getFileName())) {
                appenderBlock.close();
            }
            appenderBlock = createNewBlock();
            appenderBlock.append(data);
        }
    }

    public T dequeue() throws PersistentQueueException {

        byte[] readData = null;
        if(tailerBlock.canConsume()) { // queue block has remaining data
            readData = tailerBlock.consume();
        }
        else if(!appenderBlock.getFileName().equals(tailerBlock.getFileName())) {
            // queue block doesn't have data remaining but there are more blocks to consume
            tailerBlock = loadNextBlock();
            if(tailerBlock != null && tailerBlock.canConsume()) {
                readData = tailerBlock.consume();
            }
        }
        else {
            return null; // queue is empty
        }
        return deserializeObject(readData);
    }

    private void init() throws PersistentQueueException {

        File queueDirectory = new File(this.queueDirectoryPath);
        if(!queueDirectory.exists() && !queueDirectory.mkdirs()) {
            throw new PersistentQueueException("Error: Unable to create queue directory");
        }
        final String QUEUE_METADATA_FILE_NAME = "queue_metadata.pq";
        this.queueMetaDataHandler = new MetadataFileHandler(
                this.queueDirectoryPath + "/" + QUEUE_METADATA_FILE_NAME);
        initMetaData();
    }

    public long getMaxDiskSpaceInBytes() {

        return maxDiskSpaceInBytes;
    }

    private void initMetaData() throws PersistentQueueException {

        // handling the initialization without existing metadata file
        if(!this.queueMetaDataHandler.isInitialized()) {
            queueMetaDataHandler.addJsonArray(QUEUE_BLOCK_LIST_KEY, new JsonArray());
            createNewBlock();
        }
        loadAppenderBlock();
        loadTailerBlock();
    }

    // appender should be loaded first to allow equality check when tailer is being loaded
    private void loadAppenderBlock() throws PersistentQueueException {

        JsonArray queueBlocks = this.queueMetaDataHandler.getAsJsonArray(QUEUE_BLOCK_LIST_KEY);
        String appenderBlockName = queueBlocks.get(0).getAsString();
        this.appenderBlock = QueueBlock.loadBlock(this.queueDirectoryPath, appenderBlockName);
    }

    private void loadTailerBlock() throws PersistentQueueException {

        JsonArray queueBlocks = this.queueMetaDataHandler.getAsJsonArray(QUEUE_BLOCK_LIST_KEY);
        String tailerBlockName = queueBlocks.get(queueBlocks.size() - 1).getAsString();
        if(appenderBlock.getFileName().equals(tailerBlockName)) {
            this.tailerBlock = appenderBlock;
        }
        else {
            this.tailerBlock = QueueBlock.loadBlock(this.queueDirectoryPath, tailerBlockName);
        }
    }

    private byte[] serializeObject(T obj) throws PersistentQueueException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                new GZIPOutputStream(byteArrayOutputStream))) {
            objectOutputStream.writeObject(obj);
        }
        catch (IOException e) {
            throw new PersistentQueueException("Error while serializing object", e);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private T deserializeObject(byte[] bytes) throws PersistentQueueException {

        try (ObjectInputStream objectInputStream = new ObjectInputStream(
                new GZIPInputStream(new ByteArrayInputStream(bytes)))) {
            return (T)objectInputStream.readObject();
        }
        catch (IOException | ClassNotFoundException e) {
            throw new PersistentQueueException("Error while deserializing object", e);
        }
    }

    private QueueBlock createNewBlock() throws PersistentQueueException {


        try {
            if(getCurrentDiskUsage() >= maxDiskSpaceInBytes) {
                throw new PersistentQueueException("Error: Queue disk usage limit reached.");
            }
        } catch (IOException e) {
            throw new PersistentQueueException("Error: Unable to calculate disk usage.", e);
        }
        String QUEUE_BLOCK_FILE_NAME = "qb_%s";
        String newBlockName = String.format(QUEUE_BLOCK_FILE_NAME, System.currentTimeMillis());
        QueueBlock newBlock = new QueueBlock(this.queueDirectoryPath, newBlockName, maxBatchSizeInBytes);
        JsonArray queueBlocks = this.queueMetaDataHandler.getAsJsonArray(QUEUE_BLOCK_LIST_KEY);
        queueBlocks.add(newBlockName);
        this.queueMetaDataHandler.addJsonArray(QUEUE_BLOCK_LIST_KEY, queueBlocks);
        return newBlock;
    }

    private QueueBlock loadNextBlock() throws PersistentQueueException {

        JsonArray queueBlocks = this.queueMetaDataHandler.getAsJsonArray(QUEUE_BLOCK_LIST_KEY);
        QueueBlock consumedTailerBlock = tailerBlock;
        QueueBlock nextBlock = null;
        if(queueBlocks.size()>1) {
            String tailerBlockName = queueBlocks.get(queueBlocks.size() - 2).getAsString();
            nextBlock = QueueBlock.loadBlock(this.queueDirectoryPath, tailerBlockName);
        }
        queueBlocks.remove(queueBlocks.size() - 1);
        consumedTailerBlock.delete();
        this.queueMetaDataHandler.addJsonArray(QUEUE_BLOCK_LIST_KEY, queueBlocks);
        return nextBlock;
    }

    public long getCurrentDiskUsage() throws IOException {

        Path folder = Paths.get(this.queueDirectoryPath);
        try(java.util.stream.Stream<Path> paths = Files.walk(folder)) {
            return paths.filter(p -> p.toFile().isFile())
                    .mapToLong(p -> p.toFile().length())
                    .sum();
        }
    }

    public void close() throws PersistentQueueException {

        this.queueMetaDataHandler.close();
        try {
            this.appenderBlock.close();
            if(!appenderBlock.getFileName().equals(tailerBlock.getFileName())) {
                this.tailerBlock.close();
            }
        } catch (PersistentQueueException e) {
            throw new PersistentQueueException("Error: Unable to close queue", e);
        }
    }
}
