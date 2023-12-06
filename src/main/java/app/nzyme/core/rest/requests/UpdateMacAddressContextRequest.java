package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@AutoValue
public abstract class UpdateMacAddressContextRequest {

    @NotEmpty @Size(max = 12)
    public abstract String name();

    @Nullable
    @Size(max = 32)
    public abstract String description();

    @Nullable
    public abstract String notes();

    @JsonCreator
    public static UpdateMacAddressContextRequest create(@JsonProperty("name") String name,
                                                        @JsonProperty("description") String description,
                                                        @JsonProperty("notes") String notes) {
        return builder()
                .name(name)
                .description(description)
                .notes(notes)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UpdateMacAddressContextRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(@NotEmpty @Size(max = 12) String name);

        public abstract Builder description(@Size(max = 32) String description);

        public abstract Builder notes(String notes);

        public abstract UpdateMacAddressContextRequest build();
    }

}
