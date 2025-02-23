package app.nzyme.core.tables.uav;

import app.nzyme.core.designation.Designation;
import app.nzyme.core.rest.resources.taps.reports.tables.uav.*;
import app.nzyme.core.shared.Classification;
import app.nzyme.core.tables.DataTable;
import app.nzyme.core.tables.TablesService;
import app.nzyme.core.util.MetricNames;
import com.codahale.metrics.Timer;
import com.google.common.math.Quantiles;
import com.google.common.math.Stats;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class UAVTable implements DataTable {

    private static final Logger LOG = LogManager.getLogger(UAVTable.class);

    private final TablesService tablesService;

    private final Timer totalReportTimer;

    public UAVTable(TablesService tablesService) {
        this.tablesService = tablesService;

        this.totalReportTimer = tablesService.getNzyme().getMetrics()
                .timer(MetricNames.UAV_TOTAL_REPORT_PROCESSING_TIMER);
    }

    public void handleReport(UUID tapUuid, DateTime ignored, UavsReport report) {
        tablesService.getNzyme().getDatabase().useHandle(handle -> {
            try(Timer.Context ignored2 = totalReportTimer.time()) {
                writeUavs(handle, tapUuid, report.uavs());
            }
        });
    }

    private void writeUavs(Handle handle, UUID tapUuid, List<UavReport> uavs) {
        PreparedBatch insertBatch = handle.prepareBatch("INSERT INTO uavs(tap_uuid, identifier, designation, " +
                "classification, uav_type, detection_source, id_serial, id_registration, id_utm, id_session, " +
                "operator_id, rssi_average, operational_status, latitude, longitude, ground_track, speed, " +
                "vertical_speed, altitude_pressure, altitude_geodetic, height_type, height, accuracy_horizontal, " +
                "accuracy_vertical, accuracy_barometer, accuracy_speed, operator_location_type, operator_latitude, " +
                "operator_longitude, operator_altitude, latest_vector_timestamp, latest_operator_location_timestamp, " +
                "first_seen, last_seen) VALUES(:tap_uuid, :identifier, :designation, :classification, :uav_type, " +
                ":detection_source, :id_serial, :id_registration, :id_utm, :id_session, :operator_id, :rssi_average, " +
                ":operational_status, :latitude, :longitude, :ground_track, :speed, :vertical_speed, " +
                ":altitude_pressure, :altitude_geodetic, :height_type, :height, :accuracy_horizontal, " +
                ":accuracy_vertical, :accuracy_barometer, :accuracy_speed, :operator_location_type, " +
                ":operator_latitude, :operator_longitude, :operator_altitude, :latest_vector_timestamp, " +
                ":latest_operator_location_timestamp, :first_seen, :last_seen)");

        PreparedBatch updateBatch = handle.prepareBatch("UPDATE uavs SET rssi_average = :rssi_average, " +
                "operational_status = :operational_status, latitude = :latitude, longitude = :longitude, " +
                "ground_track = :ground_track, speed = :speed, vertical_speed = :vertical_speed, " +
                "altitude_pressure = :altitude_pressure, altitude_geodetic = :altitude_geodetic, " +
                "height_type = :height_type, height = :height, accuracy_horizontal = :accuracy_horizontal, " +
                "accuracy_vertical = :accuracy_vertical, accuracy_barometer = :accuracy_barometer, " +
                "accuracy_speed = :accuracy_speed, operator_location_type = :operator_location_type, " +
                "operator_latitude = :operator_latitude, operator_longitude = :operator_longitude, " +
                "operator_altitude = :operator_altitude, latest_vector_timestamp = :latest_vector_timestamp, " +
                "latest_operator_location_timestamp = :latest_operator_location_timestamp, last_seen = :last_seen " +
                "WHERE id = :id");

        for (UavReport uav : uavs) {
            String designation = Designation.fromSha256ShortDigest(uav.identifier().subSequence(0, 7).toString());
            double rssiAverage = uav.rssis()
                    .stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0.0);

            String operatorId = uav.operatorIds()
                    .stream()
                    .findFirst()
                    .orElse(null);

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
            DateTime latestVectorTimestamp = null;
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
                latestVectorTimestamp = lastReport.timestamp();

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
            DateTime latestOperatorLocationTimestamp = null;
            if (!uav.operatorLocationReports().isEmpty()) {
                UavOperatorLocationReport lastReport = uav.operatorLocationReports()
                        .get(uav.operatorLocationReports().size() - 1);
                operatorLocationType = lastReport.locationType();
                operatorLatitude = lastReport.latitude();
                operatorLongitude = lastReport.longitude();
                operatorAltitude = lastReport.altitude();
                latestOperatorLocationTimestamp = lastReport.timestamp();
            }

            String idSerial = null;
            String idRegistration = null;
            String idUtm = null;
            String idSession = null;
            for (UavIdReport uavId : uav.uavIds()) {
                switch (uavId.idType()) {
                    case "AnsiCtaSerial":
                        idSerial = uavId.id();
                        break;
                    case "CaaRegistrationId":
                        idRegistration = uavId.id();
                        break;
                    case "UtmAssignedUuid":
                        idUtm = uavId.id();
                        break;
                    case "SpecificSessionId":
                        idSession = uavId.id();
                        break;
                    default:
                        LOG.warn("Unknown UAV ID type [{}]", uavId.idType());
                }
            }

            Optional<Long> existingUav = handle.createQuery("SELECT id FROM uavs " +
                            "WHERE tap_uuid = :tap_uuid AND identifier = :identifier")
                    .bind("tap_uuid", tapUuid)
                    .bind("identifier", uav.identifier())
                    .mapTo(Long.class)
                    .findOne();

            if (existingUav.isEmpty()) {
                // First time seeing this UAV.
                insertBatch
                        .bind("tap_uuid", tapUuid)
                        .bind("identifier", uav.identifier())
                        .bind("designation", designation)
                        .bind("classification", Classification.UNKNOWN)
                        .bind("uav_type", uav.uavType())
                        .bind("detection_source", uav.detectionSource())
                        .bind("id_serial", idSerial)
                        .bind("id_registration", idRegistration)
                        .bind("id_utm", idUtm)
                        .bind("id_session", idSession)
                        .bind("operator_id", operatorId)
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
                        .bind("latest_vector_timestamp", latestVectorTimestamp)
                        .bind("latest_operator_location_timestamp", latestOperatorLocationTimestamp)
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
                        .bind("latest_vector_timestamp", latestVectorTimestamp)
                        .bind("latest_operator_location_timestamp", latestOperatorLocationTimestamp)
                        .bind("last_seen", uav.lastSeen())
                        .add();
            }
        }

        insertBatch.execute();
        updateBatch.execute();

        // Write vectors.
        PreparedBatch vectorBatch = handle.prepareBatch("INSERT INTO uavs_vectors(uav_id, " +
                "operational_status, latitude, longitude, ground_track, speed, vertical_speed, altitude_pressure, " +
                "altitude_geodetic, height_type, height, accuracy_horizontal, accuracy_vertical, " +
                "accuracy_barometer, accuracy_speed, timestamp) VALUES(:uav_id, :operational_status, " +
                ":latitude, :longitude, :ground_track, :speed, :vertical_speed, :altitude_pressure, " +
                ":altitude_geodetic, :height_type, :height, :accuracy_horizontal, :accuracy_vertical, " +
                ":accuracy_barometer, :accuracy_speed, :timestamp)");

        for (UavReport uav : uavs) {
            Optional<Long> existingUav = handle.createQuery("SELECT id FROM uavs " +
                            "WHERE tap_uuid = :tap_uuid AND identifier = :identifier")
                    .bind("tap_uuid", tapUuid)
                    .bind("identifier", uav.identifier())
                    .mapTo(Long.class)
                    .findOne();

            if (!existingUav.isPresent()) {
                LOG.error("Could not find UAV to write vector reports. UAV identifier: {}", uav.identifier());
                continue;
            }

            for (UavVectorReport vector : uav.vectorReports()) {
                vectorBatch
                        .bind("uav_id", existingUav.get())
                        .bind("operational_status", vector.operationalStatus())
                        .bind("latitude", vector.latitude())
                        .bind("longitude", vector.longitude())
                        .bind("ground_track", vector.groundTrack())
                        .bind("speed", vector.speed())
                        .bind("vertical_speed", vector.verticalSpeed())
                        .bind("altitude_pressure", vector.altitudePressure())
                        .bind("altitude_geodetic", vector.altitudeGeodetic())
                        .bind("height_type", vector.heightType())
                        .bind("height", vector.height())
                        .bind("accuracy_horizontal", vector.horizontalAccuracy())
                        .bind("accuracy_vertical", vector.verticalAccuracy())
                        .bind("accuracy_barometer", vector.barometerAccuracy())
                        .bind("accuracy_speed", vector.speedAccuracy())
                        .bind("timestamp", vector.timestamp())
                        .add();
            }
        }

        vectorBatch.execute();
    }

    @Override
    public void retentionClean() {
        // NOOP. Remove from plugin APIs if there remains no use. Database cleaned by category/tenant independently.
    }
}
