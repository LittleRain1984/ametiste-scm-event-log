package org.ametiste.scm.log.persistent

import org.ametiste.scm.messaging.data.InstanceLifecycleEventGenerator
import com.mongodb.DBCollection
import org.ametiste.scm.messaging.data.event.Event
import org.ametiste.scm.messaging.data.event.InstanceLifecycleEvent
import org.ametiste.scm.messaging.data.mongo.event.EventDocument
import org.ametiste.scm.messaging.data.mongo.event.InstanceLifecycleEventDocument
import org.ametiste.scm.messaging.data.mongo.event.factory.DefaultEventToDocumentConverterMapFactory
import org.ametiste.scm.messaging.data.mongo.event.factory.EventToDocumentConverterMapFactory
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Query
import spock.lang.Specification

import static org.ametiste.scm.messaging.data.EventComparator.equals

class MongoEventDAOTest extends Specification {

    private static final InstanceLifecycleEventGenerator EVENT_GENERATOR = new InstanceLifecycleEventGenerator();

    private MongoEventDAO eventDAO;
    private MongoOperations mongoOperations;
    private DBCollection dbCollection;
    private EventToDocumentConverterMapFactory factory = new DefaultEventToDocumentConverterMapFactory();

    def setup() {
        dbCollection = Mock(DBCollection.class)
        mongoOperations = Mock(MongoOperations.class)
        eventDAO = new MongoEventDAO(mongoOperations, factory);
    }

    def "should correct insert single event"() {
        given:
        Event event = EVENT_GENERATOR.generate();

        when:
        Event returnedEvent = eventDAO.insert(event)

        then:
        1 * mongoOperations.insert(_ as EventDocument)

        and:
        equals(returnedEvent, event);
    }

    def "should correct insert collection of events"() {
        given:
        Event event = EVENT_GENERATOR.generate();

        when:
        Collection<Event> returnedEvents = eventDAO.insert(Collections.singletonList(event))

        then:
        1 * mongoOperations.insert(_ as Collection<EventDocument>, EventDocument.class)

        and:
        returnedEvents.size() == 1
        equals(returnedEvents.getAt(0), event);
    }

    def "should return null if event not found"() {
        when:
        Event event = eventDAO.findOne(UUID.randomUUID())

        then:
        mongoOperations.findById(_ as Query, EventDocument.class) >> null

        and:
        event == null
    }

    def "should find correct event by id"() {
        given:
        InstanceLifecycleEvent event = EVENT_GENERATOR.generate()

        when:
        Event returnedEvent = eventDAO.findOne(event.getId())

        then:
        mongoOperations.findById(_ as UUID, EventDocument.class) >> { new InstanceLifecycleEventDocument(event) }

        and:
        equals(returnedEvent, event)
    }

    def "test count()"() {
        def count = 5L

        when:
        def result = eventDAO.count()

        then:
        dbCollection.count() >> count
        mongoOperations.getCollectionName(EventDocument.class) >> "eventDocument"
        mongoOperations.getCollection(_ as String) >> dbCollection

        and:
        result == count
    }
}
