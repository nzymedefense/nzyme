package app.nzyme.core.monitors;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.monitors.db.MonitorEntry;
import app.nzyme.core.util.filters.Filters;
import org.joda.time.DateTime;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;

import java.sql.Types;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Monitors {

    private final NzymeNode nzyme;

    public Monitors(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public Optional<MonitorEntry> find(UUID id) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT *, " +
                                "COALESCE(last_event > NOW() - (interval * INTERVAL '1 minute'), false) AS alerted " +
                                "FROM monitors WHERE uuid = :uuid")
                        .bind("uuid", id)
                        .mapTo(MonitorEntry.class)
                        .findOne()
        );
    }

    public List<MonitorEntry> findAllMonitorsOfAllTenants() {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT *, " +
                                "COALESCE(last_event > NOW() - (interval * INTERVAL '1 minute'), false) AS alerted " +
                                "FROM monitors")
                        .mapTo(MonitorEntry.class)
                        .list()
        );
    }

    public long countAllMonitorsOfType(MonitorType type, UUID organizationId, UUID tenantId) {
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

    public List<MonitorEntry> findAllMonitorsOfType(MonitorType type,
                                                    UUID organizationId,
                                                    UUID tenantId,
                                                    int offset,
                                                    int limit) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT *, " +
                                "COALESCE(last_event > NOW() - (interval * INTERVAL '1 minute'), false) AS alerted " +
                                "FROM monitors WHERE type = :type AND organization_id = :organization_id " +
                                "AND tenant_id = :tenant_id ORDER BY name ASC LIMIT :limit OFFSET :offset")
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
                              int lookback,
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
            filtersString = om.writeValueAsString(filters.filters());
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO monitors(uuid, organization_id, tenant_id, enabled, type, " +
                                "name, description, taps, trigger_condition, interval, lookback, filters, " +
                                "created_at, updated_at) VALUES(:uuid, :organization_id, :tenant_id, true, :type, " +
                                ":name, :description, :taps, :trigger_condition, :interval, :lookback, " +
                                ":filters::jsonb, NOW(), NOW())")
                        .bind("uuid", UUID.randomUUID())
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("type", type)
                        .bind("name", name)
                        .bind("description", description)
                        .bindBySqlType("taps", tapsArray, Types.ARRAY)
                        .bind("trigger_condition", triggerCondition)
                        .bind("interval", interval)
                        .bind("lookback", lookback)
                        .bind("filters", filtersString)
                        .execute()
        );

    }

    public void updateMonitorMetaInformation(UUID id,
                                             String name,
                                             String description,
                                             int triggerCondition,
                                             int interval,
                                             int lookback) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE MONITORS set name = :name, description = :description, " +
                                "trigger_condition = :trigger_condition, interval = :interval, " +
                                "lookback = :lookback WHERE uuid = :uuid")
                        .bind("uuid", id)
                        .bind("name", name)
                        .bind("description", description)
                        .bind("trigger_condition", triggerCondition)
                        .bind("interval", interval)
                        .bind("lookback", lookback)
                        .execute()
        );

    }

    public void updateMonitorFilterInformation(UUID id, @Nullable List<UUID> taps, Filters filters) {
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
            filtersString = om.writeValueAsString(filters.filters());
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE MONITORS SET taps = :taps, filters = :filters::jsonb " +
                                "WHERE uuid = :uuid")
                        .bind("uuid", id)
                        .bindBySqlType("taps", tapsArray, Types.ARRAY)
                        .bind("filters", filtersString)
                        .execute()
        );

    }

    public void deleteMonitor(UUID id) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM monitors WHERE uuid = :uuid")
                        .bind("uuid", id)
                        .execute()
        );
    }

    public void setLastEventOfMonitor(UUID id, DateTime lastEvent) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE monitors SET last_event = :last_event WHERE uuid = :uuid")
                        .bind("uuid", id)
                        .bind("last_event", lastEvent)
                        .execute()
        );
    }

    public void setLastExecutionTimeOfMonitor(UUID id, DateTime lastRun) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE monitors SET last_run = :last_run WHERE uuid = :uuid")
                        .bind("uuid", id)
                        .bind("last_run", lastRun)
                        .execute()
        );
    }

    public void setMonitorStatus(UUID id, MonitorStatus status) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE monitors SET status = :status WHERE uuid = :uuid")
                        .bind("uuid", id)
                        .bind("status", status)
                        .execute()
        );
    }

    public void onTapDeleted(UUID tapId) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE monitors SET taps = ARRAY_REMOVE(taps, :tap_id)")
                        .bind("tap_id", tapId.toString())
                        .execute()
        );
    }

}
