package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;

@AutoValue
public abstract class CreateFloorOfTenantLocationRequest {

    @Min(0)
    public abstract long number();

    @Nullable
    public abstract String name();

    @JsonCreator
    public static CreateFloorOfTenantLocationRequest create(@JsonProperty("number") long number,
                                                            @JsonProperty("name") String name) {
        return builder()
                .number(number)
                .name(name)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CreateFloorOfTenantLocationRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder number(long number);

        public abstract Builder name(String name);

        public abstract CreateFloorOfTenantLocationRequest build();
    }
}
