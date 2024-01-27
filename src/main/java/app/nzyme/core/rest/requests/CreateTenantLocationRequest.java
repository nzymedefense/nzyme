package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;

@AutoValue
public abstract class CreateTenantLocationRequest {

    @NotEmpty
    public abstract String name();

    @Nullable
    public abstract String description();

    @JsonCreator
    public static CreateTenantLocationRequest create(@JsonProperty("name") String name,
                                                     @JsonProperty("description") String description) {
        return builder()
                .name(name)
                .description(description)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CreateTenantLocationRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(@NotEmpty String name);

        public abstract Builder description(@NotEmpty String description);

        public abstract CreateTenantLocationRequest build();
    }
}
