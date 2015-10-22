package org.wso2.carbon.event.client.broker;

@Deprecated
public class BrokerClientException extends Exception{
    public String errorMessage;

    public BrokerClientException(String message, Throwable cause) {
        super(message, cause);
        errorMessage = message;
    }

    public BrokerClientException(String message) {
        super(message);
        errorMessage = message;
    }

    public BrokerClientException(Throwable cause) {
        super(cause);
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
