package app.nzyme.core.uav;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.shared.Classification;
import app.nzyme.core.uav.db.UavEntry;
import app.nzyme.core.uav.db.UavTimelineEntry;
import app.nzyme.core.uav.db.UavTypeEntry;
import app.nzyme.core.uav.types.UavTypeMatchType;
import app.nzyme.core.util.TimeRange;
import jakarta.validation.constraints.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Uav {

    private final NzymeNode nzyme;

    public Uav(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public long countAllUavs(TimeRange timeRange, List<UUID> taps) {
        if (taps.isEmpty()) {
            return 0;
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(DISTINCT identifier) FROM uavs " +
                                "WHERE last_seen >= :tr_from AND last_seen <= :tr_to " +
                                "AND tap_uuid IN (<taps>)")
                        .bindList("taps", taps)
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .mapTo(Long.class)
                        .one()
        );
    }

    public List<UavEntry> findAllUavs(TimeRange timeRange,
                                      int limit,
                                      int offset,
                                      UUID organizationId,
                                      UUID tenantId,
                                      List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT DISTINCT ON (u.identifier) *, c.classification AS classification FROM uavs AS u " +
                                "LEFT JOIN uavs_classifications AS c ON c.uav_identifier = u.identifier " +
                                "AND c.organization_id = :organization_id AND c.tenant_id = :tenant_id " +
                                "WHERE u.last_seen >= :tr_from AND u.last_seen <= :tr_to AND u.tap_uuid IN (<taps>) " +
                                "ORDER BY u.identifier, u.last_seen DESC " +
                                "LIMIT :limit OFFSET :offset")
                        .bindList("taps", taps)
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .mapTo(UavEntry.class)
                        .list()
        );
    }

    public Optional<UavEntry> findUav(String identifier,
                                      UUID organizationId,
                                      UUID tenantId,
                                      List<UUID> taps) {
        if (taps.isEmpty()) {
            return Optional.empty();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT *, c.classification AS classification FROM uavs AS u  " +
                                "LEFT JOIN uavs_classifications AS c ON c.uav_identifier = u.identifier " +
                                "AND c.organization_id = :organization_id AND c.tenant_id = :tenant_id " +
                                "WHERE u.identifier = :identifier AND u.tap_uuid IN (<taps>) " +
                                "ORDER BY u.last_seen DESC LIMIT 1")
                        .bind("identifier", identifier)
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bindList("taps", taps)
                        .mapTo(UavEntry.class)
                        .findOne()
        );
    }

    public long countTimelines(String identifier,
                               TimeRange timeRange,
                               @NotNull UUID organizationId,
                               @NotNull UUID tenantId,
                               List<UUID> taps) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM uavs_timelines AS t " +
                                "LEFT JOIN uavs AS u ON u.identifier = t.uav_identifier " +
                                "WHERE t.seen_to >= :tr_from AND t.seen_to <= :tr_to " +
                                "AND t.uav_identifier = :identifier AND t.organization_id = :organization_id " +
                                "AND t.tenant_id = :tenant_id AND u.tap_uuid IN (<taps>)")
                        .bind("identifier", identifier)
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bindList("taps", taps)
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .mapTo(Long.class)
                        .one()
        );
    }

    public List<UavTimelineEntry> findUavTimelines(String identifier,
                                                   TimeRange timeRange,
                                                   @NotNull UUID organizationId,
                                                   @NotNull UUID tenantId,
                                                   List<UUID> taps,
                                                   int limit,
                                                   int offset) {
        return nzyme.getDatabase().withHandle(handle ->
            handle.createQuery("SELECT t.seen_from, t.seen_to, t.uuid FROM uavs_timelines AS t " +
                            "LEFT JOIN uavs AS u ON u.identifier = t.uav_identifier " +
                            "WHERE t.seen_to >= :tr_from AND t.seen_to <= :tr_to " +
                            "AND t.uav_identifier = :identifier AND t.organization_id = :organization_id " +
                            "AND t.tenant_id = :tenant_id AND u.tap_uuid IN (<taps>) " +
                            "ORDER BY t.seen_to DESC " +
                            "LIMIT :limit OFFSET :offset")
                    .bind("identifier", identifier)
                    .bind("organization_id", organizationId)
                    .bind("tenant_id", tenantId)
                    .bindList("taps", taps)
                    .bind("limit", limit)
                    .bind("offset", offset)
                    .bind("tr_from", timeRange.from())
                    .bind("tr_to", timeRange.to())
                    .mapTo(UavTimelineEntry.class)
                    .list()
        );
    }

    public void setUavClassification(String identifier,
                                     UUID organizationId,
                                     UUID tenantId,
                                     Classification classification) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO uavs_classifications(uav_identifier, classification, " +
                                "organization_id, tenant_id) VALUES(:uav_identifier, :classification, " +
                                ":organization_id, :tenant_id) " +
                                "ON CONFLICT (uav_identifier, organization_id, tenant_id) " +
                                "DO UPDATE SET classification = EXCLUDED.classification")
                        .bind("uav_identifier", identifier)
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("classification", classification)
                        .execute()
        );
    }

    public long countAllCustomTypes(UUID organizationId, UUID tenantId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM uavs_types " +
                                "WHERE organization_id = :organization_id AND tenant_id = :tenant_id")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .mapTo(Long.class)
                        .one()
        );
    }

    public List<UavTypeEntry> findAllCustomTypes(UUID organizationId, UUID tenantId, int limit, int offset) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM uavs_types " +
                                "WHERE organization_id = :organization_id AND tenant_id = :tenant_id " +
                                "ORDER BY name DESC LIMIT :limit OFFSET :offset")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .mapTo(UavTypeEntry.class)
                        .list()
        );
    }

    public Optional<UavTypeEntry> findCustomType(UUID uuid, UUID organizationId, UUID tenantId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM uav_types WHERE uuid = :uuid " +
                                "AND organization_id = :organization_id AND tenant_id = :tenant_id")
                        .bind("uuid", uuid)
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .mapTo(UavTypeEntry.class)
                        .findOne()
        );
    }

    public void createCustomType(UUID organizationId,
                                 UUID tenantId,
                                 UavTypeMatchType matchType,
                                 String matchValue,
                                 Classification defaultClassification,
                                 String type,
                                 String name,
                                 String model) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO uavs_types(uuid, organization_id, tenant_id, match_type, " +
                                "match_value, default_classification, type, name, model, created_at, updated_at) " +
                                "VALUES(:uuid, :organization_id, :tenant_id, :match_type, :match_value, " +
                                ":default_classification, :type, :name, :model, NOW(), NOW())")
                        .bind("uuid", UUID.randomUUID())
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("match_type", matchType)
                        .bind("match_value", matchValue)
                        .bind("default_classification", defaultClassification)
                        .bind("type", type)
                        .bind("name", name)
                        .bind("model", model)
                        .execute()
        );
    }

    public void updateCustomType(long id,
                                 UavTypeMatchType matchType,
                                 String matchValue,
                                 Classification defaultClassification,
                                 String type,
                                 String name) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE uavs_types SET match_type = :match_type, " +
                                "match_value = :match_value, default_classification = :default_classification, " +
                                "type = :type, name = :name, updated_at = NOW() WHERE id = :id")
                        .bind("id", id)
                        .bind("match_type", matchType)
                        .bind("match_value", matchValue)
                        .bind("default_classification", defaultClassification)
                        .bind("type", type)
                        .bind("name", name)
                        .execute()
        );
    }
}