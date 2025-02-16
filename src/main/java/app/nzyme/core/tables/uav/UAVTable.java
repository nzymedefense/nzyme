package app.nzyme.core.tables.uav;

import app.nzyme.core.rest.resources.taps.reports.tables.uav.UavOperatorLocationReport;
import app.nzyme.core.rest.resources.taps.reports.tables.uav.UavReport;
import app.nzyme.core.rest.resources.taps.reports.tables.uav.UavVectorReport;
import app.nzyme.core.rest.resources.taps.reports.tables.uav.UavsReport;
import app.nzyme.core.tables.DataTable;
import app.nzyme.core.tables.TablesService;
import app.nzyme.core.util.MetricNames;
import com.codahale.metrics.Timer;
import com.google.common.math.Quantiles;
import com.google.common.math.Stats;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class UAVTable implements DataTable {

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
        PreparedBatch insertBatch = handle.prepareBatch("INSERT INTO uavs(tap_uuid, identifier, uav_type, " +
                "detection_source, rssi_average, operational_status, latitude, longitude, ground_track, speed, " +
                "vertical_speed, altitude_pressure, altitude_geodetic, height_type, height, accuracy_horizontal, " +
                "accuracy_vertical, accuracy_barometer, accuracy_speed, operator_location_type, operator_latitude, " +
                "operator_longitude, operator_altitude, first_seen, last_seen) VALUES(:tap_uuid, :identifier, " +
                ":uav_type, :detection_source, :rssi_average, :operational_status, :latitude, :longitude, " +
                ":ground_track, :speed, :vertical_speed, :altitude_pressure, :altitude_geodetic, :height_type, " +
                ":height, :accuracy_horizontal, :accuracy_vertical, :accuracy_barometer, :accuracy_speed, " +
                ":operator_location_type, :operator_latitude, :operator_longitude, :operator_altitude, " +
                ":first_seen, :last_seen)");

        PreparedBatch updateBatch = handle.prepareBatch("UPDATE uavs SET rssi_average = :rssi_average, " +
                "operational_status = :operational_status, latitude = :latitude, longitude = :longitude, " +
                "ground_track = :ground_track, speed = :speed, vertical_speed = :vertical_speed, " +
                "altitude_pressure = :altitude_pressure, altitude_geodetic = :altitude_geodetic, " +
                "height_type = :height_type, height = :height, accuracy_horizontal = :accuracy_horizontal, " +
                "accuracy_vertical = :accuracy_vertical, accuracy_barometer = :accuracy_barometer, " +
                "accuracy_speed = :accuracy_speed, operator_location_type = :operator_location_type, " +
                "operator_latitude = :operator_latitude, operator_longitude = :operator_longitude, " +
                "operator_altitude = :operator_altitude, last_seen = :last_seen WHERE id = :id");

        for (UavReport uav : uavs) {
            double rssiAverage = uav.rssis()
                    .stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0.0);

            // Operational Status (Latest)
            String operationalStatus = null;
            Double latitude = null;
            Double longitude = null;
            Integer groundTrack = null;
            Double speed = null;
            Double verticalSpeed = null;
            Double altitudePressure = null;
            Double altitudeGeodetic = null;
            String heightType = null;
            Double height = null;
            Integer accuracyHorizontal = null;
            Integer accuracyVertical = null;
            Integer accuracyBarometer = null;
            Integer accuracySpeed = null;
            if (!uav.vectorReports().isEmpty()) {
                UavVectorReport lastReport = uav.vectorReports().get(uav.vectorReports().size() - 1);
                operationalStatus = lastReport.operationalStatus();
                latitude = lastReport.latitude();
                longitude = lastReport.longitude();
                groundTrack = lastReport.groundTrack();
                altitudePressure = lastReport.altitudePressure();
                altitudeGeodetic = lastReport.altitudeGeodetic();
                heightType = lastReport.heightType();
                height = lastReport.height();

                List<Double> speeds = uav.vectorReports().stream()
                        .map(UavVectorReport::speed)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                if (!speeds.isEmpty()) {
                    speed = Stats.meanOf(speeds);
                }

                List<Double> verticalSpeeds = uav.vectorReports().stream()
                        .map(UavVectorReport::verticalSpeed)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                if (!verticalSpeeds.isEmpty()) {
                    verticalSpeed = Stats.meanOf(verticalSpeeds);
                }

                List<Integer> horizontalAccuracies = uav.vectorReports().stream()
                        .map(UavVectorReport::horizontalAccuracy)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                if (!horizontalAccuracies.isEmpty()) {
                    accuracyHorizontal = (int) Quantiles.median().compute(horizontalAccuracies);
                }

                List<Integer> verticalAccuracies = uav.vectorReports().stream()
                        .map(UavVectorReport::verticalAccuracy)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                if (!verticalAccuracies.isEmpty()) {
                    accuracyVertical = (int) Quantiles.median().compute(verticalAccuracies);
                }

                List<Integer> barometerAccuracies = uav.vectorReports().stream()
                        .map(UavVectorReport::barometerAccuracy)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                if (!barometerAccuracies.isEmpty()) {
                    accuracyBarometer = (int) Quantiles.median().compute(barometerAccuracies);
                }

                List<Integer> speedAccuracies = uav.vectorReports().stream()
                        .map(UavVectorReport::speedAccuracy)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                if (!speedAccuracies.isEmpty()) {
                    accuracySpeed = (int) Quantiles.median().compute(speedAccuracies);
                }
            }

            String operatorLocationType = null;
            Double operatorLatitude = null;
            Double operatorLongitude = null;
            Double operatorAltitude = null;
            if (!uav.operatorLocationReports().isEmpty()) {
                UavOperatorLocationReport lastReport = uav.operatorLocationReports()
                        .get(uav.operatorLocationReports().size() - 1);
                operatorLocationType = lastReport.locationType();
                operatorLatitude = lastReport.latitude();
                operatorLongitude = lastReport.longitude();
                operatorAltitude = lastReport.altitude();
            }

            Optional<Long> existingUav = handle.createQuery("SELECT id FROM uavs WHERE tap_uuid = :tap_uuid AND identifier = :identifier")
                    .bind("tap_uuid", tapUuid)
                    .bind("identifier", uav.identifier())
                    .mapTo(Long.class)
                    .findOne();

            if (existingUav.isEmpty()) {
                // First time seeing this UAV.
                insertBatch
                        .bind("tap_uuid", tapUuid)
                        .bind("identifier", uav.identifier())
                        .bind("uav_type", uav.uavType())
                        .bind("detection_source", uav.detectionSource())
                        .bind("rssi_average", rssiAverage)
                        .bind("operational_status", operationalStatus)
                        .bind("latitude", latitude)
                        .bind("longitude", longitude)
                        .bind("ground_track", groundTrack)
                        .bind("speed", speed)
                        .bind("vertical_speed", verticalSpeed)
                        .bind("altitude_pressure", altitudePressure)
                        .bind("altitude_geodetic", altitudeGeodetic)
                        .bind("height_type", heightType)
                        .bind("height", height)
                        .bind("accuracy_horizontal", accuracyHorizontal)
                        .bind("accuracy_vertical", accuracyVertical)
                        .bind("accuracy_barometer", accuracyBarometer)
                        .bind("accuracy_speed", accuracySpeed)
                        .bind("operator_location_type", operatorLocationType)
                        .bind("operator_latitude", operatorLatitude)
                        .bind("operator_longitude", operatorLongitude)
                        .bind("operator_altitude", operatorAltitude)
                        .bind("first_seen", uav.firstSeen())
                        .bind("last_seen", uav.lastSeen())
                        .add();
            } else {
                updateBatch
                        .bind("id", existingUav.get())
                        .bind("tap_uuid", tapUuid)
                        .bind("rssi_average", rssiAverage)
                        .bind("operational_status", operationalStatus)
                        .bind("latitude", latitude)
                        .bind("longitude", longitude)
                        .bind("ground_track", groundTrack)
                        .bind("speed", speed)
                        .bind("vertical_speed", verticalSpeed)
                        .bind("altitude_pressure", altitudePressure)
                        .bind("altitude_geodetic", altitudeGeodetic)
                        .bind("height_type", heightType)
                        .bind("height", height)
                        .bind("accuracy_horizontal", accuracyHorizontal)
                        .bind("accuracy_vertical", accuracyVertical)
                        .bind("accuracy_barometer", accuracyBarometer)
                        .bind("accuracy_speed", accuracySpeed)
                        .bind("operator_location_type", operatorLocationType)
                        .bind("operator_latitude", operatorLatitude)
                        .bind("operator_longitude", operatorLongitude)
                        .bind("operator_altitude", operatorAltitude)
                        .bind("last_seen", uav.lastSeen())
                        .add();
            }
        }

        insertBatch.execute();
        updateBatch.execute();
    }

    @Override
    public void retentionClean() {
        // NOOP. Remove from plugin APIs if there remains no use. Database cleaned by category/tenant independently.
    }
}
