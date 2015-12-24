package org.ametiste.scm.log.persistent;

import org.ametiste.scm.messaging.data.event.Event;
import org.ametiste.scm.messaging.data.mongo.event.EventDocument;
import org.ametiste.scm.messaging.data.mongo.event.factory.EventToDocumentConverterMapFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.util.CloseableIterator;

/**
 * For testing unexpected cases in adapter construction.
 */
public class CloseableIteratorAdapterTester extends MongoEventDAO {

    public CloseableIteratorAdapterTester(MongoOperations mongoOperations, EventToDocumentConverterMapFactory eventToDocumentConverterMapFactory) {
        super(mongoOperations, eventToDocumentConverterMapFactory);
    }

    public CloseableIterator<Event> createIterator(CloseableIterator<EventDocument> iterator, Converter<EventDocument, Event> converter) {
        return new CloseableIteratorAdapter<>(iterator, converter);
    }
}
