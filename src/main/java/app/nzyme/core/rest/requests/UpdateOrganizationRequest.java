package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.validation.constraints.NotEmpty;

@AutoValue
public abstract class UpdateOrganizationRequest {

    @NotEmpty
    public abstract String name();

    @NotEmpty
    public abstract String description();

    @JsonCreator
    public static UpdateOrganizationRequest create(@JsonProperty("name") String name,
                                                   @JsonProperty("description") String description) {
        return builder()
                .name(name)
                .description(description)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UpdateOrganizationRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract UpdateOrganizationRequest build();
    }
}
