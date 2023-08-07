package app.nzyme.core.detection.alerts;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.detection.alerts.db.DetectionAlertAttributeEntry;
import app.nzyme.core.detection.alerts.db.DetectionAlertEntry;
import app.nzyme.core.dot11.db.monitoring.MonitoredSSID;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.core.statement.Query;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class DetectionAlertService {

    private static final Logger LOG = LogManager.getLogger(DetectionAlertService.class);

    private final NzymeNode nzyme;

    public DetectionAlertService(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public void raiseAlert(@Nullable UUID organizationId,
                           @Nullable UUID tenantId,
                           @Nullable UUID dot11MonitoredNetworkId,
                           @Nullable UUID tapId,
                           DetectionType detectionType,
                           Subsystem subsystem,
                           String details,
                           Map<String, String> attributes,
                           String[] comparisonAttributeKeys) {
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

        if (alertWithComparisonChecksumExists(comparisonChecksum)) {
            // This alert has been raised in the past.
            LOG.debug("Alert of type [{}] with checksum [{}] is not new. Updating last_seen.",
                    detectionType, comparisonChecksum);

            nzyme.getDatabase().withHandle(handle ->
                    handle.createUpdate("UPDATE detection_alerts SET last_seen = NOW() " +
                                    "WHERE comparison_checksum = :comparison_checksum")
                            .bind("comparison_checksum", comparisonChecksum)
                            .execute()
            );

            return;
        }

        long alertId = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("INSERT INTO detection_alerts(uuid, organization_id, tenant_id, " +
                                "dot11_monitored_network_id, tap_id, detection_type, subsystem, comparison_checksum, " +
                                "details, created_at, last_seen) VALUES(:uuid, :organization_id, :tenant_id, " +
                                ":dot11_monitored_network_id, :tap_id, :detection_type, :subsystem, " +
                                ":comparison_checksum, :details, NOW(), NOW()) RETURNING id")
                        .bind("uuid", UUID.randomUUID())
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("dot11_monitored_network_id", dot11MonitoredNetworkId)
                        .bind("tap_id", tapId)
                        .bind("detection_type", detectionType)
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
    }

    public List<DetectionAlertEntry> findAllAlerts(@Nullable UUID organizationId,
                                                   @Nullable UUID tenantId,
                                                   int limit,
                                                   int offset) {
        return nzyme.getDatabase().withHandle(handle -> {
            Query query;
            if (organizationId == null && tenantId == null) {
                // Super Admin.
                query = handle.createQuery("SELECT * FROM detection_alerts ORDER BY last_seen DESC " +
                                "LIMIT :limit OFFSET :offset")
                        .bind("limit", limit)
                        .bind("offset", offset);
            } else if (organizationId != null && tenantId == null) {
                // Organization Admin.
                query = handle.createQuery("SELECT * FROM detection_alerts " +
                                "WHERE organization_id = :organization_id " +
                                "ORDER BY last_seen DESC LIMIT :limit OFFSET :offset")
                        .bind("organization_id", organizationId)
                        .bind("limit", limit)
                        .bind("offset", offset);
            } else {
                // Tenant User.
                query = handle.createQuery("SELECT * FROM detection_alerts " +
                                "WHERE organization_id = :organization_id AND tenant_id = :tenant_id " +
                                "ORDER BY last_seen DESC LIMIT :limit OFFSET :offset")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("limit", limit)
                        .bind("offset", offset);
            }

            return query.mapTo(DetectionAlertEntry.class).list();
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

    public boolean alertWithComparisonChecksumExists(String comparisonChecksum) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM detection_alerts " +
                                "WHERE comparison_checksum = :comparison_checksum")
                        .bind("comparison_checksum", comparisonChecksum)
                        .mapTo(Long.class)
                        .one()
        ) > 0;
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

        source.append(detectionType)
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
