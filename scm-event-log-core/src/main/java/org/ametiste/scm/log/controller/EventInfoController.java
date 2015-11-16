package org.ametiste.scm.log.controller;

import org.ametiste.scm.log.data.info.EventInfoResponse;
import org.ametiste.scm.log.service.EventInformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller provide mapping for info requests:
 * <ul>
 *     <li>get event by id;</li>
 *     <li>get last N events;</li>
 *     <li>get event for specified time period;</li>
 *     <li>get total count of stored events.</li>
 * </ul>
 * <p>
 * Controller provides default value almost for all requests parameters. It makes easier handle default requests.
 */
@RestController
@RequestMapping("/info")
public class EventInfoController {

    public static final String DEFAULT_PAGE_SIZE = "50";
    public static final String DEFAULT_SORT_DIRECTION = "DESC";

    @Autowired
    private EventInformer informer;

    /**
     * Search event in storage by id.
     * @param id event id.
     * @return instance of {@link EventInfoResponse}. If event with specified id not find return {@code EventInfoResponse}
     *         with {@literal null} values.
     */
    @RequestMapping(value = "/event/{id}", method = RequestMethod.GET)
    public EventInfoResponse getEventInfo(@PathVariable UUID id) {
        return new EventInfoResponse(informer.find(id));
    }

    /**
     * Return last N events from storage.
     * @param count number of events to fetch (required).
     * @param direction sort direction. If absent used {@code DEFAULT_SORT_DIRECTION} value.
     */
    @RequestMapping(value = "/event/last", method = RequestMethod.GET)
    public Collection<EventInfoResponse> getLastEvents(
            @RequestParam(value = "count") Integer count,
            @RequestParam(value = "sort", defaultValue = DEFAULT_SORT_DIRECTION) String direction) {

        return informer.getLastEvents(count, Sort.Direction.fromString(direction)).stream()
                .map(EventInfoResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Search events for specified time interval.
     * @param startTime start time point in seconds. If absent set to {@literal -1}.
     * @param endTime end time point in seconds. If absent set to {@literal -1}.
     * @param page zero-based page number. If absent set to zero.
     * @param size page size. If absent set to {@code DEFAULT_PAGE_SIZE}.
     * @param direction elements sort direction. If absent
     * @return {@code Page} with target content.
     *
     * @see Page
     */
    @RequestMapping(value = "/event", method = RequestMethod.GET)
    public Page<EventInfoResponse> getEventsForTimePeriod(
            @RequestParam(value = "start", defaultValue = "-1") int startTime,
            @RequestParam(value = "end", defaultValue = "-1") int endTime,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sort", defaultValue = DEFAULT_SORT_DIRECTION) String direction) {

        return informer.getEventsForTime(startTime, endTime, page, size, Sort.Direction.fromString(direction))
                .map(EventInfoResponse::new);
    }

    /**
     * Return total count of stored events.
     */
    @RequestMapping(value = "/event/count", method = RequestMethod.GET)
    public long getTotalCount() {
        return informer.totalCount();
    }
}
