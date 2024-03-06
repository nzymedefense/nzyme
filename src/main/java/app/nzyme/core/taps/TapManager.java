package app.nzyme.core.taps;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.db.TapBasedSignalStrengthResult;
import app.nzyme.core.floorplans.db.TenantLocationEntry;
import app.nzyme.core.floorplans.db.TenantLocationFloorEntry;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import com.google.auto.value.AutoValue;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import app.nzyme.core.taps.metrics.BucketSize;
import app.nzyme.core.taps.metrics.TapMetrics;
import app.nzyme.core.taps.metrics.TapMetricsGauge;
import app.nzyme.core.rest.resources.taps.reports.CapturesReport;
import app.nzyme.core.rest.resources.taps.reports.ChannelReport;
import app.nzyme.core.rest.resources.taps.reports.StatusReport;
import app.nzyme.core.taps.metrics.TapMetricsGaugeAggregation;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.*;
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
        ).scheduleAtFixedRate(this::retentionCleanMetrics, 0, 5, TimeUnit.MINUTES);
    }

    public void registerTapStatus(StatusReport report, String remoteAddress, UUID tapUUID) {
        LOG.debug("Registering report from tap [{}].", tapUUID);

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE taps SET version = :version, clock = :clock, " +
                                "processed_bytes_total = :processed_bytes_total, " +
                                "processed_bytes_average = :processed_bytes_average, memory_total = :memory_total, " +
                                "memory_free = :memory_free, memory_used = :memory_used, cpu_load = :cpu_load, " +
                                "remote_address = :remote_address, last_report = NOW() WHERE uuid = :uuid")
                        .bind("version", report.version())
                        .bind("clock", report.timestamp())
                        .bind("processed_bytes_total", report.processedBytes().total())
                        .bind("processed_bytes_average", report.processedBytes().average())
                        .bind("memory_total", report.systemMetrics().memoryTotal())
                        .bind("memory_free", report.systemMetrics().memoryFree())
                        .bind("memory_used", report.systemMetrics().memoryTotal()-report.systemMetrics().memoryFree())
                        .bind("cpu_load", report.systemMetrics().cpuLoad())
                        .bind("remote_address", remoteAddress)
                        .bind("uuid", tapUUID)
                        .execute()
        );

        // Register captures.
        for (CapturesReport capture : report.captures()) {
            long captureCount = nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery("SELECT COUNT(*) AS count FROM tap_captures " +
                                    "WHERE interface = :interface AND tap_uuid = :tap_uuid")
                            .bind("interface", capture.interfaceName())
                            .bind("tap_uuid",  tapUUID)
                            .mapTo(Long.class)
                            .one()
            );

            if (captureCount == 0) {
                nzyme.getDatabase().withHandle(handle ->
                    handle.createUpdate("INSERT INTO tap_captures(tap_uuid, interface, capture_type, is_running, " +
                            "received, dropped_buffer, dropped_interface, updated_at, created_at) VALUES(:tap_uuid, " +
                            ":interface, :capture_type, :is_running, :received, :dropped_buffer, :dropped_interface, " +
                            "NOW(), NOW())")
                            .bind("tap_uuid", tapUUID)
                            .bind("interface", capture.interfaceName())
                            .bind("capture_type", capture.captureType())
                            .bind("is_running", capture.isRunning())
                            .bind("received", capture.received())
                            .bind("dropped_buffer", capture.droppedBuffer())
                            .bind("dropped_interface", capture.droppedInterface())
                            .execute()
                );
            } else {
                nzyme.getDatabase().withHandle(handle ->
                        handle.createUpdate("UPDATE tap_captures SET capture_type = :capture_type, " +
                                "is_running = :is_running, received = :received, dropped_buffer = :dropped_buffer, " +
                                "dropped_interface = :dropped_interface, updated_at = NOW() " +
                                "WHERE tap_uuid = :tap_uuid AND interface = :interface")
                                .bind("capture_type", capture.captureType())
                                .bind("is_running", capture.isRunning())
                                .bind("received", capture.received())
                                .bind("dropped_buffer", capture.droppedBuffer())
                                .bind("dropped_interface", capture.droppedInterface())
                                .bind("tap_uuid", tapUUID)
                                .bind("interface", capture.interfaceName())
                                .execute()
                );
            }
        }

        // Register bus.
        long busCount = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) AS count FROM tap_buses WHERE tap_uuid = :tap_uuid")
                        .bind("tap_uuid", tapUUID)
                        .mapTo(Long.class)
                        .one()
        );

        if (busCount == 0) {
            nzyme.getDatabase().useHandle(handle ->
                    handle.createUpdate("INSERT INTO tap_buses(tap_uuid, name, created_at, updated_at) " +
                            "VALUES(:tap_uuid, :name, NOW(), NOW())")
                            .bind("tap_uuid", tapUUID)
                            .bind("name", report.bus().name())
                            .execute()
            );
        } else {
            nzyme.getDatabase().useHandle(handle ->
                    handle.createUpdate("UPDATE tap_buses SET updated_at = NOW() WHERE tap_uuid = :tap_uuid AND name = :name")
                            .bind("tap_uuid", tapUUID)
                            .bind("name", report.bus().name())
                            .execute()
            );
        }

        Long busId = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT id FROM tap_buses WHERE tap_uuid = :tap_uuid")
                        .bind("tap_uuid", tapUUID)
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
                                ":throughput_messages_average, NOW(), NOW())")
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
                                "updated_at = NOW() WHERE bus_id = :bus_id AND name = :name")
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
                                .execute()
                );
            }
        }

        // Metrics
        for (Map.Entry<String, Long> metric : report.gaugesLong().entrySet()) {
            writeGauge(tapUUID, metric.getKey(), metric.getValue(), report.timestamp());
        }

        // Additional metrics.
        writeGauge(tapUUID, "system.captures.throughput_bit_sec", report.processedBytes().average()*8/10, report.timestamp());
        writeGauge(tapUUID, "os.memory.bytes_used", report.systemMetrics().memoryTotal()-report.systemMetrics().memoryFree(), report.timestamp());
        writeGauge(tapUUID, "os.cpu.load.percent", report.systemMetrics().cpuLoad(), report.timestamp());
    }

    private void writeGauge(UUID tapUUID, String metricName, Long metricValue, DateTime timestamp) {
        writeGauge(tapUUID, metricName, metricValue.doubleValue(), timestamp);
    }

    private void writeGauge(UUID tapUUID, String metricName, Double metricValue, DateTime timestamp) {
        nzyme.getDatabase().withHandle(handle ->
                handle.createUpdate("INSERT INTO tap_metrics_gauges(tap_uuid, metric_name, metric_value, created_at) " +
                                "VALUES(:tap_uuid, :metric_name, :metric_value, NOW())")
                .bind("tap_uuid", tapUUID)
                .bind("metric_name", metricName)
                .bind("metric_value", metricValue)
                .execute()
        );
    }

    private void retentionCleanMetrics() {
        nzyme.getDatabase().useHandle(handle -> {
            handle.createUpdate("DELETE FROM tap_metrics_gauges WHERE created_at < :created_at")
                    .bind("created_at", DateTime.now().minusHours(24))
                    .execute();
        });
    }


    public List<Tap> findAllTapsByUUIDs(List<UUID> tapIds) {
        if (tapIds.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM taps WHERE uuid IN (<ids>) " +
                                "ORDER BY name ASC")
                        .bindList("ids", tapIds)
                        .mapTo(Tap.class)
                        .list()
        );
    }

    public List<Tap> findAllTapsOfAllUsers() {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM taps")
                        .mapTo(Tap.class)
                        .list()
        );
    }

    public List<Tap> findAllTapsOfOrganization(UUID organizationUUID) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM taps WHERE organization_id = :organization_uuid")
                        .bind("organization_uuid", organizationUUID)
                        .mapTo(Tap.class)
                        .list()
        );
    }

    public List<Tap> findAllTapsOfTenant(UUID organizationUUID, UUID tenantUUID) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM taps WHERE organization_id = :organization_uuid " +
                                "AND tenant_id = :tenant_uuid")
                        .bind("organization_uuid", organizationUUID)
                        .bind("tenant_uuid", tenantUUID)
                        .mapTo(Tap.class)
                        .list()
        );
    }

    public List<Tap> findAllTapsOnFloor(UUID organizationUUID, UUID tenantUUID, UUID locationUUID, UUID floorUUID) {
        if (organizationUUID == null && tenantUUID == null) {
            return nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery("SELECT * FROM taps " +
                                    "WHERE location_uuid = :location_uuid AND floor_uuid = :floor_uuid")
                            .bind("location_uuid", locationUUID)
                            .bind("floor_uuid", floorUUID)
                            .mapTo(Tap.class)
                            .list()
            );
        }

        if (organizationUUID != null && tenantUUID == null) {
            // Org Admin.
            return nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery("SELECT * FROM taps WHERE organization_id = :organization_uuid " +
                                    "AND location_uuid = :location_uuid AND floor_uuid = :floor_uuid")
                            .bind("organization_uuid", organizationUUID)
                            .bind("location_uuid", locationUUID)
                            .bind("floor_uuid", floorUUID)
                            .mapTo(Tap.class)
                            .list()
            );
        }

        // Tenant user.
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM taps WHERE organization_id = :organization_uuid " +
                                "AND tenant_id = :tenant_uuid AND location_uuid = :location_uuid " +
                                "AND floor_uuid = :floor_uuid")
                        .bind("organization_uuid", organizationUUID)
                        .bind("tenant_uuid", tenantUUID)
                        .bind("location_uuid", locationUUID)
                        .bind("floor_uuid", floorUUID)
                        .mapTo(Tap.class)
                        .list()
        );
    }

    public List<UUID> allTapUUIDsAccessibleByUser(AuthenticatedUser user) {
        List<UUID> allTaps = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT uuid FROM taps")
                        .mapTo(UUID.class)
                        .list()
        );

        if (user.isSuperAdministrator()) {
            return allTaps;
        }

        if (user.isOrganizationAdministrator()) {
            if (user.getOrganizationId() == null) {
                throw new RuntimeException("NULL organization ID.");
            }

            return nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery("SELECT uuid FROM taps WHERE organization_id = :organization_id")
                            .bind("organization_id", user.getOrganizationId())
                            .mapTo(UUID.class)
                            .list()
            );
        }

        // User is tenant user, check that it has required fields.
        if (user.getOrganizationId() == null || user.getTenantId() == null) {
            throw new RuntimeException("NULL organization or tenant ID.");
        }

        // Get taps from user permissions.
        List<UUID> tapPermissions = nzyme.getAuthenticationService().findTapPermissionsOfUser(user.getUserId());

        if (tapPermissions.isEmpty()) {
            // User has no specific tap permissions. Check if all taps are allowed.
            if (user.accessAllTenantTaps) {
                return nzyme.getDatabase().withHandle(handle ->
                        handle.createQuery("SELECT uuid FROM taps " +
                                        "WHERE organization_id = :organization_id AND tenant_id = :tenant_id")
                                .bind("organization_id", user.getOrganizationId())
                                .bind("tenant_id", user.getTenantId())
                                .mapTo(UUID.class)
                                .list()
                );
            } else {
                // User is not allowed to use all taps and has no specific tap permissions.
                return Collections.emptyList();
            }
        } else {
            // Return only specifically allowed taps.
            List<UUID> validatedTaps = Lists.newArrayList();
            for (UUID permission : tapPermissions) {
                if (allTaps.contains(permission)) {
                    validatedTaps.add(permission);
                }
            }

            return validatedTaps;
        }
    }

    public List<UUID> allTapUUIDsAccessibleByScope(@Nullable UUID organizationId, @Nullable UUID tenantId) {
        List<Tap> taps = Lists.newArrayList();
        if (organizationId == null && tenantId == null) {
            // Super Admin.
            taps = findAllTapsOfAllUsers();
        } else if (organizationId != null && tenantId == null) {
            // Organization Admin.
            taps = findAllTapsOfOrganization(organizationId);
        } else {
            // Tenant User.
            taps = findAllTapsOfTenant(organizationId, tenantId);
        }

        List<UUID> tapUUIDs = Lists.newArrayList();
        for (Tap tap : taps) {
            tapUUIDs.add(tap.uuid());
        }

        return tapUUIDs;
    }

    public Optional<Tap> findTap(UUID uuid) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM taps WHERE uuid = :uuid")
                        .bind("uuid", uuid)
                        .mapTo(Tap.class)
                        .findOne()
        );
    }

    public TapMetrics findMetricsOfTap(UUID tapUUID) {
        List<TapMetricsGauge> gauges = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT DISTINCT ON (metric_name) metric_name, tap_uuid, metric_value, created_at " +
                        "FROM tap_metrics_gauges WHERE tap_uuid = :tap_uuid AND created_at > :created_at " +
                        "ORDER BY metric_name, created_at DESC")
                        .bind("tap_uuid", tapUUID)
                        .bind("created_at", DateTime.now().minusMinutes(1))
                        .mapTo(TapMetricsGauge.class)
                        .list()
        );

        return TapMetrics.create(tapUUID, gauges);
    }

    public Optional<Map<DateTime, TapMetricsGaugeAggregation>> findMetricsHistogram(UUID tapUUID, String metricName, int hours, BucketSize bucketSize) {
        Map<DateTime, TapMetricsGaugeAggregation> result = Maps.newHashMap();

        List<TapMetricsGaugeAggregation> agg = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT AVG(metric_value) AS average, MAX(metric_value) AS maximum, " +
                                "MIN(metric_value) AS minimum, date_trunc(:bucket_size, created_at) AS bucket " +
                                "FROM tap_metrics_gauges WHERE tap_uuid = :tap_uuid AND metric_name = :metric_name " +
                                "AND created_at > :created_at GROUP BY bucket ORDER BY bucket DESC")
                        .bind("bucket_size", bucketSize.toString().toLowerCase())
                        .bind("tap_uuid", tapUUID)
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

    public Optional<List<Bus>> findBusesOfTap(UUID tapUUID) {
        List<Bus> buses = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM tap_buses WHERE tap_uuid = :tap_uuid AND updated_at > :last_seen")
                        .bind("tap_uuid", tapUUID)
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

    public Optional<List<Capture>> findCapturesOfTap(UUID tapUUID) {
        List<Capture> captures = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM tap_captures WHERE tap_uuid = :tap_uuid AND updated_at > :last_seen")
                        .bind("tap_uuid", tapUUID)
                        .bind("last_seen", DateTime.now().minusMinutes(1))
                        .mapTo(Capture.class)
                        .list()
        );

        return captures == null || captures.isEmpty() ? Optional.empty() : Optional.of(captures);
    }

    public Optional<TenantLocationFloorEntry> guessFloorOfSignalSource(List<TapBasedSignalStrengthResult> signalStrengths) {
        Map<TapPositionKey, Integer> floorSummaries = Maps.newHashMap();
        Map<TapPositionKey, Integer> floorTapCounts = Maps.newHashMap();

        for (TapBasedSignalStrengthResult ss : signalStrengths) {
            Tap tap = findTap(ss.tapUuid()).orElseThrow();

            if (tap.locationId() == null || tap.floorId() == null) {
                continue;
            }

            TapPositionKey key = TapPositionKey.create(tap.locationId(), tap.floorId());

            Integer signalStrength = floorSummaries.get(key);
            if (signalStrength != null) {
                floorSummaries.put(key, (int) (signalStrength+ss.signalStrength()));
            } else {
                floorSummaries.put(key, (int) ss.signalStrength());
            }

            Integer tapCount = floorTapCounts.get(key);
            if (tapCount != null) {
                floorTapCounts.put(key, tapCount+1);
            } else {
                floorTapCounts.put(key, 1);
            }
        }

        // Filter out all floors that have less than three placed taps.
        Map<TapPositionKey, Integer> filteredFloorSummaries = Maps.newHashMap();
        for (Map.Entry<TapPositionKey, Integer> tc : floorTapCounts.entrySet()) {
            if (tc.getValue() < 3) {
                continue;
            }

            Integer floorSummary = floorSummaries.get(tc.getKey());
            if (floorSummary == null) {
                continue;
            }

            filteredFloorSummaries.put(tc.getKey(), floorSummary);
        }

        if (filteredFloorSummaries.isEmpty()) {
            return Optional.empty();
        }
        
        int highest = -255;
        TapPositionKey result = null;
        for (Map.Entry<TapPositionKey, Integer> summary : filteredFloorSummaries.entrySet()) {
            if (summary.getValue() > highest) {
                highest = summary.getValue();
                result = summary.getKey();
            }
        }

        if (result == null) {
            return Optional.empty();
        }

        return Optional.of(
                nzyme.getAuthenticationService()
                        .findFloorOfTenantLocation(result.locationId(), result.floorId())
                        .orElseThrow()
        );
    }

    @AutoValue
    public static abstract class TapPositionKey {
        public abstract UUID locationId();
        public abstract UUID floorId();

        public static TapPositionKey create(UUID locationId, UUID floorId) {
            return builder()
                    .locationId(locationId)
                    .floorId(floorId)
                    .build();
        }

        public static Builder builder() {
            return new AutoValue_TapManager_TapPositionKey.Builder();
        }

        @AutoValue.Builder
        public abstract static class Builder {
            public abstract Builder locationId(UUID locationId);

            public abstract Builder floorId(UUID floorId);

            public abstract TapPositionKey build();
        }
    }
}
