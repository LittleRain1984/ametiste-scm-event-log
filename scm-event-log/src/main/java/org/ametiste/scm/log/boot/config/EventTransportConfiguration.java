package org.ametiste.scm.log.boot.config;

import org.ametiste.scm.log.util.EventReceiverURLBuilder;
import org.ametiste.scm.messaging.sender.EventSender;
import org.ametiste.scm.messaging.sender.HttpEventSender;
import org.apache.http.client.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Configuration defines components for sending messages outbound the application.
 * <p>
 * Also provides {@code EventReceiverURLBuilder} instance for building log service url. Configuration produce value equal
 * to url that service send to Eureka Server. In this case we exclude Event Log from broadcast via Event Broker.
 * <p>
 * Host name we determine from {@literal eureka.instance.host} property if exists or from {@link InetAddress#getLocalHost}.
 * Port can be resolved only after embedded servlet container initialized. That's why configuration implements
 * {@link ApplicationListener} interface to resolve port and context path.
 */
@Configuration
@EnableConfigurationProperties(SenderClientProperties.class)
public class EventTransportConfiguration implements ApplicationListener<EmbeddedServletContainerInitializedEvent> {

    @Autowired
    private SenderClientProperties properties;

    @Autowired
    private Environment env;

    @Bean
    public HttpClient eventSenderHttpClient() {
        return HttpEventSender.createHttpClient(properties.getConnectTimeout(), properties.getReadTimeout());
    }

    @Bean
    public EventSender scmEventSender() {
        return new HttpEventSender(eventSenderHttpClient());
    }

    @Bean
    public EventReceiverURLBuilder eventLogUrlBuilder() throws UnknownHostException {
        EventReceiverURLBuilder builder = new EventReceiverURLBuilder();

        if (env.containsProperty("eureka.instance.host")) {
            builder.setHost(env.getProperty("eureka.instance.host"));
        } else {
            builder.setHost(InetAddress.getLocalHost().getHostName());
        }

        return builder;
    }

    /**
     * Initialize port and context path after EmbeddedServletContainer initialized.
     */
    @Override
    public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {
        try {
            eventLogUrlBuilder().setPort(event.getEmbeddedServletContainer().getPort())
                    .setContextPath(event.getApplicationContext().getServletContext().getContextPath());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}