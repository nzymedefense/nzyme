package app.nzyme.core.gnss;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.database.generic.LatLonResult;
import app.nzyme.core.gnss.db.GNSSDoubleBucket;
import app.nzyme.core.gnss.db.GNSSIntegerBucket;
import app.nzyme.core.gnss.db.GNSSSatelliteInView;
import app.nzyme.core.shared.db.GenericIntegerHistogramEntry;
import app.nzyme.core.util.Bucketing;
import app.nzyme.core.util.TimeRange;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class GNSS {

    private final NzymeNode nzyme;

    public GNSS(NzymeNode nzyme) {
        this.nzyme = nzyme;
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
                                "(WHERE constellation = 'GPS')) AS gps, " +
                                "ROUND(AVG(maximum_time_deviation_ms) FILTER " +
                                "(WHERE constellation = 'GLONASS')) AS glonass, " +
                                "ROUND(AVG(maximum_time_deviation_ms) FILTER " +
                                "(WHERE constellation = 'BeiDou')) AS beidou, " +
                                "ROUND(AVG(maximum_time_deviation_ms) FILTER " +
                                "(WHERE constellation = 'Galileo')) AS galileo " +
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
                                "(WHERE constellation = 'GPS')) AS gps, " +
                                "ROUND(AVG(maximum_fix_satellite_count) FILTER " +
                                "(WHERE constellation = 'GLONASS')) AS glonass, " +
                                "ROUND(AVG(maximum_fix_satellite_count) FILTER " +
                                "(WHERE constellation = 'BeiDou')) AS beidou, " +
                                "ROUND(AVG(maximum_fix_satellite_count) FILTER " +
                                "(WHERE constellation = 'Galileo')) AS galileo " +
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

    public List<GNSSIntegerBucket> getAltitudeHistogram(TimeRange timeRange,
                                                        Bucketing.BucketingConfiguration bucketing,
                                                        List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT date_trunc(:date_trunc, timestamp) AS bucket, " +
                                "ROUND(AVG(maximum_altitude_meters) FILTER " +
                                "(WHERE constellation = 'GPS')) AS gps, " +
                                "ROUND(AVG(maximum_altitude_meters) FILTER " +
                                "(WHERE constellation = 'GLONASS')) AS glonass, " +
                                "ROUND(AVG(maximum_altitude_meters) FILTER " +
                                "(WHERE constellation = 'BeiDou')) AS beidou, " +
                                "ROUND(AVG(maximum_altitude_meters) FILTER " +
                                "(WHERE constellation = 'Galileo')) AS galileo " +
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
                                "(WHERE constellation = 'GPS')) AS gps, " +
                                "ROUND(AVG(maximum_satellites_in_view_count) FILTER " +
                                "(WHERE constellation = 'GLONASS')) AS glonass, " +
                                "ROUND(AVG(maximum_satellites_in_view_count) FILTER " +
                                "(WHERE constellation = 'BeiDou')) AS beidou, " +
                                "ROUND(AVG(maximum_satellites_in_view_count) FILTER " +
                                "(WHERE constellation = 'Galileo')) AS galileo " +
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

}
