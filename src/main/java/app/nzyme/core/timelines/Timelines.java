package app.nzyme.core.timelines;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.timelines.db.TimelineActivityHistogram;
import app.nzyme.core.timelines.db.TimelineActivityHistogramBucket;
import app.nzyme.core.timelines.db.TimelineEventEntry;
import app.nzyme.core.util.Bucketing;
import app.nzyme.core.util.TimeRange;
import org.joda.time.DateTime;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.joda.JodaModule;

import java.util.*;

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
                                        List<String> excludedEventTypes,
                                        int gapThresholdMinutes) {
        String eventTypeExclusion = excludedEventTypes.isEmpty()
                ? ""
                : " AND event_type NOT IN (<excluded_event_types>) ";

        boolean excludeGone = excludedEventTypes.stream()
                .anyMatch("GONE"::equalsIgnoreCase);

        String goneCount = excludeGone
                ? ""
                : "  + COUNT(*) FILTER ( " +
                "        WHERE prev_timestamp IS NOT NULL " +
                "          AND timestamp - prev_timestamp > make_interval(mins => :gap_threshold_minutes) " +
                "    ) " +
                "  + CASE " +
                "        WHEN MAX(timestamp) IS NOT NULL " +
                "         AND LEAST(:tr_to::timestamptz, now()) - MAX(timestamp) > make_interval(mins => :gap_threshold_minutes) " +
                "        THEN 1 ELSE 0 " +
                "    END ";

        return nzyme.getDatabase().withHandle(handle -> {
            var query = handle.createQuery(
                            "WITH ordered AS ( " +
                                    "    SELECT event_type, timestamp, " +
                                    "           LAG(timestamp) OVER (ORDER BY timestamp) AS prev_timestamp " +
                                    "    FROM dot11_timeline_events " +
                                    "    WHERE organization_id = :organization_id AND tenant_id = :tenant_id " +
                                    "      AND address_type = :address_type AND address = :address " +
                                    "      AND timestamp >= :tr_from AND timestamp <= :tr_to " +
                                    ") " +
                                    "SELECT " +
                                    "    COUNT(*) FILTER (WHERE event_type <> 'MARK'" + eventTypeExclusion + ") " +
                                    goneCount +
                                    "    AS total " +
                                    "FROM ordered")
                    .bind("organization_id", organizationId)
                    .bind("tenant_id", tenantId)
                    .bind("address_type", addressType)
                    .bind("address", address)
                    .bind("tr_from", timeRange.from())
                    .bind("tr_to", timeRange.to())
                    .bind("gap_threshold_minutes", gapThresholdMinutes);

            if (!excludedEventTypes.isEmpty()) {
                query.bindList("excluded_event_types", excludedEventTypes);
            }

            return query.mapTo(Long.class).one();
        });
    }

    public List<TimelineEventEntry> findAllEventsOfAddress(UUID organizationId,
                                                           UUID tenantId,
                                                           TimelineAddressType addressType,
                                                           String address,
                                                           TimeRange timeRange,
                                                           List<String> excludedEventTypes,
                                                           int gapThresholdMinutes,
                                                           int limit,
                                                           int offset) {
        String eventTypeExclusion = excludedEventTypes.isEmpty()
                ? ""
                : " AND event_type NOT IN (<excluded_event_types>) ";

        boolean excludeGone = excludedEventTypes.stream()
                .anyMatch("GONE"::equalsIgnoreCase);

        String goneUnion = excludeGone
                ? ""
                : " UNION ALL SELECT * FROM gone_events " +
                " UNION ALL SELECT * FROM trailing_gap ";

        return nzyme.getDatabase().withHandle(handle -> {
            var query = handle.createQuery(
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
                                    "    WHERE event_type <> 'MARK'" + eventTypeExclusion + " " +
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
                                    goneUnion +
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
                    .bind("offset", offset);

            if (!excludedEventTypes.isEmpty()) {
                query.bindList("excluded_event_types", excludedEventTypes);
            }

            return query.mapTo(TimelineEventEntry.class).list();
        });
    }

    public TimelineActivityHistogram getEventTypeActivityHistogram(UUID organizationId,
                                                                   UUID tenantId,
                                                                   TimelineAddressType addressType,
                                                                   String address,
                                                                   TimeRange timeRange,
                                                                   List<String> excludedEventTypes,
                                                                   int gapThresholdMinutes,
                                                                   String timeZone) {
        Bucketing.BucketingConfiguration bucketing = Bucketing.getConfig(timeRange);
        String granularity = bucketing.type().getDateTruncName();

        String eventTypeExclusion = excludedEventTypes.isEmpty()
                ? ""
                : " AND event_type NOT IN (<excluded_event_types>) ";

        boolean excludeGone = excludedEventTypes.stream()
                .anyMatch("GONE"::equalsIgnoreCase);

        String goneUnion = excludeGone
                ? ""
                : " UNION ALL SELECT * FROM gone_events " +
                " UNION ALL SELECT * FROM trailing_gap ";

        LinkedHashMap<DateTime, Map<String, Long>> byBucket = nzyme.getDatabase().withHandle(handle -> {
            var query = handle.createQuery(
                            "WITH ordered AS ( " +
                                    "    SELECT event_type, timestamp, " +
                                    "           LAG(timestamp) OVER (ORDER BY timestamp) AS prev_timestamp " +
                                    "    FROM dot11_timeline_events " +
                                    "    WHERE organization_id = :organization_id AND tenant_id = :tenant_id " +
                                    "      AND address_type = :address_type AND address = :address " +
                                    "      AND timestamp >= :tr_from AND timestamp <= :tr_to " +
                                    "), " +
                                    "real_events AS ( " +
                                    "    SELECT date_trunc(:granularity, timestamp AT TIME ZONE :tz) AT TIME ZONE :tz AS bucket, event_type " +
                                    "    FROM ordered " +
                                    "    WHERE event_type <> 'MARK'" + eventTypeExclusion + " " +
                                    "), " +
                                    "gone_events AS ( " +
                                    "    SELECT date_trunc(:granularity, prev_timestamp AT TIME ZONE :tz) AT TIME ZONE :tz AS bucket, 'GONE' AS event_type " +
                                    "    FROM ordered " +
                                    "    WHERE prev_timestamp IS NOT NULL " +
                                    "      AND timestamp - prev_timestamp > make_interval(mins => :gap_threshold_minutes) " +
                                    "), " +
                                    "trailing_gap AS ( " +
                                    "    SELECT date_trunc(:granularity, MAX(timestamp) AT TIME ZONE :tz) AT TIME ZONE :tz AS bucket, 'GONE' AS event_type " +
                                    "    FROM ordered " +
                                    "    HAVING MAX(timestamp) IS NOT NULL " +
                                    "       AND LEAST(:tr_to::timestamptz, now()) - MAX(timestamp) > make_interval(mins => :gap_threshold_minutes) " +
                                    "), " +
                                    "combined AS ( " +
                                    "    SELECT * FROM real_events " +
                                    goneUnion +
                                    ") " +
                                    "SELECT bucket, event_type, COUNT(*) AS count " +
                                    "FROM combined " +
                                    "GROUP BY bucket, event_type " +
                                    "ORDER BY bucket")
                    .bind("organization_id", organizationId)
                    .bind("tenant_id", tenantId)
                    .bind("address_type", addressType)
                    .bind("address", address)
                    .bind("tr_from", timeRange.from())
                    .bind("tr_to", timeRange.to())
                    .bind("granularity", granularity)
                    .bind("tz", timeZone)
                    .bind("gap_threshold_minutes", gapThresholdMinutes);

            if (!excludedEventTypes.isEmpty()) {
                query.bindList("excluded_event_types", excludedEventTypes);
            }

            return query.reduceRows(
                    new LinkedHashMap<>(),
                    (acc, row) -> {
                        DateTime bucket = row.getColumn("bucket", DateTime.class);
                        String eventType = row.getColumn("event_type", String.class);
                        long count = row.getColumn("count", Long.class);
                        acc.computeIfAbsent(bucket, k -> new HashMap<>())
                                .merge(eventType, count, Long::sum);
                        return acc;
                    });
        });

        List<TimelineActivityHistogramBucket> buckets = new ArrayList<>(byBucket.size());
        Map<String, Long> totalsByEventType = new HashMap<>();
        long totalEvents = 0;

        for (Map.Entry<DateTime, Map<String, Long>> entry : byBucket.entrySet()) {
            Map<String, Long> counts = entry.getValue();
            long bucketTotal = 0;
            for (Map.Entry<String, Long> c : counts.entrySet()) {
                bucketTotal += c.getValue();
                totalsByEventType.merge(c.getKey(), c.getValue(), Long::sum);
            }
            totalEvents += bucketTotal;
            buckets.add(TimelineActivityHistogramBucket.create(entry.getKey(), bucketTotal, counts));
        }

        DateTime resolvedTo = timeRange.isAllTime()
                ? DateTime.now()
                : timeRange.to();

        DateTime resolvedFrom;
        if (timeRange.isAllTime()) {
            resolvedFrom = buckets.isEmpty() ? resolvedTo : buckets.get(0).bucket();
        } else {
            resolvedFrom = timeRange.from();
        }

        return TimelineActivityHistogram.create(
                resolvedFrom,
                resolvedTo,
                bucketing.type(),
                totalEvents,
                totalsByEventType,
                buckets
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
