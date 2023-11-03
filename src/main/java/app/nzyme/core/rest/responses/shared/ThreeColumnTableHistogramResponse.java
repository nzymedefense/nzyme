package app.nzyme.core.rest.responses.shared;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class ThreeColumnTableHistogramResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("values")
    public abstract List<ThreeColumnTableHistogramValueResponse> values();

    public static ThreeColumnTableHistogramResponse create(long total, List<ThreeColumnTableHistogramValueResponse> values) {
        return builder()
                .total(total)
                .values(values)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ThreeColumnTableHistogramResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder values(List<ThreeColumnTableHistogramValueResponse> values);

        public abstract ThreeColumnTableHistogramResponse build();
    }
}
