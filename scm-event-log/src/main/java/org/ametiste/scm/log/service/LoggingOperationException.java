package org.ametiste.scm.log.service;

/**
 * {@code LoggingOperationException} signals about error that occurred during logging process execution.
 */
public class LoggingOperationException extends RuntimeException {

    public LoggingOperationException(String message) {
        super(message);
    }

    public LoggingOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
