package app.nzyme.core.rest.responses.ethernet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class EthernetMacAddressContextResponse {

    @Nullable
    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("description")
    public abstract String description();

    public static EthernetMacAddressContextResponse create(String name, String description) {
        return builder()
                .name(name)
                .description(description)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_EthernetMacAddressContextResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract EthernetMacAddressContextResponse build();
    }
}
