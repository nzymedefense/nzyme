package app.nzyme.core.rest.responses.dot11;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class Dot11MacAddressContextResponse {

    @Nullable
    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("description")
    public abstract String description();

    public static Dot11MacAddressContextResponse create(String name, String description) {
        return builder()
                .name(name)
                .description(description)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11MacAddressContextResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Dot11MacAddressContextResponse build();
    }
}
