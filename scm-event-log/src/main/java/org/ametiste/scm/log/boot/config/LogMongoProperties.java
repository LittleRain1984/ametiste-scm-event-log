package org.ametiste.scm.log.boot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Additional configuration properties for Mongo Client.
 * <p>
 * Defined properties are included {@code (org.ametiste.scm.log.mongo.*)}:
 * <ul>
 *     <li><b>autoConnectionRetry</b> - enable auto connect retry. Default is {@literal true}.</li>
 *     <li><b>connectionsPerHost</b> - maximum number of connections per host. Default is {@literal 50}.</li>
 *     <li><b>threadsAllowedToBlockForConnectionMultiplier</b> - multiplier for number of threads allowed to block
 *                                     waiting for a connection. Default is {@literal 5}.</li>
 *     <li><b>writeConcern</b> - write concern. Default is {@literal "ACKNOWLEDGED"}.</li>
 * </ul>
 *
 * @see com.mongodb.WriteConcern
 */
@ConfigurationProperties("org.ametiste.scm.log.mongo")
public class LogMongoProperties {

    private Boolean autoConnectRetry = true;
    private Integer connectionsPerHost = 50;
    private Integer threadsAllowedToBlockForConnectionMultiplier = 5;
    private String writeConcern = "ACKNOWLEDGED";

    public Boolean getAutoConnectRetry() {
        return autoConnectRetry;
    }

    public void setAutoConnectRetry(Boolean autoConnectRetry) {
        this.autoConnectRetry = autoConnectRetry;
    }

    public Integer getConnectionsPerHost() {
        return connectionsPerHost;
    }

    public void setConnectionsPerHost(Integer connectionsPerHost) {
        this.connectionsPerHost = connectionsPerHost;
    }

    public Integer getThreadsAllowedToBlockForConnectionMultiplier() {
        return threadsAllowedToBlockForConnectionMultiplier;
    }

    public void setThreadsAllowedToBlockForConnectionMultiplier(Integer threadsAllowedToBlockForConnectionMultiplier) {
        this.threadsAllowedToBlockForConnectionMultiplier = threadsAllowedToBlockForConnectionMultiplier;
    }

    public String getWriteConcern() {
        return writeConcern;
    }

    public void setWriteConcern(String writeConcern) {
        this.writeConcern = writeConcern;
    }
}
