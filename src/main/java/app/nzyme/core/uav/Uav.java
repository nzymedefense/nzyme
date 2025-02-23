package app.nzyme.core.uav;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.shared.Classification;
import app.nzyme.core.uav.db.UavEntry;
import app.nzyme.core.util.TimeRange;

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

}
