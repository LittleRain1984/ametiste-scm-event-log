package org.ametiste.scm.log.service;

/**
 * {@code ReplayTaskNotFoundException} signals about situation when service can't find replay task in registry.
 */
public class ReplayTaskNotFoundException extends RuntimeException {

    public ReplayTaskNotFoundException(String message) {
        super(message);
    }

    public ReplayTaskNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
