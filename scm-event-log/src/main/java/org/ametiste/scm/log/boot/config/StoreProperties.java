package org.ametiste.scm.log.boot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for persistent storage.
 * <p>
 * Defined properties are included ({@code org.ametiste.scm.log.store.*}):
 * <ul>
 *     <li><b>flushPeriod</b> - time interval between flush to external storage (in milliseconds). Default is {@literal 1000}.</li>
 *     <li><b>allowCreateIndex</b> - allow create indices on initialization. Default is {@literal true}.</li>
 * </ul>
 */
@ConfigurationProperties("org.ametiste.scm.log.store")
public class StoreProperties {

    private int flushPeriod = 1000;
    private boolean allowCreateIndex = true;

    public int getFlushPeriod() {
        return flushPeriod;
    }

    public void setFlushPeriod(int flushPeriod) {
        this.flushPeriod = flushPeriod;
    }

    public boolean isAllowCreateIndex() {
        return allowCreateIndex;
    }

    public void setAllowCreateIndex(boolean allowCreateIndex) {
        this.allowCreateIndex = allowCreateIndex;
    }
}
