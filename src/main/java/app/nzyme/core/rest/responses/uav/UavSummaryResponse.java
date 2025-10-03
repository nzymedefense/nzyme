package app.nzyme.core.rest.responses.uav;

import app.nzyme.core.rest.responses.shared.ClassificationResponse;
import app.nzyme.core.rest.responses.uav.enums.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.Map;
import java.util.UUID;

@AutoValue
public abstract class UavSummaryResponse {

    @JsonProperty("is_active")
    public abstract boolean isActive();

    @JsonProperty("identifier")
    public abstract String identifier();

    @JsonProperty("designation")
    public abstract String designation();

    @JsonProperty("classification")
    public abstract ClassificationResponse classification();

    @JsonProperty("uav_type")
    public abstract UavTypeResponse uavType();

    @JsonProperty("uav_model_type")
    @Nullable
    public abstract String uavModelType();

    @JsonProperty("uav_model_model")
    @Nullable
    public abstract String uavModelModel();

    @JsonProperty("uav_model_name")
    @Nullable
    public abstract String uavModelName();

    @JsonProperty("detection_source")
    public abstract UavDetectionSourceResponse detectionSource();

    @JsonProperty("id_serial")
    @Nullable
    public abstract String idSerial();

    @JsonProperty("id_registration")
    @Nullable
    public abstract String idRegistration();

    @JsonProperty("id_utm")
    @Nullable
    public abstract String idUtm();

    @JsonProperty("id_session")
    @Nullable
    public abstract String idSession();

    @JsonProperty("operator_id")
    @Nullable
    public abstract String operatorId();

    @JsonProperty("rssi_average")
    public abstract double rssiAverage();

    @JsonProperty("operational_status")
    @Nullable
    public abstract UavOperationalStatusResponse operationalStatus();

    @JsonProperty("latitude")
    @Nullable
    public abstract Double latitude();

    @JsonProperty("longitude")
    @Nullable
    public abstract Double longitude();

    @JsonProperty("ground_track")
    @Nullable
    public abstract Integer groundTrack();

    @JsonProperty("speed")
    @Nullable
    public abstract Double speed();

    @JsonProperty("vertical_speed")
    @Nullable
    public abstract Double verticalSpeed();

    @JsonProperty("altitude_pressure")
    @Nullable
    public abstract Double altitudePressure();

    @JsonProperty("altitude_geodetic")
    @Nullable
    public abstract Double altitudeGeodetic();

    @JsonProperty("height_type")
    @Nullable
    public abstract UavHeightTypeResponse heightType();

    @JsonProperty("height")
    @Nullable
    public abstract Double height();

    @JsonProperty("accuracy_horizontal")
    @Nullable
    public abstract Integer accuracyHorizontal();

    @JsonProperty("accuracy_vertical")
    @Nullable
    public abstract Integer accuracyVertical();

    @JsonProperty("accuracy_barometer")
    @Nullable
    public abstract Integer accuracyBarometer();

    @JsonProperty("accuracy_speed")
    @Nullable
    public abstract Integer accuracySpeed();

    @JsonProperty("operator_location_type")
    @Nullable
    public abstract UavOperatorLocationTypeResponse operatorLocationType();

    @JsonProperty("operator_latitude")
    @Nullable
    public abstract Double operatorLatitude();

    @JsonProperty("operator_longitude")
    @Nullable
    public abstract Double operatorLongitude();

    @JsonProperty("operator_altitude")
    @Nullable
    public abstract Double operatorAltitude();

    @JsonProperty("operator_distance_to_uav")
    @Nullable
    public abstract Double operatorDistanceToUav();

    @JsonProperty("latest_vector_timestamp")
    @Nullable
    public abstract DateTime latestVectorTimestamp();

    @JsonProperty("latest_operator_location_timestamp")
    @Nullable
    public abstract DateTime latestOperatorLocationTimestamp();

    @JsonProperty("tap_distances")
    public abstract Map<UUID, UavToTapDistanceResponse> tapDistances();

    @JsonProperty("closest_tap_distance")
    @Nullable
    public abstract Double closestTapDistance();

