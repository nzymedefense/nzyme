package app.nzyme.core.taps;

import app.nzyme.core.NzymeNode;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import app.nzyme.core.taps.metrics.BucketSize;
import app.nzyme.core.taps.metrics.TapMetrics;
import app.nzyme.core.taps.metrics.TapMetricsGauge;
import app.nzyme.core.rest.resources.taps.reports.CapturesReport;
import app.nzyme.core.rest.resources.taps.reports.ChannelReport;
import app.nzyme.core.rest.resources.taps.reports.StatusReport;
import app.nzyme.core.taps.metrics.TapMetricsGaugeAggregation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TapManager {

    private static final Logger LOG = LogManager.getLogger(TapManager.class);

    private final NzymeNode nzyme;

    public TapManager(NzymeNode nzyme) {
        this.nzyme = nzyme;

        Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder()
                        .setNameFormat("taps-cleaner-%d")
                        .setDaemon(true)
                        .build()
        ).scheduleAtFixedRate(this::retentionClean, 0, 5, TimeUnit.MINUTES);
    }

    public void registerTapStatus(StatusReport report) {
        long tapCount = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) AS count FROM taps WHERE name = :name")
                        .bind("name", report.tapName())
                        .mapTo(Long.class)
                        .one()
        );

        DateTime now = DateTime.now();

        if (tapCount == 0) {
            LOG.info("Registering first report from new tap [{}].", report.tapName());

            nzyme.getDatabase().useHandle(handle ->
                    handle.createUpdate("INSERT INTO taps(name, local_time, processed_bytes_total, processed_bytes_average, " +
                            "memory_total, memory_free, memory_used, cpu_load, created_at, updated_at) " +
                            "VALUES(:name, :local_time, :processed_bytes_total, :processed_bytes_average, :memory_total, " +
                            ":memory_free, :memory_used, :cpu_load, :created_at, :updated_at)")
                            .bind("name", report.tapName())
                            .bind("local_time", report.timestamp())
                            .bind("processed_bytes_total", report.processedBytes().total())
                            .bind("processed_bytes_average", report.processedBytes().average())
                            .bind("memory_total", report.systemMetrics().memoryTotal())
                            .bind("memory_free", report.systemMetrics().memoryFree())
                            .bind("memory_used", report.systemMetrics().memoryTotal()-report.systemMetrics().memoryFree())
                            .bind("cpu_load", report.systemMetrics().cpuLoad())
                            .bind("created_at", now)
                            .bind("updated_at", now)
                            .execute()
            );
        } else {
            LOG.debug("Registering report from existing tap [{}].", report.tapName());

            nzyme.getDatabase().useHandle(handle ->
                    handle.createUpdate("UPDATE taps SET local_time = :local_time, " +
                            "processed_bytes_total = :processed_bytes_total, " +
                            "processed_bytes_average = :processed_bytes_average, memory_total = :memory_total, " +
                            "memory_free = :memory_free, memory_used = :memory_used, cpu_load = :cpu_load, " +
                            "updated_at = :updated_at WHERE name = :name")
                            .bind("local_time", report.timestamp())
                            .bind("processed_bytes_total", report.processedBytes().total())
                            .bind("processed_bytes_average", report.processedBytes().average())
                            .bind("memory_total", report.systemMetrics().memoryTotal())
                            .bind("memory_free", report.systemMetrics().memoryFree())
                            .bind("memory_used", report.systemMetrics().memoryTotal()-report.systemMetrics().memoryFree())
                            .bind("cpu_load", report.systemMetrics().cpuLoad())
                            .bind("updated_at", now)
                            .bind("name", report.tapName())
                            .execute()
            );
        }

        // Register captures.
        for (CapturesReport capture : report.captures()) {
            long captureCount = nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery("SELECT COUNT(*) AS count FROM tap_captures " +
                                    "WHERE interface = :interface AND tap_name = :tap_name")
                            .bind("interface", capture.interfaceName())
                            .bind("tap_name",  report.tapName())
                            .mapTo(Long.class)
                            .one()
            );

            if (captureCount == 0) {
                nzyme.getDatabase().withHandle(handle ->
                    handle.createUpdate("INSERT INTO tap_captures(tap_name, interface, capture_type, is_running, " +
                            "received, dropped_buffer, dropped_interface, updated_at, created_at) VALUES(:tap_name, " +
                            ":interface, :capture_type, :is_running, :received, :dropped_buffer, :dropped_interface, " +
                            ":updated_at, :created_at)")
                            .bind("tap_name", report.tapName())
                            .bind("interface", capture.interfaceName())
                            .bind("capture_type", capture.captureType())
                            .bind("is_running", capture.isRunning())
                            .bind("received", capture.received())
                            .bind("dropped_buffer", capture.droppedBuffer())
                            .bind("dropped_interface", capture.droppedInterface())
                            .bind("updated_at", now)
                            .bind("created_at", now)
                            .execute()
                );
            } else {
                nzyme.getDatabase().withHandle(handle ->
                        handle.createUpdate("UPDATE tap_captures SET capture_type = :capture_type, " +
                                "is_running = :is_running, received = :received, dropped_buffer = :dropped_buffer, " +
                                "dropped_interface = :dropped_interface, updated_at = :updated_at " +
                                "WHERE tap_name = :tap_name AND interface = :interface")
                                .bind("capture_type", capture.captureType())
                                .bind("is_running", capture.isRunning())
                                .bind("received", capture.received())
                                .bind("dropped_buffer", capture.droppedBuffer())
                                .bind("dropped_interface", capture.droppedInterface())
                                .bind("updated_at", now)
                                .bind("tap_name", report.tapName())
                                .bind("interface", capture.interfaceName())
                                .execute()
                );
            }
        }

        // Register bus.
        long busCount = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) AS count FROM tap_buses WHERE tap_name = :tap_name")
                        .bind("tap_name", report.tapName())
                        .mapTo(Long.class)
                        .one()
        );

        if (busCount == 0) {
            nzyme.getDatabase().useHandle(handle ->
                    handle.createUpdate("INSERT INTO tap_buses(tap_name, name, created_at, updated_at) " +
                            "VALUES(:tap_name, :name, :created_at, :updated_at)")
                            .bind("tap_name", report.tapName())
                            .bind("name", report.bus().name())
                            .bind("created_at", now)
                            .bind("updated_at", now)
                            .execute()
            );
        } else {
            nzyme.getDatabase().useHandle(handle ->
                    handle.createUpdate("UPDATE tap_buses SET updated_at = :updated_at WHERE tap_name = :tap_name AND name = :name")
                            .bind("tap_name", report.tapName())
                            .bind("name", report.bus().name())
                            .bind("updated_at", now)
                            .execute()
            );
        }

        Long busId = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT id FROM tap_buses WHERE tap_name = :tap_name")
                        .bind("tap_name", report.tapName())
                        .mapTo(Long.class)
                        .one()
        );

        // Register bus channels.
        for (ChannelReport channel : report.bus().channels()) {
            long channelCount = nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery("SELECT COUNT(*) AS count FROM bus_channels " +
                                    "WHERE bus_id = :bus_id AND name = :channel_name")
                            .bind("bus_id", busId)
                            .bind("channel_name", channel.name())
                            .mapTo(Long.class)
                            .one()
            );

            if (channelCount == 0) {
                nzyme.getDatabase().withHandle(handle ->
                        handle.createUpdate("INSERT INTO bus_channels(name, bus_id, capacity, watermark, errors_total, " +
                                "errors_average, throughput_bytes_total, throughput_bytes_average, " +
                                "throughput_messages_total, throughput_messages_average, created_at, updated_at) " +
                                "VALUES(:name, :bus_id, :capacity, :watermark, :errors_total, :errors_average, " +
                                ":throughput_bytes_total, :throughput_bytes_average, :throughput_messages_total, " +
                                ":throughput_messages_average, :created_at, :updated_at)")
                                .bind("name", channel.name())
                                .bind("bus_id", busId)
                                .bind("capacity", channel.capacity())
                                .bind("watermark", channel.watermark())
                                .bind("errors_total", channel.errors().total())
                                .bind("errors_average", channel.errors().average())
                                .bind("throughput_bytes_total", channel.throughputBytes().total())
                                .bind("throughput_bytes_average", channel.throughputBytes().average())
                                .bind("throughput_messages_total", channel.throughputMessages().total())
                                .bind("throughput_messages_average", channel.throughputMessages().average())
                                .bind("created_at", now)
                                .bind("updated_at", now)
                                .execute()
                );
            } else {
                nzyme.getDatabase().withHandle(handle ->
                        handle.createUpdate("UPDATE bus_channels SET capacity = :capacity, watermark = :watermark, " +
                                "errors_total = :errors_total, errors_average = :errors_average, " +
                                "throughput_bytes_total = :throughput_bytes_total, " +
                                "throughput_bytes_average = :throughput_bytes_average, " +
                                "throughput_messages_total = :throughput_messages_total, " +
                                "throughput_messages_average = :throughput_messages_average, " +
                                "updated_at = :updated_at WHERE bus_id = :bus_id AND name = :name")
                                .bind("name", channel.name())
                                .bind("bus_id", busId)
                                .bind("capacity", channel.capacity())
                                .bind("watermark", channel.watermark())
                                .bind("errors_total", channel.errors().total())
                                .bind("errors_average", channel.errors().average())
                                .bind("throughput_bytes_total", channel.throughputBytes().total())
                                .bind("throughput_bytes_average", channel.throughputBytes().average())
                                .bind("throughput_messages_total", channel.throughputMessages().total())
                                .bind("throughput_messages_average", channel.throughputMessages().average())
                                .bind("updated_at", now)
                                .execute()
                );
            }
        }

        // Metrics
        for (Map.Entry<String, Long> metric : report.gaugesLong().entrySet()) {
            writeGauge(report.tapName(), metric.getKey(), metric.getValue(), report.timestamp());
        }

        // Additional metrics.
        writeGauge(report.tapName(), "system.captures.throughput_bit_sec", report.processedBytes().average()*8/10, report.timestamp());
        writeGauge(report.tapName(), "os.memory.bytes_used", report.systemMetrics().memoryTotal()-report.systemMetrics().memoryFree(), report.timestamp());
        writeGauge(report.tapName(), "os.cpu.load.percent", report.systemMetrics().cpuLoad(), report.timestamp());
    }

    private void writeGauge(String tapName, String metricName, Long metricValue, DateTime timestamp) {
        writeGauge(tapName, metricName, metricValue.doubleValue(), timestamp);
    }

    private void writeGauge(String tapName, String metricName, Double metricValue, DateTime timestamp) {
        nzyme.getDatabase().withHandle(handle -> handle.createUpdate("INSERT INTO metrics_gauges(tap_name, metric_name, metric_value, created_at) " +
                        "VALUES(:tap_name, :metric_name, :metric_value, :created_at)")
                .bind("tap_name", tapName)
                .bind("metric_name", metricName)
                .bind("metric_value", metricValue)
                .bind("created_at", timestamp)
                .execute()
        );
    }

    private void retentionClean() {
        nzyme.getDatabase().useHandle(handle -> {
            handle.createUpdate("DELETE FROM metrics_gauges WHERE created_at < :created_at")
                    .bind("created_at", DateTime.now().minusHours(72)) // TODO
                    .execute();
        });
    }

    public Optional<List<Tap>> findAllTaps() {
        List<Tap> taps = nzyme.getDatabase().withHandle(handle -> handle.createQuery("SELECT * FROM taps;")
                .mapTo(Tap.class)
                .list());

        return taps == null || taps.isEmpty() ? Optional.empty() : Optional.of(taps);
    }

    public Optional<Tap> findTap(String tapName) {
        Tap tap = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM taps WHERE name = :name")
                        .bind("name", tapName)
                        .mapTo(Tap.class)
                        .first()
        );

        return tap == null ? Optional.empty() : Optional.of(tap);
    }

    public TapMetrics findMetricsOfTap(String tapName) {
        List<TapMetricsGauge> gauges = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT DISTINCT ON (metric_name) metric_name, tap_name, metric_value, created_at " +
                        "FROM metrics_gauges WHERE tap_name = :tap_name AND created_at > :created_at " +
                        "ORDER BY metric_name, created_at DESC")
                        .bind("tap_name", tapName)
                        .bind("created_at", DateTime.now().minusMinutes(1))
                        .mapTo(TapMetricsGauge.class)
                        .list()
        );

        return TapMetrics.create(tapName, gauges);
    }

    public Optional<Map<DateTime, TapMetricsGaugeAggregation>> findMetricsHistogram(String tapName, String metricName, int hours, BucketSize bucketSize) {
        Map<DateTime, TapMetricsGaugeAggregation> result = Maps.newHashMap();

        List<TapMetricsGaugeAggregation> agg = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT AVG(metric_value) AS average, MAX(metric_value) AS maximum, MIN(metric_value) AS minimum, date_trunc(:bucket_size, created_at) AS bucket FROM metrics_gauges " +
                        "WHERE tap_name = :tap_name AND metric_name = :metric_name AND created_at > :created_at " +
                        "GROUP BY bucket ORDER BY bucket DESC")
                        .bind("bucket_size", bucketSize.toString().toLowerCase())
                        .bind("tap_name", tapName)
                        .bind("metric_name", metricName)
                        .bind("created_at", DateTime.now().minusHours(hours))
                        .mapTo(TapMetricsGaugeAggregation.class)
                        .list()
        );

        if (agg == null || agg.isEmpty()) {
            return Optional.empty();
        }

        for (TapMetricsGaugeAggregation x : agg) {
            result.put(x.bucket(), x);
        }

        return Optional.of(result);
    }

    public Optional<List<Bus>> findBusesOfTap(String tapName) {
        List<Bus> buses = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM tap_buses WHERE tap_name = :tap_name AND updated_at > :last_seen")
                        .bind("tap_name", tapName)
                        .bind("last_seen", DateTime.now().minusMinutes(1))
                        .mapTo(Bus.class)
                        .list()
        );

        return buses == null || buses.isEmpty() ? Optional.empty() : Optional.of(buses);
    }

    public Optional<List<Channel>> findChannelsOfBus(long busId) {
        List<Channel> channels = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM bus_channels WHERE bus_id = :bus_id AND updated_at > :last_seen")
                        .bind("bus_id", busId)
                        .bind("last_seen", DateTime.now().minusHours(1))
                        .mapTo(Channel.class)
                        .list()
        );

        return channels == null || channels.isEmpty() ? Optional.empty() : Optional.of(channels);
    }

    public Optional<List<Capture>> findCapturesOfTap(String tapName) {
        List<Capture> captures = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM tap_captures WHERE tap_name = :tap_name AND updated_at > :last_seen")
                        .bind("tap_name", tapName)
                        .bind("last_seen", DateTime.now().minusMinutes(1))
                        .mapTo(Capture.class)
                        .list()
        );

        return captures == null || captures.isEmpty() ? Optional.empty() : Optional.of(captures);
    }

}
