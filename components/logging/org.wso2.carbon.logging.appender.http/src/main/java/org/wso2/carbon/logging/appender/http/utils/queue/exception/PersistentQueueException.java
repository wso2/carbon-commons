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

package org.wso2.carbon.logging.appender.http.utils.queue.exception;

/**
 * This class implements exception to be used in PersistentQueue.
 */
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
        EMPTY_OBJECT,
        QUEUE_META_DATA_FILE_READING_FAILED
    }

    private final PersistentQueueErrorTypes errorType;

    /**
     * Constructor for PersistentQueueException.
     *
     * @param errorType error type
     * @param message   error message
     */
    public PersistentQueueException(PersistentQueueErrorTypes errorType, String message) {

        super(message);
        this.errorType = errorType;
    }

    /**
     * Constructor for PersistentQueueException.
     * @param errorType error type
     * @param message error message
     * @param cause cause of the exception
     */
    public PersistentQueueException(PersistentQueueErrorTypes errorType, String message, Throwable cause) {

        super(message, cause);
        this.errorType = errorType;
    }

    /**
     * Get the error type.
     * @return error type
     */
    public PersistentQueueErrorTypes getErrorType() {

        return errorType;
    }
}
