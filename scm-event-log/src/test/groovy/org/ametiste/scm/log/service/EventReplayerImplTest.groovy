package org.ametiste.scm.log.service

import org.ametiste.scm.log.persistent.EventDAO
import org.ametiste.scm.log.util.EventReceiverURLBuilder
import org.ametiste.scm.messaging.data.InstanceLifecycleEventGenerator
import org.ametiste.scm.messaging.sender.EventSenderMock
import org.ametiste.scm.log.data.replay.ReplayTaskStatus
import org.ametiste.scm.messaging.data.event.Event
import org.ametiste.scm.messaging.sender.EventSender
import org.springframework.data.domain.PageImpl
import org.springframework.data.util.CloseableIterator
import spock.lang.Specification

class EventReplayerImplTest extends Specification {

    public static final int BULK_SIZE = 2;

    private static final InstanceLifecycleEventGenerator EVENT_GENERATOR = new InstanceLifecycleEventGenerator();

    private EventDAO eventDAO;
    private EventSender eventSender;
    private EventReplayer eventReplayer;

    def setup() {
        eventDAO = Mock(EventDAO.class)
        eventSender = new EventSenderMock();
        eventReplayer = new EventReplayerImpl(eventDAO, eventSender, BULK_SIZE, new EventReceiverURLBuilder())
    }

    def "should replay all events from storage"() {

        given: "stored events and additional behavior for mocks"
        def events = EVENT_GENERATOR.generate(3)
        def receiver = URI.create("http://localhost")

        eventDAO.findAll(_,_,_) >> { args ->
            def start = args[0]
            def end = args[1]

            assert start == 0
            assert end > 0

            new PageImpl<Event>(Collections.emptyList(), null, events.size())
        }

        eventDAO.findAll(_,_) >> { args ->
            def start = args[0]
            def end = args[1]

            assert start == 0
            assert end > 0

            new CloseableIteratorMock<Event>(events)
        }

        when: "request replay for all events"
        UUID id = eventReplayer.replay(receiver)

        then: "wait for worker done"
        Thread.sleep(500)

        and: "task should be registered"
        ReplayTaskStatus status = eventReplayer.status(id)

        assert status != null
        assert status.getTotalEvents() == events.size()

        and:
        eventSender.totalSendEvents() == events.size()
    }


    private class CloseableIteratorMock<T> implements CloseableIterator<T>{

        private Collection<T> events;
        private int cursor = 0;

        CloseableIteratorMock(Collection<T> events) {
            this.events = events;
        }

        @Override
        void close() {}

        @Override
        boolean hasNext() {
            return cursor < events.size();
        }

        @Override
        T next() {
            return events.getAt(cursor++);
        }
    }
}
