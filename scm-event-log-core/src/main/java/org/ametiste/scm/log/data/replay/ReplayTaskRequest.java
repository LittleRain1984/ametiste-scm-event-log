package org.ametiste.scm.log.data.replay;

import org.ametiste.scm.log.controller.EventReplayController;

import java.net.URI;

/**
 * Data model for replay task request parameters.
 * <p>
 * Contains target receiver url to event receiver endpoint and time interval.<p>
 * {@literal receiverUrl} is required and must be valid URI string representation.
 * Time interval parameters ({@literal startTime} and {@literal endTime}) are optional and by default set to -1
 * that corresponds to replay all events. In usual mode they contains timestamp value in seconds from Epoch start.
 *
 * @see EventReplayController#submitReplayTask(ReplayTaskRequest)
 */
import static org.apache.commons.lang3.Validate.isTrue;

public class ReplayTaskRequest {

    private URI receiverUrl;
    private int startTime = -1;
    private int endTime = -1;

    public URI getReceiverUrl() {
        return receiverUrl;
    }

    public void setReceiverUrl(String receiverUrl) {
        isTrue(receiverUrl != null, "'receiverUrl' must be initialized!");
        this.receiverUrl = URI.create(receiverUrl);
    }

    public int getStartTime() {
        return startTime;
    }

    /**
     * Set start time point.
     * @param startTime timestamp in seconds. Must be non negative number.
     */
    public void setStartTime(int startTime) {
        isTrue(startTime >= 0, "'startTime' must be a positive number!");
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    /**
     * Set end time point.
     * @param endTime timestamp in seconds. Must be positive number.
     */
    public void setEndTime(int endTime) {
        isTrue(endTime > 0, "'startTime' must be greater that zero!");
        this.endTime = endTime;
    }
}
