package app.nzyme.core.rest.responses.locations;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class LocationEnvironmentConditionDetailsResponse {

    @JsonProperty("display_name")
    public abstract String displayName();

    @JsonProperty("severity")
    public abstract int severity();

    public static LocationEnvironmentConditionDetailsResponse create(String displayName, int severity) {
        return builder()
                .displayName(displayName)
                .severity(severity)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_LocationEnvironmentConditionDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder displayName(String displayName);

        public abstract Builder severity(int severity);

        public abstract LocationEnvironmentConditionDetailsResponse build();
    }
}
