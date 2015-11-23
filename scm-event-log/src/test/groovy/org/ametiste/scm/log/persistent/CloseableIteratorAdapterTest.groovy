package org.ametiste.scm.log.persistent
import org.ametiste.scm.messaging.data.InstanceStartupEventGenerator
import org.ametiste.scm.messaging.data.event.Event
import org.ametiste.scm.messaging.data.mongo.event.InstanceStartupEventDocument
import org.ametiste.scm.messaging.data.mongo.event.factory.DefaultEventToDocumentConverterMapFactory
import org.springframework.core.convert.converter.Converter
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.util.CloseableIterator
import spock.lang.Specification

class CloseableIteratorAdapterTest extends Specification {
    CloseableIterator mockIterator;
    MongoOperations mongoOperations;
    MongoEventDAO dao;

    CloseableIteratorAdapterTester adapterTester;

    def setup() {
        mockIterator = Mock(CloseableIterator.class)
        mongoOperations = Mock(MongoOperations.class)
        mongoOperations.stream(_, _) >> mockIterator

        dao = new MongoEventDAO(mongoOperations, new DefaultEventToDocumentConverterMapFactory())
        adapterTester = new CloseableIteratorAdapterTester(mongoOperations, new DefaultEventToDocumentConverterMapFactory())
    }

    def "constructor arguments validation"() {
        when: "try create adapter with not initialized iterator"
        adapterTester.createIterator(null, Mock(Converter));

        then: "expect IllegalArgumentException thrown"
        thrown(IllegalArgumentException.class)

        when: "try create adapter with not initialized converter"
        adapterTester.createIterator(mockIterator, null);

        then: "expect IllegalArgumentException thrown"
        thrown(IllegalArgumentException.class)

        when: "try create adapter with initialized iterator"
        adapterTester.createIterator(mockIterator, Mock(Converter));

        then: "expect no exception thrown"
        noExceptionThrown()
    }

    def "proxy methods logic check"() {

        given: "iterator adapter with mocked iterator"
        CloseableIterator<Event> iterator = dao.findAll()

        when: "close method invoke"
        iterator.close()

        then: "iterator close method invokes too"
        1 * mockIterator.close()

        when: "hasNext method invoke"
        iterator.hasNext()

        then: "iterator hasNext method invokes too"
        1 * mockIterator.hasNext() >> true

        when: "next method invoke"
        iterator.next()

        then: "iterator next method invokes too"
        1 * mockIterator.next() >> new InstanceStartupEventDocument(new InstanceStartupEventGenerator().generate())
    }
}
