package app.nzyme.core.rest.resources.taps.reports.tables.uav;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

@AutoValue
public abstract class UavVectorReport {

    public abstract DateTime timestamp();
    @Nullable
    public abstract String operationalStatus();
    @Nullable
    public abstract String heightType();
    @Nullable
    public abstract Integer groundTrack();
    @Nullable
    public abstract Double speed();
    @Nullable
    public abstract Double verticalSpeed();
    @Nullable
    public abstract Double latitude();
    @Nullable
    public abstract Double longitude();
    @Nullable
    public abstract Double altitudePressure();
    @Nullable
    public abstract Double altitudeGeodetic();
    @Nullable
    public abstract Double height();
    @Nullable
    public abstract Integer horizontalAccuracy();
    @Nullable
    public abstract Integer verticalAccuracy();
    @Nullable
    public abstract Integer barometerAccuracy();
    @Nullable
    public abstract Integer speedAccuracy();

    @JsonCreator
    public static UavVectorReport create(@JsonProperty("timestamp") DateTime timestamp,
                                         @JsonProperty("operational_status") String operationalStatus,
                                         @JsonProperty("height_type") String heightType,
                                         @JsonProperty("ground_track") Integer groundTrack,
                                         @JsonProperty("speed") Double speed,
                                         @JsonProperty("vertical_speed") Double verticalSpeed,
                                         @JsonProperty("latitude") Double latitude,
                                         @JsonProperty("longitude") Double longitude,
                                         @JsonProperty("altitude_pressure") Double altitudePressure,
                                         @JsonProperty("altitude_geodetic") Double altitudeGeodetic,
                                         @JsonProperty("height") Double height,
                                         @JsonProperty("horizontal_accuracy") Integer horizontalAccuracy,
                                         @JsonProperty("vertical_accuracy") Integer verticalAccuracy,
                                         @JsonProperty("barometer_accuracy") Integer barometerAccuracy,
                                         @JsonProperty("speed_accuracy") Integer speedAccuracy) {
        return builder()
                .timestamp(timestamp)
                .operationalStatus(operationalStatus)
                .heightType(heightType)
                .groundTrack(groundTrack)
                .speed(speed)
                .verticalSpeed(verticalSpeed)
                .latitude(latitude)
                .longitude(longitude)
                .altitudePressure(altitudePressure)
                .altitudeGeodetic(altitudeGeodetic)
                .height(height)
                .horizontalAccuracy(horizontalAccuracy)
                .verticalAccuracy(verticalAccuracy)
                .barometerAccuracy(barometerAccuracy)
                .speedAccuracy(speedAccuracy)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UavVectorReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder timestamp(DateTime timestamp);

        public abstract Builder operationalStatus(String operationalStatus);

        public abstract Builder heightType(String heightType);

        public abstract Builder groundTrack(Integer groundTrack);

        public abstract Builder speed(Double speed);

        public abstract Builder verticalSpeed(Double verticalSpeed);

        public abstract Builder latitude(Double latitude);

        public abstract Builder longitude(Double longitude);

        public abstract Builder altitudePressure(Double altitudePressure);

        public abstract Builder altitudeGeodetic(Double altitudeGeodetic);

        public abstract Builder height(Double height);

        public abstract Builder horizontalAccuracy(Integer horizontalAccuracy);

        public abstract Builder verticalAccuracy(Integer verticalAccuracy);

        public abstract Builder barometerAccuracy(Integer barometerAccuracy);

        public abstract Builder speedAccuracy(Integer speedAccuracy);

        public abstract UavVectorReport build();
    }
}
