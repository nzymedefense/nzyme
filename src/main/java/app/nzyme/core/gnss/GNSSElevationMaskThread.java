package app.nzyme.core.gnss;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.gnss.db.elevationmasks.GNSSElevationMaskAzimuthBucket;
import app.nzyme.core.periodicals.Periodical;
import app.nzyme.core.util.MetricNames;
import com.codahale.metrics.Timer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.core.statement.PreparedBatch;

import java.util.List;

public class GNSSElevationMaskThread extends Periodical {

    private static final Logger LOG = LogManager.getLogger(GNSSElevationMaskThread.class);

    private final NzymeNode nzyme;
    private final Timer analysisTimer;
    private final Timer writeTimer;

    public GNSSElevationMaskThread(NzymeNode nzyme) {
        this.nzyme = nzyme;

        this.analysisTimer = nzyme.getMetrics().timer(MetricNames.GNSS_ELEVATION_MASK_ANALYIS_TIMER);
        this.writeTimer = nzyme.getMetrics().timer(MetricNames.GNSS_ELEVATION_MASK_WRITE_TIMER);
    }

    @Override
    protected void execute() {
        LOG.info("Calculating GNSS elevation masks.");

        try {
            final List<GNSSElevationMaskAzimuthBucket> masks;
            try (Timer.Context ignored = analysisTimer.time()) {
                masks = nzyme.getDatabase().withHandle(handle ->
                        handle.createQuery("WITH params AS (" +
                                        "SELECT now() - interval '3 days' AS window_start, now() AS window_end, " +
                                        "5::int AS bin_size_deg, 2.0::double precision AS el_min_deg, " +
                                        "25.0::double precision AS skyline_el_max_deg, 30::int AS min_total_samples, " +
                                        "30::int AS min_low_subset_samples)," +
                                        "filtered AS (" +
                                        "SELECT c.tap_uuid, " +
                                        "(floor(sats.azimuth_degrees / p.bin_size_deg) * p.bin_size_deg)::int " +
                                        "AS azimuth_bucket, sats.elevation_degrees::double precision AS elevation, " +
                                        "sats.average_sno::double precision AS sno FROM gnss_sats_in_view sats" +
                                        " JOIN gnss_constellations c ON sats.gnss_constellation_id = c.id " +
                                        "CROSS JOIN params p WHERE c.timestamp >= p.window_start " +
                                        "AND c.timestamp < p.window_end AND sats.azimuth_degrees IS NOT NULL " +
                                        "AND sats.elevation_degrees IS NOT NULL AND sats.average_sno IS NOT NULL " +
                                        "AND sats.elevation_degrees >= p.el_min_deg AND sats.azimuth_degrees >= 0 " +
                                        "AND sats.azimuth_degrees < 360 ), agg AS (" +
                                        "SELECT tap_uuid, azimuth_bucket,count(*)::int AS sample_count, " +
                                        "min(elevation) AS min_elevation_observed, count(*) " +
                                        "FILTER (WHERE elevation <= (SELECT skyline_el_max_deg FROM params))::int " +
                                        "AS low_subset_count, percentile_cont(0.50) WITHIN GROUP (ORDER BY sno) " +
                                        "AS sno_median, percentile_cont(0.10) WITHIN GROUP (ORDER BY sno) AS sno_p10, " +
                                        "percentile_cont(0.10) WITHIN GROUP (ORDER BY elevation) FILTER " +
                                        "(WHERE elevation <= (SELECT skyline_el_max_deg FROM params)) AS skyline_low," +
                                        "percentile_cont(0.10) WITHIN GROUP (ORDER BY elevation) AS skyline_all " +
                                        "FROM filtered GROUP BY tap_uuid, azimuth_bucket)" +
                                        "SELECT a.tap_uuid, a.azimuth_bucket," +
                                        "CASE " +
                                        "WHEN a.sample_count < (SELECT min_total_samples FROM params) " +
                                        "THEN NULL::double precision " +
                                        "WHEN a.low_subset_count < (SELECT min_low_subset_samples FROM params) " +
                                        "THEN NULL::double precision " +
                                        "ELSE a.skyline_low " +
                                        "END AS skyline_elevation, a.low_subset_count, a.min_elevation_observed, " +
                                        "CASE " +
                                        "WHEN a.sample_count < (SELECT min_total_samples FROM params) " +
                                        "THEN NULL::double precision " +
                                        "WHEN a.low_subset_count >= (SELECT min_low_subset_samples FROM params) " +
                                        "THEN a.skyline_low " +
                                        "ELSE a.skyline_all " +
                                        "END AS skyline_elevation_best_effort," +
                                        "(a.low_subset_count < (SELECT min_low_subset_samples FROM params))" +
                                        "AS used_fallback, a.sno_median, a.sno_p10, a.sample_count," +
                                        "(SELECT window_start FROM params) AS window_start, " +
                                        "(SELECT window_end   FROM params) AS window_end " +
                                        "FROM agg a WHERE a.sample_count >= (SELECT min_total_samples FROM params) " +
                                        "ORDER BY a.tap_uuid, a.azimuth_bucket; ")
                                .mapTo(GNSSElevationMaskAzimuthBucket.class)
                                .list()
                );
            }

            try (Timer.Context ignored = writeTimer.time()) {
                nzyme.getDatabase().useHandle(handle -> {
                    PreparedBatch batch = handle.prepareBatch("INSERT INTO gnss_elevation_mask(tap_uuid, " +
                            "azimuth_bucket, skyline_elevation, skyline_elevation_best_effort, used_fallback, " +
                            "low_subset_count, min_elevation_observed, sno_median, sno_p10, sample_count, " +
                            "window_start, window_end, created_at, updated_at) VALUES(:tap_uuid, :azimuth_bucket, " +
                            ":skyline_elevation, :skyline_elevation_best_effort, :used_fallback, :low_subset_count, " +
                            ":min_elevation_observed, :sno_median, :sno_p10, :sample_count, :window_start, " +
                            ":window_end, NOW(), NOW()) ON CONFLICT (tap_uuid, azimuth_bucket) DO UPDATE " +
                            "SET skyline_elevation = EXCLUDED.skyline_elevation, " +
                            "skyline_elevation_best_effort = EXCLUDED.skyline_elevation_best_effort, " +
                            "used_fallback = EXCLUDED.used_fallback, low_subset_count = EXCLUDED.low_subset_count, " +
                            "min_elevation_observed = EXCLUDED.min_elevation_observed, " +
                            "sno_median = EXCLUDED.sno_median, sno_p10 = EXCLUDED.sno_p10, " +
                            "sample_count = EXCLUDED.sample_count, window_start = EXCLUDED.window_start, " +
                            "window_end = EXCLUDED.window_end, updated_at = NOW()");

                    for (GNSSElevationMaskAzimuthBucket bucket : masks) {
                        batch.bind("tap_uuid", bucket.tapUuid())
                                .bind("azimuth_bucket", bucket.azimuthBucket())
                                .bind("skyline_elevation", bucket.skylineElevation())
                                .bind("skyline_elevation_best_effort", bucket.skylineElevationBestEffort())
                                .bind("used_fallback", bucket.usedFallback())
                                .bind("low_subset_count", bucket.lowSubsetCount())
                                .bind("min_elevation_observed", bucket.minElevationObserved())
                                .bind("sno_median", bucket.snoMedian())
                                .bind("sno_p10", bucket.snoP10())
                                .bind("sample_count", bucket.sampleCount())
                                .bind("window_start", bucket.windowStart())
                                .bind("window_end", bucket.windowEnd())
                                .add();
                    }

                    batch.execute();
                });
            }

            LOG.info("All GNSS elevation masks computed.");
        } catch (Exception e) {
            LOG.error("Could not calculate GNSS elevation masks.", e);
        }
    }

    @Override
    public String getName() {
        return "GNSSElevationMaskThread";
    }

}
