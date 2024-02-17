package app.nzyme.core.rest.responses.floorplans;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TrilaterationLocationResponse {

    @JsonProperty("x")
    public abstract int x();

    @JsonProperty("y")
    public abstract int y();

    public static TrilaterationLocationResponse create(int x, int y) {
        return builder()
                .x(x)
                .y(y)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TrilaterationLocationResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder x(int x);

        public abstract Builder y(int y);

        public abstract TrilaterationLocationResponse build();
    }
}
