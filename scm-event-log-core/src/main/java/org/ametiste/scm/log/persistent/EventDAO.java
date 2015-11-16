package org.ametiste.scm.log.persistent;

import org.ametiste.scm.messaging.data.event.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;
import org.springframework.data.util.CloseableIterator;

import java.util.Collection;
import java.util.UUID;

/**
 * Event DAO interface that provides all necessary operation with event persistent.
 * <p>
 * DAO includes both write and read operations. Insert operation mean insert new value without check for document already
 * exists. We consider all events that are insert in repository are unique (at least on {@code id} property with
 * {@code UUID} value).
 */
public interface EventDAO extends Repository<Event, UUID> {

    /**
     * Save the given entity. Assumes the instance to be new to be able to apply insertion optimizations. Use
     * the returned instance for further operations.
     *
     * @param entity must not be {@literal null}.
     * @return the saved entity
     */
    <S extends Event> S insert(S entity);

    /**
     * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
     * entity instance completely.
     *
     * @param entity must not be {@literal null}.
     * @return the saved entity
     */
    <S extends Event> S save(S entity);

    /**
     * Inserts the given entities. Assumes the given entities to have not been persisted yet and thus will optimize the
     * insert.
     *
     * @param entities must not be {@literal null}.
     * @return the saved entities collection
     */
    Collection<Event> insert(Collection<Event> entities);

    /**
     * Saves all given entities.
     *
     * @param entities must not be {@literal null}.
     * @return the saved entities collection
     */
    Collection<Event> save(Collection<Event> entities);


    /**
     * Retrieves an entity by its id.
     *
     * @param id must not be {@literal null}.
     * @return the entity with the given id or {@literal null} if none found
     * @throws IllegalArgumentException if {@code id} is {@literal null}
     */
    Event findOne(UUID id);

    /**
     * Retrieves all entities with iterator. Useful for processing large set of documents.
     *
     * @return a {@link CloseableIterator} that wraps cursor statement that needs to be closed.
     */
    CloseableIterator<Event> findAll();

    /**
     * Returns all instances of Event with timestamp in specified time interval.
     * Timestamp defined as number of milliseconds since January 1, 1970, 00:00:00 GMT.
     *
     * @param from start of time interval (inclusive).
     * @param to end of time interval (exclusive).
     *
     * @return a {@link CloseableIterator} that wraps cursor statement that needs to be closed.
     */
    CloseableIterator<Event> findAll(long from, long to);

    /**
     * Returns a {@link Page} of entities meeting the paging restriction provided in the {@code Pageable} object.
     *
     * @param pageable {@link Pageable} object that contains information about target slice of all documents to receive.
     * @return a page of entities
     */
    Page<Event> findAll(Pageable pageable);

    /**
     * Returns all instances of Event with timestamp in specified time interval.
     * Timestamp defined as number of milliseconds since January 1, 1970, 00:00:00 GMT.
     *
     * @param from start of time interval (inclusive).
     * @param to end of time interval (exclusive).
     */
    Page<Event> findAll(long from, long to, Pageable pageable);

    /**
     * Returns the number of entities available.
     *
     * @return the number of entities
     */
    long count();
}
