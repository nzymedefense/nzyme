package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

@AutoValue
public abstract class CreateFloorOfTenantLocationRequest {

    public abstract long number();

    @Nullable
    public abstract String name();

    @Min(0)
    public abstract float pathLossExponent();

    @JsonCreator
    public static CreateFloorOfTenantLocationRequest create(@JsonProperty("number") long number,
                                                            @JsonProperty("name") String name,
                                                            @JsonProperty("path_loss_exponent") float pathLossExponent) {
        return builder()
                .number(number)
                .name(name)
                .pathLossExponent(pathLossExponent)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CreateFloorOfTenantLocationRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder number(long number);

        public abstract Builder name(String name);

        public abstract Builder pathLossExponent(@NotEmpty float pathLossExponent);

        public abstract CreateFloorOfTenantLocationRequest build();
    }
}
