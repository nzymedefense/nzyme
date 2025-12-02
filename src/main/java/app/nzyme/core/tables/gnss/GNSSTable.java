package app.nzyme.core.tables.gnss;

import app.nzyme.core.rest.resources.taps.reports.tables.gnss.GNSSConstellationReport;
import app.nzyme.core.rest.resources.taps.reports.tables.gnss.GNSSConstellationsReport;
import app.nzyme.core.tables.DataTable;
import app.nzyme.core.tables.TablesService;
import app.nzyme.core.util.MetricNames;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GNSSTable implements DataTable {

    private static final Logger LOG = LogManager.getLogger(GNSSTable.class);

    private final Timer totalReportTimer;

    private final TablesService tablesService;
    private final ObjectMapper om;

    public GNSSTable(TablesService tablesService) {
        this.tablesService = tablesService;
        this.om = new ObjectMapper();

        this.totalReportTimer = tablesService.getNzyme().getMetrics()
                .timer(MetricNames.GNSS_TOTAL_REPORT_PROCESSING_TIMER);
    }

    public void handleConstellationsReport(UUID tapUuid, DateTime timestamp, GNSSConstellationsReport report) {
        try (Timer.Context ignored = totalReportTimer.time()) {
            tablesService.getNzyme().getDatabase().useHandle(handle -> {
                PreparedBatch constellationInsertBatch = handle.prepareBatch("INSERT INTO gnss_constellations(tap_uuid, " +
                        "constellation, fixes, maximum_time_deviation_ms, positions, maximum_fix_satellite_count, " +
                        "minimum_fix_satellite_count, fix_satellites, maximum_altitude_meters, " +
                        "minimum_altitude_meters, maximum_pdop, minimum_pdop, maximum_satellites_in_view_count, " +
                        "minimum_satellites_in_view_count, maximum_jamming_indicator, maximum_agc_count, " +
                        "maximum_noise, timestamp, created_at) VALUES(:tap_uuid, :constellation, " +
                        ":fixes::jsonb, :maximum_time_deviation_ms, :positions::jsonb, " +
                        ":maximum_fix_satellite_count, :minimum_fix_satellite_count, :fix_satellites::jsonb, " +
                        ":maximum_altitude_meters, :minimum_altitude_meters, :maximum_pdop, :minimum_pdop, " +
                        ":maximum_satellites_in_view_count, :minimum_satellites_in_view_count, " +
                        ":maximum_jamming_indicator, :maximum_agc_count, :maximum_noise, :timestamp, NOW())");

                // Keep a parallel list so we can match rows/ids for additional linked inserts.
                List<GNSSConstellationReport> rowsInOrder = Lists.newArrayList();

                for (Map.Entry<String, GNSSConstellationReport> constellation : report.constellations().entrySet()) {
                    String constellationName = constellation.getKey();
                    GNSSConstellationReport data = constellation.getValue();

                    rowsInOrder.add(data);

                    try {
                        constellationInsertBatch
                                .bind("tap_uuid", tapUuid)
                                .bind("constellation", constellationName)
                                .bind("fixes", om.writeValueAsString(data.fixes()))
                                .bind("maximum_time_deviation_ms", data.maximumTimeDeviationMs())
                                .bind("positions", om.writeValueAsString(data.positions()))
                                .bind("maximum_fix_satellite_count", data.maximumFixSatelliteCount())
                                .bind("minimum_fix_satellite_count", data.minimumFixSatelliteCount())
                                .bind("fix_satellites", om.writeValueAsString(data.fixSatellites()))
                                .bind("maximum_altitude_meters", data.maximumAltitudeMeters())
                                .bind("minimum_altitude_meters", data.minimumAltitudeMeters())
                                .bind("maximum_pdop", data.maximumPdop())
                                .bind("minimum_pdop", data.minimumPdop())
                                .bind("maximum_satellites_in_view_count", data.maximumSatellitesInViewCount())
                                .bind("minimum_satellites_in_view_count", data.minimumSatellitesInViewCount())
                                .bind("maximum_jamming_indicator", data.maximumJammingIndicator())
                                .bind("maximum_agc_count", data.maximumAgcCount())
                                .bind("maximum_noise", data.maximumNoise())
                                .bind("timestamp", data.timestamp())
                                .add();
                    } catch (JsonProcessingException e) {
                        LOG.error("Could not serialize GNSS constellation report data. Skipping.", e);
                        continue;
                    }
                }

                // Insert constellation data and retrieve generated IDs.
                List<Long> constellationIds = constellationInsertBatch
                        .executePreparedBatch("id")
                        .mapTo(Long.class)
                        .list();

                PreparedBatch satellitesInsertBatch = handle.prepareBatch("INSERT INTO " +
                        "gnss_sats_in_view(gnss_constellation_id, prn, snr, azimuth_degrees, " +
                        "elevation_degrees) VALUES(:gnss_constellation_id, :prn, :snr, :azimuth_degrees, " +
                        ":elevation_degrees)");

                for (int i = 0; i < constellationIds.size(); i++) {
                    long constellationId = constellationIds.get(i);
                    GNSSConstellationReport data = rowsInOrder.get(i);

                    for (var s : data.satellitesInView()) {
                        satellitesInsertBatch
                                .bind("gnss_constellation_id", constellationId)
                                .bind("prn", s.prn())
                                .bind("snr", s.snr())
                                .bind("azimuth_degrees", s.azimuthDegrees())
                                .bind("elevation_degrees", s.elevationDegrees())
                                .add();
                    }
                }

                satellitesInsertBatch.execute();
            });
        }
    }

    @Override
    public void retentionClean() {
        // NOOP. Remove from plugin APIs if there remains no use. Database cleaned by category/tenant independently.
    }
}
