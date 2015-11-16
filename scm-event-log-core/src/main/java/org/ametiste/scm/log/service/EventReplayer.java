package org.ametiste.scm.log.service;

import org.ametiste.scm.log.data.replay.ReplayTaskStatus;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

/**
 * Interface of event replay processor.
 * <p>
 * Any implementation of {@code EventReplayer} should manage replay task execution and provide way to get their status.
 */
public interface EventReplayer {

    /**
     * Submit replay all events task.
     * @param receiver event receiver endpoint url of target receiver.
     * @return identifier of created task. Can be used for track task status.
     * @throws ReplayOperationException if any error occurred during task creation.
     */
    UUID replay(URI receiver) throws ReplayOperationException;

    /**
     * Submit replay time period task.
     * @param receiver event receiver endpoint url of target receiver.
     * @param startTime lower time period bound (timestamp in seconds). If value is negative number lower time period
     *                  bound set to zero.
     * @param endTime lower time period bound (timestamp in seconds). If value is negative number upper time period
     *                bound set to current time.
     * @return identifier of created task. Can be used for track task status.
     * @throws ReplayOperationException if any error occurred during task creation.
     */
    UUID replay(URI receiver, long startTime, long endTime) throws ReplayOperationException;

    /**
     * Cancel or interrupt task execution.
     * @param taskId task identifier.
     * @throws ReplayTaskNotFoundException when task with specified id not exists.
     */
    void stop(UUID taskId) throws ReplayTaskNotFoundException;

    /**
     * Retrieve status of task with specified id.
     * @param taskId task identifier.
     * @throws ReplayTaskNotFoundException when task with specified id not exists.
     */
    ReplayTaskStatus status(UUID taskId) throws ReplayTaskNotFoundException;

    /**
     * Retrieve status for all registered tasks.
     */
    Map<UUID, ReplayTaskStatus> status();

    /**
     * Retrieve identifier of current processed task.
     * @return {@literal null} if there is no active tasks.
     */
    UUID getActiveProcessId();

    /**
     * Retrieve flag that indicate present active replay task or not.
     * @return {@literal true} when task present and {@literal false} in other case.
     */
    boolean hasActiveReplay();

    /**
     * Remove all canceled and finished tasks.
     */
    void clean();

    /**
     * Remove task with specified id.
     * @param taskId task identifier.
     * @throws ReplayOperationException when task with specified id not exists or task not finished.
     * @throws ReplayTaskNotFoundException when task with specified id not exists.
     */
    void clean(UUID taskId) throws ReplayOperationException, ReplayTaskNotFoundException;
}
