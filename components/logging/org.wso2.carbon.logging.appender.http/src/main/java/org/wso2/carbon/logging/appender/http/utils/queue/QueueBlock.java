package org.wso2.carbon.logging.appender.http.utils.queue;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

public class QueueBlock {

    private static final String QUEUE_BLOCK_SUB_DIRECTORY_PATH ="tmp/blocks";
    private static final String META_DATA_SUB_DIRECTORY_PATH ="tmp/meta";
    RandomAccessFile file;
    private final MappedByteBuffer buffer;
    private int currentAppenderOffset = 0;
    private int currentTailerOffset = 0;
    private String queueDirectoryPath;
    private String fileName;
    private MetadataFileHandler metadataFileHandler;
    public QueueBlock(final String queueDirectoryPath, final String fileName, final long length) throws PersistentQueueException {

        this.queueDirectoryPath = queueDirectoryPath;
        this.fileName = fileName;
        try {
            String queueBlocksDirectoryPath = queueDirectoryPath + "/" + QUEUE_BLOCK_SUB_DIRECTORY_PATH;
            Files.createDirectories(Paths.get(queueBlocksDirectoryPath));
            this.file = new RandomAccessFile(queueBlocksDirectoryPath + "/" + fileName, "rw");
            this.file.setLength(length);
            this.buffer = file.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, length);
            this.metadataFileHandler = new MetadataFileHandler(queueDirectoryPath + "/" + META_DATA_SUB_DIRECTORY_PATH
                    + "/" + fileName + ".meta");
            initMetaData();
        } catch (PersistentQueueException | IOException e) {
            throw new PersistentQueueException("Error: Unable to create metadata file", e);
        }
    }

    // this constructor is to be used when loading a block from disk
    public QueueBlock(final String queueDirectoryPath, final String fileName) throws PersistentQueueException {

        this.queueDirectoryPath = queueDirectoryPath;
        this.fileName = fileName;
        try {
            String filePath = queueDirectoryPath + "/" + QUEUE_BLOCK_SUB_DIRECTORY_PATH + "/" + fileName;
            RandomAccessFile file = new RandomAccessFile(filePath, "rw");
            this.file = file;
            this.buffer = file.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, file.length());
            this.metadataFileHandler = new MetadataFileHandler(queueDirectoryPath + "/" + META_DATA_SUB_DIRECTORY_PATH
                    + "/" + fileName + ".meta");
        } catch (IOException e) {
            throw new PersistentQueueException("Error: Unable to load metadata file", e);
        }
    }

    public static QueueBlock loadBlock(String directoryPath, String fileName) throws PersistentQueueException {

        if(Files.exists(Paths.get(directoryPath + "/" + QUEUE_BLOCK_SUB_DIRECTORY_PATH + "/" + fileName))){
            return new QueueBlock(directoryPath, fileName);
        }
        throw new PersistentQueueException("Error: Unable to load block");
    }

    public boolean canAppend(int length){

        buffer.position(currentAppenderOffset);
        return buffer.remaining() >= length;
    }

    public boolean canConsume(){

        return currentTailerOffset < currentAppenderOffset;
    }

    public boolean append(byte[] data){

        if(canAppend(data.length)){
            buffer.position(currentAppenderOffset);
            buffer.putInt(data.length);
            buffer.put(data);
            currentAppenderOffset += data.length + 4; // 4 bytes for the length
            return true;
        }
        return false;
    }

    public byte[] consume(){

        if(canConsume()){
            buffer.position(currentTailerOffset);
            int length = buffer.getInt();
            byte[] data = new byte[length];
            buffer.get(data);
            currentTailerOffset += length + 4; // 4 bytes for the length
            return data;
        }
        return null;
    }

    public void delete() throws PersistentQueueException {

        String filePath = queueDirectoryPath + "/" + QUEUE_BLOCK_SUB_DIRECTORY_PATH + "/" + fileName;
        this.close();
        try {
            metadataFileHandler.deleteFile();
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            throw new PersistentQueueException("Error: Unable to delete file", e);
        }
    }

    public String getFileName() {
        return fileName;
    }

    public void close() throws PersistentQueueException {

        try {
            file.close();
            metadataFileHandler.close();
        } catch (IOException e) {
            throw new PersistentQueueException("Error: Unable to close file", e);
        }
    }

    private void initMetaData() {

        if(!metadataFileHandler.isInitialized()) {
            metadataFileHandler.addLong("currentAppenderOffset", 0);
            metadataFileHandler.addLong("currentTailerOffset", 0);
        }
    }
}
