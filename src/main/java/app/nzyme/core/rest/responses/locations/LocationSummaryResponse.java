package app.nzyme.core.rest.responses.locations;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

import java.util.UUID;

@AutoValue
public abstract class LocationSummaryResponse {

    @JsonProperty("id")
    public abstract UUID id();

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("tap_count")
    public abstract int tapCount();

    @JsonProperty("alert_count")
    public abstract int alertCount();

    @Nullable
    @JsonProperty("environment")
    public abstract LocationEnvironmentDataResponse environment();

    public static LocationSummaryResponse create(UUID id, String name, int tapCount, int alertCount, LocationEnvironmentDataResponse environment) {
        return builder()
                .id(id)
                .name(name)
                .tapCount(tapCount)
                .alertCount(alertCount)
                .environment(environment)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_LocationSummaryResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(UUID id);

        public abstract Builder name(String name);

        public abstract Builder tapCount(int tapCount);

        public abstract Builder alertCount(int alertCount);

        public abstract Builder environment(LocationEnvironmentDataResponse environment);

        public abstract LocationSummaryResponse build();
    }
}
