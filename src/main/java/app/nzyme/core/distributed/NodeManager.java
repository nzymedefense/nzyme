package app.nzyme.core.distributed;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.distributed.database.NodeEntry;
import app.nzyme.core.distributed.database.metrics.NodeMetricsGaugeAggregation;
import app.nzyme.core.taps.metrics.BucketSize;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class NodeManager {

    private static final Logger LOG = LogManager.getLogger(NodeManager.class);

    private final NzymeNode nzyme;

    private UUID localNodeId;

    private final AtomicLong tapReportSize;

    public NodeManager(NzymeNode nzyme) {
        this.nzyme = nzyme;

        this.tapReportSize = new AtomicLong(0);
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
                handle.createUpdate("INSERT INTO nodes(uuid, name, http_external_uri, version, last_seen, " +
                                "memory_bytes_total, memory_bytes_available, memory_bytes_used, heap_bytes_total, " +
                                "heap_bytes_available, heap_bytes_used, cpu_system_load, cpu_thread_count, " +
                                "process_start_time, process_virtual_size, process_arguments, os_information) " +
                                "VALUES(:uuid, :name, :http_external_uri, :version, NOW(), :memory_bytes_total, " +
                                ":memory_bytes_available, :memory_bytes_used, :heap_bytes_total, :heap_bytes_available, " +
                                " :heap_bytes_used, :cpu_system_load, :cpu_thread_count, :process_start_time, " +
                                ":process_virtual_size, :process_arguments, :os_information) " +
                                "ON CONFLICT(uuid) DO UPDATE SET name = :name, http_external_uri = :http_external_uri, " +
                                "version = :version, last_seen = NOW(), memory_bytes_total = :memory_bytes_total, " +
                                "memory_bytes_available = :memory_bytes_available, memory_bytes_used = :memory_bytes_used, " +
                                "heap_bytes_total = :heap_bytes_total, heap_bytes_available = :heap_bytes_available, " +
                                "heap_bytes_used = :heap_bytes_used, cpu_system_load = :cpu_system_load, " +
                                "cpu_thread_count = :cpu_thread_count, process_start_time = :process_start_time, " +
                                "process_virtual_size = :process_virtual_size, process_arguments = :process_arguments, " +
                                "os_information = :os_information")
                        .bind("uuid", localNodeId)
                        .bind("name", nzyme.getNodeInformation().name())
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
                        .execute()
        );
    }

    public List<Node> getActiveNodes() {
        List<NodeEntry> dbEntries = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM nodes WHERE last_seen > :timeout ORDER BY name DESC")
                        .bind("timeout", DateTime.now().minusHours(24))
                        .mapTo(NodeEntry.class)
                        .list()
        );

        List<Node> nodes = Lists.newArrayList();
        for (NodeEntry dbEntry : dbEntries) {
            try {
                URI httpExternalUri = URI.create(dbEntry.httpExternalUri());
                nodes.add(Node.create(
                        dbEntry.uuid(),
                        dbEntry.name(),
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
                        dbEntry.lastSeen()
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
                URI httpExternalUri = URI.create(ne.httpExternalUri());
                return Optional.of(Node.create(
                        ne.uuid(),
                        ne.name(),
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
                        ne.lastSeen()
                ));
            } catch (Exception e) {
                throw new RuntimeException("Could not create node from database entry.", e);
            }
        } else {
            return Optional.empty();
        }
    }

    private void runMetrics() {
        try {
            long tapReportSize = this.tapReportSize.getAndSet(0);
            NodeInformation.Info ni = new NodeInformation().collect();

            writeGauge(NodeMetricName.MEMORY_BYTES_TOTAL.database_label, ni.memoryTotal());
            writeGauge(NodeMetricName.MEMORY_BYTES_AVAILABLE.database_label, ni.memoryAvailable());
            writeGauge(NodeMetricName.MEMORY_BYTES_USED.database_label, ni.memoryUsed());
            writeGauge(NodeMetricName.HEAP_BYTES_TOTAL.database_label, ni.heapTotal());
            writeGauge(NodeMetricName.HEAP_BYTES_AVAILABLE.database_label, ni.heapAvailable());
            writeGauge(NodeMetricName.HEAP_BYTES_USED.database_label, ni.heapUsed());
            writeGauge(NodeMetricName.CPU_SYSTEM_LOAD.database_label, ni.cpuSystemLoad());
            writeGauge(NodeMetricName.PROCESS_VIRTUAL_SIZE.database_label, ni.processVirtualSize());
            writeGauge(NodeMetricName.TAP_REPORT_SIZE.database_label, tapReportSize);
        } catch(Exception e) {
            LOG.error("Could not write node metrics.", e);
        }

        // Retention clean old metrics.
        nzyme.getDatabase().useHandle(handle -> {
            handle.createUpdate("DELETE FROM node_metrics_gauges WHERE created_at < :created_at")
                    .bind("created_at", DateTime.now().minusHours(24))
                    .execute();
        });
    }

    private void writeGauge(String metricName, Long metricValue) {
        writeGauge(metricName, metricValue.doubleValue());
    }

    private void writeGauge(String metricName, Double metricValue) {
        nzyme.getDatabase().withHandle(handle -> handle.createUpdate("INSERT INTO node_metrics_gauges(node_id, metric_name, metric_value, created_at) " +
                        "VALUES(:node_id, :metric_name, :metric_value, :created_at)")
                .bind("node_id", nzyme.getNodeInformation().id())
                .bind("metric_name", metricName)
                .bind("metric_value", metricValue)
                .bind("created_at", DateTime.now())
                .execute()
        );
    }

    public Optional<Map<DateTime, NodeMetricsGaugeAggregation>> findMetricsHistogram(UUID nodeId, String metricName, int hours, BucketSize bucketSize) {
        Map<DateTime, NodeMetricsGaugeAggregation> result = Maps.newHashMap();

        List<NodeMetricsGaugeAggregation> agg = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT AVG(metric_value) AS average, MAX(metric_value) AS maximum, " +
                                "MIN(metric_value) AS minimum, SUM(metric_value) AS sum, " +
                                "date_trunc(:bucket_size, created_at) AS bucket " +
                                "FROM node_metrics_gauges WHERE node_id = :node_id AND metric_name = :metric_name " +
                                "AND created_at > :created_at GROUP BY bucket ORDER BY bucket DESC")
                        .bind("bucket_size", bucketSize.toString().toLowerCase())
                        .bind("node_id", nodeId)
                        .bind("metric_name", metricName)
                        .bind("created_at", DateTime.now().minusHours(hours))
                        .mapTo(NodeMetricsGaugeAggregation.class)
                        .list()
        );

        if (agg == null || agg.isEmpty()) {
            return Optional.empty();
        }

        for (NodeMetricsGaugeAggregation x : agg) {
            result.put(x.bucket(), x);
        }

        return Optional.of(result);
    }

    public UUID getLocalNodeId() {
        return localNodeId;
    }

    public void recordTapReportSize(long size) {
        this.tapReportSize.addAndGet(size);
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
