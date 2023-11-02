/*
  * bit sequence structure of messages in the queue block
  * 4 bytes - appender offset value
  * 4 bytes - tailer offset value
  * 4 bytes - message length
  * n bytes - message data
 */
package org.wso2.carbon.logging.appender.http.utils.queue;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

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

    public QueueBlock(final String queueDirectoryPath, final String fileName, final long length) throws PersistentQueueException {

        this.queueDirectoryPath = queueDirectoryPath;
        this.fileName = fileName;
        try {
            String queueBlocksDirectoryPath = queueDirectoryPath + "/" + QUEUE_BLOCK_SUB_DIRECTORY_PATH;
            Files.createDirectories(Paths.get(queueBlocksDirectoryPath));
            this.file = new RandomAccessFile(queueBlocksDirectoryPath + "/" + fileName
                    + QUEUE_BLOCK_FILE_EXTENSION, "rw");
            this.file.setLength(length);
            this.buffer = file.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, length);
            this.file.close();
            initMetaData();
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

        buffer.position(currentAppenderIndex);
        return buffer.remaining() >= (MESSAGE_LENGTH_BIT_COUNT + length);
    }

    public boolean hasUnprocessedItems(){

        return currentTailerIndex < currentAppenderIndex;
    }

    public boolean append(byte[] data){

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

    public byte[] consume(){

        if(hasUnprocessedItems()){
            byte[] data = peekNextItem();
            currentTailerIndex += (MESSAGE_LENGTH_BIT_COUNT + data.length); // 4 bytes for the length
            buffer.putInt(TAILER_OFFSET_VALUE_METADATA_INDEX, currentTailerIndex);
            return data;
        }
        return null;
    }

    public byte[] peekNextItem(){

        if(hasUnprocessedItems()){
            buffer.position(currentTailerIndex);
            int length = buffer.getInt();
            byte[] data = new byte[length];
            buffer.get(data);
            return data;
        }
        return null;
    }

    public void delete() throws PersistentQueueException {

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

        buffer.force();
        try {
            file.close();
        } catch (IOException e) {
            throw new PersistentQueueException(
                    PersistentQueueException.PersistentQueueErrorTypes.QUEUE_BLOCK_CLOSE_FAILED,
                    "Error: Unable to close meta data file", e);
        }
    }

    private void initMetaData() {

        this.currentAppenderIndex = this.currentTailerIndex = METADATA_BLOCK_LENGTH;
        this.buffer.putInt(APPENDER_OFFSET_VALUE_METADATA_INDEX, currentAppenderIndex);
        this.buffer.putInt(TAILER_OFFSET_VALUE_METADATA_INDEX, currentTailerIndex);
    }
}
