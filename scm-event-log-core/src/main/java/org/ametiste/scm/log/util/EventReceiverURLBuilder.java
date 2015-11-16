package org.ametiste.scm.log.util;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Utility class that provides event receiver endpoint url building.
 * <p>
 * It is necessary because process of construction separated in time and place (port can be resolved only after embedded
 * servlet container initialization).
 */
public class EventReceiverURLBuilder {

    public static final String EVENT_RECEIVER_PATH = "/event-receiver";

    private String host = "localhost";
    private int port = -1;
    private String contextPath = "";

    public String getHost() {
        return host;
    }

    public EventReceiverURLBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public EventReceiverURLBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public String getContextPath() {
        return contextPath;
    }

    public EventReceiverURLBuilder setContextPath(String contextPath) {
        this.contextPath = contextPath;
        return this;
    }

    /**
     * Build target url.
     * @return {@code URI}, if url cannot be constructed return {@literal null} without any exception thrown.
     */
    public URI build() {
        try {
            return new URI(String.format("http://%s:%d%s%s", host, port, contextPath, EVENT_RECEIVER_PATH));
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
