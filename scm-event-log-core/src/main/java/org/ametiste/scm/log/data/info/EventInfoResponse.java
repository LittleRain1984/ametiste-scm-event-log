package org.ametiste.scm.log.data.info;

import org.ametiste.scm.messaging.data.event.Event;

/**
 * Data model to represent information about event.
 * <p>
 * {@code Event} contains info about event type only in class name and when we send it to user it's lost.
 * {@code EventInfoResponse} store type of event as separate field.
 * <p>
 * Model representation of {@literal null} Event object is non-null {@code EventInfoResponse} with null fields.
 */
public class EventInfoResponse {

    private String type;
    private Event event;

    public EventInfoResponse(Event event) {
        this.event = event;
        if (event != null) {
            this.type = event.getClass().getSimpleName();
        }
    }

    public String getType() {
        return type;
    }

    public Event getEvent() {
        return event;
    }
}
