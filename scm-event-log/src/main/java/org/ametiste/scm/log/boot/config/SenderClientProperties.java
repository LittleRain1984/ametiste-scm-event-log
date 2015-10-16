package org.ametiste.scm.log.boot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Defines set of properties that would be used to configure {@code HttpClient} instance for {@code EventSender}.
 * <p>
 * Defined properties are included (org.ametiste.scm.log.sender.client.*):
 * <ul>
 * <li><b>connect-timeout</b> - connection timeout for send event request (in milliseconds);</li>
 * <li><b>read-timeout</b> - read timeout for send event request (in milliseconds).</li>
 * </ul>
 * <p>
 * This properties is designed provide usable default configuration that provides correct timeout values.
 */
@ConfigurationProperties("org.ametiste.scm.log.sender.client")
public class SenderClientProperties {

    /**
     * Connection timeout for send event request in milliseconds. Default is 1000.
     */
    private int connectTimeout = 1000;

    /**
     * Read timeout for send event request in milliseconds. Default is 1000.
     */
    private int readTimeout = 1000;

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
}