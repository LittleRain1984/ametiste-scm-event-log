package org.ametiste.scm.log.service

import org.ametiste.scm.messaging.data.InstanceStartupEventGenerator
import org.ametiste.scm.log.persistent.EventDAO
import org.ametiste.scm.messaging.data.event.Event
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import spock.lang.Specification

import static org.ametiste.scm.messaging.data.EventComparator.equals

class EventInformerImplTest extends Specification {

    private static final InstanceStartupEventGenerator EVENT_GENERATOR = new InstanceStartupEventGenerator();

    private EventInformer eventInformer;
    private EventDAO eventDAO;

    def setup() {
        eventDAO = Mock(EventDAO.class)
        eventInformer = new EventInformerImpl(eventDAO)
    }

    def "test totalCount()"() {
        when:
        def count = eventInformer.totalCount()

        then:
        1 * eventDAO.count() >> 2L

        and:
        count == 2L
    }

    def "test find()"() {
        Event event = EVENT_GENERATOR.generate()

        when:
        def returnedEvent = eventInformer.find(event.getId())

        then:
        1 * eventDAO.findOne(event.getId()) >> event

        and:
        equals(returnedEvent, event)
    }

    def "getLastEvents() should send correct request to get last events"() {
        def count = 10;

        when: "send request with ASC sort"
        eventInformer.getLastEvents(count, Sort.Direction.ASC)

        then: "must send DESC sort request to get last events and then sort"
        1 * eventDAO.findAll(_ as Pageable) >> { arguments ->
            final Pageable pageable = arguments[0];

            assert pageable.getPageNumber() == 0
            assert pageable.getPageSize() == count
            assert pageable.getSort().getOrderFor("timestamp").getDirection().equals(Sort.Direction.DESC)

            new PageImpl<Event>(Collections.emptyList(), pageable, 0)
        }
    }

    def "should request all event with default parameters"() {
        def page = 2;
        def count = 10;
        def direction = Sort.Direction.ASC;

        when:
        eventInformer.getEventsForTime(-1, -1, page, count, direction)

        then:
        1 * eventDAO.findAll(_, _, _ as Pageable) >> { arguments ->
            long start = arguments[0]
            long end = arguments[1]
            final Pageable pageable = arguments[2];

            assert start == 0
            assert end > 0
            assert pageable.getPageNumber() == page
            assert pageable.getPageSize() == count
            assert pageable.getSort().getOrderFor("timestamp").getDirection().equals(direction)

            new PageImpl<Event>(Collections.emptyList(), pageable, 0)
        }
    }

    def "should request event for specified time period"() {
        def startTime = 15000000;
        def endTime = 16000000;
        def page = 2;
        def count = 10;
        def direction = Sort.Direction.ASC;

        when:
        eventInformer.getEventsForTime(startTime, endTime, page, count, direction)

        then:
        1 * eventDAO.findAll(_, _, _ as Pageable) >> { arguments ->
            long start = arguments[0]
            long end = arguments[1]
            final Pageable pageable = arguments[2];

            assert start == startTime * 1000
            assert end == endTime * 1000
            assert pageable.getPageNumber() == page
            assert pageable.getPageSize() == count
            assert pageable.getSort().getOrderFor("timestamp").getDirection().equals(direction)

            new PageImpl<Event>(Collections.emptyList(), pageable, 0)
        }
    }
}
