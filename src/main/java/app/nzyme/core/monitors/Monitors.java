package app.nzyme.core.monitors;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.monitors.db.MonitorEntry;
import app.nzyme.core.util.filters.Filters;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;

import java.sql.Types;
import java.util.List;
import java.util.UUID;

public class Monitors {

    private final NzymeNode nzyme;

    public Monitors(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public long countAllOfType(MonitorType type,
                               UUID organizationId,
                               UUID tenantId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM monitors WHERE type = :type AND " +
                                "organization_id = :organization_id AND tenant_id = :tenant_id")
                        .bind("type", type)
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .mapTo(Long.class)
                        .one()
        );
    }

    public List<MonitorEntry> findAllOfType(MonitorType type,
                                            UUID organizationId,
                                            UUID tenantId,
                                            int offset,
                                            int limit) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM monitors WHERE type = :type AND " +
                                "organization_id = :organization_id AND tenant_id = :tenant_id " +
                                "ORDER BY name ASC LIMIT :limit OFFSET :offset")
                        .bind("type", type)
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("offset", offset)
                        .bind("limit", limit)
                        .mapTo(MonitorEntry.class)
                        .list()
        );
    }

    public void createMonitor(MonitorType type,
                              String name,
                              String description,
                              @Nullable List<UUID> taps,
                              int triggerCondition,
                              int interval,
                              Filters filters,
                              UUID organizationId,
                              UUID tenantId) {
        String[] tapsArray;
        if (taps != null) {
            tapsArray = taps.stream()
                    .map(UUID::toString)
                    .toArray(String[]::new);
        } else {
            tapsArray = null;
        }

        ObjectMapper om = new ObjectMapper();
        String filtersString;
        try {
            filtersString = om.writeValueAsString(filters);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO monitors(uuid, organization_id, tenant_id, enabled, type, " +
                                "name, description, taps, trigger_condition, interval, filters, created_at, " +
                                "updated_at) VALUES(:uuid, :organization_id, :tenant_id, true, :type, :name, " +
                                ":description, :taps, :trigger_condition, :interval, :filters::jsonb, NOW(), NOW())")
                        .bind("uuid", UUID.randomUUID())
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("type", type)
                        .bind("name", name)
                        .bind("description", description)
                        .bindBySqlType("taps", tapsArray, Types.ARRAY)
                        .bind("trigger_condition", triggerCondition)
                        .bind("interval", interval)
                        .bind("filters", filtersString)
                        .execute()
        );

    }

}
