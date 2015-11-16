package org.ametiste.scm.messaging.sender;

import org.ametiste.scm.messaging.data.event.Event;
import org.ametiste.scm.messaging.data.transport.TransportMessage;
import org.ametiste.scm.messaging.sender.EventSendException;
import org.ametiste.scm.messaging.sender.EventSender;

import java.net.URI;
import java.util.*;

/**
 * Mock of {@code EventSender} interface for testing Event Broker processing.
 * Class collect map of all send messages and subscribers.
 */
public class EventSenderMock implements EventSender {

    private final Map<URI, List<TransportMessage<Event>>> sendingMap = new LinkedHashMap<>();

    @Override
    public void send(URI receiver, TransportMessage<Event> message) throws EventSendException {
        if (sendingMap.containsKey(receiver)) {
            sendingMap.get(receiver).add(message);
        } else {
            List<TransportMessage<Event>> list = new ArrayList<>();
            list.add(message);
            sendingMap.put(receiver, list);
        }
    }

    @Override
    public void send(URI receiver, Collection<TransportMessage<Event>> messages) throws EventSendException {
        if (sendingMap.containsKey(receiver)) {
            sendingMap.get(receiver).addAll(messages);
        } else {
            List<TransportMessage<Event>> list = new ArrayList<>();
            list.addAll(messages);
            sendingMap.put(receiver, list);
        }
    }

    /**
     * Clean collected map.
     */
    public void clean() {
        sendingMap.clear();
    }

    /**
     * Return unmodified view of current state of collected map.
     */
    public Map<URI, List<TransportMessage<Event>>> getSendingMap() {
        return Collections.unmodifiableMap(sendingMap);
    }

    /**
     * Return total number of send events for all subscribers.
     */
    public long totalSendEvents() {
        return sendingMap.entrySet().stream().map(Map.Entry::getValue).flatMap(List::stream).count();
    }
}