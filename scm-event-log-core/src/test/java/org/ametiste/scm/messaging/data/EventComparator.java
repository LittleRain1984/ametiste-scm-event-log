package org.ametiste.scm.messaging.data;

import org.ametiste.scm.messaging.data.event.InstanceStartupEvent;

/**
 * Utility class that contains comparator for event objects.
 */
public class EventComparator {

    private EventComparator() {}

    public static boolean equals(InstanceStartupEvent tested, InstanceStartupEvent original) {
        return tested.getId().equals(original.getId()) &&
                tested.getTimestamp() == original.getTimestamp() &&
                tested.getInstanceId().equals(original.getInstanceId()) &&
                tested.getVersion().equals(original.getVersion()) &&
                tested.getProperties().size() == original.getProperties().size() &&
                tested.getNodeId().equals(original.getNodeId()) &&
                tested.getUri().equals(original.getUri());
    }
}
