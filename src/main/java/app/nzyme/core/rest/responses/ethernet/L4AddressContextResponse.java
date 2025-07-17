package app.nzyme.core.rest.responses.ethernet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class L4AddressContextResponse {

    // TODO: Implement L4 Address Context and then use this Response.

    @JsonProperty("implemented")
    public abstract boolean implemented();

    public static L4AddressContextResponse create() {
        return builder()
                .implemented(false)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_L4AddressContextResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder implemented(boolean implemented);

        public abstract L4AddressContextResponse build();
    }
}
