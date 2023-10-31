package org.wso2.carbon.logging.appender.http.utils;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.RollCycles;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class PersistentQueue {

    private static final String METADATA_FILE_NAME = "metadata.json";
    private static final String CURRENT_QUEUE_SIZE = "currentQueueSize";
    private static final String TAILER_INDEX = "tailerIndex";
    private static final String TAILER_CYCLE = "tailerCycle";
    private static PersistentQueue instance;
    private final ExcerptAppender appender;
    private final ExcerptTailer tailer;
    private final long queueLimit;
    private long currentQueueSize = 0;
    private File currentFile = null;
    private final MetadataFileHandler metadataFileHandler;
    private PersistentQueue(long queueLimit, String directoryPath) {

        this.metadataFileHandler = new MetadataFileHandler(directoryPath + "/" + METADATA_FILE_NAME);
        this.queueLimit = queueLimit;
        ChronicleQueue queue = SingleChronicleQueueBuilder
                .binary(directoryPath)
                .path(directoryPath)
                .blockSize(256)
                .rollCycle(RollCycles.FAST_DAILY)
                .build();
        appender = queue.createAppender();
        appender.singleThreadedCheckDisabled(true);
        tailer = queue.createTailer();
        tailer.singleThreadedCheckDisabled(true);
        initMetaData();
    }

    public static PersistentQueue getInstance(long maxMessageCount, String directoryPath) {

        synchronized (PersistentQueue.class) {
            if (instance == null) {
                instance = new PersistentQueue(maxMessageCount, directoryPath);
            }
        }
        return instance;
    }

    public synchronized boolean enqueue(Serializable serializableObj) throws PersistentQueueException {

        if (currentQueueSize >= queueLimit) {
            return false;
        }
        else {
            try {
                byte[] serializedObject = serializeObject(serializableObj);
                appender.writeBytes(b -> b.write(serializedObject));
            } catch (IOException e) {
                throw new PersistentQueueException("Error while serializing object", e);
            }
        }
        currentQueueSize++;
        saveMetaData();
        return true;
    }

    public synchronized Object dequeue() throws PersistentQueueException {

        AtomicReference<Object> deserializedObject = new AtomicReference<>();
        Bytes<ByteBuffer> bytes = Bytes.elasticByteBuffer();
        try {
            tailer.readBytes(bytes);
            try {
                if(bytes.toByteArray().length==0) {
                    return null;
                }
                deserializedObject.set(deserializeObject(bytes.toByteArray()));
            } catch (IOException | ClassNotFoundException e) {
                throw new PersistentQueueException("Error while deserializing object", e);
            }
        }
        finally {
            bytes.releaseLast();
        }

        if(currentFile==null){
            currentFile = tailer.currentFile();
        }
        else if (tailer.currentFile()!=currentFile) {
            if(!currentFile.delete()){
                System.out.println("Log file deletion failed");
            }
            currentFile = tailer.currentFile();
        }
        currentQueueSize--;
        saveMetaData();
        return deserializedObject.get();
    }

    public synchronized void undoPreviousDequeue() {

        tailer.moveToIndex(tailer.lastReadIndex());
        currentQueueSize++;
        saveMetaData();
    }

    public long getCurrentQueueSize() {

        return currentQueueSize;
    }

    public long getQueueLimit() {
        return queueLimit;
    }

    private void initMetaData(){

        if(metadataFileHandler.readMetadata(CURRENT_QUEUE_SIZE)!=null){
            currentQueueSize = Long.parseLong(metadataFileHandler.readMetadata(CURRENT_QUEUE_SIZE));
        }
        if(metadataFileHandler.readMetadata(TAILER_CYCLE)!=null){
            tailer.moveToCycle(Integer.parseInt(metadataFileHandler.readMetadata(TAILER_CYCLE)));
        }
        if(metadataFileHandler.readMetadata(TAILER_INDEX)!=null){
            tailer.moveToIndex(Long.parseLong(metadataFileHandler.readMetadata(TAILER_INDEX)));
        }
        this.currentFile=tailer.currentFile();
    }

    private static byte[] serializeObject(Serializable obj) throws IOException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new GZIPOutputStream(byteArrayOutputStream))) {
            objectOutputStream.writeObject(obj);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private static Object deserializeObject(byte[] bytes) throws IOException, ClassNotFoundException {

        try (ObjectInputStream objectInputStream = new ObjectInputStream(new GZIPInputStream(new ByteArrayInputStream(bytes)))) {
            return objectInputStream.readObject();
        }
    }

    private void saveMetaData() {
        metadataFileHandler.writeMetadata(CURRENT_QUEUE_SIZE, String.valueOf(currentQueueSize));
        metadataFileHandler.writeMetadata(TAILER_INDEX, String.valueOf(tailer.index()));
        metadataFileHandler.writeMetadata(TAILER_CYCLE, String.valueOf(tailer.cycle()));
    }
}