    @JsonProperty("first_seen")
    public abstract DateTime firstSeen();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    public static UavSummaryResponse create(boolean isActive, String identifier, String designation, ClassificationResponse classification, UavTypeResponse uavType, String uavModelType, String uavModelModel, String uavModelName, UavDetectionSourceResponse detectionSource, String idSerial, String idRegistration, String idUtm, String idSession, String operatorId, double rssiAverage, UavOperationalStatusResponse operationalStatus, Double latitude, Double longitude, Integer groundTrack, Double speed, Double verticalSpeed, Double altitudePressure, Double altitudeGeodetic, UavHeightTypeResponse heightType, Double height, Integer accuracyHorizontal, Integer accuracyVertical, Integer accuracyBarometer, Integer accuracySpeed, UavOperatorLocationTypeResponse operatorLocationType, Double operatorLatitude, Double operatorLongitude, Double operatorAltitude, Double operatorDistanceToUav, DateTime latestVectorTimestamp, DateTime latestOperatorLocationTimestamp, Map<UUID, UavToTapDistanceResponse> tapDistances, Double closestTapDistance, DateTime firstSeen, DateTime lastSeen) {
        return builder()
                .isActive(isActive)
                .identifier(identifier)
                .designation(designation)
                .classification(classification)
                .uavType(uavType)
                .uavModelType(uavModelType)
                .uavModelModel(uavModelModel)
                .uavModelName(uavModelName)
                .detectionSource(detectionSource)
                .idSerial(idSerial)
                .idRegistration(idRegistration)
                .idUtm(idUtm)
                .idSession(idSession)
                .operatorId(operatorId)
                .rssiAverage(rssiAverage)
                .operationalStatus(operationalStatus)
                .latitude(latitude)
                .longitude(longitude)
                .groundTrack(groundTrack)
                .speed(speed)
                .verticalSpeed(verticalSpeed)
                .altitudePressure(altitudePressure)
                .altitudeGeodetic(altitudeGeodetic)
                .heightType(heightType)
                .height(height)
                .accuracyHorizontal(accuracyHorizontal)
                .accuracyVertical(accuracyVertical)
                .accuracyBarometer(accuracyBarometer)
                .accuracySpeed(accuracySpeed)
                .operatorLocationType(operatorLocationType)
                .operatorLatitude(operatorLatitude)
                .operatorLongitude(operatorLongitude)
                .operatorAltitude(operatorAltitude)
                .operatorDistanceToUav(operatorDistanceToUav)
                .latestVectorTimestamp(latestVectorTimestamp)
                .latestOperatorLocationTimestamp(latestOperatorLocationTimestamp)
                .tapDistances(tapDistances)
                .closestTapDistance(closestTapDistance)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UavSummaryResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder isActive(boolean isActive);

        public abstract Builder identifier(String identifier);

        public abstract Builder designation(String designation);

        public abstract Builder classification(ClassificationResponse classification);

        public abstract Builder uavType(UavTypeResponse uavType);

        public abstract Builder uavModelType(String uavModelType);

        public abstract Builder uavModelModel(String uavModelModel);

        public abstract Builder uavModelName(String uavModelName);

        public abstract Builder detectionSource(UavDetectionSourceResponse detectionSource);

        public abstract Builder idSerial(String idSerial);

        public abstract Builder idRegistration(String idRegistration);

        public abstract Builder idUtm(String idUtm);

        public abstract Builder idSession(String idSession);

        public abstract Builder operatorId(String operatorId);

        public abstract Builder rssiAverage(double rssiAverage);

        public abstract Builder operationalStatus(UavOperationalStatusResponse operationalStatus);

        public abstract Builder latitude(Double latitude);

        public abstract Builder longitude(Double longitude);

        public abstract Builder groundTrack(Integer groundTrack);

        public abstract Builder speed(Double speed);

        public abstract Builder verticalSpeed(Double verticalSpeed);

        public abstract Builder altitudePressure(Double altitudePressure);

        public abstract Builder altitudeGeodetic(Double altitudeGeodetic);

        public abstract Builder heightType(UavHeightTypeResponse heightType);

        public abstract Builder height(Double height);

        public abstract Builder accuracyHorizontal(Integer accuracyHorizontal);

        public abstract Builder accuracyVertical(Integer accuracyVertical);

        public abstract Builder accuracyBarometer(Integer accuracyBarometer);

        public abstract Builder accuracySpeed(Integer accuracySpeed);

        public abstract Builder operatorLocationType(UavOperatorLocationTypeResponse operatorLocationType);

        public abstract Builder operatorLatitude(Double operatorLatitude);

        public abstract Builder operatorLongitude(Double operatorLongitude);

        public abstract Builder operatorAltitude(Double operatorAltitude);

        public abstract Builder operatorDistanceToUav(Double operatorDistanceToUav);

        public abstract Builder latestVectorTimestamp(DateTime latestVectorTimestamp);

        public abstract Builder latestOperatorLocationTimestamp(DateTime latestOperatorLocationTimestamp);

        public abstract Builder tapDistances(Map<UUID, UavToTapDistanceResponse> tapDistances);

        public abstract Builder closestTapDistance(Double closestTapDistance);

        public abstract Builder firstSeen(DateTime firstSeen);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract UavSummaryResponse build();
    }
}
