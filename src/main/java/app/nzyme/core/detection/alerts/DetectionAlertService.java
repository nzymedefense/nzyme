package app.nzyme.core.detection.alerts;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.detection.alerts.db.DetectionAlertAttributeEntry;
import app.nzyme.core.detection.alerts.db.DetectionAlertEntry;
import app.nzyme.core.detection.alerts.db.DetectionAlertTimelineEntry;
import app.nzyme.core.events.types.DetectionEvent;
import app.nzyme.plugin.Subsystem;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.core.statement.Query;
import org.joda.time.DateTime;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class DetectionAlertService {

    private static final Logger LOG = LogManager.getLogger(DetectionAlertService.class);

    public static final int ACTIVE_THRESHOLD_MINUTES = 5;

    private final NzymeNode nzyme;

    public DetectionAlertService(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public int countAllDetectionTypes(@Nullable Subsystem subsystem) {
        int count = 0;
        for (DetectionType t : DetectionType.values()) {
            if (subsystem == null || t.getSubsystem().equals(subsystem)) {
                count++;
            }
        }

        return count;
    }

    public List<DetectionType> findAllDetectionTypes(@Nullable Subsystem subsystem,
                                                     int limit,
                                                     int offset) {
        List<DetectionType> result = Lists.newArrayList();
        for (DetectionType t : DetectionType.values()) {
            if (subsystem == null || t.getSubsystem().equals(subsystem)) {
                result.add(t);
            }
        }

        return result.stream()
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
    }

    public void raiseAlert(UUID organizationId,
                           UUID tenantId,
                           @Nullable UUID dot11MonitoredNetworkId,
                           @Nullable UUID tapId,
                           DetectionType detectionType,
                           Subsystem subsystem,
                           String details,
                           Map<String, String> attributes,
                           String[] comparisonAttributeKeys,
                           @Nullable Float signalStrength) {
        if (organizationId == null || tenantId == null) {
            throw new RuntimeException("Detection alerts must have organization and tenant UUID set.");
        }

        TreeMap<String, String> comparisonAttributes = Maps.newTreeMap();
        for (String key : comparisonAttributeKeys) {
            String value = attributes.get(key);
            if (value == null) {
                throw new RuntimeException("Comparison attribute key [" + key + "] not in attributes." );
            }
            comparisonAttributes.put(key, value);
        }

        String comparisonChecksum = buildChecksum(
                organizationId,
                tenantId,
                dot11MonitoredNetworkId,
                detectionType,
                subsystem,
                comparisonAttributes
        );

        Optional<DetectionAlertEntry> existingAlert = findAlertWithComparisonChecksum(comparisonChecksum);
        if (existingAlert.isPresent()) {
            // This alert has been raised in the past.
            LOG.debug("Alert of type [{}] with checksum [{}] is not new. Updating.",
                    detectionType.name(), comparisonChecksum);

            nzyme.getDatabase().withHandle(handle ->
                    handle.createUpdate("UPDATE detection_alerts SET last_seen = NOW(), is_resolved = false " +
                                    "WHERE id = :id")
                            .bind("id", existingAlert.get().id())
                            .execute()
            );

            // Update alert attributes.
            for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                nzyme.getDatabase().useHandle(handle ->
                        handle.createUpdate("UPDATE detection_alert_attributes " +
                                        "SET attribute_value = :attribute_value " +
                                        "WHERE detection_alert_id = :detection_alert_id " +
                                        "AND attribute_key = :attribute_key")
                                .bind("detection_alert_id", existingAlert.get().id())
                                .bind("attribute_key", attribute.getKey())
                                .bind("attribute_value", attribute.getValue())
                                .execute()
                );
            }

            /*
             * Write alert timeline.
             *
             * To show when a potentially re-activated alert was seen, we store a timeline of alerts. A new timeline
             * entry starts when the alert is not currently active. If the alert is currently active, the current
             * timeline entry is extended. This allows for very easy querying.
             */
            if (existingAlert.get().lastSeen()
                    .isAfter(DateTime.now().minusMinutes(DetectionAlertService.ACTIVE_THRESHOLD_MINUTES))) {
                // Active alert. Extend existing timeline entry.
                nzyme.getDatabase().useHandle(handle ->
                        handle.createUpdate("UPDATE detection_alert_timeline SET seen_to = NOW() " +
                                        "WHERE id = (SELECT MAX(id) FROM detection_alert_timeline " +
                                        "WHERE detection_alert_id = :detection_alert_id)")
                                .bind("detection_alert_id", existingAlert.get().id())
                                .execute()
                );
            } else {
                // Inactive alert. Create new timeline entry.
                createAlertTimelineEntry(existingAlert.get().id());

                // Create event.
                nzyme.getEventEngine().processEvent(
                        DetectionEvent.create(existingAlert.get().uuid(), detectionType, details, DateTime.now()),
                        organizationId,
                        tenantId
                );
            }

            return;
        }

        // New alert / not re-triggered.
        UUID alertUUID = UUID.randomUUID();
        long alertId = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("INSERT INTO detection_alerts(uuid, organization_id, tenant_id, " +
                                "dot11_monitored_network_id, tap_id, detection_type, subsystem, comparison_checksum, " +
                                "details, created_at, last_seen) VALUES(:uuid, :organization_id, :tenant_id, " +
                                ":dot11_monitored_network_id, :tap_id, :detection_type, :subsystem, " +
                                ":comparison_checksum, :details, NOW(), NOW()) RETURNING id")
                        .bind("uuid", alertUUID)
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("dot11_monitored_network_id", dot11MonitoredNetworkId)
                        .bind("tap_id", tapId)
                        .bind("detection_type", detectionType.name())
                        .bind("subsystem", subsystem)
                        .bind("comparison_checksum", comparisonChecksum)
                        .bind("details", details)
                        .mapTo(Long.class)
                        .one()
        );

        for (Map.Entry<String, String> attribute : attributes.entrySet()) {
            nzyme.getDatabase().useHandle(handle ->
                    handle.createUpdate("INSERT INTO detection_alert_attributes(detection_alert_id, " +
                                    "attribute_key, attribute_value) VALUES(:detection_alert_id, :attribute_key, " +
                                    ":attribute_value)")
                            .bind("detection_alert_id", alertId)
                            .bind("attribute_key", attribute.getKey())
                            .bind("attribute_value", attribute.getValue())
                            .execute()
            );
        }

        // Write initial alert timeline entry. See comment in re-raised alert update above.
        createAlertTimelineEntry(alertId);

        // Create event.
        nzyme.getEventEngine().processEvent(
                DetectionEvent.create(alertUUID, detectionType, details, DateTime.now()),
                organizationId,
                tenantId
        );
    }

    private String buildSubsystemSelectorFragment(@Nullable Subsystem subsystem) {
        if (subsystem != null) {
            switch (subsystem) {
                case DOT11 -> { return "subsystem = 'DOT11'"; }
                case ETHERNET -> { return "subsystem = 'ETHERNET'"; }
                case BLUETOOTH -> { return"subsystem = 'BLUETOOTH'"; }
                default -> { return "1=1"; }
            }
        } else {
            return "1=1";
        }
    }

    public List<DetectionAlertEntry> findAllAlerts(@Nullable UUID organizationId,
                                                   @Nullable UUID tenantId,
                                                   @Nullable Subsystem subsystem,
                                                   int limit,
                                                   int offset) {
        String subsystemSelector = buildSubsystemSelectorFragment(subsystem);

        return nzyme.getDatabase().withHandle(handle -> {
            Query query;
            if (organizationId == null && tenantId == null) {
                // Super Admin.
                query = handle.createQuery("SELECT * FROM detection_alerts WHERE " + subsystemSelector + " " +
                                "ORDER BY last_seen DESC LIMIT :limit OFFSET :offset")
                        .bind("limit", limit)
                        .bind("offset", offset);
            } else if (organizationId != null && tenantId == null) {
                // Organization Admin.
                query = handle.createQuery("SELECT * FROM detection_alerts " +
                                "WHERE organization_id = :organization_id AND " + subsystemSelector + " " +
                                "ORDER BY last_seen DESC LIMIT :limit OFFSET :offset")
                        .bind("organization_id", organizationId)
                        .bind("limit", limit)
                        .bind("offset", offset);
            } else {
                // Tenant User.
                query = handle.createQuery("SELECT * FROM detection_alerts " +
                                "WHERE organization_id = :organization_id " +
                                "AND tenant_id = :tenant_id AND " + subsystemSelector + " " +
                                "ORDER BY last_seen DESC LIMIT :limit OFFSET :offset")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("limit", limit)
                        .bind("offset", offset);
            }

            return query.mapTo(DetectionAlertEntry.class).list();
        });
    }

    public List<DetectionAlertEntry> findAllActiveAlertsOfMonitoredNetwork(UUID monitoredNetworkId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM detection_alerts " +
                                "WHERE dot11_monitored_network_id = :network_id " +
                                "AND last_seen > :cutoff AND is_resolved = false")
                        .bind("network_id", monitoredNetworkId)
                        .bind("cutoff", DateTime.now().minusMinutes(ACTIVE_THRESHOLD_MINUTES))
                        .mapTo(DetectionAlertEntry.class)
                        .list()
        );
    }

    public long countAlerts(UUID organizationId, UUID tenantId, @Nullable Subsystem subsystem) {
        String subsystemSelector = buildSubsystemSelectorFragment(subsystem);

        return nzyme.getDatabase().withHandle(handle -> {
            Query query;
            if (organizationId == null && tenantId == null) {
                // Super Admin.
                query = handle.createQuery("SELECT COUNT(*) FROM detection_alerts WHERE " + subsystemSelector);
            } else if (organizationId != null && tenantId == null) {
                // Organization Admin.
                query = handle.createQuery("SELECT COUNT(*) FROM detection_alerts " +
                                "WHERE organization_id = :organization_id AND " + subsystemSelector)
                        .bind("organization_id", organizationId);
            } else {
                // Tenant User.
                query = handle.createQuery("SELECT COUNT(*) FROM detection_alerts " +
                                "WHERE organization_id = :organization_id AND tenant_id = :tenant_id " +
                                "AND " + subsystemSelector)
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId);
            }

            return query.mapTo(Long.class).one();
        });
    }

    public long countActiveAlerts(UUID organizationId, UUID tenantId, @Nullable Subsystem subsystem) {
        String subsystemSelector = buildSubsystemSelectorFragment(subsystem);

        return nzyme.getDatabase().withHandle(handle -> {
            Query query;
            if (organizationId == null && tenantId == null) {
                // Super Admin.
                query = handle.createQuery("SELECT COUNT(*) FROM detection_alerts " +
                                "WHERE is_resolved = false AND last_seen > :cutoff AND " + subsystemSelector)
                        .bind("cutoff", DateTime.now().minusMinutes(ACTIVE_THRESHOLD_MINUTES));
            } else if (organizationId != null && tenantId == null) {
                // Organization Admin.
                query = handle.createQuery("SELECT COUNT(*) FROM detection_alerts " +
                                "WHERE organization_id = :organization_id " +
                                "AND is_resolved = false AND last_seen > :cutoff AND " + subsystemSelector)
                        .bind("organization_id", organizationId)
                        .bind("cutoff", DateTime.now().minusMinutes(ACTIVE_THRESHOLD_MINUTES));
            } else {
                // Tenant User.
                query = handle.createQuery("SELECT COUNT(*) FROM detection_alerts " +
                                "WHERE organization_id = :organization_id AND tenant_id = :tenant_id " +
                                "AND is_resolved = false AND last_seen > :cutoff AND " + subsystemSelector)
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("cutoff", DateTime.now().minusMinutes(ACTIVE_THRESHOLD_MINUTES));
            }

            return query.mapTo(Long.class).one();
        });
    }


    public Optional<DetectionAlertEntry> findAlert(UUID uuid,
                                                   @Nullable UUID organizationId,
                                                   @Nullable UUID tenantId) {
        return nzyme.getDatabase().withHandle(handle -> {
            Query query;
            if (organizationId == null && tenantId == null) {
                // Super Admin.
                query = handle.createQuery("SELECT * FROM detection_alerts WHERE uuid = :uuid")
                        .bind("uuid", uuid);
            } else if (organizationId != null && tenantId == null) {
                // Organization Admin.
                query = handle.createQuery("SELECT * FROM detection_alerts " +
                                "WHERE organization_id = :organization_id AND uuid = :uuid")
                        .bind("organization_id", organizationId)
                        .bind("uuid", uuid);
            } else {
                // Tenant User.
                query = handle.createQuery("SELECT * FROM detection_alerts " +
                                "WHERE organization_id = :organization_id AND tenant_id = :tenant_id " +
                                "AND uuid = :uuid")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("uuid", uuid);
            }

            return query.mapTo(DetectionAlertEntry.class).findOne();
        });
    }

    public List<DetectionAlertAttributeEntry> findAlertAttributes(long alertId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM detection_alert_attributes " +
                                "WHERE detection_alert_id = :detection_alert_id")
                        .bind("detection_alert_id", alertId)
                        .mapTo(DetectionAlertAttributeEntry.class)
                        .list()
        );
    }

    public void delete(UUID uuid) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM detection_alerts WHERE uuid = :uuid")
                        .bind("uuid", uuid)
                        .execute()
        );
    }

    public void markAlertAsResolved(UUID uuid) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE detection_alerts SET is_resolved = true WHERE uuid = :uuid")
                        .bind("uuid", uuid)
                        .execute()
        );
    }

    public List<DetectionAlertTimelineEntry> findAlertTimeline(long alertId,
                                                               int limit,
                                                               int offset) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM detection_alert_timeline " +
                                "WHERE detection_alert_id = :detection_alert_id " +
                                "ORDER BY seen_to DESC " +
                                "LIMIT :limit OFFSET :offset")
                        .bind("detection_alert_id", alertId)
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .mapTo(DetectionAlertTimelineEntry.class)
                        .list()
        );
    }

    public long countAlertTimelineEntries(long alertId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM detection_alert_timeline " +
                                "WHERE detection_alert_id = :detection_alert_id")
                        .bind("detection_alert_id", alertId)
                        .mapTo(Long.class)
                        .one()
        );
    }

    private Optional<DetectionAlertEntry> findAlertWithComparisonChecksum(String comparisonChecksum) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM detection_alerts " +
                                "WHERE comparison_checksum = :comparison_checksum")
                        .bind("comparison_checksum", comparisonChecksum)
                        .mapTo(DetectionAlertEntry.class)
                        .findOne()
        );
    }

    private void createAlertTimelineEntry(long alertId) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO detection_alert_timeline(detection_alert_id, seen_from, " +
                                "seen_to) VALUES(:detection_alert_id, NOW(), NOW())")
                        .bind("detection_alert_id", alertId)
                        .execute()
        );
    }

    private String buildChecksum(@Nullable UUID organizationId,
                                 @Nullable UUID tenantId,
                                 @Nullable UUID dot11MonitoredNetworkId,
                                 DetectionType detectionType,
                                 Subsystem subsystem,
                                 TreeMap<String, String> checksumAttributes) {
        StringBuilder source = new StringBuilder();

        if (organizationId != null) {
            source.append(organizationId);
        }

        if (tenantId != null) {
            source.append(tenantId);
        }

        if (dot11MonitoredNetworkId != null) {
            source.append(dot11MonitoredNetworkId);
        }

        source.append(detectionType.name())
                .append(subsystem);

        for (Map.Entry<String, String> attr : checksumAttributes.entrySet()) {
            source.append(attr.getKey())
                    .append(attr.getValue());
        }

        return Hashing.sha256()
                .hashString(source.toString(), StandardCharsets.UTF_8)
                .toString();
    }

}
