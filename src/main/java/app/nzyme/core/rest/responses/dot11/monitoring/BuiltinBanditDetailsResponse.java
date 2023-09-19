package app.nzyme.core.rest.responses.dot11.monitoring;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class BuiltinBanditDetailsResponse {

    @JsonProperty("id")
    public abstract String id();

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("description")
    public abstract String description();

    @JsonProperty("fingerprints")
    public abstract List<String> fingerprints();

    public static BuiltinBanditDetailsResponse create(String id, String name, String description, List<String> fingerprints) {
        return builder()
                .id(id)
                .name(name)
                .description(description)
                .fingerprints(fingerprints)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BuiltinBanditDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(String id);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder fingerprints(List<String> fingerprints);

        public abstract BuiltinBanditDetailsResponse build();
    }
}
