package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.validation.constraints.NotBlank;

@AutoValue
public abstract class UpdateMacAddressContextNameRequest {

    @NotBlank
    public abstract String name();

    @JsonCreator
    public static UpdateMacAddressContextNameRequest create(@JsonProperty("name") String name) {
        return builder()
                .name(name)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UpdateMacAddressContextNameRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(@NotBlank String name);

        public abstract UpdateMacAddressContextNameRequest build();
    }
}
