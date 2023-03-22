package app.nzyme.core.distributed;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.distributed.database.metrics.TimerSnapshot;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.*;

public class ClusterManager {

    private static final Logger LOG = LogManager.getLogger(ClusterManager.class);

    private static final String CLUSTER_ID_REGISTRY_KEY = "cluster_id";

    private final NzymeNode nzyme;

    private boolean joinedExistingCluster;

    public ClusterManager(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public void initialize() {
        Optional<String> clusterId = nzyme.getDatabaseCoreRegistry().getValue(CLUSTER_ID_REGISTRY_KEY);
        if (clusterId.isPresent()) {
            LOG.info("Cluster ID [{}].", clusterId.get());
            joinedExistingCluster = true;
        } else {
            UUID uuid = UUID.randomUUID();
            nzyme.getDatabaseCoreRegistry().setValue(CLUSTER_ID_REGISTRY_KEY, uuid.toString());
            LOG.info("New cluster! Generated ID [{}].", uuid);
            joinedExistingCluster = false;
        }
    }

    public boolean joinedExistingCluster() {
        return joinedExistingCluster;
    }

    public UUID getClusterId() {
        Optional<String> clusterId = nzyme.getDatabaseCoreRegistry().getValue(CLUSTER_ID_REGISTRY_KEY);

        if (clusterId.isEmpty()) {
            throw new RuntimeException("No cluster ID found. This should never happen. Restart nzyme.");
        }

        return UUID.fromString(clusterId.get());
    }

    public Map<UUID, TimerSnapshot> findMetricTimer(String metricName) {
        List<TimerSnapshot> timers = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT DISTINCT ON (node_id) node_id, metric_name, metric_max, metric_min, " +
                                "metric_mean, metric_p99, metric_stddev, metric_counter, created_at " +
                                "FROM node_metrics_timers " +
                                "WHERE metric_name = :metric_name AND created_at > :created_at " +
                                "ORDER BY node_id, created_at DESC")
                        .bind("metric_name", metricName)
                        .bind("created_at", DateTime.now().minusMinutes(2))
                        .mapTo(TimerSnapshot.class)
                        .list()
        );

        Map<UUID, TimerSnapshot> result = Maps.newTreeMap();
        for (TimerSnapshot timer : timers) {
            result.put(timer.nodeId(), timer);
        }

        return result;
    }

}
