package app.nzyme.core.rest.responses.dot11.monitoring;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SupportedBanditResponse {

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("description")
    public abstract String description();

    public static SupportedBanditResponse create(String name, String description) {
        return builder()
                .name(name)
                .description(description)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SupportedBanditResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract SupportedBanditResponse build();
    }
}
