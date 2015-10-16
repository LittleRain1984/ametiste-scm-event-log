package org.ametiste.scm.log.controller;

import org.ametiste.scm.log.data.replay.ActiveTaskResponse;
import org.ametiste.scm.log.data.replay.ReplayTaskRequest;
import org.ametiste.scm.log.data.replay.ReplayTaskStatus;
import org.ametiste.scm.log.service.EventReplayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.Validate.isTrue;

/**
 * Controller provide mapping for replay requests:
 * <ul>
 *     <li><b>common operations:</b>
 *     <ul>
 *         <li>submit replay task;</li>
 *         <li>get active task;</li>
 *         <li>get status for all tasks;</li>
 *         <li>clean info about finished tasks.</li>
 *     </ul></li>
 *     <li><b>task operations:</b>
 *     <ul>
 *         <li>stop replay task by id;</li>
 *         <li>get task status;</li>
 *         <li>clean info about task if it's completed.</li>
 *     </ul></li>
 * </ul>
 */
@RestController
public class EventReplayController {

    @Autowired
    private EventReplayer replayer;

    @RequestMapping(value = "/replay", method = RequestMethod.POST, consumes = "application/json")
    public UUID submitReplayTask(@RequestBody ReplayTaskRequest request) {
        isTrue(request.getReceiverUrl() != null, "'Request must contains correct receiver URI.");

        return replayer.replay(request.getReceiverUrl(), request.getStartTime(), request.getEndTime());
    }

    @RequestMapping(value = "/replay/active", method = RequestMethod.GET)
    public ActiveTaskResponse getActiveTask() {
        return new ActiveTaskResponse(replayer.getActiveProcessId());
    }

    @RequestMapping(value = "/replay/status", method = RequestMethod.GET)
    public Map<UUID, ReplayTaskStatus> getAllTasksStatus() {
        return replayer.status();
    }

    @RequestMapping(value = "/replay/clean", method = RequestMethod.POST)
    public void cleanAllInactiveTasksStatus() {
        replayer.clean();
    }

    @RequestMapping(value = "/replay/{id}/stop", method = RequestMethod.POST)
    public void stopTask(@PathVariable String id) {
        replayer.stop(UUID.fromString(id));
    }

    @RequestMapping(value = "/replay/{id}/status", method = RequestMethod.GET)
    public ReplayTaskStatus getTaskStatus(@PathVariable String id) {
        return replayer.status(UUID.fromString(id));
    }

    @RequestMapping(value = "/replay/{id}/clean", method = RequestMethod.GET)
    public void cleanTaskStatus(@PathVariable String id) {
        replayer.clean(UUID.fromString(id));
    }
}
