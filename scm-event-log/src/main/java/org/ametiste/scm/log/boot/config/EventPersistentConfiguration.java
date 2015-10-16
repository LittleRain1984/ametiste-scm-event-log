package org.ametiste.scm.log.boot.config;

import org.ametiste.scm.log.persistent.EventDAO;
import org.ametiste.scm.log.persistent.MongoEventDAO;
import org.ametiste.scm.messaging.data.mongo.event.EventDocument;
import org.ametiste.scm.messaging.data.mongo.event.factory.DefaultEventToDocumentConverterMapFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.IndexOperations;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.Index;

import javax.annotation.PostConstruct;

/**
 * Configuration define data access object to event repository.
 * It contains {@code EvenDAO} object for Mongo DB repository and ensure indices created if property allow this.
 */
@Configuration
@Import(MongoDbConfiguration.class)
@EnableConfigurationProperties(StoreProperties.class)
public class EventPersistentConfiguration {

    @Autowired
    private StoreProperties properties;

    @Autowired
    private MongoOperations mongoOperations;

    @Bean
    public EventDAO mongoEventDAO() {
        return new MongoEventDAO(mongoOperations, new DefaultEventToDocumentConverterMapFactory());
    }

    @PostConstruct
    private void initializeIndices() {
        if (properties.isAllowCreateIndex()) {
            IndexOperations operations = mongoOperations.indexOps(EventDocument.class);
            operations.ensureIndex(new Index().on("timestamp", Sort.Direction.ASC));
        }
    }
}
