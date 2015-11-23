package org.ametiste.scm.log.persistent

import com.mongodb.BasicDBObject
import com.mongodb.DBCollection
import org.ametiste.scm.messaging.data.InstanceStartupEventGenerator
import org.ametiste.scm.messaging.data.event.Event
import org.ametiste.scm.messaging.data.event.InstanceStartupEvent
import org.ametiste.scm.messaging.data.mongo.event.EventDocument
import org.ametiste.scm.messaging.data.mongo.event.InstanceStartupEventDocument
import org.ametiste.scm.messaging.data.mongo.event.factory.DefaultEventToDocumentConverterMapFactory
import org.ametiste.scm.messaging.data.mongo.event.factory.EventToDocumentConverterMapFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.util.CloseableIterator
import spock.lang.Specification

import static org.ametiste.scm.messaging.data.EventComparator.equals

class MongoEventDAOTest extends Specification {

    private static final long COLLECTION_COUNT = 5L;
    private static final InstanceStartupEventGenerator EVENT_GENERATOR = new InstanceStartupEventGenerator();

    private MongoEventDAO eventDAO;
    private MongoOperations mongoOperations;
    private DBCollection dbCollection;
    private EventToDocumentConverterMapFactory factory = new DefaultEventToDocumentConverterMapFactory();

    /**
     * Init mocks and count() method behavior for target collection.
     */
    def setup() {
        dbCollection = Mock(DBCollection.class)
        mongoOperations = Mock(MongoOperations.class)
        eventDAO = new MongoEventDAO(mongoOperations, factory);

        dbCollection.count() >> COLLECTION_COUNT
        mongoOperations.getCollectionName(EventDocument.class) >> "evenDocument"
        mongoOperations.getCollection(_ as String) >> dbCollection
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

    def "findAll return correct iterator"() {
        when: "invoke findAll method"
        CloseableIterator<Event> iterator = eventDAO.findAll();

        then: "expect take valid iterator"
        iterator != null

        and: "with returned from mongoOperation stream for query with default sort by timestamp"
        1 * mongoOperations.stream(_ as Query, _ as Class) >> { Query query, Class documentClass ->

            assert documentClass == EventDocument
            assert query.getSortObject().get("timestamp") == 1

            return Mock(CloseableIterator.class)
        }
    }

    def "findAll with date range return correct iterator"() {
        given: "time range"
        long from = 100
        long to = 200

        when: "invoke findAll method"
        CloseableIterator<Event> iterator = eventDAO.findAll(from, to);

        then: "expect take valid iterator"
        iterator != null

        and: "with returned from mongoOperation stream for query with timestamp range"
        1 * mongoOperations.stream(_ as Query, _ as Class) >> { Query query, Class documentClass ->

            assert documentClass == EventDocument
            assert query.getSortObject().get("timestamp") == 1

            BasicDBObject dateQuery = (BasicDBObject)query.getQueryObject().get("timestamp")
            assert dateQuery.size() == 2
            assert dateQuery.get('$gte') == from
            assert dateQuery.get('$lte') == to

            return Mock(CloseableIterator.class)
        }
    }

    def "findAll method with pageable parameter"() {
        given: "pageable parameter"
        Pageable parameter = new PageRequest(5, 50)

        when: "invoke findAll method"
        Page<Event> page = eventDAO.findAll(parameter)

        then: "mongoOperations return empty collection"
        1 * mongoOperations.find(_ as Query, _ as Class) >> Collections.emptyList()

        and: "event dao return valid page without content"
        page != null
        !page.hasContent()
    }

    def "findAll with pageable parameter and time range"() {
        given: "time range and pageable parameter"
        long from = 100
        long to = 200
        Pageable parameter = new PageRequest(5, 50)

        when: "invoke findAll method"
        Page<Event> page = eventDAO.findAll(from, to, parameter)

        then: "mongoOperations return empty collection"
        1 * mongoOperations.find(_ as Query, _ as Class) >> Collections.emptyList()

        and: "event dao return valid page without content"
        page != null
        !page.hasContent()
    }

    def "count for target collection"() {
        when: "get count for target collection"
        def result = eventDAO.count()

        then: "take expected count"
        result == COLLECTION_COUNT
    }

    def "throw exception when try map unknown class"() {
        given: "some unknown single event instance and collection of them"
        Event event = new SomeEvent()
        Collection<Event> allEventsUnknown = Arrays.asList(event, event)
        Collection<Event> partOfEventsUnknown = Arrays.asList(event, EVENT_GENERATOR.generate(), null)

        when: "try insert unknown event"
        eventDAO.insert(event as SomeEvent)

        then: "expect IllegalArgumentException thrown"
        thrown(IllegalArgumentException.class)

        when: "try insert collection of unknown events"
        eventDAO.insert(allEventsUnknown)

        then: "expect IllegalArgumentException thrown"
        thrown(IllegalArgumentException.class)

        when: "try insert collection with only part of unknown events"
        eventDAO.insert(partOfEventsUnknown)

        then: "expect no exception thrown"
        noExceptionThrown()

        when: "try save collection with only part of unknown events"
        eventDAO.save(partOfEventsUnknown)

        then: "expect no exception thrown"
        noExceptionThrown()
    }

    class SomeEvent extends Event {}
}
