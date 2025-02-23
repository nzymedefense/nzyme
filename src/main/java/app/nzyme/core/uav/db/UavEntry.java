package app.nzyme.core.uav.db;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class UavEntry {

    public abstract long id();
    public abstract String identifier();
    public abstract String designation();
    public abstract String classification();
    public abstract String uavType();
    public abstract String detectionSource();
    @Nullable
    public abstract String idSerial();
    @Nullable
    public abstract String idRegistration();
    @Nullable
    public abstract String idUtm();
    @Nullable
    public abstract String idSession();
    @Nullable
    public abstract String operatorId();
    public abstract double rssiAverage();
    @Nullable
    public abstract String operationalStatus();
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
    public abstract String heightType();
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
    public abstract String operatorLocationType();
    @Nullable
    public abstract Double operatorLatitude();
    @Nullable
    public abstract Double operatorLongitude();
    @Nullable
    public abstract Double operatorAltitude();
    @Nullable
    public abstract DateTime latestVectorTimestamp();
    @Nullable
    public abstract DateTime latestOperatorLocationTimestamp();
    public abstract DateTime firstSeen();
    public abstract DateTime lastSeen();

    public static UavEntry create(long id, String identifier, String designation, String classification, String uavType, String detectionSource, String idSerial, String idRegistration, String idUtm, String idSession, String operatorId, double rssiAverage, String operationalStatus, Double latitude, Double longitude, Integer groundTrack, Double speed, Double verticalSpeed, Double altitudePressure, Double altitudeGeodetic, String heightType, Double height, Integer accuracyHorizontal, Integer accuracyVertical, Integer accuracyBarometer, Integer accuracySpeed, String operatorLocationType, Double operatorLatitude, Double operatorLongitude, Double operatorAltitude, DateTime latestVectorTimestamp, DateTime latestOperatorLocationTimestamp, DateTime firstSeen, DateTime lastSeen) {
        return builder()
                .id(id)
                .identifier(identifier)
                .designation(designation)
                .classification(classification)
                .uavType(uavType)
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
                .latestVectorTimestamp(latestVectorTimestamp)
                .latestOperatorLocationTimestamp(latestOperatorLocationTimestamp)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UavEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder identifier(String identifier);

        public abstract Builder designation(String designation);

        public abstract Builder classification(String classification);

        public abstract Builder uavType(String uavType);

        public abstract Builder detectionSource(String detectionSource);

        public abstract Builder idSerial(String idSerial);

        public abstract Builder idRegistration(String idRegistration);

        public abstract Builder idUtm(String idUtm);

        public abstract Builder idSession(String idSession);

        public abstract Builder operatorId(String operatorId);

        public abstract Builder rssiAverage(double rssiAverage);

        public abstract Builder operationalStatus(String operationalStatus);

        public abstract Builder latitude(Double latitude);

        public abstract Builder longitude(Double longitude);

        public abstract Builder groundTrack(Integer groundTrack);

        public abstract Builder speed(Double speed);

        public abstract Builder verticalSpeed(Double verticalSpeed);

        public abstract Builder altitudePressure(Double altitudePressure);

        public abstract Builder altitudeGeodetic(Double altitudeGeodetic);

        public abstract Builder heightType(String heightType);

        public abstract Builder height(Double height);

        public abstract Builder accuracyHorizontal(Integer accuracyHorizontal);

        public abstract Builder accuracyVertical(Integer accuracyVertical);

        public abstract Builder accuracyBarometer(Integer accuracyBarometer);

        public abstract Builder accuracySpeed(Integer accuracySpeed);

        public abstract Builder operatorLocationType(String operatorLocationType);

        public abstract Builder operatorLatitude(Double operatorLatitude);

        public abstract Builder operatorLongitude(Double operatorLongitude);

        public abstract Builder operatorAltitude(Double operatorAltitude);

        public abstract Builder latestVectorTimestamp(DateTime latestVectorTimestamp);

        public abstract Builder latestOperatorLocationTimestamp(DateTime latestOperatorLocationTimestamp);

        public abstract Builder firstSeen(DateTime firstSeen);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract UavEntry build();
    }
}
