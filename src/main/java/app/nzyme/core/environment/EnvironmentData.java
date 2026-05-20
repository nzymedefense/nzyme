package app.nzyme.core.environment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

import java.util.List;

@AutoValue
public abstract class EnvironmentData {

    @Nullable
    public abstract String stationId();

    @Nullable
    public abstract Integer temperature();

    @Nullable
    public abstract Integer windDirection();

    @Nullable
    public abstract Integer windSpeed();

    @Nullable
    public abstract Integer windGust();

    @Nullable
    public abstract Integer visibility();

    public abstract List<LocationEnvironmentAlertDetails> alerts();

    @JsonCreator
    public static EnvironmentData create(@JsonProperty("station_id") String stationId,
                                         @JsonProperty("temperature") Integer temperature,
                                         @JsonProperty("wind_direction") Integer windDirection,
                                         @JsonProperty("wind_speed") Integer windSpeed,
                                         @JsonProperty("wind_gust") Integer windGust,
                                         @JsonProperty("visibility") Integer visibility,
                                         @JsonProperty("alerts") List<LocationEnvironmentAlertDetails> alerts) {
        return builder()
                .stationId(stationId)
                .temperature(temperature)
                .windDirection(windDirection)
                .windSpeed(windSpeed)
                .windGust(windGust)
                .visibility(visibility)
                .alerts(alerts)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_EnvironmentData.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder stationId(String stationId);

        public abstract Builder temperature(Integer temperature);

        public abstract Builder windDirection(Integer windDirection);

        public abstract Builder windSpeed(Integer windSpeed);

        public abstract Builder windGust(Integer windGust);

        public abstract Builder visibility(Integer visibility);

        public abstract Builder alerts(List<LocationEnvironmentAlertDetails> alerts);

        public abstract EnvironmentData build();
    }
}
