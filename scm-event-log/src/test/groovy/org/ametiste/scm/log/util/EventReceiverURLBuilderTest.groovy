package org.ametiste.scm.log.util

import spock.lang.Specification

class EventReceiverURLBuilderTest extends Specification {

    def "should build default url"() {
        expect:
        new EventReceiverURLBuilder().build() != null
    }

    def "should build correct url"() {
        given:
        URI url = URI.create("http://peer1:8085/path/event-receiver")

        expect:
        url == new EventReceiverURLBuilder().setHost("peer1")
                .setContextPath("/path")
                .setPort(8085)
                .build();
    }
}
