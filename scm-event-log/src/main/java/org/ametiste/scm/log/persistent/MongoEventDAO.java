package org.ametiste.scm.log.persistent;

import org.ametiste.scm.messaging.data.event.Event;
import org.ametiste.scm.messaging.data.mongo.event.EventDocument;
import org.ametiste.scm.messaging.data.mongo.event.factory.EventToDocumentConverterMapFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.util.CloseableIterator;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * Implementation of {@code EventDAO} that provide access to Mongo DB Event documents repository.
 * <p>
 * {@code MongoEventDAO} store in Mongo event document representations from {@link org.ametiste.scm.messaging.data.mongo.event}
 * package. It is caused by that the {@code Event} subtype are not adapted to Spring Data Mongo model. For example, not
 * allowed default constructor, missed document annotations and so on.
 * {@code MongoEventDAO} provides conversion in both directions transparent to user.
 */
public class MongoEventDAO implements EventDAO {

    private static final Sort DEFAULT_SORT = new Sort(Sort.Direction.ASC, "timestamp");

    private final MongoOperations mongoOperations;
    private final Map<Class, Function<Event, EventDocument>> converterMap;

    /**
     * Create instance of {@code MongoEventDAO}.
     * @param mongoOperations {@code MongoOperations} object for communication with Mongo instance.
     * @param eventToDocumentConverterMapFactory factory that produce conversion map to convert Event to document DTO.
     */
    public MongoEventDAO(MongoOperations mongoOperations,
                         EventToDocumentConverterMapFactory eventToDocumentConverterMapFactory) {
        isTrue(mongoOperations != null, "'mongoOperations' must be initialized!");
        isTrue(eventToDocumentConverterMapFactory != null, "ConverterMapFactory must be initialized!");

        this.mongoOperations = mongoOperations;
        this.converterMap = eventToDocumentConverterMapFactory.getMap();
    }

    @Override
    public <S extends Event> S insert(S entity) {
        isTrue(entity != null, "Entity must not be null!");
        mongoOperations.insert(convert(entity));
        return entity;
    }

    @Override
    public <S extends Event> S save(S entity) {
        isTrue(entity != null, "Entity must not be null!");
        mongoOperations.save(convert(entity));
        return entity;
    }

    @Override
    public Collection<Event> insert(Collection<Event> entities) {
        isTrue(entities != null, "The given collection of entities must not be null!");
        mongoOperations.insert(convert(entities), EventDocument.class);
        return entities;
    }

    @Override
    public Collection<Event> save(Collection<Event> entities) {
        isTrue(entities != null, "The given collection of entities must not be null!");
        convert(entities).stream().forEach(mongoOperations::save);
        return entities;
    }

    @Override
    public Event findOne(UUID id) {
        isTrue(id != null, "The given uuid must not be null!");
        return Optional.ofNullable(mongoOperations.findById(id, EventDocument.class))
                .filter(Objects::nonNull)
                .map(EventDocument::convert)
                .orElse(null);
    }

    @Override
    public CloseableIterator<Event> findAll() {
        return new CloseableIteratorAdapter<>(
                mongoOperations.stream(new Query().with(DEFAULT_SORT), EventDocument.class),
                EventDocument::convert
        );
    }

    @Override
    public CloseableIterator<Event> findAll(long from, long to) {
        return new CloseableIteratorAdapter<>(
                mongoOperations.stream(new Query(where("timestamp").gte(from).lte(to)).with(DEFAULT_SORT), EventDocument.class),
                EventDocument::convert
        );
    }

    @Override
    public Page<Event> findAll(Pageable pageable) {
        long count = count();
        List<Event> result = mongoOperations.find(new Query().with(pageable), EventDocument.class)
                .stream().map(EventDocument::convert).collect(Collectors.toList());
        return new PageImpl<>(result, pageable, count);
    }

    @Override
    public Page<Event> findAll(long from, long to, Pageable pageable) {
        long queryCount = mongoOperations.count(new Query(where("timestamp").gte(from).lte(to)), EventDocument.class);
        List<Event> result;

        if (pageable == null) {
            result = Collections.emptyList();
        } else {
            result = mongoOperations.find(new Query().with(pageable), EventDocument.class)
                    .stream().map(EventDocument::convert).collect(Collectors.toList());
        }

        return new PageImpl<>(result, pageable, queryCount);
    }

    @Override
    public long count() {
        return mongoOperations.getCollection(mongoOperations.getCollectionName(EventDocument.class)).count();
    }

    private EventDocument convert(Event event) {
        if (event == null) {
            return null;
        } else if (converterMap.containsKey(event.getClass())) {
            return converterMap.get(event.getClass()).apply(event);
        } else {
            throw new IllegalArgumentException("repository can't map event type: " + event.getClass());
        }
    }

    private Collection<EventDocument> convert(Collection<Event> events) {
        List<EventDocument> docs = events.stream()
                .filter(Objects::nonNull)
                .filter(event -> converterMap.containsKey(event.getClass()))
                .map(e -> converterMap.get(e.getClass()).apply(e))
                .collect(Collectors.toList());

        if (docs.isEmpty()) {
            throw new IllegalArgumentException("repository can't map any of taken event objects");
        }
        return docs;
    }

    /**
     * Implementation of {@code CloseableIterator} interface that adapts one type to another.
     * <p>
     * {@code CloseableIteratorAdapter} convert objects with specified {@link Converter} instance.
     *
     * @param <P> source type
     * @param <T> target type
     */
    class CloseableIteratorAdapter<P, T> implements CloseableIterator<T> {

        private CloseableIterator<P> iterator;
        private Converter<P, T> converter;

        /**
         * Create {@code CloseableIteratorAdapter} instance.
         * @param iterator {@code CloseableIterator} instance that should be adapted.
         * @param converter converter from source {@literal P} to target {@literal T} type.
         */
        public CloseableIteratorAdapter(CloseableIterator<P> iterator, Converter<P, T> converter) {
            isTrue(iterator != null, "'iterator' must be initialized!");
            isTrue(converter != null, "'converter' must be initialized!");

            this.iterator = iterator;
            this.converter = converter;
        }

        @Override
        public void close() {
            iterator.close();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public T next() {
            return converter.convert(iterator.next());
        }
    }
}
