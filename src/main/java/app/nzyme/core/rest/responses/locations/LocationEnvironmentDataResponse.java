package app.nzyme.core.rest.responses.locations;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

import java.util.List;

@AutoValue
public abstract class LocationEnvironmentDataResponse {

    @Nullable
    @JsonProperty("station_id")
    public abstract String stationId();

    @Nullable
    @JsonProperty("metar")
    public abstract String metar();

    @Nullable
    @JsonProperty("condition")
    public abstract LocationEnvironmentConditionDetailsResponse condition();

    @Nullable
    @JsonProperty("temperature")
    public abstract Integer temperature();

    @Nullable
    @JsonProperty("wind_direction")
    public abstract Integer windDirection();

    @Nullable
    @JsonProperty("wind_speed")
    public abstract Integer windSpeed();

    @Nullable
    @JsonProperty("wind_gust")
    public abstract Integer windGust();

    @Nullable
    @JsonProperty("visibility")
    public abstract Integer visibility();

    @JsonProperty("alerts")
    public abstract List<LocationEnvironmentAlertDetailsResponse> alerts();

    public static LocationEnvironmentDataResponse create(String stationId, String metar, LocationEnvironmentConditionDetailsResponse condition, Integer temperature, Integer windDirection, Integer windSpeed, Integer windGust, Integer visibility, List<LocationEnvironmentAlertDetailsResponse> alerts) {
        return builder()
                .stationId(stationId)
                .metar(metar)
                .condition(condition)
                .temperature(temperature)
                .windDirection(windDirection)
                .windSpeed(windSpeed)
                .windGust(windGust)
                .visibility(visibility)
                .alerts(alerts)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_LocationEnvironmentDataResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder stationId(String stationId);

        public abstract Builder metar(String metar);

        public abstract Builder condition(LocationEnvironmentConditionDetailsResponse condition);

        public abstract Builder temperature(Integer temperature);

        public abstract Builder windDirection(Integer windDirection);

        public abstract Builder windSpeed(Integer windSpeed);

        public abstract Builder windGust(Integer windGust);

        public abstract Builder visibility(Integer visibility);

        public abstract Builder alerts(List<LocationEnvironmentAlertDetailsResponse> alerts);

        public abstract LocationEnvironmentDataResponse build();
    }
}
