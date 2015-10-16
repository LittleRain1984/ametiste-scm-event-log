package org.ametiste.scm.log.boot.config;

import org.ametiste.scm.log.controller.EventReplayController;
import org.ametiste.scm.log.controller.EventReplayExceptionHandler;
import org.ametiste.scm.log.persistent.EventDAO;
import org.ametiste.scm.log.service.EventReplayer;
import org.ametiste.scm.log.service.EventReplayerImpl;
import org.ametiste.scm.log.util.EventReceiverURLBuilder;
import org.ametiste.scm.messaging.sender.EventSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.springframework.context.annotation.ComponentScan.Filter;

/**
 * Configuration for Replay Feature.
 * <p>
 * Configuration contains components for event replay processing:
 * <ul>
 *     <li>include configuration for event subscribing in coordinator;</li>
 *     <li>create controller to handle replay specific requests;</li>
 *     <li>include transport component: sender, exclude url builder;</li>
 *     <li>create instance of {@code EventReplayer} service.</li>
 * </ul>
 * <p>
 * {@code EventReplayer} implementation include URI builder instead simple URI because at moment when bean build
 * container may be not fully initialized and we have store wrong URL address.
 */
@Configuration
@EnableWebMvc
@Import({EventPersistentConfiguration.class, EventTransportConfiguration.class})
@ComponentScan(value = "org.ametiste.scm.log.controller", useDefaultFilters = false,
        includeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE,
                                 classes = { EventReplayController.class, EventReplayExceptionHandler.class }))
@EnableConfigurationProperties(ReplayProperties.class)
public class ReplayFeatureConfiguration {

    @Autowired
    private ReplayProperties properties;

    @Autowired
    private EventDAO eventDAO;

    @Autowired
    private EventSender eventSender;

    @Autowired
    private EventReceiverURLBuilder eventLogUrlBuilder;

    @Bean
    public EventReplayer eventReplayerService() {
        return new EventReplayerImpl(eventDAO, eventSender, properties.getBulkSize(), eventLogUrlBuilder);
    }
}
