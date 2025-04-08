package app.nzyme.core.rest.responses.uav;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

@AutoValue
public abstract class UavVectorDetailsResponse {

    @JsonProperty("timestamp")
    public abstract DateTime timestamp();

    @JsonProperty("latitude")
    public abstract Double latitude();

    @JsonProperty("longitude")
    public abstract Double longitude();

    @Nullable
    @JsonProperty("operational_status")
    public abstract String operationalStatus();
    @Nullable
    @JsonProperty("ground_track")
    public abstract Integer groundTrack();
    @Nullable
    @JsonProperty("speed")
    public abstract Double speed();
    @Nullable
    @JsonProperty("vertical_speed")
    public abstract Double verticalSpeed();
    @Nullable
    @JsonProperty("altitude_pressure")
    public abstract Double altitudePressure();
    @Nullable
    @JsonProperty("altitude_geodetic")
    public abstract Double altitudeGeodetic();
    @Nullable
    @JsonProperty("height_type")
    public abstract String heightType();
    @Nullable
    @JsonProperty("height")
    public abstract Double height();
    @Nullable
    @JsonProperty("accuracy_horizontal")
    public abstract Integer accuracyHorizontal();
    @Nullable
    @JsonProperty("accuracy_vertical")
    public abstract Integer accuracyVertical();
    @Nullable
    @JsonProperty("accuracy_barometer")
    public abstract Integer accuracyBarometer();
    @Nullable
    @JsonProperty("accuracy_speed")
    public abstract Integer accuracySpeed();

    public static UavVectorDetailsResponse create(DateTime timestamp, Double latitude, Double longitude, String operationalStatus, Integer groundTrack, Double speed, Double verticalSpeed, Double altitudePressure, Double altitudeGeodetic, String heightType, Double height, Integer accuracyHorizontal, Integer accuracyVertical, Integer accuracyBarometer, Integer accuracySpeed) {
        return builder()
                .timestamp(timestamp)
                .latitude(latitude)
                .longitude(longitude)
                .operationalStatus(operationalStatus)
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
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UavVectorDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder timestamp(DateTime timestamp);

        public abstract Builder latitude(Double latitude);

        public abstract Builder longitude(Double longitude);

        public abstract Builder operationalStatus(String operationalStatus);

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

        public abstract UavVectorDetailsResponse build();
    }
}
