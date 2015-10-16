package org.ametiste.scm.log.boot.config;

import com.mongodb.MongoClientOptions;
import com.mongodb.WriteConcern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import static org.apache.commons.lang3.Validate.isTrue;

/**
 * Configuration for custom Mongo DB components.
 * <p>
 * It contains redefinition of {@code MappingMongoConverter} to add dot-replace logic.
 * All other components configured by Spring Boot.
 * <p>
 * Connection properties to DB instance also provides via Spring Boot MongoDB properties ({@literal spring.data.mongodb.*}).
 * Also configuration set additional mongo client options. Result bean used by Spring Boot configuration.
 *
 * @see org.springframework.boot.autoconfigure.mongo.MongoDataAutoConfiguration
 */
@Configuration
@EnableConfigurationProperties(LogMongoProperties.class)
public class MongoDbConfiguration {

    @Autowired
    private LogMongoProperties properties;

    @Bean
    public MongoClientOptions mongoOptions() {
        return MongoClientOptions.builder()
                .autoConnectRetry(properties.getAutoConnectRetry())
                .connectionsPerHost(properties.getConnectionsPerHost())
                .threadsAllowedToBlockForConnectionMultiplier(properties.getThreadsAllowedToBlockForConnectionMultiplier())
                .writeConcern(WriteConcern.valueOf(properties.getWriteConcern()))
                .build();
    }

    @Bean
    public MappingMongoConverter mongoConverter(MongoDbFactory factory, MongoMappingContext context) {
        isTrue(factory != null, "'factory' must be initialized");
        isTrue(context != null, "'context' must be initialized");

        DbRefResolver dbRefResolver = new DefaultDbRefResolver(factory);
        MappingMongoConverter mappingConverter = new MappingMongoConverter(dbRefResolver, context);
        mappingConverter.setMapKeyDotReplacement("\\+");
        return mappingConverter;
    }
}
