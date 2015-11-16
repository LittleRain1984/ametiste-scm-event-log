package org.ametiste.scm.log.service;

import org.ametiste.scm.log.persistent.EventDAO;

import org.ametiste.scm.messaging.data.event.Event;
import org.springframework.dao.DuplicateKeyException;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.apache.commons.lang3.Validate.isTrue;

/**
 * Default implementation of {@code EventLogger} interface.
 * <p>
 * Flush has mechanism to resolve situation when we some how receive event with already existed id.
 * In this case we failed to write all events in each flush period and only service restart can fix this.
 * Assumes that event with same id is the same event. Logger retry do save operation instead insert and duplicated
 * event override itself. After that system will work normal.
 */
public class EventLoggerImpl implements EventLogger {

    private final EventDAO eventDAO;

    private final ConcurrentLinkedQueue<Event> temporaryQueue;

    /**
     * Create new instance of {@code EventLogger}.
     * @param eventDAO DAO to access event repository.
     */
    public EventLoggerImpl(EventDAO eventDAO) {
        isTrue(eventDAO != null, "'eventDAO' must be initialized!");

        this.eventDAO = eventDAO;
        this.temporaryQueue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void store(Event event) throws LoggingOperationException {
        isTrue(event != null, "'event' must not be null!");
        temporaryQueue.add(event);
    }

    @Override
    public void store(Collection<Event> events) throws LoggingOperationException {
        isTrue(events != null, "'events' collection must not be null!");
        temporaryQueue.addAll(events.stream().filter(Objects::nonNull).collect(Collectors.toList()));
    }

    @Override
    public void flush() throws LoggingOperationException {
        List<Event> storedEvents = StreamSupport.stream(temporaryQueue.spliterator(), false).collect(Collectors.toList());
        Collection<Event> savedEvents;
        if (storedEvents.size() > 0) {
            try {
                savedEvents = eventDAO.insert(storedEvents);
            } catch (DuplicateKeyException e) {
                savedEvents = eventDAO.save(storedEvents);
            }
            temporaryQueue.removeAll(savedEvents);
        }
    }
}
