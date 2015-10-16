package org.ametiste.scm.log.boot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Defines set of properties that would be used to configure replay mechanism.
 * <p>
 * Defined properties are included ({@code org.ametiste.scm.log.replay.*}):
 * <ul>
 *     <li><b>bulk-size</b> - size of messages pack that send at once. Default is 100.</li>
 * </ul>
 */
@ConfigurationProperties("org.ametiste.scm.log.replay")
public class ReplayProperties {

    private Integer bulkSize = 100;

    public int getBulkSize() {
        return bulkSize;
    }

    public void setBulkSize(int bulkSize) {
        this.bulkSize = bulkSize;
    }
}
