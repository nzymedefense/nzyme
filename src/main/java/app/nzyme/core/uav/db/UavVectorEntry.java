package app.nzyme.core.uav.db;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

@AutoValue
public abstract class UavVectorEntry {

    public abstract long id();
    public abstract long uavId();
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
    public abstract DateTime timestamp();

    public static UavVectorEntry create(long id, long uavId, String operationalStatus, Double latitude, Double longitude, Integer groundTrack, Double speed, Double verticalSpeed, Double altitudePressure, Double altitudeGeodetic, String heightType, Double height, Integer accuracyHorizontal, Integer accuracyVertical, Integer accuracyBarometer, Integer accuracySpeed, DateTime timestamp) {
        return builder()
                .id(id)
                .uavId(uavId)
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
                .timestamp(timestamp)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UavVectorEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder uavId(long uavId);

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

        public abstract Builder timestamp(DateTime timestamp);

        public abstract UavVectorEntry build();
    }
}
