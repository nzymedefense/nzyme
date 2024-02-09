package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.validation.constraints.Min;

@AutoValue
public abstract class PlaceTapRequest {

    @JsonProperty("x")
    @Min(0)
    public abstract int x();

    @JsonProperty("y")
    @Min(0)
    public abstract int y();

    @JsonCreator
    public static PlaceTapRequest create(@JsonProperty("x") int x,
                                         @JsonProperty("y") int y) {
        return builder()
                .x(x)
                .y(y)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_PlaceTapRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder x(int x);

        public abstract Builder y(int y);

        public abstract PlaceTapRequest build();
    }
}
