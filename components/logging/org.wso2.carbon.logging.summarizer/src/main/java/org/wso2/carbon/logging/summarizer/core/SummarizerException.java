package org.wso2.carbon.logging.summarizer.core;

public class SummarizerException extends Exception {

    public SummarizerException(String msg, Exception e) {
        super(msg, e);
    }

    public SummarizerException(String msg) {
        super(msg);
    }
}
