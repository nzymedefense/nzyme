package app.nzyme.core.tables.uav;

import app.nzyme.core.rest.resources.taps.reports.tables.uav.UavReport;
import app.nzyme.core.rest.resources.taps.reports.tables.uav.UavsReport;
import app.nzyme.core.tables.DataTable;
import app.nzyme.core.tables.TablesService;
import app.nzyme.core.util.MetricNames;
import com.codahale.metrics.Timer;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;

public class UAVTable implements DataTable {

    private static final Logger LOG = LogManager.getLogger(UAVTable.class);

    private final TablesService tablesService;

    private final Timer totalReportTimer;

    public UAVTable(TablesService tablesService) {
        this.tablesService = tablesService;

        this.totalReportTimer = tablesService.getNzyme().getMetrics()
                .timer(MetricNames.UAV_TOTAL_REPORT_PROCESSING_TIMER);
    }

    public void handleReport(UUID tapUuid, DateTime timestamp, UavsReport report) {
        tablesService.getNzyme().getDatabase().useHandle(handle -> {
            try(Timer.Context ignored = totalReportTimer.time()) {
                writeUavs(handle, tapUuid, report.uavs());
            }
        });
    }

    private void writeUavs(Handle handle, UUID tapUuid, List<UavReport> uavs) {
        PreparedBatch batch = handle.prepareBatch("INSERT INTO uavs(tap_uuid, identifier, uav_type, " +
                "detection_source, rssi_average, first_seen, last_seen) VALUES(:tap_uuid, :identifier, :uav_type, " +
                ":detection_source, :rssi_average, :first_seen, :last_seen)");

        for (UavReport uav : uavs) {
            double rssiAverage = uav.rssis()
                    .stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0.0);

            // Operational Status (Latest)
            String operationalStatus = null;
            if (!uav.vectorReports().isEmpty()) {
                operationalStatus = uav.vectorReports().get(uav.vectorReports().size()-1).operationalStatus();
            }

            LOG.info("OS: {}", operationalStatus);

            // Ground track (Latest)
            // Speed (Avg)
            // Vertical Speed (Avg)
            // Altitude (considering type, baro/geodetic/height) (Latest)
            // Operator distance (avg)
            // Accuracies

            batch
                    .bind("tap_uuid", tapUuid)
                    .bind("identifier", uav.identifier())
                    .bind("uav_type", uav.uavType())
                    .bind("detection_source", uav.detectionSource())
                    .bind("rssi_average", rssiAverage)
                    .bind("first_seen", uav.firstSeen())
                    .bind("last_seen", uav.lastSeen())
                    .add();
        }

        batch.execute();
    }

    @Override
    public void retentionClean() {
        throw new NotImplementedException();
    }
}
