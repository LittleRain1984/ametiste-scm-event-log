package org.ametiste.scm.log.service;

import org.ametiste.scm.messaging.data.event.Event;

import java.util.Collection;

/**
 * Interface of event logging processor.
 * <p>
 * Any implementation of interface should be thread-safe. Store and flush operation can be executed in separate threads.
 * Recommended implement store operation with buffer to increase throughput and reduce connections with repository.
 */
public interface EventLogger {

    /**
     * Store event to persistent storage.
     * @param event must not be {@literal null}.
     * @throws LoggingOperationException if any error occurred during store process.
     */
    void store(Event event) throws LoggingOperationException;

    /**
     * Store collection of events to persistent storage.
     * @param events must not be {@literal null}.
     * @throws LoggingOperationException if any error occurred during store process.
     */
    void store(Collection<Event> events) throws LoggingOperationException;

    /**
     * Flush buffered events to external storage.
     * @throws LoggingOperationException if any error occurred during flush.
     */
    void flush() throws LoggingOperationException;
}
