package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;

@AutoValue
public abstract class UpdateTenantLocationRequest {

    @NotEmpty
    public abstract String name();

    @Nullable
    public abstract String description();

    @JsonCreator
    public static UpdateTenantLocationRequest create(@JsonProperty("name") String name,
                                                     @JsonProperty("description") String description) {
        return builder()
                .name(name)
                .description(description)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UpdateTenantLocationRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(@NotEmpty String name);

        public abstract Builder description(String description);

        public abstract UpdateTenantLocationRequest build();
    }
}
