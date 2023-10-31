package org.wso2.carbon.logging.appender.http.utils.queue;

public class PersistentQueueException extends Exception {

    public PersistentQueueException(String message) {
        super(message);
    }

    public PersistentQueueException(String message, Throwable cause) {
        super(message, cause);
    }
}
