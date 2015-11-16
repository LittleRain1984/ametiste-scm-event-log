package org.ametiste.scm.log.service;

import org.ametiste.scm.log.data.replay.ReplayTaskStatus;
import org.ametiste.scm.log.persistent.EventDAO;
import org.ametiste.scm.log.util.EventReceiverURLBuilder;
import org.ametiste.scm.messaging.data.event.Event;
import org.ametiste.scm.messaging.data.transport.EventTransportMessage;
import org.ametiste.scm.messaging.data.transport.TransportMessage;
import org.ametiste.scm.messaging.sender.EventSendException;
import org.ametiste.scm.messaging.sender.EventSender;
import org.springframework.data.util.CloseableIterator;

import java.net.URI;
import java.util.*;
import java.util.concurrent.*;

import static org.apache.commons.lang3.Validate.isTrue;

/**
 * Default {@code EventReplayer} interface implementation.
 * <p>
 * Replayer contains single thread task executor to process replay tasks. At the same time executes only one replay
 * task all other store in unbounded queue.
 * <p>
 * Replayer include in {@code TransportMessage} excludes list URI of service event receiver endpoint. This makes it
 * possible to exclude Event Log from broadcasting in case when replay produced through Event Broker.
 * <p>
 * URI takes from {@code EventReceiverURLBuilder} at start of each task execution.
 */
public class EventReplayerImpl implements EventReplayer {

    private final EventDAO eventDAO;
    private final EventSender eventSender;
    private final int bulkSize;
    private final EventReceiverURLBuilder excludeUrlBuilder;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Map<UUID, Future> tasks = new ConcurrentHashMap<>();
    private final Map<UUID, ReplayTaskStatus> statuses = new ConcurrentHashMap<>();

    /**
     * Create new instance of {@code EventReplayerImpl}.
     * @param eventDAO DAO to access to event storage.
     * @param eventSender {@code EventSender} instance.
     * @param bulkSize number of messages to send at once. Must be positive number.
     * @param excludeUrlBuilder {@code EventReceiverURLBuilder} instance that produce service URI.
     */
    public EventReplayerImpl(EventDAO eventDAO,
                             EventSender eventSender,
                             int bulkSize,
                             EventReceiverURLBuilder excludeUrlBuilder) {
        isTrue(eventDAO != null, "'eventDAO' must be initialized!");
        isTrue(eventSender != null, "'eventSender' must be initialized!");
        isTrue(bulkSize > 0, "'bulkSize' must be grater than zero!");
        isTrue(excludeUrlBuilder != null, "'excludeBuilders' must be initialized!");

        this.eventDAO = eventDAO;
        this.eventSender = eventSender;
        this.bulkSize = bulkSize;
        this.excludeUrlBuilder = excludeUrlBuilder;
    }

    @Override
    public UUID replay(URI receiver) throws ReplayOperationException {
        return replay(receiver, -1, -1);
    }

    @Override
    public UUID replay(URI receiver, long startTime, long endTime) throws ReplayOperationException {
        isTrue(receiver != null, "Receiver url must be not null!");

        UUID id = UUID.randomUUID();
        Future future;

        try {
            future = executor.submit(() -> this.replayTask(id, receiver, startTime, endTime));
        } catch (RejectedExecutionException e) {
            throw new ReplayOperationException(e.getMessage(), e);
        }

        tasks.put(id, future);
        statuses.put(id, new ReplayTaskStatus(ReplayTaskStatus.State.WAIT, startTime, endTime, 0, 0));

        return id;
    }

    @Override
    public void stop(UUID taskId) throws ReplayTaskNotFoundException {
        if (!tasks.containsKey(taskId)) {
            tasks.get(taskId).cancel(true);
        } else {
            throw new ReplayTaskNotFoundException("Task with taskId " + taskId + " not exists!");
        }
    }

    @Override
    public ReplayTaskStatus status(UUID taskId) throws ReplayTaskNotFoundException {
        if (statuses.containsKey(taskId)) {
            return statuses.get(taskId);
        } else {
            throw new ReplayTaskNotFoundException("Task with taskId " + taskId + " not exists!");
        }
    }

    @Override
    public Map<UUID, ReplayTaskStatus> status() {
        return Collections.unmodifiableMap(statuses);
    }

    @Override
    public UUID getActiveProcessId() {
        return statuses.entrySet().stream()
                .filter(entry -> entry.getValue().getState() == ReplayTaskStatus.State.RUNNING)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean hasActiveReplay() {
        return statuses.entrySet().stream().anyMatch(entry -> entry.getValue().getState() == ReplayTaskStatus.State.RUNNING);
    }

    @Override
    public void clean() {
        tasks.entrySet().stream()
                .filter(entry -> entry.getValue().isDone())
                .forEach(element -> {
                    tasks.remove(element.getKey());
                    statuses.remove(element.getKey());
                });
    }

    @Override
    public void clean(UUID taskId) throws ReplayOperationException, ReplayTaskNotFoundException {
        if (!tasks.containsKey(taskId)) {
            throw new ReplayTaskNotFoundException("Task with taskId " + taskId + " not exists!");
        } else if (!tasks.get(taskId).isDone()) {
            throw new ReplayOperationException("Task with taskId " + taskId + " not complete!");
        }

        tasks.remove(taskId);
        statuses.remove(taskId);
    }

    private void replayTask(UUID id, URI receiver, long startTime, long endTime) {
        long start = startTime > 0 ? startTime * 1000 : 0;
        long end = endTime > 0 ? endTime * 1000 : System.currentTimeMillis();
        long totalEventCount = eventDAO.findAll(start, end, null).getTotalElements();
        long replayedEventCount = 0;

        URI exclude = excludeUrlBuilder.build();

        updateStatus(id, ReplayTaskStatus.State.RUNNING, start, end, totalEventCount, replayedEventCount);

        try (CloseableIterator<Event> iterator = eventDAO.findAll(start, end)) {
            while (iterator.hasNext()) {
                List<TransportMessage<Event>> bulk = createBulk(iterator, exclude);

                try {
                    eventSender.send(receiver, bulk);
                } catch (EventSendException e) {
                    updateStatusWithError(id, start, end, totalEventCount, replayedEventCount, e);
                    throw new ReplayOperationException(e.getMessage(), e);
                }

                replayedEventCount += bulk.size();
                updateStatus(id, ReplayTaskStatus.State.RUNNING, start, end, totalEventCount, replayedEventCount);
            }
        }

        updateStatus(id, ReplayTaskStatus.State.COMPLETE, start, end, totalEventCount, replayedEventCount);
    }

    private List<TransportMessage<Event>> createBulk(Iterator<Event> iterator, URI exclude) {
        List<TransportMessage<Event>> bulk = new LinkedList<>();
        while(iterator.hasNext() && bulk.size() < bulkSize) {
            bulk.add(new EventTransportMessage(iterator.next(), Collections.singletonList(exclude)));
        }
        return bulk;
    }

    private void updateStatus(UUID id,
                              ReplayTaskStatus.State state,
                              long startTime,
                              long endTime,
                              long totalEventCount,
                              long replayedEventCount) {
        statuses.put(id, new ReplayTaskStatus(state, startTime, endTime, totalEventCount, replayedEventCount));
    }

    private void updateStatusWithError(UUID id,
                              long startTime,
                              long endTime,
                              long totalEventCount,
                              long replayedEventCount,
                              Throwable cause) {
        statuses.put(id, new ReplayTaskStatus(ReplayTaskStatus.State.ERROR, startTime, endTime, totalEventCount, replayedEventCount,
                cause.getMessage(), cause.getStackTrace()));
    }
}
