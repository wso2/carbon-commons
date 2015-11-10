package org.wso2.carbon.event.admin.internal.exception;

@Deprecated
public class EventAdminException extends Exception{

    public String errorMessage;

    public EventAdminException() {
    }

    public EventAdminException(String message) {
        super(message);
        errorMessage = message;
    }

    public EventAdminException(String message, Throwable cause) {
        super(message, cause);
        errorMessage = message;
    }

    public EventAdminException(Throwable cause) {
        super(cause);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
