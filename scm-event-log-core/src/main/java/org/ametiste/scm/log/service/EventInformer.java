package org.ametiste.scm.log.service;

import org.ametiste.scm.messaging.data.event.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.Collection;
import java.util.UUID;

/**
 * {@code EventInformer} interface provides protocol to retrieve information from event persistent storage
 * in convenient form.
 */
public interface EventInformer {

    /**
     * @return total count of all stored events.
     */
    long totalCount();

    /**
     * Find event with specified id.
     * @param id target event id.
     * @return {@code Event} instance or {@literal null} is event with id absent.
     */
    Event find(UUID id);

    /**
     * Retrieve last N stored events.
     * @param count number of events to retrieve.
     * @param direction sort direction.
     * @return collection with founded events.
     */
    Collection<Event> getLastEvents(int count, Sort.Direction direction);

    /**
     * Retrieve events for specified time period.
     * @param start start time point in seconds. If value is negative number informer search without lower time
     *              period bound.
     * @param end end time point in seconds. If value is negative number informer search with current time upper
     *            time period bound.
     * @param page zero-based page number.
     * @param pageSize number of documents per page.
     * @param direction sort direction.
     * @return {@code Page} with founded events content
     */
    Page<Event> getEventsForTime(int start, int end, int page, int pageSize, Sort.Direction direction);
}
