package app.nzyme.core.rest.responses.shared;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class HistogramValueStructureResponse {

    @JsonProperty("value")
    @Nullable
    public abstract Object value();

    @JsonProperty("type")
    public abstract HistogramValueType valueType();

    @JsonProperty("metadata")
    @Nullable
    public abstract Object metadata();

    public static HistogramValueStructureResponse create(Object value, HistogramValueType valueType, Object metadata) {
        return builder()
                .value(value)
                .valueType(valueType)
                .metadata(metadata)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_HistogramValueStructureResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder value(Object value);

        public abstract Builder valueType(HistogramValueType valueType);

        public abstract Builder metadata(Object metadata);

        public abstract HistogramValueStructureResponse build();
    }
}
