package org.wso2.carbon.logging.appender.http.utils.queue;

public class PersistentQueueException extends Exception {

    public enum PersistentQueueErrorTypes {
        QUEUE_DIRECTORY_CREATION_FAILED,
        QUEUE_DISK_SPACE_LIMIT_EXCEEDED,
        QUEUE_META_DATA_FILE_CREATION_FAILED,
        QUEUE_DISK_USAGE_CALCULATION_FAILED,
        QUEUE_MESSAGE_DESERIALIZATION_FAILED,
        QUEUE_MESSAGE_SERIALIZATION_FAILED,
        QUEUE_CLOSE_FAILED,
        QUEUE_BLOCK_CLOSE_FAILED,
        QUEUE_BLOCK_CREATION_FAILED,
        QUEUE_BLOCK_FILE_NOT_FOUND,
        QUEUE_BLOCK_DELETION_FAILED,
        QUEUE_BLOCK_LENGTH_CALCULATION_FAILED,
        EMPTY_OBJECT
    }

    private final PersistentQueueErrorTypes errorType;

    public PersistentQueueException(PersistentQueueErrorTypes errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public PersistentQueueException(PersistentQueueErrorTypes errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }

    public PersistentQueueErrorTypes getErrorType() {
        return errorType;
    }
}
