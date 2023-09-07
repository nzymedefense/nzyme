package app.nzyme.core.rest.responses.dot11.monitoring;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;
import java.util.UUID;

@AutoValue
public abstract class CustomBanditDetailsResponse {

    @JsonProperty("id")
    public abstract UUID id();

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("description")
    public abstract String description();

    @JsonProperty("fingerprints")
    public abstract List<String> fingerprints();

    public static CustomBanditDetailsResponse create(UUID id, String name, String description, List<String> fingerprints) {
        return builder()
                .id(id)
                .name(name)
                .description(description)
                .fingerprints(fingerprints)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CustomBanditDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(UUID id);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder fingerprints(List<String> fingerprints);

        public abstract CustomBanditDetailsResponse build();
    }
}
