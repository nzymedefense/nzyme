package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

@AutoValue
public abstract class CreateTenantLocationFloorRequest {

    @Min(0)
    public abstract long number();

    @NotEmpty
    public abstract String name();

    @JsonCreator
    public static CreateTenantLocationFloorRequest create(@JsonProperty("number") long number,
                                                          @JsonProperty("name") @NotEmpty String name) {
        return builder()
                .number(number)
                .name(name)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CreateTenantLocationFloorRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder number(@Min(0) long number);

        public abstract Builder name(@NotEmpty String name);

        public abstract CreateTenantLocationFloorRequest build();
    }
}
