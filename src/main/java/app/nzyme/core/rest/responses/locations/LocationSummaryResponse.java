package app.nzyme.core.rest.responses.locations;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.UUID;

@AutoValue
public abstract class LocationSummaryResponse {

    @JsonProperty("id")
    public abstract UUID id();
    @JsonProperty("name")
    public abstract String name();

    public static LocationSummaryResponse create(UUID id, String name) {
        return builder()
                .id(id)
                .name(name)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_LocationSummaryResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(UUID id);

        public abstract Builder name(String name);

        public abstract LocationSummaryResponse build();
    }
}
