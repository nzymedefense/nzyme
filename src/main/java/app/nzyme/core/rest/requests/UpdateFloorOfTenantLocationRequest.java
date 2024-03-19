package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;

@AutoValue
public abstract class UpdateFloorOfTenantLocationRequest {

    @Min(0)
    public abstract long number();

    @Nullable
    public abstract String name();

    @Min(0)
    public abstract float pathLossExponent();

    @JsonCreator
    public static UpdateFloorOfTenantLocationRequest create(@JsonProperty("number") long number,
                                                            @JsonProperty("name") String name,
                                                            @JsonProperty("path_loss_exponent") float pathLossExponent) {
        return builder()
                .number(number)
                .name(name)
                .pathLossExponent(pathLossExponent)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UpdateFloorOfTenantLocationRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder number(@Min(0) long number);

        public abstract Builder name(String name);

        public abstract Builder pathLossExponent(@Min(0) float pathLossExponent);

        public abstract UpdateFloorOfTenantLocationRequest build();
    }
}
