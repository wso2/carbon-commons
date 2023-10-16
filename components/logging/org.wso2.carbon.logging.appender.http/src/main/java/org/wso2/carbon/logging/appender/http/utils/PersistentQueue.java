package org.wso2.carbon.logging.appender.http.utils;

import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.RollCycles;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;

import java.io.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class PersistentQueue {

    private static final String METADATA_FILE_NAME = "metadata.json";
    private static final String CURRENT_QUEUE_SIZE = "currentQueueSize";
    private static final String TAILER_INDEX = "tailerIndex";
    private static final String TAILER_CYCLE = "tailerCycle";
    private static PersistentQueue instance;
    private final ChronicleQueue queue;
    private final ExcerptAppender appender;
    private final ExcerptTailer tailer;
    private final String directoryPath;
    private final long queueLimit;
    private long currentQueueSize = 0;
    private File currentFile = null;
    private final MetadataFileHandler metadataFileHandler;
    private PersistentQueue(long queueLimit, String directoryPath) {

        this.metadataFileHandler = new MetadataFileHandler(directoryPath + "/" + METADATA_FILE_NAME);
        this.directoryPath = directoryPath;
        this.queueLimit = queueLimit;
        queue = SingleChronicleQueueBuilder
                .binary(this.directoryPath)
                .path(this.directoryPath)
                .blockSize(256)
                .rollCycle(RollCycles.FIVE_MINUTELY)
                .build();
        appender = queue.createAppender();
        tailer = queue.createTailer();
        initMetaData();
    }

    public static PersistentQueue getInstance(long maxMessageCount, String directoryPath) {

        if (instance == null) {
            instance = new PersistentQueue(maxMessageCount, directoryPath);
        }
        return instance;
    }


    public boolean enqueue(Serializable serializableObj) {

        if (currentQueueSize >= queueLimit) {
            return false;
        }
        else {
            try {
                byte[] serializedObject = serializeObject(serializableObj);
                appender.writeBytes(b -> b.write(serializedObject));
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        currentQueueSize++;
        saveMetaData();
        return true;
    }

    public Object peek() {

        AtomicReference<Object> deserializedObject = new AtomicReference<>();
        tailer.readBytes(b->{
            try {
                deserializedObject.set(deserializeObject(b.toByteArray()));
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
        tailer.moveToIndex(tailer.lastReadIndex());
        return deserializedObject.get();
    }

    public Object dequeue() {

        AtomicReference<Object> deserializedObject = new AtomicReference<>();
        tailer.readBytes(b->{
            try {
                deserializedObject.set(deserializeObject(b.toByteArray()));
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        });

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
        if(currentQueueSize<2){
            System.out.println("Queue size is less than 2");
        }
        saveMetaData();

        return deserializedObject.get();
    }

    private void initMetaData(){

        if(metadataFileHandler.readMetadata(CURRENT_QUEUE_SIZE)!=null){
            currentQueueSize = Long.parseLong(metadataFileHandler.readMetadata(CURRENT_QUEUE_SIZE));
        }
        if(metadataFileHandler.readMetadata(TAILER_CYCLE)!=null){
            tailer.moveToIndex(Long.parseLong(metadataFileHandler.readMetadata(TAILER_CYCLE)));
        }
        if(metadataFileHandler.readMetadata(TAILER_INDEX)!=null){
            tailer.moveToIndex(Long.parseLong(metadataFileHandler.readMetadata(TAILER_INDEX)));
        }
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
