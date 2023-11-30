package app.nzyme.core.rest.responses.dot11;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Dot11MacAddressContextResponse {

    @JsonProperty("name")
    public abstract String name();

    public static Dot11MacAddressContextResponse create(String name) {
        return builder()
                .name(name)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11MacAddressContextResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String name);

        public abstract Dot11MacAddressContextResponse build();
    }
}
