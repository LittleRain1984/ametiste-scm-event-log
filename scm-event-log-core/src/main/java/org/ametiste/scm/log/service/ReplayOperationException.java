package org.ametiste.scm.log.service;

/**
 * {@code ReplayOperationException} signals about error that occurred during replay process execution or communication
 * with replay service.
 */
public class ReplayOperationException extends RuntimeException {

    public ReplayOperationException(String message) {
        super(message);
    }

    public ReplayOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
