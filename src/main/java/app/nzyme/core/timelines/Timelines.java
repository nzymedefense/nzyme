package app.nzyme.core.timelines;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.timelines.db.TimelineEventEntry;
import app.nzyme.core.util.TimeRange;
import org.joda.time.DateTime;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.joda.JodaModule;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class Timelines {

    public static final int EVENT_HORIZON_MINUTES = 5;

    private final NzymeNode nzyme;
    private final ObjectMapper objectMapper;

    public Timelines(NzymeNode nzyme) {
        this.nzyme = nzyme;
        this.objectMapper = JsonMapper.builder()
                .addModule(new JodaModule())
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }

    public void writeDot11TimelineEvent(UUID organizationId,
                                        UUID tenantId,
                                        TimelineAddressType addressType,
                                        String address,
                                        TimelineEventType eventType,
                                        Map<String, Object> eventDetails,
                                        DateTime timestamp) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO dot11_timeline_events(uuid, organization_id, tenant_id, " +
                                "address, address_type, event_type, event_details, timestamp) " +
                                "VALUES(:uuid, :organization_id, :tenant_id, :address, :address_type, " +
                                ":event_type, :event_details::jsonb, :timestamp)")
                        .bind("uuid", UUID.randomUUID())
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("address", address)
                        .bind("address_type", addressType)
                        .bind("event_type", eventType)
                        .bind("event_details", objectMapper.writeValueAsString(eventDetails))
                        .bind("timestamp", timestamp)
                        .execute()
        );
    }

    public long countAllEventsOfAddress(UUID organizationId,
                                        UUID tenantId,
                                        TimelineAddressType addressType,
                                        String address,
                                        TimeRange timeRange,
                                        int gapThresholdMinutes) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery(
                                "WITH ordered AS ( " +
                                        "    SELECT event_type, timestamp, " +
                                        "           LAG(timestamp) OVER (ORDER BY timestamp) AS prev_timestamp " +
                                        "    FROM dot11_timeline_events " +
                                        "    WHERE organization_id = :organization_id AND tenant_id = :tenant_id " +
                                        "      AND address_type = :address_type AND address = :address " +
                                        "      AND timestamp >= :tr_from AND timestamp <= :tr_to " +
                                        ") " +
                                        "SELECT " +
                                        "    COUNT(*) FILTER (WHERE event_type <> 'MARK') " +
                                        "  + COUNT(*) FILTER ( " +
                                        "        WHERE prev_timestamp IS NOT NULL " +
                                        "          AND timestamp - prev_timestamp > make_interval(mins => :gap_threshold_minutes) " +
                                        "    ) " +
                                        "  + CASE " +
                                        "        WHEN MAX(timestamp) IS NOT NULL " +
                                        "         AND LEAST(:tr_to::timestamptz, now()) - MAX(timestamp) > make_interval(mins => :gap_threshold_minutes) " +
                                        "        THEN 1 ELSE 0 " +
                                        "    END AS total " +
                                        "FROM ordered")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("address_type", addressType)
                        .bind("address", address)
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bind("gap_threshold_minutes", gapThresholdMinutes)
                        .mapTo(Long.class)
                        .one()
        );
    }

    public List<TimelineEventEntry> findAllEventsOfAddress(UUID organizationId,
                                                           UUID tenantId,
                                                           TimelineAddressType addressType,
                                                           String address,
                                                           TimeRange timeRange,
                                                           int gapThresholdMinutes,
                                                           int limit,
                                                           int offset) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery(
                                "WITH ordered AS ( " +
                                        "    SELECT id, uuid, organization_id, tenant_id, address, address_type, " +
                                        "           event_type, event_details, timestamp, " +
                                        "           LAG(timestamp) OVER (ORDER BY timestamp) AS prev_timestamp " +
                                        "    FROM dot11_timeline_events " +
                                        "    WHERE organization_id = :organization_id AND tenant_id = :tenant_id " +
                                        "      AND address_type = :address_type AND address = :address " +
                                        "      AND timestamp >= :tr_from AND timestamp <= :tr_to " +
                                        "), " +
                                        "real_events AS ( " +
                                        "    SELECT id, uuid, organization_id, tenant_id, address, address_type, " +
                                        "           event_type, event_details, timestamp " +
                                        "    FROM ordered " +
                                        "    WHERE event_type <> 'MARK' " +
                                        "), " +
                                        "gone_events AS ( " +
                                        "    SELECT " +
                                        "        NULL::bigint      AS id, " +
                                        "        gen_random_uuid() AS uuid, " +
                                        "        organization_id, " +
                                        "        tenant_id, " +
                                        "        address, " +
                                        "        address_type, " +
                                        "        'GONE'            AS event_type, " +
                                        "        jsonb_build_object( " +
                                        "            'gap_start', prev_timestamp, " +
                                        "            'gap_end',   timestamp, " +
                                        "            'minutes',   EXTRACT(EPOCH FROM (timestamp - prev_timestamp)) / 60.0, " +
                                        "            'ongoing',   false " +
                                        "        )                 AS event_details, " +
                                        "        prev_timestamp    AS timestamp " +
                                        "    FROM ordered " +
                                        "    WHERE prev_timestamp IS NOT NULL " +
                                        "      AND timestamp - prev_timestamp > make_interval(mins => :gap_threshold_minutes) " +
                                        "), " +
                                        "trailing_gap AS ( " +
                                        "    SELECT " +
                                        "        NULL::bigint                              AS id, " +
                                        "        gen_random_uuid()                         AS uuid, " +
                                        "        :organization_id::uuid                    AS organization_id, " +
                                        "        :tenant_id::uuid                          AS tenant_id, " +
                                        "        :address::text                            AS address, " +
                                        "        :address_type::text                       AS address_type, " +
                                        "        'GONE'                                    AS event_type, " +
                                        "        jsonb_build_object( " +
                                        "            'gap_start', MAX(timestamp), " +
                                        "            'gap_end',   LEAST(:tr_to::timestamptz, now()), " +
                                        "            'minutes',   EXTRACT(EPOCH FROM (LEAST(:tr_to::timestamptz, now()) - MAX(timestamp))) / 60.0, " +
                                        "            'ongoing',   true " +
                                        "        )                                         AS event_details, " +
                                        "        MAX(timestamp)                            AS timestamp " +
                                        "    FROM ordered " +
                                        "    HAVING MAX(timestamp) IS NOT NULL " +
                                        "       AND LEAST(:tr_to::timestamptz, now()) - MAX(timestamp) > make_interval(mins => :gap_threshold_minutes) " +
                                        ") " +
                                        "SELECT * FROM real_events " +
                                        "UNION ALL " +
                                        "SELECT * FROM gone_events " +
                                        "UNION ALL " +
                                        "SELECT * FROM trailing_gap " +
                                        "ORDER BY timestamp DESC " +
                                        "LIMIT :limit OFFSET :offset")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("address_type", addressType)
                        .bind("address", address)
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bind("gap_threshold_minutes", gapThresholdMinutes)
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .mapTo(TimelineEventEntry.class)
                        .list()
        );
    }

    public Optional<TimelineEventEntry> findLatestEventOfTypeAndAddress(UUID organizationId,
                                                                        UUID tenantId,
                                                                        TimelineAddressType addressType,
                                                                        String address,
                                                                        TimelineEventType eventType) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM dot11_timeline_events " +
                                "WHERE organization_id = :organization_id AND tenant_id = :tenant_id " +
                                "AND address_type = :address_type AND address = :address " +
                                "AND event_type = :event_type " +
                                "ORDER BY timestamp DESC LIMIT 1")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("address_type", addressType)
                        .bind("address", address)
                        .bind("event_type", eventType)
                        .mapTo(TimelineEventEntry.class)
                        .findOne()
        );
    }

}
