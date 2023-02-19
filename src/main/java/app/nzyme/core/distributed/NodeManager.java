package app.nzyme.core.distributed;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.distributed.database.NodeEntry;
import app.nzyme.core.distributed.database.metrics.GaugeHistogramBucket;
import app.nzyme.core.taps.metrics.BucketSize;
import app.nzyme.core.util.MetricNames;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.google.common.base.Charsets;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class NodeManager {

    private static final Logger LOG = LogManager.getLogger(NodeManager.class);

    private final NzymeNode nzyme;

    private UUID localNodeId;

    private final AtomicLong tapReportSize;

    private final LoadingCache<UUID, String> nodeNameCache;

    public NodeManager(NzymeNode nzyme) {
        this.nzyme = nzyme;

        this.tapReportSize = new AtomicLong(0);
        this.nodeNameCache = CacheBuilder.newBuilder().
                expireAfterAccess(10, TimeUnit.SECONDS)
                .build(new CacheLoader<>() {
                    @Override
                    public String load(@NotNull UUID nodeId) {
                        String nodeName = nzyme.getDatabase().withHandle(handle ->
                            handle.createQuery("SELECT name FROM nodes WHERE uuid = :uuid")
                                    .bind("uuid", nodeId)
                                    .mapTo(String.class)
                                    .one()
                        );

                        if (nodeName == null) {
                            return "[invalid node]";
                        }

                        return nodeName;
                    }
                });
    }

    public void initialize() throws NodeInitializationException {
        // Read local node id.
        Path nodeIdFile = Path.of(nzyme.getDataDirectory().toString(), "node_id");
        if (Files.exists(nodeIdFile)) {
            try {
                LOG.debug("Node ID file exists at [{}]", nodeIdFile.toAbsolutePath());
                localNodeId = UUID.fromString(Files.readString(nodeIdFile));
            } catch (IOException e) {
                throw new NodeInitializationException("Could not read node ID file at [" + nodeIdFile.toAbsolutePath() + "]", e);
            }
        } else {
            LOG.debug("Node ID file does not exist at [{}]. Creating.", nodeIdFile.toAbsolutePath());
            UUID newNodeId = UUID.randomUUID();

            try {
                Files.writeString(nodeIdFile, newNodeId.toString(), Charsets.UTF_8, StandardOpenOption.CREATE);
            } catch (IOException e) {
                throw new NodeInitializationException("Could not write node ID file at [" + nodeIdFile.toAbsolutePath() + "]", e);
            }

            localNodeId = newNodeId;
            LOG.info("Created node ID: [{}]", localNodeId);
        }

        Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder()
                        .setNameFormat("node-metrics-updater-%d")
                        .setDaemon(true)
                        .build()
        ).scheduleAtFixedRate(this::runMetrics, 1, 1, TimeUnit.MINUTES);

        LOG.info("Node ID: [{}]", localNodeId);
    }

    public void registerSelf() {
        if (localNodeId == null) {
            throw new RuntimeException("Not initialized. Cannot register myself.");
        }

        NodeInformation.Info ni = new NodeInformation().collect();

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO nodes(uuid, name, http_listen_uri, http_external_uri, version, " +
                                " last_seen, memory_bytes_total, memory_bytes_available, memory_bytes_used, " +
                                "heap_bytes_total, heap_bytes_available, heap_bytes_used, cpu_system_load, " +
                                "cpu_thread_count, process_start_time, process_virtual_size, process_arguments, " +
                                "os_information, clock, deleted) VALUES(:uuid, :name, :http_listen_uri, " +
                                ":http_external_uri, :version, NOW(), :memory_bytes_total, :memory_bytes_available, " +
                                ":memory_bytes_used, :heap_bytes_total, :heap_bytes_available, " +
                                ":heap_bytes_used, :cpu_system_load, :cpu_thread_count, :process_start_time, " +
                                ":process_virtual_size, :process_arguments, :os_information, :clock, false) " +
                                "ON CONFLICT(uuid) DO UPDATE SET name = :name, http_external_uri = :http_external_uri, " +
                                "http_listen_uri = :http_listen_uri, version = :version, last_seen = NOW()," +
                                "memory_bytes_total = :memory_bytes_total, " +
                                "memory_bytes_available = :memory_bytes_available, memory_bytes_used = :memory_bytes_used, " +
                                "heap_bytes_total = :heap_bytes_total, heap_bytes_available = :heap_bytes_available, " +
                                "heap_bytes_used = :heap_bytes_used, cpu_system_load = :cpu_system_load, " +
                                "cpu_thread_count = :cpu_thread_count, process_start_time = :process_start_time, " +
                                "process_virtual_size = :process_virtual_size, process_arguments = :process_arguments, " +
                                "os_information = :os_information, clock = :clock, deleted = false")
                        .bind("uuid", localNodeId)
                        .bind("name", nzyme.getNodeInformation().name())
                        .bind("http_listen_uri", nzyme.getConfiguration().restListenUri().toString())
                        .bind("http_external_uri", nzyme.getConfiguration().httpExternalUri().toString())
                        .bind("version", nzyme.getVersion().getVersion().toString())
                        .bind("memory_bytes_total", ni.memoryTotal())
                        .bind("memory_bytes_available", ni.memoryAvailable())
                        .bind("memory_bytes_used", ni.memoryUsed())
                        .bind("heap_bytes_total", ni.heapTotal())
                        .bind("heap_bytes_available", ni.heapAvailable())
                        .bind("heap_bytes_used", ni.heapUsed())
                        .bind("cpu_system_load", ni.cpuSystemLoad())
                        .bind("cpu_thread_count", ni.cpuThreadCount())
                        .bind("process_start_time", ni.processStartTime())
                        .bind("process_virtual_size", ni.processVirtualSize())
                        .bind("process_arguments", ni.processArguments())
                        .bind("os_information", ni.osInformation())
                        .bind("clock", DateTime.now())
                        .execute()
        );
    }

    public List<Node> getNodes() {
        List<NodeEntry> dbEntries = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM nodes WHERE last_seen > :timeout ORDER BY name DESC")
                        .bind("timeout", DateTime.now().minusHours(24))
                        .mapTo(NodeEntry.class)
                        .list()
        );

        List<Node> nodes = Lists.newArrayList();
        for (NodeEntry dbEntry : dbEntries) {
            try {
                URI listenUri = URI.create(dbEntry.httpListenUri());
                URI httpExternalUri = URI.create(dbEntry.httpExternalUri());
                nodes.add(Node.create(
                        dbEntry.uuid(),
                        dbEntry.name(),
                        listenUri,
                        httpExternalUri,
                        dbEntry.memoryBytesTotal(),
                        dbEntry.memoryBytesAvailable(),
                        dbEntry.memoryBytesUsed(),
                        dbEntry.heapBytesTotal(),
                        dbEntry.heapBytesAvailable(),
                        dbEntry.heapBytesUsed(),
                        dbEntry.cpuSystemLoad(),
                        dbEntry.cpuThreadCount(),
                        dbEntry.processStartTime(),
                        dbEntry.processVirtualSize(),
                        dbEntry.processArguments(),
                        dbEntry.osInformation(),
                        dbEntry.version(),
                        dbEntry.lastSeen(),
                        dbEntry.clock(),
                        (long) new Period(dbEntry.lastSeen(), dbEntry.clock(), PeriodType.millis()).getMillis(),
                        isNodeEphemeral(dbEntry),
                        dbEntry.deleted()
                ));
            } catch (Exception e) {
                LOG.error("Could not create node from database entry. Skipping.", e);
            }
        }

        return nodes;
    }

    public Optional<Node> getNode(UUID nodeId) {
        Optional<NodeEntry> result = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM nodes WHERE uuid = :uuid")
                        .bind("uuid", nodeId)
                        .mapTo(NodeEntry.class)
                        .findFirst()
        );

        if (result.isPresent()) {
            NodeEntry ne = result.get();
            try {
                URI httpListenUri = URI.create(ne.httpListenUri());
                URI httpExternalUri = URI.create(ne.httpExternalUri());
                return Optional.of(Node.create(
                        ne.uuid(),
                        ne.name(),
                        httpListenUri,
                        httpExternalUri,
                        ne.memoryBytesTotal(),
                        ne.memoryBytesAvailable(),
                        ne.memoryBytesUsed(),
                        ne.heapBytesTotal(),
                        ne.heapBytesAvailable(),
                        ne.heapBytesUsed(),
                        ne.cpuSystemLoad(),
                        ne.cpuThreadCount(),
                        ne.processStartTime(),
                        ne.processVirtualSize(),
                        ne.processArguments(),
                        ne.osInformation(),
                        ne.version(),
                        ne.lastSeen(),
                        ne.clock(),
                        (long) new Period(ne.lastSeen(), ne.clock(), PeriodType.millis()).getMillis(),
                        isNodeEphemeral(ne),
                        ne.deleted()
                ));
            } catch (Exception e) {
                throw new RuntimeException("Could not create node from database entry.", e);
            }
        } else {
            return Optional.empty();
        }
    }

    public void deleteNode(UUID nodeId) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE nodes SET deleted = true WHERE uuid = :node_id")
                        .bind("node_id", nodeId)
                        .execute()
        );
    }

    private void runMetrics() {
        try {
            MetricRegistry metrics = nzyme.getMetrics();
            long tapReportSize = this.tapReportSize.getAndSet(0);
            NodeInformation.Info ni = new NodeInformation().collect();

            writeGauge(MetricExternalName.MEMORY_BYTES_TOTAL.database_label, ni.memoryTotal());
            writeGauge(MetricExternalName.MEMORY_BYTES_AVAILABLE.database_label, ni.memoryAvailable());
            writeGauge(MetricExternalName.MEMORY_BYTES_USED.database_label, ni.memoryUsed());
            writeGauge(MetricExternalName.HEAP_BYTES_TOTAL.database_label, ni.heapTotal());
            writeGauge(MetricExternalName.HEAP_BYTES_AVAILABLE.database_label, ni.heapAvailable());
            writeGauge(MetricExternalName.HEAP_BYTES_USED.database_label, ni.heapUsed());
            writeGauge(MetricExternalName.CPU_SYSTEM_LOAD.database_label, ni.cpuSystemLoad());
            writeGauge(MetricExternalName.PROCESS_VIRTUAL_SIZE.database_label, ni.processVirtualSize());
            writeGauge(MetricExternalName.TAP_REPORT_SIZE.database_label, tapReportSize);

            writeTimer(MetricExternalName.PGP_ENCRYPTION_TIMER.database_label,
                    metrics.getTimers().get(MetricNames.PGP_ENCRYPTION_TIMING));
            writeTimer(MetricExternalName.PGP_DECRYPTION_TIMER.database_label,
                    metrics.getTimers().get(MetricNames.PGP_DECRYPTION_TIMING));
        } catch(Exception e) {
            LOG.error("Could not write node metrics.", e);
        }

        // Retention clean old metrics.
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM node_metrics_gauges WHERE created_at < :created_at")
                        .bind("created_at", DateTime.now().minusHours(24))
                        .execute());
    }

    private void writeGauge(String metricName, double metricValue) {
        nzyme.getDatabase().withHandle(handle -> handle.createUpdate("INSERT INTO node_metrics_gauges(node_id, " +
                        "metric_name, metric_value, created_at) VALUES(:node_id, :metric_name, :metric_value, " +
                        ":created_at)")
                .bind("node_id", nzyme.getNodeInformation().id())
                .bind("metric_name", metricName)
                .bind("metric_value", metricValue)
                .bind("created_at", DateTime.now())
                .execute()
        );
    }

    private void writeTimer(String metricName, @Nullable Timer timer) {
        if (timer == null) {
            return;
        }

        Snapshot s = timer.getSnapshot();
        writeTimer(
                metricName,
                TimeUnit.MICROSECONDS.convert(s.getMax(), TimeUnit.NANOSECONDS),
                TimeUnit.MICROSECONDS.convert(s.getMin(), TimeUnit.NANOSECONDS),
                TimeUnit.MICROSECONDS.convert((long) s.getMean(), TimeUnit.NANOSECONDS),
                TimeUnit.MICROSECONDS.convert((long) s.get99thPercentile(), TimeUnit.NANOSECONDS),
                TimeUnit.MICROSECONDS.convert((long) s.getStdDev(), TimeUnit.NANOSECONDS),
                timer.getCount()
        );
    }

    private void writeTimer(String metricName, long max, long min, long mean, long p99, long stddev, long counter) {
        nzyme.getDatabase().withHandle(handle -> handle.createUpdate("INSERT INTO node_metrics_timers(node_id, " +
                        "metric_name, metric_max, metric_min, metric_mean, metric_p99, metric_stddev, metric_counter, " +
                        "created_at) VALUES(:node_id, :metric_name, :metric_max, :metric_min, :metric_mean, " +
                        ":metric_p99, :metric_stddev, :metric_counter, :created_at)")
                .bind("node_id", nzyme.getNodeInformation().id())
                .bind("metric_name", metricName)
                .bind("metric_max", max)
                .bind("metric_min", min)
                .bind("metric_mean", mean)
                .bind("metric_p99", p99)
                .bind("metric_stddev", stddev)
                .bind("metric_counter", counter)
                .bind("created_at", DateTime.now())
                .execute()
        );
    }

    public Optional<Map<DateTime, GaugeHistogramBucket>> findMetricsHistogram(UUID nodeId, String metricName, int hours, BucketSize bucketSize) {
        Map<DateTime, GaugeHistogramBucket> result = Maps.newHashMap();

        List<GaugeHistogramBucket> agg = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT AVG(metric_value) AS average, MAX(metric_value) AS maximum, " +
                                "MIN(metric_value) AS minimum, SUM(metric_value) AS sum, " +
                                "date_trunc(:bucket_size, created_at) AS bucket " +
                                "FROM node_metrics_gauges WHERE node_id = :node_id AND metric_name = :metric_name " +
                                "AND created_at > :created_at GROUP BY bucket ORDER BY bucket DESC")
                        .bind("bucket_size", bucketSize.toString().toLowerCase())
                        .bind("node_id", nodeId)
                        .bind("metric_name", metricName)
                        .bind("created_at", DateTime.now().minusHours(hours))
                        .mapTo(GaugeHistogramBucket.class)
                        .list()
        );

        if (agg == null || agg.isEmpty()) {
            return Optional.empty();
        }

        for (GaugeHistogramBucket x : agg) {
            result.put(x.bucket(), x);
        }

        return Optional.of(result);
    }

    private boolean isNodeEphemeral(NodeEntry node) {
        return nzyme.getDatabaseCoreRegistry()
                .getValue(NodeRegistryKeys.EPHEMERAL_NODES_REGEX.key())
                .filter(r -> node.name().matches(r))
                .isPresent();
    }

    public UUID getLocalNodeId() {
        return localNodeId;
    }

    public void recordTapReportSize(long size) {
        this.tapReportSize.addAndGet(size);
    }

    public String findNameOfNode(UUID nodeId) {
        try {
            return nodeNameCache.get(nodeId);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static final class NodeInitializationException extends Throwable {

        public NodeInitializationException(String msg) {
            super(msg);
        }

        public NodeInitializationException(String msg, Throwable e) {
            super(msg, e);
        }

    }

}
