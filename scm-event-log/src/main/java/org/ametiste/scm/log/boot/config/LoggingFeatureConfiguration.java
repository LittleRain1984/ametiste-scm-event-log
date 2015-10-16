package org.ametiste.scm.log.boot.config;

import org.ametiste.scm.coordinator.config.ScmEventSubscriberConfiguration;
import org.ametiste.scm.log.persistent.EventDAO;
import org.ametiste.scm.log.service.EventLogger;
import org.ametiste.scm.log.service.EventLoggerImpl;
import org.ametiste.scm.messaging.data.event.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Configuration for Event Logging Feature.
 * <p>
 * Contains all needed components and settings to organize log of incoming events.
 * <p>
 * Configuration enables {@code org.ametiste.scm.messaging.receiver.EventReceivingController} for receiving messages and
 * publish it with Spring ApplicationEventPublisher.<p>
 * Method {@link #onEventReceived} registered as event listener for all {@code Event} objects and store events with
 * {@code EventLogger} instance.<p>
 * Method {@link #flushEvents} scheduled with fixed delay parameter that defines by property
 * {@literal org.ametiste.scm.log.store.flush-period}. It invokes {@code flush} method of {@code EventLogger} to store
 * events to persistent.
 * Delay parameter of scheduler must have reasonable value. High value lead to rise load to persistent, low value
 * lead to increase risk lost data from temp on unexpected application crash.
 */
@Configuration
@EnableWebMvc
@EnableScheduling
@ComponentScan("org.ametiste.scm.messaging.receiver")
@Import({ EventPersistentConfiguration.class, ScmEventSubscriberConfiguration.class })
public class LoggingFeatureConfiguration {

    @Autowired
    private EventDAO eventDAO;

    @EventListener
    private void onEventReceived(Event event) {
        eventLoggerService().store(event);
    }

    @Scheduled(fixedDelayString = "${org.ametiste.scm.log.store.flush-period}", initialDelay = 1000)
    private void flushEvents() {
        eventLoggerService().flush();
    }

    @Bean
    public EventLogger eventLoggerService() {
        return new EventLoggerImpl(eventDAO);
    }
}
