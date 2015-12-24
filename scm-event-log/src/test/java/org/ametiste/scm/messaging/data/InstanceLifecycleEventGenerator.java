package org.ametiste.scm.messaging.data;

import org.ametiste.scm.messaging.data.event.Event;
import org.ametiste.scm.messaging.data.event.InstanceLifecycleEvent;

import java.net.URI;
import java.util.Collection;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generate {@code InstanceStartupEvent} objects for testing needs.
 */
public class InstanceLifecycleEventGenerator {

    public static final String[] DEFAULT_INSTANCE_IDS = { "org.monitor", "IFTG", "global.peer" };
    public static final String[] DEFAULT_VERSIONS = { "0.10.0", "0.2.6.RELEASE", "0.1.5-r3-RC" };
    public static final String[] DEFAULT_NODE_IDS = { "master-node", "data-node", "data-node14" };
    public static final URI[] DEFAULT_URIS = { URI.create("http://localhost"),
                                               URI.create("http://peer1:8085/event-receiver") };

    private static final Random RANDOM = new Random();

    private final String[] instanceIds;
    private final String[] versions;
    private final String[] nodeIds;
    private final URI[] uris;

    public InstanceLifecycleEventGenerator() {
        this(null, null, null, null);
    }

    public InstanceLifecycleEventGenerator(Collection<String> instanceIds,
                                           Collection<String> versions,
                                           Collection<String> nodeIds,
                                           Collection<URI> uris) {
        if (instanceIds == null) {
            this.instanceIds = DEFAULT_INSTANCE_IDS;
        } else {
            this.instanceIds = instanceIds.toArray(new String[instanceIds.size()]);
        }

        if (versions == null) {
            this.versions = DEFAULT_VERSIONS;
        } else {
            this.versions = versions.toArray(new String[versions.size()]);
        }

        if (nodeIds == null) {
            this.nodeIds = DEFAULT_NODE_IDS;
        } else {
            this.nodeIds = nodeIds.toArray(new String[nodeIds.size()]);
        }

        if (uris == null) {
            this.uris = DEFAULT_URIS;
        } else {
            this.uris = uris.toArray(new URI[uris.size()]);
        }
    }

    public InstanceLifecycleEvent generate() {
        return InstanceLifecycleEvent.builder()
                .type(InstanceLifecycleEvent.Type.values()[RANDOM.nextInt(InstanceLifecycleEvent.Type.values().length)])
                .instanceId(instanceIds[RANDOM.nextInt(instanceIds.length)])
                .version(versions[RANDOM.nextInt(versions.length)])
                .nodeId(nodeIds[RANDOM.nextInt(nodeIds.length)])
                .uri(uris[RANDOM.nextInt(uris.length)])
                .build();
    }

    public Collection<Event> generate(int size) {
        return Stream.generate(this::generate).limit(size).collect(Collectors.toList());
    }
}
