package org.ametiste.scm.log.boot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Main configuration of Event Log Application include configurations of necessary feature:
 * <ul>
 *     <li><b>LoggingFeatureConfiguration</b> - components to provide logging incoming events to persistent storage.</li>
 *     <li><b>InformerFeatureConfiguration</b> - components to provide access to stored data.</li>
 *     <li><b>ReplayFeatureConfiguration</b> - components of event replay mechanism.</li>
 * </ul><p>
 * Any of this configurations is fully independent from other and can be used as standalone configuration.
 */
@Configuration
@Import({ LoggingFeatureConfiguration.class,
          InformerFeatureConfiguration.class,
          ReplayFeatureConfiguration.class })
public class EventLogConfiguration {
}
