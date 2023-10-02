package org.wso2.carbon.logging.appender.http.utils;

import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;

import java.io.*;
import java.util.concurrent.atomic.AtomicReference;

public class PersistentQueue {

    private static PersistentQueue instance;
    private ChronicleQueue queue;
    private ExcerptAppender appender;
    private ExcerptTailer tailer;
    private PersistentQueue() {
        String filePath = "some/path/to/file";
        init(filePath);
    }

    public static PersistentQueue getInstance() {
        if (instance == null) {
            instance = new PersistentQueue();
        }
        return instance;
    }

    public void init(String filePath){
        queue = SingleChronicleQueueBuilder.binary(filePath).path(filePath).build();
        appender = queue.createAppender();
        tailer = queue.createTailer();
    }

    public boolean enqueue(Serializable serializableObj) {
        try {
            byte[] serializedObject = serializeObject(serializableObj);
            appender.writeBytes(b->b.write(serializedObject));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
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
        return deserializedObject.get();
    }

    public static byte[] serializeObject(Serializable obj) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(obj);
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static Object deserializeObject(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return objectInputStream.readObject();
        }
    }


}
