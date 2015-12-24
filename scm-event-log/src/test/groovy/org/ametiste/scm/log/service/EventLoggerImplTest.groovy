package org.ametiste.scm.log.service

import org.ametiste.scm.messaging.data.InstanceLifecycleEventGenerator
import org.ametiste.scm.log.persistent.EventDAO
import org.ametiste.scm.messaging.data.event.Event
import org.ametiste.scm.messaging.data.event.InstanceLifecycleEvent
import spock.lang.Specification

class EventLoggerImplTest extends Specification {

    private static final InstanceLifecycleEventGenerator EVENT_GENERATOR = new InstanceLifecycleEventGenerator();

    private EventLogger eventLogger;
    private EventDAO eventDAO;

    def setup() {
        eventDAO = Mock(EventDAO.class)
        eventLogger = new EventLoggerImpl(eventDAO)
    }

    def "should store event"() {
        given:
        InstanceLifecycleEvent event = EVENT_GENERATOR.generate()

        when: "store event and flush"
        eventLogger.store(event)
        eventLogger.flush()

        then: "expect store this event with EventDAO"
        1 * eventDAO.insert(_) >> { args ->
            Collection<Event> bulk = args[0];

            assert bulk.size() == 1
            assert bulk.getAt(0) == event

            bulk
        }
    }

    def "should store collection of events"() {
        given:
        Collection<Event> events = [EVENT_GENERATOR.generate(), EVENT_GENERATOR.generate()]

        when: "store events and flush"
        eventLogger.store(events)
        eventLogger.flush()

        then: "expect store this events with EventDAO"
        1 * eventDAO.insert(_) >> { args ->
            Collection<Event> bulk = args[0];

            assert bulk.size() == events.size()
            assert bulk.contains(events.getAt(0))
            assert bulk.contains(events.getAt(1))

            bulk
        }
    }
}
