package app.nzyme.core.tables.gnss;

import app.nzyme.core.rest.resources.taps.reports.tables.gnss.GNSSConstellationReport;
import app.nzyme.core.rest.resources.taps.reports.tables.gnss.GNSSConstellationsReport;
import app.nzyme.core.tables.DataTable;
import app.nzyme.core.tables.TablesService;
import app.nzyme.core.util.MetricNames;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.joda.time.DateTime;

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
                PreparedBatch insertBatch = handle.prepareBatch("INSERT INTO gnss_constellations(tap_uuid, " +
                        "constellation, fixes, maximum_time_deviation_ms, positions, maximum_fix_satellite_count, " +
                        "minimum_fix_satellite_count, fix_satellites, maximum_altitude_meters, " +
                        "minimum_altitude_meters, maximum_pdop, minimum_pdop, satellites_in_view, " +
                        "maximum_satellites_in_view_count, minimum_satellites_in_view_count, timestamp, " +
                        "created_at) VALUES(:tap_uuid, :constellation, :fixes::jsonb, :maximum_time_deviation_ms, " +
                        ":positions::jsonb, :maximum_fix_satellite_count, :minimum_fix_satellite_count, " +
                        ":fix_satellites::jsonb, :maximum_altitude_meters, :minimum_altitude_meters, :maximum_pdop, " +
                        ":minimum_pdop, :satellites_in_view::jsonb, :maximum_satellites_in_view_count, " +
                        ":minimum_satellites_in_view_count, :timestamp, NOW())");

                for (Map.Entry<String, GNSSConstellationReport> constellation : report.constellations().entrySet()) {
                    String constellationName = constellation.getKey();
                    GNSSConstellationReport data = constellation.getValue();

                    try {
                        insertBatch.bind("tap_uuid", tapUuid)
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
                                .bind("satellites_in_view", om.writeValueAsString(data.satellitesInView()))
                                .bind("maximum_satellites_in_view_count", data.maximumSatellitesInViewCount())
                                .bind("minimum_satellites_in_view_count", data.minimumSatellitesInViewCount())
                                .bind("timestamp", data.timestamp())
                                .add();
                    } catch (JsonProcessingException e) {
                        LOG.error("Could not serialize GNSS constellation report data. Skipping.", e);
                        continue;
                    }
                }

                insertBatch.execute();
            });
        }
    }

    @Override
    public void retentionClean() {
        // NOOP. Remove from plugin APIs if there remains no use. Database cleaned by category/tenant independently.
    }
}
