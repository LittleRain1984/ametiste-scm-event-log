package org.ametiste.scm.log.data.replay;

import static org.apache.commons.lang3.Validate.isTrue;

/**
 * Model contains information about replay task processing: actual state, time range, event counts and
 * error information in case when task finish unsuccessful.
 */
public class ReplayTaskStatus {

    private final State state;
    private final long startTime;
    private final long endTime;
    private final long totalEvents;
    private final long replayedEvents;
    private final String errorMessage;
    private final StackTraceElement[] stacktrace;

    /**
     * Construct task execution status without error information.
     *
     * @see #ReplayTaskStatus(State, long, long, long, long, String, StackTraceElement[])
     */
    public ReplayTaskStatus(State state, long startTime, long endTime, long totalEvents, long replayedEvents) {
        this(state, startTime, endTime, totalEvents, replayedEvents, null, null);
    }

    /**
     * Construct task execution status.
     * @param state actual task state.
     * @param startTime start time point for replay (in milliseconds).
     * @param endTime end time point for replay (in milliseconds).
     * @param totalEvents total number of events to replay.
     * @param replayedEvents actual replayed number of events.
     * @param errorMessage error message. Might be null.
     * @param stacktrace stacktrace info. Might be null.
     */
    public ReplayTaskStatus(State state, long startTime, long endTime, long totalEvents, long replayedEvents,
                            String errorMessage, StackTraceElement[] stacktrace) {
        isTrue(state != null, "'state' must be initialized!");

        this.state = state;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalEvents = totalEvents;
        this.replayedEvents = replayedEvents;
        this.errorMessage = errorMessage;
        this.stacktrace = stacktrace;
    }

    public State getState() {
        return state;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public long getTotalEvents() {
        return totalEvents;
    }

    public long getReplayedEvents() {
        return replayedEvents;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public StackTraceElement[] getStacktrace() {
        return stacktrace;
    }

    /**
     * Enumeration with possible task states:
     * <ul>
     *     <li><b>WAIT</b> - task wait for execution;</li>
     *     <li><b>RUNNING</b> - task on execution.</li>
     *     <li><b>COMPLETE</b> - task finished success.</li>
     *     <li><b>ERROR</b> - task finished with error.</li>
     * </ul>
     */
    public enum State {
        WAIT, RUNNING, COMPLETE, ERROR
    }
}
