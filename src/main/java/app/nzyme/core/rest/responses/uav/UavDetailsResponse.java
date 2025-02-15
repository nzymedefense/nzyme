package app.nzyme.core.rest.responses.uav;

import app.nzyme.core.rest.responses.uav.enums.*;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class UavDetailsResponse {

    public abstract UUID tapUuid();
    public abstract String identifier();
    public abstract UavTypeResponse uavType();
    public abstract UavDetectionSourceResponse detectionSource();
    public abstract double rssiAverage();
    @Nullable
    public abstract UavOperationalStatusResponse operationalStatus();
    @Nullable
    public abstract Double latitude();
    @Nullable
    public abstract Double longitude();
    @Nullable
    public abstract Integer groundTrack();
    @Nullable
    public abstract Double speed();
    @Nullable
    public abstract Double verticalSpeed();
    @Nullable
    public abstract Double altitudePressure();
    @Nullable
    public abstract Double altitudeGeodetic();
    @Nullable
    public abstract UavHeightTypeResponse heightType();
    @Nullable
    public abstract Double height();
    @Nullable
    public abstract Integer accuracyHorizontal();
    @Nullable
    public abstract Integer accuracyVertical();
    @Nullable
    public abstract Integer accuracyBarometer();
    @Nullable
    public abstract Integer accuracySpeed();
    @Nullable
    public abstract UavOperatorLocationTypeResponse operatorLocationType();
    @Nullable
    public abstract Double operatorLatitude();
    @Nullable
    public abstract Double operatorLongitude();
    @Nullable
    public abstract Double operatorAltitude();
    public abstract DateTime firstSeen();
    public abstract DateTime lastSeen();

    public static UavDetailsResponse create(UUID tapUuid, String identifier, UavTypeResponse uavType, UavDetectionSourceResponse detectionSource, double rssiAverage, UavOperationalStatusResponse operationalStatus, Double latitude, Double longitude, Integer groundTrack, Double speed, Double verticalSpeed, Double altitudePressure, Double altitudeGeodetic, UavHeightTypeResponse heightType, Double height, Integer accuracyHorizontal, Integer accuracyVertical, Integer accuracyBarometer, Integer accuracySpeed, UavOperatorLocationTypeResponse operatorLocationType, Double operatorLatitude, Double operatorLongitude, Double operatorAltitude, DateTime firstSeen, DateTime lastSeen) {
        return builder()
                .tapUuid(tapUuid)
                .identifier(identifier)
                .uavType(uavType)
                .detectionSource(detectionSource)
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
        public abstract Builder tapUuid(UUID tapUuid);

        public abstract Builder identifier(String identifier);

        public abstract Builder uavType(UavTypeResponse uavType);

        public abstract Builder detectionSource(UavDetectionSourceResponse detectionSource);

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
