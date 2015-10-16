package org.ametiste.scm.log.boot.config;

import org.ametiste.scm.log.controller.EventInfoController;
import org.ametiste.scm.log.persistent.EventDAO;
import org.ametiste.scm.log.service.EventInformer;
import org.ametiste.scm.log.service.EventInformerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.springframework.context.annotation.ComponentScan.Filter;

/**
 * Configuration for Informer Feature.
 * <p>
 * Configuration defines {@code EventInformer} instance and enable {@code EventInfoController} by component scan
 * annotation to allow info requests.
 */
@Configuration
@EnableWebMvc
@Import(EventPersistentConfiguration.class)
@ComponentScan(value = "org.ametiste.scm.log.controller", useDefaultFilters = false,
        includeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = EventInfoController.class))
public class InformerFeatureConfiguration {

    @Autowired
    private EventDAO eventDAO;

    @Bean
    public EventInformer eventInformerService() {
        return new EventInformerImpl(eventDAO);
    }
}
