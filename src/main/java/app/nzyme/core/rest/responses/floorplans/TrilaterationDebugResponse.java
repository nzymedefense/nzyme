package app.nzyme.core.rest.responses.floorplans;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Map;
import java.util.UUID;

@AutoValue
public abstract class TrilaterationDebugResponse {

    @JsonProperty("tap_distances")
    public abstract Map<UUID, Double> tapDistances();

    public static TrilaterationDebugResponse create(Map<UUID, Double> tapDistances) {
        return builder()
                .tapDistances(tapDistances)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TrilaterationDebugResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder tapDistances(Map<UUID, Double> tapDistances);

        public abstract TrilaterationDebugResponse build();
    }
}
