package org.ametiste.scm.log.data.replay;

import java.util.UUID;

/**
 * Data model represents response to get active replay request.
 */
public class ActiveTaskResponse {

    private boolean hasActive;
    private UUID taskId;

    public ActiveTaskResponse(UUID taskId) {
        this.taskId = taskId;
        this.hasActive = taskId != null;
    }

    public boolean isHasActive() {
        return hasActive;
    }

    public UUID getTaskId() {
        return taskId;
    }
}
