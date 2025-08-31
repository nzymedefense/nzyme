package app.nzyme.core.gnss;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.database.generic.LatLonResult;
import app.nzyme.core.gnss.db.*;
import app.nzyme.core.gnss.db.monitoring.GNSSMonitoringRuleEntry;
import app.nzyme.core.shared.db.GenericIntegerHistogramEntry;
import app.nzyme.core.taps.Tap;
import app.nzyme.core.util.Bucketing;
import app.nzyme.core.util.TimeRange;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GNSS {

    private final NzymeNode nzyme;
    private final ObjectMapper om;

    public GNSS(NzymeNode nzyme) {
        this.nzyme = nzyme;
        this.om = new ObjectMapper();
    }

    public List<LatLonResult> getRecordedCoordinates(Constellation constellation,
                                                     TimeRange timeRange,
                                                     List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT DISTINCT round(c.lat::numeric, 5)::double precision AS lat, " +
                                "round(c.lon::numeric, 5)::double precision AS lon " +
                                "FROM gnss_constellations AS gnss " +
                                "CROSS JOIN LATERAL jsonb_to_recordset(gnss.positions) " +
                                "AS c (lat double precision, lon double precision) " +
                                "WHERE gnss.constellation = :constellation " +
                                "AND timestamp >= :tr_from AND timestamp <= :tr_to " +
                                "AND tap_uuid IN (<taps>)")
                        .bind("constellation", constellation)
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bindList("taps", taps)
                        .mapTo(LatLonResult.class)
                        .list()
        );
    }

    public List<GNSSSatelliteInView> findAllSatellitesInView(TimeRange timeRange, List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT gnss.constellation, s.prn, (AVG(s.snr))::int as snr, " +
                                "(AVG(s.azimuth_degrees))::int AS azimuth_degrees, " +
                                "(AVG(s.elevation_degrees))::int AS elevation_degrees, " +
                                "BOOL_OR(gnss.fix_satellites IS NOT NULL AND EXISTS (" +
                                "SELECT 1 FROM jsonb_array_elements_text(gnss.fix_satellites) AS e(val) " +
                                "WHERE e.val::int = s.prn)) AS used_for_fix, " +
                                "MAX(gnss.timestamp) as last_seen " +
                                "FROM gnss_constellations AS gnss " +
                                "JOIN gnss_sats_in_view AS s ON s.gnss_constellation_id = gnss.id " +
                                "WHERE s.prn IS NOT NULL AND timestamp >= :tr_from AND timestamp <= :tr_to " +
                                "AND tap_uuid IN (<taps>) " +
                                "GROUP BY gnss.constellation, s.prn " +
                                "ORDER BY constellation, prn")
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bindList("taps", taps)
                        .mapTo(GNSSSatelliteInView.class)
                        .list()
        );
    }
    public List<GNSSIntegerBucket> getTimeDeviationHistogram(TimeRange timeRange,
                                                             Bucketing.BucketingConfiguration bucketing,
                                                             List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT date_trunc(:date_trunc, timestamp) AS bucket, " +
                                "ROUND(AVG(maximum_time_deviation_ms) FILTER " +
                                "(WHERE constellation = 'GPS'))::int AS gps, " +
                                "ROUND(AVG(maximum_time_deviation_ms) FILTER " +
                                "(WHERE constellation = 'GLONASS'))::int AS glonass, " +
                                "ROUND(AVG(maximum_time_deviation_ms) FILTER " +
                                "(WHERE constellation = 'BeiDou'))::int AS beidou, " +
                                "ROUND(AVG(maximum_time_deviation_ms) FILTER " +
                                "(WHERE constellation = 'Galileo'))::int AS galileo " +
                                "FROM gnss_constellations WHERE timestamp >= :tr_from AND timestamp <= :tr_to " +
                                "AND tap_uuid IN (<taps>) " +
                                "GROUP BY bucket ORDER BY bucket DESC")
                        .bind("date_trunc", bucketing.type().getDateTruncName())
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bindList("taps", taps)
                        .mapTo(GNSSIntegerBucket.class)
                        .list()
        );
    }

    public GNSSConstellationDistances getConstellationDistancesFromTap(TimeRange timeRange, Tap tap) {
        if (tap.latitude() == null || tap.longitude() == null) {
            throw new RuntimeException("Tap has no location information assigned.");
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("WITH distances AS (" +
                                "SELECT g.constellation," +
                                "2 * 6371000.0 * ASIN(" +
                                "  SQRT(" +
                                "    POWER(SIN(RADIANS((:lat - (elem->>'lat')::double precision) / 2.0)), 2)" +
                                "    + COS(RADIANS(:lat))" +
                                "      * COS(RADIANS((elem->>'lat')::double precision))\n" +
                                "      * POWER(SIN(RADIANS((:lon - (elem->>'lon')::double precision) / 2.0)), 2)" +
                                "  )) AS distance_m " +
                                "FROM gnss_constellations AS g " +
                                "CROSS JOIN LATERAL jsonb_array_elements(g.positions) AS elem " +
                                "WHERE tap_uuid = :tap_uuid AND timestamp >= :tr_from AND timestamp <= :tr_to) " +
                                "SELECT MAX(distance_m) FILTER (WHERE constellation = 'GPS') AS gps, " +
                                "MAX(distance_m) FILTER (WHERE constellation = 'GLONASS') AS glonass, " +
                                "MAX(distance_m) FILTER (WHERE constellation = 'BeiDou')  AS beidou, " +
                                "MAX(distance_m) FILTER (WHERE constellation = 'Galileo') AS galileo " +
                                "FROM distances;")
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bind("tap_uuid", tap.uuid())
                        .bind("lat", tap.latitude())
                        .bind("lon", tap.longitude())
                        .mapTo(GNSSConstellationDistances.class)
                        .one()
        );
    }

    public List<GNSSDoubleBucket> getPdopHistogram(TimeRange timeRange,
                                                   Bucketing.BucketingConfiguration bucketing,
                                                   List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT date_trunc(:date_trunc, timestamp) AS bucket, " +
                                "AVG(maximum_pdop) FILTER (WHERE constellation = 'GPS') AS gps, " +
                                "AVG(maximum_pdop) FILTER (WHERE constellation = 'GLONASS') AS glonass, " +
                                "AVG(maximum_pdop) FILTER (WHERE constellation = 'BeiDou') AS beidou, " +
                                "AVG(maximum_pdop) FILTER (WHERE constellation = 'Galileo') AS galileo " +
                                "FROM gnss_constellations WHERE timestamp >= :tr_from AND timestamp <= :tr_to " +
                                "AND tap_uuid IN (<taps>) " +
                                "GROUP BY bucket ORDER BY bucket DESC")
                        .bind("date_trunc", bucketing.type().getDateTruncName())
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bindList("taps", taps)
                        .mapTo(GNSSDoubleBucket.class)
                        .list()
        );
    }

    public List<GNSSIntegerBucket> getFixSatelliteHistogram(TimeRange timeRange,
                                                           Bucketing.BucketingConfiguration bucketing,
                                                           List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT date_trunc(:date_trunc, timestamp) AS bucket, " +
                                "ROUND(AVG(maximum_fix_satellite_count) FILTER " +
                                "(WHERE constellation = 'GPS'))::int AS gps, " +
                                "ROUND(AVG(maximum_fix_satellite_count) FILTER " +
                                "(WHERE constellation = 'GLONASS'))::int AS glonass, " +
                                "ROUND(AVG(maximum_fix_satellite_count) FILTER " +
                                "(WHERE constellation = 'BeiDou'))::int AS beidou, " +
                                "ROUND(AVG(maximum_fix_satellite_count) FILTER " +
                                "(WHERE constellation = 'Galileo'))::int AS galileo " +
                                "FROM gnss_constellations WHERE timestamp >= :tr_from AND timestamp <= :tr_to " +
                                "AND tap_uuid IN (<taps>) " +
                                "GROUP BY bucket ORDER BY bucket DESC")
                        .bind("date_trunc", bucketing.type().getDateTruncName())
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bindList("taps", taps)
                        .mapTo(GNSSIntegerBucket.class)
                        .list()
        );
    }

    public List<GNSSStringBucket> getFixStatusHistogram(TimeRange timeRange,
                                                        Bucketing.BucketingConfiguration bucketing,
                                                        List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery(
                                "SELECT date_trunc(:date_trunc, timestamp) AS bucket, " +

                                        // GPS
                                        "CASE " +
                                        "  WHEN BOOL_OR( EXISTS ( " +
                                        "         SELECT 1 FROM jsonb_array_elements_text(fixes) AS v(val) " +
                                        "         WHERE v.val = 'Fix3D' " +
                                        "       ) ) FILTER (WHERE constellation = 'GPS') THEN 'Fix3D' " +
                                        "  WHEN BOOL_OR( EXISTS ( " +
                                        "         SELECT 1 FROM jsonb_array_elements_text(fixes) AS v(val) " +
                                        "         WHERE v.val = 'Fix2D' " +
                                        "       ) ) FILTER (WHERE constellation = 'GPS') THEN 'Fix2D' " +
                                        "  ELSE 'NoFix' " +
                                        "END AS gps, " +

                                        // GLONASS
                                        "CASE " +
                                        "  WHEN BOOL_OR( EXISTS ( " +
                                        "         SELECT 1 FROM jsonb_array_elements_text(fixes) AS v(val) " +
                                        "         WHERE v.val = 'Fix3D' " +
                                        "       ) ) FILTER (WHERE constellation = 'GLONASS') THEN 'Fix3D' " +
                                        "  WHEN BOOL_OR( EXISTS ( " +
                                        "         SELECT 1 FROM jsonb_array_elements_text(fixes) AS v(val) " +
                                        "         WHERE v.val = 'Fix2D' " +
                                        "       ) ) FILTER (WHERE constellation = 'GLONASS') THEN 'Fix2D' " +
                                        "  ELSE 'NoFix' " +
                                        "END AS glonass, " +

                                        // BeiDou
                                        "CASE " +
                                        "  WHEN BOOL_OR( EXISTS ( " +
                                        "         SELECT 1 FROM jsonb_array_elements_text(fixes) AS v(val) " +
                                        "         WHERE v.val = 'Fix3D' " +
                                        "       ) ) FILTER (WHERE constellation = 'BeiDou') THEN 'Fix3D' " +
                                        "  WHEN BOOL_OR( EXISTS ( " +
                                        "         SELECT 1 FROM jsonb_array_elements_text(fixes) AS v(val) " +
                                        "         WHERE v.val = 'Fix2D' " +
                                        "       ) ) FILTER (WHERE constellation = 'BeiDou') THEN 'Fix2D' " +
                                        "  ELSE 'NoFix' " +
                                        "END AS beidou, " +

                                        // Galileo
                                        "CASE " +
                                        "  WHEN BOOL_OR( EXISTS ( " +
                                        "         SELECT 1 FROM jsonb_array_elements_text(fixes) AS v(val) " +
                                        "         WHERE v.val = 'Fix3D' " +
                                        "       ) ) FILTER (WHERE constellation = 'Galileo') THEN 'Fix3D' " +
                                        "  WHEN BOOL_OR( EXISTS ( " +
                                        "         SELECT 1 FROM jsonb_array_elements_text(fixes) AS v(val) " +
                                        "         WHERE v.val = 'Fix2D' " +
                                        "       ) ) FILTER (WHERE constellation = 'Galileo') THEN 'Fix2D' " +
                                        "  ELSE 'NoFix' " +
                                        "END AS galileo " +

                                        "FROM gnss_constellations " +
                                        "WHERE timestamp >= :tr_from AND timestamp <= :tr_to " +
                                        "AND tap_uuid IN (<taps>) " +
                                        "GROUP BY bucket " +
                                        "ORDER BY bucket DESC"
                        )
                        .bind("date_trunc", bucketing.type().getDateTruncName())
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bindList("taps", taps)
                        .mapTo(GNSSStringBucket.class)
                        .list()
        );
    }

    public List<GNSSIntegerBucket> getAltitudeHistogram(TimeRange timeRange,
                                                        Bucketing.BucketingConfiguration bucketing,
                                                        List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT date_trunc(:date_trunc, timestamp) AS bucket, " +
                                "ROUND(AVG(maximum_altitude_meters) FILTER " +
                                "(WHERE constellation = 'GPS'))::int AS gps, " +
                                "ROUND(AVG(maximum_altitude_meters) FILTER " +
                                "(WHERE constellation = 'GLONASS'))::int AS glonass, " +
                                "ROUND(AVG(maximum_altitude_meters) FILTER " +
                                "(WHERE constellation = 'BeiDou'))::int AS beidou, " +
                                "ROUND(AVG(maximum_altitude_meters) FILTER " +
                                "(WHERE constellation = 'Galileo'))::int AS galileo " +
                                "FROM gnss_constellations WHERE timestamp >= :tr_from AND timestamp <= :tr_to " +
                                "AND tap_uuid IN (<taps>) " +
                                "GROUP BY bucket ORDER BY bucket DESC")
                        .bind("date_trunc", bucketing.type().getDateTruncName())
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bindList("taps", taps)
                        .mapTo(GNSSIntegerBucket.class)
                        .list()
        );
    }

    public List<GNSSIntegerBucket> getSatellitesInViewHistogram(TimeRange timeRange,
                                                                Bucketing.BucketingConfiguration bucketing,
                                                                List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT date_trunc(:date_trunc, timestamp) AS bucket, " +
                                "ROUND(AVG(maximum_satellites_in_view_count) FILTER " +
                                "(WHERE constellation = 'GPS'))::int AS gps, " +
                                "ROUND(AVG(maximum_satellites_in_view_count) FILTER " +
                                "(WHERE constellation = 'GLONASS'))::int AS glonass, " +
                                "ROUND(AVG(maximum_satellites_in_view_count) FILTER " +
                                "(WHERE constellation = 'BeiDou'))::int AS beidou, " +
                                "ROUND(AVG(maximum_satellites_in_view_count) FILTER " +
                                "(WHERE constellation = 'Galileo'))::int AS galileo " +
                                "FROM gnss_constellations WHERE timestamp >= :tr_from AND timestamp <= :tr_to " +
                                "AND tap_uuid IN (<taps>) " +
                                "GROUP BY bucket ORDER BY bucket DESC")
                        .bind("date_trunc", bucketing.type().getDateTruncName())
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bindList("taps", taps)
                        .mapTo(GNSSIntegerBucket.class)
                        .list()
        );
    }

    public List<GenericIntegerHistogramEntry> getPrnSnrHistogram(Constellation constellation,
                                                                 int prn,
                                                                 TimeRange timeRange,
                                                                 Bucketing.BucketingConfiguration bucketing,
                                                                 List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT date_trunc(:date_trunc, timestamp) AS bucket, " +
                                "ROUND(AVG(snr)) AS value FROM gnss_constellations AS gnss " +
                                "LEFT JOIN public.gnss_sats_in_view AS sats on gnss.id = sats.gnss_constellation_id " +
                                "WHERE gnss.constellation = :constellation AND sats.prn = :prn " +
                                "AND timestamp >= :tr_from AND timestamp <= :tr_to AND tap_uuid IN (<taps>) " +
                                "GROUP BY bucket ORDER BY bucket DESC")
                        .bind("constellation", constellation)
                        .bind("prn", prn)
                        .bind("date_trunc", bucketing.type().getDateTruncName())
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bindList("taps", taps)
                        .mapTo(GenericIntegerHistogramEntry.class)
                        .list()
        );
    }

    public List<GenericIntegerHistogramEntry> getPrnElevationHistogram(Constellation constellation,
                                                                       int prn,
                                                                       TimeRange timeRange,
                                                                       Bucketing.BucketingConfiguration bucketing,
                                                                       List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT date_trunc(:date_trunc, timestamp) AS bucket, " +
                                "ROUND(AVG(elevation_degrees)) AS value FROM gnss_constellations AS gnss " +
                                "LEFT JOIN public.gnss_sats_in_view AS sats on gnss.id = sats.gnss_constellation_id " +
                                "WHERE gnss.constellation = :constellation AND sats.prn = :prn " +
                                "AND timestamp >= :tr_from AND timestamp <= :tr_to AND tap_uuid IN (<taps>) " +
                                "GROUP BY bucket ORDER BY bucket DESC")
                        .bind("constellation", constellation)
                        .bind("prn", prn)
                        .bind("date_trunc", bucketing.type().getDateTruncName())
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bindList("taps", taps)
                        .mapTo(GenericIntegerHistogramEntry.class)
                        .list()
        );
    }

    public List<GenericIntegerHistogramEntry> getPrnAzimuthHistogram(Constellation constellation,
                                                                     int prn,
                                                                     TimeRange timeRange,
                                                                     Bucketing.BucketingConfiguration bucketing,
                                                                     List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT date_trunc(:date_trunc, timestamp) AS bucket, " +
                                "ROUND(AVG(azimuth_degrees)) AS value FROM gnss_constellations AS gnss " +
                                "LEFT JOIN public.gnss_sats_in_view AS sats on gnss.id = sats.gnss_constellation_id " +
                                "WHERE gnss.constellation = :constellation AND sats.prn = :prn " +
                                "AND timestamp >= :tr_from AND timestamp <= :tr_to AND tap_uuid IN (<taps>) " +
                                "GROUP BY bucket ORDER BY bucket DESC")
                        .bind("constellation", constellation)
                        .bind("prn", prn)
                        .bind("date_trunc", bucketing.type().getDateTruncName())
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bindList("taps", taps)
                        .mapTo(GenericIntegerHistogramEntry.class)
                        .list()
        );
    }

    public void writeMonitorRule(String name,
                                 @Nullable String description,
                                 Map<String, List<Object>> conditions,
                                 List<UUID> taps,
                                 UUID organizationId,
                                 UUID tenantId) {

        String conditionsJson;
        String tapsJson;
        try {
            conditionsJson = om.writeValueAsString(conditions);

            if (taps.isEmpty()) {
                tapsJson = null;
            } else {
                tapsJson  = om.writeValueAsString(taps);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO gnss_monitoring_rules(uuid, organization_id, tenant_id, name, " +
                                "description, conditions, taps, updated_at, created_at) VALUES(:uuid, " +
                                ":organization_id, :tenant_id, :name, :description, :conditions::jsonb, " +
                                ":taps::jsonb, NOW(), NOW())")
                        .bind("uuid", UUID.randomUUID())
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("name", name)
                        .bind("description", description)
                        .bind("conditions", conditionsJson)
                        .bind("taps", tapsJson)
                        .execute()
        );
    }

    public long countAllMonitoringRulesOfTenant(UUID organizationId, UUID tenantId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM gnss_monitoring_rules " +
                                "WHERE organization_id = :organization_id AND tenant_id = :tenant_id")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .mapTo(Long.class)
                        .one()
        );
    }

    public List<GNSSMonitoringRuleEntry> findAllMonitoringRulesOfTenant(UUID organizationId,
                                                                        UUID tenantId,
                                                                        int limit,
                                                                        int offset) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM gnss_monitoring_rules " +
                                "WHERE organization_id = :organization_id AND tenant_id = :tenant_id " +
                                "LIMIT :limit OFFSET :offset")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .mapTo(GNSSMonitoringRuleEntry.class)
                        .list()
        );
    }

}
