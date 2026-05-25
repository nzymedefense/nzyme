package app.nzyme.core.rest.responses.locations;

import app.nzyme.core.rest.responses.taps.TapHighLevelInformationDetailsResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.UUID;

@AutoValue
public abstract class LocationSummaryResponse {

    @JsonProperty("id")
    public abstract UUID id();

    @JsonProperty("name")
    public abstract String name();

    @Nullable
    @JsonProperty("description")
    public abstract String description();

    @JsonProperty("tap_count")
    public abstract int tapCount();

    @JsonProperty("alert_count")
    public abstract int alertCount();

    @Nullable
    @JsonProperty("timezone")
    public abstract String timezone();

    @Nullable
    @JsonProperty("environment")
    public abstract LocationEnvironmentDataResponse environment();

    @JsonProperty("longitude")
    @Nullable
    public abstract Double longitude();

    @JsonProperty("latitude")
    @Nullable
    public abstract Double latitude();

    @JsonProperty("taps")
    public abstract List<TapHighLevelInformationDetailsResponse> taps();

    public static LocationSummaryResponse create(UUID id, String name, String description, int tapCount, int alertCount, String timezone, LocationEnvironmentDataResponse environment, Double longitude, Double latitude, List<TapHighLevelInformationDetailsResponse> taps) {
        return builder()
                .id(id)
                .name(name)
                .description(description)
                .tapCount(tapCount)
                .alertCount(alertCount)
                .timezone(timezone)
                .environment(environment)
                .longitude(longitude)
                .latitude(latitude)
                .taps(taps)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_LocationSummaryResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(UUID id);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder tapCount(int tapCount);

        public abstract Builder alertCount(int alertCount);

        public abstract Builder timezone(String timezone);

        public abstract Builder environment(LocationEnvironmentDataResponse environment);

        public abstract Builder longitude(Double longitude);

        public abstract Builder latitude(Double latitude);

        public abstract Builder taps(List<TapHighLevelInformationDetailsResponse> taps);

        public abstract LocationSummaryResponse build();
    }
}
