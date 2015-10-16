package org.ametiste.scm.log.service;

import org.ametiste.scm.log.persistent.EventDAO;
import org.ametiste.scm.messaging.data.event.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.*;

import static org.apache.commons.lang3.Validate.isTrue;

/**
 * Default implementation of {@code EventInformer} interface.
 */
public class EventInformerImpl implements EventInformer {

    private final EventDAO eventDAO;

    /**
     * Create new instance of {@code EventInformer}.
     * @param eventDAO DAO to access event repository.
     */
    public EventInformerImpl(EventDAO eventDAO) {
        isTrue(eventDAO != null, "'eventDAO' must be initialized!");
        this.eventDAO = eventDAO;
    }

    @Override
    public long totalCount() {
        return eventDAO.count();
    }

    @Override
    public Event find(UUID id) {
        return eventDAO.findOne(id);
    }

    @Override
    public Collection<Event> getLastEvents(int count, Sort.Direction direction) {

        Pageable pageable = new PageRequest(0, count, new Sort(Sort.Direction.DESC, "timestamp"));
        List<Event> result = eventDAO.findAll(pageable).getContent();
        if (direction == Sort.Direction.DESC) {
            return result;
        } else {
            List<Event> reversed = new LinkedList<>(result);
            Collections.reverse(reversed);
            return reversed;
        }
    }

    @Override
    public Page<Event> getEventsForTime(int startTime, int endTime, int page, int pageSize, Sort.Direction direction) {
        long start = startTime > 0 ? startTime * 1000 : 0;
        long end = endTime > 0 ? endTime * 1000 : System.currentTimeMillis();

        return eventDAO.findAll(start, end, new PageRequest(page, pageSize, new Sort(direction, "timestamp")));
    }
}
