package org.ametiste.scm.log.persistent

import com.mongodb.DBCollection
import org.ametiste.scm.messaging.data.InstanceStartupEventGenerator
import org.ametiste.scm.messaging.data.event.Event
import org.ametiste.scm.messaging.data.event.InstanceStartupEvent
import org.ametiste.scm.messaging.data.mongo.event.EventDocument
import org.ametiste.scm.messaging.data.mongo.event.InstanceStartupEventDocument
import org.ametiste.scm.messaging.data.mongo.event.factory.DefaultEventToDocumentConverterMapFactory
import org.ametiste.scm.messaging.data.mongo.event.factory.EventToDocumentConverterMapFactory
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Query
import spock.lang.Specification

import static org.ametiste.scm.messaging.data.EventComparator.equals

class MongoEventDAOTest extends Specification {

    private static final InstanceStartupEventGenerator EVENT_GENERATOR = new InstanceStartupEventGenerator();

    private MongoEventDAO eventDAO;
    private MongoOperations mongoOperations;
    private DBCollection dbCollection;
    private EventToDocumentConverterMapFactory factory = new DefaultEventToDocumentConverterMapFactory();

    def setup() {
        dbCollection = Mock(DBCollection.class)
        mongoOperations = Mock(MongoOperations.class)
        eventDAO = new MongoEventDAO(mongoOperations, factory);
    }

    def "constructor arguments validation"() {
        when: "create DAO with not initialized mongo operations"
        new MongoEventDAO(null, factory)

        then: "expect IllegalArgumentException thrown"
        thrown(IllegalArgumentException.class)

        when: "create DAO with not initialized converter map factory"
        new MongoEventDAO(mongoOperations, null)

        then: "expect IllegalArgumentException thrown"
        thrown(IllegalArgumentException.class)

        when: "create DAO with valid arguments"
        new MongoEventDAO(mongoOperations, factory)

        then: "expect no exceptions thrown"
        noExceptionThrown()
    }

    def "insert single event"() {
        given: "some event"
        Event event = EVENT_GENERATOR.generate();

        when: "try insert not initialized event instance"
        eventDAO.insert((Event)null)

        then: "expect IllegalArgumentException thrown"
        thrown(IllegalArgumentException.class)

        when: "insert given event"
        Event returnedEvent = eventDAO.insert(event)

        then: "expect correct call to mongo operations"
        1 * mongoOperations.insert(_ as EventDocument)

        and: "return correct event"
        equals(returnedEvent, event);
    }

    def "save single event"() {
        given: "some event"
        Event event = EVENT_GENERATOR.generate();

        when: "try save not initialized event instance"
        eventDAO.save((Event)null)

        then: "expect IllegalArgumentException thrown"
        thrown(IllegalArgumentException.class)

        when: "save given event"
        Event returnedEvent = eventDAO.save(event)

        then: "expect correct call to mongo operations"
        1 * mongoOperations.save(_ as EventDocument)

        and: "return correct event"
        equals(returnedEvent, event);
    }

    def "insert collection of events"() {
        given: "some event collection"
        Collection<Event> events = Collections.singletonList(EVENT_GENERATOR.generate());

        when: "try insert not initialized collection of events"
        eventDAO.insert((Collection)null)

        then: "expect IllegalArgumentException thrown"
        thrown(IllegalArgumentException.class)

        when: "insert given collection"
        Collection<Event> returnedEvents = eventDAO.insert(events)

        then: "expect correct call to mongo operations"
        1 * mongoOperations.insert(_ as Collection<EventDocument>, EventDocument.class)

        and: "return correct collection"
        returnedEvents.size() == events.size()
        equals(returnedEvents.getAt(0), events.get(0));
    }

    def "save collection of events"() {
        given: "some event collection"
        Collection<Event> events = Arrays.asList(EVENT_GENERATOR.generate(), EVENT_GENERATOR.generate());

        when: "try save not initialized collection of events"
        eventDAO.save((Collection)null)

        then: "expect IllegalArgumentException thrown"
        thrown(IllegalArgumentException.class)

        when: "save given collection"
        Collection<Event> returnedEvents = eventDAO.save(events)

        then: "expect correct calls to mongo operations"
        events.size() * mongoOperations.save(_ as EventDocument)

        and: "return correct collection"
        returnedEvents.size() == events.size()
        equals(returnedEvents.getAt(0), events.get(0))
        equals(returnedEvents.getAt(1), events.get(1))
    }

    def "findOne argument validation"() {
        given: "some event"
        InstanceStartupEvent event = EVENT_GENERATOR.generate()

        when: "call method with not initialized argument"
        eventDAO.findOne(null)

        then: "expect IllegalArgumentException thrown"
        thrown(IllegalArgumentException.class)
    }

    def "findOne return null if event not found"() {
        when: "try find absent event with specified id"
        Event event = eventDAO.findOne(UUID.randomUUID())

        then: "mongo operations return null"
        mongoOperations.findById(_ as Query, EventDocument.class) >> null

        and: "method return it too"
        event == null
    }

    def "findOne return correct event by id"() {
        given: "some event"
        InstanceStartupEvent event = EVENT_GENERATOR.generate()

        when: "request find operation"
        Event returnedEvent = eventDAO.findOne(event.getId())

        then: "mongo return correct event"
        mongoOperations.findById(_ as UUID, EventDocument.class) >> { new InstanceStartupEventDocument(event) }

        and: "event is same"
        equals(returnedEvent, event)
    }

    def "test count()"() {
        def count = 5L

        when:
        def result = eventDAO.count()

        then:
        dbCollection.count() >> count
        mongoOperations.getCollectionName(EventDocument.class) >> "evenDocument"
        mongoOperations.getCollection(_ as String) >> dbCollection

        and:
        result == count
    }
}
