package app.nzyme.core.rest.responses.uav;

import app.nzyme.core.rest.responses.uav.enums.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class UavDetailsResponse {

    @JsonProperty("is_active")
    public abstract boolean isActive();

    @JsonProperty("tap_uuid")
    public abstract UUID tapUuid();

    @JsonProperty("identifier")
    public abstract String identifier();

    @JsonProperty("designation")
    public abstract String designation();

    @JsonProperty("uav_type")
    public abstract UavTypeResponse uavType();

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

    @JsonProperty("first_seen")
    public abstract DateTime firstSeen();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    public static UavDetailsResponse create(boolean isActive, UUID tapUuid, String identifier, String designation, UavTypeResponse uavType, UavDetectionSourceResponse detectionSource, String idSerial, String idRegistration, String idUtm, String idSession, double rssiAverage, UavOperationalStatusResponse operationalStatus, Double latitude, Double longitude, Integer groundTrack, Double speed, Double verticalSpeed, Double altitudePressure, Double altitudeGeodetic, UavHeightTypeResponse heightType, Double height, Integer accuracyHorizontal, Integer accuracyVertical, Integer accuracyBarometer, Integer accuracySpeed, UavOperatorLocationTypeResponse operatorLocationType, Double operatorLatitude, Double operatorLongitude, Double operatorAltitude, DateTime firstSeen, DateTime lastSeen) {
        return builder()
                .isActive(isActive)
                .tapUuid(tapUuid)
                .identifier(identifier)
                .designation(designation)
                .uavType(uavType)
                .detectionSource(detectionSource)
                .idSerial(idSerial)
                .idRegistration(idRegistration)
                .idUtm(idUtm)
                .idSession(idSession)
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
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UavDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder isActive(boolean isActive);

        public abstract Builder tapUuid(UUID tapUuid);

        public abstract Builder identifier(String identifier);

        public abstract Builder designation(String designation);

        public abstract Builder uavType(UavTypeResponse uavType);

        public abstract Builder detectionSource(UavDetectionSourceResponse detectionSource);

        public abstract Builder idSerial(String idSerial);

        public abstract Builder idRegistration(String idRegistration);

        public abstract Builder idUtm(String idUtm);

        public abstract Builder idSession(String idSession);

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

        public abstract Builder firstSeen(DateTime firstSeen);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract UavDetailsResponse build();
    }
}
