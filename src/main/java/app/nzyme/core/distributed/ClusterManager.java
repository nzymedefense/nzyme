package app.nzyme.core.distributed;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.distributed.database.metrics.TimerSnapshot;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClusterManager {

    private final NzymeNode nzyme;

    public ClusterManager(NzymeNode nzyme) {
        this.nzyme = nzyme;
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
